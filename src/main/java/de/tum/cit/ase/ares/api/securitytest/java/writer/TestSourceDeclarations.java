package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithImplements;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The classes declared in an exercise's test sources, read with a Java parser,
 * so a check that a class or method exists looks at real declarations rather
 * than at text that merely looks like one, such as a comment or a call.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 */
final class TestSourceDeclarations {

	/** The test source root every name is resolved below. */
	@Nonnull
	private final Path testFolderPath;

	/** Reads test sources at the exercise's language level. */
	@Nonnull
	private final JavaParser parser;

	/** Each source file parsed so far, so a file is read once. */
	@Nonnull
	private final Map<Path, CompilationUnit> parsedFiles = new HashMap<>();

	/** Declarations keyed by the names written in their Java packages. */
	@Nonnull
	private final Map<String, TypeDeclaration<?>> declarations = new HashMap<>();

	/** Whether the test source root has already been indexed. */
	private boolean indexed;

	/**
	 * Creates the declarations of one test source root.
	 *
	 * @param testFolderPath the test source root.
	 * @param languageLevel  the Java version the exercise compiles for.
	 */
	TestSourceDeclarations(@Nonnull Path testFolderPath, @Nonnull ParserConfiguration.LanguageLevel languageLevel) {
		this.testFolderPath = Objects.requireNonNull(testFolderPath, "testFolderPath must not be null");
		this.parser = new JavaParser(new ParserConfiguration()
				.setLanguageLevel(Objects.requireNonNull(languageLevel, "languageLevel must not be null")));
	}

	/**
	 * Finds a class by its declared canonical name. Every top-level declaration in
	 * each correctly placed source file is indexed, including secondary types.
	 *
	 * @param canonicalName the name with dots only, such as
	 *                      {@code pkg.Outer.Inner}.
	 * @return the class, or empty when no file and nesting match
	 * @throws SecurityException naming a source that cannot be read or parsed
	 */
	@Nonnull
	Optional<TypeDeclaration<?>> findClass(@Nonnull String canonicalName) {
		indexSources();
		return Optional.ofNullable(declarations.get(canonicalName));
	}

	/**
	 * Reads correctly placed test sources once and indexes their declared types.
	 */
	private void indexSources() {
		if (indexed) {
			return;
		}
		try (Stream<Path> files = Files.walk(testFolderPath)) {
			files.filter(file -> Files.isRegularFile(file) && file.toString().endsWith(".java")).sorted()
					.forEach(this::indexFile);
		} catch (IOException unreadable) {
			throw new SecurityException(
					Messages.localized("security.writer.hidden.tests.unparsable", testFolderPath.toString()),
					unreadable);
		}
		indexed = true;
	}

	/** Adds every top-level and member type from one correctly placed source. */
	private void indexFile(@Nonnull Path file) {
		CompilationUnit unit = parse(file);
		Path declaredFolder = unit.getPackageDeclaration().map(pkg -> Path.of(pkg.getNameAsString().replace('.', '/')))
				.orElse(Path.of(""));
		Path actualFolder = file.getParent();
		if (actualFolder == null
				|| !testFolderPath.resolve(declaredFolder).normalize().equals(actualFolder.normalize())) {
			return;
		}
		unit.getTypes().forEach(this::indexType);
	}

	/** Adds a declared type and the member types directly inside it. */
	private void indexType(@Nonnull TypeDeclaration<?> type) {
		type.getFullyQualifiedName().ifPresent(name -> declarations.put(name, type));
		memberClasses(type).forEach(this::indexType);
	}

	/** The annotations that make a method a JUnit test, by simple name. */
	private static final Set<String> TEST_ANNOTATIONS = Set.of("Test", "ParameterizedTest", "RepeatedTest",
			"TestFactory", "TestTemplate", "PublicTest", "HiddenTest");

	/**
	 * Whether a class declares a test method of that name or inherits one from a
	 * superclass or interface declared in the test sources. A test method carries a
	 * JUnit or Ares test annotation, or an annotation of the test sources marked
	 * with one. Methods of an enclosing class do not count.
	 *
	 * @param testClass  the class.
	 * @param methodName the method's name.
	 * @return true when it does
	 */
	boolean hasTestMethod(@Nonnull TypeDeclaration<?> testClass, @Nonnull String methodName) {
		Deque<TypeDeclaration<?>> pending = new ArrayDeque<>(List.of(testClass));
		Set<TypeDeclaration<?>> visited = new HashSet<>();
		while (!pending.isEmpty()) {
			TypeDeclaration<?> current = pending.pop();
			if (!visited.add(current)) {
				continue;
			}
			TypeDeclaration<?> declaring = current;
			if (current.getMethodsByName(methodName).stream().anyMatch(method -> isTest(method, declaring))) {
				return true;
			}
			supertypes(current).stream().map(supertype -> resolve(supertype, current)).flatMap(Optional::stream)
					.forEach(pending::push);
		}
		return false;
	}

	/**
	 * Whether a method carries a test annotation, directly or through an annotation
	 * of the test sources.
	 *
	 * @param method    the method.
	 * @param declaring the class that declares it.
	 * @return true when JUnit runs it as a test
	 */
	private boolean isTest(@Nonnull MethodDeclaration method, @Nonnull TypeDeclaration<?> declaring) {
		return method.getAnnotations().stream()
				.anyMatch(annotation -> marksATest(annotation, declaring, new HashSet<>()));
	}

