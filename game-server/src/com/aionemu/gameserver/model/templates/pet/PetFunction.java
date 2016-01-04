package com.aionemu.gameserver.model.templates.pet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author IlBuono
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
		PetFunction result = new PetFunction();
		result.type = PetFunctionType.NONE;
		return result;
	}

}
