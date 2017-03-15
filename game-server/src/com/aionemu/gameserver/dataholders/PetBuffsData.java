package com.aionemu.gameserver.dataholders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.pet.PetBuff;

/**
 * @author Rolandas
 */

/**
 * <p>
 * Java class for anonymous complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="buff" type="{}PetBuff" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
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
