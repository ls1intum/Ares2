package de.tum.cit.ase.ares.api.io;

interface LineProvider {
	Line getNextLine();

	boolean hasNextLine();
}
