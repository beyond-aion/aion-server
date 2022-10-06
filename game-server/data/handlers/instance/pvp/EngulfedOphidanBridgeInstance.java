package instance.pvp;

import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancescore.PvpInstanceScore;
import com.aionemu.gameserver.model.instance.playerreward.PvpInstancePlayerReward;
import com.aionemu.gameserver.network.aion.instanceinfo.PvpInstanceScoreWriter;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Tibald
 */
@InstanceID(301210000)
public class EngulfedOphidanBridgeInstance extends BasicPvpInstance {

	private final static int MAX_PLAYERS_PER_FACTION = 6;
	private int timeInMin = 0;

	public EngulfedOphidanBridgeInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected void spawnFactionRelatedNpcs() {
		spawn((raceStartPosition == 0 ? 802025 : 802026), 753.31775f, 570.89905f, 577.3619f, (byte) 36);
		spawn((raceStartPosition == 0 ? 701949 : 701948), 753.31775f, 570.89905f, 577.3619f, (byte) 87);
		spawn((raceStartPosition == 0 ? 701949 : 701948), 767.931f, 569.22375f, 577.3449f, (byte) 87);
		spawn((raceStartPosition == 0 ? 701949 : 701948), 768.46606f, 534.54016f, 576.375f, (byte) 87);
		spawn((raceStartPosition == 0 ? 701949 : 701948), 773.47577f, 552.68604f, 576.5519f, (byte) 87);
		spawn((raceStartPosition == 0 ? 701949 : 701948), 776.1901f, 569.8358f, 576.91437f, (byte) 87);
		spawn((raceStartPosition == 0 ? 701947 : 701950), 739.6952f, 536.6952f, 576.5571f, (byte) 87);
		spawn((raceStartPosition == 0 ? 701947 : 701950), 741.9557f, 557.69574f, 576.7407f, (byte) 87);
		spawn((raceStartPosition == 0 ? 701947 : 701950), 744.59814f, 573.4721f, 577.14624f, (byte) 87);
		spawn((raceStartPosition == 0 ? 802025 : 802026), 311.8876f, 570.89905f, 597.13184f, (byte) 70);
		spawn((raceStartPosition == 0 ? 701948 : 701949), 300.15576f, 500.93283f, 597.13184f, (byte) 0);
		spawn((raceStartPosition == 0 ? 701948 : 701949), 300.3278f, 476.23727f, 597.13184f, (byte) 0);
		spawn((raceStartPosition == 0 ? 701948 : 701949), 309.21387f, 509.46008f, 596.54047f, (byte) 0);
		spawn((raceStartPosition == 0 ? 701948 : 701949), 327.29257f, 506.21063f, 596.5348f, (byte) 0);
		spawn((raceStartPosition == 0 ? 701948 : 701949), 347.668f, 504.71292f, 595.2064f, (byte) 0);
		spawn((raceStartPosition == 0 ? 701950 : 701947), 311.0972f, 469.62903f, 596.54047f, (byte) 0);
		spawn((raceStartPosition == 0 ? 701950 : 701947), 328.51215f, 474.87762f, 596.53375f, (byte) 0);
		spawn((raceStartPosition == 0 ? 701950 : 701947), 346.49152f, 478.24072f, 595.2064f, (byte) 0);
	}

