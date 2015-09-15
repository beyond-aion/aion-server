package instance;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javolution.util.FastTable;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.actions.PlayerActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.instance.instancereward.IronWallFrontReward;
import com.aionemu.gameserver.model.instance.playerreward.IronWallFrontPlayerReward;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.instanceinfo.IronWallFrontScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Tibald
 */
@InstanceID(301220000)
public class IronWallFrontInstance extends GeneralInstanceHandler {

	protected IronWallFrontReward ironWallFrontReward;
	private Map<Integer, StaticDoor> doors;
	private long instanceTime;
	private Future<?> instanceTask;
	private Future<?> timeCheckTask;
	private boolean isInstanceDestroyed = false;
	private static List<WorldPosition> generalsPos = new FastTable<>();
	private static List<WorldPosition> garnonPos = new FastTable<>();

	static {
		generalsPos.add(new WorldPosition(301120000, 1437.7f, 1368.7f, 600.8967f, (byte) 40));
		generalsPos.add(new WorldPosition(301120000, 1172.2f, 1445, 586.55f, (byte) 35));
		generalsPos.add(new WorldPosition(301120000, 1428.67f, 1617.67f, 599.9493f, (byte) 70));
		garnonPos.add(new WorldPosition(301120000, 1138.4039f, 1619.2574f, 598.43506f, (byte) 53));
		garnonPos.add(new WorldPosition(301120000, 1184.5309f, 1408.2471f, 586.6199f, (byte) 6));
		garnonPos.add(new WorldPosition(301120000, 1241.9187f, 1557.2854f, 585.2431f, (byte) 46));
		garnonPos.add(new WorldPosition(301120000, 1270.4377f, 1455.0625f, 595.2903f, (byte) 13));
		garnonPos.add(new WorldPosition(301120000, 1325.634f, 1326.134f, 596.4888f, (byte) 106));
		garnonPos.add(new WorldPosition(301120000, 1346.7902f, 1717.1029f, 598.43396f, (byte) 30));
		garnonPos.add(new WorldPosition(301120000, 1410.7446f, 1579.752f, 595.7288f, (byte) 93));
		garnonPos.add(new WorldPosition(301120000, 1455.881f, 1392.8229f, 598.5873f, (byte) 10));
		garnonPos.add(new WorldPosition(301120000, 1540.113f, 1395.6737f, 596.625f, (byte) 105));
	}

	private void addPlayerToReward(Player player) {
		ironWallFrontReward.addPlayerReward(new IronWallFrontPlayerReward(player.getObjectId(), player.getRace()));
	}

	private boolean containPlayer(Integer object) {
		return ironWallFrontReward.containPlayer(object);
	}

