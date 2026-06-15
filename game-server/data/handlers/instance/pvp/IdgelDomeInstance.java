package instance.pvp;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancescore.PvpInstanceScore;
import com.aionemu.gameserver.model.instance.playerreward.PvpInstancePlayerReward;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Ritsu, Estrayl
 */
@InstanceID(301310000)
public class IdgelDomeInstance extends BasicPvpInstance {

	private final static int MAX_PLAYERS_PER_FACTION = 6;
	private final List<WorldPosition> chestPositions = new ArrayList<>();

	public IdgelDomeInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		instanceScore = new PvpInstanceScore<>(5600, 1120, 3360); // No info found for draws, so let's guess
		super.onInstanceCreate();
	}

	@Override
	protected void spawnFactionRelatedNpcs() {
		spawn((raceStartPosition == 0 ? 802384 : 802383), 254.9255f, 179.3691f, 80.3904f, (byte) 90); // Quest Npcs
		spawn((raceStartPosition == 0 ? 802383 : 802384), 274.5722f, 338.7958f, 80.6454f, (byte) 31);
		spawnAndSetRespawn((raceStartPosition == 0 ? 702300 : 702301), 286.2041f, 338.7794f, 79.8274f, (byte) 105, 20); // Defense Turrets
		spawnAndSetRespawn((raceStartPosition == 0 ? 702300 : 702301), 279.9443f, 332.1660f, 79.8274f, (byte) 105, 20);
		spawnAndSetRespawn((raceStartPosition == 0 ? 702300 : 702301), 263.9707f, 341.6699f, 79.7848f, (byte) 15, 20);
		spawnAndSetRespawn((raceStartPosition == 0 ? 702300 : 702301), 277.0629f, 355.2583f, 79.7844f, (byte) 75, 20);
		spawnAndSetRespawn((raceStartPosition == 0 ? 702301 : 702300), 243.0305f, 179.2297f, 79.8241f, (byte) 45, 20);
		spawnAndSetRespawn((raceStartPosition == 0 ? 702301 : 702300), 249.3263f, 185.8511f, 79.8241f, (byte) 45, 20);
		spawnAndSetRespawn((raceStartPosition == 0 ? 702301 : 702300), 252.1385f, 162.8889f, 79.7711f, (byte) 15, 20);
		spawnAndSetRespawn((raceStartPosition == 0 ? 702301 : 702300), 265.3730f, 176.5772f, 79.7719f, (byte) 75, 20);
		spawnAndSetRespawn((raceStartPosition == 0 ? 802548 : 802549), 199.187f, 191.761f, 80.7466f, (byte) 15, 180); // Lever
		spawnAndSetRespawn((raceStartPosition == 0 ? 802549 : 802548), 329.799f, 326.113f, 81.8731f, (byte) 75, 180);
	}

	@Override
	protected void onStart() {
		updateProgress(InstanceProgressionType.START_PROGRESS);
		instance.forEachDoor(door -> door.setOpen(true));
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> spawn(287249, 264.4382f, 258.58527f, 88.452042f, (byte) 31), 600000));
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> onStop(false), 1200000));
		spawnChest();
		spawnChest();
	}

	@Override
	protected void setAndDistributeRewards(Player player, PvpInstancePlayerReward reward, Race winningRace, boolean isBossKilled) {
		int scorePoints = instanceScore.getPointsByRace(reward.getRace());
		if (reward.getRace() == winningRace) {
			reward.setBaseAp(instanceScore.getWinnerApReward() + (isBossKilled ? 2000 : 0));
			reward.setBonusAp(2 * scorePoints / MAX_PLAYERS_PER_FACTION);
			reward.setBaseGp(50);
			reward.setReward1(186000242, 3, 0); // Custom: Ceramium Medal
			reward.setReward2(188053030, 1, 0); // Idgel Dome Reward Box
			if (isBossKilled) {
				int mythicKunaxEqItemId = 0;
				if (Rnd.chance() < 20)
					mythicKunaxEqItemId = reward.getMythicKunaxEquipment(player);
				reward.setReward3(mythicKunaxEqItemId, mythicKunaxEqItemId == 0 ? 0 : 1);
				reward.setReward4(188053032, 1); // Idgel Dome Tribute Box
			}
		} else {
			reward.setBaseAp(instanceScore.getLoserApReward());
			reward.setBonusAp(scorePoints / MAX_PLAYERS_PER_FACTION);
			reward.setBaseGp(10);
			reward.setReward1(186000242, 1, 0); // Custom: Ceramium Medal
			reward.setReward2(188053031, 1, 0); // Idgel Dome Consolation Prize
			if (winningRace == Race.NONE)
				reward.setBaseAp(instanceScore.getDrawApReward()); // Base AP are overridden in a draw case
		}
		distributeRewards(player, reward);
	}

	@Override
	public void onSpawn(VisibleObject object) {
		if (object instanceof Npc) {
			switch (((Npc) object).getNpcId()) {
				case 287249 -> { // Kunax
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5_FORTRESS_RE_BOSS_SPAWN());
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_FORTRESS_RE_BOSSSPAWN());
				}
				case 802548, 802549 -> instance.getPlayersInside().stream().filter(p -> p.getRace() != ((Npc) object).getRace())
					.forEach(p -> setFlameVentNoInteraction(p, object));
			}
		}
	}

	private void setFlameVentNoInteraction(Player player, VisibleObject flameVent) {
		PacketSendUtility.sendPacket(player, new SM_CUSTOM_SETTINGS(flameVent.getObjectId(), 0, CreatureType.PEACE.getId(), 0));
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		Player player = npc.getAggroList().getMostPlayerDamage(); // Checked with retail, maybe the player making the last hit gets the points
		if (player == null || instanceScore.getInstanceProgressionType() != InstanceProgressionType.START_PROGRESS)
			return;

		int points = 0;
		boolean isKunaxKilled = false;
		switch (npc.getNpcId()) {
			case 234186, 234187, 234189 -> points = 120;
			case 234751, 234752, 234753 -> points = 200;
			case 287249 -> {
				points = 6000;
				isKunaxKilled = true;
			}
		}
		if (points > 0)
			updatePoints(player, player.getRace(), npc.getObjectTemplate().getL10n(), points);
		if (isKunaxKilled)
			onStop(true);
	}

	private void spawnChest() {
		WorldPosition p = Rnd.get(chestPositions);
		if (p != null) {
			chestPositions.remove(p);
			spawn(702581 + Rnd.get(0, 2), p.getX(), p.getY(), p.getZ(), p.getHeading());
		}
	}

	private void scheduleChestRespawn() {
		tasks.add(ThreadPoolManager.getInstance().schedule(this::spawnChest, 120000));
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 702581, 702582, 702583 -> {
				chestPositions.add(npc.getPosition());
				scheduleChestRespawn();
			}
			case 802548 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_FORTRESS_RE_FIRESPAWN_A());
			case 802549 -> sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_FORTRESS_RE_FIRESPAWN_B());
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		Npc forbiddenFlameVent = getNpc(player.getRace() == Race.ASMODIANS ? 802548 : 802549);
		if (forbiddenFlameVent != null)
			setFlameVentNoInteraction(player, forbiddenFlameVent);

		super.onEnterInstance(player);
	}

	@Override
	public void portToStartPosition(Player player) {
		if (player.getRace() == Race.ELYOS && raceStartPosition == 0 || player.getRace() == Race.ASMODIANS && raceStartPosition != 0)
			TeleportService.teleportTo(player, instance.getMapId(), instance.getInstanceId(), 269.76874f, 348.35953f, 79.44365f, (byte) 105);
		else
			TeleportService.teleportTo(player, instance.getMapId(), instance.getInstanceId(), 259.3971f, 169.18243f, 79.430855f, (byte) 45);
	}

	@Override
	public boolean isBoss(Npc npc) {
		return npc.getNpcId() == 287249;
	}
}