	@Override
	protected void onStart() {
		updateProgress(InstanceProgressionType.START_PROGRESS);
		openFirstDoors();
		startTimeCheck();
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> onStop(false), 1200000));
	}

	private void startTimeCheck() {
		tasks.add(ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			timeInMin += 30;
			switch (timeInMin) {
				case 300: // 5 minutes *TODO Spawn supply on captured bases
					if (!instance.getNpcs(802038).isEmpty()) { // BALAUR
						spawn(702042, 545.2472f, 444.18713f, 620.16034f, (byte) 25); // Northen
					} else if (!instance.getNpcs(802036).isEmpty()) { // Ely
						spawn(702043, 542.6644f, 438.9099f, 620.25f, (byte) 25); // Northen
					} else if (!instance.getNpcs(802037).isEmpty()) { // Asmo
						spawn(702044, 542.6644f, 438.9099f, 620.25f, (byte) 25); // Northen
					}
					if (!instance.getNpcs(802035).isEmpty()) { // BALAUR
						spawn(702042, 689.44977f, 484.8635f, 599.9068f, (byte) 25); // Defense
					} else if (!instance.getNpcs(802033).isEmpty()) { // Ely
						spawn(702043, 693.6922f, 478.96912f, 599.9574f, (byte) 25); // Defense
					} else if (!instance.getNpcs(802034).isEmpty()) { // Asmo
						spawn(702044, 693.6922f, 478.96912f, 599.9574f, (byte) 25); // Defense
					}
					if (!instance.getNpcs(802041).isEmpty()) { // BALAUR
						spawn(702042, 595.0814f, 569.0518f, 590.91034f, (byte) 25); // Southern
					} else if (!instance.getNpcs(802039).isEmpty()) { // Ely
						spawn(702043, 600.98865f, 572.7864f, 590.91034f, (byte) 25); // Southern
					} else if (!instance.getNpcs(802040).isEmpty()) { // Asmo
						spawn(702044, 600.98865f, 572.7864f, 590.91034f, (byte) 25); // Southern
					}
					if (!instance.getNpcs(802044).isEmpty()) { // BALAUR
						spawn(702042, 494.6366f, 519.49677f, 597.6485f, (byte) 25); // Bridge
					} else if (!instance.getNpcs(802042).isEmpty()) { // Ely
						spawn(702043, 499.52756f, 522.5279f, 597.6485f, (byte) 25); // Bridge
					} else if (!instance.getNpcs(802043).isEmpty()) { // Asmo
						spawn(702044, 499.52756f, 522.5279f, 597.6485f, (byte) 25); // Bridge
					}
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_GuardDarkHeroTrigger_Spawn_IDLDF5_Under_01_War());
					spawn((raceStartPosition == 0 ? 701988 : 701989), 758.5892f, 564.36304f, 576.8448f, (byte) 83);
					spawn((raceStartPosition == 0 ? 701984 : 701985), 761.9165f, 561.8183f, 577.1422f, (byte) 83);
					spawn((raceStartPosition == 0 ? 701986 : 701987), 754.92f, 563.0107f, 577.17413f, (byte) 83);
					spawn((raceStartPosition == 0 ? 701989 : 701988), 320.97195f, 490.1614f, 596.14105f, (byte) 0);
					spawn((raceStartPosition == 0 ? 701985 : 701984), 320.28528f, 494.81866f, 596.2401f, (byte) 0);
					spawn((raceStartPosition == 0 ? 701987 : 701986), 320.01007f, 485.90097f, 596.2371f, (byte) 0);
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5_Under_01_War_Drop_MSG_01());
					break;
				case 480: // 8 minutes
					spawn(701976, 575.5561f, 477.6238f, 620.8339f, (byte) 0); // hidden box supply
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5_Under_01_War_Drop_MSG_01());
					break;
				case 600: // 10 minutes
					if (!instance.getNpcs(802038).isEmpty()) { // BALAUR
						spawn(702042, 518.3042f, 448.46414f, 620.154f, (byte) 25); // Northen
					} else if (!instance.getNpcs(802036).isEmpty()) { // Ely
						spawn(702043, 527.8359f, 452.22488f, 620.4822f, (byte) 25); // Northen
					} else if (!instance.getNpcs(802037).isEmpty()) { // Asmo
						spawn(702044, 527.8359f, 452.22488f, 620.4822f, (byte) 25); // Northen
					}
					if (!instance.getNpcs(802035).isEmpty()) { // BALAUR
						spawn(702042, 688.0218f, 455.8713f, 599.9777f, (byte) 25); // Defense
					} else if (!instance.getNpcs(802033).isEmpty()) { // Ely
						spawn(702043, 679.59875f, 450.72556f, 599.9777f, (byte) 25); // Defense
					} else if (!instance.getNpcs(802034).isEmpty()) { // Asmo
						spawn(702044, 679.59875f, 450.72556f, 599.9777f, (byte) 25); // Defense
					}
					if (!instance.getNpcs(802041).isEmpty()) { // BALAUR
						spawn(702042, 623.7144f, 560.762f, 590.91034f, (byte) 25); // Southern
					} else if (!instance.getNpcs(802039).isEmpty()) { // Ely
						spawn(702043, 620.8022f, 567.3166f, 590.91034f, (byte) 25); // Southern
					} else if (!instance.getNpcs(802040).isEmpty()) { // Asmo
						spawn(702044, 620.8022f, 567.3166f, 590.91034f, (byte) 25); // Southern
					}
					if (!instance.getNpcs(802044).isEmpty()) { // BALAUR
						spawn(702042, 489.15707f, 552.2536f, 597.6485f, (byte) 25); // Bridge
					} else if (!instance.getNpcs(802042).isEmpty()) { // Ely
						spawn(702043, 499.61383f, 552.3009f, 597.6485f, (byte) 25); // Bridge
					} else if (!instance.getNpcs(802043).isEmpty()) { // Asmo
						spawn(702044, 499.61383f, 552.3009f, 597.6485f, (byte) 25); // Bridge
					}
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_GuardDarkHeroTrigger_Spawn_IDLDF5_Under_01_War());
					spawn(702018, 759.98627f, 549.6435f, 577.5169f, (byte) 30); // supply box
					spawn(702018, 332.92816f, 486.6423f, 596.86804f, (byte) 0); // supply box
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_GUARDLIGHTHERO_SPAWN_IDLDF5_UNDER_01_WAR());
					if (raceStartPosition == 0) {
						spawn(233491, 612.2644f, 569.6577f, 590.75f, (byte) 77); // Captain Avran
						spawn(233490, 615.8132f, 568.7348f, 590.75f, (byte) 77); // Avran crusher
						spawn(233490, 611.1977f, 571.6211f, 590.75f, (byte) 80); // Avran crusher
					} else {
						spawn(233491, 526.1039f, 440.7422f, 620.25f, (byte) 23); // Captain Avran
						spawn(233490, 523.39056f, 437.67584f, 620.25f, (byte) 0); // Avran crusher
						spawn(233490, 523.8341f, 443.3746f, 620.16406f, (byte) 3); // Avran crusher
					}
					break;
				case 930: // 15 minutes
					if (!instance.getNpcs(802038).isEmpty()) { // BALAUR
						spawn(702042, 513.20764f, 431.37198f, 620.2005f, (byte) 25); // Northen
					} else if (!instance.getNpcs(802036).isEmpty()) { // Ely
						spawn(702043, 513.20764f, 431.37198f, 620.2005f, (byte) 25); // Northen
					} else if (!instance.getNpcs(802037).isEmpty()) { // Asmo
						spawn(702044, 513.20764f, 431.37198f, 620.2005f, (byte) 25); // Northen
					}
					if (!instance.getNpcs(802035).isEmpty()) { // BALAUR
						spawn(702042, 660.04846f, 462.27594f, 600.08746f, (byte) 25); // Defense
					} else if (!instance.getNpcs(802033).isEmpty()) { // Ely
						spawn(702043, 660.04846f, 462.27594f, 600.08746f, (byte) 25); // Defense
					} else if (!instance.getNpcs(802034).isEmpty()) { // Asmo
						spawn(702044, 660.04846f, 462.27594f, 600.08746f, (byte) 25); // Defense
					}
					if (!instance.getNpcs(802041).isEmpty()) { // BALAUR
						spawn(702042, 616.68884f, 542.6021f, 590.582f, (byte) 25); // Southern
					} else if (!instance.getNpcs(802039).isEmpty()) { // Ely
						spawn(702043, 616.68884f, 542.6021f, 590.582f, (byte) 25); // Southern
					} else if (!instance.getNpcs(802040).isEmpty()) { // Asmo
						spawn(702044, 616.68884f, 542.6021f, 590.582f, (byte) 25); // Southern
					}
					if (!instance.getNpcs(802044).isEmpty()) { // BALAUR
						spawn(702042, 475.17694f, 541.69165f, 597.5f, (byte) 25); // Bridge
					} else if (!instance.getNpcs(802042).isEmpty()) { // Ely
						spawn(702043, 475.17694f, 541.69165f, 597.5f, (byte) 25); // Bridge
					} else if (!instance.getNpcs(802043).isEmpty()) { // Asmo
						spawn(702044, 475.17694f, 541.69165f, 597.5f, (byte) 25); // Bridge
					}
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_GuardDarkHeroTrigger_Spawn_IDLDF5_Under_01_War());
					spawn(702018, 752.6556f, 550.7634f, 577.52795f, (byte) 30); // supply box
					spawn(702018, 332.7056f, 494.46088f, 596.86206f, (byte) 0); // supply box
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_GUARDLIGHTHERO_SPAWN_IDLDF5_UNDER_01_WAR());
					break;
				case 960: // 16 minutes
					spawn(701976, 619.3583f, 515.6447f, 592.1339f, (byte) 0); // hidden box supply
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5_Under_01_War_Drop_MSG_01());
					break;
			}
			checkBases();
		}, 30000, 30000));
	}

	private void checkBases() {
		updatePointsByGenerators(instance.getNpcs(701944), Race.ASMODIANS, true);
		updatePointsByGenerators(instance.getNpcs(701943), Race.ELYOS, true);
	}

	@Override
	protected void setAndDistributeRewards(Player player, PvpInstancePlayerReward reward, Race winningRace, boolean isBossKilled) {
		int scorePoints = instanceScore.getPointsByRace(reward.getRace());
		if (reward.getRace() == winningRace) {
			reward.setBaseAp(instanceScore.getWinnerApReward());
			reward.setBonusAp(2 * scorePoints / MAX_PLAYERS_PER_FACTION);
			reward.setBaseGp(50);
			reward.setReward1(188052681, 1, 0);
			if (Rnd.chance() < 20)
				reward.setReward2(188950017, 1, 0); // CUSTOM: Special Courier Pass (Abyss Eternal/Lv. 61-65)
		} else {
			reward.setBaseAp(instanceScore.getLoserApReward());
			reward.setBonusAp(scorePoints / MAX_PLAYERS_PER_FACTION);
			reward.setBaseGp(10);
			reward.setReward1(188053209, 1, 0);
			if (winningRace == Race.NONE)
				reward.setBaseAp(instanceScore.getDrawApReward()); // Base AP are overridden in a draw case
		}
		distributeRewards(player, reward);
	}

	@Override
	protected void updatePoints(Player player, Race race, String npcL10n, int points) {
		super.updatePoints(player, race, npcL10n, points);

		int diff = Math.abs(instanceScore.getAsmodiansPoints() - instanceScore.getElyosPoints());
		if (diff >= 30000)
			onStop(false);
	}

	private void updatePointsByGenerators(List<Npc> generators, Race race, boolean check) {
		if (check && !instanceScore.isStartProgress())
			return;
		if (generators.isEmpty())
			return;
		int points = generators.size() * 100;
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(generators.get(0).getObjectTemplate().getL10n(), points));
		instanceScore.addPointsByRace(race, points);
		sendPacket(
			new SM_INSTANCE_SCORE(instance.getMapId(), new PvpInstanceScoreWriter(instanceScore, InstanceScoreType.UPDATE_FACTION_SCORE, race), getTime()));
		int diff = Math.abs(instanceScore.getAsmodiansPoints() - instanceScore.getElyosPoints());
		if (diff >= 30000)
			onStop(false);
	}

	@Override
	public void onDie(Npc npc) {
		Player player = npc.getAggroList().getMostPlayerDamage();
		captureBasesBalaurs(npc);
		if (player == null) {
			return;
		}
		int points = 0;
		switch (npc.getNpcId()) {
			case 233473:
			case 233856:
				points = 100;
				break;
			case 233484:
			case 233485:
			case 233486:
			case 233481:
			case 233482:
			case 233476:
			case 233475:
			case 233479:
			case 233478:
				points = 200;
				break;
			case 233477:
			case 233483:
			case 233487:
			case 233480:
				points = 300;
				break;
			case 702042:
				points = 500;
				break;
			case 233492:
			case 233493:
				points = 2000;
				break;
			case 701945: // balaur power generator
				points = 5000;
				break;
			case 233494:
				points = 30000;
				break;
		}
		if (points > 0) {
			updatePoints(player, player.getRace(), npc.getObjectTemplate().getL10n(), points);
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		if (player == null) {
			return;
		}
		captureBases(player, npc);
	}

	private void captureBasesBalaurs(Npc npc) {
		if (npc.getNpcId() == 701944 || npc.getNpcId() == 701943) {
			int npcId = 701945;
			switch (npc.getSpawn().getStaticId()) {
				case 172, 173 -> spawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 164);
				case 169, 166 -> spawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 158);
				case 170, 171 -> spawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 162);
				case 175, 174 -> spawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), 160);
			}
			updatePoints(null, npc.getRace(), null, -5000);
			npc.getController().delete();
		}
	}

	private void captureBases(Player player, Npc npc) {
		if (npc.getNpcId() == 701945 || npc.getNpcId() == 701944 || npc.getNpcId() == 701943) {
			int npcId = player.getRace() == Race.ELYOS ? 701943 : 701944;
			if (npc.getRace() == player.getRace()) {
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_Base_IDLDF5_Under_01_War_Flag02());
				return;
			}
			switch (npc.getSpawn().getStaticId()) {
				case 158, 169, 166 -> spawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), player.getRace() == Race.ELYOS ? 169 : 166);
				case 162, 170, 171 -> spawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), player.getRace() == Race.ELYOS ? 171 : 170);
				case 164, 172, 173 -> spawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), player.getRace() == Race.ELYOS ? 172 : 173);
				case 160, 175, 174 -> spawn(npcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), player.getRace() == Race.ELYOS ? 175 : 174);
			}
			updatePoints(player, player.getRace(), npc.getObjectTemplate().getL10n(), 5000);
			npc.getController().delete();
		}
	}

	private void openFirstDoors() {
		instance.setDoorState(177, true);
		instance.setDoorState(176, true);
	}

	@Override
	public void onInstanceCreate() {
		instanceScore = new PvpInstanceScore<>(5600, 1120, 3360); // No info found for draws, so let's guess
		super.onInstanceCreate();
	}

	@Override
	public void portToStartPosition(Player player) {
		if (player.getRace() == Race.ELYOS && raceStartPosition == 0 || player.getRace() == Race.ASMODIANS && raceStartPosition != 0) {
			TeleportService.teleportTo(player, instance, 760.9035f, 575.408f, 578.4075f, (byte) 86);
		} else {
			TeleportService.teleportTo(player, instance, 305.4604f, 488.341f, 598.5838f);
		}
	}
}