	/**
	 * Whether an annotation is a test annotation, or one declared in the test
	 * sources that is itself marked with a test annotation, at any depth.
	 *
	 * @param annotation the annotation as written.
	 * @param context    the declaration it is written on.
	 * @param seen       the annotation declarations already followed.
	 * @return true when it makes a method a test
	 */
	private boolean marksATest(@Nonnull AnnotationExpr annotation, @Nonnull TypeDeclaration<?> context,
			@Nonnull Set<TypeDeclaration<?>> seen) {
		String name = annotation.getNameAsString();
		if (TEST_ANNOTATIONS.contains(name.substring(name.lastIndexOf('.') + 1))) {
			return true;
		}
		return candidateNames(name, context).stream().map(this::findClass).flatMap(Optional::stream).findFirst()
				.filter(AnnotationDeclaration.class::isInstance).filter(seen::add)
				.map(declared -> declared.getAnnotations().stream().anyMatch(meta -> marksATest(meta, declared, seen)))
				.orElse(Boolean.FALSE);
	}

	/**
	 * The classes declared directly inside a class.
	 *
	 * @param type the class.
	 * @return its member classes
	 */
	@Nonnull
	private static Stream<TypeDeclaration<?>> memberClasses(@Nonnull TypeDeclaration<?> type) {
		return type.getMembers().stream().filter(TypeDeclaration.class::isInstance)
				.map(member -> (TypeDeclaration<?>) member);
	}

	/**
	 * The superclass and interfaces a class names in its declaration.
	 *
	 * @param type the class.
	 * @return the named supertypes, possibly empty
	 */
	@Nonnull
	private static List<ClassOrInterfaceType> supertypes(@Nonnull TypeDeclaration<?> type) {
		List<ClassOrInterfaceType> supertypes = new ArrayList<>();
		if (type instanceof ClassOrInterfaceDeclaration declaration) {
			supertypes.addAll(declaration.getExtendedTypes());
		}
		if (type instanceof NodeWithImplements<?> implementing) {
			supertypes.addAll(implementing.getImplementedTypes());
		}
		return supertypes;
	}

	/**
	 * Resolves a supertype name to its declaration in the test sources, the way
	 * {@code javac} looks it up: classes nested in the declaring class and its
	 * enclosing classes, single imports, the same package, the name as written,
	 * then wildcard imports. Static imports count too, since they can bring in a
	 * nested class; a name that is not a type in the test sources matches nothing.
	 *
	 * @param supertype the name in the declaration.
	 * @param declaring the class whose declaration names it.
	 * @return the declaration, or empty when it is not in the test sources
	 */
	@Nonnull
	private Optional<TypeDeclaration<?>> resolve(@Nonnull ClassOrInterfaceType supertype,
			@Nonnull TypeDeclaration<?> declaring) {
		String written = supertype.getNameWithScope();
		return candidateNames(written, declaring).stream().map(this::findClass).flatMap(Optional::stream).findFirst();
	}

	/**
	 * The canonical names a written type name may stand for, most specific first.
	 *
	 * @param written   the name as written, such as {@code Base} or
	 *                  {@code Outer.Base}.
	 * @param declaring the class whose declaration names it.
	 * @return the candidate canonical names
	 */
	@Nonnull
	private static List<String> candidateNames(@Nonnull String written, @Nonnull TypeDeclaration<?> declaring) {
		String firstSegment = written.split("\\.", 2)[0];
		String rest = written.substring(firstSegment.length());
		List<String> candidates = new ArrayList<>();
		enclosingClasses(declaring)
				.filter(type -> memberClasses(type).anyMatch(member -> member.getNameAsString().equals(firstSegment)))
				.map(TypeDeclaration::getFullyQualifiedName).flatMap(Optional::stream)
				.forEach(enclosing -> candidates.add(enclosing + "." + written));
		Optional<CompilationUnit> unit = declaring.findCompilationUnit();
		List<ImportDeclaration> imports = unit.<List<ImportDeclaration>>map(CompilationUnit::getImports)
				.orElse(List.of());
		imports.stream().filter(importDeclaration -> !importDeclaration.isAsterisk())
				.map(ImportDeclaration::getNameAsString)
				.filter(name -> name.equals(firstSegment) || name.endsWith("." + firstSegment))
				.forEach(name -> candidates.add(name + rest));
		unit.flatMap(CompilationUnit::getPackageDeclaration)
				.ifPresent(packageDeclaration -> candidates.add(packageDeclaration.getNameAsString() + "." + written));
		candidates.add(written);
		imports.stream().filter(ImportDeclaration::isAsterisk)
				.forEach(importDeclaration -> candidates.add(importDeclaration.getNameAsString() + "." + written));
		return candidates;
	}

	/**
	 * A class followed by every class it is nested in, innermost first.
	 *
	 * @param type the class.
	 * @return the class and its enclosing classes
	 */
	@Nonnull
	private static Stream<TypeDeclaration<?>> enclosingClasses(@Nonnull TypeDeclaration<?> type) {
		return Stream.<Node>iterate(type, Objects::nonNull, node -> node.getParentNode().orElse(null))
				.filter(TypeDeclaration.class::isInstance).map(node -> (TypeDeclaration<?>) node);
	}

	/**
	 * Parses a test source once.
	 *
	 * @param file the source file.
	 * @return its syntax tree
	 * @throws SecurityException naming the file when it cannot be read or parsed
	 */
	@Nonnull
	private CompilationUnit parse(@Nonnull Path file) {
		CompilationUnit known = parsedFiles.get(file);
		if (known != null) {
			return known;
		}
		ParseResult<CompilationUnit> result;
		try {
			result = parser.parse(file);
		} catch (IOException unreadable) {
			throw new SecurityException(Messages.localized("security.writer.hidden.tests.unparsable", file.toString()),
					unreadable);
		}
		CompilationUnit unit = result.getResult().filter(parsed -> result.isSuccessful())
				.orElseThrow(() -> new SecurityException(
						Messages.localized("security.writer.hidden.tests.unparsable", file.toString())));
		parsedFiles.put(file, unit);
		return unit;
	}
}
