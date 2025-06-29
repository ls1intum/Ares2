package de.tum.cit.ase.ares.integration.testuser.subject;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.apache.xyz.*;

import de.tum.cit.ase.ares.api.io.IOTester;
import de.tum.cit.ase.ares.integration.testuser.subject.structural.SomeClass;

public final class SecurityPenguin {

	interface CompletableFutureExecution {
		void execute() throws ExecutionException, InterruptedException;
	}

	private SecurityPenguin() {
	}

	public static void definePackage() {
		//REMOVED: Checking Package Definition of the system's SecurityManager for "java.util"
	}

	public static void maliciousExceptionA() {
		throw new MaliciousExceptionA();
	}

	public static boolean maliciousExceptionB() {
		AtomicBoolean ab = new AtomicBoolean();
		try {
			throw new MaliciousExceptionB(ab);
		} catch (@SuppressWarnings("unused") SecurityException e) {
			// nothing
		}
		return ab.get();
	}

	public static void maliciousInvocationTargetException() throws Exception {
		throw new MaliciousInvocationTargetException();
	}

	@SuppressWarnings("resource")
	public static void newClassLoader() throws IOException {
		new URLClassLoader(new URL[0]).close();
	}

	//REMOVED: Creating a new Security Manager

	public static boolean tryEvilPermission() {
		AtomicBoolean ab = new AtomicBoolean();
		try {
			//REMOVED: Checking Permission of the system's SecurityManager for "setIO"
		} catch (@SuppressWarnings("unused") SecurityException e) {
			// do nothing
		}
		return ab.get();
	}

	@SuppressWarnings("resource")
	public static String tryExecuteGit() {
		try {
			return new String(Runtime.getRuntime().exec("git --help").getInputStream().readAllBytes())
					.replaceAll("\\W| ", "_");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static void tryLoadNativeLibrary() {
		System.load(new File("path_to_lib.so").getAbsolutePath());
	}

	public static void tryManageProcess() {
		ProcessHandle.current().destroy();
	}

	//REMOVED: Setting the SecurityManager to null

	@SuppressWarnings("resource")
	public static void trySetSystemOut() {
		System.setOut(new PrintStream(OutputStream.nullOutputStream()));
	}

	private static void useCommonPool(CompletableFutureExecution action) throws Throwable {
		try {
			action.execute();
		} catch (@SuppressWarnings("unused") InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}

	public static void useCommonPoolBadNormal() throws Throwable {
		useCommonPool(() -> CompletableFuture.runAsync(() -> IOTester.class.getDeclaredFields()[0].setAccessible(true))
				.get());
	}

	public static void useCommonPoolBadTrick() throws Throwable {
		FakeTrustedClass fd = new FakeTrustedClass();
		useCommonPool(FakeTrustedClass::useCommonPoolBad);
		fd.equals(fd);
	}

	public static void useCommonPoolGood() throws Throwable {
		useCommonPool(() -> CompletableFuture.supplyAsync(() -> "").get());
	}

	public static void useReflection() {
		try {
			Class.forName("de.tum.cit.ase.ares.api.io.IOTester").getDeclaredFields()[0].setAccessible(true);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void useReflection2() throws Throwable {
		Thread d = new Circumvention();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		d.setUncaughtExceptionHandler((t, e) -> failure.set(e));
		d.start();
		try {
			d.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Circumvention.thrown.ifPresent(failure::set);
		if (failure.get() != null)
			throw failure.get();
	}

	public static void useReflectionPackagePrivateExecute() throws Throwable {
		SomeClass.class.getDeclaredConstructor(String.class).newInstance("");
	}

	public static void useReflectionPackagePrivateSetAccessible() throws Throwable {
		SomeClass.class.getDeclaredConstructor(String.class).trySetAccessible();
	}

	// REMOVED: Doing privileged actions on the AccessController
}
