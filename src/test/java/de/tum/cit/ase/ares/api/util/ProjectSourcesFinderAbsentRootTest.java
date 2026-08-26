package de.tum.cit.ase.ares.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

/**
 * Covers what discovery does with a declared source root that is not there.
 * <p>
 * A build tool collects nothing from a source directory that does not exist, so
 * a descriptor naming one is legal and a project may create it later or never.
 * Discovery passes such a root over rather than ending the run, and records
 * that the source set is no longer known to be the whole of the project. What
 * still ends the run is a declared path that exists and is not a directory, and
 * one that leaves the project.
 */
@DisplayName("ProjectSourcesFinder absent declared roots")
class ProjectSourcesFinderAbsentRootTest {

	@TempDir
	Path projectRoot;

	private Logger finderLogger;

	private ListAppender<ILoggingEvent> appender;

	/**
	 * Collects what the finder logs, so a test can assert on the warning rather
	 * than only on the roots it returns.
	 */
	@BeforeEach
	void attachAppender() {
		finderLogger = (Logger) LoggerFactory.getLogger(ProjectSourcesFinder.class);
		appender = new ListAppender<>();
		appender.start();
		finderLogger.addAppender(appender);
	}

	/** Leaves the logger as it was found, so tests cannot affect one another. */
	@AfterEach
	void detachAppender() {
		finderLogger.detachAppender(appender);
		appender.stop();
	}

	// <editor-fold desc="Gradle">

	/**
	 * The roots that are there still scope enforcement. Only the certainty that
	 * they are all of them is lost, which is what the completeness flag carries.
	 */
	@Test
	@DisplayName("Keeps the present root of a replacement that also names an absent one")
	void keepsThePresentRootOfAReplacementNamingAnAbsentOne() throws IOException {
		Files.createDirectories(projectRoot.resolve("present"));
		writeGradle("sourceSets { main { java { srcDirs = ['present', 'absent'] } } }");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(List.of(projectRoot.resolve("present").toRealPath()), configuration.productionSourceRoots());
		assertFalse(configuration.productionRootsComplete(),
				"an entry that is not there means the discovered roots are not known to be the whole set");
	}

	/**
	 * The case this whole change turns on. A replacement clears the conventional
	 * root before its own operand is read, so passing over the only entry it names
	 * must leave the set empty. Answering with the convention instead would
	 * supervise a directory the descriptor had just replaced.
	 */
	@Test
	@DisplayName("Leaves the source set empty when the only declared root is absent")
	void leavesTheSourceSetEmptyWhenTheOnlyDeclaredRootIsAbsent() throws IOException {
		Files.createDirectories(projectRoot.resolve("src/main/java"));
		writeGradle("sourceSets { main { java { srcDirs = ['absent'] } } }");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(List.of(), configuration.productionSourceRoots(),
				"the replacement removed the conventional root, so it must not come back");
		assertFalse(configuration.productionRootsComplete());
	}

	/**
	 * A replacement supersedes the uncertainty before it as well as the roots, so a
	 * later one naming only roots that are there restores completeness.
	 */
	@Test
	@DisplayName("Lets a later fully present replacement restore completeness")
	void letsALaterFullyPresentReplacementRestoreCompleteness() throws IOException {
		Files.createDirectories(projectRoot.resolve("later"));
		writeGradle("""
				sourceSets {
				  main {
				    java {
				      srcDirs = ['absent']
				      srcDirs = ['later']
				    }
				  }
				}
				""");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(List.of(projectRoot.resolve("later").toRealPath()), configuration.productionSourceRoots());
		assertTrue(configuration.productionRootsComplete(), "the second replacement answered for the whole source set");
	}

	/**
	 * An addition adds, so passing over an absent one leaves what came before it
	 * standing. It can still only make the picture less complete.
	 */
	@Test
	@DisplayName("Keeps the conventional root when an addition is absent")
	void keepsTheConventionalRootWhenAnAdditionIsAbsent() throws IOException {
		Files.createDirectories(projectRoot.resolve("src/main/java"));
		writeGradle("sourceSets { main { java { srcDir 'absent' } } }");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(List.of(projectRoot.resolve("src/main/java").toRealPath()), configuration.productionSourceRoots());
		assertFalse(configuration.productionRootsComplete());
	}

