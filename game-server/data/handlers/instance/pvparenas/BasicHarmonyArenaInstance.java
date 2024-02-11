package instance.pvparenas;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.util.concurrent.Future;

import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancescore.HarmonyArenaScore;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.network.aion.instanceinfo.HarmonyScoreWriter;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
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
 * @author xTz
 */
public class BasicHarmonyArenaInstance extends GeneralInstanceHandler {

	private static final int START_DELAY = 120000;
	private static final int ROUND_DURATION = 180000;
	private static final int POINTS_PER_PLAYER_KILL = 800;
	private static final int POINTS_PER_DEATH = -150;
	private static final int RUNNER_UP_SCORE_MOD = 3;

	protected HarmonyArenaScore instanceReward;
	private Future<?> instanceTask;

	public BasicHarmonyArenaInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public InstanceScore<?> getInstanceScore() {
		return instanceReward;
	}

	@Override
	public void onInstanceCreate() {
		instanceReward = new HarmonyArenaScore(instance);
		instanceReward.setLowerScoreCap(7000);
		instanceReward.setUpperScoreCap(50000);
		instanceReward.setMaxScoreGap(43000);
		instanceReward.setInstanceProgressionType(InstanceProgressionType.PREPARING);
		instanceReward.setInstanceStartTime();
		spawnRings();
		instanceTask = ThreadPoolManager.getInstance().schedule(this::startInstance, START_DELAY);
	}

	private void startInstance() {
		if (instanceReward.isRewarded())
			return;

		if (instanceReward.getHarmonyGroupInside().size() < 2) {
			endInstance();
			return;
		}

		instance.forEachDoor(door -> door.setOpen(true));
		instanceReward.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
		broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_PROGRESS);

