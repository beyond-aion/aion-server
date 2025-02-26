package instance;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * Quick Difficulty Guide:
 * Difficulty 5: Hard Mode
 * - Players have to kill the twin bosses in under 5 minutes within a 15s gap
 * - Players have to kill Orissan in his second vulnerable phase
 * - Players have to defeat 5 waves to defend their siege weapon
 * - Players have to dispel 3 buffs of Beritra and he'll transform into a dragon
 * Difficulty 3: Normal Mode
 * - Players have failed to kill the twin bosses in under 5 minutes or failed to kill Orissan in his second vulnerable phase
 * - Players have to kill a weakened Orissan in his second vulnerable phase (if they already failed the twin bosses)
 * - Players have to defeat 3 waves to defend their siege weapon
 * - Beritra remains human and NPCs will help players removing "Everlasting Life"
 * Difficulty 1: Easy Mode
 * - Players have failed to kill the twin bosses in under 5 minutes and failed to kill the weakened Orissan in his second vulnerable phase
 * - Players have to defeat 1 wave to defend their siege weapon
 * - Beritra remains human and NPCs will immediately help players removing all buffs
 * Difficulty 0: Fail Run
 * - Players have failed to defend their siege weapon, so no Beritra for them
 *
 * @author Estrayl
 */
@InstanceID(301390000)
public class DrakenspireDepthsInstance extends GeneralInstanceHandler {

	private static final int HARD_MODE = 5;
	private static final int NORMAL_MODE = 3;
	private static final int EASY_MODE = 1;
	private static final int FAILED = 0;
	private final AtomicInteger difficulty = new AtomicInteger(HARD_MODE);
	private final AtomicBoolean isStageActive = new AtomicBoolean();
	private final AtomicInteger waveSurvivors = new AtomicInteger(8); // increases the final boss time by 15s for each survivor
	private final AtomicInteger eventCounter = new AtomicInteger();
	private final AtomicReference<Race> race = new AtomicReference<>();
	private Future<?> currentEventTask, additionalEventTask; // re-usable for all stages

	public DrakenspireDepthsInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceDestroy() {
		cancelTasks(currentEventTask, additionalEventTask);
	}

	@Override
	public void onEnterInstance(Player player) {
		if (race.compareAndSet(null, player.getRace())) {
			spawn(race.get() == Race.ELYOS ? 209678 : 209743, 353.443f, 185.629f, 1686.1825f, (byte) 60);
			spawn(race.get() == Race.ELYOS ? 209679 : 209744, 353.443f, 179.259f, 1686.1825f, (byte) 60);
			spawn(race.get() == Race.ELYOS ? 209680 : 209745, 347.099f, 192.390f, 1686.1825f, (byte) 90);
			spawn(race.get() == Race.ELYOS ? 209680 : 209745, 351.295f, 192.437f, 1686.1825f, (byte) 90);
			spawn(race.get() == Race.ELYOS ? 209681 : 209746, 347.099f, 173.774f, 1686.1825f, (byte) 30);
			spawn(race.get() == Race.ELYOS ? 209681 : 209746, 351.295f, 173.821f, 1686.1825f, (byte) 30);
		}
	}

