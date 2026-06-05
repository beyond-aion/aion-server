package com.aionemu.gameserver.model.templates.pet;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.L10n;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.model.templates.stats.PetStatsTemplate;

/**
 * @author IlBuono
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "pet")
public class PetTemplate extends VisibleObjectTemplate implements L10n {

	@XmlAttribute(name = "id", required = true)
	private int id;
	@XmlAttribute(name = "name", required = true)
	private String name;
	@XmlAttribute(name = "nameid", required = true)
	private int nameId;
	@XmlAttribute(name = "condition_reward")
	private int conditionReward;
	@XmlElement(name = "petfunction")
	private List<PetFunction> petFunctions;
	@XmlElement(name = "petstats")
	private PetStatsTemplate petStats;
	@XmlTransient
	Boolean hasPlayerFuncs = null;

	@Override
	public int getTemplateId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getL10nId() {
		return nameId;
	}

	public List<PetFunction> getPetFunctions() {
		if (hasPlayerFuncs == null) {
			hasPlayerFuncs = false;
			if (petFunctions == null) {
				List<PetFunction> result = new ArrayList<>();
				result.add(PetFunction.CreateEmpty());
				petFunctions = result;
			} else {
				for (PetFunction func : petFunctions) {
					if (func.getPetFunctionType().isPlayerFunction()) {
						hasPlayerFuncs = true;
						break;
					}
				}
				if (!hasPlayerFuncs)
					petFunctions.add(PetFunction.CreateEmpty());
			}
		}
		return petFunctions;
	}

	public boolean containsFunction(PetFunctionType type) {
		if (type.getId() < 0)
			return false;

		for (PetFunction t : getPetFunctions()) {
			if (t.getPetFunctionType() == type)
				return true;
		}
		return false;
	}

	public PetFunction getPetFunction(PetFunctionType type) {
		for (PetFunction t : getPetFunctions()) {
			if (t.getPetFunctionType() == type)
				return t;
		}
		return null;
	}

	@SuppressWarnings("unused")
	public PetStatsTemplate getPetStats() {
		return petStats;
	}

	public final int getConditionReward() {
		return conditionReward;
	}
}