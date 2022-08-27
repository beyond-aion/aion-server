package instance;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.util.ArrayList;
import java.util.List;
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
import com.aionemu.gameserver.model.instance.instancereward.KamarReward;
import com.aionemu.gameserver.model.instance.playerreward.KamarPlayerReward;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceGroup;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.instanceinfo.KamarBattlefieldScoreInfo;
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
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author xTz
 */
@InstanceID(301120000)
public class KamarBattlefieldInstance extends GeneralInstanceHandler {

	private KamarReward kamarReward;
	private long instanceTime;
	private Future<?> instanceTask;
	private Future<?> timeCheckTask;
	private byte timeInMin = -1;
	private boolean isInstanceDestroyed = false;
	private static List<WorldPosition> generalsPos = new ArrayList<>();
	private static List<WorldPosition> garnonPos = new ArrayList<>();

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

	public KamarBattlefieldInstance(WorldMapInstance instance) {
		super(instance);
	}

	private void addPlayerToReward(Player player) {
		kamarReward.addPlayerReward(new KamarPlayerReward(player.getObjectId(), player.getRace()));
	}

	private boolean containPlayer(int objectId) {
		return kamarReward.containsPlayer(objectId);
	}

	@Override
	public void onEnterInstance(Player player) {
		if (!containPlayer(player.getObjectId())) {
			addPlayerToReward(player);
		}
		kamarReward.getPlayerReward(player.getObjectId()).applyBoostMoraleEffect(player);
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new KamarBattlefieldScoreInfo(kamarReward, 4, player.getObjectId()), getTime()));
		PacketSendUtility.sendPacket(player,
			new SM_INSTANCE_SCORE(instance.getMapId(), new KamarBattlefieldScoreInfo(kamarReward, 6, player.getObjectId()), getTime()));
	}

	private void startInstanceTask() {
		instanceTime = System.currentTimeMillis();
		instanceTask = ThreadPoolManager.getInstance().schedule(() -> {
			kamarReward.setInstanceProgressionType(InstanceProgressionType.PREPARING);
			sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new KamarBattlefieldScoreInfo(kamarReward, 6, 0), getTime()));
			// teleport secondary group
			for (Player player : instance.getPlayersInside()) {
				PlayerAllianceGroup secGroup = player.getPlayerAlliance().getAllianceGroup(1001);
				if (secGroup != null && secGroup.equals(player.getPlayerAllianceGroup())) {
					kamarReward.portToPosition(player, instance);
				}
			}
			instanceTask = ThreadPoolManager.getInstance().schedule(() -> {
				openFirstDoors();
				kamarReward.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
				sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new KamarBattlefieldScoreInfo(kamarReward, 6, 0), getTime()));
				startTimeCheck();
				instanceTask = ThreadPoolManager.getInstance().schedule(this::stopInstance, 1800000);
			}, 60000);
		}, 180000);

	}

	private void startTimeCheck() {
		WorldPosition pos = Rnd.get(garnonPos);
		spawn(801903, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
		timeCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			timeInMin++;
			switch (timeInMin) {
				case 5:
					spawn(802016, 1440.3145f, 1227.4073f, 587.36328f, (byte) 0, 223);
					spawn(802017, 1109.5887f, 1532.7554f, 586.6358f, (byte) 0, 221);
					spawn(802018, 1213.4902f, 1363.4617f, 613.93866f, (byte) 0, 225);
					spawn(802019, 1527.215f, 1561.5153f, 613.47742f, (byte) 0, 224);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDKamar_StartTeleporter_Spawn());
					break;
				case 10:
					spawn(801772, 1353.1956f, 1413.8037f, 598.75f, (byte) 0);
					spawn(801772, 1356.0574f, 1479.6165f, 594.15155f, (byte) 0);
					spawn(801772, 1371.584f, 1550.1755f, 595.375f, (byte) 0);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDKamar_YunSupply_Spawn());
					break;
				case 12:
					spawnAndSetRespawn(701808, 1285.834f, 1489.1963f, 595.66486f, (byte) 0, 180);
					spawnAndSetRespawn(701912, 1414.2816f, 1463.925f, 598.7676f, (byte) 0, 180);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDKamar_SeigeWeapon_Spawn());
					break;
				case 14:
					spawn(801962, 1325.73f, 1521.42f, 700.0f, (byte) 15);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDKamar_Dreadgion_Spawn());
					break;
				case 15:
					spawn(232847, 1221.6609f, 1563.3887f, 585.343f, (byte) 30);
					spawn(232847, 1312.6637f, 1426.5917f, 596.912f, (byte) 0);
					spawn(232847, 1421.0524f, 1503.8083f, 597.0f, (byte) 0);
					spawn(232847, 1347.8895f, 1278.5276f, 593.75f, (byte) 0);
					spawn(232848, 1318.0083f, 1423.2358f, 697.1422f, (byte) 0);
					spawn(232848, 1352.3656f, 1281.6598f, 593.75f, (byte) 0);
					spawn(232848, 1415.9098f, 1507.7222f, 597.0f, (byte) 0);
					spawn(232848, 1226.0847f, 1566.771f, 585.25f, (byte) 53);
					spawn(232849, 1328.4695f, 1667.7284f, 598.75f, (byte) 0);
					spawn(232849, 1316.2865f, 1526.8649f, 594.4299f, (byte) 100);
					spawn(232849, 1168.7726f, 1606.4891f, 598.7017f, (byte) 0);
					spawn(232850, 1134.1378f, 1498.5004f, 585.3203f, (byte) 15);
					spawn(232850, 1529.4595f, 1402.4359f, 597.5f, (byte) 20);
					spawn(232850, 1322.879f, 1531.0671f, 594.4299f, (byte) 100);
					spawn(232851, 1531.8644f, 1454.7493f, 596.7186f, (byte) 80);
					spawn(232851, 1321.8517f, 1525.4725f, 594.4299f, (byte) 100);
					spawn(232851, 1133.2808f, 1504.6725f, 585.22835f, (byte) 116);
					spawn(233261, 1357.5049f, 1434.2639f, 598.875f, (byte) 88);
					spawn(233261, 1375.0513f, 1531.0963f, 597.12115f, (byte) 16);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDKamar_DrakanH_Spawn());
					break;
				case 18:
					List<WorldPosition> temp = new ArrayList<>(generalsPos);
					int index = Rnd.get(temp.size());
					WorldPosition pos1 = temp.remove(index);
					spawn(232854, pos1.getX(), pos1.getY(), pos1.getZ(), pos1.getHeading());
					index = Rnd.get(temp.size());
					pos1 = temp.remove(index);
					spawn(232853, pos1.getX(), pos1.getY(), pos1.getZ(), pos1.getHeading());
					index = Rnd.get(temp.size());
					pos1 = temp.remove(index);
					spawn(232852, pos1.getX(), pos1.getY(), pos1.getZ(), pos1.getHeading());
					spawn(232846, 1442.18f, 1370.7f, 600.6902f, (byte) 40);
					spawn(232846, 1434.45f, 1365.7f, 600.70776f, (byte) 40);
					spawn(232846, 1178.58f, 1445.6f, 586.5563f, (byte) 35);
					spawn(232846, 1166.8f, 1442.0f, 586.5563f, (byte) 35);
					spawn(232846, 1427.12f, 1621.19f, 599.9493f, (byte) 70);
					spawn(232846, 1431.09f, 1613.77f, 599.9493f, (byte) 70);
					// spawn Bark
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDKamar_DrakanGeneral_Spawn());
					break;
				case 25:
					spawn(232857, 1250.54f, 1646.07f, 584.9f, (byte) 100);
					spawn(232859, 1246.65f, 1645.06f, 584.9f, (byte) 100);
					spawn(232859, 1253.43f, 1649.13f, 584.9f, (byte) 100);
					spawn(232858, 1388.45f, 1438.7f, 600, (byte) 40);
					spawn(232860, 1394, 1440.34f, 600, (byte) 40);
					spawn(232860, 1385.74f, 1435.5f, 600, (byte) 40);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDKamar_LightDarkGeneral_Spawn());
					if (timeCheckTask != null && !timeCheckTask.isDone()) {
						timeCheckTask.cancel(true);
					}
					break;
			}
		}, 0, 60000);
	}

	private void stopInstance() {
		if (instanceTask != null && !instanceTask.isDone()) {
			instanceTask.cancel(true);
		}
		if (timeCheckTask != null && !timeCheckTask.isDone()) {
			timeCheckTask.cancel(true);
		}
		if (kamarReward.isRewarded()) {
			return;
		}
		kamarReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
		Race winnerRace = kamarReward.getRaceWithHighestPoints();
		instance.forEachPlayer(player -> {
			KamarPlayerReward reward = kamarReward.getPlayerReward(player.getObjectId());
			reward.setKamarBox(1);
			reward.setBonusReward(Rnd.get(2300, 2700));
			if (reward.getRace() == winnerRace) {
				reward.setGloryPoints(50);
				reward.setKamarBox(1);
				reward.setBaseReward(kamarReward.getWinnerApReward());
			} else {
				reward.setGloryPoints(10);
				reward.setBaseReward(kamarReward.getLoserApReward());
			}
			sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new KamarBattlefieldScoreInfo(kamarReward, 5, player.getObjectId()), getTime()));
			AbyssPointsService.addAp(player, reward.getBaseReward() + reward.getBonusReward());
			if (reward.getGloryPoints() > 0)
				GloryPointsService.increaseGpBy(player.getObjectId(), reward.getGloryPoints());
			if (reward.getBloodMarks() > 0) {
				ItemService.addItem(player, 1860000236, reward.getBloodMarks());
			}
			if (reward.getKamarBox() > 0) {
				ItemService.addItem(player, 188052670, reward.getKamarBox());
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
		if (instanceTask != null && !instanceTask.isDone()) {
			instanceTask.cancel(true);
		}
		if (timeCheckTask != null && !timeCheckTask.isDone()) {
			timeCheckTask.cancel(true);
		}
	}

	private void updatePoints(int points, Race race, boolean check, String npcL10n, Player player) {
		if (check && !kamarReward.isStartProgress()) {
			return;
		}
		if (npcL10n != null)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(npcL10n, points));
		kamarReward.addPointsByRace(race, points);
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new KamarBattlefieldScoreInfo(kamarReward, 10, race == Race.ELYOS ? 0 : 1), getTime()));
		int diff = Math.abs(kamarReward.getAsmodiansPoints() - kamarReward.getElyosPoints());
		if (diff >= 20000) {
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
			case 232856:
			case 232855:
			case 232852:
				points = 1250;
				break;
			case 701807:
			case 701808:
			case 701911:
			case 701912:
				points = 225;
				break;
			case 232847:
			case 232848:
			case 232849:
			case 232850:
			case 232851:
			case 233261:
				points = 140;
				break;
			case 233260:
			case 232841:
			case 232842:
			case 232843:
			case 232844:
			case 232845:
			case 232846:
				points = 50;
				break;
			case 801771:
				points = 75;
				break;
			case 232853:
				points = 3500;
				stopInstance();
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
		instance.setDoorState(4, true);
		instance.setDoorState(8, true);
		instance.setDoorState(10, true);
		instance.setDoorState(11, true);
	}

	private void sendPacket(AionServerPacket packet) {
		PacketSendUtility.broadcastToMap(instance, packet);
	}

	@Override
	public void onInstanceCreate() {
		kamarReward = new KamarReward();
		kamarReward.setInstanceProgressionType(InstanceProgressionType.REINFORCE_MEMBER);
		startInstanceTask();
	}

	@Override
	public InstanceReward<?> getInstanceReward() {
		return kamarReward;
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		kamarReward.portToPosition(player, instance);
		return true;
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		KamarPlayerReward ownerReward = kamarReward.getPlayerReward(player.getObjectId());
		ownerReward.endBoostMoraleEffect(player);
		ownerReward.applyBoostMoraleEffect(player);
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new KamarBattlefieldScoreInfo(kamarReward, 4, player.getObjectId()), getTime()));
		PacketSendUtility.sendPacket(player, new SM_DIE(player.canUseRebirthRevive(), false, 0, 8));
		if (lastAttacker instanceof Player) {
			if (lastAttacker.getRace() != player.getRace()) {
				updatePoints(150, lastAttacker.getRace(), true, null, (Player) lastAttacker);
				PacketSendUtility.sendPacket((Player) lastAttacker, new SM_SYSTEM_MESSAGE(1400277, 150));
				kamarReward.incrementKillsByRace(lastAttacker.getRace());
				sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(),
					new KamarBattlefieldScoreInfo(kamarReward, 10, lastAttacker.getRace() == Race.ELYOS ? 0 : 1), getTime()));
			}
		}
		updatePoints(-100, player.getRace(), true, null, player);
		return true;
	}

	private int getTime() {
		long result = System.currentTimeMillis() - instanceTime;
		if (result < 180000) {
			return (int) (180000 - result);
		} else if (result < 240000) {
			return (int) (60000 - (result - 180000));
		} else if (result < 2040000) {
			return (int) (1800000 - (result - 240000));
		}
		return 0;
	}

}
