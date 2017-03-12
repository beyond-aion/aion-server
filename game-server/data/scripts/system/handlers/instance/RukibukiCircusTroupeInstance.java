package instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.controllers.effect.PlayerEffectController;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 */
@InstanceID(301160000)
public class RukibukiCircusTroupeInstance extends GeneralInstanceHandler {

	private List<Integer> movies = new ArrayList<>();
	private boolean isInstanceDestroyed;
	private Future<?> spawnTask;
	private Future<?> despawnBossTask;
	private AtomicBoolean moviePlayed = new AtomicBoolean();

	@Override
	public void onEnterInstance(final Player player) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				SkillEngine.getInstance().applyEffectDirectly(player.getRace() == Race.ELYOS ? 21329 : 21332, player, player, 0);

			}
		}, 1000);
	}

	private void cancelSpawnTask() {
		if (spawnTask != null && !spawnTask.isCancelled()) {
			spawnTask.cancel(true);
		}
	}

	private void cancelDespawnBossTask() {
		if (despawnBossTask != null && !despawnBossTask.isCancelled()) {
			despawnBossTask.cancel(true);
		}
	}

	@Override
	public void onPlayMovieEnd(Player player, int movieId) {
		switch (movieId) {
			case 983:
				if (moviePlayed.compareAndSet(false, true)) {
					PacketSendUtility.broadcastMessage(getNpc(831747), 1500966);
					despawnBossTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							getNpc(831747).getController().delete();
						}
					}, 3000);
					spawnPhase();
					startDespawnBossTask();
					break;
				}
		}
	}

	private void spawnPhase() {
		spawnTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed) {
					// Left
					// Wave 1
					sp(233455, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 0, "3011600001");
					sp(233455, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 0, "3011600002");
					sp(233455, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 0, "3011600003");
					sp(233455, 521.329f, 626.74036f, 207.89403f, (byte) 90, 0, "3011600004");

					// Wave 2
					sp(233450, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 10000, "3011600001");
					sp(233450, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 10000, "3011600002");
					sp(233450, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 10000, "3011600003");
					sp(233450, 521.329f, 626.74036f, 207.89403f, (byte) 90, 10000, "3011600004");

					// Wave 3
					sp(233455, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 20000, "3011600001");
					sp(233455, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 20000, "3011600002");
					sp(233455, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 20000, "3011600003");
					sp(233455, 521.329f, 626.74036f, 207.89403f, (byte) 90, 20000, "3011600004");

					// Wave 4
					sp(233450, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 30000, "3011600001");
					sp(233450, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 30000, "3011600002");
					sp(233450, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 30000, "3011600003");
					sp(233450, 521.329f, 626.74036f, 207.89403f, (byte) 90, 30000, "3011600004");

					// Wave 5
					sp(233455, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 40000, "3011600001");
					sp(233455, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 40000, "3011600002");
					sp(233455, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 40000, "3011600003");
					sp(233455, 521.329f, 626.74036f, 207.89403f, (byte) 90, 40000, "3011600004");

					// Wave 6
					sp(233450, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 50000, "3011600001");
					sp(233450, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 50000, "3011600002");
					sp(233450, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 50000, "3011600003");
					sp(233450, 521.329f, 626.74036f, 207.89403f, (byte) 90, 50000, "3011600004");

					// Wave 7
					sp(233455, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 60000, "3011600001");
					sp(233455, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 60000, "3011600002");
					sp(233455, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 60000, "3011600003");
					sp(233455, 521.329f, 626.74036f, 207.89403f, (byte) 90, 60000, "3011600004");

					// Wave 8
					sp(233450, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 70000, "3011600001");
					sp(233450, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 70000, "3011600002");
					sp(233450, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 70000, "3011600003");
					sp(233450, 521.329f, 626.74036f, 207.89403f, (byte) 90, 70000, "3011600004");

					// Wave 9
					sp(233455, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 80000, "3011600001");
					sp(233455, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 80000, "3011600002");
					sp(233455, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 80000, "3011600003");
					sp(233455, 521.329f, 626.74036f, 207.89403f, (byte) 90, 80000, "3011600004");

					// Wave 10
					sp(233450, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 90000, "3011600001");
					sp(233450, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 90000, "3011600002");
					sp(233450, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 90000, "3011600003");
					sp(233450, 521.329f, 626.74036f, 207.89403f, (byte) 90, 90000, "3011600004");

					// Wave 11
					sp(233455, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 100000, "3011600001");
					sp(233455, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 100000, "3011600002");
					sp(233455, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 100000, "3011600003");
					sp(233455, 521.329f, 626.74036f, 207.89403f, (byte) 90, 100000, "3011600004");

					// Wave 12
					sp(233450, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 110000, "3011600001");
					sp(233450, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 110000, "3011600002");
					sp(233450, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 110000, "3011600003");
					sp(233450, 521.329f, 626.74036f, 207.89403f, (byte) 90, 110000, "3011600004");

					// Wave 13
					sp(233455, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 120000, "3011600001");
					sp(233455, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 120000, "3011600002");
					sp(233455, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 120000, "3011600003");
					sp(233455, 521.329f, 626.74036f, 207.89403f, (byte) 90, 120000, "3011600004");

					// Wave 14
					sp(233450, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 130000, "3011600001");
					sp(233450, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 130000, "3011600002");
					sp(233450, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 130000, "3011600003");
					sp(233450, 521.329f, 626.74036f, 207.89403f, (byte) 90, 130000, "3011600004");

					// Wave 15
					sp(233455, 524.41406f, 628.0354f, 207.97612f, (byte) 90, 140000, "3011600001");
					sp(233455, 521.2755f, 628.16846f, 208.06583f, (byte) 90, 140000, "3011600002");
					sp(233455, 524.4364f, 626.2443f, 207.68132f, (byte) 90, 140000, "3011600003");
					sp(233455, 521.329f, 626.74036f, 207.89403f, (byte) 90, 140000, "3011600004");

					// Right
					// Wave 1
					sp(233450, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 0, "3011600005");
					sp(233450, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 0, "3011600006");
					sp(233450, 523.791f, 496.6259f, 198.52597f, (byte) 90, 0, "3011600007");
					sp(233450, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 0, "3011600008");

					// Wave 2
					sp(233455, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 10000, "3011600005");
					sp(233455, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 10000, "3011600006");
					sp(233455, 523.791f, 496.6259f, 198.52597f, (byte) 90, 10000, "3011600007");
					sp(233455, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 10000, "3011600008");

					// Wave 3
					sp(233450, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 20000, "3011600005");
					sp(233450, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 20000, "3011600006");
					sp(233450, 523.791f, 496.6259f, 198.52597f, (byte) 90, 20000, "3011600007");
					sp(233450, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 20000, "3011600008");

					// Wave 4
					sp(233455, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 30000, "3011600005");
					sp(233455, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 30000, "3011600006");
					sp(233455, 523.791f, 496.6259f, 198.52597f, (byte) 90, 30000, "3011600007");
					sp(233455, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 30000, "3011600008");

					// Wave 5
					sp(233450, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 40000, "3011600005");
					sp(233450, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 40000, "3011600006");
					sp(233450, 523.791f, 496.6259f, 198.52597f, (byte) 90, 40000, "3011600007");
					sp(233450, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 40000, "3011600008");

					// Wave 6
					sp(233455, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 50000, "3011600005");
					sp(233455, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 50000, "3011600006");
					sp(233455, 523.791f, 496.6259f, 198.52597f, (byte) 90, 50000, "3011600007");
					sp(233455, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 50000, "3011600008");

					// Wave 7
					sp(233450, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 60000, "3011600005");
					sp(233450, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 60000, "3011600006");
					sp(233450, 523.791f, 496.6259f, 198.52597f, (byte) 90, 60000, "3011600007");
					sp(233450, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 60000, "3011600008");

					// Wave 8
					sp(233455, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 70000, "3011600005");
					sp(233455, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 70000, "3011600006");
					sp(233455, 523.791f, 496.6259f, 198.52597f, (byte) 90, 70000, "3011600007");
					sp(233455, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 70000, "3011600008");

					// Wave 9
					sp(233450, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 80000, "3011600005");
					sp(233450, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 80000, "3011600006");
					sp(233450, 523.791f, 496.6259f, 198.52597f, (byte) 90, 80000, "3011600007");
					sp(233450, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 80000, "3011600008");

					// Wave 10
					sp(233455, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 90000, "3011600005");
					sp(233455, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 90000, "3011600006");
					sp(233455, 523.791f, 496.6259f, 198.52597f, (byte) 90, 90000, "3011600007");
					sp(233455, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 90000, "3011600008");

					// Wave 11
					sp(233450, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 100000, "3011600005");
					sp(233450, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 100000, "3011600006");
					sp(233450, 523.791f, 496.6259f, 198.52597f, (byte) 90, 100000, "3011600007");
					sp(233450, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 100000, "3011600008");

					// Wave 12
					sp(233455, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 110000, "3011600005");
					sp(233455, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 110000, "3011600006");
					sp(233455, 523.791f, 496.6259f, 198.52597f, (byte) 90, 110000, "3011600007");
					sp(233455, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 110000, "3011600008");

					// Wave 13
					sp(233450, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 120000, "3011600005");
					sp(233450, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 120000, "3011600006");
					sp(233450, 523.791f, 496.6259f, 198.52597f, (byte) 90, 120000, "3011600007");
					sp(233450, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 120000, "3011600008");

					// Wave 14
					sp(233455, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 130000, "3011600005");
					sp(233455, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 130000, "3011600006");
					sp(233455, 523.791f, 496.6259f, 198.52597f, (byte) 90, 130000, "3011600007");
					sp(233455, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 130000, "3011600008");

					// Wave 15
					sp(233450, 523.77045f, 494.52615f, 198.37659f, (byte) 90, 140000, "3011600005");
					sp(233450, 521.3197f, 494.83768f, 198.40182f, (byte) 90, 140000, "3011600006");
					sp(233450, 523.791f, 496.6259f, 198.52597f, (byte) 90, 140000, "3011600007");
					sp(233450, 521.4548f, 496.7582f, 198.46896f, (byte) 90, 140000, "3011600008");
					sendMsg(1501136, 160000);
					// Mistress Viloa
					sp(233459, 549.0792f, 565.6647f, 198.83736f, (byte) 60, 160000);
					sendMsg(1501123, 180000);
					sendMsg(1501143, 200000);
					// Harlequin Lord Reshka
					sp(233453, 549.0792f, 565.6647f, 198.83736f, (byte) 60, 200000);
					sendMsg(1501131, 300000);
					// Nightmare Lord Heiramune
					sp(233467, 549.0792f, 565.6647f, 198.83736f, (byte) 60, 300000);
				}
			}
		}, 300);
	}

	protected void sp(final int npcId, final float x, final float y, final float z, final byte h, final int time) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed) {
					spawn(npcId, x, y, z, h);
				}
			}

		}, time);
	}

	protected void sp(final int npcId, final float x, final float y, final float z, final byte h, final int time, final String walkerId) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isInstanceDestroyed) {
					Npc npc = (Npc) spawn(npcId, x, y, z, h);
					npc.getSpawn().setWalkerId(walkerId);
					WalkManager.startWalking((NpcAI) npc.getAi());
				}
			}

		}, time);
	}

	private void startDespawnBossTask() {
		despawnBossTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				despawnNpc(getNpc(233459));
			}
		}, 210000);
		despawnBossTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				despawnNpc(getNpc(233453));
			}
		}, 310000);
	}

	private void despawnNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			npc.getController().delete();
		}
	}

	private List<Npc> getNpcs(int npcId) {
		if (!isInstanceDestroyed) {
			return instance.getNpcs(npcId);
		}
		return null;
	}

	@Override
	public void onDie(Npc npc) {

		switch (npc.getNpcId()) {
			case 831740:
			case 831348:
			case 831349:
			case 233145:
			case 233146:
			case 233450:
			case 233455:
			case 233462:
			case 233463:
				despawnNpc(npc);
				break;
			case 233453:
				despawnNpcs(getNpcs(233462));
				despawnNpcs(getNpcs(233463));
				despawnNpcs(getNpcs(831348));
				despawnNpcs(getNpcs(831349));
				break;
			case 233467:
				instance.forEachPlayer(new Consumer<Player>() {

					@Override
					public void accept(Player p) {
						PacketSendUtility.sendPacket(p, new SM_PLAY_MOVIE(0, 984));
					}

				});
				despawnNpc(getNpc(831740));
				despawnNpc(getNpc(831627));
				despawnNpc(getNpc(831741));
				despawnNpc(getNpc(831718));
				despawnNpc(getNpc(831551));
				despawnNpc(getNpc(831552));
				despawnNpc(getNpc(831553));
				// Open Cage
				spawn(831598, 522.3982f, 564.6901f, 199.0337f, (byte) 60, 14);
				// Yume
				spawn(831744, 519.29773f, 565.52289f, 199.72002f, (byte) 60);
				// Ringmaster Rukibuki's Treasure Box
				spawn(831575, 507.42548f, 552.5874f, 199.86153f, (byte) 0);
				spawn(831575, 507.46768f, 556.34448f, 199.86153f, (byte) 0);
				spawn(831575, 507.75433f, 568.13837f, 199.86153f, (byte) 0);
				spawn(831575, 507.77673f, 564.34204f, 199.86153f, (byte) 0);
				spawn(831575, 507.83087f, 571.92407f, 199.86153f, (byte) 0);
				spawn(831575, 507.90619f, 575.8056f, 199.86153f, (byte) 0);
				spawn(831745, 515.7631f, 577.2256f, 198.7648f, (byte) 0);
				spawn(831745, 515.26715f, 580.4844f, 198.6708f, (byte) 0);
				spawn(831745, 519.28345f, 579.31573f, 198.70723f, (byte) 0);
				spawn(831745, 526.01355f, 583.04895f, 198.75696f, (byte) 0);
				spawn(831745, 531.1339f, 579.4351f, 198.69586f, (byte) 0);
				spawn(831745, 533.6956f, 576.092f, 198.75f, (byte) 0);
				spawn(831745, 535.5213f, 578.81696f, 198.77115f, (byte) 0);
				spawn(831745, 537.1102f, 575.385f, 198.75f, (byte) 0);
				spawn(831745, 537.35767f, 572.5098f, 198.74171f, (byte) 0);
				spawn(831745, 522.6655f, 583.15027f, 198.8219f, (byte) 0);
				spawn(831745, 536.34247f, 550.96124f, 198.875f, (byte) 0);
				spawn(831745, 533.497f, 548.8109f, 198.89424f, (byte) 0);
				spawn(831745, 530.3073f, 551.6777f, 198.66435f, (byte) 0);
				spawn(831745, 525.0943f, 547.747f, 198.875f, (byte) 0);
				spawn(831745, 524.2205f, 551.79425f, 198.75f, (byte) 0);
				spawn(831745, 520.3118f, 549.126f, 198.78514f, (byte) 0);
				// Circus Exit
				spawn(831576, 483.58258f, 567.21149f, 201.73489f, (byte) 0);
				// Yume's Friends
				spawn(831560, 520.41559f, 563.30469f, 200.16179f, (byte) 53);
				spawn(831559, 517.784f, 562.0495f, 200.16179f, (byte) 30);
				spawn(831561, 517.84387f, 568.37622f, 200.16179f, (byte) 90);
				spawn(831562, 520.56641f, 567.83276f, 200.16179f, (byte) 73);
				break;
		}
	}

	protected void despawnNpc(Npc npc) {
		if (npc != null) {
			npc.getController().delete();
		}
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.sendPacket(player, new SM_DIE(false, false, 0, 8));
		return true;
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		TeleportService.teleportTo(player, mapId, instanceId, 473.54022f, 567.6342f, 201.83635f, (byte) 118);
		return true;
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
		effectController.removeEffect(player.getRace() == Race.ELYOS ? 21329 : 21332);
		effectController.removeEffect(player.getRace() == Race.ELYOS ? 21331 : 21334);
	}

	@Override
	public void onInstanceDestroy() {
		cancelSpawnTask();
		cancelDespawnBossTask();
		movies.clear();
		isInstanceDestroyed = true;
	}
}
