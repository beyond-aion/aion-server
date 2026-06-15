package instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

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
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Cheatkiller, Luzien, Estrayl
 */
@InstanceID(300800000)
public class InfinityShardInstance extends GeneralInstanceHandler {

	private final List<Future<?>> tasks = new ArrayList<>();
	private final AtomicBoolean isRunning = new AtomicBoolean(true);

	public InfinityShardInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 231083: // Hyperion Defense Elite Assassin
				removeIdeAethericFieldEffect(231074); // Ide Forcefield Generator
				break;
			case 231087: // Hyperion Defense Elite Magus
				removeIdeAethericFieldEffect(231078); // Ide Forcefield Generator
				break;
			case 231079: // Hyperion Defense Elite Assassin
				removeIdeAethericFieldEffect(231082); // Ide Forcefield Generator
				break;
			case 231075: // Agent Kabalash
				removeIdeAethericFieldEffect(231086); // Ide Forcefield Generator
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
				despawnAllAndSpawnExit();
				// rewardGP();
				break;
		}
	}

	private void removeIdeAethericFieldEffect(int npcId) {
		Npc npc = instance.getNpc(npcId);
		if (npc != null)
			npc.getEffectController().removeEffect(21371);
	}

	@Override
	public void onBackHome(Npc npc) {
		if (npc.getNpcId() == 231073) { // hyperion
			if (isRunning.getAndSet(false)) {
				cancelTasks();
				despawnAllAndSpawnExit();
			}
		}
	}

	@Override
	public void onStartEffect(Effect effect) {
		Creature effected = effect.getEffected();
		if (effected instanceof Npc && ((Npc) effected).getNpcId() == 231073)
			switch (effect.getSkillId()) {
				case 21258 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_CHARGER1_COMPLETED());
				case 21382 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_CHARGER2_COMPLETED());
				case 21384 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_CHARGER3_COMPLETED());
				case 21416 -> {
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_CHARGER4_COMPLETED());
					ThreadPoolManager.getInstance().schedule(() -> failInstance(true), 12000);
				}
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
		deleteAliveNpcs(730741); // remove barrier in center
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
		for (Player p : instance.getPlayersInside()) {
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
			if (hyperion != null && !hyperion.isDead()) {
				failInstance(false);
			}
		}, 20 * 60 * 1000)); // 20 min
	}

	private void failInstance(boolean wipePlayers) {
		if (isRunning.getAndSet(false)) {
			cancelTasks();
			deleteAliveNpcs(231073);
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDRUNEWP_USER_KILL());
			if (wipePlayers)
				for (Npc npc : instance.getNpcs(231104))
					npc.getController().useSkill(21199);
			ThreadPoolManager.getInstance().schedule(this::despawnAllAndSpawnExit, 1500); // delay despawn for invisible NPCs to use the skill
		}
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

	private void despawnAllAndSpawnExit() {
		instance.forEachNpc(npc -> npc.getController().deleteIfAliveOrCancelRespawn());
		spawn(730842, 146.65f, 135.33f, 112.18f, (byte) 59); // exit portal
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
	public void onPlayerLogout(Player player) {
		super.onPlayerLogout(player);
		if (player.isDead()) {
			TeleportService.moveToBindLocation(player);
		}
	}

	@Override
	public void onInstanceDestroy() {
		cancelTasks();
	}

	@Override
	public boolean isBoss(Npc npc) {
		return npc.getNpcId() == 231073; // Hyperion
	}
}
