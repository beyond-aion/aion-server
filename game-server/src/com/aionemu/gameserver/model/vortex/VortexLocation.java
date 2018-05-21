package com.aionemu.gameserver.model.vortex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.controllers.RVController;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.vortex.VortexTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.vortex.DimensionalVortex;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.zone.InvasionZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.handler.ZoneHandler;

/**
 * @author Source
 */
public class VortexLocation implements ZoneHandler {

	private boolean isActive;
	private DimensionalVortex<VortexLocation> activeVortex;
	private RVController vortexController;
	private final VortexTemplate template;
	private final List<InvasionZoneInstance> zones = new ArrayList<>();
	private final Map<Integer, Player> players = new HashMap<>();
	private final Map<Integer, Kisk> kisks = new HashMap<>();
	private final List<VisibleObject> spawned = new ArrayList<>();

	public VortexLocation(VortexTemplate template) {
		this.template = template;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActiveVortex(DimensionalVortex<VortexLocation> vortex) {
		isActive = vortex != null;
		this.activeVortex = vortex;
	}

	public DimensionalVortex<VortexLocation> getActiveVortex() {
		return activeVortex;
	}

	public void setVortexController(RVController controller) {
		this.vortexController = controller;
	}

	public RVController getVortexController() {
		return vortexController;
	}

	public final VortexTemplate getTemplate() {
		return template;
	}

	public WorldPosition getHomePoint() {
		return template.getHomePoint().getHomePoint();
	}

	public WorldPosition getResurrectionPoint() {
		return template.getResurrectionPoint().getResurrectionPoint();
	}

	public WorldPosition getStartPoint() {
		return template.getStartPoint().getStartPoint();
	}

	public int getId() {
		return template.getId();
	}

	public Race getDefendersRace() {
		return template.getDefendersRace();
	}

	public Race getInvadersRace() {
		return template.getInvadersRace();
	}

	public int getHomeWorldId() {
		return template.getHomePoint().getWorldId();
	}

	public int getInvasionWorldId() {
		return template.getStartPoint().getWorldId();
	}

	public List<VisibleObject> getSpawned() {
		return spawned;
	}

	public Map<Integer, Player> getPlayers() {
		return players;
	}

	public Map<Integer, Kisk> getInvadersKisks() {
		return kisks;
	}

	public boolean isInvaderInside(int objId) {
		return isActive() && getVortexController().getPassedPlayers().containsKey(objId);
	}

	public boolean isInsideActiveVotrex(Player player) {
		return isActive() && isInsideLocation(player);
	}

	public void addZone(InvasionZoneInstance zone) {
		zones.add(zone);
		zone.addHandler(this);
	}

	public boolean isInsideLocation(Creature creature) {
		if (!zones.isEmpty()) {
			for (InvasionZoneInstance zone : zones) {
				if (zone.isInsideCreature(creature))
					return true;
			}
		}
		return false;
	}

	public List<InvasionZoneInstance> getZones() {
		return zones;
	}

	@Override
	public void onEnterZone(Creature creature, ZoneInstance zone) {
		if (creature instanceof Kisk) {
			if (creature.getRace().equals(getInvadersRace())) {
				kisks.put(creature.getObjectId(), (Kisk) creature);
			}
		} else if (creature instanceof Player) {
			Player player = (Player) creature;

			if (!players.containsKey(player.getObjectId())) {
				players.put(player.getObjectId(), player);

				if (isActive()) {
					if (player.getRace().equals(getInvadersRace())) {
						if (getVortexController().getPassedPlayers().containsKey(player.getObjectId())
							&& !getActiveVortex().getInvaders().containsKey(player.getObjectId())) {
							getActiveVortex().addPlayer(player, true);
						}
					} else {
						getActiveVortex().updateDefenders(player);
					}
				}
			}
		}
	}

	@Override
	public void onLeaveZone(Creature creature, ZoneInstance zone) {
		if (!isInsideLocation(creature)) {
			if (creature instanceof Kisk) {
				kisks.remove(creature.getObjectId());
			} else if (creature instanceof Player) {
				Player player = (Player) creature;

				players.remove(player.getObjectId());

				if (isActive()) {
					if (player.getRace().equals(getInvadersRace())) {
						if (getVortexController().getPassedPlayers().containsKey(player.getObjectId())) {
							// You have left the battlefield.
							PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(904305));

							// start kick timer
							ThreadPoolManager.getInstance().schedule(() -> {
								if (player.isOnline() && !isInsideActiveVotrex(player)) {
									getActiveVortex().kickPlayer(player, true);
								}
							}, 10 * 1000);
						}
					} else {
						// start kick timer
						ThreadPoolManager.getInstance().schedule(() -> {
							if (player.isOnline() && !isInsideActiveVotrex(player)) {
								getActiveVortex().kickPlayer(player, false);
							}
						}, 10 * 1000);
					}
				}
			}
		}
	}

}
