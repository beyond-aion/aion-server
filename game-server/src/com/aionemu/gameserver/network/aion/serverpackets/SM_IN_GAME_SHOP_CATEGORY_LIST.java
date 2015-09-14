package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.configs.ingameshop.InGameShopProperty;
import com.aionemu.gameserver.model.ingameshop.InGameShopEn;
import com.aionemu.gameserver.model.templates.ingameshop.IGCategory;
import com.aionemu.gameserver.model.templates.ingameshop.IGSubCategory;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_IN_GAME_SHOP_CATEGORY_LIST extends AionServerPacket {

	private int type;
	private int categoryId;
	private InGameShopProperty ing;

	public SM_IN_GAME_SHOP_CATEGORY_LIST(int type, int category) {
		this.type = type;
		this.categoryId = category;
		ing = InGameShopEn.getInstance().getIGSProperty();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(type);
		switch (type) {
			case 0:
				writeH(ing.size()); // size
				for (IGCategory category : ing.getCategories()) {
					writeD(category.getId()); // categry Id
					writeS(category.getName()); // category Name
				}
				break;
			case 2:
				if (categoryId < ing.size()) {
					IGCategory iGCategory = ing.getCategories().get(categoryId);
					writeH(iGCategory.getSubCategories().size()); // size
					for (IGSubCategory subCategory : iGCategory.getSubCategories()) {
						writeD(subCategory.getId()); // sub category Id
						writeS(subCategory.getName()); // sub category Name
					}
				}
				break;
		}
	}

}
