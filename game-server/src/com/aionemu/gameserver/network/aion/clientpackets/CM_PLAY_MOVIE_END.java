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

	private byte type;
	private int targetObjectId;
	private int questId;
	private int movieId;
	@SuppressWarnings({ "unused", "FieldCanBeLocal" })
	private boolean canSkip;

	public CM_PLAY_MOVIE_END(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		type = readC(); // 1: CutSceneMovies, otherwise CutScenes
		targetObjectId = readD();
		questId = readD();
		movieId = readD();
		readC(); // unknown
		canSkip = readC() == 0;
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (!player.isInCustomState(CustomPlayerState.WATCHING_CUTSCENE)) {
			// the client automatically plays movies when reading certain books (3: 730079/730091, 4: 730092, 5: 730085)
			Set<Integer> bookMovieIds = type == 1 ? Set.of(3, 4, 5) : Set.of();
			if (questId != 0 || !bookMovieIds.contains(movieId))
				AuditLogger.log(player, "sent " + getPacketName() + " for cutscene " + movieId + " that wasn't sent by the server");
			return;
		}
		player.unsetCustomState(CustomPlayerState.WATCHING_CUTSCENE);
		VisibleObject target = player.isTargeting(targetObjectId) ? player.getTarget() : null;
		QuestEngine.getInstance().onMovieEnd(new QuestEnv(target, player, questId), movieId);
		player.getPosition().getWorldMapInstance().getInstanceHandler().onPlayMovieEnd(player, movieId);
	}
}
