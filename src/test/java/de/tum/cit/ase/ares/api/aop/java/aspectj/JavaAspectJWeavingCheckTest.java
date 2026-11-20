package de.tum.cit.ase.ares.api.aop.java.aspectj;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.integration.aop.allowed.subject.weaving.WovenWithoutJoinPoint;

/**
 * Checks that the weaving check refuses classes built without the Ares aspects,
 * lets woven ones through even when no advice was woven into them, and that a
 * student's source cannot write the marker itself.
 */
class JavaAspectJWeavingCheckTest {

	/** A folder for the classes this test compiles without the aspects. */
	@TempDir
	Path tempDir;

	/**
	 * Compiles one source file with plain javac, so no aspect is woven in.
	 *
	 * @param relativePath the source path below the source folder
	 * @param source       the source text
	 * @return the compiler's exit code and messages
	 */
	private CompilationResult compileWithoutAspects(String relativePath, String source) throws IOException {
		Path sourceFile = tempDir.resolve("src").resolve(relativePath);
		Files.createDirectories(sourceFile.getParent());
		Files.writeString(sourceFile, source, StandardCharsets.UTF_8);
		Files.createDirectories(tempDir.resolve("classes"));
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		ByteArrayOutputStream messages = new ByteArrayOutputStream();
		int exitCode = compiler.run(null, messages, messages, "-cp", aresClasses().toString(), "-d",
				tempDir.resolve("classes").toString(), sourceFile.toString());
		return new CompilationResult(exitCode, messages.toString(StandardCharsets.UTF_8));
	}

