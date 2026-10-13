package example.nest;

/**
 * A class with every kind of class declared inside it, whose compiled
 * {@code NestMembers} attribute must list all of them.
 */
public final class NestHostFixture {

	/** A local class declared in the static initialiser, created once. */
	public static final Object IN_INITIALISER;

	static {
		final class InInitialiser {
		}
		IN_INITIALISER = new InInitialiser();
	}

	/** Prevents instances of the fixture. */
	private NestHostFixture() {
	}

	/** Returns an instance of an anonymous class declared here. */
	public static Runnable anonymous() {
		return new Runnable() {
			/** Does nothing of note. */
			@Override
			public void run() {
				Thread.onSpinWait();
			}
		};
	}

	/** Returns an instance of a local class declared here. */
	public static Object local() {
		final class Local {
		}
		return new Local();
	}

	/** A member class with a further class declared inside it. */
	public static final class Member {

		/** Prevents instances of the fixture. */
		private Member() {
		}

		/** A class two levels deep. */
		public static final class Deeper {

			/** Prevents instances of the fixture. */
			private Deeper() {
			}
		}
	}

	/** An enum whose second constant has its own body, a class of its own. */
	public enum Kind {
		/** A constant without a body. */
		PLAIN,
		/** A constant with a body. */
		SPECIAL {
			/** Returns the constant's lower-case name. */
			@Override
			public String toString() {
				return "special";
			}
		}
	}

	/**
	 * A record declared inside the host.
	 *
	 * @param value the stored value
	 */
	public record Pair(int value) {
	}
}
