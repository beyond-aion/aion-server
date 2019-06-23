package com.aionemu.gameserver.custom.instance.neuralnetwork;

/**
 * @author Jo
 */
public class Sigmoid {

	public static double output(double x) {
		return x < -45.0 ? 0.0 : x > 45.0 ? 1.0 : 1.0 / (1.0 + Math.exp((float) -x));
	}

	public static double derivative(double x) {
		return x * (1 - x);
	}
}