	/**
	 * Returns where the Ares classes, the marker among them, were compiled to.
	 *
	 * @return the folder or JAR holding the Ares classes
	 */
	private static Path aresClasses() {
		try {
			return Path.of(JavaAspectJWeavingCheck.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException malformed) {
			throw new IllegalStateException(malformed);
		}
	}

	/**
	 * The exit code and the messages of one compilation.
	 *
	 * @param exitCode the compiler's exit code, zero on success
	 * @param messages what the compiler printed
	 */
	private record CompilationResult(int exitCode, String messages) {
	}

	/**
	 * Reads the classes compiled without the aspects.
	 *
	 * @return the imported classes
	 */
	private JavaClasses unwovenClasses() {
		return new ClassFileImporter().importPath(tempDir.resolve("classes"));
	}

	/**
	 * Returns the folder the woven fixture was compiled into by this build.
	 *
	 * @return the folder holding the woven fixture
	 */
	private static Path wovenFixtureFolder() throws URISyntaxException {
		return Path.of(WovenWithoutJoinPoint.class.getResource("WovenWithoutJoinPoint.class").toURI()).getParent();
	}

	/** A class built with the aspects but without any advice counts as woven. */
	@Test
	void aWovenClassWithoutAnyAdvicePasses() throws URISyntaxException {
		JavaClasses woven = new ClassFileImporter().importPath(wovenFixtureFolder());
		assertTrue(woven.contain(WovenWithoutJoinPoint.class));
		assertDoesNotThrow(() -> JavaAspectJWeavingCheck.requireWoven(woven, List.of()));
	}

	/** A class compiled by plain javac is refused, and the refusal names it. */
	@Test
	void aClassBuiltWithoutTheAspectsIsRefused() throws IOException {
		assertEquals(0,
				compileWithoutAspects("example/unwoven/Plain.java", "package example.unwoven; public class Plain { }")
						.exitCode());
		SecurityException refusal = assertThrows(SecurityException.class,
				() -> JavaAspectJWeavingCheck.requireWoven(unwovenClasses(), List.of()));
		assertTrue(refusal.getMessage().contains("example.unwoven.Plain"), refusal.getMessage());
	}

	/** One unwoven class among woven ones is enough for a refusal. */
	@Test
	void oneUnwovenClassAmongWovenOnesIsRefused() throws IOException, URISyntaxException {
		compileWithoutAspects("example/unwoven/Plain.java", "package example.unwoven; public class Plain { }");
		JavaClasses mixed = new ClassFileImporter()
				.importPaths(List.of(tempDir.resolve("classes"), wovenFixtureFolder()));
		assertEquals(List.of("example.unwoven.Plain"), JavaAspectJWeavingCheck.unwovenClassNames(mixed, List.of()));
		assertThrows(SecurityException.class, () -> JavaAspectJWeavingCheck.requireWoven(mixed, List.of()));
	}

	/** An exempted class, such as a test class, is not checked. */
	@Test
	void anExemptedUnwovenClassPasses() throws IOException {
		compileWithoutAspects("example/unwoven/PlainTest.java", "package example.unwoven; public class PlainTest { }");
		assertDoesNotThrow(
				() -> JavaAspectJWeavingCheck.requireWoven(unwovenClasses(), List.of("example.unwoven.PlainTest")));
	}

	/** A package descriptor holds no code and is not checked. */
	@Test
	void aPackageDescriptorPasses() throws IOException {
		assertEquals(0,
				compileWithoutAspects("example/unwoven/package-info.java", "@Deprecated package example.unwoven;")
						.exitCode());
		assertTrue(Files.exists(tempDir.resolve("classes/example/unwoven/package-info.class")));
		assertDoesNotThrow(() -> JavaAspectJWeavingCheck.requireWoven(unwovenClasses(), List.of()));
	}

	/** A student's source that writes the marker itself does not compile. */
	@Test
	void studentSourceCannotWriteTheMarker() throws IOException {
		CompilationResult forged = compileWithoutAspects("example/unwoven/Forged.java",
				"package example.unwoven; @" + JavaAspectJWeavingCheck.MARKER + " public class Forged { }");
		assertTrue(forged.exitCode() != 0, "a student source could write the marker");
		assertTrue(forged.messages().contains("JavaAspectJWoven"), forged.messages());
	}

	/**
	 * The aspect leaves out every reserved namespace, so the two lists cannot drift
	 * apart. Leaving one out is only about compiler warnings, never about safety.
	 */
	@Test
	void theMarkerAspectLeavesOutEveryReservedNamespace() throws IOException {
		Set<String> leftOut = Arrays.stream(Files.readString(Path.of(
				"src/main/java/de/tum/cit/ase/ares/api/aop/java/aspectj/adviceandpointcut/JavaAspectJWovenMarkerDefinitions.aj"))
				.split("[\\s|()!]+")).collect(Collectors.toSet());
		try (InputStream prefixes = JavaAspectJWeavingCheckTest.class.getResourceAsStream(
				"/de/tum/cit/ase/ares/api/configuration/reservedPackages/ReservedPackagePrefixes.txt")) {
			assertNotNull(prefixes);
			new String(prefixes.readAllBytes(), StandardCharsets.UTF_8).lines().map(String::strip)
					.filter(line -> !line.isEmpty() && !line.startsWith("#"))
					.map(line -> line.replace("/**", "..*").replace('/', '.'))
					.forEach(pattern -> assertTrue(leftOut.contains(pattern),
							() -> "the marker aspect does not leave out " + pattern));
		}
	}

	/**
	 * The marker named by the check exists, is not public, and stays out of
	 * reflection, so a reflection-based structural test never sees it on a woven
	 * class.
	 */
	@Test
	void theMarkerIsHiddenFromStudentsAndReflection() throws ClassNotFoundException {
		Class<?> marker = Class.forName(JavaAspectJWeavingCheck.MARKER, false,
				JavaAspectJWeavingCheckTest.class.getClassLoader());
		assertTrue(marker.isAnnotation());
		assertFalse(Modifier.isPublic(marker.getModifiers()));
		Retention retention = marker.getAnnotation(Retention.class);
		assertNotNull(retention);
		assertEquals(RetentionPolicy.CLASS, retention.value());
		assertTrue(Arrays.stream(WovenWithoutJoinPoint.class.getAnnotations())
				.noneMatch(annotation -> annotation.annotationType().equals(marker)));
	}
}
