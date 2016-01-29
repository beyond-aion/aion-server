package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author SoulKeeper
 * @author_fix nerolory
 */
public class CM_EMOTION extends AionClientPacket {

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(CM_EMOTION.class);
	/**
	 * Emotion number
	 */
	EmotionType emotionType;
	/**
	 * Emotion number
	 */
	int emotion;
	/**
	 * Coordinates of player
	 */
	float x;
	float y;
	float z;
	byte heading;

	int targetObjectId;

	/**
	 * Constructs new client packet instance.
	 * 
	 * @param opcode
	 */
	public CM_EMOTION(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * Read data
	 */
	@Override
	protected void readImpl() {
		int et;
		et = readC();
		emotionType = EmotionType.getEmotionTypeById(et);

		switch (emotionType) {
			case SELECT_TARGET:// select target
			case JUMP: // jump
			case SIT: // resting
			case STAND: // end resting
			case LAND_FLYTELEPORT: // fly teleport land
			case FLY: // fly up
			case LAND: // land
			case DIE: // die
			case END_DUEL: // duel end
			case WALK: // walk on
			case RUN: // walk off
			case OPEN_DOOR: // open static doors
			case CLOSE_DOOR: // close static doors
			case POWERSHARD_ON: // powershard on
			case POWERSHARD_OFF: // powershard off
			case ATTACKMODE_IN_MOVE: // get equip weapon
			case ATTACKMODE_IN_STANDING: // get equip weapon
			case NEUTRALMODE_IN_MOVE: // remove equip weapon
			case NEUTRALMODE_IN_STANDING: // remove equip weapon
			case START_SPRINT:
			case END_SPRINT:
			case WINDSTREAM_STRAFE:
				break;
			case EMOTE:
				emotion = readH();
				targetObjectId = readD();
				break;
			case CHAIR_SIT: // sit on chair
			case CHAIR_UP: // stand on chair
				x = readF();
				y = readF();
				z = readF();
				heading = (byte) readC();
				break;
			default:
				log.error("Unknown emotion type? 0x" + Integer.toHexString(et/* !!!!! */).toUpperCase());
				break;
		}
	}

	/**
	 * Send emotion packet
	 */
	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.getLifeStats().isAlreadyDead()) {
			return;
		}

		if (emotionType != EmotionType.SELECT_TARGET && emotionType != EmotionType.ATTACKMODE_IN_MOVE
			&& emotionType != EmotionType.ATTACKMODE_IN_STANDING && emotionType != EmotionType.NEUTRALMODE_IN_MOVE
			&& emotionType != EmotionType.NEUTRALMODE_IN_STANDING) {
			if (player.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_MOVE_STATE) || player.getEffectController().isUnderFear()) {
				return;
			}
		}

		if (player.getState() == CreatureState.PRIVATE_SHOP.getId() || player.isAttackMode()
			&& (emotionType == EmotionType.CHAIR_SIT || emotionType == EmotionType.JUMP))
			return;

		player.getController().cancelUseItem();
		if (emotionType != EmotionType.SELECT_TARGET)
			player.getController().cancelCurrentSkill();

		// check for stance
		if (player.getController().isUnderStance()) {
			switch (emotionType) {
				case SELECT_TARGET:
					break;
				case FLY:
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_TAKE_OFF__WHILE_IN_CURRENT_STANCE);
					return;
				default:
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_CHANGE_MODE__WHILE_IN_CURRENT_STANCE);
					return;
			}
		}

		if (emotionType == EmotionType.JUMP) {
			player.getMoveController().updateLastJump();
		}

		switch (emotionType) {
			case SELECT_TARGET:
				return;
			case SIT:
				if (player.isInState(CreatureState.PRIVATE_SHOP)) {
					return;
				}
				player.getObserveController().notifySitObservers();
				if (player.isInPlayerMode(PlayerMode.RIDE)) {
					player.unsetPlayerMode(PlayerMode.RIDE);
				}
				player.setState(CreatureState.RESTING);
				break;
			case STAND:
				player.unsetState(CreatureState.RESTING);
				break;
			case CHAIR_SIT:
				if (!player.isInState(CreatureState.WEAPON_EQUIPPED))
					player.setState(CreatureState.CHAIR);
				break;
			case CHAIR_UP:
				player.unsetState(CreatureState.CHAIR);
				break;
			case LAND_FLYTELEPORT:
				player.getController().onFlyTeleportEnd();
				break;
			case FLY:
				if (!player.getFlyController().startFly())
					return;
				break;
			case LAND:
				player.getFlyController().endFly(false);
				break;
			case ATTACKMODE_IN_MOVE:
			case ATTACKMODE_IN_STANDING:
				player.setAttackMode(true);
				player.setState(CreatureState.WEAPON_EQUIPPED);
				break;
			case NEUTRALMODE_IN_MOVE:
			case NEUTRALMODE_IN_STANDING:
				player.setAttackMode(false);
				player.unsetState(CreatureState.WEAPON_EQUIPPED);
				break;
			case WALK:
				// cannot toggle walk when you flying or gliding
				if (player.isFlying())
					return;
				player.setState(CreatureState.WALKING);
				break;
			case RUN:
				player.unsetState(CreatureState.WALKING);
				break;
			case OPEN_DOOR:
			case CLOSE_DOOR:
				break;
			case POWERSHARD_ON:
				if (!player.getEquipment().isPowerShardEquipped()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_WEAPON_BOOST_NO_BOOSTER_EQUIPED);
					return;
				}
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_WEAPON_BOOST_BOOST_MODE_STARTED);
				player.setState(CreatureState.POWERSHARD);
				break;
			case POWERSHARD_OFF:
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_WEAPON_BOOST_BOOST_MODE_ENDED);
				player.unsetState(CreatureState.POWERSHARD);
				break;
			case START_SPRINT:
				if (!player.isInPlayerMode(PlayerMode.RIDE) || player.getLifeStats().getCurrentFp() < player.ride.getStartFp() || player.isFlying()
					|| !player.ride.canSprint()) {
					return;
				}
				player.setSprintMode(true);
				player.getLifeStats().triggerFpReduceByCost(player.ride.getCostFp());
				break;
			case END_SPRINT:
				if (!player.isInPlayerMode(PlayerMode.RIDE) || !player.ride.canSprint() || !player.isInSprintMode()) {
					return;
				}
				player.setSprintMode(false);
				player.getLifeStats().triggerFpRestore();
				break;
		}

		if (player.getEmotions().canUse(emotion)) {
			PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, emotionType, emotion, x, y, z, heading, getTargetObjectId(player)), true);
		}

		if (player.isProtectionActive() && emotionType != EmotionType.SELECT_TARGET) {
			player.getController().stopProtectionActiveTask();
		}
	}

	/**
	 * @param player
	 * @return
	 */
	private final int getTargetObjectId(Player player) {
		int target = player.getTarget() == null ? 0 : player.getTarget().getObjectId();
		return target != 0 ? target : this.targetObjectId;
	}
}
