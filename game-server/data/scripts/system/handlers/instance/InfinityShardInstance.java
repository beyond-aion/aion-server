package instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 * @modified Luzien
 * @reworked Estrayl March 6th, 2018
 */
@InstanceID(300800000)
public class InfinityShardInstance extends GeneralInstanceHandler {

	private List<Future<?>> tasks = new ArrayList<>();

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 231083:
				instance.getNpc(231074).getEffectController().removeEffect(21371);
				break;
			case 231087:
				instance.getNpc(231078).getEffectController().removeEffect(21371);
				break;
			case 231079:
				instance.getNpc(231082).getEffectController().removeEffect(21371);
				break;
			case 231075:
				instance.getNpc(231086).getEffectController().removeEffect(21371);
				break;
			case 231074:
			case 231078:
			case 231082:
			case 231086:
				npc.getController().delete();
				checkGeneratorState();
				break;
			case 231092:
			case 231093:
			case 231094:
			case 231095:
			case 231102:
				tasks.add(
					ThreadPoolManager.getInstance().schedule(() -> spawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), (byte) 0), Rnd.get(29000, 31000)));
				break;
			case 231073: // Hyperion
				cancelTasks();
				despawnInstance();
				// rewardGP();
				break;
		}
	}

	@Override
	public void onStartEffect(Effect effect) {
		Creature effected = effect.getEffected();
		if (effected instanceof Npc && ((Npc) effected).getNpcId() == 231073)
			switch (effect.getSkillId()) {
				case 21258:
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_CHARGER1_COMPLETED());
					break;
				case 21382:
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_CHARGER2_COMPLETED());
					break;
				case 21384:
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_CHARGER3_COMPLETED());
					break;
				case 21416:
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_CHARGER4_COMPLETED());
					ThreadPoolManager.getInstance().schedule(() -> failInstance(true), 12000);
					break;
			}
	}

	private void checkGeneratorState() {
		for (int id = 231074; id <= 231086; id += 4) {
			if (instance.getNpc(id) != null) {
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_BROKENPROTECTION());
				return;
			}
		}
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_BROKENPROTECTIONALL());
		instance.getNpc(730741).getController().delete(); // remove barrier in center
		instance.getNpc(231073).getEffectController().removeEffect(21254);
		startHatingNearestPlayer();
		SkillEngine.getInstance().getSkill(instance.getNpc(231073), 21255, 56, instance.getNpc(231073)).useWithoutPropSkill();
		spawnResonators();
		spawnTurrets();
		startFailTimer();
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_CHARGING());
	}

	private void startHatingNearestPlayer() {
		Npc hyperion = instance.getNpc(231073);
		Player nearest = null;
		double dist = Double.MAX_VALUE;
		for(Player p: instance.getPlayersInside()) {
			if (!p.isDead()) {
				double locDist = PositionUtil.getDistance(hyperion, p, false);
				if (locDist < dist) {
					dist = locDist;
					nearest = p;
				}
			}
		}
		if (nearest != null) {
			hyperion.getAi().onCreatureEvent(AIEventType.CREATURE_AGGRO, nearest);
		}
	}


	private void startFailTimer() {
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> {
			Npc hyperion = instance.getNpc(231073);
			if (hyperion != null && !hyperion.isDead())
				failInstance(false);
		}, 20 * 60 * 1000)); // 20 min
	}

	private void failInstance(boolean wipePlayers) {
		cancelTasks();
		instance.getNpc(231073).getController().delete();
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_USER_KILL());
		if (wipePlayers)
			for (Npc npc : instance.getNpcs(231104))
				npc.getController().useSkill(21199);
		ThreadPoolManager.getInstance().schedule(() -> despawnInstance(), 1500); // delay despawn for invisible NPCs to use the skill
	}

	private void spawnResonators() {
		spawn(231092, 108.55013f, 138.96948f, 132.60164f, (byte) 0);
		ThreadPoolManager.getInstance().schedule(() -> spawn(231093, 126.5471f, 154.47961f, 131.47116f, (byte) 0), 10000);
		ThreadPoolManager.getInstance().schedule(() -> spawn(231094, 146.72455f, 139.12267f, 132.68515f, (byte) 0), 20000);
		ThreadPoolManager.getInstance().schedule(() -> spawn(231095, 129.41306f, 121.34766f, 131.47116f, (byte) 0), 30000);
	}

	private void spawnTurrets() {
		ThreadPoolManager.getInstance().schedule(() -> spawn(231102, 107.53553f, 142.51953f, 127.03997f, (byte) 0), Rnd.get(30000, 45000));
		ThreadPoolManager.getInstance().schedule(() -> spawn(231102, 113.86417f, 154.06656f, 127.68255f, (byte) 110), Rnd.get(30000, 45000));
		ThreadPoolManager.getInstance().schedule(() -> spawn(231102, 144.52719f, 122.26577f, 127.44639f, (byte) 45), Rnd.get(30000, 45000));
		ThreadPoolManager.getInstance().schedule(() -> spawn(231102, 150.33377f, 132.67754f, 126.57981f, (byte) 50), Rnd.get(30000, 45000));
	}

	private void despawnInstance() {
		instance.forEachNpc(npc -> {
			if (npc.getNpcId() != 231073)
				npc.getController().delete();
		});
	}

	/*
	 * private void rewardGP() {
	 * int reward = 600 / instance.getPlayersInside().size();
	 * for (Player p : instance.getPlayersInside()) {
	 * if (p != null && p.isOnline())
	 * GloryPointsService.addGp(p, reward, true);
	 * }
	 * }
	 */

	private void cancelTasks() {
		for (Future<?> task : tasks)
			if (task != null && !task.isCancelled())
				task.cancel(true);
	}

	@Override
	public void onPlayerLogOut(Player player) {
		super.onPlayerLogOut(player);
		if (player.isDead()) {
			TeleportService.moveToBindLocation(player);
		}
	}

	@Override
	public void onInstanceDestroy() {
		cancelTasks();
	}
}
