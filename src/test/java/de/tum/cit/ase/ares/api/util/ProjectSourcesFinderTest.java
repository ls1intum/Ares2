package de.tum.cit.ase.ares.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;

class ProjectSourcesFinderTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void discoversMavenCustomRoots() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.createDirectories(temporaryDirectory.resolve("checks"));
		Files.writeString(temporaryDirectory.resolve("pom.xml"), """
				<project><build><sourceDirectory>${project.basedir}/assignment</sourceDirectory>
				<testSourceDirectory>${project.basedir}/checks</testSourceDirectory></build></project>
				""");
		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);
		assertEquals(BuildMode.MAVEN, configuration.buildMode());
		assertEquals(temporaryDirectory.resolve("assignment").toRealPath(),
				configuration.productionSourceRoots().get(0));
		assertEquals(temporaryDirectory.resolve("checks").toRealPath(), configuration.testSourceRoots().get(0));
	}

	@Test
	void discoversGroovyAndKotlinGradleRoots() throws IOException {
		for (String descriptor : new String[] { "build.gradle", "build.gradle.kts" }) {
			Path root = Files.createDirectory(temporaryDirectory.resolve(descriptor.replace('.', '-')));
			Files.createDirectories(root.resolve("assignment"));
			Files.createDirectories(root.resolve("checks"));
			Files.writeString(root.resolve("gradle.properties"), "assignmentPath=assignment\n");
			Files.writeString(root.resolve(descriptor), """
					sourceSets {
					  main { java { srcDir(assignmentPath) } }
					  test { java { srcDir 'checks' } }
					}
					""");
			var configuration = ProjectSourcesFinder.discover(root);
			assertEquals(BuildMode.GRADLE, configuration.buildMode());
			assertEquals(root.resolve("assignment").toRealPath(), configuration.productionSourceRoots().get(0));
			assertEquals(root.resolve("checks").toRealPath(), configuration.testSourceRoots().get(0));
		}
	}

	/**
	 * Regression: 'srcDir' is a prefix of 'srcDirs', so an alternation that tried
	 * the singular form first consumed it and captured the leftover "s = [", which
	 * resolved to nothing. Every srcDirs declaration was dropped without a trace,
	 * and a project declaring its main sources that way looked like a project with
	 * no production sources at all.
	 */
	@Test
	void discoversGradleRootsDeclaredWithSrcDirsList() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment/src"));
		Files.createDirectories(temporaryDirectory.resolve("test"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				def assignmentSrcDir = "assignment/src"
				sourceSets {
				  test { java { srcDir 'test' } }
				  main { java { srcDirs = [assignmentSrcDir] } }
				}
				""");
		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);
		assertEquals(temporaryDirectory.resolve("assignment/src").toRealPath(),
				configuration.productionSourceRoots().get(0));
		assertEquals(temporaryDirectory.resolve("test").toRealPath(), configuration.testSourceRoots().get(0));
	}

	@Test
	void discoversGradleRootsFromLiteralListsAndAppendedAssignments() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("first"));
		Files.createDirectories(temporaryDirectory.resolve("second"));
		Files.createDirectories(temporaryDirectory.resolve("appended"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs = ['first', 'second'] } }
				  test { java { srcDirs += ['appended'] } }
				}
				""");
		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);
		assertEquals(2, configuration.productionSourceRoots().size());
		assertEquals(temporaryDirectory.resolve("first").toRealPath(), configuration.productionSourceRoots().get(0));
		assertEquals(temporaryDirectory.resolve("second").toRealPath(), configuration.productionSourceRoots().get(1));
		assertEquals(temporaryDirectory.resolve("appended").toRealPath(), configuration.testSourceRoots().get(0));
	}

	@Test
	void rejectsAmbiguousMissingAndEscapingProjects() throws IOException {
		Files.writeString(temporaryDirectory.resolve("pom.xml"), "<project/>");
		Files.writeString(temporaryDirectory.resolve("build.gradle"), "plugins { id 'java' }");
		assertThrows(IllegalStateException.class, () -> ProjectSourcesFinder.discover(temporaryDirectory));
		assertEquals(BuildMode.GRADLE, ProjectSourcesFinder.discover(temporaryDirectory, BuildMode.GRADLE).buildMode());
		Files.delete(temporaryDirectory.resolve("pom.xml"));
		Path escape = temporaryDirectory.resolveSibling(temporaryDirectory.getFileName() + "-escape");
		Files.createDirectory(escape);
		Files.writeString(temporaryDirectory.resolve("build.gradle"),
				"sourceSets { main { java { srcDir '../" + escape.getFileName() + "' } } }");
		assertThrows(SecurityException.class,
				() -> ProjectSourcesFinder.discover(temporaryDirectory, BuildMode.GRADLE));
	}

	/**
	 * Regression: an occurrence of srcDirs that Gradle never executes used to be
	 * read as a declaration. Since a declared root that is not a directory is
	 * rejected, a commented-out line, or one inside a string, aborted discovery for
	 * a descriptor Gradle accepts.
	 */
	@Test
	void ignoresSourceDirectoriesInsideCommentsAndStrings() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDir 'assignment' } }
				}

				/*
				srcDirs = ['old/path']
				*/
				println "srcDirs = ['not/a/real/dir']"
				// srcDirs = ['commented/out']
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("assignment").toRealPath()),
				configuration.productionSourceRoots());
	}

	/**
	 * Regression: the source set used to be tracked per line, so a declaration
	 * sharing its line with the block that opens it was attributed to whatever the
	 * previous line left behind. A test root then counted as production, which
	 * feeds the supervised-package vote directly.
	 */
	@Test
	void attributesSourceSetsDeclaredOnASingleLine() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.createDirectories(temporaryDirectory.resolve("testsources"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets { main { java { srcDirs = ['assignment'] } } }
				sourceSets { test { java { srcDirs = ['testsources'] } } }
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("assignment").toRealPath()),
				configuration.productionSourceRoots());
		assertEquals(List.of(temporaryDirectory.resolve("testsources").toRealPath()), configuration.testSourceRoots());
	}

	/**
	 * Regression: the source set used to be decided by asking whether the line
	 * contained the text "test" anywhere, so a main source root whose own path
	 * contains it, such as 'contest', became a test root and production was left
	 * empty.
	 */
	@Test
	void classifiesBySourceSetNameRatherThanBySubstring() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("contest"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDir 'contest' } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("contest").toRealPath()),
				configuration.productionSourceRoots());
		assertTrue(configuration.testSourceRoots().isEmpty());
	}

	/**
	 * A resources directory is not a Java source root, and JavaProjectScanner reads
	 * every .java file under a production root without asking Gradle whether it
	 * treats the directory as Java source. Attributing one would therefore let
	 * files Gradle does not compile influence the supervised package.
	 */
	@Test
	void ignoresDirectoriesDeclaredOutsideTheJavaBlock() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.createDirectories(temporaryDirectory.resolve("assets"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main {
				    java { srcDir 'assignment' }
				    resources { srcDir 'assets' }
				  }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("assignment").toRealPath()),
				configuration.productionSourceRoots());
	}

	/**
	 * A block called main that is not a source set declares no source root. Without
	 * the enclosing sourceSets requirement any such block would, and the name is
	 * common enough in build logic for that to matter.
	 */
	@Test
	void ignoresSourceSetNamesOutsideTheSourceSetsBlock() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("src/main/java"));
		Files.createDirectories(temporaryDirectory.resolve("elsewhere"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				application {
				  main { java { srcDir 'elsewhere' } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("src/main/java").toRealPath()),
				configuration.productionSourceRoots());
	}

	/**
	 * Gradle's srcDir and srcDirs += add to the source set rather than defining it,
	 * so the conventional root stays. Dropping it, as an accumulate-everything
	 * reading does, silently narrows the supervised scope to the added directory.
	 */
	@Test
	void keepsTheConventionalRootWhenADeclarationOnlyAdds() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("src/main/java"));
		Files.createDirectories(temporaryDirectory.resolve("generated"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs += ['generated'] } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		// BuildToolConfiguration sorts the roots, so this is both of them rather than
		// the order they were declared in.
		assertEquals(
				List.of(temporaryDirectory.resolve("generated").toRealPath(),
						temporaryDirectory.resolve("src/main/java").toRealPath()),
				configuration.productionSourceRoots());
	}

	/**
	 * An assignment replaces what came before it, so two of them are not two source
	 * roots. Accumulating them keeps a directory the descriptor has already
	 * discarded, and the conventional root goes with the first replacement.
	 */
	@Test
	void appliesRepeatedAssignmentsInOrderSoTheLastOneWins() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("src/main/java"));
		Files.createDirectories(temporaryDirectory.resolve("discarded"));
		Files.createDirectories(temporaryDirectory.resolve("effective"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main {
				    java { srcDirs = ['discarded'] }
				    java { srcDirs = ['effective'] }
				  }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("effective").toRealPath()),
				configuration.productionSourceRoots());
	}

	/**
	 * Regression: the operand's bounds were found on the mask but its text was read
	 * from the original, so a comment inside a live list came back as part of the
	 * path. Since the assignment had already cleared the conventional root, the
	 * source set was then left empty, which is the same silent failure this reader
	 * exists to remove.
	 */
	@Test
	void readsAPathBesideACommentInsideALiveList() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs = [ /* the old one */ 'assignment' /* and that is all */ ] } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("assignment").toRealPath()),
				configuration.productionSourceRoots());
		assertTrue(configuration.productionRootsComplete(), "every token resolved, so the roots are the whole set");
	}

	/**
	 * Regression: a newline terminated the operand, so a list written over several
	 * lines, which is ordinary Gradle, cleared the conventional root and then
	 * declared nothing.
	 */
	@Test
	void readsAListWrittenOverSeveralLines() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.createDirectories(temporaryDirectory.resolve("generated"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main {
				    java {
				      srcDirs = [
				        'assignment',
				        // the generated one is added by the codegen task
				        'generated'
				      ]
				    }
				  }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("assignment").toRealPath(),
				temporaryDirectory.resolve("generated").toRealPath()), configuration.productionSourceRoots());
		assertTrue(configuration.productionRootsComplete());
	}

	@Test
	void keepsAnArgumentCommaOutOfTheSourceRootList() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs = [files('a', 'b'), 'assignment'] } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		// The comma inside files(...) separates arguments rather than source roots, so
		// the operand is two entries. The first is not a static path and leaves the set
		// incomplete; the second is, and is declared.
		assertEquals(List.of(temporaryDirectory.resolve("assignment").toRealPath()),
				configuration.productionSourceRoots());
		assertTrue(!configuration.productionRootsComplete(),
				"files('a', 'b') was not resolved, so what was found is not the whole source set");
	}

	@Test
	void readsADeclarationWhoseOperatorIsHiddenBehindAComment() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs /* replaced on purpose */ = ['assignment'] } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("assignment").toRealPath()),
				configuration.productionSourceRoots());
	}

	@Test
	void marksTheSourceSetIncompleteWhenAValueIsComputed() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("src/main/java"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs = someList } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		// The declaration is valid Gradle this reader cannot evaluate. Rejecting the
		// project would break it for no security gain, and answering the conventional
		// root would name one the descriptor replaced, so the uncertainty is recorded
		// and travels to whoever would otherwise trust the roots.
		assertTrue(!configuration.productionRootsComplete());
	}

	@Test
	void marksTheSourceSetIncompleteWhenTheListIsExtended() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("known"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs = ['known'] + generatedRoots } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("known").toRealPath()), configuration.productionSourceRoots());
		assertTrue(!configuration.productionRootsComplete(),
				"'known' resolved, but the roots added after it did not, so this is part of the source set");
	}

	@Test
	void marksTheSourceSetIncompleteWhenAnExpressionContinuesOnTheNextLine() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("known"));
		Files.writeString(temporaryDirectory.resolve("build.gradle.kts"), """
				sourceSets {
				  main { java { srcDirs = listOf("known")
				      .plus(generatedSourceDir) } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		// A line break decides nothing: the expression continues on the next line, and
		// treating the newline as the end of the statement would drop .plus(...) while
		// reporting a complete source set.
		assertTrue(!configuration.productionRootsComplete());
	}

	@Test
	void marksTheSourceSetIncompleteWhenAPathIsInterpolated() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("generated"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs = ["${generatedRoot}"] } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		// Taking the text literally would name a directory called ${generatedRoot}.
		// That normally does not exist and is rejected, but a project that happened to
		// contain one would have been supervised over the wrong tree without a word.
		assertTrue(!configuration.productionRootsComplete());
	}

	@Test
	void treatsAnEmptyListAsAnEmptySourceSetRatherThanAnUnreadableOne() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("src/main/java"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs = [ /* deliberately none */ ] } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertTrue(configuration.productionSourceRoots().isEmpty());
		assertTrue(configuration.productionRootsComplete(),
				"an empty list is a source set that declares nothing, which is known rather than unreadable");
	}

	@Test
	void letsALaterResolvedAssignmentRestoreCompleteness() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("effective"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main {
				    java { srcDirs = someList }
				    java { srcDirs = ['effective'] }
				  }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		// The second assignment replaces whatever the first left behind, uncertainty
		// included, so the roots are once again the whole of the source set.
		assertEquals(List.of(temporaryDirectory.resolve("effective").toRealPath()),
				configuration.productionSourceRoots());
		assertTrue(configuration.productionRootsComplete());
	}

	@Test
	void marksTheSourceSetIncompleteWhenAnAdditionCannotBeResolved() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("src/main/java"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs += generatedRoots } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		// The conventional root still resolves, so the roots are not empty and nothing
		// looks wrong. That is exactly why an unresolved addition has to be recorded:
		// a package vote taken over what was found would answer confidently and leave
		// the added tree unsupervised.
		assertEquals(List.of(temporaryDirectory.resolve("src/main/java").toRealPath()),
				configuration.productionSourceRoots());
		assertTrue(!configuration.productionRootsComplete());
	}

	/**
	 * A Groovy slashy string is a string, so a declaration written inside one is
	 * text rather than code. Masking it is what stops a printed example becoming a
	 * source root, and since a declared root that is not a directory is rejected,
	 * failing to mask it would abort discovery for a descriptor Gradle accepts.
	 */
	@Test
	void ignoresSourceDirectoriesInsideASlashyString() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		// The decoy exists and the string sits inside the java block, so a failure to
		// mask it would not merely be tolerated: the assignment would be replaced by
		// it, and the test would say so.
		Files.createDirectories(temporaryDirectory.resolve("decoy"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main {
				    java {
				      srcDirs = ['assignment']
				      println(/srcDirs = ['decoy']/)
				    }
				  }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("assignment").toRealPath()),
				configuration.productionSourceRoots());
		assertTrue(configuration.productionRootsComplete());
	}

	/**
	 * The dollar-slashy form is the one Gradle users reach for when a value
	 * contains slashes, which paths do, so it is exactly where a decoy declaration
	 * would sit most plausibly.
	 */
	@Test
	void ignoresSourceDirectoriesInsideADollarSlashyString() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.createDirectories(temporaryDirectory.resolve("decoy"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main {
				    java {
				      srcDirs = ['assignment']
				      def documentation = $/srcDirs = ['decoy']/$
				    }
				  }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("assignment").toRealPath()),
				configuration.productionSourceRoots());
		assertTrue(configuration.productionRootsComplete());
	}

	/**
	 * The other half of reading a slash: division is not a string, and the reader
	 * has to decide which it is from the character before it. This covers the
	 * decision returning "not a string"; its assertion is weaker than the two
	 * above, because an unterminated slashy string already stops at the line break,
	 * so a wrong answer here would not by itself hide the declaration below.
	 */
	@Test
	void readsDeclarationsAfterADivision() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				def half = 10 / 2

				sourceSets {
				  main { java { srcDirs = ['assignment'] } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("assignment").toRealPath()),
				configuration.productionSourceRoots());
		assertTrue(configuration.productionRootsComplete());
	}

	/**
	 * An unterminated single-line string must end at the line break rather than
	 * swallowing the rest of the descriptor, or one stray quote would hide every
	 * declaration below it.
	 */
	@Test
	void readsDeclarationsAfterAnUnterminatedString() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				// a quote that never closes: it's here

				sourceSets {
				  main { java { srcDirs = ['assignment'] } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(List.of(temporaryDirectory.resolve("assignment").toRealPath()),
				configuration.productionSourceRoots());
	}
}
