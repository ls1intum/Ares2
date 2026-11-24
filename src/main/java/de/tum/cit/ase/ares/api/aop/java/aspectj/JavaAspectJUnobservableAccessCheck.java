package de.tum.cit.ase.ares.api.aop.java.aspectj;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnitReference;
import com.tngtech.archunit.core.domain.Source;

import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

import de.tum.cit.ase.ares.api.architecture.java.ForbiddenMethodMatcher;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Refuses, in an AspectJ run, the ways of reaching a checked method that the
 * woven aspects cannot see: a method or constructor reference, a call through
 * {@code super}, and a constructor reached through {@code super(...)}. AspectJ
 * checks only calls it finds in the student's compiled code, so these would run
 * unchecked once the policy grants anything in that area.
 */
public final class JavaAspectJUnobservableAccessCheck {

	/**
	 * Simple names of the class files that describe a package or a module. They
	 * hold no code that could run, so they are not checked.
	 */
	@Nonnull
	private static final Set<String> DESCRIPTOR_NAMES = Set.of("package-info", "module-info");

	/** Prevents creating instances of this utility class. */
	private JavaAspectJUnobservableAccessCheck() {
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "JavaAspectJUnobservableAccessCheck"));
	}

	/**
	 * Refuses the first construct found that reaches a method of one of the given
	 * lists in a way AspectJ cannot check.
	 *
	 * @param supervisedClasses  the classes read from the supervised class path
	 * @param exemptedClassNames the exact names of the exempted classes
	 * @param referenceLists     the lists whose methods may not be referenced or
	 *                           called through {@code super}
	 * @param constructorLists   the lists whose constructors may not be reached
	 *                           through {@code super(...)}
	 * @throws SecurityException naming the class and the method it reaches
	 */
	public static void requireObservable(@Nonnull JavaClasses supervisedClasses,
			@Nonnull Collection<String> exemptedClassNames, @Nonnull Collection<Path> referenceLists,
			@Nonnull Collection<Path> constructorLists) {
		if (referenceLists.isEmpty() && constructorLists.isEmpty()) {
			return;
		}
		Set<String> referenced = methodsOf(referenceLists);
		Set<String> constructed = methodsOf(constructorLists);
		for (JavaClass javaClass : supervisedClasses) {
			if (DESCRIPTOR_NAMES.contains(javaClass.getSimpleName())
					|| exemptedClassNames.contains(javaClass.getName())) {
				continue;
			}
			refuseReferences(javaClass, supervisedClasses, referenced);
			refuseSuperCalls(javaClass, supervisedClasses, referenced, constructed);
		}
	}

	/**
	 * Reads the methods of the given lists.
	 *
	 * @param lists the method lists
	 * @return their methods, canonical
	 */
	@Nonnull
	private static Set<String> methodsOf(@Nonnull Collection<Path> lists) {
		return lists.stream().flatMap(list -> ForbiddenMethodMatcher.effectiveMethods(list).stream())
				.collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * Refuses a method or constructor reference of a class to a listed method.
	 *
	 * @param javaClass  the class to look at
	 * @param classes    all supervised classes, to follow the type hierarchy
	 * @param referenced the listed methods
	 * @throws SecurityException for the first such reference
	 */
	private static void refuseReferences(@Nonnull JavaClass javaClass, @Nonnull JavaClasses classes,
			@Nonnull Set<String> referenced) {
		for (JavaCodeUnitReference<?> reference : javaClass.getCodeUnitReferencesFromSelf()) {
			AccessTarget.CodeUnitReferenceTarget target = reference.getTarget();
			String parameters = target.getRawParameterTypes().stream().map(JavaClass::getName)
					.collect(Collectors.joining(", ", "(", ")"));
			Optional<String> listed = firstListed(target.getOwner().getName(), target.getName(), parameters, classes,
					referenced);
			if (listed.isPresent()) {
				throw new SecurityException(Messages.localized("security.aspectj.unobservable.reference",
						javaClass.getName(), listed.get()));
			}
		}
	}

	/**
	 * Refuses a call of a class through {@code super} to a listed method, and a
	 * {@code super(...)} constructor call with arguments to a listed constructor. A
	 * constructor without arguments opens nothing it could be told to open.
	 *
	 * @param javaClass   the class to look at
	 * @param classes     all supervised classes, to follow the type hierarchy
	 * @param referenced  the methods that may not be called through {@code super}
	 * @param constructed the constructors that may not be reached through
	 *                    {@code super(...)}
	 * @throws SecurityException for the first such call
	 */
	private static void refuseSuperCalls(@Nonnull JavaClass javaClass, @Nonnull JavaClasses classes,
			@Nonnull Set<String> referenced, @Nonnull Set<String> constructed) {
		for (SuperCall call : superCallsOf(javaClass)) {
			boolean constructor = "<init>".equals(call.name());
			if (constructor && "()".equals(call.descriptorParameters())) {
				continue;
			}
			Optional<String> listed = firstListed(call.owner(), call.name(), call.descriptorParameters(), classes,
					constructor ? constructed : referenced);
			if (listed.isPresent()) {
				throw new SecurityException(
						Messages.localized(
								constructor ? "security.aspectj.unobservable.super.constructor"
										: "security.aspectj.unobservable.super.call",
								javaClass.getName(), listed.get()));
			}
		}
	}

	/**
	 * Returns the first listed method that the named method is, on its own class or
	 * on any class or interface above it among the supervised classes.
	 *
	 * @param ownerName  the binary name of the class the code names
	 * @param name       the method name
	 * @param parameters the parameter list, in source or descriptor form
	 * @param classes    all supervised classes
	 * @param listed     the listed methods
	 * @return the matching listed signature, if any
	 */
	@Nonnull
	private static Optional<String> firstListed(@Nonnull String ownerName, @Nonnull String name,
			@Nonnull String parameters, @Nonnull JavaClasses classes, @Nonnull Set<String> listed) {
		if (listed.isEmpty()) {
			return Optional.empty();
		}
		return ownerAndSupertypes(ownerName, classes).map(owner -> owner + "." + name + parameters)
				.map(ForbiddenMethodMatcher::canonicalise).filter(signature -> isListed(signature, listed)).findFirst();
	}

	/**
	 * Returns a class name and, if the class is supervised, the names of every
	 * class and interface above it.
	 *
	 * @param ownerName the binary name of the class
	 * @param classes   all supervised classes
	 * @return the names, the class itself first
	 */
	@Nonnull
	private static Stream<String> ownerAndSupertypes(@Nonnull String ownerName, @Nonnull JavaClasses classes) {
		if (!classes.contain(ownerName)) {
			return Stream.of(ownerName);
		}
		JavaClass owner = classes.get(ownerName);
		Set<String> names = new LinkedHashSet<>();
		names.add(ownerName);
		owner.getAllRawSuperclasses().forEach(type -> names.add(type.getName()));
		owner.getAllRawInterfaces().forEach(type -> names.add(type.getName()));
		return names.stream();
	}

	/**
	 * Whether a canonical signature is listed, exactly or under an entry without a
	 * parameter list, as the architecture rules read the lists.
	 *
	 * @param signature the canonical signature
	 * @param listed    the listed methods
	 * @return {@code true} if it is listed
	 */
	private static boolean isListed(@Nonnull String signature, @Nonnull Set<String> listed) {
		return listed.contains(signature) || listed.stream().filter(entry -> entry.indexOf('(') < 0)
				.anyMatch(entry -> ForbiddenMethodMatcher.matches(signature, entry));
	}

	/**
	 * Reads from the class file the calls a class makes through {@code super}: an
	 * invokespecial of another class's method, and a constructor of the superclass
	 * called more often than objects of that class are created.
	 *
	 * @param javaClass the class to read
	 * @return the calls, or every call an unreadable class file cannot rule out
	 * @throws SecurityException if the class file cannot be read
	 */
	@Nonnull
	static List<SuperCall> superCallsOf(@Nonnull JavaClass javaClass) {
		Optional<URI> classFile = javaClass.getSource().map(Source::getUri);
		if (classFile.isEmpty()) {
			throw new SecurityException(
					Messages.localized("security.architecture.class.file.missing", javaClass.getName()));
		}
		try (InputStream input = classFile.get().toURL().openStream()) {
			return superCallsOf(input.readAllBytes());
		} catch (IOException | RuntimeException unreadable) {
			throw new SecurityException(
					Messages.localized("security.architecture.class.file.missing", javaClass.getName()), unreadable);
		}
	}

	/**
	 * Reads the calls through {@code super} from the bytes of a class file.
	 *
	 * @param classFile the bytes of the class file
	 * @return the calls
	 */
	@Nonnull
	static List<SuperCall> superCallsOf(@Nonnull byte[] classFile) {
		SuperCallCollector collector = new SuperCallCollector();
		new ClassReader(classFile).accept(collector, ClassReader.SKIP_FRAMES);
		return List.copyOf(collector.calls);
	}

	/**
	 * One call through {@code super}.
	 *
	 * @param owner                the binary name of the class the call names
	 * @param name                 the method name, {@code <init>} for a constructor
	 * @param descriptorParameters the parameter list in descriptor form
	 */
	record SuperCall(@Nonnull String owner, @Nonnull String name, @Nonnull String descriptorParameters) {
	}

	/** Collects the calls through {@code super} while ASM reads a class file. */
	private static final class SuperCallCollector extends ClassVisitor {

		/** The calls found so far. */
		@Nonnull
		private final List<SuperCall> calls = new ArrayList<>();

		/** The internal name of the class being read. */
		@Nullable
		private String self;

		/** The internal name of its superclass. */
		@Nullable
		private String superName;

		/** Creates a collector for the ASM version Byte Buddy ships. */
		SuperCallCollector() {
			super(Opcodes.ASM9);
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			this.self = name;
			this.superName = superName;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
				String[] exceptions) {
			return new SuperCallMethodVisitor(name, descriptor);
		}

		/** Records the calls through {@code super} of one method. */
		private final class SuperCallMethodVisitor extends MethodVisitor {

			/** The name of the method being read. */
			@Nonnull
			private final String methodName;

			/** The descriptor of the method being read. */
			@Nonnull
			private final String methodDescriptor;

			/** How many objects of the superclass the method creates with new. */
			private int superclassObjectsCreated;

			/** The superclass constructors the method calls, by descriptor. */
			@Nonnull
			private final List<String> superclassConstructors = new ArrayList<>();

			/**
			 * Creates the visitor for one method.
			 *
			 * @param methodName       the name of the method
			 * @param methodDescriptor the descriptor of the method
			 */
			SuperCallMethodVisitor(@Nonnull String methodName, @Nonnull String methodDescriptor) {
				super(Opcodes.ASM9);
				this.methodName = methodName;
				this.methodDescriptor = methodDescriptor;
			}

			/**
			 * Whether a call through super is the method handing on to the method it
			 * overrides. AspectJ sees every call of the override, so it sees this one.
			 *
			 * @param name       the called method's name
			 * @param descriptor the called method's descriptor
			 * @return {@code true} for such a hand-on
			 */
			private boolean isOverrideHandingOn(@Nonnull String name, @Nonnull String descriptor) {
				return name.equals(methodName) && descriptor.equals(methodDescriptor);
			}

			@Override
			public void visitTypeInsn(int opcode, String type) {
				if (opcode == Opcodes.NEW && type.equals(superName)) {
					superclassObjectsCreated++;
				}
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
				if (opcode != Opcodes.INVOKESPECIAL || owner.equals(self)) {
					return;
				}
				if (!"<init>".equals(name)) {
					if (!isOverrideHandingOn(name, descriptor)) {
						calls.add(new SuperCall(owner.replace('/', '.'), name, parametersOf(descriptor)));
					}
				} else if ("<init>".equals(methodName) && owner.equals(superName)) {
					superclassConstructors.add(descriptor);
				}
			}

			@Override
			public void visitEnd() {
				if (superclassConstructors.size() > superclassObjectsCreated) {
					superclassConstructors.forEach(descriptor -> calls
							.add(new SuperCall(superName.replace('/', '.'), "<init>", parametersOf(descriptor))));
				}
			}
		}
	}

	/**
	 * Returns the parameter part of a method descriptor, such as
	 * {@code (Ljava/lang/String;I)}.
	 *
	 * @param descriptor the method descriptor
	 * @return its parameter part
	 */
	@Nonnull
	private static String parametersOf(@Nonnull String descriptor) {
		return descriptor.substring(0, descriptor.indexOf(')') + 1);
	}
}
