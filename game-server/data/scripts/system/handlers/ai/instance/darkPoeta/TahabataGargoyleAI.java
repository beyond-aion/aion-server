package ai.instance.darkPoeta;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl 12.06.2017
 */
@AIName("tahabata_gargoyle")
public class TahabataGargoyleAI extends AggressiveNpcAI {

	@Override
	public void onEndUseSkill(NpcSkillEntry usedSkill) {
		if (usedSkill.getSkillId() == 18219)
			getOwner().getController().delete();
	}
}
