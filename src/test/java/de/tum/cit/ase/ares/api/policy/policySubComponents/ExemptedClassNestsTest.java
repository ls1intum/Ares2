package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import example.nest.NestHostFixture;

/**
 * Checks that an exemption extends exactly to the classes an exempted class's
 * own compiled file lists as declared inside it, and that a defective or
 * foreign class file extends it to nothing.
 */
class ExemptedClassNestsTest {

	/** The host whose nest the tests read. */
	private static final String HOST = NestHostFixture.class.getName();

	/** The loader that sees the compiled fixtures. */
	private static final ClassLoader LOADER = ExemptedClassNestsTest.class.getClassLoader();

	/**
	 * Every kind of class declared inside the host is listed, at any depth, and a
	 * separate top-level class named like a nested one is not.
	 */
	@Test
	void theNestListsEveryClassDeclaredInsideAndNothingElse() throws Exception {
		Set<String> expanded = Set.copyOf(ExemptedClassNests.expandThroughLoader(List.of(HOST), LOADER));
		Set<String> jvmView = Arrays.stream(NestHostFixture.class.getNestMembers()).map(Class::getName)
				.collect(Collectors.toSet());
		assertEquals(jvmView, expanded);
		for (Class<?> declared : List.of(NestHostFixture.anonymous().getClass(), NestHostFixture.local().getClass(),
				NestHostFixture.IN_INITIALISER.getClass(), NestHostFixture.Member.Deeper.class,
				NestHostFixture.Kind.SPECIAL.getClass(), NestHostFixture.Pair.class)) {
			assertTrue(expanded.contains(declared.getName()), declared.getName() + " in " + expanded);
		}
		assertNotNull(Class.forName("example.nest.NestHostFixture$Evil"));
		assertFalse(expanded.contains("example.nest.NestHostFixture$Evil"), expanded.toString());
	}

	/** The trusted test output is read by path, without any loader. */
	@Test
	void theTestOutputIsReadByPath(@TempDir Path testOutput) throws Exception {
		Path file = testOutput.resolve("example/nest/NestHostFixture.class");
		Files.createDirectories(file.getParent());
		Files.write(file, classFile(HOST));
		assertEquals(ExemptedClassNests.expandThroughLoader(List.of(HOST), LOADER),
				ExemptedClassNests.expandFromTestOutput(List.of(HOST), testOutput));
	}

	/**
	 * A test class missing from the trusted test output keeps only its exact name,
	 * even though a loader could find a class of that name elsewhere, for example
	 * in the student's output.
	 */
	@Test
	void aTestClassMissingFromTheTestOutputAddsNothing(@TempDir Path testOutput) {
		assertEquals(List.of(HOST), ExemptedClassNests.expandFromTestOutput(List.of(HOST), testOutput));
		assertEquals(List.of(HOST), ExemptedClassNests.expandFromTestOutput(List.of(HOST), null));
	}

	/**
	 * A test class reached through a linked package directory or a linked class
	 * file keeps only its exact name, since the link may lead outside the trusted
	 * test output.
	 */
	@Test
	void aLinkInTheTestOutputAddsNothing(@TempDir Path testOutput, @TempDir Path elsewhere) throws Exception {
		Path real = elsewhere.resolve("nest/NestHostFixture.class");
		Files.createDirectories(real.getParent());
		Files.write(real, classFile(HOST));
		Path linkedDirectory = testOutput.resolve("directory");
		Path linkedFile = testOutput.resolve("file/example/nest/NestHostFixture.class");
		Files.createDirectories(linkedDirectory.resolve("example"));
		Files.createDirectories(linkedFile.getParent());
		try {
			Files.createSymbolicLink(linkedDirectory.resolve("example/nest"), real.getParent());
			Files.createSymbolicLink(linkedFile, real);
		} catch (UnsupportedOperationException | IOException unsupported) {
			org.junit.jupiter.api.Assumptions.abort("Symbolic links are unavailable here: " + unsupported);
		}
		assertEquals(List.of(HOST), ExemptedClassNests.expandFromTestOutput(List.of(HOST), linkedDirectory));
		assertEquals(List.of(HOST), ExemptedClassNests.expandFromTestOutput(List.of(HOST), testOutput.resolve("file")));
	}

