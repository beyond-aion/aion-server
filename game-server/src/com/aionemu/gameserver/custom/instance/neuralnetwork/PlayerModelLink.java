package com.aionemu.gameserver.custom.instance.neuralnetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jo
 */
public class PlayerModelLink {

	public List<Link> inputs;
	public List<Link> outputs;
	public double bias;
	public double biasDelta;
	public double gradient;
	public double value;

	public PlayerModelLink() {
		inputs = new ArrayList<Link>();
		outputs = new ArrayList<Link>();
		bias = PlayerModel.getRandom();
	}

	public PlayerModelLink(List<PlayerModelLink> inputList) {
		this();

		for (PlayerModelLink input : inputList) {
			Link l = new Link(input, this);
			input.outputs.add(l);
			inputs.add(l);
		}
	}

	public double calculateValue() {
		double sum = 0;
		for (Link l : inputs)
			sum += l.getWeight() * l.getInput().value;

		value = Sigmoid.output(sum + bias);
		return value;
	}

	public double calculateError(double target) {
		return target - value;
	}

	public double calculateGradient(Double target) {
		if (target == null) {
			double sum = 0;
			for (Link l : outputs)
				sum += l.getOutput().gradient * l.getWeight();
			gradient = sum * Sigmoid.derivative(value);
			return gradient;
		}

		gradient = calculateError(target.doubleValue()) * Sigmoid.derivative(value);
		return gradient;
	}

	public void updateWeights(double learnRate, double momentum) {
		double prevDelta = biasDelta;
		biasDelta = learnRate * gradient;
		bias += biasDelta + momentum * prevDelta;

		for (Link l : inputs) {
			prevDelta = l.getWeightDelta();
			l.setWeightDelta(learnRate * gradient * l.getInput().value);
			l.setWeight(l.getWeight() + l.getWeightDelta() + momentum * prevDelta);
		}
	}

}
