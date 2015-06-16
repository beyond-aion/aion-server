package com.aionemu.gameserver.model.templates.staticdoor;

import java.util.EnumSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.geoEngine.bounding.BoundingBox;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;

/**
 * @author Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StaticDoor")
public class StaticDoorTemplate extends VisibleObjectTemplate {

	@XmlAttribute
	protected DoorType type = DoorType.DOOR;
	@XmlAttribute
	protected Float x;
	@XmlAttribute
	protected Float y;
	@XmlAttribute
	protected Float z;
	@XmlAttribute(name = "doorid")
	protected int doorId;
	@XmlAttribute(name = "keyid")
	protected int keyId;
	@XmlAttribute(name = "state")
	protected String statesHex;
	@XmlAttribute(name = "mesh")
	private String meshFile;
	@XmlElement(name = "box")
	private StaticDoorBounds box;

	@XmlTransient
	EnumSet<StaticDoorState> states = EnumSet.noneOf(StaticDoorState.class);

	public Float getX() {
		return x;
	}

	public Float getY() {
		return y;
	}

	public Float getZ() {
		return z;
	}

	/**
	 * @return the doorId
	 */
	public int getDoorId() {
		return doorId;
	}

	/**
	 * @return the keyItem
	 */
	public int getKeyId() {
		return keyId;
	}

	@Override
	public int getTemplateId() {
		return 300001;
	}

	@Override
	public String getName() {
		return "door";
	}

	@Override
	public int getNameId() {
		return 0;
	}

	public EnumSet<StaticDoorState> getInitialStates() {
		if (statesHex != null) {
			int radix = 16;
			if (statesHex.startsWith("0x")) {
				statesHex = statesHex.replace("0x", "");
			}
			else
				radix = 10;
			try {
				StaticDoorState.setStates(Integer.parseInt(statesHex, radix), states);
			}
			catch (NumberFormatException ex) {
			}
			finally {
				statesHex = null;
			}
		}
		return states;
	}

	public String getMeshFile() {
		return meshFile;
	}

	public BoundingBox getBoundingBox() {
		if (box == null)
			return null;
		return box.getBoundingBox();
	}

	public DoorType getDoorType() {
		return type;
	}

}
