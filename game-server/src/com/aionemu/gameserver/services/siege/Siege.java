package com.aionemu.gameserver.services.siege;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeReward;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SIEGE_LOCATION_STATE;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.mail.AbyssSiegeLevel;
import com.aionemu.gameserver.services.mail.MailFormatter;
import com.aionemu.gameserver.services.mail.SiegeResult;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author SoulKeeper, Source
 */
public abstract class Siege<SL extends SiegeLocation> {

	private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");
	private final SiegeBossDeathListener siegeBossDeathListener = new SiegeBossDeathListener(this);
	private final SiegeBossDoAddDamageListener siegeBossDoAddDamageListener = new SiegeBossDoAddDamageListener(this);
	private final AtomicBoolean finished = new AtomicBoolean();
	private final SiegeCounter siegeCounter = new SiegeCounter();
	private final SL siegeLocation;
	private boolean bossKilled;
	private SiegeNpc boss;
	private long startTime;
	private boolean started;

	public Siege(SL siegeLocation) {
		this.siegeLocation = siegeLocation;
	}

	public final void startSiege() {

		boolean doubleStart = false;

		// keeping synchronization as minimal as possible
		synchronized (this) {
			if (started) {
				doubleStart = true;
			} else {
				startTime = System.currentTimeMillis();
				started = true;
			}
		}

		if (doubleStart) {
			log.error("Attempt to start " + this + " twice", new IllegalStateException());
		} else {
			onSiegeStart();
		}
	}

	public final void startSiege(int locationId) {
		SiegeService.getInstance().startSiege(locationId);
	}

	public final void stopSiege() {
		if (finished.compareAndSet(false, true)) {
			onSiegeFinish();

			if (SiegeConfig.BALAUR_AUTO_ASSAULT) {
				BalaurAssaultService.getInstance().onSiegeFinish(this);
			}
		} else {
			log.error("Attempt to stop " + this + " twice", new IllegalStateException());
		}
	}

	public SL getSiegeLocation() {
		return siegeLocation;
	}

	public int getSiegeLocationId() {
		return siegeLocation.getLocationId();
	}

	public boolean isBossKilled() {
		return bossKilled;
	}

	public void setBossKilled(boolean bossKilled) {
		this.bossKilled = bossKilled;
	}

	public SiegeNpc getBoss() {
		return boss;
	}

	public void setBoss(SiegeNpc boss) {
		this.boss = boss;
	}

	public SiegeCounter getSiegeCounter() {
		return siegeCounter;
	}

	protected abstract void onSiegeStart();

	protected abstract void onSiegeFinish();

	public void addBossDamage(Creature attacker, int damage) {
		// We don't have to add damage anymore if siege is finished
		if (isFinished())
			return;

		// Just to be sure that attacker exists.
		// if don't - dunno what to do
		if (attacker == null)
			return;

		// Actually we don't care if damage was done from summon.
		// We should treat all the damage like it was done from the owner
		attacker = attacker.getMaster();
		getSiegeCounter().addDamage(attacker, damage);
	}

	public abstract boolean isEndless();

	public abstract void onAbyssPointsAdded(Player player, int abyssPoints);

	public boolean isStarted() {
		return started;
	}

	public boolean isFinished() {
		return finished.get();
	}

	public long getStartTime() {
		return startTime;
	}

	protected void registerSiegeBossListeners() {
		// Add hate listener - we should know when someone attacked general
		getBoss().getAggroList().addEventListener(siegeBossDoAddDamageListener);

		// Add die listener - we should stop the siege when general dies
		getBoss().getAi().addEventListener(siegeBossDeathListener);
	}

	protected void unregisterSiegeBossListeners() {
		// Add hate listener - we should know when someone attacked general
		getBoss().getAggroList().removeEventListener(siegeBossDoAddDamageListener);

		// Add die listener - we should stop the siege when general dies
		getBoss().getAi().removeEventListener(siegeBossDeathListener);
	}

