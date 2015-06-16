package instance;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.drop.DropItem;
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
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Cheatkiller, Tibald
 *
 */
@InstanceID(300480000)
public class DanuarMysticariumInstance extends GeneralInstanceHandler {

    private Map<Integer, StaticDoor> doors;
    private Future<?> instanceTimer;
    private long startTime;
    private NormalReward instanceReward;
    private boolean isInstanceDestroyed;
    private Future<?> failTimerTask;
    private boolean isSpawnedPrisoner1 = false;
    private boolean isSpawnedPrisoner2 = false;
    private boolean isSpawnedPrisoner3 = false;
    private boolean isSpawnedPrisoner4 = false;
    private boolean isSpawnedPrisoner5 = false;
    private boolean isSpawnedPrisoner6 = false;
    private boolean isSpawnedPrisoner7 = false;
    private boolean isSpawnedPrisoner8 = false;

    @Override
    public void onOpenDoor(int door) {
        switch (door) {
            case 3:
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
            case 101:
                if (isSpawnedPrisoner1) {
                    return;
                }
                isSpawnedPrisoner1 = true;
                spawn(230058, 213.9430f, 508.9750f, 153.2284f, (byte) 110);
                break;
            case 6:
                if (isSpawnedPrisoner2) {
                    return;
                }
                isSpawnedPrisoner2 = true;
                spawn(230057, 226.7380f, 529.7029f, 153.03912f, (byte) 100);
                break;
            case 7:
                if (isSpawnedPrisoner3) {
                    return;
                }
                isSpawnedPrisoner3 = true;
                spawn(230051, 242.3215f, 540.8343f, 152.59f, (byte) 93);
                break;
            case 8:
                if (isSpawnedPrisoner4) {
                    return;
                }
                isSpawnedPrisoner4 = true;
                spawn(230052, 262.3589f, 544.4155f, 150.5014f, (byte) 83);
                break;
            case 10:
                if (isSpawnedPrisoner5) {
                    return;
                }
                isSpawnedPrisoner5 = true;
                spawn(230053, 296.3592f, 547.9075f, 148.7211f, (byte) 83);
                break;
            case 11:
                if (isSpawnedPrisoner6) {
                    return;
                }
                isSpawnedPrisoner6 = true;
                spawn(230054, 317.7392f, 544.2635f, 148.7996f, (byte) 80);
                break;
            case 12:
                if (isSpawnedPrisoner7) {
                    return;
                }
                isSpawnedPrisoner7 = true;
                spawn(230056, 337.1739f, 531.7154f, 148.4716f, (byte) 73);
                break;
            case 13:
                if (isSpawnedPrisoner8) {
                    return;
                }
                isSpawnedPrisoner8 = true;
                spawn(230055, 346.2431f, 511.4081f, 148.1805f, (byte) 66);
                break;
        }
    }

    @Override
    public void onDie(Npc npc) {
        Creature master = npc.getMaster();
        if (master instanceof Player) {
            return;
        }

        int npcId = npc.getNpcId();
        switch (npcId) {
            case 230080:
            case 230081:
                addPoints(npc, 3051);
                cancelFailTask();
                checkRank(instanceReward.getPoints(), npc);
                break;
            case 230051:
            case 230052:
            case 230053:
            case 230054:
            case 230055:
            case 230056:
            case 230057:
            case 230058:
                addPoints(npc, 2010);
                break;
            case 230074:
                addPoints(npc, 1125);
                break;
            case 230065:
            case 230066:
            case 230067:
            case 230078:
            case 230079:
                addPoints(npc, 285);
                break;
            case 230062:
            case 230063:
            case 230064:
                addPoints(npc, 150);
                break;
        }
    }

    @Override
    public void handleUseItemFinish(Player player, Npc npc) {
        switch (npc.getNpcId()) {
            case 831146:
                addPoints(npc, 500);
                npc.getController().onDelete();
                break;
        }
    }

    private void addPoints(Npc npc, int points) {
        if (instanceReward.getInstanceScoreType().isStartProgress()) {
            instanceReward.addPoints(points);
            sendPacket(npc.getObjectTemplate().getNameId(), points);
        }
    }

    private int getTime() {
        long result = System.currentTimeMillis() - startTime;
        if (instanceReward.getInstanceScoreType().isPreparing()) {
            return (int) (60000 - result);
        } else if (instanceReward.getInstanceScoreType().isStartProgress() && result < 1801000) {
            return (int) (1800000 - result);
        }
        return 0;
    }

