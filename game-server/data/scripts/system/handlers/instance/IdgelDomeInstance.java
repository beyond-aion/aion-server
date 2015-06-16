package instance;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_REBIRTH_MASSAGE_ME;

import java.util.Map;
import java.util.concurrent.Future;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.actions.PlayerActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.IdgelDomeReward;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.instance.playerreward.IdgelDomePlayerReward;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.instanceinfo.IdgelDomeScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 *
 * @author Ritsu
 */
@InstanceID(301310000)
public class IdgelDomeInstance extends GeneralInstanceHandler {

	protected IdgelDomeReward idgelDomeReward;
	private Map<Integer, StaticDoor> doors;
	private long instanceTime;
	private Future<?> instanceTask;
	private Future<?> timeCheckTask;
	private Future<?> spawnChestTask;
	private Future<?> spawnBossTask;
	private boolean isInstanceDestroyed = false;

	private void addPlayerToReward(Player player) {
		idgelDomeReward.addPlayerReward(new IdgelDomePlayerReward(player.getObjectId(), player.getRace()));
	}

	private boolean containPlayer(Integer object) {
		return idgelDomeReward.containPlayer(object);
	}

	@Override
	public void onEnterInstance(Player player) {
		if (!containPlayer(player.getObjectId())) {
			addPlayerToReward(player);
		}
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idgelDomeReward, 3, player.getObjectId()), idgelDomeReward, getTime()));
		PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idgelDomeReward, 6, player.getObjectId()), idgelDomeReward, getTime()));
	}

	protected void startInstanceTask() 
	{
		instanceTime = System.currentTimeMillis();
		ThreadPoolManager.getInstance().schedule(new Runnable() 
		{

			@Override
			public void run() {
				openFirstDoors();
				idgelDomeReward.setInstanceScoreType(InstanceScoreType.START_PROGRESS);
				sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idgelDomeReward, 6, 0), idgelDomeReward, getTime()));
				spawnChestTask();
				bossSpawn();
			}

		}, 120000);
		instanceTask = ThreadPoolManager.getInstance().schedule(new Runnable() 
		{

			@Override
			public void run() 
			{
				stopInstance();
			}

		}, 1320000);
	}

	private void spawnChestTask() 
	{
		spawnChestTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() 
		{
			@Override
			public void run() 
			{
				spawn(702581 + Rnd.get(0,2), 253.07826f, 246.24838f, 92.94253f, (byte) 75);
				spawn(702581 + Rnd.get(0,2), 276.37427f, 271.68332f, 92.94253f, (byte) 15);
			}
		}, 0, 300000);
	}

	private void bossSpawn() 
	{
		spawnBossTask = ThreadPoolManager.getInstance().schedule(new Runnable() 
		{

			@Override
			public void run() 
			{
				spawn(234190, 264.4382f, 258.58527f, 88.452042f, (byte) 31); // Destroyer Kunax
				sendMsg(1402598);
				sendMsg(1402367);
			}
		}, 600000);
	}

	public void stopInstance() 
	{
		if (instanceTask != null && !instanceTask.isDone()) 
		{
			instanceTask.cancel(true);
		}
		if (timeCheckTask != null && !timeCheckTask.isDone()) 
		{
			timeCheckTask.cancel(true);
		}
		if (spawnChestTask != null && !spawnChestTask.isDone()) 
		{
			spawnChestTask.cancel(true);
		}
		if (spawnBossTask != null && !spawnBossTask.isDone()) 
		{
			spawnBossTask.cancel(true);
		}
		if (idgelDomeReward.isRewarded()) 
		{
			return;
		}
		idgelDomeReward.setInstanceScoreType(InstanceScoreType.END_PROGRESS);
		final Race winningrace = idgelDomeReward.getWinningRace();
		instance.doOnAllPlayers(new Visitor<Player>() 
			{
			@Override
			public void visit(Player player) 
			{
				IdgelDomePlayerReward reward = idgelDomeReward.getPlayerReward(player.getObjectId());
				if (reward.getRace().equals(winningrace)) 
				{
					reward.setBonusReward(Rnd.get(5000, 7000));
					reward.setGloryPoints(50);
					reward.setFragmentedCeramium(6);
					reward.setIdgelDomeBox(1);
					reward.setBaseReward(IdgelDomeReward.winningPoints);
				} 
				else 
				{
					reward.setBonusReward(Rnd.get(1500, 3000));
					reward.setGloryPoints(10);
					reward.setFragmentedCeramium(2);
					reward.setBaseReward(IdgelDomeReward.looserPoints);
				}
				sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idgelDomeReward, 5, player.getObjectId()), idgelDomeReward, getTime()));
				AbyssPointsService.addAp(player, reward.getBaseReward() + reward.getBonusReward());
				GloryPointsService.addGp(player, reward.getGloryPoints());
				if (reward.getIdgelDomeBox() > 0)
				{
					ItemService.addItem(player, 188053030, reward.getIdgelDomeBox());
				}
			}

			});
		for (Npc npc : instance.getNpcs())
		{
			npc.getController().onDelete();
		}
		ThreadPoolManager.getInstance().schedule(new Runnable()
		{
			@Override
			public void run() 
			{
				if (!isInstanceDestroyed)
				{
					for (Player player : instance.getPlayersInside()) 
					{
						if (PlayerActions.isAlreadyDead(player)) {
							PlayerReviveService.duelRevive(player);
						}
						onExitInstance(player);
					}
					AutoGroupService.getInstance().unRegisterInstance(instanceId);
				}
			}

		}, 10000);
	}

	@Override
	public void onExitInstance(Player player) {
		TeleportService2.moveToInstanceExit(player, mapId, player.getRace());
	}

	@Override
	public void onInstanceDestroy() 
	{
		isInstanceDestroyed = true;
		if (instanceTask != null && !instanceTask.isDone()) 
		{
			instanceTask.cancel(true);
		}
		if (timeCheckTask != null && !timeCheckTask.isDone()) 
		{
			timeCheckTask.cancel(true);
		}
		if (spawnChestTask != null && !spawnChestTask.isDone()) 
		{
			spawnChestTask.cancel(true);
		}
		if (spawnBossTask != null && !spawnBossTask.isDone()) 
		{
			spawnBossTask.cancel(true);
		}
	}

	private void updatePoints(int points, Race race, boolean check, int nameId, Player player) 
	{
		if (check && !idgelDomeReward.isStartProgress())
		{
			return;
		}
		if (nameId != 0)
		{
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400237, new DescriptionId(nameId * 2 + 1), points));
		}
		idgelDomeReward.addPointsByRace(race, points);
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idgelDomeReward, 10, race.equals(Race.ELYOS) ? 0 : 1), idgelDomeReward, getTime()));
		int diff = Math.abs(idgelDomeReward.getAsmodiansPoint().intValue() - idgelDomeReward.getElyosPoints().intValue());
		if (diff >= 30000)
		{
			stopInstance();
		}
	}

	@Override
	public void onDie(Npc npc) 
	{
		Player player = npc.getAggroList().getMostPlayerDamage();
		if (player == null) 
		{
			return;
		}
		int points = 0;
		switch (npc.getNpcId()) 
		{
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
				stopInstance();
				break;
		}
		if (points > 0) 
		{
			updatePoints(points, player.getRace(), true, npc.getObjectTemplate().getNameId(), player);
		}
	}

	public void sendPacket(final AionServerPacket packet) 
	{
		instance.doOnAllPlayers(new Visitor<Player>() 
			{
			@Override
			public void visit(Player player) 
			{
				PacketSendUtility.sendPacket(player, packet);
			}

			});
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) 
	{
		super.onInstanceCreate(instance);
		idgelDomeReward = new IdgelDomeReward(mapId, instanceId);
		idgelDomeReward.setInstanceScoreType(InstanceScoreType.PREPARING);
		doors = instance.getDoors();
		startInstanceTask();
	}

	@Override
	public InstanceReward<?> getInstanceReward() 
	{
		return idgelDomeReward;
	}

	public void openFirstDoors() 
	{
		openDoor(1);
		openDoor(99);
	}

	protected void openDoor(int doorId)
	{
		StaticDoor door = doors.get(doorId);
		if (door != null) {
			door.setOpen(true);
		}
	}

	@Override
	public boolean onReviveEvent(Player player) {
		PacketSendUtility.sendPacket(player, STR_REBIRTH_MASSAGE_ME);
		PlayerReviveService.revive(player, 100, 100, false, 0);
		player.getGameStats().updateStatsAndSpeedVisually();
		idgelDomeReward.portToPosition(player);
		return true;
	}

	@Override
	public boolean onDie(Player player, Creature lastAttacker) 
	{
		sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idgelDomeReward, 3, player.getObjectId()), idgelDomeReward, getTime()));
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0
			: lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), false, 0, 8));
		if (lastAttacker instanceof Player) {
			if (lastAttacker.getRace() != player.getRace()) 
			{
				int killPoints = 200;
				if (idgelDomeReward.isStartProgress() && getTime() >= 600000) // After 10 minutes increase reward points
				{
					killPoints = 300; // TODO: Check on retail
				}
				updatePoints(killPoints, lastAttacker.getRace(), true, 0, (Player) lastAttacker);
				PacketSendUtility.sendPacket((Player) lastAttacker, new SM_SYSTEM_MESSAGE(1400277, killPoints));
				idgelDomeReward.getKillsByRace(lastAttacker.getRace()).increment();
				sendPacket(new SM_INSTANCE_SCORE(new IdgelDomeScoreInfo(idgelDomeReward, 10, lastAttacker.getRace().equals(Race.ELYOS) ? 0 : 1), idgelDomeReward, getTime()));
			}
		}
		updatePoints(-100, player.getRace(), true, 0, player);
		return true;
	}

	private int getTime() 
	{
		long result = System.currentTimeMillis() - instanceTime;
		if (result < 120000) {
			return (int) (120000 - result);
		} else if (result < 1320000) {
			return (int) (1200000 - (result - 120000));
		}
		return 0;
	}
}