	/**
	 * A link inside the production output fails the scan, which does not follow
	 * links.
	 */
	@Test
	void aLinkInTheProductionOutputFailsTheScan(@TempDir Path production, @TempDir Path elsewhere) throws Exception {
		Files.createDirectories(production.resolve("example"));
		try {
			Files.createSymbolicLink(production.resolve("example/nest"), elsewhere);
		} catch (UnsupportedOperationException | IOException unsupported) {
			org.junit.jupiter.api.Assumptions.abort("Symbolic links are unavailable here: " + unsupported);
		}
		assertThrows(SecurityException.class, () -> ExemptedClassNests
				.requireNoProductionClassNamedLikeAnExemptedOne(production, "example.nest", List.of(HOST)));
	}

	/**
	 * An exempted class that is itself nested keeps only its exact name; its own
	 * descendants are not added.
	 */
	@Test
	void aNestedExemptedClassAddsNoDescendants() {
		String member = NestHostFixture.Member.class.getName();
		assertEquals(List.of(member), ExemptedClassNests.expandThroughLoader(List.of(member), LOADER));
	}

	/** A class Ares cannot find keeps only its exact name. */
	@Test
	void aMissingClassKeepsOnlyItsExactName() {
		assertEquals(List.of("example.nest.Missing"),
				ExemptedClassNests.expandThroughLoader(List.of("example.nest.Missing"), LOADER));
	}

	/** A file declaring another class grants nothing to the requested name. */
	@Test
	void aFileForAnotherClassGrantsNothing() throws Exception {
		assertEquals(List.of(), ExemptedClassNests.nestMembersOf("example.nest.Other", classFile(HOST)));
	}

	/** A defect anywhere, even after a valid listing, grants nothing. */
	@Test
	void aDefectAfterAValidListingGrantsNothing() throws Exception {
		byte[] valid = classFile(HOST);
		assertFalse(ExemptedClassNests.nestMembersOf(HOST, valid).isEmpty());
		byte[] trailing = Arrays.copyOf(valid, valid.length + 1);
		assertEquals(List.of(), ExemptedClassNests.nestMembersOf(HOST, trailing));
		assertEquals(List.of(), ExemptedClassNests.nestMembersOf(HOST, Arrays.copyOf(valid, valid.length - 1)));
		byte[] wrongMagic = valid.clone();
		wrongMagic[0] = 0;
		assertEquals(List.of(), ExemptedClassNests.nestMembersOf(HOST, wrongMagic));
	}

	/**
	 * A minimal well-formed class file lists its members, the control for the cases
	 * below.
	 */
	@Test
	void aMinimalClassFileListsItsMembers() throws Exception {
		assertEquals(List.of("x.T$A"), ExemptedClassNests.nestMembersOf("x.T", minimal(Variant.VALID)));
	}

	/**
	 * Method handles of a kind that matches the member they refer to are accepted,
	 * the control for the malformed handles below.
	 */
	@Test
	void wellFormedMethodHandlesAreAccepted() throws Exception {
		for (Variant variant : List.of(Variant.FIELD_HANDLE, Variant.CONSTRUCTOR_HANDLE)) {
			assertEquals(List.of("x.T$A"), ExemptedClassNests.nestMembersOf("x.T", minimal(variant)), variant.name());
		}
	}

	/** Malformed nest attributes and constants grant nothing. */
	@Test
	void malformedAttributesGrantNothing() throws Exception {
		for (Variant variant : List.of(Variant.TWO_NEST_MEMBERS, Variant.WRONG_LENGTH, Variant.NEST_HOST_TOO,
				Variant.MEMBER_IS_TEXT, Variant.UNKNOWN_TAG, Variant.DANGLING_REFERENCE, Variant.METHOD_HANDLE_TO_FIELD,
				Variant.VIRTUAL_HANDLE_TO_CONSTRUCTOR, Variant.INTERFACE_HANDLE_TO_CLASS_METHOD)) {
			assertEquals(List.of(), ExemptedClassNests.nestMembersOf("x.T", minimal(variant)), variant.name());
		}
	}

