package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.housing.*;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "housingObjects" })
@XmlRootElement(name = "housing_objects")
public class HousingObjectData {

	@XmlElements({ @XmlElement(name = "postbox", type = HousingPostbox.class), @XmlElement(name = "use_item", type = HousingUseableItem.class),
		@XmlElement(name = "move_item", type = HousingMoveableItem.class), @XmlElement(name = "chair", type = HousingChair.class),
		@XmlElement(name = "picture", type = HousingPicture.class), @XmlElement(name = "passive", type = HousingPassiveItem.class),
		@XmlElement(name = "npc", type = HousingNpc.class), @XmlElement(name = "storage", type = HousingStorage.class),
		@XmlElement(name = "jukebox", type = HousingJukeBox.class), @XmlElement(name = "moviejukebox", type = HousingMovieJukeBox.class),
		@XmlElement(name = "emblem", type = HousingEmblem.class) })
	protected List<PlaceableHouseObject> housingObjects;

	@XmlTransient
	private final Map<Integer, PlaceableHouseObject> objectTemplatesById = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (PlaceableHouseObject obj : housingObjects) {
			objectTemplatesById.put(obj.getTemplateId(), obj);
		}

		housingObjects = null;
	}

	public int size() {
		return objectTemplatesById.size();
	}

	public PlaceableHouseObject getTemplateById(int templateId) {
		return objectTemplatesById.get(templateId);
	}

}
