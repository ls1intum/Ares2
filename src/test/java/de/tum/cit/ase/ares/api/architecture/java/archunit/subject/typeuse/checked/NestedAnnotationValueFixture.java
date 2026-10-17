package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.permitted.PermittedTypeUse;
import org.example.subject.typeuse.values.ValueMarker;

/**
 * Names a forbidden annotation only as a nested value inside an array.
 */
public class NestedAnnotationValueFixture {

	/**
	 * The annotated field.
	 */
	@PermittedTypeUse(markers = { @ValueMarker, @ValueMarker })
	String name = "";
}
