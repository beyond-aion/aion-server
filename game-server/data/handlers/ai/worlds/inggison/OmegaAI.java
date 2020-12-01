package ai.worlds.inggison;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.ai.Percentage;
import com.aionemu.gameserver.model.templates.ai.SummonGroup;

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
	protected void handleSpawnFinished(SummonGroup summonGroup) {
		if (summonGroup.getNpcId() == 281948)
			AIActions.useSkill(this, 18671);
	}

	@Override
	protected boolean checkBeforeSpawn() {
		for (Player player : getKnownList().getKnownPlayers().values())
			if (isInRange(player, 30))
				return true;
		return false;
	}
}
