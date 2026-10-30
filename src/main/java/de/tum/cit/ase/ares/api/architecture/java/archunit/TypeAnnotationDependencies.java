package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.Source;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.AnnotationVisitor;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.ClassReader;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.ClassVisitor;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.FieldVisitor;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.Label;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.MethodVisitor;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.Opcodes;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.RecordComponentVisitor;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.Type;
import com.tngtech.archunit.thirdparty.org.objectweb.asm.TypePath;

import de.tum.cit.ase.ares.api.localization.Messages;
//</editor-fold>

/**
 * Finds the types a class names only through type annotations, such as
 * {@code @NonNull String}. ArchUnit does not import those annotations, so the
 * package-import rule would not see them. This class reads the class file
 * itself and reports every such type, together with the types inside the
 * annotation values, so the rule can treat them as imports.
 */
public final class TypeAnnotationDependencies {

	// <editor-fold desc="Attributes">
	/**
	 * Text that marks a class this check could not read. It is never translated, so
	 * the error parser finds it in every language and can report the unreadable
	 * class instead of an import.
	 */
	@Nonnull
	public static final String UNREADABLE_MARKER = "ARES_TYPE_ANNOTATION_UNREADABLE";

	/**
	 * The ASM API level the visitors use, the newest one the shaded ASM inside
	 * ArchUnit offers, so that records and current class files are read.
	 */
	private static final int ASM_API = Opcodes.ASM9;
	// </editor-fold>

	// <editor-fold desc="Constructor">
	/**
	 * Prevents instantiation, since this class only offers static helpers.
	 */
	private TypeAnnotationDependencies() {
		throw new SecurityException(Messages.localized("security.general.utility.initialization",
				TypeAnnotationDependencies.class.getSimpleName()));
	}
	// </editor-fold>

	// <editor-fold desc="Condition">
	/**
	 * Builds a condition that a class meets when one of its type annotations names
	 * a type from a forbidden package, or when its class file cannot be read. Used
	 * under {@code noClasses()}, every such class becomes a violation.
	 *
	 * @param forbiddenPackage tells whether a package name is forbidden
	 * @return the condition
	 */
	@Nonnull
	public static ArchCondition<JavaClass> useTypeAnnotationsFrom(@Nonnull Predicate<String> forbiddenPackage) {
		return new TypeAnnotationCondition(Objects.requireNonNull(forbiddenPackage, "forbiddenPackage"));
	}

	/**
	 * Adds the events for one class: one met event per forbidden type, one met
	 * event when the class cannot be read, and one unmet event when the class was
	 * read and names no forbidden type.
	 *
	 * @param javaClass        the class to check
	 * @param forbiddenPackage tells whether a package name is forbidden
	 * @param events           the events of the running rule
	 */
	static void reportTypeAnnotations(@Nonnull JavaClass javaClass, @Nonnull Predicate<String> forbiddenPackage,
			@Nonnull ConditionEvents events) {
		Set<String> namedTypes;
		try {
			namedTypes = typesNamedByTypeAnnotations(javaClass);
		} catch (IOException | RuntimeException unreadable) {
			events.add(SimpleConditionEvent.satisfied(javaClass, unreadableMessage(javaClass, unreadable)));
			return;
		}
		Set<String> forbiddenTypes = new TreeSet<>();
		for (String namedType : namedTypes) {
			if (forbiddenPackage.test(packageOf(namedType))) {
				forbiddenTypes.add(namedType);
			}
		}
		if (forbiddenTypes.isEmpty()) {
			events.add(SimpleConditionEvent.violated(javaClass,
					"Class <" + javaClass.getName() + "> uses no forbidden type annotation"));
			return;
		}
		for (String forbiddenType : forbiddenTypes) {
			events.add(SimpleConditionEvent.satisfied(javaClass, "Class <" + javaClass.getName() + "> depends on <"
					+ forbiddenType + "> through a type annotation in (" + sourceFileName(javaClass) + ":0)"));
		}
	}
	// </editor-fold>

	// <editor-fold desc="Class file reading">
	/**
	 * Reads the class file ArchUnit imported the class from and returns every type
	 * its type annotations name.
	 *
	 * @param javaClass the class to read
	 * @return the fully qualified names of the named types, sorted
	 * @throws IOException if the class has no class file or it cannot be read
	 */
	@Nonnull
	static Set<String> typesNamedByTypeAnnotations(@Nonnull JavaClass javaClass) throws IOException {
		URI classFile = javaClass.getSource().map(Source::getUri)
				.orElseThrow(() -> new IOException("no class file for " + javaClass.getName()));
		try (InputStream input = classFile.toURL().openStream()) {
			return typesNamedByTypeAnnotations(input.readAllBytes());
		}
	}

