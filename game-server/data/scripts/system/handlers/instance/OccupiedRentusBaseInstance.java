package instance;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Tibald
 * @modified Estrayl
 */
@InstanceID(300620000)
public class OccupiedRentusBaseInstance extends GeneralInstanceHandler {

	private Map<Integer, StaticDoor> doors;
	private AtomicBoolean isRaceKnown = new AtomicBoolean();
	private AtomicBoolean isXastaEventStarted = new AtomicBoolean();

	@Override
	public void onDie(final Npc npc) {
		switch (npc.getNpcId()) {
			case 236299:
				if (isDeadNpc(236301)) {
					doors.get(145).setOpen(true);
					despawnNpc(instance.getNpc(701156));
				}
				break;
			case 236301:
				if (isDeadNpc(236299)) {
					doors.get(145).setOpen(true);
					despawnNpc(instance.getNpc(701156));
				}
				break;
			case 236298: // Kuhara
				spawn(236705, 141.54f, 255.06f, 213f, (byte) 25);
				doors.get(43).setOpen(false);
				doors.get(150).setOpen(true);
				npc.getController().onDelete();
				break;
			case 236302: // Archmagus Upadi
				doors.get(70).setOpen(true);
				break;
			case 236300: // Brigade General Vasharti
				deleteNpc(799669);
				doors.get(70).setOpen(true);
				spawn(730401, 193.6f, 436.5f, 262f, (byte) 86);
				spawn(833048, 195.48f, 413.87f, 260.97f, (byte) 27); // Rentus Quality Supply Storage Box
				Npc ariana = (Npc) spawn(799670, 183.736f, 391.392f, 260.571f, (byte) 26);
				NpcShoutsService.getInstance().sendMsg(ariana, 1500417, ariana.getObjectId(), 0, 5000);
				NpcShoutsService.getInstance().sendMsg(ariana, 1500418, ariana.getObjectId(), 0, 8000);
				NpcShoutsService.getInstance().sendMsg(ariana, 1500419, ariana.getObjectId(), 0, 11000);
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
				despawnNpc(npc);
				break;
			case 283000:
			case 283001:
				despawnNpc(npc);
				break;
			case 217299:
				spawn(217300, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				despawnNpc(npc);
				break;
		}
	}
	
	@Override
	public void onAggro(Npc npc) {
		switch (npc.getNpcId()) {
		case 236298: // Kuhara
			doors.get(43).setOpen(true);
			NpcShoutsService.getInstance().sendMsg(npc, 1500393, npc.getObjectId(), 0, 0);
			break;
		case 236300: // Vasharti
			doors.get(70).setOpen(false);
			break;
		}
	}

	private void spawnEndEvent(int npcId, String walkern, int time) {
		sp(npcId, 193.39548f, 435.56158f, 260.57135f, (byte) 86, time, walkern);
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		doors = instance.getDoors();
	}

	@Override
	public void onLeaveInstance(Player player) {
		player.getEffectController().removeEffect(player.getRace() == Race.ELYOS ? 21805 : 21806);
		player.getInventory().decreaseByItemId(185000229, 1);
	}

	@Override
	public void onPlayerLogOut(Player player) {
		player.getEffectController().removeEffect(player.getRace() == Race.ELYOS ? 21805 : 21806);
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
				TeleportService2.teleportTo(player, npc.getWorldId(), npc.getInstanceId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				SkillEngine.getInstance().getSkill(npc, 21806, 60, player).useNoAnimationSkill();
				despawnNpc(npc);
				break;
			case 702683:
			case 702684:
			case 702685:
			case 702686:
			case 702687:
			case 702688:
				TeleportService2.teleportTo(player, npc.getWorldId(), npc.getInstanceId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				SkillEngine.getInstance().getSkill(npc, 21805, 60, player).useNoAnimationSkill();
				despawnNpc(npc);
				break;
			case 701151:
				SkillEngine.getInstance().getSkill(npc, 19909, 60, npc).useNoAnimationSkill();
				despawnNpc(npc);
				break;
			case 701152:
				SkillEngine.getInstance().getSkill(npc, 19910, 60, npc).useNoAnimationSkill();
				despawnNpc(npc);
				break;
			case 701097:
				despawnNpc(npc);
				break;
			case 701100:
				if (instance.getNpc(799543) == null)
					spawn(799543, 511.227f, 613.762f, 158.179f, (byte) 0);
				break;
		}
	}

	private void sp(final int npcId, final float x, final float y, final float z, final byte h, final int time, final String walkern) {
		final Npc npc = (Npc) spawn(npcId, x, y, z, h);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				npc.getSpawn().setWalkerId(walkern);
				WalkManager.startWalking((NpcAI2) npc.getAi2());
				npc.setState(CreatureState.WALKING.getId());
				PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.START_EMOTE2, 0, npc.getObjectId()));
			}

		}, time);
	}

	private void despawnNpc(Npc npc) {
		if (npc != null)
			npc.getController().onDelete();
	}

	private void deleteNpc(int npcId) {
		if (getNpc(npcId) != null && !getNpc(npcId).getLifeStats().isAlreadyDead())
			getNpc(npcId).getController().onDelete();
	}

	private boolean isDeadNpc(int npcId) {
		return (getNpc(npcId) == null || getNpc(npcId).getLifeStats().isAlreadyDead());
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}

	@Override
	public void onCreatureDetected(Npc detector, Creature detected) {
		if (detected instanceof Player) {
			if (detector.getNpcId() == 856056 && isXastaEventStarted.compareAndSet(false, true)) {
				sp(236271, 521.33f, 499.49f, 179.946f, (byte) 27, 2000, "300620000_Xasta_Path");
				despawnNpc(detector);
			}
		}
	}

	@Override
	public void onSpecialEvent(Npc npc) {
		if (npc.getNpcId() == 236271) {
			if (npc.getLifeStats().getHpPercentage() <= 50)
				spawn(236297, 354.53f, 596.26f, 148.298f, (byte) 100);
			else
				spawn(236296, 354.53f, 596.26f, 148.298f, (byte) 100);
			getNpc(856015).getController().onDie(npc);
			despawnNpc(npc);
			
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		if (isRaceKnown.compareAndSet(false, true)) {
			if (player.getRace() == Race.ELYOS) {
				for (int npcId = 702677; npcId <= 702682; npcId++) {
					Npc npc = getNpc(npcId);
					spawn(npcId + 6, npc.getX(), npc.getY(), 151.785f, npc.getHeading()).getSpawn().setStaticId(npc.getSpawn().getStaticId());
					npc.getController().onDelete();
				}
			}
		}
	}
}
