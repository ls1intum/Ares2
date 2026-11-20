package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks which call stacks count as the JDK handing its own Windows
 * shell-folder task to its COM thread. The stacks are built by hand, newest
 * frame first, so these tests run on every system; a frame "of the JVM" claims
 * a class the JVM loaded itself, any other frame claims this test class.
 */
class JavaInstrumentationShellFolderHandOffTest {

	/** A feature release whose shell-folder code was checked. */
	private static final int AUDITED_RELEASE = 21;

	/** The student class at the bottom of every stack. */
	private static final String STUDENT = "de.tum.cit.ase.ares.integration.student.Student";

	/**
	 * A hand-made stack frame.
	 *
	 * @param className      the frame's class, by its binary name
	 * @param methodName     the frame's method
	 * @param declaringClass the class whose loader the frame reports
	 */
	private record Frame(String className, String methodName, Class<?> declaringClass)
			implements StackWalker.StackFrame {

		/** {@inheritDoc} */
		@Override
		public String getClassName() {
			return className;
		}

		/** {@inheritDoc} */
		@Override
		public String getMethodName() {
			return methodName;
		}

		/** {@inheritDoc} */
		@Override
		public Class<?> getDeclaringClass() {
			return declaringClass;
		}

		/** {@inheritDoc} */
		@Override
		public int getByteCodeIndex() {
			return 0;
		}

		/** {@inheritDoc} */
		@Override
		public String getFileName() {
			return null;
		}

		/** {@inheritDoc} */
		@Override
		public int getLineNumber() {
			return -1;
		}

		/** {@inheritDoc} */
		@Override
		public boolean isNativeMethod() {
			return false;
		}

		/** {@inheritDoc} */
		@Override
		public StackTraceElement toStackTraceElement() {
			return new StackTraceElement(className, methodName, null, -1);
		}
	}

	/**
	 * Builds a frame of a class the JVM loaded itself.
	 *
	 * @param className  the class
	 * @param methodName the method
	 * @return the frame
	 */
	private static Frame jvm(String className, String methodName) {
		return new Frame(className, methodName, String.class);
	}

	/**
	 * Builds a frame of a class some other class loader loaded.
	 *
	 * @param className  the class
	 * @param methodName the method
	 * @return the frame
	 */
	private static Frame other(String className, String methodName) {
		return new Frame(className, methodName, JavaInstrumentationShellFolderHandOffTest.class);
	}

	/**
	 * The hand-off the Windows CI refused: Ares' check inside {@code submit},
	 * reached from {@code FileSystemView.getFiles}.
	 *
	 * @return the frames from Ares' check down to {@code ShellFolder.invoke}
	 */
	private static List<Frame> handOff() {
		return new ArrayList<>(List.of(jvm(
				"de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceThreadSystemToolbox",
				"checkThreadSystemInteraction"), jvm("java.util.concurrent.AbstractExecutorService", "submit"),
				jvm("sun.awt.shell.Win32ShellFolderManager2$ComInvoker", "invoke"),
				jvm("sun.awt.shell.ShellFolder", "invoke")));
	}

	/**
	 * The frames from {@code createShellFolder} down to the student calling
	 * {@code getFiles}.
	 *
	 * @return those frames
	 */
	private static List<Frame> checkedEntry() {
		return List.of(jvm("sun.awt.shell.Win32ShellFolderManager2", "createShellFolder"),
				jvm("sun.awt.shell.ShellFolder", "getShellFolder"),
				jvm("javax.swing.filechooser.FileSystemView", "getShellFolder"),
				jvm("javax.swing.filechooser.FileSystemView", "getFiles"), other(STUDENT, "list"));
	}

	/**
	 * Joins pieces of a stack.
	 *
	 * @param pieces the pieces, newest first
	 * @return the whole stack
	 */
	@SafeVarargs
	private static List<Frame> stack(List<Frame>... pieces) {
		List<Frame> frames = new ArrayList<>();
		for (List<Frame> piece : pieces) {
			frames.addAll(piece);
		}
		return frames;
	}

	/**
	 * Asks the toolbox about a stack on a checked release.
	 *
	 * @param frames the stack, newest first
	 * @return its answer
	 */
	private static boolean isHandOff(List<Frame> frames) {
		return isHandOff(frames, AUDITED_RELEASE);
	}

	/**
	 * Asks the toolbox about a stack.
	 *
	 * @param frames         the stack, newest first
	 * @param featureRelease the JDK release to claim
	 * @return its answer
	 */
	private static boolean isHandOff(List<Frame> frames, int featureRelease) {
		return JavaInstrumentationAdviceAbstractToolbox
				.isJdkShellFolderHandOff(new ArrayList<StackWalker.StackFrame>(frames).iterator(), featureRelease);
	}

