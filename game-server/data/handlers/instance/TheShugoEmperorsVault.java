package instance;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.model.instance.instancescore.NormalScore;
import com.aionemu.gameserver.network.aion.instanceinfo.TheShugoEmperorsVaultScoreWriter;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Yeats
 */
@InstanceID(301400000)
public class TheShugoEmperorsVault extends GeneralInstanceHandler {

	private final Map<Integer, Integer> transformationCache = new ConcurrentHashMap<>(); // save transformations to avoid effect loss during sendlogs
	private final AtomicBoolean started = new AtomicBoolean();
	private final AtomicInteger killedShugoTurncoats = new AtomicInteger();
	private final AtomicInteger stage = new AtomicInteger();
	private final Set<Integer> spawns = ConcurrentHashMap.newKeySet();
	private NormalScore instanceReward;
	private Future<?> timer, failTimerTask;
	private long startTime;
	private boolean lastStage;

	public TheShugoEmperorsVault(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		instanceReward = new NormalScore();
		instanceReward.setInstanceProgressionType(InstanceProgressionType.PREPARING);
		spawnMorphShugos();
		if (timer == null) {
			startTime = System.currentTimeMillis();
			timer = ThreadPoolManager.getInstance().schedule(() -> {
				if (started.compareAndSet(false, true)) {
					startTime = System.currentTimeMillis();
					instanceReward.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
					sendPacket(null, 0);
					instance.setDoorState(430, true);
					startInstance();
					startFailTask();
				}
			}, 60000);
		}
	}

	@Override
	public void onOpenDoor(int door) {
		if (door == 430 && started.compareAndSet(false, true)) {
			if (timer != null && !timer.isDone())
				timer.cancel(false);
			instanceReward.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
			startTime = System.currentTimeMillis();
			sendPacket(null, 0);
			startInstance();
			if (failTimerTask == null)
				startFailTask();
		}
	}

	@Override
	public void onStartEffect(Effect effect) {
		if (effect != null) {
			switch (effect.getSkillId()) {
				case 21829, 21830, 21831, 21832, 21833, 21834 -> transformationCache.putIfAbsent(effect.getEffected().getObjectId(), effect.getSkillId());
			}
		}
	}

