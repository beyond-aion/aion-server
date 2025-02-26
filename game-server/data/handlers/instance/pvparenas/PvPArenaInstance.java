package instance.pvparenas;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.model.autogroup.AGPlayer;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.model.instance.instancescore.PvPArenaScore;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.rewards.ArenaRewardItem;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Estrayl
 */
public abstract class PvPArenaInstance extends GeneralInstanceHandler {

	private static final int START_DELAY = 120000;
	private static final int ROUND_DURATION = 180000;
	protected static final float RANK_REWARD_RATE = 0.7f;
	protected static final float SCORE_REWARD_RATE = 0.3f;

	protected int pointsPerKill = 800;
	protected int pointsPerDeath = -150;

	protected PvPArenaScore instanceScore;
	private Future<?> instanceTask;
	private boolean isInterrupted;

	public PvPArenaInstance(WorldMapInstance instance) {
		super(instance);
	}

	protected PvPArenaScore createNewArenaScore() {
		return new PvPArenaScore(instance);
	}

	protected void setScoreCaps() {
		instanceScore.setLowerScoreCap(7000);
		instanceScore.setUpperScoreCap(50000);
		instanceScore.setMaxScoreGap(43000);
	}

	@Override
	public void onInstanceCreate() {
		instanceScore = createNewArenaScore();
		setScoreCaps();
		instanceScore.setInstanceProgressionType(InstanceProgressionType.PREPARING);
		instanceScore.setInstanceStartTime();
		spawnRings();
		instanceTask = ThreadPoolManager.getInstance().schedule(this::startInstance, START_DELAY);
	}

	private void startInstance() {
		if (instanceScore.isRewarded())
			return;

		if (!canStart()) {
			isInterrupted = true;
			endInstance();
			return;
		}

		instance.forEachDoor(door -> door.setOpen(true));
		instanceScore.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
		broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_PROGRESS);