    private void sendPacket(final int nameId, final int point) {
        instance.doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player player) {
                if (nameId != 0) {
                    PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400237, new DescriptionId(nameId * 2 + 1), point));
                }
                PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(new NormalScoreInfo(instanceReward), instanceReward, getTime()));
            }

        });
    }

    private int checkRank(int totalPoints) {
        return checkRank(totalPoints, null);
    }

    protected int checkRank(int totalPoints, Npc npc) {
        int timeRemain = getTime();
        int rank = 0;
        if (timeRemain > 1080000 && totalPoints >= 29911) {
            instanceReward.setFinalAp(1402);
            instanceReward.setRewardItem1(186000239);
            instanceReward.setRewardItem1Count(12);
            instanceReward.setRewardItem2(186000242);
            instanceReward.setRewardItem2Count(1);
            instanceReward.setRewardItem3(188052543);
            instanceReward.setRewardItem3Count(1);
            rank = 1;
        } else if (timeRemain > 960000 && totalPoints >= 21682) {
            instanceReward.setFinalAp(1020);
            instanceReward.setRewardItem1(186000239);
            instanceReward.setRewardItem1Count(8);
            instanceReward.setRewardItem2(186000243);
            instanceReward.setRewardItem2Count(3);
            instanceReward.setRewardItem3(188052547);
            instanceReward.setRewardItem3Count(1);
            rank = 2;
        } else if (timeRemain > 840000 && totalPoints > 14824) {
            instanceReward.setFinalAp(892);
            instanceReward.setRewardItem1(186000239);
            instanceReward.setRewardItem1Count(7);
            instanceReward.setRewardItem2(186000243);
            instanceReward.setRewardItem2Count(1);
            rank = 3;
        } else if (timeRemain > 360000 && totalPoints > 9338) {
            instanceReward.setFinalAp(765);
            instanceReward.setRewardItem1(186000239);
            instanceReward.setRewardItem1Count(6);
            rank = 4;
        } else if (timeRemain > 120000 && totalPoints > 6595) {
            instanceReward.setFinalAp(382);
            instanceReward.setRewardItem1(186000239);
            instanceReward.setRewardItem1Count(3);
            rank = 5;
        } else {
            //No Rewards
            rank = 8;
        }
        instanceReward.setInstanceScoreType(InstanceScoreType.END_PROGRESS);
        instanceReward.setRank(rank);
        doReward(rank, npc);
        return rank;
    }

    private void doReward(int rank, Npc boss) {
        for (Npc npc : instance.getNpcs()) {
            if (npc != boss) {
                npc.getController().onDelete();
            }
        }
        spawn(701572, 558.2879f, 414.86456f, 96.81002f, (byte) 40);
        switch (rank) {
            case 1:
                spawn(230082, 556.36096f, 416.39127f, 96.81002f, (byte) 100);
            case 2:
            case 3:
                spawn(701526, 546.6725f, 413.5222f, 96.0598f, (byte) 53, 113);
            case 4:
                Set<DropItem> dropItems = DropRegistrationService.getInstance().getCurrentDropMap().get(boss.getObjectId());
                dropItems.add(DropRegistrationService.getInstance().regDropItem(1, 0, boss.getNpcId(), 185000127, 1));
                break;
            default:
                break;
        }

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
        spawn(Rnd.get(230080, 230081), 520.6471f, 468.8483f, 95.58755f, (byte) 40);
        if (instanceTimer == null) {
            startTime = System.currentTimeMillis();
            instanceTimer = ThreadPoolManager.getInstance().schedule(new Runnable() {

                @Override
                public void run() {
                    instanceReward.setInstanceScoreType(InstanceScoreType.START_PROGRESS);
                    sendPacket(0, 0);
                    doors.get(3).setOpen(true);
                    startFailTask();
                }
            }, 62000);
        }
    }

    private void startFailTask() {
        failTimerTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                checkRank(0);
            }
        }, 1800000);
    }

    private void cancelFailTask() {
        if (failTimerTask != null && !failTimerTask.isCancelled()) {
            failTimerTask.cancel(true);
        }
    }

    @Override
    public boolean onDie(final Player player, Creature lastAttacker) {
        PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0
                : lastAttacker.getObjectId()), true);

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