	/**
	 * The two ways of not knowing a root compose. A token this reader cannot
	 * resolve and one it resolved but could not find both leave the source set
	 * incomplete, and neither cancels the other out.
	 */
	@Test
	@DisplayName("Stays incomplete when an unresolvable token accompanies an absent one")
	void staysIncompleteWhenAnUnresolvableTokenAccompaniesAnAbsentOne() throws IOException {
		Files.createDirectories(projectRoot.resolve("present"));
		writeGradle("sourceSets { main { java { srcDirs = ['present', \"${generated}/sources\", 'absent'] } } }");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(List.of(projectRoot.resolve("present").toRealPath()), configuration.productionSourceRoots());
		assertFalse(configuration.productionRootsComplete());
	}

	/**
	 * An addition can only ever make the picture less complete, so a later one
	 * naming roots that are all there does not undo what an absent one recorded.
	 * Only a replacement, which supersedes the uncertainty as well as the roots,
	 * can do that.
	 */
	@Test
	@DisplayName("Does not let a later complete addition undo what an absent one recorded")
	void doesNotLetALaterCompleteAdditionUndoWhatAnAbsentOneRecorded() throws IOException {
		Files.createDirectories(projectRoot.resolve("src/main/java"));
		Files.createDirectories(projectRoot.resolve("extra"));
		writeGradle("""
				sourceSets {
				  main {
				    java {
				      srcDir 'absent'
				      srcDir 'extra'
				    }
				  }
				}
				""");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(
				List.of(projectRoot.resolve("extra").toRealPath(), projectRoot.resolve("src/main/java").toRealPath()),
				configuration.productionSourceRoots(),
				"the conventional root is kept and the present addition joins it, sorted as the configuration sorts");
		assertFalse(configuration.productionRootsComplete(), "the absent addition still counts against the set");
	}

	/**
	 * A link pointing at nothing leads nowhere, so it is passed over like any other
	 * root that is not there. A link pointing at a directory is that directory, and
	 * containment is decided on what it resolves to rather than on the link, so it
	 * cannot be used to reach outside the project.
	 */
	@Test
	@DisplayName("Follows a link to a directory and passes over one pointing at nothing")
	void followsALinkToADirectoryAndPassesOverOnePointingAtNothing() throws IOException {
		assumeTrue(canCreateSymbolicLinks(), "needs a filesystem that allows symbolic links");
		Files.createSymbolicLink(projectRoot.resolve("dangling"), projectRoot.resolve("nowhere"));
		Files.createSymbolicLink(projectRoot.resolve("link"), Files.createDirectories(projectRoot.resolve("real")));
		writeGradle("sourceSets { main { java { srcDirs = ['link', 'dangling'] } } }");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(List.of(projectRoot.resolve("real").toRealPath()), configuration.productionSourceRoots());
		assertFalse(configuration.productionRootsComplete());
	}

	/**
	 * Containment is decided before presence, so a root outside the project is
	 * refused as the boundary violation it is rather than passed over as one more
	 * directory that happens not to be there.
	 */
	@Test
	@DisplayName("Refuses an absent root that leaves the project")
	void refusesAnAbsentRootThatLeavesTheProject() throws IOException {
		writeGradle("sourceSets { main { java { srcDir '../outside' } } }");

		assertThrows(SecurityException.class, () -> ProjectSourcesFinder.discover(projectRoot));
	}

	/**
	 * Completeness is tracked per source set and only the production one reaches
	 * the consumer, so an absent test root must not make the production roots look
	 * partial.
	 */
	@Test
	@DisplayName("Leaves production completeness alone when a test root is absent")
	void leavesProductionCompletenessAloneWhenATestRootIsAbsent() throws IOException {
		Files.createDirectories(projectRoot.resolve("src/main/java"));
		writeGradle("sourceSets { test { java { srcDir 'absent' } } }");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(List.of(projectRoot.resolve("src/main/java").toRealPath()), configuration.productionSourceRoots());
		assertTrue(configuration.productionRootsComplete());
	}

