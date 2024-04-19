package de.tum.cit.ase.ares.integration.testuser.subject.structural;

public enum SomeEnum {
	ONE,
	TWO;

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
