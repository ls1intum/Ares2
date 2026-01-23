package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;

class JavaInstrumentationAdviceFileSystemToolboxTest {

	@Test
	void testCheckFileSystemInteraction_AllowedInteraction() {
		try (MockedStatic<JavaInstrumentationAdviceFileSystemToolbox> mockedToolbox = mockStatic(JavaInstrumentationAdviceFileSystemToolbox.class)) {
			Method getValueFromSettings = JavaInstrumentationAdviceFileSystemToolbox.class.getDeclaredMethod("getValueFromSettings", String.class);
			getValueFromSettings.setAccessible(true);

			mockedToolbox.when(() -> getValueFromSettings.invoke(null, "aopMode")).thenReturn("INSTRUMENTATION");
			mockedToolbox.when(() -> getValueFromSettings.invoke(null, "restrictedPackage")).thenReturn("de.tum.cit.ase");
			mockedToolbox.when(() -> getValueFromSettings.invoke(null, "allowedListedClasses")).thenReturn(new String[]{ "de.tum.cit.ase.safe" });
			mockedToolbox.when(() -> getValueFromSettings.invoke(null, "pathsAllowedToBeRead")).thenReturn(new String[]{ "/allowed/path" });

			assertDoesNotThrow(() -> JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("read", "de.tum.cit.ase.safe.FileReader", "readFile", "(Ljava/lang/String;)V", null,
					new Object[]{ "/allowed/path" }, null));
		} catch (Exception e) {
			fail("Exception should not have been thrown: " + e.getMessage());
		}
	}

	@Test
	void testCheckFileSystemInteraction_AllowsCreateOptions(@TempDir Path tempDir) throws Exception {
		Method reset = JavaAOPTestCaseSettings.class.getDeclaredMethod("reset");
		reset.setAccessible(true);
		try {
			reset.invoke(null);
			JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", "INSTRUMENTATION", "ARCH", "INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", "de.tum.cit.ase", "ARCH", "INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCH", "INSTRUMENTATION");
			String allowedPath = tempDir.toString();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten", new String[]{ allowedPath }, "ARCH", "INSTRUMENTATION");

			Path target = tempDir.resolve("created.txt");
			Object[] parameters = new Object[]{ target, new StandardOpenOption[]{ StandardOpenOption.CREATE, StandardOpenOption.WRITE } };

			assertDoesNotThrow(() -> JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("overwrite", "de.tum.cit.ase.restricted.Subject", "openStream",
					"(Ljava/nio/file/Path;[Ljava/nio/file/OpenOption;)Ljava/io/OutputStream;", null, parameters, null));
		} finally {
			reset.invoke(null);
		}
	}

	@Test
	void testLocalizeFallback() {
		String key = "security.advice.test.key";
		String result = JavaInstrumentationAdviceAbstractToolbox.localize(key, "arg1", "arg2");
		key = "!security.advice.test.key!";
		assertEquals(key, result);
	}
}
