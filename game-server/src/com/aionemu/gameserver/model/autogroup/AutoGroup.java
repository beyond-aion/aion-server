package com.aionemu.gameserver.model.autogroup;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.L10n;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AutoGroup")
public class AutoGroup implements L10n {

	@XmlAttribute(required = true)
	private int id;
	@XmlAttribute(required = true)
	private int instanceId;
	@XmlAttribute(name = "name_id")
	private int nameId;
	@XmlAttribute(name = "title_id")
	private int titleId;
	@XmlAttribute(name = "min_lvl")
	private int minLvl;
	@XmlAttribute(name = "max_lvl")
	private int maxLvl;
	@XmlAttribute(name = "register_quick")
	private boolean registerQuick;
	@XmlAttribute(name = "register_group")
	private boolean registerGroup;
	@XmlAttribute(name = "register_new")
	private boolean registerNew;
	@XmlAttribute(name = "npc_ids")
	private List<Integer> npcIds;

	public int getMaskId() {
		return id;
	}

	public int getInstanceMapId() {
		return instanceId;
	}

	@Override
	public int getL10nId() {
		return nameId;
	}

	public int getTitleId() {
		return titleId;
	}

	public int getMinLvl() {
		return minLvl;
	}

	public int getMaxLvl() {
		return maxLvl;
	}

	public boolean canRegisterQuickEntry() {
		return registerQuick;
	}

	public boolean hasRegisterGroup() {
		return registerGroup;
	}

	public boolean canRegisterNewEntry() {
		return registerNew;
	}

	public List<Integer> getNpcIds() {
		return npcIds == null ? Collections.emptyList() : npcIds;
	}

	public boolean isRecruitableInstance() {
		return id >= 302 && id < 400 || instanceId == 300600000 || instanceId == 300220000;
	}
}
