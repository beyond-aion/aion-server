package instance;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_IDStation_Doping_01_AD;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author xTz
 */
@InstanceID(300240000)
public class AturamSkyFortressInstance extends GeneralInstanceHandler {

	private final AtomicBoolean msgIsSent = new AtomicBoolean();
	private final AtomicInteger officerKilled = new AtomicInteger();
	private final AtomicInteger chiefKilled = new AtomicInteger();
	private final AtomicInteger generators = new AtomicInteger();
	private boolean isInstanceDestroyed;

	public AturamSkyFortressInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onDie(Npc npc) {
		if (isInstanceDestroyed) {
			return;
		}

		switch (npc.getNpcId()) {
			case 702651:
				spawn(282281, 524.1896f, 489.7742f, 649.916f, (byte) 34);
				break;
			case 700982:
				spawn(282279, 467.7094f, 465.6622f, 647.93896f, (byte) 40);
				break;
			case 700983:
				spawn(282278, 449.5576f, 420.7812f, 652.9143f, (byte) 89);
				instance.setDoorState(68, true);
				break;
			case 702650:
				spawn(282277, 572.8088f, 459.4094f, 647.93896f, (byte) 15);
				break;
			case 702652:
				spawn(282280, 581.1f, 401.3544f, 648.6401f, (byte) 9);
				break;
			case 217373:
				spawn(730375, 374.85f, 424.32f, 653.52f, (byte) 0);
				break;
			case 701043:
				npc.getController().delete();
				deleteAliveNpcs(701030);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDStation_HugenNM_00());
				break;
			case 217371:
				spawn(730374, npc.getX(), npc.getY(), npc.getZ(), (byte) 0);
				break;
			case 217370:
				int killed1 = officerKilled.incrementAndGet();
				if (killed1 == 4) {
					instance.setDoorState(174, true);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDStation_3FDoor_311());
					startOfficerWalkerEvent();
				} else if (killed1 == 8) {
					instance.setDoorState(175, true);
					startMarbataWalkerEvent();
				}
				npc.getController().delete();
				break;
			case 217656:
				int killed2 = chiefKilled.incrementAndGet();
				if (killed2 == 1) {
					startOfficerWalkerEvent();
				} else if (killed2 == 2) {
					instance.setDoorState(178, true);
					instance.setDoorState(308, false); // reopen side windows
					ThreadPoolManager.getInstance().schedule(() -> instance.setDoorState(307, true), 10000); // close side windows
				}
				npc.getController().delete();
				break;
			case 217382:
				instance.setDoorState(307, false); // reopen side windows
				instance.setDoorState(230, true);
				Player player = npc.getAggroList().getMostPlayerDamage();
				if (player != null) {
					AbyssPointsService.addAp(player, 540);
				}
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDStation_3FDoor_322());
				break;
			case 218577:
				spawn(217382, 258.3894f, 796.7554f, 901.6453f, (byte) 80);
				break;
			case 701029:
				Npc boss = instance.getNpc(217371);
				int used = generators.incrementAndGet();
				if (boss != null) {
					if (used == 1) {
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDStation_HugenNM_01());
					} else if (used == 2) {
						boss.getEffectController().removeEffect(19406);
						SkillEngine.getInstance().getSkill(boss, 19407, 1, boss).useNoAnimationSkill();
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDStation_HugenNM_02());
					} else if (used == 3) {
						boss.getEffectController().removeEffect(19407);
						SkillEngine.getInstance().getSkill(boss, 19408, 1, boss).useNoAnimationSkill();
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDStation_HugenNM_03());
					} else if (used == 4) {
						boss.getEffectController().removeEffect(19408);
						SkillEngine.getInstance().getSkill(boss, 18117, 1, boss).useNoAnimationSkill();
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDStation_HugenNM_04());
					}
				}
				npc.getController().delete();
				break;
			case 217369:
			case 217368:
			case 217655:
				npc.getController().delete();
				break;
		}
	}

	private void startMarbataWalkerEvent() {
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDStation_3FDoor_311());
		startWalk((Npc) spawn(218577, 193.45583f, 802.1455f, 900.7575f, (byte) 103), "3002400009");
		startWalk((Npc) spawn(217655, 198.34431f, 801.4107f, 900.66125f, (byte) 110), "30024000010");
		startWalk((Npc) spawn(217655, 197.13315f, 798.7863f, 900.6499f, (byte) 110), "30024000011");
	}

	private void startWalk(final Npc npc, final String walkId) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isInstanceDestroyed) {
				npc.getSpawn().setWalkerId(walkId);
				WalkManager.startWalking((NpcAI) npc.getAi());
				npc.setState(CreatureState.ACTIVE, true);
				PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.CHANGE_SPEED, 0, npc.getObjectId()));
			}
		}, 2000);
	}

	private void startOfficerWalkerEvent() {
		startWalk((Npc) spawn(217655, 146.53816f, 713.5974f, 901.0108f, (byte) 111), "3002400003");
		startWalk((Npc) spawn(217655, 144.84991f, 720.9318f, 901.0604f, (byte) 96), "3002400004");
		startWalk((Npc) spawn(217655, 146.19899f, 709.60455f, 901.0078f, (byte) 110), "3002400005");
		startWalk((Npc) spawn(217656, 144.11845f, 716.8327f, 901.046f, (byte) 100), "3002400006");
		startWalk((Npc) spawn(217369, 144.96825f, 712.83344f, 901.0133f, (byte) 110), "3002400007");
		startWalk((Npc) spawn(217369, 144.75804f, 718.4293f, 901.05493f, (byte) 80), "3002400008");
	}

	@Override
	public void onInstanceCreate() {
		instance.setDoorState(177, true);
		Npc npc = instance.getNpc(217371);
		if (npc != null) {
			SkillEngine.getInstance().getSkill(npc, 19406, 1, npc).useNoAnimationSkill();
		}
	}

	@Override
	public void onInstanceDestroy() {
		isInstanceDestroyed = true;
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 730398:
				player.getLifeStats().increaseHp(SM_ATTACK_STATUS.TYPE.HP, 5205, npc);
				player.getLifeStats().increaseMp(SM_ATTACK_STATUS.TYPE.MP, 5205, 0, LOG.REGULAR);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDStation_Doping_02());
				npc.getController().delete();
				break;
			case 730397:
				SkillEngine.getInstance().getSkill(npc, 19520, 51, player).useNoAnimationSkill();
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDStation_Doping_01());
				break;
			case 730410:
				instance.setDoorState(90, true);
				break;
			case 731533:
				SkillEngine.getInstance().getSkill(player, 21807, 1, player).useNoAnimationSkill();
				break;
			case 731534:
				SkillEngine.getInstance().getSkill(player, 21808, 1, player).useNoAnimationSkill();
				break;
		}
	}

	@Override
	public void onEnterZone(Player player, ZoneInstance zone) {
		if (zone.getAreaTemplate().getZoneName() == ZoneName.get("SKY_FORTRESS_WAREHOUSE_ZONE_300240000")) {
			// wtf is that? Notify only one player ?
			if (msgIsSent.compareAndSet(false, true)) {
				PacketSendUtility.sendPacket(player, STR_MSG_IDStation_Doping_01_AD());
			}
		}
	}

	@Override
	public void onPlayMovieEnd(Player player, int movieId) {
		if (movieId == 471)
			ThreadPoolManager.getInstance().schedule(() -> instance.setDoorState(308, true), 10000); // close side windows
	}
}
