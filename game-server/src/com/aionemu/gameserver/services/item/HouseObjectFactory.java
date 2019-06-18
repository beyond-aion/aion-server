package com.aionemu.gameserver.services.item;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.ChairObject;
import com.aionemu.gameserver.model.gameobjects.EmblemObject;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.JukeBoxObject;
import com.aionemu.gameserver.model.gameobjects.MoveableObject;
import com.aionemu.gameserver.model.gameobjects.NpcObject;
import com.aionemu.gameserver.model.gameobjects.PassiveObject;
import com.aionemu.gameserver.model.gameobjects.PictureObject;
import com.aionemu.gameserver.model.gameobjects.PostboxObject;
import com.aionemu.gameserver.model.gameobjects.StorageObject;
import com.aionemu.gameserver.model.gameobjects.UseableItemObject;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HouseRegistry;
import com.aionemu.gameserver.model.templates.housing.HousingChair;
import com.aionemu.gameserver.model.templates.housing.HousingEmblem;
import com.aionemu.gameserver.model.templates.housing.HousingJukeBox;
import com.aionemu.gameserver.model.templates.housing.HousingMoveableItem;
import com.aionemu.gameserver.model.templates.housing.HousingNpc;
import com.aionemu.gameserver.model.templates.housing.HousingPicture;
import com.aionemu.gameserver.model.templates.housing.HousingPostbox;
import com.aionemu.gameserver.model.templates.housing.HousingStorage;
import com.aionemu.gameserver.model.templates.housing.HousingUseableItem;
import com.aionemu.gameserver.model.templates.housing.PlaceableHouseObject;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.SummonHouseObjectAction;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author Rolandas
 */
public final class HouseObjectFactory {

	/**
	 * For loading data from DB
	 */
	public static HouseObject<?> createNew(HouseRegistry registry, int objectId, int objectTemplateId) {
		PlaceableHouseObject template = DataManager.HOUSING_OBJECT_DATA.getTemplateById(objectTemplateId);
		if (template instanceof HousingChair)
			return new ChairObject(registry, objectId, template.getTemplateId());
		else if (template instanceof HousingJukeBox)
			return new JukeBoxObject(registry, objectId, template.getTemplateId());
		else if (template instanceof HousingMoveableItem)
			return new MoveableObject(registry, objectId, template.getTemplateId());
		else if (template instanceof HousingNpc)
			return new NpcObject(registry, objectId, template.getTemplateId());
		else if (template instanceof HousingPicture)
			return new PictureObject(registry, objectId, template.getTemplateId());
		else if (template instanceof HousingPostbox)
			return new PostboxObject(registry, objectId, template.getTemplateId());
		else if (template instanceof HousingStorage)
			return new StorageObject(registry, objectId, template.getTemplateId());
		else if (template instanceof HousingUseableItem)
			return new UseableItemObject(registry, objectId, template.getTemplateId());
		else if (template instanceof HousingEmblem)
			return new EmblemObject(registry, objectId, template.getTemplateId());
		return new PassiveObject(registry, objectId, template.getTemplateId());
	}

	/**
	 * For transferring item from inventory to house registry
	 */
	public static HouseObject<?> createNew(House house, ItemTemplate itemTemplate) {
		Objects.requireNonNull(itemTemplate.getActions(), "template actions null");

		SummonHouseObjectAction action = itemTemplate.getActions().getHouseObjectAction();
		Objects.requireNonNull(action, "template actions miss SummonHouseObjectAction");

		int objectTemplateId = action.getTemplateId();
		HouseObject<?> obj = createNew(house.getRegistry(), IDFactory.getInstance().nextId(), objectTemplateId);
		int useDays = obj.getObjectTemplate().getUseDays();
		if (useDays > 0) {
			int expireEnd = (int) (System.currentTimeMillis() / 1000 + TimeUnit.DAYS.toSeconds(useDays));
			obj.setExpireTime(expireEnd);
		}
		return obj;
	}
}