	/**
	 * A production class named like an exempted one in the supervised scope is
	 * refused.
	 */
	@Test
	void aProductionClassNamedLikeAnExemptedOneIsRefused(@TempDir Path production) throws Exception {
		Path spoof = production.resolve("example/nest/NestHostFixture$1.class");
		Files.createDirectories(spoof.getParent());
		Files.write(spoof, new byte[] { 1 });
		List<String> exempted = ExemptedClassNests.expandThroughLoader(List.of(HOST), LOADER);
		SecurityException refused = assertThrows(SecurityException.class, () -> ExemptedClassNests
				.requireNoProductionClassNamedLikeAnExemptedOne(production, "example.nest", exempted));
		assertTrue(refused.getMessage().contains("example.nest.NestHostFixture$1"), refused.getMessage());
	}

	/**
	 * The scan uses the same raw prefix as the runtime checks, so a sibling package
	 * those checks supervise is covered, and a class outside it is not.
	 */
	@Test
	void theScanCoversTheRuntimeScopeOnly(@TempDir Path production) throws Exception {
		Path sibling = production.resolve("example/nestother/Helper$1.class");
		Files.createDirectories(sibling.getParent());
		Files.write(sibling, new byte[] { 1 });
		List<String> exempted = List.of("example.nestother.Helper$1");
		assertThrows(SecurityException.class, () -> ExemptedClassNests
				.requireNoProductionClassNamedLikeAnExemptedOne(production, "example.nest", exempted));
		ExemptedClassNests.requireNoProductionClassNamedLikeAnExemptedOne(production, "other", exempted);
	}

	/** Unrelated production classes pass, and missing output fails closed. */
	@Test
	void unrelatedClassesPassAndMissingOutputFailsClosed(@TempDir Path production) throws Exception {
		Path ordinary = production.resolve("example/nest/Penguin.class");
		Files.createDirectories(ordinary.getParent());
		Files.write(ordinary, new byte[] { 1 });
		ExemptedClassNests.requireNoProductionClassNamedLikeAnExemptedOne(production, "example.nest",
				ExemptedClassNests.expandThroughLoader(List.of(HOST), LOADER));
		assertThrows(SecurityException.class,
				() -> ExemptedClassNests.requireNoProductionClassNamedLikeAnExemptedOne(production.resolve("absent"),
						"example.nest", List.of(HOST)));
	}

	/** Reads a compiled fixture's bytes. */
	private static byte[] classFile(String className) throws IOException {
		try (InputStream input = LOADER.getResourceAsStream(className.replace('.', '/') + ".class")) {
			return input.readAllBytes();
		}
	}

	/** The defects a hand-built class file can carry. */
	private enum Variant {
		/** A well-formed file. */
		VALID,
		/** Two NestMembers attributes. */
		TWO_NEST_MEMBERS,
		/** A NestMembers length that does not match its entries. */
		WRONG_LENGTH,
		/** NestMembers together with NestHost. */
		NEST_HOST_TOO,
		/** A member index pointing at a text instead of a class constant. */
		MEMBER_IS_TEXT,
		/** A constant with an unknown tag. */
		UNKNOWN_TAG,
		/** A string constant referring to index 0, beside a valid listing. */
		DANGLING_REFERENCE,
		/** A field-reading handle to a field. */
		FIELD_HANDLE,
		/** A constructor handle to a method named {@code <init>}. */
		CONSTRUCTOR_HANDLE,
		/** A virtual-method handle to a field. */
		METHOD_HANDLE_TO_FIELD,
		/** A virtual-method handle to a method named {@code <init>}. */
		VIRTUAL_HANDLE_TO_CONSTRUCTOR,
		/** An interface-method handle to a class method. */
		INTERFACE_HANDLE_TO_CLASS_METHOD
	}

