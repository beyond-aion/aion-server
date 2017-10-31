package ai.instance.danuarReliquary;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 * @modified Estrayl October 29th, 2017.
 */
@AIName("malicious_ice_storm")
public class MaliciousIceStormAI extends NpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(MaliciousIceStormAI.this, 21180), 1000);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate) {
		if (skillTemplate.getSkillId() == 21180)
			getOwner().getController().delete();
	}
}
