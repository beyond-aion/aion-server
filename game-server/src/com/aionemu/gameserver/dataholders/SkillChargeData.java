package com.aionemu.gameserver.dataholders;

import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.skillengine.model.ChargeSkillEntry;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "chargeSkills" })
@XmlRootElement(name = "skill_charge")
public class SkillChargeData {

	@XmlElement(name = "charge", required = true)
	protected List<ChargeSkillEntry> chargeSkills;

	@XmlTransient
	private final Map<Integer, ChargeSkillEntry> skillChargeData = new HashMap<>();
	@XmlTransient
	private final Set<Integer> skillIds = new HashSet<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (ChargeSkillEntry chargeSkill : chargeSkills) {
			skillChargeData.put(chargeSkill.getId(), chargeSkill);
			chargeSkill.getSkills().forEach(s -> skillIds.add(s.getId()));
		}
		chargeSkills = null;
	}

	public ChargeSkillEntry getChargedSkillEntry(int chargeId) {
		return skillChargeData.get(chargeId);
	}

	public boolean isChargeSkill(SkillTemplate skillTemplate) {
		return skillIds.contains(skillTemplate.getSkillId());
	}

	public int size() {
		return skillChargeData.size();
	}
}
