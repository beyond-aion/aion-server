package instance.pvp;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancescore.PvpInstanceScore;
import com.aionemu.gameserver.model.instance.playerreward.PvpInstancePlayerReward;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Tibald
 */
@InstanceID(301220000)
public class IronWallWarfrontInstance extends BasicPvpInstance {

	private final static int MAX_PLAYERS_PER_FACTION = 24;

	public IronWallWarfrontInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected void onStart() {
		updateProgress(InstanceProgressionType.PREPARING);
		instance.getPlayersInside().forEach(this::portToStartPosition); // split groups
		tasks.add(ThreadPoolManager.getInstance().schedule(this::endPreparingAndStart, 60000));
	}

	private void endPreparingAndStart() {
		updateProgress(InstanceProgressionType.START_PROGRESS);
		openFirstDoors();
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> onStop(false), 2400000));
	}

	@Override
	protected void setAndDistributeRewards(Player player, PvpInstancePlayerReward reward, Race winningRace, boolean isBossKilled) {
		int scorePoints = instanceScore.getPointsByRace(reward.getRace());
		if (reward.getRace() == winningRace) {
			reward.setBaseAp(instanceScore.getWinnerApReward() + (isBossKilled ? 3850 : 0)); // increased by 3850 if pashid is killed
			reward.setBonusAp(2 * scorePoints / MAX_PLAYERS_PER_FACTION);
			reward.setBaseGp(100);
			reward.setReward1(186000243, 9, 0); // Fragmented Ceramium
			if (isBossKilled) {
				reward.setReward2(188052729, 1, 0); // Eternal Bastion Warfront Reward Chest
				if (Rnd.chance() < 5)
					reward.setReward3(188950020, 1); // CUSTOM: Special Courier Pass (Abyss Mythic/Lv. 61-65)
			}
		} else {
			reward.setBaseAp(instanceScore.getLoserApReward());
			reward.setBonusAp(scorePoints / MAX_PLAYERS_PER_FACTION);
			reward.setBaseGp(10);
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

	@Override
	public void onDie(Npc npc) {
		Player player = npc.getAggroList().getMostPlayerDamage();
		if (player == null) {
			return;
		}
		int points = 0;
		switch (npc.getNpcId()) {
			case 233473:
				points = 100;
				break;
			case 702042:
				points = 500;
				break;
			case 233491: // random position boss
			case 701943: // elyos power generator
			case 701944: // asmodian power generator
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
		int points = 0;
		switch (npc.getNpcId()) {
			case 801903:
				points = 1500;
				break;
			case 801772:
				points = 525;
				break;
			case 801766:
			case 801767:
			case 801818:
			case 801819:
			case 801820:
			case 801821:
				points = 255;
				break;
			case 730861:
			case 730878:
			case 730879:
			case 730880:
				updatePoints(player, player.getRace(), npc.getObjectTemplate().getL10n(), 200);
				if (player.getRace() == Race.ELYOS) {
					spawn(701900, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				} else {
					spawn(701901, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
					npc.getController().delete();
				}
				break;
		}
		if (points > 0) {
			updatePoints(player, player.getRace(), npc.getObjectTemplate().getL10n(), points);
			npc.getController().delete();
		}
	}

	private void openFirstDoors() {
		instance.setDoorState(177, true);
		instance.setDoorState(176, true);
	}

	@Override
	protected int getReinforceMemberPhaseDelay() {
		return 120000;
	}

	@Override
	public void onInstanceCreate() {
		instanceScore = new PvpInstanceScore<>(8400, 1680, 5040); // No info found for draws, so let's guess
		super.onInstanceCreate();
	}

	public void portToPosition(Player player, WorldMapInstance instance) {
		boolean useAlternativePos = player.isInAlliance()
			&& (player.getPlayerAllianceGroup().getObjectId() == 1001 || player.getPlayerAllianceGroup().getObjectId() == 1003);
		if (player.getRace() == Race.ELYOS && raceStartPosition == 0 || player.getRace() == Race.ASMODIANS && raceStartPosition != 0) {
			if (useAlternativePos)
				TeleportService.teleportTo(player, instance, 274.143f, 384.335f, 239.973f, (byte) 14);
			else
				TeleportService.teleportTo(player, instance, 342.138f, 616.856f, 248.197f, (byte) 35);
		} else {
			if (useAlternativePos)
				TeleportService.teleportTo(player, instance, 598.229f, 712.984f, 223.306f, (byte) 73);
			else
				TeleportService.teleportTo(player, instance, 711.403f, 621.797f, 213.276f, (byte) 31);
		}
	}
}
