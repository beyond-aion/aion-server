package com.aionemu.gameserver.skillengine.properties;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.DispelCategoryType;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author ATracer
 */
public class FirstTargetProperty {

	public static boolean set(Skill skill, Properties properties) {
		Creature effector = skill.getEffector();
		switch (properties.getFirstTarget()) {
			case ME:
				skill.setFirstTargetRangeCheck(false);
				skill.setFirstTarget(effector);
				break;
			case TARGETORME:
				if (effector.equals(skill.getFirstTarget()))
					break;
				boolean changeTargetToMe = false;
				if (skill.getFirstTarget() == null) {
					changeTargetToMe = true;
				} else {
					switch (properties.getTargetRelation()) {
						case ENEMY:
							if (!skill.getFirstTarget().isEnemy(effector))
								changeTargetToMe = true;
							break;
						case FRIEND:
							if (skill.getFirstTarget().isEnemy(effector))
								changeTargetToMe = true;
							break;
						case MYPARTY:
							if (!isTargetTeamMember(skill, false)) {
								if (skill.getFirstTarget().isEnemy(effector)) {
									changeTargetToMe = true;
								} else {
									PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_SKILL_INVALID_TARGET_PARTY_ONLY());
									return false;
								}
							}
							break;
					}
					if (!changeTargetToMe && !isTargetAllowed(skill, skill.getFirstTarget()))
						changeTargetToMe = true;
				}
				if (changeTargetToMe) {
					if (skill.getFirstTarget() != null && effector instanceof Player playerEffector)
						PacketSendUtility.sendPacket(playerEffector, SM_SYSTEM_MESSAGE.STR_SKILL_AUTO_CHANGE_TARGET_TO_MY());
					skill.setFirstTarget(effector);
				}
				break;
			case TARGET:
				// Exception for effect skills which are not used directly
				if (skill.getSkillId() > 8000 && skill.getSkillId() < 9000)
					break;
				// Exception for NPC skills which applied on players
				if (skill.getSkillTemplate().getDispelCategory() == DispelCategoryType.NPC_BUFF
					|| skill.getSkillTemplate().getDispelCategory() == DispelCategoryType.NPC_DEBUFF_PHYSICAL)
					break;

				TargetRelationAttribute relation = skill.getSkillTemplate().getProperties().getTargetRelation();
				if (skill.getFirstTarget() == null || skill.getFirstTarget().equals(effector)) {
					if (effector instanceof Player playerEffector) {
						if (skill.getSkillTemplate().getProperties().getTargetType() == TargetRangeAttribute.AREA)
							return skill.getFirstTarget() != null;

						TargetRangeAttribute type = skill.getSkillTemplate().getProperties().getTargetType();
						if ((relation != TargetRelationAttribute.ALL && relation != TargetRelationAttribute.MYPARTY && relation != TargetRelationAttribute.FRIEND)
							|| type == TargetRangeAttribute.PARTY) {
							PacketSendUtility.sendPacket(playerEffector, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID());
							return false;
						}
					}
				}

				if (relation == TargetRelationAttribute.FRIEND) {
					if (skill.getFirstTarget() == null || effector.isEnemy(skill.getFirstTarget())) {
						if (effector instanceof Player playerEffector)
							PacketSendUtility.sendPacket(playerEffector, SM_SYSTEM_MESSAGE.STR_SKILL_INVALID_TARGET_NOTENEMY_ONLY());
						return false;
					}
				} else if (relation == TargetRelationAttribute.MYPARTY) {
					if (!isTargetTeamMember(skill, false)) {
						if (effector instanceof Player playerEffector)
							PacketSendUtility.sendPacket(playerEffector, SM_SYSTEM_MESSAGE.STR_SKILL_INVALID_TARGET_PARTY_ONLY());
						return false;
					}
				} else if (relation != TargetRelationAttribute.ENEMY && !isTargetAllowed(skill, skill.getFirstTarget())) {
					if (effector instanceof Player playerEffector) {
						PacketSendUtility.sendPacket(playerEffector, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID());
					}
					return false;
				}
				break;
			case MYPET:
				if (effector instanceof Player playerEffector) {
					Summon summon = playerEffector.getSummon();
					if (summon == null || !isTargetAllowed(skill, summon)) {
						PacketSendUtility.sendPacket(playerEffector, SM_SYSTEM_MESSAGE.STR_SKILL_INVALID_TARGET_PET_ONLY());
						return false;
					}
					skill.setFirstTarget(summon);
				} else {
					return false;
				}
				break;
			case MYMASTER:
				if (effector instanceof Summon summon) {
					if (summon.getMaster() != null)
						skill.setFirstTarget(summon.getMaster());
					else
						return false;
				} else {
					return false;
				}
				break;
			case PASSIVE:
				skill.setFirstTarget(effector);
				break;
			case TARGET_MYPARTY_NONVISIBLE: // Summon Group Member
				if (!isTargetTeamMember(skill, true))
					return false;

				skill.setFirstTargetRangeCheck(false);
				break;
			case POINT:
				skill.setFirstTarget(effector);
				return true;
		}

		if (skill.getFirstTarget() != null) {
			// update heading for npcs (players may look in a different direction)
			if (effector instanceof Npc && !effector.equals(skill.getFirstTarget()))
				effector.getPosition().setH(PositionUtil.getHeadingTowards(effector, skill.getFirstTarget()));
			skill.getEffectedList().add(skill.getFirstTarget());
		}
		return true;
	}

	private static boolean isTargetTeamMember(Skill skill, boolean onlyGroup) {
		if (skill.getFirstTarget() instanceof Player && skill.getEffector() instanceof Player) {
			Player effector = (Player) skill.getEffector();
			TemporaryPlayerTeam<?> team = onlyGroup ? effector.getCurrentGroup() : effector.getCurrentTeam();
			if (team != null) {
				for (Player member : team.getMembers()) {
					if (member.equals(skill.getFirstTarget()) && !member.equals(skill.getEffector()))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return true = allow buff, false = deny buff
	 */
	public static boolean isTargetAllowed(Skill skill, Creature target) {
		Creature source = skill.getEffector();
		return TargetRelationProperty.isBuffAllowed(source, target);
	}

}
