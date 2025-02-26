package instance;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * According to the version 4.8 the instance was simplified and shortened. The starting point of this
 * instance was set to the area behind Zantaraz and everything prior to that was removed. Similar
 * to the harder version called Occupied Rentus Base, a minigame was added, which if successfully
 * completed allows to open a chest with additional loot after defeating Brigade General Vasharti.<br>
 * <br>
 * August 7th, 2016
 * 
 * @author xTz, Estrayl
 */
@InstanceID(300280000)
public class RentusBaseInstance extends GeneralInstanceHandler {

	private final AtomicBoolean isRaceKnown = new AtomicBoolean();
	private final AtomicBoolean isXastaEventStarted = new AtomicBoolean();

	public RentusBaseInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onDie(final Npc npc) {
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 217315: // Umatha the Crazed
				if (isDeadNpc(217316)) {
					instance.setDoorState(145, true);
					deleteAliveNpcs(701156);
				}
				break;
			case 217316: // Ambusher Kiriana
				if (isDeadNpc(217315)) {
					instance.setDoorState(145, true);
					deleteAliveNpcs(701156);
				}
				break;
			case 217311: // Kuhara
				spawn(219215, 141.54f, 255.06f, 213f, (byte) 25);
				instance.setDoorState(43, false);
				instance.setDoorState(150, true);
				npc.getController().delete();
				break;
			case 217317: // Archmagus Upadi
				instance.setDoorState(70, true);
				break;
			case 217313: // Brigade General Vasharti
				deleteAliveNpcs(799669);
				instance.setDoorState(70, true);
				spawn(730520, 193.6f, 436.5f, 262f, (byte) 86); // rentus base exit
				spawn(833047, 195.48f, 413.87f, 260.97f, (byte) 27); // rentus supplies storage box
				Npc ariana = (Npc) spawn(799670, 183.736f, 391.392f, 260.571f, (byte) 26);
				PacketSendUtility.broadcastMessage(ariana, 1500417, 5000);
				PacketSendUtility.broadcastMessage(ariana, 1500418, 8000);
				PacketSendUtility.broadcastMessage(ariana, 1500419, 11000);
				spawnEndEvent(800227, "3002800003", 2000);
				spawnEndEvent(800227, "3002800004", 2000);
				spawnEndEvent(800228, "3002800007", 4000);
				spawnEndEvent(800227, "3002800005", 6000);
				spawnEndEvent(800228, "3002800006", 8000);
				spawnEndEvent(800229, "3002800008", 10000);
				spawnEndEvent(800229, "3002800009", 10000);
				spawnEndEvent(800230, "30028000010", 12000);
				spawnEndEvent(800230, "30028000011", 12000);
				break;
			case 282394:
				spawn(282395, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				npc.getController().delete();
				break;
			case 283000:
			case 283001:
				npc.getController().delete();
				break;
			case 236283:
				spawn(236284, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				npc.getController().delete();
				break;
		}
	}

	@Override
	public void onAggro(Npc npc) {
		switch (npc.getNpcId()) {
			case 217311: // Kuhara
				instance.setDoorState(43, true);
				PacketSendUtility.broadcastMessage(npc, 1500393);
				break;
			case 217313: // Vasharti
				instance.setDoorState(70, false);
				break;
		}
	}

	private void spawnEndEvent(int npcId, String walkern, int time) {
		sp(npcId, 193.39548f, 435.56158f, 260.57135f, (byte) 86, time, walkern);
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 702677:
			case 702678:
			case 702679:
			case 702680:
			case 702681:
			case 702682:
				TeleportService.teleportTo(player, npc.getWorldId(), npc.getInstanceId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				SkillEngine.getInstance().applyEffectDirectly(21806, npc, player);
				npc.getController().delete();
				break;
			case 702683:
			case 702684:
			case 702685:
			case 702686:
			case 702687:
			case 702688:
				TeleportService.teleportTo(player, npc.getWorldId(), npc.getInstanceId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				SkillEngine.getInstance().applyEffectDirectly(21805, npc, player);
				npc.getController().delete();
				break;
			case 701097:
				npc.getController().delete();
				break;
			case 701100:
				if (instance.getNpc(799543) == null)
					spawn(799543, 511.227f, 613.762f, 158.179f, (byte) 0);
				break;
		}
	}

	private void sp(final int npcId, final float x, final float y, final float z, final byte h, final int time, final String walkern) {
		final Npc npc = (Npc) spawn(npcId, x, y, z, h);
		ThreadPoolManager.getInstance().schedule(() -> {
			npc.getSpawn().setWalkerId(walkern);
			WalkManager.startWalking((NpcAI) npc.getAi());
			npc.setState(CreatureState.WALK_MODE, true);
			PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.CHANGE_SPEED, 0, npc.getObjectId()));
		}, time);
	}

	private boolean isDeadNpc(int npcId) {
		return (getNpc(npcId) == null || getNpc(npcId).isDead());
	}

	@Override
	public void onCreatureDetected(Npc detector, Creature detected) {
		if (detected instanceof Player) {
			if (detector.getNpcId() == 856056 && isXastaEventStarted.compareAndSet(false, true)) {
				sp(236271, 521.33f, 499.49f, 179.946f, (byte) 27, 2000, "300620000_Xasta_Path");
				detector.getController().delete();
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDYUN_RASTA_SPAWN_01());
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDYUN_RASTA_SPAWN_02(), 2000);
			}
		}
	}

	@Override
	public void onSpecialEvent(Npc npc) {
		if (npc.getNpcId() == 236271) {
			if (npc.getLifeStats().getHpPercentage() <= 50)
				spawn(217310, 354.53f, 596.26f, 148.298f, (byte) 100);
			else
				spawn(217309, 354.53f, 596.26f, 148.298f, (byte) 100);
			Npc drakanBarricade = getNpc(856015);
			if (drakanBarricade != null)
				drakanBarricade.getController().die(npc);
			npc.getController().delete();
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		if (isRaceKnown.compareAndSet(false, true)) {
			if (player.getRace() == Race.ELYOS) {
				for (int npcId = 702677; npcId <= 702682; npcId++) {
					Npc npc = getNpc(npcId);
					spawn(npcId + 6, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), npc.getSpawn().getStaticId());
					npc.getController().delete();
				}
			}
		}
	}

	@Override
	public boolean isBoss(Npc npc) {
		return switch (npc.getNpcId()) {
			case 217309, 217310, 217311, 217315, 217316, 217317, 217313 -> true;
			default -> false;
		};
	}
}
