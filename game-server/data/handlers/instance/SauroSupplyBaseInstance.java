package instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Cheatkiller
 */
@InstanceID(301130000)
public class SauroSupplyBaseInstance extends GeneralInstanceHandler {

	private static final List<WorldPosition> chestPoints = new ArrayList<>();
	private Future<?> scheduledGeneratorTask;
	static {
		chestPoints.add(new WorldPosition(301130000, 253.97533f, 363.97156f, 159.64023f, (byte) 0));
		chestPoints.add(new WorldPosition(301130000, 262.36151f, 393.78619f, 156.83209f, (byte) 30));
		chestPoints.add(new WorldPosition(301130000, 263.41193f, 463.63086f, 156.70573f, (byte) 90));
		chestPoints.add(new WorldPosition(301130000, 282.64951f, 330.15793f, 159.36792f, (byte) 0));
		chestPoints.add(new WorldPosition(301130000, 300.37936f, 332.13354f, 159.74071f, (byte) 60));
		chestPoints.add(new WorldPosition(301130000, 325.39862f, 364.31143f, 160.89003f, (byte) 60));
		chestPoints.add(new WorldPosition(301130000, 325.41577f, 388.60355f, 160.89003f, (byte) 60));
		chestPoints.add(new WorldPosition(301130000, 431.13638f, 481.81009f, 183.08244f, (byte) 0));
		chestPoints.add(new WorldPosition(301130000, 465.58185f, 412.19705f, 183.75404f, (byte) 90));
		chestPoints.add(new WorldPosition(301130000, 467.54926f, 377.6048f, 182.8183f, (byte) 30));
		chestPoints.add(new WorldPosition(301130000, 495.8497f, 359.10245f, 182.77852f, (byte) 30));
		chestPoints.add(new WorldPosition(301130000, 496.02997f, 381.77118f, 182.94859f, (byte) 30));
		chestPoints.add(new WorldPosition(301130000, 497.55228f, 402.35077f, 183.24211f, (byte) 30));
		chestPoints.add(new WorldPosition(301130000, 510.22513f, 535.37701f, 182.3832f, (byte) 90));
		chestPoints.add(new WorldPosition(301130000, 518.31726f, 463.9472f, 182.00262f, (byte) 45));
		chestPoints.add(new WorldPosition(301130000, 519.60052f, 505.98093f, 182.13989f, (byte) 60));
		chestPoints.add(new WorldPosition(301130000, 569.55652f, 495.64389f, 193.63138f, (byte) 0));
		chestPoints.add(new WorldPosition(301130000, 576.3725f, 500.66254f, 202.54437f, (byte) 105));
		chestPoints.add(new WorldPosition(301130000, 576.96289f, 471.08911f, 202.54466f, (byte) 15));
		chestPoints.add(new WorldPosition(301130000, 599.65344f, 361.96112f, 204.74123f, (byte) 0));
		chestPoints.add(new WorldPosition(301130000, 656.28381f, 398.28476f, 204.74123f, (byte) 90));
		chestPoints.add(new WorldPosition(301130000, 666.01855f, 363.71048f, 204.74123f, (byte) 90));
		chestPoints.add(new WorldPosition(301130000, 666.21979f, 345.6026f, 204.63773f, (byte) 30));
	}

	public SauroSupplyBaseInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		// spawn Sauro Base Grave Robber (pool=1)
		switch (Rnd.get(1, 5)) {
			case 1 -> spawn(230846, 460.69705f, 390.10602f, 182.75943f, (byte) 0);
			case 2 -> spawn(230846, 463.92523f, 402.63937f, 183.31064f, (byte) 0);
			case 3 -> spawn(230846, 496.42526f, 412.04755f, 182.72771f, (byte) 0);
			case 4 -> spawn(230846, 497.27997f, 361.05948f, 182.45613f, (byte) 0);
			case 5 -> spawn(230846, 497.55908f, 389.86649f, 182.8175f, (byte) 0);
		}

		// spawn Commander Ranodim
		spawn(Rnd.get(new int[] { 230852, 233316, 233317 }), 289.8795f, 343.75858f, 159.34445f, (byte) 90);

