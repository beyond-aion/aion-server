package com.aionemu.gameserver.controllers.attack;

import com.aionemu.gameserver.skillengine.model.HitType;
import com.aionemu.gameserver.skillengine.model.ShieldType;

/**
 * @author ATracer modified by Sippolo, kecimis
 */
public class AttackResult {

	private int damage;

	private AttackStatus attackStatus;
	
	private HitType hitType = HitType.EVERYHIT;

	/**
	 * shield effects related
	 */
	private int shieldType;
	private int reflectedDamage = 0;
	private int reflectedSkillId = 0;
	private int protectedSkillId = 0;
	private int protectedDamage = 0;
	private int protectorId = 0;
	private int mpAbsorbed = 0;
	private int mpShieldSkillId = 0;
	
	private boolean launchSubEffect = true;

	public AttackResult(int damage, AttackStatus attackStatus) {
		this.damage = damage;
		this.attackStatus = attackStatus;
	}

	public AttackResult(int damage, AttackStatus attackStatus, HitType type) {
		this(damage, attackStatus);
		this.hitType = type;
	}

	/**
	 * @return the damage
	 */
	public int getDamage() {
		return damage;
	}

	/**
	 * @param damage
	 *          the damage to set
	 */
	public void setDamage(int damage) {
		this.damage = damage;
	}

	/**
	 * @return the attackStatus
	 */
	public AttackStatus getAttackStatus() {
		return attackStatus;
	}

	/**
	 * @return the Damage Type
	 */
	public HitType getDamageType() {
		return hitType;
	}

	/**
	 * @param type
	 *          the Damage Type to set
	 */
	public void setDamageType(HitType type) {
		this.hitType = type;
	}
	
	/**
	 * shield effects related
	 * 
	 */
	
	/**
	 * @return the shieldType
	 */
	public int getShieldType() {
		return shieldType;
	}

	/**
	 * @param shieldType
	 *          the shieldType to set
	 */
	public void setShieldType(int shieldType) {
		this.shieldType |= shieldType;
	}

	public int getReflectedDamage() {
		return this.reflectedDamage;
	}

	public void setReflectedDamage(int reflectedDamage) {
		this.reflectedDamage = reflectedDamage;
	}

	public int getReflectedSkillId() {
		return this.reflectedSkillId;
	}

	public void setReflectedSkillId(int skillId) {
		this.reflectedSkillId = skillId;
	}
	
	public int getProtectedSkillId() {
		return this.protectedSkillId;
	}

	public void setProtectedSkillId(int skillId) {
		this.protectedSkillId = skillId;
	}
	
	public int getProtectedDamage() {
		return this.protectedDamage;
	}

	public void setProtectedDamage(int protectedDamage) {
		this.protectedDamage = protectedDamage;
	}
	
	public int getProtectorId() {
		return this.protectorId;
	}

	public void setProtectorId(int protectorId) {
		this.protectorId = protectorId;
	}
	
	public boolean isLaunchSubEffect() {
		return launchSubEffect;
	}
	
	public void setLaunchSubEffect(boolean launchSubEffect) {
		this.launchSubEffect = launchSubEffect;
	}

	public int getMpAbsorbed() {
		return mpAbsorbed;
	}

	public void setMpAbsorbed(int mpAbsorbed) {
		this.mpAbsorbed = mpAbsorbed;
	}

	
	/**
	 * @return the mpShieldSkillId
	 */
	public int getMpShieldSkillId() {
		return mpShieldSkillId;
	}

	
	/**
	 * @param mpShieldSkillId the mpShieldSkillId to set
	 */
	public void setMpShieldSkillId(int mpShieldSkillId) {
		this.mpShieldSkillId = mpShieldSkillId;
	}
	
}
