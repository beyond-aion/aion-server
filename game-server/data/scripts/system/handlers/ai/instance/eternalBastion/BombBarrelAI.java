package ai.instance.eternalBastion;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ViAl
 */
@AIName("bomb_barrel")
public class BombBarrelAI extends NpcAI {

	private AtomicBoolean exploded = new AtomicBoolean(false);

	public BombBarrelAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		if (exploded.compareAndSet(false, true)) {
			AIActions.useSkill(this, 18407);
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					AIActions.deleteOwner(BombBarrelAI.this);
				}

			}, 1500);
		}
	}

}
