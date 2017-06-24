package com.aionemu.gameserver.restrictions;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.templates.item.ItemUseLimits;
import com.aionemu.gameserver.model.templates.panels.SkillPanel;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.services.VortexService;
import com.aionemu.gameserver.services.ban.ChatBanService;
import com.aionemu.gameserver.services.player.PlayerChatService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.skillengine.model.TransformType;
import com.aionemu.gameserver.skillengine.properties.TargetRelationAttribute;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author lord_rex modified by Sippolo
 */
public class PlayerRestrictions extends AbstractRestrictions {

	@Override
	public boolean canAffectBySkill(Player player, VisibleObject target, Skill skill) {
		if (skill == null)
			return false;

		if (target instanceof Player && !player.equals(target)) {
			Player tPlayer = (Player) target;
			if (tPlayer.getRace() != player.getRace()) {
				if (!tPlayer.isEnemyFrom(player))
					return false;
			} else if (tPlayer.isDueling(player)) {
				if (skill.getSkillTemplate().getProperties().getTargetRelation() != TargetRelationAttribute.ENEMY) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID());
					return false;
				}
			}
		}

		if (player.isUsingFlyTeleport() || (target instanceof Player && ((Player) target).isUsingFlyTeleport()))
			return false;

		if (((Creature) target).getLifeStats().isAboutToDie() && !skill.isNonTargetAOE())
			return false;

		if (((Creature) target).isDead() && !skill.getSkillTemplate().hasResurrectEffect() && !skill.isNonTargetAOE()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID());
			return false;
		}

		// cant resurrect non players and non dead
		if (skill.getSkillTemplate().hasResurrectEffect() && (!(target instanceof Player) || !((Creature) target).isDead()))
			return false;

		if (!skill.getSkillTemplate().hasEvadeEffect()) {
			if (player.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE))
				return false;
		}

		if (player.getStore() != null) { // You cannot use an item while running a Private Store.
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_USE_ITEM_DURING_PATH_FLYING(new DescriptionId(2800123)));
			return false;
		}

		return true;
	}

	private boolean checkFly(Player player, VisibleObject target) {
		if (player.isUsingFlyTeleport() || player.isInPlayerMode(PlayerMode.WINDSTREAM)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_RESTRICTION_NO_FLY());
			return false;
		}

		if (target instanceof Player) {
			Player playerTarget = (Player) target;
			if (playerTarget.isUsingFlyTeleport() || playerTarget.isInPlayerMode(PlayerMode.WINDSTREAM)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean canUseSkill(Player player, Skill skill) {
		VisibleObject target = player.getTarget();
		SkillTemplate template = skill.getSkillTemplate();

		// TODO check if its ok
		if (!checkFly(player, target) || player.getLifeStats().isAboutToDie() || player.isDead()) {
			return false;
		}
		// check if is casting to avoid multicast exploit
		// TODO cancel skill if other is used
		if (player.isCasting())
			return false;

		if ((!player.canAttack()) && !template.hasEvadeEffect()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_ATTACK_WHILE_IN_ABNORMAL_STATE());
			return false;
		}

		// in 3.0 players can use remove shock even when silenced
		if (template.getType() == SkillType.MAGICAL && player.getEffectController().isAbnormalSet(AbnormalState.SILENCE) && !template.hasEvadeEffect()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST_MAGIC_SKILL_WHILE_SILENCED());
			return false;
		}

		if (template.getType() == SkillType.PHYSICAL && player.getEffectController().isAbnormalSet(AbnormalState.BIND)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST_PHYSICAL_SKILL_IN_FEAR());
			return false;
		}

		if (player.isSkillDisabled(template))
			return false;

		// cannot use skills while transformed
		if (player.getTransformModel().isActive()) {
			if (player.getTransformModel().getBanUseSkills() == 1) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_CAST_IN_SHAPECHANGE());
				return false;
			}
			// can use only panel skills in FORM1
			if (player.getTransformModel().getType() == TransformType.FORM1) {
				SkillPanel panel = DataManager.PANEL_SKILL_DATA.getSkillPanel(player.getTransformModel().getPanelId());
				if (panel == null || !panel.isSkillPresent(skill.getSkillId())) {
					AuditLogger.log(player, "tried to use non panel skill while transformed in TransformType.FORM1");
					return false;
				}
			}
		}

		// Fix for Summon Group Member, cannot be used while either caster or summoned is actively in combat
		// example skillId: 1606
		if (skill.getSkillTemplate().hasRecallInstant()) {
			if (!(target instanceof Player))
				return false;
			if (player.getController().isInCombat() || ((Player) target).getController().isInCombat()
				|| ((Player) target).getTransformModel().getRes1() == 1)// cannot be summoned while transformed
			{
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_Recall_CANNOT_ACCEPT_EFFECT(target.getName()));
				return false;
			}
		}

		if (template.hasResurrectEffect()) {
			if (!(target instanceof Player)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID());
				return false;
			}
			Player targetPlayer = (Player) target;
			if (!targetPlayer.isDead()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID());
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean canInviteToGroup(Player player, Player target) {
		if (target == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_NO_USER_TO_INVITE());
			return false;
		}

		if (target.isInCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS) && !target.isInFfaTeamMode()
			|| player.isInCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS) && !player.isInFfaTeamMode()) {
			PacketSendUtility.sendMessage(player, "You can't invite players in FFA mode");
			return false;
		}

		if (player.isInInstance() && AutoGroupService.getInstance().isAutoInstance(player.getInstanceId())
			|| target.isInInstance() && AutoGroupService.getInstance().isAutoInstance(target.getInstanceId())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_CANT_INVITE_PARTY_COMMAND());
			return false;
		}

		PlayerGroup group = player.getPlayerGroup();

		if (group != null && group.isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_CANT_ADD_NEW_MEMBER());
			return false;
		} else if (group != null && !player.equals(group.getLeader().getObject())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_ONLY_LEADER_CAN_INVITE());
			return false;
		} else if (target.getRace() != player.getRace() && !GroupConfig.GROUP_INVITEOTHERFACTION) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_CANT_INVITE_OTHER_RACE());
			return false;
		} else if (target.equals(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_CAN_NOT_INVITE_SELF());
			return false;
		} else if (target.isDead()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_UI_PARTY_DEAD());
			return false;
		} else if (player.isDead()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_CANT_INVITE_WHEN_DEAD());
			return false;
		} else if (player.isInGroup() && target.isInGroup() && player.getPlayerGroup().getTeamId() == target.getPlayerGroup().getTeamId()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_HE_IS_ALREADY_MEMBER_OF_OUR_PARTY(target.getName()));
		} else if (target.isInGroup()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_HE_IS_ALREADY_MEMBER_OF_OTHER_PARTY(target.getName()));
			return false;
		} else if (target.isInAlliance()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_ALREADY_OTHER_FORCE(target.getName()));
			return false;
		}

		return true;
	}

	@Override
	public boolean canInviteToAlliance(Player player, Player target) {
		if (target == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_NO_USER_TO_INVITE());
			return false;
		}

		if (target.isInCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS) && !target.isInFfaTeamMode()
			|| player.isInCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS) && !player.isInFfaTeamMode()) {
			PacketSendUtility.sendMessage(player, "You can't invite players in FFA mode");
			return false;
		}

		if (player.isInInstance() && AutoGroupService.getInstance().isAutoInstance(player.getInstanceId())
			|| target.isInInstance() && AutoGroupService.getInstance().isAutoInstance(target.getInstanceId())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_CANT_INVITE_PARTY_COMMAND());
			return false;
		}

		if (target.getRace() != player.getRace() && !GroupConfig.ALLIANCE_INVITEOTHERFACTION) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_CANT_INVITE_OTHER_RACE());
			return false;
		}

		PlayerAlliance alliance = player.getPlayerAlliance();
		if (alliance != null && alliance.getTeamType().isDefence()) {
			if (target.isInTeam()) {
				for (Player tm : target.getCurrentTeam().getMembers()) {
					if (tm.isInInstance()) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_CANT_INVITE_WHEN_HE_IS_IN_INSTANCE());
						return false;
					} else if (!VortexService.getInstance().isInsideVortexZone(tm)) {
						// TODO: chk on retail
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_CANT_INVITE_WHEN_HE_IS_ASKED_QUESTION(tm.getName()));
						return false;
					}
				}
			} else if (!VortexService.getInstance().isInsideVortexZone(target)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_INVITE_DEFENSE_FORCE());
				return false;
			}
		}

		if (target.isInAlliance()) {
			if (target.getPlayerAlliance() == alliance) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_HE_IS_ALREADY_MEMBER_OF_OUR_ALLIANCE(target.getName()));
				return false;
			} else {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_ALREADY_OTHER_FORCE(target.getName()));
				return false;
			}
		}

		if (alliance != null && alliance.isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_CANT_ADD_NEW_MEMBER());
			return false;
		}

		if (alliance != null && !alliance.isSomeCaptain(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_ONLY_PARTY_LEADER_CAN_LEAVE_ALLIANCE());
			return false;
		}

		if (target.equals(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_CAN_NOT_INVITE_SELF());
			return false;
		}

		if (target.isDead()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_UI_PARTY_DEAD());
			return false;
		}

		if (player.isDead()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_CANT_INVITE_WHEN_DEAD());
			return false;
		}

		if (target.isInGroup()) {
			PlayerGroup targetGroup = target.getPlayerGroup();
			if (alliance != null && (targetGroup.size() + alliance.size() >= 24)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_INVITE_FAILED_NOT_ENOUGH_SLOT());
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean canAttack(Player player, VisibleObject target) {
		if (!player.isSpawned() || target == null || !checkFly(player, target) || player.getLifeStats().isAboutToDie()
			|| player.isDead())
			return false;

		if (!player.canAttack()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_ATTACK_WHILE_IN_ABNORMAL_STATE());
			return false;
		}

		if (!(target instanceof Creature)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return false;
		}

		Creature creature = (Creature) target;

		if (creature.isDead() || creature.getLifeStats().isAboutToDie()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return false;
		}

		// cannot attack while transformed
		if (player.getTransformModel().getRes3() == 1) {
			return false;
		}

		return player.isEnemy(creature);
	}

	@Override
	public boolean canUseWarehouse(Player player) {
		if (player == null || !player.isOnline())
			return false;

		// TODO retail message to requester and player
		if (player.isTrading())
			return false;

		return true;
	}

	@Override
	public boolean canTrade(Player player) {
		if (player == null || !player.isOnline())
			return false;

		if (player.isTrading()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXCHANGE_PARTNER_IS_EXCHANGING_WITH_OTHER());
			return false;
		}

		return true;
	}

	@Override
	public boolean canChat(Player player) {
		if (player == null || !player.isOnline())
			return false;

		if (ChatBanService.isBanned(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INGAME_BLOCK_IN_NO_CHAT(ChatBanService.getBanMinutes(player)));
			return false;
		}

		if (PlayerChatService.isFlooding(player)) {
			ChatBanService.banPlayer(player, 2 * 60 * 1000);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FLOODING());
			return false;
		}

		return true;
	}

	@Override
	public boolean canUseItem(Player player, Item item) {
		if (player == null || !player.isOnline())
			return false;

		if (player.getLifeStats().isAboutToDie() || player.isDead())
			return false;

		if (player.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_USE_ITEM_WHILE_IN_ABNORMAL_STATE());
			return false;
		}

		// cannot use item while transformed
		if (player.getTransformModel().getRes5() == 1) {
			// client sends message by itself
			return false;
		}

		if (player.getStore() != null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_IS_NOT_USABLE());
			return false;
		}

		ItemUseLimits limits = item.getItemTemplate().getUseLimits();
		if (limits.getGenderPermitted() != null && limits.getGenderPermitted() != player.getGender()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_GENDER());
			return false;
		}

		if (item.getItemTemplate().hasAreaRestriction()) {
			ZoneName restriction = item.getItemTemplate().getUseArea();
			if (restriction == ZoneName.get("_ABYSS_CASTLE_AREA_")) {
				boolean isInFortZone = false;
				for (ZoneInstance zone : player.findZones()) {
					if (zone.getZoneTemplate().getZoneType().equals(ZoneClassName.FORT)) {
						isInFortZone = true;
						break;
					}
				}
				if (!isInFortZone) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_USE_ITEM_IN_CURRENT_POSITION());
					return false;
				}
			} else if (restriction != null && !player.isInsideItemUseZone(restriction)) {
				// You cannot use that item here.
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_USE_ITEM_IN_CURRENT_POSITION());
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean canChangeEquip(Player player) {
		if (player.getController().isUnderStance()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_EQUIP_ITEM_WHILE_IN_CURRENT_STANCE());
			return false;
		}
		if (player.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_EQUIP_ITEM_WHILE_IN_ABNORMAL_STATE());
			return false;
		}
		if (player.getController().hasScheduledTask(TaskId.ITEM_USE)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANT_EQUIP_ITEM_IN_ACTION());
			return false;
		}
		return true;
	}

	@Override
	public boolean canFly(Player player) {
		if (!player.getCommonData().isDaeva()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GLIDE_ONLY_DEVA_CAN());
			return false;
		}
		if (!player.hasAccess(AdminConfig.FREE_FLIGHT)) {
			if (!player.isInsideZoneType(ZoneType.FLY)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FLYING_FORBIDDEN_HERE());
				return false;
			}
		}
		// If player is under NoFly Effect, show the retail message for it and return
		if (player.isUnderNoFly()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANT_FLY_NOW_DUE_TO_NOFLY());
			return false;
		}
		// cannot fly in transform
		if (player.getTransformModel().getRes6() == 1) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FLY_CANNOT_FLY_POLYMORPH_STATUS());
			return false;
		}
		// cannot fly while private store is active
		if (player.getStore() != null)
			return false;

		return true;
	}

	@Override
	public boolean canGlide(Player player) {
		if (!player.getCommonData().isDaeva()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GLIDE_ONLY_DEVA_CAN());
			return false;
		}
		// cannot glide in transform
		if (player.getTransformModel().getRes6() == 1) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GLIDE_CANNOT_GLIDE_POLYMORPH_STATUS());
			return false;
		}

		return true;
	}

	@Override
	public boolean canPrivateStore(Player player) {
		if (player.isFlying()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_DISABLED_IN_FLY_MODE());
			return false;
		}
		if (player.getMoveController().isInMove()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_DISABLED_IN_MOVING_OBJECT());
			return false;
		}
		if (player.isInAttackMode()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_DISABLED_IN_COMBAT_MODE());
			return false;
		}
		if (player.isTrading()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_OPEN_STORE_DURING_CRAFTING()); // name "crafting" is NC fail, msg is correct
			return false;
		}
		if (player.isInPlayerMode(PlayerMode.RIDE)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PERSONAL_SHOP_RESTRICTION_RIDE());
			return false;
		}
		if (player.getEffectController().isAbnormalSet(AbnormalState.HIDE)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_DISABLED_IN_HIDDEN_MODE());
			return false;
		}
		if (player.isDead())
			return false;
		if (player.isInState(CreatureState.RESTING))
			return false;

		return true;
	}

}
