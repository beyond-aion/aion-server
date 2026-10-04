package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.cosmeticitems.CosmeticItemTemplate;

/**
 * @author xTz
 */
@XmlRootElement(name = "cosmetic_items")
@XmlAccessorType(XmlAccessType.FIELD)
public class CosmeticItemsData {

	@XmlElement(name = "cosmetic_item", type = CosmeticItemTemplate.class)
	private List<CosmeticItemTemplate> templates;
	private final Map<String, CosmeticItemTemplate> cosmeticItemTemplates = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (CosmeticItemTemplate template : templates) {
			cosmeticItemTemplates.put(template.getCosmeticName(), template);
		}
		templates = null;
	}

	public int size() {
		return cosmeticItemTemplates.size();
	}

	public CosmeticItemTemplate getCosmeticItemsTemplate(String str) {
		return cosmeticItemTemplates.get(str);
	}
}
