package com.aionemu.gameserver.model;

import com.aionemu.gameserver.model.templates.L10n;

/**
 * States the client puts into messages which name one, like STR_SKILL_CANT_CAST ("You cannot do that while you are %0.") or
 * STR_MSG_CANNOT_USE_ITEM_DURING_PATH_FLYING ("You cannot use an item while %0."). The comments show what each one reads as.
 */
public enum ActionState implements L10n {

	STANDING(1400053), // standing
	PATH_FLYING(1400054), // flying
	FREE_FLYING(1400055), // flying
	RIDING(1400056), // riding
	RESTING(1400057), // resting
	SITTING(1400058), // sitting
	DEAD(1400059), // dead
	FLY_DEAD(1400060), // dead
	PERSONAL_SHOP(1400061), // running a Private Store
	LOOTING(1400062), // looting
	FLY_LOOTING(1400063), // looting
	CURRENT_STATUS(1400064), // in your current status
	COMBAT(1400079), // in combat
	MOVING(1400080), // moving
	USING_SKILL(1400081), // using a skill
	GLIDING(1400082), // gliding
	POLYMORPH(1401212); // Transformation Mode

	private final int l10nId;

	private ActionState(int l10nId) {
		this.l10nId = l10nId;
	}

	@Override
	public int getL10nId() {
		return l10nId;
	}
}
