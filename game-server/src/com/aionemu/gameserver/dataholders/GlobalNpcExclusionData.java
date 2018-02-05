package com.aionemu.gameserver.dataholders;

import java.util.Collections;
import java.util.Set;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.npc.NpcTemplateType;

/**
 * @author bobobear
 */
@XmlRootElement(name = "global_npc_exclusions")
@XmlAccessorType(XmlAccessType.FIELD)
public class GlobalNpcExclusionData {

	@XmlList
	@XmlElement(name = "npc_ids")
	private Set<Integer> excludedNpcIds;

	@XmlList
	@XmlElement(name = "npc_names")
	private Set<String> excludedNpcNames;

	@XmlList
	@XmlElement(name = "npc_types")
	private Set<NpcTemplateType> excludedTypes;

	@XmlList
	@XmlElement(name = "npc_tribes")
	private Set<TribeClass> excludedTribes;

	@XmlList
	@XmlElement(name = "npc_abyss_types")
	private Set<AbyssNpcType> excludedAbyssTypes;

	@XmlTransient
	boolean isEmpty;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		isEmpty = excludedNpcIds == null && excludedNpcNames == null && excludedTypes == null && excludedTribes == null && excludedAbyssTypes == null;
	}

	public Set<Integer> getNpcIds() {
		return excludedNpcIds == null ? Collections.emptySet() : excludedNpcIds;
	}

	public Set<String> getNpcNames() {
		return excludedNpcNames == null ? Collections.emptySet() : excludedNpcNames;
	}

	public Set<NpcTemplateType> getNpcTemplateTypes() {
		return excludedTypes == null ? Collections.emptySet() : excludedTypes;
	}

	public Set<TribeClass> getNpcTribes() {
		return excludedTribes == null ? Collections.emptySet() : excludedTribes;
	}

	public Set<AbyssNpcType> getNpcAbyssTypes() {
		return excludedAbyssTypes == null ? Collections.emptySet() : excludedAbyssTypes;
	}

	public boolean isEmpty() {
		return isEmpty;
	}
}
