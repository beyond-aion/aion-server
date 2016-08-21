package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.npc.NpcTemplateType;

import javolution.util.FastTable;

/**
 * @author synchro2, bobobear
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
			excludedNpcIds = new FastTable<>();
		}
		return excludedNpcIds;
	}

	public List<String> getNpcNames() {
		if (excludedNpcNames == null) {
			excludedNpcNames = new FastTable<>();
		}
		return excludedNpcNames;
	}

	public List<NpcTemplateType> getNpcTemplateTypes() {
		if (excludedTypes == null) {
			excludedTypes = new FastTable<>();
		}
		return excludedTypes;
	}

	public List<TribeClass> getNpcTribes() {
		if (excludedTribes == null) {
			excludedTribes = new FastTable<>();
		}
		return excludedTribes;
	}

	public List<AbyssNpcType> getNpcAbyssTypes() {
		if (excludedAbyssTypes == null) {
			excludedAbyssTypes = new FastTable<>();
		}
		return excludedAbyssTypes;
	}
}
