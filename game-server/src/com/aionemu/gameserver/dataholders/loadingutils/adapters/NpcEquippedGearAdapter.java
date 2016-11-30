package com.aionemu.gameserver.dataholders.loadingutils.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.aionemu.gameserver.model.items.NpcEquippedGear;

/**
 * @author Luno
 */
public class NpcEquippedGearAdapter extends XmlAdapter<NpcEquipmentList, NpcEquippedGear> {

	@Override
	public NpcEquipmentList marshal(NpcEquippedGear v) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NpcEquippedGear unmarshal(NpcEquipmentList v) throws Exception {
		return new NpcEquippedGear(v);
	}

}
