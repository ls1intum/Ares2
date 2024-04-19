package de.tum.cit.ase.ares.api.internal.sanitization;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(status = Status.INTERNAL)
@FunctionalInterface
interface SanitizationAction<T> {
	T executeSanitization();
}
