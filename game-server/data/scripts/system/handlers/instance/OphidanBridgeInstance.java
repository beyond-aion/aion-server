package instance;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;
import com.aionemu.gameserver.model.utils3d.Point3D;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_MOVE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Cheatkiller, Tibald, Luzien
 */
@InstanceID(300590000)
public class OphidanBridgeInstance extends GeneralInstanceHandler {

	private final AtomicLong startTime = new AtomicLong();
	private Future<?> bombardKillAllTask;
	private Map<Integer, StaticDoor> doors;
	private Race instanceRace;
	private final AtomicBoolean isSpawned = new AtomicBoolean(false);
	private final AtomicBoolean isComplete = new AtomicBoolean(false);
	private static final float x = 436.19562f, y = 496.5426f, z = 604.8871f;
	private static final byte heading = 62;

	@Override
	public void onInstanceDestroy() {
		doors.clear();
		bombardKillAllTask.cancel(true);
	}

	@Override
	public void onEnterInstance(final Player player) {
		if (isSpawned.compareAndSet(false, true)) {
			if (instanceRace == null) {
				instanceRace = player.getRace();
			}
			int mortar = instanceRace == Race.ELYOS ? 701646 : 701647;
			int infiltrator = instanceRace == Race.ELYOS ? 801763 : 801765;
			spawn(mortar, 765.7196f, 534.3431f, 576.2391f, (byte) 60);
			spawn(infiltrator, 739.8924f, 536.3952f, 574.8705f, (byte) 15);
		}
	}

	private void spawnBridgeBarrier() {
		FlyRing f1 = new FlyRing(new FlyRingTemplate("OPHIDIAN_BRIDGE_1", mapId, new Point3D(435.25446, 496.38007, 604.8871), new Point3D(435.25446,
			496.38007, 616.8871), new Point3D(432.8339, 508.1334, 604.8871), 12), instanceId);
		f1.spawn();

		FlyRing f2 = new FlyRing(new FlyRingTemplate("OPHIDIAN_BRIDGE_2", mapId, new Point3D(429.2187, 495.9813, 604.9689), new Point3D(429.2187,
			495.9813, 616.9689), new Point3D(426.79813, 507.73462, 604.9689), 12), instanceId);
		f2.spawn();

		FlyRing f3 = new FlyRing(new FlyRingTemplate("OPHIDIAN_BRIDGE_3", mapId, new Point3D(424.53012, 495.6659, 604.9849), new Point3D(424.53012,
			495.6659, 616.9849), new Point3D(422.10956, 507.41922, 604.9849), 12), instanceId);
		f3.spawn();
	}

