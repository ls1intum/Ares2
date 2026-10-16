package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.Handle;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.OpenedClassReader;

/**
 * Rewrites a method's calls like the given substitution, except the one call a
 * Java record uses to build its {@code toString}, {@code equals} and
 * {@code hashCode}. Byte Buddy cannot read that call and fails the whole class,
 * so a record in student code would fail every test. The call only reads the
 * record's own fields, so it cannot start a thread or wait on a monitor.
 *
 * @since 2.2.2
 * @author Luka Petrovic
 */
final class RecordMethodsPassThrough implements AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper {

	/**
	 * The class that builds a record's {@code toString}, {@code equals} and
	 * {@code hashCode}, in the slash form the class file uses.
	 */
	private static final String RECORD_METHODS_OWNER = "java/lang/runtime/ObjectMethods";

	/**
	 * The method of {@link #RECORD_METHODS_OWNER} the record's call starts from.
	 */
	private static final String RECORD_METHODS_BOOTSTRAP = "bootstrap";

	/**
	 * The substitution applied to every other call.
	 */
	@Nonnull
	private final MemberSubstitution substitution;

	/**
	 * Wraps a substitution.
	 *
	 * @param substitution the substitution applied to every call but the record's
	 */
	RecordMethodsPassThrough(@Nonnull MemberSubstitution substitution) {
		this.substitution = Objects.requireNonNull(substitution, "substitution must not be null");
	}

	/**
	 * Tells whether a dynamic call starts from the record method builder.
	 *
	 * @param bootstrap the method a dynamic call starts from
	 * @return {@code true} only for {@code ObjectMethods.bootstrap}
	 */
	static boolean isRecordMethodsBootstrap(@Nonnull Handle bootstrap) {
		return RECORD_METHODS_OWNER.equals(bootstrap.getOwner())
				&& RECORD_METHODS_BOOTSTRAP.equals(bootstrap.getName());
	}

	/**
	 * Puts the substitution in front of the method's visitor and lets the record's
	 * call go past it to that visitor unchanged.
	 *
	 * @param instrumentedType      the class being rewritten
	 * @param instrumentedMethod    the method being rewritten
	 * @param methodVisitor         the visitor that writes the method
	 * @param implementationContext the context of the rewrite
	 * @param typePool              the types the substitution resolves against
	 * @param writerFlags           the class writer's flags
	 * @param readerFlags           the class reader's flags
	 * @return the visitor the method's instructions are sent to
	 */
	@Override
	public MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod,
			MethodVisitor methodVisitor, Implementation.Context implementationContext, TypePool typePool,
			int writerFlags, int readerFlags) {
		MethodVisitor substituting = substitution.wrap(instrumentedType, instrumentedMethod, methodVisitor,
				implementationContext, typePool, writerFlags, readerFlags);
		return new RecordCallBypass(substituting, methodVisitor);
	}

	/**
	 * Sends every instruction to the substituting visitor, except the record's
	 * call, which goes straight to the visitor beneath it.
	 */
	private static final class RecordCallBypass extends MethodVisitor {

		/**
		 * The visitor beneath the substitution, which writes the method.
		 */
		@Nonnull
		private final MethodVisitor writer;

		/**
		 * Creates the visitor.
		 *
		 * @param substituting the visitor that applies the substitution
		 * @param writer       the visitor beneath it
		 */
		private RecordCallBypass(@Nonnull MethodVisitor substituting, @Nonnull MethodVisitor writer) {
			super(OpenedClassReader.ASM_API, substituting);
			this.writer = writer;
		}

		/**
		 * Lets the record's call past the substitution and sends any other dynamic call
		 * through it.
		 *
		 * @param name       the name of the dynamic call
		 * @param descriptor the type of the dynamic call
		 * @param bootstrap  the method the call starts from
		 * @param arguments  the constant arguments of that method
		 */
		@Override
		public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrap, Object... arguments) {
			if (isRecordMethodsBootstrap(bootstrap)) {
				writer.visitInvokeDynamicInsn(name, descriptor, bootstrap, arguments);
			} else {
				super.visitInvokeDynamicInsn(name, descriptor, bootstrap, arguments);
			}
		}
	}
}
