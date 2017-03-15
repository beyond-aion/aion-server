package com.aionemu.gameserver.skillengine.action;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Actions", propOrder = { "actions" })
public class Actions {

	@XmlElements({ @XmlElement(name = "itemuse", type = ItemUseAction.class), @XmlElement(name = "mpuse", type = MpUseAction.class),
		@XmlElement(name = "hpuse", type = HpUseAction.class), @XmlElement(name = "dpuse", type = DpUseAction.class) })
	protected List<Action> actions;

	/**
	 * Gets the value of the actions property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the actions property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getActions().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link ItemUseAction } {@link MpUseAction } {@link HpUseAction } {@link DpUseAction }
	 */
	public List<Action> getActions() {
		if (actions == null) {
			actions = new ArrayList<>();
		}
		return this.actions;
	}

}
