package com.aionemu.gameserver.model.templates.npcshout;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Rolandas
 */

/**
 * <p>
 * Java class for NpcShout complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NpcShout">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="string_id" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="when" use="required" type="{}ShoutEventType" />
 *       &lt;attribute name="pattern" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="param" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" type="{}ShoutType" default="BROADCAST" />
 *       &lt;attribute name="skill_no" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *       &lt;attribute name="poll_delay" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcShout")
public class NpcShout {

	@XmlAttribute(name = "string_id", required = true)
	protected int stringId;

	@XmlAttribute(name = "when", required = true)
	protected ShoutEventType when;

	@XmlAttribute(name = "pattern")
	protected String pattern;

	@XmlAttribute(name = "param")
	protected String param;

	@XmlAttribute(name = "skill_no")
	protected Integer skillNo;

	@XmlAttribute(name = "poll_delay")
	protected Integer pollDelay;

	/**
	 * Gets the value of the stringId property.
	 */
	public int getStringId() {
		return stringId;
	}

	/**
	 * Gets the value of the when property.
	 * 
	 * @return possible object is {@link ShoutEventType }
	 */
	public ShoutEventType getWhen() {
		return when;
	}

	/**
	 * Gets the value of the pattern property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Gets the value of the param property.
	 * 
	 * @return possible object is {@link String }
	 */
	public String getParam() {
		return param;
	}

	/**
	 * Gets the value of the skillNo property.
	 * 
	 * @return possible object is {@link Integer }
	 */
	public int getSkillNo() {
		if (skillNo == null)
			return 0;
		return skillNo;
	}

	public int getPollDelay() {
		if (pollDelay == null)
			return 0;
		return pollDelay;
	}

	public int getShoutRange(Npc npc) {
		return npc.getObjectTemplate().getMinimumShoutRange();
	}

}
