package com.aionemu.gameserver.questEngine.exceptions;

import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * OnDialogEvent exception
 * 
 * @author vlog
 */
public class QuestDialogException extends RuntimeException {

	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = -4323594385872762590L;

	public QuestDialogException(QuestEnv env) {
		super(new String("Info: QuestID: " + env.getQuestId() + ", DialogID: " + env.getDialogId()
			+ env.getVisibleObject().getObjectTemplate().getTemplateId() == null ? "0"
			: ", TargetID: " + env.getVisibleObject().getObjectTemplate().getTemplateId() + "."
				+ env.getPlayer().getQuestStateList().getQuestState(env.getQuestId()) == null ? " QuestState not initialized." : " QuestState: "
				+ env.getPlayer().getQuestStateList().getQuestState(env.getQuestId()).getStatus().toString()
				+ env.getPlayer().getQuestStateList().getQuestState(env.getQuestId()).getQuestVarById(0)));
	}
}
