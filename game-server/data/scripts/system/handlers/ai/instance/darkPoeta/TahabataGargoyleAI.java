package ai.instance.darkPoeta;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl 12.06.2017
 */
@AIName("tahabata_gargoyle")
public class TahabataGargoyleAI extends AggressiveNpcAI {

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate) {
		if (skillTemplate.getSkillId() == 18219)
			getOwner().getController().delete();
	}
}
