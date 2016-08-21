package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.base.BaseLocation;
import com.aionemu.gameserver.model.base.SiegeBaseLocation;
import com.aionemu.gameserver.model.base.StainedBaseLocation;
import com.aionemu.gameserver.model.templates.base.BaseTemplate;

import javolution.util.FastMap;

/**
 * @author Source
 * @modified Estrayl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "base_locations")
public class BaseData {

	@XmlElement(name = "base_location")
	private List<BaseTemplate> baseTemplates;
	@XmlTransient
	private FastMap<Integer, BaseLocation> allBaseLocs = new FastMap<>();
	@XmlTransient
	private FastMap<Integer, BaseLocation> casualBaseLocs = new FastMap<>();
	@XmlTransient
	private FastMap<Integer, SiegeBaseLocation> siegeBaseLocs = new FastMap<>();
	@XmlTransient
	private FastMap<Integer, StainedBaseLocation> stainedBaseLocs = new FastMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (BaseTemplate template : baseTemplates) {
			switch (template.getType()) {
				case CASUAL:
					BaseLocation b = new BaseLocation(template);
					casualBaseLocs.put(template.getId(), b);
					allBaseLocs.put(template.getId(), b);
					break;
				case SIEGE:
					SiegeBaseLocation s = new SiegeBaseLocation(template);
					siegeBaseLocs.put(template.getId(), s);
					allBaseLocs.put(template.getId(), s);
					break;
				case STAINED:
					StainedBaseLocation st = new StainedBaseLocation(template);
					stainedBaseLocs.put(template.getId(), st);
					allBaseLocs.put(template.getId(), st);
					break;
			}
			allBaseLocs.put(template.getId(), new BaseLocation(template));
		}
	}

	public int size() {
		return allBaseLocs.size();
	}

	public FastMap<Integer, BaseLocation> getAllBaseLocations() {
		return allBaseLocs;
	}

	public FastMap<Integer, BaseLocation> getCasualBaseLocations() {
		return casualBaseLocs;
	}

	public FastMap<Integer, SiegeBaseLocation> getSiegeBaseLocations() {
		return siegeBaseLocs;
	}

	public FastMap<Integer, StainedBaseLocation> getStainedBaseLocations() {
		return stainedBaseLocs;
	}
}