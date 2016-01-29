package ai.instance.elementisForest;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author xTz
 */
@AIName("jurdins_illusion")
public class JurdinsIllusionAI2 extends GeneralNpcAI2 {

	private AtomicBoolean isSpawned = new AtomicBoolean(false);

	@Override
	protected void handleDialogStart(Player player) {
		if (isSpawned.compareAndSet(false, true)) {
			WorldPosition p = getPosition();
			final int instanceId = p.getInstanceId();
			final int worldId = p.getMapId();
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							spawn(worldId, 217238, 472.989f, 798.109f, 130.072f, (byte) 90, 0, instanceId);
							Npc smoke = (Npc) spawn(282465, 472.989f, 798.109f, 130.072f, (byte) 0);
							NpcActions.delete(smoke);
						}

					}, 4000);
					AI2Actions.deleteOwner(JurdinsIllusionAI2.this);
				}

			}, 3000);
		}
		super.handleDialogStart(player);
	}
}
