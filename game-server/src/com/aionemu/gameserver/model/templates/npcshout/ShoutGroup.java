package com.aionemu.gameserver.model.templates.npcshout;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */

/**
 * <p>
 * Java class for ShoutGroup complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ShoutGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="shout_npcs" type="{}ShoutList" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="client_ai" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShoutGroup", propOrder = { "shoutNpcs" })
public class ShoutGroup {

	@XmlElement(name = "shout_npcs", required = true)
	protected List<ShoutList> shoutNpcs;

	@XmlAttribute(name = "client_ai")
	protected String clientAi;

	/**
	 * Gets the value of the shoutNpcs property.
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the shoutNpcs property.
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getShoutNpcs().add(newItem);
	 * </pre>
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link ShoutList }
	 */
	public List<ShoutList> getShoutNpcs() {
		if (shoutNpcs == null) {
			shoutNpcs = new ArrayList<>();
		}
		return this.shoutNpcs;
	}

	/**
	 * Gets the value of the clientAi property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getClientAi() {
		return clientAi;
	}

	public void makeNull() {
		this.shoutNpcs = null;
		this.clientAi = null;
	}

}
