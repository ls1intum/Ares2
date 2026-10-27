package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.testutilities.JceFileSystemContract;

/** Exercises the filesystem aspect through supervised real-backend probes. */
class JavaAspectJFileSystemAdviceDefinitionsTest extends JceFileSystemContract {

	/** Selects the woven caller-site enforcement backend. */
	@Override
	protected AOPMode jceMode() {
		return AOPMode.ASPECTJ;
	}
}
