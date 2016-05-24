package com.aionemu.gameserver.dao;

import java.util.List;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.services.web.WebRequest;

/**
 * Created on 24.05.2016
 * 
 * @author Estrayl
 */
public abstract class WebDAO implements DAO {
	
	@Override
	public final String getClassName() {
		return WebDAO.class.getName();
	}

	public abstract List<WebRequest> loadRequests();

	public abstract void deleteRequest(int requestId);

}
