package com.aionemu.gameserver.services.siege;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.callbacks.util.GlobalCallbackHelper;
import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dao.SiegeDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.ArtifactLocation;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.team.legion.Legion;
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
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

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
		deSpawnNpcs(getSiegeLocationId());
		spawnNpcs(getSiegeLocationId(), getSiegeLocation().getRace(), SiegeModType.SIEGE);
		initSiegeBoss();
		this.oldLegionId = getSiegeLocation().getLegionId();
		initMercenaryZones();
		// Check for Balaur Assault
		if (SiegeConfig.BALAUR_AUTO_ASSAULT)
			BalaurAssaultService.getInstance().onSiegeStart(this);
		if (!getSiegeLocation().getRace().equals(SiegeRace.BALAUR) && getBoss().getLevel() == 65)
			ThreadPoolManager.getInstance().schedule(() -> scheduleFactionTroopAssault(), Rnd.get(600, 1800) * 1000); // Faction Balance NPCs
	}

	/**
	 * Handles an additional assault of race-specific troops, which were requested
	 * by players to ensure their glory point rewards.
	 */
	private final void scheduleFactionTroopAssault() {
		if (!getSiegeLocation().isVulnerable())
			return;

		final int worldId = getSiegeLocation().getWorldId();
		final SiegeRace oppositeRace = SiegeRace.getOppositeRace(getSiegeLocation().getRace());
		for (SiegeNpc sn : World.getInstance().getLocalSiegeNpcs(getSiegeLocationId())) {
			if (sn.getAbyssNpcType() == AbyssNpcType.ARTIFACT || Rnd.get(1, 100) <= 35)
				continue;
			final int amount = Rnd.get(1, 2);
			for (int i = 0; i < amount; i++) {
				float x1 = (float) (sn.getX() + Math.cos(Math.PI * Rnd.get()) * Rnd.get(1, 3));
				float y1 = (float) (sn.getY() + Math.sin(Math.PI * Rnd.get()) * Rnd.get(1, 3));
				SpawnTemplate temp = SpawnEngine.addNewSiegeSpawn(worldId,
					oppositeRace == SiegeRace.ELYOS ? Rnd.get(252408, 252412) : Rnd.get(252413, 252417), getSiegeLocationId(), oppositeRace,
					SiegeModType.ASSAULT, x1, y1, sn.getZ(), (byte) 0);
				SpawnEngine.spawnObject(temp, 1);
			}
		}
	}

	private final void initMercenaryZones() {
		if (getSiegeLocation().getRace().equals(SiegeRace.BALAUR))
			return;
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
			getSiegeLocation().doOnAllPlayers(new Visitor<Player>() {

				@Override
				public void visit(Player player) {
					if (SiegeRace.getByRace(player.getRace()) == getSiegeLocation().getRace())
						QuestEngine.getInstance().onKill(new QuestEnv(getBoss(), player, 0, 0));
				}

			});
		}
	}

	public void onCapture() {
		SiegeRaceCounter winner = getSiegeCounter().getWinnerRaceCounter();
		SiegeRace winnerRace = winner.getSiegeRace();

		try {
			// Players gain buffs on capture of some fortresses
			applyWorldBuffs(winnerRace, getSiegeLocation().getRace());
		} catch (Exception e) {
			log.error("Error while applying buffs after capture, location " + getSiegeLocation().getLocationId(), e);
		}
		// Set new fortress and artifact owner race
		getSiegeLocation().setRace(winnerRace);
		getArtifact().setRace(winnerRace);

		// reset occupy count
		getSiegeLocation().setOccupiedCount(winnerRace == SiegeRace.BALAUR ? 0 : 1);

		if (this.oldLegionId != 0 && getSiegeLocation().hasValidGpRewards()) { // make sure holding GP are deducted on Capture
			int oldLegionGeneral = LegionService.getInstance().getLegionBGeneral(this.oldLegionId);
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
		SiegeRace loserRace = getSiegeLocation().getRace() != SiegeRace.BALAUR ? (getSiegeLocation().getRace() == SiegeRace.ELYOS ? SiegeRace.ASMODIANS
			: SiegeRace.ELYOS) : SiegeRace.BALAUR;

		// Increase fortress occupied count
		if (getSiegeLocation().getRace() != SiegeRace.BALAUR && getSiegeLocation().getTemplate().getMaxOccupyCount() > 0) {
			getSiegeLocation().increaseOccupiedCount();
		}

		try {
			// Players gain buffs for successfully defense / failed capture the fortress
			applyWorldBuffs(getSiegeLocation().getRace(), loserRace);
		} catch (Exception e) {
			log.error("Error while applying buffs after defense, location " + getSiegeLocation().getLocationId(), e);
		}
	}

	private void applyWorldBuffs(SiegeRace wRace, SiegeRace lRace) {
		final int loserSkillId;
		final int winnerSkillId;
		final int floc = getSiegeLocation().getLocationId();
		final Race winningRace = wRace != SiegeRace.BALAUR ? (wRace == SiegeRace.ELYOS ? Race.ELYOS : Race.ASMODIANS) : null;
		final Race losingRace = lRace != SiegeRace.BALAUR ? (lRace == SiegeRace.ELYOS ? Race.ELYOS : Race.ASMODIANS) : null;

		switch (floc) {
			case 1131:
				loserSkillId = 0;
				winnerSkillId = 12147;
				break;
			case 1132:
				loserSkillId = 0;
				winnerSkillId = 12148;
				break;
			case 1141:
				loserSkillId = 0;
				winnerSkillId = 12149;
				break;
			case 1221:
				loserSkillId = 0;
				winnerSkillId = 12075;
				break;
			case 1231:
				loserSkillId = 0;
				winnerSkillId = 12076;
				break;
			case 1241:
				loserSkillId = 0;
				winnerSkillId = 12077;
				break;
			case 1251:
				loserSkillId = 0;
				winnerSkillId = 12074;
				break;
			case 2011:
				loserSkillId = 0;
				winnerSkillId = 12155;
				break;
			case 2021:
				loserSkillId = 0;
				winnerSkillId = 12156;
				break;
			case 3011:
				loserSkillId = 0;
				winnerSkillId = 12157;
				break;
			case 3021:
				loserSkillId = 0;
				winnerSkillId = 12158;
				break;
			default:
				return;
		}

		World.getInstance().doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				if (floc == 1131 || floc == 1132 || floc == 1141 || floc == 1221 || floc == 1231 || floc == 1241 || floc == 1251) {
					if (player.getWorldId() == 400010000) {
						if (winningRace != null && player.getRace().equals(winningRace)) {
							SkillEngine.getInstance().applyEffectDirectly(winnerSkillId, player, player, 0);
						} else if (losingRace != null && player.getRace().equals(losingRace) && loserSkillId != 0) {
							SkillEngine.getInstance().applyEffectDirectly(loserSkillId, player, player, 0);
						}
					}
				} else if (floc == 2011 || floc == 2021 || floc == 3011 || floc == 3021) {
					if (player.getWorldId() == 220070000 || player.getWorldId() == 600010000 || player.getWorldId() == 210050000) {
						if (winningRace != null && player.getRace().equals(winningRace)) {
							SkillEngine.getInstance().applyEffectDirectly(winnerSkillId, player, player, 0);
						} else if (losingRace != null && player.getRace().equals(losingRace) && loserSkillId != 0) {
							SkillEngine.getInstance().applyEffectDirectly(loserSkillId, player, player, 0);
						}
					}
				} else if (floc == 5011 || floc == 6011 || floc == 6021) {
					if (player.getWorldId() == 600050000 || player.getWorldId() == 600060000) {
						if (winningRace != null && player.getRace().equals(winningRace)) {
							SkillEngine.getInstance().applyEffectDirectly(winnerSkillId, player, player, 0);
						} else if (losingRace != null && player.getRace().equals(losingRace) && loserSkillId != 0) {
							SkillEngine.getInstance().applyEffectDirectly(loserSkillId, player, player, 0);
						}
					}
				}
			}
		});
	}

	protected void giveRewardsToLegion() {
		try {
			// Legion with id 0 = not exists?
			if (getSiegeLocation().getLegionId() == 0) {
				if (LoggingConfig.LOG_SIEGE)
					log.info("[SIEGE] > [FORTRESS:" + getSiegeLocationId() + "] [RACE: " + getSiegeLocation().getRace() + "] [LEGION :"
						+ getSiegeLocation().getLegionId() + "] Legion Reward not sending because fortress not owned by any legion.");
				return;
			}

			List<SiegeLegionReward> legionRewards = getSiegeLocation().getLegionReward();
			int legionBGeneral = LegionService.getInstance().getLegionBGeneral(getSiegeLocation().getLegionId());
			if (legionBGeneral != 0) {
				PlayerCommonData BGeneral = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonData(legionBGeneral);
				if (LoggingConfig.LOG_SIEGE) {
					log.info("[SIEGE] > [FORTRESS:" + getSiegeLocationId() + "] [RACE: " + getSiegeLocation().getRace()
						+ "] Legion Reward in process... LegionId:" + getSiegeLocation().getLegionId() + " General Name:" + BGeneral.getName());
				}
				if (legionRewards != null) {
					for (SiegeLegionReward medalsType : legionRewards) {
						if (LoggingConfig.LOG_SIEGE) {
							log.info("[SIEGE] > [Legion Reward to: " + BGeneral.getName() + "] ITEM RETURN " + medalsType.getItemId() + " ITEM COUNT "
								+ medalsType.getCount());
						}
						MailFormatter.sendAbyssRewardMail(getSiegeLocation(), BGeneral, AbyssSiegeLevel.NONE, SiegeResult.PROTECT, System.currentTimeMillis(),
							medalsType.getItemId(), medalsType.getCount(), 0);
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
			int legionBGeneral = LegionService.getInstance().getLegionBGeneral(winnerLegionId);

			boolean defenceSuccessful = winnerLegionId == this.oldLegionId;
			if (defenceSuccessful) {
				if (legionBGeneral != 0) {
					List<Integer> deputies = LegionService.getInstance().getMembersByRank(winnerLegionId, LegionRank.DEPUTY);
					int gpReward = Math.round(500 / (float) (1 + deputies.size()));
					GloryPointsService.increaseGp(legionBGeneral, gpReward);
					for (int playerObjId : deputies) {
						GloryPointsService.increaseGp(playerObjId, gpReward);
					}
				}
			} else {
				if (legionBGeneral != 0) {
					GloryPointsService.increaseGp(legionBGeneral, 1000, false);
					Legion legion = LegionService.getInstance().getLegion(winnerLegionId);
					legion.increaseSiegeGloryPoints(1000);
				}
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
			List<Integer> topPlayersIds = new FastTable<>();
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
