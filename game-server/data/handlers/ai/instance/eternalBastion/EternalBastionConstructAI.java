package ai.instance.eternalBastion;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Tibald, Estrayl
 */
@AIName("eternal_bastion_construct")
public class EternalBastionConstructAI extends NpcAI {

	private long lastMsgTime;

	public EternalBastionConstructAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleAttack(Creature creature) {
		if (getNpcId() == 831333 || getNpcId() == 831335) {
			if (System.currentTimeMillis() - lastMsgTime > 30000) {
				lastMsgTime = System.currentTimeMillis();
				broadcastMsg(getNpcId() == 831333 ? SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_Notice_03() : SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_Notice_01());
			}
		}
	}

	private void broadcastMsg(SM_SYSTEM_MESSAGE msg) {
		PacketSendUtility.broadcastToMap(getOwner(), msg);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case IS_IMMUNE_TO_ABNORMAL_STATES -> true;
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}
}
