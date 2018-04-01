package ai.instance.dragonLordsRefuge;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

import ai.GeneralNpcAI;

/**
 * @author Estrayl March 10th, 2018
 */
@AIName("IDTiamat_2_NPC_Tiamat")
public class IDTiamat_2_NPC_TiamatAI extends GeneralNpcAI {

	public IDTiamat_2_NPC_TiamatAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleBeforeSpawned() {
		super.handleBeforeSpawned();
		getOwner().overrideNpcType(CreatureType.PEACE);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate) {
		if (skillTemplate.getSkillId() == 20919) {
			spawn(219361, 466.7468f, 514.5500f, 417.4044f, (byte) 0); // Dragon Tiamat
			AIActions.deleteOwner(this);
		}
	}
}