		List<WorldPosition> temp = new ArrayList<>(chestPoints);
		for (int i = 0; i < 8; i++) {
			int index = Rnd.nextInt(temp.size());
			WorldPosition pos = temp.remove(index);
			spawn(230847, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
		}
		for (WorldPosition worldPosition : temp) {
			spawn(230848, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getHeading());
		}
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 230837:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDVritra_Base_DoorOpen_03());
				instance.setDoorState(372, true);
				break;
			case 230849:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDVritra_Base_DoorOpen_01());
				instance.setDoorState(383, true);
				break;
			case 230850:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDVritra_Base_DoorOpen_04());
				instance.setDoorState(375, true);
				break;
			case 230851:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDVritra_Base_DoorOpen_02());
				instance.setDoorState(59, true);
				break;
			case 230852: // Ranodim
			case 233316:
			case 233317:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDVritra_Base_DoorOpen_06());
				instance.setDoorState(388, true);
				break;
			case 233255:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDVritra_Base_DoorOpen_05());
				instance.setDoorState(378, true);
				break;
			case 230838:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDVritra_Base_DoorOpen_07());
				instance.setDoorState(376, true);
				break;
			case 230853:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDVritra_Base_DoorOpen_08());
				spawn(730872, 129.16417f, 432.32669f, 153.33147f, (byte) 2, 3);// Boss Teleporter
				break;
			case 230854:
				spawn(801967, 91.69f, 889.8f, 411.45f, (byte) 120);// Sauro Exit
				break;
			case 230855:
				spawn(801967, 289.38f, 889.82f, 411.45f, (byte) 120);// Sauro Exit
				break;
			case 230856:
				spawn(801967, 485.1f, 889.93f, 411.45f, (byte) 0);// Sauro Exit
				break;
			case 230857:
				spawn(801967, 721.84f, 889.93f, 411.45f, (byte) 60);// Sauro Exit
				cancelTask();
				break;
			case 230858:
				spawn(801967, 880.82f, 889.93f, 411.45f, (byte) 120);// Sauro Exit
				break;
			case 284437: // protective shield generator
			case 284445:
			case 284446:
				if (instance.getNpc(230857) != null && !instance.getNpc(230857).isDead()) {
					spawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
					Npc ahuradim = instance.getNpc(230857);
					if (ahuradim != null && !ahuradim.isDead()) {
						if (npc.getNpcId() == 284437) {
							startGeneratorTask(Rnd.nextBoolean() ? 284445 : 284446, ahuradim);
						} else if (npc.getNpcId() == 284445) {
							startGeneratorTask(Rnd.nextBoolean() ? 284446 : 284437, ahuradim);
						} else {
							startGeneratorTask(Rnd.nextBoolean() ? 284437 : 284445, ahuradim);
						}
					}
				}
				npc.getController().delete();
				ThreadPoolManager.getInstance().schedule(() -> {
					Npc gen1 = instance.getNpc(284437);
					Npc gen2 = instance.getNpc(284445);
					Npc gen3 = instance.getNpc(284446);
					if (gen1 != null) {
						SkillEngine.getInstance().getSkill(gen1, 20773, 1, gen1).useWithoutPropSkill();
					}
					if (gen2 != null) {
						SkillEngine.getInstance().getSkill(gen2, 20773, 1, gen2).useWithoutPropSkill();
					}
					if (gen3 != null) {
						SkillEngine.getInstance().getSkill(gen3, 20773, 1, gen3).useWithoutPropSkill();
					}
				}, 500);
				break;
		}
	}

	private void startGeneratorTask(int npcId, Npc ahuradim) {
		scheduledGeneratorTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (ahuradim.isDead())
				return;
			Npc generator = instance.getNpc(npcId);
			if (generator == null || generator.isDead())
				return;
			generator.setTarget(ahuradim);
			PacketSendUtility.broadcastMessage(generator, 1501014);
			scheduledGeneratorTask = ThreadPoolManager.getInstance().schedule(() -> {
				if (!generator.isDead()) {
					PacketSendUtility.broadcastMessage(generator, 1501015);
					SkillEngine.getInstance().getSkill(generator, 21200, 1, generator).useWithoutPropSkill();
				}
			}, 15 * 1000);
		}, 40 * 1000);
	}

	private void cancelTask() {
		if (scheduledGeneratorTask != null) {
			scheduledGeneratorTask.cancel(false);
		}
	}

	@Override
	public void onBackHome(Npc npc) {
		if (npc.getNpcId() == 230857) {
			cancelTask();
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 730876:
				TeleportService.teleportTo(player, player.getWorldId(), player.getInstanceId(), 721.84f, 889.93f, 411.45f, (byte) 60,
					TeleportAnimation.FADE_OUT_BEAM);
				break;
			case 730877:
				TeleportService.teleportTo(player, player.getWorldId(), player.getInstanceId(), 880.82f, 889.93f, 411.45f, (byte) 120,
					TeleportAnimation.FADE_OUT_BEAM);
				break;
		}
	}

	@Override
	public boolean isBoss(Npc npc) {
		return switch (npc.getNpcId()) {
			case 230849, 230850, 230851, 230853, 230857, 230858, 233255, 233256, 233257, 233258 -> true;
			default -> false;
		};
	}
}
