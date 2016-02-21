package ai.instance.stonespearReach;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import java.util.concurrent.Future;

/**
 * Created by Yeats on 20.02.2016.
 */
@AIName("stonespear_guardian_stone")
public class StonespearGuardianStoneAI2 extends NpcAI2 {


    private Future<?> task;

    @Override
    public void handleSpawned() {
        super.handleSpawned();
        startTask();
    }

    private void startTask() {
        task = ThreadPoolManager.getInstance().schedule((Runnable) () -> {
            if (getOwner() != null) {
                for (Player p : getOwner().getKnownList().getKnownPlayers().values()) {
                    PacketSendUtility.sendPacket(p, new SM_SYSTEM_MESSAGE(1402925));
                }
                getOwner().getController().onDelete();
            }
        }, 55000); //message says 2mins but its actually only ~1min.
    }

    @Override
    public void handleDied() {
        super.handleDied();
        getOwner().getController().onDelete();
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
    }

    @Override
    public void handleDespawned() {
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
        super.handleDespawned();
    }

    @Override
    protected AIAnswer pollInstance(AIQuestion question) {
        switch (question) {
            case SHOULD_DECAY:
                return AIAnswers.POSITIVE;
            case SHOULD_RESPAWN:
                return AIAnswers.NEGATIVE;
            case SHOULD_REWARD:
                return AIAnswers.NEGATIVE;
            case SHOULD_LOOT:
                return AIAnswers.NEGATIVE;
            default:
                return null;
        }
    }
}
