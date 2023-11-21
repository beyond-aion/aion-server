package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.flypath.FlyPathEntry;

/**
 * @author KID
 */
@XmlRootElement(name = "flypath_template")
@XmlAccessorType(XmlAccessType.FIELD)
public class FlyPathData {

	@XmlElement(name = "flypath_location")
	private List<FlyPathEntry> list;

	@XmlTransient
	private final Map<Integer, FlyPathEntry> loctlistData = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (FlyPathEntry loc : list) {
			loctlistData.put(loc.getId(), loc);
		}
		list = null;
	}

	public int size() {
		return loctlistData.size();
	}

	public FlyPathEntry getPathTemplate(int id) {
		return loctlistData.get(id);
	}
}
