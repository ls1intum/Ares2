package de.tum.cit.ase.ares.api.internal.sanitization;

@FunctionalInterface
interface ThrowableCreator {

	Throwable create(ThrowableInfo throwableInfo);
}
