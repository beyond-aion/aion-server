package instance;

import java.util.Map;
import java.util.concurrent.Future;

import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.NormalReward;
import com.aionemu.gameserver.network.aion.instanceinfo.NormalScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Cheatkiller, Tibald
 */
@InstanceID(300580000)
public class VoidCubeInstance extends GeneralInstanceHandler {

	private Map<Integer, StaticDoor> doors;
	private Future<?> instanceTimer;
	private long startTime;
	private NormalReward instanceReward;
	private boolean isInstanceDestroyed;
	private Future<?> failTimerTask;

	// TODO static initialization random pos chests (max 5)
	// TODO static initialization random pos yellow door

	@Override
	public void onOpenDoor(int door) {
		switch (door) {
			case 26:
				instanceReward.setInstanceScoreType(InstanceScoreType.START_PROGRESS);
				startTime = System.currentTimeMillis();
				sendPacket(0, 0);
				if (instanceTimer != null) {
					instanceTimer.cancel(false);
				}
				if (failTimerTask == null) {
					startFailTask();
				}
				break;
		}
	}

	@Override
	public void onDie(Npc npc) {
		Creature master = npc.getMaster();
		if (master instanceof Player)
			return;

		int npcId = npc.getNpcId();
		switch (npcId) {
			case 230387:
				cancelFailTask();
				checkRank(instanceReward.getPoints());
				break;
			case 230092:
			case 230093:
				addPoints(npc, 1000);
				break;
			case 230355:
				addPoints(npc, 900);
				break;
			case 230086:
				addPoints(npc, 450);
				break;
			case 230088:
			case 230089:
				addPoints(npc, 225);
				break;
			case 230085:
			case 230411:
			case 230412:
				addPoints(npc, 200);
				break;
			case 230090:
			case 230091:
			case 230098:
			case 230099:
				addPoints(npc, 150);
				break;
			case 230095:
			case 230097:
				addPoints(npc, 75);
				break;
			case 230087:
				addPoints(npc, 50);
				break;
			case 230084:
			case 230094:
				addPoints(npc, 25);
				break;

		}
	}

	@Override
	public void onApplyEffect(Creature effector, Creature effected, int skillId) {
		switch (skillId) {
			case 10600:
				effected.getController().onDie(effector);
				break;
		}
	}

	private void addPoints(Npc npc, int points) {
		if (instanceReward.getInstanceScoreType().isStartProgress()) {
			instanceReward.addPoints(points);
			sendPacket(npc.getObjectTemplate().getNameId(), points);
			if (getTime() > 840000 && instanceReward.getPoints() >= 10000) {
				instanceReward.setFinalAp(1042);
				instanceReward.setRewardItem1(186000240);
				instanceReward.setRewardItem1Count(12);
				instanceReward.setRewardItem2(186000242);
				instanceReward.setRewardItem2Count(1);
				instanceReward.setRewardItem3(188052543);
				instanceReward.setRewardItem3Count(1);
				instanceReward.setInstanceScoreType(InstanceScoreType.END_PROGRESS);
				instanceReward.setRank(1);
				for (Npc npcs : instance.getNpcs())
					npcs.getController().onDelete();
				doReward(1);
				cancelFailTask();
			}
		}
	}

	private int getTime() {
		long result = System.currentTimeMillis() - startTime;
		if (instanceReward.getInstanceScoreType().isPreparing()) {
			return (int) (120000 - result);
		}
		else if (instanceReward.getInstanceScoreType().isStartProgress() && result < 1501000) {
			return (int) (1500000 - result);
		}
		return 0;
	}

