package com.aionemu.gameserver.restrictions;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author lord_rex
 */
public class PrisonRestrictions extends AbstractRestrictions {

	@Override
	public boolean isRestricted(Player player, Class<? extends Restrictions> callingRestriction) {
		if (isInPrison(player)) {
			PacketSendUtility.sendMessage(player, "You are in prison!");
			return true;
		}

		return false;
	}

	@Override
	public boolean canAttack(Player player, VisibleObject target) {
		if (isInPrison(player)) {
			PacketSendUtility.sendMessage(player, "You cannot attack in prison!");
			return false;
		}

		return true;
	}

	@Override
	public boolean canUseSkill(Player player, Skill skill) {
		if (isInPrison(player)) {
			PacketSendUtility.sendMessage(player, "You cannot use skills in prison!");
			return false;
		}

		return true;
	}

	@Override
	public boolean canAffectBySkill(Player player, VisibleObject target, Skill skill) {
		return true;
	}

	@Override
	public boolean canChat(Player player) {
		if (isInPrison(player)) {
			PacketSendUtility.sendMessage(player, "You cannot chat in prison!");
			return false;
		}

		return true;
	}

	@Override
	public boolean canInviteToGroup(Player player, Player target) {
		if (isInPrison(player)) {
			PacketSendUtility.sendMessage(player, "You cannot invite members to group in prison!");
			return false;
		}

		return true;
	}

	@Override
	public boolean canInviteToAlliance(Player player, Player target) {
		if (isInPrison(player)) {
			PacketSendUtility.sendMessage(player, "You cannot invite members to alliance in prison!");
			return false;
		}

		return true;
	}

	@Override
	public boolean canChangeEquip(Player player) {
		if (isInPrison(player)) {
			PacketSendUtility.sendMessage(player, "You cannot equip / unequip item in prison!");
			return false;
		}

		return true;
	}

	@Override
	public boolean canUseItem(Player player, Item item) {
		if (isInPrison(player)) {
			PacketSendUtility.sendMessage(player, "You cannot use item in prison!");
			return false;
		}
		return true;
	}

	private boolean isInPrison(Player player) {
		return player.isInPrison() || player.getWorldId() == WorldMapType.DE_PRISON.getId() || player.getWorldId() == WorldMapType.DF_PRISON.getId();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.gameserver.restrictions.Restrictions#canPrivateStore(com.aionemu.gameserver.model.gameobjects.player.Player)
	 */
	@Override
	public boolean canPrivateStore(Player player) {
		if (isInPrison(player)) {
			return false;
		}

		return true;
	}
}
