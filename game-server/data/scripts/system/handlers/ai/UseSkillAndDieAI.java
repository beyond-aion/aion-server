package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.skill.NpcSkillList;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillConditionTemplate;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats
 */
@AIName("useSkillAndDie")
public class UseSkillAndDieAI extends NpcAI {

	boolean canDie = true;

	public UseSkillAndDieAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		scheduleSkill();
	}

	private void scheduleSkill() {
		if (getOwner().getCreatorId() == 0) {
			System.out.println("brüfte");
			despawn(1);
			return;
		}
		NpcSkillList skillList = getOwner().getSkillList();
		if (skillList.getNpcSkills().isEmpty()) {
			despawn(1);
			return;
		}
		NpcSkillConditionTemplate conditionTemplate = skillList.getNpcSkills().get(0).getConditionTemplate();
		if (conditionTemplate != null) {
			canDie = conditionTemplate.canDie();
			ThreadPoolManager.getInstance().schedule(() -> {
				if (getOwner() != null && !getOwner().isDead()) {
					Creature spawner = findCreator(getOwner().getCreatorId());
					if (spawner != null && !spawner.isDead()) {
						SkillEngine.getInstance()
							.getSkill(getOwner(), skillList.getNpcSkills().get(0).getSkillId(), skillList.getNpcSkills().get(0).getSkillLevel(), getOwner())
							.useSkill();
					}
				}
				despawn(conditionTemplate.getDespawnTime());
			}, conditionTemplate.getDelay());
		}
	}

	private Creature findCreator(int objId) {
		for (VisibleObject obj : getOwner().getKnownList().getKnownObjects().values()) {
			if (obj instanceof Creature) {
				if (obj.getObjectId() == objId) {
					return (Creature) obj;
				}
			}
		}
		return null;
	}

	private void despawn(int despawn_time) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (getOwner() != null && !getOwner().isDead())
					getOwner().getController().delete();
			}
		}, despawn_time);
	}

	@Override
	public int modifyDamage(Creature attacker, int damage, Effect effect) {
		if (!canDie)
			return 0;
		else
			return damage;
	}

	@Override
	public void handleDied() {
		super.handleDied();
		getOwner().getController().delete();
	}
}