	/** The reader is told which entry was passed over, and in which descriptor. */
	@Test
	@DisplayName("Warns naming the entry passed over and the descriptor declaring it")
	void warnsNamingTheEntryAndTheDescriptor() throws IOException {
		writeGradle("sourceSets { main { java { srcDir 'absent' } } }");

		ProjectSourcesFinder.discover(projectRoot);

		String warnings = appender.list.stream().map(ILoggingEvent::getFormattedMessage).reduce("",
				(all, message) -> all + message + "\n");
		assertTrue(warnings.contains("absent"), "the entry passed over is not named: " + warnings);
		assertTrue(warnings.contains("build.gradle"), "the descriptor is not named: " + warnings);
	}

	// </editor-fold>

	// <editor-fold desc="Maven">

	/**
	 * A declared source directory replaces the convention in Maven too, so passing
	 * it over must not reactivate {@code src/main/java}, even where that directory
	 * is right there.
	 */
	@Test
	@DisplayName("Does not fall back to the convention when the declared Maven root is absent")
	void doesNotFallBackToTheConventionWhenTheDeclaredMavenRootIsAbsent() throws IOException {
		Files.createDirectories(projectRoot.resolve("src/main/java"));
		writePom("<sourceDirectory>absent</sourceDirectory>");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(List.of(), configuration.productionSourceRoots(),
				"the declaration replaced the convention, so the convention must not answer for it");
		assertFalse(configuration.productionRootsComplete());
	}

	/**
	 * Maven keeps the convention where nothing was declared, which is what makes
	 * the test above about the declaration rather than about the fallback.
	 */
	@Test
	@DisplayName("Keeps the Maven convention when nothing is declared")
	void keepsTheMavenConventionWhenNothingIsDeclared() throws IOException {
		Files.createDirectories(projectRoot.resolve("src/main/java"));
		writePom("");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(List.of(projectRoot.resolve("src/main/java").toRealPath()), configuration.productionSourceRoots());
		assertTrue(configuration.productionRootsComplete());
	}

	/**
	 * A path that is there and is not a directory is a configuration error under
	 * any reading, and it must be reported as one rather than as a descriptor that
	 * could not be parsed.
	 */
	@Test
	@DisplayName("Refuses a declared Maven root that is a file, saying so")
	void refusesADeclaredMavenRootThatIsAFile() throws IOException {
		Files.writeString(projectRoot.resolve("occupied"), "");
		writePom("<sourceDirectory>occupied</sourceDirectory>");

		IllegalStateException rejection = assertThrows(IllegalStateException.class,
				() -> ProjectSourcesFinder.discover(projectRoot));

		assertTrue(rejection.getMessage().contains("is not a directory"),
				"the diagnosis is about the directory, not about parsing: " + rejection.getMessage());
	}

	/**
	 * The same boundary as for Gradle, and a check that reading the descriptor no
	 * longer rewrites the refusal into a parsing failure.
	 */
	@Test
	@DisplayName("Refuses an absent Maven root that leaves the project")
	void refusesAnAbsentMavenRootThatLeavesTheProject() throws IOException {
		writePom("<sourceDirectory>../outside</sourceDirectory>");

		assertThrows(SecurityException.class, () -> ProjectSourcesFinder.discover(projectRoot));
	}

	// </editor-fold>

	/**
	 * Whether this filesystem lets the test create a symbolic link, which not every
	 * one the suite runs on does.
	 *
	 * @return whether a link could be created
	 */
	private boolean canCreateSymbolicLinks() {
		try {
			Path probe = projectRoot.resolve("link-probe");
			Files.createSymbolicLink(probe, projectRoot);
			Files.delete(probe);
			return true;
		} catch (IOException | UnsupportedOperationException | SecurityException refused) {
			return false;
		}
	}

	/**
	 * Writes the Gradle descriptor discovery will read.
	 *
	 * @param descriptor the contents of build.gradle
	 */
	private void writeGradle(String descriptor) throws IOException {
		Files.writeString(projectRoot.resolve("build.gradle"), descriptor);
	}

	/**
	 * Writes a Maven descriptor whose build section holds the given elements.
	 *
	 * @param buildElements the elements to place inside {@code <build>}
	 */
	private void writePom(String buildElements) throws IOException {
		Files.writeString(projectRoot.resolve("pom.xml"), "<project><build>" + buildElements + "</build></project>");
	}
}
