package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author synchro2
 */
public class WeddingsConfig {

	@Property(key = "gameserver.weddings.enable", defaultValue = "false")
	public static boolean WEDDINGS_ENABLE;

	@Property(key = "gameserver.weddings.gift.enable", defaultValue = "false")
	public static boolean WEDDINGS_GIFT_ENABLE;

	@Property(key = "gameserver.weddings.gift", defaultValue = "0")
	public static int WEDDINGS_GIFT;

	@Property(key = "gameserver.weddings.suit.enable", defaultValue = "false")
	public static boolean WEDDINGS_SUIT_ENABLE;

	@Property(key = "gameserver.weddings.suit", defaultValue = "")
	public static String WEDDINGS_SUITS;

	@Property(key = "gameserver.weddings.membership", defaultValue = "0")
	public static byte WEDDINGS_MEMBERSHIP;

	@Property(key = "gameserver.weddings.same_sex", defaultValue = "false")
	public static boolean WEDDINGS_SAME_SEX;

	@Property(key = "gameserver.weddings.races", defaultValue = "false")
	public static boolean WEDDINGS_DIFF_RACES;

	@Property(key = "gameserver.weddings.kinah", defaultValue = "0")
	public static int WEDDINGS_KINAH;

	@Property(key = "gameserver.weddings.announce", defaultValue = "true")
	public static boolean WEDDINGS_ANNOUNCE;
}