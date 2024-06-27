package instance.crucible;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.model.instance.playerreward.CruciblePlayerReward;
import com.aionemu.gameserver.network.aion.instanceinfo.CrucibleScoreWriter;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_STAGE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Luzien, w4terbomb
 */
@InstanceID(300300000)
public class EmpyreanCrucibleInstance extends CrucibleInstance {

	private final List<Npc> npcs = new ArrayList<>();
	private final List<EmpyreanStage> empyreanStage = new ArrayList<>();
	private byte stage;
	private boolean isDoneStage4, isDoneStage6Round2, isDoneStage6Round1;

	public EmpyreanCrucibleInstance(WorldMapInstance instance) {
		super(instance);
	}

	private class EmpyreanStage {

		private final List<Npc> stageNpcs;

		private EmpyreanStage(List<Npc> npcs) {
			this.stageNpcs = npcs;
		}

		public boolean containsNpcs() {
			return instance.getNpcs().stream().anyMatch(npc -> stageNpcs.stream().anyMatch(npc::equals));
		}

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
		if (stage > 0) {
			CruciblePlayerReward playerReward = getPlayerReward(player.getObjectId());
			if (isNew) {
				moveToReadyRoom(player);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ENTERED_BIRTHAREA_IDARENA());
			} else if (playerReward.isPlayerLeave()) {
				leaveInstance(player);
				return;
			} else if (playerReward.isRewarded()) {
				doReward(player);
				return;
			}
		}
		PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(instance.getMapId(), new CrucibleScoreWriter(instanceScore)));
		PacketSendUtility.sendPacket(player, new SM_INSTANCE_STAGE_INFO(2, stageType.getId(), stageType.getType()));
	}

	private void addPointsAndSendPacket(int points, Npc npc) {
		instance.forEachPlayer(player -> {
			if (player.isOnline()) {
				CruciblePlayerReward playerReward = getPlayerReward(player.getObjectId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(npc.getObjectTemplate().getL10n(), points));
				if (!playerReward.isRewarded())
					playerReward.addPoints(points);
				PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(instance.getMapId(), new CrucibleScoreWriter(instanceScore)));
			}
		});
	}

	private void setStage(StageType type, int time) {
		this.stageType = type;
		ThreadPoolManager.getInstance().schedule(
			() -> instance.forEachPlayer(player -> PacketSendUtility.sendPacket(player, new SM_INSTANCE_STAGE_INFO(2, type.getId(), type.getType()))),
			time);
	}

	@Override
	public void onDie(Npc npc) {
		synchronized (npcs) {
			npcs.remove(npc);
		}
		EmpyreanStage es = getEmpyreanStage(npc);
		int point = switch (npc.getNpcId()) {
			case 217511, 217512, 217513, 217514 -> 55;
			case 217515, 217516, 217517, 217518 -> 110;
			case 217520, 217522, 217567 -> 210;
			case 217560, 217561 -> 260;
			case 217557, 217558, 217559 -> 280;
			case 217504, 217507, 217508, 217569, 217545, 217547, 217548, 217549, 217550 -> 300;
			case 217502, 217505, 217506 -> 400;
			case 217568 -> 480;
			case 217519, 217521, 217523, 217524, 217525, 217526 -> 475;
			case 217652, 217653 -> 590;
			case 205396, 217477, 205405, 217486 -> 600;
			case 217651 -> 695;
			case 205399, 217480, 205408, 217489, 217503, 217529, 217484, 217493 -> 500;
			case 205402, 205411, 217483, 217492 -> 1100;
			case 205397, 205406, 217478, 217487 -> 1200;
			case 205401, 205410, 217482, 217491 -> 1300;
			case 217500, 217509, 217531, 217532, 217533, 217534, 217535, 217536, 217570, 217600, 217601, 217602 -> 1000;
			case 217530 -> 1000;
			case 217603, 217604, 217605, 217606 -> 2100;
			case 217578 -> 2900;
			case 217579 -> 3100;
			case 217581 -> 3400;
			case 217572 -> 3370;
			case 217527, 217528 -> 3250;
			case 217580 -> 3800;
			case 217588, 217589 -> 4000;
			case 217596, 217597 -> 4250;
			case 217590 -> 4200;
			case 217591 -> 5000;
			case 217501, 217510 -> 5500;
			case 217582, 217583, 217584, 217585, 217587 -> 4800;
			case 217592 -> 6000;
			case 217594 -> 6200;
			case 217595 -> 7300;
			case 217553, 217554, 217555 -> 2000;
			case 217552 -> 1900;
			case 217543, 217544 -> 2500;
			case 217556 -> 7800;
			case 217573 -> 8900;
			case 217598 -> 9400;
			case 217586 -> 12000;
			case 217593 -> 17000;
			case 217607 -> 17800;
			case 217599 -> 25900;
			case 217608 -> 192500;
			case 217609 -> 204400;
			default -> 0;
		};
		if (point != 0) {
			addPointsAndSendPacket(point, npc);
		}
		switch (npc.getNpcId()) {
			case 205396, 205405, 217477, 217486, 205399, 205408, 217480, 217489 -> {
				npc.getController().delete();
				if (stageType == StageType.START_STAGE_1_ROUND_1 && IntStream.of(205396, 205405, 217477, 217486, 205399, 205408, 217480, 217489).allMatch(
					i -> getNpcs(i).isEmpty())) {
					startStage1Round2();
				}
			}
			case 217483, 217492 -> {
				npc.getController().delete();
				if (stageType == StageType.START_STAGE_1_ROUND_2 && IntStream.of(217483, 217492).allMatch(i -> getNpcs(i).isEmpty())) {
					startStage1Round3();
				}
			}
			case 217478, 217487 -> {
				npc.getController().delete();
				if (stageType == StageType.START_STAGE_1_ROUND_3 && IntStream.of(217478, 217487).allMatch(i -> getNpcs(i).isEmpty())) {
					startStage1Round4();
				}
			}
			case 217482, 217491 -> {
				npc.getController().delete();
				if (stageType == StageType.START_STAGE_1_ROUND_4 && IntStream.of(217482, 217491).allMatch(i -> getNpcs(i).isEmpty())) {
					startStage1Round5();
				}
			}
			case 217484, 217493 -> {
				setStage(StageType.PASS_GROUP_STAGE_1, 0);
				sp(217756, 342.65106f, 357.4013f, 96.09094f, (byte) 0);
				sp(217756, 349.07376f, 357.5898f, 96.090965f, (byte) 0);
				sp(217756, 345.69272f, 359.40958f, 96.09094f, (byte) 0);
				sp(217756, 342.59192f, 353.73386f, 96.090965f, (byte) 0);
				sp(217756, 342.6043f, 360.8932f, 96.09093f, (byte) 0);
				sp(217756, 345.69318f, 355.56677f, 96.09094f, (byte) 0);
				sp(799568, 345.25f, 349.24f, 96.09097f, (byte) 0);
				npc.getController().delete();
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_COMPLETE_ROUND_IDARENA());
			}
			case 217502 -> {
				npc.getController().delete();
				switch (stageType) {
					case START_STAGE_2_ROUND_1 -> {
						if (IntStream.of(217503, 217504).allMatch(i -> getNpcs(i).isEmpty()))
							startStage2Round2();
					}
					case START_STAGE_2_ROUND_2 -> {
						if (IntStream.of(217508, 217507, 217504).allMatch(i -> getNpcs(i).isEmpty()))
							startStage2Round3();
					}
				}
			}
			case 217503 -> {
				npc.getController().delete();
				if (stageType == StageType.START_STAGE_2_ROUND_1 && IntStream.of(217502, 217504).allMatch(i -> getNpcs(i).isEmpty()))
					startStage2Round2();
			}
			case 217504 -> {
				npc.getController().delete();
				switch (stageType) {
					case START_STAGE_2_ROUND_1 -> {
						if (IntStream.of(217502, 217503).allMatch(i -> getNpcs(i).isEmpty()))
							startStage2Round2();
					}
					case START_STAGE_2_ROUND_2 -> {
						if (IntStream.of(217502, 217507, 217508).allMatch(i -> getNpcs(i).isEmpty()))
							startStage2Round3();
					}
				}
			}
			case 217507 -> {
				npc.getController().delete();
				switch (stageType) {
					case START_STAGE_2_ROUND_2 -> {
						if (IntStream.of(217502, 217504, 217508).allMatch(i -> getNpcs(i).isEmpty()))
							startStage2Round3();
					}
				}
				if (es != null && !es.containsNpcs()) {
					startStage2Round5();
				}
			}
			case 217508 -> {
				npc.getController().delete();
				if (es != null) {
					return;
				}
				switch (stageType) {
					case START_STAGE_2_ROUND_2 -> {
						if (IntStream.of(217502, 217504, 217507).allMatch(i -> getNpcs(i).isEmpty()))
							startStage2Round3();
					}
					case START_STAGE_2_ROUND_4 -> {
						if (getNpc(217505) == null)
							startStage4Round4_1();
					}
				}
			}
			case 217500, 217509 -> {
				npc.getController().delete();
				setStage(StageType.START_STAGE_2_ROUND_4, 2000);
				sp(217505, 341.95056f, 334.77692f, 96.09093f, (byte) 0, 6000);
				sp(217508, 344.17813f, 334.42462f, 96.090935f, (byte) 0, 6000);
			}
			case 217505 -> {
				npc.getController().delete();
				if (getNpc(217508) == null)
					startStage4Round4_1();
			}
			case 217506 -> {
				npc.getController().delete();
				if (es != null && !es.containsNpcs())
					startStage2Round5();
			}
			case 217510, 217501 -> {
				npc.getController().delete();
				setStage(StageType.PASS_GROUP_STAGE_2, 0);
				ThreadPoolManager.getInstance().schedule(() -> {
					sp(217737, 334.49496f, 349.2322f, 96.090935f, (byte) 0);
					setStage(StageType.START_BONUS_STAGE_2, 0);
					ThreadPoolManager.getInstance().schedule(() -> {
						if (getNpc(217737) != null) {
							sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S2_Saam1_01());
							ThreadPoolManager.getInstance().schedule(() -> {
								if (getNpc(217737) != null) {
									deleteAliveNpcs(217737);
									sp(799569, 345.25f, 349.24f, 96.09097f, (byte) 0);
								}
							}, 30000);
						}
					}, 30000);
				}, 8000);
			}
			case 217737 -> {
				npc.getController().delete();
				sp(799569, 345.25f, 349.24f, 96.09097f, (byte) 0);
			}
			case 217511, 217512, 217513, 217514 -> {
				npc.getController().delete();
				if (IntStream.of(217511, 217512, 217513, 217514).allMatch(id -> getNpcs(id).isEmpty())) {
					setStage(StageType.START_STAGE_3_ROUND_2, 2000);
					sp(217515, 336.32092f, 345.0251f, 96.090935f, (byte) 0, 6000);
					sp(217516, 347.16144f, 361.89084f, 96.09093f, (byte) 0, 6000);
					sp(217518, 352.77557f, 360.97845f, 96.09091f, (byte) 0, 6000);
					sp(217518, 340.2231f, 351.10208f, 96.09098f, (byte) 0, 6000);
					sp(217517, 354.132f, 337.14255f, 96.09089f, (byte) 0, 6000);
					sp(217517, 353.7888f, 354.4324f, 96.091064f, (byte) 0, 6000);
					sp(217516, 350.0108f, 342.09482f, 96.090935f, (byte) 0, 6000);
					sp(217515, 349.16327f, 335.63864f, 96.09095f, (byte) 0, 6000);
					sp(217517, 341.23633f, 344.55603f, 96.09096f, (byte) 0, 6000);
					sp(217518, 354.66513f, 343.31537f, 96.091095f, (byte) 0, 6000);
					sp(217516, 334.60898f, 352.01447f, 96.09095f, (byte) 0, 6000);
					sp(217515, 348.87338f, 354.90146f, 96.09096f, (byte) 0, 6000);
				}
			}
			case 217515, 217516, 217517, 217518 -> {
				npc.getController().delete();
				if (IntStream.of(217515, 217516, 217517, 217518).allMatch(i -> getNpcs(i).isEmpty())) {
					setStage(StageType.START_STAGE_3_ROUND_3, 2000);
					sp(217519, 351.08026f, 341.61298f, 96.090935f, (byte) 0, 6000);
					sp(217521, 333.4532f, 354.7357f, 96.09094f, (byte) 0, 6000);
					sp(217522, 342.1805f, 360.534f, 96.09092f, (byte) 0, 6000);
					sp(217520, 334.2686f, 342.60797f, 96.09091f, (byte) 0, 6000);
					sp(217522, 350.34537f, 356.18558f, 96.09094f, (byte) 0, 6000);
					sp(217520, 343.7485f, 336.2869f, 96.09092f, (byte) 0, 6000);
				}
			}
			case 217519, 217520, 217521, 217522 -> {
				npc.getController().delete();
				if (IntStream.of(217519, 217520, 217521, 217522).allMatch(i -> getNpcs(i).isEmpty())) {
					setStage(StageType.START_STAGE_3_ROUND_4, 2000);
					sp(217524, 349.66446f, 341.4752f, 96.090965f, (byte) 0, 6000);
					sp(217525, 338.32742f, 356.29636f, 96.090935f, (byte) 0, 6000);
					sp(217526, 349.31473f, 358.43762f, 96.09096f, (byte) 0, 6000);
					sp(217523, 338.73138f, 342.35876f, 96.09094f, (byte) 0, 6000);
				}
			}
			case 217523, 217524, 217525, 217526 -> {
				npc.getController().delete();
				if (IntStream.of(217523, 217524, 217525, 217526).allMatch(i -> getNpcs(i).isEmpty())) {
					setStage(StageType.START_STAGE_3_ROUND_5, 2000);
					sp(217527, 335.37524f, 346.34567f, 96.09094f, (byte) 0, 6000);
					sp(217528, 335.36105f, 353.16922f, 96.09094f, (byte) 0, 6000);
				}
			}
			case 217527, 217528 -> {
				npc.getController().delete();
				if (IntStream.of(217527, 217528).allMatch(id -> getNpcs(id).isEmpty())) {
					setStage(StageType.START_BONUS_STAGE_3, 7000);
					sp(217744, 342.45215f, 349.339f, 96.09096f, (byte) 0, 7000);
					ThreadPoolManager.getInstance().schedule(this::startBonusStage3, 39000);
				}
			}
			case 217557, 217559, 217562 -> {
				npc.getController().delete();
				switch (stageType) {
					case START_STAGE_4_ROUND_1 -> {
						if (IntStream.of(217557, 217559, 217562).allMatch(id -> getNpcs(id).isEmpty())) {
							sp(217558, 330.27792f, 339.2779f, 96.09093f, (byte) 6);
							sp(217558, 328.08972f, 346.3553f, 96.090904f, (byte) 1);
						}
					}
					case START_STAGE_4_ROUND_2 -> {
						if (es != null && !es.containsNpcs()) {
							startStage4Round3();
						}
					}
				}
			}
			case 217558, 217561 -> {
				npc.getController().delete();
				switch (stageType) {
					case START_STAGE_4_ROUND_1 -> {
						if (getNpcs(217558).isEmpty()) {
							setStage(StageType.START_STAGE_4_ROUND_2, 2000);
							sp(217559, 330.53665f, 349.23523f, 96.09093f, (byte) 0, 6000);
							sp(217562, 334.89508f, 363.78442f, 96.090904f, (byte) 105, 6000);
							sp(217560, 334.61942f, 334.80353f, 96.090904f, (byte) 15, 6000);
							ThreadPoolManager.getInstance().schedule(() -> {
								List<Npc> round = new ArrayList<>();
								round.add(sp(217557, 357.24625f, 338.30093f, 96.09104f, (byte) 65));
								round.add(sp(217558, 357.20663f, 359.28714f, 96.091064f, (byte) 75));
								round.add(sp(217561, 365.109f, 349.1218f, 96.09114f, (byte) 60));
								empyreanStage.add(new EmpyreanStage(round));
							}, 47000);
						}
					}
					case START_STAGE_4_ROUND_2 -> {
						if (es != null && !es.containsNpcs())
							startStage4Round3();
					}
				}
			}
			case 217563, 217566 -> {
				npc.getController().delete();
				if (es != null && !es.containsNpcs()) {
					setStage(StageType.START_STAGE_4_ROUND_4, 2000);
					sp(217567, 345.73895f, 349.49786f, 96.09097f, (byte) 0, 6000);
				}
			}
			case 217564, 217565, 217560, 217745, 217746, 217747, 217748, 205413, 205414, 217576, 217577 -> npc.getController().delete();
			case 217567 -> {
				npc.getController().delete();
				setStage(StageType.START_STAGE_4_ROUND_5, 2000);
				sp(217653, 327.76917f, 349.26215f, 96.09092f, (byte) 0, 6000);
				sp(217651, 364.8972f, 349.25653f, 96.09114f, (byte) 60, 18000);
				sp(217652, 361.1795f, 339.99252f, 96.09112f, (byte) 50, 35000);
				sp(217653, 354.4119f, 333.6749f, 96.09091f, (byte) 40, 54000);
				sp(217651, 331.61502f, 358.4374f, 96.09091f, (byte) 110, 69000);
				sp(217652, 338.38858f, 364.91507f, 96.090904f, (byte) 100, 83000);
				sp(217651, 346.39847f, 368.19427f, 96.090904f, (byte) 90, 99000);
				sp(217652, 353.92606f, 364.92636f, 96.090904f, (byte) 80, 110000);
				sp(217653, 361.13452f, 358.90424f, 96.091156f, (byte) 65, 130000);
				sp(217652, 346.34402f, 329.9449f, 96.09091f, (byte) 30, 142000);
				ThreadPoolManager.getInstance().schedule(() -> {
					sp(217653, 331.53894f, 339.8832f, 96.09091f, (byte) 10);
					isDoneStage4 = true;
				}, 174000);
			}
			case 217651, 217652, 217653 -> {
				npc.getController().delete();
				if (isDoneStage4 && IntStream.of(217651, 217652, 217653).allMatch(id -> getNpcs(id).isEmpty())) {
					setStage(StageType.PASS_GROUP_STAGE_4, 0);
					sp(217749, 340.59f, 349.32166f, 96.09096f, (byte) 0, 6000);
					setStage(StageType.START_BONUS_STAGE_4, 6000);
					ThreadPoolManager.getInstance().schedule(this::startBonusStage4, 33000);
				}
			}
			case 217547, 217548, 217549 -> {
				npc.getController().delete();
				if (IntStream.of(217547, 217548, 217549).allMatch(i -> getNpcs(i).isEmpty())) {
					sp(217550, 1266.293f, 778.3254f, 358.60574f, (byte) 30, 4000);
					sp(217545, 1254.261f, 778.3817f, 358.6056f, (byte) 30, 4000);
				}
			}
			case 217545, 217550 -> {
				npc.getController().delete();
				if (IntStream.of(217545, 217550).allMatch(i -> getNpcs(i).isEmpty())) {
					setStage(StageType.START_STAGE_5_ROUND_2, 2000);
					sp(217552, 1246.0197f, 788.8341f, 358.60556f, (byte) 11, 6000);
				}
			}
			case 217552 -> {
				npc.getController().delete();
				setStage(StageType.START_STAGE_5_ROUND_3, 2000);
				sp(700527, 1253.3123f, 789.38385f, 358.60562f, (byte) 119);
				sp(700528, 1260.2015f, 800.14886f, 358.6056f, (byte) 16);
				sp(217553, 1259.4706f, 812.30505f, 358.6056f, (byte) 30, 6000);
			}
			case 217553 -> {
				npc.getController().delete();
				deleteAliveNpcs(700527, 700528);
				sp(281111, 1246.4855f, 796.90735f, 358.6056f, (byte) 0);
				sp(281110, 1259.5508f, 784.5548f, 358.60562f, (byte) 0);
				sp(281112, 1276.6561f, 812.5499f, 358.60565f, (byte) 0);
				sp(281322, 1243.2113f, 813.0927f, 358.60565f, (byte) 0);
				sp(281109, 1272.9266f, 797.1055f, 358.60562f, (byte) 0);
				sp(281113, 1275.894f, 780.51544f, 358.60565f, (byte) 0);
				sp(281108, 1260.003f, 810.555f, 358.6056f, (byte) 0);
				sp(281114, 1244.3293f, 780.4284f, 358.60562f, (byte) 0);
				setStage(StageType.START_STAGE_5_ROUND_4, 2000);
				sp(217554, 1243.1877f, 796.79553f, 358.6056f, (byte) 0, 6000);
			}
			case 217554 -> {
				npc.getController().delete();
				deleteAliveNpcs(281108, 281109, 281110, 281111, 281112, 281113, 281114, 281322);
				setStage(StageType.START_STAGE_5_ROUND_5, 2000);
				sp(217556, 1259.8387f, 785.6266f, 358.60562f, (byte) 30, 6000);
				sp(281191, 1261.8f, 804.5f, 358.7f, (byte) 0, 6000);
				sp(281192, 1267.6f, 793.9f, 358.7f, (byte) 0, 6000);
				sp(281193, 1257.4f, 787.9f, 358.7f, (byte) 0, 6000);
				sp(281194, 1251.3f, 798.6f, 358.7f, (byte) 0, 6000);
			}
			case 217556 -> {
				npc.getController().delete();
				deleteAliveNpcs(281191, 281192, 281193, 281194);
				setStage(StageType.PASS_GROUP_STAGE_5, 0);
				sp(205339, 1260.1465f, 795.07495f, 358.60562f, (byte) 30);
			}
			case 217568 -> {
				sp(205413, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				npc.getController().delete();
				if (isDoneStage6Round1 && getNpcs(217568).isEmpty()) {
					setStage(StageType.START_STAGE_6_ROUND_2, 2000);
					sp(217570, 1629.4642f, 154.8044f, 126f, (byte) 30, 6000);
					sp(217569, 1643.7776f, 161.63562f, 126f, (byte) 46, 6000);
					sp(217569, 1639.7843f, 142.09268f, 126f, (byte) 40, 6000);
					ThreadPoolManager.getInstance().schedule(() -> {
						sp(217569, 1614.6377f, 164.04999f, 126.00113f, (byte) 3);
						sp(217569, 1625.8965f, 135.62509f, 126f, (byte) 30);
						isDoneStage6Round2 = true;
					}, 12000);
				}
			}
			case 217570 -> {
				for (int i = 0; i < 5; i++)
					sp(211984, npc.getX() + Rnd.get(-2, 2), npc.getY() + Rnd.get(-2, 2), npc.getZ(), npc.getHeading());
			}
			case 217569 -> {
				sp(205414, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				npc.getController().delete();
				if (stageType == StageType.START_STAGE_6_ROUND_2 && isDoneStage6Round2 && IntStream.of(217569, 217570).allMatch(i -> getNpcs(i).isEmpty())) {
					setStage(StageType.START_STAGE_6_ROUND_3, 2000);
					sp(217572, 1629.5837f, 138.38435f, 126f, (byte) 30, 9000);
					sp(217569, 1635.01535f, 150.01535f, 126f, (byte) 45, 6000);
					sp(217569, 1638.3817f, 152.84074f, 126f, (byte) 45, 6000);
				}
			}
			case 217572 -> {
				npc.getController().delete();
				setStage(StageType.START_STAGE_6_ROUND_4, 2000);
				sp(217573, 1634.7891f, 141.99077f, 126f, (byte) 0);
			}
			case 217573 -> {
				npc.getController().delete();
				setStage(StageType.PASS_GROUP_STAGE_6, 0);
				sp(217750, 1624.1908f, 155.16148f, 126f, (byte) 0, 8000);
				setStage(StageType.START_BONUS_STAGE_6, 8000);
			}
			case 217750 -> sp(205340, 1625.08f, 159.15f, 126f, (byte) 0);
			case 217582, 217578 -> {
				setStage(StageType.START_STAGE_7_ROUND_2, 2000);
				sp(217579, 1794.81f, 779.53925f, 469.35016f, (byte) 40, 6000);
			}
			case 217579, 217583 -> {
				setStage(StageType.START_STAGE_7_ROUND_3, 2000);
				Race race = getRegisteredTeamRace();
				switch (race) {
					case ASMODIANS -> sp(217580, 1775.6254f, 811.43225f, 469.35022f, (byte) 100, 6000);
					case ELYOS -> sp(217584, 1775.6254f, 811.43225f, 469.35022f, (byte) 100, 6000);
				}
			}
			case 217580, 217584 -> {
				setStage(StageType.START_STAGE_7_ROUND_4, 2000);
				Race race = getRegisteredTeamRace();
				switch (race) {
					case ASMODIANS -> sp(217581, 1775.716f, 779.630f, 469.564f, (byte) 20, 6000);
					case ELYOS -> sp(217585, 1775.716f, 779.630f, 469.564f, (byte) 20, 6000);
				}
			}
			case 217581, 217585 -> {
				setStage(StageType.START_STAGE_7_ROUND_5, 2000);
				Race race = getRegisteredTeamRace();
				switch (race) {
					case ASMODIANS -> sp(217586, 1773.194f, 796.537f, 469.350f, (byte) 0, 6000);
					case ELYOS -> sp(217587, 1773.194f, 796.537f, 469.350f, (byte) 0, 6000);
				}
			}
			case 217586, 217587 -> {
				setStage(StageType.PASS_GROUP_STAGE_7, 0);
				sp(217753, 1782.881f, 800.114f, 469.420f, (byte) 0, 2000);
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S7_BookBox_01());
				sp(205341, 1781.610f, 796.920f, 469.350f, (byte) 0, 2000);
			}
			case 217588, 217589 -> {
				setStage(StageType.START_STAGE_8_ROUND_2, 2000);
				sp(217590, 1764.377f, 1761.510f, 303.695f, (byte) 0, 6000);
			}
			case 217590 -> {
				setStage(StageType.START_STAGE_8_ROUND_3, 2000);
				sp(217591, 1776.946f, 1749.255f, 303.696f, (byte) 30, 6000);
			}
			case 217591 -> {
				setStage(StageType.START_STAGE_8_ROUND_4, 2000);
				sp(217592, 1790.693f, 1761.911f, 303.877f, (byte) 60, 6000);
			}
			case 217592 -> {
				setStage(StageType.START_STAGE_8_ROUND_5, 2000);
				sp(280790, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				sp(217593, 1777.065f, 1763.706f, 303.695f, (byte) 90, 6000);
			}
			case 217593 -> {
				setStage(StageType.PASS_GROUP_STAGE_8, 0);
				sp(205342, 1776.757f, 1764.624f, 303.695f, (byte) 90);
			}
			case 217594 -> {
				setStage(StageType.START_STAGE_9_ROUND_2, 2000);
				sp(217595, 1322.311f, 1741.508f, 316.349f, (byte) 65, 6000);
			}
			case 217595 -> {
				setStage(StageType.START_STAGE_9_ROUND_3, 2000);
				sp(217596, 1308.038f, 1729.718f, 315.996f, (byte) 36, 6000);
				sp(217597, 1302.290f, 1745.471f, 316.092f, (byte) 96, 6000);
			}
			case 217596, 217597 -> {
				Npc counterpart = getNpc(npc.getNpcId() == 217596 ? 217597 : 217596);
				if (counterpart != null && !counterpart.isDead())
					SkillEngine.getInstance().getSkill(counterpart, 19624, 10, counterpart).useNoAnimationSkill();
				npc.getController().delete();
				if (IntStream.of(217596, 217597).allMatch(i -> getNpcs(i).isEmpty())) {
					setStage(StageType.START_STAGE_9_ROUND_4, 2000);
					sp(217598, 1311.5238f, 1755.2079f, 317.1f, (byte) 97, 2000);
				}
			}
			case 217598 -> {
				npc.getController().delete();
				setStage(StageType.START_STAGE_9_ROUND_5, 2000);
				sp(217599, 1304.2659f, 1722.2467f, 316.5f, (byte) 23, 2000);
			}
			case 217599 -> {
				npc.getController().delete();
				setStage(StageType.PASS_GROUP_STAGE_9, 0);
				sp(205343, 1304.2659f, 1722.2467f, 316.5f, (byte) 8, 2000);
			}
			case 217600, 217601, 217602 -> {
				npc.getController().delete();
				if (IntStream.of(217600, 217601, 217602).allMatch(i -> getNpcs(i).isEmpty())) {
					setStage(StageType.START_STAGE_10_ROUND_2, 2000);
					sp(217603, 1744.6332f, 1280.0349f, 394.3f, (byte) 9, 2000);
					sp(217604, 1756.2661f, 1305.561f, 394.3f, (byte) 97, 6000);
					sp(217605, 1763.1177f, 1268.2404f, 394.3f, (byte) 22, 10000);
					sp(217606, 1765.2681f, 1306.5621f, 394.3f, (byte) 89, 14000);
				}
			}
			case 217603, 217604, 217605, 217606 -> {
				npc.getController().delete();
				if (IntStream.of(217603, 217604, 217605, 217606).allMatch(i -> getNpcs(i).isEmpty())) {
					setStage(StageType.START_STAGE_10_ROUND_3, 2000);
					sp(700441, 1742.39f, 1289.59f, 394.237f, (byte) 68, 2000);
					sp(700441, 1782.32f, 1272.74f, 394.237f, (byte) 94, 2000);
					sp(217607, 1769.9637f, 1297.393f, 394.237f, (byte) 82, 2000);
				}
			}
			case 217607 -> {
				npc.getController().delete();
				deleteAliveNpcs(700441);
				setStage(StageType.START_STAGE_10_ROUND_4, 2000);
				sp(217608, 1769.9637f, 1297.393f, 394.237f, (byte) 82, 2000);
			}
			case 217608 -> {
				npc.getController().delete();
				setStage(StageType.START_STAGE_10_ROUND_5, 2000);
				sp(217609, 1765.6692f, 1288.092f, 394.3f, (byte) 82, 2000);
			}
			case 217609 -> {
				npc.getController().delete();
				setStage(StageType.PASS_GROUP_STAGE_10, 0);
				sp(205344, 1764.6368f, 1288.831f, 394.23755f, (byte) 77);
			}
		}
	}

	private void startBonusStage3() {
		sp(217742, 360.76f, 349.42f, 96.1f, (byte) 0);
		sp(217743, 346.27f, 363.35f, 96.1f, (byte) 11);
		sp(217740, 332.12f, 349.22f, 96.1f, (byte) 0);
		sp(217741, 346.42f, 335.1f, 96.1f, (byte) 87);
		ThreadPoolManager.getInstance().schedule(() -> {
			sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S3_Bonus_01());
			ThreadPoolManager.getInstance().schedule(() -> {
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S3_Bonus_02());
				ThreadPoolManager.getInstance().schedule(() -> {
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDArena_S3_Bonus_03());
					ThreadPoolManager.getInstance().schedule(() -> {
						deleteAliveNpcs(217740, 217741, 217742, 217743, 217744);
						sp(205331, 345.25f, 349.24f, 96.09097f, (byte) 0);
						sp(217735, 378.9331f, 346.74878f, 96.74762f, (byte) 0);
						sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_S3_ResurBox1_01());
					}, 5000);
				}, 5000);
			}, 20000);
		}, 30000);
	}

	private void startBonusStage4() {
		sp(217778, 346.2f, 366.85f, 96.55f, (byte) 1);
		sp(217747, 346.2204f, 367.52002f, 96.090904f, (byte) 60, 3000);
		sp(217747, 346.2204f, 367.52002f, 96.090904f, (byte) 60, 6000);
		sp(217748, 346.65222f, 366.3634f, 96.09092f, (byte) 60, 9000);
		sp(217748, 346.65222f, 366.3634f, 96.09092f, (byte) 60, 12000);
		sp(217748, 345.7578f, 366.7986f, 96.09094f, (byte) 60, 15000);
		sp(217748, 345.863f, 367.471f, 96.09094f, (byte) 60, 18000);
		sp(217747, 346.9996f, 366.3978f, 96.09092f, (byte) 60, 21000);
		sp(217746, 345.7872f, 366.3056f, 96.09092f, (byte) 60, 24000);
		sp(217745, 346.4504f, 367.8004f, 96.09094f, (byte) 60, 27000);
		sp(217747, 346.75043f, 367.467f, 96.09094f, (byte) 60, 30000);
		sp(217747, 346.535f, 367.3128f, 96.090904f, (byte) 60, 33000);
		sp(217747, 345.8452f, 367.2468f, 96.09092f, (byte) 60, 36000);
		sp(217746, 345.428f, 366.2954f, 96.09093f, (byte) 60, 39000);
		sp(217747, 346.71082f, 366.7156f, 96.090904f, (byte) 60, 42000);
		sp(217747, 346.38782f, 366.1606f, 96.09093f, (byte) 60, 45000);
		sp(217747, 345.36002f, 366.0284f, 96.09093f, (byte) 60, 48000);
		sp(217747, 345.5378f, 366.1876f, 96.09092f, (byte) 60, 51000);
		sp(217747, 346.5176f, 365.8592f, 96.09092f, (byte) 60, 54000);
		sp(217745, 345.8434f, 367.8082f, 96.090904f, (byte) 60, 57000);
		sp(217747, 345.297f, 366.3014f, 96.09093f, (byte) 60, 60000);
		sp(217747, 346.0346f, 367.2426f, 96.090904f, (byte) 60, 63000);
		sp(217747, 345.52863f, 366.62622f, 96.09092f, (byte) 60, 66000);
		sp(217745, 345.80862f, 366.9388f, 96.09092f, (byte) 60, 69000);
		sp(217747, 346.393f, 366.9766f, 96.090904f, (byte) 60, 72000);
		sp(217746, 345.5726f, 366.3462f, 96.09092f, (byte) 60, 75000);
		sp(217745, 345.2004f, 366.36902f, 96.09092f, (byte) 60, 78000);
		sp(217746, 346.2528f, 365.9208f, 96.09093f, (byte) 60, 81000);
		sp(217747, 346.0686f, 366.9096f, 96.090904f, (byte) 60, 84000);
		sp(217746, 345.4606f, 367.14862f, 96.090904f, (byte) 60, 87000);
		sp(217747, 345.8016f, 367.7212f, 96.090904f, (byte) 60, 90000);
		sp(217747, 347.1144f, 365.875f, 96.09092f, (byte) 60, 93000);
		sp(217747, 345.3226f, 367.7414f, 96.0909f, (byte) 60, 96000);
		sp(217747, 345.4836f, 367.3886f, 96.090904f, (byte) 60, 99000);
		sp(217747, 345.80862f, 366.0682f, 96.09092f, (byte) 60, 102000);
		ThreadPoolManager.getInstance().schedule(() -> {
			deleteAliveNpcs(217745, 217746, 217747, 217748, 217749, 217778);
			sp(205338, 345.25f, 349.24f, 96.09097f, (byte) 0);
		}, 102000);
	}

	private void startStage4Round4_1() {
		List<Npc> round = new ArrayList<>();
		round.add(sp(217508, 334.06754f, 339.84393f, 96.09091f, (byte) 0));
		empyreanStage.add(new EmpyreanStage(round));
		ThreadPoolManager.getInstance().schedule(() -> {
			List<Npc> round1 = new ArrayList<>();
			round1.add(sp(217506, 342.12405f, 364.4922f, 96.09093f, (byte) 0));
			round1.add(sp(217507, 344.4953f, 365.14444f, 96.09092f, (byte) 0));
			empyreanStage.add(new EmpyreanStage(round1));
		}, 5000);
	}

	private void startStage4Round3() {
		setStage(StageType.START_STAGE_4_ROUND_3, 2000);
		sp(217563, 339.70975f, 333.54272f, 96.090904f, (byte) 20, 6000);
		sp(217564, 342.92892f, 333.43994f, 96.09092f, (byte) 18, 6000);
		sp(217565, 341.55396f, 330.70847f, 96.09093f, (byte) 23, 12000);
		ThreadPoolManager.getInstance().schedule(() -> {
			List<Npc> round = new ArrayList<>();
			round.add(sp(217566, 362.87164f, 357.87164f, 96.091125f, (byte) 73));
			round.add(sp(217563, 359.1135f, 359.6953f, 96.091125f, (byte) 80));
			empyreanStage.add(new EmpyreanStage(round));
		}, 43000);
	}

	private void startStage2Round2() {
		setStage(StageType.START_STAGE_2_ROUND_2, 2000);
		sp(217502, 328.78433f, 348.77353f, 96.09092f, (byte) 0, 6000);
		sp(217508, 329.01874f, 343.79257f, 96.09092f, (byte) 0, 6000);
		sp(217507, 329.2849f, 355.2314f, 96.090935f, (byte) 0, 6000);
		sp(217504, 328.90808f, 351.6184f, 96.09092f, (byte) 0, 6000);
	}

	private void startStage2Round3() {
		setStage(StageType.START_STAGE_2_ROUND_3, 2000);
		sp(Rnd.get(1, 2) == 1 ? 217500 : 217509, 332.0035f, 349.55893f, 96.09093f, (byte) 0, 6000);
	}

	private void startStage2Round5() {
		setStage(StageType.START_STAGE_2_ROUND_5, 2000);
		sp(Rnd.get(1, 2) == 1 ? 217510 : 217501, 332.0035f, 349.55893f, 96.09093f, (byte) 0, 6000);
	}

	private void rewardGroup() {
		instance.getPlayersInside().forEach(this::doReward);
	}

	@Override
	public void doReward(Player player) {
		CruciblePlayerReward playerReward = getPlayerReward(player.getObjectId());
		if (playerReward == null)
			return;
		float reward = 0.01f * playerReward.getPoints() + 350;
		if (!playerReward.isRewarded()) {
			playerReward.setRewarded();
			playerReward.setInsignia((int) reward);
			ItemService.addItem(player, 186000130, (int) reward);
		} else {
			TeleportService.teleportTo(player, mapId, player.getInstanceId(), 381.41684f, 346.78162f, 96.74763f, (byte) 43);
		}
		instanceScore.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
		PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(instance.getMapId(), new CrucibleScoreWriter(instanceScore)));
	}

	@Override
	public void onInstanceDestroy() {
		super.onInstanceDestroy();
		npcs.clear();
		empyreanStage.clear();
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		super.onDie(player, lastAttacker);
		getPlayerReward(player.getObjectId()).setPlayerDefeated(true);
		boolean defeat = true;
		for (Player p : instance.getPlayersInside())
			if (!getPlayerReward(p.getObjectId()).isPlayerDefeated()) {
				defeat = false;
				break;
			}
		if (defeat)
			rewardGroup();
		return true;
	}

	@Override
	public boolean onReviveEvent(Player player) {
		super.onReviveEvent(player);
		moveToReadyRoom(player);
		instance.forEachPlayer(p -> {
			PacketSendUtility.sendPacket(p, player.equals(p) ? SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_MOVE_BIRTHAREA_ME_IDARENA()
				: SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_MOVE_BIRTHAREA_FRIENDLY_IDARENA(player.getName()));
		});
		return true;
	}

	private EmpyreanStage getEmpyreanStage(Npc npc) {
		return empyreanStage.stream().filter(es -> es.stageNpcs.contains(npc)).findFirst().orElse(null);
	}

	private boolean isSpawn(List<Integer> round) {
		return npcs.stream().noneMatch(n -> round.contains(n.getNpcId()));
	}

	private Race getRegisteredTeamRace() {
		return instance.getRegisteredTeam().getRace();
	}

	@Override
	public void onChangeStage(StageType type) {
		switch (type) {
			case START_STAGE_1_ELEVATOR -> {
				setStage(type, 0);
				setStage(StageType.START_STAGE_1_ROUND_1, 5000);
				stage = 1;
				Race race = getRegisteredTeamRace();
				List<Integer> round = new ArrayList<>();
				switch (race) {
					case ASMODIANS -> {
						round.add(217486);
						round.add(217489);
						sp(217486, 327.73657f, 347.96228f, 96.09092f, (byte) 0, 9000);
						sp(217489, 327.81943f, 350.948f, 96.09093f, (byte) 0, 9000);
					}
					case ELYOS -> {
						round.add(217477);
						round.add(217480);
						sp(217477, 327.73657f, 347.96228f, 96.09092f, (byte) 0, 9000);
						sp(217480, 327.81943f, 350.948f, 96.09093f, (byte) 0, 9000);
					}
				}
				ThreadPoolManager.getInstance().schedule(() -> {
					if (!isSpawn(round)) {
						startStage1Round2();
					}
					round.clear();
				}, 62000);
			}
			case START_STAGE_2_ELEVATOR -> {
				setStage(type, 0);
				deleteAliveNpcs(217756);
				stage = 2;
				setStage(StageType.START_STAGE_2_ROUND_1, 5000);
				sp(217503, 325.71194f, 352.81027f, 96.09092f, (byte) 0, 9000);
				sp(217502, 325.78696f, 346.07263f, 96.090904f, (byte) 0, 9000);
				sp(217504, 325.06122f, 349.4784f, 96.090904f, (byte) 0, 9000);
			}
			case START_STAGE_3_ELEVATOR -> {
				setStage(type, 0);
				setStage(StageType.START_STAGE_3_ROUND_1, 5000);
				stage = 3;
				sp(217512, 344.23056f, 347.89594f, 96.09096f, (byte) 0, 9000);
				sp(217513, 341.09082f, 337.95187f, 96.09097f, (byte) 0, 9000);
				sp(217512, 342.06656f, 361.16135f, 96.090935f, (byte) 0, 9000);
				sp(217511, 356.75006f, 335.27487f, 96.09096f, (byte) 0, 9000);
				sp(217514, 345.4355f, 365.05215f, 96.09093f, (byte) 0, 9000);
				sp(217512, 352.8222f, 358.33463f, 96.09092f, (byte) 0, 9000);
				sp(217513, 342.32755f, 365.00473f, 96.09093f, (byte) 0, 9000);
				sp(217514, 356.19113f, 362.22543f, 96.090965f, (byte) 0, 9000);
				sp(217511, 344.25127f, 334.1194f, 96.090935f, (byte) 0, 9000);
				sp(217511, 344.07086f, 346.8839f, 96.09092f, (byte) 0, 9000);
				sp(217514, 334.01746f, 350.76382f, 96.090935f, (byte) 0, 9000);
				sp(217513, 344.49155f, 351.73932f, 96.09093f, (byte) 0, 9000);
				sp(217513, 353.0832f, 362.178f, 96.09092f, (byte) 0, 9000);
				sp(217511, 356.24454f, 358.34552f, 96.09103f, (byte) 0, 9000);
				sp(217512, 330.64853f, 346.87302f, 96.09091f, (byte) 0, 9000);
				sp(217512, 353.32773f, 335.26398f, 96.09092f, (byte) 0, 9000);
				sp(217514, 356.69666f, 339.1548f, 96.09103f, (byte) 0, 9000);
				sp(217511, 347.6529f, 347.90683f, 96.09098f, (byte) 0, 9000);
				sp(217514, 347.5995f, 351.78674f, 96.09099f, (byte) 0, 9000);
				sp(217512, 340.82983f, 334.1085f, 96.09093f, (byte) 0, 9000);
				sp(217514, 344.19876f, 337.9993f, 96.09094f, (byte) 0, 9000);
				sp(217513, 353.5887f, 339.10763f, 96.09092f, (byte) 0, 9000);
				sp(217511, 345.4889f, 361.17224f, 96.090935f, (byte) 0, 9000);
				sp(217513, 330.90952f, 350.7164f, 96.09093f, (byte) 0, 9000);
			}
			case START_STAGE_4_ELEVATOR -> {
				setStage(type, 0);
				deleteAliveNpcs(217735);
				setStage(StageType.START_STAGE_4_ROUND_1, 5000);
				stage = 4;
				sp(217557, 328.88104f, 349.55392f, 96.090904f, (byte) 0, 9000);
				sp(217559, 328.38922f, 342.39066f, 96.09091f, (byte) 5, 9000);
				sp(217557, 333.17947f, 336.4504f, 96.090904f, (byte) 8, 9000);
			}
			case START_STAGE_5_ROUND_1 -> {
				setStage(type, 2000);
				sp(217549, 1263.1987f, 778.4129f, 358.6056f, (byte) 30, 6000);
				sp(217548, 1260.1381f, 778.84644f, 358.60562f, (byte) 30, 6000);
				sp(217547, 1257.3065f, 778.35016f, 358.60562f, (byte) 30, 6000);
			}
			case START_STAGE_6_ROUND_1 -> {
				setStage(type, 2000);
				sp(217568, 1636.7102f, 166.87984f, 126f, (byte) 60, 6000);
				sp(217568, 1619.4432f, 153.83188f, 126f, (byte) 60, 6000);
				sp(217568, 1636.6416f, 164.15344f, 126f, (byte) 60, 6000);
				ThreadPoolManager.getInstance().schedule(() -> {
					sp(217568, 1638.7107f, 165.40533f, 126f, (byte) 60);
					sp(217568, 1638.6783f, 162.67389f, 126f, (byte) 60);
					isDoneStage6Round1 = true;
				}, 12000);
			}
			case START_STAGE_6_ROUND_5 -> setStage(type, 0);
			case START_STAGE_7_ROUND_1 -> {
				setStage(type, 5000);
				Race race = getRegisteredTeamRace();
				switch (race) {
					case ASMODIANS -> sp(217578, 1783.0873f, 796.8426f, 469.35013f, (byte) 0, 6000);
					case ELYOS -> sp(217582, 1783.0873f, 796.8426f, 469.35013f, (byte) 0, 6000);
				}
			}
			case START_STAGE_8_ROUND_1 -> {
				setStage(type, 2000);
				sp(Rnd.get(1, 2) == 1 ? 217588 : 217589, 1776.578f, 1773.231f, 303.695f, (byte) 90, 6000);
			}
			case START_STAGE_9_ROUND_1 -> {
				setStage(type, 2000);
				sp(217594, 1274.890f, 1730.676f, 318.194f, (byte) 3, 6000);
			}
			case START_STAGE_10_ROUND_1 -> {
				setStage(type, 2000);
				sp(217600, 1765.1488f, 1305.1216f, 394.3f, (byte) 84, 2000);
				sp(217601, 1776.6201f, 1296.9429f, 394.2375f, (byte) 74, 2000);
				sp(217602, 1771.5571f, 1302.0781f, 394.3f, (byte) 82, 2000);
			}
			case START_STAGE_5 -> {
				stage = 5;
				sp(205426, 1256.2872f, 834.28986f, 358.60565f, (byte) 103);
				sp(205332, 1260.1292f, 795.06964f, 358.60562f, (byte) 30);
				teleport(1260.15f, 812.34f, 358.6056f, (byte) 90);
				setStage(type, 1000);
			}
			case START_STAGE_6 -> {
				stage = 6;
				sp(205427, 1594.4756f, 145.26898f, 128.67778f, (byte) 16);
				sp(205333, 1625.1771f, 159.15244f, 126f, (byte) 70);
				teleport(1616.0248f, 154.43837f, 126f, (byte) 10);
				setStage(type, 1000);
			}
			case START_STAGE_7 -> {
				stage = 7;
				sp(205428, 1820.39f, 800.81805f, 470.1394f, (byte) 86);
				sp(205334, 1781.6106f, 796.9224f, 469.35016f, (byte) 0);
				teleport(1793.9233f, 796.92f, 469.36542f, (byte) 60);
				setStage(type, 1000);
			}
			case START_STAGE_8 -> {
				stage = 8;
				sp(205335, 1776.759f, 1764.705f, 303.695f, (byte) 90);
				sp(205429, 1780.103f, 1723.458f, 304.039f, (byte) 53);
				teleport(1776.4169f, 1749.9952f, 303.69553f, (byte) 0); // get retail
				setStage(type, 1000);
			}
			case START_STAGE_9 -> {
				stage = 9;
				sp(205430, 1359.375f, 1758.057f, 319.625f, (byte) 90);
				sp(205336, 1309.309f, 1732.540f, 315.782f, (byte) 7);
				teleport(1320.4513f, 1738.4838f, 316.1746f, (byte) 66);
				setStage(type, 1000);
			}
			case START_STAGE_10 -> {
				stage = 10;
				sp(205431, 1755.709f, 1253.4136f, 394.2378f, (byte) 33);
				sp(205337, 1766.5986f, 1291.2572f, 394.23755f, (byte) 82, 10000);
				teleport(1760.9441f, 1278.033f, 394.23764f, (byte) 0);
				setStage(type, 2000);
			}
		}
	}

	private void startStage1Round2() {
		setStage(StageType.START_STAGE_1_ROUND_2, 2000);
		Race race = getRegisteredTeamRace();
		List<Integer> round = new ArrayList<>();
		switch (race) {
			case ASMODIANS -> {
				round.add(217492);
				sp(217492, 332.7714f, 358.48206f, 96.09092f, (byte) 106, 6000);
			}
			case ELYOS -> {
				round.add(217483);
				sp(217483, 332.7714f, 358.48206f, 96.09092f, (byte) 106, 6000);
			}
		}
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isSpawn(round))
				startStage1Round3();
			round.clear();
		}, 59000);
	}

	private void startStage1Round3() {
		setStage(StageType.START_STAGE_1_ROUND_3, 2000);
		List<Integer> round = new ArrayList<>();
		Race race = getRegisteredTeamRace();
		switch (race) {
			case ASMODIANS -> {
				round.add(217487);
				sp(217487, 334.844f, 339.92618f, 96.09094f, (byte) 106, 6000);
			}
			case ELYOS -> {
				round.add(217478);
				sp(217478, 334.844f, 339.92618f, 96.09094f, (byte) 106, 6000);
			}
		}
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isSpawn(round))
				startStage1Round4();
			round.clear();
		}, 63000);
	}

	private void startStage1Round4() {
		setStage(StageType.START_STAGE_1_ROUND_4, 2000);
		List<Integer> round = new ArrayList<>();
		Race race = getRegisteredTeamRace();
		switch (race) {
			case ASMODIANS -> {
				round.add(217491);
				sp(217491, 341.03156f, 361.04315f, 96.09093f, (byte) 106, 6000);
			}
			case ELYOS -> {
				round.add(217482);
				sp(217482, 341.03156f, 361.04315f, 96.09093f, (byte) 106, 6000);
			}
		}
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isSpawn(round))
				startStage1Round5();
			round.clear();
		}, 167000);
	}

	private void startStage1Round5() {
		setStage(StageType.START_STAGE_1_ROUND_5, 2000);
		Race race = instance.getRegisteredTeam().getRace();
		switch (race) {
			case ASMODIANS -> {
				sp(217493, 332.093f, 349.36847f, 96.090935f, (byte) 119, 6000);
			}
			case ELYOS -> {
				sp(217484, 332.093f, 349.36847f, 96.090935f, (byte) 119, 6000);
			}
		}
	}

	private void teleport(float x, float y, float z, byte h) {
		for (Player playerInside : instance.getPlayersInside())
			if (playerInside.isOnline())
				if (!getPlayerReward(playerInside.getObjectId()).isPlayerDefeated())
					teleport(playerInside, x, y, z, h);
				else
					moveToReadyRoom(playerInside);
	}

	private void moveToReadyRoom(Player player) {
		switch (stage) {
			case 1, 2, 3, 4 -> teleport(player, 381.41684f, 346.78162f, 96.74763f, (byte) 43);
			case 5 -> teleport(player, 1260.9495f, 832.87317f, 358.60562f, (byte) 92);
			case 6 -> teleport(player, 1592.8813f, 149.78166f, 128.81355f, (byte) 117);
			case 7 -> teleport(player, 1820.8805f, 795.80914f, 470.18304f, (byte) 51);
			case 8 -> teleport(player, 1780.103f, 1723.458f, 304.039f, (byte) 53);
			case 9 -> teleport(player, 1359.5046f, 1751.7952f, 319.59406f, (byte) 30);
			case 10 -> teleport(player, 1755.709f, 1253.4136f, 394.2378f, (byte) 33);
		}
	}

	@Override
	public void leaveInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	@Override
	public void onLeaveInstance(Player player) {
		CruciblePlayerReward reward = getPlayerReward(player.getObjectId());
		if (reward != null)
			reward.setPlayerLeave();
	}

	@Override
	public void onStopTraining(Player player) {
		doReward(player);
	}

	private void sp(int npcId, float x, float y, float z, byte h, int time) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isInstanceDestroyed) {
				synchronized (npcs) {
					npcs.add((Npc) spawn(npcId, x, y, z, h));
				}
			}
		}, time);
	}

	private Npc sp(int npcId, float x, float y, float z, byte h) {
		Npc npc = null;
		if (!isInstanceDestroyed) {
			npc = (Npc) spawn(npcId, x, y, z, h);
			synchronized (npcs) {
				npcs.add(npc);
			}
		}
		return npc;
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 217756, 217735 -> {
				ItemService.addItem(player, 186000124, 1);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401009));
				npc.getController().delete();
			}
		}
	}

	@Override
	public void onDropRegistered(Npc npc, int winnerObj) {
		Set<DropItem> dropItems = DropRegistrationService.getInstance().getCurrentDropMap().get(npc.getObjectId());
		int npcId = npc.getNpcId();
		int itemId = 0;
		switch (npcId) {
			case 217740, 217741, 217743, 217742 -> {
				dropItems.clear();
				itemId = 162000108;
				if (Rnd.chance() < 6) {
					itemId = switch (npc.getNpcId()) {
						case 217740 -> 125002593;
						case 217741 -> 125002595;
						case 217742 -> 125002592;
						case 217743 -> 125002594;
						default -> itemId;
					};
				}
			}
			case 217750 -> {
				dropItems.clear();
				itemId = 162000109;
				if (Rnd.chance() < 6) {
					itemId = switch (Rnd.get(1, 9)) {
						case 1 -> 101700911;
						case 2 -> 100201003;
						case 3 -> 100900869;
						case 4 -> 100100874;
						case 5 -> 100500886;
						case 6 -> 101300836;
						case 7 -> 100600944;
						case 8 -> 100001135;
						case 9 -> 101500895;
						default -> itemId;
					};
				}
			}
			case 217753 -> {
				if (Rnd.chance() < 51) {
					Race race = instance.getRegisteredTeam().getRace();
					itemId = switch (Rnd.get(1, 4)) {
						case 1 -> 169500935;
						case 2 -> 169500933;
						case 3 -> race == Race.ELYOS ? 169500947 : 169500951;
						case 4 -> race == Race.ELYOS ? 169500927 : 169500931;
						default -> itemId;
					};
				}
			}
		}
		if (itemId != 0)
			dropItems.add(DropRegistrationService.getInstance().regDropItem(1, 0, npcId, itemId, 1));
	}
}