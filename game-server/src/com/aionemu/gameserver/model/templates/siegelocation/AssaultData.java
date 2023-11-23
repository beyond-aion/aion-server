package com.aionemu.gameserver.model.templates.siegelocation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.siege.Assaulter;
import com.aionemu.gameserver.model.siege.AssaulterType;

/**
 * @author Estrayl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssaultData")
public class AssaultData {

	@XmlElement(name = "assaulter")
	private List<AssaulterTemplate> assaulterTemplates;

	@XmlAttribute(name = "dredgion_id")
	private int dredgionId;
	@XmlAttribute(name = "base_budget")
	private int baseBudget;
	@XmlAttribute(name = "base_delay")
	private int baseDelay;

	@XmlTransient
	private EnumMap<AssaulterType, List<Assaulter>> processedAssaulters = new EnumMap<>(AssaulterType.class);

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (AssaulterTemplate a : assaulterTemplates) {
			AssaulterType type = a.getAssaulterType();
			List<Integer> npcIds = a.getNpcIds();
			List<Float> spawnCosts = type.getSpawnCosts();
			List<Assaulter> processed = new ArrayList<>();
			for (int i = 0; i < npcIds.size(); i++) {
				if (type == AssaulterType.TELEPORT)
					processed.add(new Assaulter(npcIds.get(i), 0, a.getHeadingOffset(), a.getDistanceOffset()));
				else if (i < spawnCosts.size())
					processed.add(new Assaulter(npcIds.get(i), spawnCosts.get(i), a.getHeadingOffset(), a.getDistanceOffset()));
			}
			processedAssaulters.put(type, processed);
		}
		assaulterTemplates = null;
	}

	public EnumMap<AssaulterType, List<Assaulter>> getProcessedAssaulters() {
		return processedAssaulters;
	}

	public int getDredgionId() {
		return dredgionId;
	}

	public int getBaseBudget() {
		return baseBudget;
	}

	public int getBaseDelay() {
		return baseDelay;
	}

}
