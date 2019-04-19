package com.aionemu.gameserver.model.templates.itemgroups;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Chance;
import com.aionemu.gameserver.model.templates.rewards.BonusType;

/**
 * <p>
 * Java class for ItemGroup complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BonusItemGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="bonusType" use="required" type="{}BonusType" />
 *       &lt;attribute name="chance" type="{http://www.w3.org/2001/XMLSchema}float" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BonusItemGroup")
@XmlSeeAlso({ CraftItemGroup.class, CraftRecipeGroup.class, EventGroup.class, ManastoneGroup.class, FoodGroup.class, MedicineGroup.class,
	OreGroup.class, GatherGroup.class, EnchantGroup.class, BossGroup.class })
public abstract class BonusItemGroup implements Chance {

	@XmlAttribute(name = "bonusType", required = true)
	protected BonusType bonusType;

	@XmlAttribute(name = "chance")
	protected float chance = 100f;

	public BonusType getBonusType() {
		return bonusType;
	}

	@Override
	public float getChance() {
		return chance;
	}

	public abstract List<? extends ItemRaceEntry> getItems();

}