		instanceTask = ThreadPoolManager.getInstance().schedule(this::startNextRound, ROUND_DURATION);
	}

	private void startNextRound() {
		if (instanceReward.isRewarded())
			return;
		if (instanceReward.getRound() == 3) {
			endInstance();
			return;
		}

		instanceReward.incrementRound();
		instanceReward.setRndZone();
		broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_PROGRESS);
		changeZone();

		if (isFinalRound())
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_START_SCOREMOD());

		instanceTask = ThreadPoolManager.getInstance().schedule(this::startNextRound, ROUND_DURATION);
	}

	private void endInstance() {
		instance.forEachNpc(npc -> npc.getController().delete());
		instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
		instanceTask = ThreadPoolManager.getInstance().schedule(() -> instance.forEachPlayer(this::onExitInstance), 60000);
		reviveAllPlayers();

		reward();
		broadcastResults();
	}

	@Override
	public boolean onDie(Player victim, Creature lastAttacker) {
		endMoraleBoost(victim);
		PacketSendUtility.sendPacket(victim, new SM_DIE(false, false, 0, 8));

		HarmonyGroupReward victimGroup = instanceReward.getHarmonyGroupReward(victim.getObjectId());
		updatePoints(victimGroup, victim, lastAttacker, POINTS_PER_DEATH);

		if (lastAttacker != victim && lastAttacker instanceof Player winner) {
			HarmonyGroupReward winnerGroup = instanceReward.getHarmonyGroupReward(winner.getObjectId());
			winnerGroup.addPvPKill();

			int scoreBonus = isFinalRound() && instanceReward.getRank(winnerGroup.getPoints()) == 1 ? RUNNER_UP_SCORE_MOD : 1;
			updatePoints(winnerGroup, winner, victim, POINTS_PER_PLAYER_KILL * scoreBonus);

			// notify Kill-Quests
			int worldId = winner.getWorldId();
			QuestEngine.getInstance().onKillInWorld(new QuestEnv(victim, winner, 0), worldId);
		}
		return true;
	}

	@Override
	public void onDie(Npc npc) {
		if (!instanceReward.isStartProgress() || npc.getAggroList().getMostPlayerDamage() == null)
			return;

		int bonus = getNpcBonus(npc.getNpcId());
		if (bonus == 0)
			return;

		int totalDamage = npc.getAggroList().getTotalDamage();
		// Reward all damagers
		for (AggroInfo aggroInfo : npc.getAggroList().getFinalDamageList(false)) {
			if (aggroInfo.getDamage() == 0)
				continue;
			if (!(aggroInfo.getAttacker() instanceof Creature))
				continue;
			if (((Creature) aggroInfo.getAttacker()).getMaster() instanceof Player attacker) {
				int rewardPoints = bonus * aggroInfo.getDamage() / totalDamage;
				HarmonyGroupReward receiver = instanceReward.getHarmonyGroupReward(attacker.getObjectId());
				updatePoints(receiver, attacker, npc, rewardPoints);
			}
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		HarmonyGroupReward group = instanceReward.getHarmonyGroupReward(player.getObjectId());
		if (!instanceReward.isStartProgress() || group == null)
			return;

		int rewardPoints = getNpcBonus(npc.getNpcId());
		int skill = instanceReward.getNpcBonusSkill(npc.getNpcId());
		if (skill != 0)
			useSkill(npc, player, skill >> 8, skill & 0xFF);

		updatePoints(group, player, npc, rewardPoints);
	}

	protected void updatePoints(HarmonyGroupReward receiver, Player player, Creature victim, int rewardPoints) {
		if (receiver == null)
			return;

		receiver.addPoints(rewardPoints);
		broadcastUpdate(player, InstanceScoreType.UPDATE_RANK);
		broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_BUFFS_AND_SCORE);
		sendSystemMsg(player, victim, rewardPoints);

		if (instanceReward.reachedScoreCap())
			endInstance();
	}

	private boolean isFinalRound() {
		return instanceReward.getRound() == 3;
	}

	protected void sendSystemMsg(Player player, Creature creature, int rewardPoints) {
		if (creature instanceof Player)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE_FOR_ENEMY(rewardPoints));
		else
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(creature.getObjectTemplate().getL10n(), rewardPoints));
	}

	private int getNpcBonus(int npcId) {
		switch (npcId) {
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
			case 207099: // 2nd Floor Bomb
				return 200;
			case 219280: // Heated Negotiator Grangvolkan Lv 50
			case 219281: // Heated Negotiator Grangvolkan Lv 55
			case 219282: // Heated Negotiator Grangvolkan Lv 60
			case 219649: // Heated Negotiator Grangvolkan Lv 65
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
				instanceReward.portToPosition(player);
				broadcastUpdate(player, InstanceScoreType.UPDATE_PLAYER_BUFF_STATUS);
			}
		}, 1000);
	}

	@Override
	public void onEnterInstance(final Player player) {
		int objectId = player.getObjectId();
		if (instanceReward.regPlayerReward(player)) {
			applyMoraleBoost(player);
			instanceReward.setRndPosition(objectId);
		} else {
			instanceReward.portToPosition(player);
		}
		sendEnterPacket(player);
	}

	@Override
	public void onPlayerLogin(Player player) {
		sendEnterPacket(player);
	}

	@Override
	public void onExitInstance(Player player) {
		if (player.getPosition().getMapId() != mapId) // Check if player has not already left
			return;
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	private void clearDebuffs(Player player) {
		for (Effect ef : player.getEffectController().getAbnormalEffects()) {
			switch (ef.getSkillTemplate().getDispelCategory()) {
				case DEBUFF, DEBUFF_MENTAL, DEBUFF_PHYSICAL, ALL -> {
					ef.endEffect();
					player.getEffectController().clearEffect(ef);
				}
			}
		}
	}

	protected void spawnRings() {
	}

	protected void reward() {
		if (instanceReward.canRewarded()) {
			for (Player player : instance.getPlayersInside()) {
				HarmonyGroupReward group = instanceReward.getHarmonyGroupReward(player.getObjectId());
				float playerRate = Rates.get(player, RatesConfig.PVP_ARENA_HARMONY_REWARD_RATES);
				AbyssPointsService.addAp(player, group.getBasicAP() + group.getRankingAP() + (int) (group.getScoreAP() * playerRate));
				GloryPointsService.increaseGpBy(player.getObjectId(), group.getBasicGP() + group.getRankingGP() + group.getScoreGP());
				int courage = group.getBasicCourage() + group.getRankingCourage() + group.getScoreCourage();
				if (courage != 0) {
					ItemService.addItem(player, 186000137, Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(player, courage));
				}

				RewardItem rewardItem1 = group.getRewardItem1();
				if (rewardItem1 != null) {
					ItemService.addItem(player, rewardItem1.getId(), rewardItem1.getCount());
				}
				RewardItem rewardItem2 = group.getRewardItem2();
				if (rewardItem2 != null) {
					ItemService.addItem(player, rewardItem2.getId(), rewardItem2.getCount());
				}
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
		instanceReward.portToPosition(player);
		applyMoraleBoost(player);
		return true;
	}

	@Override
	public void onLeaveInstance(Player player) {
		clearDebuffs(player);
		PvPArenaPlayerReward playerReward = instanceReward.getPlayerReward(player.getObjectId());
		if (playerReward != null) {
			playerReward.endBoostMoraleEffect(player);
			instanceReward.clearPosition(playerReward.getPosition(), Boolean.FALSE);
			instanceReward.removePlayerReward(playerReward);
			broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_BUFFS_AND_SCORE);
		}
	}

	@Override
	public void onInstanceDestroy() {
		instanceReward.clear();
		if (instanceTask != null && !instanceTask.isCancelled())
			instanceTask.cancel(true);
	}

	private void applyMoraleBoost(Player player) {
		PvPArenaPlayerReward reward = instanceReward.getPlayerReward(player.getObjectId());
		if (reward == null)
			return;

		instanceReward.getPlayerReward(player.getObjectId()).applyBoostMoraleEffect(player);
		broadcastUpdate(player, InstanceScoreType.UPDATE_PLAYER_BUFF_STATUS);
	}

	private void endMoraleBoost(Player player) {
		PvPArenaPlayerReward reward = instanceReward.getPlayerReward(player.getObjectId());
		if (reward == null)
			return;

		instanceReward.getPlayerReward(player.getObjectId()).endBoostMoraleEffect(player);
		broadcastUpdate(player, InstanceScoreType.UPDATE_PLAYER_BUFF_STATUS);
	}

	private void reviveAllPlayers() {
		instance.forEachPlayer(p -> {
			if (p.isDead())
				PlayerReviveService.duelRevive(p);
		});
	}

	private void sendEnterPacket(final Player player) {
		broadcastUpdate(player, InstanceScoreType.UPDATE_RANK);
		broadcastUpdate(player, InstanceScoreType.INIT_PLAYER);
		broadcastUpdate(player, InstanceScoreType.UPDATE_PLAYER_BUFF_STATUS);
		broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_BUFFS_AND_SCORE);
	}

	protected void broadcastUpdate(Player target, InstanceScoreType scoreType) {
		PacketSendUtility.broadcastToMap(instance,
			new SM_INSTANCE_SCORE(instance.getMapId(), new HarmonyScoreWriter(instanceReward, scoreType, target), getTime()));
	}

	protected void broadcastUpdate(InstanceScoreType scoreType) {
		PacketSendUtility.broadcastToMap(instance,
			new SM_INSTANCE_SCORE(instance.getMapId(), new HarmonyScoreWriter(instanceReward, scoreType), getTime()));
	}

	protected void broadcastResults() {
		instance.forEachPlayer(player -> sendPacket(player, InstanceScoreType.SHOW_REWARD));
	}

	protected void sendPacket(Player receiver, InstanceScoreType scoreType) {
		PacketSendUtility.sendPacket(receiver,
			new SM_INSTANCE_SCORE(instance.getMapId(), new HarmonyScoreWriter(instanceReward, scoreType, receiver), getTime()));
	}

	private int getTime() {
		return instanceReward.getTime();
	}
}
