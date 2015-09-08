package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.npc.NpcTemplateType;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.TribeClass;

/**
 * @author synchro2, bobobear
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalExclusion")
public class GlobalExclusion {

	@XmlAttribute(name = "npc_ids", required = false)
	private List<Integer> excludedNpcIds;

	@XmlAttribute(name = "npc_names", required = false)
	private List<String> excludedNpcNames;

	@XmlAttribute(name = "npc_types", required = false)
	private List<NpcTemplateType> excludedTypes;

	@XmlAttribute(name = "npc_tribes", required = false)
	private List<TribeClass> excludedTribes;

	@XmlAttribute(name = "npc_abyss_types", required = false)
	private List<AbyssNpcType> excludedAbyssTypes;

  public List<Integer> getNpcIds() {
    if (excludedNpcIds == null) {
    	excludedNpcIds = new ArrayList<Integer>();
    }
    return excludedNpcIds;
}
	public List<String> getNpcNames() {
    if (excludedNpcNames == null) {
    	excludedNpcNames = new ArrayList<String>();
    }
    return excludedNpcNames;
	}

	public List<NpcTemplateType> getNpcTemplateTypes() {
    if (excludedTypes == null) {
    	excludedTypes = new ArrayList<NpcTemplateType>();
    }
    return excludedTypes;
	}

	public List<TribeClass> getNpcTribes() {
    if (excludedTribes == null) {
    	excludedTribes = new ArrayList<TribeClass>();
    }
    return excludedTribes;
	}

	public List<AbyssNpcType> getNpcAbyssTypes() {
    if (excludedAbyssTypes == null) {
    	excludedAbyssTypes = new ArrayList<AbyssNpcType>();
    }
		return excludedAbyssTypes;
	}
}
