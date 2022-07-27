package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectReserved;
import com.aionemu.gameserver.skillengine.model.EffectResult;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * This packet show cast spell result (including hit time).
 * 
 * @author alexa026, Sweetkr
 */
public class SM_CASTSPELL_RESULT extends AionServerPacket {

	private Creature effector;
	private Creature target;
	private Skill skill;
	private int cooldown;
	private int hitTime;
	private List<Effect> effects;
	private int dashStatus;
	private int targetType;
	private boolean chainSuccess;

	public SM_CASTSPELL_RESULT(Skill skill, List<Effect> effects, int hitTime, boolean chainSuccess, int dashStatus) {
		this.skill = skill;
		this.effector = skill.getEffector();
		this.target = skill.getFirstTarget();
		this.effects = effects;
		this.cooldown = skill.getCooldown();
		this.chainSuccess = chainSuccess;
		this.targetType = 0;
		this.hitTime = hitTime;
		this.dashStatus = dashStatus;
	}

	public SM_CASTSPELL_RESULT(Skill skill, List<Effect> effects, int hitTime, boolean chainSuccess, int dashStatus, int targetType) {
		this(skill, effects, hitTime, chainSuccess, dashStatus);
		this.targetType = targetType;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(effector.getObjectId());
		writeC(targetType);
		switch (targetType) {
			case 0:
			case 3:
			case 4:
				writeD(target.getObjectId());
				break;
			case 1:
				writeF(skill.getX());
				writeF(skill.getY());
				writeF(skill.getZ());
				break;
			case 2:
				writeF(skill.getX());
				writeF(skill.getY());
				writeF(skill.getZ());
				writeF(0);// unk1
				writeF(0);// unk2
				writeF(0);// unk3
				writeF(0);// unk4
				writeF(0);// unk5
				writeF(0);// unk6
				writeF(0);// unk7
				writeF(0);// unk8
				break;
		}
		writeH(skill.getSkillTemplate().getSkillId());
		writeC(skill.getSkillTemplate().getLvl());
		writeD(cooldown);
		writeH(hitTime);
		writeC(0); // unk

		/**
		 * 0 : no chain skill 16 : no damage to all target like dodge, resist or effect size is 0 32 : regular Seen: 0xA0 for skill 2723, 0x22 for skill
		 * 1169; Skill id doesn't fit to this structure: 2395
		 */
		if (effects.isEmpty())
			writeC(16);
		else if (chainSuccess)
			writeC(32);
		else
			writeC(0);

		if (skill.getItemTemplate() != null && skill.getItemTemplate().isCombatActivated()) {
			writeC(2);
			writeD(skill.getItemObjectId());
			writeD(skill.getItemTemplate().getTemplateId());
			writeC(0); // unk 0
		} else {
			if (skill.getSkillMethod() == Skill.SkillMethod.PENALTY)
				writeC(4);
			else
				writeC(0);
			writeC(this.dashStatus);
			switch (this.dashStatus) {
				case 1:
				case 2:
				case 3:
				case 4:
				case 6:
					writeC(skill.getH());
					writeF(skill.getX());
					writeF(skill.getY());
					writeF(skill.getZ());
					break;
			}
		}

		writeH(effects.size());
		for (Effect effect : effects) {
			Creature effected = effect.getOriginalEffected();

			if (effected != null) {
				writeD(effected.getObjectId());
				writeC(effect.getEffectResult().getId());// 0 - NORMAL, 1 - ABSORBED, 2 - CONFLICT, 3 - DODGE, 4 - RESIST
				writeC(effect.getEffectedHp() == -1 ? effected.getLifeStats().getHpPercentage() : effect.getEffectedHp()); // target %hp
			} else { // point point skills
				writeD(effector.getObjectId());
				writeC(0);
				writeC(100);
			}

			writeC(effector.getLifeStats().getHpPercentage()); // attacker %hp

			/**
			 * Spell Status 1 : stumble 2 : knockback 4 : open aerial 8 : close aerial 16 : spin 32 : block 64 : parry 128 : dodge 256 : resist
			 */
			writeC(effect.getSpellStatus().getId());
			writeC(effect.getSuccessfulEffectsAsByte());
			writeH(0);
			writeC(effect.getCarvedSignet()); // current carve signet count
			switch (effect.getSpellStatus().getId()) {
				case 1:
				case 2:
				case 4:
				case 8:
					writeF(effect.getTargetX());
					writeF(effect.getTargetY());
					writeF(effect.getTargetZ());
					break;
				case 16:
					writeC(effect.getEffector().getHeading());
					break;
				default:
					switch (effect.getSubEffectType()) {
						case PULL:
						case PULL_NPC:
						case SIMPLE_MOVE_BACK:
							writeF(effect.getTargetX());
							writeF(effect.getTargetY());
							writeF(effect.getTargetZ());
							break;
					}
					break;
			}

			Set<EffectReserved> reservedEffects = effect.getReservedEffectsToSend();
			writeC(reservedEffects.size()); // it's success effect count (MP and HP heal, for example, always atleast 1)
			for (EffectReserved er : reservedEffects) {
				writeC(er.getType().getValue());// HP - 0 , MP - 1, FP - 2, DP - 3?
				writeD(er.getValueToSend());
				writeC(effect.getAttackStatus().getId());
				boolean isCounter = effect.getAttackStatus().isCounterSkill();
				if (effect.getEffectResult() == EffectResult.RESIST)
					isCounter = true;
				// setting counter skill from packet to have the best synchronization of time with client
				if (effect.getEffected() instanceof Player) {
					if (isCounter)
						((Player) effect.getEffected()).setLastCounterSkill(effect.getEffectResult() == EffectResult.RESIST ? AttackStatus.RESIST : effect
							.getAttackStatus());
				}

				/**
				 * shield Type: 1: reflector 2: normal shield 8: protect effect (ex. skillId: 417 Bodyguard) 16: mp shield TODO find out 4
				 */
				writeC(effect.getShieldDefense());
				switch (effect.getShieldDefense()) {
					case 0:
					case 2:
						break;
					case 8:
					case 10:
						writeD(effect.getProtectorId()); // protectorId
						writeD(effect.getProtectedDamage()); // protected damage
						writeD(effect.getProtectedSkillId()); // skillId
						break;
					default:
						writeD(effect.getProtectorId()); // protectorId
						writeD(effect.getProtectedDamage()); // protected damage
						writeD(effect.getProtectedSkillId()); // skillId
						writeD(effect.getReflectedDamage()); // reflect damage
						writeD(effect.getReflectedSkillId()); // skill id
						writeD(effect.getMpAbsorbed());
						writeD(effect.getMpShieldSkillId()); // skill id
						break;
				}
			}
		}
	}
}
