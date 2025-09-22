package com.aionemu.gameserver.model.templates.npcshout;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShoutList", propOrder = { "npcShouts" })
public class ShoutList {

	@XmlElement(name = "shout", required = true)
	protected List<NpcShout> npcShouts;

	@XmlAttribute(name = "npc_ids", required = true)
	protected List<Integer> npcIds;

	@XmlAttribute(name = "restrict_world")
	protected Integer restrictWorld;

	/**
	 * Gets the value of the npcShouts property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the npcShouts property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getNpcShouts().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link NpcShout }
	 */
	public List<NpcShout> getNpcShouts() {
		if (npcShouts == null) {
			npcShouts = new ArrayList<>();
		}
		return this.npcShouts;
	}

	/**
	 * Gets the value of the npcIds property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the npcIds property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getNpcIds().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Integer }
	 */
	public List<Integer> getNpcIds() {
		if (npcIds == null) {
			npcIds = new ArrayList<>();
		}
		return this.npcIds;
	}

	/**
	 * Gets the value of the restrictWorld property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public int getRestrictWorld() {
		if (restrictWorld == null)
			return 0;
		return restrictWorld;
	}

	public void makeNull() {
		this.npcIds = null;
		this.npcShouts = null;
		this.restrictWorld = null;
	}

}
