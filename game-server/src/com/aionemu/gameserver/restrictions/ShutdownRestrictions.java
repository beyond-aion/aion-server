package com.aionemu.gameserver.restrictions;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author lord_rex
 */
public class ShutdownRestrictions extends AbstractRestrictions {

	@Override
	public boolean isRestricted(Player player, Class<? extends Restrictions> callingRestriction) {
		if (isInShutdownProgress(player)) {
			PacketSendUtility.sendMessage(player, "You are in shutdown progress!");
			return true;
		}
		return false;
	}

	@Override
	public boolean canAttack(Player player, VisibleObject target) {
		if (isInShutdownProgress(player)) {
			PacketSendUtility.sendMessage(player, "You cannot attack in Shutdown progress!");
			return false;
		}
		return true;
	}

	@Override
	public boolean canAffectBySkill(Player player, VisibleObject target, Skill skill) {
		return true;
	}

	@Override
	public boolean canUseSkill(Player player, Skill skill) {
		if (isInShutdownProgress(player)) {
			PacketSendUtility.sendMessage(player, "You cannot use skills in Shutdown progress!");
			return false;
		}
		return true;
	}

	@Override
	public boolean canChat(Player player) {
		if (isInShutdownProgress(player)) {
			PacketSendUtility.sendMessage(player, "You cannot chat in Shutdown progress!");
			return false;
		}
		return true;
	}

	@Override
	public boolean canInviteToGroup(Player player, Player target) {
		if (isInShutdownProgress(player)) {
			PacketSendUtility.sendMessage(player, "You cannot invite members to group in Shutdown progress!");
			return false;
		}
		return true;
	}

	@Override
	public boolean canInviteToAlliance(Player player, Player target) {
		if (isInShutdownProgress(player)) {
			PacketSendUtility.sendMessage(player, "You cannot invite members to alliance in Shutdown progress!");
			return false;
		}
		return true;
	}

	@Override
	public boolean canChangeEquip(Player player) {
		if (isInShutdownProgress(player)) {
			PacketSendUtility.sendMessage(player, "You cannot equip / unequip item in Shutdown progress!");
			return false;
		}
		return true;
	}

	@Override
	public boolean canTrade(Player player) {
		if (isInShutdownProgress(player)) {
			PacketSendUtility.sendMessage(player, "You cannot trade in Shutdown progress!");
			return false;
		}
		return true;
	}

	@Override
	public boolean canUseWarehouse(Player player) {
		if (isInShutdownProgress(player)) {
			PacketSendUtility.sendMessage(player, "You cannot use warehouse in Shutdown progress!");
			return false;
		}
		return true;
	}

	private boolean isInShutdownProgress(Player player) {
		return player.getController().isInShutdownProgress();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.gameserver.restrictions.Restrictions#canPrivateStore(com.aionemu.gameserver.model.gameobjects.player.Player)
	 */
	@Override
	public boolean canPrivateStore(Player player) {
		if (isInShutdownProgress(player)) {
			return false;
		}
		return true;
	}
}
