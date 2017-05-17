package com.aionemu.gameserver.services.siege;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.callbacks.util.GlobalCallbackHelper;
import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dao.SiegeDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.ArtifactLocation;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionHistoryType;
import com.aionemu.gameserver.model.team.legion.LegionRank;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLegionReward;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeMercenaryZone;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeReward;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.mail.AbyssSiegeLevel;
import com.aionemu.gameserver.services.mail.MailFormatter;
import com.aionemu.gameserver.services.mail.SiegeResult;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * Object that controls siege of certain fortress. Siege object is not reusable. New siege = new instance.
 * <p/>
 *
 * @author SoulKeeper
 */
public class FortressSiege extends Siege<FortressLocation> {

	private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");
	private final Map<Integer, MercenaryLocation> activeMercenaryLocs = new ConcurrentHashMap<>();
	private final AbyssPointsListener addAPListener = new AbyssPointsListener(this);
	private int oldLegionId;

	public FortressSiege(FortressLocation fortress) {
		super(fortress);
	}

	@Override
	public void onSiegeStart() {
		if (LoggingConfig.LOG_SIEGE)
			log.info("[SIEGE] > Siege started. [FORTRESS:" + getSiegeLocationId() + "] [RACE: " + getSiegeLocation().getRace() + "] [LegionId:"
				+ getSiegeLocation().getLegionId() + "]");
		// Mark fortress as vulnerable
		getSiegeLocation().setVulnerable(true);

		// Let the world know where the siege is
		broadcastState(getSiegeLocation());

		// Clear fortress from enemys
		getSiegeLocation().clearLocation();

		// Register abyss points listener
		// We should listen for abyss point callbacks that players are earning
		GlobalCallbackHelper.addCallback(addAPListener);

		// Remove all and spawn siege NPCs
		despawnNpcs(getSiegeLocationId());
		spawnNpcs(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.SIEGE);
		initSiegeBoss();
		this.oldLegionId = getSiegeLocation().getLegionId();
		if (getSiegeLocation().getRace() == SiegeRace.BALAUR) {
			initMercenaryZones();
		} else {
			if (getBoss().getLevel() == 65) {
				SiegeRace oppositeRace = getSiegeLocation().getRace() == SiegeRace.ELYOS ? SiegeRace.ASMODIANS : SiegeRace.ELYOS;
				ThreadPoolManager.getInstance().schedule(() -> spawnFactionTroopAssault(oppositeRace), Rnd.get(600, 1800) * 1000); // Faction Balance NPCs
			}
		}
		// Check for Balaur Assault
		if (SiegeConfig.BALAUR_AUTO_ASSAULT)
			BalaurAssaultService.getInstance().onSiegeStart(this);
	}

	/**
	 * Handles an additional assault of race-specific troops (asmo/ely only), to ensure players glory point rewards
	 */
	private final void spawnFactionTroopAssault(SiegeRace race) {
		if (!getSiegeLocation().isVulnerable())
			return;

		final int worldId = getSiegeLocation().getWorldId();
		for (SiegeNpc sn : World.getInstance().getLocalSiegeNpcs(getSiegeLocationId())) {
			if (sn.getAbyssNpcType() == AbyssNpcType.ARTIFACT || Rnd.chance() < 35)
				continue;
			final int amount = Rnd.get(1, 2);
			for (int i = 0; i < amount; i++) {
				float x1 = (float) (sn.getX() + Math.cos(Math.PI * Rnd.get()) * Rnd.get(1, 3));
				float y1 = (float) (sn.getY() + Math.sin(Math.PI * Rnd.get()) * Rnd.get(1, 3));
				SpawnTemplate temp = SpawnEngine.addNewSiegeSpawn(worldId, race == SiegeRace.ELYOS ? Rnd.get(252408, 252412) : Rnd.get(252413, 252417),
					getSiegeLocationId(), race, SiegeModType.ASSAULT, x1, y1, sn.getZ(), (byte) 0);
				SpawnEngine.spawnObject(temp, 1);
			}
		}
	}

