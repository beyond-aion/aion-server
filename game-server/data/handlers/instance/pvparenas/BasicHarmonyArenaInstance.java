package instance.pvparenas;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

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
import com.aionemu.gameserver.skillengine.model.DispelCategoryType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public class BasicHarmonyArenaInstance extends GeneralInstanceHandler {

	private static final int POINTS_GAINED_PER_PLAYER_KILL = 800;
	private static final int POINTS_LOST_PER_DEATH = -150;
	protected HarmonyArenaScore instanceReward;
	protected boolean isInstanceDestroyed;

	public BasicHarmonyArenaInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public InstanceScore<?> getInstanceScore() {
		return instanceReward;
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

	private void sendEnterPacket(final Player player) {
		broadcastUpdate(player, InstanceScoreType.UPDATE_RANK);
		broadcastUpdate(player, InstanceScoreType.INIT_PLAYER);
		broadcastUpdate(player, InstanceScoreType.UPDATE_PLAYER_BUFF_STATUS);
		broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_BUFFS_AND_SCORE);
	}

	private void updatePoints(Creature victim) {

		if (!instanceReward.isStartProgress()) {
			return;
		}

		int bonus;
		int rank = 0;

		// Decrease victim points
		if (victim instanceof Player) {
			HarmonyGroupReward victimGroup = instanceReward.getHarmonyGroupReward(victim.getObjectId());
			victimGroup.addPoints(POINTS_LOST_PER_DEATH);
			bonus = POINTS_GAINED_PER_PLAYER_KILL;
			rank = instanceReward.getRank(victimGroup.getPoints());
			broadcastUpdate((Player) victim, InstanceScoreType.UPDATE_RANK);
		} else
			bonus = getNpcBonus(((Npc) victim).getNpcId());

		if (bonus == 0)
			return;

		if (victim instanceof Player && instanceReward.getRound() == 3 && rank == 0)
			bonus *= 3;

		int totalDamage = victim.getAggroList().getTotalDamage();
		// Reward all damagers
		for (AggroInfo aggroInfo : victim.getAggroList().getFinalDamageList(false)) {
			if (aggroInfo.getDamage() == 0)
				continue;
			if (!(aggroInfo.getAttacker() instanceof Creature))
				continue;
			if (((Creature) aggroInfo.getAttacker()).getMaster() instanceof Player attacker) {
				int rewardPoints = bonus * aggroInfo.getDamage() / totalDamage;
				instanceReward.getHarmonyGroupReward(attacker.getObjectId()).addPoints(rewardPoints);
				sendSystemMsg(attacker, victim, rewardPoints);
				broadcastUpdate(attacker, InstanceScoreType.UPDATE_RANK);
			}
		}
		broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_BUFFS_AND_SCORE);
		if (instanceReward.hasCapPoints()) {
			instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
			reward();
			broadcastResults();
		}
	}

	@Override
	public void onDie(Npc npc) {
		if (npc.getAggroList().getMostPlayerDamage() == null) {
			return;
		}
		updatePoints(npc);
	}

	protected void sendSystemMsg(Player player, Creature creature, int rewardPoints) {
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(creature.getObjectTemplate().getL10n(), rewardPoints));
	}

	private int getNpcBonus(int npcId) {
		switch (npcId) {
			case 207102: // Recovery Relics
			case 207116: // 1st Floor Stunner
			case 219279: // Roaming Volcanic Petrahulk
			case 219481: // Volcanic Heart Crystal
				return 400;
			case 207099: // 2nd Floor Bomb
				return 200;
			case 219282: // Heated Negotiator Grangvolkan
				return 100;
			case 219285: // Lurking Fangwing
			case 219328: // Plaza Wall
				return 50;
			default:
				return 0;
		}
	}

	@Override
	public void onPlayerLogin(Player player) {
		sendEnterPacket(player);
	}

	@Override
	public void onInstanceCreate() {
		instanceReward = new HarmonyArenaScore(instance);
		instanceReward.setInstanceProgressionType(InstanceProgressionType.PREPARING);
		instanceReward.setInstanceStartTime();
		spawnRings();
		ThreadPoolManager.getInstance().schedule(() -> {
			// start round 1
			if (!isInstanceDestroyed && !instanceReward.isRewarded() && canStart()) {
				instance.forEachDoor(door -> door.setOpen(true));
				sendMsg(new SM_SYSTEM_MESSAGE(1401058));
				instanceReward.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
				broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_PROGRESS);
				ThreadPoolManager.getInstance().schedule(() -> {
					// start round 2
					if (!isInstanceDestroyed && !instanceReward.isRewarded()) {
						instanceReward.setRound(2);
						instanceReward.setRndZone();
						broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_PROGRESS);
						changeZone();
						ThreadPoolManager.getInstance().schedule(() -> {
							// start round 3
							if (!isInstanceDestroyed && !instanceReward.isRewarded()) {
								instanceReward.setRound(3);
								instanceReward.setRndZone();
								sendMsg(new SM_SYSTEM_MESSAGE(1401203));
								broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_PROGRESS);
								changeZone();
								ThreadPoolManager.getInstance().schedule(() -> {
									// end
									if (!isInstanceDestroyed && !instanceReward.isRewarded()) {
										instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
										reward();
										broadcastResults();
									}
								}, 180000);

							}
						}, 180000);
					}
				}, 180000);
			}
		}, 120000);
	}

	protected void spawnRings() {
	}

	private boolean canStart() {
		if (instanceReward.getHarmonyGroupInside().size() < 2) {
			sendMsg(new SM_SYSTEM_MESSAGE(1401303));
			instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
			reward();
			broadcastResults();
			return false;
		}
		return true;
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
	public void onExitInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	private void clearDebuffs(Player player) {
		for (Effect ef : player.getEffectController().getAbnormalEffects()) {
			DispelCategoryType category = ef.getSkillTemplate().getDispelCategory();
			if (category == DispelCategoryType.DEBUFF || category == DispelCategoryType.DEBUFF_MENTAL || category == DispelCategoryType.DEBUFF_PHYSICAL
				|| category == DispelCategoryType.ALL) {
				ef.endEffect();
				player.getEffectController().clearEffect(ef);
			}
		}
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
		instance.forEachNpc(npc -> npc.getController().delete());
		ThreadPoolManager.getInstance().schedule(this::cleanUp, 60000);
	}

	private void cleanUp() {
		if (!isInstanceDestroyed) {
			for (Player player : instance.getPlayersInside()) {
				if (player.isDead())
					PlayerReviveService.duelRevive(player);
				onExitInstance(player);
			}
		}
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		endMoraleBoost(player);
		broadcastUpdate(player, InstanceScoreType.UPDATE_PLAYER_BUFF_STATUS);
		PacketSendUtility.sendPacket(player, new SM_DIE(false, false, 0, 8));

		if (lastAttacker != null && lastAttacker != player) {
			if (lastAttacker instanceof Player winner) {
				int winnerObj = winner.getObjectId();
				instanceReward.getHarmonyGroupReward(winnerObj).addPvPKillToPlayer();
				// notify Kill-Quests
				int worldId = winner.getWorldId();
				QuestEngine.getInstance().onKillInWorld(new QuestEnv(player, winner, 0), worldId);
			}
		}
		updatePoints(player);
		return true;
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		int objectId = player.getObjectId();
		HarmonyGroupReward group = instanceReward.getHarmonyGroupReward(objectId);
		if (!instanceReward.isStartProgress() || group == null) {
			return;
		}
		int rewardetPoints = getNpcBonus(npc.getNpcId());
		int skill = instanceReward.getNpcBonusSkill(npc.getNpcId());
		if (skill != 0) {
			useSkill(npc, player, skill >> 8, skill & 0xFF);
		}
		group.addPoints(rewardetPoints);
		sendSystemMsg(player, npc, rewardetPoints);
		broadcastUpdate(player, InstanceScoreType.UPDATE_RANK);
		broadcastUpdate(InstanceScoreType.UPDATE_INSTANCE_BUFFS_AND_SCORE);
	}

	protected void useSkill(Npc npc, Player player, int skillId, int level) {
		SkillEngine.getInstance().getSkill(npc, skillId, level, player).useNoAnimationSkill();
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		if (!isInstanceDestroyed) {
			instanceReward.portToPosition(player);
			applyMoraleBoost(player);
		}
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
		isInstanceDestroyed = true;
		instanceReward.clear();
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
