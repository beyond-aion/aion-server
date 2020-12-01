package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("camp_defense_cannon")
public class CampDefenseCannonAI extends AggressiveNpcAI {

	public CampDefenseCannonAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleFinishAttack() {
		if (!canThink())
			return;
		Npc npc = getOwner();
		EmoteManager.emoteStopAttacking(npc);
		npc.getController().loseAggro(false);
		npc.setSkillNumber(0);
	}
}
