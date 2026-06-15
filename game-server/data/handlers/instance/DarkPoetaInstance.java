package instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancescore.DarkPoetaScore;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.network.aion.instanceinfo.DarkPoetaScoreWriter;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Hilgert, xTz, Tiger, Ritsu, Estrayl
 */
@InstanceID(300040000)
public class DarkPoetaInstance extends GeneralInstanceHandler {

	private final List<Integer> excludedNpcs = new ArrayList<>();
	private final AtomicInteger killedGenerators = new AtomicInteger();
	private DarkPoetaScore instanceReward;
	private Future<?> instanceTimer;
	private long startTime;

	public DarkPoetaInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onDie(Npc npc) {
		Creature master = npc.getMaster();
		if (master instanceof Player)
			return;

		int npcId = npc.getNpcId();
		int points = calculatePointsReward(npc);
		if (instanceReward.getInstanceProgressionType().isStartProgress() && !excludedNpcs.contains(npcId)) {
			instanceReward.addNpcKill();
			instanceReward.addPoints(points);
			sendPacket(npc, points);
		}
		switch (npcId) {
			case 214895: // Main Power Generator
			case 214896: // Auxiliary Power Generator
			case 214897: // Emergency Generator
				if (killedGenerators.incrementAndGet() == 3)
					spawn(214904, 275.34537f, 323.02072f, 130.9302f, (byte) 52);
				break;
			case 214904: // Brigade General Anuhart
				instanceReward.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
				instanceReward.setRank(checkRank(instanceReward.getPoints()));
				sendPacket(null, 0);
				break;
			case 215280: // Tahabata Pyrelord
			case 215281: // Calindi Flamelord
			case 215282: // Vanuka Infernus
			case 215283: // Asaratu Bloodshade
			case 215284: // Chramati Firetail
				spawn(730211, 1171.9467f, 1223.2805f, 145.43983f, (byte) 16); // Exit
				break;
		}
	}

	private int getTime() {
		int current = (int) (System.currentTimeMillis() - startTime);
		return switch (instanceReward.getInstanceProgressionType()) {
			case PREPARING -> 120000 - current;
			case START_PROGRESS, END_PROGRESS -> 14400000 - current;
			default -> 0;
		};
	}

	private void sendPacket(Npc npc, int points) {
		if (npc != null)
			PacketSendUtility.broadcastToMap(instance, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(npc.getObjectTemplate().getL10n(), points));
		PacketSendUtility.broadcastToMap(instance, new SM_INSTANCE_SCORE(instance.getMapId(), new DarkPoetaScoreWriter(instanceReward), getTime()));
	}

	private int checkRank(int totalPoints) {
		int timeRemain = getTime();
		int rank = 8;
		if (timeRemain > 7200000 && totalPoints >= 17817) {
			spawn(215280, 1189f, 1244f, 141f, (byte) 76);
			rank = 1;
		} else if (timeRemain > 5400000 && totalPoints >= 15219) {
			spawn(215281, 1189f, 1244f, 141f, (byte) 76);
			rank = 2;
		} else if (timeRemain > 3600000 && totalPoints > 10913) {
			spawn(215282, 1189f, 1244f, 141f, (byte) 76);
			rank = 3;
		} else if (timeRemain > 1800000 && totalPoints > 6656) {
			spawn(215283, 1189f, 1244f, 141f, (byte) 76);
			rank = 4;
		} else if (timeRemain > 1) {
			spawn(215284, 1189f, 1244f, 141f, (byte) 76);
			rank = 5;
		}
		schedulePortalDespawn((Npc) spawn(700478, 298.24423f, 316.21954f, 133.29759f, (byte) 56));
		return rank;
	}

	private void schedulePortalDespawn(Npc portal) {
		ThreadPoolManager.getInstance().schedule(() -> portal.getController().delete(), 180000);
	}

