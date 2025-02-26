package instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancescore.NormalScore;
import com.aionemu.gameserver.network.aion.instanceinfo.EternalBastionScoreWriter;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * Remaining Online Information:<br>
 * Summarized guide: <a href="https://web.archive.org/web/20160918063923/http://aion.mouseclic.com:80/wiki/instance/bastion?lang=us">Link</a><br>
 * Guide based on interview: <a href="https://web.archive.org/web/20150215111653/http://aion.mouseclic.com:80/instances/bastion.php">Link</a><br>
 * Rewards: <<a href="https://aionpowerbook.com/powerbook/index.php?title=Steel_Wall_Bastion_-_Drop&setlang=en&aionclassic=0">Link</a><br>
 * Quick Summary:
 * Players need to defend the fortress commander while also progressing the instance and accumulating additional points by killing the surrounding
 * camp commanders. The first three phases are progressed by killing three specific commanders, whereas the fourth phase will be completed if all
 * five siege towers are killed.<br>
 * Killing a specific barricade or dredgion signal tower or activating the siege cannon will result in additional assault pod spawns.
 * Every two minutes an assault wave will spawn. It's strength, i.e. assaulter count, increases over time. Additional waves can spawn from
 * assault pods, siege towers or broken wall/gate.<br>
 * Players can skip specific waves by killing enough commanders and thus reducing the assault strength. They can also use the cannons or tank to
 * make defending/attacking easier.
 *
 * @author Cheatkiller, Estrayl
 */
@InstanceID(300540000)
public class EternalBastionInstance extends GeneralInstanceHandler {

	private static final int START_DELAY = 180 * 1000;
	private final AtomicInteger assaultPower = new AtomicInteger(12); // Retail
	private final AtomicInteger progressionKills = new AtomicInteger();
	private final AtomicBoolean isRaceSet = new AtomicBoolean();
	private final List<Future<?>> spawnTasks = new ArrayList<>();
	private Future<?> instanceTimerTask, assaultWaveTask;
	private int waveCount;
	private long startTime;
	private NormalScore instanceReward;

