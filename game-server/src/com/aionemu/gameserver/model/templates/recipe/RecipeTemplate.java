package com.aionemu.gameserver.model.templates.recipe;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.L10n;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecipeTemplate")
public class RecipeTemplate implements L10n {

	@XmlElement(name = "components_data")
	protected List<ComponentsData> componentsData;
	@XmlElement(name = "comboproduct")
	protected List<ComboProduct> comboproduct;
	@XmlAttribute(name = "max_production_count")
	protected Integer maxProductionCount;
	@XmlAttribute(name = "craft_delay_time")
	protected Integer craftDelayTime;
	@XmlAttribute(name = "craft_delay_id")
	protected Integer craftDelayId;
	@XmlAttribute
	protected int quantity;
	@XmlAttribute
	protected int productid;
	@XmlAttribute
	protected int autolearn;
	@XmlAttribute
	protected int dp;
	@XmlAttribute
	protected int skillpoint;
	@XmlAttribute
	protected Race race;
	@XmlAttribute
	protected int skillid;
	@XmlAttribute
	protected int itemid;
	@XmlAttribute
	protected int nameid;
	@XmlAttribute
	protected int id;

	/**
	 * Gets the value of the component property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the component property.
	 * </p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getComponent().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Component}
	 */
	public List<ComponentsData> getComponents() {
		return componentsData == null ? Collections.<ComponentsData> emptyList() : componentsData;
	}

	public Integer getComboProduct(int num) {
		if (comboproduct == null || comboproduct.get(num - 1) == null) {
			return null;
		}
		return comboproduct.get(num - 1).getItemId();
	}

	public Integer getComboProductSize() {
		if (comboproduct == null) {
			return 0;
		}
		return comboproduct.size();
	}

	/**
	 * Gets the value of the quantity property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getQuantity() {
		return quantity;
	}

	/**
	 * Gets the value of the productid property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getProductId() {
		return productid;
	}

	/**
	 * Gets the value of the autolearn property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public int getAutoLearn() {
		return autolearn;
	}

	/**
	 * Gets the value of the dp property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getDp() {
		return dp;
	}

	/**
	 * Gets the value of the skillpoint property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getSkillpoint() {
		return skillpoint;
	}

	/**
	 * Gets the value of the race property.
	 * 
	 * @return possible object is {@link String }
	 */
	public Race getRace() {
		return race;
	}

	/**
	 * Gets the value of the skillid property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getSkillId() {
		return skillid;
	}

	/**
	 * Gets the value of the itemid property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getItemId() {
		return itemid;
	}

	/**
	 * @return the nameid
	 */
	@Override
	public int getL10nId() {
		return nameid;
	}

	/**
	 * Gets the value of the id property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @return Returns the maxProductionCount.
	 */
	public Integer getMaxProductionCount() {
		return maxProductionCount;
	}

	/**
	 * @return Returns the craftDelayTime.
	 */
	public Integer getCraftDelayTime() {
		return craftDelayTime;
	}

	/**
	 * @return Returns the craftDelayId.
	 */
	public Integer getCraftDelayId() {
		return craftDelayId;
	}
}
