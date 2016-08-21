package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.ChargeSkillEntry;

import gnu.trove.map.hash.TIntObjectHashMap;

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
	private TIntObjectHashMap<ChargeSkillEntry> skillChargeData = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (ChargeSkillEntry chargeSkill : chargeSkills) {
			skillChargeData.put(chargeSkill.getId(), chargeSkill);
		}
		chargeSkills.clear();
		chargeSkills = null;
	}

	public ChargeSkillEntry getChargedSkillEntry(int chargeId) {
		return skillChargeData.get(chargeId);
	}

	public int size() {
		return skillChargeData.size();
	}
}
