package com.aionemu.gameserver.custom.instance.neuralnetwork;

/**
 * @author Jo
 */
public class DataSet {

	private double[] values;
	private double[] targets;

	public DataSet(double[] values, double[] targets) {
		this.values = values;
		this.targets = targets;
	}

	public double[] getValues() {
		return values;
	}

	public double[] getTargets() {
		return targets;
	}
}
