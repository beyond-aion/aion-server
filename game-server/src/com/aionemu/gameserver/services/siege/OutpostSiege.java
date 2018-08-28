package com.aionemu.gameserver.services.siege;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.aionemu.commons.callbacks.util.GlobalCallbackHelper;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.OutpostLocation;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.mail.SiegeResult;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author SoulKeeper, Estrayl
 */
public class OutpostSiege extends Siege<OutpostLocation> {

	private final AbyssPointsListener apListener = new AbyssPointsListener(this);

	public OutpostSiege(OutpostLocation siegeLocation) {
		super(siegeLocation);
	}

	@Override
	protected void onSiegeStart() {
		SiegeService.getInstance().deSpawnNpcs(getSiegeLocationId());

		getSiegeLocation().setVulnerable(true);
		GlobalCallbackHelper.addCallback(apListener);
		despawnNpcs(getSiegeLocationId());
		spawnNpcs(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.SIEGE);
		initSiegeBoss();

		PacketSendUtility.broadcastToWorld(
			getSiegeLocationId() == 2111 ? SM_SYSTEM_MESSAGE.STR_FIELDABYSS_LIGHTBOSS_SPAWN() : SM_SYSTEM_MESSAGE.STR_FIELDABYSS_DARKBOSS_SPAWN());
		broadcastUpdate(getSiegeLocation());
	}

	@Override
	protected void onSiegeFinish() {
		GlobalCallbackHelper.removeCallback(apListener);
		unregisterSiegeBossListeners();
		getSiegeLocation().setVulnerable(false);
		despawnSiegeNpcs();

		if (isBossKilled()) {
			onAgentDefeated();
			sendRewardsToParticipants(getSiegeCounter().getWinnerRaceCounter(), SiegeResult.OCCUPY);
			sendRewardsToParticipants(getSiegeCounter().getRaceCounter(getSiegeLocationId() == 2111 ? SiegeRace.ELYOS : SiegeRace.ASMODIANS),
				SiegeResult.FAIL);
		} else {
			PacketSendUtility.broadcastToWorld(
				getSiegeLocationId() == 2111 ? SM_SYSTEM_MESSAGE.STR_FIELDABYSS_LIGHTBOSS_DESPAWN() : SM_SYSTEM_MESSAGE.STR_FIELDABYSS_DARKBOSS_DESPAWN());
			sendRewardsToParticipants(getSiegeCounter().getRaceCounter(SiegeRace.ELYOS), SiegeResult.EMPTY);
			sendRewardsToParticipants(getSiegeCounter().getRaceCounter(SiegeRace.ASMODIANS), SiegeResult.EMPTY);
		}
		broadcastUpdate(getSiegeLocation());
		spawnNpcs(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.PEACE);
	}

	private void onAgentDefeated() {
		SiegeRaceCounter winnerCounter = getSiegeCounter().getWinnerRaceCounter();
		Map<Integer, Long> topPlayerDamages = winnerCounter.getPlayerDamageCounter();
		if (!topPlayerDamages.isEmpty()) {
			Player topOnlinePlayer = null;
			for (Iterator<Integer> iter = topPlayerDamages.keySet().iterator(); iter.hasNext();) {
				Integer topPlayerId = topPlayerDamages.keySet().iterator().next();
				topOnlinePlayer = World.getInstance().findPlayer(topPlayerId);
				if (topOnlinePlayer != null)
					break;
			}

			String playerName = "";
			String legionName = "";
			if (topOnlinePlayer != null) { // Should never happens - otherwise the message will be empty
				playerName = topOnlinePlayer.getName();
				if (topOnlinePlayer.isLegionMember())
					legionName = topOnlinePlayer.getLegion().getName();
			}
			PacketSendUtility.broadcastToWorld(getSiegeLocationId() == 2111 ? SM_SYSTEM_MESSAGE.STR_FIELDABYSS_LIGHTBOSS_KILLED(playerName, legionName)
				: SM_SYSTEM_MESSAGE.STR_FIELDABYSS_DARKBOSS_KILLED(playerName, legionName));
			Race winnerRace = winnerCounter.getSiegeRace() == SiegeRace.ELYOS ? Race.ELYOS : Race.ASMODIANS;

			World.getInstance().forEachPlayer(p -> {
				if (p.getRace().equals(winnerRace))
					SkillEngine.getInstance().applyEffectDirectly(winnerRace == Race.ELYOS ? 12120 : 12119, p, p);
			});
		}
	}

	public void despawnSiegeNpcs() {
		Collection<SiegeNpc> npcs = World.getInstance().getLocalSiegeNpcs(getSiegeLocationId());
		for (SiegeNpc npc : npcs) {
			if (npc != null && !npc.isDead())
				npc.getController().delete();
		}
	}

	@Override
	public boolean isEndless() {
		return false;
	}

	@Override
	public void addAbyssPoints(Player player, int abyssPoints) {
		getSiegeCounter().addAbyssPoints(player, abyssPoints);
	}

}
