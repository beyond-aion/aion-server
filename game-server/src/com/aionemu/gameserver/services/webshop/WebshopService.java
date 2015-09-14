package com.aionemu.gameserver.services.webshop;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.configs.main.InGameShopConfig;
import com.aionemu.gameserver.dao.WebshopDAO;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.services.mail.SystemMailService;

/**
 * @author ViAl
 */
public class WebshopService {

	private static final Logger log = LoggerFactory.getLogger(WebshopService.class);
	private static final AtomicBoolean isProcessing = new AtomicBoolean(false);

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final WebshopService instance = new WebshopService();
	}

	public static WebshopService getInstance() {
		return SingletonHolder.instance;
	}

	private WebshopService() {
		if (InGameShopConfig.WEBSHOP_ENABLED) {
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					processRequests();
				}
			}, InGameShopConfig.WEBSHOP_UPDATE_FREQUENCY, InGameShopConfig.WEBSHOP_UPDATE_FREQUENCY);
		}
	}

	private static void processRequests() {
		if (isProcessing.compareAndSet(false, true)) {
			List<WebshopRequest> requests = DAOManager.getDAO(WebshopDAO.class).loadRequests();
			for (WebshopRequest request : requests) {
				try {
					SystemMailService.getInstance().sendMail(request.getBuyerName(), request.getReceiverName(), "In Game Shop", "", request.getItemId(),
						request.getItemCount(), 0, LetterType.BLACKCLOUD);
					DAOManager.getDAO(WebshopDAO.class).updateRequest(request.getRequestId());
				} catch (Exception e) {
					log.error("Error while processing webshop request.", e);
				}
			}
			requests.clear();
			requests = null;
			isProcessing.set(false);
		}
	}
}
