package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.challenge.ChallengeQuest;
import com.aionemu.gameserver.model.challenge.ChallengeTask;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.challenge.ChallengeType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ViAl
 */
public class SM_CHALLENGE_LIST extends AionServerPacket {

	int action;
	int ownerId;
	ChallengeType ownerType;
	List<ChallengeTask> tasks;
	ChallengeTask task;

	public SM_CHALLENGE_LIST(int action, int ownerId, ChallengeType ownerType, List<ChallengeTask> tasks) {
		this.action = action;
		this.ownerId = ownerId;
		this.ownerType = ownerType;
		this.tasks = tasks;
	}

	public SM_CHALLENGE_LIST(int action, int ownerId, ChallengeType ownerType, ChallengeTask task) {
		this.action = action;
		this.ownerId = ownerId;
		this.ownerType = ownerType;
		this.task = task;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player player = con.getActivePlayer();
		writeC(action);
		writeD(ownerId); // legionId or townId
		writeC(ownerType.getId()); // 1 for legion, 2 for town
		writeD(player.getObjectId());
		switch (action) {
			case 2: // send challenge tasks list
				writeD((int) (System.currentTimeMillis() / 1000));
				writeH(tasks.size());
				for (ChallengeTask task : tasks) {
					writeD(32); // unk
					writeD(task.getTaskId());
					writeC(1); // unk
					writeC(21); // unk
					writeC(0); // unk
					writeD(task.getCompleteTimeEpochSeconds());
				}
				break;
			case 7: // send individual challenge task info
				writeD(32); // unk
				writeD(task.getTaskId());
				writeH(task.getQuestsCount());
				for (ChallengeQuest quest : task.getQuests().values()) {
					writeD(quest.getQuestId());
					writeH(quest.getMaxRepeats());
					writeD(quest.getScorePerQuest());
					writeH(quest.getCompleteCount()); // unk
				}
				break;
		}
	}

}