	private int calculatePointsReward(Npc npc) {
		int pointsReward = 0;

		// Usually calculated by npcRank
		switch (npc.getObjectTemplate().getRating()) {
			case HERO:
				switch (npc.getObjectTemplate().getHpGauge()) {
					case 21:
						pointsReward = 786;
						break;
					default:
						pointsReward = 300;
				}
				break;
			default:
				if (npc.getObjectTemplate().getRace() == null) {
					break;
				}

				switch (npc.getObjectTemplate().getRace().getRaceId()) {
					case 22: // UNDEAD
						pointsReward = 12;
						break;
					case 9: // BROWNIE
						pointsReward = 18;
						break;
					case 6: // LIZARDMAN
						pointsReward = 24;
						break;
					case 8: // NAGA
					case 18: // DRAGON
					case 24: // MAGICALnpc
						pointsReward = 30;
						break;
					default:
						if (npc.getNpcId() != 281178)
							pointsReward = 11;
						break;
				}
		}

		// Special npcs
		switch (npc.getNpcId()) {
			// Drana
			case 700520:
				pointsReward = 52;
				break;
			// Walls
			case 700517:
			case 700518:
			case 700556:
			case 700558:
				pointsReward = 156;
				break;
			// Mutated Fungie
			case 214885:
				pointsReward = 21;
				break;
			// Named1
			case 214841:
				pointsReward = -209;
				break;
			case 281116:
				pointsReward = 1241;
				break;
			case 215431:
				pointsReward = 208;
				break;
			// Named2
			case 215429:
			case 215430:
				pointsReward = 190;
				break;
			case 214842:
			case 215432:
				pointsReward = 357;
				break;
			// Named3
			case 214871:
			case 215386:
			case 215428:
				pointsReward = 204;
				break;
			// Marabata
			case 214849:
			case 214850:
			case 214851:
				pointsReward = 319;
				break;
			// Generators
			case 214895:
			case 214896:
				pointsReward = 377;
				break;
			case 214897:
				pointsReward = 330;
				break;
			// Atmach
			case 214843:
				pointsReward = 456;
				break;
			// Boss
			case 214864:
			case 214880:
			case 214894:
			case 215387:
			case 215388:
			case 215389:
				pointsReward = 789;
				break;
			case 214904:
				pointsReward = 954;
				break;
		}
		TemporaryPlayerTeam<?> team = (TemporaryPlayerTeam<?>) instance.getRegisteredTeam();
		if (team != null) {
			if (team.getLeaderObject().getAbyssRank().getRank().getId() >= AbyssRankEnum.STAR1_OFFICER.getId())
				pointsReward = Math.round(pointsReward * 1.1f);
		}
		return pointsReward;
	}

	private void onStart(boolean manually) {
		instanceReward.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
		startTime = System.currentTimeMillis();
		sendPacket(null, 0);
		if (!manually)
			instance.forEachDoor(d -> d.setOpen(true));
	}

	@Override
	public void onEnterInstance(final Player player) {
		sendPacket(null, 0);
	}

	@Override
	public void onOpenDoor(int doorId) {
		if (doorId == 33) {
			if (instanceTimer != null && !instanceTimer.isCancelled())
				instanceTimer.cancel(true);
			onStart(true);
		}
	}

	@Override
	public void onInstanceDestroy() {
		if (instanceTimer != null)
			instanceTimer.cancel(false);
	}

	@Override
	public void onInstanceCreate() {
		excludedNpcs.addAll(Arrays.asList(700439, 700440, 700441, 700442, 700443, 700444, 700445, 700446, 700447, 281178));
		instanceReward = new DarkPoetaScore();
		instanceReward.setInstanceProgressionType(InstanceProgressionType.PREPARING);
		startTime = System.currentTimeMillis();
		instanceTimer = ThreadPoolManager.getInstance().schedule(() -> onStart(false), 121000);
	}

	@Override
	public void onGather(Player player, Gatherable gatherable) {
		instanceReward.addGather();
		sendPacket(null, 1);
	}

	@Override
	public void leaveInstance(Player player) {
		if (instanceReward.getInstanceProgressionType().isEndProgress())
			TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	@Override
	public void onEndCastSkill(Skill skill) {
		if (skill.getSkillId() != 18130) // Kobold Bomb Effect
			return;
		Npc camouflageStoneWall = getNpc(700516);
		if (camouflageStoneWall != null && PositionUtil.isInRange(skill.getEffector(), camouflageStoneWall, 10))
			camouflageStoneWall.getController().die();
	}
}
