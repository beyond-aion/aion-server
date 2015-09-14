package instance;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * @author Cheatkiller
 * @modified Luzien
 */
@InstanceID(300800000)
public class InfinityShardInstance extends GeneralInstanceHandler {

	private Future<?> ideResonatorTask, instanceFailTask;
	private List<Integer> resonators = Arrays.asList(231092, 231093, 231094, 231095);

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
				checkDeadAllGenerators();
				break;
			case 231092:
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						spawn(231102, 107.53553f, 142.51953f, 127.03997f, (byte) 0);
					}
				}, 5000);
				break;
			case 231093:
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						spawn(231102, 113.86417f, 154.06656f, 127.68255f, (byte) 110);
					}
				}, 5000);
				break;
			case 231094:
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						spawn(231102, 144.52719f, 122.26577f, 127.44639f, (byte) 45);
					}
				}, 5000);
				break;
			case 231095:
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						spawn(231102, 150.33377f, 132.67754f, 126.57981f, (byte) 50);
					}
				}, 5000);
				break;
			case 231073:
				cancelIdeResonatorTask();
				cancelFailTask();
				despawnAdds();
				rewardGP();
				break;
		}
	}

	private boolean checkDeadAllGenerators() {
		sendMsg(1401795);
		Npc gen1 = getNpc(231074);
		Npc gen2 = getNpc(231078);
		Npc gen3 = getNpc(231082);
		Npc gen4 = getNpc(231086);
		if (isDead(gen1) && isDead(gen2) && isDead(gen3) && isDead(gen4)) {
			sendMsg(1401796);
			instance.getNpc(730741).getController().onDelete();
			instance.getNpc(231073).getEffectController().removeEffect(21254);
			startIdeResonatorTask();
			startFailTimer();
			sendMsg(1401790);
			return true;
		}
		return false;
	}

	private void startIdeResonatorTask() {
		ideResonatorTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				instanceFailCheck();
				spawnResonators();
			}
		}, 5000, 45000);
	}

	private void startFailTimer() {
		instanceFailTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				Npc hyperion = instance.getNpc(231073);
				if (hyperion != null && !hyperion.getLifeStats().isAlreadyDead()) {
					cancelIdeResonatorTask();
					failInstance();
				}
			}
		}, 20 * 60 * 1000); // 20 min
	}

	private void instanceFailCheck() {
		Npc hyperion = instance.getNpc(231073);
		if (hasBuff(hyperion, 21258) && hasBuff(hyperion, 21382) && hasBuff(hyperion, 21384) && hasBuff(hyperion, 21416)) {
			cancelIdeResonatorTask();
			cancelFailTask();
			failInstance();
		}
	}

	private void failInstance() {
		final List<Npc> idePolls = instance.getNpcs(231104);
		final Npc hyperion = instance.getNpc(231073);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				hyperion.getController().onDelete();
				sendMsg(1401909);
				for (Npc npc : idePolls) {
					npc.getController().useSkill(21199);
				}
			}
		}, 20000);
	}

	private boolean hasBuff(Npc npc, int buff) {
		return npc.getEffectController().hasAbnormalEffect(buff);
	}

	private void spawnResonators() {
		int delay = 5000;
		final Npc hyperion = instance.getNpc(231073);
		for (final Integer npcid : resonators) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					if (hyperion != null && !hyperion.getLifeStats().isAlreadyDead())
						spawnResonator(npcid);
				}

			}, delay);
			delay += 10000;
		}
	}

	private void spawnResonator(int npcid) {
		Npc resonator = null;
		resonator = instance.getNpc(npcid);
		if (isDead(resonator)) {
			switch (npcid) {
				case 231092:
					spawn(231092, 108.55013f, 138.96948f, 132.60164f, (byte) 0);
					break;
				case 231093:
					spawn(231093, 126.5471f, 154.47961f, 131.47116f, (byte) 0);
					break;
				case 231094:
					spawn(231094, 146.72455f, 139.12267f, 132.68515f, (byte) 0);
					break;
				case 231095:
					spawn(231095, 129.41306f, 121.34766f, 131.47116f, (byte) 0);
					break;
			}
		}
	}

	private void despawnAdds() {
		for (Integer npcid : resonators) {
			deleteNpcs(instance.getNpcs(npcid));
		}
		deleteNpcs(instance.getNpcs(231104));
		deleteNpcs(instance.getNpcs(231102));
		deleteNpcs(instance.getNpcs(231096));
		deleteNpcs(instance.getNpcs(231097));
		deleteNpcs(instance.getNpcs(231098));
		deleteNpcs(instance.getNpcs(231099));
		deleteNpcs(instance.getNpcs(231103));
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().onDelete();
			}
		}
	}

	private void rewardGP() {
		int reward = 600 / instance.getPlayersInside().size();
		for (Player p : instance.getPlayersInside()) {
			if (p != null && p.isOnline())
				GloryPointsService.addGp(p, reward, true);
		}
	}

	private void cancelIdeResonatorTask() {
		if (ideResonatorTask != null && !ideResonatorTask.isCancelled()) {
			ideResonatorTask.cancel(true);
		}
	}

	private void cancelFailTask() {
		if (instanceFailTask != null && !instanceFailTask.isDone()) {
			instanceFailTask.cancel(true);
		}
	}

	private boolean isDead(Npc npc) {
		return (npc == null || npc.getLifeStats().isAlreadyDead());
	}

	@Override
	public void onPlayerLogOut(Player player) {
		super.onPlayerLogOut(player);
		if (player.getLifeStats().isAlreadyDead()) {
			TeleportService2.moveToBindLocation(player, true);
		}
	}

	@Override
	public void onInstanceDestroy() {
		cancelIdeResonatorTask();
		cancelFailTask();
	}
}