	/**
	 * Returns every type the type annotations in a class file name: the annotations
	 * themselves and the classes, enums and annotations inside their values, in any
	 * retention the class file keeps.
	 *
	 * @param classFile the bytes of a class file
	 * @return the fully qualified names of the named types, sorted
	 */
	@Nonnull
	static Set<String> typesNamedByTypeAnnotations(@Nonnull byte[] classFile) {
		Set<String> namedTypes = new TreeSet<>();
		new ClassReader(classFile).accept(new TypeAnnotationClassVisitor(namedTypes), ClassReader.SKIP_FRAMES);
		return namedTypes;
	}

	/**
	 * Returns the package of a fully qualified class name, or an empty string for
	 * the unnamed package. A nested class keeps the package of its outer class.
	 *
	 * @param className the fully qualified class name
	 * @return the package name
	 */
	@Nonnull
	static String packageOf(@Nonnull String className) {
		int lastDot = className.lastIndexOf('.');
		return lastDot < 0 ? "" : className.substring(0, lastDot);
	}

	/**
	 * Builds the report line for a class that could not be read. It starts with
	 * {@link #UNREADABLE_MARKER} and names the class and the cause.
	 *
	 * @param javaClass the class that could not be read
	 * @param cause     what went wrong
	 * @return the report line
	 */
	@Nonnull
	static String unreadableMessage(@Nonnull JavaClass javaClass, @Nonnull Exception cause) {
		return UNREADABLE_MARKER + " <" + javaClass.getName() + ">: " + cause;
	}

	/**
	 * Returns the source file name the class was compiled from, or its simple name
	 * with {@code .java} when the class file does not record one.
	 *
	 * @param javaClass the class
	 * @return the source file name
	 */
	@Nonnull
	private static String sourceFileName(@Nonnull JavaClass javaClass) {
		return javaClass.getSource().flatMap(Source::getFileName).orElse(javaClass.getSimpleName() + ".java");
	}

	/**
	 * Adds the type behind a descriptor type to the set: the class for an object
	 * type, the element class for an array, nothing for a primitive or void.
	 *
	 * @param type       the type
	 * @param namedTypes the set the class name is added to
	 */
	private static void addType(@Nonnull Type type, @Nonnull Set<String> namedTypes) {
		if (type.getSort() == Type.ARRAY) {
			addType(type.getElementType(), namedTypes);
		} else if (type.getSort() == Type.OBJECT) {
			namedTypes.add(type.getClassName());
		}
	}

	/**
	 * Adds the annotation type of a descriptor and returns a visitor that also
	 * collects the types inside the annotation values.
	 *
	 * @param descriptor the annotation descriptor
	 * @param namedTypes the set the names are added to
	 * @return the visitor for the annotation values
	 */
	@Nonnull
	private static AnnotationVisitor collectAnnotation(@Nonnull String descriptor, @Nonnull Set<String> namedTypes) {
		addType(Type.getType(descriptor), namedTypes);
		return new AnnotationValueVisitor(namedTypes);
	}
	// </editor-fold>

	// <editor-fold desc="Condition and visitors">
	/**
	 * The condition behind {@link #useTypeAnnotationsFrom(Predicate)}.
	 */
	private static final class TypeAnnotationCondition extends ArchCondition<JavaClass> {

		/**
		 * Tells whether a package name is forbidden.
		 */
		@Nonnull
		private final Predicate<String> forbiddenPackage;

		/**
		 * Creates the condition.
		 *
		 * @param forbiddenPackage tells whether a package name is forbidden
		 */
		private TypeAnnotationCondition(@Nonnull Predicate<String> forbiddenPackage) {
			super("use type annotations from forbidden packages");
			this.forbiddenPackage = forbiddenPackage;
		}

		/**
		 * Reports the forbidden type annotations of one class.
		 *
		 * @param javaClass the class to check
		 * @param events    the events of the running rule
		 */
		@Override
		public void check(@Nonnull JavaClass javaClass, @Nonnull ConditionEvents events) {
			reportTypeAnnotations(javaClass, forbiddenPackage, events);
		}
	}

	/**
	 * Visits a class and hands every type annotation it finds, on the class, its
	 * fields, methods, method bodies and record components, to the collector.
	 */
	private static final class TypeAnnotationClassVisitor extends ClassVisitor {

		/**
		 * The set the named types are added to.
		 */
		@Nonnull
		private final Set<String> namedTypes;

		/**
		 * Creates the visitor.
		 *
		 * @param namedTypes the set the named types are added to
		 */
		private TypeAnnotationClassVisitor(@Nonnull Set<String> namedTypes) {
			super(ASM_API);
			this.namedTypes = namedTypes;
		}

		/**
		 * Collects a type annotation on the class itself, for example on a type
		 * parameter, its bound, the superclass or an implemented interface.
		 */
		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
				boolean visible) {
			return collectAnnotation(descriptor, namedTypes);
		}

