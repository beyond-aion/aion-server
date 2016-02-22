package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Created by Yeats on 20.02.2016.
 */
@AIName("aggressive_boss_summon")
public class AggressiveBossSummonNpcAI2 extends AggressiveNpcAI2 {

    private Creature spawner = null;
    private boolean searched = false;

    @Override
    public void handleAttackComplete() {
        super.handleAttackComplete();
        if (searched && (spawner == null || spawner.getLifeStats().isAlreadyDead())) {
            getOwner().getController().onDelete();
        }
    }

    @Override
    public void handleSpawned() {
        super.handleSpawned();
        findCreator();
    }

    private void findCreator() {
        if (getOwner().getCreatorId() == 0) {
            getOwner().getController().onDelete();
            return;
        }
        ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                for (VisibleObject obj : getOwner().getKnownList().getKnownObjects().values()) {
                    if (obj != null && obj instanceof Creature) {
                        if (obj.getObjectId() == getOwner().getCreatorId()) {
                            spawner = (Creature) obj;
                            break;
                        }
                    }
                }
                searched = true;
            }
        }, 5000);
    }

    @Override
    public void handleDied() {
        super.handleDied();
        getOwner().getController().onDelete();
    }
}
