package instance.pvp;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

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
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.instanceinfo.IdgelDomeScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Ritsu, Estrayl
 */
@InstanceID(301310000)
public class IdgelDomeInstance extends GeneralInstanceHandler {

	private List<WorldPosition> chestPositions = new ArrayList<>();
	private List<Future<?>> tasks = new ArrayList<>();
	private IdgelDomeInfo idi;
	private long startTime;

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		idi = new IdgelDomeInfo(mapId, instanceId);
		idi.setInstanceProgressionType(InstanceProgressionType.PREPARING);
		startTime = System.currentTimeMillis();
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> onStart(), 120000));
	}

	private void onStart() {
		idi.setInstanceProgressionType(InstanceProgressionType.START_PROGRESS);
		startTime = System.currentTimeMillis();
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PROGRESS), idi, getTime()));
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_INFO, instance.getPlayersInside()), idi, getTime()));
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_SCORE, instance.getPlayersInside()), idi, getTime()));
		instance.getDoors().values().forEach(d -> d.setOpen(true));
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
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PROGRESS), idi, getTime()));
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_SCORE, instance.getPlayersInside()), idi, getTime()));
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_INFO, instance.getPlayersInside()), idi, getTime()));
		Race winningrace = idi.getWinningRace();
		RewardItem spacer = new RewardItem(0, 0);
		int winnerBonusAp = Rnd.get(10000, 14000);
		int loserBonusAp = Rnd.get(3000, 6000);
		instance.forEachPlayer(p -> {
			IdgelDomePlayerInfo reward = idi.getPlayerReward(p.getObjectId());
			if (reward.getRace() == winningrace) {
				reward.setBaseAp(IdgelDomeInfo.WIN_AP);
				reward.setBonusAp(winnerBonusAp);
				reward.setBaseGp(50);
				reward.addItemReward(new RewardItem(186000242, 3));
				reward.addItemReward(new RewardItem(188053030, 1));
				reward.addItemReward(spacer);
				if (isKunaxKilled) {
					RewardItem mythicKunaxEq = null;
					if (Rnd.chance() < 20)
						mythicKunaxEq = idi.getMythicKunaxEquipment(p);
					reward.addItemReward(mythicKunaxEq == null ? spacer : mythicKunaxEq);
					reward.addItemReward(new RewardItem(188053032, 1));
				} else {
					reward.addItemReward(spacer);
					reward.addItemReward(spacer);
				}
			} else {
				reward.setBaseAp(IdgelDomeInfo.DEFEAT_AP);
				reward.setBonusAp(loserBonusAp);
				reward.setBaseGp(10);
				reward.addItemReward(new RewardItem(186000242, 1));
				reward.addItemReward(new RewardItem(188053031, 1));
				reward.addItemReward(spacer);
				reward.addItemReward(spacer);
				reward.addItemReward(spacer);
			}
			sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.SHOW_REWARD, p.getObjectId(), 0), idi, getTime()));
			AbyssPointsService.addAp(p, reward.getBaseAp() + reward.getBonusAp());
			GloryPointsService.addGp(p, reward.getBaseGp() + reward.getBonusGp());
			for (RewardItem ri : reward.getItemRewards())
				if (ri.getId() != 0)
					ItemService.addItem(p, ri.getId(), ri.getCount());
		});
		instance.getNpcs().forEach(n -> n.getController().delete());
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> {
			for (Player player : instance.getPlayersInside()) {
				if (player.isDead())
					PlayerReviveService.duelRevive(player);
				tasks.add(ThreadPoolManager.getInstance().schedule(() -> onExitInstance(player), 30000));
			}
			AutoGroupService.getInstance().unRegisterInstance(instanceId);
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
		Player player = npc.getAggroList().getMostPlayerDamage();
		if (player == null)
			return;

		int points = 0;
		boolean isKunaxKilled = false;
		switch (npc.getNpcId()) {
			case 234186:
			case 234187:
			case 234189:
				points = 120;
				break;
			case 234751:
			case 234752:
			case 234753:
				points = 200;
				break;
			case 234190:
				points = 6000;
				isKunaxKilled = true;
				break;
		}
		if (points > 0)
			updatePoints(points, player.getRace(), npc.getObjectTemplate().getL10n(), player);
		if (isKunaxKilled)
			onStop(true);
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) {
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_STATUS, player.getObjectId(), 60), idi, getTime()));
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.INIT_PLAYER, player.getObjectId(), 60), idi, getTime()));
		PacketSendUtility.sendPacket(player, new SM_DIE(player.canUseRebirthRevive(), false, 0, 8));
		if (lastAttacker instanceof Player) {
			if (lastAttacker.getRace() != player.getRace()) {
				int killPoints = 200;
				// After 10 minutes the outplayed faction gets bonus points
				if (idi.isStartProgress() && getTime() >= 600000 && !player.getRace().equals(idi.getWinningRace()))
					killPoints += 100;

				updatePoints(killPoints, lastAttacker.getRace(), null, (Player) lastAttacker);
				PacketSendUtility.sendPacket((Player) lastAttacker, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE_FOR_ENEMY(killPoints));
				idi.incrementKillsByRace(lastAttacker.getRace());
				sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_SCORE, instance.getPlayersInside()), idi, getTime()));
			}
		}
		updatePoints(-100, player.getRace(), null, player);
		return true;
	}

	private void spawnChest() {
		WorldPosition p = Rnd.get(chestPositions);
		chestPositions.remove(p);
		spawn(702581 + Rnd.get(0, 2), p.getX(), p.getY(), p.getZ(), p.getHeading());
	}

	private void scheduleChestRespawn() {
		tasks.add(ThreadPoolManager.getInstance().schedule(() -> spawnChest(), 120000));
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 702581:
			case 702582:
			case 702583:
				chestPositions.add(npc.getPosition());
				scheduleChestRespawn();
				break;
			case 802548:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_FORTRESS_RE_FIRESPAWN_A());
				break;
			case 802549:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_FORTRESS_RE_FIRESPAWN_B());
				break;
		}
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME());
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		idi.teleportToStartPosition(player);
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_STATUS, player.getObjectId(), 0), idi, getTime()));
		return true;
	}

	@Override
	public void onEnterInstance(Player player) {
		Npc forbiddenFlameVent = getNpc(player.getRace() == Race.ASMODIANS ? 802548 : 802549);
		if (forbiddenFlameVent != null)
			setFlameVentNoInteraction(player, forbiddenFlameVent);
		if (!idi.containsPlayer(player.getObjectId()))
			idi.addPlayerReward(new IdgelDomePlayerInfo(player.getObjectId(), player.getRace()));

		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_SCORE, instance.getPlayersInside()), idi, getTime()));
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_STATUS, player.getObjectId(), 0), idi, getTime()));
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_INFO, instance.getPlayersInside()), idi, getTime()));
	}

	@Override
	public void onExitInstance(Player player) {
		TeleportService.moveToInstanceExit(player, mapId, player.getRace());
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.PLAYER_QUIT, player.getObjectId(), 0), idi, getTime()));
	}

	@Override
	public void onPlayerLogOut(Player player) {
		if (player.isDead())
			onReviveEvent(player);
	}

	@Override
	public void onLeaveInstance(Player player) {
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.PLAYER_QUIT, player.getObjectId(), 0), idi, getTime()));
	}

	@Override
	public void onInstanceDestroy() {
		cancelTasks();
	}

	private int getTime() {
		int current = (int) (System.currentTimeMillis() - startTime);
		switch (idi.getInstanceProgressionType()) {
			case PREPARING:
				return 120000 - current;
			case START_PROGRESS:
			case END_PROGRESS:
				return 1200000 - current;
			default:
				return 0;
		}
	}

	private void updatePoints(int points, Race race, String npcL10n, Player player) {
		if (!idi.isStartProgress())
			return;

		idi.addPointsByRace(race, points);
		if (npcL10n != null)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_GET_SCORE(npcL10n, points));
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_SCORE, instance.getPlayersInside()), idi, getTime()));
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idi, InstanceScoreType.UPDATE_PLAYER_INFO, instance.getPlayersInside()), idi, getTime()));
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
