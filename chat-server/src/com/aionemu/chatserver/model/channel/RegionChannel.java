package com.aionemu.chatserver.model.channel;

import com.aionemu.chatserver.model.ChannelType;
import com.aionemu.chatserver.model.Race;

/**
 * @author ATracer
 */
public class RegionChannel extends RaceChannel
{
	protected int mapId;

	/**
	 * @param channelId
	 * @param mapId
	 * @param race
	 */
	public RegionChannel(int mapId, Race race, String identifier)
	{
		super(ChannelType.PUBLIC, race, identifier);
		this.mapId = mapId;
	}

	/**
	 * @return the mapId
	 */
	public int getMapId()
	{
		return mapId;
	}
}
