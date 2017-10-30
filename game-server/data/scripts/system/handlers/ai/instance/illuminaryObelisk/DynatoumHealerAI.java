package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("dynatoum_healer")
public class DynatoumHealerAI extends GeneralNpcAI {

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
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21535, 1, 100, 0, 10000)));
		}
	}

	@Override
	public void onEndUseSkill(NpcSkillEntry usedSkill) {
		if (usedSkill.getSkillId() == 21535)
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21535, 1, 100, 0, 10000)));
	}
}
