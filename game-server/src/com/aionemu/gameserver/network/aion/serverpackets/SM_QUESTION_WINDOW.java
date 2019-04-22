package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Opens a yes/no question window on the client. Question based on the code given, defined in client_strings.xml
 * 
 * @author Ben, avol, Lyahim, Neon
 */
public class SM_QUESTION_WINDOW extends AionServerPacket {

	/**
	 * %0 has challenged you to a duel. Do you accept?
	 */
	public static final int STR_DUEL_DO_YOU_ACCEPT_REQUEST = 50028;

	/**
	 * Do you want to withdraw your challenge to %0?
	 */
	public static final int STR_DUEL_DO_YOU_WITHDRAW_REQUEST = 50030;

	/**
	 * %0 has invited you to join a group. Accept the invitation?
	 */
	public static final int STR_PARTY_DO_YOU_ACCEPT_INVITATION = 60000;

	/**
	 * %0 has invited you to join an Alliance. Accept the invitation?
	 */
	public static final int STR_PARTY_ALLIANCE_DO_YOU_ACCEPT_HIS_INVITATION = 70000;

	/**
	 * %0 has asked to change the Alliance's loot distribution to 'free for all' mode. Accept?
	 */
	public static final int STR_PARTY_ALLIANCE_CHANGE_LOOT_TO_FREE_HE_ASKED = 70001;

	/**
	 * %0 has asked to change the Alliance's loot distribution to 'auto loot' mode. Accept?
	 */
	public static final int STR_PARTY_ALLIANCE_CHANGE_LOOT_TO_RANDOM_HE_ASKED = 70002;

	/**
	 * %0 requested permission to pick up %1. Grant permission?
	 */
	public static final int STR_PARTY_ALLIANCE_PICKUP_ITEM_HE_ASKED = 70003;

	/**
	 * %0 has invited you to join an Alliance. Accept the invitation?
	 */
	public static final int STR_FORCE_DO_YOU_ACCEPT_INVITATION = 70004;

	/**
	 * Creating a Legion requires &lt;font font_xml="v3_msgbox_money"&gt;%qina0&lt;/font&gt;. Create a Legion?
	 */
	public static final int STR_GUILD_CREATE_DO_YOU_ACCEPT_PAY = 80000;

	/**
	 * You have been invited to the %0 Legion (Level %1) by %2. Accept the invitation?
	 */
	public static final int STR_GUILD_INVITE_DO_YOU_ACCEPT_INVITATION = 80001;

	/**
	 * Are you sure you want to transfer Legion Brigade General authority to %0?
	 */
	public static final int STR_GUILD_TRANSFER_GUILDMASTER = 80005;

	/**
	 * Are you sure you want to leave the %0 Legion?
	 */
	public static final int STR_GUILD_DO_YOU_LEAVE = 80006;

	/**
	 * Are you sure you want to kick %0 out of the Legion?
	 */
	public static final int STR_GUILD_DO_YOU_BANISH = 80007;

	/**
	 * The Legion will remain in disbanding mode for a day following your request. Are you sure you wish to &lt;font
	 * color="ff0000"&gt;disband&lt;/font&gt; the Legion?
	 */
	public static final int STR_GUILD_DISPERSE_STAYMODE = 80008;

	/**
	 * The %0 Legion is currently waiting to be disbanded. Do you want to cancel the disbanding process?
	 */
	public static final int STR_GUILD_DISPERSE_STAYMODE_CANCEL = 80009;

	/**
	 * Level upgrade requires &lt;font font_xml="v3_msgbox_money"&gt;%qina0&lt;/font&gt;. Do you want to upgrade?
	 */
	public static final int STR_GUILD_CHANGE_LEVEL_DO_YOU_ACCEPT_PAY = 80010;

	/**
	 * %0 nominated you as Legion Brigade General. Accept the position?
	 */
	public static final int STR_GUILD_CHANGE_MASTER_DO_YOU_ACCEPT_OFFER = 80011;

	/**
	 * The price of this item is set rather high. Are you sure you want to buy it?
	 */
	public static final int STR_BUY_SELL_CONFIRM_PURCHASE_EXCESSIVE_PRICE = 90000;

	/**
	 * %0 has asked you to trade items. Accept?
	 */
	public static final int STR_EXCHANGE_DO_YOU_ACCEPT_EXCHANGE = 90001;

	/**
	 * Are you sure you want to abandon this quest?
	 */
	public static final int STR_QUEST_GIVEUP = 150000;

