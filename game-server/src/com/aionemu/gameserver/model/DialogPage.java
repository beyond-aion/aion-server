package com.aionemu.gameserver.model;

/**
 * @author Rolandas, Neon
 */
public enum DialogPage {

	NULL(DialogAction.NULL, 0),
	STIGMA(DialogAction.OPEN_STIGMA_WINDOW, 1),
	CREATE_LEGION(DialogAction.CREATE_LEGION, 2),
	ASK_QUEST_ACCEPT_WINDOW(4),
	SELECT_QUEST_REWARD_WINDOW1(5),
	SELECT_QUEST_REWARD_WINDOW2(6),
	SELECT_QUEST_REWARD_WINDOW3(7),
	SELECT_QUEST_REWARD_WINDOW4(8),
	SELECT_QUEST_REWARD_WINDOW5(45),
	SELECT_QUEST_REWARD_WINDOW6(46),
	SELECT_QUEST_REWARD_WINDOW7(47),
	SELECT_QUEST_REWARD_WINDOW8(48),
	SELECT_QUEST_REWARD_WINDOW9(49),
	SELECT_QUEST_REWARD_WINDOW10(50),
	VENDOR(DialogAction.OPEN_VENDOR, 13),
	RETRIEVE_CHAR_WAREHOUSE(DialogAction.RETRIEVE_CHAR_WAREHOUSE, 14),
	DEPOSIT_CHAR_WAREHOUSE(DialogAction.DEPOSIT_CHAR_WAREHOUSE, 26), // open char warehouse
	RETRIEVE_ACCOUNT_WAREHOUSE(DialogAction.RETRIEVE_ACCOUNT_WAREHOUSE, 16),
	DEPOSIT_ACCOUNT_WAREHOUSE(DialogAction.DEPOSIT_ACCOUNT_WAREHOUSE, 17),
	MAIL(DialogAction.OPEN_POSTBOX, 18),
	CHANGE_ITEM_SKIN(DialogAction.CHANGE_ITEM_SKIN, 19),
	REMOVE_MANASTONE(DialogAction.REMOVE_ITEM_OPTION, 20),
	GIVE_ITEM_PROC(DialogAction.GIVE_ITEM_PROC, 21),
	GATHER_SKILL_LEVELUP(DialogAction.GATHER_SKILL_LEVELUP, 23),
	LOOT(24),
	LEGION_WAREHOUSE(DialogAction.OPEN_LEGION_WAREHOUSE, 25),
	// PERSONAL_WAREHOUSE(DialogAction.OPEN_PERSONAL_WAREHOUSE, 26),
	NO_RIGHT(27),
	COMBINETASK_WINDOW(DialogAction.COMBINE_TASK, 28),
	COMPOUND_WEAPON(DialogAction.COMPOUND_WEAPON, 29),
	DECOMPOUND_WEAPON(DialogAction.DECOMPOUND_WEAPON, 30),
	HOUSING_MARKER(DialogAction.HOUSING_BUILD, 32), // housing build
	HOUSING_LIFETIME(DialogAction.HOUSING_DESTRUCT, 33), // housing destruct
	CHARGE_ITEM(DialogAction.CHARGE_ITEM_SINGLE, 35), // Actually, two choices
	CHARGE_ITEM2(DialogAction.CHARGE_ITEM_SINGLE2, 42),
	HOUSING_FRIENDLIST(DialogAction.HOUSING_BUDDY_LIST, 36),
	HOUSING_POST(37), // Unknown
	HOUSING_AUCTION(DialogAction.HOUSING_PERSONAL_AUCTION, 38),
	HOUSING_PAY_RENT(DialogAction.HOUSING_PAY_RENT, 39),
	HOUSING_KICK(DialogAction.HOUSING_KICK, 40),
	HOUSING_CONFIG(DialogAction.HOUSING_CONFIG, 41),
	TOWN_CHALLENGE_TASK(DialogAction.TOWN_CHALLENGE, 43),
	ITEM_UPGRADE(DialogAction.ITEM_UPGRADE, 52),
	OPEN_STIGMA_ENCHANT(DialogAction.OPEN_STIGMA_ENCHANT, 53);

	private int id;
	private int dialogActionId;

	private DialogPage(int id) {
		this.id = id;
	}

	private DialogPage(int dialogActionId, int id) {
		this.id = id;
		this.dialogActionId = dialogActionId;
	}

	public int id() {
		return id;
	}

	public static DialogPage getByActionId(int dialogActionId) {
		for (DialogPage page : DialogPage.values()) {
			if (page.dialogActionId == dialogActionId)
				return page;
		}
		return DialogPage.NULL;
	}

	public static DialogPage getRewardPageByIndex(Integer rewardIndex) {
		if (rewardIndex != null) {
			switch (rewardIndex) {
				case 0:
					return DialogPage.SELECT_QUEST_REWARD_WINDOW1;
				case 1:
					return DialogPage.SELECT_QUEST_REWARD_WINDOW2;
				case 2:
					return DialogPage.SELECT_QUEST_REWARD_WINDOW3;
				case 3:
					return DialogPage.SELECT_QUEST_REWARD_WINDOW4;
				case 4:
					return DialogPage.SELECT_QUEST_REWARD_WINDOW5;
				case 5:
					return DialogPage.SELECT_QUEST_REWARD_WINDOW6;
				case 6:
					return DialogPage.SELECT_QUEST_REWARD_WINDOW7;
				case 7:
					return DialogPage.SELECT_QUEST_REWARD_WINDOW8;
				case 8:
					return DialogPage.SELECT_QUEST_REWARD_WINDOW9;
				case 9:
					return DialogPage.SELECT_QUEST_REWARD_WINDOW10;
			}
		}
		return DialogPage.NULL;
	}
}
