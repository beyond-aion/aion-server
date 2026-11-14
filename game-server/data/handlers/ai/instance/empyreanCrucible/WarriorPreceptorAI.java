package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;

/**
 * @author Luzien, w4terbomb
 */
@AIName("warrior_preceptor")
public class WarriorPreceptorAI extends CrucibleBossAI {

	public WarriorPreceptorAI(Npc owner) {
		super(owner);
	}

	private boolean isLight() {
		return getNpcId() == 217578;
	}

	@Override
	protected void onFightStart() {
		say(isLight() ? 1500207 : 1500209);
		cast(19576, 10);
		cast(19577, 10);
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		if (every(seconds, 30, 40))
			cast(19595, 10, NpcSkillTargetAttribute.RANDOM);
		if (every(seconds, 37, 40))
			cast(19596, 15, NpcSkillTargetAttribute.ME);
		if (every(seconds, 10, 15))
			cast(19598, 15, NpcSkillTargetAttribute.ME);
		if (seconds % 10 == 0) {
			if (passed(80) || passed(40))
				cast(19597, 10);
			if (passed(60) || passed(20))
				cast(19599, 10, NpcSkillTargetAttribute.ME);
		}
	}

	@Override
	protected void handleDied() {
		say(isLight() ? 1500208 : 1500210);
		super.handleDied();
	}
}
