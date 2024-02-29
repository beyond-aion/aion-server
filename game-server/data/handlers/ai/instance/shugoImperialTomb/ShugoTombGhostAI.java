package ai.instance.shugoImperialTomb;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("shugo_tomb_ghost")
public class ShugoTombGhostAI extends AggressiveNpcAI {

	public ShugoTombGhostAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		getOwner().setState(CreatureState.ACTIVE, true);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getObjectId()));
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, ALLOW_DECAY -> switch (getNpcId()) {
				case 219505, 219506, 219507 -> false;
				default -> true;
			};
			default -> super.ask(question);
		};
	}
}
