package ai.instance.sauroBase;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.ai.Percentage;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.SummonerAI;

/**
 * @author Estrayl, modified March 22nd, 2018
 */
@AIName("brigade_general_sheba")
public class BrigadeGeneralShebaAI extends SummonerAI {

	public BrigadeGeneralShebaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleBeforeSpawn(Percentage percent) {
		if (percent.getPercent() == 25 || percent.getPercent() == 10) {
			ThreadPoolManager.getInstance().schedule(() -> {
				if (!isDead())
					getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21186, 1, 100)));
			}, 11000);
		}
	}
}
