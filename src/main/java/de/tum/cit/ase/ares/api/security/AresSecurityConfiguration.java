package de.tum.cit.ase.ares.api.security;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(status = Status.INTERNAL)
public final class AresSecurityConfiguration {
	private final Optional<Class<?>> testClass;
	private final Optional<Method> testMethod;
	private final Path executionPath;

	AresSecurityConfiguration(Optional<Class<?>> testClass, Optional<Method> testMethod, Path executionPath) {
		this.testClass = Objects.requireNonNull(testClass);
		this.testMethod = Objects.requireNonNull(testMethod);
		this.executionPath = executionPath.toAbsolutePath();
	}

	public Optional<Class<?>> testClass() {
		return testClass;
	}

	public Optional<Method> testMethod() {
		return testMethod;
	}

	public Path executionPath() {
		return executionPath;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AresSecurityConfiguration))
			return false;
		AresSecurityConfiguration other = (AresSecurityConfiguration) obj;
		return Objects.equals(executionPath, other.executionPath) && Objects.equals(testClass, other.testClass)
				&& Objects.equals(testMethod, other.testMethod);
	}

	@Override
	public int hashCode() {
		return Objects.hash(executionPath, testClass, testMethod);
	}

	@Override
	public String toString() {
		return String.format(
				"AresSecurityConfiguration [ executionPath=%s, testClass=%s, testMethod=%s]", //$NON-NLS-1$
				executionPath, testClass, testMethod
		);
	}

	public String shortDesc() {
		return String.format(
				"ASC-Impl [testMethod=%s, executionPath=%s]", //$NON-NLS-1$
				testMethod, executionPath
		);
	}
}
