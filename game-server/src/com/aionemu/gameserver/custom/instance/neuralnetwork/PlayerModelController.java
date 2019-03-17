package com.aionemu.gameserver.custom.instance.neuralnetwork;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.custom.instance.CustomInstanceService;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Jo
 */
public class PlayerModelController {

	public static PlayerModel trainModelForPlayer(Player player, List<Integer> skillSet) {
		// get input data:
		List<PlayerModelEntry> playerModelEntries = CustomInstanceService.getInstance().getPlayerModelEntries(player.getObjectId());

		// specify model:
		List<DataSet> dataSets = new ArrayList<>();

		for (int i = 0; i < playerModelEntries.size(); i++) {
			if (!playerModelEntries.get(i).isBossPhase())
				continue;
			int previousSkillID = -1;
			if (i > 0)
				previousSkillID = playerModelEntries.get(i - 1).getSkillID();
			dataSets.add(
				new DataSet(playerModelEntries.get(i).toStateInputArray(skillSet, previousSkillID), playerModelEntries.get(i).toActionOutputArray(skillSet)));
		}

		if (dataSets.isEmpty())
			return null;

		PlayerModel model = new PlayerModel(dataSets.get(0).getValues().length, 10, dataSets.get(0).getTargets().length, 1, null, null);

		// train
		ThreadPoolManager.getInstance().execute(() -> model.train(dataSets, 1000));
		return model;
	}

	public static List<Integer> getSkillSetForPlayer(int playerId) {
		List<Integer> skillSet = new ArrayList<>();
		for (PlayerModelEntry pme : CustomInstanceService.getInstance().getPlayerModelEntries(playerId))
			if (!skillSet.contains(pme.getSkillID()))
				skillSet.add(pme.getSkillID());

		return skillSet;
	}

	public static int getActionOutput(PlayerModel model, double[] inputArray) {
		List<Double> targetValues = model.getOutputEstimation(inputArray);
		return getMaxIndex(targetValues);
	}

	public static int getMaxIndex(List<Double> values) {
		int i = 0;
		int actionI = 0;
		double max_t = 0;

		for (double t : values) {
			if (t > max_t) {
				max_t = t;
				actionI = i;
			}
			i++;
		}
		return actionI;

	}

}
