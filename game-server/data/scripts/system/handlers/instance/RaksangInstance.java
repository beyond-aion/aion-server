package instance;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300310000)
public class RaksangInstance extends GeneralInstanceHandler {

	private Map<Integer, StaticDoor> doors;
	private AtomicInteger generatorKilled = new AtomicInteger();
	private AtomicInteger ashulagenKilled = new AtomicInteger();
	private AtomicInteger gargoyleKilled = new AtomicInteger();
	private AtomicInteger rakshaHelpersKilled = new AtomicInteger();
	private boolean isInstanceDestroyed;

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 730453:
			case 730454:
			case 730455:
			case 730456:
				int killCount = generatorKilled.incrementAndGet();
				if (killCount == 1) {
					sendMsg(1401133);
					doors.get(87).setOpen(true);
				} else if (killCount == 2) {
					sendMsg(1401133);
					doors.get(167).setOpen(true);
				} else if (killCount == 3) {
					sendMsg(1401133);
					doors.get(114).setOpen(true);
				} else if (killCount == 4) {
					sendMsg(1401134);
					doors.get(165).setOpen(true);
				}
				despawnNpc(npc);
				break;
			case 217399:
			case 217400:
				isDeadKerops();
				break;
			case 217392:
				doors.get(103).setOpen(true);
				break;
			case 217469:
				doors.get(107).setOpen(true);
				break;
			case 217471:
			case 217472:
				if (gargoyleKilled.incrementAndGet() == 2) {
					Npc magic = instance.getNpc(217473);
					if (magic != null) {
						sendMsg(1401159);
						magic.getEffectController().removeEffect(19126);
					}
				}
				despawnNpc(npc);
				break;
			case 217473:
				despawnNpc(npc);
				final Npc dust = (Npc) spawn(701075, 1068.630f, 967.205f, 138.785f, (byte) 0, 323);
				doors.get(105).setOpen(true);
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						if (!isInstanceDestroyed && dust != null && !NpcActions.isAlreadyDead(dust)) {
							NpcActions.delete(dust);
						}
					}

				}, 4000);
				break;
			case 217455:
				int aKillCount = ashulagenKilled.incrementAndGet();
				if (aKillCount == 1 || aKillCount == 2 || aKillCount == 3) {
					sendMsg(1401160);
				} else if (aKillCount == 4) {
					spawn(217456, 615.081f, 640.660f, 524.195f, (byte) 0);
					sendMsg(1401135);
				}
				break;
			case 217425:
			case 217451:
			case 217456:
				int rKillCount = rakshaHelpersKilled.incrementAndGet();
				if (rKillCount < 3) {
					sendMsg(1401161);
				} else if (rKillCount == 3) {
					sendMsg(1401162);
				}
				break;
			case 217647:
				Npc boss1 = getNpc(217425);
				Npc boss2 = getNpc(217451);
				if (boss1 != null && !boss1.getLifeStats().isAlreadyDead())
					boss1.getController().onDelete();
				if (boss2 != null && !boss2.getLifeStats().isAlreadyDead())
					boss2.getController().onDelete();
				break;
			case 217475:
				rakshaHelpersKilled.set(4);
				break;
		}
	}

	@Override
	public void onInstanceDestroy() {
		isInstanceDestroyed = true;
		doors.clear();
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		doors = instance.getDoors();
		Npc melkennis = getNpc(217392);
		SkillEngine.getInstance().getSkill(melkennis, 19126, 60, melkennis).useNoAnimationSkill();
	}

	private void despawnNpc(Npc npc) {
		if (npc != null) {
			npc.getController().onDelete();
		}
	}

	private boolean isDeadKerops() {
		Npc kerop1 = getNpc(217399);
		Npc kerop2 = getNpc(217400);
		if (isDead(kerop1) && isDead(kerop2)) {
			Npc melkennis = getNpc(217392);
			if (melkennis != null)
				melkennis.getEffectController().removeEffect(19126);
			return true;
		}
		return false;
	}

	private boolean isDead(Npc npc) {
		return (npc == null || npc.getLifeStats().isAlreadyDead());
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player,
			new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}

}
