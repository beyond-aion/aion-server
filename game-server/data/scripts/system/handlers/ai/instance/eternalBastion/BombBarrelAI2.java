package ai.instance.eternalBastion;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ViAl
 */
@AIName("bomb_barrel")
public class BombBarrelAI2 extends NpcAI2 {

	private AtomicBoolean exploded = new AtomicBoolean(false);
	
	@Override
	protected void handleAttack(Creature creature) {
		if(exploded.compareAndSet(false, true)) {
			AI2Actions.useSkill(this, 18407);
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					AI2Actions.deleteOwner(BombBarrelAI2.this);
				}

			}, 1500);
		}
	}

}