	private final void initMercenaryZones() {
		List<SiegeMercenaryZone> mercs = getSiegeLocation().getSiegeMercenaryZones(); // can be null if not implemented
		if (mercs == null)
			return;
		for (SiegeMercenaryZone zone : mercs) {
			MercenaryLocation mLoc = new MercenaryLocation(zone, getSiegeLocation().getRace(), getSiegeLocationId());
			activeMercenaryLocs.put(zone.getId(), mLoc);
		}
	}

	@Override
	public void onSiegeFinish() {
		if (LoggingConfig.LOG_SIEGE) {
			SiegeRaceCounter winner = getSiegeCounter().getWinnerRaceCounter();
			if (isBossKilled() && winner != null)
				log.info("[SIEGE] > Siege finished. [FORTRESS:" + getSiegeLocationId() + "] [OLD RACE: " + getSiegeLocation().getRace() + "] [OLD LegionId:"
					+ getSiegeLocation().getLegionId() + "] [NEW RACE: " + winner.getSiegeRace() + "] [NEW LegionId:"
					+ (winner.getWinnerLegionId() == null ? 0 : winner.getWinnerLegionId()) + "]");
			else
				log.info("[SIEGE] > Siege finished. No winner found [FORTRESS:" + getSiegeLocationId() + "] [RACE: " + getSiegeLocation().getRace()
					+ "] [LegionId:" + getSiegeLocation().getLegionId() + "]");
		}

		// Unregister abyss points listener callback
		// We really don't need to add abyss points anymore
		GlobalCallbackHelper.removeCallback(addAPListener);

		// Unregister siege boss listeners
		// cleanup :)
		unregisterSiegeBossListeners();

		// despawn protectors and make fortress invulnerable
		SiegeService.getInstance().deSpawnNpcs(getSiegeLocationId());
		getSiegeLocation().setVulnerable(false);
		getSiegeLocation().setUnderShield(false);

		// Guardian deity general was not killed, fortress stays with previous
		if (isBossKilled()) {
			onCapture();
			broadcastUpdate(getSiegeLocation());
		} else {
			onDefended();
			broadcastState(getSiegeLocation());
		}

		SiegeService.getInstance().spawnNpcs(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.PEACE);

		// Reward players and owning legion
		// If fortress was not captured by balaur
		if (SiegeRace.BALAUR != getSiegeLocation().getRace()) {
			giveRewardsToLegion();
			if (getSiegeLocation().hasValidGpRewards())
				calculateLegionGloryPointsRewards();

			SiegeRace winnerRace = getSiegeLocation().getRace();
			SiegeRace looserRace = winnerRace == SiegeRace.ASMODIANS ? SiegeRace.ELYOS : SiegeRace.ASMODIANS;
			sendRewardsToParticipatedPlayers(getSiegeCounter().getRaceCounter(winnerRace), true);
			sendRewardsToParticipatedPlayers(getSiegeCounter().getRaceCounter(looserRace), false);
		}

		// Update outpost status
		// Certain fortresses are changing outpost ownership
		updateOutpostStatusByFortress(getSiegeLocation());

		// Update data in the DB
		DAOManager.getDAO(SiegeDAO.class).updateSiegeLocation(getSiegeLocation());
		if (isBossKilled()) {
			getSiegeLocation().forEachPlayer(p -> {
				if (SiegeRace.getByRace(p.getRace()) == getSiegeLocation().getRace())
					QuestEngine.getInstance().onKill(new QuestEnv(getBoss(), p, 0));
			});
		}
	}

