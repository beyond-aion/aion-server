package instance;

import java.util.Objects;
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
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
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
 * - Players still have to dispel 3 buffs but Beritra remains human
 * Difficulty 1: Easy Mode
 * - Players have failed to kill the twin bosses in under 5 minutes and failed to kill the weakened Orissan in his second vulnerable phase
 * - Players have to defeat 1 wave to defend their siege weapon
 * - Beritra did not have any buffs and remains human
 * Difficulty 0: Fail Run
 * - Players have failed to defend their siege weapon, so no Beritra for them
 *
 * @author Estrayl
 */
@InstanceID(301390000)
public class DrakenspireDepthsInstance extends GeneralInstanceHandler {

	private final AtomicInteger difficulty = new AtomicInteger(5); // 5 = HM, 3 = NM, 1 = EM
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
					break;
			}
		}
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		int npcId = npc.getNpcId();
		switch (npcId) {
			case 236227: // Lava Protector
			case 236228: // Heatvent Protector
				if (getNpc(npcId == 236227 ? 855709 : 855708) != null) {
					onTwinsComplete();
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
				deleteNpcsByIds(855607, 855608, 855699, 855700);
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
				spawn(833012, 152f, 518f, 1749.6f, (byte) 55);
				break;
			case 236245: // Beritra Normal Mode
				spawn(833013, 152f, 518f, 1749.6f, (byte) 55);
				break;
			case 236246: // Beritra Hard Mode
			case 236247: // Beritra Dragon
				// TODO: spawn exit and NPCs
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
				return;
		}
		npc.getController().delete();
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
					}
				}
			}

		}, 30000, 30 * 1000));
	}

	private void onTwinRespawn() {
		if (getNpc(855708) != null)
			spawn(236227, 531.0885f, 212.43806f, 1683.4116f, (byte) 60);
		if (getNpc(855709) != null)
			spawn(236228, 530.8584f, 151.8681f, 1683.4116f, (byte) 60);
		deleteNpcsByIds(855708, 855709);
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_RESSURECT_03());
	}

	private void onTwinsComplete() {
		cancelTasks(currentEventTask, additionalEventTask);
		sendMsg(difficulty.get() == 5 ? SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_07() : SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_06());
		deleteNpcsByIds(855708, 855709);

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

		if (difficulty.get() == 5)
			spawn(236232, 812.0238f, 568.3602f, 1701.045f, (byte) 92); // HM Orissan
		else
			spawn(236229, 812.0238f, 568.3602f, 1701.045f, (byte) 92); // NM Orissan

		isStageActive.set(false);
	}

	private void onTwinsFail() {
		if (difficulty.compareAndSet(5, 3)) {
			Npc protector = instance.getNpc(236227); // Lava Protector
			if (protector != null)
				protector.getController().delete();
			protector = instance.getNpc(236228); // Heatvent Protector
			if (protector != null)
				protector.getController().delete();

			spawn(236225, 531.0885f, 212.4381f, 1683.412f, (byte) 60); // Fountless Lava Protector
			spawn(236226, 530.8584f, 151.8681f, 1683.412f, (byte) 60); // Fountless Heatvent Protector
		}
	}

	/**
	 * Immortality phases are always 90s + 10s buff duration, vice versa his vulnerability phase differs.
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
					case 3 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_07(), 5000);
					case 1 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_08(), 5000);
				}
				delay = 240000; // Let's give them a few seconds more
			}
			default -> {
				// Maybe retail has lowered the difficulty to easy mode at this point (if normal mode was present)
				switch (difficulty.get()) {
					case 3 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_09(), 5000);
					case 1 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_10(), 5000);
				}
				return;
			}
		}

		setCurrentEventTask(ThreadPoolManager.getInstance().schedule(() -> {
			Npc orissan = getNpc(difficulty.get() == 5 ? 236234 : 236231);
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
				deleteNpcsByIds(race.get() == Race.ELYOS ? 209720 : 209785);
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
						if (difficulty.get() == 5)
							sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_05());
					}
					case 3 -> {
						instance.setDoorState(7, true);
						instance.setDoorState(310, true);
						spawn(731581, 707.34f, 876.80f, 1603.69f, (byte) 0, 398);
						spawn(731581, 570.70f, 877.51f, 1599.80f, (byte) 0, 401);
						if (difficulty.get() == 5)
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
		deleteNpcsByIds(236204, 236205, 236206, 236216, 236217, 236218, 236219, 236220, 236239, 236240, 236241, 236242, 236243, 731581);
		if (isSuccessful) {
			SkillEngine.getInstance().getSkill(getNpc(race.get() == Race.ELYOS ? 702719 : 702720), 20838, 1, getNpc(731580)).useSkill();
			switch (waveSurvivors.get()) {
				case 8 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_BONUS_04());
				case 7, 6 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_BONUS_03());
				case 5, 4, 3 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_BONUS_02());
				case 2, 1 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_BONUS_01());
			}
			spawn(236244, 152f, 518f, 1749.6f, (byte) 55); // Beritra Easy Mode
			/*
			 * TODO: use this after implementing the complete beritra stuff
			 * switch (difficulty.get()) {
			 * case 5 -> spawn(236246, 152f, 518f, 1749.6f, (byte) 55);
			 * case 3 -> spawn(236245, 152f, 518f, 1749.6f, (byte) 55);
			 * case 1 -> spawn(236244, 152f, 518f, 1749.6f, (byte) 55);
			 * }
			 */
		} else {
			deleteNpcsByIds(702719, 702720);
			// TODO: find sys msg for fail
		}

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
		}
	}

	@Override
	public void onEndEffect(Effect effect) {
		switch (effect.getSkillId()) {
			case 21635, 21885 -> { // Weaken Ascension Domination
				int nextId;
				if (effect.getSkillId() == 21635)
					nextId = difficulty.get() == 5 ? 236233 : 236230;
				else
					nextId = difficulty.get() == 5 ? 236234 : 236231;
				WorldPosition pos = effect.getEffected().getPosition();
				effect.getEffected().getController().delete();
				spawn(nextId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
			}
		}
	}

	@Override
	public void onExitInstance(Player player) {
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

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.sendPacket(player, new SM_DIE(false, false, 0, 8));
		return true;
	}

	private void deleteNpcsByIds(int... ids) {
		instance.getNpcs(ids).stream().filter(Objects::nonNull).forEach(n -> n.getController().delete());
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
}
