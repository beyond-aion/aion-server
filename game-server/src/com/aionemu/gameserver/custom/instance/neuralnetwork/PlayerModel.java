package com.aionemu.gameserver.custom.instance.neuralnetwork;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.commons.utils.Rnd;

/**
 * @author Jo
 */
public class PlayerModel {

	public boolean isReady;
	public double learnRate;
	public double momentum;
	public List<PlayerModelLink> input;
	public List<List<PlayerModelLink>> inner;
	public List<PlayerModelLink> output;

	public PlayerModel(int inputSize, int hiddenSize, int outputSize, int innerSize, Double learnRate, Double momentum) {
		if (learnRate == null)
			this.learnRate = .4;
		else
			this.learnRate = learnRate;
		if (momentum == null)
			this.momentum = .9;
		else
			this.momentum = momentum;
		input = new ArrayList<>();
		inner = new ArrayList<>();
		output = new ArrayList<>();
		isReady = false;

		for (int i = 0; i < inputSize; i++)
			input.add(new PlayerModelLink());

		if (innerSize < 1)
			innerSize = 1;
		for (int i = 0; i < innerSize; i++) {
			inner.add(new ArrayList<>());
			for (int j = 0; j < hiddenSize; j++)
				inner.get(i).add(new PlayerModelLink(i == 0 ? input : inner.get(i - 1)));
		}

		for (int i = 0; i < outputSize; i++)
			output.add(new PlayerModelLink(inner.get(innerSize - 1)));
	}

	public void train(List<DataSet> dataSets, int numEpochs) {
		isReady = false;
		for (int i = 0; i < numEpochs; i++) {
			for (DataSet dataSet : dataSets) {
				processInput(dataSet.getValues());
				valideToOutput(dataSet.getTargets());
			}
		}
		isReady = true;
	}

	private void processInput(double... inputs) {
		int i = 0;
		for (PlayerModelLink n : input)
			n.value = inputs[i++];
		for (List<PlayerModelLink> layer : inner)
			layer.forEach(a -> a.calculateValue());
		output.forEach(a -> a.calculateValue());
	}

	private void valideToOutput(double... targets) {
		int i = 0;
		for (PlayerModelLink n : output)
			n.calculateGradient(targets[i++]);

		for (int j = inner.size() - 1; j >= 0; j--) {
			inner.get(j).forEach(a -> a.calculateGradient(null));
			inner.get(j).forEach(a -> a.updateWeights(learnRate, momentum));
		}

		output.forEach(a -> a.updateWeights(learnRate, momentum));
	}

	public List<Double> getOutputEstimation(double... inputs) {
		processInput(inputs);

		List<Double> outputList = new ArrayList<>();
		for (PlayerModelLink n : output)
			outputList.add(n.value);

		return outputList;
	}

	public static double getRandom() {
		return 2 * Rnd.nextDouble() - 1;
	}
}
