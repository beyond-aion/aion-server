package com.aionemu.commons.callbacks;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.aionemu.commons.callbacks.files.*;
import com.aionemu.commons.callbacks.util.GlobalCallbackHelper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Rolandas
 */
@SuppressFBWarnings({ "BC_IMPOSSIBLE_INSTANCEOF", "BC_IMPOSSIBLE_CAST" })
public class CallbacksTest {

	@Test
	public void testIntResultNoCallback() {
		final AtomicBoolean beforeInvoked = new AtomicBoolean();
		final AtomicBoolean afterInvoked = new AtomicBoolean();
		int val = 10;
		TestCallbackIntObject obj = new TestCallbackIntObject(val);
		EnhancedObject eo = (EnhancedObject) obj;
		eo.addCallback(new AbstractCallback() {

			@Override
			public CallbackResult<?> beforeCall(Object obj, Object[] args) {
				beforeInvoked.set(true);
				return CallbackResult.newContinue();
			}

			@Override
			public CallbackResult<?> afterCall(Object obj, Object[] args, Object methodResult) {
				afterInvoked.set(true);
				return CallbackResult.newContinue();
			}
		});

		int res = obj.getValue();

		assertTrue(beforeInvoked.get());
		assertTrue(afterInvoked.get());
		assertEquals(res, val);
	}

	@Test
	public void testIntResultBeforeCallback() {
		final AtomicBoolean beforeInvoked = new AtomicBoolean();
		final AtomicBoolean afterInvoked = new AtomicBoolean();
		int val = 10;
		final int newVal = 100;
		TestCallbackIntObject obj = new TestCallbackIntObject(val);
		EnhancedObject eo = (EnhancedObject) obj;
		eo.addCallback(new AbstractCallback() {

			@Override
			public CallbackResult<?> beforeCall(Object obj, Object[] args) {
				beforeInvoked.set(true);
				return CallbackResult.newFullBlocker(newVal);
			}

			@Override
			public CallbackResult<?> afterCall(Object obj, Object[] args, Object methodResult) {
				afterInvoked.set(true);
				return CallbackResult.newContinue();
			}
		});

		int res = obj.getValue();

		assertTrue(beforeInvoked.get());
		assertTrue(afterInvoked.get());
		assertEquals(res, newVal);
	}

	@Test
	public void testGlobalStaticMehtodCallback() {
		assertEquals(TestGlobalCallbacksCaller.sayStaticHello("Hello"), "Hello");

		Callback<?> cb = new AbstractCallback() {

			@Override
			public CallbackResult<?> beforeCall(Object obj, Object[] args) {
				return CallbackResult.newFullBlocker("Hello World");
			}

			@Override
			public CallbackResult<?> afterCall(Object obj, Object[] args, Object methodResult) {
				return CallbackResult.newContinue();
			}
		};
		GlobalCallbackHelper.addCallback(cb);
		assertEquals(TestGlobalCallbacksCaller.sayStaticHello("Hello"), "Hello World");

		GlobalCallbackHelper.removeCallback(cb);
		assertEquals(TestGlobalCallbacksCaller.sayStaticHello("Hello"), "Hello");
	}

	@Test
	public void testGlobalMethodCallback() {
		assertEquals(TestGlobalCallbacksCaller.getInstance().sayHello("Hello"), "Hello");

		Callback<?> cb = new AbstractCallback() {

			@Override
			public CallbackResult<?> beforeCall(Object obj, Object[] args) {
				return CallbackResult.newFullBlocker("Hello World");
			}

			@Override
			public CallbackResult<?> afterCall(Object obj, Object[] args, Object methodResult) {
				return CallbackResult.newContinue();
			}
		};
		GlobalCallbackHelper.addCallback(cb);
		assertEquals(TestGlobalCallbacksCaller.getInstance().sayHello("Hello"), "Hello World");

		GlobalCallbackHelper.removeCallback(cb);
		assertEquals(TestGlobalCallbacksCaller.getInstance().sayHello("Hello"), "Hello");
	}

