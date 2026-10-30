package instance.crucible;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.attack.AggroTarget;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.model.instance.playerreward.CruciblePlayerReward;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.network.aion.instanceinfo.InstanceScoreWriter;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_STAGE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Luzien, w4terbomb
 */
@InstanceID(300300000)
public class EmpyreanCrucibleInstance extends CrucibleInstance {

	private static final String FIGHTER_AI = "empyrean_fighter";
	private static final String SUPPLY_AI = "empyrean_manduri_supply_device";
	private final List<Npc> npcs = new ArrayList<>();
	private final AtomicInteger stageKills = new AtomicInteger();
	private final Set<Integer> stage1Advanced = ConcurrentHashMap.newKeySet();
	private final Map<Integer, Future<?>> stage1Timers = new ConcurrentHashMap<>();
	private final Set<Integer> bossGreeted = ConcurrentHashMap.newKeySet();
	private final Set<Integer> inWorld = ConcurrentHashMap.newKeySet(); // entered and not left, logged out ones included
	private byte stage;
	private boolean isDoneStage6Round2, isDoneStage6Round1, isKrallSet, isBonusStage2Over, gameOver;
	private long lastSerfShout;
	private Future<?> dragonTimeoutTask, bonusStage4Task, bonusStage6Task, bonusStage9Task, golemTask;

