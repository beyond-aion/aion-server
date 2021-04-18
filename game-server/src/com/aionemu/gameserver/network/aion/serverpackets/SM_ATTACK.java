package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.controllers.attack.AttackResult;
import com.aionemu.gameserver.model.animations.AttackHandAnimation;
import com.aionemu.gameserver.model.animations.AttackTypeAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author -Nemesiss-, Sweetkr
 */
public class SM_ATTACK extends AionServerPacket {

	private int attackno;
	private int time;
	private AttackHandAnimation attackHandAnimation;
	private AttackTypeAnimation attackTypeAnimation;
	private List<AttackResult> attackList;
	private Creature attacker;
	private Creature target;
	private Effect criticalEffect;

	public SM_ATTACK(Creature attacker, Creature target, int attackno, int time, AttackTypeAnimation attackTypeAnimation, AttackHandAnimation attackHandAnimation, List<AttackResult> attackList) {
		this(attacker, target, attackno, time, attackTypeAnimation, attackHandAnimation, attackList, null);
	}

	public SM_ATTACK(Creature attacker, Creature target, int attackno, int time, AttackTypeAnimation attackTypeAnimation, AttackHandAnimation attackHandAnimation, List<AttackResult> attackList, Effect criticalEffect) {
		this.attacker = attacker;
		this.target = target;
		this.attackno = attackno;// empty
		this.time = time;// empty
		this.attackHandAnimation = attackHandAnimation;
		this.attackTypeAnimation = attackTypeAnimation;
		this.attackList = attackList;
		this.criticalEffect = criticalEffect;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(attacker.getObjectId());
		writeC(attackno);
		writeH(time);
		writeC(attackTypeAnimation.getId());
		writeC(attackHandAnimation.getId());

		writeD(target.getObjectId());

		int attackerMaxHp = attacker.getLifeStats().getMaxHp();
		int attackerCurrHp = attacker.getLifeStats().getCurrentHp();
		int targetMaxHp = target.getLifeStats().getMaxHp();
		int targetCurrHp = target.getLifeStats().getCurrentHp();

		writeC((byte) (100f * targetCurrHp / targetMaxHp)); // target %hp
		writeC((byte) (100f * attackerCurrHp / attackerMaxHp)); // attacker %hp

		// TODO refactor attack controller
		switch (attackList.get(0).getAttackStatus().getId()) // Counter skills
		{
			case -60: // case CRITICAL_BLOCK
			case 4: // case BLOCK
				writeH(32);
				break;
			case -62: // case CRITICAL_PARRY
			case 2: // case PARRY
				writeH(64);
				break;
			case -64: // case CRITICAL_DODGE
			case 0: // case DODGE
				writeH(128);
				break;
			case -58: // case PHYSICAL_CRITICAL_RESIST
			case 6: // case RESIST
				writeH(256); // need more info becuz sometimes 0
				break;
			default:
				if (criticalEffect != null) {
					if (target instanceof Player)
						writeH(criticalEffect.getSkillId() == 8218 ? 1 : 2);
					else
						writeH(criticalEffect.getSkillId() == 8218 ? 1025 : 1026);
				} else {
					writeH(0);
				}
				break;
		}
		// setting counter skill from packet to have the best synchronization of time with client
		if (target instanceof Player) {
			if (attackList.get(0).getAttackStatus().isCounterSkill())
				((Player) target).setLastCounterSkill(attackList.get(0).getAttackStatus());
		}

		writeH(0);
		if (criticalEffect != null) {
			writeF(criticalEffect.getTargetX());
			writeF(criticalEffect.getTargetY());
			writeF(criticalEffect.getTargetZ());
		}
		// TODO! those 2h (== d) up is some kind of very weird flag...
		// writeD(attackFlag);
		/*
		 * if(attackFlag & 0x10A0F != 0) { writeF(0); writeF(0); writeF(0); } if(attackFlag & 0x10010 != 0) { writeC(0); } if(attackFlag & 0x10000 != 0) {
		 * writeD(0); writeD(0); }
		 */

		writeC(attackList.size());
		for (AttackResult attack : attackList) {
			writeD(attack.getDamage());
			writeC(attack.getAttackStatus().getId());

			byte shieldType = (byte) attack.getShieldType();
			writeC(shieldType);

			/**
			 * shield Type: 1: reflector 2: normal shield 8: protect effect (ex. skillId: 417 Bodyguard) TODO find out 4
			 */
			switch (shieldType) {
				case 0:
				case 2:
					break;
				case 8:
				case 10:
					writeD(attack.getProtectorId()); // protectorId
					writeD(attack.getProtectedDamage()); // protected damage
					writeD(attack.getProtectedSkillId()); // skillId
					break;
				case 16:
					writeD(0);
					writeD(0);
					writeD(0);
					writeD(0);
					writeD(0);
					writeD(attack.getMpAbsorbed());
					writeD(attack.getReflectedSkillId()); // skill id
					break;
				default:
					writeD(attack.getProtectorId()); // protectorId
					writeD(attack.getProtectedDamage()); // protected damage
					writeD(attack.getProtectedSkillId()); // skillId
					writeD(attack.getReflectedDamage()); // reflect damage
					writeD(attack.getReflectedSkillId()); // skill id
					writeD(0);
					writeD(0);
					break;
			}
		}
		writeC(0);
	}

}