	private void startFailTask() {
		failTimerTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!instanceReward.isRewarded()) {
				checkRank(instanceReward.getPoints());
			}
		}, 480500); // 8min
	}

	private void startInstance() {
		spawnShugos();
		spawnRoom1();
	}

	@Override
	public void onEnterInstance(final Player player) {
		super.onEnterInstance(player);
		if (!instanceReward.isRewarded())
			sendPacket(null, 0);
		reApplyTransformation(player);
	}

	@Override
	public void onInstanceDestroy() {
		if (timer != null && !timer.isDone())
			timer.cancel(true);
		if (failTimerTask != null && !failTimerTask.isDone())
			failTimerTask.cancel(true);
		super.onInstanceDestroy();
	}

	private void reApplyTransformation(Player player) {
		if (transformationCache.containsKey(player.getObjectId()))
			SkillEngine.getInstance().applyEffectDirectly(transformationCache.get(player.getObjectId()), player, player);
	}

	private void spawnRoom1() {
		spawn(235629, 543.0397f, 379.352f, 395.5941f, (byte) -29);
		spawn(235629, 550.9587f, 386.137f, 395.4688f, (byte) -29);
		spawn(235629, 559.0825f, 378.723f, 395.27f, (byte) -29);
		spawn(235630, 558.6583f, 397.267f, 395.1358f, (byte) -29);
		spawn(235630, 547.8836f, 396.374f, 395.635f, (byte) -29);
		spawn(235660, 550.3436f, 394.35f, 395.8965f, (byte) 89);
	}

	private void spawnRoom2() {
		spawn(235652, 553.8901f, 458.2198f, 393.7637f, (byte) 82);
		spawn(235652, 552.2185f, 460.511f, 393.84183f, (byte) 70);
		spawn(235653, 554.2595f, 460.333f, 394.06946f, (byte) 73);
		spawn(235653, 543.0456f, 465.201f, 393.7852f, (byte) 4);
		spawn(235652, 542.338f, 463.762f, 393.91965f, (byte) 5);
		spawn(235652, 541.4067f, 465.952f, 393.82715f, (byte) 13);
		spawn(235653, 535.63f, 479.995f, 393.95377f, (byte) 90);
		spawn(235652, 533.9135f, 478.527f, 393.7392f, (byte) 99);
		spawn(235652, 534.0383f, 480.717f, 393.7548f, (byte) 94);
		spawn(235652, 528.2363f, 491.233f, 393.29797f, (byte) 113);
		spawn(235652, 527.017f, 493.8221f, 393.4868f, (byte) 108);
		spawn(235653, 529.137f, 493.5578f, 393.375f, (byte) 116);
		spawn(235652, 539.268f, 535.151f, 394.4232f, (byte) 89);
		spawn(235652, 537.238f, 533.194f, 394.1782f, (byte) 91);
		spawn(235652, 527.908f, 532.867f, 394.01147f, (byte) 92);
		spawn(235652, 525.535f, 531.957f, 394.33725f, (byte) 92);
		spawn(235652, 522.866f, 532.479f, 394.90833f, (byte) 93);
		spawn(235641, 536.034f, 540.707f, 393.75214f, (byte) 74);
		spawn(235641, 526.133f, 539.792f, 394.33356f, (byte) 7);
		spawn(235652, 534.795f, 545.717f, 393.8253f, (byte) 84);
		spawn(235652, 525.675f, 543.710f, 394.52032f, (byte) 105);
		spawn(235644, 530.4512f, 551.6766f, 393.7782f, (byte) 89);
	}

	private void spawnRoom3() {
		spawn(235634, 485.096f, 639.773f, 395.92987f, (byte) 94);
	}

	private void spawnRoom4() {
		spawnAndSetRespawn(235637, 423.61447f, 729.6906f, 398.42203f, (byte) 33, 90);
		spawnAndSetRespawn(235635, 352.69745f, 712.68024f, 398.42203f, (byte) 15, 90);
		spawnAndSetRespawn(235680, 400.42035f, 727.32336f, 398.42203f, (byte) 2, 90);
		spawnAndSetRespawn(235650, 378.0228f, 684.02826f, 398.42203f, (byte) 84, 90);
		spawnAndSetRespawn(235635, 379.9993f, 691.18524f, 398.42203f, (byte) 116, 90);
		spawnAndSetRespawn(235635, 381.25742f, 697.28516f, 398.42203f, (byte) 105, 90);
		spawnAndSetRespawn(235635, 385.989f, 699.4102f, 398.42203f, (byte) 101, 90);
		spawnAndSetRespawn(235681, 381.38754f, 713.63446f, 398.42203f, (byte) 15, 90);
		spawnAndSetRespawn(235681, 383.76205f, 711.30884f, 398.42203f, (byte) 16, 90);
		spawnAndSetRespawn(235637, 405.62924f, 762.81384f, 398.42203f, (byte) 114, 90);
		spawnAndSetRespawn(235635, 423.06882f, 735.8446f, 398.42203f, (byte) 0, 90);
		spawnAndSetRespawn(235650, 399.2653f, 695.98505f, 398.42203f, (byte) 74, 90);
		spawnAndSetRespawn(235650, 402.23898f, 782.25745f, 398.42203f, (byte) 49, 90);
		spawnAndSetRespawn(235635, 393.6548f, 766.0695f, 398.42203f, (byte) 108, 90);
		spawnAndSetRespawn(235635, 394.32178f, 701.4838f, 398.42203f, (byte) 74, 90);
		spawnAndSetRespawn(235680, 390.20734f, 777.6227f, 398.42203f, (byte) 17, 90);
		spawnAndSetRespawn(235650, 388.70392f, 764.589f, 398.42203f, (byte) 45, 90);
		spawnAndSetRespawn(235651, 389.43408f, 687.66925f, 398.42203f, (byte) 28, 90);
		spawnAndSetRespawn(235637, 397.21872f, 776.68567f, 398.42203f, (byte) 56, 90);
		spawnAndSetRespawn(235635, 378.5506f, 746.2136f, 398.42203f, (byte) 85, 90);
		spawnAndSetRespawn(235635, 382.0421f, 769.5312f, 398.42203f, (byte) 44, 90);
		spawnAndSetRespawn(235637, 386.8663f, 734.6197f, 398.42203f, (byte) 93, 90);
		spawnAndSetRespawn(235651, 383.6482f, 779.2648f, 398.42203f, (byte) 93, 90);
		spawnAndSetRespawn(235681, 389.91922f, 734.438f, 398.42203f, (byte) 100, 90);
		spawnAndSetRespawn(235637, 344.79205f, 746.1171f, 398.42203f, (byte) 70, 90);
		spawnAndSetRespawn(235637, 392.1462f, 738.2172f, 398.42203f, (byte) 108, 90);
		spawnAndSetRespawn(235635, 374.55893f, 777.6737f, 398.42203f, (byte) 29, 90);
		spawnAndSetRespawn(235635, 361.5572f, 735.9487f, 398.42203f, (byte) 26, 90);
		spawnAndSetRespawn(235635, 350.42673f, 743.33057f, 398.42203f, (byte) 21, 90);
		spawnAndSetRespawn(235636, 361.23822f, 728.20465f, 398.42203f, (byte) 13, 90);
		spawnAndSetRespawn(235650, 423.15427f, 741.9008f, 398.42203f, (byte) 44, 90);
		spawnAndSetRespawn(235637, 341.50488f, 722.87396f, 398.42203f, (byte) 23, 90);
		spawnAndSetRespawn(235650, 336.37634f, 723.83484f, 398.42203f, (byte) 88, 90);
		spawnAndSetRespawn(235650, 351.52338f, 734.4255f, 398.42203f, (byte) 70, 90);
		spawnAndSetRespawn(235635, 349.0813f, 727.07935f, 398.42203f, (byte) 92, 90);
		spawnAndSetRespawn(235635, 426.55957f, 746.3025f, 398.42203f, (byte) 102, 90);
		spawnAndSetRespawn(235637, 385.46777f, 716.5629f, 398.42203f, (byte) 72, 90);
		spawnAndSetRespawn(235637, 400.8014f, 713.61847f, 398.42203f, (byte) 117, 90);
		spawnAndSetRespawn(235681, 403.21252f, 719.55286f, 398.42203f, (byte) 104, 90);
		spawnAndSetRespawn(235637, 407.94897f, 721.49384f, 398.42203f, (byte) 102, 90);
		spawnAndSetRespawn(235638, 351.84393f, 719.6718f, 398.42203f, (byte) 77, 90);
		spawnAndSetRespawn(235637, 433.31375f, 751.8881f, 398.42203f, (byte) 22, 90);
		spawnAndSetRespawn(235637, 372.8639f, 715.69763f, 398.42203f, (byte) 14, 90);
		spawnAndSetRespawn(235651, 340.65656f, 735.55255f, 398.42203f, (byte) 116, 90);
		spawnAndSetRespawn(235681, 367.5229f, 709.8396f, 398.42203f, (byte) 14, 90);
		spawnAndSetRespawn(235681, 365.3486f, 711.52545f, 398.42203f, (byte) 13, 90);
		spawnAndSetRespawn(235680, 423.1627f, 755.64795f, 398.42203f, (byte) 107, 90);
		spawnAndSetRespawn(235681, 369.784f, 707.38403f, 398.42203f, (byte) 15, 90);
		spawnAndSetRespawn(235681, 404.43335f, 744.22394f, 398.42203f, (byte) 47, 90);
		spawnAndSetRespawn(235681, 352.79605f, 705.9181f, 398.42203f, (byte) 12, 90);
		spawnAndSetRespawn(235681, 410.6537f, 750.42694f, 398.42203f, (byte) 113, 90);
		spawnAndSetRespawn(235681, 409.7045f, 766.54474f, 398.42203f, (byte) 79, 90);
		spawnAndSetRespawn(235681, 411.96558f, 763.2033f, 398.42203f, (byte) 74, 90);
		spawnAndSetRespawn(235650, 426.9842f, 724.7398f, 398.42203f, (byte) 6, 90);
		spawnAndSetRespawn(235681, 406.22287f, 767.8437f, 398.42203f, (byte) 85, 90);
		spawnAndSetRespawn(235680, 359.43243f, 721.8015f, 398.42203f, (byte) 24, 90);
		spawnAndSetRespawn(235651, 436.61176f, 736.3438f, 398.42203f, (byte) 63, 90);
	}

	@Override
	public void onDie(Npc npc) {
		int npcId = npc.getNpcId();
		switch (npcId) {
			case 235629: // Intruder Skirmisher
			case 235630: // Intruder Scout
				addPoints(npc, 180);
				break;
			case 235660: // Ruthless Jabaraki
				addPoints(npc, 1740);
				instance.setDoorState(431, true);
				spawnRoom2();
				break;
			case 235631: // Brainwashed Peon
				addPoints(npc, 160);
				break;
			case 235652: // Intruder Warrior
			case 235653: // Intruder Sharpeye
				addPoints(npc, 250);
				break;
			case 235641: // Shugo Turncoat
				addPoints(npc, 660);
				if (killedShugoTurncoats.incrementAndGet() >= 2) {
					instance.setDoorState(428, true);
					spawnRoom3();
				}
				break;
			case 235634: // Watchman Hokuruki
				addPoints(npc, 2040);
				lastStage = true;
				spawnVaultDoorAndHealingSpring();
				break;
			case 235649: // Intruder Sniper
				addPoints(npc, 760);
				break;
			case 236083: // Intruder Marksman
			case 235633: // Intruder Marksman
				addPoints(npc, 1070);
				break;
			case 235650: // Intruder Assassin
			case 235635: // Intruder Challenger
			case 235636: // Intruder Guard
				addPoints(npc, 700);
				break;
			case 235637: // Intruder Scrapper
			case 235638: // Intruder Combatant
				addPoints(npc, 820);
				break;
			case 235651: // Intruder Gladiator
				addPoints(npc, 1400);
				break;
			case 235681: // Intruder Lookout
			case 235680: // Intruder Brawler
				addPoints(npc, 530);
				break;
			case 235640: // Captain Mirez
				addPoints(npc, 12000);
				stage.incrementAndGet();
				break;
			case 235685: // Longknife Zodica
				addPoints(npc, 14400);
				stage.incrementAndGet();
				break;
			case 235684: // Sorcerer Budyn
				addPoints(npc, 16000);
				stage.incrementAndGet();
				break;
			case 235683: // Elite captain rupasha
				addPoints(npc, 88000);
				stage.incrementAndGet();
				break;
			case 235647: // Grand Commander Gradi
				addPoints(npc, 224000);
				sendMsg(SM_SYSTEM_MESSAGE.STR_IDSweep_Stage2_End()); // Intruders have fled
				checkRank(instanceReward.getPoints());
				break;
		}

		if (lastStage) {
			if (instanceReward != null)
				checkStage(instanceReward.getPoints());
		}
	}

	private synchronized void checkStage(int points) {
		switch (stage.get()) {
			case 0:
				if (points >= 50000 && spawns.add(235640)) {
					spawnCaptainMirez();
				}
				break;
			case 1:
				if (points >= 82500 && spawns.add(235685)) {
					spawnLongknifeZodica();
				}
				break;
			case 2:
				if (points >= 120000 && spawns.add(235684)) {
					spawnSorcererBudyn();
				}
				break;
			case 3:
				if (points >= 162000 && spawns.add(235683)) {
					spawnEliteCaptainRupasha();
				}
				break;
			case 4:
				if (points >= 275000 && spawns.add(235647)) {
					spawnCommanderGradi();
				}
				break;
		}
	}

	private synchronized void addPoints(Npc npc, int points) {
		if (instanceReward.getInstanceProgressionType().isStartProgress()) {
			instanceReward.addPoints(points);
			sendPacket(npc.getObjectTemplate().getL10n(), points);
		}
	}

	private void spawnCaptainMirez() {
		spawn(235640, 361.35477f, 757.3215f, 398.42203f, (byte) 105);
		spawn(235681, 366.179f, 761.93866f, 398.42203f, (byte) 103);
		spawn(235681, 356.9046f, 753.294f, 398.42203f, (byte) 104);
	}

	private void spawnLongknifeZodica() {
		spawn(235680, 368.96704f, 766.34576f, 398.42203f, (byte) 104);
		spawn(235680, 356.247f, 754.2303f, 398.42203f, (byte) 105);
		spawn(235685, 361.95898f, 758.1539f, 398.42203f, (byte) 105);
		spawn(235680, 365.84665f, 763.2926f, 398.42203f, (byte) 104);
		spawn(235680, 353.47806f, 751.28973f, 398.42203f, (byte) 105);
	}

	private void spawnSorcererBudyn() {
		spawn(235650, 370.77155f, 765.0706f, 398.42203f, (byte) 104);
		spawn(235684, 362.24368f, 757.7284f, 398.42203f, (byte) 106);
		spawn(235650, 356.04114f, 751.38055f, 398.42203f, (byte) 104);
		spawn(235650, 358.2026f, 753.55994f, 398.42203f, (byte) 104);
		spawn(235650, 353.95975f, 749.06775f, 398.42203f, (byte) 104);
		spawn(235650, 366.51083f, 761.46844f, 398.42203f, (byte) 104);
		spawn(235650, 368.6693f, 763.1063f, 398.42203f, (byte) 104);
	}

	private void spawnEliteCaptainRupasha() {
		spawn(235637, 371.05835f, 765.09326f, 398.42203f, (byte) 103);
		spawn(235683, 362.33813f, 757.7157f, 398.42203f, (byte) 105);
		spawn(235637, 357.44644f, 753.30096f, 398.42203f, (byte) 105);
		spawn(235637, 353.57394f, 749.0348f, 398.42203f, (byte) 104);
		spawn(235637, 355.39728f, 751.22296f, 398.42203f, (byte) 105);
		spawn(235637, 368.85718f, 763.3262f, 398.42203f, (byte) 103);
		spawn(235637, 366.7636f, 761.72046f, 398.42203f, (byte) 103);
	}

	private void spawnCommanderGradi() {
		spawn(235651, 353.5293f, 747.9316f, 398.42203f, (byte) 104);
		spawn(235651, 356.48816f, 750.8252f, 398.42203f, (byte) 105);
		spawn(235647, 363.56564f, 756.829f, 398.42203f, (byte) 104);
		spawn(235651, 369.30795f, 763.17236f, 398.42203f, (byte) 104);
		spawn(235651, 372.4574f, 765.96375f, 398.42203f, (byte) 105);
	}

	private void spawnVaultDoorAndHealingSpring() {
		spawn(832925, 469.500f, 658.475f, 397.0672f, (byte) 106, 432);
		spawn(832919, 464.428f, 640.168f, 395.475f, (byte) 59, 251);
		Npc healingTowerNpc = (Npc) spawn(832918, 464.428f, 640.168f, 394.475f, (byte) 59);
		// despawn healing function after 1 min
		healingTowerNpc.getController().addTask(TaskId.DESPAWN,
			ThreadPoolManager.getInstance().schedule(() -> healingTowerNpc.getController().delete(), 1, TimeUnit.MINUTES));
		spawn(832922, 465.9878f, 645.2775f, 395.6612f, (byte) 110);

		// spawn portal and healing tower at instance start location
		spawn(832919, 543.7689f, 305.1814f, 400.4406f, (byte) 0, 252);
		spawn(832924, 549.1239f, 313.5525f, 400.3768f, (byte) 31, 433);
		spawn(832918, 543.7689f, 305.1814f, 400.5525f, (byte) 31);
		spawnRoom4();
	}

	private synchronized void checkRank(int totalPoints) {
		int rank;
		if (totalPoints >= 471200) {
			int additionalKeys = Rnd.chance() < 10 ? Rnd.get(2, 4) : 0;
			instanceReward.setRewardItem1(185000222);
			instanceReward.setRewardItem1Count(6 + additionalKeys);
			rank = 1;
		} else if (totalPoints >= 233700) {
			instanceReward.setRewardItem1(185000222);
			instanceReward.setRewardItem1Count(3);
			rank = 2;
		} else if (totalPoints >= 86400) {
			instanceReward.setRewardItem1(185000222);
			instanceReward.setRewardItem1Count(2);
			rank = 3;
		} else if (totalPoints >= 52100) {
			instanceReward.setRewardItem1(185000222);
			instanceReward.setRewardItem1Count(1);
			rank = 4;
		} else if (totalPoints >= 180) {
			rank = 5;
		} else {
			rank = 8;
		}

		if (!instanceReward.isRewarded()) {
			despawnAll();
			instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
			instanceReward.setRank(rank);
			sendPacket(null, 0);
			reward();
			if (rank <= 5) {
				spawn(832932, 384f, 736f, 398.42203f, (byte) 105); // Shugo Emperor's Butler
				spawnChests();
			}
		}
	}

	private void reward() {
		instance.forEachPlayer(player -> ItemService.addItem(player, instanceReward.getRewardItem1(), instanceReward.getRewardItem1Count()));
	}

	private void spawnShugos() {
		spawn(832927, 544.512f, 360.4175f, 396.0757f, (byte) 49);
		spawn(832927, 551.6949f, 343.4657f, 396.75f, (byte) 49);
		spawn(832927, 535.149f, 402.6f, 393.756f, (byte) 118);
	}

	private void spawnMorphShugos() {
		spawn(833491, 543.808f, 306.197f, 400.387f, (byte) 90);
		spawn(833492, 548.7077f, 306.5808f, 400.24207f, (byte) 88);
		spawn(833493, 553.514f, 305.849f, 400.469f, (byte) 75);
	}

	private void spawnChests() {
		spawn(832931, 175.04216f, 198.45958f, 395.9671f, (byte) 36);
		spawn(832930, 161.91292f, 198.62294f, 395.0f, (byte) 23);
		spawn(832930, 180.83046f, 204.10951f, 395.0f, (byte) 28);
		spawn(832930, 169.48213f, 219.73682f, 395.0f, (byte) 25);
		spawn(832930, 178.27629f, 226.21896f, 395.09406f, (byte) 59);
		spawn(832930, 175.61734f, 214.88867f, 395.17435f, (byte) 53);
		spawn(832950, 168.29312f, 391.00455f, 395.16843f, (byte) 95);
		spawn(832930, 159.77527f, 219.72781f, 395.04214f, (byte) 3);
		spawn(833461, 164.82785f, 389.4378f, 395.5f, (byte) 97);
		spawn(832930, 163.26276f, 213.88086f, 395.0f, (byte) 12);
		spawn(832930, 156.53253f, 210.77219f, 395.36688f, (byte) 117);
		spawn(833462, 171.6808f, 391.14163f, 395.1569f, (byte) 88);
		spawn(832930, 187.77423f, 218.2426f, 395.0042f, (byte) 64);
		spawn(832930, 164.37688f, 205.95332f, 395.0f, (byte) 44);
		spawn(832930, 182.22774f, 213.80852f, 395.0f, (byte) 49);
		spawn(832930, 170.05452f, 209.80673f, 395.0f, (byte) 36);
		spawn(832929, 161.29674f, 209.23973f, 395.0f, (byte) 19);
		spawn(832929, 173.14182f, 222.378f, 395.0f, (byte) 40);
		spawn(832929, 157.76303f, 217.2553f, 395.05923f, (byte) 116);
		spawn(832929, 177.34726f, 209.02199f, 395.0f, (byte) 43);
		spawn(832929, 168.96295f, 214.35237f, 395.0f, (byte) 38);
		spawn(832929, 180.53427f, 219.9979f, 395.0f, (byte) 61);
		spawn(833461, 164.49313f, 235.94745f, 395.09418f, (byte) 95);
		spawn(833462, 171.53752f, 237.57254f, 395.2754f, (byte) 90);
		spawn(832950, 167.85106f, 237.10973f, 395.0f, (byte) 94);
		spawn(832930, 156.38768f, 517.86017f, 395.8043f, (byte) 7);
		spawn(833461, 165.01184f, 545.4925f, 395.0f, (byte) 100);
		spawn(833462, 171.41339f, 547.39813f, 395.07523f, (byte) 97);
		spawn(832930, 179.25534f, 514.87665f, 395.0f, (byte) 21);
		spawn(832930, 181.37575f, 524.6897f, 395.0f, (byte) 50);
		spawn(832931, 175.6018f, 350.78186f, 395.9671f, (byte) 35);
		spawn(832930, 171.0464f, 372.37576f, 395.0f, (byte) 25);
		spawn(832930, 165.21965f, 364.9769f, 395.0f, (byte) 12);
		spawn(832930, 160.43564f, 373.34912f, 395.0f, (byte) 115);
		spawn(832930, 166.53394f, 356.35916f, 395.0f, (byte) 52);
		spawn(832930, 162.39465f, 523.1102f, 395.0f, (byte) 18);
		spawn(832930, 159.47453f, 530.86194f, 395.26273f, (byte) 1);
		spawn(832930, 189.07771f, 369.41547f, 395.16553f, (byte) 63);
		spawn(832930, 179.67395f, 379.52f, 395.0f, (byte) 48);
		spawn(832930, 168.58124f, 520.26904f, 395.08838f, (byte) 28);
		spawn(832930, 179.81627f, 358.6796f, 395.0f, (byte) 25);
		spawn(832930, 171.88452f, 362.89175f, 395.0f, (byte) 39);
		spawn(832930, 183.29233f, 366.9576f, 395.0f, (byte) 51);
		spawn(832930, 162.50578f, 515.2418f, 395.28015f, (byte) 44);
		spawn(832930, 176.62233f, 366.62146f, 395.03885f, (byte) 58);
		spawn(832930, 162.78757f, 353.12155f, 395.0227f, (byte) 26);
		spawn(832929, 158.0911f, 528.741f, 395.16562f, (byte) 13);
		spawn(832930, 157.76079f, 360.71704f, 395.0299f, (byte) 8);
		spawn(832930, 163.3729f, 509.10294f, 395.05606f, (byte) 26);
		spawn(832929, 174.1135f, 532.88416f, 395.25058f, (byte) 55);
		spawn(832929, 158.18898f, 370.64267f, 395.0f, (byte) 11);
		spawn(832929, 181.74757f, 530.2635f, 395.0f, (byte) 73);
		spawn(832950, 168.16693f, 546.9225f, 395.0f, (byte) 95);
		spawn(832929, 170.41643f, 366.2041f, 395.0f, (byte) 31);
		spawn(832929, 168.60648f, 525.53674f, 395.0f, (byte) 42);
		spawn(832929, 174.27791f, 374.93924f, 395.24783f, (byte) 46);
		spawn(832929, 177.98854f, 520.131f, 395.0f, (byte) 48);
		spawn(832929, 162.49776f, 358.50577f, 395.0f, (byte) 26);
		spawn(832929, 182.5515f, 374.0672f, 395.0042f, (byte) 61);
		spawn(832929, 162.45122f, 519.44666f, 395.03458f, (byte) 23);
		spawn(832931, 175.34236f, 507.75705f, 395.96707f, (byte) 36);
		spawn(832929, 179.90283f, 364.51767f, 395.0f, (byte) 44);
		spawn(832930, 180.06203f, 535.93286f, 395.0f, (byte) 54);
		spawn(832930, 170.12015f, 531.15533f, 395.015f, (byte) 29);
		spawn(832930, 188.49672f, 527.9214f, 395.02612f, (byte) 61);
		spawn(832930, 174.70316f, 523.2922f, 395.0f, (byte) 57);
	}

	private void despawnAll() {
		List<Integer> ignoreNpcIds = Arrays.asList(832919, 832924, 832918, 832925);
		instance.forEachNpc(npc -> {
			if (!ignoreNpcIds.contains(npc.getNpcId()))
				npc.getController().delete();
		});
	}

	private void sendPacket(String npcL10n, int points) {
		if (npcL10n != null)
			PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(npcL10n, points));
		PacketSendUtility.broadcastToMap(instance, new SM_INSTANCE_SCORE(instance.getMapId(), new TheShugoEmperorsVaultScoreWriter(instanceReward), getTime()));
	}

	private int getTime() {
		long result = System.currentTimeMillis() - startTime;
		if (instanceReward.getInstanceProgressionType().isPreparing()) {
			return (int) (60000 - result);
		} else if (result < 480000) {
			return (int) (480000 - result);
		}
		return 0;
	}

	@Override
	public InstanceScore<?> getInstanceScore() {
		return instanceReward;
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		TeleportService.teleportTo(player, instance, 542.9366f, 299.9885f, 401f, (byte) 22);
		return true;
	}

	@Override
	public void leaveInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}
}
