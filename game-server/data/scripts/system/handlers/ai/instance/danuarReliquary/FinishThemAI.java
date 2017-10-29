package ai.instance.danuarReliquary;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;

/**
 * @author Estrayl October 28th, 2017.
 */
@AIName("finish_them")
public class FinishThemAI extends NpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(FinishThemAI.this, 21199), 1000);
	}

	@Override
	public void onEndUseSkill(NpcSkillEntry usedSkill) {
		if (usedSkill.getSkillId() == 21199)
			getOwner().getController().delete();
	}
}
