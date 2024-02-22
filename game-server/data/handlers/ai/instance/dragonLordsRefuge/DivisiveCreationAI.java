package ai.instance.dragonLordsRefuge;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * March 25th, 2018: Reduced the damage output of 'Summon Rock' by 50% since on retail templars only receive
 * 2k to 3k damage, which is about 50% of the current. The base damage of this skill is 4500 so it is reduced
 * by something on retail. Maybe remove this hard-coded adjustment if something changes in damage calculations.
 * 
 * @author Cheatkiller, Estrayl
 */
@AIName("divisive_creation")
public class DivisiveCreationAI extends AggressiveNpcAI {

	public DivisiveCreationAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		final WorldMapInstance instance = getPosition().getWorldMapInstance();
		ThreadPoolManager.getInstance().schedule(() -> {
			AIActions.targetCreature(DivisiveCreationAI.this, Rnd.get(instance.getPlayersInside()));
			setStateIfNot(AIState.WALKING);
			getOwner().setState(CreatureState.ACTIVE, true);
			getMoveController().moveToTargetObject();
			PacketSendUtility.broadcastToMap(getOwner(), new SM_EMOTION(getOwner(), EmotionType.WALK));
		}, 5000);
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		if (effect != null) {
			switch (effect.getSkillId()) {
				case 20986:
					damage *= 0.6f;
					break;
				case 21897:
				case 21898:
					damage *= 0.5f;
					break;
			}
		}
		return damage;
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return ItemAttackType.MAGICAL_EARTH;
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
