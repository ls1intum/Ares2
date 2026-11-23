package de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * Checks that the library-load check is bound to exactly one JDK method,
 * {@code Runtime.loadLibrary0(Class, String)}, chosen by its types.
 */
class JavaInstrumentationLoadLibrary0PointcutTest {

	/**
	 * Of all methods {@code Runtime} declares, the matcher picks
	 * {@code loadLibrary0} alone, with its JVM descriptor.
	 */
	@Test
	void theMatcherPicksOnlyLoadLibrary0() {
		ElementMatcher<MethodDescription> matcher = JavaInstrumentationPointcutDefinitions.getLoadLibrary0Matcher();
		List<String> matched = TypeDescription.ForLoadedType.of(Runtime.class).getDeclaredMethods().stream()
				.filter(matcher::matches).map(method -> method.getInternalName() + method.getDescriptor()).toList();
		assertThat(matched).containsExactly("loadLibrary0(Ljava/lang/Class;Ljava/lang/String;)V");
	}

	/**
	 * The same name and argument count on another class is not matched, so the name
	 * alone never selects a method.
	 */
	@Test
	void aMethodOfTheSameNameElsewhereIsNotPicked() {
		ElementMatcher<MethodDescription> matcher = JavaInstrumentationPointcutDefinitions.getLoadLibrary0Matcher();
		assertThat(TypeDescription.ForLoadedType.of(LookAlike.class).getDeclaredMethods().stream()
				.filter(method -> "loadLibrary0".equals(method.getInternalName())).noneMatch(matcher::matches))
						.isTrue();
	}

	/**
	 * Selecting the classes for the binding chooses {@code Runtime}, not
	 * {@code System}.
	 */
	@Test
	void theBindingWatchesRuntimeOnly() {
		ElementMatcher<TypeDescription> types = JavaInstrumentationPointcutDefinitions
				.getClassesMatcher(JavaInstrumentationPointcutDefinitions.METHODS_WHICH_LOAD_LIBRARIES_FOR_A_CALLER);
		assertThat(types.matches(TypeDescription.ForLoadedType.of(Runtime.class))).isTrue();
		assertThat(types.matches(TypeDescription.ForLoadedType.of(System.class))).isFalse();
	}

	/** A class with a method shaped like {@code Runtime.loadLibrary0}. */
	static final class LookAlike {

		/**
		 * Does nothing; only its name and parameters matter.
		 *
		 * @param fromClass   ignored
		 * @param libraryName ignored
		 */
		void loadLibrary0(Class<?> fromClass, String libraryName) {
		}
	}
}
