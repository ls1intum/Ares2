package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithImplements;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The classes declared in an exercise's test sources, read with a Java parser,
 * so a check that a class or method exists looks at real declarations rather
 * than at text that merely looks like one, such as a comment or a call.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
final class TestSourceDeclarations {

	/** The test source root every name is resolved below. */
	@Nonnull
	private final Path testFolderPath;

	/** Reads test sources at the language level Ares supports. */
	@Nonnull
	private final JavaParser parser = new JavaParser(
			new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17));

	/** Each source file parsed so far, so a file is read once. */
	@Nonnull
	private final Map<Path, CompilationUnit> parsedFiles = new HashMap<>();

	/**
	 * Creates the declarations of one test source root.
	 *
	 * @param testFolderPath the test source root.
	 */
	TestSourceDeclarations(@Nonnull Path testFolderPath) {
		this.testFolderPath = Objects.requireNonNull(testFolderPath, "testFolderPath must not be null");
	}

	/**
	 * Finds a class by its canonical name: the shortest prefix that names a source
	 * file, then each later segment as a class declared directly inside the one
	 * before.
	 *
	 * @param canonicalName the name with dots only, such as
	 *                      {@code pkg.Outer.Inner}.
	 * @return the class, or empty when no file and nesting match
	 * @throws SecurityException naming a source that cannot be read or parsed
	 */
	@Nonnull
	Optional<TypeDeclaration<?>> findClass(@Nonnull String canonicalName) {
		List<String> segments = Arrays.asList(canonicalName.split("\\."));
		for (int fileSegment = 0; fileSegment < segments.size(); fileSegment++) {
			Path file = testFolderPath.resolve(String.join("/", segments.subList(0, fileSegment + 1)) + ".java");
			if (Files.isRegularFile(file)) {
				return nestedClass(parse(file).getTypes().stream(), segments.subList(fileSegment, segments.size()));
			}
		}
		return Optional.empty();
	}

	/**
	 * Whether a class declares a method of that name or inherits one from a
	 * superclass or interface declared in the test sources. Methods of an enclosing
	 * class do not count.
	 *
	 * @param testClass  the class.
	 * @param methodName the method's name.
	 * @return true when it does
	 */
	boolean hasMethod(@Nonnull TypeDeclaration<?> testClass, @Nonnull String methodName) {
		Deque<TypeDeclaration<?>> pending = new ArrayDeque<>(List.of(testClass));
		Set<TypeDeclaration<?>> visited = new HashSet<>();
		while (!pending.isEmpty()) {
			TypeDeclaration<?> current = pending.pop();
			if (!visited.add(current)) {
				continue;
			}
			if (!current.getMethodsByName(methodName).isEmpty()) {
				return true;
			}
			supertypes(current).stream().map(supertype -> resolve(supertype, current)).flatMap(Optional::stream)
					.forEach(pending::push);
		}
		return false;
	}

	/**
	 * Descends from a set of classes into nested classes by name.
	 *
	 * @param candidates the classes the first segment is looked up in.
	 * @param segments   the remaining simple names, outermost first.
	 * @return the innermost class, or empty when a segment matches nothing
	 */
	@Nonnull
	private static Optional<TypeDeclaration<?>> nestedClass(@Nonnull Stream<TypeDeclaration<?>> candidates,
			@Nonnull List<String> segments) {
		Optional<TypeDeclaration<?>> found = candidates.filter(type -> type.getNameAsString().equals(segments.get(0)))
				.findFirst();
		if (found.isEmpty() || segments.size() == 1) {
			return found;
		}
		return nestedClass(memberClasses(found.get()), segments.subList(1, segments.size()));
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
	 * then wildcard imports.
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
		imports.stream().filter(importDeclaration -> !importDeclaration.isAsterisk() && !importDeclaration.isStatic())
				.map(ImportDeclaration::getNameAsString)
				.filter(name -> name.equals(firstSegment) || name.endsWith("." + firstSegment))
				.forEach(name -> candidates.add(name + rest));
		unit.flatMap(CompilationUnit::getPackageDeclaration)
				.ifPresent(packageDeclaration -> candidates.add(packageDeclaration.getNameAsString() + "." + written));
		candidates.add(written);
		imports.stream().filter(importDeclaration -> importDeclaration.isAsterisk() && !importDeclaration.isStatic())
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
