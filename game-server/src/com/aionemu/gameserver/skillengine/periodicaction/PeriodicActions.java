package com.aionemu.gameserver.skillengine.periodicaction;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author antness
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PeriodicActions", propOrder = "periodicActions")
public class PeriodicActions {

	@XmlElements({ @XmlElement(name = "hpuse", type = HpUsePeriodicAction.class), @XmlElement(name = "mpuse", type = MpUsePeriodicAction.class) })
	protected List<PeriodicAction> periodicActions;
	@XmlAttribute(name = "checktime")
	protected int checktime;

	public List<PeriodicAction> getPeriodicActions() {
		return periodicActions;
	}

	public int getChecktime() {
		return checktime;
	}
}
