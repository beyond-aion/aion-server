package ai.instance.rakes;

import ai.ActionItemNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;

@AIName("big_badaboom")
public class BigBadaboomAI2 extends ActionItemNpcAI2 {

    @Override
    protected void handleUseItemFinish(Player player) {
        player.getController().stopProtectionActiveTask();
        int morphSkill = 0;
        switch (getNpcId()) {
            case 231016: //Big Badaboom.
            case 231017: //Bigger Badaboom.
                morphSkill = 0x4E502E;
                break;
        }
        SkillEngine.getInstance().getSkill(getOwner(), morphSkill >> 8, morphSkill & 0xFF, player).useNoAnimationSkill();
        AI2Actions.deleteOwner(this);
    }
}