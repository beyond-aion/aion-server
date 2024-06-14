package com.aionemu.gameserver.services.siege;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.dao.SiegeDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.ArtifactLocation;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author SoulKeeper
 */
public class ArtifactSiege extends Siege<ArtifactLocation> {

	private static final Logger log = LoggerFactory.getLogger(ArtifactSiege.class.getName());

	public ArtifactSiege(ArtifactLocation siegeLocation) {
		super(siegeLocation);
	}

	@Override
	protected void onSiegeStart() {
		initSiegeBoss();
		getSiegeLocation().setInitialDelay(getStartTime());
		// Check for Balaur Assault
		if (SiegeConfig.BALAUR_AUTO_ASSAULT)
			BalaurAssaultService.getInstance().onSiegeStart(this);
	}

	@Override
	protected void onSiegeFinish() {
		// cleanup
		unregisterSiegeBossListeners();

		// despawn npcs
		despawnNpcs(getSiegeLocationId());

		// for artifact should be always true
		if (isBossKilled())
			onCapture();
		else
			log.error("Artifact siege (artifactId:" + getSiegeLocationId() + ") ended without killing a boss.");

		// add new spawns
		spawnNpcs(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.PEACE);

		// Store siege results in DB
		SiegeDAO.updateLocation(getSiegeLocation());

		broadcastUpdate(getSiegeLocation());
		startSiege(getSiegeLocationId());
	}

	protected void onCapture() {
		// Update winner counter
		SiegeRaceCounter wRaceCounter = getSiegeCounter().getWinnerRaceCounter();
		getSiegeLocation().setRace(wRaceCounter.getSiegeRace());

		// Update legion
		Integer wLegionId = wRaceCounter.getWinnerLegionId();
		getSiegeLocation().setLegionId(wLegionId != null ? wLegionId : 0);

		// misc stuff to send player system message
		if (getSiegeLocation().getRace() == SiegeRace.BALAUR) {
			PacketSendUtility.broadcastToWorld(
				SM_SYSTEM_MESSAGE.STR_GUILD_EVENT_LOSE_ARTIFACT(getSiegeLocation().getL10n(), getSiegeLocation().getRace().getL10n()));
		} else {
			// Prepare packet data
			String wPlayerName = "";
			final Race wRace = wRaceCounter.getSiegeRace() == SiegeRace.ELYOS ? Race.ELYOS : Race.ASMODIANS;
			Legion wLegion = wLegionId != null ? LegionService.getInstance().getLegion(wLegionId) : null;
			if (!wRaceCounter.getPlayerDamageCounter().isEmpty()) {
				Integer wPlayerId = wRaceCounter.getPlayerDamageCounter().keySet().iterator().next();
				wPlayerName = PlayerService.getPlayerName(wPlayerId);
			}
			String winnerName = wLegion != null ? wLegion.getName() : wPlayerName;

			// prepare packets, we can use single packet instance
			AionServerPacket wRacePacket = SM_SYSTEM_MESSAGE.STR_GUILD_EVENT_WIN_ARTIFACT(wRace.getL10n(), winnerName,
				getSiegeLocation().getL10n());
			AionServerPacket lRacePacket = SM_SYSTEM_MESSAGE.STR_GUILD_EVENT_LOSE_ARTIFACT(getSiegeLocation().getL10n(), wRace.getL10n());

			// send update to players
			World.getInstance().forEachPlayer(p -> PacketSendUtility.sendPacket(p, p.getRace().equals(wRace) ? wRacePacket : lRacePacket));
		}
	}

	@Override
	public boolean isEndless() {
		return true;
	}

	@Override
	public void onAbyssPointsAdded(Player player, int abysPoints) {
		// No need to control AP
	}

}
