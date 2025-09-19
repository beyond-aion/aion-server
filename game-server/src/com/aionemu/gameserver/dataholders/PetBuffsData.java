package com.aionemu.gameserver.dataholders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.pet.PetBuff;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "buffs" })
@XmlRootElement(name = "pet_buffs")
public class PetBuffsData {

	@XmlElement(name = "buff", required = true)
	protected List<PetBuff> buffs;

	@XmlTransient
	private Map<Integer, PetBuff> petBuffsById = new LinkedHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (buffs == null)
			return;

		for (PetBuff buff : buffs)
			petBuffsById.put(buff.getId(), buff);

		buffs.clear();
		buffs = null;
	}

	public PetBuff getPetBuff(Integer buffId) {
		return petBuffsById.get(buffId);
	}

	public int size() {
		return petBuffsById.size();
	}
}