	@Override
	public boolean onPassFlyingRing(Player player, String flyingRing) {
		StaticDoor door = doors.get(47);
		if (door == null || door.isOpen()) {
			return false;
		}
		World.getInstance().updatePosition(player, x, y, z, heading);
		player.getMoveController().updateLastMove();
		PacketSendUtility.sendPacket(player, new SM_PLAYER_MOVE(x, y, z, heading));
		PacketSendUtility.broadcastPacket(player, new SM_MOVE(player), false);
		return false;
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		doors = instance.getDoors();
		spawnBridgeBarrier();
	}

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 233320:
				if (startTime.compareAndSet(0, System.currentTimeMillis())) {
					sendMsg(1401892);
					sendMsg(1401875, 0, false, 25, 15 * 60 * 1000); // 10 minutes before firing
					sendMsg(1401876, 0, false, 25, 20 * 60 * 1000); // 5 minutes before firing
					sendMsg(1401877, 0, false, 25, 24 * 60 * 1000); // 1 minute before firing
					bombardKillAllTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							sendMsg(1401878); // Report a charged gun bombard
							bombardKillAll();
						}
					}, 25 * 60000);
				}
				break;
			case 230413:
				sendMsg(1401904);
				spawnPCGen(230417, npc.getX(), npc.getY(), npc.getZ(), 157);
				checkDeadAllGenerators();
				break;
			case 230414:
				sendMsg(1401905);
				spawnPCGen(230418, npc.getX(), npc.getY(), npc.getZ(), 161);
				checkDeadAllGenerators();
				break;
			case 230415:
				sendMsg(1401906);
				spawnPCGen(230419, npc.getX(), npc.getY(), npc.getZ(), 159);
				checkDeadAllGenerators();
				break;
			case 230416:
				sendMsg(1401907);
				spawnPCGen(230420, npc.getX(), npc.getY(), npc.getZ(), 163);
				checkDeadAllGenerators();
				break;
			case 230417:
				sendMsg(1401885);
				spawn(230413, npc.getX(), npc.getY(), npc.getZ(), (byte) 0, 164);
				break;
			case 230418:
				sendMsg(1401887);
				spawn(230414, npc.getX(), npc.getY(), npc.getZ(), (byte) 0, 160);
				break;
			case 230419:
				sendMsg(1401889);
				spawn(230415, npc.getX(), npc.getY(), npc.getZ(), (byte) 0, 158);
				break;
			case 230420:
				sendMsg(1401891);
				spawn(230416, npc.getX(), npc.getY(), npc.getZ(), (byte) 0, 162);
				break;
			case 231050:
				bombardKillAllTask.cancel(true);
				spawn(730868, 350.92664f, 490.76566f, 606.3221f, (byte) 61);// Ophidian Bridge Exit
				// TODO
				break;
		}
	}

	private void spawnPCGen(final int npcId, float x, float y, float z, int staticId) {
		final Npc gen = (Npc) spawn(npcId, x, y, z, (byte) 0, staticId);
		int delay = Rnd.get(170, 230);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				alarm(npcId);
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						spawnInvaders(gen);
					}
				}, 10000);
			}
		}, delay * 1000);

	}

	private void alarm(int npcId) {
		switch (npcId) {
			case 230417:
				sendMsg(1401942);
				break;
			case 230418:
				sendMsg(1401943);
				break;
			case 230419:
				sendMsg(1401944);
				break;
			case 230420:
				sendMsg(1401945);
				break;
		}
	}

	private void spawnInvaders(final Npc npc) {
		double radian = Math.toRadians(MathUtil.convertHeadingToDegree(npc.getHeading()));
		int dist = 10;
		float x = (float) (Math.cos(radian) * dist);
		float y = (float) (Math.sin(radian) * dist);
		final Npc att1 = (Npc) spawn(231188, npc.getPosition().getX() + x, npc.getPosition().getY() + y, npc.getPosition().getZ(), (byte) 0);
		final Npc att2 = (Npc) spawn(231189, npc.getPosition().getX() + x, npc.getPosition().getY() + y, npc.getPosition().getZ(), (byte) 0);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				att1.getAggroList().addHate(npc, 3000);
				att2.getAggroList().addHate(npc, 3000);
			}

		}, 1000);
		switch (npc.getNpcId()) {
			case 230413:
				sendMsg(1401884);
				break;
			case 230414:
				sendMsg(1401886);
				break;
			case 230415:
				sendMsg(1401888);
				break;
			case 230416:
				sendMsg(1401890);
				break;
		}
	}

	private boolean checkDeadAllGenerators() {
		Npc gen1 = getNpc(230413);
		Npc gen2 = getNpc(230414);
		Npc gen3 = getNpc(230415);
		Npc gen4 = getNpc(230416);
		if (isDead(gen1) && isDead(gen2) && isDead(gen3) && isDead(gen4)) {
			if (isComplete.compareAndSet(false, true)) {
				spawn(701644, 436.99045f, 496.55634f, 605.95203f, (byte) 2);// Bridge Control
				spawn(231051, 369.48102f, 497.58191f, 605.95197f, (byte) 0);// Surkana Aetherturret
				spawn(231051, 370.99677f, 484.57648f, 605.90326f, (byte) 0);// Surkana Aetherturret
				spawn(231182, 355.30307f, 464.72681f, 605.76324f, (byte) 0);// Vera Defender Medic
				spawn(231052, 359.1683f, 491.27066f, 606.1158f, (byte) 0);// Vera Defender Archmage
				spawn(231050, 317.46237f, 488.88406f, 609.2848f, (byte) 0);// Vera
			}
			return true;
		}
		return false;
	}

	private void bombardKillAll() {
		for (Npc bombard : instance.getNpcs(284689)) {
			SkillEngine.getInstance().getSkill(bombard, 21199, 80, bombard).useNoAnimationSkill();
		}
	}

	private boolean isDead(Npc npc) {
		return (npc == null || npc.getLifeStats().isAlreadyDead());
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 701646:
			case 701647:
				SkillEngine.getInstance().applyEffectDirectly(instanceRace == Race.ELYOS ? 21434 : 21435, player, player, 0);
				// balaurs(231193) attacks player which in transformation
				break;
			case 701644:
				if (!doors.get(47).isOpen()) {
					sendMsg(1401879);
					doors.get(47).setOpen(true);
					Npc vera = getNpc(231050);
					if (!vera.getEffectController().hasAbnormalEffect(21438)) // useInSpawned tends to fail...
						SkillEngine.getInstance().getSkill(vera, 21438, 41, vera).useNoAnimationSkill();
				}
				break;
		}
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player,
			new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}

	@Override
	public void onPlayerLogOut(Player player) {
		removeEffects(player);
	}

	@Override
	public void onLeaveInstance(Player player) {
		removeEffects(player);
	}

	private void removeEffects(Player player) {
		player.getEffectController().removeEffect(21434);
		player.getEffectController().removeEffect(21435);
	}
}
