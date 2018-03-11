package ai.instance.dragonLordsRefuge;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl March 10th, 2018
 */
@AIName("IDTiamat_2_Kahrun_Sequence")
public class IDTiamat_2_KahrunSequenceAI extends NpcAI {

	public IDTiamat_2_KahrunSequenceAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		PacketSendUtility.broadcastToMap(getOwner(), new SM_CUSTOM_SETTINGS(getOwner().getObjectId(), 0, CreatureType.PEACE.getId(), 0));
		PacketSendUtility.broadcastMessage(getOwner(), 1500604, 1500);
		ThreadPoolManager.getInstance().schedule(() -> {
			getMoveController().moveToPoint(500f, 516.6f, 240.27f);
			setStateIfNot(AIState.WALKING);
			getOwner().setState(CreatureState.ACTIVE, true);
			PacketSendUtility.broadcastToMap(getOwner(), new SM_EMOTION(getOwner(), EmotionType.WALK));
			ThreadPoolManager.getInstance().schedule(() -> spawnOrb(), 1500);
		}, 4000);
	}

	private void spawnOrb() {
		spawn(730625, 503.2197f, 516.6517f, 242.6040f, (byte) 0, 4);
		AIActions.deleteOwner(this);
	}

}
