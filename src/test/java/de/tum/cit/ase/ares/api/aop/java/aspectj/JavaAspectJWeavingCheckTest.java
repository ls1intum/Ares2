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

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.SyntheticState;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Handle;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;

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

	/**
	 * The switch-map table javac leaves behind for an enum switch is not listed,
	 * for an own enum, a JDK enum and a switch expression, while the class holding
	 * the switch still is.
	 */
	@Test
	void aJavacSwitchMapHolderIsNotListed() throws IOException {
		assertEquals(0, compileWithoutAspects("example/unwoven/Switcher.java", """
				package example.unwoven;
				public class Switcher {
					enum Colour { RED, GREEN }
					static int classic(Colour c) { switch (c) { case RED: return 1; default: return 2; } }
					static int jdk(java.time.DayOfWeek d) { switch (d) { case MONDAY: return 1; default: return 0; } }
					static int expression(Colour c) { return switch (c) { case RED -> 3; case GREEN -> 4; }; }
				}
				""").exitCode());
		JavaClasses classes = unwovenClasses();
		assertTrue(JavaAspectJWeavingCheck.isJavacSwitchMapHolder(classes.get("example.unwoven.Switcher$1")));
		assertEquals(List.of("example.unwoven.Switcher", "example.unwoven.Switcher$Colour"),
				JavaAspectJWeavingCheck.unwovenClassNames(classes, List.of()));
	}

	/**
	 * A class written to look like a switch-map table, but not synthetic, is
	 * listed.
	 */
	@Test
	void aSourceLookAlikeOfASwitchMapHolderIsListed() throws IOException {
		assertEquals(0,
				compileWithoutAspects("example/unwoven/LookAlike.java",
						"package example.unwoven; class LookAlike { static final int[] $SwitchMap$x = new int[1]; }")
								.exitCode());
		assertEquals(List.of("example.unwoven.LookAlike"),
				JavaAspectJWeavingCheck.unwovenClassNames(unwovenClasses(), List.of()));
	}

	/**
	 * Writes a synthetic class shaped like a switch-map table into the class
	 * folder, as a class file shipped among the resources could be.
	 *
	 * @param name    the binary name of the class
	 * @param builder adds what the forged class has beyond the shape
	 */
	private void writeForgedHolder(String name, java.util.function.UnaryOperator<DynamicType.Builder<Object>> builder)
			throws IOException {
		DynamicType.Builder<Object> holder = new ByteBuddy()
				.subclass(Object.class, ConstructorStrategy.Default.NO_CONSTRUCTORS).name(name)
				.modifiers(Visibility.PACKAGE_PRIVATE, SyntheticState.SYNTHETIC).defineField("$SwitchMap$x",
						int[].class, Ownership.STATIC, FieldManifestation.FINAL, SyntheticState.SYNTHETIC);
		builder.apply(holder).make().saveIn(tempDir.resolve("classes").toFile());
	}

	/** A synthetic table whose initializer calls anything else is listed. */
	@Test
	void aForgedHolderWhoseInitializerCallsMoreIsListed() throws IOException, NoSuchMethodException {
		java.lang.reflect.Method lineSeparator = System.class.getMethod("lineSeparator");
		writeForgedHolder("example.unwoven.Forged$1", holder -> holder.invokable(ElementMatchers.isTypeInitializer())
				.intercept(MethodCall.invoke(lineSeparator)));
		assertEquals(List.of("example.unwoven.Forged$1"),
				JavaAspectJWeavingCheck.unwovenClassNames(unwovenClasses(), List.of()));
	}

	/** A synthetic table with a method of its own is listed. */
	@Test
	void aForgedHolderWithAMethodIsListed() throws IOException {
		writeForgedHolder("example.unwoven.Forged$2", holder -> holder
				.defineMethod("run", String.class, Ownership.STATIC).intercept(FixedValue.value("ran")));
		assertEquals(List.of("example.unwoven.Forged$2"),
				JavaAspectJWeavingCheck.unwovenClassNames(unwovenClasses(), List.of()));
	}

	/**
	 * A table of the exact shape whose initializer loads a method handle is listed,
	 * although ArchUnit records no access for that load.
	 */
	@Test
	void aForgedHolderHoldingAMethodHandleIsListed() throws IOException {
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		writer.visit(Opcodes.V17, Opcodes.ACC_SUPER | Opcodes.ACC_SYNTHETIC, "example/unwoven/Forged$3", null,
				"java/lang/Object", null);
		writer.visitField(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC, "$SwitchMap$x", "[I", null,
				null).visitEnd();
		MethodVisitor initializer = writer.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
		initializer.visitCode();
		initializer.visitLdcInsn(
				new Handle(Opcodes.H_INVOKESTATIC, "java/lang/System", "lineSeparator", "()Ljava/lang/String;", false));
		initializer.visitInsn(Opcodes.POP);
		initializer.visitInsn(Opcodes.RETURN);
		initializer.visitMaxs(0, 0);
		initializer.visitEnd();
		writer.visitEnd();
		byte[] forged = writer.toByteArray();
		Path classFile = tempDir.resolve("classes/example/unwoven/Forged$3.class");
		Files.createDirectories(classFile.getParent());
		Files.write(classFile, forged);
		assertFalse(JavaAspectJWeavingCheck.holdsNoDynamicConstants(forged));
		assertTrue(unwovenClasses().get("example.unwoven.Forged$3").getAccessesFromSelf().isEmpty());
		assertEquals(List.of("example.unwoven.Forged$3"),
				JavaAspectJWeavingCheck.unwovenClassNames(unwovenClasses(), List.of()));
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
