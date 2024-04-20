package ai.instance.idgelDome;

import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.ActionItemNpcAI;

/**
 * @author Ritsu, Estrayl
 */
@AIName("flame_vent")
public class FlameVentAI extends ActionItemNpcAI {

	public FlameVentAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		switch (getNpcId()) {
			case 802548:
				Npc repelling1 = (Npc) spawn(855010, 234.4599f, 194.21619f, 79.589996f, (byte) 119);
				Npc fx1 = (Npc) spawn(702405, 232.19067f, 185.85762f, 80.199997f, (byte) 75);
				Npc fx2 = (Npc) spawn(702405, 238.54938f, 200.84813f, 80f, (byte) 15);
				repelling1.getController().addTask(TaskId.DESPAWN,
					ThreadPoolManager.getInstance().schedule(() -> repelling1.getController().delete(), 60, TimeUnit.SECONDS));
				fx1.getController().addTask(TaskId.DESPAWN,
					ThreadPoolManager.getInstance().schedule(() -> fx1.getController().delete(), 60, TimeUnit.SECONDS));
				fx2.getController().addTask(TaskId.DESPAWN,
					ThreadPoolManager.getInstance().schedule(() -> fx2.getController().delete(), 60, TimeUnit.SECONDS));
				getOwner().getController().die();
				break;
			case 802549:
				Npc repelling2 = (Npc) spawn(855010, 294.62436f, 324.11783f, 79.790443f, (byte) 119);
				Npc fx3 = (Npc) spawn(702405, 290.67102f, 317.26324f, 80.099998f, (byte) 75);
				Npc fx4 = (Npc) spawn(702405, 297.08356f, 332.35382f, 80.099998f, (byte) 15);
				repelling2.getController().addTask(TaskId.DESPAWN,
					ThreadPoolManager.getInstance().schedule(() -> repelling2.getController().delete(), 60, TimeUnit.SECONDS));
				fx3.getController().addTask(TaskId.DESPAWN,
					ThreadPoolManager.getInstance().schedule(() -> fx3.getController().delete(), 60, TimeUnit.SECONDS));
				fx4.getController().addTask(TaskId.DESPAWN,
					ThreadPoolManager.getInstance().schedule(() -> fx4.getController().delete(), 60, TimeUnit.SECONDS));
				getOwner().getController().die();
				break;
		}
	}
}
