package de.tum.cit.ase.ares.integration.testuser;

import net.jqwik.api.Example;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jqwik.Public;

@Deadline("2200-01-01 16:00")
@Policy(activated = false)
public class JqwickClassDeadlineUser {

	@Public
	@Example
	void publicMethodInheritsClassDeadline() {
		// The public method must reject the enclosing class deadline.
	}
}
