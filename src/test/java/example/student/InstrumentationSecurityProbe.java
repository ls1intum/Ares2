package example.student;

import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.util.List;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceAbstractToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceCommandSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceNetworkSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceThreadSystemToolbox;

public final class InstrumentationSecurityProbe {

	private InstrumentationSecurityProbe() {
	}

	public static String reflectiveStackCheck() throws Exception {
		Method method = InstrumentationSecurityProbe.class.getDeclaredMethod("stackCheckHelper");
		method.setAccessible(true);
		return (String) method.invoke(null);
	}

	public static void checkFileUrlOpenStream(URL fileUrl) {
		JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("read", "java.net.URL", "openStream",
				"()Ljava/io/InputStream;", null, new Object[0], fileUrl);
	}

	public static void checkDeleteIfExists(Path path) {
		JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("delete", "java.nio.file.Files",
				"deleteIfExists", "(Ljava/nio/file/Path;)Z", null, new Object[] { path }, null);
	}

	/**
	 * Simulates the "read" leg of a woven
	 * {@code Files.copy(source, destination, ...)} call (I-114): the real advice
	 * fires this action from a separate pointcut match than "overwrite", so the two
	 * legs are exercised independently, matching how weaving actually invokes them.
	 */
	public static void checkFilesCopyReadLeg(Path source, Path destination, CopyOption... options) {
		JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("read", "java.nio.file.Files", "copy",
				"(Ljava/nio/file/Path;Ljava/nio/file/Path;[Ljava/nio/file/CopyOption;)Ljava/nio/file/Path;", null,
				new Object[] { source, destination, options }, null);
	}

	/**
	 * Simulates the "overwrite" leg of a woven {@code Files.copy(...)} call
	 * (I-114).
	 */
	public static void checkFilesCopyOverwriteLeg(Path source, Path destination, CopyOption... options) {
		JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("overwrite", "java.nio.file.Files",
				"copy", "(Ljava/nio/file/Path;Ljava/nio/file/Path;[Ljava/nio/file/CopyOption;)Ljava/nio/file/Path;",
				null, new Object[] { source, destination, options }, null);
	}

	/**
	 * Simulates the "read" leg of a woven {@code FileChannel.transferTo(...)} call
	 * (I-114): the receiver ({@code sourceChannel}) is the channel being read FROM.
	 */
	public static void checkFileChannelTransferToReadLeg(FileChannel sourceChannel, long position, long count,
			Object target) {
		JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("read", "java.nio.channels.FileChannel",
				"transferTo", "(JJLjava/nio/channels/WritableByteChannel;)J", null,
				new Object[] { position, count, target }, sourceChannel);
	}

	/**
	 * Simulates the "overwrite" leg of a woven
	 * {@code FileChannel.transferFrom(...)} call (I-114): the receiver
	 * ({@code destinationChannel}) is the channel being written TO. Was previously
	 * not intercepted by either backend at all.
	 */
	public static void checkFileChannelTransferFromOverwriteLeg(FileChannel destinationChannel, Object source,
			long position, long count) {
		JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("overwrite",
				"java.nio.channels.FileChannel", "transferFrom", "(Ljava/nio/channels/ReadableByteChannel;JJ)J", null,
				new Object[] { source, position, count }, destinationChannel);
	}

	public static void checkNetworkConnect(String host, int port) {
		JavaInstrumentationAdviceNetworkSystemToolbox.checkNetworkSystemInteraction("connect", "java.net.Socket",
				"connect", "(Ljava/net/SocketAddress;I)V", null, new Object[] { host, port }, null);
	}

	public static void checkNetworkConnectAddress(SocketAddress address) {
		JavaInstrumentationAdviceNetworkSystemToolbox.checkNetworkSystemInteraction("connect",
				"java.nio.channels.SocketChannel", "connect", "(Ljava/net/SocketAddress;)Z", null,
				new Object[] { address }, null);
	}

	public static void checkCommandExecution(String command) {
		JavaInstrumentationAdviceCommandSystemToolbox.checkCommandSystemInteraction("execute", "java.lang.Runtime",
				"exec", "(Ljava/lang/String;)Ljava/lang/Process;", null, new Object[] { command }, null);
	}

	public static void checkProcessBuilderStartPipeline(List<ProcessBuilder> builders) {
		JavaInstrumentationAdviceCommandSystemToolbox.checkCommandSystemInteraction("execute",
				"java.lang.ProcessBuilder", "startPipeline", "(Ljava/util/List;)Ljava/util/List;", null,
				new Object[] { builders }, null);
	}

	public static void checkThreadCreation(Runnable task) {
		JavaInstrumentationAdviceThreadSystemToolbox.checkThreadSystemInteraction("create", "java.lang.Thread",
				"<init>", "", new Object[0], new Object[] { task }, null);
	}

	public static void checkImplicitParallelStream() {
		JavaInstrumentationAdviceThreadSystemToolbox.checkThreadSystemInteraction("create", "java.util.Collection",
				"parallelStream", "()Ljava/util/stream/Stream;", new Object[0], new Object[0], null);
	}

	private static String stackCheckHelper() throws Exception {
		// checkIfCallstackCriteriaIsViolated was hardened to package-private, so
		// student
		// code can no longer call it directly and must use reflection - exactly the
		// path
		// this probe simulates. The reflective frames are in IGNORE_CALLSTACK, so the
		// check still attributes the violation to this student frame (no bypass).
		Method check = JavaInstrumentationAdviceAbstractToolbox.class.getDeclaredMethod(
				"checkIfCallstackCriteriaIsViolated", String.class, String[].class, String.class, String.class);
		check.setAccessible(true);
		return (String) check.invoke(null, "example.student", new String[0],
				InstrumentationSecurityProbe.class.getName(), "stackCheckHelper");
	}

	/**
	 * Reflectively invokes the instrumentation backend's package-private
	 * {@code checkIfCallstackCriteriaIsViolated} with caller-supplied
	 * {@code restrictedPackage}/{@code allowedClasses}, so a fixture class in an
	 * arbitrary package can exercise the allowed-class/allowed-package
	 * prefix-collision fix (I-105) end to end: this probe's own frame is not in the
	 * caller's restricted package, so the real stack walk still attributes the
	 * check to the caller's frame, not this one.
	 */
	public static String checkCallstackCriteria(String restrictedPackage, String[] allowedClasses) throws Exception {
		Method check = JavaInstrumentationAdviceAbstractToolbox.class.getDeclaredMethod(
				"checkIfCallstackCriteriaIsViolated", String.class, String[].class, String.class, String.class);
		check.setAccessible(true);
		return (String) check.invoke(null, restrictedPackage, allowedClasses,
				InstrumentationSecurityProbe.class.getName(), "checkCallstackCriteria");
	}
}
