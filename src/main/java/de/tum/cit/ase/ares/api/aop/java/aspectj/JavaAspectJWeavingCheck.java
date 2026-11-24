package de.tum.cit.ase.ares.api.aop.java.aspectj;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaFieldAccess;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.Source;

import net.bytebuddy.jar.asm.ClassReader;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Refuses an AspectJ run whose supervised classes were built without the Ares
 * aspects woven in, since nothing would check them while they run. It is to
 * AspectJ what the refusal of a missing agent is to instrumentation.
 */
public final class JavaAspectJWeavingCheck {

	/**
	 * The binary name of the marker the Ares aspects put on every class they are
	 * woven into. Compared by name, because the type itself is not public.
	 */
	@Nonnull
	public static final String MARKER = "de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJWoven";

	/**
	 * Simple names of the class files that describe a package or a module. They
	 * hold no code that could run, so they are not checked.
	 */
	@Nonnull
	private static final Set<String> DESCRIPTOR_NAMES = Set.of("package-info", "module-info");

	/**
	 * Constant-pool tags through which a class can call code that ArchUnit does not
	 * list as an access: method handles, method types, dynamic constants and
	 * invokedynamic call sites.
	 */
	@Nonnull
	private static final Set<Integer> DYNAMIC_CONSTANT_TAGS = Set.of(15, 16, 17, 18);

