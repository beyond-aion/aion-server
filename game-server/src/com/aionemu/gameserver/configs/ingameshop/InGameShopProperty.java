package com.aionemu.gameserver.configs.ingameshop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.ingameshop.IGCategory;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "in_game_shop")
public class InGameShopProperty {

	@XmlElement(name = "category", required = true)
	private List<IGCategory> categories;

	public List<IGCategory> getCategories() {
		if (categories == null) {
			categories = new ArrayList<>();
		}
		return this.categories;
	}

	public int size() {
		return getCategories().size();
	}

	public void clear() {
		if (categories != null) {
			categories.clear();
		}
	}

	public static InGameShopProperty load() {
		return JAXBUtil.deserialize(new File("./config/ingameshop/in_game_shop.xml"), InGameShopProperty.class);
	}

}
