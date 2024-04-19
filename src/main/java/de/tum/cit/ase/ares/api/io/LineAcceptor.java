package de.tum.cit.ase.ares.api.io;

import java.nio.CharBuffer;

interface LineAcceptor {
	void acceptOutput(CharBuffer output);
}
