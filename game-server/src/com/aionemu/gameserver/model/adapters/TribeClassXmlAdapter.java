package com.aionemu.gameserver.model.adapters;

import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.utils.xml.CaseInsensitiveEnumAdapter;

/**
 * @author Rolandas
 */
public class TribeClassXmlAdapter extends CaseInsensitiveEnumAdapter<TribeClass> {
	public TribeClassXmlAdapter(Class<TribeClass> clazz) {
		super(TribeClass.class);
	}
}
