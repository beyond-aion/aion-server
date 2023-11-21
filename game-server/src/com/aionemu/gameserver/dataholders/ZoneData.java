package com.aionemu.gameserver.dataholders;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.geometry.*;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;
import com.aionemu.gameserver.utils.xml.XmlUtil;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "zones")
public class ZoneData {

	private static final Logger log = LoggerFactory.getLogger(ZoneData.class);

	@XmlElement(name = "zone")
	public List<ZoneTemplate> zoneList;

	@XmlTransient
	private final Map<Integer, List<ZoneInfo>> zoneNameMap = new HashMap<>();

	@XmlTransient
	private final Map<ZoneTemplate, Integer> weatherZoneIds = new HashMap<>();

	@XmlTransient
	private int count;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		int lastMapId = 0;
		int weatherZoneId = 1;
		for (ZoneTemplate zone : zoneList) {
			Area area = null;
			switch (zone.getAreaType()) {
				case POLYGON:
					area = new PolyArea(zone.getName(), zone.getMapid(), zone.getPoints().getPoint(), zone.getPoints().getBottom(), zone.getPoints().getTop());
					break;
				case CYLINDER:
					area = new CylinderArea(zone.getName(), zone.getMapid(), zone.getCylinder().getX(), zone.getCylinder().getY(), zone.getCylinder().getR(),
						zone.getCylinder().getBottom(), zone.getCylinder().getTop());
					break;
				case SPHERE:
					if (zone.getSphere().getR() <= 0)
						break;
					area = new SphereArea(zone.getName(), zone.getMapid(), zone.getSphere().getX(), zone.getSphere().getY(), zone.getSphere().getZ(), zone
						.getSphere().getR());
					break;
				case SEMISPHERE:
					area = new SemisphereArea(zone.getName(), zone.getMapid(), zone.getSemisphere().getX(), zone.getSemisphere().getY(), zone.getSemisphere()
						.getZ(), zone.getSemisphere().getR());
			}
			if (area != null) {
				List<ZoneInfo> zones = zoneNameMap.get(zone.getMapid());
				if (zones == null) {
					zones = new ArrayList<>();
					zoneNameMap.put(zone.getMapid(), zones);
				}
				if (zone.getZoneType() == ZoneClassName.WEATHER) {
					if (lastMapId != zone.getMapid()) {
						lastMapId = zone.getMapid();
						weatherZoneId = 1;
					}
					weatherZoneIds.put(zone, weatherZoneId++);
				}
				zones.add(new ZoneInfo(area, zone));
				count++;
			}
		}
		zoneList = null;
	}

	public Map<Integer, List<ZoneInfo>> getZones() {
		return zoneNameMap;
	}

	public int size() {
		return count;
	}

	/**
	 * Weather zone ID it's an order number (starts from 1)
	 */
	public int getWeatherZoneId(ZoneTemplate template) {
		Integer id = weatherZoneIds.get(template);
		if (id == null)
			return 0;
		return id;
	}

	public void saveData() {
		File xml = new File("./data/static_data/zones/generated_zones.xml");
		try {
			JAXBContext jc = JAXBContext.newInstance(ZoneData.class);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setSchema(XmlUtil.getSchema("./data/static_data/zones/zones.xsd"));
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this, xml);
		} catch (JAXBException e) {
			log.error("Error while saving data: " + e.getMessage(), e.getCause());
			return;
		}
	}
}