		/**
		 * Returns a visitor that collects the type annotations of a field.
		 */
		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			return new TypeAnnotationFieldVisitor(namedTypes);
		}

		/**
		 * Returns a visitor that collects the type annotations of a method and of the
		 * code inside it.
		 */
		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
				String[] exceptions) {
			return new TypeAnnotationMethodVisitor(namedTypes);
		}

		/**
		 * Returns a visitor that collects the type annotations of a record component.
		 */
		@Override
		public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
			return new TypeAnnotationRecordComponentVisitor(namedTypes);
		}
	}

	/**
	 * Collects the type annotations of a field.
	 */
	private static final class TypeAnnotationFieldVisitor extends FieldVisitor {

		/**
		 * The set the named types are added to.
		 */
		@Nonnull
		private final Set<String> namedTypes;

		/**
		 * Creates the visitor.
		 *
		 * @param namedTypes the set the named types are added to
		 */
		private TypeAnnotationFieldVisitor(@Nonnull Set<String> namedTypes) {
			super(ASM_API);
			this.namedTypes = namedTypes;
		}

		/**
		 * Collects a type annotation on the field type.
		 */
		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
				boolean visible) {
			return collectAnnotation(descriptor, namedTypes);
		}
	}

	/**
	 * Collects the type annotations of a method signature and of the instructions,
	 * exception handlers and local variables in its body.
	 */
	private static final class TypeAnnotationMethodVisitor extends MethodVisitor {

		/**
		 * The set the named types are added to.
		 */
		@Nonnull
		private final Set<String> namedTypes;

		/**
		 * Creates the visitor.
		 *
		 * @param namedTypes the set the named types are added to
		 */
		private TypeAnnotationMethodVisitor(@Nonnull Set<String> namedTypes) {
			super(ASM_API);
			this.namedTypes = namedTypes;
		}

		/**
		 * Collects a type annotation on the signature: a type parameter or its bound,
		 * the return type, the receiver, a parameter or a thrown exception.
		 */
		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
				boolean visible) {
			return collectAnnotation(descriptor, namedTypes);
		}

		/**
		 * Collects a type annotation on an instruction: a cast, {@code instanceof},
		 * {@code new}, a method reference or an explicit type argument.
		 */
		@Override
		public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor,
				boolean visible) {
			return collectAnnotation(descriptor, namedTypes);
		}

		/**
		 * Collects a type annotation on the type of a caught exception.
		 */
		@Override
		public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor,
				boolean visible) {
			return collectAnnotation(descriptor, namedTypes);
		}

		/**
		 * Collects a type annotation on a local or resource variable.
		 */
		@Override
		public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start,
				Label[] end, int[] index, String descriptor, boolean visible) {
			return collectAnnotation(descriptor, namedTypes);
		}
	}

	/**
	 * Collects the type annotations of a record component.
	 */
	private static final class TypeAnnotationRecordComponentVisitor extends RecordComponentVisitor {

		/**
		 * The set the named types are added to.
		 */
		@Nonnull
		private final Set<String> namedTypes;

		/**
		 * Creates the visitor.
		 *
		 * @param namedTypes the set the named types are added to
		 */
		private TypeAnnotationRecordComponentVisitor(@Nonnull Set<String> namedTypes) {
			super(ASM_API);
			this.namedTypes = namedTypes;
		}

		/**
		 * Collects a type annotation on the record component type.
		 */
		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
				boolean visible) {
			return collectAnnotation(descriptor, namedTypes);
		}
	}

	/**
	 * Collects the types inside annotation values: class literals, enum constants,
	 * nested annotations and arrays of any of them.
	 */
	private static final class AnnotationValueVisitor extends AnnotationVisitor {

		/**
		 * The set the named types are added to.
		 */
		@Nonnull
		private final Set<String> namedTypes;

		/**
		 * Creates the visitor.
		 *
		 * @param namedTypes the set the named types are added to
		 */
		private AnnotationValueVisitor(@Nonnull Set<String> namedTypes) {
			super(ASM_API);
			this.namedTypes = namedTypes;
		}

		/**
		 * Collects a class literal; primitive and string values name no type.
		 */
		@Override
		public void visit(String name, Object value) {
			if (value instanceof Type type) {
				addType(type, namedTypes);
			}
		}

		/**
		 * Collects the type of an enum constant.
		 */
		@Override
		public void visitEnum(String name, String descriptor, String value) {
			addType(Type.getType(descriptor), namedTypes);
		}

		/**
		 * Collects a nested annotation and the types inside its values.
		 */
		@Override
		public AnnotationVisitor visitAnnotation(String name, String descriptor) {
			return collectAnnotation(descriptor, namedTypes);
		}

		/**
		 * Returns a visitor that collects the types inside an array value.
		 */
		@Override
		public AnnotationVisitor visitArray(String name) {
			return new AnnotationValueVisitor(namedTypes);
		}
	}
	// </editor-fold>
}