	public EternalBastionInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 231168: // Pashid Scout Commander Azute
			case 231169: // Pashid Scout Commander Zest
			case 231170: // Pashid Scout Commander Sartas
			case 231171: // Pashid Infantry Commander Matuk
			case 231172: // Pashid Assault Commander Badute
			case 231173: // Pashid Assault Commander Katsu
			case 231174: // Pashid Artillery Commander Murat
			case 231175: // Pashid Artillery Commander Kaimdu
			case 231176: // Pashid Artillery Commander Nirta
				addPoints(npc, 1880);
				checkProgress(progressionKills.incrementAndGet());
				break;
			case 231143: // Pashid Siege Tower
			case 231152: // Pashid Siege Tower
			case 231153: // Pashid Siege Tower
			case 231154: // Pashid Siege Tower
			case 231155: // Pashid Siege Tower
				addPoints(npc, 334);
				checkProgress(progressionKills.incrementAndGet());
				break;
			case 231177: // Deathbringer Tariksha
				addPoints(npc, 1880);
				break;
			case 231178: // Commander Hakunta
			case 231179: // Commander Rakunta
				addPoints(npc, 1880);
				assaultPower.addAndGet(-2); // Retail
				break;
			case 230784: // Pashid Snare Turret
			case 230785: // Pashid Assault Flamethrower
			case 231137: // Pashid Danuar Turret
			case 231138: // Pashid Danuar Turret
			case 231140: // Pashid Assault Pod
			case 231141: // Pashid Siege Drop Pod
			case 231144: // Pashid Siege Cannon
			case 231156: // Pashid Assault Pod
			case 231157: // Pashid Assault Pod
			case 231158: // Pashid Assault Pod
			case 231159: // Pashid Assault Pod
			case 231160: // Pashid Assault Pod
			case 231162: // Pashid Assault Pod
			case 231163: // Pashid Siege Drop Pod
			case 231164: // Pashid Siege Drop Pod
			case 231165: // Pashid Siege Drop Pod
			case 231167: // Pashid Siege Drop Pod
			case 231180: // Dredgion Signal Tower
				addPoints(npc, 334);
				break;
			case 231148: // Dredgion Signal Tower
				addPoints(npc, 334);
				PacketSendUtility.broadcastToMap(npc, SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_AddWave_03());
				spawnWithDelay(231157, 778.845f, 323.282f, 253.434f, (byte) 40, 30000);
				spawnWithDelay(231159, 697.564f, 305.424f, 249.303f, (byte) 100, 30000);
				break;
			case 231149: // Pashid Army Barricade
				addPoints(npc, 266);
				PacketSendUtility.broadcastToMap(npc, SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_AddWave_02());
				spawnWithDelay(231164, 667.350f, 281.046f, 225.698f, (byte) 33, 30000); // Pashid Assault Pod
				spawnWithDelay(231165, 721.498f, 358.172f, 230.940f, (byte) 0, 30000);
			case 231181: // Pashid Army Barricade
				addPoints(npc, 266);
				break;
			case 230746: // Pashid Assault Tribuni Sentry
			case 230753: // Pashid Assault Rider
			case 230754: // Pashid Assault Gunner
			case 230756: // Pashid Assault Supply Officer
			case 230757: // Pashid Assault Dragon
				addPoints(npc, 1002);
				assaultPower.decrementAndGet();
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_Notice_06());
				break;
			case 230744: // Pashid Assault Tribuni Combatant
			case 230745: // Pashid Assault Tribuni Protector
			case 230749: // Pashid Assault Tribuni Marksman
			case 231131: // Pashid Siege Dragon
			case 231132: // Pashid Siege Dragon
			case 231133: // Pashid Siege Dragon
			case 231134: // Pashid Siege Dragon
				addPoints(npc, 1002);
				break;
			case 831333: // Castle Wall
				addPoints(npc, -150);
				deleteAliveNpcs(831332); // Right Castle Gate
				deleteAliveNpcs(231150); // Drill
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_Notice_04());
				break;
			case 831335: // Inner Water Gate
				addPoints(npc, -150);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_Notice_02());
				break;
			case 209516: // Commander Lysander
			case 209517: // Commander Granir
				addPoints(npc, -100000); // Retail
				endInstance();
				break;
			case 209555: // Lysander's Disciple
			case 209557: // Granir's Disciple
				addPoints(npc, -50);
				break;
			case 231130: // Grand Commander Pashid
				addPoints(npc, 24000);
				endInstance();
				break;
			case 231117: // Pashid Elite Siege Combatant
			case 231118: // Pashid Elite Siege Protector
			case 231119: // Pashid Elite Siege Ambusher
			case 231120: // Pashid Elite Siege Troublemaker
			case 231122: // Pashid Elite Siege Marksman
			case 231123: // Pashid Elite Siege Rampager
			case 231124: // Pashid Elite Siege Magus
			case 231125: // Pashid Elite Siege Summoner
			case 231126: // Pashid Elite Siege Cavalry
			case 231127: // Pashid Elite Siege Striker
			case 231128: // Pashid Elite Siege Medic
			case 233310: // Pashid Siege Cavalry
			case 233311: // Pashid Siege Engineer
				addPoints(npc, 42);
				break;
			case 233312: // Pashid Siege Healer
			case 233314: // Pashid Elite Siege Defender
			case 233315: // Pashid Elite Siege Gunner
				addPoints(npc, 36);
				break;
			case 231115: // Pashid Siege Soldier
			case 231116: // Pashid Siege Mage
			case 233309: // Pashid Siege Ambusher
				addPoints(npc, 33);
				break;
			case 233313:
				addPoints(npc, 20);
				break;
		}
	}

	private void checkProgress(int progressionKills) {
		switch (progressionKills) {
			case 3 -> {
				Npc outerWaterGate = getNpc(831334);
				if (outerWaterGate != null)
					outerWaterGate.getController().deleteIfAliveOrCancelRespawn();
				spawn(233314, 575.858f, 146.753f, 221.351f, (byte) 33); // Pashid Elite Siege Defender
				spawn(233314, 587.445f, 152.020f, 218.004f, (byte) 63);
				spawn(233314, 609.691f, 187.747f, 216.455f, (byte) 87);
				spawn(233314, 630.440f, 192.271f, 219.763f, (byte) 40);
				spawn(233315, 598.051f, 160.956f, 216.754f, (byte) 100); // Pashid Elite Siege Gunner
				spawn(233315, 609.099f, 150.973f, 216.063f, (byte) 57);
				spawn(233315, 637.820f, 203.284f, 222.032f, (byte) 77);
				spawn(233315, 641.959f, 197.833f, 221.788f, (byte) 77);

				spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> {
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_MainWave_02());
					spawn(231171, 655.755f, 212.606f, 223.931f, (byte) 80); // Pashid Infantry Commander Matuk
					spawnWithWalker(231142, 604.397f, 170.492f, 216.042f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_Z1_S2_D1"); // Pashid Siege Volatile
					spawnWithWalker(231142, 605.397f, 171.492f, 216.092f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_Z1_S2_D1");
					spawnWithWalker(231142, 603.397f, 171.492f, 216.085f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_Z1_S2_D1");
					spawnWithWalker(231173, 657.052f, 465.173f, 225.052f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_Z1_S2_B2F2"); // Pashid Assault Commander Katsu
					spawnWithWalker(233313, 659.052f, 467.173f, 225.000f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_Z1_S2_B2F2");
					spawnWithWalker(233313, 655.052f, 467.173f, 225.133f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_Z1_S2_B2F2");
					spawnWithWalker(231172, 604.429f, 413.910f, 223.782f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_Z1_S2_B1F2"); // Pashid Assault Commander Badute
					spawnWithWalker(233313, 606.429f, 411.910f, 224.027f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_Z1_S2_B1F2");
					spawnWithWalker(233313, 602.429f, 411.910f, 223.756f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_Z1_S2_B1F2");
				}, 90, TimeUnit.SECONDS));
			}
			case 6 -> spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> {
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_MainWave_03());
				spawn(233313, 572.428f, 368.118f, 226.464f, (byte) 113); // Pashid Siege Combatant
				spawn(233313, 577.691f, 374.779f, 226.077f, (byte) 110);
				spawn(233313, 583.372f, 380.359f, 225.562f, (byte) 107);
				spawn(233313, 590.788f, 386.729f, 224.273f, (byte) 100);
				spawn(233313, 652.680f, 456.840f, 225.698f, (byte) 110);
				spawn(233313, 660.402f, 469.521f, 225.095f, (byte) 113);
				spawn(233313, 670.701f, 477.320f, 225.120f, (byte) 100);
				spawn(233313, 681.626f, 481.653f, 224.853f, (byte) 100);
				spawn(231137, 569.389f, 374.023f, 228.221f, (byte) 110); // Pashid Danuar Turret
				spawn(231137, 576.424f, 381.682f, 226.099f, (byte) 107);
				spawn(231137, 584.247f, 388.219f, 225.080f, (byte) 103);
				spawn(231138, 650.886f, 466.252f, 225.282f, (byte) 110);
				spawn(231138, 661.941f, 478.229f, 226.286f, (byte) 103);
				spawn(231138, 673.506f, 486.307f, 225.869f, (byte) 100);
				spawn(231140, 635.426f, 243.117f, 238.075f, (byte) 33); // Pashid Assault Pods
				spawn(231141, 666.361f, 294.435f, 225.698f, (byte) 20);
				spawn(231158, 768.339f, 390.709f, 243.356f, (byte) 40);
				spawn(231174, 669.851f, 468.267f, 225.250f, (byte) 107); // Pashid Artillery Commander Murat
				spawn(231175, 583.830f, 373.812f, 225.280f, (byte) 107); // Pashid Artillery Commander Kaimdu
				spawn(231176, 760.219f, 392.471f, 243.354f, (byte) 50); // Pashid Infantry Commander Nirta
			}, 90, TimeUnit.SECONDS));
			case 9 -> spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> {
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_MainWave_04());
				spawn(231143, 613.231f, 262.163f, 227.255f, (byte) 3);
				spawn(231152, 608.371f, 303.514f, 226.295f, (byte) 113);
				spawn(231153, 625.244f, 352.624f, 226.295f, (byte) 113);
				spawn(231154, 668.864f, 405.970f, 228.500f, (byte) 83);
				spawn(231155, 691.536f, 409.367f, 231.720f, (byte) 98);
			}, 90, TimeUnit.SECONDS));
			case 14 -> spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> {
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_MainWave_05());
				spawn(231130, 740.668f, 298.082f, 233.889f, (byte) 100); // Commander Pashid
				spawn(231131, 686.574f, 358.216f, 243.386f, (byte) 100); // Pashid Siege Dragons
				spawn(231131, 655.856f, 351.118f, 241.595f, (byte) 20);
				spawn(231131, 732.982f, 371.320f, 230.942f, (byte) 106);
				spawn(231132, 582.631f, 376.172f, 225.461f, (byte) 100);
				spawn(231133, 745.820f, 322.916f, 249.287f, (byte) 86);
				spawn(231133, 713.242f, 289.971f, 249.285f, (byte) 0);
				spawn(231134, 668.732f, 473.705f, 225.159f, (byte) 100);
				spawn(231156, 641.551f, 339.264f, 238.075f, (byte) 20); // Pashid Assault Pods
				spawn(231163, 727.175f, 364.431f, 230.941f, (byte) 7);
			}, 90, TimeUnit.SECONDS));
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		if (!instanceReward.isRewarded())
			sendPacket();
		if (isRaceSet.compareAndSet(false, true)) {
			spawnRaceGuards(player.getRace());
			if (Rnd.nextBoolean()) {
				spawn(231177, 821.146f, 607.305f, 239.703f, (byte) 73); // Deathbringer Tariksha
				spawn(230746, 551.146f, 412.105f, 222.760f, (byte) 30); // Pashid Assault Tribuni Sentry
				spawn(231149, 702.116f, 552.614f, 232.423f, (byte) 110); // Pashid Army Barricade (Assault Pod Trigger)
				spawn(231181, 564.414f, 250.835f, 233.198f, (byte) 110); // Pashid Army Barricade
			} else {
				spawn(230746, 821.146f, 607.305f, 239.703f, (byte) 73); // Pashid Assault Tribuni Sentry
				spawn(231177, 551.146f, 412.105f, 222.760f, (byte) 30); // Deathbringer Tariksha
				spawn(231181, 702.116f, 552.614f, 232.423f, (byte) 110); // Pashid Army Barricade
				spawn(231149, 564.414f, 250.835f, 233.198f, (byte) 110); // Pashid Army Barricade (Assault Pod Trigger)
			}
		}
	}

	private void spawnRaceGuards(Race race) {
		int guardId = race == Race.ELYOS ? 209555 : 209557;
		spawn(race == Race.ELYOS ? 209516 : 209517, 750.205f, 285.880f, 233.752f, (byte) 40); // Commander
		spawn(race == Race.ELYOS ? 701923 : 701924, 744.174f, 292.949f, 233.698f, (byte) 40); // Flag
		spawn(race == Race.ELYOS ? 701625 : 701922, 640.862f, 412.784f, 243.940f, (byte) 40); // Siege Cannon
		spawn(guardId, 595.476f, 284.680f, 226.375f, (byte) 40);
		spawn(guardId, 598.868f, 284.201f, 226.424f, (byte) 40);
		spawn(guardId, 602.328f, 340.964f, 225.794f, (byte) 40);
		spawn(guardId, 605.731f, 343.153f, 225.448f, (byte) 40);
		spawn(guardId, 607.450f, 387.642f, 223.353f, (byte) 40);
		spawn(guardId, 611.817f, 388.865f, 223.500f, (byte) 40);
		spawn(guardId, 681.742f, 444.580f, 226.818f, (byte) 40);
		spawn(guardId, 684.437f, 447.848f, 226.787f, (byte) 40);
		spawn(guardId, 690.046f, 351.800f, 244.744f, (byte) 40);
		spawn(guardId, 690.220f, 341.532f, 228.674f, (byte) 40);
		spawn(guardId, 692.778f, 337.952f, 228.674f, (byte) 40);
		spawn(guardId, 693.082f, 354.432f, 244.733f, (byte) 40);
		spawn(guardId, 715.405f, 427.312f, 230.025f, (byte) 40);
		spawn(guardId, 719.378f, 428.101f, 230.112f, (byte) 40);
		spawn(guardId, 748.146f, 361.345f, 230.945f, (byte) 40);
		spawn(guardId, 749.389f, 364.988f, 230.945f, (byte) 40);
		if (race == Race.ELYOS) {
			spawn(701596, 617.501f, 248.196f, 235.740f, (byte) 60); // Cannons
			spawn(701597, 612.806f, 275.206f, 235.740f, (byte) 67);
			spawn(701598, 616.159f, 313.939f, 235.740f, (byte) 53);
			spawn(701599, 625.603f, 339.608f, 235.734f, (byte) 53);
			spawn(701600, 650.914f, 372.932f, 238.607f, (byte) 53);
			spawn(701601, 677.853f, 396.203f, 238.632f, (byte) 40);
			spawn(701602, 710.145f, 410.661f, 241.014f, (byte) 30);
			spawn(701603, 736.803f, 414.121f, 241.017f, (byte) 40);
			spawn(701604, 772.961f, 410.834f, 241.014f, (byte) 20);
			spawn(701605, 798.383f, 401.605f, 241.015f, (byte) 30);
			spawn(701606, 709.602f, 313.531f, 254.216f, (byte) 40);
			spawn(701607, 726.757f, 327.932f, 254.216f, (byte) 50);
		} else {
			spawn(701610, 617.501f, 248.196f, 235.740f, (byte) 60); // Cannons
			spawn(701611, 612.806f, 275.206f, 235.740f, (byte) 67);
			spawn(701612, 616.159f, 313.939f, 235.740f, (byte) 53);
			spawn(701613, 625.603f, 339.608f, 235.734f, (byte) 53);
			spawn(701614, 650.914f, 372.932f, 238.607f, (byte) 53);
			spawn(701615, 677.853f, 396.203f, 238.632f, (byte) 40);
			spawn(701616, 710.145f, 410.661f, 241.014f, (byte) 30);
			spawn(701617, 736.803f, 414.121f, 241.017f, (byte) 40);
			spawn(701618, 772.961f, 410.834f, 241.014f, (byte) 20);
			spawn(701619, 798.383f, 401.605f, 241.015f, (byte) 30);
			spawn(701620, 709.602f, 313.531f, 254.216f, (byte) 40);
			spawn(701621, 726.757f, 327.932f, 254.216f, (byte) 50);
		}
	}

	@Override
	public void onInstanceCreate() {
		instanceReward = new NormalScore();
		instanceReward.setInstanceProgressionType(InstanceProgressionType.PREPARING);
		instanceReward.setPoints(20000);
		startTime = System.currentTimeMillis();
		instanceTimerTask = ThreadPoolManager.getInstance().schedule(this::onStart, START_DELAY);
	}

	private void onStart() {
		startTime = System.currentTimeMillis();
		instanceReward.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
		sendPacket();
		instance.forEachDoor(door -> door.setOpen(true));
		assaultWaveTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::spawnAssaultWave, 60000, 60000);
		instanceTimerTask = ThreadPoolManager.getInstance().schedule(this::onTimeOut, 30, TimeUnit.MINUTES);

		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_MainWave_01());
		spawn(233313, 584.013f, 371.221f, 225.374f, (byte) 110); // Pashid Siege Fighter
		spawn(233313, 588.725f, 377.543f, 225.221f, (byte) 110);
		spawn(233313, 655.190f, 454.515f, 225.936f, (byte) 110);
		spawn(233313, 659.293f, 461.406f, 225.449f, (byte) 110);
		spawn(233313, 795.178f, 462.909f, 225.853f, (byte) 118);
		spawn(233313, 804.449f, 461.860f, 227.897f, (byte) 58);
		spawn(233315, 572.093f, 377.641f, 227.147f, (byte) 110); // Pashid Elite Siege Gunner
		spawn(233315, 580.561f, 387.814f, 225.668f, (byte) 110);
		spawn(233315, 646.597f, 458.471f, 225.575f, (byte) 117);
		spawn(233315, 652.617f, 467.432f, 225.265f, (byte) 113);
		spawn(233315, 794.179f, 474.019f, 225.361f, (byte) 88);
		spawn(233315, 806.574f, 473.837f, 227.837f, (byte) 98);
		spawn(231168, 652.191f, 461.264f, 225.095f, (byte) 110); // Pashid Scout Commander Azute
		spawn(231169, 581.777f, 377.664f, 225.528f, (byte) 110); // Pashid Scout Commander Zest
		spawn(231170, 800.515f, 469.416f, 228.586f, (byte) 88); // Pashid Scout Commander Sartas
		spawn(831334, 569.772f, 162.763f, 220.048f, (byte) 53, 271); // Outer Water Gate
		spawnWithDelay(231167, 735.282f, 295.307f, 233.752f, (byte) 115, 9000); // Pashid Assault Pods
		spawnWithDelay(231162, 747.273f, 300.182f, 233.752f, (byte) 97, 6000);
	}

	private void spawnAssaultWave() {
		switch (++waveCount) {
			case 1, 5, 13, 17, 25 -> spawnAssaultPodWave();
			case 2 -> spawnEasternWaveOne();
			case 4 -> {
				spawnEasternWaveOne();
				spawnNorthernWaveOne();
			}
			case 6 -> {
				spawnEasternWaveOne();
				spawnNorthernWaveOne();
				spawnEasternWaveTwo();
				spawnCanalWave();
				spawnSiegeTowerWave();
			}
			case 8 -> {
				spawnEasternWaveOne();
				spawnNorthernWaveOne();
				spawnEasternWaveTwo();
				spawnWesternWave();
			}
			case 9, 21 -> {
				spawnAssaultPodWave();
				spawnSiegeTowerWave();
			}
			case 10 -> {
				spawnEasternWaveOne();
				spawnNorthernWaveOne();
				spawnEasternWaveTwo();
				spawnWesternWave();
				spawnNorthernWaveTwo();
				spawnCanalWave();
				if (assaultPower.get() >= 8) {
					spawnWithWalker(231142, 795.579f, 478.629f, 225.086f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S38"); // Pashid Siege Volatile
					spawnWithWalker(231142, 798.579f, 479.629f, 225.221f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S38");
					spawnWithWalker(231142, 792.579f, 479.629f, 224.934f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S38");
					spawnWithWalker(231142, 801.579f, 481.629f, 225.845f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S38");
					spawnWithWalker(231142, 789.579f, 481.629f, 224.622f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S38");
				}
			}
			case 12 -> {
				spawnEasternWaveOne();
				spawnNorthernWaveOne();
				spawnEasternWaveTwo();
				spawnWesternWave();
				spawnNorthernWaveTwo();
				spawnEasternWaveThree();
				spawnSiegeTowerWave();
			}
			case 14 -> {
				spawnEasternWaveOne();
				spawnNorthernWaveOne();
				spawnEasternWaveTwo();
				spawnWesternWave();
				spawnNorthernWaveTwo();
				spawnEasternWaveThree();
				spawnCanalWave();
			}
			case 15 -> {
				spawnCanalWave();
				spawnSiegeTowerWave();
				if (assaultPower.get() >= 7 && getNpc(831333) != null)
					spawnWithWalker(231150, 798.563f, 477.952f, 225.231f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S73"); // Pashid Siege Ram
			}
			case 16 -> {
				if (assaultPower.get() >= 11)
					spawnEasternWaveOne();
				spawnNorthernWaveOne();
				spawnEasternWaveTwo();
				if (assaultPower.get() >= 12)
					spawnWesternWave();
				spawnNorthernWaveTwo();
				spawnEasternWaveThree();
			}
			case 18 -> {
				spawnEasternWaveOne();
				spawnNorthernWaveOne();
				if (assaultPower.get() >= 10)
					spawnEasternWaveTwo();
				spawnWesternWave();
				if (assaultPower.get() >= 9)
					spawnNorthernWaveTwo();
				spawnEasternWaveThree();
				spawnCanalWave();
				spawnSouthernWave();
				spawnSiegeTowerWave();
			}
			case 20 -> {
				spawnEasternWaveOne();
				spawnNorthernWaveOne();
				spawnEasternWaveTwo();
				spawnWesternWave();
				spawnNorthernWaveTwo();
				spawnEasternWaveThree();
			}
			case 22 -> {
				spawnEasternWaveOne();
				spawnNorthernWaveOne();
				spawnEasternWaveTwo();
				spawnWesternWave();
				spawnNorthernWaveTwo();
				spawnEasternWaveThree();
				spawnCanalWave();
				spawnSouthernWave();
			}
			case 24 -> {
				if (assaultPower.get() >= 5)
					spawnEasternWaveOne();
				spawnNorthernWaveOne();
				spawnEasternWaveTwo();
				if (assaultPower.get() >= 6)
					spawnWesternWave();
				spawnNorthernWaveTwo();
				spawnEasternWaveThree();
				spawnSiegeTowerWave();
			}
			case 26 -> {
				spawnEasternWaveOne();
				if (assaultPower.get() >= 3)
					spawnNorthernWaveOne();
				spawnEasternWaveTwo();
				spawnWesternWave();
				spawnNorthernWaveTwo();
				if (assaultPower.get() >= 4)
					spawnEasternWaveThree();
				spawnCanalWave();
				spawnSouthernWave();
			}
			case 28 -> {
				spawnEasternWaveOne();
				spawnNorthernWaveOne();
				if (assaultPower.get() >= 1)
					spawnEasternWaveTwo();
				spawnWesternWave();
				if (assaultPower.get() >= 2)
					spawnNorthernWaveTwo();
				spawnEasternWaveThree();
			}
		}
	}

	private void spawnEasternWaveOne() {
		spawnWithWalker(231113, 652.071f, 475.738f, 226.125f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S46"); // East 1
		spawnWithWalker(231110, 655.071f, 478.738f, 226.125f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S46");
		spawnWithWalker(231110, 649.071f, 478.738f, 226.125f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S46");
	}

	private void spawnEasternWaveTwo() {
		spawnWithWalker(231114, 671.857f, 480.417f, 225.195f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S32"); // East 2
		spawnWithWalker(231112, 674.857f, 483.417f, 225.337f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S32");
		spawnWithWalker(231112, 668.857f, 483.417f, 226.457f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S32");
	}

	private void spawnEasternWaveThree() {
		spawnWithWalker(231113, 632.525f, 451.311f, 223.422f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S34"); // East 3
		spawnWithWalker(231111, 635.525f, 454.311f, 223.193f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S34");
		spawnWithWalker(231111, 629.525f, 454.311f, 220.445f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S34");
	}

	private void spawnNorthernWaveOne() {
		spawnWithWalker(231113, 598.026f, 411.715f, 223.784f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S71"); // North 1
		spawnWithWalker(231110, 601.026f, 414.715f, 223.519f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S71");
		spawnWithWalker(231110, 595.026f, 414.715f, 223.552f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S71");
	}

	private void spawnNorthernWaveTwo() {
		spawnWithWalker(231113, 569.237f, 387.007f, 227.533f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S36"); // North 2
		spawnWithWalker(231111, 572.237f, 390.007f, 227.905f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S36");
		spawnWithWalker(231111, 566.237f, 390.007f, 228.194f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S36");
	}

	private void spawnWesternWave() {
		spawnWithWalker(231114, 587.952f, 239.621f, 229.530f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S44"); // West
		spawnWithWalker(231112, 590.952f, 242.621f, 229.152f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S44");
		spawnWithWalker(231112, 584.952f, 242.621f, 229.822f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S44");
	}

	private void spawnSouthernWave() {
		if (getNpc(831333) == null) {
			spawnWithWalker(231113, 794.134f, 483.021f, 224.756f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S39"); // South Wall
			spawnWithWalker(231113, 796.134f, 481.021f, 225.008f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S39");
			spawnWithWalker(231113, 792.134f, 481.021f, 224.820f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_S39");
		}
	}

	private void spawnCanalWave() {
		if (getNpc(831335) == null) {
			spawnWithWalker(231110, 610.571f, 189.724f, 216.509f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_Z1_S2_T1"); // Canal
			spawnWithWalker(231108, 612.571f, 191.724f, 216.589f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_Z1_S2_T1");
			spawnWithWalker(231108, 608.571f, 187.724f, 216.574f, (byte) 100, "NPCPathIDLDF5b_TD_Mob_Z1_S2_T1");
		}
	}

	private void spawnAssaultPodWave() {
		if (getNpc(231140) != null) {
			spawnWithWalker(231106, 633.457f, 245.792f, 238.075f, (byte) 33, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD01");
			spawnWithWalker(231108, 635.457f, 247.792f, 238.075f, (byte) 33, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD01");
			spawnWithWalker(231108, 631.457f, 247.792f, 238.075f, (byte) 33, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD01");
		}
		if (getNpc(231156) != null) {
			spawnWithWalker(231106, 642.871f, 343.420f, 238.075f, (byte) 20, "NPCPathIDLDF5b_TD_Z1_S5_POD01");
			spawnWithWalker(231108, 644.871f, 345.420f, 238.075f, (byte) 20, "NPCPathIDLDF5b_TD_Z1_S5_POD01");
			spawnWithWalker(231108, 640.871f, 345.420f, 238.075f, (byte) 20, "NPCPathIDLDF5b_TD_Z1_S5_POD01");
		}
		if (getNpc(231157) != null) {
			spawnWithWalker(231106, 776.242f, 326.041f, 253.434f, (byte) 40, "NPCPathIDLDF5b_TD_Z4_POD02");
			spawnWithWalker(231108, 778.242f, 328.041f, 253.434f, (byte) 40, "NPCPathIDLDF5b_TD_Z4_POD02");
			spawnWithWalker(231108, 774.242f, 328.041f, 253.434f, (byte) 40, "NPCPathIDLDF5b_TD_Z4_POD02");
		}
		if (getNpc(231158) != null) {
			spawnWithWalker(231106, 765.481f, 393.614f, 243.354f, (byte) 40, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD3");
			spawnWithWalker(231108, 767.481f, 395.614f, 243.354f, (byte) 40, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD3");
			spawnWithWalker(231108, 763.481f, 395.614f, 243.354f, (byte) 40, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD3");
		}
		if (getNpc(231141) != null) {
			spawnWithWalker(231105, 667.631f, 297.565f, 225.700f, (byte) 20, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD2");
			spawnWithWalker(231107, 669.631f, 299.565f, 225.700f, (byte) 20, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD2");
			spawnWithWalker(231107, 665.631f, 299.565f, 225.700f, (byte) 20, "NPCPathIDLDF5b_TD_Mob_Z1_S3_POD2");
		}
		if (getNpc(231163) != null) {
			spawnWithWalker(231105, 731.089f, 365.461f, 230.941f, (byte) 7, "NPCPathIDLDF5b_TD_Z1_S5_POD02");
			spawnWithWalker(231107, 731.089f, 365.461f, 230.941f, (byte) 7, "NPCPathIDLDF5b_TD_Z1_S5_POD02");
			spawnWithWalker(231107, 731.089f, 365.461f, 230.941f, (byte) 7, "NPCPathIDLDF5b_TD_Z1_S5_POD02");
		}
		if (getNpc(231159) != null) {
			spawnWithWalker(231106, 699.760f, 302.938f, 249.303f, (byte) 100, "NPCPathIDLDF5b_TD_Z4_POD01");
			spawnWithWalker(231108, 701.760f, 304.938f, 249.303f, (byte) 100, "NPCPathIDLDF5b_TD_Z4_POD01");
			spawnWithWalker(231108, 697.760f, 304.938f, 249.303f, (byte) 100, "NPCPathIDLDF5b_TD_Z4_POD01");
		}
		if (getNpc(231162) != null) { // Could be a bug on retail, but anyway
			spawnWithWalker(231106, 724.927f, 359.346f, 230.941f, (byte) 0, "NPCPathIDLDF5b_TD_Z3_POD02");
			spawnWithWalker(231108, 726.927f, 361.346f, 230.941f, (byte) 0, "NPCPathIDLDF5b_TD_Z3_POD02");
			spawnWithWalker(231108, 722.927f, 361.346f, 230.941f, (byte) 0, "NPCPathIDLDF5b_TD_Z3_POD02");
		}
		if (getNpc(231164) != null) {
			spawnWithWalker(231106, 724.927f, 359.346f, 230.941f, (byte) 0, "NPCPathIDLDF5b_TD_Z3_POD02");
			spawnWithWalker(231108, 726.927f, 361.346f, 230.941f, (byte) 0, "NPCPathIDLDF5b_TD_Z3_POD02");
			spawnWithWalker(231108, 722.927f, 361.346f, 230.941f, (byte) 0, "NPCPathIDLDF5b_TD_Z3_POD02");
		}
		if (getNpc(231165) != null) {
			spawnWithWalker(231106, 724.927f, 359.346f, 230.941f, (byte) 0, "NPCPathIDLDF5b_TD_Z3_POD02");
			spawnWithWalker(231108, 726.927f, 361.346f, 230.941f, (byte) 0, "NPCPathIDLDF5b_TD_Z3_POD02");
			spawnWithWalker(231108, 722.927f, 361.346f, 230.941f, (byte) 0, "NPCPathIDLDF5b_TD_Z3_POD02");
		}
	}

	private void spawnSiegeTowerWave() {
		if (getNpc(230783) != null) {
			spawnWithWalker(231107, 623.235f, 263.392f, 238.484f, (byte) 3, "NPCPathIDLDF5b_TD_Z1_S4_T1");
			spawnWithWalker(231105, 625.235f, 265.392f, 238.484f, (byte) 3, "NPCPathIDLDF5b_TD_Z1_S4_T1");
			spawnWithWalker(231105, 621.235f, 265.392f, 238.484f, (byte) 3, "NPCPathIDLDF5b_TD_Z1_S4_T1");
		}
		if (getNpc(231152) != null) {
			spawnWithWalker(231107, 621.920f, 298.179f, 238.075f, (byte) 113, "NPCPathIDLDF5b_TD_Z1_S4_T2");
			spawnWithWalker(231105, 623.920f, 300.179f, 238.075f, (byte) 113, "NPCPathIDLDF5b_TD_Z1_S4_T2");
			spawnWithWalker(231105, 619.920f, 300.179f, 238.075f, (byte) 113, "NPCPathIDLDF5b_TD_Z1_S4_T2");
		}
		if (getNpc(231153) != null) {
			spawnWithWalker(231107, 644.089f, 351.522f, 239.764f, (byte) 113, "NPCPathIDLDF5b_TD_Z1_S4_T3");
			spawnWithWalker(231105, 646.089f, 353.522f, 241.151f, (byte) 113, "NPCPathIDLDF5b_TD_Z1_S4_T3");
			spawnWithWalker(231105, 642.089f, 353.522f, 239.809f, (byte) 113, "NPCPathIDLDF5b_TD_Z1_S4_T3");
		}
		if (getNpc(231154) != null) {
			spawnWithWalker(231107, 664.091f, 394.303f, 240.223f, (byte) 83, "NPCPathIDLDF5b_TD_Z1_S4_T4");
			spawnWithWalker(231105, 666.091f, 396.303f, 240.223f, (byte) 83, "NPCPathIDLDF5b_TD_Z1_S4_T4");
			spawnWithWalker(231105, 662.091f, 396.303f, 240.223f, (byte) 83, "NPCPathIDLDF5b_TD_Z1_S4_T4");
		}
		if (getNpc(231155) != null) {
			spawnWithWalker(231107, 692.867f, 396.708f, 241.594f, (byte) 85, "NPCPathIDLDF5b_TD_Z1_S4_T5");
			spawnWithWalker(231105, 694.867f, 398.708f, 242.018f, (byte) 85, "NPCPathIDLDF5b_TD_Z1_S4_T5");
			spawnWithWalker(231105, 690.867f, 398.708f, 241.594f, (byte) 85, "NPCPathIDLDF5b_TD_Z1_S4_T5");
		}
	}

	private void onTimeOut() {
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_MainWave_06());
		endInstance();
	}

	/*
	 * Original points for ranks:
	 * 92,000 = S-Rank
	 * 84,000 = A-Rank
	 * 76,000 = B-Rank
	 * 50,000 = C-Rank
	 * 10,000 = D-Rank
	 */
	private void endInstance() {
		cancelTasks();
		instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);

		int rank = getFinalRank();
		switch (rank) {
			case 1 -> {
				instanceReward.setFinalAp(35000);
				instanceReward.setRewardItem1(186000242); // Ceramium Medal
				instanceReward.setRewardItem1Count(4);
				instanceReward.setRewardItem2(188052596); // Highest Grade Material Support Bundle
				instanceReward.setRewardItem2Count(1);
				instanceReward.setRewardItem3(188052594); // Highest Grade Material Box
				instanceReward.setRewardItem3Count(1);
			}
			case 2 -> {
				instanceReward.setFinalAp(25000);
				instanceReward.setRewardItem1(186000242); // Ceramium Medal
				instanceReward.setRewardItem1Count(2);
				instanceReward.setRewardItem2(188052594); // Highest Grade Material Box
				instanceReward.setRewardItem2Count(1);
				instanceReward.setRewardItem3(188052597); // High Grade Material Support Bundle
				instanceReward.setRewardItem3Count(1);
			}
			case 3 -> { // B-Rank
				instanceReward.setFinalAp(15000);
				instanceReward.setRewardItem1(186000242); // Ceramium Medal
				instanceReward.setRewardItem1Count(1);
				instanceReward.setRewardItem2(188052595); // High Grade Material Box
				instanceReward.setRewardItem2Count(1);
				instanceReward.setRewardItem3(188052598); // Low Grade Material Support Bundle
				instanceReward.setRewardItem3Count(1);
			}
			case 4 -> { // C-Rank
				instanceReward.setFinalAp(11000);
				instanceReward.setRewardItem1(188052598); // Low Grade Material Support Bundle
				instanceReward.setRewardItem1Count(1);
			}
			case 5 -> instanceReward.setFinalAp(7000); // D-Rank
		}
		instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
		instanceReward.setRank(rank);
		instance.forEachNpc(npc -> npc.getController().delete());
		sendPacket();
		instance.forEachPlayer(this::distributeRewards);
		spawnFinalChest(rank);
		spawn(730871, 766.458f, 263.157f, 233.498f, (byte) 100); // Exit
		log.info("[{}] Instance completed with {} points resulting in {}-Rank. Player(s) in instance: {}",
			DataManager.WORLD_MAPS_DATA.getTemplate(mapId).getName(), instanceReward.getPoints(), getRankNameById(rank),
			instance.getPlayersInside().stream().map(p -> String.format("%s (ID:%d)", p.getName(), p.getObjectId())).collect(Collectors.joining(", ")));
	}

	private int getFinalRank() {
		if (instanceReward.getPoints() >= 90000) { // S-Rank
			return 1;
		} else if (instanceReward.getPoints() >= 82000) { // A-Rank
			return 2;
		} else if (instanceReward.getPoints() >= 60000) { // B-Rank
			return 3;
		} else if (instanceReward.getPoints() >= 30000) { // C-Rank
			return 4;
		} else if (instanceReward.getPoints() >= 5000) { // D-Rank
			return 5;
		} else {
			return 8;
		}
	}

	private void spawnFinalChest(int rank) {
		switch (rank) {
			case 1 -> spawn(701913, 744.167f, 292.860f, 233.702f, (byte) 100); // Biggest in model size
			case 2 -> spawn(701914, 744.167f, 292.860f, 233.702f, (byte) 100);
			case 3 -> spawn(701915, 744.167f, 292.860f, 233.702f, (byte) 100);
			case 4 -> spawn(701916, 744.167f, 292.860f, 233.702f, (byte) 100);
			case 5 -> spawn(701917, 744.167f, 292.860f, 233.702f, (byte) 100); // Smallest in model size
		}
	}

	private void distributeRewards(Player player) {
		AbyssPointsService.addAp(player, instanceReward.getFinalAp());
		if (instanceReward.getRewardItem1() > 0)
			ItemService.addItem(player, instanceReward.getRewardItem1(), instanceReward.getRewardItem1Count(), true);
		if (instanceReward.getRewardItem2() > 0)
			ItemService.addItem(player, instanceReward.getRewardItem2(), instanceReward.getRewardItem2Count(), true);
		if (instanceReward.getRewardItem3() > 0)
			ItemService.addItem(player, instanceReward.getRewardItem3(), instanceReward.getRewardItem3Count(), true);
		if (instanceReward.getRewardItem4() > 0)
			ItemService.addItem(player, instanceReward.getRewardItem4(), instanceReward.getRewardItem4Count(), true);
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		if (npc.getNpcId() == 701625 || npc.getNpcId() == 701922) {
			SkillEngine.getInstance().getSkill(npc, 21069, 1, npc).useSkill();
			ThreadPoolManager.getInstance().schedule(() -> npc.getController().delete(), 3000);
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_AddWave_01(), 3000);
			spawnWithDelay(231160, 707.703f, 259.173f, 253.038f, (byte) 40, 33000); // Assault Pod
		}
	}

	@Override
	public void onEndEffect(Effect effect) {
		if (effect.getEffected()instanceof Player player && !player.isDead() && !player.getLifeStats().isAboutToDie()) {
			switch (effect.getSkillId()) {
				case 21138, 21139 -> { // Cannons respawn if not killed
					Point3D pos = new Point3D(player.getX(), player.getY(), player.getZ());
					Race race = player.getRace();
					spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> {
						if (instanceReward.getInstanceProgressionType() == InstanceProgressionType.START_PROGRESS)
							spawn(race == Race.ELYOS ? 701596 : 701610, pos.getX(), pos.getY(), pos.getZ(), (byte) 50);
					}, 10, TimeUnit.SECONDS));
				}
			}
		}
	}

	@Override
	public void onInstanceDestroy() {
		cancelTasks();
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		TeleportService.teleportTo(player, instance, 449.581f, 448.846f, 270.747f, (byte) 70);
		return true;
	}

	private void cancelTasks() {
		spawnTasks.forEach(task -> {
			if (task != null && !task.isDone())
				task.cancel(false);
		});
		if (instanceTimerTask != null && !instanceTimerTask.isCancelled())
			instanceTimerTask.cancel(false);
		if (assaultWaveTask != null && !assaultWaveTask.isCancelled())
			assaultWaveTask.cancel(false);
	}

	private void spawnWithDelay(int npcId, float x, float y, float z, byte h, int delay) {
		spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> spawn(npcId, x, y, z, h), delay));
	}

	private void spawnWithWalker(int npcId, float x, float y, float z, byte h, final String walker) {
		spawn(npcId, x, y, z, h).getSpawn().setWalkerId(walker);
	}

	private void addPoints(Npc npc, int points) {
		if (instanceReward.getInstanceProgressionType() == InstanceProgressionType.START_PROGRESS) {
			instanceReward.addPoints(points);
			PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(npc.getObjectTemplate().getL10n(), points));
			sendPacket();
		}
	}

	private int getTime() {
		int current = (int) (System.currentTimeMillis() - startTime);
		return instanceReward.getInstanceProgressionType() == InstanceProgressionType.PREPARING ? 180000 - current : Math.max(1800000 - current, 0);
	}

	private void sendPacket() {
		PacketSendUtility.broadcastToMap(instance, new SM_INSTANCE_SCORE(instance.getMapId(), new EternalBastionScoreWriter(instanceReward), getTime()));
	}

	@Override
	public void leaveInstance(Player player) {
		if (instanceReward.getInstanceProgressionType() == InstanceProgressionType.END_PROGRESS)
			TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	private String getRankNameById(int rank) {
		return switch (rank) {
			case 1 -> "S";
			case 2 -> "A";
			case 3 -> "B";
			case 4 -> "C";
			case 5 -> "D";
			default -> "F";
		};
	}

	@Override
	public boolean isBoss(Npc npc) {
		return switch (npc.getNpcId()) {
			case 209516, 209517, 231168, 231169, 231170, 231171, 231172, 231173, 231174, 231175, 231176, 231130 -> true;
			default -> false;
		};
	}
}
