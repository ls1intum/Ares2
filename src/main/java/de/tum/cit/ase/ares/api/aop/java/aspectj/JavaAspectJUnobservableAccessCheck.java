package de.tum.cit.ase.ares.api.aop.java.aspectj;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnitReference;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.Source;

import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.Handle;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;

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

	/**
	 * Listed classes whose constructor without arguments opens nothing: a reader or
	 * writer without a source, and a socket not yet connected, whose connect is a
	 * call AspectJ checks. Any other listed constructor without arguments, such as
	 * the one of FileHandler that opens the configured log file, is refused through
	 * super() too.
	 */
	@Nonnull
	private static final Set<String> INERT_WITHOUT_ARGUMENTS = Set.of("java.io.Reader", "java.io.Writer",
			"java.net.Socket");

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
		Hierarchy hierarchy = new Hierarchy(supervisedClasses);
		for (JavaClass javaClass : supervisedClasses) {
			if (DESCRIPTOR_NAMES.contains(javaClass.getSimpleName())
					|| exemptedClassNames.contains(javaClass.getName())) {
				continue;
			}
			refuseReferences(javaClass, hierarchy, referenced);
			refuseSuperCalls(javaClass, hierarchy, referenced, constructed);
			refuseInheritedImplementations(javaClass, hierarchy, referenced);
		}
	}

	/**
	 * Refuses a class that lets a listed method it inherits from a class Ares does
	 * not supervise stand for a method of a supervised interface. AspectJ checks a
	 * call by the type the calling code names, so a call through that interface
	 * would reach the listed method unchecked.
	 *
	 * @param javaClass  the class to look at
	 * @param hierarchy  the type hierarchy of this run
	 * @param referenced the listed methods
	 * @throws SecurityException for the first such method
	 */
	private static void refuseInheritedImplementations(@Nonnull JavaClass javaClass, @Nonnull Hierarchy hierarchy,
			@Nonnull Set<String> referenced) {
		if (javaClass.isInterface()) {
			return;
		}
		for (String typeName : hierarchy.ownerAndSupertypes(javaClass.getName())) {
			for (JavaMethod method : hierarchy.instanceMethodsOfSupervisedInterface(typeName)) {
				List<String> parameters = method.getRawParameterTypes().stream().map(JavaClass::getName).toList();
				Optional<String> listed = firstListed(javaClass.getName(), method.getName(), parameters, hierarchy,
						referenced);
				if (listed.isPresent()) {
					throw new SecurityException(Messages.localized("security.aspectj.unobservable.inherited",
							javaClass.getName(), listed.get(), typeName));
				}
			}
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
	 * @param hierarchy  the type hierarchy of this run
	 * @param referenced the listed methods
	 * @throws SecurityException for the first such reference
	 */
	private static void refuseReferences(@Nonnull JavaClass javaClass, @Nonnull Hierarchy hierarchy,
			@Nonnull Set<String> referenced) {
		for (JavaCodeUnitReference<?> reference : javaClass.getCodeUnitReferencesFromSelf()) {
			AccessTarget.CodeUnitReferenceTarget target = reference.getTarget();
			List<String> parameters = target.getRawParameterTypes().stream().map(JavaClass::getName).toList();
			Optional<String> listed = firstListed(target.getOwner().getName(), target.getName(), parameters, hierarchy,
					referenced);
			if (listed.isPresent()) {
				throw new SecurityException(Messages.localized("security.aspectj.unobservable.reference",
						javaClass.getName(), listed.get()));
			}
		}
	}

	/**
	 * Refuses a call of a class through {@code super} to a listed method, and a
	 * {@code super(...)} call to a listed constructor, unless it is one without
	 * arguments that opens nothing, see {@link #opensNothing}.
	 *
	 * @param javaClass   the class to look at
	 * @param hierarchy   the type hierarchy of this run
	 * @param referenced  the methods that may not be called through {@code super}
	 * @param constructed the constructors that may not be reached through
	 *                    {@code super(...)}
	 * @throws SecurityException for the first such call
	 */
	private static void refuseSuperCalls(@Nonnull JavaClass javaClass, @Nonnull Hierarchy hierarchy,
			@Nonnull Set<String> referenced, @Nonnull Set<String> constructed) {
		for (SuperCall call : hierarchy.callsOf(javaClass).superCalls()) {
			boolean constructor = "<init>".equals(call.name());
			if (constructor && call.parameterTypes().isEmpty() && opensNothing(call.owner(), hierarchy)) {
				continue;
			}
			Optional<String> listed = firstListed(call.owner(), call.name(), call.parameterTypes(), hierarchy,
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
	 * Whether the constructor without arguments of a class opens nothing: one of
	 * {@link #INERT_WITHOUT_ARGUMENTS}, or an exception, which holds only a
	 * message.
	 *
	 * @param ownerName the binary name of the class
	 * @param hierarchy the type hierarchy of this run
	 * @return {@code true} if a super() call to it needs no check
	 */
	private static boolean opensNothing(@Nonnull String ownerName, @Nonnull Hierarchy hierarchy) {
		return INERT_WITHOUT_ARGUMENTS.contains(ownerName)
				|| hierarchy.ownerAndSupertypes(ownerName).contains("java.lang.Throwable");
	}

	/**
	 * Returns the first listed method the named method is, looking at its own class
	 * and then at every class and interface above it. A supervised class declaring
	 * it on the way, such as the method holding a lambda's body, ends the search:
	 * its code is woven and checked. A declaration that only hands on to the method
	 * it overrides through {@code super} does not. A constructor is looked up in
	 * its own class only.
	 *
	 * @param ownerName      the binary name of the class the code names
	 * @param name           the method name
	 * @param parameterTypes the binary names of the parameter types
	 * @param hierarchy      the type hierarchy of this run
	 * @param listed         the listed methods
	 * @return the matching listed signature, if any
	 */
	@Nonnull
	private static Optional<String> firstListed(@Nonnull String ownerName, @Nonnull String name,
			@Nonnull List<String> parameterTypes, @Nonnull Hierarchy hierarchy, @Nonnull Set<String> listed) {
		if (listed.isEmpty()) {
			return Optional.empty();
		}
		List<String> typeNames = "<init>".equals(name) ? List.of(ownerName) : hierarchy.ownerAndSupertypes(ownerName);
		for (String typeName : typeNames) {
			if (hierarchy.runsOwnCode(typeName, name, parameterTypes)) {
				return Optional.empty();
			}
			String signature = ForbiddenMethodMatcher.canonicalise(typeName + "." + methodKey(name, parameterTypes));
			if (isListed(signature, listed)) {
				return Optional.of(signature);
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns a method name followed by its parameter list, as the method lists
	 * write it: {@code list(int, java.lang.String)}.
	 *
	 * @param name           the method name
	 * @param parameterTypes the binary names of the parameter types
	 * @return the name with its parameter list
	 */
	@Nonnull
	private static String methodKey(@Nonnull String name, @Nonnull List<String> parameterTypes) {
		return name + "(" + String.join(", ", parameterTypes) + ")";
	}

	/**
	 * Whether a class declares a method or constructor of the given name and
	 * parameter types itself, with a body. An abstract declaration runs no code of
	 * its own, so it does not count.
	 *
	 * @param javaClass      the class
	 * @param name           the name, {@code <init>} for a constructor
	 * @param parameterTypes the binary names of the parameter types
	 * @return {@code true} if it declares one with a body
	 */
	private static boolean declares(@Nonnull JavaClass javaClass, @Nonnull String name,
			@Nonnull List<String> parameterTypes) {
		return javaClass.getCodeUnits().stream().anyMatch(unit -> unit.getName().equals(name)
				&& !unit.getModifiers().contains(JavaModifier.ABSTRACT)
				&& unit.getRawParameterTypes().stream().map(JavaClass::getName).toList().equals(parameterTypes));
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
	 * invokespecial of another class's method, and a superclass constructor called
	 * on the object being built rather than on one created with {@code new}.
	 *
	 * @param javaClass the class to read
	 * @return the calls
	 * @throws SecurityException if the class file cannot be read
	 */
	@Nonnull
	static ClassCalls callsOf(@Nonnull JavaClass javaClass) {
		Optional<URI> classFile = javaClass.getSource().map(Source::getUri);
		if (classFile.isEmpty()) {
			throw new SecurityException(
					Messages.localized("security.architecture.class.file.missing", javaClass.getName()));
		}
		try (InputStream input = classFile.get().toURL().openStream()) {
			return callsOf(input.readAllBytes());
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
	static ClassCalls callsOf(@Nonnull byte[] classFile) {
		SuperCallCollector collector = new SuperCallCollector();
		new ClassReader(classFile).accept(collector, ClassReader.SKIP_FRAMES);
		return new ClassCalls(List.copyOf(collector.calls), Set.copyOf(collector.handOns));
	}

	/**
	 * What one class file does through {@code super}.
	 *
	 * @param superCalls the calls through {@code super} the check looks at
	 * @param handOns    the methods, as {@link #methodKey}, whose override only
	 *                   hands on to the overridden method through {@code super}
	 */
	record ClassCalls(@Nonnull List<SuperCall> superCalls, @Nonnull Set<String> handOns) {
	}

	/**
	 * The direct supertypes of one class.
	 *
	 * @param superclass the binary name of its superclass, {@code null} for none
	 * @param interfaces the binary names of the interfaces it names
	 */
	private record Supertypes(@Nullable String superclass, @Nonnull List<String> interfaces) {

		/** What a class Ares cannot read is taken to have: nothing above it. */
		@Nonnull
		private static final Supertypes NONE = new Supertypes(null, List.of());
	}

	/**
	 * The type hierarchy one check needs, read once per class: the supervised
	 * classes from ArchUnit and their class files, every other class from the class
	 * file the class path holds, such as a JDK class.
	 */
	private static final class Hierarchy {

		/** The supervised classes. */
		@Nonnull
		private final JavaClasses classes;

		/** What each supervised class read so far does through super, by name. */
		@Nonnull
		private final Map<String, ClassCalls> callsByClass = new HashMap<>();

		/** The direct supertypes of each class read so far, by name. */
		@Nonnull
		private final Map<String, Supertypes> supertypesByClass = new HashMap<>();

		/**
		 * Creates the hierarchy of one check.
		 *
		 * @param classes the supervised classes
		 */
		Hierarchy(@Nonnull JavaClasses classes) {
			this.classes = classes;
		}

		/**
		 * Returns what a supervised class does through super.
		 *
		 * @param javaClass the class
		 * @return its calls
		 */
		@Nonnull
		ClassCalls callsOf(@Nonnull JavaClass javaClass) {
			return callsByClass.computeIfAbsent(javaClass.getName(),
					name -> JavaAspectJUnobservableAccessCheck.callsOf(javaClass));
		}

		/**
		 * Returns the methods a call through a supervised interface can reach on an
		 * object: its abstract and default methods. Any other type has none here.
		 *
		 * @param typeName the binary name of the type
		 * @return the methods, empty unless the type is a supervised interface
		 */
		@Nonnull
		List<JavaMethod> instanceMethodsOfSupervisedInterface(@Nonnull String typeName) {
			if (!classes.contain(typeName) || !classes.get(typeName).isInterface()) {
				return List.of();
			}
			return classes.get(typeName).getMethods().stream()
					.filter(method -> !method.getModifiers().contains(JavaModifier.STATIC)
							&& !method.getModifiers().contains(JavaModifier.PRIVATE))
					.toList();
		}

		/**
		 * Whether a supervised class declares the method with code of its own, more
		 * than a hand-on to the method it overrides.
		 *
		 * @param typeName       the binary name of the class
		 * @param name           the method name
		 * @param parameterTypes the binary names of the parameter types
		 * @return {@code true} if calling it runs woven student code
		 */
		boolean runsOwnCode(@Nonnull String typeName, @Nonnull String name, @Nonnull List<String> parameterTypes) {
			if (!classes.contain(typeName) || !declares(classes.get(typeName), name, parameterTypes)) {
				return false;
			}
			return !callsOf(classes.get(typeName)).handOns().contains(methodKey(name, parameterTypes));
		}

		/**
		 * Returns a class name, then its superclasses in order, then every interface
		 * above them.
		 *
		 * @param ownerName the binary name of the class
		 * @return the names, the class itself first
		 */
		@Nonnull
		List<String> ownerAndSupertypes(@Nonnull String ownerName) {
			Set<String> names = new LinkedHashSet<>();
			String typeName = ownerName;
			while (typeName != null && names.add(typeName)) {
				typeName = supertypesOf(typeName).superclass();
			}
			Deque<String> pending = new ArrayDeque<>(names);
			while (!pending.isEmpty()) {
				for (String interfaceName : supertypesOf(pending.remove()).interfaces()) {
					if (names.add(interfaceName)) {
						pending.add(interfaceName);
					}
				}
			}
			return List.copyOf(names);
		}

		/**
		 * Returns the direct supertypes of a class.
		 *
		 * @param typeName the binary name of the class
		 * @return its supertypes, or none if Ares cannot read the class
		 */
		@Nonnull
		private Supertypes supertypesOf(@Nonnull String typeName) {
			return supertypesByClass.computeIfAbsent(typeName,
					name -> classes.contain(name) ? supervisedSupertypes(classes.get(name))
							: classPathSupertypes(name));
		}

		/**
		 * Returns the direct supertypes of a supervised class, as ArchUnit read them.
		 *
		 * @param javaClass the class
		 * @return its supertypes
		 */
		@Nonnull
		private static Supertypes supervisedSupertypes(@Nonnull JavaClass javaClass) {
			return new Supertypes(javaClass.getRawSuperclass().map(JavaClass::getName).orElse(null),
					javaClass.getRawInterfaces().stream().map(JavaClass::getName).toList());
		}

		/**
		 * Returns the direct supertypes of a class from its class file on the class
		 * path, without loading the class.
		 *
		 * @param typeName the binary name of the class
		 * @return its supertypes, or none if there is no readable class file
		 */
		@Nonnull
		private static Supertypes classPathSupertypes(@Nonnull String typeName) {
			try (InputStream input = ClassLoader.getSystemResourceAsStream(typeName.replace('.', '/') + ".class")) {
				if (input == null) {
					return Supertypes.NONE;
				}
				ClassReader reader = new ClassReader(input);
				String superName = reader.getSuperName();
				return new Supertypes(superName == null ? null : superName.replace('/', '.'),
						Arrays.stream(reader.getInterfaces()).map(name -> name.replace('/', '.')).toList());
			} catch (IOException | RuntimeException unreadable) {
				return Supertypes.NONE;
			}
		}
	}

	/**
	 * One call through {@code super}.
	 *
	 * @param owner          the binary name of the class the call names
	 * @param name           the method name, {@code <init>} for a constructor
	 * @param parameterTypes the binary names of the parameter types
	 */
	record SuperCall(@Nonnull String owner, @Nonnull String name, @Nonnull List<String> parameterTypes) {
	}

	/** Collects the calls through {@code super} while ASM reads a class file. */
	private static final class SuperCallCollector extends ClassVisitor {

		/** The calls found so far. */
		@Nonnull
		private final List<SuperCall> calls = new ArrayList<>();

		/**
		 * The overrides found so far that only hand on through super, as method keys:
		 * their whole body passes the parameters unchanged to the overridden method
		 * once and returns its result.
		 */
		@Nonnull
		private final Set<String> handOns = new HashSet<>();

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

			/**
			 * The classes the method created with new whose constructor has not run yet,
			 * the latest first. javac calls each such constructor before the one of an
			 * enclosing new, so the latest is the one a constructor call builds.
			 */
			@Nonnull
			private final Deque<String> pendingCreations = new ArrayDeque<>();

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
			 * The instructions of the method so far, each written as a short token, so that
			 * a whole body can be compared with the one of a plain hand-on.
			 */
			@Nonnull
			private final List<String> instructions = new ArrayList<>();

			/**
			 * The internal names of the classes the method calls itself on through super.
			 */
			@Nonnull
			private final List<String> handOnOwners = new ArrayList<>();

			/**
			 * Whether a call through super is to the method this one overrides.
			 *
			 * @param name       the called method's name
			 * @param descriptor the called method's descriptor
			 * @return {@code true} for a call to the overridden method
			 */
			private boolean isOverrideHandingOn(@Nonnull String name, @Nonnull String descriptor) {
				return name.equals(methodName) && descriptor.equals(methodDescriptor);
			}

			/**
			 * Returns the tokens of a body that only hands on: this and every parameter
			 * loaded in order, the call through super, and the return.
			 *
			 * @param owner the internal name of the class the call names
			 * @return the tokens
			 */
			@Nonnull
			private List<String> handOnBody(@Nonnull String owner) {
				List<String> body = new ArrayList<>();
				body.add("v" + Opcodes.ALOAD + ":0");
				int slot = 1;
				for (Type parameter : Type.getArgumentTypes(methodDescriptor)) {
					body.add("v" + parameter.getOpcode(Opcodes.ILOAD) + ":" + slot);
					slot += parameter.getSize();
				}
				body.add("m" + Opcodes.INVOKESPECIAL + ":" + owner + "." + methodName + methodDescriptor);
				body.add("i" + Type.getReturnType(methodDescriptor).getOpcode(Opcodes.IRETURN));
				return body;
			}

			@Override
			public void visitInsn(int opcode) {
				instructions.add("i" + opcode);
			}

			@Override
			public void visitIntInsn(int opcode, int operand) {
				instructions.add("x" + opcode);
			}

			@Override
			public void visitVarInsn(int opcode, int varIndex) {
				instructions.add("v" + opcode + ":" + varIndex);
			}

			@Override
			public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
				instructions.add("x" + opcode);
			}

			@Override
			public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
					Object... bootstrapMethodArguments) {
				instructions.add("x" + Opcodes.INVOKEDYNAMIC);
			}

			@Override
			public void visitJumpInsn(int opcode, Label label) {
				instructions.add("x" + opcode);
			}

			@Override
			public void visitLdcInsn(Object value) {
				instructions.add("x" + Opcodes.LDC);
			}

			@Override
			public void visitIincInsn(int varIndex, int increment) {
				instructions.add("x" + Opcodes.IINC);
			}

			@Override
			public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
				instructions.add("x" + Opcodes.TABLESWITCH);
			}

			@Override
			public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
				instructions.add("x" + Opcodes.LOOKUPSWITCH);
			}

			@Override
			public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
				instructions.add("x" + Opcodes.MULTIANEWARRAY);
			}

			@Override
			public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
				instructions.add("t");
			}

			@Override
			public void visitTypeInsn(int opcode, String type) {
				instructions.add("x" + opcode);
				if (opcode == Opcodes.NEW) {
					pendingCreations.push(type);
				}
			}

			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
				instructions.add("m" + opcode + ":" + owner + "." + name + descriptor);
				if (opcode != Opcodes.INVOKESPECIAL) {
					return;
				}
				if ("<init>".equals(name)) {
					recordConstructorCall(owner, descriptor);
					return;
				}
				if (owner.equals(self)) {
					return;
				}
				if (isOverrideHandingOn(name, descriptor)) {
					handOnOwners.add(owner);
				} else {
					calls.add(new SuperCall(owner.replace('/', '.'), name, parameterTypesOf(descriptor)));
				}
			}

			/**
			 * Decides, once the whole method is read, whether its calls to the overridden
			 * method are a plain hand-on. Any other body, such as one that passes other
			 * values or calls it twice, counts as calls through super.
			 */
			@Override
			public void visitEnd() {
				if (handOnOwners.size() == 1 && instructions.equals(handOnBody(handOnOwners.get(0)))) {
					handOns.add(methodKey(methodName, parameterTypesOf(methodDescriptor)));
					return;
				}
				handOnOwners.forEach(owner -> calls
						.add(new SuperCall(owner.replace('/', '.'), methodName, parameterTypesOf(methodDescriptor))));
			}

			/**
			 * Records a superclass constructor called on the object being built. A call
			 * building the latest object created with new is not one.
			 *
			 * @param owner      the internal name of the constructor's class
			 * @param descriptor the constructor's descriptor
			 */
			private void recordConstructorCall(@Nonnull String owner, @Nonnull String descriptor) {
				if (owner.equals(pendingCreations.peek())) {
					pendingCreations.pop();
				} else if ("<init>".equals(methodName) && owner.equals(superName)) {
					calls.add(new SuperCall(owner.replace('/', '.'), "<init>", parameterTypesOf(descriptor)));
				}
			}
		}
	}

	/**
	 * Returns the binary names of the parameter types of a method descriptor, as
	 * ArchUnit names classes: {@code java.lang.String}, {@code int},
	 * {@code [Ljava.lang.String;}.
	 *
	 * @param descriptor the method descriptor
	 * @return the names
	 */
	@Nonnull
	private static List<String> parameterTypesOf(@Nonnull String descriptor) {
		List<String> names = new ArrayList<>();
		for (Type type : Type.getArgumentTypes(descriptor)) {
			names.add(type.getSort() == Type.ARRAY ? type.getDescriptor().replace('/', '.') : type.getClassName());
		}
		return names;
	}
}
