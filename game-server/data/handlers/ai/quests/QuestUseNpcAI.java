package ai.quests;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.handler.TalkEventHandler;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;

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
	protected void handleUseItemFinish(Player player) {
		if (getObjectTemplate().isDialogNpc())
			TalkEventHandler.onTalk(this, player);
	}
}
