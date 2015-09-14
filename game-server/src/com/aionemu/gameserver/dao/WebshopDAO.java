package com.aionemu.gameserver.dao;

import java.util.List;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.services.webshop.WebshopRequest;

/**
 * @author ViAl
 */
public abstract class WebshopDAO implements DAO {

	public abstract List<WebshopRequest> loadRequests();

	public abstract void updateRequest(int requestId);

	@Override
	public String getClassName() {
		return WebshopDAO.class.getName();
	}
}
