package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.state.FlyState;
import com.aionemu.gameserver.model.templates.windstreams.WindstreamPath;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TRANSFORM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_WINDSTREAM;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.utils.PacketSendUtility;
public class CM_WINDSTREAM extends AionClientPacket {

	int teleportId;
	int distance;
	int state;

	public CM_WINDSTREAM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		teleportId = readD();
		distance = readD();
		state = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		switch (state) {
			case 0: // ?
				player.unsetPlayerMode(PlayerMode.RIDE);
				break;
			case 1: // entering windstream
				if (player.isInPlayerMode(PlayerMode.WINDSTREAM) || !player.isFlying())
					return;
				player.setPlayerMode(PlayerMode.WINDSTREAM, new WindstreamPath(teleportId, distance));
				player.unsetState(CreatureState.ACTIVE);
				player.unsetState(CreatureState.GLIDING);
				player.setState(CreatureState.FLYING);
				player.unsetFlyState(FlyState.GLIDING);
				player.setFlyState(FlyState.FLYING);
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.WINDSTREAM, teleportId, distance), true);
				player.getLifeStats().triggerFpRestore();
				QuestEngine.getInstance().onEnterWindStream(new QuestEnv(null, player, 0), teleportId);
				return; // don't send SM_WINDSTREAM
			case 2: // leaving windstream (gliding)
			case 3: // leaving windstream
				if (!player.isInPlayerMode(PlayerMode.WINDSTREAM))
					return;
				player.unsetState(CreatureState.FLYING);
				player.setState(CreatureState.ACTIVE);
				player.unsetFlyState(FlyState.FLYING);
				player.unsetFlyState(FlyState.GLIDING);
				if (state == 2)
					player.getFlyController().switchToGliding();
				else
					player.getGameStats().updateStatsAndSpeedVisually();
				player.unsetPlayerMode(PlayerMode.WINDSTREAM);
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, state == 2 ? EmotionType.WINDSTREAM_END : EmotionType.WINDSTREAM_EXIT),
					true);
				if (player.isTransformed()) // send sm_transform if player is transformed
					PacketSendUtility.broadcastPacketAndReceive(player, new SM_TRANSFORM(player));
				break;
			case 4: // ?
				break;
			case 7: // start boost
			case 8: // end boost
				PacketSendUtility.broadcastPacket(player,
					new SM_EMOTION(player, state == 7 ? EmotionType.WINDSTREAM_START_BOOST : EmotionType.WINDSTREAM_END_BOOST), true);
				break;
			default:
				LoggerFactory.getLogger(CM_WINDSTREAM.class).warn("Unknown Windstream state #" + state + " was sent from " + player.getPosition());
				return;
		}
		PacketSendUtility.sendPacket(player, new SM_WINDSTREAM(state, 1));
	}

}