	public void onCapture() {
		SiegeRaceCounter winner = getSiegeCounter().getWinnerRaceCounter();
		SiegeRace winnerRace = winner.getSiegeRace();

		// Players gain buffs on capture of some fortresses
		applyWorldBuffs(winnerRace);
		// Set new fortress and artifact owner race
		getSiegeLocation().setRace(winnerRace);
		getArtifact().setRace(winnerRace);

		// reset occupy count
		getSiegeLocation().setOccupiedCount(winnerRace == SiegeRace.BALAUR ? 0 : 1);

		if (oldLegionId != 0 && getSiegeLocation().hasValidGpRewards()) { // make sure holding GP are deducted on Capture
			int oldLegionGeneral = LegionService.getInstance().getBrigadeGeneralOfLegion(oldLegionId);
			if (oldLegionGeneral != 0) {
				GloryPointsService.decreaseGp(oldLegionGeneral, 1000);
				LegionService.getInstance().getLegion(oldLegionId).decreaseSiegeGloryPoints(1000);
			}
		}

		// If new race is balaur
		if (SiegeRace.BALAUR == winnerRace) {
			getSiegeLocation().setLegionId(0);
			getArtifact().setLegionId(0);
		} else {
			Integer topLegionId = winner.getWinnerLegionId();
			getSiegeLocation().setLegionId(topLegionId != null ? topLegionId : 0);
			getArtifact().setLegionId(topLegionId != null ? topLegionId : 0);
		}
	}

	public void removeOldOwnersGp() {

	}

	private void onDefended() {
		// Increase fortress occupied count
		if (getSiegeLocation().getRace() != SiegeRace.BALAUR && getSiegeLocation().getTemplate().getMaxOccupyCount() > 0) {
			getSiegeLocation().increaseOccupiedCount();
		}

		// Players gain buffs for successfully defense / failed capture the fortress
		applyWorldBuffs(getSiegeLocation().getRace());
	}

	private void applyWorldBuffs(SiegeRace winner) {
		final int skillId;

		switch (getSiegeLocation().getLocationId()) {
			case 1131:
				skillId = 12147;
				break;
			case 1132:
				skillId = 12148;
				break;
			case 1141:
				skillId = 12149;
				break;
			case 1221:
				skillId = 12075;
				break;
			case 1231:
				skillId = 12076;
				break;
			case 1241:
				skillId = 12077;
				break;
			case 1251:
				skillId = 12074;
				break;
			case 2011:
				skillId = 12155;
				break;
			case 2021:
				skillId = 12156;
				break;
			case 3011:
				skillId = 12157;
				break;
			case 3021:
				skillId = 12158;
				break;
			default:
				return;
		}

		World.getInstance().forEachPlayer(p -> {
			if (SiegeRace.getByRace(p.getRace()) == winner) {
				SkillEngine.getInstance().applyEffectDirectly(skillId, p, p, 0);
			}
		});
	}

	protected void giveRewardsToLegion() {
		try {
			// Legion with id 0 = not exists?
			int legionId = getSiegeLocation().getLegionId();
			if (legionId == 0) {
				if (LoggingConfig.LOG_SIEGE)
					log.info("[SIEGE] > [FORTRESS:" + getSiegeLocationId() + "] [RACE: " + getSiegeLocation().getRace() + "] [LEGION :" + legionId
						+ "] Legion Reward not sending because fortress not owned by any legion.");
				return;
			}
			List<SiegeLegionReward> legionRewards = getSiegeLocation().getLegionReward();
			if (legionRewards != null) {
				Legion legion = LegionService.getInstance().getLegion(legionId);

				int bgId = legion.getBrigadeGeneral();
				if (bgId != 0) {
					PlayerCommonData brigadeGeneral = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonData(bgId);
					for (SiegeLegionReward legionReward : legionRewards) {
						if (LoggingConfig.LOG_SIEGE) {
							log.info("[SIEGE] > [Legion Reward to: " + brigadeGeneral.getName() + "] ITEM RETURN " + legionReward.getItemId() + " ITEM COUNT "
								+ legionReward.getCount());
						}
						if (legionReward.getItemId() == 182400001) { // handles kinah rewards
							int kinahAmount = isBossKilled() ? legionReward.getCount() : Math.round(legionReward.getCount() * 0.7f);
							legion.getLegionWarehouse().increaseKinah(kinahAmount);
							LegionService.getInstance().addRewardHistory(legion, kinahAmount,
								isBossKilled() ? LegionHistoryType.OCCUPATION : LegionHistoryType.DEFENSE, getSiegeLocationId());
							continue;
						}
						MailFormatter.sendAbyssRewardMail(getSiegeLocation(), brigadeGeneral, AbyssSiegeLevel.NONE, SiegeResult.PROTECT,
							System.currentTimeMillis(), legionReward.getItemId(), legionReward.getCount(), 0);
					}
				}
			}
		} catch (Exception e) {
			log.error("[SIEGE] Error while calculating legion reward for fortress siege. Location:" + getSiegeLocation().getLocationId(), e);
		}
	}

