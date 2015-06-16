package com.aionemu.gameserver.dataholders.loadingutils.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.aionemu.gameserver.model.items.NpcEquippedGear;

/**
 * @author Luno
 */
public class NpcEquippedGearAdapter extends XmlAdapter<NpcEquipmentList, NpcEquippedGear> {

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public NpcEquipmentList marshal(NpcEquippedGear v) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public NpcEquippedGear unmarshal(NpcEquipmentList v) throws Exception {
		return new NpcEquippedGear(v);
	}

}