		instanceTask = ThreadPoolManager.getInstance().schedule(this::startNextRound, ROUND_DURATION);
	}

	private void startNextRound() {
		if (instanceScore.isRewarded())
			return;
		if (isFinalRound()) {
			endInstance();
			return;
		}

		instanceScore.incrementRound();
		instanceScore.setRndZone();
		broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_PROGRESS);
		changeZone();

		if (isFinalRound())
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_START_SCOREMOD());

		instanceTask = ThreadPoolManager.getInstance().schedule(this::startNextRound, ROUND_DURATION);
	}

	private void endInstance() {
		instance.forEachNpc(npc -> npc.getController().delete());
		instanceScore.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
		instanceTask = ThreadPoolManager.getInstance().schedule(() -> instance.forEachPlayer(this::leaveInstance), 60000);

		calculateRewards();
		reward();
		broadcastResults();
	}

	@Override
	public boolean onDie(Player victim, Creature lastAttacker) {
		PvPArenaPlayerReward victimReward = getStatReward(victim);
		PvPArenaPlayerReward winnerReward = null;
		if (lastAttacker != victim && lastAttacker instanceof Player winner) {
			winnerReward = getStatReward(winner);
			winnerReward.addPvPKill();
			QuestEngine.getInstance().onKillInWorld(new QuestEnv(victim, winner, 0), winner.getWorldId()); // Notify Kill-Quests
		}
		calculateAndUpdatePoints(victim, Math.round(pointsPerKill * getRunnerUpScoreMod(victimReward, winnerReward)));
		updatePoints(victimReward, victim, lastAttacker, pointsPerDeath, false); // Update victim points after rewarding the winner
		applyMoraleBoost(victim);
		return true;
	}

	@Override
	public void onDie(Npc npc) {
		int npcId = npc.getNpcId();
		if (npcId == 701187 || npcId == 701188)
			ThreadPoolManager.getInstance().schedule(this::spawnRndRelics, 30000);

		int points = getPoints(npcId);
		if (points == 0)
			return;

		calculateAndUpdatePoints(npc, points);
	}

	private void calculateAndUpdatePoints(Creature victim, int points) {
		if (!instanceScore.isStartProgress())
			return;

		int totalDamage = victim.getAggroList().getTotalDamage();
		for (AggroInfo aggroInfo : victim.getAggroList().getFinalDamageList(shouldMergeGroupDamage())) {
			if (aggroInfo.getDamage() == 0)
				continue;
			int rewardPoints = points * aggroInfo.getDamage() / totalDamage;
			if (aggroInfo.getAttacker() instanceof Creature && ((Creature) aggroInfo.getAttacker()).getMaster() instanceof Player attacker) {
				updatePoints(getStatReward(attacker), attacker, victim, rewardPoints);
			} else if (aggroInfo.getAttacker() instanceof TemporaryPlayerTeam<?> team) {
				Player leader = team.getLeaderObject();
				updatePoints(getStatReward(leader), leader, victim, rewardPoints);
				team.getOnlineMembers().stream().filter(p -> !p.equals(leader)).forEach(p -> sendSystemMsg(p, victim, rewardPoints));
			}
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		PvPArenaPlayerReward arenaReward = getStatReward(player);
		if (!instanceScore.isStartProgress() || arenaReward == null)
			return;

		int rewardPoints = getPoints(npc.getNpcId());
		int skill = instanceScore.getNpcBonusSkill(npc.getNpcId());
		if (skill != 0)
			useSkill(npc, player, skill >> 8, skill & 0xFF);

		updatePoints(arenaReward, player, npc, rewardPoints);
	}

	protected void updatePoints(PvPArenaPlayerReward receiver, Player player, VisibleObject victim, int rewardPoints) {
		updatePoints(receiver, player, victim, rewardPoints, true);
	}

	protected void updatePoints(PvPArenaPlayerReward receiver, Player player, VisibleObject victim, int rewardPoints, boolean broadcastPackets) {
		if (receiver == null)
			return;

		receiver.addPoints(rewardPoints, instanceScore);

		if (broadcastPackets) {
			broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_BUFFS_AND_SCORE);
			broadcastUpdate(player, InstanceScoreType.UPDATE_PLAYER_BUFF_STATUS);
		}
		sendSystemMsg(player, victim, rewardPoints);

		if (instanceScore.reachedScoreGap())
			endInstance();
	}

	protected boolean isFinalRound() {
		return instanceScore.getRound() == 3;
	}

	protected void sendSystemMsg(Player player, VisibleObject creature, int rewardPoints) {
		if (rewardPoints <= 0) // Checked with retail, only net gain is sent as msg
			return;
		if (creature instanceof Player)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE_FOR_ENEMY(rewardPoints));
		else
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(creature.getObjectTemplate().getL10n(), rewardPoints));
	}

	private int calcAndFloor(int number, float rate) {
		return (int) (number * rate);
	}

	private int calcAndFloor(int number, float rate, float configRate) {
		return (int) (number * rate * configRate);
	}

	protected Collection<PvPArenaPlayerReward> getArenaRewards() {
		return instanceScore.getPlayerRewards();
	}

	private int getPoints(int npcId) {
		switch (npcId) {
			case 701187: // Blessed Relics
			case 701188: // Cursed Relics
				return 1750;
			case 218690: // Pale Carmina
			case 218703: // Pale Carmina
			case 218716: // Pale Carmina
			case 218691: // Corrupt Casus
			case 218704: // Corrupt Casus
			case 218717: // Corrupt Casus
				return 1500;
			case 218682: // MuMu Rake Gatherer
			case 218695: // MuMu Rake Gatherer
			case 218708: // MuMu Rake Gatherer
				return 1250;
			case 701181: // Cursed Relics
			case 701195: // Cursed Relics
			case 701209: // Cursed Relics
			case 218689: // Casus Manor Noble
			case 218702: // Casus Manor Noble
			case 218715: // Casus Manor Noble
				return 750;
			case 218701: // Casus Manor Butler
			case 218688: // Casus Manor Butler
			case 218714: // Casus Manor Butler
				return 650;
			case 701172: // Plaza Flame Thrower
			case 701171: // Plaza Flame Thrower
			case 701170: // Plaza Flame Thrower
			case 701169: // Plaza Flame Thrower
			case 218806: // Casus Manor Chief Maid
			case 218807: // Casus Manor Chief Maid
			case 218808: // Casus Manor Chief Maid
			case 701317: // Blessed Relics
			case 701318: // Blessed Relics
			case 701319: // Blessed Relics
				return 500;
			case 207102: // Recovery Relics
			case 207116: // 1st Floor Stunner
			case 219277: // Roaming Volcanic Petrahulk Lv 50
			case 219278: // Roaming Volcanic Petrahulk Lv 55
			case 219279: // Roaming Volcanic Petrahulk Lv 60
			case 219481: // Volcanic Heart Crystal Lv 50
			case 219485: // Volcanic Heart Crystal Lv 55
			case 219486: // Volcanic Heart Crystal Lv 60
			case 219648: // Roaming Volcanic Petrahulk Lv 65
			case 219652: // Volcanic Heart Crystal Lv 65
				return 400;
			case 218685: // Casus Manor Guard
			case 218698: // Casus Manor Guard
			case 218711: // Casus Manor Guard
			case 218687: // Casus Manor Maid
			case 218700: // Casus Manor Maid
			case 218713: // Casus Manor Maid
			case 218686: // Casus Manor Maidservant
			case 218699: // Casus Manor Maidservant
			case 218712: // Casus Manor Maidservant
				return 250;
			case 207099: // 2nd Floor Bomb
				return 200;
			case 218683: // Black Claw Scratcher
			case 218696: // Black Claw Scratcher
			case 218709: // Black Claw Scratcher
			case 218684: // Mutated Drakan Fighter
			case 218697: // Mutated Drakan Fighter
			case 218710: // Mutated Drakan Fighter
			case 218693: // Red Sand Tog
			case 218706: // Red Sand Tog
			case 218719: // Red Sand Tog
			case 219280: // Heated Negotiator Grangvolkan Lv 50
			case 219281: // Heated Negotiator Grangvolkan Lv 55
			case 219282: // Heated Negotiator Grangvolkan Lv 60
			case 219649: // Heated Negotiator Grangvolkan Lv 65
			case 233058: // Red Sand Tog
			case 701215: // Blesed Relic
			case 701220: // Blesed Relic
			case 701225: // Blesed Relic
			case 701216: // Blesed Relic
			case 701221: // Blesed Relic
			case 701226: // Blesed Relic
				return 100;
			case 219283: // Lurking Fangwing Lv 50
			case 219284: // Lurking Fangwing Lv 55
			case 219285: // Lurking Fangwing Lv 60
			case 219650: // Lurking Fangwing Lv 65
			case 219328: // Plaza Wall
				return 50;
			default:
				return 0;
		}
	}

	private void changeZone() {
		ThreadPoolManager.getInstance().schedule(() -> {
			for (Player player : instance.getPlayersInside()) {
				if (player.isDead()) {
					PlayerReviveService.revive(player, 100, 100, false, 0);
					player.getGameStats().updateStatsAndSpeedVisually();
				}
				instanceScore.portToPosition(player);
				broadcastUpdate(player, InstanceScoreType.UPDATE_PLAYER_BUFF_STATUS);
			}
		}, 1000);
	}

	@Override
	public void onEnterInstance(Player player) {
		if (instanceScore.regPlayerReward(player)) {
			instanceScore.setRndPosition(player.getObjectId());
		} else {
			instanceScore.portToPosition(player);
			getPlayerSpecificReward(player).updateBonusTime();
		}
		sendEntryPacket(player);
	}

	@Override
	public void onPlayerLogout(Player player) {
		getPlayerSpecificReward(player).updateLogoutTime();
	}

	private void clearDebuffs(Player player) {
		for (Effect ef : player.getEffectController().getAbnormalEffects()) {
			switch (ef.getSkillTemplate().getDispelCategory()) {
				case DEBUFF, DEBUFF_MENTAL, DEBUFF_PHYSICAL, ALL -> ef.endEffect();
			}
		}
	}

	protected void useSkill(Npc npc, Player player, int skillId, int level) {
		SkillEngine.getInstance().getSkill(npc, skillId, level, player).useNoAnimationSkill();
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		instanceScore.portToPosition(player);
		return true;
	}

	@Override
	public void leaveInstance(Player player) {
		if (player.getWorldMapInstance().equals(instance))
			TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	@Override
	public void onLeaveInstance(Player player) {
		super.onLeaveInstance(player);
		clearDebuffs(player);
		PvPArenaPlayerReward playerReward = getPlayerSpecificReward(player);
		if (playerReward != null) {
			endMoraleBoost(player);
			instanceScore.clearPosition(playerReward.getPosition(), Boolean.FALSE);
			instanceScore.removePlayerReward(playerReward);
			broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_BUFFS_AND_SCORE);
		}
	}

	@Override
	public void onInstanceDestroy() {
		instanceScore.clear();
		if (instanceTask != null && !instanceTask.isCancelled())
			instanceTask.cancel(true);
	}

	private void applyMoraleBoost(Player player) {
		PvPArenaPlayerReward reward = getPlayerSpecificReward(player);
		if (reward == null)
			return;

		reward.applyBoostMoraleEffect(player, getBoostMoraleEffectDuration(instanceScore.getRank(reward)));
		broadcastUpdate(player, InstanceScoreType.UPDATE_PLAYER_BUFF_STATUS);
	}

	private void endMoraleBoost(Player player) {
		PvPArenaPlayerReward reward = getPlayerSpecificReward(player);
		if (reward != null && reward.hasBoostMorale()) {
			reward.endBoostMoraleEffect(player);
		}
		broadcastUpdate(player, InstanceScoreType.UPDATE_PLAYER_BUFF_STATUS);
	}

	private void spawnRndRelics() {
		if (instanceScore != null && !instanceScore.isRewarded())
			spawn(Rnd.get(1, 2) == 1 ? 701187 : 701188, 1841.951f, 1733.968f, 300.242f, (byte) 0);
	}

	@Override
	public InstanceScore<?> getInstanceScore() {
		return instanceScore;
	}

	/**
	 * Overridden in HarmonyTrainingGroundsInstance to differentiate between player and group specific reward objects
	 */
	protected PvPArenaPlayerReward getStatReward(Player player) {
		return getPlayerSpecificReward(player);
	}

	protected PvPArenaPlayerReward getPlayerSpecificReward(Player player) {
		return instanceScore.getPlayerReward(player.getObjectId());
	}

	protected boolean canStart() {
		return instance.getPlayersInside().size() > 1;
	}

	protected void spawnRings() {
	}

	protected float getRunnerUpScoreMod(PvPArenaPlayerReward victim, PvPArenaPlayerReward winner) {
		if (winner == null || !isFinalRound())
			return 1f;

		int victimRank = instanceScore.getRank(victim);
		return victimRank < instanceScore.getRank(winner) ? getRunnerUpScoreMod(victimRank) : 1f;
	}

	protected int getBoostMoraleEffectDuration(int rank) {
		return 15000; // Retail Default
	}

	protected float getRunnerUpScoreMod(int victimRank) {
		return 3f;
	}

	protected boolean shouldMergeGroupDamage() {
		return false;
	}

	protected BaseRewards getBaseRewardsPerPlayer(int difficultyId) {
		return new BaseRewards(0, 0, 0, 0);
	}

	protected BaseRewards getBaseRewards(int difficultyId) {
		return new BaseRewards(0, 0, 0, 0);
	}

	protected float getRewardRate(int rank, int difficultyId) {
		return 0f;
	}

	protected void setRewardItems(PvPArenaPlayerReward reward, int rank, int difficultyId) {
	}

	protected float getConfigRate(Player player) {
		return 1f;
	}

	protected void calculateRewards() {
		int difficultyId = instanceScore.getDifficultyId();
		BaseRewards baseValues = getBaseRewardsPerPlayer(difficultyId);

		int playerCount = instanceScore.getPlayerRewards().size();
		int totalAp = baseValues.ap() * playerCount;
		int totalGp = baseValues.gp() * playerCount;
		int totalCrucibleInsignia = baseValues.crucibleInsignia() * playerCount;
		int totalCourageInsignia = baseValues.courageInsignia() * playerCount;

		int rankAp = calcAndFloor(totalAp, RANK_REWARD_RATE);
		int rankGp = calcAndFloor(totalGp, RANK_REWARD_RATE);
		int rankCrucibleInsignia = calcAndFloor(totalCrucibleInsignia, RANK_REWARD_RATE);
		int rankCourageInsignia = calcAndFloor(totalCourageInsignia, RANK_REWARD_RATE);

		int scoreAp = calcAndFloor(totalAp, SCORE_REWARD_RATE);
		int scoreGp = calcAndFloor(totalGp, SCORE_REWARD_RATE);
		int scoreCrucibleInsignia = calcAndFloor(totalCrucibleInsignia, SCORE_REWARD_RATE);
		int scoreCourageInsignia = calcAndFloor(totalCourageInsignia, SCORE_REWARD_RATE);

		int totalPoints = instanceScore.getTotalPoints();

		for (PvPArenaPlayerReward reward : getArenaRewards()) {
			if (reward.isRewarded())
				continue;

			Player player = instance.getPlayer(reward.getOwnerId());
			if (!(reward instanceof HarmonyGroupReward) && player == null) // player left
				continue;

			float configRate = getConfigRate(player);

			int score = reward.getScorePoints();
			// Score Formula to verify: floor(scoreAp * winnerKills * winnerPoints / (winnerKills * winnerPoints + loserKills * loserPoints))
			float scoreRate = score / (float) totalPoints;

			int rank = instanceScore.getRank(reward);
			if (isInterrupted)
				rank = reward instanceof HarmonyGroupReward ? 1 : instance.getMaxPlayers() - 1;
			float rankRewardRate = getRewardRate(rank, difficultyId);

			int rankRewardAp = calcAndFloor(rankAp, rankRewardRate, configRate);
			int rankRewardGp = calcAndFloor(rankGp, rankRewardRate, 1f);
			int rankRewardCrucibleInsignia = calcAndFloor(rankCrucibleInsignia, rankRewardRate, configRate);
			int rankRewardCourageInsignia = calcAndFloor(rankCourageInsignia, rankRewardRate, configRate);

			int scoreRewardAp = calcAndFloor(scoreAp, scoreRate, configRate);
			int scoreRewardGp = calcAndFloor(scoreGp, scoreRate, 1f);
			int scoreRewardCrucibleInsignia = calcAndFloor(scoreCrucibleInsignia, scoreRate, configRate);
			int scoreRewardCourageInsignia = calcAndFloor(scoreCourageInsignia, scoreRate, configRate);

			BaseRewards baseRewards = getBaseRewards(difficultyId);
			reward.setAp(new ArenaRewardItem(0, baseRewards.ap(), rankRewardAp, scoreRewardAp));
			reward.setGp(new ArenaRewardItem(0, baseRewards.gp(), rankRewardGp, scoreRewardGp));

			ArenaRewardItem crucibleInsignia = new ArenaRewardItem(186000130, baseRewards.crucibleInsignia(), rankRewardCrucibleInsignia,
				scoreRewardCrucibleInsignia);
			ArenaRewardItem courageInsignia = new ArenaRewardItem(186000137, baseRewards.courageInsignia(), rankRewardCourageInsignia,
				scoreRewardCourageInsignia);

			if (crucibleInsignia.getTotalCount() > 0)
				reward.setCrucibleInsignia(crucibleInsignia);
			if (courageInsignia.getTotalCount() > 0)
				reward.setCourageInsignia(courageInsignia);

			setRewardItems(reward, rank, difficultyId);

			if (reward instanceof HarmonyGroupReward harmonyGroupReward) {
				splitGroupRewards(harmonyGroupReward);
			}
		}
	}

	private void splitGroupRewards(HarmonyGroupReward groupReward) {
		List<AGPlayer> associatedPlayers = groupReward.getAssociatedPlayers();
		if (associatedPlayers.isEmpty())
			return;

		int size = associatedPlayers.size();

		for (AGPlayer agPlayer : associatedPlayers) {
			PvPArenaPlayerReward apr = instanceScore.getPlayerReward(agPlayer.getObjectId());
			Player player = instance.getPlayer(agPlayer.getObjectId());
			if (apr == null || player == null)
				continue;

			float configRate = getConfigRate(player);
			apr.setAp(calculateIndividualReward(groupReward.getAp(), size, configRate));
			apr.setGp(calculateIndividualReward(groupReward.getGp(), size, 1f));
			apr.setCrucibleInsignia(calculateIndividualReward(groupReward.getCrucibleInsignia(), size, configRate));
			apr.setCourageInsignia(calculateIndividualReward(groupReward.getCourageInsignia(), size, configRate));
			apr.setRewardItem1(groupReward.getRewardItem1());
			apr.setRewardItem2(groupReward.getRewardItem2());
		}
	}

	private ArenaRewardItem calculateIndividualReward(ArenaRewardItem rewardItem, int size, float configRate) {
		int baseCount = Math.round(rewardItem.baseCount() * configRate / size);
		int rankingCount = Math.round(rewardItem.rankingCount() * configRate / size);
		int scoreCount = Math.round(rewardItem.scoreCount() * configRate / size);
		return new ArenaRewardItem(rewardItem.itemId(), baseCount, rankingCount, scoreCount);
	}

	private void reward() {
		for (Player player : instance.getPlayersInside()) {
			if (player.isDead())
				PlayerReviveService.duelRevive(player);
			PvPArenaPlayerReward reward = getPlayerSpecificReward(player);
			if (!reward.isRewarded()) {
				reward.setRewarded();

				ArenaRewardItem apReward = reward.getAp();
				if (apReward.getTotalCount() > 0)
					AbyssPointsService.addAp(player, apReward.getTotalCount());

				ArenaRewardItem gpReward = reward.getGp();
				if (gpReward.getTotalCount() > 0)
					GloryPointsService.increaseGpBy(player.getObjectId(), gpReward.getTotalCount(), false, true);

				ArenaRewardItem courageInsigniaReward = reward.getCourageInsignia();
				if (courageInsigniaReward.getTotalCount() > 0)
					ItemService.addItem(player, courageInsigniaReward.itemId(), courageInsigniaReward.getTotalCount());

				ArenaRewardItem crucibleInsigniaReward = reward.getCrucibleInsignia();
				if (crucibleInsigniaReward.getTotalCount() > 0)
					ItemService.addItem(player, crucibleInsigniaReward.itemId(), crucibleInsigniaReward.getTotalCount());

				RewardItem rewardItem1 = reward.getRewardItem1();
				if (rewardItem1 != null)
					ItemService.addItem(player, rewardItem1.getId(), rewardItem1.getCount());

				RewardItem rewardItem2 = reward.getRewardItem2();
				if (rewardItem2 != null)
					ItemService.addItem(player, rewardItem2.getId(), rewardItem2.getCount());
			}
		}
	}

	protected Npc getNpc(float x, float y, float z) {
		for (Npc npc : instance.getNpcs()) {
			SpawnTemplate st = npc.getSpawn();
			if (st.getX() == x && st.getY() == y && st.getZ() == z)
				return npc;
		}
		return null;
	}

	protected void sendEntryPacket(Player player) {
		sendPacket(null, null);
	}

	protected void broadcastUpdate(Player target, InstanceScoreType scoreType) {
		sendPacket(null, null);
	}

	protected void broadcastUpdate(InstanceScoreType scoreType) {
		sendPacket(null, null);
	}

	protected void broadcastResults() {
		instance.forEachPlayer(player -> sendPacket(player, InstanceScoreType.SHOW_REWARD));
	}

	protected void sendPacket(Player receiver, InstanceScoreType scoreType) {
	}

	@Override
	public boolean allowSelfReviveBySkill() {
		return false;
	}

	@Override
	public boolean allowSelfReviveByItem() {
		return false;
	}

	protected record BaseRewards(int ap, int gp, int crucibleInsignia, int courageInsignia) {}
}
