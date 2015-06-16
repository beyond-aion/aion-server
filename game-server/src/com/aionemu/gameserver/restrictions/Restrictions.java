package com.aionemu.gameserver.restrictions;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author lord_rex
 */
public interface Restrictions {

	public boolean isRestricted(Player player, Class<? extends Restrictions> callingRestriction);

	public boolean canAttack(Player player, VisibleObject target);

	public boolean canAffectBySkill(Player player, VisibleObject target, Skill skill);

	public boolean canUseSkill(Player player, Skill skill);

	public boolean canChat(Player player);

	public boolean canInviteToGroup(Player player, Player target);

	public boolean canInviteToAlliance(Player player, Player target);

	public boolean canChangeEquip(Player player);

	public boolean canUseWarehouse(Player player);

	public boolean canTrade(Player player);

	public boolean canUseItem(Player player, Item item);
	
	public boolean canFly(Player player);
	
	public boolean canGlide(Player player);
	
	public boolean canPrivateStore(Player player);
}
