package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.BindPointTemplate;

/**
 * @author avol
 */
@XmlRootElement(name = "bind_points")
@XmlAccessorType(XmlAccessType.FIELD)
public class BindPointData {

	@XmlElement(name = "bind_point")
	private List<BindPointTemplate> bplist;

	@XmlTransient
	private final Map<Integer, BindPointTemplate> bindplistData = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (BindPointTemplate bind : bplist) {
			bindplistData.put(bind.getNpcId(), bind);
		}
		bplist = null;
	}

	public int size() {
		return bindplistData.size();
	}

	public BindPointTemplate getBindPointTemplate(int npcId) {
		return bindplistData.get(npcId);
	}
}
