package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author ViAl
 */
public class AntiHackConfig {

	@Property(key = "hdd.serial.lock.new.accounts", defaultValue = "false")
	public static boolean HDD_SERIAL_LOCK_NEW_ACCOUNTS;

	@Property(key = "hdd.serial.hacked.accounts.kick", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_KICK;

	@Property(key = "hdd.serial.hacked.accounts.allow.exchange", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_EXCHANGE;

	@Property(key = "hdd.serial.hacked.accounts.allow.privatestore", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_PRIVATESTORE;

	@Property(key = "hdd.serial.hacked.accounts.allow.ingameshop", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_INGAMESHOP;

	@Property(key = "hdd.serial.hacked.accounts.allow.mail", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MAIL;

	@Property(key = "hdd.serial.hacked.accounts.allow.trade", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_TRADE;

	@Property(key = "hdd.serial.hacked.accounts.allow.broker", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_BROKER;

	@Property(key = "hdd.serial.hacked.accounts.allow.chatmessages", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_CHATMESSAGES;

	@Property(key = "hdd.serial.hacked.accounts.allow.delete.characters", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_DELETE_CHARACTERS;

	@Property(key = "hdd.serial.hacked.accounts.allow.create.characters", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_CREATE_CHARACTERS;

	@Property(key = "hdd.serial.hacked.accounts.allow.edit.characters", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_EDIT_CHARACTERS;

	@Property(key = "hdd.serial.hacked.accounts.allow.manage.friends", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANAGE_FRIENDS;

	@Property(key = "hdd.serial.hacked.accounts.allow.manage.recipes", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANAGE_RECIPES;

	@Property(key = "hdd.serial.hacked.accounts.allow.manage.house", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANAGE_HOUSE;

	@Property(key = "hdd.serial.hacked.accounts.allow.manage.legion", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANAGE_LEGION;

	@Property(key = "hdd.serial.hacked.accounts.allow.manage.macrosses", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANAGE_MACROSSES;

	@Property(key = "hdd.serial.hacked.accounts.allow.break.weapons", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_BREAK_WEAPONS;

	@Property(key = "hdd.serial.hacked.accounts.allow.fusion.weapons", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_FUSION_WEAPONS;

	@Property(key = "hdd.serial.hacked.accounts.allow.composite.stones", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_COMPOSITE_STONES;

	@Property(key = "hdd.serial.hacked.accounts.allow.godstone.socketing", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_GODSTONE_SOCKETING;

	@Property(key = "hdd.serial.hacked.accounts.allow.manastone.socketing", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANASTONE_SOCKETING;

	@Property(key = "hdd.serial.hacked.accounts.allow.craft.items", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_CRAFT_ITEMS;

	@Property(key = "hdd.serial.hacked.accounts.allow.delete.items", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_DELETE_ITEMS;

	@Property(key = "hdd.serial.hacked.accounts.allow.move.items", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MOVE_ITEMS;

	@Property(key = "hdd.serial.hacked.accounts.allow.select.decomposables", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_SELECT_DECOMPOSABLES;

	@Property(key = "hdd.serial.hacked.accounts.allow.remodel.items", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_REMODEL_ITEMS;

	@Property(key = "hdd.serial.hacked.accounts.allow.use.items", defaultValue = "false")
	public static boolean HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_USE_ITEMS;
}
