package com.aionemu.gameserver.custom.instance.neuralnetwork;

/**
 * @author Jo
 */
public class Link {

	private PlayerModelLink input;
	private PlayerModelLink output;
	private double weight;
	private double weightDelta;

	public Link(PlayerModelLink input, PlayerModelLink output) {
		this.input = input;
		this.output = output;
		weight = PlayerModel.getRandom();
	}

	public PlayerModelLink getInput() {
		return input;
	}

	public PlayerModelLink getOutput() {
		return output;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getWeightDelta() {
		return weightDelta;
	}

	public void setWeightDelta(double weightDelta) {
		this.weightDelta = weightDelta;
	}

}
