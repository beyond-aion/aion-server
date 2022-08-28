package instance.pvparenas;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.util.Objects;

import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancereward.HarmonyArenaReward;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.network.aion.instanceinfo.HarmonyScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.model.DispelCategoryType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public class HarmonyArenaInstance extends GeneralInstanceHandler {

	protected int killBonus = 1000;
	protected int deathFine = -150;
	protected HarmonyArenaReward instanceReward;
	protected boolean isInstanceDestroyed;

	public HarmonyArenaInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public InstanceReward<?> getInstanceReward() {
		return instanceReward;
	}

	@Override
	public void onEnterInstance(final Player player) {
		int objectId = player.getObjectId();
		if (instanceReward.regPlayerReward(player)) {
			instanceReward.getPlayerReward(objectId).applyBoostMoraleEffect(player);
			instanceReward.setRndPosition(objectId);
		} else {
			instanceReward.portToPosition(player);
		}
		sendEnterPacket(player);
	}

	private void sendEnterPacket(final Player player) {
		int objectId = player.getObjectId();
		HarmonyGroupReward group = instanceReward.getHarmonyGroupReward(objectId);
		if (group == null) {
			return;
		}

		instance.forEachPlayer(opponent -> {
			if (!group.containPlayer(opponent.getObjectId())) {
				PacketSendUtility.sendPacket(opponent,
					new SM_INSTANCE_SCORE(instance.getMapId(), new HarmonyScoreInfo(instanceReward, 10, player), getTime()));
				PacketSendUtility.sendPacket(player,
					new SM_INSTANCE_SCORE(instance.getMapId(), new HarmonyScoreInfo(instanceReward, 10, opponent), getTime()));
				PacketSendUtility.sendPacket(opponent,
					new SM_INSTANCE_SCORE(instance.getMapId(), new HarmonyScoreInfo(instanceReward, 3, player), getTime()));
			} else {
				PacketSendUtility.sendPacket(opponent,
					new SM_INSTANCE_SCORE(instance.getMapId(), new HarmonyScoreInfo(instanceReward, 10, opponent), getTime()));
				if (!Objects.equals(objectId, opponent.getObjectId())) {
					PacketSendUtility.sendPacket(opponent,
						new SM_INSTANCE_SCORE(instance.getMapId(), new HarmonyScoreInfo(instanceReward, 3, player), getTime()));
				}
			}
		});
		PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(instance.getMapId(), new HarmonyScoreInfo(instanceReward, 6, player), getTime()));
		instanceReward.sendPacket(4, player);
	}

	private void updatePoints(Creature victim) {

		if (!instanceReward.isStartProgress()) {
			return;
		}

		int bonus;
		int rank = 0;

		// Decrease victim points
		if (victim instanceof Player) {
			final HarmonyGroupReward victimGroup = instanceReward.getHarmonyGroupReward(victim.getObjectId());
			victimGroup.addPoints(deathFine);
			bonus = killBonus;
			rank = instanceReward.getRank(victimGroup.getPoints());
			instanceReward.sendPacket(10, (Player) victim);
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
				instanceReward.sendPacket(10, attacker);
			}
		}
		if (instanceReward.hasCapPoints()) {
			instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
			reward();
			instanceReward.sendPacket(5, null);
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
				return 400;
			case 207099: // 2nd Floor Bomb
				return 200;
			case 219285: // Lurking Fangwing
			case 219328: // Plaza Wall
				return 50;
			default:
				return 0;
		}
	}

	private int getTime() {
		return instanceReward.getTime();
	}

	@Override
	public void onPlayerLogin(Player player) {
		sendEnterPacket(player);
	}

	@Override
	public void onInstanceCreate() {
		instanceReward = new HarmonyArenaReward(instance);
		instanceReward.setInstanceProgressionType(InstanceProgressionType.PREPARING);
		instanceReward.setInstanceStartTime();
		spawnRings();
		ThreadPoolManager.getInstance().schedule(() -> {
			// start round 1
			if (!isInstanceDestroyed && !instanceReward.isRewarded() && canStart()) {
				instance.forEachDoor(door -> door.setOpen(true));
				sendMsg(new SM_SYSTEM_MESSAGE(1401058));
				instanceReward.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
				instanceReward.sendPacket(10, null);
				instanceReward.sendPacket(2, null);
				ThreadPoolManager.getInstance().schedule(() -> {
					// start round 2
					if (!isInstanceDestroyed && !instanceReward.isRewarded()) {
						instanceReward.setRound(2);
						instanceReward.setRndZone();
						instanceReward.sendPacket(10, null);
						instanceReward.sendPacket(2, null);
						changeZone();
						ThreadPoolManager.getInstance().schedule(() -> {
							// start round 3
							if (!isInstanceDestroyed && !instanceReward.isRewarded()) {
								instanceReward.setRound(3);
								instanceReward.setRndZone();
								sendMsg(new SM_SYSTEM_MESSAGE(1401203));
								instanceReward.sendPacket(10, null);
								instanceReward.sendPacket(2, null);
								changeZone();
								ThreadPoolManager.getInstance().schedule(() -> {
									// end
									if (!isInstanceDestroyed && !instanceReward.isRewarded()) {
										instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
										reward();
										instanceReward.sendPacket(5, null);
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
			instanceReward.sendPacket(5, null);
			return false;
		}
		return true;
	}

	private void changeZone() {
		ThreadPoolManager.getInstance().schedule(() -> {
			for (Player player : instance.getPlayersInside()) {
				instanceReward.portToPosition(player);
				instanceReward.sendPacket(4, player);
			}
		}, 1000);
	}

	@Override
	public void onLeaveInstance(Player player) {
		clearDebuffs(player);
		PvPArenaPlayerReward playerReward = instanceReward.getPlayerReward(player.getObjectId());
		if (playerReward != null) {
			playerReward.endBoostMoraleEffect(player);
			instanceReward.clearPosition(playerReward.getPosition(), Boolean.FALSE);
			instanceReward.removePlayerReward(playerReward);
		}
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
				// GloryPointsService.addGp(player, group.getBasicGP() + group.getRankingGP() + group.getScoreGP());
				int courage = group.getBasicCourage() + group.getRankingCourage() + group.getScoreCourage();
				if (courage != 0) {
					ItemService.addItem(player, 186000137, Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(player, courage));
				}
				int victoryReward = group.getVictoryReward();
				if (victoryReward != 0) {
					ItemService.addItem(player, 188052605, victoryReward);
				}
				int consolationReward = group.getConsolationReward();
				if (victoryReward != 0) {
					ItemService.addItem(player, 188052606, consolationReward);
				}
				int arenaSupply = group.getArenaSupply();
				if (arenaSupply != 0) {
					ItemService.addItem(player, 188052181, arenaSupply);
				}
				int arenaSuperiorSupply = group.getArenaSuperiorSupply();
				if (arenaSuperiorSupply != 0) {
					ItemService.addItem(player, 188052482, arenaSuperiorSupply);
				}
			}
		}
		instance.forEachNpc(npc -> npc.getController().delete());
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isInstanceDestroyed) {
				for (Player player : instance.getPlayersInside()) {
					if (player.isDead())
						PlayerReviveService.duelRevive(player);
					onExitInstance(player);
				}
			}
		}, 10000);
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		PvPArenaPlayerReward ownerReward = instanceReward.getPlayerReward(player.getObjectId());
		ownerReward.endBoostMoraleEffect(player);
		ownerReward.applyBoostMoraleEffect(player);
		instanceReward.sendPacket(4, player);
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
			// useSkill(npc, player, skill >> 8, skill & 0xFF);
		}
		group.addPoints(rewardetPoints);
		sendSystemMsg(player, npc, rewardetPoints);
		instanceReward.sendPacket(10, player);
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		if (!isInstanceDestroyed) {
			instanceReward.portToPosition(player);
		}
		return true;
	}

	@Override
	public void onInstanceDestroy() {
		isInstanceDestroyed = true;
		instanceReward.clear();
	}
}
