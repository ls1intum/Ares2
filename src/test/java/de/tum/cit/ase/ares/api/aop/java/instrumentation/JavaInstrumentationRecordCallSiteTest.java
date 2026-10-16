package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

/**
 * Checks that the agent's rewrite of thread and monitor calls accepts a Java
 * record, still rewrites the calls inside it, and changes nothing else.
 */
class JavaInstrumentationRecordCallSiteTest {

	/** Resets the counters. */
	@BeforeEach
	void resetCounters() {
		RecordCallSiteFixtures.STARTS.set(0);
		RecordCallSiteFixtures.NOTIFIES.set(0);
	}

	/**
	 * A record passes through the rewrite, twice in a row, and its generated
	 * {@code toString}, {@code equals} and {@code hashCode} still work.
	 */
	@Test
	void aRecordIsRewrittenAndKeepsItsGeneratedMethods() throws ReflectiveOperationException {
		for (int round = 0; round < 2; round++) {
			Class<?> point = rewritten(RecordCallSitePoint.class);
			Object first = point.getConstructor(int.class, int.class).newInstance(1, 2);
			Object second = point.getConstructor(int.class, int.class).newInstance(1, 2);
			assertThat(first).hasToString("RecordCallSitePoint[x=1, y=2]").isEqualTo(second);
			assertThat(first.hashCode()).isEqualTo(second.hashCode());
		}
	}

	/**
	 * A thread start and a monitor call inside a record are still rewritten.
	 */
	@Test
	void callsInsideARecordAreStillRewritten() throws ReflectiveOperationException {
		Class<?> point = rewritten(RecordCallSitePoint.class);
		Thread thread = new Thread(() -> {
		});
		point.getMethod("startThread", Thread.class).invoke(null, thread);
		point.getMethod("notifyMonitor", Object.class).invoke(null, new Object());
		assertThat(RecordCallSiteFixtures.STARTS).hasValue(1);
		assertThat(RecordCallSiteFixtures.NOTIFIES).hasValue(1);
		assertThat(thread.getState()).isEqualTo(Thread.State.NEW);
	}

	/**
	 * Every other dynamic call is rewritten exactly as the plain substitution
	 * rewrites it: the bytes are identical.
	 */
	@Test
	void otherDynamicCallsAreRewrittenExactlyAsBefore() throws ReflectiveOperationException {
		Method start = RecordCallSiteFixtures.class.getMethod("countStart", Thread.class);
		AsmVisitorWrapper plain = MemberSubstitution.relaxed().method(ElementMatchers.isDeclaredBy(Thread.class)
				.and(ElementMatchers.named("start")).and(ElementMatchers.takesArguments(0))).replaceWith(start)
				.on(ElementMatchers.any());
		AsmVisitorWrapper wrapped = JavaInstrumentationAgent.threadStartSubstitution(start);
		assertThat(bytesOf(RecordCallSiteFixtures.OtherDynamicCalls.class, wrapped))
				.isEqualTo(bytesOf(RecordCallSiteFixtures.OtherDynamicCalls.class, plain));
	}

	/**
	 * Applies the agent's thread-start and notify rewrites to a class and loads the
	 * result in its own class loader.
	 *
	 * @param type the class
	 * @return the rewritten class
	 */
	private static Class<?> rewritten(Class<?> type) throws ReflectiveOperationException {
		Method start = RecordCallSiteFixtures.class.getMethod("countStart", Thread.class);
		Method notify = RecordCallSiteFixtures.class.getMethod("countNotify", Object.class);
		ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(type.getClassLoader());
		return new ByteBuddy().redefine(TypePool.Default.of(locator).describe(type.getName()).resolve(), locator)
				.visit(JavaInstrumentationAgent.threadStartSubstitution(start))
				.visit(JavaInstrumentationAgent.monitorSubstitution("notify", 0, notify)).make()
				.load(type.getClassLoader(), ClassLoadingStrategy.Default.CHILD_FIRST).getLoaded();
	}

	/**
	 * Rewrites a class with one rewrite and returns the bytes.
	 *
	 * @param type    the class
	 * @param rewrite the rewrite
	 * @return the rewritten class file
	 */
	private static byte[] bytesOf(Class<?> type, AsmVisitorWrapper rewrite) {
		ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(type.getClassLoader());
		return new ByteBuddy().redefine(TypePool.Default.of(locator).describe(type.getName()).resolve(), locator)
				.visit(rewrite).make().getBytes();
	}
}
