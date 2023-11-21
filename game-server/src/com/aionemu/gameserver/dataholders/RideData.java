package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.ride.RideInfo;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "rides" })
@XmlRootElement(name = "rides")
public class RideData {

	@XmlElement(name = "ride_info")
	private List<RideInfo> rides;

	@XmlTransient
	private final Map<Integer, RideInfo> rideInfos = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (RideInfo info : rides) {
			rideInfos.put(info.getNpcId(), info);
		}
		rides = null;
	}

	public RideInfo getRideInfo(int npcId) {
		return rideInfos.get(npcId);
	}

	public int size() {
		return rideInfos.size();
	}

}
