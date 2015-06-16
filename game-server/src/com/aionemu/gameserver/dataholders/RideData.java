package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

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
	private TIntObjectHashMap<RideInfo> rideInfos;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		rideInfos = new TIntObjectHashMap<RideInfo>();

		for (RideInfo info : rides) {
			rideInfos.put(info.getNpcId(), info);
		}
		rides.clear();
		rides = null;
	}

	public RideInfo getRideInfo(int npcId) {
		return rideInfos.get(npcId);
	}

	public int size() {
		return rideInfos.size();
	}

}
