package com.aionemu.gameserver.model.templates.npc;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TalkInfo")
public class TalkInfo {

	@XmlAttribute(name = "distance")
	private int talkDistance = 2;

	@XmlAttribute(name = "delay")
	private int talkDelay;

	@XmlAttribute(name = "is_dialog")
	private boolean hasDialog;

	@XmlAttribute(name = "func_dialogs")
	private List<Integer> funcDialogIds;

	@XmlAttribute(name = "subdialog_type")
	private SubDialogType subDialogType;

	@XmlAttribute(name = "subdialog_value")
	private int subDialogValue;

	@XmlAttribute(name = "can_talk_invisible")
	private boolean canTalkInvisible = true;

	public int getDistance() {
		return talkDistance;
	}

	public int getDelay() {
		return talkDelay;
	}

	public boolean isDialogNpc() {
		return hasDialog;
	}

	public List<Integer> getFuncDialogIds() {
		return funcDialogIds;
	}

	public SubDialogType getSubDialogType() {
		return subDialogType;
	}

	public int getSubDialogValue() {
		return subDialogValue;
	}

	public boolean isCanTalkInvisible() {
		return canTalkInvisible;
	}

}
