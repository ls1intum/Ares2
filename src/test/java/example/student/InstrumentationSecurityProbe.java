package example.student;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceAbstractToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;

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

	private static String stackCheckHelper() {
		return JavaInstrumentationAdviceAbstractToolbox.checkIfCallstackCriteriaIsViolated("example.student",
				new String[0], InstrumentationSecurityProbe.class.getName(), "stackCheckHelper");
	}
}
