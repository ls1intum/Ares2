package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures;

import java.io.File;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * Fixture reaching into packages a security policy is expected to be able to
 * forbid. {@code java.lang.reflect} is deliberately included: it is a
 * subpackage of the unavoidable {@code java.lang} baseline and must stay
 * blocked.
 */
public class UsesForbidden {

	public File leakFile() {
		return new File("/etc/passwd");
	}

	public Socket leakSocket() throws Exception {
		return new Socket("example.invalid", 1337);
	}

	public Method leakReflection() throws Exception {
		return String.class.getMethod("length");
	}
}
