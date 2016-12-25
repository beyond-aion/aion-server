package instance;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import javolution.util.FastTable;

/**
 * @author Estrayl
 *         TODO: Orissans switch count triggers different types of npcs standing in front of the door (f.e. weapon holding | hurt)
 *         TODO: Missing support spawns in orissan fight
 *         TODO: Correct positions and npcIds for twin fight supporting npcs
 *         TODO: Loot
 *         833015 - boss loot
 *         855444 - 855446 | Trueboss adds
 *         855622 - heatvent adds
 *         855625 - heatvent tornado
 *         855621 - lava adds
 *         855708 - lava font
 *         855709 - heatvent font
 *         855700 - icing crystal | servants
 *         855699 - frigid crystal | crystal bombs
 *         21641 - twin skills
 *         21811
 *         21642 - silence curse
 *         21639 - orissan line attack
 *         855468 - seal guardians
 *         855463
 *         855461
 *         855460
 *         855458 - adds?
 *         855456
 *         855454
 *         855452
 *         855447 - 855450 - Skill spawns
 *         855435 - 855436
 *         855612
 */
@InstanceID(301390000)
public class DrakenspireDepths extends GeneralInstanceHandler {

	private AtomicBoolean isRaceSet = new AtomicBoolean();
	private AtomicBoolean isTwinFightStarted = new AtomicBoolean();
	private AtomicBoolean isOrissanFightStarted = new AtomicBoolean();
	private AtomicBoolean isOrissanFailed = new AtomicBoolean();
	private AtomicBoolean isWaveEventStarted = new AtomicBoolean();
	private AtomicBoolean isBeritraFightStarted = new AtomicBoolean();
	private AtomicBoolean isBeritraTransformed = new AtomicBoolean();
	private AtomicInteger killedCommanderCount = new AtomicInteger();
	private AtomicInteger fallenWaveDefender = new AtomicInteger();
	private AtomicInteger dispelledBuffs = new AtomicInteger();
	private Map<Integer, StaticDoor> doors;
	private List<Future<?>> waveAssaultTasks = new FastTable<>();
	private Future<?> twinFailTask;
	private Future<?> orissanSwitchTask;
	// private Future<?> beritraDespawnTask;
	private Race race = Race.ASMODIANS;
	private boolean isHardmode = true;
	private byte twinProgressCount = 0;
	private byte orissanImmortalityCount = 0;
	private byte waveCount = 0;
	private byte beritraProgressCount = 0;

	@Override
	public void onInstanceCreate(WorldMapInstance wmi) {
		super.onInstanceCreate(wmi);
		doors = wmi.getDoors();
	}

