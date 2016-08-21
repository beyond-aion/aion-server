package ai.quests;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.handler.TalkEventHandler;
import com.aionemu.gameserver.model.gameobjects.player.Player;

import ai.ActionItemNpcAI2;

/**
 * @Author Majka
 */
@AIName("quest_use_npc")
public class QuestUseNpcAI2 extends ActionItemNpcAI2 {

	@Override
	protected void handleUseItemFinish(Player player) {
		if (getObjectTemplate().isDialogNpc())
			TalkEventHandler.onTalk(this, player);
	}
}
