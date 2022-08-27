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
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancereward.DredgionReward;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.instance.playerreward.DredgionPlayerReward;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;
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

	protected final AtomicInteger killedSurkanas = new AtomicInteger();
	protected DredgionReward dredgionReward;
	private float losingGroupMultiplier = 1;
	private boolean isInstanceDestroyed = false;
	protected AtomicBoolean isInstanceStarted = new AtomicBoolean(false);
	private long instanceTime;
	private Future<?> instanceTask;

	public DredgionInstance(WorldMapInstance instance) {
		super(instance);
	}

	protected DredgionPlayerReward getPlayerReward(Player player) {
		if (dredgionReward.getPlayerReward(player.getObjectId()) == null) {
			addPlayerToReward(player);
		}
		return dredgionReward.getPlayerReward(player.getObjectId());
	}

	protected void captureRoom(Race race, int roomId) {
		dredgionReward.getDredgionRoomById(roomId).captureRoom(race);
	}

	private void addPlayerToReward(Player player) {
		dredgionReward.addPlayerReward(new DredgionPlayerReward(player.getObjectId()));
	}

	private boolean containPlayer(int objectId) {
		return dredgionReward.containsPlayer(objectId);
	}

	protected void startInstanceTask() {
		instanceTime = System.currentTimeMillis();
		ThreadPoolManager.getInstance().schedule(() -> {
			openFirstDoors();
			dredgionReward.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
			sendPacket();
		}, 120000);
		instanceTask = ThreadPoolManager.getInstance().schedule(() -> stopInstance(dredgionReward.getRaceWithHighestPoints()), 2520000);
	}

	@Override
	public void onEnterInstance(final Player player) {
		if (!containPlayer(player.getObjectId())) {
			addPlayerToReward(player);
		}
		sendPacket();
	}

	@Override
	public void onInstanceCreate() {
		dredgionReward = new DredgionReward(instance.getMapId());
		dredgionReward.setInstanceProgressionType(InstanceProgressionType.PREPARING);
	}

	protected void stopInstance(Race winnerRace) {
		stopInstanceTask();
		dredgionReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
		doReward(winnerRace);
		sendPacket();
	}

	public void doReward(Race winnerRace) {
		for (Player player : instance.getPlayersInside()) {
			InstancePlayerReward playerReward = getPlayerReward(player);
			int abyssPoint = playerReward.getPoints(); // to do find out on what depend this modifier
			if (player.getRace() == winnerRace) {
				abyssPoint += dredgionReward.getWinnerApReward();
				ItemService.addItem(player, 186000242, 1); // Ceramium Medal
				if (Rnd.chance() < 30)
					ItemService.addItem(player, 188950017, 1); // Special Courier Pass (Abyss Eternal/Lv. 61-65)
			} else {
				abyssPoint += dredgionReward.getLoserApReward();
				ItemService.addItem(player, 186000147, 1); // Mithril Medal
			}
			AbyssPointsService.addAp(player, (int) Rates.AP_DREDGION.calcResult(player, abyssPoint));
			QuestEnv env = new QuestEnv(null, player, 0);
			QuestEngine.getInstance().onDredgionReward(env);
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
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		dredgionReward.portToPosition(player, instance);
		return true;
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		PacketSendUtility.sendPacket(player, new SM_DIE(player.canUseRebirthRevive(), false, 0, 8));
		int points = 60;
		if (lastAttacker instanceof Player && lastAttacker.getRace() != player.getRace()) {
			if (lastAttacker.getRace() != dredgionReward.getRaceWithHighestPoints())
				points *= losingGroupMultiplier;
			else if (losingGroupMultiplier == 10 || getPlayerReward(player).getPoints() == 0)
				points = 0;

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
		dredgionReward.addPointsByRace(player.getRace(), points);

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
		int pointDifference = dredgionReward.getAsmodiansPoints() - dredgionReward.getElyosPoints();
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
		dredgionReward.clear();
	}

	protected void openFirstDoors() {
	}

	private void sendPacket() {
		PacketSendUtility.broadcastToMap(instance, new SM_INSTANCE_SCORE(instance.getMapId(), new DredgionScoreInfo(dredgionReward, instance.getPlayersInside()), getTime()));
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
		return dredgionReward;
	}

	@Override
	public void onExitInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}
}
