package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.walker.RouteParent;
import com.aionemu.gameserver.model.templates.walker.RouteVersion;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "routeGroups" })
@XmlRootElement(name = "walker_versions")
public class WalkerVersionsData {

	@XmlElement(name = "walk_parent")
	private List<RouteParent> routeGroups;

	@XmlTransient
	private Map<String, String> walkParents = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (RouteParent group : routeGroups) {
			for (RouteVersion version : group.getRouteVersion())
				walkParents.put(version.getId(), group.getId());
		}
		routeGroups.clear();
		routeGroups = null;
	}

	public boolean isRouteVersioned(String routeId) {
		if (routeId == null)
			return false;
		return walkParents.containsKey(routeId);
	}

	public String getRouteVersionId(String routeId) {
		if (routeId == null)
			return null;
		return walkParents.get(routeId);
	}

	public int size() {
		return walkParents.size();
	}
}
