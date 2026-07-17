package de.tum.cit.ase.ares.api.internal;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(status = Status.INTERNAL)
public final class PrivilegedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final Throwable priviledgedThrowable;

	public PrivilegedException(Throwable priviledgedThrowable) {
		super("priviledged " + priviledgedThrowable.getClass(), null, false, false); //$NON-NLS-1$
		this.priviledgedThrowable = priviledgedThrowable;
	}

	public Throwable getPriviledgedThrowable() {
		return priviledgedThrowable;
	}
}
