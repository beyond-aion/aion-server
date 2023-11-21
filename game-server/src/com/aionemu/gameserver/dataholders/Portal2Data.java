package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.portal.PortalDialog;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.model.templates.portal.PortalScroll;
import com.aionemu.gameserver.model.templates.portal.PortalUse;
import com.aionemu.gameserver.services.teleport.PortalService;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "portalUse", "portalDialog", "portalScroll" })
@XmlRootElement(name = "portal_templates2")
public class Portal2Data {

	@XmlElement(name = "portal_use")
	protected List<PortalUse> portalUse;
	@XmlElement(name = "portal_dialog")
	protected List<PortalDialog> portalDialog;
	@XmlElement(name = "portal_scroll")
	protected List<PortalScroll> portalScroll;

	@XmlTransient
	private final Map<Integer, PortalUse> portalUses = new HashMap<>();
	@XmlTransient
	private final Map<Integer, PortalDialog> portalDialogs = new HashMap<>();
	@XmlTransient
	private final Map<String, PortalScroll> portalScrolls = new HashMap<>();

	void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		if (portalUse != null) {
			for (PortalUse portal : portalUse) {
				portalUses.put(portal.getNpcId(), portal);
			}
			portalUse = null;
		}
		if (portalDialog != null) {
			for (PortalDialog portal : portalDialog) {
				portalDialogs.put(portal.getNpcId(), portal);
			}
			portalDialog = null;
		}
		if (portalScroll != null) {
			for (PortalScroll portal : portalScroll) {
				portalScrolls.put(portal.getName(), portal);
			}
			portalScroll = null;
		}
	}

	public int size() {
		return portalScrolls.size() + portalDialogs.size() + portalUses.size();
	}

	/**
	 * Tries to find the portal for the players race, but can return the path of the opposite race if there is no matching one.<br>
	 * With this you're able to send an invalid race error to the player (see {@link PortalService#port(PortalPath, Player, int)}).
	 * 
	 * @return PortalPath for the specified dialog action ID.
	 */
	public PortalPath getPortalDialogPath(int npcId, int dialogActionId, Player player) {
		PortalDialog portal = portalDialogs.get(npcId);
		if (portal != null) {
			PortalPath matchingPortalPath = null;
			for (PortalPath path : portal.getPortalPaths()) {
				if (path.getDialog() == dialogActionId) {
					if (path.getRace() == player.getRace() || path.getRace() == Race.PC_ALL)
						return path;
					matchingPortalPath = path;
				}
			}
			return matchingPortalPath; // return any matched path to send invalid race error afterwards
		}
		return null;
	}

	/**
	 * Tries to find the portal for the players race, but can return the path of the opposite race if there is no matching one.<br>
	 * With this you're able to send an invalid race error to the player (see {@link PortalService#port(PortalPath, Player, int)}).
	 * 
	 * @return PortalPath for the specified dialog action ID.
	 */
	public PortalPath getPortalUsePath(int npcId, Player player) {
		PortalUse portal = portalUses.get(npcId);
		if (portal != null) {
			PortalPath matchingPortalPath = null;
			for (PortalPath path : portal.getPortalPaths()) {
				if (player.getRace() == path.getRace() || path.getRace() == Race.PC_ALL)
					return path;
				matchingPortalPath = path;
			}
			return matchingPortalPath; // return any matched path to send invalid race error afterwards
		}
		return null;
	}

	public boolean isPortalNpc(int npcId) {
		return portalUses.get(npcId) != null || portalDialogs.get(npcId) != null;
	}

	public PortalScroll getPortalScroll(String name) {
		return portalScrolls.get(name);
	}

	public int getTeleportDialogId(int npcId) {
		PortalDialog portal = portalDialogs.get(npcId);
		return portal == null ? 1011 : portal.getTeleportDialogId();
	}
}
