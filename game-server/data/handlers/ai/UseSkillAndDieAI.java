package ai;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillConditionTemplate;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats
 */
@AIName("useSkillAndDie")
public class UseSkillAndDieAI extends NpcAI {

	private volatile boolean canDie = true;

	public UseSkillAndDieAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		scheduleSkill();
	}

	private void scheduleSkill() {
		NpcSkillEntry skill = getOwner().getSkillList().getSkillOnPosition(0);
		if (skill == null) {
			LoggerFactory.getLogger(getClass()).warn(getOwner() + " has no skill list");
			getOwner().getController().delete();
			return;
		}
		NpcSkillConditionTemplate conditionTemplate = skill.getConditionTemplate();
		if (conditionTemplate != null) {
			canDie = conditionTemplate.canDie();
			ThreadPoolManager.getInstance().schedule(() -> {
				if (getOwner().isDead() || !getOwner().isSpawned())
					return;
				if (getCreatorId() == 0 || getKnownList().getObject(getCreatorId()) instanceof Creature creator && !creator.isDead()) {
					SkillEngine.getInstance().getSkill(getOwner(), skill.getSkillId(), skill.getSkillLevel(), getOwner()).useSkill();
				}
				ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().delete(), conditionTemplate.getDespawnTime());
			}, conditionTemplate.getDelay());
		}
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return canDie ? damage : 0;
	}

	@Override
	public void handleDied() {
		super.handleDied();
		getOwner().getController().delete();
	}
}
