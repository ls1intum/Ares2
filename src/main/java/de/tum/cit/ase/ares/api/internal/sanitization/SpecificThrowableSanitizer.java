package de.tum.cit.ase.ares.api.internal.sanitization;

interface SpecificThrowableSanitizer {

	boolean canSanitize(Throwable t);

	Throwable sanitize(Throwable t, MessageTransformer messageTransformer);

	default Throwable sanitize(Throwable t) {
		return sanitize(t, MessageTransformer.IDENTITY);
	}
}
