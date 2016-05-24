package com.aionemu.gameserver.services.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.InGameShopConfig;
import com.aionemu.gameserver.dao.WebDAO;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Created on 24.05.2016
 * 
 * @author Estrayl
 */
public class WebService {

	private Logger log = LoggerFactory.getLogger("INGAMESHOP_LOG");
	private Object lock = new Object();

	private WebService() {
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> deliverItems(), 300000, InGameShopConfig.WEBREQUEST_UPDATE_FREQUENCY * 1000);
	}

	public static WebService getInstance() {
		return SingletonHolder.instance;
	}

	private void deliverItems() {
		synchronized (lock) {
			List<WebRequest> requests = DAOManager.getDAO(WebDAO.class).loadRequests();
			for (WebRequest request : requests) {
				if (request != null) {
					SystemMailService.getInstance().sendMail("Beyond Aion Team", request.getReceiverName(), "Webshop Rewards", "", request.getItemId(),
						request.getItemCount(), 0, LetterType.BLACKCLOUD);
					DAOManager.getDAO(WebDAO.class).deleteRequest(request.getRequestId());
					log.info("Item: " + request.getItemId() + " added to player " + request.getReceiverName() + " (count: " + request.getItemCount() + ")");
				}
			}
		}
	}

	private static class SingletonHolder {

		protected static final WebService instance = new WebService();
	}
}
