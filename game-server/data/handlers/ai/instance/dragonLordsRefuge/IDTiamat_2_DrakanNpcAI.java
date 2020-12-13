package ai.instance.dragonLordsRefuge;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.OneDmgAI;

/**
 * @author Estrayl March 10th, 2018
 */
@AIName("IDTiamat_2_Drakan_NPC")
public class IDTiamat_2_DrakanNpcAI extends OneDmgAI {

	public IDTiamat_2_DrakanNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return damage;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		AIActions.targetCreature(this, Rnd.get(getPosition().getWorldMapInstance().getPlayersInside()));
		setStateIfNot(AIState.WALKING);
		getOwner().setState(CreatureState.ACTIVE, true);
		getMoveController().moveToTargetObject();
		PacketSendUtility.broadcastToMap(getOwner(), new SM_EMOTION(getOwner(), EmotionType.WALK));
	}

}
