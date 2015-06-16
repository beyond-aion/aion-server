package com.aionemu.gameserver.model.templates.quest;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author vlog
 */
@XmlType(name = "QuestRepeatCycle")
@XmlEnum
public enum QuestRepeatCycle {
	ALL(0),
	MON(1),
	TUE(2),
	WED(3),
	THU(4),
	FRI(5),
	SAT(6),
	SUN(7);

	private int weekDay;

	private QuestRepeatCycle(int weekDay) {
		this.weekDay = weekDay;
	}

	public int getDay() {
		return weekDay;
	}
}
