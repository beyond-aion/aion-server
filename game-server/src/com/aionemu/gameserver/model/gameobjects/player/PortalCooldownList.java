package com.aionemu.gameserver.model.gameobjects.player;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.PortalCooldownsDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class PortalCooldownList {

	private Player owner;
	private Map<Integer, PortalCooldown> portalCooldowns;

	/**
	 * @param owner
	 */
	PortalCooldownList(Player owner) {
		this.owner = owner;
	}

	/**
	 * @param worldId
	 ** @return
	 */
	public boolean isPortalUseDisabled(int worldId) {
		if (portalCooldowns == null || !portalCooldowns.containsKey(worldId))
			return false;

		PortalCooldown coolDown = portalCooldowns.get(worldId);
		if (coolDown == null)
			return false;

		if (coolDown.getReuseTime() < System.currentTimeMillis()) {
			portalCooldowns.remove(worldId);
			return false;
		}

		if (coolDown.getEnterCount() < DataManager.INSTANCE_COOLTIME_DATA.getInstanceMaxCountByWorldId(worldId)) {
			return false;
		}

		return true;
	}

	/**
	 * @param worldId
	 * @return
	 */
	public long getPortalCooldownTime(int worldId) {
		if (portalCooldowns == null || !portalCooldowns.containsKey(worldId))
			return 0;
		long coolDown = portalCooldowns.get(worldId).getReuseTime();

		if (coolDown < System.currentTimeMillis()) {
			portalCooldowns.remove(worldId);
			return 0;
		}

		return coolDown;
	}

	public PortalCooldown getPortalCooldown(int worldId) {
		return portalCooldowns == null ? null : portalCooldowns.get(worldId);
	}

	public Map<Integer, PortalCooldown> getPortalCoolDowns() {
		return portalCooldowns;
	}

	public void setPortalCoolDowns(Map<Integer, PortalCooldown> portalCoolDowns) {
		this.portalCooldowns = portalCoolDowns;
	}

	/**
	 * @param worldId
	 * @param time
	 */
	public void addPortalCooldown(int worldId, long useDelay) {
		if (portalCooldowns == null)
			portalCooldowns = new HashMap<>();

		PortalCooldown portalCooldown = portalCooldowns.get(worldId);
		if (portalCooldown == null)
			portalCooldown = new PortalCooldown(worldId, useDelay, 0);

		portalCooldown.increaseEnterCount();
		portalCooldowns.put(worldId, portalCooldown);

		DAOManager.getDAO(PortalCooldownsDAO.class).storePortalCooldowns(owner);

		if (owner.isInTeam())
			owner.getCurrentTeam().sendPacket(new SM_INSTANCE_INFO((byte) 2, owner, worldId));
		else
			PacketSendUtility.sendPacket(owner, new SM_INSTANCE_INFO((byte) 2, owner, worldId));
	}

	/**
	 * @param worldId
	 */
	public void removePortalCooldown(int worldId) {
		if (portalCooldowns != null)
			portalCooldowns.remove(worldId);
	}

	/**
	 * @return
	 */
	public boolean hasCooldowns() {
		return portalCooldowns != null && portalCooldowns.size() > 0;
	}

	/**
	 * @return
	 */
	public int size() {
		return portalCooldowns != null ? portalCooldowns.size() : 0;
	}

}
