package ai.quests;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.handler.TalkEventHandler;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.QuestEngine;

import ai.ActionItemNpcAI;

/**
 * @Author Majka
 */
@AIName("quest_use_npc")
public class QuestUseNpcAI extends ActionItemNpcAI {

	public QuestUseNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		// on retail a quest object doesn't react at all as long as no quest step of the player needs it
		if (QuestEngine.getInstance().isNpcNeededByAnyQuestOf(player, getNpcId()))
			super.handleDialogStart(player);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (getObjectTemplate().isDialogNpc())
			TalkEventHandler.onTalk(this, player);
	}
}
