package instance;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.instance.instancereward.IronWallFrontReward;
import com.aionemu.gameserver.model.instance.playerreward.IronWallFrontPlayerReward;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.instanceinfo.IronWallFrontScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Tibald
 */
@InstanceID(301220000)
public class IronWallFrontInstance extends GeneralInstanceHandler {

	private IronWallFrontReward ironWallFrontReward;
	private long instanceTime;
	private Future<?> instanceTask;
	private boolean isInstanceDestroyed = false;

	public IronWallFrontInstance(WorldMapInstance instance) {
		super(instance);
	}

	private void addPlayerToReward(Player player) {
		ironWallFrontReward.addPlayerReward(new IronWallFrontPlayerReward(player.getObjectId(), player.getRace()));
	}

	private boolean containPlayer(int objectId) {
		return ironWallFrontReward.containsPlayer(objectId);
	}

	@Override
	public void onEnterInstance(Player player) {
		if (!containPlayer(player.getObjectId())) {
			addPlayerToReward(player);
		}
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IronWallFrontScoreInfo(ironWallFrontReward, 3, player.getObjectId()), getTime()));
		PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(instance.getMapId(), new IronWallFrontScoreInfo(ironWallFrontReward, 6, player.getObjectId()), getTime()));
	}

	private void startInstanceTask() {
		instanceTime = System.currentTimeMillis();
		ThreadPoolManager.getInstance().schedule(() -> {
			openFirstDoors();
			ironWallFrontReward.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
			sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IronWallFrontScoreInfo(ironWallFrontReward, 6, 0), getTime()));
		}, 120000);
		instanceTask = ThreadPoolManager.getInstance().schedule(this::stopInstance, 1320000);
	}

	private void stopInstance() {
		if (instanceTask != null && !instanceTask.isDone())
			instanceTask.cancel(true);
		if (ironWallFrontReward.isRewarded())
			return;
		ironWallFrontReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
		Race winnerRace = ironWallFrontReward.getRaceWithHighestPoints();
		instance.forEachPlayer(player -> {
			IronWallFrontPlayerReward reward = ironWallFrontReward.getPlayerReward(player.getObjectId());
			reward.setBonusReward(Rnd.get(2300, 2700));
			if (reward.getRace() == winnerRace) {
				reward.setGloryPoints(100);
				reward.setFragmentedCeramium(9);
				reward.setIronWarFrontBox(1);
				reward.setBaseReward(ironWallFrontReward.getWinnerApReward());
			} else {
				reward.setGloryPoints(10);
				reward.setBaseReward(ironWallFrontReward.getLoserApReward());
			}
			sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IronWallFrontScoreInfo(ironWallFrontReward, 5, player.getObjectId()), getTime()));
			AbyssPointsService.addAp(player, reward.getBaseReward() + reward.getBonusReward());
			if (reward.getGloryPoints() > 0)
				GloryPointsService.increaseGpBy(player.getObjectId(), reward.getGloryPoints());
			if (reward.getIronWarFrontBox() > 0) {
				ItemService.addItem(player, 188052729, reward.getIronWarFrontBox());
			}
		});
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
	public void onExitInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	@Override
	public void onInstanceDestroy() {
		isInstanceDestroyed = true;
		if (instanceTask != null && !instanceTask.isDone())
			instanceTask.cancel(true);
	}

	private void updatePoints(int points, Race race, boolean check, String npcL10n, Player player) {
		if (check && !ironWallFrontReward.isStartProgress()) {
			return;
		}
		if (npcL10n != null)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(npcL10n, points));
		ironWallFrontReward.addPointsByRace(race, points);
		sendPacket(
			new SM_INSTANCE_SCORE(instance.getMapId(), new IronWallFrontScoreInfo(ironWallFrontReward, 10, race == Race.ELYOS ? 0 : 1), getTime()));
		int diff = Math.abs(ironWallFrontReward.getAsmodiansPoints() - ironWallFrontReward.getElyosPoints());
		if (diff >= 30000) {
			stopInstance();
		}

	}

	@Override
	public void onDie(Npc npc) {
		Player player = npc.getAggroList().getMostPlayerDamage();
		if (player == null) {
			return;
		}
		int points = 0;
		switch (npc.getNpcId()) {
			case 233473:
				points = 100;
				break;
			case 702042:
				points = 500;
				break;
			case 233491: // random position boss
			case 701943: // elyos power generator
			case 701944: // asmodian power generator
			case 701945: // balaur power generator
				points = 5000;
				break;
			case 233494:
				points = 30000;
				break;
		}
		if (points > 0) {
			updatePoints(points, player.getRace(), true, npc.getObjectTemplate().getL10n(), player);
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		if (player == null) {
			return;
		}
		int points = 0;
		switch (npc.getNpcId()) {
			case 801903:
				points = 1500;
				break;
			case 801772:
				points = 525;
				break;
			case 801766:
			case 801767:
			case 801818:
			case 801819:
			case 801820:
			case 801821:
				points = 255;
				break;
			case 730861:
			case 730878:
			case 730879:
			case 730880:
				updatePoints(200, player.getRace(), false, npc.getObjectTemplate().getL10n(), player);
				if (player.getRace() == Race.ELYOS) {
					spawn(701900, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				} else {
					spawn(701901, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
					npc.getController().delete();
				}
				break;
		}
		if (points > 0) {
			updatePoints(points, player.getRace(), true, npc.getObjectTemplate().getL10n(), player);
			npc.getController().delete();
		}
	}

	private void openFirstDoors() {
		instance.setDoorState(177, true);
		instance.setDoorState(176, true);
	}

	private void sendPacket(AionServerPacket packet) {
		PacketSendUtility.broadcastToMap(instance, packet);
	}

	@Override
	public void onInstanceCreate() {
		ironWallFrontReward = new IronWallFrontReward();
		ironWallFrontReward.setInstanceProgressionType(InstanceProgressionType.PREPARING);
		startInstanceTask();
	}

	@Override
	public InstanceReward<?> getInstanceReward() {
		return ironWallFrontReward;
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		ironWallFrontReward.portToPosition(player, instance);
		return true;
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IronWallFrontScoreInfo(ironWallFrontReward, 3, player.getObjectId()), getTime()));
		PacketSendUtility.sendPacket(player, new SM_DIE(player.canUseRebirthRevive(), false, 0, 8));
		if (lastAttacker instanceof Player) {
			if (lastAttacker.getRace() != player.getRace()) {
				int killPoints = 100;
				if (ironWallFrontReward.isStartProgress() && getTime() >= 900000) {
					killPoints = 300;
				}
				updatePoints(killPoints, lastAttacker.getRace(), true, null, (Player) lastAttacker);
				PacketSendUtility.sendPacket((Player) lastAttacker, new SM_SYSTEM_MESSAGE(1400277, killPoints));
				ironWallFrontReward.incrementKillsByRace(lastAttacker.getRace());
				sendPacket(
					new SM_INSTANCE_SCORE(instance.getMapId(), new IronWallFrontScoreInfo(ironWallFrontReward, 10, lastAttacker.getRace() == Race.ELYOS ? 0 : 1), getTime()));
			}
		}
		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400277, -100));
		updatePoints(-100, player.getRace(), true, null, player);
		return true;
	}

	private int getTime() {
		long result = System.currentTimeMillis() - instanceTime;
		if (result < 120000) {
			return (int) (120000 - result);
		} else if (result < 1320000) {
			return (int) (1200000 - (result - 120000));
		}
		return 0;
	}
}
