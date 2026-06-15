package instance;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Cheatkiller
 */
@InstanceID(300510000)
public class TiamatStrongHoldInstance extends GeneralInstanceHandler {

	private final AtomicInteger drakans = new AtomicInteger();
	private final AtomicBoolean startSuramaEvent = new AtomicBoolean();
	private boolean isInstanceDestroyed;

	public TiamatStrongHoldInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onDie(Npc npc) {
		if (isInstanceDestroyed) {
			return;
		}
		switch (npc.getNpcId()) {
			case 730612:
				firstWave();
				break;
			case 219373:// ex 219421
			case 219369:// ex 219417
			case 219411:// ex 219459
			case 219370:// ex 219418
				int killedDrakans = drakans.incrementAndGet();
				if (killedDrakans == 5)
					secondWave();
				else if (killedDrakans == 12)
					thirdWave();
				break;
			case 219352: // ex 219400
				sendMsg(SM_SYSTEM_MESSAGE.STR_IDTIAMAT_TIAMAT_REWARD_SPAWN());
				spawn(283177, 1175.65f, 1069.08f, 498.52f, (byte) 0); // ex 283913
				spawn(701501, 1075.4409f, 1078.5071f, 787.685f, (byte) 16);
				instance.setDoorState(48, true);
				spawnKahrun();
				break;
			case 219357:// ex 219405
				sendMsg(SM_SYSTEM_MESSAGE.STR_IDTIAMAT_TIAMAT_REWARD_SPAWN());
				spawn(701501, 1077.1716f, 1058.1995f, 787.685f, (byte) 61);
				instance.setDoorState(37, true);
				break;
			case 219358:// ex 219406
				sendMsg(SM_SYSTEM_MESSAGE.STR_IDTIAMAT_TIAMAT_REWARD_SPAWN());
				spawn(701541, 677.35785f, 1069.5361f, 499.86716f, (byte) 0);
				spawn(701527, 1073.948f, 1068.8732f, 787.685f, (byte) 61);
				spawn(730622, 652.4821f, 1069.0302f, 498.7787f, (byte) 0, 82);
				spawn(283178, 679.88f, 1068.88f, 504.2f, (byte) 119);// ex 283916
				spawnExitIfCleared();
				break;
			case 219353:// ex 219401
				sendMsg(SM_SYSTEM_MESSAGE.STR_IDTIAMAT_TIAMAT_REWARD_SPAWN());
				spawn(701501, 1071.5909f, 1040.6797f, 787.685f, (byte) 23);
				instance.setDoorState(711, true);
				break;
			case 219354:// ex 219402
				sendMsg(SM_SYSTEM_MESSAGE.STR_IDTIAMAT_TIAMAT_REWARD_SPAWN());
				spawn(283179, 1030.03f, 301.83f, 411f, (byte) 26);// ex 283914
				spawn(701501, 1086.274f, 1098.3997f, 787.685f, (byte) 90);
				spawn(730622, 1029.792f, 267.0502f, 409.7982f, (byte) 0, 83);
				spawnExitIfCleared();
				break;
			case 219355:// ex 219403
				sendMsg(SM_SYSTEM_MESSAGE.STR_IDTIAMAT_TIAMAT_REWARD_SPAWN());
				spawn(701501, 1063.5973f, 1092.7402f, 787.685f, (byte) 107);
				instance.setDoorState(51, true);
				instance.setDoorState(54, true);
				instance.setDoorState(78, true);
				instance.setDoorState(11, true);
				instance.setDoorState(79, true);
				break;
			case 219356:// ex 219404
				sendMsg(SM_SYSTEM_MESSAGE.STR_IDTIAMAT_TIAMAT_REWARD_SPAWN());
				spawn(701501, 1099.8691f, 1047.1895f, 787.685f, (byte) 64);
				spawn(730622, 644.4221f, 1319.6221f, 488.7422f, (byte) 0, 15);
				spawn(800438, 665.63409f, 1319.7051f, 487.9f, (byte) 61);
				spawn(283180, 629.1f, 1319.5f, 501.2f, (byte) 0);// ex 283915
				spawnExitIfCleared();
				break;
		}
	}

	private void firstWave() {
		ThreadPoolManager.getInstance().schedule(() -> {
			attackPlayer((Npc) spawn(219373, 1505.09f, 1068.54f, 491.38f, (byte) 0));
			attackPlayer((Npc) spawn(219369, 1510.54f, 1058.04f, 491.5f, (byte) 0));
			attackPlayer((Npc) spawn(219411, 1517.38f, 1063.5f, 491.52f, (byte) 0));
			attackPlayer((Npc) spawn(219411, 1516.81f, 1073.6f, 491.52f, (byte) 0));
			attackPlayer((Npc) spawn(219369, 1510.41f, 1078.8f, 491.52f, (byte) 0));
		}, 5000);
	}

