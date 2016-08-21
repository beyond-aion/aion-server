package ai.worlds.inggison;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.ai.Percentage;
import com.aionemu.gameserver.model.ai.SummonGroup;
import com.aionemu.gameserver.model.gameobjects.player.Player;

import ai.SummonerAI2;

/**
 * @author Luzien, xTz
 */
@AIName("omega")
public class OmegaAI2 extends SummonerAI2 {

	@Override
	protected void handleBeforeSpawn(Percentage percent) {
		AI2Actions.useSkill(this, 19189);
		AI2Actions.useSkill(this, 19191);
	}

	@Override
	protected void handleSpawnFinished(SummonGroup summonGroup) {
		if (summonGroup.getNpcId() == 281948)
			AI2Actions.useSkill(this, 18671);
	}

	@Override
	protected boolean checkBeforeSpawn() {
		for (Player player : getKnownList().getKnownPlayers().values())
			if (isInRange(player, 30))
				return true;
		return false;
	}
}
