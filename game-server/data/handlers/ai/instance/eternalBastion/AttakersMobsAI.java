package ai.instance.eternalBastion;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("eb_attakersMobs")
public class AttakersMobsAI extends AggressiveNpcAI {

	public AttakersMobsAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleMoveValidate() {
		super.handleMoveValidate();
		if (getOwner().getAi().getState() == AIState.WALKING && getOwner().getState() != CreatureState.ACTIVE.getId()) {
			getOwner().setState(CreatureState.ACTIVE, true);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getOwner().getObjectId()));
		}
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		Npc generalE = getPosition().getWorldMapInstance().getNpc(209516);
		Npc generalA = getPosition().getWorldMapInstance().getNpc(209517);
		if (generalE != null && !getOwner().getAggroList().isHating(generalE) && PositionUtil.isInRange(getOwner(), generalE, 20)
			|| generalA != null && !getOwner().getAggroList().isHating(generalA) && PositionUtil.isInRange(getOwner(), generalA, 20)) {
			getSpawnTemplate().setWalkerId(null);
			WalkManager.stopWalking(this);
			if (generalE != null) {
				getOwner().getAggroList().addHate(generalE, 1000);
			} else if (generalA != null) {
				getOwner().getAggroList().addHate(generalA, 1000);
			}
		}
	}

	@Override
	protected void handleBackHome() {

	}
}