	private void secondWave() {
		attackPlayer((Npc) spawn(219370, 1426.08f, 1068.41f, 491.38f, (byte) 0));
		attackPlayer((Npc) spawn(219369, 1430.3f, 1061.13f, 491.5f, (byte) 0));
		attackPlayer((Npc) spawn(219411, 1428.5f, 1056.6f, 491.52f, (byte) 0));
		attackPlayer((Npc) spawn(219411, 1439.49f, 1058.5f, 491.4f, (byte) 0));
		attackPlayer((Npc) spawn(219369, 1430.3f, 1075.49f, 491.52f, (byte) 0));
		attackPlayer((Npc) spawn(219411, 1439.4f, 1078.6f, 491.4f, (byte) 0));
		attackPlayer((Npc) spawn(219411, 1428.5f, 1080.9f, 491.46f, (byte) 0));
	}

	private void thirdWave() {
		attackPlayer((Npc) spawn(219370, 1296.1f, 1068.3f, 491.38f, (byte) 0));
		attackPlayer((Npc) spawn(219411, 1290.9f, 1059.13f, 491.5f, (byte) 0));
		attackPlayer((Npc) spawn(219369, 1300.6f, 1056.4f, 491.52f, (byte) 0));
		attackPlayer((Npc) spawn(219411, 1302.78f, 1053.55f, 491.4f, (byte) 0));
		attackPlayer((Npc) spawn(219411, 1290.94f, 1077.8f, 491.52f, (byte) 0));
		attackPlayer((Npc) spawn(219369, 1300.6f, 1080.3f, 491.4f, (byte) 0));
		attackPlayer((Npc) spawn(219411, 1302.78f, 1082.8f, 491.5f, (byte) 0));
	}

