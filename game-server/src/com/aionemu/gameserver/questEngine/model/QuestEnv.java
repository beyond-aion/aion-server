package com.aionemu.gameserver.questEngine.model;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author MrPoke
 */
public class QuestEnv {

	private VisibleObject visibleObject;
	private Player player;
	private int questId;
	private int dialogActionId;
	private boolean isDialogContinuationFromPreQuest;
	private int extendedRewardIndex;

	public QuestEnv(VisibleObject visibleObject, Player player, int questId) {
		this(visibleObject, player, questId, DialogAction.NULL);
	}

	public QuestEnv(VisibleObject visibleObject, Player player, int questId, int dialogActionId) {
		this.visibleObject = visibleObject;
		this.player = player;
		this.questId = questId;
		this.dialogActionId = dialogActionId;
	}

	/**
	 * @return the visibleObject
	 */
	public VisibleObject getVisibleObject() {
		return visibleObject;
	}

	/**
	 * @param visibleObject
	 *          the visibleObject to set
	 */
	public void setVisibleObject(VisibleObject visibleObject) {
		this.visibleObject = visibleObject;
	}

	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @param player
	 *          the player to set
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * @return the questId
	 */
	public int getQuestId() {
		return questId;
	}

	/**
	 * @param questId
	 *          the questId to set
	 */
	public void setQuestId(Integer questId) {
		this.questId = questId;
	}

	public int getDialogActionId() {
		return dialogActionId;
	}

	public void setDialogActionId(int dialogActionId) {
		this.dialogActionId = dialogActionId;
	}

	public boolean isDialogContinuationFromPreQuest() {
		return isDialogContinuationFromPreQuest;
	}

	public void setDialogContinuationFromPreQuest(boolean isDialogContinuationFromPreQuest) {
		this.isDialogContinuationFromPreQuest = isDialogContinuationFromPreQuest;
	}

	/**
	 * @return the target template id, 0 if no target ({@link #getVisibleObject()}) is set
	 */
	public int getTargetId() {
		return visibleObject == null ? 0 : visibleObject.getObjectTemplate().getTemplateId();
	}

	public void setExtendedRewardIndex(int index) {
		this.extendedRewardIndex = index;
	}

	public int getExtendedRewardIndex() {
		return this.extendedRewardIndex;
	}
}
