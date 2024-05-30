package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * Should also be able to request support once by dropping below 35% HP.
 *
 * @author Estrayl
 */
@AIName("ahserion_assault_commander")
public class AhserionAssaultCommanderAI extends AhserionAggressiveNpcAI {

	public AhserionAssaultCommanderAI(Npc owner) {
		super(owner);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 20648) // Pincer Attack
			addHateToRndTarget();
	}
}
