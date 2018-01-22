package com.aionemu.gameserver.dataholders.loadingutils.adapters;

import java.time.LocalDateTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Neon
 */
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

	@Override
	public String marshal(LocalDateTime v) throws Exception {
		return v.toString();
	}

	@Override
	public LocalDateTime unmarshal(String v) throws Exception {
		return LocalDateTime.parse(v);
	}
}
