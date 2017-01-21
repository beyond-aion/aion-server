package ai.quests;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.handler.TalkEventHandler;
import com.aionemu.gameserver.model.gameobjects.player.Player;

import ai.ActionItemNpcAI;

/**
 * @Author Majka
 */
@AIName("quest_use_npc")
public class QuestUseNpcAI extends ActionItemNpcAI {

	@Override
	protected void handleUseItemFinish(Player player) {
		if (getObjectTemplate().isDialogNpc())
			TalkEventHandler.onTalk(this, player);
	}
}
