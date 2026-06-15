package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class CM_SUMMON_EMOTION extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_SUMMON_EMOTION.class);

	@SuppressWarnings("unused")
	private int objId;
	private int emotionTypeId;

	public CM_SUMMON_EMOTION(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		objId = readD();
		emotionTypeId = readUC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Creature summonOrMercenary = player.getSummonOrMercenary(objId);
		if (summonOrMercenary == null) // commonly due to lags when the pet dies
			return;

		EmotionType emotionType = EmotionType.getEmotionTypeById(emotionTypeId);
		switch (emotionType) {
			case FLY:
			case LAND:
				PacketSendUtility.broadcastPacket(summonOrMercenary, new SM_EMOTION(summonOrMercenary, EmotionType.CHANGE_SPEED));
				PacketSendUtility.broadcastPacket(summonOrMercenary, new SM_EMOTION(summonOrMercenary, emotionType));
				break;
			case JUMP:
			case SUMMON_STOP_JUMP:
				PacketSendUtility.broadcastPacket(summonOrMercenary, new SM_EMOTION(summonOrMercenary, emotionType));
				break;
			case ATTACKMODE_IN_MOVE: // start attacking
				summonOrMercenary.setState(CreatureState.WEAPON_EQUIPPED);
				PacketSendUtility.broadcastPacket(summonOrMercenary, new SM_EMOTION(summonOrMercenary, emotionType));
				break;
			case NEUTRALMODE_IN_MOVE: // stop attacking
				summonOrMercenary.unsetState(CreatureState.WEAPON_EQUIPPED);
				PacketSendUtility.broadcastPacket(summonOrMercenary, new SM_EMOTION(summonOrMercenary, emotionType));
				break;
			case NONE:
				if (emotionTypeId != EmotionType.NONE.getTypeId())
					log.warn("Unknown emotion type " + emotionTypeId + " from " + player);
		}
	}
}
