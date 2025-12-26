package com.aionemu.gameserver.model.templates.pet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "petfunction")
public class PetFunction {
	@XmlAttribute(name = "type")
	private PetFunctionType type;
	@XmlAttribute(name = "id")
	private int id;
	@XmlAttribute(name = "slots")
	private int slots;
	@XmlAttribute(name = "rate_price")
	private int ratePrice;

	public PetFunctionType getPetFunctionType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public int getSlots() {
		return slots;
	}

	public int getRatePrice() {
		return ratePrice;
	}

	public static PetFunction CreateEmpty() {
		var result = new PetFunction();
		result.type = PetFunctionType.NONE;
		return result;
	}
}