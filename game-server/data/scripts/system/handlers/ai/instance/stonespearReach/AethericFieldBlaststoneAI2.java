package ai.instance.stonespearReach;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.handler.MoveEventHandler;
import com.aionemu.gameserver.ai2.handler.TargetEventHandler;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Created by Yeats on 20.02.2016.
 */
@AIName("atheric_field_blaststone")
public class AethericFieldBlaststoneAI2 extends NpcAI2 {

    @Override
    public void handleSpawned() {
        super.handleSpawned();
        if (getNpcId() == 856305) {
            getOwner().getSpawn().setWalkerId("301500000_clown_path");
            WalkManager.startWalking(this);
            getOwner().setState(CreatureState.WALKING);
            PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getOwner().getObjectId()));
        }
    }

    @Override
    public void handleDied() {
        super.handleDied();
        getOwner().getController().onDelete();
    }

    @Override
    protected void handleTargetReached() {
        TargetEventHandler.onTargetReached(this);
    }

    @Override
    protected void handleMoveArrived() {
        super.handleMoveArrived();
        MoveEventHandler.onMoveArrived(this);
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
