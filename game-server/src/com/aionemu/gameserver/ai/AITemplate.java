package com.aionemu.gameserver.ai;

import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npcshout.ShoutEventType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 */
public abstract class AITemplate<T extends Creature> extends AbstractAI<T> {

	protected AITemplate(T owner) {
		super(owner);
	}

	@Override
	public void think() {
	}

	@Override
	public boolean canThink() {
		return true;
	}

	@Override
	public boolean ask(AIQuestion question) {
		return false;
	}

	@Override
	protected void handleActivate() {
	}

	@Override
	protected void handleDeactivate() {
	}

	@Override
	protected void handleMoveValidate() {
	}

	@Override
	protected void handleMoveArrived() {
	}

	@Override
	protected void handleAttack(Creature creature) {
	}

	@Override
	protected boolean handleCreatureNeedsSupport(Creature creature) {
		return false;
	}

	@Override
	protected boolean handleGuardAgainstAttacker(Creature creature) {
		return false;
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
	}

	@Override
	protected void handleCreatureNotSee(Creature creature) {
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
	}

	@Override
	protected void handleFollowMe(Creature creature) {
	}

	@Override
	protected void handleStopFollowMe(Creature creature) {
	}

	@Override
	protected void handleDialogStart(Player player) {
	}

	@Override
	protected void handleDialogFinish(Player player) {
	}

	@Override
	protected void handleCustomEvent(int eventId, Object... args) {
	}

	@Override
	protected void handleBeforeSpawned() {
	}

	@Override
	protected void handleSpawned() {
	}

	@Override
	protected void handleDespawned() {
	}

	@Override
	protected void handleDied() {
	}

	@Override
	protected void handleAttackComplete() {
	}

	@Override
	protected void handleFinishAttack() {
	}

	@Override
	protected void handleTargetTooFar() {
	}

	@Override
	protected void handleTargetGiveup() {
	}

	@Override
	protected void handleTargetChanged(Creature creature) {
	}

	@Override
	protected void handleNotAtHome() {
	}

	@Override
	protected void handleBackHome() {
	}

	@Override
	protected void handleDropRegistered() {
	}

	@Override
	public boolean onPatternShout(ShoutEventType event, String pattern, int skillNumber) {
		return false;
	}

	@Override
	protected void creatureNeedsHelp(Creature creature) {
	}

	@Override
	public AttackIntention chooseAttackIntention() {
		return AttackIntention.SIMPLE_ATTACK;
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
	}

	@Override
	public void onEffectApplied(Effect effect) {
	}

	@Override
	public void onEffectEnd(Effect effect) {
	}

	@Override
	public boolean isDestinationReached() {
		return false;
	}
}
