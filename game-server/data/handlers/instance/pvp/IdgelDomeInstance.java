package instance.pvp;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.IdgelDomeInfo;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.instance.playerreward.IdgelDomePlayerInfo;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.instanceinfo.IdgelDomeScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.time.ServerTime;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Ritsu, Estrayl
 */
@InstanceID(301310000)
public class IdgelDomeInstance extends GeneralInstanceHandler {

	private final List<WorldPosition> chestPositions = new ArrayList<>();
	private final List<Future<?>> tasks = new ArrayList<>();
	private IdgelDomeInfo idi;
	private long startTime;

	public IdgelDomeInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		idi = new IdgelDomeInfo();
		idi.setInstanceProgressionType(InstanceProgressionType.PREPARING);
		startTime = System.currentTimeMillis();
		tasks.add(ThreadPoolManager.getInstance().schedule(this::onStart, 120000));
	}

	private void onStart() {
		idi.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
		startTime = System.currentTimeMillis();
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PROGRESS), getTime()));
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_INFO, instance.getPlayersInside()), getTime()));
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_SCORE, instance.getPlayersInside()), getTime()));
		instance.forEachDoor(door -> door.setOpen(true));
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> spawn(234190, 264.4382f, 258.58527f, 88.452042f, (byte) 31), 600000));
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> onStop(false), 1200000));
		spawnAndSetRespawn(802548, 199.187f, 191.761f, 80.7466f, (byte) 15, 180);
		spawnAndSetRespawn(802549, 329.799f, 326.113f, 81.8731f, (byte) 75, 180);
		spawnChest();
		spawnChest();
	}

	private void onStop(boolean isKunaxKilled) {
		cancelTasks();

		idi.setInstanceProgressionType(InstanceProgressionType.END_PROGRESS);
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PROGRESS), getTime()));
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_SCORE, instance.getPlayersInside()), getTime()));
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_INFO, instance.getPlayersInside()), getTime()));
		Race winningrace = idi.getRaceWithHighestPoints();
		int winnerBonusAp = Rnd.get(10000, 14000);
		int loserBonusAp = Rnd.get(3000, 6000);
		boolean isEventActive = ServerTime.now().isAfter(ServerTime.of(LocalDateTime.of(2020, 3, 27, 0, 0)))
			&& ServerTime.now().isBefore(ServerTime.of(LocalDateTime.of(2020, 4, 5, 23, 59))); // TODO: Remove me
		instance.forEachPlayer(p -> {
			IdgelDomePlayerInfo reward = idi.getPlayerReward(p.getObjectId());
			if (reward.getRace() == winningrace) {
				reward.setBaseAp(idi.getWinnerApReward());
				reward.setBonusAp(winnerBonusAp);
				reward.setBaseGp(50);
				reward.setReward1(186000242, 3, 0);
				reward.setReward2(188053030, 1, 0);
				if (isKunaxKilled) {
					int mythicKunaxEqItemId = 0;
					if (Rnd.chance() < 20)
						mythicKunaxEqItemId = idi.getMythicKunaxEquipment(p);
					reward.setReward3(mythicKunaxEqItemId, mythicKunaxEqItemId == 0 ? 0 : 1);
					reward.setReward4(188053032, 1);
				}
				if (isEventActive)
					reward.setBonusReward(186000388, 35);
			} else {
				reward.setBaseAp(idi.getLoserApReward());
				reward.setBonusAp(loserBonusAp);
				reward.setBaseGp(10);
				reward.setReward1(186000242, 1, 0);
				reward.setReward2(188053031, 1, 0);
				if (isEventActive)
					reward.setBonusReward(186000388, 15);
			}
			sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.SHOW_REWARD, p.getObjectId(), 0), getTime()));
			AbyssPointsService.addAp(p, reward.getBaseAp() + reward.getBonusAp());
			int gpToAdd = reward.getBaseGp() + reward.getBonusGp();
			if (gpToAdd > 0)
				GloryPointsService.increaseGpBy(p.getObjectId(), gpToAdd);
			if (reward.getReward1ItemId() > 0)
				ItemService.addItem(p, reward.getReward1ItemId(), reward.getReward1Count() + reward.getReward1BonusCount());
			if (reward.getReward2ItemId() > 0)
				ItemService.addItem(p, reward.getReward2ItemId(), reward.getReward2Count() + reward.getReward2BonusCount());
			if (reward.getReward3ItemId() > 0)
				ItemService.addItem(p, reward.getReward3ItemId(), reward.getReward3Count());
			if (reward.getReward4ItemId() > 0)
				ItemService.addItem(p, reward.getReward4ItemId(), reward.getReward4Count());
			if (reward.getBonusRewardItemId() > 0)
				ItemService.addItem(p, reward.getBonusRewardItemId(), reward.getBonusRewardCount());
		});
		instance.forEachNpc(npc -> npc.getController().delete());
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> {
			for (Player player : instance.getPlayersInside()) {
				if (player.isDead())
					PlayerReviveService.duelRevive(player);
				onExitInstance(player);
			}
		}, 10000));
	}

	@Override
	public void onSpawn(VisibleObject object) {
		if (object instanceof Npc) {
			switch (((Npc) object).getNpcId()) {
				case 234190: // Kunax
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5_FORTRESS_RE_BOSS_SPAWN());
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_FORTRESS_RE_BOSSSPAWN());
					break;
				case 802548:
				case 802549:
					instance.getPlayersInside().stream().filter(p -> p.getRace() != ((Npc) object).getRace())
						.forEach(p -> setFlameVentNoInteraction(p, object));
					break;
			}
		}
	}

	private void setFlameVentNoInteraction(Player player, VisibleObject flameVent) {
		PacketSendUtility.sendPacket(player, new SM_CUSTOM_SETTINGS(flameVent.getObjectId(), 0, CreatureType.PEACE.getId(), 0));
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		Player player = npc.getAggroList().getMostPlayerDamage();
		if (player == null)
			return;

		int points = 0;
		boolean isKunaxKilled = false;
		switch (npc.getNpcId()) {
			case 234186, 234187, 234189 -> points = 120;
			case 234751, 234752, 234753 -> points = 200;
			case 234190 -> {
				points = 6000;
				isKunaxKilled = true;
			}
		}
		if (points > 0)
			updatePoints(points, player.getRace(), npc.getObjectTemplate().getL10n(), player);
		if (isKunaxKilled)
			onStop(true);
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_STATUS, player.getObjectId(), 60), getTime()));
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.INIT_PLAYER, player.getObjectId(), 60), getTime()));
		PacketSendUtility.sendPacket(player, new SM_DIE(player.canUseRebirthRevive(), false, 0, 8));
		if (lastAttacker instanceof Player) {
			if (lastAttacker.getRace() != player.getRace()) {
				int killPoints = 200;
				// After 10 minutes the outplayed faction gets bonus points
				if (idi.isStartProgress() && getTime() >= 600000 && player.getRace() != idi.getRaceWithHighestPoints())
					killPoints += 100;

				updatePoints(killPoints, lastAttacker.getRace(), null, (Player) lastAttacker);
				PacketSendUtility.sendPacket((Player) lastAttacker, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE_FOR_ENEMY(killPoints));
				idi.incrementKillsByRace(lastAttacker.getRace());
				sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_SCORE, instance.getPlayersInside()), getTime()));
			}
		}
		updatePoints(-100, player.getRace(), null, player);
		return true;
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
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		idi.teleportToStartPosition(player, instance);
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_STATUS, player.getObjectId(), 0), getTime()));
		return true;
	}

	@Override
	public void onEnterInstance(Player player) {
		Npc forbiddenFlameVent = getNpc(player.getRace() == Race.ASMODIANS ? 802548 : 802549);
		if (forbiddenFlameVent != null)
			setFlameVentNoInteraction(player, forbiddenFlameVent);
		if (!idi.containsPlayer(player.getObjectId()))
			idi.addPlayerReward(new IdgelDomePlayerInfo(player.getObjectId(), player.getRace()));

		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_SCORE, instance.getPlayersInside()), getTime()));
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_STATUS, player.getObjectId(), 0), getTime()));
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_INFO, instance.getPlayersInside()), getTime()));
	}

	@Override
	public void onExitInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.PLAYER_QUIT, player.getObjectId(), 0), getTime()));
	}

	@Override
	public void onPlayerLogOut(Player player) {
		if (player.isDead())
			onReviveEvent(player);
	}

	@Override
	public void onLeaveInstance(Player player) {
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.PLAYER_QUIT, player.getObjectId(), 0), getTime()));
	}

	@Override
	public void onInstanceDestroy() {
		cancelTasks();
	}

	private int getTime() {
		int current = (int) (System.currentTimeMillis() - startTime);
		return switch (idi.getInstanceProgressionType()) {
			case PREPARING -> 120000 - current;
			case START_PROGRESS, END_PROGRESS -> 1200000 - current;
			default -> 0;
		};
	}

	private void updatePoints(int points, Race race, String npcL10n, Player player) {
		if (!idi.isStartProgress())
			return;

		idi.addPointsByRace(race, points);
		if (npcL10n != null)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(npcL10n, points));
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_SCORE, instance.getPlayersInside()), getTime()));
		sendPacket(new SM_INSTANCE_SCORE(instance.getMapId(), new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_INFO, instance.getPlayersInside()), getTime()));
	}

	private void sendPacket(AionServerPacket packet) {
		PacketSendUtility.broadcastToMap(instance, packet);
	}

	private void cancelTasks() {
		for (Future<?> task : tasks)
			if (task != null && !task.isCancelled())
				task.cancel(true);
	}

	@Override
	public InstanceReward<?> getInstanceReward() {
		return idi;
	}
}
