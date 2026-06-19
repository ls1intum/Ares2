package de.tum.cit.ase.ares.api.architecture.java.wala;

//<editor-fold desc="Imports">

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;

import de.tum.cit.ase.ares.api.architecture.java.FileHandlerConstants;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.util.FileTools;
//</editor-fold>

/**
 * Architecture test case for the Java programming language using WALA and
 * concrete product of the abstract factory design pattern.
 *
 * @author Sarp Sahinalp
 * @version 2.0.0
 * @see <a href=
 *      "https://refactoring.guru/design-patterns/abstract-factory">Abstract
 *      Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaWalaTestCase extends JavaArchitectureTestCase {

	// <editor-fold desc="Disk-backed rule outcome cache">

	/**
	 * In-memory mirror of the disk cache: maps a content fingerprint + rule name to
	 * the cached outcome of running that WALA rule. WALA call-graph construction
	 * dominates the cost of the test modes, and each test fork runs in a separate
	 * JVM, so the disk-backed cache lets a later fork short-circuit rule checks
	 * whose outcome was already computed.
	 */
	private static final class CachedOutcome {
		/** Violation message, or {@code null} when the rule passed. */
		@Nullable
		final String violationMessage;

		CachedOutcome(@Nullable String violationMessage) {
			this.violationMessage = violationMessage;
		}
	}

	@Nonnull
	private static final ConcurrentHashMap<String, CachedOutcome> RULE_OUTCOME_CACHE = new ConcurrentHashMap<>();

	private static final String HMAC_ALGORITHM = "HmacSHA256";

	/**
	 * Per-user private cache directory (POSIX 0700) under the system temp dir, so a
	 * co-tenant on a shared host cannot read, write, or poison the verdict cache.
	 * {@code null} disables the disk cache (the in-memory map still works within
	 * one JVM).
	 */
	@Nullable
	private static final Path CACHE_DIR = resolveCacheDir();

	/**
	 * Per-user secret (0600) used to HMAC the cache payload. The first process
	 * creates it; same-user forks reuse it, so cross-fork sharing still works while
	 * a co-tenant - who cannot read the 0700 dir - cannot forge a valid MAC.
	 * {@code null} disables the disk cache.
	 */
	@Nullable
	private static final byte[] CACHE_SECRET = loadOrCreateSecret();

	/**
	 * Persistent cache file inside the private cache directory; {@code null} when
	 * the directory could not be created.
	 */
	@Nullable
	private static final Path CACHE_FILE = CACHE_DIR == null ? null : CACHE_DIR.resolve("outcomes.v2");

	static {
		loadCacheFromDisk();
		Runtime.getRuntime().addShutdownHook(new Thread(JavaWalaTestCase::saveCacheToDisk, "ares-wala-outcomes-saver"));
	}

	@Nullable
	private static Path resolveCacheDir() {
		try {
			String tmp = System.getProperty("java.io.tmpdir");
			if (tmp == null) {
				return null;
			}
			String user = System.getProperty("user.name", "anon");
			Path dir = Paths.get(tmp, "ares-wala-cache-" + Integer.toHexString(user.hashCode()));
			try {
				// Create atomically with 0700 so there is no window in which the directory
				// exists with looser permissions.
				Files.createDirectory(dir, java.nio.file.attribute.PosixFilePermissions
						.asFileAttribute(java.nio.file.attribute.PosixFilePermissions.fromString("rwx------")));
			} catch (java.nio.file.FileAlreadyExistsException existing) {
				// Pre-existing directory: validated below before it is trusted.
			} catch (UnsupportedOperationException nonPosix) {
				// Non-POSIX filesystem: best-effort create; rely on per-user temp ACLs.
				Files.createDirectories(dir);
			}
			// Trust the directory only if it is a real (non-symlink) directory owned by the
			// current user with no group/other access; otherwise a co-tenant could have
			// pre-created it (and a pre-seeded secret) to feed us forged "pass" verdicts.
			if (!isTrustworthyOwnerOnly(dir, true)) {
				return null;
			}
			return dir;
		} catch (Exception ignored) {
			return null;
		}
	}

	/**
	 * Returns {@code true} only when {@code path} is a non-symlink owned by the
	 * current user with no group or other permission bits. On non-POSIX file
	 * systems it returns {@code true} and relies on the per-user temp ACLs.
	 */
	private static boolean isTrustworthyOwnerOnly(@Nonnull Path path, boolean directory) {
		try {
			if (Files.isSymbolicLink(path)) {
				return false;
			}
			if (directory && !Files.isDirectory(path, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
				return false;
			}
			java.nio.file.attribute.PosixFileAttributes attributes;
			try {
				attributes = Files.readAttributes(path, java.nio.file.attribute.PosixFileAttributes.class,
						java.nio.file.LinkOption.NOFOLLOW_LINKS);
			} catch (UnsupportedOperationException nonPosix) {
				return true;
			}
			String user = System.getProperty("user.name");
			if (user != null && !user.equals(attributes.owner().getName())) {
				return false;
			}
			for (java.nio.file.attribute.PosixFilePermission permission : attributes.permissions()) {
				switch (permission) {
				case GROUP_READ, GROUP_WRITE, GROUP_EXECUTE, OTHERS_READ, OTHERS_WRITE, OTHERS_EXECUTE -> {
					return false;
				}
				default -> {
					// Owner bits are acceptable.
				}
				}
			}
			return true;
		} catch (IOException ignored) {
			return false;
		}
	}

	/**
	 * Best-effort owner-only restriction (0700 dir / 0600 file) on POSIX file
	 * systems. On non-POSIX systems the per-user temp directory ACLs are relied
	 * upon instead.
	 */
	private static void restrictToOwner(@Nonnull Path path, boolean directory) {
		try {
			Files.setPosixFilePermissions(path,
					java.nio.file.attribute.PosixFilePermissions.fromString(directory ? "rwx------" : "rw-------"));
		} catch (UnsupportedOperationException | IOException ignored) {
			// Non-POSIX filesystem: fall back to default per-user temp permissions.
		}
	}

	@Nullable
	private static byte[] loadOrCreateSecret() {
		if (CACHE_DIR == null) {
			return null;
		}
		Path secretFile = CACHE_DIR.resolve("secret");
		try {
			byte[] secret = new byte[32];
			new java.security.SecureRandom().nextBytes(secret);
			try {
				// Create atomically with 0600 so the secret never exists world-readable.
				try {
					Files.createFile(secretFile, java.nio.file.attribute.PosixFilePermissions
							.asFileAttribute(java.nio.file.attribute.PosixFilePermissions.fromString("rw-------")));
				} catch (UnsupportedOperationException nonPosix) {
					Files.createFile(secretFile);
				}
				Files.write(secretFile, secret, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				return secret;
			} catch (java.nio.file.FileAlreadyExistsException existing) {
				// Another same-user process created it first; reuse it so forks share a
				// secret, but only after confirming it is still owner-only (defence in depth
				// on top of the 0700 directory check).
				if (!isTrustworthyOwnerOnly(secretFile, false)) {
					return null;
				}
				byte[] read = Files.readAllBytes(secretFile);
				return read.length == 0 ? null : read;
			}
		} catch (Exception ignored) {
			return null;
		}
	}

	@Nullable
	private static byte[] hmac(@Nonnull byte[] data) {
		if (CACHE_SECRET == null) {
			return null;
		}
		try {
			javax.crypto.Mac mac = javax.crypto.Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new javax.crypto.spec.SecretKeySpec(CACHE_SECRET, HMAC_ALGORITHM));
			return mac.doFinal(data);
		} catch (java.security.GeneralSecurityException ignored) {
			return null;
		}
	}

	private static void loadCacheFromDisk() {
		try {
			if (CACHE_FILE == null || CACHE_SECRET == null || !Files.isRegularFile(CACHE_FILE)) {
				return;
			}
			String content = new String(Files.readAllBytes(CACHE_FILE), StandardCharsets.UTF_8);
			int newline = content.indexOf('\n');
			if (newline < 0) {
				return;
			}
			String macLine = content.substring(0, newline);
			String body = content.substring(newline + 1);
			byte[] expected = hmac(body.getBytes(StandardCharsets.UTF_8));
			if (expected == null) {
				return;
			}
			// Constant-time compare; on any mismatch (tampering, corruption, or a different
			// secret) ignore the cache and recompute from scratch - fail closed.
			String expectedMacLine = Base64.getEncoder().encodeToString(expected);
			if (!MessageDigest.isEqual(macLine.getBytes(StandardCharsets.UTF_8),
					expectedMacLine.getBytes(StandardCharsets.UTF_8))) {
				return;
			}
			parseCacheBody(body);
		} catch (Exception ignored) {
			// Best-effort: any failure leaves an empty in-memory cache. Must never escape
			// the
			// static initialiser, or the class would be poisoned for the whole JVM.
		}
	}

	private static void parseCacheBody(@Nonnull String body) {
		for (String line : body.split("\n")) {
			if (line.isEmpty()) {
				continue;
			}
			String[] parts = line.split("\t", 3);
			if (parts.length < 2) {
				continue;
			}
			try {
				String key = new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
				boolean violated = "1".equals(parts[1]);
				String message = violated ? (parts.length == 3 && !parts[2].isEmpty()
						? new String(Base64.getDecoder().decode(parts[2]), StandardCharsets.UTF_8)
						: "") : null;
				RULE_OUTCOME_CACHE.put(key, new CachedOutcome(message));
			} catch (IllegalArgumentException ignored) {
				// Skip a malformed line rather than failing the whole load.
			}
		}
	}

	private static void saveCacheToDisk() {
		if (RULE_OUTCOME_CACHE.isEmpty() || CACHE_FILE == null || CACHE_DIR == null || CACHE_SECRET == null) {
			return;
		}
		try {
			StringBuilder body = new StringBuilder();
			for (Map.Entry<String, CachedOutcome> entry : RULE_OUTCOME_CACHE.entrySet()) {
				String message = entry.getValue().violationMessage;
				body.append(Base64.getEncoder().encodeToString(entry.getKey().getBytes(StandardCharsets.UTF_8)))
						.append('\t').append(message == null ? '0' : '1').append('\t')
						.append(message == null ? ""
								: Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8)))
						.append('\n');
			}
			byte[] mac = hmac(body.toString().getBytes(StandardCharsets.UTF_8));
			if (mac == null) {
				return;
			}
			String fileText = Base64.getEncoder().encodeToString(mac) + "\n" + body;
			Path tmp = Files.createTempFile(CACHE_DIR, "outcomes", ".tmp");
			restrictToOwner(tmp, false);
			Files.writeString(tmp, fileText);
			try {
				Files.move(tmp, CACHE_FILE, StandardCopyOption.ATOMIC_MOVE);
			} catch (java.nio.file.AtomicMoveNotSupportedException atomicUnsupported) {
				Files.move(tmp, CACHE_FILE, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception ignored) {
			// Failure to persist is non-fatal: the next run rebuilds the cache.
		}
	}

	/**
	 * Builds a stable cache key from the analysis classpath, the Ares-jar mtime,
	 * the rule name, and a SHA-256 over a STRUCTURAL fingerprint of the analysed
	 * classes (each class's name plus the sorted full names of the methods it
	 * calls) and the allowed packages. Hashing the call structure with full SHA-256
	 * - rather than only class names through a 32-bit {@code hashCode} - both
	 * removes the trivially-constructible collision and detects a resubmission that
	 * keeps the class names but changes a body to add a forbidden call.
	 */
	@Nonnull
	private String cacheKey() {
		long aresMtime = 0L;
		try {
			Path own = Paths.get(JavaWalaTestCase.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			aresMtime = Files.getLastModifiedTime(own).toMillis();
		} catch (Exception ignored) {
			// Fall back to the structural fingerprint below.
		}
		String classpath = Stream.of(System.getProperty("java.class.path", "").split(java.io.File.pathSeparator))
				.filter(p -> p.contains("classes") || p.contains("build")).sorted()
				.collect(Collectors.joining(java.io.File.pathSeparator));
		String structuralFingerprint = javaClasses.stream()
				.map(javaClass -> javaClass.getFullName() + "#" + javaClass.getAccessesFromSelf().stream()
						.map(access -> access.getTarget().getFullName()).sorted().collect(Collectors.joining(",")))
				.sorted().collect(Collectors.joining(";"));
		// Fold the actual compiled bytecode of each analysed class into the key, so two
		// submissions with the same class names and call-target set but different
		// bytecode (e.g. different control flow that only reaches a forbidden call in
		// one) cannot collide to the same cached verdict.
		String bytecodeFingerprint = javaClasses.stream()
				.map(javaClass -> javaClass.getFullName() + "#" + classBytecodeHash(javaClass)).sorted()
				.collect(Collectors.joining(";"));
		String allowedPackagesFingerprint = allowedPackages.stream().map(PackagePermission::importTheFollowingPackage)
				.sorted().collect(Collectors.joining(","));
		// The allow-listed classes change which violations are exempt, so they must be
		// part of the key or a run with exemptions could be served a no-exemption
		// outcome.
		String allowedClassesFingerprint = getAllowedClasses().stream().map(ClassPermission::className).sorted()
				.collect(Collectors.joining(","));
		// Fold the blocklist content into the key: the Ares-jar mtime does not change
		// when a *-methods.txt or the false-positive file is edited under an exploded
		// classes directory, so without this a cached PASS could survive a tightening
		// of
		// the rules.
		String blocklistFingerprint = blocklistFingerprint(
				(JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported);
		return classpath + "|" + aresMtime + "|"
				+ ((JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported).name() + "|"
				+ sha256Hex(structuralFingerprint + "||" + bytecodeFingerprint + "||" + allowedPackagesFingerprint
						+ "||" + allowedClassesFingerprint + "||" + blocklistFingerprint);
	}

	/**
	 * Hashes the forbidden-method list for the analysed category together with the
	 * false-positive exclusion file, so that editing either invalidates the disk
	 * cache even when the Ares code-source mtime does not change.
	 */
	@Nonnull
	private static String blocklistFingerprint(@Nonnull JavaArchitectureTestCaseSupported supported) {
		Path methodsFile = switch (supported) {
		case FILESYSTEM_INTERACTION -> FileHandlerConstants.WALA_FILESYSTEM_METHODS;
		case NETWORK_CONNECTION -> FileHandlerConstants.WALA_NETWORK_METHODS;
		case COMMAND_EXECUTION -> FileHandlerConstants.WALA_COMMAND_EXECUTION_METHODS;
		case THREAD_CREATION -> FileHandlerConstants.WALA_THREAD_MANIPULATION_METHODS;
		case REFLECTION -> FileHandlerConstants.WALA_REFLECTION_METHODS;
		case TERMINATE_JVM -> FileHandlerConstants.WALA_JVM_TERMINATION_METHODS;
		case SERIALIZATION -> FileHandlerConstants.WALA_SERIALIZATION_METHODS;
		case CLASS_LOADING -> FileHandlerConstants.WALA_CLASSLOADER_METHODS;
		default -> null;
		};
		StringBuilder content = new StringBuilder();
		if (methodsFile != null) {
			content.append(readFileQuietly(methodsFile));
		}
		content.append("||").append(readFileQuietly(FileHandlerConstants.FALSE_POSITIVES_FILE_SYSTEM_INTERACTIONS));
		return sha256Hex(content.toString());
	}

	@Nonnull
	private static String readFileQuietly(@Nonnull Path path) {
		try {
			return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch (IOException | RuntimeException unreadable) {
			// A missing/unreadable blocklist contributes an empty component; the key still
			// changes if it later becomes readable with different content.
			return "";
		}
	}

	@Nonnull
	private static String sha256Hex(@Nonnull String value) {
		return sha256Hex(value.getBytes(StandardCharsets.UTF_8));
	}

	@Nonnull
	private static String sha256Hex(@Nonnull byte[] value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(value);
			StringBuilder hex = new StringBuilder(digest.length * 2);
			for (byte b : digest) {
				hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
			}
			return hex.toString();
		} catch (java.security.NoSuchAlgorithmException neverHappens) {
			// SHA-256 is guaranteed present on every JRE; degrade safely if absent.
			return Integer.toHexString(java.util.Arrays.hashCode(value));
		}
	}

	/**
	 * Hashes the actual compiled bytecode of an analysed class, so two submissions
	 * with the same set of class names and call targets but different bytecode
	 * cannot collide to the same cached verdict. Best-effort: a jar-packaged or
	 * unreadable source contributes an empty component, and the structural
	 * fingerprint still discriminates.
	 */
	@Nonnull
	private static String classBytecodeHash(@Nonnull JavaClass javaClass) {
		return javaClass.getSource().map(source -> {
			try {
				return sha256Hex(Files.readAllBytes(Path.of(source.getUri())));
			} catch (IOException | RuntimeException unreadable) {
				return "";
			}
		}).orElse("");
	}

	// </editor-fold>

	// <editor-fold desc="Constructors">

	/**
	 * Lazy supplier of the WALA call graph used for rule checks.
	 * <p>
	 * When constructed via the eager-CallGraph constructor (kept for backward
	 * compatibility), this is a thunk over the already-resolved instance. When
	 * constructed via the supplier-based constructor (used by {@code JavaCreator}),
	 * the call graph is built only on the first cache miss in
	 * {@link #runRuleAndCapture}, so JVM forks whose rule outcomes are entirely
	 * served from disk never pay the call-graph construction cost.
	 */
	@Nonnull
	private final Supplier<CallGraph> callGraphSupplier;

	public JavaWalaTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses,
			@Nonnull CallGraph callGraph) {
		super(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, callGraph);
		this.callGraphSupplier = () -> callGraph;
	}

	/**
	 * Eager variant that also carries the allow-listed (exempt) classes.
	 */
	public JavaWalaTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses,
			@Nonnull CallGraph callGraph, @Nonnull Set<ClassPermission> allowedClasses) {
		super(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, callGraph, /* callGraphSupplier */ null,
				allowedClasses);
		this.callGraphSupplier = () -> callGraph;
	}

	/**
	 * Lazy variant: defer call-graph construction until the rule outcome is
	 * actually needed. Used by {@code JavaCreator.javaCreate} so that disk-cached
	 * rule outcomes can short-circuit before WALA touches the bytecode.
	 */
	public JavaWalaTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses,
			@Nonnull Supplier<CallGraph> callGraphSupplier) {
		super(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, /* callGraph */ null);
		this.callGraphSupplier = callGraphSupplier;
	}

	/**
	 * Lazy variant that also carries the allow-listed (exempt) classes, used by the
	 * runtime execution path.
	 */
	public JavaWalaTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses,
			@Nonnull Supplier<CallGraph> callGraphSupplier, @Nonnull Set<ClassPermission> allowedClasses) {
		super(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, /* callGraph */ null,
				/* callGraphSupplier */ null, allowedClasses);
		this.callGraphSupplier = callGraphSupplier;
	}

	// </editor-fold>

	// <editor-fold desc="Write security test case methods">

	/**
	 * Formats the Set<PackagePermission> structure as a Java-literal
	 * Set.of(PackagePermission(...), ...).
	 */
	private String allowedPackagesAsCode() {
		if (allowedPackages.isEmpty()) {
			return "Set.of()";
		}
		String inner = allowedPackages.stream().map(pp -> String.format("new %s(\"%s\")",
				PackagePermission.class.getSimpleName(), pp.importTheFollowingPackage()))
				.collect(Collectors.joining(", "));
		return "Set.of(" + inner + ")";
	}

	/**
	 * Formats the JavaClasses structure as a Java-literal
	 * ClassFileImporter.importPackages(...) String.
	 */
	private String javaClassesAsCode() {
		Set<String> packages = javaClasses.stream().map(JavaClass::getPackageName)
				.collect(Collectors.toCollection(HashSet::new));

		if (packages.isEmpty()) {
			return "new ClassFileImporter().importPackages()";
		}
		String packagesAsString = packages.stream().map(p -> "\"" + p + "\"").collect(Collectors.joining(", "));
		return "new ClassFileImporter().importPackages(" + packagesAsString + ")";
	}

	/**
	 * Formats the CallGraph structure as a Java-literal expression that builds a
	 * WALA CallGraph.
	 */
	private String callGraphAsCode() {
		String classPathExpr = "System.getProperty(\"java.class.path\")";
		return "new de.tum.cit.ase.ares.api.architecture.java.wala.CustomCallgraphBuilder(" + classPathExpr + ")"
				+ ".buildCallGraph(" + classPathExpr + ")";
	}

	/**
	 * Returns the content of the architecture test case file in the Java
	 * programming language.
	 */
	@Override
	@Nonnull
	public String writeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
		try {
			String testWithPlaceholders = FileTools
					.readRuleFile(FileTools.readFile(FileTools.resolveFileOnSourceDirectory("templates", "architecture",
							"java", "wala", "rules",
							((JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported).name() + ".txt")))
					.stream().reduce("", (acc, line) -> acc + line + "\n");
			return testWithPlaceholders.replace("${allowedPackages}", allowedPackagesAsCode())
					.replace("${javaClasses}", javaClassesAsCode()).replace("${callGraph}", callGraphAsCode());
		} catch (AssertionError | IOException e) {
			throw new SecurityException(Messages.localized("architecture.illegal.statement", e.getMessage()));
		}
	}
	// </editor-fold>

	// <editor-fold desc="Execute security test case methods">

	/**
	 * Executes the architecture test case.
	 * <p>
	 * Outcomes are cached on disk keyed by classpath + Ares-jar mtime + rule name,
	 * so subsequent JVM forks (e.g. {@code testPermitted} after
	 * {@code testUnprotected}) skip the expensive call-graph traversal when the
	 * same rule was already evaluated for the same classpath.
	 */
	@Override
	public void executeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
		JavaArchitectureTestCaseSupported supported = (JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported;
		// Priority rules (NATIVE_CODE, AGENT_ATTACH, ENVIRONMENT_ACCESS, MODULE_SYSTEM,
		// JNDI_INJECTION) always run in every test mode and delegate to ArchUnit. Their
		// outcome depends on which resources are permitted in the current test
		// scenario,
		// but the allowedPackages component of the cache key is identical between
		// testPermitted and testBlockedAll for these rules (permitted agent/env/JNDI
		// resources do not contribute package imports). Skipping the disk cache for
		// these cases prevents a testBlockedAll violation from being served to
		// testPermitted.
		boolean skipCache = (supported == JavaArchitectureTestCaseSupported.NATIVE_CODE
				|| supported == JavaArchitectureTestCaseSupported.AGENT_ATTACH
				|| supported == JavaArchitectureTestCaseSupported.ENVIRONMENT_ACCESS
				|| supported == JavaArchitectureTestCaseSupported.MODULE_SYSTEM
				|| supported == JavaArchitectureTestCaseSupported.JNDI_INJECTION);
		if (skipCache) {
			runRuleAndCapture(architectureMode, aopMode).ifPresent(e -> {
				throw e;
			});
			return;
		}
		String key = cacheKey();
		CachedOutcome cached = RULE_OUTCOME_CACHE.get(key);
		if (cached != null) {
			if (cached.violationMessage != null) {
				throw new SecurityException(cached.violationMessage);
			}
			return;
		}
		Optional<SecurityException> result = runRuleAndCapture(architectureMode, aopMode);
		// Store the violation MESSAGE (never a live exception object): a violation with
		// a
		// null message is recorded as an empty string so it still reads back as a
		// violation, while a clean pass is null.
		String message = result.map(e -> e.getMessage() == null ? "" : e.getMessage()).orElse(null);
		RULE_OUTCOME_CACHE.put(key, new CachedOutcome(message));
		result.ifPresent(e -> {
			throw e;
		});
	}

	/**
	 * Run the WALA rule for this case and translate the AssertionError outcome (if
	 * any) into a {@link SecurityException} so it can be cached uniformly.
	 */
	@Nonnull
	private Optional<SecurityException> runRuleAndCapture(@Nonnull String architectureMode, @Nonnull String aopMode) {
		try {
			JavaArchitectureTestCaseSupported supported = (JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported;
			// Resolve the call graph lazily and only for rules that actually need it.
			// PACKAGE_IMPORT and the JDK-delegated categories operate on javaClasses or
			// delegate to ArchUnit and never touch the call graph.
			boolean needsCallGraph = !(supported == JavaArchitectureTestCaseSupported.PACKAGE_IMPORT
					|| supported == JavaArchitectureTestCaseSupported.NATIVE_CODE
					|| supported == JavaArchitectureTestCaseSupported.AGENT_ATTACH
					|| supported == JavaArchitectureTestCaseSupported.ENVIRONMENT_ACCESS
					|| supported == JavaArchitectureTestCaseSupported.MODULE_SYSTEM
					|| supported == JavaArchitectureTestCaseSupported.JNDI_INJECTION);
			CallGraph cg = needsCallGraph ? callGraphSupplier.get() : null;
			Set<ClassPermission> exemptClasses = getAllowedClasses();
			switch (supported) {
			case FILESYSTEM_INTERACTION -> JavaWalaTestCaseCollection.NO_CLASS_MUST_ACCESS_FILE_SYSTEM.check(cg,
					exemptClasses);
			case NETWORK_CONNECTION -> JavaWalaTestCaseCollection.NO_CLASS_MUST_ACCESS_NETWORK.check(cg, exemptClasses);
			case COMMAND_EXECUTION -> JavaWalaTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS.check(cg,
					exemptClasses);
			case THREAD_CREATION -> JavaWalaTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS.check(cg, exemptClasses);
			case PACKAGE_IMPORT -> JavaWalaTestCaseCollection
					.noClassMustImportForbiddenPackages(allowedPackages, exemptClasses).check(javaClasses);
			case REFLECTION -> JavaWalaTestCaseCollection.NO_CLASS_MUST_USE_REFLECTION.check(cg, exemptClasses);
			case TERMINATE_JVM -> JavaWalaTestCaseCollection.NO_CLASS_MUST_TERMINATE_JVM.check(cg, exemptClasses);
			case SERIALIZATION -> JavaWalaTestCaseCollection.NO_CLASS_MUST_SERIALIZE.check(cg, exemptClasses);
			case CLASS_LOADING -> JavaWalaTestCaseCollection.NO_CLASS_MUST_USE_CLASSLOADERS.check(cg, exemptClasses);
			case NATIVE_CODE, AGENT_ATTACH, ENVIRONMENT_ACCESS, MODULE_SYSTEM, JNDI_INJECTION ->
				// WALA's call-graph approach needs JDK methods inside the analysis scope to
				// trace a path from student code to a forbidden API. For these categories the
				// forbidden APIs (Runtime.load, ManagementFactory, java.lang.Module,
				// javax.naming, …) live in JDK packages outside the application analysis
				// scope. ArchUnit's class-level scan does see them — delegate.
				de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCase.archunitBuilder()
						.javaArchitectureTestCaseSupported(
								(JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported)
						.javaClasses(this.javaClasses).allowedPackages(this.allowedPackages)
						.allowedClasses(exemptClasses).build().executeArchitectureTestCase(architectureMode, aopMode);
			default -> throw new SecurityException(
					Messages.localized("security.common.unsupported.operation", this.architectureTestCaseSupported));
			}
			return Optional.empty();
		} catch (AssertionError ae) {
			try {
				JavaArchitectureTestCase.parseErrorMessage(ae);
			} catch (SecurityException parsed) {
				return Optional.of(parsed);
			}
			// A WALA rule raises an AssertionError only for a detected violation. If
			// parseErrorMessage did not re-throw it as a SecurityException, fail closed by
			// surfacing the violation rather than silently treating it as a clean pass.
			return Optional.of(new SecurityException(ae.getMessage(), ae));
		} catch (SecurityException sec) {
			// The NATIVE_CODE/etc. branch delegates to ArchUnit which throws
			// SecurityException
			// directly. Preserve and cache.
			return Optional.of(sec);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Builder">

	/**
	 * Creates a new builder instance for constructing JavaArchitectureTestCase
	 * objects.
	 *
	 * @since 2.0.0
	 * @author Sarp Sahinalp
	 * @return A new Builder instance
	 */
	@Nonnull
	public static JavaWalaTestCase.Builder walaBuilder() {
		return new JavaWalaTestCase.Builder();
	}

	/**
	 * Builder for the Java architecture test case.
	 */
	public static class Builder {
		@Nullable
		private JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported;
		@Nullable
		private JavaClasses javaClasses;
		@Nullable
		private CallGraph callGraph;
		@Nullable
		private Set<PackagePermission> allowedPackages;
		@Nonnull
		private Set<ClassPermission> allowedClasses = Set.of();

		/**
		 * Sets the architecture test case type supported by this instance.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @param javaArchitectureTestCaseSupported The type of architecture test case
		 *                                          to support
		 * @return This builder instance for method chaining
		 * @throws SecurityException if the parameter is null
		 */
		@Nonnull
		public JavaWalaTestCase.Builder javaArchitectureTestCaseSupported(
				@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported) {
			this.javaArchitectureTestCaseSupported = Preconditions.checkNotNull(javaArchitectureTestCaseSupported,
					"javaArchitecturalTestCaseSupported must not be null");
			return this;
		}

		/**
		 * Sets the Java classes to be analyzed by this architecture test case.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @param javaClasses Collection of Java classes for analysis
		 * @return This builder instance for method chaining
		 */
		@Nonnull
		public JavaWalaTestCase.Builder javaClasses(@Nonnull JavaClasses javaClasses) {
			this.javaClasses = Preconditions.checkNotNull(javaClasses, "javaClasses must not be null");
			return this;
		}

		/**
		 * Sets the call graph to be used for analysis. Required for WALA mode but not
		 * for ARCHUNIT mode.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @param callGraph Call graph representing method relationships
		 * @return This builder instance for method chaining
		 */
		@Nonnull
		public JavaWalaTestCase.Builder callGraph(@Nullable CallGraph callGraph) {
			this.callGraph = callGraph;
			return this;
		}

		/**
		 * Sets the allowed package permissions.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @param allowedPackages Set of package permissions that should be allowed
		 * @return This builder instance for method chaining
		 */
		@Nonnull
		public JavaWalaTestCase.Builder allowedPackages(@Nonnull Set<PackagePermission> allowedPackages) {
			this.allowedPackages = Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null");
			return this;
		}

		/**
		 * Sets the allowed (exempt) class permissions.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param allowedClasses Set of class permissions exempt from the rules
		 * @return This builder instance for method chaining
		 */
		@Nonnull
		public JavaWalaTestCase.Builder allowedClasses(@Nonnull Set<ClassPermission> allowedClasses) {
			this.allowedClasses = Preconditions.checkNotNull(allowedClasses, "allowedClasses must not be null");
			return this;
		}

		/**
		 * Builds and returns a new JavaArchitectureTestCase instance with the
		 * configured properties.
		 *
		 * @since 2.0.0
		 * @author Sarp Sahinalp
		 * @return A new JavaArchitectureTestCase instance
		 * @throws SecurityException if required parameters are missing
		 */
		@Nonnull
		public JavaWalaTestCase build() {
			return new JavaWalaTestCase(
					Preconditions.checkNotNull(javaArchitectureTestCaseSupported,
							"javaArchitecturalTestCaseSupported must not be null"),
					Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null"),
					Preconditions.checkNotNull(javaClasses, "javaClasses must not be null"),
					Preconditions.checkNotNull(callGraph, "callGraph must not be null"), allowedClasses);
		}
	}
	// </editor-fold>
}
