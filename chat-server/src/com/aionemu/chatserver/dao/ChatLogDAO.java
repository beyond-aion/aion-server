package com.aionemu.chatserver.dao;

import com.aionemu.commons.database.dao.DAO;

public abstract class ChatLogDAO implements DAO
{
	public abstract void add_ChannelChat(String sender, String message, String receiver, String type);
	
	@Override
	public final String getClassName()
	{
		return ChatLogDAO.class.getName();
	}
}
