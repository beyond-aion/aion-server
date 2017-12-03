package ai.siege;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Source
 */
@AIName("incarnate")
public class IncarnateAI extends SiegeNpcAI {

	public IncarnateAI(Npc owner) {
		super(owner);
	}

	// spawn for quest
	@Override
	protected void handleDied() {
		super.handleDied();
		if (getOwner().getNpcId() == 259614) {
			spawn(701237, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
			despawnClaw();
		}
	}

	private void despawnClaw() {
		final Npc claw = getPosition().getWorldMapInstance().getNpc(701237);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				claw.getController().delete();
			}
		}, 60000 * 5);
	}
}
