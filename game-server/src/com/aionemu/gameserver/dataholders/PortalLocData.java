package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.portal.PortalLoc;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "portalLoc" })
@XmlRootElement(name = "portal_locs")
public class PortalLocData {

	@XmlElement(name = "portal_loc")
	protected List<PortalLoc> portalLoc;

	@XmlTransient
	private final Map<Integer, PortalLoc> portalLocs = new HashMap<>();

	void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		for (PortalLoc loc : portalLoc) {
			portalLocs.put(loc.getLocId(), loc);

		}
		portalLoc = null;
	}

	public int size() {
		return portalLocs.size();
	}

	public PortalLoc getPortalLoc(int locId) {
		return portalLocs.get(locId);
	}

}
