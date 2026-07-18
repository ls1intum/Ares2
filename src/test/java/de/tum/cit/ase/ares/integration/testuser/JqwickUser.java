package de.tum.cit.ase.ares.integration.testuser;

import static de.tum.cit.ase.ares.api.io.OutputTestOptions.DONT_IGNORE_LAST_EMPTY_LINE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import de.tum.cit.ase.ares.api.*;
import de.tum.cit.ase.ares.api.io.*;
import de.tum.cit.ase.ares.api.jqwik.*;
import de.tum.cit.ase.ares.api.localization.UseLocale;

@Hidden
@UseLocale("de")
@SuppressWarnings("static-method")
@Deadline("2200-01-01 16:00")
// Replaces the former inert @TrustedThreads(ALL_THREADS) and the two @WhitelistPath rules with the
// equivalent active @Policy, scanned against the benign HelloWorld subject.
@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyJqwickUser.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
public class JqwickUser {

	@Example
	@Deadline("2200-01-01 16:00")
	void exampleHiddenCustomDeadlineFuture() {
		// nothing
	}

	@Hidden
	@Example
	@Deadline("2000-01-01 16:00")
	void exampleHiddenCustomDeadlinePast() {
		// nothing
	}

	@Example
	void exampleHiddenNormal() {
		// nothing
	}

	@Public
	@Example
	@Deadline("2200-01-01 16:00")
	void examplePublicCustomDeadline() {
		// nothing
	}

	@Public
	@Example
	void examplePublicNormal() {
		// nothing
	}

	@Hidden
	@Property
	@Deadline("2200-01-01 16:00")
	boolean propertyHiddenCustomDeadlineFuture(@ForAll @Positive int x) {
		return x != 0;
	}

	@Hidden
	@Property
	@Deadline("2000-01-01 16:00")
	boolean propertyHiddenCustomDeadlinePast(@ForAll @Positive int x) {
		return x != 0;
	}

	@Hidden
	@Property
	boolean propertyHiddenNormal(@ForAll @Positive int x) {
		return x != 0;
	}

	@Public
	@Property
	@Deadline("2200-01-01 16:00")
	boolean propertyPublicCustomDeadline(@ForAll @Positive int x) {
		return x != 0;
	}

	@Public
	@Property
	boolean propertyPublicNormal(@ForAll @Positive int x) {
		return x != 0;
	}

	@Public
	@Property
	void propertyUseIOTesterCorrect(@ForAll IOTester test, @ForAll @CharRange(from = 'a', to = 'z') String s) {
		test.reset();
		System.out.println(s);
		Assertions.assertThat(test.out().getLines(DONT_IGNORE_LAST_EMPTY_LINE)).containsExactly(Line.of(s),
				Line.of(""));
	}

	@Public
	@Property
	void propertyUseIOTesterWrong(@ForAll IOTester test, @ForAll @CharRange(from = 'a', to = 'z') String s) {
		test.reset();
		System.out.println(s.length() < 5 ? s : s.substring(0, 5));
		Assertions.assertThat(test.out().getLines(DONT_IGNORE_LAST_EMPTY_LINE)).containsExactly(Line.of(s),
				Line.of(""));
	}

	@Public
	@Example
	@StrictTimeout(value = 500, unit = TimeUnit.MILLISECONDS)
	void provokeTimeoutEndlessLoop(@ForAll @Positive int x) {
		int y = 0;
		while (!Thread.currentThread().isInterrupted()) {
			y += x;
		}
	}

	@Public
	@Example
	@StrictTimeout(value = 200, unit = TimeUnit.MILLISECONDS)
	void provokeTimeoutSleepExample(@SuppressWarnings("unused") @ForAll @Positive int x) throws InterruptedException {
		sleepUntilInterrupted();
	}

	@Public
	@Property
	@StrictTimeout(value = 200, unit = TimeUnit.MILLISECONDS)
	void provokeTimeoutSleepProperty(@SuppressWarnings("unused") @ForAll @Positive int x) throws InterruptedException {
		sleepUntilInterrupted();
	}

	@Public
	@Property(tries = 1)
	@StrictTimeout(value = 200, unit = TimeUnit.MILLISECONDS)
	void provokeTimeoutSleepTries(@SuppressWarnings("unused") @ForAll @Positive int x) throws InterruptedException {
		sleepUntilInterrupted();
	}

	@Public
	@Property(tries = 5)
	@StrictTimeout(value = 250, unit = TimeUnit.MILLISECONDS)
	void strictTimeoutAppliesPerTry(@SuppressWarnings("unused") @ForAll int x) throws InterruptedException {
		Thread.sleep(100);
	}

	private void sleepUntilInterrupted() throws InterruptedException {
		try {
			Thread.sleep(300);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw exception;
		}
	}

	@Hidden
	void testHiddenIncomplete() {
		// nothing
	}

	@Public
	void testPublicIncomplete() {
		// nothing
	}

	@Public
	@Example
	void testLocaleDe() {
		assertThat(Locale.getDefault()).isEqualTo(Locale.GERMAN);
	}
}
