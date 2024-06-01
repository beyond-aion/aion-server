package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("dynatoum_healer")
public class DynatoumHealerAI extends GeneralNpcAI {

	public DynatoumHealerAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startHealing();
	}

	private void startHealing() {
		Npc boss = getPosition().getWorldMapInstance().getNpc(getPosition().getMapId() == 301230000 ? 233740 : 234686);
		if (boss != null) {
			AIActions.targetCreature(this, boss);
			getOwner().queueSkill(21535, 1, 10000);
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21535)
			getOwner().queueSkill(21535, 1, 10000);
	}
}
