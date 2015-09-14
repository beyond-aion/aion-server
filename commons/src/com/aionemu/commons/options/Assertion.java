package com.aionemu.commons.options;

/**
 * Class with public static final booleans indicating parts of this "project" where assertion should be enabled. If assertion is disabled, assertion
 * code will be removed at compile time by javac compiler.
 * 
 * @author -Nemesiss-
 */
public final class Assertion {

	/**
	 * False if assertion at Network code should be removed at compile time. [0 overhead]
	 */
	public static final boolean NetworkAssertion = false;
}
