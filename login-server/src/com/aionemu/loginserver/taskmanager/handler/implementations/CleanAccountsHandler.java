package com.aionemu.loginserver.taskmanager.handler.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.taskmanager.handler.TaskFromDBHandler;

/**
 *
 * @author nrg
 */
public class CleanAccountsHandler extends TaskFromDBHandler {
	private static Logger log = LoggerFactory.getLogger(CleanAccountsHandler.class);
	private int daysOfInactivity;

	@Override
	public boolean isValid() {
		if (params.length != 1) {
			log.warn("CleanAccountHandler has not exactly one parameter (daysOfInactivity) - handler is not registered");
			return false;
		}
		return true;
	}

	@Override
	public void trigger() {
		daysOfInactivity = Integer.parseInt(params[0]);
		log.info("Deleting all accounts, older as " + daysOfInactivity + " days");
		DAOManager.getDAO(AccountDAO.class).deleteInactiveAccounts(daysOfInactivity);
	}
}
