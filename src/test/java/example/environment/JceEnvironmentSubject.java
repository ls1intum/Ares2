package example.environment;

/**
 * Attempts the environment change separately from the filesystem capture proof.
 */
public final class JceEnvironmentSubject {

	/** Prevents instances of the student fixture. */
	private JceEnvironmentSubject() {
	}

	/** Uses the actual environment API that both architecture rules forbid. */
	public static void changeJavaHome(String home) {
		System.setProperty("java.home", home);
	}
}
