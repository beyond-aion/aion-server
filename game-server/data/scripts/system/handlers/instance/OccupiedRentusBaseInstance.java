package instance;

import java.util.Map;

import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.controllers.effect.PlayerEffectController;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Tibald
 */
@InstanceID(300620000)
public class OccupiedRentusBaseInstance extends GeneralInstanceHandler {

	private Map<Integer, StaticDoor> doors;
	private boolean isInstanceDestroyed;

	@Override
	public void onDie(final Npc npc) {
		if (isInstanceDestroyed)
			return;

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
				break;
			case 236302: // Archmagus Upadi
				spawn(236705, 141.54f, 255.06f, 213f, (byte) 25);
				doors.get(70).setOpen(true);
				break;
			case 236300: // Brigade General Vasharti
				deleteNpc(799669);
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
				final float x = npc.getX();
				final float y = npc.getY();
				final float z = npc.getZ();
				final byte h = npc.getHeading();
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						if (!isInstanceDestroyed) {
							if (x > 0 && y > 0 && z > 0) {
								spawn(217300, x, y, z, h);
							}
						}
					}

				}, 4000);
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						despawnNpc(npc);
					}

				}, 2000);
				break;
		}
	}

	private void stopWalk(Npc npc) {
		npc.getSpawn().setWalkerId(null);
		WalkManager.stopWalking((NpcAI2) npc.getAi2());
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
		removeEffects(player);
	}

	@Override
	public void onPlayerLogOut(Player player) {
		removeEffects(player);
	}

	private void removeEffects(Player player) {
		PlayerEffectController effectController = player.getEffectController();
		effectController.removeEffect(21806);
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
				SkillEngine.getInstance().getSkill(npc, 21806, 60, player).useNoAnimationSkill();
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
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed) {
					Npc npc = (Npc) spawn(npcId, x, y, z, h);
					npc.getSpawn().setWalkerId(walkern);
					startEndWalker(npc);
					unSetEndWalker(npc);
				}
			}

		}, time);
	}

	private void startEndWalker(final Npc npc) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed) {
					WalkManager.startWalking((NpcAI2) npc.getAi2());
					npc.setState(1);
					PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.START_EMOTE2, 0, npc.getObjectId()));
				}
			}

		}, 3000);
	}

	private void unSetEndWalker(final Npc npc) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed)
					stopWalk(npc);
			}

		}, 8000);
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
		PacketSendUtility.broadcastPacket(player,
			new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}
}
