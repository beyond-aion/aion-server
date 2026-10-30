package com.aionemu.gameserver.model.templates.instance_bonusatrr;

import java.util.List;

import jakarta.xml.bind.annotation.*;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InstanceBonusAttr", propOrder = { "penaltyAttr" })
public class InstanceBonusAttr {

	@XmlElement(name = "penalty_attr")
	protected List<InstancePenaltyAttr> penaltyAttr;
	@XmlAttribute(name = "buff_id", required = true)
	protected int buffId;

	public List<InstancePenaltyAttr> getPenaltyAttr() {
		return penaltyAttr == null ? List.of() : penaltyAttr;
	}

	public int getBuffId() {
		return buffId;
	}
}
