package com.aionemu.gameserver.model.templates.quest;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.L10n;

/**
 * @author vlog
 */
@XmlType(name = "QuestRepeatCycle")
@XmlEnum
public enum QuestRepeatCycle implements L10n {
	ALL(0, 0),
	MON(1, 900331),
	TUE(2, 900332),
	WED(3, 900333),
	THU(4, 900334),
	FRI(5, 900335),
	SAT(6, 900336),
	SUN(7, 900330);

	private int weekDay;
	private int nameId;

	private QuestRepeatCycle(int weekDay, int nameId) {
		this.weekDay = weekDay;
		this.nameId = nameId;
	}

	public int getDay() {
		return weekDay;
	}

	@Override
	public int getL10nId() {
		return nameId;
	}
}
