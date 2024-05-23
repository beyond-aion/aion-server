package com.aionemu.gameserver.dataholders;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.templates.item.bonuses.RandomBonusSet;
import com.aionemu.gameserver.model.templates.item.bonuses.StatBonusType;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

/**
 * Generated on: 2012.04.21 at 10:56:19 PM EEST
 * 
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "random_bonuses")
public class ItemRandomBonusData {

	@XmlElement(name = "random_bonus", required = true)
	private List<RandomBonusSet> randomBonusSets;

	@XmlTransient
	private final Map<StatBonusType, Map<Integer, RandomBonusSet>> bonusData = new EnumMap<>(StatBonusType.class);

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (StatBonusType statBonusType : StatBonusType.values())
			bonusData.put(statBonusType, new HashMap<>());
		for (RandomBonusSet bonus : randomBonusSets) {
			bonusData.get(bonus.getBonusType()).put(bonus.getId(), bonus);
		}
		randomBonusSets = null;
	}

	/**
	 * NCSoft in their wisdom decided to implement different stat bonus sets with identical content and use one for the base item and the other one for
	 * the purified version of that item. To ensure that purification does not trigger a re-roll of random bonus stats, we need this method 
	 */
	public boolean areBonusSetsEqual(StatBonusType statBonusType, int statBonusSetId1, int statBonusSetId2) {
		if (statBonusSetId1 == statBonusSetId2)
			return true;
		RandomBonusSet bonusSet1 = getBonusSet(statBonusType, statBonusSetId1);
		RandomBonusSet bonusSet2 = getBonusSet(statBonusType, statBonusSetId2);
		if (bonusSet1 == null || bonusSet2 == null)
			return bonusSet1 == bonusSet2;
		// if size comparison isn't sufficient we should override ModifiersTemplate.equals() and check if both lists are equal
		return bonusSet1.getModifiers().size() == bonusSet2.getModifiers().size();
	}

	private RandomBonusSet getBonusSet(StatBonusType statBonusType, int statBonusSetId) {
		return bonusData.get(statBonusType).get(statBonusSetId);
	}

	public int selectRandomBonusNumber(StatBonusType statBonusType, int statBonusSetId) {
		RandomBonusSet bonus = getBonusSet(statBonusType, statBonusSetId);
		if (bonus == null)
			return 0;

		List<ModifiersTemplate> modifiersGroup = bonus.getModifiers();
		float chance = Rnd.nextFloat(calculateSumOfChances(modifiersGroup));
		float chanceSum = 0;
		for (int i = 0; i < modifiersGroup.size(); i++) {
			chanceSum += modifiersGroup.get(i).getChance();
			if (chanceSum >= chance)
				return i + 1;
		}
		return 0;
	}

	private float calculateSumOfChances(List<ModifiersTemplate> modifiersGroup) {
		float sumOfAllChances = 0;
		for (ModifiersTemplate modifiersTemplate : modifiersGroup)
			sumOfAllChances += modifiersTemplate.getChance();
		return sumOfAllChances;
	}

	public ModifiersTemplate getTemplate(StatBonusType statBonusType, int statBonusSetId, int statBonusId) {
		RandomBonusSet bonus = getBonusSet(statBonusType, statBonusSetId);
		return bonus == null ? null : bonus.getModifiers().get(statBonusId - 1);
	}

	public int size() {
		return bonusData.values().stream().mapToInt(Map::size).sum();
	}

}
