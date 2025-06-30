package de.tum.cit.ase.ares.api.internal;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

//REMOVED: Import of ArtemisSecurityManager

@API(status = Status.INTERNAL)
public final class PrivilegedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final Throwable priviledgedThrowable;

	public PrivilegedException(Throwable priviledgedThrowable) {
		super("priviledged " + priviledgedThrowable.getClass(), null, false, false); //$NON-NLS-1$
		//REMOVED: Asking ArtemisSecurityManager for checking the current stack
		this.priviledgedThrowable = priviledgedThrowable;
	}

	public Throwable getPriviledgedThrowable() {
		return priviledgedThrowable;
	}
}
