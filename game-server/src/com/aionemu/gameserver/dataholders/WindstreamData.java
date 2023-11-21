package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.windstreams.WindstreamTemplate;

/**
 * @author LokiReborn
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "windstreams")
public class WindstreamData {

	@XmlElement(name = "windstream")
	private List<WindstreamTemplate> wts;

	@XmlTransient
	private final Map<Integer, WindstreamTemplate> windstreams = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (WindstreamTemplate wt : wts) {
			windstreams.put(wt.getMapId(), wt);
		}
		wts = null;
	}

	public WindstreamTemplate getStreamTemplate(int mapId) {
		return windstreams.get(mapId);
	}

	public int size() {
		return windstreams.size();
	}
}