	/** Prevents creating instances of this utility class. */
	private JavaAspectJWeavingCheck() {
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "JavaAspectJWeavingCheck"));
	}

	/**
	 * Refuses when a supervised class lacks the marker. Exempted classes, such as
	 * the test classes, are not checked, because enforcement skips them anyway.
	 *
	 * @param supervisedClasses  the classes read from the supervised class path
	 * @param exemptedClassNames the exact names of the exempted classes
	 * @throws SecurityException naming how many classes lack the marker and the
	 *                           first of them by name
	 */
	public static void requireWoven(@Nonnull JavaClasses supervisedClasses,
			@Nonnull Collection<String> exemptedClassNames) {
		List<String> unwoven = unwovenClassNames(supervisedClasses, exemptedClassNames);
		if (!unwoven.isEmpty()) {
			throw new SecurityException(
					Messages.localized("security.aspectj.not.woven", unwoven.size(), unwoven.get(0)));
		}
	}

	/**
	 * Lists, sorted, the supervised classes that are neither exempted nor a package
	 * or module descriptor nor a javac switch-map holder and do not carry the
	 * marker.
	 *
	 * @param supervisedClasses  the classes read from the supervised class path
	 * @param exemptedClassNames the exact names of the exempted classes
	 * @return the names of the classes built without the Ares aspects
	 */
	@Nonnull
	public static List<String> unwovenClassNames(@Nonnull JavaClasses supervisedClasses,
			@Nonnull Collection<String> exemptedClassNames) {
		return supervisedClasses.stream().filter(javaClass -> !DESCRIPTOR_NAMES.contains(javaClass.getSimpleName()))
				.filter(javaClass -> !exemptedClassNames.contains(javaClass.getName()))
				.filter(javaClass -> !isJavacSwitchMapHolder(javaClass))
				.filter(javaClass -> !javaClass.isAnnotatedWith(MARKER)).map(JavaClass::getName).sorted().toList();
	}

	/**
	 * Whether a class is the table javac writes for a switch over an enum. The
	 * Maven setup compiles twice into one folder: javac writes this class, ajc then
	 * builds the switch another way and leaves it behind, unwoven but never used.
	 * Only the exact javac shape counts, down to its constant pool, so the class
	 * can run no code of its own.
	 *
	 * @param javaClass the class to look at
	 * @return {@code true} if the class has exactly that shape
	 */
	static boolean isJavacSwitchMapHolder(@Nonnull JavaClass javaClass) {
		return javaClass.getModifiers().contains(JavaModifier.SYNTHETIC) && javaClass.getMethods().isEmpty()
				&& javaClass.getConstructors().isEmpty() && !javaClass.getFields().isEmpty()
				&& javaClass.getFields().stream().allMatch(JavaAspectJWeavingCheck::isSwitchMapField)
				&& javaClass.getAccessesFromSelf().stream().allMatch(access -> isSwitchMapAccess(javaClass, access))
				&& holdsNoDynamicConstants(javaClass);
	}

	/**
	 * Whether the class file of a class holds no dynamic constant, so every call it
	 * can make is one ArchUnit lists. A class without a readable class file does
	 * not pass.
	 *
	 * @param javaClass the class to read
	 * @return {@code true} if its class file holds none
	 */
	private static boolean holdsNoDynamicConstants(@Nonnull JavaClass javaClass) {
		Optional<URI> classFile = javaClass.getSource().map(Source::getUri);
		if (classFile.isEmpty()) {
			return false;
		}
		try (InputStream input = classFile.get().toURL().openStream()) {
			return holdsNoDynamicConstants(input.readAllBytes());
		} catch (IOException unreadable) {
			return false;
		}
	}

	/**
	 * Whether the constant pool of a class file holds none of the
	 * {@link #DYNAMIC_CONSTANT_TAGS}.
	 *
	 * @param classFile the bytes of the class file
	 * @return {@code true} if it holds none
	 */
	static boolean holdsNoDynamicConstants(@Nonnull byte[] classFile) {
		ClassReader reader = new ClassReader(classFile);
		return IntStream.range(1, reader.getItemCount()).map(reader::getItem).filter(offset -> offset > 0)
				.noneMatch(offset -> DYNAMIC_CONSTANT_TAGS.contains((int) classFile[offset - 1]));
	}

	/**
	 * Whether a field is one javac switch-map table: a synthetic static final
	 * {@code int[]} whose name starts with {@code $SwitchMap$}.
	 *
	 * @param field the field to look at
	 * @return {@code true} if the field has that shape
	 */
	private static boolean isSwitchMapField(@Nonnull JavaField field) {
		return field.getName().startsWith("$SwitchMap$")
				&& field.getModifiers()
						.containsAll(Set.of(JavaModifier.STATIC, JavaModifier.FINAL, JavaModifier.SYNTHETIC))
				&& field.getRawType().isEquivalentTo(int[].class);
	}

	/**
	 * Whether one access of a switch-map holder is one javac writes there: a use of
	 * its own tables, a read of an enum constant, or a call of {@code values()} or
	 * {@code ordinal()}. Every other access, a constructor call among them, does
	 * not count.
	 *
	 * @param holder the class making the access
	 * @param access the access
	 * @return {@code true} if the access is one of those
	 */
	private static boolean isSwitchMapAccess(@Nonnull JavaClass holder, @Nonnull JavaAccess<?> access) {
		if (access instanceof JavaFieldAccess fieldAccess) {
			return fieldAccess.getTargetOwner().equals(holder) || isEnumConstantRead(fieldAccess);
		}
		return access instanceof JavaMethodCall call
				&& (isOrdinalCall(call.getTarget()) || isValuesCall(call.getTarget()));
	}

	/**
	 * Whether a field access reads a static field typed as its own class, which is
	 * how javac reads an enum constant.
	 *
	 * @param fieldAccess the field access
	 * @return {@code true} if it reads such a field
	 */
	private static boolean isEnumConstantRead(@Nonnull JavaFieldAccess fieldAccess) {
		return fieldAccess.getAccessType() == JavaFieldAccess.AccessType.GET
				&& fieldAccess.getTarget().getRawType().equals(fieldAccess.getTargetOwner());
	}

	/**
	 * Whether a call target is {@code ordinal()}: no parameters, returning an
	 * {@code int}.
	 *
	 * @param target the call target
	 * @return {@code true} if it is
	 */
	private static boolean isOrdinalCall(@Nonnull AccessTarget.MethodCallTarget target) {
		return "ordinal".equals(target.getName()) && target.getRawParameterTypes().isEmpty()
				&& target.getRawReturnType().isEquivalentTo(int.class);
	}

	/**
	 * Whether a call target is an enum's {@code values()}: no parameters, returning
	 * an array of its own class.
	 *
	 * @param target the call target
	 * @return {@code true} if it is
	 */
	private static boolean isValuesCall(@Nonnull AccessTarget.MethodCallTarget target) {
		return "values".equals(target.getName()) && target.getRawParameterTypes().isEmpty()
				&& target.getRawReturnType().getName().equals("[L" + target.getOwner().getName() + ";");
	}
}