	protected void initSiegeBoss() {
		SiegeNpc boss = null;

		Collection<SiegeNpc> npcs = World.getInstance().getLocalSiegeNpcs(getSiegeLocationId());
		for (SiegeNpc npc : npcs) {
			if (npc.getObjectTemplate().getAbyssNpcType().equals(AbyssNpcType.BOSS)) {
				if (boss != null)
					throw new SiegeException("Found 2 siege bosses for outpost " + getSiegeLocationId());

				boss = npc;
			}
		}
		if (boss == null)
			throw new SiegeException("Siege Boss not found for siege " + getSiegeLocationId());

		setBoss(boss);
		registerSiegeBossListeners();
	}

	protected void spawnNpcs(int locationId, SiegeRace race, SiegeModType type) {
		SiegeService.getInstance().spawnNpcs(locationId, race, type);
	}

	protected void despawnNpcs(int locationId) {
		SiegeService.getInstance().deSpawnNpcs(locationId);
	}

	protected void broadcastState(SiegeLocation location) {
		PacketSendUtility.broadcastToWorld(new SM_SIEGE_LOCATION_STATE(location));
	}

	protected void broadcastUpdate(SiegeLocation location) {
		SiegeService.getInstance().broadcastUpdate(location);
	}

	protected void updateOutpostStatusByFortress(FortressLocation location) {
		SiegeService.getInstance().updateOutpostSiegeState(location);
	}

	protected void sendRewardsToParticipants(SiegeRaceCounter raceCounter, SiegeResult raceResult) {
		try {
			Map<Integer, Long> playerAbyssPoints = raceCounter.getPlayerAbyssPoints();
			List<Integer> topPlayersIds = new ArrayList<>(playerAbyssPoints.keySet());
			boolean isWinner = raceResult == SiegeResult.OCCUPY || raceResult == SiegeResult.DEFENDER;

			int playerIndex = 0;
			int rewardLevel = 0;
			long timeMillis = System.currentTimeMillis();
			for (SiegeReward topGrade : getSiegeLocation().getRewards()) {
				List<Integer> rewardedGpPlayers = new ArrayList<>();
				long kinahRewardForRewardLevel = getSiegeLocation().getTemplate().getKinahRewardByRewardLevel(rewardLevel);
				AbyssSiegeLevel level = AbyssSiegeLevel.getLevelById(++rewardLevel);
				int gp = isWinner ? topGrade.getGpForWin() : topGrade.getGpForDefeat();

				for (int i = 0; i < topGrade.getTop() && playerIndex < topPlayersIds.size(); i++, playerIndex++) {
					int playerId = topPlayersIds.get(playerIndex);
					long kinahReward = Math.round(kinahRewardForRewardLevel / (float) topGrade.getTop());
					if (kinahReward > 40000000)
						kinahReward = 40000000;
					if (isWinner && topGrade.hasItemRewardsForWin())
						MailFormatter.sendAbyssRewardMail(getSiegeLocation(), PlayerService.getOrLoadPlayerCommonData(playerId), level, raceResult, timeMillis,
							topGrade.getItemId(), topGrade.getItemCount(), kinahReward);
					else if (!isWinner && topGrade.hasItemRewardsForDefeat())
						MailFormatter.sendCustomAbyssDefeatRewardMail(PlayerService.getOrLoadPlayerCommonData(playerId), topGrade.getItemIdDefeat(), topGrade.getItemCountDefeat());

					if (gp > 0) {
						rewardedGpPlayers.add(playerId);
						GloryPointsService.increaseGpBy(playerId, gp);
					}
				}
				if (LoggingConfig.LOG_SIEGE && !rewardedGpPlayers.isEmpty()) {
					log.info(this + ": Distributed " + gp + " " + (isWinner ? "winner" : "loser") + " GP each, to the following players (rank " + rewardLevel
						+ "): " + rewardedGpPlayers);
				}
			}
		} catch (Exception e) {
			log.error("Error while distributing rewards for " + this, e);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [locationId=" + getSiegeLocationId() + "]";
	}
}
