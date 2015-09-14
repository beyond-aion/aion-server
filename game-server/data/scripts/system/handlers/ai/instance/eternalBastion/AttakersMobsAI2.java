package ai.instance.eternalBastion;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;


/**
 * @author Cheatkiller
 *
 */
@AIName("eb_attakersMobs")
public class AttakersMobsAI2 extends AggressiveNpcAI2 {
	
		
	@Override
	protected void handleMoveValidate() {
		super.handleMoveValidate();
		if (getOwner().getAi2().getState() == AIState.WALKING && getOwner().getState() != 1) {
			getOwner().setState(1);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getOwner().getObjectId()));
		}
	}
	
	@Override
	protected void handleCreatureMoved(Creature creature) {
		Npc generalE = getPosition().getWorldMapInstance().getNpc(209516);
		Npc generalA = getPosition().getWorldMapInstance().getNpc(209517);
		if (generalE != null && !getOwner().getAggroList().isHating(generalE) &&  MathUtil.isIn3dRange(getOwner(), generalE, 20) || generalA != null && !getOwner().getAggroList().isHating(generalA) && MathUtil.isIn3dRange(getOwner(), generalA, 20)) {
			getSpawnTemplate().setWalkerId(null);
			WalkManager.stopWalking(this);
			if (generalE != null) {
				getOwner().getAggroList().addDamage(generalE, 1000);
			}
			else if (generalA != null) {
				getOwner().getAggroList().addDamage(generalA, 1000);
			}
		} 	
	}
	
	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
	}
	
	@Override
	protected void handleBackHome() {
	
	}
}
