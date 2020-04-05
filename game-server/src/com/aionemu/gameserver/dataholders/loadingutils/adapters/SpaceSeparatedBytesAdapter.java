package com.aionemu.gameserver.dataholders.loadingutils.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB supports space separated int lists. This adapter is designed to work in the same way, but for bytes instead.
 * 
 * @author Neon
 */
public class SpaceSeparatedBytesAdapter extends XmlAdapter<String, byte[]> {

	@Override
	public String marshal(byte[] v) {
		StringBuilder sb = new StringBuilder(v.length * 3);
		for (int i = 0; i < v.length; i++) {
			if (i > 0)
				sb.append(' ');
			sb.append(v[i]);
		}
		return sb.toString();
	}

	@Override
	public byte[] unmarshal(String v) {
		String[] values = v.split(" ");
		byte[] bytes = new byte[values.length];
		for (int i = 0; i < values.length; i++) {
			bytes[i] = Byte.parseByte(values[i]);
		}
		return bytes;
	}
}
