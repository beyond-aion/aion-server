package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("suramathetraitor")
public class SuramaTheTraitorAI extends GeneralNpcAI {

	public SuramaTheTraitorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		moveToRaksha();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		PacketSendUtility.broadcastMessage(getOwner(), 390845, 2000);
	}

	private void moveToRaksha() {
		setStateIfNot(AIState.WALKING);
		getOwner().setState(CreatureState.ACTIVE, true);
		getMoveController().moveToPoint(651, 1319, 487);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getOwner().getObjectId()));
		ThreadPoolManager.getInstance().schedule(this::startDialog, 10000);
	}

	private void startDialog() {
		Npc raksha = getPosition().getWorldMapInstance().getNpc(219356);
		PacketSendUtility.broadcastMessage(getOwner(), 390841);
		PacketSendUtility.broadcastMessage(getOwner(), 390842, 3000);
		PacketSendUtility.broadcastMessage(raksha, 390843, 6000);
		ThreadPoolManager.getInstance().schedule(() -> {
			raksha.setTarget(getOwner());
			SkillEngine.getInstance().getSkill(raksha, 20952, 60, getOwner()).useNoAnimationSkill();
			raksha.overrideNpcType(CreatureType.ATTACKABLE);
		}, 8000);
	}
}
