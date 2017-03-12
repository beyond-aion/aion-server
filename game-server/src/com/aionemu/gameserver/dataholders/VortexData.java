package com.aionemu.gameserver.dataholders;

import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.vortex.VortexTemplate;
import com.aionemu.gameserver.model.vortex.VortexLocation;

/**
 * @author Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dimensional_vortex")
public class VortexData {

	@XmlElement(name = "vortex_location")
	private List<VortexTemplate> vortexTemplates;
	@XmlTransient
	private LinkedHashMap<Integer, VortexLocation> vortex = new LinkedHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (VortexTemplate template : vortexTemplates) {
			vortex.put(template.getId(), new VortexLocation(template));
		}
	}

	public int size() {
		return vortex.size();
	}

	public VortexLocation getVortexLocation(int invasionWorldId) {
		for (VortexLocation loc : vortex.values()) {
			if (loc.getInvasionWorldId() == invasionWorldId) {
				return loc;
			}
		}
		return null;
	}

	public LinkedHashMap<Integer, VortexLocation> getVortexLocations() {
		return vortex;
	}

}
