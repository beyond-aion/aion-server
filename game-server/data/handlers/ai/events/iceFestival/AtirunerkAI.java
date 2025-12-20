package ai.events.iceFestival;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;

import ai.GeneralNpcAI;

@AIName("atirunerk")
public class AtirunerkAI extends GeneralNpcAI {

	public AtirunerkAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		super.handleDialogStart(player);
		QuestEngine.getInstance().onDialog(new QuestEnv(getOwner(), player, 80719, DialogAction.QUEST_SELECT));
	}
}
