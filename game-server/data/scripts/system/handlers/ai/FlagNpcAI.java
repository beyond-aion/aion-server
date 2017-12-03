package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Cheatkiller
 */
@AIName("flag")
public class FlagNpcAI extends NpcAI {

	public FlagNpcAI(Npc owner) {
		super(owner);
	}
}
