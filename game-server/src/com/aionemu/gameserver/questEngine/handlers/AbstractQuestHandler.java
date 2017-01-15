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

	public abstract int getQuestId();

	public abstract void register();

	public abstract boolean onDialogEvent(QuestEnv questEnv);

	/**
	 * This method is called on every handler (which registered the event), after a player entered a map.
	 */
	public abstract boolean onEnterWorldEvent(QuestEnv env);

	public abstract boolean onEnterZoneEvent(QuestEnv questEnv, ZoneName zoneName);

	public abstract boolean onLeaveZoneEvent(QuestEnv questEnv, ZoneName zoneName);

	public abstract HandlerResult onItemUseEvent(QuestEnv questEnv, Item item);

	public abstract boolean onHouseItemUseEvent(QuestEnv env);

	public abstract boolean onGetItemEvent(QuestEnv questEnv);

	public abstract boolean onUseSkillEvent(QuestEnv questEnv, int skillId);

	public abstract boolean onKillEvent(QuestEnv questEnv);

	public abstract boolean onAttackEvent(QuestEnv questEnv);

	/**
	 * This method is called on every handler (which registered the event), after a player leveled up or down.
	 * 
	 * @param player
	 *          - The player whose level changed
	 */
	public abstract void onLevelChangedEvent(Player player);

	/**
	 * This method is called on every handler (which registered the event), after a quest completed.
	 * 
	 * @param env
	 *          - QuestEnv containing the player and the quest ID he completed
	 */
	public abstract void onQuestCompletedEvent(QuestEnv env);

	public abstract boolean onDieEvent(QuestEnv questEnv);

	public abstract boolean onLogOutEvent(QuestEnv env);

	public abstract boolean onNpcReachTargetEvent(QuestEnv env);

	public abstract boolean onNpcLostTargetEvent(QuestEnv env);

	public abstract boolean onMovieEndEvent(QuestEnv questEnv, int movieId);

	public abstract boolean onQuestTimerEndEvent(QuestEnv questEnv);

	public abstract boolean onInvisibleTimerEndEvent(QuestEnv questEnv);

	public abstract boolean onPassFlyingRingEvent(QuestEnv questEnv, String flyingRing);

	public abstract boolean onKillRankedEvent(QuestEnv env);

	public abstract boolean onKillInWorldEvent(QuestEnv env);

	public abstract boolean onKillInZoneEvent(QuestEnv env);

	public abstract boolean onFailCraftEvent(QuestEnv env, int itemId);

	public abstract boolean onEquipItemEvent(QuestEnv env, int itemId);

	public abstract boolean onCanAct(QuestEnv env, QuestActionType questEventType, Object... objects);

	public abstract boolean onAddAggroListEvent(QuestEnv questEnv);

	public abstract boolean onAtDistanceEvent(QuestEnv questEnv);

	public abstract boolean onEnterWindStreamEvent(QuestEnv questEnv, int worldId);

	public abstract boolean rideAction(QuestEnv questEnv, int rideItemId);

	public abstract boolean onDredgionRewardEvent(QuestEnv env);

	public abstract HandlerResult onBonusApplyEvent(QuestEnv env, BonusType bonusType, List<QuestItems> rewardItems);

	public abstract boolean onProtectEndEvent(QuestEnv env);

	public abstract boolean onProtectFailEvent(QuestEnv env);
}
