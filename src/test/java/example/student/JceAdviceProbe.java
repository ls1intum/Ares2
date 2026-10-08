package example.student;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.runtime.reflect.Factory;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;

/**
 * Enters actual backend checks from a supervised frame with real path values.
 */
public final class JceAdviceProbe {

	/** Prevents creating probe instances. */
	private JceAdviceProbe() {
	}

	/**
	 * Supplies parameter, File receiver, stream-path attribute or glob arguments.
	 */
	public static void check(AOPMode mode, String action, String representation, Path file, Object instance) {
		Class<?> owner = representation.equals("receiver") ? File.class
				: representation.equals("attribute") ? FileInputStream.class : Files.class;
		String method = representation.equals("glob") ? "newDirectoryStream"
				: representation.equals("receiver") ? "list" : "read";
		Object[] parameters = representation.equals("parameter") ? new Object[] { file }
				: representation.equals("glob") ? new Object[] { file, "{default,exempt}_*.policy" } : new Object[0];
		Object[] attributes = representation.equals("attribute") ? new Object[] { file.toString() } : new Object[0];
		if (mode == AOPMode.INSTRUMENTATION) {
			String descriptor = representation.equals("glob")
					? "(Ljava/nio/file/Path;Ljava/lang/String;)Ljava/nio/file/DirectoryStream;"
					: "()V";
			JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction(action, owner.getName(), method,
					descriptor, attributes, parameters, instance);
		} else {
			Factory factory = new Factory("JceAdviceProbe.java", JceAdviceProbe.class);
			Class<?>[] parameterTypes = representation.equals("glob") ? new Class<?>[] { Path.class, String.class }
					: representation.equals("parameter") ? new Class<?>[] { Path.class } : new Class<?>[0];
			MethodSignature signature = factory.makeMethodSig(java.lang.reflect.Modifier.PUBLIC, method, owner,
					parameterTypes, new String[parameterTypes.length], new Class<?>[0], void.class);
			JoinPoint point = Factory.makeJP(factory.makeSJP(JoinPoint.METHOD_CALL, signature, 1), null, instance,
					parameters);
			JavaAspectJFileSystemAdviceDefinitions.aspectOf().checkFileSystemInteraction(action, point);
		}
	}
}