	private void attackPlayer(final Npc npc) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isInstanceDestroyed) {
				for (Player player : instance.getPlayersInside()) {
					npc.setTarget(player);
					npc.getAi().setStateIfNot(AIState.WALKING);
					npc.setState(CreatureState.ACTIVE, true);
					npc.getMoveController().moveToTargetObject();
					PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.CHANGE_SPEED, 0, npc.getObjectId()));
				}
			}
		}, 2000);
	}

	private void spawnKahrun() {
		ThreadPoolManager.getInstance().schedule(() -> {
			moveToForward((Npc) spawn(800463, 1201.272f, 1074.5463f, 491f, (byte) 61), 1039.5f, 1075.9f, 497.3f, false);
			moveToForward((Npc) spawn(800463, 1201.272f, 1072.5137f, 491f, (byte) 61), 1130, 1072, 497.3f, false);
			moveToForward((Npc) spawn(800463, 1192.8656f, 1071.1085f, 491f, (byte) 61), 1112, 1070, 497, false);
			moveToForward((Npc) spawn(800463, 1201.272f, 1064.1759f, 491f, (byte) 61), 1039, 1061, 497.3f, false);
			moveToForward((Npc) spawn(800463, 1208.4175f, 1071.1797f, 491f, (byte) 61), 1133, 1072.5f, 497.3f, false);
			moveToForward((Npc) spawn(800463, 1192.8656f, 1068.3411f, 491f, (byte) 61), 1114, 1067, 496.7f, false);
			moveToForward((Npc) spawn(800463, 1208.4175f, 1068.3979f, 491f, (byte) 61), 1133.32f, 1066.47f, 497.3f, false);
			moveToForward((Npc) spawn(800463, 1201.272f, 1066.2085f, 491f, (byte) 61), 1128.8f, 1067, 497.3f, false);
			moveToForward((Npc) spawn(800380, 1190.323f, 1068.1558f, 491.03488f, (byte) 61), 1108, 1066, 497.3f, false);
			moveToForward((Npc) spawn(800374, 1188.4259f, 1066.4757f, 491.55029f, (byte) 61), 1094, 1064, 497.4f, true);
			moveToForward((Npc) spawn(800374, 1188.2158f, 1074.2047f, 491.55029f, (byte) 61), 1092.5f, 1074.6f, 497.4f, true);
			moveToForward((Npc) spawn(800376, 1190.3859f, 1071.6548f, 491.03488f, (byte) 61), 1109, 1073, 497.2f, false);
			moveToForward((Npc) spawn(800461, 1184.7582f, 1068.6f, 491.03488f, (byte) 61), 1111, 1068.6f, 497.33f, false);
			moveToForward((Npc) spawn(800460, 1184.7358f, 1070.77f, 491.03488f, (byte) 61), 1111, 1071, 497, false);
			moveToForward((Npc) spawn(800347, 1178.0425f, 1072.28f, 491.02545f, (byte) 61), 1106, 1072, 497.2f, false);
			moveToForward((Npc) spawn(800336, 1178.0559f, 1069.6f, 491.02545f, (byte) 61), 1104, 1069, 497, true);
		}, 7000);
	}

	private void moveToForward(final Npc npc, float x, float y, float z, boolean despawn) {
		npc.getAi().setStateIfNot(AIState.WALKING);
		npc.setState(CreatureState.ACTIVE, true);
		npc.getMoveController().moveToPoint(x, y, z);
		PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.CHANGE_SPEED, 0, npc.getObjectId()));
		if (despawn) {
			ThreadPoolManager.getInstance().schedule(() -> {
				if (npc.getNpcId() == 800336) {
					spawn(800338, 1104, 1069f, 497, (byte) 61);
					Npc kahrun = getNpc(800338);
					PacketSendUtility.broadcastMessage(kahrun, 1500599, 1000);
					PacketSendUtility.broadcastMessage(kahrun, 1500600, 5000);
				}
				npc.getController().delete();
			}, 13000);
		}
	}

	private void spawnColonels() {
		switch (Rnd.nextInt(4)) {
			case 0:
				spawn(219364, 763.4179f, 1445.6504f, 495.6519f, (byte) 90);
				spawn(219395, 893.7009f, 1445.4846f, 495.6421f, (byte) 90);
				spawn(219395, 893.3f, 1190.71f, 495.6f, (byte) 30);
				spawn(219395, 762.6f, 1192.1f, 495.6f, (byte) 30);
				break;
			case 1:
				spawn(219395, 763.4179f, 1445.6504f, 495.6519f, (byte) 90);
				spawn(219364, 893.7009f, 1445.4846f, 495.6421f, (byte) 90);
				spawn(219395, 893.3f, 1190.71f, 495.6f, (byte) 30);
				spawn(219395, 762.6f, 1192.1f, 495.6f, (byte) 30);
				break;
			case 2:
				spawn(219395, 763.4179f, 1445.6504f, 495.6519f, (byte) 90);
				spawn(219395, 893.7009f, 1445.4846f, 495.6421f, (byte) 90);
				spawn(219364, 893.3f, 1190.71f, 495.6f, (byte) 30);
				spawn(219395, 762.6f, 1192.1f, 495.6f, (byte) 30);
				break;
			case 3:
				spawn(219395, 763.4179f, 1445.6504f, 495.6519f, (byte) 90);
				spawn(219395, 893.7009f, 1445.4846f, 495.6421f, (byte) 90);
				spawn(219395, 893.3f, 1190.71f, 495.6f, (byte) 30);
				spawn(219364, 762.6f, 1192.1f, 495.6f, (byte) 30);
				break;
		}
	}

	private void spawnExitIfCleared() {
		if (instance.getNpcs(219354, 219356, 219358).stream().allMatch(Creature::isDead)) {
			spawn(800464, 1119.7076f, 1071.1401f, 496.8615f, (byte) 119);
			spawn(800465, 1119.7421f, 1068.4998f, 496.8616f, (byte) 3);
			spawn(730629, 1121.3807f, 1069.8124f, 500.3319f, (byte) 0, 555);
		}
	}

	@Override
	public void onEnterZone(Player player, ZoneInstance zone) {
		if (zone.getAreaTemplate().getZoneName() == ZoneName.get("LAKSYAKA_LEGION_HQ_300510000")) {
			if (startSuramaEvent.compareAndSet(false, true)) {
				spawn(800433, 725.93f, 1319.9f, 490.7f, (byte) 61);
			}
		} else if (zone.getAreaTemplate().getZoneName() == ZoneName.get("GLORIOUS_NEXUS_300510000")) {
			player.getEffectController().removeEffect(300);
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		if (npc.getNpcId() == 701494)
			instance.setDoorState(22, true);
	}

	@Override
	public void onInstanceCreate() {
		instance.setDoorState(610, true);
		// instance.setDoorState(20, true);
		instance.setDoorState(706, true);
		spawnColonels();
	}

	@Override
	public void onInstanceDestroy() {
		isInstanceDestroyed = true;
	}

	@Override
	public boolean isBoss(Npc npc) {
		return switch (npc.getNpcId()) {
			case 219352, 219353, 219354, 219355, 219356, 219357, 219358 -> true;
			default -> false;
		};
	}
}
