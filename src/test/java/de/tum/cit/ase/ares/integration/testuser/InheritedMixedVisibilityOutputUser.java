package de.tum.cit.ase.ares.integration.testuser;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;

/** Checks class lifecycle capture with inherited hidden and public tests. */
@Public
@Policy(activated = false)
public class InheritedMixedVisibilityOutputUser extends MixedVisibilityOutputUser {
}
