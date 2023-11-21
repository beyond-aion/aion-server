package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.teleport.TelelocationTemplate;

/**
 * @author orz
 */
@XmlRootElement(name = "teleport_location")
@XmlAccessorType(XmlAccessType.FIELD)
public class TeleLocationData {

	@XmlElement(name = "teleloc_template")
	private List<TelelocationTemplate> tlist;

	@XmlTransient
	private final Map<Integer, TelelocationTemplate> loctlistData = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (TelelocationTemplate loc : tlist) {
			loctlistData.put(loc.getLocId(), loc);
		}
		tlist = null;
	}

	public int size() {
		return loctlistData.size();
	}

	public TelelocationTemplate getTelelocationTemplate(int id) {
		return loctlistData.get(id);
	}
}