	/**
	 * Discard %0 and abandon the %1 quest?
	 */
	public static final int STR_QUEST_GIVEUP_WHEN_DELETE_QUEST_ITEM = 150001;

	/**
	 * &lt;p&gt;You can restore XP and remove the resurrection aftereffect if your soul is healed. Soul healing requires &lt;font
	 * font_xml="v3_msgbox_money"&gt;%qina0&lt;/font&gt;.&lt;/p&gt;&lt;p&gt;Do you want to heal your soul?&lt;/p&gt;
	 */
	public static final int STR_ASK_RECOVER_EXPERIENCE = 160011;

	/**
	 * Binding to this location costs &lt;font font_xml="v3_msgbox_money"&gt;%qina0&lt;/font&gt;. Proceed?
	 */
	public static final int STR_ASK_REGISTER_RESURRECT_POINT = 160012;

	/**
	 * It costs &lt;font font_xml="v3_msgbox_money"&gt; %qina1 &lt;/font&gt; to travel to %0. Proceed?
	 */
	public static final int STR_TELEPORT_NEED_CONFIRM = 160013;

	/**
	 * Do you want to travel using the magic passage?
	 */
	public static final int STR_ASK_GROUP_GATE_DO_YOU_ACCEPT_MOVE = 160014;

	/**
	 * &lt;p&gt;Travel through this passage?&lt;/p&gt;&lt;p&gt;&lt;font color="ff0000"&gt;You cannot return once you go.&lt;/P&gt;
	 */
	public static final int STR_HOUSE_GATE_ACCEPT_MOVE_DONT_RETURN = 904435;

	/**
	 * Using the artifact requires &lt;font font_xml="v3_msgbox_money"&gt;%1 %0(s)&lt;/font&gt;. Proceed?
	 */
	public static final int STR_ASK_USE_ARTIFACT = 160016;

	/**
	 * Do you want to pass through the castle gate?
	 */
	public static final int STR_ASK_PASS_BY_GATE = 160017;

	/**
	 * Do you want to bind yourself to this Obelisk?
	 */
	public static final int STR_ASK_REGISTER_BINDSTONE = 160018;

	/**
	 * Do you want to teleport through the Rift?
	 */
	public static final int STR_ASK_PASS_BY_DIRECT_PORTAL = 160019;

	/**
	 * The repair costs &lt;font font_xml="v3_msgbox_money"&gt;%0(%1 pieces)&lt;/font&gt;. Repair?
	 */
	public static final int STR_ASK_DOOR_REPAIR_DO_YOU_ACCEPT_REPAIR = 160021;

	/**
	 * @
	 */
	public static final int STR_ASK_DOOR_REPAIR_POPUPDIALOG = 160027;

	/**
	 * @
	 */
	public static final int STR_ASK_ARTIFACT_POPUPDIALOG = 160028;

	/**
	 * You are currently a member of %0. Do you want to leave it to join %1?
	 */
	public static final int STR_ASK_JOIN_NEW_FACTION = 160033;

	/**
	 * %0 is an untradable item. Are you sure you want to acquire it?
	 */
	public static final int STR_CONFIRM_LOOT = 900495;

	/**
	 * &lt;p&gt;To expand the cube you need &lt;font font_xml="v3_msgbox_money"&gt;%qina0&lt;/font&gt; Kinah.&lt;/p&gt; &lt;p&gt;Do you want to expand
	 * it?&lt;/p&gt;
	 */
	public static final int STR_WAREHOUSE_EXPAND_WARNING = 900686;

	/**
	 * To upgrade the %0 skill, you need &lt;font font_xml="v3_msgbox_money"&gt;%qina1&lt;/font&gt;. Are you sure you want to upgrade the skill?
	 */
	public static final int STR_CRAFT_ADDSKILL_CONFIRM = 900852;

	/**
	 * You need %num0 %1 to buy the item. Proceed with the purchase?
	 */
	public static final int STR_AIONJEWEL_SHOP_BUY_CONFIRM = 901972;

	/**
	 * &lt;p align="left"&gt;%0 is about to summon you using %1. Will you accept?&lt;/p&gt; &lt;p&gt;&lt;/p&gt;&lt;p align="left"&gt;You must decide in
	 * %2 seconds.&lt;/p&gt;
	 */
	public static final int STR_SUMMON_PARTY_DO_YOU_ACCEPT_REQUEST = 901721;

	/**
	 * Enter %WORLDNAME0 (Difficulty: %1)?
	 */
	public static final int STR_INSTANCE_DUNGEON_WITH_DIFFICULTY_ENTER_CONFIRM = 902050;