	/** The stack the Windows CI refused passes. */
	@Test
	void theRefusedListingPasses() {
		assertTrue(isHandOff(
				stack(handOff(), List.of(jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")), checkedEntry())));
	}

	/** The first hand-off, which also starts the COM thread, passes. */
	@Test
	void theFirstWorkerStartPasses() {
		List<Frame> frames = stack(
				List.of(jvm("java.lang.Thread", "start"), jvm("java.util.concurrent.ThreadPoolExecutor", "addWorker"),
						jvm("java.util.concurrent.ThreadPoolExecutor", "execute")),
				handOff(), List.of(jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")), checkedEntry());
		assertTrue(isHandOff(frames));
	}

	/** The thread factory call that builds the COM thread passes. */
	@Test
	void theComThreadFactoryPasses() {
		List<Frame> frames = stack(
				List.of(jvm("sun.awt.shell.Win32ShellFolderManager2$ComInvoker", "newThread"),
						jvm("java.util.concurrent.ThreadPoolExecutor$Worker", "<init>"),
						jvm("java.util.concurrent.ThreadPoolExecutor", "addWorker"),
						jvm("java.util.concurrent.ThreadPoolExecutor", "execute")),
				handOff(), List.of(jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")), checkedEntry());
		assertTrue(isHandOff(frames));
	}

	/** {@code getChild} as the first checked method passes on its own. */
	@Test
	void getChildAloneIsAnEntry() {
		assertTrue(isHandOff(stack(handOff(),
				List.of(jvm("sun.awt.shell.Win32ShellFolder2", "hasAttribute"),
						jvm("javax.swing.filechooser.FileSystemView", "isFileSystem"),
						jvm("javax.swing.filechooser.FileSystemView", "getChild"), other(STUDENT, "child")))));
	}

	/** {@code isParent} as the first checked method passes on its own. */
	@Test
	void isParentAloneIsAnEntry() {
		assertTrue(isHandOff(stack(handOff(),
				List.of(jvm("sun.awt.shell.Win32ShellFolder2", "pidlsEqual"),
						jvm("sun.awt.shell.Win32ShellFolder2", "equals"),
						jvm("javax.swing.filechooser.FileSystemView", "isParent"), other(STUDENT, "containsEntry")))));
	}

	/**
	 * A JDK 17 or 21 stack with {@code AccessController} in the hand-off passes.
	 */
	@Test
	void aPrivilegedHandOffPasses() {
		List<Frame> frames = stack(
				List.of(jvm("java.lang.Thread", "start"), jvm("java.security.AccessController", "doPrivileged")),
				handOff(), List.of(jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")), checkedEntry());
		assertTrue(isHandOff(frames));
	}

	/** Both {@code ShellFolder.invoke} overloads in a row pass. */
	@Test
	void bothInvokeOverloadsPass() {
		assertTrue(isHandOff(stack(handOff(),
				List.of(jvm("sun.awt.shell.ShellFolder", "invoke"),
						jvm("sun.awt.shell.Win32ShellFolder2", "hasAttribute"),
						jvm("sun.awt.shell.Win32ShellFolder2", "isHidden"),
						jvm("javax.swing.filechooser.FileSystemView", "isHiddenFile"),
						jvm("javax.swing.filechooser.FileSystemView", "getFiles"), other(STUDENT, "list")))));
	}

	/** A listing reached through {@code getChild} passes. */
	@Test
	void aListingForGetChildPasses() {
		assertTrue(isHandOff(stack(handOff(),
				List.of(jvm("sun.awt.shell.Win32ShellFolder2", "listFiles"),
						jvm("javax.swing.filechooser.FileSystemView", "getFiles"),
						jvm("javax.swing.filechooser.FileSystemView", "getChild"),
						jvm("javax.swing.filechooser.WindowsFileSystemView", "getChild"), other(STUDENT, "child")))));
	}

	/** Student code above {@code ComInvoker.invoke} gets no pass. */
	@Test
	void studentCodeInTheHandOffIsRefused() {
		List<Frame> frames = stack(List.of(jvm("java.lang.Thread", "start"), other(STUDENT, "callback")), handOff(),
				List.of(jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")), checkedEntry());
		assertFalse(isHandOff(frames));
	}

	/**
	 * A student override between the shell folder and the checked entry gets no
	 * pass, as when a student's {@code isHiddenFile} asks for a link.
	 */
	@Test
	void studentCodeBetweenProducerAndEntryIsRefused() {
		assertFalse(isHandOff(stack(handOff(),
				List.of(jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName"),
						jvm("sun.awt.shell.Win32ShellFolderManager2", "createShellFolder"),
						jvm("sun.awt.shell.ShellFolder", "getShellFolder"),
						jvm("javax.swing.filechooser.FileSystemView", "isLink"), other(STUDENT, "isHiddenFile")),
				checkedEntry())));
	}

	/** An unchecked {@code FileSystemView} method gets no pass. */
	@Test
	void anUncheckedEntryIsRefused() {
		assertFalse(isHandOff(stack(handOff(),
				List.of(jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName"),
						jvm("sun.awt.shell.Win32ShellFolderManager2", "createShellFolder"),
						jvm("sun.awt.shell.ShellFolder", "getShellFolder"),
						jvm("javax.swing.filechooser.FileSystemView", "isLink"), other(STUDENT, "isLink")))));
	}

	/** {@code createNewFolder} is not counted as a checked entry. */
	@Test
	void createNewFolderIsNoEntry() {
		assertFalse(isHandOff(stack(handOff(),
				List.of(jvm("sun.awt.shell.Win32ShellFolder2", "hasAttribute"),
						jvm("javax.swing.filechooser.WindowsFileSystemView", "createNewFolder"),
						other(STUDENT, "newFolderIn")))));
	}

	/** A stack that ends after the producer gets no pass. */
	@Test
	void aStackWithoutEntryIsRefused() {
		assertFalse(isHandOff(stack(handOff(), List.of(jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")))));
	}

	/** The sorting producers get no pass, even above a checked entry. */
	@Test
	void sortingProducersAreRefused() {
		List<Frame> sortProducers = List.of(jvm("sun.awt.shell.ShellFolder", "sort"),
				jvm("sun.awt.shell.ShellFolder", "sortChildren"),
				jvm("sun.awt.shell.Win32ShellFolder2", "sortChildren"),
				jvm("sun.awt.shell.Win32ShellFolder2$ColumnComparator", "compare"));
		for (Frame producer : sortProducers) {
			assertFalse(isHandOff(stack(handOff(), List.of(producer), checkedEntry())), producer.toString());
		}
	}

	/**
	 * The sorting route through a student's {@code BasicDirectoryModel} gets no
	 * pass.
	 */
	@Test
	void theDirectoryModelSortIsRefused() {
		assertFalse(isHandOff(stack(handOff(), List.of(jvm("sun.awt.shell.ShellFolder", "sort"),
				jvm("javax.swing.plaf.basic.BasicDirectoryModel", "sort"), other(STUDENT, "sortThem")))));
	}

	/** An unlisted {@code Win32ShellFolder2} method gets no pass. */
	@Test
	void anUnlistedProducerIsRefused() {
		assertFalse(isHandOff(
				stack(handOff(), List.of(jvm("sun.awt.shell.Win32ShellFolder2", "getDisplayName")), checkedEntry())));
	}

	/** Each section with the right name but another class loader gets no pass. */
	@Test
	void framesOfAnotherLoaderAreRefused() {
		List<List<Frame>> forged = List.of(
				stack(handOff().subList(0, 2),
						List.of(other("sun.awt.shell.Win32ShellFolderManager2$ComInvoker", "invoke"),
								jvm("sun.awt.shell.ShellFolder", "invoke"),
								jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")),
						checkedEntry()),
				stack(handOff().subList(0, 3),
						List.of(other("sun.awt.shell.ShellFolder", "invoke"),
								jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")),
						checkedEntry()),
				stack(handOff(), List.of(other("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")), checkedEntry()),
				stack(handOff(),
						List.of(jvm("sun.awt.shell.Win32ShellFolder2", "listFiles"),
								other("javax.swing.filechooser.FileSystemView", "getFiles"), other(STUDENT, "list"))),
				stack(List.of(other("de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.Forged", "check")),
						handOff(), List.of(jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")),
						checkedEntry()));
		for (List<Frame> frames : forged) {
			assertFalse(isHandOff(frames), frames.toString());
		}
	}

	/** A student's own executor, without the JDK's COM invoker, gets no pass. */
	@Test
	void aStudentSubmitIsRefused() {
		assertFalse(isHandOff(List.of(jvm("java.util.concurrent.AbstractExecutorService", "submit"),
				other(STUDENT, "submitTask"), jvm("javax.swing.filechooser.FileSystemView", "getFiles"))));
	}

	/**
	 * {@code ComInvoker.invoke} not reached through {@code ShellFolder.invoke} gets
	 * no pass.
	 */
	@Test
	void anInvokerWithoutForwarderIsRefused() {
		assertFalse(isHandOff(stack(handOff().subList(0, 3),
				List.of(jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")), checkedEntry())));
	}

	/** A stack cut short after any section gets no pass. */
	@Test
	void aTruncatedStackIsRefused() {
		List<Frame> whole = stack(handOff(), List.of(jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")),
				checkedEntry());
		int entryIndex = whole.size() - 2;
		for (int length = 0; length <= entryIndex; length++) {
			assertFalse(isHandOff(whole.subList(0, length)), "cut after " + length + " frames");
		}
		assertTrue(isHandOff(whole.subList(0, entryIndex + 1)));
	}

	/** The pass is refused on a JDK release whose code was not checked. */
	@Test
	void anUncheckedReleaseIsRefused() {
		List<Frame> frames = stack(handOff(), List.of(jvm("sun.awt.shell.Win32ShellFolder2", "parseDisplayName")),
				checkedEntry());
		assertTrue(isHandOff(frames, 17));
		assertTrue(isHandOff(frames, 25));
		assertFalse(isHandOff(frames, 22));
		assertFalse(isHandOff(frames, 26));
	}
}