	private void sendPacket(final int nameId, final int point) {
		instance.doOnAllPlayers((Player player) -> {
            if (nameId != 0) {
                PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400237, new DescriptionId(nameId * 2 + 1), point));
            }
            PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(new NormalScoreInfo(instanceReward), instanceReward, getTime()));
        });
	}

	protected int checkRank(int totalPoints) {
		int timeRemain = getTime();
		int rank = 0;
		if (timeRemain > 840000 && totalPoints >= 10000) {
			instanceReward.setFinalAp(1042);
			instanceReward.setRewardItem1(186000240);
			instanceReward.setRewardItem1Count(12);
			instanceReward.setRewardItem2(186000242);
			instanceReward.setRewardItem2Count(1);
			instanceReward.setRewardItem3(188052543);
			instanceReward.setRewardItem3Count(1);
			rank = 1;
		}
		else if (timeRemain > 600000 && totalPoints >= 6800) {
			instanceReward.setFinalAp(1020);
			instanceReward.setRewardItem1(186000240);
			instanceReward.setRewardItem1Count(8);
			instanceReward.setRewardItem2(186000243);
			instanceReward.setRewardItem2Count(4);
			instanceReward.setRewardItem3(188052547);
			instanceReward.setRewardItem3Count(1);
			rank = 2;
		}
		else if (timeRemain > 600000 && totalPoints > 5700) {
			instanceReward.setFinalAp(892);
			instanceReward.setRewardItem1(186000240);
			instanceReward.setRewardItem1Count(7);
			instanceReward.setRewardItem2(186000243);
			instanceReward.setRewardItem2Count(2);
			rank = 3;
		}
		else if (timeRemain > 300000 && totalPoints > 3900) {
			instanceReward.setFinalAp(765);
			instanceReward.setRewardItem1(186000240);
			instanceReward.setRewardItem1Count(6);
			rank = 4;
		}
		else if (timeRemain > 300000 && totalPoints > 1800) {
			instanceReward.setFinalAp(382);
			instanceReward.setRewardItem1(186000240);
			instanceReward.setRewardItem1Count(3);
			rank = 5;
		}
		else {
			// No Rewards
			rank = 8;
		}
		instanceReward.setInstanceScoreType(InstanceScoreType.END_PROGRESS);
		instanceReward.setRank(rank);
		for (Npc npc : instance.getNpcs())
			npc.getController().onDelete();
		doReward(rank);
		return rank;
	}

	private void doReward(int rank) {
		// todo if needed

		instance.doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player player) {
				AbyssPointsService.addAp(player, instanceReward.getFinalAp());
				ItemService.addItem(player, instanceReward.getRewardItem1(), instanceReward.getRewardItem1Count());
				ItemService.addItem(player, instanceReward.getRewardItem2(), instanceReward.getRewardItem2Count());
				ItemService.addItem(player, instanceReward.getRewardItem3(), instanceReward.getRewardItem3Count());
				ItemService.addItem(player, instanceReward.getRewardItem4(), instanceReward.getRewardItem4Count());
				sendPacket(0, 0);
			}
		});
	}

	@Override
	public void onEnterInstance(final Player player) {
		sendPacket(0, 0);
	}

	@Override
	public void onInstanceDestroy() {
		if (instanceTimer != null) {
			instanceTimer.cancel(false);
		}
		cancelFailTask();
		isInstanceDestroyed = true;
		doors.clear();
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		instanceReward = new NormalReward(mapId, instanceId);
		instanceReward.setInstanceScoreType(InstanceScoreType.PREPARING);
		doors = instance.getDoors();
		if (instanceTimer == null) {
			startTime = System.currentTimeMillis();
			instanceTimer = ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					instanceReward.setInstanceScoreType(InstanceScoreType.START_PROGRESS);
					sendPacket(0, 0);
					startFailTask();
				}
			}, 122000);
		}
	}

	private void startFailTask() {
		failTimerTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				checkRank(0);
			}
		}, 1500000);
	}

	private void cancelFailTask() {
		if (failTimerTask != null && !failTimerTask.isCancelled()) {
			failTimerTask.cancel(true);
		}
	}

	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player,
			new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0 : lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}

	@Override
	public void onExitInstance(Player player) {
		if (instanceReward.getInstanceScoreType().isEndProgress()) {
			TeleportService2.moveToInstanceExit(player, mapId, player.getRace());
		}
	}
}