	protected void calculateLegionGloryPointsRewards() {
		try {
			int winnerLegionId = getSiegeLocation().getLegionId();
			if (winnerLegionId == 0)
				return;

			int legionBGeneral = LegionService.getInstance().getBrigadeGeneralOfLegion(winnerLegionId);
			if (legionBGeneral == 0)
				return;

			boolean defenseSuccessful = winnerLegionId == oldLegionId;
			if (defenseSuccessful) {
				List<Integer> deputies = LegionService.getInstance().getMembersByRank(winnerLegionId, LegionRank.DEPUTY);
				int gpReward = Math.round(500 / (float) (1 + deputies.size()));
				GloryPointsService.increaseGp(legionBGeneral, gpReward);
				for (int playerObjId : deputies) {
					GloryPointsService.increaseGp(playerObjId, gpReward);
				}
			} else {
				GloryPointsService.increaseGp(legionBGeneral, 1000, false);
				Legion legion = LegionService.getInstance().getLegion(winnerLegionId);
				legion.increaseSiegeGloryPoints(1000);
			}
		} catch (Exception e) {
			log.error("Error while calculating glory points reward for fortress siege.", e);
		}
	}

	/**
	 * Will send rewards to players who participated in this siege.
	 * 
	 * @param damageCounter
	 * @param isWinner
	 */
	protected void sendRewardsToParticipatedPlayers(SiegeRaceCounter damage, boolean isWinner) {
		try {
			Map<Integer, Long> playerAbyssPoints = damage.getPlayerAbyssPoints();
			List<Integer> topPlayersIds = new ArrayList<>();
			topPlayersIds.addAll(playerAbyssPoints.keySet());
			SiegeResult result;
			if (isBossKilled())
				result = isWinner ? SiegeResult.OCCUPY : SiegeResult.FAIL;
			else
				result = isWinner ? SiegeResult.DEFENDER : SiegeResult.EMPTY;

			int i = 0;
			List<SiegeReward> playerRewards = getSiegeLocation().getReward();
			int rewardLevel = 0;
			for (SiegeReward topGrade : playerRewards) {
				AbyssSiegeLevel level = AbyssSiegeLevel.getLevelById(++rewardLevel);
				for (int rewardedPC = 0; i < topPlayersIds.size() && rewardedPC < topGrade.getTop(); ++i) {
					Integer playerId = topPlayersIds.get(i);
					PlayerCommonData pcd = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonData(playerId);
					++rewardedPC;
					if (result.equals(SiegeResult.OCCUPY) || result.equals(SiegeResult.DEFENDER))
						MailFormatter.sendAbyssRewardMail(getSiegeLocation(), pcd, level, result, System.currentTimeMillis(), topGrade.getItemId(),
							topGrade.getMedalCount(), 0);

					if (getSiegeLocation().hasValidGpRewards())
						GloryPointsService.increaseGp(playerId, isWinner ? topGrade.getGpForWin() : topGrade.getGpForDefeat());
				}
			}
		} catch (Exception e) {
			log.error("[SIEGE] Error while calculating rewards for fortress siege.", e);
		}
	}

	@Override
	public boolean isEndless() {
		return false;
	}

	@Override
	public void addAbyssPoints(Player player, int abysPoints) {
		getSiegeCounter().addAbyssPoints(player, abysPoints);
	}

	protected ArtifactLocation getArtifact() {
		return SiegeService.getInstance().getFortressArtifacts().get(getSiegeLocationId());
	}

	protected boolean hasArtifact() {
		return getArtifact() != null;
	}

	public MercenaryLocation getMercenaryLocationByZoneId(int zoneId) {
		return activeMercenaryLocs.get(zoneId);
	}
}
