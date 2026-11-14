package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Shulack captain of stage 5: his area attacks come from the untargetable stigma amplifier in the middle of the deck, the captain only tells
 * it when to strike.
 */
@AIName("empyrean_shulack_captain")
public class ShulackCaptainAI extends CrucibleBossAI {

	private int stigmaIn, attackIn, snareIn;
	private boolean secondStigmaSkill;

	public ShulackCaptainAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void onFightStart() {
		PacketSendUtility.broadcastMessage(getOwner(), 341892);
		cast(18196, 37, NpcSkillTargetAttribute.ME);
		stigmaIn = 60;
		attackIn = 30;
		snareIn = 30;
		secondStigmaSkill = false;
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		if (--stigmaIn <= 0) {
			int[] skills = hp > 70 ? new int[] { 18197, 18198 } : hp > 30 ? new int[] { 18199, 18200 } : new int[] { 18198, 18202 };
			stigmaStrike(skills[secondStigmaSkill ? 1 : 0]);
			secondStigmaSkill = !secondStigmaSkill;
			stigmaIn = hp > 70 ? 60 : hp > 30 ? 40 : 20;
		}
		if (--attackIn <= 0) {
			if (hp > 70) {
				cast(18193, 37);
				cast(18195, 37);
				attackIn = 30;
			} else if (hp > 30) {
				cast(18195, 37, NpcSkillTargetAttribute.RANDOM);
				attackIn = 20;
			} else {
				cast(18193, 37);
				cast(18195, 37);
				cast(18196, 37, NpcSkillTargetAttribute.ME);
				attackIn = 20;
			}
		}
		if (hp <= 70 && --snareIn <= 0) {
			cast(18194, 37);
			if (weakestAttacker() instanceof Creature weakest)
				getAggroList().addHate(weakest, 100000);
			snareIn = 60;
		}
	}

	private void stigmaStrike(int skillId) {
		Npc stigma = getPosition().getWorldMapInstance().getNpc(281180);
		if (stigma == null || !(getOwner().getTarget() instanceof Creature target))
			return;
		Skill skill = SkillEngine.getInstance().getSkill(stigma, skillId, 36, target);
		if (skill == null)
			return;
		skill.setFirstTargetRangeCheck(false); // the stigma strikes the whole deck from where it stands
		skill.useNoAnimationSkill();
	}
}
