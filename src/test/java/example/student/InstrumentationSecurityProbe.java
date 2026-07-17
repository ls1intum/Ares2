package example.student;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;

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

	public static void checkNetworkConnect(String host, int port) {
		JavaInstrumentationAdviceNetworkSystemToolbox.checkNetworkSystemInteraction("connect", "java.net.Socket",
				"connect", "(Ljava/net/SocketAddress;I)V", null, new Object[] { host, port }, null);
	}

	public static void checkCommandExecution(String command) {
		JavaInstrumentationAdviceCommandSystemToolbox.checkCommandSystemInteraction("execute", "java.lang.Runtime",
				"exec", "(Ljava/lang/String;)Ljava/lang/Process;", null, new Object[] { command }, null);
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
}