	/**
	 * &lt;p&gt;%0 legion has invited your force to join their League.&lt;/p&gt;&lt;p&gt;Will you accept the invitation?&lt;/p&gt;
	 */
	public static final int STR_MSGBOX_UNION_INVITE_ME = 902249;

	/**
	 * &lt;p&gt;The %0 must be soulbound to equip it.&lt;/p&gt; &lt;p&gt;You cannot trade the item that has been soulbound.&lt;/p&gt;
	 * &lt;p&gt;Equip?&lt;/p&gt;
	 */
	public static final int STR_SOUL_BOUND_ITEM_DO_YOU_WANT_SOUL_BOUND = 95006;

	/**
	 * &lt;p&gt;All the equipped items will be conditioned to the maximum level. &lt;/p&gt;&lt;p&gt;You need &lt;font
	 * font_xml="v3_msgbox_money"&gt;%qina0&lt;/font&gt;to do this. Are you sure you want to condition them?&lt;/p&gt;
	 */
	public static final int STR_ITEM_CHARGE_ALL_CONFIRM = 903026;

	public static final int STR_ITEM_CHARGE2_ALL_CONFIRM = 904039;
	/**
	 * &lt;p&gt;You can condition only %0 of the registered items up to level %1. &lt;/p&gt;&lt;p&gt;You need &lt;font
	 * font_xml="v3_msgbox_money"&gt;%qina2&lt;/font&gt;to do this. &lt;/p&gt;&lt;p&gt;Are you sure you want to condition them?&lt;/p&gt;
	 */
	public static final int STR_ITEM_CHARGE_CONFIRM_SOME_ALREADY_CHARGED = 903028;

	/**
	 * You can make %1 by assembling %0. Do you want to assemble it
	 */
	public static final int STR_ASSEMBLY_ITEM_POPUP_CONFIRM = 903441;

	/**
	 * You are about to teleport to your own house. Continue?
	 */
	public static final int STR_HOUSING_TELEPORT_HOME_CONFIRM = 903533;

	/**
	 * You are about to teleport to %0's house. Continue?
	 */
	public static final int STR_HOUSING_TELEPORT_BUDDY_CONFIRM = 903534;

	/**
	 * You are about to teleport to the house of a friend of the current house owner. Continue?
	 */
	public static final int STR_HOUSING_TELEPORT_RANDOM_CONFIRM = 903535;

	/**
	 * You are about to teleport to your Legion's house. Continue?
	 */
	public static final int STR_HOUSING_TELEPORT_GUILD_CONFIRM = 903536;

	/**
	 * Do you want to enter the Advance Corridor?
	 */
	public static final int STR_ASK_PASS_BY_SVS_DIRECT_PORTAL = 905067;

	/**
	 * %0 wants to list you as a friend. Do you want to accept it?
	 */
	public static final int STR_BUDDYLIST_ADD_BUDDY_REQUEST = 1401498;

	private static final int MAX_PARAM_COUNT = 3;

	private final int code;
	private final int senderId;
	private final int rangeOrCooldownSeconds;
	private final Object[] params;

	/**
	 * Creates a new <tt>SM_QUESTION_WINDOW</tt> packet
	 * 
	 * @param code
	 *          - code The string code to display, found in client_strings.xml
	 * @param senderId
	 *          - sender Object id
	 * @param rangeOrCooldownSeconds
	 *          - the valid range for this dialog (relative to the senderId) or the stone cooldown time for artifact or door repair windows
	 * @param params
	 *          - params The parameters for the string, if any
	 */
	public SM_QUESTION_WINDOW(int code, int senderId, int rangeOrCooldownSeconds, Object... params) {
		this.code = code;
		this.senderId = senderId;
		this.rangeOrCooldownSeconds = rangeOrCooldownSeconds;
		if (params.length > MAX_PARAM_COUNT)
			throw new IllegalArgumentException("More than " + MAX_PARAM_COUNT + " message parameters are not supported");
		this.params = params;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(code);
		for (int i = 0; i < MAX_PARAM_COUNT; i++) // client always wants content here, even if there is none
			writeS(i < params.length ? String.valueOf(params[i]) : null);
		writeD(0x00);// unk
		writeC(rangeOrCooldownSeconds > 0 ? 1 : 0); // 1 = check for range (client will auto decline) / display cooldown time
		writeD(senderId);
		writeD(rangeOrCooldownSeconds); // range within the question is valid or artifact/repair stone cooldown to display
	}
}
