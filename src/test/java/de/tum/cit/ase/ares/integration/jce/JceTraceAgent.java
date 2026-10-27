package de.tum.cit.ase.ares.integration.jce;

import static net.bytebuddy.matcher.ElementMatchers.nameEndsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.none;

import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;

/** Records real backend entries in fixtures without changing JDK bytecode. */
public final class JceTraceAgent {

	/** Prevents creating instances of this test agent. */
	private JceTraceAgent() {
	}

	/**
	 * Instruments only the Ares filesystem entry methods for trusted diagnostics.
	 */
	public static void premain(String arguments, Instrumentation instrumentation) {
		new AgentBuilder.Default().disableClassFormatChanges().ignore(none())
				.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
				.type(nameEndsWith("JavaAspectJFileSystemAdviceDefinitions")
						.or(nameEndsWith("JavaInstrumentationAdviceFileSystemToolbox")))
				.transform((builder, type, loader, module, domain) -> builder
						.visit(Advice.to(TraceAdvice.class).on(named("checkFileSystemInteraction"))))
				.installOn(instrumentation);
	}

	/** Packages the diagnostics separately from the production agent. */
	public static Path packageAgent() throws Exception {
		Path destination = Path.of("target", "jce-trace-agent.jar").toAbsolutePath();
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		manifest.getMainAttributes().putValue("Premain-Class", JceTraceAgent.class.getName());
		manifest.getMainAttributes().putValue("Can-Retransform-Classes", "true");
		try (var jar = new JarOutputStream(Files.newOutputStream(destination), manifest)) {
			for (Class<?> type : new Class<?>[] { JceTraceAgent.class, TraceAdvice.class }) {
				String resource = type.getName().replace('.', '/') + ".class";
				jar.putNextEntry(new JarEntry(resource));
				try (var input = type.getClassLoader().getResourceAsStream(resource)) {
					java.util.Objects.requireNonNull(input, resource).transferTo(jar);
				}
				jar.closeEntry();
			}
		}
		return destination;
	}

	/** Inlines JDK-only diagnostics into the selected Ares backend. */
	public static final class TraceAdvice {

		/** Prevents creating instances of the advice container. */
		private TraceAdvice() {
		}

		/** Distinguishes actual JCE setup entries from the supervised subject. */
		@Advice.OnMethodEnter
		public static void record(@Advice.Origin("#t") String backend) {
			String origin = null;
			for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
				if (frame.getClassName().equals("javax.crypto.JceSecurity")
						&& frame.getMethodName().equals("setupJurisdictionPolicies")) {
					origin = "JDK_JCE";
					break;
				}
				if (frame.getClassName().equals("example.jce.JceCryptoSubject")) {
					origin = "STUDENT";
				}
			}
			if (origin != null) {
				System.out.println("JCE_BACKEND_ENTRY " + backend + " " + origin);
			}
		}
	}
}
