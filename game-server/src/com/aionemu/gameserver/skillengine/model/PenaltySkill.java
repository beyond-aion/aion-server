package com.aionemu.gameserver.skillengine.model;

import com.aionemu.gameserver.model.gameobjects.Creature;

public class PenaltySkill extends Skill {

    public PenaltySkill(SkillTemplate skillTemplate, Creature effector, int skillLevel, Creature firstTarget) {
        super(skillTemplate, effector, skillLevel, effector, null);
        setFirstTarget(firstTarget);
    }

    @Override
    public void calculateAndSetCastDuration() {
    }

    @Override
    public boolean useSkill() {
        super.useWithoutPropSkill();
        return true;
    }

    @Override
    public void initializeSkillMethod() {
        skillMethod = SkillMethod.PENALTY;
    }
}
