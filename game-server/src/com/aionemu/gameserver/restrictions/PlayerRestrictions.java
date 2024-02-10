package com.aionemu.gameserver.restrictions;

import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.templates.item.ItemUseLimits;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
import com.aionemu.gameserver.model.templates.panels.SkillPanel;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_RESPONSE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.services.VortexService;
import com.aionemu.gameserver.services.ban.ChatBanService;
import com.aionemu.gameserver.services.player.PlayerChatService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.skillengine.model.TransformType;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author lord_rex, Sippolo
 */
public class PlayerRestrictions {

	private static boolean checkFly(Player player, VisibleObject target) {
		if (player.isUsingFlyTeleport() || player.isInPlayerMode(PlayerMode.WINDSTREAM)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_RESTRICTION_NO_FLY());
			return false;
		}

		if (target instanceof Player playerTarget) {
			if (playerTarget.isUsingFlyTeleport() || playerTarget.isInPlayerMode(PlayerMode.WINDSTREAM)) {
				return false;
			}
		}
		return true;
	}

	public static boolean canUseSkill(Player player, Skill skill) {
		if (player.isInPrison()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ACCUSE_TARGET_IS_NOT_VALID());
			return false;
		}
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

		if (!player.canAttack() && !template.hasEvadeEffect()) {
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

	public static boolean canInviteToGroup(Player player, Player target) {
		return canInviteToTeam(player, target, false, player.getPlayerGroup());
	}

	public static boolean canInviteToAlliance(Player player, Player target) {
		return canInviteToTeam(player, target, true, player.getPlayerAlliance());
	}

	private static boolean canInviteToTeam(Player player, Player target, boolean isAlliance, TemporaryPlayerTeam<? extends TeamMember<Player>> team) {
		if (player.isDead()) {
			PacketSendUtility.sendPacket(player, isAlliance ? SM_SYSTEM_MESSAGE.STR_FORCE_CANT_INVITE_WHEN_DEAD() : SM_SYSTEM_MESSAGE.STR_PARTY_CANT_INVITE_WHEN_DEAD());
			return false;
		}
		if (player.isInPrison()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_CANT_INVITE_PARTY_COMMAND());
			return false;
		}
		if (target == null) {
			PacketSendUtility.sendPacket(player, isAlliance ? SM_SYSTEM_MESSAGE.STR_FORCE_NO_USER_TO_INVITE() : SM_SYSTEM_MESSAGE.STR_PARTY_NO_USER_TO_INVITE());
			return false;
		}
		if (target.isInCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS) && !target.isInFfaTeamMode()
				|| player.isInCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS) && !player.isInFfaTeamMode()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DISABLE("FFA mode"));
			return false;
		}
		if (AutoGroupService.getInstance().isInAutoInstance(player) || AutoGroupService.getInstance().isInAutoInstance(target)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_CANT_INVITE_PARTY_COMMAND());
			return false;
		}
		if (team != null) {
			if (!team.isLeader(player) && (!(team instanceof PlayerAlliance alliance) || !alliance.isViceCaptain(player))) {
				PacketSendUtility.sendPacket(player, isAlliance ? SM_SYSTEM_MESSAGE.STR_FORCE_ONLY_LEADER_CAN_INVITE() : SM_SYSTEM_MESSAGE.STR_PARTY_ONLY_LEADER_CAN_INVITE());
				return false;
			}
			if (team.isFull()) {
				PacketSendUtility.sendPacket(player, isAlliance ? SM_SYSTEM_MESSAGE.STR_FORCE_CANT_ADD_NEW_MEMBER() : SM_SYSTEM_MESSAGE.STR_PARTY_CANT_ADD_NEW_MEMBER());
				return false;
			}
		}
		if (target.equals(player)) {
			PacketSendUtility.sendPacket(player, isAlliance ? SM_SYSTEM_MESSAGE.STR_FORCE_CAN_NOT_INVITE_SELF() : SM_SYSTEM_MESSAGE.STR_PARTY_CAN_NOT_INVITE_SELF());
			return false;
		}
		if (target.getRace() != player.getRace() && (isAlliance ? !GroupConfig.ALLIANCE_INVITEOTHERFACTION : !GroupConfig.GROUP_INVITEOTHERFACTION)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_PARTY_CANT_INVITE_OTHER_RACE());
			return false;
		}
		if (target.isDead()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_UI_PARTY_DEAD());
			return false;
		}
		TemporaryPlayerTeam<? extends TeamMember<Player>> targetTeam = target.getCurrentTeam();
		if (targetTeam != null) {
			if (targetTeam == team) {
				PacketSendUtility.sendPacket(player, isAlliance ? SM_SYSTEM_MESSAGE.STR_FORCE_HE_IS_ALREADY_MEMBER_OF_OUR_FORCE(target.getName()) : SM_SYSTEM_MESSAGE.STR_PARTY_HE_IS_ALREADY_MEMBER_OF_OUR_PARTY(target.getName()));
				return false;
			}
			if (isAlliance && targetTeam instanceof PlayerGroup targetGroup) {
				if (team != null && targetGroup.size() + team.size() > team.getMaxMemberCount()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_INVITE_FAILED_NOT_ENOUGH_SLOT());
					return false;
				}
			} else {
				PacketSendUtility.sendPacket(player, targetTeam instanceof PlayerAlliance ? SM_SYSTEM_MESSAGE.STR_FORCE_ALREADY_OTHER_FORCE(target.getName()) : SM_SYSTEM_MESSAGE.STR_PARTY_HE_IS_ALREADY_MEMBER_OF_OTHER_PARTY(target.getName()));
				return false;
			}
		}
		if (team instanceof PlayerAlliance alliance && alliance.getTeamType().isDefence()) {
			if (targetTeam != null) {
				for (Player tm : targetTeam.getMembers()) {
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
		return true;
	}

	public static boolean canAttack(Player player, VisibleObject target) {
		if (player.isInPrison()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ACCUSE_TARGET_IS_NOT_VALID());
			return false;
		}

		if (!player.isSpawned() || target == null || !checkFly(player, target) || player.getLifeStats().isAboutToDie() || player.isDead())
			return false;

		if (!player.canAttack()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_ATTACK_WHILE_IN_ABNORMAL_STATE());
			PacketSendUtility.sendPacket(player, SM_ATTACK_RESPONSE.STOP_WITHOUT_MESSAGE(player.getGameStats().getAttackCounter()));
			return false;
		}

		if (!(target instanceof Creature)) {
			PacketSendUtility.sendPacket(player, SM_ATTACK_RESPONSE.STOP_INVALID_TARGET(player.getGameStats().getAttackCounter()));
			return false;
		}

		Creature creature = (Creature) target;

		if (creature.isDead() || creature.getLifeStats().isAboutToDie()) {
			PacketSendUtility.sendPacket(player, SM_ATTACK_RESPONSE.STOP_INVALID_TARGET(player.getGameStats().getAttackCounter()));
			return false;
		}

		// cannot attack while transformed
		if (player.getTransformModel().getRes3() == 1) {
			return false;
		}

		return player.isEnemy(creature);
	}

	public static boolean canTrade(Player player) {
		if (player == null || player.isDead() || !player.isOnline())
			return false;
		if (GameServer.isShuttingDownSoon()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DISABLE("Shutdown Progress"));
			return false;
		}
		if (player.isTrading()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXCHANGE_PARTNER_IS_EXCHANGING_WITH_OTHER());
			return false;
		}
		return true;
	}

	public static boolean canChat(Player player) {
		if (player == null || !player.isOnline())
			return false;

		if (player.isInPrison()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INGAME_BLOCK_IN_NO_CHAT(player.getPrisonDurationSeconds() / 60 + 1));
			return false;
		}

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

	public static boolean canUseItem(Player player, Item item) {
		if (player == null || !player.isOnline())
			return false;

		if (player.isInPrison()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ACCUSE_TARGET_IS_NOT_VALID());
			return false;
		}

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

		if (player.getStore() != null) { // You cannot use an item while running a Private Store.
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_USE_ITEM_DURING_PATH_FLYING(ChatUtil.l10n(1400061)));
			return false;
		}

		// Prevents potion spamming, and relogging to use kisks/aether jelly/long CD items.
		if (player.hasCooldown(item)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANT_USE_UNTIL_DELAY_TIME());
			return false;
		}

		ItemActions itemActions = item.getItemTemplate().getActions();
		if (itemActions == null || itemActions.getItemActions().isEmpty()) {
			if (!QuestEngine.getInstance().isRegisteredQuestItem(item.getItemId())) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_IS_NOT_USABLE());
				return false;
			}
		}

		ItemUseLimits limits = item.getItemTemplate().getUseLimits();
		if (limits.getGenderPermitted() != null && limits.getGenderPermitted() != player.getGender()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_GENDER());
			return false;
		}

		if (item.getItemTemplate().getRace() != Race.PC_ALL && item.getItemTemplate().getRace() != player.getRace()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_RACE());
			return false;
		}

		if (!item.getItemTemplate().isClassSpecific(player.getCommonData().getPlayerClass())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_CLASS());
			return false;
		}

		int requiredLevel = item.getItemTemplate().getRequiredLevel(player.getPlayerClass());
		if (requiredLevel > player.getLevel()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_TOO_LOW_LEVEL_MUST_BE_THIS_LEVEL(item.getL10n(), requiredLevel));
			return false;
		}

		byte levelRestrict = item.getItemTemplate().getMaxLevelRestrict(player.getPlayerClass());
		if (levelRestrict != 0 && player.getLevel() > levelRestrict) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_TOO_HIGH_LEVEL(levelRestrict, item.getL10n()));
			return false;
		}

		if (item.getItemTemplate().hasAreaRestriction()) {
			ZoneName restriction = item.getItemTemplate().getUseArea();
			if (!player.isInsideItemUseZone(restriction)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_USE_ITEM_IN_CURRENT_POSITION());
				return false;
			}
		}

		if (item.getItemTemplate().getActivationRace() != null) {
			// TODO: check retail messages
			if (!(player.getTarget() instanceof Creature)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANT_FIND_VALID_TARGET());
				return false;
			}
			if (((Creature) player.getTarget()).getRace() != item.getItemTemplate().getActivationRace()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST_TO_CURRENT_TARGET());
				return false;
			}
		}

		return true;
	}

	public static boolean canChangeEquip(Player player) {
		if (player.isInPrison()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ACCUSE_TARGET_IS_NOT_VALID());
			return false;
		}
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
}