	/**
	 * Builds a minimal class file for {@code x.T} whose nest lists {@code x.T$A},
	 * with the given defect.
	 */
	private static byte[] minimal(Variant variant) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bytes);
		out.writeInt(0xCAFEBABE);
		out.writeShort(0);
		out.writeShort(61);
		ByteArrayOutputStream extraBytes = new ByteArrayOutputStream();
		int extraSlots = writeExtraConstants(new DataOutputStream(extraBytes), variant);
		out.writeShort(9 + extraSlots);
		out.writeByte(1);
		out.writeUTF("x/T");
		out.writeByte(7);
		out.writeShort(1);
		out.writeByte(1);
		out.writeUTF("x/T$A");
		out.writeByte(7);
		out.writeShort(3);
		out.writeByte(1);
		out.writeUTF("NestMembers");
		out.writeByte(1);
		out.writeUTF("java/lang/Object");
		out.writeByte(7);
		out.writeShort(6);
		out.writeByte(1);
		out.writeUTF("NestHost");
		out.write(extraBytes.toByteArray());
		out.writeShort(0x21);
		out.writeShort(2);
		out.writeShort(7);
		out.writeShort(0);
		out.writeShort(0);
		out.writeShort(0);
		int attributes = variant == Variant.TWO_NEST_MEMBERS || variant == Variant.NEST_HOST_TOO ? 2 : 1;
		out.writeShort(attributes);
		writeNestMembers(out, variant);
		if (variant == Variant.TWO_NEST_MEMBERS) {
			writeNestMembers(out, Variant.VALID);
		}
		if (variant == Variant.NEST_HOST_TOO) {
			out.writeShort(8);
			out.writeInt(2);
			out.writeShort(2);
		}
		out.flush();
		return bytes.toByteArray();
	}

	/**
	 * Writes the constants a variant adds from index 9 on and returns how many
	 * slots they take.
	 */
	private static int writeExtraConstants(DataOutputStream out, Variant variant) throws IOException {
		switch (variant) {
		case UNKNOWN_TAG -> out.writeByte(99);
		case DANGLING_REFERENCE -> {
			out.writeByte(8);
			out.writeShort(0);
		}
		case FIELD_HANDLE, METHOD_HANDLE_TO_FIELD -> writeHandle(out, 9, "f", "I",
				variant == Variant.FIELD_HANDLE ? 1 : 5);
		case CONSTRUCTOR_HANDLE, VIRTUAL_HANDLE_TO_CONSTRUCTOR -> writeHandle(out, 10, "<init>", "()V",
				variant == Variant.CONSTRUCTOR_HANDLE ? 8 : 5);
		case INTERFACE_HANDLE_TO_CLASS_METHOD -> writeHandle(out, 10, "run", "()V", 9);
		default -> {
			return 0;
		}
		}
		return variant == Variant.UNKNOWN_TAG || variant == Variant.DANGLING_REFERENCE ? 1 : 5;
	}

	/**
	 * Writes a name, a descriptor, their pairing, a member reference with the given
	 * tag on class {@code x.T}, and a method handle of the given kind to it, at
	 * indices 9 to 13.
	 */
	private static void writeHandle(DataOutputStream out, int memberTag, String name, String descriptor, int kind)
			throws IOException {
		out.writeByte(1);
		out.writeUTF(name);
		out.writeByte(1);
		out.writeUTF(descriptor);
		out.writeByte(12);
		out.writeShort(9);
		out.writeShort(10);
		out.writeByte(memberTag);
		out.writeShort(2);
		out.writeShort(11);
		out.writeByte(15);
		out.writeByte(kind);
		out.writeShort(12);
	}

	/**
	 * Writes one NestMembers attribute listing {@code x.T$A}, with the variant's
	 * defect.
	 */
	private static void writeNestMembers(DataOutputStream out, Variant variant) throws IOException {
		out.writeShort(5);
		out.writeInt(variant == Variant.WRONG_LENGTH ? 6 : 4);
		out.writeShort(1);
		out.writeShort(variant == Variant.MEMBER_IS_TEXT ? 3 : 4);
		if (variant == Variant.WRONG_LENGTH) {
			out.writeShort(0);
		}
	}
}
