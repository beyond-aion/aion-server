package ai.instance.shugoImperialTomb;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Ritsu
 */
@AIName("shugo_tomb_modo")
public class ShugoTombModoAI extends ShugoTombAttackerAI {

	public ShugoTombModoAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		handleHate();
	}
}