	private void onTwinFightStart() {
		twinFailTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			twinProgressCount++;
			switch (twinProgressCount) {
				case 8:
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_04());
					break;
				case 9:
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_05(), 5000);
					break;
				case 10:
					cancelTask(twinFailTask);
					onTwinFail();
					break;
			}
		}, 30000, 30 * 1000);
	}

	public void onWaveEventStart() {
		sp(race == Race.ELYOS ? 209720 : 209785, 635.53f, 886.83f, 1600.72f, (byte) 30, "301390000_NPCPathFunction_Npc_Path06", CreatureState.WALK_MODE,
			7000);
		spawn(race == Race.ELYOS ? 209731 : 209796, 639.05f, 895.86f, 1600.41f, (byte) 30);
		ThreadPoolManager.getInstance().schedule(() -> {
			sp(race == Race.ELYOS ? 209722 : 209787, 637.75f, 847.98f, 1599.94f, (byte) 30, "301390000_NPCPathFunction_Npc_Path07", 2000);
			sp(race == Race.ELYOS ? 209722 : 209787, 637.75f, 847.98f, 1599.94f, (byte) 30, "301390000_NPCPathFunction_Npc_Path08", 2000);
			sp(race == Race.ELYOS ? 209722 : 209787, 637.75f, 847.98f, 1599.94f, (byte) 30, "301390000_NPCPathFunction_Npc_Path09", 2000);
			sp(race == Race.ELYOS ? 209722 : 209787, 637.75f, 847.98f, 1599.94f, (byte) 30, "301390000_NPCPathFunction_Npc_Path10", 2000);
			sp(race == Race.ELYOS ? 209722 : 209787, 633.95f, 847.58f, 1599.87f, (byte) 30, "301390000_NPCPathFunction_Npc_Path13", 2000);
			sp(race == Race.ELYOS ? 209722 : 209787, 633.95f, 847.58f, 1599.87f, (byte) 30, "301390000_NPCPathFunction_Npc_Path14", 2000);
			sp(race == Race.ELYOS ? 209722 : 209787, 633.95f, 847.58f, 1599.87f, (byte) 30, "301390000_NPCPathFunction_Npc_Path15", 2000);
			sp(race == Race.ELYOS ? 209722 : 209787, 633.95f, 847.58f, 1599.87f, (byte) 30, "301390000_NPCPathFunction_Npc_Path16", 2000);
			ThreadPoolManager.getInstance().schedule(() -> {
				deleteNpcById(race == Race.ELYOS ? 209720 : 209785);
				spawn(race == Race.ELYOS ? 209723 : 209788, 636.09f, 879.98f, 1600.82f, (byte) 33); // Main Npc
				spawn(race == Race.ELYOS ? 702719 : 702720, 635.68f, 883.03f, 1603.91f, (byte) 29); // Siege Weapon
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_01(), 3000);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_02(), 6000);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_03(), 9000);
				ThreadPoolManager.getInstance().schedule(() -> handleWaveAttacks(), 5000);
			}, 12000);
		}, 18000);
	}

	private void scheduleOrissansImmortalPhase(VisibleObject npc, int delay) {
		if (orissanImmortalityCount >= 5) { // interrupts respawn cycle
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_10(), 5000);
			return;
		}
		orissanSwitchTask = ThreadPoolManager.getInstance().schedule(() -> {
			orissanImmortalityCount++;
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_01());
			SkillEngine.getInstance().getSkill((Creature) npc, 21635, 1, npc).useSkill();
			orissanSwitchTask = ThreadPoolManager.getInstance().schedule(() -> {
				if (orissanImmortalityCount >= 3 && isOrissanFailed.compareAndSet(false, true))
					onOrissanFail();

				scheduleOrissansExhaustedPhase(spawn(236233, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading()));
				npc.getController().delete();
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_02());
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_03(), 2000);
			}, 12000);
		}, delay);
	}

	private void scheduleOrissansExhaustedPhase(VisibleObject npc) {
		SkillEngine.getInstance().getSkill((Creature) npc, 21634, 1, npc).useSkill();
		orissanSwitchTask = ThreadPoolManager.getInstance().schedule(() -> {
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_04());
			orissanSwitchTask = ThreadPoolManager.getInstance().schedule(() -> {
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_05());
				switch (orissanImmortalityCount) {
					case 2:
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_06(), 2000);
						break;
					case 3:
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_07(), 2000);
						break;
					case 4:
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_08(), 2000);
						break;
					case 5:
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_IMMORTAL_09(), 2000);
						break;
				}
				int newDelay = orissanImmortalityCount == 1 ? 110000 : 218000;
				scheduleOrissansImmortalPhase(spawn(236234, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading()), newDelay);
				npc.getController().delete();
			}, 10000);
		}, 70000);
	}

	private void handleWaveAttacks() {
		if (!isHardmode && waveCount >= 3 || waveCount >= 5)
			return;
		waveCount++;
		switch (waveCount) {
			case 1:
				doors.get(267).setOpen(true);
				doors.get(271).setOpen(true);
				spawn(731581, 578.55f, 819.27f, 1609.42f, (byte) 0).getSpawn().setStaticId(84);
				spawn(731581, 690.49f, 822.41f, 1609.57f, (byte) 0).getSpawn().setStaticId(405);
				spawn(731581, 635.39f, 784.05f, 1596.72f, (byte) 0).getSpawn().setStaticId(548);
				scheduleWaveSpawns(236204);
				break;
			case 2:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_05());
				break;
			case 3:
				doors.get(7).setOpen(true);
				doors.get(310).setOpen(true);
				spawn(731581, 707.34f, 876.80f, 1603.69f, (byte) 0).getSpawn().setStaticId(398);
				spawn(731581, 570.70f, 877.51f, 1599.80f, (byte) 0).getSpawn().setStaticId(401);
				if (isHardmode)
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_06());
				else
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_08());
				scheduleWaveSpawns(236205);
				break;
			case 4:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_07());
				break;
			case 5:
				doors.get(210).setOpen(true);
				doors.get(312).setOpen(true);
				spawn(731581, 694.01f, 935.97f, 1618.09f, (byte) 0).getSpawn().setStaticId(399);
				spawn(731581, 572.94f, 940.04f, 1620.04f, (byte) 0).getSpawn().setStaticId(407);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_08());
				scheduleWaveSpawns(236206);
				break;
		}
		scheduleCommanderWave();
		ThreadPoolManager.getInstance().schedule(() -> handleWaveAttacks(), 60000);
	}

	private void scheduleWaveSpawns(int npcId) {
		waveAssaultTasks.add(ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			switch (npcId) {
				case 236204:
					sp(npcId, 581.92f, 823.65f, 1609.64f, (byte) 15, "301390000_Wave_Top_Left_01", CreatureState.WALK_MODE, 2000);
					sp(npcId, 581.92f, 823.65f, 1609.64f, (byte) 15, "301390000_Wave_Top_Left_02", CreatureState.WALK_MODE, 2000);
					sp(npcId, 687.29f, 825.78f, 1609.66f, (byte) 45, "301390000_Wave_Bottom_Left_01", CreatureState.WALK_MODE, 2000);
					sp(npcId, 687.29f, 825.78f, 1609.66f, (byte) 45, "301390000_Wave_Bottom_Left_02", CreatureState.WALK_MODE, 2000);
					break;
				case 236205:
					sp(npcId, 575.15f, 877.52f, 1600.89f, (byte) 0, "301390000_Wave_Top_Central_01", CreatureState.WALK_MODE, 2000);
					sp(npcId, 575.15f, 877.52f, 1600.89f, (byte) 0, "301390000_Wave_Top_Central_02", CreatureState.WALK_MODE, 2000);
					sp(npcId, 704.21f, 877.60f, 1604.55f, (byte) 60, "301390000_Wave_Bottom_Central_01", CreatureState.WALK_MODE, 2000);
					sp(npcId, 704.21f, 877.60f, 1604.55f, (byte) 60, "301390000_Wave_Bottom_Central_02", CreatureState.WALK_MODE, 2000);
					break;
				case 236206:
					sp(npcId, 576.92f, 936.11f, 1620.33f, (byte) 104, "301390000_Wave_Top_Right_01", CreatureState.WALK_MODE, 2000);
					sp(npcId, 576.92f, 936.11f, 1620.33f, (byte) 104, "301390000_Wave_Top_Right_02", CreatureState.WALK_MODE, 2000);
					sp(npcId, 690.59f, 932.85f, 1618.27f, (byte) 75, "301390000_Wave_Bottom_Right_01", CreatureState.WALK_MODE, 2000);
					sp(npcId, 690.59f, 932.85f, 1618.27f, (byte) 75, "301390000_Wave_Bottom_Right_02", CreatureState.WALK_MODE, 2000);
					break;
			}
		}, 10000, 40000));
	}

	private void scheduleCommanderWave() {
		ThreadPoolManager.getInstance().schedule(() -> {
			sp(Rnd.get(236216, 236220), 635.17f, 811.90f, 1598.50f, (byte) 30, "301390000_Wave_Commander_Left", 1000);
			sp(Rnd.get(236216, 236220), 635.17f, 811.90f, 1598.50f, (byte) 30, "301390000_Wave_Commander_Middle", 0);
			sp(Rnd.get(236216, 236220), 635.17f, 811.90f, 1598.50f, (byte) 30, "301390000_Wave_Commander_Right", 1000);
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					sp(Rnd.get(236216, 236220), 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Left", 0);
					sp(Rnd.get(236216, 236220), 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Right", 0);
					if (waveCount >= 3)
						sp(Rnd.get(236216, 236220), 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Middle", 0);
					if (waveCount >= 4) {
						sp(Rnd.get(236216, 236220), 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Left", 4000);
						sp(Rnd.get(236216, 236220), 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Right", 4000);
					}
					if (!isHardmode && waveCount == 3) {
						sp(236243, 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Middle", 1000); // Commander Virtsha
						return;
					}
					sp(236238 + waveCount, 634.80f, 790.95f, 1596.80f, (byte) 30, "301390000_Wave_Commander_Middle", 3000); // Boss
				}
			}, 8000);
		}, 20000);
	}

	private void scheduleBeritrasDespawn() {
		/* beritraDespawnTask = */ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				beritraProgressCount++;
				switch (beritraProgressCount) {
				// TODO:

				}
			}
		}, 10000, 10000);
	}

	private void onTwinsComplete() {
		ThreadPoolManager.getInstance().schedule(() -> {
			sp(race == Race.ELYOS ? 209690 : 209755, 543.35f, 149.81f, 1681.82f, (byte) 0, "301390000_NPCPathFunction_Npc_Path03", 3000);
			sp(race == Race.ELYOS ? 209690 : 209755, 543.45f, 208.97f, 1681.82f, (byte) 0, "301390000_NPCPathFunction_Npc_Path01", 3000);
			sp(race == Race.ELYOS ? 209693 : 209758, 543.43f, 155.08f, 1681.82f, (byte) 0, "301390000_NPCPathFunction_Npc_Path04", 3000);
			sp(race == Race.ELYOS ? 209693 : 209758, 543.43f, 214.24f, 1681.82f, (byte) 0, "301390000_NPCPathFunction_Npc_Path02", 3000);
			ThreadPoolManager.getInstance().schedule(() -> {
				spawn(race == Race.ELYOS ? 209695 : 209760, 582.61f, 183.52f, 1683.73f, (byte) 0);
				spawn(race == Race.ELYOS ? 209694 : 209759, 582.55f, 178.02f, 1683.73f, (byte) 0);
			}, 15000);
		}, 2000);
	}

	private void onOrissanComplete() {
		ThreadPoolManager.getInstance().schedule(() -> {
			spawn(race == Race.ELYOS ? 209705 : 209770, 816.18f, 517.78f, 1707.41f, (byte) 33);
			spawn(race == Race.ELYOS ? 209705 : 209770, 820.82f, 517.82f, 1707.41f, (byte) 33);
			spawn(race == Race.ELYOS ? 209704 : 209769, 816.00f, 521.35f, 1706.88f, (byte) 33);
			spawn(race == Race.ELYOS ? 209704 : 209769, 820.48f, 521.49f, 1706.88f, (byte) 33);

			spawn(race == Race.ELYOS ? 209711 : 209776, 811.00f, 587.83f, 1701.045f, (byte) 32);
			spawn(race == Race.ELYOS ? 209709 : 209774, 807.50f, 583.54f, 1701.045f, (byte) 32);
			spawn(race == Race.ELYOS ? 209710 : 209775, 814.85f, 583.56f, 1701.045f, (byte) 32);
			spawn(race == Race.ELYOS ? 209708 : 209773, 811.11f, 582.62f, 1701.045f, (byte) 32);
		}, 2500);
	}

	private void onWaveEventComplete() {
		for (Future<?> task : waveAssaultTasks)
			cancelTask(task);
		deleteNpcById(236204);
		deleteNpcById(236205);
		deleteNpcById(236206);
		deleteNpcById(236216);
		deleteNpcById(236217);
		deleteNpcById(236218);
		deleteNpcById(236219);
		deleteNpcById(236220);
		SkillEngine.getInstance().getSkill(getNpc(race == Race.ELYOS ? 702719 : 702720), 20838, 1, getNpc(731580)).useSkill();
		switch (fallenWaveDefender.get()) {
			case 0:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_BONUS_04());
				break;
			case 1:
			case 2:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_BONUS_03());
				break;
			case 3:
			case 4:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_BONUS_02());
				break;
			case 5:
			case 6:
			case 7:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_BONUS_01());
				break;
		}
	}

	private void onTwinFail() {
		isHardmode = false;
		deleteNpcById(236232);
		spawn(236229, 812.0238f, 568.3602f, 1701.045f, (byte) 92);
		// TODO: spawn weak Beritra and despawn hardmode version
	}

	private void onOrissanFail() {
		isHardmode = false;
		// TODO: spawn weak Beritra and despawn hardmode version
	}

	private void deleteNpcById(int id) {
		for (Npc npc : instance.getNpcs()) {
			if (npc.getNpcId() == id)
				npc.getController().delete();
		}
	}

	@Override
	public void onAggro(Npc npc) {
		switch (npc.getNpcId()) {
			case 236227:
			case 236228:
				if (isTwinFightStarted.compareAndSet(false, true)) {
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_01());
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_02(), 2000);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_03(), 4000);
					onTwinFightStart();
				}
				break;
			case 236232:
				if (isOrissanFightStarted.compareAndSet(false, true))
					ThreadPoolManager.getInstance().schedule(() -> scheduleOrissansImmortalPhase(npc, 0), 5000);
				break;
			case 236246: // Lv3mode Human Beritra
				if (isBeritraFightStarted.compareAndSet(false, true))
					/* onBeritraFightStart() */;
				break;
			case 236247: // Dragon Beritra
				if (isBeritraTransformed.compareAndSet(false, true))
					scheduleBeritrasDespawn();
				break;
		}
	}

	@Override
	public void onDie(Npc npc) {
		int npcId = npc.getNpcId();
		switch (npcId) {
			case 236248:
			case 236249:
				if (fallenWaveDefender.incrementAndGet() == 8) {
					deleteNpcById(race == Race.ELYOS ? 209723 : 209788);
					deleteNpcById(race == Race.ELYOS ? 702719 : 702720);
				}
				break;
			case 236227:
			case 236228:
				if (getNpc(npcId == 236227 ? 236228 : 236227) == null) {
					cancelTask(twinFailTask);
					sendMsg(isHardmode ? SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_07() : SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_06());
					deleteNpcById(855708);
					deleteNpcById(855709);
					onTwinsComplete();
				} else {
					spawn(npcId == 236227 ? 855708 : 855709, npc.getX(), npc.getY(), npc.getZ(), (byte) 0); // Sphere
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_RESSURECT_02());
					ThreadPoolManager.getInstance().schedule(() -> {
						if (getNpc(npcId == 236227 ? 236228 : 236227) != null) {
							switch (npcId) {
								case 236227: // Lava Protector
									spawn(npcId, 531.0885f, 212.43806f, 1683.4116f, (byte) 60);
									break;
								case 236228: // Heatvent Protector
									spawn(npcId, 530.8584f, 151.8681f, 1683.4116f, (byte) 60);
									break;
							}
							deleteNpcById(855708);
							deleteNpcById(855709);
							sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_RESSURECT_03());
						}
					}, 15000);
				}
				npc.getController().delete();
				break;
			case 236229: // Orissan Hero
			case 236234: // Exhausted Orissan Legendary
				cancelTask(orissanSwitchTask);
				onOrissanComplete();
				break;
			case 236204:
			case 236205:
			case 236206:
			case 236216:
			case 236217:
			case 236218:
			case 236219:
			case 236220:
				npc.getController().delete();
				break;
			case 236239:
			case 236240:
			case 236241:
			case 236242:
			case 236243:
				if (killedCommanderCount.incrementAndGet() == (isHardmode ? 5 : 3))
					onWaveEventComplete();
				npc.getController().delete();
				break;
			case 236224:
				doors.get(375).setOpen(true);
				break;
			case 236661:
				doors.get(376).setOpen(true);
				break;
			case 236662:
				doors.get(378).setOpen(true);
				break;
			case 731580:
				switch (npc.getSpawn().getStaticId()) {
					case 332:
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_WAVE_09());
						break;
					case 522:
					case 603:
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDSEAL_TWIN_08());
				}
				break;
		}
	}

	@Override
	public void onEndEffect(Creature effector, Creature effected, int skillId) {
		switch (skillId) {
			case 21610:
			case 21611:
			case 21612:
				if (dispelledBuffs.incrementAndGet() == 3)
					// onBeritraTransform();
					break;
		}
	}

	@Override
	public void onCreatureDetected(Npc detector, Creature detected) {
		if (!(detected instanceof Player))
			return;
		switch (detector.getNpcId()) {
			case 206405:
				if (isWaveEventStarted.compareAndSet(false, true))
					onWaveEventStart();
				break;
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		if (isRaceSet.compareAndSet(false, true)) {
			if (player.getRace() == Race.ELYOS)
				race = Race.ELYOS;

			spawn(race == Race.ELYOS ? 209678 : 209743, 353.443f, 185.629f, 1686.1825f, (byte) 60);
			spawn(race == Race.ELYOS ? 209679 : 209744, 353.443f, 179.259f, 1686.1825f, (byte) 60);
			spawn(race == Race.ELYOS ? 209680 : 209745, 347.099f, 192.390f, 1686.1825f, (byte) 90);
			spawn(race == Race.ELYOS ? 209680 : 209745, 351.295f, 192.437f, 1686.1825f, (byte) 90);
			spawn(race == Race.ELYOS ? 209681 : 209746, 347.099f, 173.774f, 1686.1825f, (byte) 30);
			spawn(race == Race.ELYOS ? 209681 : 209746, 351.295f, 173.821f, 1686.1825f, (byte) 30);
		}
	}

	@Override
	public void onExitInstance(Player player) {
		TeleportService2.moveToInstanceExit(player, mapId, player.getRace());
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PlayerReviveService.revive(player, 25, 25, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		TeleportService2.teleportTo(player, mapId, instanceId, 361.706f, 182.503f, 1684.290f, (byte) 0);
		return true;
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.sendPacket(player, new SM_DIE(false, false, 0, 8));
		return true;
	}

	private void sp(int id, float x, float y, float z, byte h, String walkerId, int delay) {
		sp(id, x, y, z, h, walkerId, CreatureState.ACTIVE, delay);
	}

	private void sp(int id, float x, float y, float z, byte h, String walkerId, CreatureState state, int delay) {
		final Npc npc = (Npc) spawn(id, x, y, z, h);
		ThreadPoolManager.getInstance().schedule(() -> {
			npc.getSpawn().setWalkerId(walkerId);
			WalkManager.startWalking((NpcAI2) npc.getAi2());
			npc.setState(state, true);
			PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.START_EMOTE2, 0, npc.getObjectId()));
		}, delay);
	}

	private void cancelTask(Future<?> task) {
		if (task != null && !task.isCancelled())
			task.cancel(true);
	}
}
