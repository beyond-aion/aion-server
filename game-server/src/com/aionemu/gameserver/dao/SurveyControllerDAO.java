package com.aionemu.gameserver.dao;

import java.util.List;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.templates.survey.SurveyItem;

/**
 * @author KID
 */
public abstract class SurveyControllerDAO implements DAO {

	@Override
	public final String getClassName() {
		return SurveyControllerDAO.class.getName();
	}

	public abstract boolean useItem(int id);

	public abstract List<SurveyItem> getAllUnused();
}