	@Override
	public void onSpawn(VisibleObject object) {
		if (object instanceof Npc) {
			switch (((Npc) object).getNpcId()) {
				case 236230: // Immortal Orissan
				case 236233:
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_03(), 2500);
					break;
				case 236231: // Exhausted Orissan
				case 236234:
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_05(), 2500);
					scheduleOrissansNextImmortality();
					break;
				case 702719: // Empyrean Lord's Siege Weapon
				case 702720:
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_01(), 3000);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_02(), 6000);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_03(), 9000);
					break;
				case 236204: // Flamesquelch Destroyer
				case 236205: // Flamesquelch Burnsmark
				case 236206: // Flamesquelch Rangerblaze
				case 236216:
				case 236217:
				case 236218:
				case 236219:
				case 236220:
					startWalking((Npc) object, CreatureState.WALK_MODE);
					break;
				case 236243: // Commander Virtsha
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_08());
					break;
				case 236247: // Dragon Beritra
					// TODO: scheduleBeritrasDespawn();
					spawn(702699, 153.250f, 516.473f, 1751.0494f, (byte) 0, 383); // Pillar remains
					break;
				case 855460:
				case 855461:
				case 855462:
				case 855463:
				case 855464: // Drakenspire Protector
				case 855465:
				case 855466:
				case 855467:
				case 855468:
				case 855469:
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_GUARDIAN_01());
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_GUARDIAN_02(), 3000);
					break;
			}
		}
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		int npcId = npc.getNpcId();
		switch (npcId) {
			case 236225:
			case 236226:
				Npc oppositeProtector = getNpc(npcId == 236225 ? 236226 : 236225);
				if (oppositeProtector == null || oppositeProtector.isDead())
					onTwinsComplete();
				break;
			case 236227: // Lava Protector
			case 236228: // Heatvent Protector
				if (getNpc(npcId == 236227 ? 855709 : 855708) != null) {
					onTwinsComplete();
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_07());
				} else {
					spawn(npcId == 236227 ? 855708 : 855709, npc.getX(), npc.getY(), npc.getZ(), (byte) 0); // Sphere
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_RESSURECT_02());
					setAdditionalEventTask(ThreadPoolManager.getInstance().schedule(this::onTwinRespawn, 15000));
				}
				break;
			case 236223: // Fetid Phantomscorch Chimera
			case 833012:
			case 833013:
			case 833015:
				return;
			case 236224: // Rapacious Kadena
				instance.setDoorState(375, true);
				break;
			case 236661: // Rapacious Kadena
				instance.setDoorState(376, true);
				break;
			case 236662: // Rapacious Kadena
				instance.setDoorState(378, true);
				break;
			case 236231: // Exhausted Orissan
			case 236234:
				onOrissanComplete();
				deleteAliveNpcs(855607, 855608, 855699, 855700);
				break;
			case 236239: // Wave Commanders
			case 236240:
			case 236241:
			case 236242:
			case 236243:
				if (eventCounter.incrementAndGet() >= difficulty.get()) // difficulty is equivalent to the necessary kills
					onWaveEventComplete(true);
				break;
			case 236244: // Beritra Easy Mode
			case 236245: // Beritra Normal Mode
				WorldPosition pos = npc.getPosition();
				spawn(npcId == 236244 ? 833012 : 833013, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
				spawnExit();
				break;
			case 236246: // Beritra Hard Mode
			case 236247: // Beritra Dragon
				// TODO: spawn exit and NPCs
				spawnExit();
				break;
			case 236248:
			case 236249:
				if (waveSurvivors.decrementAndGet() == 0)
					onWaveEventComplete(false);
				break;
			// exploding flame 856459 skill use
			case 731580:
				RespawnService.scheduleDecayTask(npc, 4000);
				switch (npc.getSpawn().getStaticId()) {
					case 332 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_09());
					case 522, 603 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_08());
				}
		}
	}

	@Override
	public void onAggro(Npc npc) {
		switch (npc.getNpcId()) {
			case 236227: // Lava Protector
			case 236228: // Heatvent Protector
				if (isStageActive.compareAndSet(false, true)) {
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_01());
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_02(), 2500);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_03(), 5000);
					onTwinsFightStart();
				}
				break;
		}
	}

	private void onTwinsFightStart() {
		setCurrentEventTask(ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			private int twinProgressCount;

			@Override
			public void run() {
				switch (++twinProgressCount) {
					case 2 -> {
						instance.setDoorState(380, true); // one of the best doors which closes if you set it open
						instance.setDoorState(374, true);
						instance.setDoorState(377, true);
						instance.setDoorState(379, true);
						instance.setDoorState(391, true);
					}
					case 8 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_04());
					case 9 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_05(), 5000);
					case 10 -> {
						cancelTasks(currentEventTask, additionalEventTask);
						onTwinsFail();
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_06());
					}
				}
			}

		}, 30000, 10 * 1000));
	}

	private void onTwinRespawn() {
		if (getNpc(855708) != null)
			spawn(236227, 531.0885f, 212.43806f, 1683.4116f, (byte) 60);
		if (getNpc(855709) != null)
			spawn(236228, 530.8584f, 151.8681f, 1683.4116f, (byte) 60);
		deleteAliveNpcs(855708, 855709);
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_RESSURECT_03());
	}

	private void onTwinsComplete() {
		cancelTasks(currentEventTask, additionalEventTask);
		deleteAliveNpcs(855708, 855709);

		ThreadPoolManager.getInstance().schedule(() -> {
			sp(race.get() == Race.ELYOS ? 209690 : 209755, 543.35f, 149.81f, 1681.82f, (byte) 0, "301390000_NPCPathFunction_Npc_Path03", 4000);
			sp(race.get() == Race.ELYOS ? 209690 : 209755, 543.45f, 208.97f, 1681.82f, (byte) 0, "301390000_NPCPathFunction_Npc_Path01", 4000);
			sp(race.get() == Race.ELYOS ? 209693 : 209758, 543.43f, 155.08f, 1681.82f, (byte) 0, "301390000_NPCPathFunction_Npc_Path04", 4000);
			sp(race.get() == Race.ELYOS ? 209693 : 209758, 543.43f, 214.24f, 1681.82f, (byte) 0, "301390000_NPCPathFunction_Npc_Path02", 4000);
			ThreadPoolManager.getInstance().schedule(() -> {
				spawn(race.get() == Race.ELYOS ? 209695 : 209760, 582.61f, 183.52f, 1683.73f, (byte) 0);
				spawn(race.get() == Race.ELYOS ? 209694 : 209759, 582.55f, 178.02f, 1683.73f, (byte) 0);
			}, 16000);
		}, 2000);

		if (difficulty.get() == HARD_MODE)
			spawn(236232, 812.0238f, 568.3602f, 1701.045f, (byte) 92); // HM Orissan
		else
			spawn(236229, 812.0238f, 568.3602f, 1701.045f, (byte) 92); // NM Orissan

		isStageActive.set(false);
	}

	private void onTwinsFail() {
		if (difficulty.compareAndSet(HARD_MODE, NORMAL_MODE)) {
			deleteAliveNpcs(236227, 236228, 855708, 855709);
			spawn(236225, 531.0885f, 212.4381f, 1683.412f, (byte) 60); // Fountless Lava Protector
			spawn(236226, 530.8584f, 151.8681f, 1683.412f, (byte) 60); // Fountless Heatvent Protector
		}
	}

	/**
	 * Immortality phases are always 90s + 10s buff duration, vice versa; his vulnerability phase differs.
	 * So let's handle only this phase.
	 * 1st = 120s; 2nd = 228s, 3rd = 228s, 4th endless
	 */
	private void scheduleOrissansNextImmortality() {
		int delay;
		switch (eventCounter.incrementAndGet()) {
			case 1 -> delay = 120000;
			case 2 -> {
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_06(), 5000);
				delay = 228000;
			}
			case 3 -> {
				int prevDiff = difficulty.get();
				switch (difficulty.getAndSet(prevDiff - 2)) {
					case NORMAL_MODE -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_07(), 5000);
					case EASY_MODE -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_08(), 5000);
				}
				delay = 240000; // Let's give them a few seconds more
			}
			default -> {
				// Maybe retail has lowered the difficulty to easy mode at this point (if normal mode was present)
				switch (difficulty.get()) {
					case NORMAL_MODE -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_09(), 5000);
					case EASY_MODE -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_10(), 5000);
				}
				return;
			}
		}

		setCurrentEventTask(ThreadPoolManager.getInstance().schedule(() -> {
			Npc orissan = getNpc(difficulty.get() == HARD_MODE ? 236234 : 236231);
			if (orissan != null) // should not happen
				SkillEngine.getInstance().getSkill(orissan, 21635, 1, orissan).useSkill(); // Summon Crystal
		}, delay));
	}

	private void onOrissanComplete() {
		cancelTasks(currentEventTask);
		ThreadPoolManager.getInstance().schedule(() -> {
			spawn(race.get() == Race.ELYOS ? 209705 : 209770, 816.18f, 517.78f, 1707.41f, (byte) 33);
			spawn(race.get() == Race.ELYOS ? 209705 : 209770, 820.82f, 517.82f, 1707.41f, (byte) 33);
			spawn(race.get() == Race.ELYOS ? 209704 : 209769, 816.00f, 521.35f, 1706.88f, (byte) 33);
			spawn(race.get() == Race.ELYOS ? 209704 : 209769, 820.48f, 521.49f, 1706.88f, (byte) 33);

			spawn(race.get() == Race.ELYOS ? 209711 : 209776, 811.00f, 587.83f, 1701.045f, (byte) 32);
			spawn(race.get() == Race.ELYOS ? 209709 : 209774, 807.50f, 583.54f, 1701.045f, (byte) 32);
			spawn(race.get() == Race.ELYOS ? 209710 : 209775, 814.85f, 583.56f, 1701.045f, (byte) 32);
			spawn(race.get() == Race.ELYOS ? 209708 : 209773, 811.11f, 582.62f, 1701.045f, (byte) 32);
		}, 4000);
		eventCounter.set(0);
		spawn(206405, 639.50f, 866.50f, 1600.88f, (byte) 0);
	}

	private void onWaveEventStart() {
		sp(race.get() == Race.ELYOS ? 209720 : 209785, 635.53f, 886.83f, 1600.72f, (byte) 30, "301390000_NPCPathFunction_Npc_Path06",
			CreatureState.WALK_MODE, 7000);
		spawn(race.get() == Race.ELYOS ? 209731 : 209796, 639.05f, 895.86f, 1600.41f, (byte) 30);
		ThreadPoolManager.getInstance().schedule(() -> {
			sp(race.get() == Race.ELYOS ? 209722 : 209787, 637.75f, 847.98f, 1599.94f, (byte) 30, "301390000_NPCPathFunction_Npc_Path07", 2000);
			sp(race.get() == Race.ELYOS ? 209722 : 209787, 637.75f, 847.98f, 1599.94f, (byte) 30, "301390000_NPCPathFunction_Npc_Path08", 2000);
			sp(race.get() == Race.ELYOS ? 209722 : 209787, 637.75f, 847.98f, 1599.94f, (byte) 30, "301390000_NPCPathFunction_Npc_Path09", 2000);
			sp(race.get() == Race.ELYOS ? 209722 : 209787, 637.75f, 847.98f, 1599.94f, (byte) 30, "301390000_NPCPathFunction_Npc_Path10", 2000);
			sp(race.get() == Race.ELYOS ? 209722 : 209787, 633.95f, 847.58f, 1599.87f, (byte) 30, "301390000_NPCPathFunction_Npc_Path13", 2000);
			sp(race.get() == Race.ELYOS ? 209722 : 209787, 633.95f, 847.58f, 1599.87f, (byte) 30, "301390000_NPCPathFunction_Npc_Path14", 2000);
			sp(race.get() == Race.ELYOS ? 209722 : 209787, 633.95f, 847.58f, 1599.87f, (byte) 30, "301390000_NPCPathFunction_Npc_Path15", 2000);
			sp(race.get() == Race.ELYOS ? 209722 : 209787, 633.95f, 847.58f, 1599.87f, (byte) 30, "301390000_NPCPathFunction_Npc_Path16", 2000);
			ThreadPoolManager.getInstance().schedule(() -> {
				deleteAliveNpcs(race.get() == Race.ELYOS ? 209720 : 209785);
				spawn(race.get() == Race.ELYOS ? 209723 : 209788, 636.09f, 879.98f, 1600.82f, (byte) 33); // Main Npc
				spawn(race.get() == Race.ELYOS ? 702719 : 702720, 635.68f, 883.03f, 1603.91f, (byte) 29); // Siege Weapon
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_01(), 3000);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_02(), 6000);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_03(), 9000);
				scheduleWaveStarts();
			}, 12000);
		}, 18000);
	}

	private void scheduleWaveStarts() {
		setCurrentEventTask(ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			private int waveCount;

			@Override
			public void run() {
				switch (++waveCount) {
					case 1 -> {
						instance.setDoorState(267, true);
						instance.setDoorState(271, true);
						spawn(731581, 578.55f, 819.27f, 1609.42f, (byte) 0, 84);
						spawn(731581, 690.49f, 822.41f, 1609.57f, (byte) 0, 405);
						spawn(731581, 635.39f, 784.05f, 1596.72f, (byte) 0, 548);
					}
					case 2 -> {
						if (difficulty.get() == HARD_MODE)
							sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_05());
					}
					case 3 -> {
						instance.setDoorState(7, true);
						instance.setDoorState(310, true);
						spawn(731581, 707.34f, 876.80f, 1603.69f, (byte) 0, 398);
						spawn(731581, 570.70f, 877.51f, 1599.80f, (byte) 0, 401);
						if (difficulty.get() == HARD_MODE)
							sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_06());
					}
					case 4 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_07());
					case 5 -> {
						instance.setDoorState(210, true);
						instance.setDoorState(312, true);
						spawn(731581, 694.01f, 935.97f, 1618.09f, (byte) 0, 399);
						spawn(731581, 572.94f, 940.04f, 1620.04f, (byte) 0, 407);
					}
				}
				if (waveCount <= difficulty.get())
					scheduleCommanderWave(waveCount);
			}

		}, 6000, 60000));
	}

	private void scheduleCommanderWave(int waveCount) {
		setAdditionalEventTask(ThreadPoolManager.getInstance().schedule(() -> {
			sp(Rnd.get(236216, 236220), 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Left", 0);
			sp(Rnd.get(236216, 236220), 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Right", 0);
			if (waveCount >= 3)
				sp(Rnd.get(236216, 236220), 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Middle", 0);
			if (waveCount >= 4) {
				sp(Rnd.get(236216, 236220), 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Left", 4000);
				sp(Rnd.get(236216, 236220), 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Right", 4000);
			}
			sp(236243 - difficulty.get() + waveCount, 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Middle", 3000); // Boss
		}, 28000));
	}

	private void onWaveEventComplete(boolean isSuccessful) {
		cancelTasks(currentEventTask, additionalEventTask);
		deleteAliveNpcs(236204, 236205, 236206, 236216, 236217, 236218, 236219, 236220, 236239, 236240, 236241, 236242, 236243, 731581);
		if (isSuccessful) {
			SkillEngine.getInstance().getSkill(getNpc(race.get() == Race.ELYOS ? 702719 : 702720), 20838, 1, getNpc(731580)).useSkill();
			switch (waveSurvivors.get()) {
				case 8 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_BONUS_04());
				case 7, 6 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_BONUS_03());
				case 5, 4, 3 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_BONUS_02());
				case 2, 1 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_BONUS_01());
			}
			switch (difficulty.get()) {
				// case 5 -> spawn(236246, 152f, 518f, 1749.6f, (byte) 55); TODO: Implementation
				case HARD_MODE, NORMAL_MODE -> spawn(236245, 152.38f, 518.68f, 1749.6f, (byte) 55);
				case EASY_MODE -> spawn(236244, 152.38f, 518.68f, 1749.6f, (byte) 55);
			}

		} else {
			deleteAliveNpcs(702719, 702720);
			// TODO: find sys msg for fail
		}

	}

	private void spawnExit() {
		Race r = race.get();
		switch (difficulty.get()) {
			case FAILED -> {
				spawn(r == Race.ELYOS ? 209741 : 209806, 155.390f, 516.622f, 1750f, (byte) 0); // Masionel | Parsia
				spawn(r == Race.ELYOS ? 209742 : 209807, 155.361f, 520.999f, 1750f, (byte) 0); // Detachment Entry Soldier
			}
			case EASY_MODE -> {
				spawn(r == Race.ELYOS ? 209740 : 209805, 155.390f, 516.622f, 1750f, (byte) 0); // Masionel | Parsia
				spawn(r == Race.ELYOS ? 209742 : 209807, 155.361f, 520.999f, 1750f, (byte) 0); // Detachment Entry Soldier
			}
			case NORMAL_MODE -> {
				spawn(r == Race.ELYOS ? 209739 : 209804, 155.390f, 516.622f, 1750f, (byte) 0); // Masionel | Parsia
				spawn(r == Race.ELYOS ? 209742 : 209807, 155.361f, 520.999f, 1750f, (byte) 0); // Detachment Entry Soldier
			}
			case HARD_MODE -> {
				spawn(r == Race.ELYOS ? 209738 : 209803, 140.656f, 518.922f, 1750f, (byte) 0); // Masionel | Parsia
				spawn(r == Race.ELYOS ? 209742 : 209807, 144.452f, 516.218f, 1749.4784f, (byte) 0); // Detachment Entry Soldier
				spawn(r == Race.ELYOS ? 209742 : 209807, 144.433f, 521.472f, 1749.4768f, (byte) 0); // Detachment Entry Soldier
			}
		}
		// Exits
		spawn(731548, 320.005f, 183.281f, 1687.2552f, (byte) 60); // Start Exit
		spawn(731548, 545.194f, 153.283f, 1681.8224f, (byte) 60); // Protector Exit
		spawn(731548, 545.325f, 212.691f, 1681.8224f, (byte) 60); // Protector Exit
		spawn(731548, 810.652f, 591.986f, 1701.0449f, (byte) 90); // Orissan Exit
		spawn(731548, 137.524f, 518.576f, 1749.4153f, (byte) 0); // Beritra Exit
	}

	@Override
	public void onCreatureDetected(Npc detector, Creature detected) {
		if (!(detected instanceof Player))
			return;
		if (detector.getNpcId() == 206405) {
			if (isStageActive.compareAndSet(false, true)) {
				onWaveEventStart();
				detector.getController().delete();
			}
		}
	}

	@Override
	public void onStartEffect(Effect effect) {
		switch (effect.getSkillId()) {
			case 21635 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_01()); // Summon Crystal
			case 21885 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_04()); // Weaken Ascension Domination
			case 21610 -> { // Dark Affinity
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_VRITRA_HUMAN_01());
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_VRITRA_HUMAN_02(), 3000);
			}
		}
	}

	@Override
	public void onEndEffect(Effect effect) {
		switch (effect.getSkillId()) {
			case 21635, 21885 -> { // Weaken Ascension Domination
				int nextId;
				if (effect.getSkillId() == 21635)
					nextId = difficulty.get() == HARD_MODE ? 236233 : 236230;
				else
					nextId = difficulty.get() == HARD_MODE ? 236234 : 236231;
				WorldPosition pos = effect.getEffected().getPosition();
				effect.getEffected().getController().delete();
				spawn(nextId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
			}
			case 21618 -> { // Dragon Lord's Authority
				// TODO: sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_VRITRA_HUMAN_03()); => only if it ends naturally
			}
		}
	}

	@Override
	public void leaveInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME());
		TeleportService.teleportTo(player, instance, 361.706f, 182.503f, 1684.290f, (byte) 0);
		return true;
	}

	private synchronized void setCurrentEventTask(Future<?> task) {
		cancelTasks(currentEventTask);
		currentEventTask = task;
	}

	private synchronized void setAdditionalEventTask(Future<?> task) {
		cancelTasks(additionalEventTask);
		additionalEventTask = task;
	}

	private synchronized void cancelTasks(Future<?>... tasks) {
		for (Future<?> task : tasks)
			if (task != null && !task.isDone())
				task.cancel(true);
	}

	private void sp(int id, float x, float y, float z, byte h, String walkerId, int delay) {
		sp(id, x, y, z, h, walkerId, CreatureState.ACTIVE, delay);
	}

	private void sp(int id, float x, float y, float z, byte h, String walkerId, CreatureState state, int delay) {
		ThreadPoolManager.getInstance().schedule(() -> {
			Npc npc = (Npc) spawn(id, x, y, z, h);
			npc.getSpawn().setWalkerId(walkerId);
			startWalking(npc, state);
		}, delay);
	}

	private void startWalking(Npc npc, CreatureState state) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (npc == null)
				return;
			WalkManager.startWalking((NpcAI) npc.getAi());
			if (state == CreatureState.ACTIVE) {
				npc.unsetState(CreatureState.WALK_MODE);
				npc.setState(state);
				PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.CHANGE_SPEED));
			}
		}, 2000);
	}

	@Override
	public boolean isBoss(Npc npc) {
		return switch (npc.getNpcId()) {
			case 236227, 236228, 236231, 236234, 236244, 236245, 236247 -> true;
			default -> false;
		};
	}
}
