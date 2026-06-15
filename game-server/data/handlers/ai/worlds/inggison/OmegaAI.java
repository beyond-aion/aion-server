package ai.worlds.inggison;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.ai.Percentage;

import ai.SummonerAI;

/**
 * @author Luzien, xTz
 */
@AIName("omega")
public class OmegaAI extends SummonerAI {

	public OmegaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleBeforeSpawn(Percentage percent) {
		AIActions.useSkill(this, 19189);
		AIActions.useSkill(this, 19191);
	}

	@Override
	protected boolean checkBeforeSpawn() {
		return getKnownList().streamPlayers().anyMatch(player -> isInRange(player, 30));
	}
}
