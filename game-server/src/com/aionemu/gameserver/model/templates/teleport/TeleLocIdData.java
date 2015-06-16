package com.aionemu.gameserver.model.templates.teleport;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author ATracer
 */
@XmlRootElement(name = "locations")
@XmlAccessorType(XmlAccessType.FIELD)
public class TeleLocIdData {

	@XmlElement(name = "telelocation")
	private List<TeleportLocation> locids;

	/**
	 * @return Teleport locations
	 */
	public List<TeleportLocation> getTelelocations() {
		return locids;
	}

	public TeleportLocation getTeleportLocation(int value) {
		for (TeleportLocation t : locids) {
			if (t != null && t.getLocId() == value) {
				return t;
			}
		}
		return null;
	}
}
