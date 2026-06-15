package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.templates.tribe.Tribe;

/**
 * @author ATracer
 */
@XmlRootElement(name = "tribe_relations")
@XmlAccessorType(XmlAccessType.FIELD)
public class TribeRelationsData {

	@XmlElement(name = "tribe", required = true)
	private List<Tribe> tribeList;

	@XmlTransient
	private final Map<TribeClass, Tribe> tribeNameMap = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (Tribe tribe : tribeList) {
			tribeNameMap.put(tribe.getName(), tribe);
		}
		tribeList = null;
	}

	public int size() {
		return tribeNameMap.size();
	}

	public TribeClass getBaseTribe(TribeClass tribeName) {
		Tribe tribe = tribeNameMap.get(tribeName);
		return tribe.getBase();
	}

	public boolean isAggressiveRelation(TribeClass tribeName1, TribeClass tribeName2) {
		Tribe tribe1 = tribeNameMap.get(tribeName1);
		Tribe tribe2 = tribeNameMap.get(tribeName2);
		if (tribe1 == null || tribe2 == null)
			return false;
		return tribe1.getAggro().contains(tribe2.getBase()) || tribe1.getAggro().contains(tribeName2) || tribe2.getAggro().contains(tribe1.getBase())
			|| tribe2.getAggro().contains(tribeName1);
	}

	/**
	 * @param tribeName1
	 * @param tribeName2
	 * @return
	 */
	public boolean isSupportRelation(TribeClass tribeName1, TribeClass tribeName2) {
		Tribe tribe1 = tribeNameMap.get(tribeName1);
		Tribe tribe2 = tribeNameMap.get(tribeName2);
		if (tribe1 == null || tribe2 == null)
			return false;
		return tribe1.getSupport().contains(tribe2.getBase()) || tribe1.getSupport().contains(tribeName2)
			|| tribe2.getSupport().contains(tribe1.getBase()) || tribe2.getSupport().contains(tribeName1);
	}

	/**
	 * @param tribeName1
	 * @param tribeName2
	 * @return
	 */
	public boolean isFriendlyRelation(TribeClass tribeName1, TribeClass tribeName2) {
		Tribe tribe1 = tribeNameMap.get(tribeName1);
		Tribe tribe2 = tribeNameMap.get(tribeName2);
		if (tribe1 == null || tribe2 == null)
			return false;
		return tribe1.getFriend().contains(tribe2.getBase()) || tribe1.getFriend().contains(tribeName2) || tribe2.getFriend().contains(tribe1.getBase())
			|| tribe2.getFriend().contains(tribeName1);
	}

	/**
	 * @param tribeName1
	 * @param tribeName2
	 * @return
	 */
	public boolean isNeutralRelation(TribeClass tribeName1, TribeClass tribeName2) {
		Tribe tribe1 = tribeNameMap.get(tribeName1);
		Tribe tribe2 = tribeNameMap.get(tribeName2);
		if (tribe1 == null || tribe2 == null)
			return false;
		return tribe1.getNeutral().contains(tribe2.getBase()) || tribe1.getNeutral().contains(tribeName2)
			|| tribe2.getNeutral().contains(tribe1.getBase()) || tribe2.getNeutral().contains(tribeName1);
	}

	/**
	 * @param tribeName1
	 * @param tribeName2
	 * @return
	 */
	public boolean isNoneRelation(TribeClass tribeName1, TribeClass tribeName2) {
		Tribe tribe1 = tribeNameMap.get(tribeName1);
		Tribe tribe2 = tribeNameMap.get(tribeName2);
		if (tribe1 == null || tribe2 == null)
			return false;
		return tribe1.getNone().contains(tribe2.getBase()) || tribe1.getNone().contains(tribeName2) || tribe2.getNone().contains(tribe1.getBase())
			|| tribe2.getNone().contains(tribeName1);
	}

	/**
	 * @param tribeName1
	 * @param tribeName2
	 * @return
	 */
	public boolean isHostileRelation(TribeClass tribeName1, TribeClass tribeName2) {
		Tribe tribe1 = tribeNameMap.get(tribeName1);
		Tribe tribe2 = tribeNameMap.get(tribeName2);
		if (tribe1 == null || tribe2 == null)
			return false;
		return tribe1.getHostile().contains(tribe2.getBase()) || tribe1.getHostile().contains(tribeName2)
			|| tribe2.getHostile().contains(tribe1.getBase()) || tribe2.getHostile().contains(tribeName1);
	}

	/**
	 * @return True, if tribeName can support tribeNameAskingForSupport
	 */
	public boolean canSupport(TribeClass tribeName, TribeClass tribeNameAskingForSupport) {
		Tribe tribe = tribeNameMap.get(tribeName);
		Tribe tribeAskingForSupport = tribeNameMap.get(tribeNameAskingForSupport);
		if (tribe == null || tribeAskingForSupport == null)
			return false;
		return tribe.getSupport().contains(tribeNameAskingForSupport) || tribe.getSupport().contains(tribeAskingForSupport.getBase());
	}
}
