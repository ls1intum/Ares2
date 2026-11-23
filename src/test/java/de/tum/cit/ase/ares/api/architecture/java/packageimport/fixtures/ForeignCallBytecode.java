package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;

/**
 * Writes a class whose only method calls one interface method of
 * {@code java.lang.foreign}. The foreign API cannot be compiled against Java
 * 17, so the call is written as bytecode; the static rules read bytecode, so
 * they see it as they would see compiled student code.
 */
public final class ForeignCallBytecode {

	/** The name of the generated class. */
	public static final String CLASS_NAME = "com.example.ForeignUser";

	/** Prevents instantiation of this helper. */
	private ForeignCallBytecode() {
		throw new UnsupportedOperationException("ForeignCallBytecode is a utility class");
	}

	/**
	 * Writes the class into a folder and imports it.
	 *
	 * @param folder     an empty folder to write the class file into
	 * @param owner      the internal name of the interface, such as
	 *                   {@code java/lang/foreign/MemorySegment}
	 * @param method     the name of the called method
	 * @param descriptor the descriptor of the called method
	 * @return the imported class
	 * @throws IOException if the class file cannot be written
	 */
	public static JavaClasses importCallTo(Path folder, String owner, String method, String descriptor)
			throws IOException {
		Path classFile = Files.createDirectories(folder.resolve("com").resolve("example")).resolve("ForeignUser.class");
		Files.write(classFile, bytecode(owner, method, descriptor));
		return new ClassFileImporter().importPath(folder);
	}

	/**
	 * Builds the bytes of the class. The receiver and the arguments are nulls and
	 * zeros of the right width, since only the call site is read.
	 *
	 * @param owner      the internal name of the interface
	 * @param method     the name of the called method
	 * @param descriptor the descriptor of the called method
	 * @return the class file bytes
	 */
	private static byte[] bytecode(String owner, String method, String descriptor) {
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, CLASS_NAME.replace('.', '/'), null, "java/lang/Object", null);
		MethodVisitor visitor = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "run", "()V", null, null);
		visitor.visitCode();
		visitor.visitInsn(Opcodes.ACONST_NULL);
		pushArguments(visitor, descriptor);
		visitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, owner, method, descriptor, true);
		visitor.visitInsn(descriptor.endsWith(")V") ? Opcodes.NOP : Opcodes.POP);
		visitor.visitInsn(Opcodes.RETURN);
		visitor.visitMaxs(0, 0);
		visitor.visitEnd();
		writer.visitEnd();
		return writer.toByteArray();
	}

	/**
	 * Pushes a placeholder for every parameter in the descriptor.
	 *
	 * @param visitor    the method being written
	 * @param descriptor the descriptor of the called method
	 */
	private static void pushArguments(MethodVisitor visitor, String descriptor) {
		for (Type parameter : Type.getArgumentTypes(descriptor)) {
			switch (parameter.getSort()) {
			case Type.LONG -> visitor.visitInsn(Opcodes.LCONST_0);
			case Type.DOUBLE -> visitor.visitInsn(Opcodes.DCONST_0);
			case Type.FLOAT -> visitor.visitInsn(Opcodes.FCONST_0);
			case Type.OBJECT, Type.ARRAY -> visitor.visitInsn(Opcodes.ACONST_NULL);
			default -> visitor.visitInsn(Opcodes.ICONST_0);
			}
		}
	}
}
