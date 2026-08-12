package com.aionemu.gameserver.model.gameobjects.player;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.dao.PortalCooldownsDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class PortalCooldownList {

	private final Player owner;
	private Map<Integer, PortalCooldown> portalCooldowns;

	PortalCooldownList(Player owner) {
		this.owner = owner;
	}

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

		return coolDown.getEnterCount() >= DataManager.INSTANCE_COOLTIME_DATA.getInstanceMaxCountByWorldId(worldId);
	}

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

	/**
	 * @return The cooldown of the given world, creating it if absent, or null if the world has no entry limit (the client
	 *         displays such instances as unlimited, as long as we don't send any entry info for them).
	 */
	public PortalCooldown getOrCreatePortalCooldown(int worldId) {
		long reuseTime = DataManager.INSTANCE_COOLTIME_DATA.calculateInstanceEntranceCooltime(owner, worldId);
		return reuseTime == 0 ? null : getOrCreatePortalCooldown(worldId, reuseTime);
	}

	private synchronized PortalCooldown getOrCreatePortalCooldown(int worldId, long reuseTime) {
		if (portalCooldowns == null)
			portalCooldowns = new HashMap<>();
		return portalCooldowns.computeIfAbsent(worldId, id -> new PortalCooldown(id, reuseTime, 0));
	}

	public Map<Integer, PortalCooldown> getPortalCoolDowns() {
		return portalCooldowns;
	}

	public void setPortalCoolDowns(Map<Integer, PortalCooldown> portalCoolDowns) {
		this.portalCooldowns = portalCoolDowns;
	}

	public void addPortalCooldown(int worldId, long useDelay) {
		getOrCreatePortalCooldown(worldId, useDelay).increaseEnterCount();

		PortalCooldownsDAO.storePortalCooldowns(owner);

		sendEntryInfo(worldId);
	}

	public void sendEntryInfo(int worldId) {
		if (owner.isInTeam())
			owner.getCurrentTeam().sendPackets(new SM_INSTANCE_INFO((byte) 2, owner, worldId));
		else
			PacketSendUtility.sendPacket(owner, new SM_INSTANCE_INFO((byte) 2, owner, worldId));
	}

	public void removePortalCooldown(int worldId) {
		if (portalCooldowns != null)
			portalCooldowns.remove(worldId);
	}

	public boolean hasCooldowns() {
		return portalCooldowns != null && portalCooldowns.size() > 0;
	}

	public int size() {
		return portalCooldowns != null ? portalCooldowns.size() : 0;
	}

}
