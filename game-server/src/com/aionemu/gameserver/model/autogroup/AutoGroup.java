package com.aionemu.gameserver.model.autogroup;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AutoGroup")
public class AutoGroup {

	@XmlAttribute(required = true)
	protected int id;
	@XmlAttribute(required = true)
	protected int instanceId;
	@XmlAttribute(name = "name_id")
	protected int nameId;
	@XmlAttribute(name = "title_id")
	protected int titleId;
	@XmlAttribute(name = "min_lvl")
	protected int minLvl;
	@XmlAttribute(name = "max_lvl")
	protected int maxLvl;
	@XmlAttribute(name = "register_quick")
	protected boolean registerQuick;
	@XmlAttribute(name = "register_group")
	protected boolean registerGroup;
	@XmlAttribute(name = "register_new")
	protected boolean registerNew;
	@XmlAttribute(name = "npc_ids")
	protected List<Integer> npcIds;

	public int getId() {
		return id;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public int getNameId() {
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

	public boolean hasRegisterQuick() {
		return registerQuick;
	}

	public boolean hasRegisterGroup() {
		return registerGroup;
	}

	public boolean hasRegisterNew() {
		return registerNew;
	}

	public List<Integer> getNpcIds() {
		if (npcIds == null) {
			npcIds = Collections.emptyList();
		}
		return this.npcIds;
	}
}
