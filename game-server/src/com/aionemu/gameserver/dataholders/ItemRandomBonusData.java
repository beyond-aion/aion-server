package com.aionemu.gameserver.dataholders;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.templates.item.bonuses.RandomBonusSet;
import com.aionemu.gameserver.model.templates.item.bonuses.StatBonusType;
import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

import gnu.trove.impl.hash.THash;
import gnu.trove.map.hash.TIntObjectHashMap;

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

	private final Map<StatBonusType, TIntObjectHashMap<RandomBonusSet>> bonusData = new EnumMap<>(StatBonusType.class);

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (StatBonusType statBonusType : StatBonusType.values())
			bonusData.put(statBonusType, new TIntObjectHashMap<>());
		for (RandomBonusSet bonus : randomBonusSets) {
			bonusData.get(bonus.getBonusType()).put(bonus.getId(), bonus);
		}
		randomBonusSets = null;
	}

	public int selectRandomBonusNumber(StatBonusType statBonusType, int statBonusSetId) {
		RandomBonusSet bonus = bonusData.get(statBonusType).get(statBonusSetId);
		if (bonus == null)
			return 0;

		List<ModifiersTemplate> modifiersGroup = bonus.getModifiers();
		float chance = Rnd.get() * calculateSumOfChances(modifiersGroup);
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
		RandomBonusSet bonus = bonusData.get(statBonusType).get(statBonusSetId);
		return bonus == null ? null : bonus.getModifiers().get(statBonusId - 1);
	}

	public int size() {
		return bonusData.values().stream().mapToInt(THash::size).sum();
	}

}
