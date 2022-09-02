package instance.dredgion;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.instance.DredgionRooms;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.instance.instancereward.PvpInstanceReward;
import com.aionemu.gameserver.model.instance.playerreward.PvpInstancePlayerReward;
import com.aionemu.gameserver.network.aion.instanceinfo.DredgionScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public class DredgionInstance extends GeneralInstanceHandler {

	protected final List<DredgionRooms> dredgionRooms = new ArrayList<>();
	protected final AtomicInteger killedSurkanas = new AtomicInteger();
	protected AtomicBoolean isInstanceStarted = new AtomicBoolean();
	protected PvpInstanceReward<PvpInstancePlayerReward> pInstanceReward;
	private final static int MAX_PLAYERS_PER_FACTION = 6;
	private final int raceStartPosition = Rnd.get(2);
	private Future<?> instanceTask;
	private long instanceTime;
	private float losingGroupMultiplier = 1;
	private boolean isInstanceDestroyed;

	public DredgionInstance(WorldMapInstance instance) {
		super(instance);
	}

	protected PvpInstancePlayerReward getPlayerReward(Player player) {
		if (pInstanceReward.getPlayerReward(player.getObjectId()) == null) {
			addPlayerToReward(player);
		}
		return pInstanceReward.getPlayerReward(player.getObjectId());
	}

	protected void captureRoom(Race race, int roomId) {
		for (DredgionRooms dredgionRoom : dredgionRooms) {
			if (dredgionRoom.getRoomId() == roomId) {
				dredgionRoom.captureRoom(race);
			}
		}
	}

	private void addPlayerToReward(Player player) {
		pInstanceReward.addPlayerReward(new PvpInstancePlayerReward(player.getObjectId(), player.getRace()));
	}

	protected void startInstanceTask() {
		instanceTime = System.currentTimeMillis();
		ThreadPoolManager.getInstance().schedule(() -> {
			openFirstDoors();
			pInstanceReward.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
			sendPacket();
		}, 120000);
		instanceTask = ThreadPoolManager.getInstance().schedule(() -> stopInstance(pInstanceReward.getRaceWithHighestPoints()), 2520000);
	}

	@Override
	public void onEnterInstance(final Player player) {
		if (!pInstanceReward.containsPlayer(player.getObjectId()))
			pInstanceReward.addPlayerReward(new PvpInstancePlayerReward(player.getObjectId(), player.getRace()));
		sendPacket();
	}

	@Override
	public void onInstanceCreate() {
		initializeInstance(4500, 2500, 3750);
	}

	protected void initializeInstance(int winnerAp, int loserAp, int drawAp) {
		pInstanceReward = new PvpInstanceReward<>(winnerAp, loserAp, drawAp);
		pInstanceReward.setInstanceProgressionType(InstanceProgressionType.PREPARING);
		for (int i = 1; i < 15; i++) {
			dredgionRooms.add(new DredgionRooms(i));
		}
	}

	protected void stopInstance(Race winnerRace) {
		stopInstanceTask();
		pInstanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
		instance.forEachPlayer(p -> doReward(p, getPlayerReward(p), winnerRace));
		instance.forEachNpc(npc -> npc.getController().delete());
		ThreadPoolManager.getInstance().schedule(() -> instance.getPlayersInside().forEach(this::revivePlayerOnEnd), 10000);
		ThreadPoolManager.getInstance().schedule(() -> instance.getPlayersInside().forEach(this::onExitInstance), 60000);
		sendPacket();
	}

	public void doReward(Player player, PvpInstancePlayerReward reward, Race winningRace) {
		int scorePoints = pInstanceReward.getPointsByRace(reward.getRace());
		if (reward.getRace() == winningRace) {
			reward.setBaseAp(pInstanceReward.getWinnerApReward());
			reward.setBonusAp(2 * scorePoints / MAX_PLAYERS_PER_FACTION);
			reward.setReward1(186000242, 1, 0); // CUSTOM: Ceramium Medal
			if (Rnd.chance() < 30)
				reward.setReward2(188053030, 1, 0); // CUSTOM: Special Courier Pass (Abyss Eternal/Lv. 61-65)
		} else {
			reward.setBaseAp(pInstanceReward.getLoserApReward());
			reward.setBonusAp(scorePoints / MAX_PLAYERS_PER_FACTION);
			reward.setReward1(186000147, 1, 0); // CUSTOM: Mithril Medal
			if (winningRace == Race.NONE)
				reward.setBaseAp(pInstanceReward.getDrawApReward()); // Base AP are overridden in a draw case
		}
		distributeRewards(player, reward);
	}

	private void distributeRewards(Player player, PvpInstancePlayerReward reward) {
		QuestEnv env = new QuestEnv(null, player, 0);
		QuestEngine.getInstance().onDredgionReward(env);
		AbyssPointsService.addAp(player, (int) Rates.AP_DREDGION.calcResult(player, reward.getBaseAp() + reward.getBonusAp()));
		if (reward.getReward1ItemId() > 0)
			ItemService.addItem(player, reward.getReward1ItemId(), reward.getReward1Count() + reward.getReward1BonusCount(), true);
		if (reward.getReward2ItemId() > 0)
			ItemService.addItem(player, reward.getReward2ItemId(), reward.getReward2Count() + reward.getReward2BonusCount(), true);
		if (reward.getReward3ItemId() > 0)
			ItemService.addItem(player, reward.getReward3ItemId(), reward.getReward3Count(), true);
		if (reward.getReward4ItemId() > 0)
			ItemService.addItem(player, reward.getReward4ItemId(), reward.getReward4Count(), true);
		if (reward.getBonusRewardItemId() > 0)
			ItemService.addItem(player, reward.getBonusRewardItemId(), reward.getBonusRewardCount(), true);
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		portToStartPosition(player);
		return true;
	}

	protected void revivePlayerOnEnd(Player player) {
		if (player.isDead())
			PlayerReviveService.duelRevive(player);
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		PacketSendUtility.sendPacket(player, new SM_DIE(player.canUseRebirthRevive(), false, 0, 8));
		int points = 60;
		if (lastAttacker instanceof Player killer && killer.getRace() != player.getRace()) {
			if (killer.getRace() != pInstanceReward.getRaceWithHighestPoints())
				points *= losingGroupMultiplier;
			else if (losingGroupMultiplier == 10 || getPlayerReward(player).getPoints() == 0)
				points = 0;

			if (player.getAbyssRank().getRank().getId() - killer.getAbyssRank().getRank().getId() >= 4)
				points *= 1.6f;

			updateScore((Player) lastAttacker, player, points, true);
		}
		updateScore(player, player, -points, false);
		return true;
	}

	private void addPointToPlayer(Player player, int points) {
		getPlayerReward(player).addPoints(points);
	}

	private void addPvPKillToPlayer(Player player) {
		getPlayerReward(player).addPvPKillToPlayer();
	}

	private void addBalaurKillToPlayer(Player player) {
		getPlayerReward(player).addMonsterKillToPlayer();
	}

	protected void updateScore(Player player, Creature target, int points, boolean pvpKill) {
		if (points == 0)
			return;

		// group score
		pInstanceReward.addPointsByRace(player.getRace(), points);

		// player score
		List<Player> playersToGainScore = new ArrayList<>();

		if (target != null && player.isInGroup()) {
			for (Player member : player.getPlayerGroup().getOnlineMembers()) {
				if (member.isDead()) {
					continue;
				}
				if (PositionUtil.isInRange(member, target, GroupConfig.GROUP_MAX_DISTANCE)) {
					playersToGainScore.add(member);
				}
			}
		} else {
			playersToGainScore.add(player);
		}

		for (Player playerToGainScore : playersToGainScore) {
			addPointToPlayer(playerToGainScore, points / playersToGainScore.size());
			if (target instanceof Npc) {
				PacketSendUtility.sendPacket(playerToGainScore, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(((Npc) target).getObjectTemplate().getL10n(), points));
			} else if (target instanceof Player) {
				PacketSendUtility.sendPacket(playerToGainScore, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(target.getName(), points));
			}
		}

		// recalculate point multiplier
		int pointDifference = pInstanceReward.getAsmodiansPoints() - pInstanceReward.getElyosPoints();
		if (pointDifference < 0) {
			pointDifference *= -1;
		}
		if (pointDifference >= 3000) {
			losingGroupMultiplier = 10;
		} else if (pointDifference >= 1000) {
			losingGroupMultiplier = 1.5f;
		} else {
			losingGroupMultiplier = 1;
		}

		// pvpKills for pvp and balaurKills for pve
		if (pvpKill && points > 0) {
			addPvPKillToPlayer(player);
		} else if (target instanceof Npc && target.getRace() == Race.DRAKAN) {
			addBalaurKillToPlayer(player);
		}
		sendPacket();
	}

	@Override
	public void onDie(Npc npc) {
		int hpGauge = npc.getObjectTemplate().getHpGauge();
		Player mostPlayerDamage = npc.getAggroList().getMostPlayerDamage();
		if (hpGauge <= 5) {
			updateScore(mostPlayerDamage, npc, 12, false);
		} else if (hpGauge <= 9) {
			updateScore(mostPlayerDamage, npc, 32, false);
		} else {
			updateScore(mostPlayerDamage, npc, 42, false);
		}
	}

	@Override
	public void onInstanceDestroy() {
		stopInstanceTask();
		isInstanceDestroyed = true;
		pInstanceReward.clear();
	}

	protected void openFirstDoors() {
	}

	private void sendPacket() {
		PacketSendUtility.broadcastToMap(instance,
			new SM_INSTANCE_SCORE(instance.getMapId(), new DredgionScoreInfo(pInstanceReward, instance.getPlayersInside(), dredgionRooms), getTime()));
	}

	private int getTime() {
		long result = System.currentTimeMillis() - instanceTime;
		if (result < 120000) {
			return (int) (120000 - result);
		} else if (result < 2520000) {
			return (int) (2400000 - (result - 120000));
		}
		return 0;
	}

	protected void sp(final int npcId, final float x, final float y, final float z, final byte h, final int time) {
		sp(npcId, x, y, z, h, 0, time);
	}

	protected void sp(final int npcId, final float x, final float y, final float z, final byte h, final int staticId, final int time) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isInstanceDestroyed) {
				spawn(npcId, x, y, z, h, staticId);
			}
		}, time);
	}

	protected void sp(final int npcId, final float x, final float y, final float z, final byte h, final int time, final String walkerId) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isInstanceDestroyed) {
				Npc npc = (Npc) spawn(npcId, x, y, z, h);
				npc.getSpawn().setWalkerId(walkerId);
				WalkManager.startWalking((NpcAI) npc.getAi());
			}
		}, time);
	}

	protected void sendMsgByRace(final int msg, final Race race, int time) {
		ThreadPoolManager.getInstance().schedule(() -> instance.forEachPlayer(player -> {
			if (player.getRace() == race || race == Race.PC_ALL) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(msg));
			}
		}), time);

	}

	private void stopInstanceTask() {
		if (instanceTask != null) {
			instanceTask.cancel(true);
		}
	}

	@Override
	public InstanceReward<?> getInstanceReward() {
		return pInstanceReward;
	}

	@Override
	public void onExitInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	@Override
	public void portToStartPosition(Player player) {
		if (player.getRace() == Race.ELYOS && raceStartPosition == 0 || player.getRace() == Race.ASMODIANS && raceStartPosition != 0) {
			TeleportService.teleportTo(player, instance.getMapId(), instance.getInstanceId(), 570.468f, 166.897f, 432.28986f);
		} else {
			TeleportService.teleportTo(player, instance.getMapId(), instance.getInstanceId(), 400.741f, 166.713f, 432.290f);
		}
	}
}
