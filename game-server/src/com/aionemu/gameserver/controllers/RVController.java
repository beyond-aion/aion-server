package com.aionemu.gameserver.controllers;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.RiftService;
import com.aionemu.gameserver.services.VortexService;
import com.aionemu.gameserver.services.rift.RiftEnum;
import com.aionemu.gameserver.services.rift.RiftInformer;
import com.aionemu.gameserver.services.rift.RiftManager;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, Source, Sykra
 */
public class RVController extends NpcController {

	private boolean isMaster = false;
	private boolean isVortex = false;
	private boolean isVolatile = false;
	private boolean isInvasion = false;
	private final Map<Integer, Player> passedPlayers = new HashMap<>();
	private SpawnTemplate slaveSpawnTemplate;
	private Npc slave;
	private Integer maxEntries;
	private Integer minLevel;
	private Integer maxLevel;
	private int usedEntries = 0;
	private boolean isAccepting;
	private RiftEnum riftTemplate;
	private int deSpawnedTime;

	/**
	 * Used to create master rifts or slave rifts (slave == null)
	 */
	public RVController(Npc slave, RiftEnum riftTemplate) {
		this.riftTemplate = riftTemplate;
		this.isVortex = riftTemplate.isVortex();
		this.maxEntries = riftTemplate.getEntries();
		this.minLevel = riftTemplate.getMinLevel();
		this.maxLevel = riftTemplate.getMaxLevel();
		this.deSpawnedTime = ((int) (System.currentTimeMillis() / 1000))
			+ (isVortex ? VortexService.getInstance().getDuration() * 3600 : RiftService.getInstance().getDuration() * 3600);
		this.isInvasion = riftTemplate.isInvasionRift();

		if (slave != null)// master rift should be created
		{
			this.slave = slave;
			this.slaveSpawnTemplate = slave.getSpawn();
			isMaster = true;
			isAccepting = true;
		}
	}

	public RVController(Npc slave, RiftEnum riftTemplate, boolean isWithGuards) {
		this.riftTemplate = riftTemplate;
		this.isVortex = riftTemplate.isVortex();
		this.maxEntries = riftTemplate.getEntries();
		this.minLevel = riftTemplate.getMinLevel();
		this.maxLevel = riftTemplate.getMaxLevel();
		this.deSpawnedTime = ((int) (System.currentTimeMillis() / 1000))
			+ (isVortex ? VortexService.getInstance().getDuration() * 3600 : RiftService.getInstance().getDuration() * 3600);
		this.isInvasion = riftTemplate.isInvasionRift();

		if (slave != null)// master rift should be created
		{
			this.slave = slave;
			slaveSpawnTemplate = slave.getSpawn();
			isMaster = true;
			isAccepting = true;
			isVolatile = riftTemplate.canBeVolatile() && isWithGuards;
		}
	}

	@Override
	public void onDialogRequest(Player player) {
		if (isMaster || isAccepting) {
			if (isInvasion && player.getOppositeRace() != riftTemplate.getDestination())
				return;
			onRequest(player);
		}
	}

	private void onRequest(Player player) {
		if (isVortex) {
			RequestResponseHandler<Npc> responseHandler = new RequestResponseHandler<Npc>(getOwner()) {

				@Override
				public void acceptRequest(Npc requester, Player responder) {
					if (onAccept(responder)) {
						if (responder.isInTeam()) {
							if (responder.getCurrentTeam() instanceof PlayerGroup) {
								PlayerGroupService.removePlayer(responder);
							} else {
								PlayerAllianceService.removePlayer(responder);
							}
						}

						VortexLocation loc = VortexService.getInstance().getLocationByRift(requester.getNpcId());
						TeleportService.teleportTo(responder, loc.getStartPoint());

						PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_MSG_INVADE_DIRECT_PORTAL_OPEN_NOTICE());

						// Update passed players count
						passedPlayers.put(responder.getObjectId(), responder);
						syncPassed(true);
					}
				}

			};

			boolean requested = player.getResponseRequester().putRequest(904304, responseHandler);
			if (requested) {
				PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(904304, getOwner().getObjectId(), 5));
			}
		} else {
			RequestResponseHandler<Npc> responseHandler = new RequestResponseHandler<Npc>(getOwner()) {

				@Override
				public void acceptRequest(Npc requester, Player responder) {
					if (onAccept(responder)) {
						int worldId = slaveSpawnTemplate.getWorldId();
						float x = slaveSpawnTemplate.getX();
						float y = slaveSpawnTemplate.getY();
						float z = slaveSpawnTemplate.getZ();

						TeleportService.teleportTo(responder, worldId, x, y, z);
						// Update passed players count
						syncPassed(false);
					}
				}

			};

			boolean requested = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_ASK_PASS_BY_DIRECT_PORTAL, responseHandler);
			if (requested) {
				PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_ASK_PASS_BY_DIRECT_PORTAL, 0, 0));
			}
		}
	}

	private boolean onAccept(Player player) {
		if (!isAccepting) {
			return false;
		}

		if (!getOwner().isSpawned()) {
			return false;
		}

		if (player.getLevel() > getMaxLevel() || player.getLevel() < getMinLevel()) {
			AuditLogger.log(player, "tried to use rift outside level restriction");
			return false;
		}

		if (isVortex && getUsedEntries() >= getMaxEntries()) {
			return false;
		}

		return true;
	}

	@Override
	public void onDespawn() {
		RiftInformer.sendRiftDespawn(getOwner().getWorldId(), getOwner().getObjectId());
		RiftManager.removeSpawnedRift(getOwner());
		super.onDespawn();
	}

	public boolean isMaster() {
		return isMaster;
	}

	public boolean isVortex() {
		return isVortex;
	}

	/**
	 * @return the maxEntries
	 */
	public Integer getMaxEntries() {
		return maxEntries;
	}

	/**
	 * @return the minLevel
	 */
	public Integer getMinLevel() {
		return minLevel;
	}

	/**
	 * @return the maxLevel
	 */
	public Integer getMaxLevel() {
		return maxLevel;
	}

	/**
	 * @return the riftTemplate
	 */
	public RiftEnum getRiftTemplate() {
		return riftTemplate;
	}

	/**
	 * @return slave rift
	 */
	public Npc getSlave() {
		return slave;
	}

	/**
	 * @return the usedEntries
	 */
	public int getUsedEntries() {
		return usedEntries;
	}

	public int getRemainTime() {
		return deSpawnedTime - (int) (System.currentTimeMillis() / 1000);
	}

	public boolean isVolatile() {
		return isVolatile;
	}

	public boolean isInvasion() {
		return isInvasion;
	}

	public Map<Integer, Player> getPassedPlayers() {
		return passedPlayers;
	}

	public void syncPassed(boolean invasion) {
		usedEntries = invasion ? passedPlayers.size() : ++usedEntries;
		RiftInformer.sendRiftInfo(getWorldsList(this));
	}

	private int[] getWorldsList(RVController controller) {
		int first = controller.getOwner().getWorldId();
		if (controller.isMaster()) {
			return new int[] { first, controller.slaveSpawnTemplate.getWorldId() };
		}
		return new int[] { first };
	}

}
