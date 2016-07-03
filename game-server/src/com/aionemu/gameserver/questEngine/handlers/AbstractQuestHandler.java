package com.aionemu.gameserver.questEngine.handlers;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.model.templates.rewards.BonusType;
import com.aionemu.gameserver.questEngine.model.QuestActionType;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * The methods will be overridden in concrete quest handlers
 * 
 * @author vlog
 */
public abstract class AbstractQuestHandler {

	public abstract void register();

	public boolean onDialogEvent(QuestEnv questEnv) {
		return false;
	}

	/**
	 * This method is called on every handler (which registered the event), after a player entered a map.
	 */
	public boolean onEnterWorldEvent(QuestEnv env) {
		return false;
	}

	public boolean onEnterZoneEvent(QuestEnv questEnv, ZoneName zoneName) {
		return false;
	}

	public boolean onLeaveZoneEvent(QuestEnv questEnv, ZoneName zoneName) {
		return false;
	}

	public HandlerResult onItemUseEvent(QuestEnv questEnv, Item item) {
		return HandlerResult.UNKNOWN;
	}

	public boolean onHouseItemUseEvent(QuestEnv env) {
		return false;
	}

	public boolean onGetItemEvent(QuestEnv questEnv) {
		return false;
	}

	public boolean onUseSkillEvent(QuestEnv questEnv, int skillId) {
		return false;
	}

	public boolean onKillEvent(QuestEnv questEnv) {
		return false;
	}

	public boolean onAttackEvent(QuestEnv questEnv) {
		return false;
	}

	/**
	 * This method is called on every handler (which registered the event), after a player leveled up or down.
	 * 
	 * @param player
	 *          - The player whose level changed
	 */
	public void onLevelChangedEvent(Player player) {
	}

	/**
	 * This method is called on every handler (which registered the event), after a quest completed.
	 * 
	 * @param env
	 *          - QuestEnv containing the player and the quest ID he completed
	 */
	public void onQuestCompletedEvent(QuestEnv env) {
	}

	public boolean onDieEvent(QuestEnv questEnv) {
		return false;
	}

	public boolean onLogOutEvent(QuestEnv env) {
		return false;
	}

	public boolean onNpcReachTargetEvent(QuestEnv env) {
		return false;
	}

	public boolean onNpcLostTargetEvent(QuestEnv env) {
		return false;
	}

	public boolean onMovieEndEvent(QuestEnv questEnv, int movieId) {
		return false;
	}

	public boolean onQuestTimerEndEvent(QuestEnv questEnv) {
		return false;
	}

	public boolean onInvisibleTimerEndEvent(QuestEnv questEnv) {
		return false;
	}

	public boolean onPassFlyingRingEvent(QuestEnv questEnv, String flyingRing) {
		return false;
	}

	public boolean onKillRankedEvent(QuestEnv env) {
		return false;
	}

	public boolean onKillInWorldEvent(QuestEnv env) {
		return false;
	}

	public boolean onKillInZoneEvent(QuestEnv env) {
		return false;
	}

	public boolean onFailCraftEvent(QuestEnv env, int itemId) {
		return false;
	}

	public boolean onEquipItemEvent(QuestEnv env, int itemId) {
		return false;
	}

	public boolean onCanAct(QuestEnv env, QuestActionType questEventType, Object... objects) {
		return false;
	}

	public boolean onAddAggroListEvent(QuestEnv questEnv) {
		return false;
	}

	public boolean onAtDistanceEvent(QuestEnv questEnv) {
		return false;
	}

	public boolean onEnterWindStreamEvent(QuestEnv questEnv, int worldId) {
		return false;
	}

	public boolean rideAction(QuestEnv questEnv, int rideItemId) {
		return false;
	}

	public boolean onDredgionRewardEvent(QuestEnv env) {
		return false;
	}

	public HandlerResult onBonusApplyEvent(QuestEnv env, BonusType bonusType, List<QuestItems> rewardItems) {
		return HandlerResult.UNKNOWN;
	}

	public boolean onProtectEndEvent(QuestEnv env) {
		return false;
	}

	public boolean onProtectFailEvent(QuestEnv env) {
		return false;
	}
}
