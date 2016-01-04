package com.aionemu.gameserver.model.adapters;

import com.aionemu.gameserver.model.TribeClass;


/**
 * @author Rolandas
 */
public class TribeClassXmlAdapter extends CaseInsensitiveEnumAdapter<TribeClass> {
	public TribeClassXmlAdapter(Class<TribeClass> clazz) {
		super(TribeClass.class);
	}
}
