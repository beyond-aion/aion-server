package ai.instance.dragonLordsRefuge;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl March 10th, 2018
 */
@AIName("tiamat_dragon")
public class TiamatDragonAI extends AggressiveNpcAI {

	public TiamatDragonAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(this, 20920), 4000);
		ThreadPoolManager.getInstance()
			.schedule(() -> getOwner().queueSkill(20984, 1), 300000);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 20920:
				AIActions.useSkill(this, 20975); // Fissure Buff
				AIActions.useSkill(this, 20976); // Wrath Buff
				AIActions.useSkill(this, 20977); // Gravity Buff
				AIActions.useSkill(this, 20978); // Petrification Buff
				break;
			case 20984:
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_IDTIAMAT_TIAMAT_WARNING_MSG());
				break;
		}
	}

	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
