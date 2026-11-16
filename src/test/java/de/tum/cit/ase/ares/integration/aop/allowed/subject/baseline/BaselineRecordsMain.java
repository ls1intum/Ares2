package de.tum.cit.ase.ares.integration.aop.allowed.subject.baseline;

/**
 * Student code using Java records. Each test uses its own record, so each one
 * is loaded, and rewritten by the agent, for the first time under that test's
 * mode combination.
 */
public final class BaselineRecordsMain {

	/**
	 * A record used only under ArchUnit and AspectJ.
	 *
	 * @param x the first component
	 * @param y the second component
	 */
	public record ArchunitAspectJPoint(int x, int y) {
	}

	/**
	 * A record used only under ArchUnit and instrumentation.
	 *
	 * @param x the first component
	 * @param y the second component
	 */
	public record ArchunitInstrumentationPoint(int x, int y) {
	}

	/**
	 * A record used only under WALA and AspectJ.
	 *
	 * @param x the first component
	 * @param y the second component
	 */
	public record WalaAspectJPoint(int x, int y) {
	}

	/**
	 * A record used only under WALA and instrumentation.
	 *
	 * @param x the first component
	 * @param y the second component
	 */
	public record WalaInstrumentationPoint(int x, int y) {
	}

	/**
	 * A record used only by a test with a switched-off ArchUnit policy.
	 *
	 * @param x the first component
	 * @param y the second component
	 */
	public record DeactivatedArchunitPoint(int x, int y) {
	}

	/**
	 * A record used only by a test with a switched-off WALA policy.
	 *
	 * @param x the first component
	 * @param y the second component
	 */
	public record DeactivatedWalaPoint(int x, int y) {
	}

	/** Prevents creating instances. */
	private BaselineRecordsMain() {
	}

	/**
	 * Describes two equal records through their generated methods.
	 *
	 * @param first  one record
	 * @param second an equal record
	 * @return the first record's text, then whether they are equal and hash alike
	 */
	public static String describe(Record first, Record second) {
		return first + " " + first.equals(second) + " " + (first.hashCode() == second.hashCode());
	}

	/**
	 * Uses the ArchUnit and AspectJ record.
	 *
	 * @return its description
	 */
	public static String useArchunitAspectJRecord() {
		return describe(new ArchunitAspectJPoint(1, 2), new ArchunitAspectJPoint(1, 2));
	}

	/**
	 * Uses the ArchUnit and instrumentation record.
	 *
	 * @return its description
	 */
	public static String useArchunitInstrumentationRecord() {
		return describe(new ArchunitInstrumentationPoint(1, 2), new ArchunitInstrumentationPoint(1, 2));
	}

	/**
	 * Uses the WALA and AspectJ record.
	 *
	 * @return its description
	 */
	public static String useWalaAspectJRecord() {
		return describe(new WalaAspectJPoint(1, 2), new WalaAspectJPoint(1, 2));
	}

	/**
	 * Uses the WALA and instrumentation record.
	 *
	 * @return its description
	 */
	public static String useWalaInstrumentationRecord() {
		return describe(new WalaInstrumentationPoint(1, 2), new WalaInstrumentationPoint(1, 2));
	}

	/**
	 * Uses the record of the switched-off ArchUnit test.
	 *
	 * @return its description
	 */
	public static String useDeactivatedArchunitRecord() {
		return describe(new DeactivatedArchunitPoint(1, 2), new DeactivatedArchunitPoint(1, 2));
	}

	/**
	 * Uses the record of the switched-off WALA test.
	 *
	 * @return its description
	 */
	public static String useDeactivatedWalaRecord() {
		return describe(new DeactivatedWalaPoint(1, 2), new DeactivatedWalaPoint(1, 2));
	}
}
