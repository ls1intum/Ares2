package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a class the AspectJ compiler wove with the Ares aspects, so Ares can
 * refuse to run classes built without them. It is not public, so a student's
 * source cannot write it, and it stays in the class file only, so reflection,
 * and a structural test with it, never sees it.
 */
@Retention(RetentionPolicy.CLASS)
@interface JavaAspectJWoven {
}
