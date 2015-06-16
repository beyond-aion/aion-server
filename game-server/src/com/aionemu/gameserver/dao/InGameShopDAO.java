package com.aionemu.gameserver.dao;

import java.util.List;

import javolution.util.FastMap;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.ingameshop.IGItem;

/**
 * @author xTz, KID
 */
public abstract class InGameShopDAO implements DAO {

	public abstract boolean deleteIngameShopItem(int itemId, byte category, byte subCategory, int list);

	public abstract FastMap<Byte, List<IGItem>> loadInGameShopItems();

	public abstract void saveIngameShopItem(int objectId, int itemId, long itemCount, long itemPrice, byte category, byte subCategory, int list, int salesRanking,
			byte itemType, byte gift, String titleDescription, String description);

	public abstract boolean increaseSales(int object, int current);

	@Override
	public String getClassName() {
		return InGameShopDAO.class.getName();
	}
}
