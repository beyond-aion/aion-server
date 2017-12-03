package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("sinkingsand")
public class SinkingSandAI extends NpcAI {

	public SinkingSandAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		useskill();
	}

	private void useskill() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.useSkill(SinkingSandAI.this, 20723);
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						AIActions.deleteOwner(SinkingSandAI.this);
					}
				}, 1000);
			}
		}, 3000);
	}
}