	@Test
	public void testGlobalMethodTwoCallback() {
		assertEquals(TestGlobalCallbacksCaller.getInstance().sayHello("Hello"), "Hello");

		Callback<?> cb1 = new AbstractCallback() {

			@Override
			public CallbackResult<?> beforeCall(Object obj, Object[] args) {
				return CallbackResult.newFullBlocker("Hello World 1");
			}

			@Override
			public CallbackResult<?> afterCall(Object obj, Object[] args, Object methodResult) {
				return CallbackResult.newContinue();
			}
		};

		Callback<?> cb2 = new AbstractCallback() {

			@Override
			public CallbackResult<?> beforeCall(Object obj, Object[] args) {
				return CallbackResult.newFullBlocker("Hello World 2");
			}

			@Override
			public CallbackResult<?> afterCall(Object obj, Object[] args, Object methodResult) {
				return CallbackResult.newContinue();
			}
		};

		GlobalCallbackHelper.addCallback(cb1);
		assertEquals(TestGlobalCallbacksCaller.getInstance().sayHello("Hello"), "Hello World 1");

		GlobalCallbackHelper.addCallback(cb2);
		GlobalCallbackHelper.removeCallback(cb1);
		assertEquals(TestGlobalCallbacksCaller.getInstance().sayHello("Hello"), "Hello World 2");

		GlobalCallbackHelper.removeCallback(cb2);
		assertEquals(TestGlobalCallbacksCaller.getInstance().sayHello("Hello"), "Hello");
	}

	@Test
	public void testInheritance() {
		InheritanceTestChild itc = new InheritanceTestChild();
		assertTrue(itc instanceof EnhancedObject);

		String result = "fffffff";

		((EnhancedObject) itc).addCallback(new InheritanceTestCallback(result));

		assertEquals(itc.publicMethod(), result);
	}

	@Test
	public void testRemoveObjectCallbackFromCallback() {
		TestCallbackIntObject obj = new TestCallbackIntObject();
		final EnhancedObject eo = (EnhancedObject) obj;
		final AtomicBoolean shouldBeTrue = new AtomicBoolean();
		final AtomicBoolean shouldNotBeTrue = new AtomicBoolean();

		eo.addCallback(new AbstractCallback() {

			@Override
			public CallbackResult<?> beforeCall(Object obj, Object[] args) {
				assertEquals(eo.getCallbacks().size(), 1);
				shouldBeTrue.set(true);
				eo.removeCallback(this);
				return CallbackResult.newContinue();
			}

			@Override
			public CallbackResult<?> afterCall(Object obj, Object[] args, Object methodResult) {
				shouldNotBeTrue.set(true);
				return CallbackResult.newContinue();
			}
		});

		obj.getValue();

		assertTrue(shouldBeTrue.get());
		assertFalse(shouldNotBeTrue.get());
		assertNull(eo.getCallbacks());
	}

	@Test
	public void testRemoveGlobalCallbackFromCallback() {

		final AtomicBoolean shouldBeTrue = new AtomicBoolean();
		final AtomicBoolean shouldNotBeTrue = new AtomicBoolean();

		Callback<?> cb = new AbstractCallback() {

			@Override
			public CallbackResult<?> beforeCall(Object obj, Object[] args) {
				shouldBeTrue.set(true);
				GlobalCallbackHelper.removeCallback(this);
				return CallbackResult.newContinue();
			}

			@Override
			public CallbackResult<?> afterCall(Object obj, Object[] args, Object methodResult) {
				shouldNotBeTrue.set(true);
				return CallbackResult.newContinue();
			}
		};

		GlobalCallbackHelper.addCallback(cb);
		TestGlobalCallbacksCaller.getInstance().sayHello("Hello");

		assertTrue(shouldBeTrue.get());
		assertFalse(shouldNotBeTrue.get());
	}
}