	public EmpyreanCrucibleInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		super.onInstanceCreate();
		stage = 0;
		sp(799567, 345.25107f, 349.40176f, 96.09097f, (byte) 0);
		sp(799573, 384.51f, 352.61078f, 96.747635f, (byte) 83);
	}

	@Override
	public void onEnterInstance(Player player) {
		boolean isNew = !instanceScore.containsPlayer(player.getObjectId());
		super.onEnterInstance(player);
		inWorld.add(player.getObjectId());
		CruciblePlayerReward playerReward = getPlayerReward(player.getObjectId());
		if (gameOver) {
			doReward(player);
		} else if (stage > 0) {
			if (isNew) { // a late joiner waits until the arbiter lets them in
				playerReward.setPlayerDefeated(true);
				moveToReadyRoom(player);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ENTERED_BIRTHAREA_IDARENA());
			} else {
				portToStartPosition(player);
			}
		}
		broadcastScore();
		PacketSendUtility.sendPacket(player, new SM_INSTANCE_STAGE_INFO(2, stageType.getId(), stageType.getType()));
	}

	@Override
	public boolean canEnter(Player player) {
		CruciblePlayerReward reward = getPlayerReward(player.getObjectId());
		if (reward != null && reward.isRewarded()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_REENTER_INSTANCE_IDARENA());
			return false;
		}
		return true;
	}

	private void addPointsAndSendPacket(int points, Npc npc) {
		instance.forEachPlayer(player -> {
			if (player.isOnline()) {
				CruciblePlayerReward playerReward = getPlayerReward(player.getObjectId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(npc.getObjectTemplate().getL10n(), points));
				if (!playerReward.isRewarded())
					playerReward.addPoints(points);
			}
		});
		broadcastScore();
	}

	private void broadcastScore() {
		if (isInstanceDestroyed)
			return;
		Set<Integer> present = instance.getPlayersInside().stream().map(Player::getObjectId).collect(Collectors.toSet());
		instance.forEachPlayer(p -> PacketSendUtility.sendPacket(p, new SM_INSTANCE_SCORE(instance.getMapId(), new ScoreWriter(instanceScore, present))));
	}

	private void setStage(StageType type, int time) {
		this.stageType = type;
		ThreadPoolManager.getInstance().schedule(
			() -> instance.forEachPlayer(player -> PacketSendUtility.sendPacket(player, new SM_INSTANCE_STAGE_INFO(2, type.getId(), type.getType()))),
			time);
		broadcastScore();
	}

	@Override
	public void onDie(Npc npc) {
		synchronized (npcs) {
			npcs.remove(npc);
		}
		int points = switch (npc.getNpcId()) {
			case 217511, 217512, 217513, 217514 -> 55;
			case 217518 -> 110;
			case 217515, 217516, 217517 -> 120;
			case 217522 -> 250;
			case 217560, 217561 -> 260;
			case 217557, 217558, 217559 -> 280;
			case 217494, 217497, 217499, 217504, 217507, 217508, 217519, 217520, 217521, 217529, 217531, 217545, 217547, 217548, 217549, 217550, 217569 -> 300;
			case 217562 -> 340;
			case 217530, 217564, 217565 -> 350;
			case 217495, 217498, 217502, 217505, 217506, 217536, 217563, 217566 -> 400;
			case 217654 -> 455;
			case 217523, 217524, 217525, 217526 -> 475;
			case 217568 -> 480;
			case 217480, 217489, 217496, 217503, 217532, 217533, 217534, 217535, 217653 -> 500;
			case 217652 -> 590;
			case 217477, 217486 -> 600;
			case 217651 -> 695;
			case 217570 -> 700;
			case 217600, 217601, 217602 -> 1090;
			case 217483, 217492 -> 1100;
			case 217478, 217487 -> 1200;
			case 217482, 217491 -> 1300;
			case 217603, 217604, 217605, 217606 -> 1390;
			case 217500, 217509 -> 1500;
			case 217552 -> 1900;
			case 217543, 217553 -> 2000;
			case 217567 -> 2100;
			case 217554 -> 2400;
			case 217578, 217582 -> 2900;
			case 217579, 217583 -> 3100;
			case 217527, 217528 -> 3250;
			case 217572 -> 3370;
			case 217581, 217585 -> 3400;
			case 217580, 217584 -> 3800;
			case 217588, 217589 -> 4000;
			case 217590 -> 4200;
			case 217596, 217597 -> 4250;
			case 217484, 217493, 217591 -> 5000;
			case 217501, 217510 -> 5500;
			case 217592 -> 6000;
			case 217594 -> 6200;
			case 217595 -> 7300;
			case 217544, 217556 -> 7800;
			case 217573 -> 8900;
			case 217598 -> 9400;
			case 217586, 217587 -> 11500;
			case 217593 -> 17000;
			case 217607 -> 17800;
			case 217599 -> 25900;
			case 217608 -> 192500;
			case 217609 -> 204400;
			default -> 0;
		};
		if (points != 0) {
			addPointsAndSendPacket(points, npc);
		}
		switch (npc.getNpcId()) {
			case 217477, 217478, 217480, 217482, 217483, 217486, 217487, 217489, 217491, 217492 -> {
				cancelStage1Timer(npc);
				advanceStage1(npc);
			}
			case 217484, 217493 -> {
				PacketSendUtility.broadcastMessage(npc, npc.getNpcId() == 217484 ? 1500206 : 1500246);
				setStage(StageType.PASS_GROUP_STAGE_1, 0);
				int boxId = getRegisteredTeamRace() == Race.ASMODIANS ? 217756 : 217734; // S1_ResurBox1Dark_55_n / S1_ResurBox1_55_n
				sp(boxId, 342.65106f, 357.4013f, 96.09094f, (byte) 0, 1000);
				sp(boxId, 349.07376f, 357.5898f, 96.090965f, (byte) 0, 1000);
				sp(boxId, 345.69272f, 359.40958f, 96.09094f, (byte) 0, 1000);
				sp(boxId, 342.59192f, 353.73386f, 96.090965f, (byte) 0, 1000);
				sp(boxId, 342.6043f, 360.8932f, 96.09093f, (byte) 0, 1000);
				sp(boxId, 345.69318f, 355.56677f, 96.09094f, (byte) 0, 1000);
				spawnArenaRecordKeeper(799568); // IDArena_2nd_MC_Start
			}
			case 217494, 217495, 217496, 217497, 217498, 217499, 217500, 217502, 217503, 217504, 217505, 217506, 217507, 217508, 217509 -> {
				switch (stageKills.incrementAndGet()) {
					case 3 -> startStage2Round(2);
					case 7 -> startStage2Round(3);
					case 8 -> startStage2Round(4);
					case 13 -> startStage2Round(5);
				}
			}
			case 217510, 217501 -> {
				setStage(StageType.PASS_GROUP_STAGE_2, 0);
				ThreadPoolManager.getInstance().schedule(this::startBonusStage2, 8000);
			}
			case 217737 -> {
				PacketSendUtility.broadcastMessage(npc, 342353);
				endBonusStage2();
			}
			case 217511, 217512, 217513, 217514 -> {
				if (allDead(217511, 217512, 217513, 217514)) {
					setStage(StageType.START_STAGE_3_ROUND_2, 3000);
					sp(217515, 336.32092f, 345.0251f, 96.090935f, (byte) 0, 8000); // S3_ElementalEarth_2nd_55_Ae
					sp(217516, 347.16144f, 361.89084f, 96.09093f, (byte) 0, 8000); // S3_ElementalFire_2nd_55_Ae
					sp(217517, 354.132f, 337.14255f, 96.09089f, (byte) 0, 8000); // S3_ElementalWater_2nd_55_Ae
					sp(217518, 352.77557f, 360.97845f, 96.09091f, (byte) 0, 8000); // S3_ElementalAir_2nd_55_Ae
					sp(217515, 349.16327f, 335.63864f, 96.09095f, (byte) 0, 8000); // S3_ElementalEarth_2nd_55_Ae
					sp(217516, 350.0108f, 342.09482f, 96.090935f, (byte) 0, 8000); // S3_ElementalFire_2nd_55_Ae
					sp(217517, 353.7888f, 354.4324f, 96.091064f, (byte) 0, 8000); // S3_ElementalWater_2nd_55_Ae
					sp(217518, 340.2231f, 351.10208f, 96.09098f, (byte) 0, 8000); // S3_ElementalAir_2nd_55_Ae
					sp(217515, 348.87338f, 354.90146f, 96.09096f, (byte) 0, 8000); // S3_ElementalEarth_2nd_55_Ae
					sp(217516, 334.60898f, 352.01447f, 96.09095f, (byte) 0, 8000); // S3_ElementalFire_2nd_55_Ae
					sp(217517, 341.23633f, 344.55603f, 96.09096f, (byte) 0, 8000); // S3_ElementalWater_2nd_55_Ae
					sp(217518, 354.66513f, 343.31537f, 96.091095f, (byte) 0, 8000); // S3_ElementalAir_2nd_55_Ae
				}
			}
			case 217515, 217516, 217517, 217518 -> {
				if (allDead(217515, 217516, 217517, 217518)) {
					setStage(StageType.START_STAGE_3_ROUND_3, 3000);
					sp(217519, 351.08026f, 341.61298f, 96.090935f, (byte) 0, 8000); // S3_ElementalEarth_3rd_55_Ae
					sp(217521, 333.4532f, 354.7357f, 96.09094f, (byte) 0, 8000); // S3_ElementalWater_3rd_55_Ae
					sp(217522, 342.1805f, 360.534f, 96.09092f, (byte) 0, 8000); // S3_ElementalAir_3rd_55_Ae
					sp(217520, 334.2686f, 342.60797f, 96.09091f, (byte) 0, 8000); // S3_ElementalFire_3rd_55_Ae
					sp(217522, 350.34537f, 356.18558f, 96.09094f, (byte) 0, 8000); // S3_ElementalAir_3rd_55_Ae
					sp(217520, 343.7485f, 336.2869f, 96.09092f, (byte) 0, 8000); // S3_ElementalFire_3rd_55_Ae
				}
			}
			case 217519, 217520, 217521, 217522 -> {
				if (allDead(217519, 217520, 217521, 217522)) {
					setStage(StageType.START_STAGE_3_ROUND_4, 3000);
					sp(217524, 349.66446f, 341.4752f, 96.090965f, (byte) 0, 8000); // S3_ElementalFire_4th_55_Ae
					sp(217525, 338.32742f, 356.29636f, 96.090935f, (byte) 0, 8000); // S3_ElementalWater_4th_55_Ae
					sp(217526, 349.31473f, 358.43762f, 96.09096f, (byte) 0, 8000); // S3_ElementalAir_4th_55_Ae
					sp(217523, 338.73138f, 342.35876f, 96.09094f, (byte) 0, 8000); // S3_ElementalEarth_4th_55_Ae
				}
			}
			case 217523, 217524, 217525, 217526 -> {
				if (allDead(217523, 217524, 217525, 217526)) {
					setStage(StageType.START_STAGE_3_ROUND_5, 3000);
					sp(217527, 335.37524f, 346.34567f, 96.09094f, (byte) 0, 8000); // S3_MagmaElemental_Boss_55_Ah
					sp(217528, 335.36105f, 353.16922f, 96.09094f, (byte) 0, 8000); // S3_TempestElemental_Boss_55_Ah
				}
			}
			case 217527, 217528 -> {
				if (allDead(217527, 217528)) {
					setStage(StageType.START_BONUS_STAGE_3, 8000);
					sp(217744, 342.45215f, 349.339f, 96.09096f, (byte) 0, 8000); // S3_Elemeltal_CTRL
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S3_Elemeltal_CTRL_01(), 8000);
					ThreadPoolManager.getInstance().schedule(this::startBonusStage3, 40000);
				}
			}
			case 217557, 217558, 217559, 217560, 217561, 217562, 217563, 217564, 217565, 217566 -> {
				switch (stageKills.incrementAndGet()) {
					case 5 -> startStage4Round(2);
					case 11 -> startStage4Round(3);
					case 16 -> startStage4Round(4);
				}
			}
			case 217571, 217745, 217746, 217747, 217748, 205413, 205414, 217576, 217577 -> npc.getController().delete();
			case 217567 -> {
				stageKills.incrementAndGet();
				startStage4Round(5);
			}
			case 217651, 217652, 217653, 217654 -> {
				if (stageKills.incrementAndGet() == 29) {
					setStage(StageType.PASS_GROUP_STAGE_4, 1000);
					sp(217749, 340.59f, 349.32166f, 96.09096f, (byte) 0, 8000); // S4_Draky_CTRL
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S4_Draky_CTRL_00(), 8000);
					setStage(StageType.START_BONUS_STAGE_4, 8000);
					ThreadPoolManager.getInstance().schedule(this::startBonusStage4, 40000);
				}
			}
			case 217545, 217547, 217548, 217549, 217550 -> {
				if (stageKills.incrementAndGet() == 5) {
					setStage(StageType.START_STAGE_5_ROUND_2, 3000);
					sp(217552, 1246.0197f, 788.8341f, 358.60556f, (byte) 11, 8000); // S4_ShulackBartenderNmd_55_Ah
				}
			}
			case 217529, 217530, 217531, 217532, 217533, 217534, 217535, 217536, 217543 -> {
				switch (stageKills.incrementAndGet()) {
					case 5 -> startLehparRound(2);
					case 10 -> startLehparRound(3);
					case 11 -> startLehparRound(4);
					case 16 -> startLehparRound(5);
				}
			}
			case 217552 -> {
				setStage(StageType.START_STAGE_5_ROUND_3, 3000);
				ThreadPoolManager.getInstance().schedule(() -> {
					spawnLever(700527);
					spawnLever(700528);
				}, 1000);
				sp(217553, "empyrean_manduri_giant", 1259.4706f, 812.30505f, 358.6056f, (byte) 30, 8000); // S4_ManduriGiantNmd_55_Ah
			}
			case 217553 -> {
				deleteAliveNpcs(700527, 700528, 281128, 281129);
				sp(281111, 1246.4855f, 796.90735f, 358.6056f, (byte) 0, 1000); // BIDShulack_EngineerNozzleD_45_An
				sp(281110, 1259.5508f, 784.5548f, 358.60562f, (byte) 0, 1000); // BIDShulack_EngineerNozzleC_45_An
				sp(281112, 1276.6561f, 812.5499f, 358.60565f, (byte) 0, 1000); // BIDShulack_EngineerNozzleE_45_An
				sp(281322, 1243.2113f, 813.0927f, 358.60565f, (byte) 0, 1000); // BIDShulack_EngineerNozzleH_45_An
				sp(281109, 1272.9266f, 797.1055f, 358.60562f, (byte) 0, 1000); // BIDShulack_EngineerNozzleB_45_An
				sp(281113, 1275.894f, 780.51544f, 358.60565f, (byte) 0, 1000); // BIDShulack_EngineerNozzleF_45_An
				sp(281108, 1260.003f, 810.555f, 358.6056f, (byte) 0, 1000); // BIDShulack_EngineerNozzleA_45_An
				sp(281114, 1244.3293f, 780.4284f, 358.60562f, (byte) 0, 1000); // BIDShulack_EngineerNozzleG_45_An
				setStage(StageType.START_STAGE_5_ROUND_4, 3000);
				sp(217554, 1243.1877f, 796.79553f, 358.6056f, (byte) 0, 8000); // S4_ShulackEngineerNmd_55_Ah
			}
			case 217554 -> {
				deleteAliveNpcs(281108, 281109, 281110, 281111, 281112, 281113, 281114, 281322);
				setStage(StageType.START_STAGE_5_ROUND_5, 3000);
				sp(217556, "empyrean_shulack_captain", 1259.8387f, 785.6266f, 358.60562f, (byte) 30, 8000); // S4_ShulackCaptainNmd_55_Ah
				sp(281191, 1261.8f, 804.5f, 358.7f, (byte) 0, 8000); // BIDShulack_CaptainStigmaStoneA_45_n
				sp(281192, 1267.6f, 793.9f, 358.7f, (byte) 0, 8000); // BIDShulack_CaptainStigmaStoneB_45_n
				sp(281193, 1257.4f, 787.9f, 358.7f, (byte) 0, 8000); // BIDShulack_CaptainStigmaStoneC_45_n
				sp(281194, 1251.3f, 798.6f, 358.7f, (byte) 0, 8000); // BIDShulack_CaptainStigmaStoneD_45_n
				sp(281180, "no_dmg_no_action", 1259.5123f, 796.5463f, 360.0908f, (byte) 0, 1000); // BIDShulack_CaptainStigma_45_n
				sp(205499, 1259.5135f, 796.5465f, 361.4129f, (byte) 0, 1000); // IDArena_5S_Stigma
			}
			case 217544, 217556 -> {
				deleteAliveNpcs(281191, 281192, 281193, 281194, 281180, 205499);
				setStage(StageType.PASS_GROUP_STAGE_5, 0);
				sp(205339, 1260.1465f, 795.07495f, 358.60562f, (byte) 30, 1000); // IDArena_5th_MC_End
			}
			case 217568 -> {
				sp(205413, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading()); // S6_NPC_Graveknight_55_Ae
				npc.getController().delete();
				if (stageType == StageType.START_STAGE_6_ROUND_1 && isDoneStage6Round1 && allDead(217568)) {
					setStage(StageType.START_STAGE_6_ROUND_2, 3000);
					sp(217570, 1629.4642f, 154.8044f, 126f, (byte) 30, 8000); // S6_Zombie_55_Ae
					sp(217569, 1643.7776f, 161.63562f, 126f, (byte) 46, 8000); // S6_LichEquip_55_Ae
					sp(217569, 1639.7843f, 142.09268f, 126f, (byte) 40, 8000); // S6_LichEquip_55_Ae
					ThreadPoolManager.getInstance().schedule(() -> {
						sp(217569, 1614.6377f, 164.04999f, 126.00113f, (byte) 3); // S6_LichEquip_55_Ae
						sp(217569, 1625.8965f, 135.62509f, 126f, (byte) 30); // S6_LichEquip_55_Ae
						isDoneStage6Round2 = true;
					}, 18000);
				}
			}
			case 217570 -> {
				for (int i = 0; i < 5; i++) {
					Npc worm = sp(217571, npc.getX() + Rnd.get(-2, 2), npc.getY() + Rnd.get(-2, 2), npc.getZ(), npc.getHeading()); // S6_Clodworm_55_Ae
					if (worm != null)
						ThreadPoolManager.getInstance().schedule(() -> worm.getController().delete(), 30000);
				}
			}
			case 217569 -> {
				sp(205414, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				npc.getController().delete();
				if (stageType == StageType.START_STAGE_6_ROUND_2 && isDoneStage6Round2 && allDead(217569, 217570)) {
					setStage(StageType.START_STAGE_6_ROUND_3, 3000);
					sp(217572, 1629.5837f, 138.38435f, 126f, (byte) 30, 8000); // S6_DeathNight_Live_55_Ah
					sp(217569, 1635.01535f, 150.01535f, 126f, (byte) 45, 8000); // S6_LichEquip_55_Ae
					sp(217569, 1638.3817f, 152.84074f, 126f, (byte) 45, 8000); // S6_LichEquip_55_Ae
					sp(217568, 1622.8409f, 173.1985f, 126f, (byte) 90, 20000); // S5_Graveknight_55_Ae
				}
			}
			case 217572 -> {
				setStage(StageType.START_STAGE_6_ROUND_4, 3000);
				sp(217573, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading()); // S6_Deathknight_Ghost_55_Ah_01
			}
			case 217573 -> {
				setStage(StageType.PASS_GROUP_STAGE_6, 0);
				sp(217736, 1589.2f, 149.5f, 128.8f, (byte) 0, 1000); // S6_ResurBox1_55_n
				sp(217736, 1590.1f, 153.6f, 128.8f, (byte) 0, 1000); // S6_ResurBox1_55_n
				sp(217750, "empyrean_ghost_arminos", 1624.1908f, 155.16148f, 126f, (byte) 0, 8000); // S6_Ghost_55_Ah
				setStage(StageType.START_BONUS_STAGE_6, 8000);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S6_Ghost_55_Ah_01(), 8000);
			}
			case 217750 -> {
				PacketSendUtility.broadcastMessage(npc, 1500259);
				if (bonusStage6Task != null)
					bonusStage6Task.cancel(false);
				deleteAliveNpcs(217751, 217752);
				sp(205340, 1625.08f, 159.15f, 126f, (byte) 70, 1000); // IDArena_6th_MC_End
			}
			case 217582, 217578 -> {
				setStage(StageType.START_STAGE_7_ROUND_2, 3000);
				int npcId = getRegisteredTeamRace() == Race.ASMODIANS ? 217583 : 217579; // S7_MasterD_Sc_55_Ah / S7_MasterL_Sc_55_Ah
				sp(npcId, "ranger_preceptor", 1794.81f, 779.53925f, 469.35016f, (byte) 40, 8000);
			}
			case 217579, 217583 -> {
				setStage(StageType.START_STAGE_7_ROUND_3, 3000);
				int npcId = getRegisteredTeamRace() == Race.ASMODIANS ? 217584 : 217580; // S7_MasterD_Ma_55_Ah / S7_MasterL_Ma_55_Ah
				sp(npcId, "mage_preceptor", 1775.6254f, 811.43225f, 469.35022f, (byte) 100, 8000);
			}
			case 217580, 217584 -> {
				setStage(StageType.START_STAGE_7_ROUND_4, 3000);
				int npcId = getRegisteredTeamRace() == Race.ASMODIANS ? 217585 : 217581; // S7_MasterD_Pr_55_Ah / S7_MasterL_Pr_55_Ah
				sp(npcId, "priest_preceptor", 1775.716f, 779.630f, 469.564f, (byte) 20, 8000);
			}
			case 217581, 217585 -> {
				setStage(StageType.START_STAGE_7_ROUND_5, 3000);
				int npcId = getRegisteredTeamRace() == Race.ASMODIANS ? 217587 : 217586; // S7_Stigma_MasterD_Boss_55_Ah / S7_Stigma_MasterL_Boss_55_Ah
				sp(npcId, "stigma_boss_preceptor", 1773.194f, 796.537f, 469.350f, (byte) 0, 8000);
			}
			case 217586, 217587 -> {
				setStage(StageType.PASS_GROUP_STAGE_7, 0);
				int bookBoxId = getRegisteredTeamRace() == Race.ASMODIANS ? 217759 : 217753; // S7_BookBoxDark_55_n / S7_BookBox_55_n
				sp(bookBoxId, "noaction", 1782.881f, 800.114f, 469.420f, (byte) 0, 1000);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S7_BookBox_01(), 1000);
				sp(205341, 1781.610f, 796.920f, 469.350f, (byte) 0, 1000); // IDArena_7th_MC_End
			}
			case 217588, 217589 -> {
				setStage(StageType.START_STAGE_8_ROUND_2, 3000);
				sp(217590, 1764.377f, 1761.510f, 303.695f, (byte) 0, 8000); // S8_Seiren_Queen_55_Ah
			}
			case 217590 -> {
				setStage(StageType.START_STAGE_8_ROUND_3, 3000);
				sp(217591, 1776.946f, 1749.255f, 303.696f, (byte) 30, 8000); // S8_AkanNamed_50_Ah
			}
			case 217591 -> {
				setStage(StageType.START_STAGE_8_ROUND_4, 3000);
				sp(217592, 1790.693f, 1761.911f, 303.877f, (byte) 60, 8000); // S8_TestResult_55_Ah
			}
			case 217592 -> {
				setStage(StageType.START_STAGE_8_ROUND_5, 3000);
				Npc perfectMan = sp(280790, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading()); // BLF3_NM_PerfectManSum_45_Ae
				if (perfectMan != null)
					ThreadPoolManager.getInstance().schedule(() -> perfectMan.getController().delete(), 120000);
				sp(217593, 1777.065f, 1763.706f, 303.695f, (byte) 90, 8000); // S8_TestResult_Boss_55_Ah
			}
			case 217593 -> {
				setStage(StageType.PASS_GROUP_STAGE_8, 0);
				sp(205342, 1776.757f, 1764.624f, 303.695f, (byte) 90, 1000); // IDArena_8th_MC_End
			}
			case 217594 -> {
				setStage(StageType.START_STAGE_9_ROUND_2, 3000);
				sp(217595, 1322.311f, 1741.508f, 316.349f, (byte) 65, 8000); // S9_Condor_Gi_55_Ah
			}
			case 217595 -> {
				setStage(StageType.START_STAGE_9_ROUND_3, 3000);
				sp(217596, 1308.038f, 1729.718f, 315.996f, (byte) 36, 8000); // S9_Manduri_Gi_55_Ah
				sp(217597, 1302.290f, 1745.471f, 316.092f, (byte) 96, 8000); // S9_Frillfaimam_Gi_55_Ah
			}
			case 217596, 217597 -> {
				Npc counterpart = getNpc(npc.getNpcId() == 217596 ? 217597 : 217596);
				if (counterpart != null && !counterpart.isDead())
					counterpart.queueSkill(19624, 10, -1, NpcSkillTargetAttribute.ME);
				if (allDead(217596, 217597)) {
					setStage(StageType.START_STAGE_9_ROUND_4, 3000);
					sp(217598, 1323.1051f, 1717.7927f, 317.1f, (byte) 45, 8000); // S9_Shellizard_Gi_55_Ah
				}
			}
			case 217598 -> {
				setStage(StageType.START_STAGE_9_ROUND_5, 3000);
				sp(217599, 1304.6428f, 1733.9614f, 316.5f, (byte) 3, 8000); // S9_Chimera_Boss_55_Ah
			}
			case 217599 -> {
				setStage(StageType.PASS_GROUP_STAGE_9, 0);
				startBonusStage9();
			}
			case 217754 -> endBonusStage9();
			case 217600, 217601, 217602 -> {
				if (stageKills.incrementAndGet() == 10) {
					setStage(StageType.START_STAGE_10_ROUND_2, 3000);
					sp(217604, 1769.898f, 1303.915f, 394.3f, (byte) 77, 8000); // S10_Drakan_Sc_High_55_Ae
					sp(217605, 1773.268f, 1302.647f, 394.3f, (byte) 77, 8000); // S10_Drakan_Ma_High_55_Ae
					sp(217606, 1776.197f, 1299.988f, 394.3f, (byte) 77, 8000); // S10_Drakan_Cl_High_55_Ae
					sp(217603, 1778.467f, 1298.172f, 394.3f, (byte) 77, 24000); // S10_Drakan_Fi_High_55_Ae
					sp(217603, 1765.897f, 1305.908f, 394.3f, (byte) 77, 24000); // S10_Drakan_Fi_High_55_Ae
					sp(217604, 1771.398f, 1306.403f, 394.3f, (byte) 77, 70000); // S10_Drakan_Sc_High_55_Ae
					sp(217605, 1774.632f, 1305.220f, 394.3f, (byte) 77, 70000); // S10_Drakan_Ma_High_55_Ae
					sp(217606, 1777.682f, 1303.205f, 394.3f, (byte) 77, 70000); // S10_Drakan_Cl_High_55_Ae
					sp(217603, 1767.865f, 1308.109f, 394.3f, (byte) 77, 82000); // S10_Drakan_Fi_High_55_Ae
					sp(217603, 1780.228f, 1300.834f, 394.3f, (byte) 77, 82000); // S10_Drakan_Fi_High_55_Ae
				}
			}
			case 217603, 217604, 217605, 217606 -> {
				if (stageKills.incrementAndGet() == 20) {
					setStage(StageType.START_STAGE_10_ROUND_3, 3000);
					sp(217607, FIGHTER_AI, 1765.4668f, 1287.6345f, 394.237f, (byte) 76, 8000); // S10_WaterGolem_55_Ah
				}
			}
			case 700439 -> {
				Npc golem = getNpc(217607);
				if (golem != null && !golem.isDead()) {
					golem.getEffectController().removeEffect(18110); // Cancel Torrent of the summoned aquar
					if (Rnd.chance() < 30) {
						if (golem.getAggroList().getTarget(AggroTarget.SECOND_MOST_HATED) instanceof Creature second)
							golem.getAggroList().addHate(second, 500);
					} else {
						golem.queueSkill(16684, 41);
					}
				}
			}
			case 217607 -> {
				if (golemTask != null)
					golemTask.cancel(false);
				deleteAliveNpcs(700439);
				setStage(StageType.START_STAGE_10_ROUND_4, 3000);
				sp(217608, 1771.9846f, 1301.1503f, 394.237f, (byte) 76, 8000); // S10_Dragon_55_Ah
			}
			case 217608 -> {
				if (dragonTimeoutTask != null)
					dragonTimeoutTask.cancel(false);
				setStage(StageType.START_STAGE_10_ROUND_5, 3000);
				sp(217609, "vanktrist", 1767.3652f, 1294.0907f, 394.3f, (byte) 3, 8000); // SFinal_Immotal_Boss_55_Ah
			}
			case 217609 -> {
				setStage(StageType.PASS_GROUP_STAGE_10, 0);
				sp(205344, 1766.2f, 1292.0f, 394.23755f, (byte) 75, 1000); // IDArena_10th_MC_End
			}
		}
	}

	@Override
	public void onAggro(Npc npc) {
		switch (npc.getNpcId()) {
			case 217477, 217478, 217480, 217483, 217486, 217487, 217489, 217492 -> // the priests only advance the round by dying
				stage1Timers.computeIfAbsent(npc.getObjectId(), id -> ThreadPoolManager.getInstance().schedule(() -> advanceStage1(npc), 40000));
			case 217484 -> {
				if (bossGreeted.add(npc.getObjectId()))
					PacketSendUtility.broadcastMessage(npc, 1500205);
			}
			case 217493 -> {
				if (bossGreeted.add(npc.getObjectId()))
					PacketSendUtility.broadcastMessage(npc, 1500245);
			}
			case 217740, 217741, 217742, 217743 -> { // S3_Elemeltal_Earth/Warter/Fire/Wind_55_Ah
				if (bossGreeted.add(npc.getObjectId()))
					PacketSendUtility.broadcastMessage(npc, 342394 + npc.getNpcId() - 217740);
			}
			case 217750 -> startBonusStage6(npc);
			case 217751 -> { // a blood ward goes off as soon as someone steps on it
				Skill skill = SkillEngine.getInstance().getSkill(npc, 19643, 46, npc);
				if (skill != null)
					skill.useNoAnimationSkill();
			}
			case 217752 -> ThreadPoolManager.getInstance().schedule(() -> npc.getController().delete(), 15000);
			case 217607 -> startGolemSwitches(npc);
		}
		if (npc.getNpcId() == 217608 && dragonTimeoutTask == null) // S10_Dragon_55_Ah
			dragonTimeoutTask = ThreadPoolManager.getInstance().schedule(() -> {
				if (npc.isDead() || !npc.isSpawned())
					return;
				PacketSendUtility.broadcastMessage(npc, 341867);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_S_RANK_BATTLE_END());
				npc.queueSkill(19679, 41);
				ThreadPoolManager.getInstance().schedule(() -> npc.getController().delete(), 3000);
			}, 600000);
	}

	@Override
	public void onBackHome(Npc npc) {
		cancelStage1Timer(npc);
		bossGreeted.remove(npc.getObjectId());
	}

	@Override
	public void onSpawn(VisibleObject object) {
		if (!(object instanceof Npc npc))
			return;
		switch (npc.getNpcId()) {
			case 217751 -> ThreadPoolManager.getInstance().schedule(() -> npc.getController().delete(), 20000);
			case 217588, 217589 -> ThreadPoolManager.getInstance().schedule(() -> { // Kromede waits under Blessing of Rock
				Skill skill = SkillEngine.getInstance().getSkill(npc, 17052, 28, npc);
				if (skill != null && npc.isSpawned() && !npc.isDead())
					skill.useSkill();
			}, 1000);
			case 217752 -> {
				long now = System.currentTimeMillis();
				if (now - lastSerfShout > 2000) { // one shout per summoning
					lastSerfShout = now;
					Npc arminos = getNpc(217750);
					if (arminos != null)
						PacketSendUtility.broadcastMessage(arminos, 1500257);
				}
			}
		}
	}

	@Override
	public void onEndCastSkill(Skill skill) {
		if (skill.getSkillId() == 19643 && skill.getEffector() instanceof Npc npc && npc.getNpcId() == 217751)
			npc.getController().delete();
	}

	private void cancelStage1Timer(Npc npc) {
		Future<?> timer = stage1Timers.remove(npc.getObjectId());
		if (timer != null)
			timer.cancel(false);
	}

	private void startBonusStage2() {
		isBonusStage2Over = false;
		sp(217737, "empyrean_bonus_saam", 334.49496f, 349.2322f, 96.090935f, (byte) 0); // S2_SAAM1_55_n
		setStage(StageType.START_BONUS_STAGE_2, 0);
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S2_SAAM_CTRL_01());
		scheduleWhileAlive(217737, () -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S2_Saam1_01()), 40000);
		scheduleWhileAlive(217737, () -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S2_Saam1_02()), 60000);
		scheduleWhileAlive(217737, () -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S2_Saam1_03()), 65000);
		scheduleWhileAlive(217737, () -> {
			PacketSendUtility.broadcastMessage(getNpc(217737), 342353);
			endBonusStage2();
		}, 70000);
	}

	private synchronized void endBonusStage2() {
		if (isBonusStage2Over)
			return;
		isBonusStage2Over = true;
		deleteAliveNpcs(217737, 217738);
		spawnArenaRecordKeeper(799569); // IDArena_3rd_MC_Start
	}

	private void startBonusStage3() {
		sp(217742, FIGHTER_AI, 360.76f, 349.42f, 96.1f, (byte) 0); // S3_Elemeltal_Fire_55_Ah
		sp(217743, FIGHTER_AI, 346.27f, 363.35f, 96.1f, (byte) 11); // S3_Elemeltal_Wind_55_Ah
		sp(217740, FIGHTER_AI, 332.12f, 349.22f, 96.1f, (byte) 0); // S3_Elemeltal_Earth_55_Ah
		sp(217741, FIGHTER_AI, 346.42f, 335.1f, 96.1f, (byte) 87); // S3_Elemeltal_Warter_55_Ah
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S3_Bonus_01(), 30000);
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S3_Bonus_02(), 50000);
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S3_Bonus_03(), 55000);
		ThreadPoolManager.getInstance().schedule(() -> deleteAliveNpcs(217740, 217741, 217742, 217743), 60000);
		ThreadPoolManager.getInstance().schedule(() -> {
			deleteAliveNpcs(217744);
			spawnArenaRecordKeeper(205331); // IDArena_4th_MC_Start
			sp(217735, 378.9331f, 346.74878f, 96.74762f, (byte) 0, 1000); // S3_ResurBox1_55_n
		}, 63000);
	}

	private void startBonusStage4() {
		sp(217778, 346.2f, 366.85f, 96.55f, (byte) 1); // S4_Draky_CTRL_Gate
		AtomicInteger released = new AtomicInteger(), epicRolls = new AtomicInteger();
		AtomicBoolean epicReleased = new AtomicBoolean();
		bonusStage4Task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			int n = released.incrementAndGet();
			int npcId;
			if (Rnd.chance() < 25 && epicRolls.incrementAndGet() >= 3 && epicReleased.compareAndSet(false, true))
				npcId = 217748; // S4_Draky_Epic_55_Ae
			else if (Rnd.chance() < 20)
				npcId = 217745; // S4_Draky_Fast_55_An
			else if (Rnd.chance() < 40)
				npcId = 217746; // S4_Draky_Mid_55_An
			else
				npcId = 217747; // S4_Draky_slow_55_An
			sp(npcId, 346.2f, 366.85f, 96.09f, (byte) 60);
			switch (n) {
				case 20 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S4_Bonus_01());
				case 26 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S4_Bonus_02());
				case 28 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S4_Bonus_03());
				case 30 -> {
					bonusStage4Task.cancel(false);
					deleteAliveNpcs(217745, 217746, 217747, 217748, 217778);
					Npc arminos = getNpc(217749);
					if (arminos != null)
						PacketSendUtility.broadcastMessage(arminos, 1500255);
					ThreadPoolManager.getInstance().schedule(() -> {
						deleteAliveNpcs(217749);
						sp(205338, 345.25f, 349.24f, 96.09097f, (byte) 0, 1000); // IDArena_4th_MC_End
					}, 8000);
				}
			}
		}, 3000, 3000);
	}

	private void startBonusStage6(Npc arminos) {
		if (bonusStage6Task != null)
			return;
		PacketSendUtility.broadcastMessage(arminos, 1500256);
		scheduleWhileAlive(217750, () -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S6_Bonus_01()), 30000);
		scheduleWhileAlive(217750, () -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S6_Bonus_02()), 50000);
		scheduleWhileAlive(217750, () -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S6_Bonus_03()), 55000);
		bonusStage6Task = ThreadPoolManager.getInstance().schedule(() -> {
			if (arminos.isDead())
				return;
			PacketSendUtility.broadcastMessage(arminos, 1500258);
			arminos.getController().delete();
			deleteAliveNpcs(217751, 217752);
			sp(205340, 1625.08f, 159.15f, 126f, (byte) 70, 1000); // IDArena_6th_MC_End
		}, 60000);
	}

	private void startBonusStage9() {
		sp(217754, "empyrean_dusk_draky", 1308.9f, 1765.3f, 316.1f, (byte) 60, 8000); // S9_DuskDraky_55_Ah
		setStage(StageType.START_BONUS_STAGE_9, 8000);
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S9_DuskDraky_55_Ah_01(), 8000);
		scheduleWhileAlive(217754, () -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S9_Bonus_01()), 38000);
		scheduleWhileAlive(217754, () -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S9_Bonus_02()), 58000);
		scheduleWhileAlive(217754, () -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S9_Bonus_03()), 63000);
		bonusStage9Task = ThreadPoolManager.getInstance().schedule(() -> {
			if (!allDead(217754)) {
				deleteAliveNpcs(217754);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S9_DuskDraky_55_Ah_02());
			}
			endBonusStage9();
		}, 68000);
	}

	private synchronized void endBonusStage9() {
		if (bonusStage9Task == null)
			return;
		bonusStage9Task.cancel(false);
		bonusStage9Task = null;
		sp(205343, 1309.6390f, 1732.6423f, 316.5f, (byte) 6, 1000); // IDArena_9th_MC_End
	}

	private void startStage4Round(int round) {
		switch (round) {
			case 2 -> {
				setStage(StageType.START_STAGE_4_ROUND_2, 3000);
				sp(217560, 334.61942f, 334.80353f, 96.090904f, (byte) 15, 8000); // S5_Naga_Fi_55_Ae
				sp(217559, 330.53665f, 349.23523f, 96.09093f, (byte) 0, 8000); // S5_Lizard_Pr_55_Ae
				sp(217562, 334.89508f, 363.78442f, 96.090904f, (byte) 105, 8000); // S5_Naga_Wi_55_Ae
				sp(217557, 357.24625f, 338.30093f, 96.09104f, (byte) 45, 48000); // S5_Lizard_Fi_55_Ae
				sp(217561, 365.109f, 349.1218f, 96.09114f, (byte) 60, 48000); // S5_Naga_Ch_55_Ae
				sp(217558, 357.20663f, 359.28714f, 96.091064f, (byte) 75, 48000); // S5_Lizard_As_55_Ae
			}
			case 3 -> {
				setStage(StageType.START_STAGE_4_ROUND_3, 3000);
				sp(217563, 339.70975f, 333.54272f, 96.090904f, (byte) 20, 8000); // S5_Drakan_Fi_55_Ae
				sp(217564, 342.92892f, 333.43994f, 96.09092f, (byte) 18, 8000); // S5_Drakan_As_55_Ae
				sp(217565, 341.55396f, 330.70847f, 96.09093f, (byte) 23, 15000); // S5_Drakan_Wi_55_Ae
				sp(217566, 362.87164f, 357.87164f, 96.091125f, (byte) 73, 48000); // S5_Drakan_Pr_55_Ae
				sp(217563, 359.1135f, 359.6953f, 96.091125f, (byte) 80, 48000); // S5_Drakan_Fi_55_Ae
			}
			case 4 -> {
				setStage(StageType.START_STAGE_4_ROUND_4, 3000);
				sp(217567, 345.73895f, 349.49786f, 96.09097f, (byte) 0, 8000); // S5_Drakan_Fi_Boss_55_Ah
			}
			case 5 -> {
				setStage(StageType.START_STAGE_4_ROUND_5, 3000);
				sp(217653, 327.76917f, 349.26215f, 96.09092f, (byte) 0, 8000); // S5_Drop_Drakan_Wi_55_Ae
				sp(217651, 364.8972f, 349.25653f, 96.09114f, (byte) 60, 21000); // S5_Drop_Drakan_Fi_55_Ae
				sp(217652, 361.1795f, 339.99252f, 96.09112f, (byte) 50, 37000); // S5_Drop_Drakan_As_55_Ae
				sp(217653, 354.4119f, 333.6749f, 96.09091f, (byte) 40, 56000); // S5_Drop_Drakan_Wi_55_Ae
				sp(217651, 331.61502f, 358.4374f, 96.09091f, (byte) 110, 70000); // S5_Drop_Drakan_Fi_55_Ae
				sp(217652, 338.38858f, 364.91507f, 96.090904f, (byte) 100, 85000); // S5_Drop_Drakan_As_55_Ae
				sp(217651, 346.39847f, 368.19427f, 96.090904f, (byte) 90, 102000); // S5_Drop_Drakan_Fi_55_Ae
				sp(217652, 353.92606f, 364.92636f, 96.090904f, (byte) 80, 114000); // S5_Drop_Drakan_As_55_Ae
				sp(217653, 361.13452f, 358.90424f, 96.091156f, (byte) 70, 133000); // S5_Drop_Drakan_Wi_55_Ae
				sp(217652, 346.34402f, 329.9449f, 96.09091f, (byte) 30, 145000); // S5_Drop_Drakan_As_55_Ae
				sp(217654, 338.7f, 333.3f, 96.09091f, (byte) 20, 161000); // S5_Drop_Drakan_Pr_55_Ae
				sp(217653, 331.53894f, 339.8832f, 96.09091f, (byte) 10, 178000); // S5_Drop_Drakan_Wi_55_Ae
			}
		}
	}

	private void startStage2Round(int round) {
		StageType type = switch (round) {
			case 2 -> StageType.START_STAGE_2_ROUND_2;
			case 3 -> StageType.START_STAGE_2_ROUND_3;
			case 4 -> StageType.START_STAGE_2_ROUND_4;
			default -> StageType.START_STAGE_2_ROUND_5;
		};
		setStage(type, 3000);
		boolean k = isKrallSet;
		switch (round) {
			case 2 -> {
				sp(k ? 217495 : 217504, 328.908f, 351.618f, 96.09093f, (byte) 0, 8000); // S2_BrownieMSc_55_Ae / S2_RatmanScout_55_Ae
				sp(k ? 217497 : 217508, 329.019f, 343.793f, 96.09093f, (byte) 0, 8000); // S2_KrallWarrior_55_Ae / S2_LycanFighter_55_Ae
				sp(k ? 217499 : 217507, 329.285f, 355.231f, 96.09093f, (byte) 0, 8000); // S2_KrallScout_55_Ae / S2_LycanRanger_55_Ae
				sp(k ? 217494 : 217502, 328.784f, 348.774f, 96.09093f, (byte) 0, 8000); // S2_BrownieFWa_55_Ae / S2_RatmanWarrior_55_Ae
			}
			case 3 -> sp(k ? 217500 : 217509, 332.243f, 349.493f, 96.09093f, (byte) 0, 8000); // S2_KrallScout_MidBoss_55_Ah / S2_LycanRanger_Named_55_Ah
			case 4 -> {
				sp(k ? 217499 : 217505, 341.951f, 334.777f, 96.09093f, (byte) 0, 8000); // S2_KrallScout_55_Ae / S2_LycanShaman_55_Ae
				sp(k ? 217497 : 217508, 344.178f, 334.425f, 96.09093f, (byte) 0, 8000); // S2_KrallWarrior_55_Ae / S2_LycanFighter_55_Ae
				sp(k ? 217498 : 217508, 334.068f, 339.844f, 96.09093f, (byte) 0, 30000); // S2_KrallShaman_55_Ae / S2_LycanFighter_55_Ae
				sp(k ? 217497 : 217506, 342.124f, 364.492f, 96.09093f, (byte) 0, 35000); // S2_KrallWarrior_55_Ae / S2_LycanPriest_55_Ae
				sp(k ? 217498 : 217507, 344.495f, 365.144f, 96.09093f, (byte) 0, 35000); // S2_KrallShaman_55_Ae / S2_LycanRanger_55_Ae
			}
			case 5 -> sp(k ? 217501 : 217510, 332.004f, 349.559f, 96.09093f, (byte) 0, 8000); // S2_KrallWarrior_Boss_55_Ah / S2_LycanWizard_Boss_55_Ah
		}
	}

	private void startLehparRound(int round) {
		StageType type = switch (round) {
			case 2 -> StageType.START_STAGE_5_ROUND_2;
			case 3 -> StageType.START_STAGE_5_ROUND_3;
			case 4 -> StageType.START_STAGE_5_ROUND_4;
			default -> StageType.START_STAGE_5_ROUND_5;
		};
		setStage(type, 3000);
		switch (round) {
			case 2 -> {
				sp(217530, 1244.810f, 796.650f, 358.6056f, (byte) 0, 8000); // S4_hippolizard_work_55_Ae
				sp(217535, 1246.020f, 788.834f, 358.6056f, (byte) 12, 8000); // S4_LehparL_Ra_55_Ae
				sp(217530, 1255.182f, 783.183f, 358.6056f, (byte) 27, 8000); // S4_hippolizard_work_55_Ae
				sp(217531, 1244.836f, 791.915f, 358.6056f, (byte) 2, 38000); // S4_LehparL_Fi_55_Ae
				sp(217536, 1248.894f, 786.183f, 358.6056f, (byte) 20, 38000); // S4_LehparL_Pr_55_Ae
			}
			case 3 -> sp(217543, 1259.471f, 812.305f, 358.6056f, (byte) 30, 8000); // S4_Lehpar_Fi_MidBoss_55_Ah
			case 4 -> {
				sp(217533, 1258.720f, 792.232f, 358.6056f, (byte) 27, 8000); // S4_LehparL_As_55_Ae
				sp(217534, 1243.188f, 796.796f, 358.6056f, (byte) 0, 8000); // S4_LehparL_Wi_55_Ae
				sp(217535, 1272.299f, 797.088f, 358.6056f, (byte) 60, 8000); // S4_LehparL_Ra_55_Ae
				sp(217536, 1268.029f, 796.001f, 358.6056f, (byte) 60, 38000); // S4_LehparL_Pr_55_Ae
				sp(217532, 1245.805f, 801.722f, 358.6056f, (byte) 0, 38000); // S4_LehparL_Kn_55_Ae
			}
			case 5 -> sp(217544, "lehpar_captain", 1259.839f, 785.627f, 358.6056f, (byte) 30, 8000); // S4_Lehpar_Fi_Boss_55_Ah
		}
	}

	/**
	 * Ends the game once no group member in this world fights any more. Players who logged out inside still count.
	 * Departs from retail: retail runs this check before removing the leaving player, so the last fighter leaving the instance without
	 * the training exit button still counts as alive, and whoever waits in the resurrection area gets no result until they leave
	 * themselves. We leave the departing player out. To match retail, pass null on leave as well.
	 */
	private void checkAllDefeated(Player leaving) {
		for (int objectId : inWorld) {
			if (leaving != null && leaving.getObjectId() == objectId)
				continue;
			CruciblePlayerReward reward = getPlayerReward(objectId);
			if (reward != null && !reward.isPlayerDefeated() && !reward.isRewarded())
				return;
		}
		gameOver();
	}

	private synchronized void gameOver() {
		if (gameOver)
			return;
		gameOver = true;
		instanceScore.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
		instance.getPlayersInside().stream().filter(Player::isOnline).forEach(this::doReward);
		broadcastScore();
	}

	@Override
	public void doReward(Player player) {
		CruciblePlayerReward playerReward = getPlayerReward(player.getObjectId());
		if (playerReward == null || playerReward.isRewarded())
			return;
		int reward = (int) (0.01f * playerReward.getPoints() + 350);
		playerReward.setRewarded();
		playerReward.setInsignia(reward);
		ItemService.addItem(player, 186000130, reward);
	}

	@Override
	public void onInstanceDestroy() {
		super.onInstanceDestroy();
		npcs.clear();
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		super.onDie(player, lastAttacker);
		getPlayerReward(player.getObjectId()).setPlayerDefeated(true);
		checkAllDefeated(null);
		broadcastScore();
		return true;
	}

	@Override
	public boolean onReviveEvent(Player player) {
		super.onReviveEvent(player);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_MOVE_BIRTHAREA_ME_IDARENA());
		PacketSendUtility.broadcastPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_MOVE_BIRTHAREA_FRIENDLY_IDARENA(player.getName()));
		moveToReadyRoom(player);
		return true;
	}

	private Race getRegisteredTeamRace() {
		if (instance.getRegisteredTeam() != null) {
			return instance.getRegisteredTeam().getRace();
		}
		if (!instance.getPlayersInside().isEmpty()) {
			return instance.getPlayersInside().get(0).getRace();
		}
		return Race.ELYOS;
	}

	@Override
	public void onChangeStage(StageType type) {
		switch (type) {
			case START_STAGE_1_ELEVATOR -> {
				setStage(type, 0);
				setStage(StageType.START_STAGE_1_ROUND_1, 3000);
				stage = 1;
				Race race = getRegisteredTeamRace();
				stageKills.set(0);
				int npcId1 = race == Race.ASMODIANS ? 217486 : 217477; // S1_TempleD_Kn_55_Ae / S1_TempleL_Kn_55_Ae
				int npcId2 = race == Race.ASMODIANS ? 217489 : 217480; // S1_TempleD_Wi_55_Ae / S1_TempleL_Wi_55_Ae
				sp(npcId1, FIGHTER_AI, 327.73657f, 347.96228f, 96.09092f, (byte) 0, 8000);
				sp(npcId2, FIGHTER_AI, 327.81943f, 350.948f, 96.09093f, (byte) 0, 8000);
			}
			case START_STAGE_2_ELEVATOR -> {
				setStage(type, 0);
				deleteAliveNpcs(217734, 217756);
				stage = 2;
				setStage(StageType.START_STAGE_2_ROUND_1, 3000);
				isKrallSet = Rnd.nextBoolean();
				stageKills.set(0);
				sp(isKrallSet ? 217496 : 217503, 325.712f, 352.810f, 96.09093f, (byte) 0, 8000); // S2_BrownieMWo_55_Ae / S2_RatmanGatherer_55_Ae
				sp(isKrallSet ? 217495 : 217504, 325.061f, 349.478f, 96.09093f, (byte) 0, 8000); // S2_BrownieMSc_55_Ae / S2_RatmanScout_55_Ae
				sp(isKrallSet ? 217494 : 217502, 325.787f, 346.073f, 96.09093f, (byte) 0, 8000); // S2_BrownieFWa_55_Ae / S2_RatmanWarrior_55_Ae
			}
			case START_STAGE_3_ELEVATOR -> {
				setStage(type, 0);
				setStage(StageType.START_STAGE_3_ROUND_1, 3000);
				stage = 3;
				sp(217512, 344.23056f, 347.89594f, 96.09096f, (byte) 0, 8000); // S3_ElementalFire_1st_55_Ae
				sp(217513, 341.09082f, 337.95187f, 96.09097f, (byte) 0, 8000); // S3_ElementalWater_1st_55_Ae
				sp(217512, 342.06656f, 361.16135f, 96.090935f, (byte) 0, 8000); // S3_ElementalFire_1st_55_Ae
				sp(217511, 356.75006f, 335.27487f, 96.09096f, (byte) 0, 8000); // S3_ElementalEarth_1st_55_Ae
				sp(217514, 345.4355f, 365.05215f, 96.09093f, (byte) 0, 8000); // S3_ElementalAir_1st_55_Ae
				sp(217512, 352.8222f, 358.33463f, 96.09092f, (byte) 0, 8000); // S3_ElementalFire_1st_55_Ae
				sp(217513, 342.32755f, 365.00473f, 96.09093f, (byte) 0, 8000); // S3_ElementalWater_1st_55_Ae
				sp(217514, 356.19113f, 362.22543f, 96.090965f, (byte) 0, 8000); // S3_ElementalAir_1st_55_Ae
				sp(217511, 344.25127f, 334.1194f, 96.090935f, (byte) 0, 8000); // S3_ElementalEarth_1st_55_Ae
				sp(217511, 344.07086f, 346.8839f, 96.09092f, (byte) 0, 8000); // S3_ElementalEarth_1st_55_Ae
				sp(217514, 334.01746f, 350.76382f, 96.090935f, (byte) 0, 8000); // S3_ElementalAir_1st_55_Ae
				sp(217513, 344.49155f, 351.73932f, 96.09093f, (byte) 0, 8000); // S3_ElementalWater_1st_55_Ae
				sp(217513, 353.0832f, 362.178f, 96.09092f, (byte) 0, 8000); // S3_ElementalWater_1st_55_Ae
				sp(217511, 356.24454f, 358.34552f, 96.09103f, (byte) 0, 8000); // S3_ElementalEarth_1st_55_Ae
				sp(217512, 330.64853f, 346.87302f, 96.09091f, (byte) 0, 8000); // S3_ElementalFire_1st_55_Ae
				sp(217512, 353.32773f, 335.26398f, 96.09092f, (byte) 0, 8000); // S3_ElementalFire_1st_55_Ae
				sp(217514, 356.69666f, 339.1548f, 96.09103f, (byte) 0, 8000); // S3_ElementalAir_1st_55_Ae
				sp(217511, 347.6529f, 347.90683f, 96.09098f, (byte) 0, 8000); // S3_ElementalEarth_1st_55_Ae
				sp(217514, 347.5995f, 351.78674f, 96.09099f, (byte) 0, 8000); // S3_ElementalAir_1st_55_Ae
				sp(217512, 340.82983f, 334.1085f, 96.09093f, (byte) 0, 8000); // S3_ElementalFire_1st_55_Ae
				sp(217514, 344.19876f, 337.9993f, 96.09094f, (byte) 0, 8000); // S3_ElementalAir_1st_55_Ae
				sp(217513, 353.5887f, 339.10763f, 96.09092f, (byte) 0, 8000); // S3_ElementalWater_1st_55_Ae
				sp(217511, 345.4889f, 361.17224f, 96.090935f, (byte) 0, 8000); // S3_ElementalEarth_1st_55_Ae
				sp(217513, 330.90952f, 350.7164f, 96.09093f, (byte) 0, 8000); // S3_ElementalWater_1st_55_Ae
			}
			case START_STAGE_4_ELEVATOR -> {
				setStage(type, 0);
				deleteAliveNpcs(217735);
				setStage(StageType.START_STAGE_4_ROUND_1, 3000);
				stage = 4;
				stageKills.set(0);
				sp(217557, 328.88104f, 349.55392f, 96.090904f, (byte) 0, 8000); // S5_Lizard_Fi_55_Ae
				sp(217559, 328.38922f, 342.39066f, 96.09091f, (byte) 5, 8000); // S5_Lizard_Pr_55_Ae
				sp(217557, 333.17947f, 336.4504f, 96.090904f, (byte) 8, 8000); // S5_Lizard_Fi_55_Ae
				sp(217558, 330.27792f, 339.2779f, 96.09093f, (byte) 7, 38000); // S5_Lizard_As_55_Ae
				sp(217558, 328.08972f, 346.3553f, 96.090904f, (byte) 2, 38000); // S5_Lizard_As_55_Ae
			}
			case START_STAGE_5_ROUND_1 -> {
				setStage(type, 3000);
				stageKills.set(0);
				boolean isLehparSet = Rnd.get(1, 10) <= 3;
				if (isLehparSet) {
					sp(217529, 1257.307f, 778.350f, 358.6056f, (byte) 30, 8000); // S4_Pretor_55_Ae
					sp(217529, 1260.138f, 778.846f, 358.6056f, (byte) 30, 8000); // S4_Pretor_55_Ae
					sp(217529, 1263.199f, 778.413f, 358.6056f, (byte) 30, 8000); // S4_Pretor_55_Ae
					sp(217529, 1254.261f, 778.382f, 358.6056f, (byte) 30, 38000); // S4_Pretor_55_Ae
					sp(217529, 1266.293f, 778.325f, 358.6056f, (byte) 30, 38000); // S4_Pretor_55_Ae
				} else {
					sp(217549, 1263.1987f, 778.4129f, 358.6056f, (byte) 30, 8000); // S4_ShulackWi_55_Ae
					sp(217548, 1260.1381f, 778.84644f, 358.60562f, (byte) 30, 8000); // S4_ShulackRa_55_Ae
					sp(217547, 1257.3065f, 778.35016f, 358.60562f, (byte) 30, 8000); // S4_ShulackAs_55_Ae
					sp(217550, 1266.293f, 778.3254f, 358.60574f, (byte) 30, 38000); // S4_ShulackPr_55_Ae
					sp(217545, 1254.261f, 778.3817f, 358.6056f, (byte) 30, 38000); // S4_ShulackFi_55_Ae
				}
			}
			case START_STAGE_6_ROUND_1 -> {
				setStage(type, 3000);
				sp(217568, 1636.7102f, 166.87984f, 126f, (byte) 60, 8000); // S5_Graveknight_55_Ae
				sp(217568, 1636.6416f, 164.15344f, 126f, (byte) 60, 8000); // S5_Graveknight_55_Ae
				ThreadPoolManager.getInstance().schedule(() -> {
					sp(217568, 1638.7107f, 165.40533f, 126f, (byte) 60); // S5_Graveknight_55_Ae
					sp(217568, 1638.6783f, 162.67389f, 126f, (byte) 60); // S5_Graveknight_55_Ae
					isDoneStage6Round1 = true;
				}, 18000);
			}
			case START_STAGE_6_ROUND_5 -> setStage(type, 0);
			case START_STAGE_7_ROUND_1 -> {
				setStage(type, 3000);
				int npcId = getRegisteredTeamRace() == Race.ASMODIANS ? 217582 : 217578; // S7_MasterD_Wa_55_Ah / S7_MasterL_Wa_55_Ah
				sp(npcId, "warrior_preceptor", 1794.9f, 812.0f, 469.35f, (byte) 80, 8000);
			}
			case START_STAGE_8_ROUND_1 -> {
				setStage(type, 3000);
				sp(Rnd.nextBoolean() ? 217588 : 217589, 1776.578f, 1773.231f, 303.695f, (byte) 90, 8000); // S8_Cromede_55_Ah, S8_Cromede_Hard_55_Ah
			}
			case START_STAGE_9_ROUND_1 -> {
				setStage(type, 3000);
				sp(217594, 1274.890f, 1730.676f, 318.194f, (byte) 3, 8000); // S9_Potcrab_Gi_55_Ah
			}
			case START_STAGE_10_ROUND_1 -> {
				setStage(type, 3000);
				stageKills.set(0);
				sp(217600, 1769.255f, 1305.830f, 394.3f, (byte) 77, 8000); // S10_Naga_Fi_High_55_Ae
				sp(217602, 1773.790f, 1302.550f, 394.3f, (byte) 77, 8000); // S10_Naga_Pr_High_55_Ae
				sp(217601, 1777.328f, 1298.115f, 394.3f, (byte) 77, 8000); // S10_Naga_Ma_High_55_Ae
				sp(217600, 1781.384f, 1295.471f, 394.3f, (byte) 77, 38000); // S10_Naga_Fi_High_55_Ae
				sp(217600, 1764.249f, 1306.207f, 394.3f, (byte) 77, 38000); // S10_Naga_Fi_High_55_Ae
				sp(217602, 1772.114f, 1299.004f, 394.3f, (byte) 77, 70000); // S10_Naga_Pr_High_55_Ae
				sp(217601, 1775.701f, 1295.118f, 394.3f, (byte) 77, 70000); // S10_Naga_Ma_High_55_Ae
				sp(217600, 1767.737f, 1300.925f, 394.3f, (byte) 77, 70000); // S10_Naga_Fi_High_55_Ae
				sp(217600, 1763.040f, 1303.508f, 394.3f, (byte) 77, 90000); // S10_Naga_Fi_High_55_Ae
				sp(217600, 1780.297f, 1292.267f, 394.3f, (byte) 77, 90000); // S10_Naga_Fi_High_55_Ae
			}
			case START_STAGE_5 -> {
				stage = 5;
				sendScene(200000);
				sp(205426, 1256.2872f, 834.28986f, 358.60565f, (byte) 103); // IDArena_5th_Resurrect_NPC
				sp(205332, 1260.1292f, 795.06964f, 358.60562f, (byte) 30); // IDArena_5th_MC_Start
				moveGroupToStage();
				spawnPickups();
				setStage(type, 1000);
			}
			case START_STAGE_6 -> {
				stage = 6;
				sendScene(300000);
				sp(205427, 1594.4756f, 145.26898f, 128.67778f, (byte) 16); // IDArena_6th_Resurrect_NPC
				sp(205333, 1625.1771f, 159.15244f, 126f, (byte) 70); // IDArena_6th_MC_Start
				moveGroupToStage();
				spawnPickups();
				setStage(type, 1000);
			}
			case START_STAGE_7 -> {
				stage = 7;
				sendScene(400000);
				sp(205428, 1820.39f, 800.81805f, 470.1394f, (byte) 86); // IDArena_7th_Resurrect_NPC
				sp(205334, 1781.6106f, 796.9224f, 469.35016f, (byte) 0); // IDArena_7th_MC_Start
				moveGroupToStage();
				spawnPickups();
				setStage(type, 1000);
			}
			case START_STAGE_8 -> {
				stage = 8;
				sendScene(500000);
				sp(205429, 1780.103f, 1723.458f, 304.039f, (byte) 53); // IDArena_8th_Resurrect_NPC
				sp(205335, 1776.759f, 1764.705f, 303.695f, (byte) 90); // IDArena_8th_MC_Start
				moveGroupToStage();
				spawnPickups();
				setStage(type, 1000);
			}
			case START_STAGE_9 -> {
				stage = 9;
				sendScene(600000);
				sp(205430, 1359.375f, 1758.057f, 319.625f, (byte) 90); // IDArena_9th_Resurrect_NPC
				sp(205336, 1309.309f, 1732.540f, 315.782f, (byte) 7); // IDArena_9th_MC_Start
				moveGroupToStage();
				spawnPickups();
				setStage(type, 1000);
			}
			case START_STAGE_10 -> {
				stage = 10;
				sendScene(700000);
				sp(205431, 1750.81f, 1252.47f, 394.2378f, (byte) 45); // IDArena_10th_Resurrect_NPC
				sp(205337, "empyreanrecordkeeper", 1766.2f, 1292.0f, 394.23755f, (byte) 75, 1000); // IDArena_10th_MC_Start
				moveGroupToStage();
				spawnPickups();
				setStage(type, 1000);
			}
			case PASS_GROUP_STAGE_10 -> gameOver();
		}
	}

	private void advanceStage1(Npc npc) {
		if (!stage1Advanced.add(npc.getObjectId()))
			return;
		switch (stageKills.incrementAndGet()) {
			case 2 -> startStage1Round(2);
			case 3 -> startStage1Round(3);
			case 4 -> startStage1Round(4);
			case 5 -> startStage1Round(5);
		}
	}

	private void startStage1Round(int round) {
		boolean asmo = getRegisteredTeamRace() == Race.ASMODIANS;
		switch (round) {
			case 2 -> {
				setStage(StageType.START_STAGE_1_ROUND_2, 3000);
				sp(asmo ? 217492 : 217483, FIGHTER_AI, 332.7714f, 358.48206f, 96.09092f, (byte) 107, 8000); // S1_TempleD_Ch_55_Ae / S1_TempleL_Ch_55_Ae
			}
			case 3 -> {
				setStage(StageType.START_STAGE_1_ROUND_3, 3000);
				sp(asmo ? 217487 : 217478, FIGHTER_AI, 334.844f, 339.92618f, 96.09094f, (byte) 18, 8000); // S1_TempleD_As_55_Ae / S1_TempleL_As_55_Ae
			}
			case 4 -> {
				setStage(StageType.START_STAGE_1_ROUND_4, 3000);
				sp(asmo ? 217491 : 217482, FIGHTER_AI, 341.03156f, 361.04315f, 96.09093f, (byte) 90, 8000); // S1_TempleD_Pr_55_Ae / S1_TempleL_Pr_55_Ae
			}
			case 5 -> {
				setStage(StageType.START_STAGE_1_ROUND_5, 3000);
				sp(asmo ? 217493 : 217484, FIGHTER_AI, 332.093f, 349.36847f, 96.090935f, (byte) 0, 8000); // S1_TempleD_Fi_Boss_55_Ah / S1_TempleL_Fi_Boss_55_Ah
			}
		}
	}

	private boolean allDead(int... npcIds) {
		return instance.getNpcs(npcIds).stream().allMatch(Npc::isDead);
	}

	private void scheduleWhileAlive(int npcId, Runnable action, int delay) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!allDead(npcId))
				action.run();
		}, delay);
	}

	private void spawnArenaRecordKeeper(int npcId) {
		ThreadPoolManager.getInstance().schedule(() -> {
			sp(npcId, 345.25f, 349.24f, 96.09097f, (byte) 0);
			moveGroupToStage();
		}, 1000);
	}

	private void sendScene(int sceneStatus) {
		instance.forEachPlayer(player -> PacketSendUtility.sendPacket(player, new SM_INSTANCE_STAGE_INFO(2, sceneStatus & 0xFFFF, sceneStatus >>> 16)));
	}

	private void moveGroupToStage() {
		for (Player playerInside : instance.getPlayersInside())
			if (playerInside.isOnline())
				portToStartPosition(playerInside);
	}

	/**
	 * Fighters go to the arena of the current stage, everyone else to its waiting room.
	 */
	@Override
	public void portToStartPosition(Player player) {
		CruciblePlayerReward reward = getPlayerReward(player.getObjectId());
		if (reward == null)
			return;
		if (reward.isPlayerDefeated() || reward.isRewarded())
			moveToReadyRoom(player);
		else
			moveToStageStart(player);
		broadcastScore();
	}

	private void moveToStageStart(Player player) {
		switch (stage) {
			case 1, 2, 3, 4 -> teleport(player, 354.79f, 349.35f, 96.09097f, (byte) 60);
			case 5 -> teleport(player, 1260.15f, 812.34f, 358.6056f, (byte) 90);
			case 6 -> teleport(player, 1616.02f, 154.44f, 126f, (byte) 10);
			case 7 -> teleport(player, 1793.92f, 796.92f, 469.36542f, (byte) 60);
			case 8 -> teleport(player, 1776.79f, 1755.59f, 303.69553f, (byte) 30);
			case 9 -> teleport(player, 1320.45f, 1738.48f, 316.1746f, (byte) 67);
			case 10 -> teleport(player, 1762.43f, 1284.99f, 394.23764f, (byte) 15);
		}
	}

	private void moveToReadyRoom(Player player) {
		switch (stage) {
			case 1, 2, 3, 4 -> teleport(player, 383.54f, 349.19f, 96.74763f, (byte) 23);
			case 5 -> teleport(player, 1260.14f, 832.06f, 358.60562f, (byte) 43);
			case 6 -> teleport(player, 1596.60f, 148.47f, 128.81355f, (byte) 77);
			case 7 -> teleport(player, 1819.54f, 796.74f, 470.18304f, (byte) 27);
			case 8 -> teleport(player, 1776.38f, 1724.55f, 304.039f, (byte) 113);
			case 9 -> teleport(player, 1359.50f, 1751.80f, 319.59406f, (byte) 30);
			case 10 -> teleport(player, 1748.26f, 1254.75f, 394.2378f, (byte) 105);
		}
	}

	/**
	 * On each move to the next stage, the previous arena and waiting room get sensors and assistant record keepers that send stragglers
	 * after the group. They stay until the following move.
	 */
	private void spawnPickups() {
		deleteAliveNpcs(205506, 205507, 205508, 205509, 205510, 205511, 205512, 205513, 205514, 205515, 205516, 205517, 206185, 206186, 206187,
			206188, 206189, 206190, 206191, 206192, 206193, 206194, 206195, 206196);
		String ai = "empyrean_straggler_pickup";
		switch (stage) {
			case 5 -> {
				sp(206185, ai, 355.12f, 349.36f, 96.09097f, (byte) 0, 10000);
				sp(206191, ai, 384.09f, 349.47f, 96.74763f, (byte) 0, 10000);
				sp(205506, ai, 345.33f, 353.24f, 96.09097f, (byte) 0, 15000);
				sp(205512, ai, 384.10f, 349.06f, 96.74763f, (byte) 60, 15000);
			}
			case 6 -> {
				sp(206186, ai, 1261.01f, 812.38f, 358.6056f, (byte) 0, 10000);
				sp(206192, ai, 1261.07f, 831.68f, 358.6056f, (byte) 0, 10000);
				sp(205507, ai, 1256.14f, 795.12f, 358.6056f, (byte) 30, 15000);
				sp(205513, ai, 1261.11f, 835.70f, 358.6056f, (byte) 90, 15000);
			}
			case 7 -> {
				sp(206187, ai, 1615.20f, 153.83f, 126f, (byte) 0, 10000);
				sp(206193, ai, 1595.82f, 146.91f, 128.81355f, (byte) 0, 10000);
				sp(205508, ai, 1626.77f, 155.78f, 126f, (byte) 70, 15000);
				sp(205514, ai, 1590.20f, 147.73f, 128.81355f, (byte) 7, 15000);
			}
			case 8 -> {
				sp(206188, ai, 1793.57f, 797.87f, 469.35f, (byte) 0, 10000);
				sp(206194, ai, 1819.39f, 796.95f, 470.18304f, (byte) 0, 10000);
				sp(205509, ai, 1781.74f, 800.80f, 469.35f, (byte) 0, 15000);
				sp(205515, ai, 1822.52f, 797.05f, 470.18304f, (byte) 60, 15000);
			}
			case 9 -> {
				sp(206189, ai, 1777.44f, 1754.37f, 303.695f, (byte) 0, 10000);
				sp(206195, ai, 1776.32f, 1724.09f, 304.039f, (byte) 0, 10000);
				sp(205510, ai, 1780.47f, 1764.70f, 303.695f, (byte) 90, 15000);
				sp(205516, ai, 1776.04f, 1721.40f, 304.039f, (byte) 30, 15000);
			}
			case 10 -> {
				sp(206190, ai, 1321.10f, 1738.41f, 316.1746f, (byte) 0, 10000);
				sp(206196, ai, 1358.80f, 1750.57f, 319.59406f, (byte) 0, 10000);
				sp(205511, ai, 1308.18f, 1736.28f, 315.782f, (byte) 7, 15000);
				sp(205517, ai, 1363.10f, 1753.32f, 319.59406f, (byte) 67, 15000);
			}
		}
	}

	private void spawnLever(int npcId) {
		if (allDead(217553))
			return;
		switch (npcId) {
			case 700527 -> { // IDshulackship_Lever_A
				switch (Rnd.get(1, 3)) {
					case 1 -> sp(npcId, SUPPLY_AI, 1253.31f, 789.38f, 358.60562f, (byte) 119);
					case 2 -> sp(npcId, SUPPLY_AI, 1253.66f, 804.91f, 358.60562f, (byte) 119);
					default -> sp(npcId, SUPPLY_AI, 1269.83f, 796.32f, 358.60562f, (byte) 119);
				}
			}
			case 700528 -> { // IDshulackship_Lever_B
				switch (Rnd.get(1, 4)) {
					case 1 -> sp(npcId, SUPPLY_AI, 1248.94f, 800.99f, 358.6056f, (byte) 16);
					case 2 -> sp(npcId, SUPPLY_AI, 1268.03f, 789.64f, 358.6056f, (byte) 16);
					case 3 -> sp(npcId, SUPPLY_AI, 1264.92f, 804.84f, 358.6056f, (byte) 16);
					default -> sp(npcId, SUPPLY_AI, 1260.20f, 800.15f, 358.6056f, (byte) 16);
				}
			}
		}
	}

	private void startGolemSwitches(Npc golem) {
		if (golemTask != null)
			return;
		golemTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (golem.isDead() || !golem.isSpawned())
				return;
			float roll = Rnd.chance();
			Npc booster; // BIDLF1_WaterGolem1_Switch1
			if (roll < 30)
				booster = sp(700439, "noaction", 1788f, 1283f, 394.237f, (byte) 38);
			else if (roll < 51)
				booster = sp(700439, "noaction", 1747f, 1304f, 394.237f, (byte) 15);
			else
				booster = sp(700439, "noaction", 1754f, 1267f, 394.237f, (byte) 27);
			if (booster != null) {
				Skill mirror = SkillEngine.getInstance().getSkill(booster, 18553, 41, booster);
				if (mirror != null)
					mirror.useNoAnimationSkill();
			}
			golem.queueSkill(18110, 41);
			ThreadPoolManager.getInstance().schedule(() -> {
				if (!golem.isDead())
					golem.queueSkill(18112, 41, 0, NpcSkillTargetAttribute.RANDOM);
			}, 10000);
		}, 10000, 30000);
	}

	@Override
	public void leaveInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	@Override
	public void onLeaveInstance(Player player) {
		super.onLeaveInstance(player);
		checkAllDefeated(player);
		inWorld.remove(player.getObjectId());
	}

	@Override
	public void onStopTraining(Player player) {
		doReward(player);
		broadcastScore();
		instance.forEachPlayer(p -> PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_MSG_FRIENDLY_LEAVE_IDARENA(player.getName())));
	}

	private void sp(int npcId, float x, float y, float z, byte h, int time) {
		sp(npcId, null, x, y, z, h, time);
	}

	private void sp(int npcId, String aiName, float x, float y, float z, byte h, int time) {
		ThreadPoolManager.getInstance().schedule(() -> sp(npcId, aiName, x, y, z, h), time);
	}

	private Npc sp(int npcId, float x, float y, float z, byte h) {
		return sp(npcId, null, x, y, z, h);
	}

	private Npc sp(int npcId, String aiName, float x, float y, float z, byte h) {
		Npc npc = null;
		if (!isInstanceDestroyed) {
			npc = (Npc) spawn(npcId, x, y, z, h, aiName);
			synchronized (npcs) {
				npcs.add(npc);
			}
		}
		return npc;
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 217734, 217735, 217736, 217756 -> {
				ItemService.addItem(player, 186000124, 1);
				npc.getController().delete();
			}
			case 700527, 700528 -> { // two players pulled: feed or water comes out, the device comes back elsewhere while the giant lives
				sp(npc.getNpcId() == 700527 ? 281128 : 281129, "empyrean_manduri_food", npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				npc.getController().delete();
				ThreadPoolManager.getInstance().schedule(() -> spawnLever(npc.getNpcId()), Rnd.get(5000, 6000));
			}
		}
	}

	@Override
	public void onDropRegistered(Npc npc, int winnerObj) {
		Set<DropItem> dropItems = DropRegistrationService.getInstance().getCurrentDropMap().get(npc.getObjectId());
		int npcId = npc.getNpcId();
		int itemId = 0;
		switch (npcId) {
			case 217737 -> { // King Saam
				dropItems.clear();
				itemId = 188051349;
			}
			case 217738 -> {
				dropItems.clear();
				itemId = Rnd.chance() < 10 ? 188051349 : 162000107;
			}
			case 217740, 217741, 217742, 217743 -> {
				dropItems.clear();
				itemId = Rnd.chance() < 20 ? switch (npcId) {
					case 217740 -> 125002593;
					case 217741 -> 125002595;
					case 217742 -> 125002592;
					default -> 125002594;
				} : 162000108;
			}
			case 217748 -> { // epic drakie
				dropItems.clear();
				itemId = 186000096;
			}
			case 217750 -> {
				dropItems.clear();
				itemId = Rnd.chance() < 27 ? switch (Rnd.get(1, 9)) {
					case 1 -> 101700911;
					case 2 -> 100201003;
					case 3 -> 100900869;
					case 4 -> 100100874;
					case 5 -> 100500886;
					case 6 -> 101300836;
					case 7 -> 100600944;
					case 8 -> 100001135;
					default -> 101500895;
				} : 162000109;
			}
			case 217753, 217759 -> {
				dropItems.add(DropRegistrationService.getInstance().regDropItem(2, 0, npcId, 186000124, 1)); // worthiness ticket
				if (Rnd.chance() < 20) {
					Race race = getRegisteredTeamRace();
					itemId = switch (Rnd.get(1, 4)) {
						case 1 -> 169500935;
						case 2 -> 169500933;
						case 3 -> race == Race.ELYOS ? 169500947 : 169500951;
						default -> race == Race.ELYOS ? 169500927 : 169500931;
					};
				}
			}
			case 217754 -> {
				dropItems.clear();
				int roll = Rnd.get(1, 100);
				itemId = roll <= 20 ? 190000067 : roll <= 40 ? 160003573 : 164000160;
			}
		}
		if (itemId != 0)
			dropItems.add(DropRegistrationService.getInstance().regDropItem(1, 0, npcId, itemId, 1));
	}

	private static class ScoreWriter extends InstanceScoreWriter<InstanceScore<CruciblePlayerReward>> {

		private final Set<Integer> present;

		ScoreWriter(InstanceScore<CruciblePlayerReward> score, Set<Integer> present) {
			super(score);
			this.present = present;
		}

		@Override
		public void writeMe(ByteBuffer buf) {
			int playerCount = 0;
			for (CruciblePlayerReward reward : instanceScore.getPlayerRewards()) {
				if (playerCount == 6 || !present.contains(reward.getOwnerId()))
					continue;
				writeD(buf, reward.getOwnerId());
				writeD(buf, reward.getPoints());
				writeD(buf, reward.isRewarded() ? 3 : reward.isPlayerDefeated() ? 2 : 1);
				writeD(buf, reward.getInsignia());
				playerCount++;
			}
			if (playerCount < 6)
				writeB(buf, new byte[16 * (6 - playerCount)]);
		}
	}
}
