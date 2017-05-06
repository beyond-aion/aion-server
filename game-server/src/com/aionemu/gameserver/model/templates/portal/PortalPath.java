package com.aionemu.gameserver.model.templates.portal;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PortalPath")
public class PortalPath {

	@XmlAttribute(name = "dialog")
	private int dialog;
	@XmlAttribute(name = "loc_id")
	private int locId;
	@XmlAttribute(name = "player_count")
	private int playerCount = 1;
	@XmlAttribute(name = "instance")
	private boolean instance;
	@XmlAttribute(name = "siege_id")
	private int siegeId;
	@XmlAttribute(name = "race")
	private Race race = Race.PC_ALL;
	@XmlAttribute(name = "min_level")
	private int minLevel;
	@XmlAttribute(name = "max_level")
	private int maxLevel;
	@XmlAttribute(name = "min_rank")
	private int minRank;
	@XmlAttribute(name = "kinah")
	private int kinah;
	@XmlAttribute(name = "title_id")
	private int titleId;
	@XmlAttribute(name = "err_group")
	private int errGroup;
	@XmlAttribute(name = "err_level")
	private int errLevel;
	@XmlElement(name = "quest_req")
	private List<QuestReq> questReq;
	@XmlElement(name = "item_req")
	private List<ItemReq> itemReq;

	public int getDialog() {
		return dialog;
	}

	public int getLocId() {
		return locId;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public boolean isInstance() {
		return instance;
	}

	public int getSiegeId() {
		return siegeId;
	}

	public Race getRace() {
		return race;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public int getMinRank() {
		return minRank;
	}

	public int getKinah() {
		return kinah;
	}

	public int getTitleId() {
		return titleId;
	}

	public int getErrGroup() {
		return errGroup;
	}

	public int getErrLevel() {
		return errLevel;
	}

	public List<QuestReq> getQuestReq() {
		return questReq;
	}

	public List<ItemReq> getItemReq() {
		return itemReq;
	}

}
