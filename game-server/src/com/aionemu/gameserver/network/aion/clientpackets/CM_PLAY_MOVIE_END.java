package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * Returns data sent in {@link com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE} after the cutscene has finished or is skipped.
 * 
 * @author MrPoke
 */
public class CM_PLAY_MOVIE_END extends AionClientPacket {

	@SuppressWarnings("unused")
	private byte type;
	private int targetObjectId;
	private int questId;
	private int movieId;

	public CM_PLAY_MOVIE_END(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		type = readC();
		targetObjectId = readD();
		questId = readD();
		movieId = readD();
		readC(); // unknown
		readC(); // 0: skippable, else unskippable
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (!player.isInCustomState(CustomPlayerState.WATCHING_CUTSCENE)) {
			AuditLogger.log(player, "sent " + getClass().getSimpleName() + " for cutscene " + movieId + " that wasn't sent by the server");
			return;
		}
		player.unsetCustomState(CustomPlayerState.WATCHING_CUTSCENE);
		VisibleObject target = player.isTargeting(targetObjectId) ? player.getTarget() : null;
		QuestEngine.getInstance().onMovieEnd(new QuestEnv(target, player, questId), movieId);
		player.getPosition().getWorldMapInstance().getInstanceHandler().onPlayMovieEnd(player, movieId);
	}
}