	@Override
	public void onEnterInstance(Player player) {
		if (!containPlayer(player.getObjectId())) {
			addPlayerToReward(player);
		}
		sendPacket(new SM_INSTANCE_SCORE(new IronWallFrontScoreInfo(ironWallFrontReward, 3, player.getObjectId()), ironWallFrontReward, getTime()));
		PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(new IronWallFrontScoreInfo(ironWallFrontReward, 6, player.getObjectId()),
			ironWallFrontReward, getTime()));
		// sendPacket();
	}

	protected void startInstanceTask() {
		instanceTime = System.currentTimeMillis();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				openFirstDoors();
				ironWallFrontReward.setInstanceScoreType(InstanceScoreType.START_PROGRESS);
				sendPacket(new SM_INSTANCE_SCORE(new IronWallFrontScoreInfo(ironWallFrontReward, 6, 0), ironWallFrontReward, getTime()));
			}

		}, 120000);
		instanceTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				stopInstance();
			}

		}, 1320000);
	}

	public void stopInstance() {
		if (instanceTask != null && !instanceTask.isDone()) {
			instanceTask.cancel(true);
		}
		if (timeCheckTask != null && !timeCheckTask.isDone()) {
			timeCheckTask.cancel(true);
		}
		if (ironWallFrontReward.isRewarded()) {
			return;
		}
		ironWallFrontReward.setInstanceScoreType(InstanceScoreType.END_PROGRESS);
		final Race winningrace = ironWallFrontReward.getWinningRace();
		instance.doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				IronWallFrontPlayerReward reward = ironWallFrontReward.getPlayerReward(player.getObjectId());
				reward.setBonusReward(Rnd.get(2300, 2700));
				if (reward.getRace().equals(winningrace)) {
					reward.setGloryPoints(100);
					reward.setFragmentedCeramium(9);
					reward.setIronWarFrontBox(1);
					reward.setBaseReward(IronWallFrontReward.winningPoints);
				} else {
					reward.setGloryPoints(10);
					reward.setBaseReward(IronWallFrontReward.looserPoints);
				}
				sendPacket(new SM_INSTANCE_SCORE(new IronWallFrontScoreInfo(ironWallFrontReward, 5, player.getObjectId()), ironWallFrontReward, getTime()));
				AbyssPointsService.addAp(player, reward.getBaseReward() + reward.getBonusReward());
				GloryPointsService.addGp(player, reward.getGloryPoints());
				if (reward.getIronWarFrontBox() > 0) {
					ItemService.addItem(player, 188052729, reward.getIronWarFrontBox());
				}
			}

		});
		for (Npc npc : instance.getNpcs()) {
			npc.getController().onDelete();
		}
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed) {
					for (Player player : instance.getPlayersInside()) {
						if (PlayerActions.isAlreadyDead(player)) {
							PlayerReviveService.duelRevive(player);
						}
						onExitInstance(player);
					}
					AutoGroupService.getInstance().unRegisterInstance(instanceId);
				}
			}

		}, 10000);
	}

	@Override
	public void onExitInstance(Player player) {
		TeleportService2.moveToInstanceExit(player, mapId, player.getRace());
	}

	@Override
	public void onInstanceDestroy() {
		isInstanceDestroyed = true;
		if (instanceTask != null && !instanceTask.isDone()) {
			instanceTask.cancel(true);
		}
		if (timeCheckTask != null && !timeCheckTask.isDone()) {
			timeCheckTask.cancel(true);
		}
	}

	public void updatePoints(int points, Race race, boolean check, int nameId, Player player) {
		if (check && !ironWallFrontReward.isStartProgress()) {
			return;
		}
		if (nameId != 0) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400237, new DescriptionId(nameId * 2 + 1), points));
		}
		ironWallFrontReward.addPointsByRace(race, points);
		sendPacket(new SM_INSTANCE_SCORE(new IronWallFrontScoreInfo(ironWallFrontReward, 10, race.equals(Race.ELYOS) ? 0 : 1), ironWallFrontReward,
			getTime()));
		int diff = Math.abs(ironWallFrontReward.getAsmodiansPoint().intValue() - ironWallFrontReward.getElyosPoints().intValue());
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
			updatePoints(points, player.getRace(), true, npc.getObjectTemplate().getNameId(), player);
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
				updatePoints(200, player.getRace(), false, npc.getObjectTemplate().getNameId(), player);
				if (player.getRace().equals(Race.ELYOS)) {
					spawn(701900, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				} else {
					spawn(701901, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
					npc.getController().onDelete();
				}
				break;
		}
		if (points > 0) {
			updatePoints(points, player.getRace(), true, npc.getObjectTemplate().getNameId(), player);
			npc.getController().onDelete();
		}
	}

	public void openFirstDoors() {
		openDoor(177);
		openDoor(176);
	}

	protected void openDoor(int doorId) {
		StaticDoor door = doors.get(doorId);
		if (door != null) {
			door.setOpen(true);
		}
	}

	public void sendPacket(final AionServerPacket packet) {
		instance.doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				PacketSendUtility.sendPacket(player, packet);
			}

		});
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		ironWallFrontReward = new IronWallFrontReward(mapId, instanceId);
		ironWallFrontReward.setInstanceScoreType(InstanceScoreType.PREPARING);
		doors = instance.getDoors();
		startInstanceTask();
	}

	@Override
	public InstanceReward<?> getInstanceReward() {
		return ironWallFrontReward;
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME);
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		ironWallFrontReward.portToPosition(player);
		return true;
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		sendPacket(new SM_INSTANCE_SCORE(new IronWallFrontScoreInfo(ironWallFrontReward, 3, player.getObjectId()), ironWallFrontReward, getTime()));
		PacketSendUtility.broadcastPacket(player,
			new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), false, 0, 8));
		if (lastAttacker instanceof Player) {
			if (lastAttacker.getRace() != player.getRace()) {
				int killPoints = 100;
				if (ironWallFrontReward.isStartProgress() && getTime() >= 900000) {
					killPoints = 300;
				}
				updatePoints(killPoints, lastAttacker.getRace(), true, 0, (Player) lastAttacker);
				PacketSendUtility.sendPacket((Player) lastAttacker, new SM_SYSTEM_MESSAGE(1400277, killPoints));
				ironWallFrontReward.getKillsByRace(lastAttacker.getRace()).increment();
				sendPacket(new SM_INSTANCE_SCORE(new IronWallFrontScoreInfo(ironWallFrontReward, 10, lastAttacker.getRace().equals(Race.ELYOS) ? 0 : 1),
					ironWallFrontReward, getTime()));
			}
		}
		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400277, -100));
		updatePoints(-100, player.getRace(), true, 0, player);
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
