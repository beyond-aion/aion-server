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
	private DialogAction dialogAction;
	private int extendedRewardIndex;

	/**
	 * @param creature
	 * @param player
	 * @param questId
	 * @param dialogId
	 */
	public QuestEnv(VisibleObject visibleObject, Player player, int questId, int dialogId) {
		this.visibleObject = visibleObject;
		this.player = player;
		this.questId = questId;
		this.dialogAction = DialogAction.getByActionId(dialogId);
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

	/**
	 * @return the dialogId
	 */
	public int getDialogId() {
		return dialogAction.id();
	}

	public DialogAction getDialog() {
		return dialogAction;
	}

	public void setDialogAction(DialogAction dialogAction) {
		this.dialogAction = dialogAction;
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
