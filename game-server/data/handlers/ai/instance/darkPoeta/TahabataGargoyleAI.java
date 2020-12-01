package ai.instance.darkPoeta;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl 12.06.2017
 */
@AIName("tahabata_gargoyle")
public class TahabataGargoyleAI extends AggressiveNpcAI {

	public TahabataGargoyleAI(Npc owner) {
		super(owner);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 18219)
			getOwner().getController().delete();
	}
}
