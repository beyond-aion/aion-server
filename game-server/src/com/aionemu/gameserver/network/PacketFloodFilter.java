package com.aionemu.gameserver.network;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.PropertiesUtils;
import com.aionemu.gameserver.configs.main.SecurityConfig;

/**
 * @author KID
 */
public class PacketFloodFilter {

	private static PacketFloodFilter pff = new PacketFloodFilter();
	
	private final Logger log = LoggerFactory.getLogger(PacketFloodFilter.class);
	
	public static PacketFloodFilter getInstance() {
		return pff;
	}
	
	private int[] packets;
	private short maxClientRequest = 0x2ff;

	public PacketFloodFilter() {
		if(SecurityConfig.PFF_ENABLE) {
			int cnt = 0;
			packets = new int[maxClientRequest];
			try {
				java.util.Properties props = PropertiesUtils.load("config/administration/pff.properties");
				for(Object key : props.keySet()){
					String str = (String) key;
					packets[Integer.decode(str)] = Integer.valueOf(props.getProperty(str).trim());
					cnt++;
				}
			} catch (IOException e) {
				log.error("Can't read pff.properties", e);
			}
			log.info("PacketFloodFilter initialized with "+cnt+" packets.");
		} else
			log.info("PacketFloodFilter disabled.");
	}
	
	public final int[] getPackets() {
		return this.packets;
	}
}
