package com.aionemu.gameserver.ai2.manager;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class EmoteManager {

	/**
	 * Npc starts attacking from idle state
	 * 
	 * @param owner
	 */
	public static final void emoteStartAttacking(Npc owner) {
		Creature target = (Creature) owner.getTarget();
		owner.unsetState(CreatureState.WALKING);
		if (!owner.isInState(CreatureState.WEAPON_EQUIPPED)) {
			owner.setState(CreatureState.WEAPON_EQUIPPED);
			PacketSendUtility
				.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, target.getObjectId()));
			PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.ATTACKMODE_IN_MOVE, 0, target.getObjectId()));
		}
	}

	/**
	 * Npc stops attacking
	 * 
	 * @param owner
	 */
	public static final void emoteStopAttacking(Npc owner) {
		owner.unsetState(CreatureState.WEAPON_EQUIPPED);
		if (owner.getTarget() != null && owner.getTarget() instanceof Player) {
			PacketSendUtility.sendPacket((Player) owner.getTarget(),
				SM_SYSTEM_MESSAGE.STR_UI_COMBAT_NPC_RETURN(owner.getObjectTemplate().getNameId()));
		}
	}

	/**
	 * Npc starts following other creature
	 * 
	 * @param owner
	 */
	public static final void emoteStartFollowing(Npc owner) {
		owner.unsetState(CreatureState.WALKING);
		PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, 0));
		PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.NEUTRALMODE_IN_MOVE, 0, 0));
	}

	/**
	 * Npc starts walking (either random or path)
	 * 
	 * @param owner
	 */
	public static final void emoteStartWalking(Npc owner) {
		owner.setState(CreatureState.WALKING);
		PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.WALK));
	}

	/**
	 * Npc stops walking
	 * 
	 * @param owner
	 */
	public static final void emoteStopWalking(Npc owner) {
		owner.unsetState(CreatureState.WALKING);
	}

	/**
	 * Npc starts returning to spawn location
	 * 
	 * @param owner
	 */
	public static final void emoteStartReturning(Npc owner) {
		PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, 0));
		PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.NEUTRALMODE_IN_MOVE, 0, 0));
	}

	/**
	 * Npc starts idling
	 * 
	 * @param owner
	 */
	public static final void emoteStartIdling(Npc owner) {
		owner.setState(CreatureState.WALKING);
		PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, 0));
		PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.NEUTRALMODE_IN_MOVE, 0, 0));
	}
}
