package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * System message packet.
 * 
 * @author -Nemesiss-
 * @author EvilSpirit
 * @author Luno :D
 * @author Avol!
 * @author Simple :)
 * @author Sarynth
 */
public final class SM_SYSTEM_MESSAGE extends AionServerPacket {

	/**
	 * You inflicted %num1 damage on %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_ATTACK(int num1, String value0) {
		return new SM_SYSTEM_MESSAGE(1200000, num1, value0);
	}

	/**
	 * Critical Hit! You inflicted %num1 critical damage on %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_CRITICAL(int num1, String value0) {
		return new SM_SYSTEM_MESSAGE(1200001, num1, value0);
	}

	/**
	 * %0 inflicted %num2 damage on %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_SUMMONED_ATTACK(String value0, int num2, String value1) {
		return new SM_SYSTEM_MESSAGE(1200002, value0, num2, value1);
	}

	/**
	 * Critical Hit! %0 inflicted %num2 critical damage on %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_SUMMONED_CRITICAL(String value0, int num2, String value1) {
		return new SM_SYSTEM_MESSAGE(1200003, value0, num2, value1);
	}

	/**
	 * %1 received %num2 damage from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_SUMMONED_ENEMY_ATTACK(String value1, int num2, String value0) {
		return new SM_SYSTEM_MESSAGE(1200004, value1, num2, value0);
	}

	/**
	 * Critical Hit! %0 inflicted %num2 critical damage on %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_SUMMONED_ENEMY_CRITICAL(String value0, int num2, String value1) {
		return new SM_SYSTEM_MESSAGE(1200005, value0, num2, value1);
	}

	/**
	 * %0 has been dismissed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUMMON_UNSUMMONED(String value0) {
		return new SM_SYSTEM_MESSAGE(1200006, value0);
	}

	/**
	 * %0 is in Stand-by mode.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUMMON_STAY_MODE(String value0) {
		return new SM_SYSTEM_MESSAGE(1200007, value0);
	}

	/**
	 * %0 starts to attack the enemy.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUMMON_ATTACK_MODE(String value0) {
		return new SM_SYSTEM_MESSAGE(1200008, value0);
	}

	/**
	 * %0 is in Guard mode.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUMMON_GUARD_MODE(String value0) {
		return new SM_SYSTEM_MESSAGE(1200009, value0);
	}

	/**
	 * %0 is in Resting mode.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUMMON_REST_MODE(String value0) {
		return new SM_SYSTEM_MESSAGE(1200010, value0);
	}

	/**
	 * You unsummon %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUMMON_UNSUMMON_FOLLOWER(String value0) {
		return new SM_SYSTEM_MESSAGE(1200011, value0);
	}

	/**
	 * You summon %0 Spirit. Cooldown time begins when it is unsummoned, and takes longer when the spirit is killed by an enemy.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUMMON_COOLDOWN(String value0) {
		return new SM_SYSTEM_MESSAGE(1200012, value0);
	}

	/**
	 * You are bleeding.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_BLEED_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200214);
	}

	/**
	 * You are no longer bleeding.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_BLEED_END() {
		return new SM_SYSTEM_MESSAGE(1200215);
	}

	/**
	 * You cannot see.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_BLIND_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200216);
	}

	/**
	 * You can see again.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_BLIND_END() {
		return new SM_SYSTEM_MESSAGE(1200217);
	}

	/**
	 * You are charmed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_CHARM_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200218);
	}

	/**
	 * You are no longer charmed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_CHARM_END() {
		return new SM_SYSTEM_MESSAGE(1200219);
	}

	/**
	 * You are confused.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_CONFUSE_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200220);
	}

	/**
	 * You are no longer confused.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_CONFUSE_END() {
		return new SM_SYSTEM_MESSAGE(1200221);
	}

	/**
	 * A defense wall has been created to convert received damage into HP.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_CONVERT_HEAL_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200222);
	}

	/**
	 * The defense wall that converts received damage into HP has expired.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_CONVERT_HEAL_END() {
		return new SM_SYSTEM_MESSAGE(1200223);
	}

	/**
	 * A defense wall that absorbs damage has been created.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_SHIELD_MAGIC_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200224);
	}

	/**
	 * The defense wall that absorbs damage has expired.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_SHIELD_MAGIC_END() {
		return new SM_SYSTEM_MESSAGE(1200225);
	}

	/**
	 * You are cursed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_CURSE_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200226);
	}

	/**
	 * You are no longer cursed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_CURSE_END() {
		return new SM_SYSTEM_MESSAGE(1200227);
	}

	/**
	 * You are diseased.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_DISEASE_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200228);
	}

	/**
	 * You recovered from the disease.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_DISEASE_END() {
		return new SM_SYSTEM_MESSAGE(1200229);
	}

	/**
	 * You are struck by fear.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_FEAR_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200230);
	}

	/**
	 * You recovered from your fear.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_FEAR_END() {
		return new SM_SYSTEM_MESSAGE(1200231);
	}

	/**
	 * You are invisible.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_INVISIBLE_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200232);
	}

	/**
	 * You are no longer invisible.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_INVISIBLE_END() {
		return new SM_SYSTEM_MESSAGE(1200233);
	}

	/**
	 * You are paralyzed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_PARALYZE_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200234);
	}

	/**
	 * You are no longer paralyzed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_PARALYZE_END() {
		return new SM_SYSTEM_MESSAGE(1200235);
	}

	/**
	 * You are petrified.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_PETRIFICATION_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200236);
	}

	/**
	 * You are no longer petrified.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_PETRIFICATION_END() {
		return new SM_SYSTEM_MESSAGE(1200237);
	}

	/**
	 * You are poisoned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_POISON_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200238);
	}

	/**
	 * You are no longer poisoned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_POISON_END() {
		return new SM_SYSTEM_MESSAGE(1200239);
	}

	/**
	 * You are immobilized.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_ROOT_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200240);
	}

	/**
	 * You are no longer immobilized.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_ROOT_END() {
		return new SM_SYSTEM_MESSAGE(1200241);
	}

	/**
	 * You fell asleep.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_SLEEP_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200242);
	}

	/**
	 * You woke up.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_SLEEP_END() {
		return new SM_SYSTEM_MESSAGE(1200243);
	}

	/**
	 * You have been stunned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_STUN_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200244);
	}

	/**
	 * You are no longer stunned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_STUN_END() {
		return new SM_SYSTEM_MESSAGE(1200245);
	}

	/**
	 * You are silenced.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_SILENCE_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200246);
	}

	/**
	 * You are no longer silenced.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_SILENCE_END() {
		return new SM_SYSTEM_MESSAGE(1200247);
	}

	/**
	 * You are snared in mid-air.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_OPEN_AERIAL_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200248);
	}

	/**
	 * You are released from the Aerial Snare.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_OPEN_AERIAL_END() {
		return new SM_SYSTEM_MESSAGE(1200249);
	}

	/**
	 * Your movement speed has decreased.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_SNARE_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200250);
	}

	/**
	 * You have normal movement speed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_SNARE_END() {
		return new SM_SYSTEM_MESSAGE(1200251);
	}

	/**
	 * Your attack speed is decreased.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_SLOW_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200252);
	}

	/**
	 * You have normal attack speed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_SLOW_END() {
		return new SM_SYSTEM_MESSAGE(1200253);
	}

	/**
	 * You are spinning from shock.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_SPIN_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200254);
	}

	/**
	 * You are no longer in shock.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_SPIN_END() {
		return new SM_SYSTEM_MESSAGE(1200255);
	}

	/**
	 * You fell down from shock.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_STUMBLE_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200256);
	}

	/**
	 * You are no longer in shock.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_STUMBLE_END() {
		return new SM_SYSTEM_MESSAGE(1200257);
	}

	/**
	 * You are stunned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_STAGGER_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200258);
	}

	/**
	 * You are no longer stunned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_STAGGER_END() {
		return new SM_SYSTEM_MESSAGE(1200259);
	}

	/**
	 * You are bound.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_BIND_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200260);
	}

	/**
	 * You are no longer bound.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_BIND_END() {
		return new SM_SYSTEM_MESSAGE(1200261);
	}

	/**
	 * You are being pulled.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_PULLED_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1200262);
	}

	/**
	 * You are no longer being pulled.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_EFFECT_PULLED_END() {
		return new SM_SYSTEM_MESSAGE(1200263);
	}

	/**
	 * You became blinded after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Blind_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200277, skillname);
	}

	/**
	 * You became confused after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Confuse_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200278, skillname);
	}

	/**
	 * You became diseased after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Disease_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200279, skillname);
	}

	/**
	 * You are struck with fear after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fear_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200280, skillname);
	}

	/**
	 * You became paralyzed after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Paralyze_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200281, skillname);
	}

	/**
	 * You became immobilized after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Root_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200282, skillname);
	}

	/**
	 * You became silenced after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Silence_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200283, skillname);
	}

	/**
	 * You fell asleep after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sleep_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200284, skillname);
	}

	/**
	 * You are spinning after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Spin_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200285, skillname);
	}

	/**
	 * You were knocked back from a shock after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stagger_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200286, skillname);
	}

	/**
	 * You fell down from shock after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stumble_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200287, skillname);
	}

	/**
	 * You became stunned after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stun_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200288, skillname);
	}

	/**
	 * You can see again
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Blind_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200289);
	}

	/**
	 * You are no longer confused.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Confuse_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200290);
	}

	/**
	 * You are no longer diseased.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Disease_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200291);
	}

	/**
	 * You recovered from your fear.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fear_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200292);
	}

	/**
	 * You are no longer paralyzed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Paralyze_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200293);
	}

	/**
	 * You are no longer immobilized.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Root_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200294);
	}

	/**
	 * You are no longer silenced.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Silence_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200295);
	}

	/**
	 * You woke up.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sleep_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200296);
	}

	/**
	 * You have stopped spinning.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Spin_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200297);
	}

	/**
	 * You are no longer staggering.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stagger_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200298);
	}

	/**
	 * You are no longer shocked.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stumble_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200299);
	}

	/**
	 * You are no longer stunned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stun_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200300);
	}

	/**
	 * Your loot rate has increased because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostDropRate_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200301, skillname);
	}

	/**
	 * Your visual range has reduced because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OutofSight_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200302, skillname);
	}

	/**
	 * You exchanged your enmity with the spirit's by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SwitchHostile_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200303, skillname);
	}

	/**
	 * You used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ReturnHome_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200304, skillname);
	}

	/**
	 * You began using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Aura_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200305, skillname);
	}

	/**
	 * You stopped using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Aura_END_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200306, skillname);
	}

	/**
	 * You used [%SkillName] and became bound.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bind_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200307, skillname);
	}

	/**
	 * You are bleeding after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200308, skillname);
	}

	/**
	 * You are cursed after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Curse_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200309, skillname);
	}

	/**
	 * You are unable to fly because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoFly_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200310, skillname);
	}

	/**
	 * You are snared in mid-air after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OpenAerial_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200311, skillname);
	}

	/**
	 * You became petrified after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Petrification_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200312, skillname);
	}

	/**
	 * You became poisoned after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200313, skillname);
	}

	/**
	 * Your attack speed has decreased after you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Slow_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200314, skillname);
	}

	/**
	 * Your movement speed has decreased after you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Snare_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200315, skillname);
	}

	/**
	 * You are no longer bound
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bind_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200316);
	}

	/**
	 * You are no longer bleeding.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200317);
	}

	/**
	 * You are released from the cursed state.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Curse_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200318);
	}

	/**
	 * You are able to fly again.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoFly_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200319);
	}

	/**
	 * You are released from the Aerial Snare.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OpenAerial_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200320);
	}

	/**
	 * You are no longer petrified.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Petrification_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200321);
	}

	/**
	 * You are no longer poisoned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200322);
	}

	/**
	 * Your attack speed is restored to normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Slow_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200323);
	}

	/**
	 * Your movement speed is restored to normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Snare_END_ME_TO_SELF() {
		return new SM_SYSTEM_MESSAGE(1200324);
	}

	/**
	 * You boosted your block by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysBlock_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200325, skillname);
	}

	/**
	 * You boosted your evasion by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysDodge_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200326, skillname);
	}

	/**
	 * You boosted your accuracy by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysHit_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200327, skillname);
	}

	/**
	 * You removed your elemental defense by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysNoResist_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200328, skillname);
	}

	/**
	 * You boosted your parry by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysParry_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200329, skillname);
	}

	/**
	 * You boosted your elemental defense by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysResist_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200330, skillname);
	}

	/**
	 * You boosted your recovery by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostHealEffect_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200331, skillname);
	}

	/**
	 * You changed your casting speed by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillCastingTime_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200332, skillname);
	}

	/**
	 * You changed your MP consumption by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillCost_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200333, skillname);
	}

	/**
	 * You reduced your MP consumption for mantra skills by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillToggleCost_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200334, skillname);
	}

	/**
	 * You boosted your spell skill by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSpellAttackEffect_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200335, skillname);
	}

	/**
	 * You inflicted %num0 damage on yourself by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BackDashATK_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200336, num0, skillname);
	}

	/**
	 * You %0d your enmity by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostHate_ME_TO_SELF(String value0d, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200337, value0d, skillname);
	}

	/**
	 * You inflicted %num0 damage and the rune carve effect on yourself by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CarveSignet_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200338, num0, skillname);
	}

	/**
	 * You received the HP recovery effect by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CaseHeal_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200339, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CaseHeal_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200340, num0, skillname);
	}

	/**
	 * You %0d your enmity by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ChangeHateOnAttacked_ME_TO_SELF(String value0d, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200341, value0d, skillname);
	}

	/**
	 * You are released from the Aerial Snare by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CloseAerial_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200342, skillname);
	}

	/**
	 * You recovered from the transformation by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200343, skillname);
	}

	/**
	 * You recovered HP by %num0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200344, num0, skillname);
	}

	/**
	 * You inflicted %num0 damage on yourself by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DashATK_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200345, num0, skillname);
	}

	/**
	 * You inflicted %num0 damage on yourself by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DeathBlow_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200346, num0, skillname);
	}

	/**
	 * Your recovery amount changed after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DeboostHealAmount_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200347, skillname);
	}

	/**
	 * You transformed yourself into a(n) %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Deform_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200348, value0, skillname);
	}

	/**
	 * You decreased your own flight time by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedFPATK_Instant_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200349, skillname);
	}

	/**
	 * You reduced your flight time by %num0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedFPATK_Instant_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200350, num0, skillname);
	}

	/**
	 * You will inflict damage on yourself in a moment because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSpellATK_Instant_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200351, skillname);
	}

	/**
	 * You reduced your flight time by %num0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSpellATK_Instant_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200352, num0, skillname);
	}

	/**
	 * You dispelled the magic effect by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Dispel_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200353, skillname);
	}

	/**
	 * You dispelled magical buffs by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelBuff_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200354, skillname);
	}

	/**
	 * You suffered %num0 damage and dispelled some of the magical buffs by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelBuffCounterATK_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200355, num0, skillname);
	}

	/**
	 * You dispelled magical debuffs by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuff_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200356, skillname);
	}

	/**
	 * You removed abnormal mental conditions by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuffMental_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200357, skillname);
	}

	/**
	 * You removed abnormal physical conditions by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuffPhysical_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200358, skillname);
	}

	/**
	 * You transferred %num0 DP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPTransfer_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200359, num0, skillname);
	}

	/**
	 * You expanded the range of mantra by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ExtendAuraRange_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200360, skillname);
	}

	/**
	 * You were forced to crash by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fall_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200361, skillname);
	}

	/**
	 * You decreased your own flight time by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200362, skillname);
	}

	/**
	 * You reduced your flight time by %num0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200363, num0, skillname);
	}

	/**
	 * Your flight time has increased by %num0 because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_Instant_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200364, num0, skillname);
	}

	/**
	 * Your flight time has been restored by [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200365, skillname);
	}

	/**
	 * You increased your flight time by %num0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200366, num0, skillname);
	}

	/**
	 * You increased the flight time by %num0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_Instant_HEAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200367, num0, skillname);
	}

	/**
	 * You are continuously recovering HP because of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200368, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200369, num0, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_Instant_HEAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200370, num0, skillname);
	}

	/**
	 * You converted damage to healing by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnAttacked_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200371, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnAttacked_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200372, num0, skillname);
	}

	/**
	 * You converted death to healing by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnTargetDead_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200373, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnTargetDead_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200374, num0, skillname);
	}

	/**
	 * You hid yourself by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Hide_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200375, skillname);
	}

	/**
	 * You %0d your enmity by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HostileUp_ME_TO_SELF(String value0d, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200376, value0d, skillname);
	}

	/**
	 * You made a magical counterattack by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MagicCounterATK_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200377, skillname);
	}

	/**
	 * You inflicted %num0 damage on yourself by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MagicCounterATK_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200378, num0, skillname);
	}

	/**
	 * You received %num0 damage due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MoveBehindATK_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200379, num0, skillname);
	}

	/**
	 * You recovered %num0 MP after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPAttack_Instant_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200380, num0, skillname);
	}

	/**
	 * You recovered MP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200381, skillname);
	}

	/**
	 * You recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200382, num0, skillname);
	}

	/**
	 * You recovered %num0 MP after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_Instant_HEAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200383, num0, skillname);
	}

	/**
	 * You boosted your recovery by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostHealEffect_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200384, skillname);
	}

	/**
	 * You boosted your skill by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostSkillAttack_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200385, skillname);
	}

	/**
	 * You boosted your critical hit skill by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostSkillCritical_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200386, skillname);
	}

	/**
	 * You boosted your skill by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeTypeBoostSkillLevel_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200387, skillname);
	}

	/**
	 * You made the spirit use its skills by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PetOrderUseUltraSkill_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200388, skillname);
	}

	/**
	 * You have transformed into %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Polymorph_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200389, value0, skillname);
	}

	/**
	 * You received %num0 damage due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcATK_Instant_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200390, num0, skillname);
	}

	/**
	 * You received %num0 damage due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcATK_Instant_Ratio_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200391, num0, skillname);
	}

	/**
	 * You increased the flight time by %num0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCFPHeal_Instant_HEAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200392, num0, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCHeal_Instant_HEAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200393, num0, skillname);
	}

	/**
	 * You recovered %num0 MP after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCMPHeal_Instant_HEAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200394, num0, skillname);
	}

	/**
	 * You protected yourself by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Protect_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200395, skillname);
	}

	/**
	 * You protected yourself from %num0 damage by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Protect_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200396, num0, skillname);
	}

	/**
	 * You received the effect by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Provoker_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200397, skillname);
	}

	/**
	 * You inflicted %num0 damage and the pull effect on yourself by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Pulled_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200398, num0, skillname);
	}

	/**
	 * You teleported yourself by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_RandomMoveLoc_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200399, skillname);
	}

	/**
	 * You ensured resurrection by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Rebirth_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200400, skillname);
	}

	/**
	 * You gave yourself the reflection effect by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200401, skillname);
	}

	/**
	 * You reflected %num0 damage by the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200402, num0, skillname);
	}

	/**
	 * You resurrected yourself by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Resurrect_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200403, skillname);
	}

	/**
	 * You resurrected and telerported yourself by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ResurrectPositional_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200404, skillname);
	}

	/**
	 * You received the see-through effect by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Search_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200405, skillname);
	}

	/**
	 * You have transformed into %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ShapeChange_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200406, value0, skillname);
	}

	/**
	 * You gave yourself a defense shield by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200407, skillname);
	}

	/**
	 * You protected yourself from %num0 damage by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_INTERVAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200408, num0, skillname);
	}

	/**
	 * You received %num0 damage due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SignetBurst_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200409, num0, skillname);
	}

	/**
	 * You received %num0 damage due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATK_Instant_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200410, num0, skillname);
	}

	/**
	 * You absorb %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATKDrain_Instant_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200411, num0, skillname);
	}

	/**
	 * You received continuous damage due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATK_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200412, skillname);
	}

	/**
	 * You received %num0 damage due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATK_Instant_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200413, num0, skillname);
	}

	/**
	 * You absorb %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200414, num0, skillname);
	}

	/**
	 * You absorb %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_Instant_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200415, num0, skillname);
	}

	/**
	 * Your movement speed has been increased by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sprint_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200416, skillname);
	}

	/**
	 * Your %0 has been weakened by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_StatDown_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200417, value0, skillname);
	}

	/**
	 * Your %0 has been boosted by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_StatUp_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200418, value0, skillname);
	}

	/**
	 * Your %0 resistance effects are weakened after using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SubTypeBoostResist_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200419, value0, skillname);
	}

	/**
	 * You changed the duration of %0 skills by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SubTypeExtendDuration_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200420, value0, skillname);
	}

	/**
	 * You summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Summon_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200421, value0, skillname);
	}

	/**
	 * You summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonBindingGroupGate_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200422, value0, skillname);
	}

	/**
	 * You summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonGroupGate_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200423, value0, skillname);
	}

	/**
	 * You summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonHoming_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200424, value0, skillname);
	}

	/**
	 * You summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonServant_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200425, value0, skillname);
	}

	/**
	 * You summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonTotem_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200426, value0, skillname);
	}

	/**
	 * You summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonTrap_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200427, value0, skillname);
	}

	/**
	 * You exchanged your MP with your HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SwitchHPMP_Instant_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200428, skillname);
	}

	/**
	 * %0 was changed using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_WeaponStatUp_ME_TO_SELF(String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200429, value0, skillname);
	}

	/**
	 * You blinded [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Blind_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200430, skilltarget, skillname);
	}

	/**
	 * You confused [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Confuse_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200431, skilltarget, skillname);
	}

	/**
	 * You diseased [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Disease_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200432, skilltarget, skillname);
	}

	/**
	 * You made [%SkillTarget] afraid by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fear_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200433, skilltarget, skillname);
	}

	/**
	 * You paralyzed [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Paralyze_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200434, skilltarget, skillname);
	}

	/**
	 * You immobilized [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Root_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200435, skilltarget, skillname);
	}

	/**
	 * You silenced [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Silence_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200436, skilltarget, skillname);
	}

	/**
	 * You put [%SkillTarget] to sleep by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sleep_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200437, skilltarget, skillname);
	}

	/**
	 * You span [%SkillTarget] around by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Spin_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200438, skilltarget, skillname);
	}

	/**
	 * You knocked [%SkillTarget] back by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stagger_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200439, skilltarget, skillname);
	}

	/**
	 * You knocked [%SkillTarget] over by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stumble_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200440, skilltarget, skillname);
	}

	/**
	 * You stunned [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stun_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200441, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] is no longer blind.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Blind_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200442, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer confused.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Confuse_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200443, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer diseased.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Disease_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200444, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer afraid.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fear_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200445, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer paralyzed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Paralyze_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200446, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer immobilized.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Root_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200447, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer silenced.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Silence_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200448, skilltarget);
	}

	/**
	 * [%SkillTarget] woke up.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sleep_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200449, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer spinning.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Spin_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200450, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer staggering.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stagger_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200451, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer shocked.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stumble_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200452, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer stunned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stun_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200453, skilltarget);
	}

	/**
	 * [%SkillTarget] was resurrected as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostDropRate_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200454, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s visual range has decreased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OutofSight_ME_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200455, skilltarget, skillcaster, skillname);
	}

	/**
	 * You exchanged [%SkillTarget]'s enmity with the spirit's by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SwitchHostile_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200456, skilltarget, skillname);
	}

	/**
	 * You used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ReturnHome_ME_TO_B(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200457, skillname);
	}

	/**
	 * You start using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Aura_ME_TO_B(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200458, skillname);
	}

	/**
	 * You stop using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Aura_END_ME_TO_B(String skillname) {
		return new SM_SYSTEM_MESSAGE(1200459, skillname);
	}

	/**
	 * You used [%SkillName] and [%SkillTarget] became bound.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bind_ME_TO_B(String skillname, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200460, skillname, skilltarget);
	}

	/**
	 * You caused [%SkillTarget] to bleed by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200461, skilltarget, skillname);
	}

	/**
	 * You cursed [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Curse_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200462, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] is unable to fly because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoFly_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200463, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] became snared in mid-air because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OpenAerial_ME_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200464, skilltarget, skillcaster, skillname);
	}

	/**
	 * You petrified [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Petrification_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200465, skilltarget, skillname);
	}

	/**
	 * You poisoned [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200466, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s attack speed has decreased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Slow_ME_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200467, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget]'s movement speed decreased as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Snare_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200468, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] is no longer bound
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bind_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200469, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer bleeding.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200470, skilltarget);
	}

	/**
	 * [%SkillTarget] recovered from the cursed state.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Curse_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200471, skilltarget);
	}

	/**
	 * [%SkillTarget] is able to fly again.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoFly_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200472, skilltarget);
	}

	/**
	 * [%SkillTarget] is released from the Aerial Snare.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OpenAerial_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200473, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer petrified.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Petrification_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200474, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer poisoned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200475, skilltarget);
	}

	/**
	 * [%SkillTarget]'s attack speed is restored to normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Slow_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200476, skilltarget);
	}

	/**
	 * [%SkillTarget]'s movement speed is restored to normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Snare_END_ME_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200477, skilltarget);
	}

	/**
	 * [%SkillCaster] has boosted [%SkillTarget]'s block by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysBlock_ME_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200478, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] has boosted [%SkillTarget]'s evasion by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysDodge_ME_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200479, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] has boosted [%SkillTarget]'s parry by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysHit_ME_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200480, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%Skillcaster] removed [%SkillTarget]'s elemental defense by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysNoResist_ME_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200481, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] has boosted [%SkillTarget]'s parry by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysParry_ME_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200482, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] is in the elemental maximum defense state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysResist_ME_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200483, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has boosted [%SkillTarget]'s recovery skill by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostHealEffect_ME_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200484, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s casting time increased as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillCastingTime_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200485, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s movement speed decreased as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillCost_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200486, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s movement speed decreased as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillToggleCost_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200487, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] has boosted [%SkillTarget]'s spell skill by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSpellAttackEffect_ME_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200488, skillcaster, skilltarget, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BackDashATK_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200489, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] %0d [%SkillTarget]'s enmity %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostHate_ME_TO_B(String skillcaster, String value0d, String skilltarget, String value0,
		String skillname) {
		return new SM_SYSTEM_MESSAGE(1200490, skillcaster, value0d, skilltarget, value0, skillname);
	}

	/**
	 * You inflicted %num0 damage and the rune carve effect on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CarveSignet_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200491, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] is recovering HP as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CaseHeal_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200492, skilltarget, skillname);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CaseHeal_INTERVAL_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200493, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] %0d [%SkillTarget]'s enmity %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ChangeHateOnAttacked_ME_TO_B(String skillcaster, String value0d, String skilltarget, String value0,
		String skillname) {
		return new SM_SYSTEM_MESSAGE(1200494, skillcaster, value0d, skilltarget, value0, skillname);
	}

	/**
	 * You released [%SkillTarget] from the Aerial Snare by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CloseAerial_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200495, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] received the transformation recovery effect as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200496, skilltarget, skillname);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_INTERVAL_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200497, num0, skilltarget, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DashATK_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200498, num0, skilltarget, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DeathBlow_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200499, num0, skilltarget, skillname);
	}

	/**
	 * You changed [%SkillTarget]'s recovery amount by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DeboostHealAmount_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200500, skilltarget, skillname);
	}

	/**
	 * You transformed [%SkillTarget] into %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Deform_ME_TO_B(String skilltarget, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200501, skilltarget, value0, skillname);
	}

	/**
	 * In a moment, [%SkillTarget]'s flight time will decrease because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedFPATK_Instant_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200502, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time decreased by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedFPATK_Instant_INTERVAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200503, skilltarget, num0, skillname);
	}

	/**
	 * [%SkillTarget] received the Delayed Blast effect as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSpellATK_Instant_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200504, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time decreased by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSpellATK_Instant_INTERVAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200505, skilltarget, num0, skillname);
	}

	/**
	 * You dispelled the magic effect from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Dispel_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200506, skilltarget, skillname);
	}

	/**
	 * You dispelled magical buffs from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelBuff_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200507, skilltarget, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] and dispelled some of its magical buffs by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelBuffCounterATK_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200508, num0, skilltarget, skillname);
	}

	/**
	 * You dispelled magical debuffs from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuff_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200509, skilltarget, skillname);
	}

	/**
	 * You removed abnormal mental conditions from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuffMental_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200510, skilltarget, skillname);
	}

	/**
	 * You removed abnormal physical conditions from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuffPhysical_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200511, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] is in the DP recovery state because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPTransfer_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200512, skilltarget, skillname);
	}

	/**
	 * You expanded [%SkillTarget]'s mantra range by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ExtendAuraRange_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200513, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] received the forced crash effect as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fall_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200514, skilltarget, skillname);
	}

	/**
	 * In a moment, [%SkillTarget]'s flight time will decrease because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200515, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time decreased by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_INTERVAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200516, skilltarget, num0, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time has decreased by %num0 because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_Instant_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200517, skilltarget, num0, skillname);
	}

	/**
	 * You restored [%SkillTarget]'s flight time by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200518, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time increased by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_INTERVAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200519, skilltarget, num0, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time has increased by %num0 because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_Instant_HEAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200520, skilltarget, num0, skillname);
	}

	/**
	 * [%SkillCaster] has caused [%SkillTarget] to recover HP over time by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_ME_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200521, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] recovered its HP by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_INTERVAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200522, skilltarget, num0, skillname);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_Instant_HEAL_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200523, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] converted [%SkillTarget]'s damage to healing by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnAttacked_ME_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200524, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] has recovered %num0 HP due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnAttacked_INTERVAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200525, skilltarget, num0, skillname);
	}

	/**
	 * [%SkillTarget] converted death to healing by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnTargetDead_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200526, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] has recovered %num0 HP due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnTargetDead_INTERVAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200527, skilltarget, num0, skillname);
	}

	/**
	 * You hid [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Hide_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200528, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] %0d [%SkillTarget]'s enmity %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HostileUp_ME_TO_B(String skillcaster, String value0d, String skilltarget, String value0,
		String skillname) {
		return new SM_SYSTEM_MESSAGE(1200529, skillcaster, value0d, skilltarget, value0, skillname);
	}

	/**
	 * You granted [%SkillTarget] a magical counterattack by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MagicCounterATK_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200530, skilltarget, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MagicCounterATK_INTERVAL_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200531, num0, skilltarget, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MoveBehindATK_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200532, num0, skilltarget, skillname);
	}

	/**
	 * You reduced [%SkillTarget]'s MP by %num0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPAttack_Instant_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200533, skilltarget, num0, skillname);
	}

	/**
	 * [%SkillCaster] has boosted [%SkillTarget]'s MP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_ME_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200534, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_INTERVAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200535, skilltarget, num0, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 MP because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_Instant_HEAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200536, skilltarget, num0, skillname);
	}

	/**
	 * [%SkillCaster] has boosted [%SkillTarget]'s recovery skill by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostHealEffect_ME_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200537, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] received the boost skill effect as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostSkillAttack_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200538, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] received the critical hit effect as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostSkillCritical_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200539, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] received the boost skill effect as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeTypeBoostSkillLevel_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200540, skilltarget, skillname);
	}

	/**
	 * Your spirit uses its skills on [%SkillTarget] as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PetOrderUseUltraSkill_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200541, skilltarget, skillname);
	}

	/**
	 * You transformed [%SkillTarget] into %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Polymorph_ME_TO_B(String skilltarget, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200542, skilltarget, value0, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcATK_Instant_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200543, num0, skilltarget, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcATK_Instant_Ratio_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200544, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time has increased by %num0 because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCFPHeal_Instant_HEAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200545, skilltarget, num0, skillname);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCHeal_Instant_HEAL_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200546, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] recovered MP by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCMPHeal_Instant_HEAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200547, skilltarget, num0, skillname);
	}

	/**
	 * You protected [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Protect_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200548, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] blocked %num0 damage due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Protect_INTERVAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200549, skilltarget, num0, skillname);
	}

	/**
	 * You affected [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Provoker_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200550, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] received %num0 damage and the pull effect as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Pulled_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200551, skilltarget, num0, skillname);
	}

	/**
	 * [%SkillTarget] teleported as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_RandomMoveLoc_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200552, skilltarget, skillname);
	}

	/**
	 * You placed [%SkillTarget] in the reserved resurrection state as by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Rebirth_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200553, skilltarget, skillname);
	}

	/**
	 * You gave [%SkillTarget] the reflection effect by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200554, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] reflected %num0 damage due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_INTERVAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200555, skilltarget, num0, skillname);
	}

	/**
	 * [%SkillTarget] has resurrected as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Resurrect_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200556, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] is in the resurrection state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ResurrectPositional_ME_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200557, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the see-through state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Search_ME_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200558, skilltarget, skillcaster, skillname);
	}

	/**
	 * You transformed [%SkillTarget] into %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ShapeChange_ME_TO_B(String skilltarget, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200559, skilltarget, value0, skillname);
	}

	/**
	 * [%SkillTarget] received the defense shield effect as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200560, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] blocked %num0 damage due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_INTERVAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200561, skilltarget, num0, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SignetBurst_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200562, num0, skilltarget, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATK_Instant_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200563, num0, skilltarget, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATKDrain_Instant_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200564, num0, skilltarget, skillname);
	}

	/**
	 * You inflicted continuous damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATK_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200565, skilltarget, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATK_Instant_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200566, num0, skilltarget, skillname);
	}

	/**
	 * You start to absorb [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200567, skilltarget, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_Instant_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200568, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] is in the movement speed increase state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sprint_ME_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200569, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has weakened [%SkillTarget]'s %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_StatDown_ME_TO_B(String skillcaster, String skilltarget, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200570, skillcaster, skilltarget, value0, skillname);
	}

	/**
	 * [%SkillCaster] has boosted [%SkillTarget]'s Physical Def by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_StatUp_ME_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200571, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s %0 resistance effects were weakened as you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SubTypeBoostResist_ME_TO_B(String skilltarget, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200572, skilltarget, value0, skillname);
	}

	/**
	 * You changed [%SkillTarget]'s %0 skill duration by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SubTypeExtendDuration_ME_TO_B(String skilltarget, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200573, skilltarget, value0, skillname);
	}

	/**
	 * You summoned %0 to [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Summon_ME_TO_B(String value0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200574, value0, skilltarget, skillname);
	}

	/**
	 * You summoned %0 to [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonBindingGroupGate_ME_TO_B(String value0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200575, value0, skilltarget, skillname);
	}

	/**
	 * You summoned %0 to [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonGroupGate_ME_TO_B(String value0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200576, value0, skilltarget, skillname);
	}

	/**
	 * You summoned %0 by using [%SkillName] to let it attack [%SkillTarget].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonHoming_ME_TO_B(String value0, String skillname, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200577, value0, skillname, skilltarget);
	}

	/**
	 * You summoned %0 by using [%SkillName] to let it attack [%SkillTarget].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonServant_ME_TO_B(String value0, String skillname, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200578, value0, skillname, skilltarget);
	}

	/**
	 * You summoned %0 to [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonTotem_ME_TO_B(String value0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200579, value0, skilltarget, skillname);
	}

	/**
	 * You summoned %0 to [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonTrap_ME_TO_B(String value0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200580, value0, skilltarget, skillname);
	}

	/**
	 * You caused [%SkillTarget] to exchange MP with HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SwitchHPMP_Instant_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200581, skilltarget, skillname);
	}

	/**
	 * You changed [%SkillTarget]'s %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_WeaponStatUp_ME_TO_B(String skilltarget, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200582, skilltarget, value0, skillname);
	}

	/**
	 * [%SkillCaster] has blinded you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Blind_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200583, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has confused you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Confuse_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200584, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has diseased you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Disease_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200585, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has made you afraid by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fear_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200586, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has diseased you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Paralyze_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200587, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has immobilized you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Root_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200588, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has silenced you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Silence_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200589, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has put you to sleep by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sleep_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200590, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has spun you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Spin_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200591, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has knocked you back by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stagger_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200592, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has knocked you down by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stumble_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200593, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has stunned you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stun_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200594, skillcaster, skillname);
	}

	/**
	 * You can see again
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Blind_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200595);
	}

	/**
	 * You are no longer confused.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Confuse_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200596);
	}

	/**
	 * You are no longer diseased.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Disease_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200597);
	}

	/**
	 * You recovered from your fear.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fear_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200598);
	}

	/**
	 * You are no longer paralyzed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Paralyze_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200599);
	}

	/**
	 * You are no longer immobilized.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Root_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200600);
	}

	/**
	 * You are no longer silenced.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Silence_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200601);
	}

	/**
	 * You woke up.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sleep_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200602);
	}

	/**
	 * You have stopped spinning.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Spin_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200603);
	}

	/**
	 * You are no longer staggering.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stagger_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200604);
	}

	/**
	 * You are no longer shocked.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stumble_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200605);
	}

	/**
	 * You are no longer stunned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stun_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200606);
	}

	/**
	 * Your loot rate has increased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostDropRate_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200607, skillcaster, skillname);
	}

	/**
	 * Your visual range has decreased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OutofSight_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200608, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] caused you to exchange your enmity with the spirit's by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SwitchHostile_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200609, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ReturnHome_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200610, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] started using [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Aura_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200611, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] stops using [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Aura_END_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200612, skillcaster, skillname);
	}

	/**
	 * You became bound because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bind_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200613, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] caused you to bleed by using [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200614, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has cursed you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Curse_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200615, skillcaster, skillname);
	}

	/**
	 * You are unable to fly because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoFly_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200616, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has snared you in mid-air by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OpenAerial_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200617, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has petrified you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Petrification_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200618, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has poisoned you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200619, skillcaster, skillname);
	}

	/**
	 * Your attack speed has decreased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Slow_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200620, skillcaster, skillname);
	}

	/**
	 * Your movement speed has decreased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Snare_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200621, skillcaster, skillname);
	}

	/**
	 * You are no longer bound
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bind_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200622);
	}

	/**
	 * You are no longer bleeding.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200623);
	}

	/**
	 * You are released from the cursed state.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Curse_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200624);
	}

	/**
	 * You are able to fly again.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoFly_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200625);
	}

	/**
	 * You are released from the Aerial Snare.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OpenAerial_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200626);
	}

	/**
	 * You are no longer petrified.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Petrification_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200627);
	}

	/**
	 * You are no longer poisoned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200628);
	}

	/**
	 * Your attack speed is restored to normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Slow_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200629);
	}

	/**
	 * Your movement speed is restored to normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Snare_END_A_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1200630);
	}

	/**
	 * [%SkillCaster] has boosted your block by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysBlock_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200631, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has boosted your evasion by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysDodge_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200632, skillcaster, skillname);
	}

	/**
	 * You received the boost accuracy effect because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysHit_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200633, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has removed your elemental defense by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysNoResist_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200634, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has boosted your parry by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysParry_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200635, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has maximized your elemental defense by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysResist_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200636, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has boosted your recovery skill by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostHealEffect_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200637, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has changed your casting speed by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillCastingTime_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200638, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has changed your MP consumption by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillCost_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200639, skillcaster, skillname);
	}

	/**
	 * Your mantra skill MP consumption has changed because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillToggleCost_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200640, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has boosted your spell skill by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSpellAttackEffect_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200641, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BackDashATK_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200642, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] inflicted enmity %0 on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostHate_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200643, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage and the rune carve effect on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CarveSignet_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200644, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] has boosted [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CaseHeal_A_TO_ME(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200645, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 HP because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CaseHeal_INTERVAL_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200646, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] inflicted enmity %0 on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ChangeHateOnAttacked_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200647, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] released you from the aerial snare by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CloseAerial_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200648, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] restored you from the transformation by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200649, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 HP because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_INTERVAL_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200650, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DashATK_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200651, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DeathBlow_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200652, skillcaster, num0, skillname);
	}

	/**
	 * Your recovery amount has changed because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DeboostHealAmount_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200653, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] transformed you into a(n) %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Deform_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200654, skillcaster, value0, skillname);
	}

	/**
	 * In a moment, [%SkillCaster] will decrease your flight time because they used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedFPATK_Instant_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200655, skillcaster, skillname);
	}

	/**
	 * Your flight time has increased by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedFPATK_Instant_INTERVAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200656, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted a Delayed Blast on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSpellATK_Instant_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200657, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s flight time has decreased by %num0 because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSpellATK_Instant_INTERVAL_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200658, skillcaster, num0, skillname);
	}

	/**
	 * Your magic effect was dispelled because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Dispel_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200659, skillcaster, skillname);
	}

	/**
	 * Your magical buffs were dispelled because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelBuff_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200660, skillcaster, skillname);
	}

	/**
	 * You suffered %num0 damage and lost some of your magical buffs because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelBuffCounterATK_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200661, num0, skillcaster, skillname);
	}

	/**
	 * Your magical debuffs were dispelled because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuff_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200662, skillcaster, skillname);
	}

	/**
	 * Your abnormal mental conditions were removed because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuffMental_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200663, skillcaster, skillname);
	}

	/**
	 * Your abnormal physical conditions were removed because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuffPhysical_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200664, skillcaster, skillname);
	}

	/**
	 * You received %num0 DP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPTransfer_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200665, num0, skillcaster, skillname);
	}

	/**
	 * Your aura range has expanded because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ExtendAuraRange_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200666, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] forced you to crash by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fall_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200667, skillcaster, skillname);
	}

	/**
	 * In a moment, [%SkillCaster] will decrease your flight time because they used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200668, skillcaster, skillname);
	}

	/**
	 * Your flight time decreased by %num0 due to the effect of [%SkillName] used by [%SkillCaster].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_INTERVAL_A_TO_ME(int num0, String skillname, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1200669, num0, skillname, skillcaster);
	}

	/**
	 * Your flight time decreased by %num0 due to the effect of [%SkillName] used by [%SkillCaster].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_Instant_A_TO_ME(int num0, String skillname, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1200670, num0, skillname, skillcaster);
	}

	/**
	 * [%SkillCaster] restored your flight time by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200671, skillcaster, skillname);
	}

	/**
	 * Your flight time has increased by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_INTERVAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200672, num0, skillcaster, skillname);
	}

	/**
	 * Your flight time has increased by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_Instant_HEAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200673, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is continuously restoring your HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200674, skillcaster, skillname);
	}

	/**
	 * You recovered %num0 HP because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_INTERVAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200675, num0, skillcaster, skillname);
	}

	/**
	 * You recovered %num0 HP because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_Instant_HEAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200676, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has converted damage dealt to you to healing by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnAttacked_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200677, skillcaster, skillname);
	}

	/**
	 * You recovered %num0 HP because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnAttacked_INTERVAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200678, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has converted death dealt to you to healing by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnTargetDead_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200679, skillcaster, skillname);
	}

	/**
	 * You recovered %num0 HP because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnTargetDead_INTERVAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200680, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has hidden you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Hide_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200681, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] inflicted enmity %0 on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HostileUp_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200682, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] has given you a magical counterattack by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MagicCounterATK_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200683, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MagicCounterATK_INTERVAL_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200684, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MoveBehindATK_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200685, skillcaster, num0, skillname);
	}

	/**
	 * Your MP has decreased by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPAttack_Instant_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200686, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has restored your MP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200687, skillcaster, skillname);
	}

	/**
	 * You recovered %num0 MP because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_INTERVAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200688, num0, skillcaster, skillname);
	}

	/**
	 * You recovered %num0 HP because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_Instant_HEAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200689, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has boosted your recovery skill by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostHealEffect_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200690, skillcaster, skillname);
	}

	/**
	 * You received the boost skill effect because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostSkillAttack_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200691, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has boosted your parry by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostSkillCritical_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200692, skillcaster, skillname);
	}

	/**
	 * You received the boost skill effect because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeTypeBoostSkillLevel_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200693, skillcaster, skillname);
	}

	/**
	 * The spirit uses its skills because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PetOrderUseUltraSkill_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200694, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has transformed you into a(n) %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Polymorph_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200695, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcATK_Instant_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200696, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcATK_Instant_Ratio_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200697, skillcaster, num0, skillname);
	}

	/**
	 * Your flight time has increased by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCFPHeal_Instant_HEAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200698, num0, skillcaster, skillname);
	}

	/**
	 * You recovered %num0 HP because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCHeal_Instant_HEAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200699, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCMPHeal_Instant_HEAL_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200700, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] has protected you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Protect_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200701, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] blocked %num0 damage through the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Protect_INTERVAL_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200702, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster]'s [%SkillName] affected you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Provoker_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200703, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage and pulled you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Pulled_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200704, skillcaster, num0, skillname);
	}

	/**
	 * You teleported because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_RandomMoveLoc_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200705, skillcaster, skillname);
	}

	/**
	 * You entered the reserved resurrection state because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Rebirth_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200706, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has cast a reflector on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200707, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] reflected %num0 damage through to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_INTERVAL_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200708, skillcaster, num0, skillname);
	}

	/**
	 * You resurrected as [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Resurrect_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200709, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has decreased your movement speed by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ResurrectPositional_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200710, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has put a see-through effect on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Search_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200711, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has transformed you into a(n) %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ShapeChange_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200712, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] has put a defense shield on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200713, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] blocked %num0 damage through the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_INTERVAL_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200714, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SignetBurst_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200715, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATK_Instant_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200716, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATKDrain_Instant_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200717, skillcaster, num0, skillname);
	}

	/**
	 * You received continuous damage because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATK_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200718, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATK_Instant_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200719, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] has begun draining your HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200720, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has inflicted %num0 damage on you by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_Instant_A_TO_ME(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200721, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] has increased your movement speed by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sprint_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200722, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has weakened [%SkillTarget]'s %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_StatDown_A_TO_ME(String skillcaster, String skilltarget, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200723, skillcaster, skilltarget, value0, skillname);
	}

	/**
	 * [%SkillCaster] has boosted your %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_StatUp_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200724, skillcaster, value0, skillname);
	}

	/**
	 * Your %0 resistance effect was weakened because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SubTypeBoostResist_A_TO_ME(String value0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200725, value0, skillcaster, skillname);
	}

	/**
	 * Your %0 skill duration has changed because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SubTypeExtendDuration_A_TO_ME(String value0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200726, value0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has caused you to summon %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Summon_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200727, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] has caused you to summon %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonBindingGroupGate_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200728, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] has caused you to summon %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonGroupGate_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200729, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] has caused you to summon %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonHoming_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200730, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] has caused you to summon %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonServant_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200731, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] has caused you to summon %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonTotem_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200732, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] has caused you to summon %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonTrap_A_TO_ME(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200733, skillcaster, value0, skillname);
	}

	/**
	 * You exchanged HP with MP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SwitchHPMP_Instant_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200734, skillcaster, skillname);
	}

	/**
	 * Your %0 has changed because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_WeaponStatUp_A_TO_ME(String value0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200735, value0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] became blinded because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Blind_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200736, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] became confused because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Confuse_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200737, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] became diseased because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Disease_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200738, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] was put in the fear state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fear_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200739, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] became paralyzed because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Paralyze_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200740, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] became immobilized because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Root_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200741, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] became silenced because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Silence_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200742, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] fell asleep because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sleep_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200743, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is spinning because it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Spin_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200744, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] was knocked back from shock because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stagger_A_TO_SELF(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200745, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] fell down from shock because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stumble_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200746, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] became stunned because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stun_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200747, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is no longer blind.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Blind_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200748, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer confused.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Confuse_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200749, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer diseased.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Disease_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200750, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer afraid.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fear_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200751, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer paralyzed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Paralyze_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200752, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer immobilized.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Root_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200753, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer silenced.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Silence_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200754, skilltarget);
	}

	/**
	 * [%SkillTarget] woke up.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sleep_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200755, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer spinning.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Spin_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200756, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer staggering.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stagger_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200757, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer shocked.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stumble_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200758, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer stunned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stun_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200759, skilltarget);
	}

	/**
	 * [%SkillCaster]'s loot rate has increased because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostDropRate_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200760, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s visual range has reduced because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OutofSight_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200761, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] exchanged its enmity with the spirit's by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SwitchHostile_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200762, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ReturnHome_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200763, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] started using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Aura_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200764, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] stops using [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Aura_END_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200765, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] used [%SkillName] and became bound.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bind_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200766, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is bleeding because it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200767, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is cursed because it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Curse_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200768, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is unable to fly because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoFly_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200769, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] became snared in mid-air because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OpenAerial_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200770, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] became petrified because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Petrification_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200771, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] became poisoned because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200772, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s attack speed has decreased because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Slow_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200773, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s movement speed has decreased because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Snare_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200774, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is no longer bound
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bind_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200775, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer bleeding.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200776, skilltarget);
	}

	/**
	 * [%SkillTarget] is released from the cursed state.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Curse_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200777, skilltarget);
	}

	/**
	 * [%SkillTarget] is able to fly again.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoFly_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200778, skilltarget);
	}

	/**
	 * [%SkillTarget] is released from the Aerial Snare.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OpenAerial_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200779, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer petrified.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Petrification_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200780, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer poisoned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200781, skilltarget);
	}

	/**
	 * [%SkillTarget] restored its attack speed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Slow_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200782, skilltarget);
	}

	/**
	 * [%SkillTarget] restored its movement speed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Snare_END_A_TO_SELF(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200783, skilltarget);
	}

	/**
	 * [%SkillTarget] is in the boost block state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysBlock_A_TO_SELF(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200784, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the boost evasion state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysDodge_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200785, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the boost accuracy state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysHit_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200786, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the no elemental defense state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysNoResist_A_TO_SELF(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200787, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the boost parry state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysParry_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200788, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the elemental maximum defense state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysResist_A_TO_SELF(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200789, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the boost recovery skill state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostHealEffect_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200790, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s casting speed has changed because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillCastingTime_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200791, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s MP consumption has changed because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillCost_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200792, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s mantra skill MP consumption has decreased because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillToggleCost_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200793, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the boost recovery skill state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSpellAttackEffect_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200794, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] received %num0 damage as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BackDashATK_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200795, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] is in the enmity %0 state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostHate_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200796, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage and the rune carve effect on themselves by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CarveSignet_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200797, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] recovered HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CaseHeal_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200798, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 HP because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CaseHeal_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200799, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] is in the enmity %0 state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ChangeHateOnAttacked_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200800, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] was released from the aerial snare by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CloseAerial_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200801, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] recovered from the transformation by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200802, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200803, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] received %num0 damage as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DashATK_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200804, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] received %num0 damage as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DeathBlow_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200805, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] changed his own recovery amount by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DeboostHealAmount_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200806, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has transformed into %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Deform_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200807, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] will receive the decrease flight time effect in a moment because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedFPATK_Instant_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200808, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s flight time decreased by %num0 as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedFPATK_Instant_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200809, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] will receive damage in a moment because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSpellATK_Instant_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200810, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s flight time decreased by %num0 as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSpellATK_Instant_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200811, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] dispelled its magic effect by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Dispel_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200812, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] dispelled its magical buffs by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelBuff_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200813, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] suffered %num0 damage and dispelled some of its magical buffs by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelBuffCounterATK_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200814, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] dispelled its magical debuffs by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuff_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200815, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] removed its abnormal mental conditions by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuffMental_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200816, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] removed its abnormal physical conditions by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuffPhysical_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200817, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] transferred %num0 DP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPTransfer_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200818, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] boosted his mantra range by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ExtendAuraRange_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200819, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the forced crash state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fall_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200820, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] will receive the decrease flight time effect in a moment because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200821, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s flight time decreased by %num0 as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200822, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster]'s flight time decreased by %num0 as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_Instant_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200823, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] is in the flight time recovery state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200824, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s flight time increased by %num0 as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200825, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster]'s flight time increased by %num0 as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_Instant_HEAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200826, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] is in the continuous healing state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200827, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 HP because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200828, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_Instant_HEAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200829, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] is in the convert damage healing state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnAttacked_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200830, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnAttacked_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200831, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] is in the convert death healing state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnTargetDead_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200832, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnTargetDead_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200833, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] is in the hide state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Hide_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200834, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the enmity %0 state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HostileUp_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200835, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] is in the magical counterattack state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MagicCounterATK_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200836, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] received %num0 damage as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MagicCounterATK_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200837, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] received %num0 damage as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MoveBehindATK_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200838, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster]'s MP was reduced by %num0 as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPAttack_Instant_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200839, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] is in the Mana Treatment state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200840, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 MP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200841, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 MP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_Instant_HEAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200842, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] is in the boost recovery skill state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostHealEffect_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200843, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the boost skill state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostSkillAttack_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200844, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the critical hit state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostSkillCritical_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200845, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the boost skill state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeTypeBoostSkillLevel_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200846, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] caused the spirit to use its skill by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PetOrderUseUltraSkill_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200847, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has transformed into %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Polymorph_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200848, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] received %num0 damage as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcATK_Instant_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200849, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] received %num0 damage as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcATK_Instant_Ratio_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200850, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster]'s flight time increased by %num0 as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCFPHeal_Instant_HEAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200851, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCHeal_Instant_HEAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200852, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 MP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCMPHeal_Instant_HEAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200853, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] is in the protection state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Protect_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200854, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] blocked %num0 damage by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Protect_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200855, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] was affected by its own [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Provoker_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200856, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] received %num0 damage and was put in the pull state because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Pulled_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200857, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] is in the teleport state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_RandomMoveLoc_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200858, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the reserved resurrection state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Rebirth_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200859, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the reflection state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200860, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] reflected %num0 damage by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200861, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] is in the resurrection state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Resurrect_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200862, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] is in the summon-resurrection state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ResurrectPositional_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200863, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the see-through state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Search_A_TO_SELF(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200864, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] has transformed into %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ShapeChange_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200865, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] is in the defense shield state as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200866, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] blocked %num0 damage by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_INTERVAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200867, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] received %num0 damage as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SignetBurst_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200868, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] received %num0 damage as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATK_Instant_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200869, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] received %num0 damage as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATKDrain_Instant_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200870, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] received the continuous damage effect because he used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATK_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200871, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] received %num0 damage as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATK_Instant_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200872, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] absorbed %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200873, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] absorbed %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_Instant_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200874, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillTarget] is in the movement speed increase state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sprint_A_TO_SELF(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200875, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the weaken %0 state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_StatDown_A_TO_SELF(String skilltarget, String value0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200876, skilltarget, value0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the boost %0 state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_StatUp_A_TO_SELF(String skilltarget, String value0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200877, skilltarget, value0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s %0 resistance effects are weakened as it used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SubTypeBoostResist_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200878, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] changed his %0 skill duration by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SubTypeExtendDuration_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200879, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Summon_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200880, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonBindingGroupGate_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200881, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonGroupGate_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200882, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonHoming_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200883, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonServant_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200884, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonTotem_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200885, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonTrap_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200886, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] exchanged his HP and MP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SwitchHPMP_Instant_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200887, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] changed his %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_WeaponStatUp_A_TO_SELF(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200888, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillTarget] became blinded because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Blind_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200889, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] became confused because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Confuse_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200890, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] became diseased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Disease_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200891, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the fear state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fear_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200892, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] became paralyzed because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Paralyze_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200893, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is unable to fly because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Root_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200894, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] became silenced because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Silence_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200895, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] fell asleep because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sleep_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200896, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is spinning because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Spin_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200897, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] was knocked back from shock because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stagger_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200898, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] fell down from shock because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stumble_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200899, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] became stunned because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stun_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200900, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is no longer blind.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Blind_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200901, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer confused.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Confuse_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200902, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer diseased.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Disease_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200903, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer afraid.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fear_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200904, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer paralyzed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Paralyze_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200905, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer immobilized.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Root_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200906, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer silenced.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Silence_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200907, skilltarget);
	}

	/**
	 * [%SkillTarget] woke up.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sleep_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200908, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer spinning.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Spin_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200909, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer staggering.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stagger_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200910, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer shocked.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stumble_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200911, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer stunned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stun_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200912, skilltarget);
	}

	/**
	 * [%SkillTarget]'s loot rate has increased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostDropRate_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200913, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget]'s visual range has decreased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OutofSight_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200914, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] exchanged his enmity toward [%SkillTarget] with his spirit's by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SwitchHostile_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200915, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ReturnHome_A_TO_B(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200916, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] started using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Aura_A_TO_B(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200917, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] stopped using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Aura_END_A_TO_B(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200918, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] became bound because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bind_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200919, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is bleeding because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200920, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is cursed because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Curse_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200921, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is unable to fly because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoFly_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200922, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] became snared in mid-air because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OpenAerial_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200923, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] became petrified because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Petrification_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200924, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] became poisoned because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200925, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget]'s attack speed has decreased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Slow_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200926, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget]'s attack speed has decreased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Snare_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200927, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is no longer bound
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bind_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200928, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer bleeding.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200929, skilltarget);
	}

	/**
	 * [%SkillTarget] recovered from the cursed state.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Curse_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200930, skilltarget);
	}

	/**
	 * [%SkillTarget] is able to fly again.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoFly_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200931, skilltarget);
	}

	/**
	 * [%SkillTarget] is released from the aerial snare.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OpenAerial_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200932, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer petrified.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Petrification_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200933, skilltarget);
	}

	/**
	 * [%SkillTarget] is no longer poisoned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200934, skilltarget);
	}

	/**
	 * [%SkillTarget]'s attack speed is restored to normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Slow_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200935, skilltarget);
	}

	/**
	 * [%SkillTarget]'s movement speed is restored to normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Snare_END_A_TO_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1200936, skilltarget);
	}

	/**
	 * [%SkillTarget] is in the boost block state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysBlock_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200937, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the boost evasion state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysDodge_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200938, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the boost accuracy state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysHit_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200939, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the no elemental defense state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysNoResist_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200940, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the boost parry state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysParry_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200941, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the elemental maximum defense state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_AlwaysResist_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200942, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the boost recovery skill state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostHealEffect_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200943, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] changed [%SkillTarget]'s casting speed by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillCastingTime_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200944, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] changed [%SkillTarget]'s MP consumption by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillCost_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200945, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s mantra skill MP consumption has decreased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSkillToggleCost_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200946, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the boost spell skill state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostSpellAttackEffect_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200947, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BackDashATK_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200948, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] is in the spinning state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_BoostHate_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200949, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] and caused the Rune Carve effect by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CarveSignet_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200950, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] is recovering HP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CaseHeal_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200951, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 HP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CaseHeal_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200952, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the spinning state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ChangeHateOnAttacked_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200953, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] was released from the aerial snare because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CloseAerial_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200954, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the transformation recovery state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200955, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 HP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200956, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DashATK_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200957, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DeathBlow_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200958, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] changed [%SkillTarget]'s recovery amount by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DeboostHealAmount_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200959, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] has transformed into %0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Deform_A_TO_B(String skilltarget, String value0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200960, skilltarget, value0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] will receive the decrease flight time effect in a moment because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedFPATK_Instant_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200961, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time has decreased by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedFPATK_Instant_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster,
		String skillname) {
		return new SM_SYSTEM_MESSAGE(1200962, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] received the Delayed Blast effect because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSpellATK_Instant_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200963, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time has decreased by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSpellATK_Instant_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster,
		String skillname) {
		return new SM_SYSTEM_MESSAGE(1200964, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] dispelled the magic effect from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Dispel_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200965, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] dispelled the magical buffs from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelBuff_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200966, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] and dispelled some of its magical buffs by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelBuffCounterATK_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200967, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] dispelled the magical debuffs from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuff_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200968, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] removed abnormal mental conditions from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuffMental_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200969, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] removed abnormal physical conditions from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelDeBuffPhysical_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200970, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] transferred %num0 DP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPTransfer_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200971, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] boosted [%SkillTarget]'s mantra range by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ExtendAuraRange_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200972, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] was put in the forced crash state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fall_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200973, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] will receive the decrease flight time effect in a moment because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200974, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time has decreased by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200975, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time has decreased by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_Instant_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200976, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the flight time recovery state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200977, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time increased by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200978, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time increased by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_Instant_HEAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200979, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the continuous healing state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200980, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 HP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200981, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 HP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_Instant_HEAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200982, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the convert damage healing state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnAttacked_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200983, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 HP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnAttacked_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster,
		String skillname) {
		return new SM_SYSTEM_MESSAGE(1200984, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the convert death healing state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnTargetDead_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200985, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 HP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnTargetDead_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster,
		String skillname) {
		return new SM_SYSTEM_MESSAGE(1200986, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the spinning state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Hide_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200987, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the spinning state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HostileUp_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200988, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] was put in the magical counterattack state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MagicCounterATK_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200989, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MagicCounterATK_INTERVAL_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200990, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MoveBehindATK_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200991, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s MP was reduced by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPAttack_Instant_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200992, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the Mana Treatment state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200993, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 MP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200994, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 MP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_Instant_HEAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200995, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the boost recovery skill state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostHealEffect_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200996, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the boost skill state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostSkillAttack_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200997, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the critical hit state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeBoostSkillCritical_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200998, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the boost skill state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OneTimeTypeBoostSkillLevel_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1200999, skilltarget, skillcaster, skillname);
	}

	/**
	 * The spirit used a skill on [%SkillTarget] because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PetOrderUseUltraSkill_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201000, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] has transformed into %0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Polymorph_A_TO_B(String skilltarget, String value0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201001, skilltarget, value0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcATK_Instant_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201002, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcATK_Instant_Ratio_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201003, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time increased by %num0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCFPHeal_Instant_HEAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201004, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 HP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCHeal_Instant_HEAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201005, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 MP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PROCMPHeal_Instant_HEAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201006, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the protection state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Protect_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201007, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] blocked %num0 damage because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Protect_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201008, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] was affected because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Provoker_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201009, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] received %num0 damage and was put in the pull state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Pulled_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201010, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the teleport state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_RandomMoveLoc_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201011, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] entered the reserved resurrection state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Rebirth_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201012, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the reflection state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201013, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] reflected %num0 damage because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201014, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the resurrection state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Resurrect_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201015, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the resurrection summoning state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ResurrectPositional_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201016, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the see-through state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Search_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201017, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] has transformed into %0 because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ShapeChange_A_TO_B(String skilltarget, String value0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201018, skilltarget, value0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the defense shield state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201019, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] blocked %num0 damage because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_INTERVAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201020, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SignetBurst_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201021, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATK_Instant_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201022, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATKDrain_Instant_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201023, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] used [%SkillName] to inflict the continuous damage effect on [%SkillTarget].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATK_A_TO_B(String skillcaster, String skillname, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1201024, skillcaster, skillname, skilltarget);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATK_Instant_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201025, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] absorbs [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201026, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_Instant_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201027, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s movement speed increased because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sprint_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201028, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the weaken %0 state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_StatDown_A_TO_B(String skilltarget, String value0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201029, skilltarget, value0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is in the boost %0 state because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_StatUp_A_TO_B(String skilltarget, String value0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201030, skilltarget, value0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget]'s %0 resistance effects were weakened because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SubTypeBoostResist_A_TO_B(String skilltarget, String value0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201031, skilltarget, value0, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget]'s %0 skill durations changed because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SubTypeExtendDuration_A_TO_B(String skilltarget, String value0, String skillcaster,
		String skillname) {
		return new SM_SYSTEM_MESSAGE(1201032, skilltarget, value0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Summon_A_TO_B(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201033, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] summoned %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonBindingGroupGate_A_TO_B(String skillcaster, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201034, skillcaster, value0, skillname);
	}

	/**
	 * [%SkillCaster] has summoned %0 to [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonGroupGate_A_TO_B(String skillcaster, String value0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201035, skillcaster, value0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] has summoned %0 to attack [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonHoming_A_TO_B(String skillcaster, String value0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201036, skillcaster, value0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] has summoned %0 to attack [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonServant_A_TO_B(String skillcaster, String value0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201037, skillcaster, value0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] has summoned %0 to [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonTotem_A_TO_B(String skillcaster, String value0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201038, skillcaster, value0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] has summoned %0 to [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonTrap_A_TO_B(String skillcaster, String value0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201039, skillcaster, value0, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] exchanged [%SkillTarget]'s HP and MP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SwitchHPMP_Instant_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201040, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] changed [%SkillTarget]'s %0 by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_WeaponStatUp_A_TO_B(String skillcaster, String skilltarget, String value0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201041, skillcaster, skilltarget, value0, skillname);
	}

	/**
	 * [%SkillTarget] received %num0 bleeding damage after you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_INTERVAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201042, skilltarget, num0, skillname);
	}

	/**
	 * You received %num0 bleeding damage due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_INTERVAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201043, num0, skillname);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CaseHeal_INTERVAL_HEAL_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201044, num0, skilltarget, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_CaseHeal_INTERVAL_HEAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201045, num0, skillname);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_INTERVAL_HEAL_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201046, num0, skilltarget, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_INTERVAL_HEAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201047, num0, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_INTERVAL_HEAL_MP_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201048, skilltarget, num0, skillname);
	}

	/**
	 * You recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ConvertHeal_INTERVAL_HEAL_MP_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201049, num0, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time decreased by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedFPATK_Instant_INTERVAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201050, skilltarget, num0, skillname);
	}

	/**
	 * Your flight time decreased by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedFPATK_Instant_INTERVAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201051, num0, skillname);
	}

	/**
	 * [%SkillTarget] received %num0 damage due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSpellATK_Instant_INTERVAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201052, skilltarget, num0, skillname);
	}

	/**
	 * You received %num0 damage due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSpellATK_Instant_INTERVAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201053, num0, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time decreased by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_INTERVAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201054, skilltarget, num0, skillname);
	}

	/**
	 * Your flight time decreased by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPATK_INTERVAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201055, num0, skillname);
	}

	/**
	 * [%SkillTarget]'s flight time increased by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_INTERVAL_HEAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201056, skilltarget, num0, skillname);
	}

	/**
	 * Your flight time increased by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_FPHeal_INTERVAL_HEAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201057, num0, skillname);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_INTERVAL_HEAL_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201058, num0, skilltarget, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Heal_INTERVAL_HEAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201059, num0, skillname);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnAttacked_INTERVAL_HEAL_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201060, num0, skilltarget, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnAttacked_INTERVAL_HEAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201061, num0, skillname);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnTargetDead_INTERVAL_HEAL_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201062, num0, skilltarget, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_HealCastorOnTargetDead_INTERVAL_HEAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201063, num0, skillname);
	}

	/**
	 * [%SkillTarget] received %num0 damage due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MagicCounterATK_INTERVAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201064, skilltarget, num0, skillname);
	}

	/**
	 * You receive %num0 damage due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MagicCounterATK_INTERVAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201065, num0, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_INTERVAL_HEAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201066, skilltarget, num0, skillname);
	}

	/**
	 * You recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPHeal_INTERVAL_HEAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201067, num0, skillname);
	}

	/**
	 * [%SkillTarget] received %num0 poisoning damage after you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_INTERVAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201068, skilltarget, num0, skillname);
	}

	/**
	 * You received %num0 poisoning damage due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_INTERVAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201069, num0, skillname);
	}

	/**
	 * [%SkillTarget] blocked %num0 damage through the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Protect_INTERVAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201070, skilltarget, num0, skillname);
	}

	/**
	 * You blocked %num0 damage through the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Protect_INTERVAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201071, num0, skillname);
	}

	/**
	 * [%SkillTarget] reflected %num0 damage.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_INTERVAL_TO_B(String skilltarget, int num0) {
		return new SM_SYSTEM_MESSAGE(1201072, skilltarget, num0);
	}

	/**
	 * You reflected %num0 damage.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_INTERVAL_TO_ME(int num0) {
		return new SM_SYSTEM_MESSAGE(1201073, num0);
	}

	/**
	 * [%SkillTarget] blocked %num0 damage.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_INTERVAL_TO_B(String skilltarget, int num0) {
		return new SM_SYSTEM_MESSAGE(1201074, skilltarget, num0);
	}

	/**
	 * You blocked %num0 damage.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_INTERVAL_TO_ME(int num0) {
		return new SM_SYSTEM_MESSAGE(1201075, num0);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATKDrain_Instant_INTERVAL_HEAL_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201076, num0, skilltarget, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATKDrain_Instant_INTERVAL_HEAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201077, num0, skillname);
	}

	/**
	 * [%SkillTarget] absorbed [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSORBED_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201078, skilltarget, skillname);
	}

	/**
	 * [%SkillName] conflicted with [%SkillTarget]'s existing skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CONFLICT_ME_TO_B(String skillname, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1201079, skillname, skilltarget);
	}

	/**
	 * %0 evaded the attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_DODGED_ME_TO_B(String value0) {
		return new SM_SYSTEM_MESSAGE(1201080, value0);
	}

	/**
	 * [%SkillTarget] resisted [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_RESISTED_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201081, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] is immune to your [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_IMMUNED_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201082, skilltarget, skillname);
	}

	/**
	 * [%SkillName] was cancelled as [%SkillTarget] is under too many effects.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NO_AVAILABLE_SLOT_ME_TO_B(String skillname, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1201083, skillname, skilltarget);
	}

	/**
	 * %0 blocked the attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_BLOCK_ME_TO_B(String value0) {
		return new SM_SYSTEM_MESSAGE(1201084, value0);
	}

	/**
	 * %0 parried the attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARRY_ME_TO_B(String value0) {
		return new SM_SYSTEM_MESSAGE(1201085, value0);
	}

	/**
	 * You absorbed [%SkillCaster]'s [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSORBED_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201086, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s [%SkillName] conflicted with your existing skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CONFLICT_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201087, skillcaster, skillname);
	}

	/**
	 * You evaded %0's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_DODGED_A_TO_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1201088, value0);
	}

	/**
	 * You resisted [%SkillCaster]'s [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_RESISTED_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201089, skillcaster, skillname);
	}

	/**
	 * You are immune to [%SkillCaster]'s [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_IMMUNED_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201090, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s [%SkillName] was cancelled as you are under too many effects.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NO_AVAILABLE_SLOT_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201091, skillcaster, skillname);
	}

	/**
	 * You blocked %0's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_BLOCK_A_TO_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1201092, value0);
	}

	/**
	 * You parried %0's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARRY_A_TO_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1201093, value0);
	}

	/**
	 * [%SkillTarget] was affected by [%SkillCaster]'s [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSORBED_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201094, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s [%SkillName] conflicted with [%SkillTarget]'s existing skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CONFLICT_A_TO_B(String skillcaster, String skillname, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1201095, skillcaster, skillname, skilltarget);
	}

	/**
	 * %0 evaded %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_DODGED_A_TO_B(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1201096, value0, value1);
	}

	/**
	 * [%SkillTarget] resisted [%SkillCaster]'s [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_RESISTED_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201097, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is immune to [%SkillCaster]'s [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_IMMUNED_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201098, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster]'s [%SkillName] was cancelled as [%SkillTarget] is under too many effects.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NO_AVAILABLE_SLOT_A_TO_B(String skillcaster, String skillname, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1201099, skillcaster, skillname, skilltarget);
	}

	/**
	 * %0 blocked %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_BLOCK_A_TO_B(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1201100, value0, value1);
	}

	/**
	 * %0 parried %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARRY_A_TO_B(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1201101, value0, value1);
	}

	/**
	 * [%SkillTarget] evaded [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_DODGED_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201102, skilltarget, skillname);
	}

	/**
	 * You evaded [%SkillCaster]'s [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_DODGED_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201103, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] evaded [%SkillCaster]'s [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_DODGED_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201104, skilltarget, skillcaster, skillname);
	}

	/**
	 * You can see again
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Blind_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201105);
	}

	/**
	 * You are no longer confused.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Confuse_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201106);
	}

	/**
	 * You are no longer diseased.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Disease_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201107);
	}

	/**
	 * You recovered from your fear.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fear_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201108);
	}

	/**
	 * You are no longer paralyzed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Paralyze_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201109);
	}

	/**
	 * You are no longer immobilized.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Root_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201110);
	}

	/**
	 * You are no longer silenced.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Silence_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201111);
	}

	/**
	 * You woke up.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sleep_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201112);
	}

	/**
	 * You have stopped spinning.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Spin_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201113);
	}

	/**
	 * You are no longer staggering.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stagger_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201114);
	}

	/**
	 * You are no longer shocked.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stumble_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201115);
	}

	/**
	 * You are no longer stunned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stun_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201116);
	}

	/**
	 * You are no longer bound.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bind_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201117);
	}

	/**
	 * You are no longer bleeding.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201118);
	}

	/**
	 * You recovered from the cursed state.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Curse_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201119);
	}

	/**
	 * You are able to fly again.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoFly_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201120);
	}

	/**
	 * You are released from the Aerial Snare.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OpenAerial_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201121);
	}

	/**
	 * You are no longer petrified.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Petrification_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201122);
	}

	/**
	 * You are no longer poisoned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201123);
	}

	/**
	 * Your attack speed is restored to normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Slow_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201124);
	}

	/**
	 * Your movement speed is restored to normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Snare_END_ME() {
		return new SM_SYSTEM_MESSAGE(1201125);
	}

	/**
	 * %0 is no longer blinded.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Blind_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201126, value0);
	}

	/**
	 * %0 is no longer confused.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Confuse_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201127, value0);
	}

	/**
	 * %0 is no longer diseased.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Disease_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201128, value0);
	}

	/**
	 * %0 is no longer afraid.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Fear_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201129, value0);
	}

	/**
	 * %0 is no longer paralyzed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Paralyze_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201130, value0);
	}

	/**
	 * %0 is no longer immobilized.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Root_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201131, value0);
	}

	/**
	 * %0 is no longer silenced.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Silence_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201132, value0);
	}

	/**
	 * %0 woke up.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Sleep_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201133, value0);
	}

	/**
	 * %0 is no longer spinning.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Spin_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201134, value0);
	}

	/**
	 * %0 is no longer staggering.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stagger_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201135, value0);
	}

	/**
	 * %0 is no longer shocked.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stumble_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201136, value0);
	}

	/**
	 * %0 is no longer stunned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Stun_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201137, value0);
	}

	/**
	 * %0 is no longer bound.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bind_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201138, value0);
	}

	/**
	 * %0 is no longer bleeding.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Bleed_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201139, value0);
	}

	/**
	 * %0 recovered from the cursed state.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Curse_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201140, value0);
	}

	/**
	 * %0 is able to fly again.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoFly_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201141, value0);
	}

	/**
	 * %0 is released from the Aerial Snare.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_OpenAerial_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201142, value0);
	}

	/**
	 * %0 recovered from the petrified state.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Petrification_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201143, value0);
	}

	/**
	 * %0 is no longer poisoned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Poison_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201144, value0);
	}

	/**
	 * %0's attack speed is restored to normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Slow_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201145, value0);
	}

	/**
	 * %0's movement speed is restored to normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Snare_END_A(String value0) {
		return new SM_SYSTEM_MESSAGE(1201146, value0);
	}

	/**
	 * You use [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonSkillArea_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1201147, skillname);
	}

	/**
	 * [%SkillCaster] uses [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonSkillArea_ME_TO_B(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201148, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] uses [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonSkillArea_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201149, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] uses [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonSkillArea_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201150, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] uses [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SummonSkillArea_A_TO_B(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201151, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] received %num0 damage due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATK_INTERVAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201152, skilltarget, num0, skillname);
	}

	/**
	 * You receive %num0 damage due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATK_INTERVAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201153, num0, skillname);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_Instant_INTERVAL_HEAL_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201154, num0, skilltarget, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_Instant_INTERVAL_HEAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201155, num0, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_Instant_INTERVAL_HEAL_MP_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201156, skilltarget, num0, skillname);
	}

	/**
	 * You recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_Instant_INTERVAL_HEAL_MP_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201157, num0, skillname);
	}

	/**
	 * [%SkillTarget] received %num0 damage due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_INTERVAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201158, skilltarget, num0, skillname);
	}

	/**
	 * You receive %num0 damage due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_INTERVAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201159, num0, skillname);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_INTERVAL_HEAL_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201160, num0, skilltarget, skillname);
	}

	/**
	 * You recovered %num0 HP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_INTERVAL_HEAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201161, num0, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_INTERVAL_HEAL_MP_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201162, skilltarget, num0, skillname);
	}

	/**
	 * You recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SpellATKDrain_INTERVAL_HEAL_MP_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201163, num0, skillname);
	}

	/**
	 * You received %num0 damage as the [%SkillName] you used on [%SkillTarget] was reflected back at you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_PROTECT_SKILL_ME_to_B(int num0, String skillname, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1201164, num0, skillname, skilltarget);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by reflecting [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_PROTECT_SKILL_A_to_ME(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201165, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] inflicted %num0 damage on [%SkillCaster] by reflecting [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_PROTECT_SKILL_A_to_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201166, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * Your attack on [%SkillTarget] was reflected and inflicted %num0 damage on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_PROTECT_ME_to_B(String skilltarget, int num0) {
		return new SM_SYSTEM_MESSAGE(1201167, skilltarget, num0);
	}

	/**
	 * [%SkillTarget] inflicted %num0 damage on [%SkillCaster] by reflecting the attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_PROTECT_A_to_ME(String skilltarget, int num0, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1201168, skilltarget, num0, skillcaster);
	}

	/**
	 * [%SkillTarget] inflicted %num0 damage on [%SkillCaster] by reflecting the attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Reflector_PROTECT_A_to_B(String skilltarget, int num0, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1201169, skilltarget, num0, skillcaster);
	}

	/**
	 * [%SkillName] was blocked by the protective shield effect cast on [%SkillTarget].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_PROTECT_SKILL_ME_to_B(String skillname, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1201170, skillname, skilltarget);
	}

	/**
	 * You blocked the [%SkillName] used by [%SkillCaster] with the protective shield effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_PROTECT_SKILL_A_to_ME(String skillname, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1201171, skillname, skillcaster);
	}

	/**
	 * [%SkillTarget] blocked the [%SkillName] used by [%SkillCaster] with the protective shield effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_PROTECT_SKILL_A_to_B(String skilltarget, String skillname, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1201172, skilltarget, skillname, skillcaster);
	}

	/**
	 * The attack was blocked by the protective shield effect cast on [%SkillTarget].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_PROTECT_ME_to_B(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1201173, skilltarget);
	}

	/**
	 * You blocked [%SkillCaster]'s attack with the protective shield effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_PROTECT_A_to_ME(String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1201174, skillcaster);
	}

	/**
	 * [%SkillTarget] blocked [%SkillCaster]'s attack with the protective shield effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Shield_PROTECT_A_to_B(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1201175, skilltarget, skillcaster);
	}

	/**
	 * You received the %num0 damage inflicted on [%SkillTarget] by [%SkillCaster]'s [%SkillName], because of the protection effect you cast on it.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_protect_PROTECT_SKILL_A_to_B(int num0, String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201176, num0, skilltarget, skillcaster, skillname);
	}

	/**
	 * You received the %num0 damage inflicted on [%SkillTarget] by [%SkillCaster], because of the protection effect you cast on it.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_protect_PROTECT_A_to_B(int num0, String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1201177, num0, skilltarget, skillcaster);
	}

	/**
	 * [%Protector] received the %num0 damage inflicted on [%SkillTarget] by a [%SkillName], because of the protection effect cast on it.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_protect_PROTECT_SKILL_HEAL_ME_to_B(String protector, int num0, String skilltarget,
		String skillname) {
		return new SM_SYSTEM_MESSAGE(1201178, protector, num0, skilltarget, skillname);
	}

	/**
	 * [%Protector] received the %num0 damage inflicted by [%SkillCaster] 's [%SkillName], because of the protection effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_protect_PROTECT_SKILL_HEAL_A_to_ME(String protector, int num0, String skillcaster,
		String skillname) {
		return new SM_SYSTEM_MESSAGE(1201179, protector, num0, skillcaster, skillname);
	}

	/**
	 * [%Protector] received the %num0 damage inflicted on [%SkillTarget] by [%SkillCaster]'s [%SkillName], because of the protection effect cast on it.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_protect_PROTECT_SKILL_HEAL_A_to_B(String protector, int num0, String skilltarget, String skillcaster,
		String skillname) {
		return new SM_SYSTEM_MESSAGE(1201180, protector, num0, skilltarget, skillcaster, skillname);
	}

	/**
	 * [%Protector] received %num0 damage inflicted on [%SkillTarget], because of the protection effect cast on it.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_protect_PROTECT_HEAL_ME_to_B(String protector, int num0, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1201181, protector, num0, skilltarget);
	}

	/**
	 * [%Protector] received %num0 damage inflicted on you by [%SkillCaster], because of the protection effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_protect_PROTECT_HEAL_A_to_ME(String protector, int num0, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1201182, protector, num0, skillcaster);
	}

	/**
	 * [%Protector] received %num0 damage inflicted on [%SkillTarget] by [%SkillCaster], because of the protection effect cast on it.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_protect_PROTECT_HEAL_A_to_B(String protector, int num0, String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1201183, protector, num0, skilltarget, skillcaster);
	}

	/**
	 * You recovered %num0 MP.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MPHeal_TO_ME(int num0) {
		return new SM_SYSTEM_MESSAGE(1201196, num0);
	}

	/**
	 * You restored your flight time by %num0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FPHeal_TO_ME(int num0) {
		return new SM_SYSTEM_MESSAGE(1201197, num0);
	}

	/**
	 * You recovered %num0 HP.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Heal_TO_ME(int num0) {
		return new SM_SYSTEM_MESSAGE(1201198, num0);
	}

	/**
	 * %0 restored %num1 MP.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MPHeal_TO_OTHER(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1201199, value0, num1);
	}

	/**
	 * %0 restored his flight time by %num1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FPHeal_TO_OTHER(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1201200, value0, num1);
	}

	/**
	 * [%SkillTarget] received %num0 damage due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcATK_Instant_INTERVAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201201, skilltarget, num0, skillname);
	}

	/**
	 * You receive %num0 damage due to [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcATK_Instant_INTERVAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201202, num0, skillname);
	}

	/**
	 * %0 restored %num1 HP.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Heal_TO_OTHER(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1201203, value0, num1);
	}

	/**
	 * You released [%SkillTarget]'s spirit by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PetOrderUnSummon_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201204, skilltarget, skillname);
	}

	/**
	 * Your spirit was unsummoned by the effect of [%SkillName] used by [%SkillCaster].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PetOrderUnSummon_A_TO_ME(String skillname, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1201205, skillname, skillcaster);
	}

	/**
	 * [%SkillCaster] released [%SkillTarget]'s spirit by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_PetOrderUnSummon_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201206, skillcaster, skilltarget, skillname);
	}

	/**
	 * You inflicted continuous damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPAttack_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201207, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] has reduced your MP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPAttack_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201208, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] gave [%SkillTarget] the continuous MP reduction effect by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPAttack_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201209, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget]'s MP decreased by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPAttack_INTERVAL_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201210, skilltarget, num0, skillname);
	}

	/**
	 * Your MP decreased by %num0 due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_MPAttack_INTERVAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201211, num0, skillname);
	}

	/**
	 * You gave yourself an XP bonus by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_XPBoost_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1201212, skillname);
	}

	/**
	 * You gave [%SkillTarget] an XP bonus by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_XPBoost_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201213, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] gave you an XP bonus by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_XPBoost_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201214, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] gave themselves an XP bonus by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_XPBoost_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201215, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] gave [%SkillTarget] an XP bonus by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_XPBoost_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201216, skillcaster, skilltarget, skillname);
	}

	/**
	 * You made yourself more resistant to crashing and prohibitions on flying by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_InvulnerableWing_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1201217, skillname);
	}

	/**
	 * You gave [%SkillTarget] the crash and flying prohibition resistance effects by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_InvulnerableWing_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201218, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] made you more resistant to crashing and prohibitions on flying by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_InvulnerableWing_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201219, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] made themselves more resistant to crashing and prohibitions on flying by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_InvulnerableWing_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201220, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] made [%SkillTarget] more resistant to crashing and prohibitions on flying by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_InvulnerableWing_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201221, skillcaster, skilltarget, skillname);
	}

	/**
	 * You recovered %num0 DP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPHeal_Instant_HEAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201222, num0, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 DP because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPHeal_Instant_HEAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201223, skilltarget, num0, skillname);
	}

	/**
	 * You recovered %num0 DP because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPHeal_Instant_HEAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201224, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 DP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPHeal_Instant_HEAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201225, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 DP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPHeal_Instant_HEAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201226, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * You recovered some DP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPHeal_HEAL_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1201227, skillname);
	}

	/**
	 * You restored some of [%SkillTarget]'s DP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPHeal_HEAL_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201228, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] has boosted [%SkillTarget]'s DP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPHeal_HEAL_A_TO_ME(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201229, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] restored some DP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPHeal_HEAL_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201230, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] restored some of [%SkillTarget]'s DP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPHeal_HEAL_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201231, skillcaster, skilltarget, skillname);
	}

	/**
	 * You restored %num0 of [%SkillTarget]'s DP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPHeal_INTERVAL_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201232, num0, skilltarget, skillname);
	}

	/**
	 * You recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DPHeal_INTERVAL_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201233, num0, skillname);
	}

	/**
	 * You recovered %num0 DP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcDPHeal_Instant_HEAL_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201234, num0, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 DP because you used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcDPHeal_Instant_HEAL_ME_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201235, skilltarget, num0, skillname);
	}

	/**
	 * You recovered %num0 DP because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcDPHeal_Instant_HEAL_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201236, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] recovered %num0 DP by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcDPHeal_Instant_HEAL_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201237, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 DP because [%SkillCaster] used [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ProcDPHeal_Instant_HEAL_A_TO_B(String skilltarget, int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201238, skilltarget, num0, skillcaster, skillname);
	}

	/**
	 * You caused [%SkillTarget] to forcibly resurrect at the bind point by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ResurrectBase_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201239, skilltarget, skillname);
	}

	/**
	 * You are forced to resurrect at the bind point because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ResurrectBase_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201240, skillcaster, skillname);
	}

	/**
	 * [%SkillTarget] is forced to resurrect at the bind point because [%SkillCaster] used [%SkillName] on it.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_ResurrectBase_A_TO_B(String skilltarget, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201241, skilltarget, skillcaster, skillname);
	}

	/**
	 * You requested [%SkillTarget] to be summoned by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Recall_Instant_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201242, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] requested you to be summoned by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Recall_Instant_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201243, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] requested [%SkillTarget] to be summoned by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_Recall_Instant_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201244, skillcaster, skilltarget, skillname);
	}

	/**
	 * You dispelled magical buffs by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCBuff_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1201245, skillname);
	}

	/**
	 * You dispelled magical debuffs by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuff_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1201246, skillname);
	}

	/**
	 * You removed abnormal mental conditions by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuffMental_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1201247, skillname);
	}

	/**
	 * You removed abnormal physical conditions by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuffPhysical_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1201248, skillname);
	}

	/**
	 * You dispelled magical buffs from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCBuff_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201249, skilltarget, skillname);
	}

	/**
	 * You dispelled magical debuffs from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuff_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201250, skilltarget, skillname);
	}

	/**
	 * You removed abnormal mental conditions from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuffMental_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201251, skilltarget, skillname);
	}

	/**
	 * You removed abnormal physical conditions from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuffPhysical_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201252, skilltarget, skillname);
	}

	/**
	 * Your magical buffs were dispelled because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCBuff_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201253, skillcaster, skillname);
	}

	/**
	 * Your magical debuffs were dispelled because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuff_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201254, skillcaster, skillname);
	}

	/**
	 * Your abnormal mental conditions were removed because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuffMental_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201255, skillcaster, skillname);
	}

	/**
	 * Your abnormal physical conditions were removed because [%SkillCaster] used [%SkillName] on you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuffPhysical_A_TO_ME(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201256, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] dispelled its magical buffs by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCBuff_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201257, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] dispelled its magical debuffs by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuff_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201258, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] removed its abnormal mental conditions by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuffMental_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201259, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] removed its abnormal physical conditions by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuffPhysical_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201260, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] dispelled the magical buffs from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCBuff_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201261, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] dispelled the magical debuffs from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuff_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201262, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] removed abnormal mental conditions from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuffMental_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201263, skillcaster, skilltarget, skillname);
	}

	/**
	 * [%SkillCaster] removed abnormal physical conditions from [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DispelNPCDeBuffPhysical_A_TO_B(String skillcaster, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201264, skillcaster, skilltarget, skillname);
	}

	/**
	 * You received a delayed chain effect by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSkill_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1201265, skillname);
	}

	/**
	 * [%SkillTarget] received a delayed chain effect from [%SkillName] used by you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSkill_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201266, skilltarget, skillname);
	}

	/**
	 * You received a delayed chain effect from [%SkillName] used by [%SkillCaster].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSkill_A_TO_ME(String skillname, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1201267, skillname, skillcaster);
	}

	/**
	 * [%SkillCaster] used [%SkillName] and received a delayed chain effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSkill_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201268, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] used [%SkillName] to give [%SkillTarget] a delayed chain effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_DelayedSkill_A_TO_B(String skillcaster, String skillname, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1201269, skillcaster, skillname, skilltarget);
	}

	/**
	 * You received a periodic chain effect by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_InteralSkill_ME_TO_SELF(String skillname) {
		return new SM_SYSTEM_MESSAGE(1201270, skillname);
	}

	/**
	 * [%SkillTarget] received a periodic chain effect from [%SkillName] used by you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_InteralSkill_ME_TO_B(String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201271, skilltarget, skillname);
	}

	/**
	 * You received a periodic chain effect from [%SkillName] used by [%SkillCaster].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_InteralSkill_A_TO_ME(String skillname, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1201272, skillname, skillcaster);
	}

	/**
	 * [%SkillCaster] used [%SkillName] and received a periodic chain effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_InteralSkill_A_TO_SELF(String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201273, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] used [%SkillName] to give [%SkillTarget] a periodic chain effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_InteralSkill_A_TO_B(String skillcaster, String skillname, String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1201274, skillcaster, skillname, skilltarget);
	}

	/**
	 * You suffer %num0 damage from [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoReduceSpellATK_Instant_ME_TO_SELF(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201275, num0, skillname);
	}

	/**
	 * You inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoReduceSpellATK_Instant_ME_TO_B(int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201276, num0, skilltarget, skillname);
	}

	/**
	 * You receive %num0 damage from [%SkillCaster]'s [%SkillName] effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoReduceSpellATK_Instant_A_TO_ME(int num0, String skillcaster, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201277, num0, skillcaster, skillname);
	}

	/**
	 * [%SkillCaster] suffers %num0 damage from [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoReduceSpellATK_Instant_A_TO_SELF(String skillcaster, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201278, skillcaster, num0, skillname);
	}

	/**
	 * [%SkillCaster] inflicted %num0 damage on [%SkillTarget] by using [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_NoReduceSpellATK_Instant_A_TO_B(String skillcaster, int num0, String skilltarget, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201279, skillcaster, num0, skilltarget, skillname);
	}

	/**
	 * [%SkillTarget] recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATKDrain_Instant_INTERVAL_HEAL_MP_TO_B(String skilltarget, int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201280, skilltarget, num0, skillname);
	}

	/**
	 * You recovered %num0 MP due to the effect of [%SkillName].
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUCC_SkillATKDrain_Instant_INTERVAL_HEAL_MP_TO_ME(int num0, String skillname) {
		return new SM_SYSTEM_MESSAGE(1201281, num0, skillname);
	}

	/**
	 * You blocked %0's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_BLOCK(String value0) {
		return new SM_SYSTEM_MESSAGE(1210000, value0);
	}

	/**
	 * You parried %0's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_PARRY(String value0) {
		return new SM_SYSTEM_MESSAGE(1210001, value0);
	}

	/**
	 * You evaded %0's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_DODGE(String value0) {
		return new SM_SYSTEM_MESSAGE(1210002, value0);
	}

	/**
	 * You resisted [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_RESISTED_MAGIC_MY(String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1210003, skillcaster);
	}

	/**
	 * You are immune to [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_IMMUNED_MAGIC_MY(String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1210004, skillcaster);
	}

	/**
	 * You absorbed [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSORBED_MY(String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1210005, skillcaster);
	}

	/**
	 * You received %num1 damage from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_ENEMY_ATTACK(int num1, String value0) {
		return new SM_SYSTEM_MESSAGE(1210006, num1, value0);
	}

	/**
	 * Critical Hit! You received %num1 damage from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_ENEMY_CRITICAL(int num1, String value0) {
		return new SM_SYSTEM_MESSAGE(1210007, num1, value0);
	}

	/**
	 * %0 blocked your attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_ENEMY_BLOCK(String value0) {
		return new SM_SYSTEM_MESSAGE(1210224, value0);
	}

	/**
	 * %0 parried your attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_ENEMY_PARRY(String value0) {
		return new SM_SYSTEM_MESSAGE(1210225, value0);
	}

	/**
	 * %0 evaded your attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_ENEMY_DODGE(String value0) {
		return new SM_SYSTEM_MESSAGE(1210226, value0);
	}

	/**
	 * [%SkillTarget] resisted your magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_RESISTED_MAGIC_TARGET(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1210227, skilltarget);
	}

	/**
	 * [%SkillTarget] is immune to your magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_IMMUNED_MAGIC_TARGET(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1210228, skilltarget);
	}

	/**
	 * [%SkillTarget] absorbed your magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSORBED_TARGET(String skilltarget) {
		return new SM_SYSTEM_MESSAGE(1210229, skilltarget);
	}

	/**
	 * %0 inflicted %num2 damage on %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARTY_ATTACK(String value0, int num2, String value1) {
		return new SM_SYSTEM_MESSAGE(1220000, value0, num2, value1);
	}

	/**
	 * Critical Hit! %0 inflicted %num2 critical damage on %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARTY_CRITICAL(String value0, int num2, String value1) {
		return new SM_SYSTEM_MESSAGE(1220001, value0, num2, value1);
	}

	/**
	 * %0 blocked %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARTY_BLOCK(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1220219, value0, value1);
	}

	/**
	 * %0 parried %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARTY_PARRY(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1220220, value0, value1);
	}

	/**
	 * %0 evaded %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARTY_DODGE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1220221, value0, value1);
	}

	/**
	 * [%SkillTarget] resisted [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_RESISTED_TO_MAGIC_PARTY(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1220222, skilltarget, skillcaster);
	}

	/**
	 * [%SkillTarget] is immune to [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_IMMUNED_MAGIC_PARTY(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1220223, skilltarget, skillcaster);
	}

	/**
	 * [%SkillTarget] absorbed [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSORBED_PARTY(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1220224, skilltarget, skillcaster);
	}

	/**
	 * %1 received %num2 damage from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARTY_ENEMY_ATTACK(String value1, int num2, String value0) {
		return new SM_SYSTEM_MESSAGE(1230000, value1, num2, value0);
	}

	/**
	 * Critical Hit! %1 received %num2 critical damage from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARTY_ENEMY_CRITICAL(String value1, int num2, String value0) {
		return new SM_SYSTEM_MESSAGE(1230001, value1, num2, value0);
	}

	/**
	 * %0 blocked %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARTY_ENEMY_BLOCK(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1230218, value0, value1);
	}

	/**
	 * %0 parried %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARTY_ENEMY_PARRY(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1230219, value0, value1);
	}

	/**
	 * %0 evaded %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_PARTY_ENEMY_DODGE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1230220, value0, value1);
	}

	/**
	 * [%SkillTarget] resisted [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_RESISTED_MAGIC_PARTY_ENEMY(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1230221, skilltarget, skillcaster);
	}

	/**
	 * [%SkillTarget] is immune to [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_IMMUNED_MAGIC_PARTY_ENEMY(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1230222, skilltarget, skillcaster);
	}

	/**
	 * [%SkillTarget] absorbed [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSORBED_PARTY_ENEMY(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1230223, skilltarget, skillcaster);
	}

	/**
	 * %0 inflicted %num2 damage on %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_FRIENDLY_ATTACK(String value0, int num2, String value1) {
		return new SM_SYSTEM_MESSAGE(1240000, value0, num2, value1);
	}

	/**
	 * Critical Hit! %0 inflicted %num2 critical damage on %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_FRIENDLY_CRITICAL(String value0, int num2, String value1) {
		return new SM_SYSTEM_MESSAGE(1240001, value0, num2, value1);
	}

	/**
	 * %0 blocked %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_FRIENDLY_BLOCK(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1240217, value0, value1);
	}

	/**
	 * %0 parried %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_FRIENDLY_PARRY(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1240218, value0, value1);
	}

	/**
	 * %0 evaded %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_FRIENDLY_DODGE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1240219, value0, value1);
	}

	/**
	 * [%SkillTarget] resisted [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_RESISTED_MAGIC_OTHER_FRIENDLY(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1240220, skilltarget, skillcaster);
	}

	/**
	 * [%SkillTarget] is immune to [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_IMMUNED_MAGIC_OTHER_FRIENDLY(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1240221, skilltarget, skillcaster);
	}

	/**
	 * [%SkillTarget] absorbed [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSORBED_OTHER_FRIENDLY(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1240222, skilltarget, skillcaster);
	}

	/**
	 * %0 inflicted %num2 damage on %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_HOSTILE_ATTACK(String value0, int num2, String value1) {
		return new SM_SYSTEM_MESSAGE(1250000, value0, num2, value1);
	}

	/**
	 * Critical Hit! %0 inflicted %num2 critical damage on %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_HOSTILE_CRITICAL(String value0, int num2, String value1) {
		return new SM_SYSTEM_MESSAGE(1250001, value0, num2, value1);
	}

	/**
	 * %0 blocked %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_HOSTILE_BLOCK(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1250217, value0, value1);
	}

	/**
	 * %0 parried %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_HOSTILE_PARRY(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1250218, value0, value1);
	}

	/**
	 * %0 evaded %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_HOSTILE_DODGE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1250219, value0, value1);
	}

	/**
	 * [%SkillTarget] resisted [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_RESISTED_MAGIC_OTHER_HOSTILE(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1250220, skilltarget, skillcaster);
	}

	/**
	 * [%SkillTarget] is immune to [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_IMMUNED_MAGIC_OTHER_HOSTILE(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1250221, skilltarget, skillcaster);
	}

	/**
	 * [%SkillTarget] absorbed [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSORBED_OTHER_HOSTILE(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1250222, skilltarget, skillcaster);
	}

	/**
	 * %0 inflicted %num2 damage on %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_NPC_ATTACK(String value0, int num2, String value1) {
		return new SM_SYSTEM_MESSAGE(1260000, value0, num2, value1);
	}

	/**
	 * Critical Hit! %0 inflicted %num2 critical damage on %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_NPC_CRITICAL(String value0, int num2, String value1) {
		return new SM_SYSTEM_MESSAGE(1260001, value0, num2, value1);
	}

	/**
	 * %0 blocked %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_NPC_BLOCK(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1260217, value0, value1);
	}

	/**
	 * %0 parried %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_NPC_PARRY(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1260218, value0, value1);
	}

	/**
	 * %0 evaded %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_OTHER_NPC_DODGE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1260219, value0, value1);
	}

	/**
	 * [%SkillTarget] resisted [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_RESISTED_MAGIC_OTHER_NPC(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1260220, skilltarget, skillcaster);
	}

	/**
	 * [%SkillTarget] is immune to [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_IMMUNED_MAGIC_OTHER_NPC(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1260221, skilltarget, skillcaster);
	}

	/**
	 * [%SkillTarget] absorbed [%SkillCaster]'s magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSORBED_OTHER_NPC(String skilltarget, String skillcaster) {
		return new SM_SYSTEM_MESSAGE(1260222, skilltarget, skillcaster);
	}

	/**
	 * The weapon has been changed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CHANGE_WEAPON() {
		return new SM_SYSTEM_MESSAGE(1300000);
	}

	/**
	 * You can use it after registering it on the Quickbar.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NEED_TO_REGIST_SHORTCUT() {
		return new SM_SYSTEM_MESSAGE(1300001);
	}

	/**
	 * You do not have much flight time left. Please land on a secure place.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WARNING_FLY() {
		return new SM_SYSTEM_MESSAGE(1300002);
	}

	/**
	 * Warning! You do not have much flight time left.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WARNING_FLY_Notice() {
		return new SM_SYSTEM_MESSAGE(1300003);
	}

	/**
	 * You suffered damage as you have submerged deep in the water. Please get out of the water.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WARNING_Swim() {
		return new SM_SYSTEM_MESSAGE(1300004);
	}

	/**
	 * Warning! You suffered damage as you have submerged deep in the water.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WARNING_Swim_Notice() {
		return new SM_SYSTEM_MESSAGE(1300005);
	}

	/**
	 * No target has been selected.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NO_TARGET() {
		return new SM_SYSTEM_MESSAGE(1300006);
	}

	/**
	 * Invalid target. You can only use this on objects.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_INVALID_TARGET_OBJECT_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300007);
	}

	/**
	 * Invalid target. You can only use this on NPCs.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_INVALID_TARGET_NPC_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300008);
	}

	/**
	 * Invalid target. You can only use this only on other players.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_INVALID_TARGET_PC_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300009);
	}

	/**
	 * Invalid target. You can only use this on spirits.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_INVALID_TARGET_PET_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300010);
	}

	/**
	 * Invalid target. You can only use this on group members.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_INVALID_TARGET_PARTY_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300011);
	}

	/**
	 * You can only use this on living targets.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_TARGET_IS_NOT_ALIVE() {
		return new SM_SYSTEM_MESSAGE(1300012);
	}

	/**
	 * Invalid target.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_TARGET_IS_NOT_VALID() {
		return new SM_SYSTEM_MESSAGE(1300013);
	}

	/**
	 * You do not have enough health to use that skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NOT_ENOUGH_HP() {
		return new SM_SYSTEM_MESSAGE(1300014);
	}

	/**
	 * You do not have enough mana to use that skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NOT_ENOUGH_MP() {
		return new SM_SYSTEM_MESSAGE(1300015);
	}

	/**
	 * You do not have enough DP to use that skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NOT_ENOUGH_DP() {
		return new SM_SYSTEM_MESSAGE(1300016);
	}

	/**
	 * You cannot learn the design because your skill level is not high enough.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NOT_ENOUGH_DP_LEVEL() {
		return new SM_SYSTEM_MESSAGE(1300017);
	}

	/**
	 * You do not have enough %0 necessary to use the skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NOT_ENOUGH_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300018, value0);
	}

	/**
	 * You need to equip another weapon to use that skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NO_WEAPON() {
		return new SM_SYSTEM_MESSAGE(1300019);
	}

	/**
	 * You have not learned the %0 skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NOT_LEARNED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300020, value0);
	}

	/**
	 * You are not ready to use that skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NOT_READY() {
		return new SM_SYSTEM_MESSAGE(1300021);
	}

	/**
	 * You are too far from the target to use that skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_TOO_FAR() {
		return new SM_SYSTEM_MESSAGE(1300022);
	}

	/**
	 * The skill was cancelled.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANCELED() {
		return new SM_SYSTEM_MESSAGE(1300023);
	}

	/**
	 * You have failed to use the skill because the target disappeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_TARGET_LOST() {
		return new SM_SYSTEM_MESSAGE(1300024);
	}

	/**
	 * You are using too many skills simultaneously.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_TOO_MANY_COOLING() {
		return new SM_SYSTEM_MESSAGE(1300025);
	}

	/**
	 * You cannot do that while you are %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_CAST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300026, value0);
	}

	/**
	 * You can use the skill only during combat.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_CAST_NOT_IN_COMBAT_MODE() {
		return new SM_SYSTEM_MESSAGE(1300027);
	}

	/**
	 * That skill does not exist.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_CAST_NO_SUCH_SKILL() {
		return new SM_SYSTEM_MESSAGE(1300028);
	}

	/**
	 * The skill has failed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_FAILED() {
		return new SM_SYSTEM_MESSAGE(1300029);
	}

	/**
	 * You cannot use that because there is an obstacle in the way.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_OBSTACLE() {
		return new SM_SYSTEM_MESSAGE(1300030);
	}

	/**
	 * You do not have a proper target for that skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_FIND_VALID_TARGET() {
		return new SM_SYSTEM_MESSAGE(1300031);
	}

	/**
	 * The target is too far away.
	 */
	public static SM_SYSTEM_MESSAGE STR_ATTACK_TOO_FAR_FROM_TARGET() {
		return new SM_SYSTEM_MESSAGE(1300032);
	}

	/**
	 * You cannot attack as there is an obstacle in the way.
	 */
	public static SM_SYSTEM_MESSAGE STR_ATTACK_OBSTACLE_EXIST() {
		return new SM_SYSTEM_MESSAGE(1300033);
	}

	/**
	 * You cannot attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_ATTACK_CANT_FINT_VALID_TARGET() {
		return new SM_SYSTEM_MESSAGE(1300034);
	}

	/**
	 * You acquired the %0 title as a quest reward.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_GET_REWARD_TITLE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300035, value0);
	}

	/**
	 * A survey has arrived. Click the icon to open the survey window.
	 */
	public static SM_SYSTEM_MESSAGE STR_GMPOLL_GOT_POLL() {
		return new SM_SYSTEM_MESSAGE(1300036);
	}

	/**
	 * There is no remaining survey to take part in.
	 */
	public static SM_SYSTEM_MESSAGE STR_GMPOLL_NO_POLL_REMAINED() {
		return new SM_SYSTEM_MESSAGE(1300037);
	}

	/**
	 * %0 is running away.
	 */
	public static SM_SYSTEM_MESSAGE STR_UI_COMBAT_NPC_FLEE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300038, value0);
	}

	/**
	 * %0 gives up the pursuit.
	 */
	public static SM_SYSTEM_MESSAGE STR_UI_COMBAT_NPC_RETURN(String value0) {
		return new SM_SYSTEM_MESSAGE(1300039, value0);
	}

	/**
	 * You have discovered [%subzone].
	 */
	public static SM_SYSTEM_MESSAGE STR_UI_DISCOVERY_NEWZONE() {
		return new SM_SYSTEM_MESSAGE(1300040);
	}

	/**
	 * You cannot remove the equipped item because the inventory is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_UI_INVENTORY_FULL() {
		return new SM_SYSTEM_MESSAGE(1300042);
	}

	/**
	 * You left the group.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_SECEDE() {
		return new SM_SYSTEM_MESSAGE(1300043);
	}

	/**
	 * A dead person cannot be invited to a group.
	 */
	public static SM_SYSTEM_MESSAGE STR_UI_PARTY_DEAD() {
		return new SM_SYSTEM_MESSAGE(1300044);
	}

	/**
	 * You cannot check the information on characters of another race.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ASK_PCINFO_OTHER_RACE() {
		return new SM_SYSTEM_MESSAGE(1300045);
	}

	/**
	 * That person is not logged on.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ASK_PCINFO_LOGOFF() {
		return new SM_SYSTEM_MESSAGE(1300046);
	}

	/**
	 * You cannot leave the group in %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_LEAVE_PARTY_DURING_PATH_FLYING(String value0) {
		return new SM_SYSTEM_MESSAGE(1300047, value0);
	}

	/**
	 * You cannot use an item while %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_USE_ITEM_DURING_PATH_FLYING(String value0) {
		return new SM_SYSTEM_MESSAGE(1300048, value0);
	}

	/**
	 * %0 gives up the attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_UI_COMBAT_NPC_RETURN_NOMOVE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300049, value0);
	}

	/**
	 * You learned %0 (Level %1).
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_LEARNED_NEW_SKILL(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300050, value0, value1);
	}

	/**
	 * You stopped using %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_TOGGLE_SKILL_TURNED_OFF(String value0) {
		return new SM_SYSTEM_MESSAGE(1300051, value0);
	}

	/**
	 * That skill is not being used.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_TOGGLE_SKILL_ALREADY_TURNED_OFF() {
		return new SM_SYSTEM_MESSAGE(1300052);
	}

	/**
	 * You stopped using %0 skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_MAINTAIN_SKILL_TURNED_OFF(String value0) {
		return new SM_SYSTEM_MESSAGE(1300053, value0);
	}

	/**
	 * That skill is not being used.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_MAINTAIN_SKILL_ALREADY_TURNED_OFF() {
		return new SM_SYSTEM_MESSAGE(1300054);
	}

	/**
	 * The %0 skill effect has been removed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSTATUS_SKILL_TURNED_OFF(String value0) {
		return new SM_SYSTEM_MESSAGE(1300055, value0);
	}

	/**
	 * The %0 skill effect cannot be removed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSTATUS_SKILL_CAN_NOT_BE_TURNED_OFF_BY_TARGET(String value0) {
		return new SM_SYSTEM_MESSAGE(1300056, value0);
	}

	/**
	 * The %0 skill effect cannot be removed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABSTATUS_SKILL_CAN_NOT_BE_TURNED_OFF_BY_CASTOR(String value0) {
		return new SM_SYSTEM_MESSAGE(1300057, value0);
	}

	/**
	 * You cured the altered state caused by %0 skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_TURN_OFF_ABNORMAL_STATUS(String value0) {
		return new SM_SYSTEM_MESSAGE(1300058, value0);
	}

	/**
	 * The %0 skill was cancelled as %1 is already under a more powerful skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CONFLICT_WITH_OTHER_SKILL(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300059, value0, value1);
	}

	/**
	 * You have not learned the skill to equip this weapon.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NO_WEAPON_MASTERY_SKILL() {
		return new SM_SYSTEM_MESSAGE(1300060);
	}

	/**
	 * You have not learned the skill to equip this armor.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NO_ARMOR_MASTERY_SKILL() {
		return new SM_SYSTEM_MESSAGE(1300061);
	}

	/**
	 * You cannot cast spells while silenced.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_CAST_MAGIC_SKILL_WHILE_SILENCED() {
		return new SM_SYSTEM_MESSAGE(1300062);
	}

	/**
	 * You cannot use physical skills while in a state of fear or restraint.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_CAST_PHYSICAL_SKILL_IN_FEAR() {
		return new SM_SYSTEM_MESSAGE(1300063);
	}

	/**
	 * You cannot use the skill while in an Altered State.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_CAST_IN_ABNORMAL_STATE() {
		return new SM_SYSTEM_MESSAGE(1300064);
	}

	/**
	 * Your actions are limited while in an Altered State.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_ACT_WHILE_IN_ABNORMAL_STATE() {
		return new SM_SYSTEM_MESSAGE(1300065);
	}

	/**
	 * You cannot attack while in an Altered State.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_ATTACK_WHILE_IN_ABNORMAL_STATE() {
		return new SM_SYSTEM_MESSAGE(1300066);
	}

	/**
	 * You cannot gather while in an Altered State.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_GATHER_WHILE_IN_ABNORMAL_STATE() {
		return new SM_SYSTEM_MESSAGE(1300067);
	}

	/**
	 * You cannot use the item while in an Altered State.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_USE_ITEM_WHILE_IN_ABNORMAL_STATE() {
		return new SM_SYSTEM_MESSAGE(1300068);
	}

	/**
	 * You cannot equip the item while in an Altered State.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_EQUIP_ITEM_WHILE_IN_ABNORMAL_STATE() {
		return new SM_SYSTEM_MESSAGE(1300069);
	}

	/**
	 * The %0 skill failed as there are already too many skills in effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SLOT_FULL(String value0) {
		return new SM_SYSTEM_MESSAGE(1300070, value0);
	}

	/**
	 * You cannot use the %0 skill in your current stance.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_CAST_THIS_SKILL_IN_CURRENT_STANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300071, value0);
	}

	/**
	 * You already have a spirit following you.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUMMON_ALREADY_HAVE_A_FOLLOWER() {
		return new SM_SYSTEM_MESSAGE(1300072);
	}

	/**
	 * As the spirit is too far, your summon has been forcibly canceled.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUMMON_UNSUMMON_BY_TOO_DISTANCE() {
		return new SM_SYSTEM_MESSAGE(1300073);
	}

	/**
	 * You are too far from the spirit is to issue an order.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUMMON_CANT_ORDER_BY_TOO_DISTANCE() {
		return new SM_SYSTEM_MESSAGE(1300074);
	}

	/**
	 * You have not learned the Advanced Dual-Wielding skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NO_WEAPON_DUEL_SKILL() {
		return new SM_SYSTEM_MESSAGE(1300075);
	}

	/**
	 * The target cannot be charmed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ENSLAVE_TARGET_CANT_BE_ENSLAVED() {
		return new SM_SYSTEM_MESSAGE(1300076);
	}

	/**
	 * You have failed to charm the target.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ENSLAVE_FAILED_TO_ENSLAVE() {
		return new SM_SYSTEM_MESSAGE(1300077);
	}

	/**
	 * You have charmed the target.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ENSLAVE_SUCCEDED_TO_ENSLAVE() {
		return new SM_SYSTEM_MESSAGE(1300078);
	}

	/**
	 * You have no dead pets.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENSLAVE_RESURRECT_PET_DONT_HAVE_DEAD_STONE() {
		return new SM_SYSTEM_MESSAGE(1300079);
	}

	/**
	 * You do not have enough Kinah to resurrect the Charm Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENSLAVE_RESURRECT_PET_NOT_ENOUGH_MONEY() {
		return new SM_SYSTEM_MESSAGE(1300080);
	}

	/**
	 * You are too far from the NPC to resurrect it.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENSLAVE_RESURRECT_PET_TOO_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300081);
	}

	/**
	 * You have resurrected the pet.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENSLAVE_RESURRECT_PET_SUCCEEDED() {
		return new SM_SYSTEM_MESSAGE(1300082);
	}

	/**
	 * Please try again after you have closed other dialog boxes.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENSLAVE_RESURRECT_PET_RETRY_WHEN_CLOSE_OTHER_QUESTION_WND() {
		return new SM_SYSTEM_MESSAGE(1300083);
	}

	/**
	 * You cannot transfer XP.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENSLAVE_GIVE_EXP_TO_PET_DONT_HAVE_PET() {
		return new SM_SYSTEM_MESSAGE(1300084);
	}

	/**
	 * The amount of XP you have transferred to the spirit is not enough.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENSLAVE_GIVE_EXP_TO_PET_NOT_ENOUGH_EXP() {
		return new SM_SYSTEM_MESSAGE(1300085);
	}

	/**
	 * %0 has reached level %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENSLAVE_PET_LEVEL_CHANGE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300086, value0, value1);
	}

	/**
	 * There is no target.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUMMON_NO_TARGET() {
		return new SM_SYSTEM_MESSAGE(1300087);
	}

	/**
	 * Invalid target.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_SUMMON_IS_NOT_VALID() {
		return new SM_SYSTEM_MESSAGE(1300088);
	}

	/**
	 * You cannot use this on enemies.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_INVALID_TARGET_NOTENEMY_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300089);
	}

	/**
	 * You can only use this on enemies.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_INVALID_TARGET_ENEMY_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300090);
	}

	/**
	 * You cannot duel with %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_PARTNER_INVALID(String value0) {
		return new SM_SYSTEM_MESSAGE(1300091, value0);
	}

	/**
	 * %0 is already fighting a duel with another opponent.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_PARTNER_IN_DUEL_ALREADY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300092, value0);
	}

	/**
	 * You are already fighting a duel with another opponent.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_YOU_ARE_IN_DUEL_ALREADY() {
		return new SM_SYSTEM_MESSAGE(1300093);
	}

	/**
	 * You challenged %0 to a duel.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_REQUEST_TO_PARTNER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300094, value0);
	}

	/**
	 * %0 is answering another request and cannot respond.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_CANT_REQUEST_WHEN_HE_IS_ASKED_QUESTION(String value0) {
		return new SM_SYSTEM_MESSAGE(1300095, value0);
	}

	/**
	 * There is no one for you to challenge to a duel.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_NO_USER_TO_REQUEST() {
		return new SM_SYSTEM_MESSAGE(1300096);
	}

	/**
	 * %0 declined your challenge.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_HE_REJECT_DUEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1300097, value0);
	}

	/**
	 * You won the duel against %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_YOU_WIN(String value0) {
		return new SM_SYSTEM_MESSAGE(1300098, value0);
	}

	/**
	 * You lost the duel against %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_YOU_LOSE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300099, value0);
	}

	/**
	 * The duel with %0 ended due to the time limit.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_TIMEOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300100, value0);
	}

	/**
	 * You are too far from %0 to start a duel.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_PARTNER_TOO_FAR_FOR_START(String value0) {
		return new SM_SYSTEM_MESSAGE(1300101, value0);
	}

	/**
	 * You cannot find the user you have challenged to a duel.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_LOST_REQUEST_DUEL_PARTNER() {
		return new SM_SYSTEM_MESSAGE(1300102);
	}

	/**
	 * You are not ready to start a duel.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_NOT_READY_TO_START_DUEL() {
		return new SM_SYSTEM_MESSAGE(1300103);
	}

	/**
	 * The duel with %0 ended due to the time limit.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_TIMEOUT_WITHOUT_PARTNER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300104, value0);
	}

	/**
	 * You cannot use that on your target.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_CAST_TO_CURRENT_TARGET() {
		return new SM_SYSTEM_MESSAGE(1300105);
	}

	/**
	 * You can use it only when you are in Counterattack mode.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_CAST_IN_NONE_COUNTER_STATUS() {
		return new SM_SYSTEM_MESSAGE(1300106);
	}

	/**
	 * This skill can only be used as part of a Chain Skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_CAST_IN_NONE_CHAINSKILL_STATUS() {
		return new SM_SYSTEM_MESSAGE(1300107);
	}

	/**
	 * You can only cast that on a group member who is using a Special Attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_CAST_ONLY_TO_MY_PARTY_CASTING_ULTRASKILL() {
		return new SM_SYSTEM_MESSAGE(1300108);
	}

	/**
	 * You interrupted the target's skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_TARGET_SKILL_CANCELED() {
		return new SM_SYSTEM_MESSAGE(1300109);
	}

	/**
	 * You must be equipped with a shield to use this skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NEED_SHIELD() {
		return new SM_SYSTEM_MESSAGE(1300110);
	}

	/**
	 * You must be equipped with an Off-hand Weapon to use the skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NEED_DUAL_WEAPON() {
		return new SM_SYSTEM_MESSAGE(1300111);
	}

	/**
	 * This skill can only be used from the rear of your opponent.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_USE_TO_TARGETS_BACK_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300112);
	}

	/**
	 * You can use this skill only while flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_RESTRICTION_FLY_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300113);
	}

	/**
	 * You cannot use a skill while you are flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_RESTRICTION_NO_FLY() {
		return new SM_SYSTEM_MESSAGE(1300114);
	}

	/**
	 * The attacker or the target is in a different area.
	 */
	public static SM_SYSTEM_MESSAGE STR_ATTACK_INVALID_POSITION() {
		return new SM_SYSTEM_MESSAGE(1300115);
	}

	/**
	 * Invalid target.
	 */
	public static SM_SYSTEM_MESSAGE STR_ATTACK_IMPROPER_TARGET() {
		return new SM_SYSTEM_MESSAGE(1300116);
	}

	/**
	 * You cannot equip the shield as you have not learned the Equip Shield skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NO_SHIELD_MASTERY_SKILL() {
		return new SM_SYSTEM_MESSAGE(1300117);
	}

	/**
	 * You cannot attack in your current stance.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_ATTACK_WHILE_IN_CURRENT_STANCE() {
		return new SM_SYSTEM_MESSAGE(1300118);
	}

	/**
	 * You cannot gather in your current stance.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_GATHER_WHILE_IN_CURRENT_STANCE() {
		return new SM_SYSTEM_MESSAGE(1300119);
	}

	/**
	 * You cannot use that item in your current stance.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_USE_ITEM_WHILE_IN_CURRENT_STANCE() {
		return new SM_SYSTEM_MESSAGE(1300120);
	}

	/**
	 * You cannot change your equipment in your current stance.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_EQUIP_ITEM_WHILE_IN_CURRENT_STANCE() {
		return new SM_SYSTEM_MESSAGE(1300121);
	}

	/**
	 * You cannot craft in your current stance.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_COMBINE_WHILE_IN_CURRENT_STANCE() {
		return new SM_SYSTEM_MESSAGE(1300122);
	}

	/**
	 * You cannot use that skill in your current stance.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_CAST_IN_CURRENT_STANCE() {
		return new SM_SYSTEM_MESSAGE(1300123);
	}

	/**
	 * You cannot change mode in your current stance.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_CHANGE_MODE__WHILE_IN_CURRENT_STANCE() {
		return new SM_SYSTEM_MESSAGE(1300124);
	}

	/**
	 * You are too close to the target to use that skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_TOO_CLOSE() {
		return new SM_SYSTEM_MESSAGE(1300125);
	}

	/**
	 * You cannot use the magic passage while flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_USE_GROUPGATE_WHEN_FLYING() {
		return new SM_SYSTEM_MESSAGE(1300126);
	}

	/**
	 * You can only use that when you have a spirit.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_USE_THIS_SKILL_WITHOUT_A_PET() {
		return new SM_SYSTEM_MESSAGE(1300127);
	}

	/**
	 * You can only use it when your spirit is in attack mode.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_USE_THIS_SKILL_WHEN_PET_IS_NOT_ATTACK_MODE() {
		return new SM_SYSTEM_MESSAGE(1300128);
	}

	/**
	 * You use the skill on yourself instead of the currently selected target.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_AUTO_CHANGE_TARGET_TO_MY() {
		return new SM_SYSTEM_MESSAGE(1300129);
	}

	/**
	 * Your spirit has no skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_USE_THIS_SKILL_TO_A_PET_THAT_HAS_NO_ULTRASKILL() {
		return new SM_SYSTEM_MESSAGE(1300130);
	}

	/**
	 * You cannot use this skill during combat.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_CAST_IN_COMBAT_STATE() {
		return new SM_SYSTEM_MESSAGE(1300131);
	}

	/**
	 * %0 is running away.
	 */
	public static SM_SYSTEM_MESSAGE STR_UI_COMBAT_NPC_FLEE_ORG(String value0) {
		return new SM_SYSTEM_MESSAGE(1300132, value0);
	}

	/**
	 * %0 gives up the pursuit.
	 */
	public static SM_SYSTEM_MESSAGE STR_UI_COMBAT_NPC_RETURN_ORG(String value0) {
		return new SM_SYSTEM_MESSAGE(1300133, value0);
	}

	/**
	 * %0 has withdrawn the challenge for a duel.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_REQUESTER_WITHDRAW_REQUEST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300134, value0);
	}

	/**
	 * You have withdrawn the challenge to %0 for a duel.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_WITHDRAW_REQUEST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300135, value0);
	}

	/**
	 * A duel between %0 and %1 has started.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_START_BROADCAST(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300136, value0, value1);
	}

	/**
	 * %0 defeated %1 in a duel.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_STOP_BROADCAST(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300137, value0, value1);
	}

	/**
	 * The duel between %0 and %1 was a draw.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_TIMEOUT_BROADCAST(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300138, value0, value1);
	}

	/**
	 * The duel ends in %0 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_TIMEOUT_NOTIFY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300139, value0);
	}

	/**
	 * You cannot request a duel to %0 as the player is currently busy.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_START_OTHER_IS_BUSY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300140, value0);
	}

	/**
	 * You cannot learn this skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILLLEARNBOOK_CANT_USE_NO_SKILL() {
		return new SM_SYSTEM_MESSAGE(1300141);
	}

	/**
	 * You have already learned this skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILLLEARNBOOK_CANT_USE_ALREADY_HAS_SKILL() {
		return new SM_SYSTEM_MESSAGE(1300142);
	}

	/**
	 * You cannot use that item here.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_USE_ITEM_IN_CURRENT_POSITION() {
		return new SM_SYSTEM_MESSAGE(1300143);
	}

	/**
	 * You cannot craft here.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_CAN_NOT_COMBINE_IN_CURRENT_POSITION() {
		return new SM_SYSTEM_MESSAGE(1300144);
	}

	/**
	 * You cannot gather here.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_GATHER_IN_CURRENT_POSTION() {
		return new SM_SYSTEM_MESSAGE(1300145);
	}

	/**
	 * You cannot use the skill here.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CANT_CAST_IN_CURRENT_POSTION() {
		return new SM_SYSTEM_MESSAGE(1300146);
	}

	/**
	 * You cannot take off in your current stance.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_TAKE_OFF__WHILE_IN_CURRENT_STANCE() {
		return new SM_SYSTEM_MESSAGE(1300147);
	}

	/**
	 * %0 has logged out.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ASK_OTHER_HAS_LOGOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300148, value0);
	}

	/**
	 * You cannot use this skill while transformed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_CAST_IN_SHAPECHANGE() {
		return new SM_SYSTEM_MESSAGE(1300149);
	}

	/**
	 * You have no right to use the selected Magic Passage.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_USE_GROUPGATE_NO_RIGHT() {
		return new SM_SYSTEM_MESSAGE(1300150);
	}

	/**
	 * This skill can only be used in the Abyss.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_CAST_IN_NOT_ABYSS_WORLD() {
		return new SM_SYSTEM_MESSAGE(1300151);
	}

	/**
	 * You cannot invite any more group members.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_CANT_ADD_NEW_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1300152);
	}

	/**
	 * Only the group leader can transfer authority to another person.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ONLY_LEADER_CAN_CHANGE_LEADER() {
		return new SM_SYSTEM_MESSAGE(1300153);
	}

	/**
	 * %0 has become the new group leader.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_HE_IS_NEW_LEADER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300154, value0);
	}

	/**
	 * You have become the new group leader.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_YOU_BECOME_NEW_LEADER() {
		return new SM_SYSTEM_MESSAGE(1300155);
	}

	/**
	 * Only the group leader can change the item distribution method.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ONLY_LEADER_CAN_CHANGE_LOOTING() {
		return new SM_SYSTEM_MESSAGE(1300156);
	}

	/**
	 * The item distribution method of the group has been changed to Manual.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_LOOTING_CHANGED_TO_MANUAL() {
		return new SM_SYSTEM_MESSAGE(1300157);
	}

	/**
	 * The item distribution method of the group has been changed to Auto.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_LOOTING_CHANGED_TO_AUTO() {
		return new SM_SYSTEM_MESSAGE(1300158);
	}

	/**
	 * The user you invited to the group is currently offline.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_NO_USER_TO_INVITE() {
		return new SM_SYSTEM_MESSAGE(1300159);
	}

	/**
	 * Only group leader can invite.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ONLY_LEADER_CAN_INVITE() {
		return new SM_SYSTEM_MESSAGE(1300160);
	}

	/**
	 * %0 has declined your invitation.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_HE_REJECT_INVITATION(String value0) {
		return new SM_SYSTEM_MESSAGE(1300161, value0);
	}

	/**
	 * You cannot invite yourself to a group.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_CAN_NOT_INVITE_SELF() {
		return new SM_SYSTEM_MESSAGE(1300162);
	}

	/**
	 * You cannot issue an invitation while you are dead.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_CANT_INVITE_WHEN_DEAD() {
		return new SM_SYSTEM_MESSAGE(1300163);
	}

	/**
	 * The selected group member is currently offline.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_OFFLINE_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1300164);
	}

	/**
	 * Only the group leader can kick a member out.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ONLY_LEADER_CAN_BANISH() {
		return new SM_SYSTEM_MESSAGE(1300165);
	}

	/**
	 * You have been kicked out of the group.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_YOU_ARE_BANISHED() {
		return new SM_SYSTEM_MESSAGE(1300166);
	}

	/**
	 * The group has been disbanded.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_IS_DISPERSED() {
		return new SM_SYSTEM_MESSAGE(1300167);
	}

	/**
	 * %0 has left your group.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_HE_LEAVE_PARTY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300168, value0);
	}

	/**
	 * %0 is already a member of another group.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_HE_IS_ALREADY_MEMBER_OF_OTHER_PARTY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300169, value0);
	}

	/**
	 * %0 is already a member of your group.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_HE_IS_ALREADY_MEMBER_OF_OUR_PARTY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300170, value0);
	}

	/**
	 * You are not in any group.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_YOU_ARE_NOT_PARTY_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1300171);
	}

	/**
	 * You are not a group member.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_NOT_PARTY_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1300172);
	}

	/**
	 * You have invited %0 to join your group.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_INVITED_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300173, value0);
	}

	/**
	 * Currently, %0 cannot accept your group invitation.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_CANT_INVITE_WHEN_HE_IS_ASKED_QUESTION(String value0) {
		return new SM_SYSTEM_MESSAGE(1300174, value0);
	}

	/**
	 * %0 has been disconnected.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_HE_BECOME_OFFLINE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300175, value0);
	}

	/**
	 * %0 has been offline for too long and is automatically excluded from the group.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_HE_BECOME_OFFLINE_TIMEOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300176, value0);
	}

	/**
	 * %0 has been kicked out of your group.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_HE_IS_BANISHED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300177, value0);
	}

	/**
	 * The rare item distribution method of the group has been changed to Free-for-All.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_RARE_LOOTING_CHANGED_TO_MANUAL() {
		return new SM_SYSTEM_MESSAGE(1300178);
	}

	/**
	 * The rare item distribution method of the group has been changed to Auto.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_RARE_LOOTING_CHANGED_TO_AUTO() {
		return new SM_SYSTEM_MESSAGE(1300179);
	}

	/**
	 * The rare item distribution method of the group has been changed to Dice Roll.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_RARE_LOOTING_CHANGED_TO_DICE() {
		return new SM_SYSTEM_MESSAGE(1300180);
	}

	/**
	 * A group member cannot be kicked out before the completion of loot distribution.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_CANNOT_BANISH_ITEMPOOL_NOT_EMPTY() {
		return new SM_SYSTEM_MESSAGE(1300181);
	}

	/**
	 * %0 rolled the dice and got a %num1.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ITEM_DICE(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1300182, value0, num1);
	}

	/**
	 * You can roll the dice once more if the rolled number is less than 100.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ITEM_DICE_AGAIN() {
		return new SM_SYSTEM_MESSAGE(1300183);
	}

	/**
	 * The item distribution method of the group has been changed to Free-for-All.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_LOOTING_CHANGED_TO_FREEFORALL() {
		return new SM_SYSTEM_MESSAGE(1300184);
	}

	/**
	 * The item distribution method of the group has been changed to Round-robin.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_LOOTING_CHANGED_TO_ROUNDROBIN() {
		return new SM_SYSTEM_MESSAGE(1300185);
	}

	/**
	 * The item distribution method of the group has been changed to Group Leader.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_LOOTING_CHANGED_TO_LEADERONLY() {
		return new SM_SYSTEM_MESSAGE(1300186);
	}

	/**
	 * %0 rolled the dice and got a %num1 (max. %num2).
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ITEM_DICE_CUSTOM(String value0, int num1, int num2) {
		return new SM_SYSTEM_MESSAGE(1300187, value0, num1, num2);
	}

	/**
	 * You cannot invite members of other race.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_CANT_INVITE_OTHER_RACE() {
		return new SM_SYSTEM_MESSAGE(1300188);
	}

	/**
	 * You have invited %0's group to the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_INVITED_HIS_PARTY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300189, value0);
	}

	/**
	 * %0 has declined your invitation to join the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_HE_REJECT_INVITATION(String value0) {
		return new SM_SYSTEM_MESSAGE(1300190, value0);
	}

	/**
	 * Currently, %0 cannot accept your invitation to join the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_CANT_INVITE_WHEN_HE_IS_ASKED_QUESTION(String value0) {
		return new SM_SYSTEM_MESSAGE(1300191, value0);
	}

	/**
	 * %0 is already a member of another alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_HE_IS_ALREADY_MEMBER_OF_OTHER_ALLIANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300192, value0);
	}

	/**
	 * %0 is already a member of your alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_HE_IS_ALREADY_MEMBER_OF_OUR_ALLIANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300193, value0);
	}

	/**
	 * You cannot invite %0 to the alliance as he or she is not a group leader.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_CAN_NOT_INVITE_HIM_HE_IS_NOT_PARTY_LEADER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300194, value0);
	}

	/**
	 * You cannot invite %0 to the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_CAN_NOT_INVITE_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300195, value0);
	}

	/**
	 * You cannot invite any more as the alliance is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_CANT_ADD_NEW_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1300196);
	}

	/**
	 * Only the group leader can leave the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_ONLY_PARTY_LEADER_CAN_LEAVE_ALLIANCE() {
		return new SM_SYSTEM_MESSAGE(1300197);
	}

	/**
	 * Your group is not part of an alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_YOUR_PARTY_IS_NOT_ALLIANCE_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1300198);
	}

	/**
	 * %0's group has left the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_HIS_PARTY_LEAVE_ALLIANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300199, value0);
	}

	/**
	 * Your group has left the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_MY_PARTY_LEAVE_ALLIANCE() {
		return new SM_SYSTEM_MESSAGE(1300200);
	}

	/**
	 * The alliance has been disbanded.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_DISPERSED() {
		return new SM_SYSTEM_MESSAGE(1300201);
	}

	/**
	 * %0 has left the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_HE_LEAVED_PARTY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300202, value0);
	}

	/**
	 * %0 has been offline for too long and has been automatically kicked out of the group and the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_HE_LEAVED_PARTY_OFFLINE_TIMEOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300203, value0);
	}

	/**
	 * %0 has been kicked out of the group and thus the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_HE_IS_BANISHED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300204, value0);
	}

	/**
	 * %0 has become the new group leader.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_HE_BECOME_PARTY_LEADER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300205, value0);
	}

	/**
	 * The item distribution method of the alliance has been changed to Free-for-All.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_LOOTING_CHANGED_TO_FREE() {
		return new SM_SYSTEM_MESSAGE(1300206);
	}

	/**
	 * The item distribution method of the alliance has been changed to Auto.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_LOOTING_CHANGED_TO_RANDOM() {
		return new SM_SYSTEM_MESSAGE(1300207);
	}

	/**
	 * %0 has already requested the item distribution method to be changed.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_CHANGE_LOOT_PROCESSING_HIS_REQUEST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300208, value0);
	}

	/**
	 * Your request to change the item distribution method is being processed.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_CHANGE_LOOT_PROCESSING_YOUR_REQUEST() {
		return new SM_SYSTEM_MESSAGE(1300209);
	}

	/**
	 * %0 denied %1's request to change the item distribution method.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_CHANGE_LOOT_HE_DENIED_HIS_ASK(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300210, value0, value1);
	}

	/**
	 * %0's request to change the item distribution method has been denied.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_CHANGE_LOOT_HE_DENIED_MY_ASK(String value0) {
		return new SM_SYSTEM_MESSAGE(1300211, value0);
	}

	/**
	 * You asked the alliance Captain to change the item distribution method.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_CHANGE_LOOT_ASK_SUBMITTED() {
		return new SM_SYSTEM_MESSAGE(1300212);
	}

	/**
	 * %0's request to change the item distribution method timed out.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_CHANGE_LOOT_TIMEOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300213, value0);
	}

	/**
	 * You asked the alliance Captain for permission to pick up %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_PICKUP_ITEM_ASK_SUBMITTED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300214, value0);
	}

	/**
	 * Your request for permission to pick up %0 is being processed.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_PICKUP_ITEM_PROCESSING_YOUR_REQUEST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300215, value0);
	}

	/**
	 * %0's request for permission to pick up %1 was approved.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_PICKUP_ITEM_ALL_ACCEPT_HIM(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300216, value0, value1);
	}

	/**
	 * %0 denied %1's request for permission to pick up %2.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_PICKUP_ITEM_HE_DENIED(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1300217, value0, value1, value2);
	}

	/**
	 * %0 denied your request for permission to pick up %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_PICKUP_ITEM_HE_DENIED_MY_ASK(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300218, value0, value1);
	}

	/**
	 * %0's request for permission to pick up %1 timed out.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_PICKUP_ITEM_TIMEOUT(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300219, value0, value1);
	}

	/**
	 * The request cannot be processed, as there are already too many requests pending approval by the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_TOO_MANY_VOTE() {
		return new SM_SYSTEM_MESSAGE(1300220);
	}

	/**
	 * You are not a member of an alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_YOU_ARE_NOT_PARTY_ALLIANCE_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1300221);
	}

	/**
	 * %0 has been disconnected.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_HE_BECOME_OFFLINE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300222, value0);
	}

	/**
	 * Only the alliance captain can change the item distribution method.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_ONLY_LEADER_CAN_CHANGE_LOOTING() {
		return new SM_SYSTEM_MESSAGE(1300223);
	}

	/**
	 * The item distribution method of the alliance has been changed to Free-for-All.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_LOOTING_CHANGED_TO_FREEFORALL() {
		return new SM_SYSTEM_MESSAGE(1300224);
	}

	/**
	 * The item distribution method of the alliance has been changed to Round-robin.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_LOOTING_CHANGED_TO_ROUNDROBIN() {
		return new SM_SYSTEM_MESSAGE(1300225);
	}

	/**
	 * The item distribution method of the alliance has been changed to Captain.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_LOOTING_CHANGED_TO_LEADERONLY() {
		return new SM_SYSTEM_MESSAGE(1300226);
	}

	/**
	 * You cannot invite the selected player as he or she is too busy.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_INVITE_OTHER_IS_BUSY() {
		return new SM_SYSTEM_MESSAGE(1300227);
	}

	/**
	 * That name is invalid. Please try another.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CREATE_INVALID_GUILD_NAME() {
		return new SM_SYSTEM_MESSAGE(1300228);
	}

	/**
	 * You are too far from the NPC to create a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CREATE_TOO_FAR_FROM_CREATOR_NPC() {
		return new SM_SYSTEM_MESSAGE(1300229);
	}

	/**
	 * Please try again after you have closed other dialog boxes.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CREATE_RETRY_WHEN_CLOSE_OTHER_QUESTION_WND() {
		return new SM_SYSTEM_MESSAGE(1300230);
	}

	/**
	 * You do not have enough Kinah to create a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CREATE_NOT_ENOUGH_MONEY() {
		return new SM_SYSTEM_MESSAGE(1300231);
	}

	/**
	 * You cannot create a Legion as you are already a member of another Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CREATE_ALREADY_BELONGS_TO_GUILD() {
		return new SM_SYSTEM_MESSAGE(1300232);
	}

	/**
	 * That name is invalid. Please try another..
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CREATE_SAME_GUILD_EXIST() {
		return new SM_SYSTEM_MESSAGE(1300233);
	}

	/**
	 * You cannot create a new Legion as the grace period between creating Legions has not expired.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CREATE_LAST_DAY_CHECK() {
		return new SM_SYSTEM_MESSAGE(1300234);
	}

	/**
	 * The %0 Legion has been created.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CREATED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300235, value0);
	}

	/**
	 * You cannot leave your Legion during a war.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_LEAVE_CANT_LEAVE_WHILE_WAR() {
		return new SM_SYSTEM_MESSAGE(1300236);
	}

	/**
	 * You cannot leave your Legion while using the Legion Warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_LEAVE_CANT_LEAVE_GUILD_WHILE_USING_WAREHOUSE() {
		return new SM_SYSTEM_MESSAGE(1300237);
	}

	/**
	 * You cannot leave your Legion unless you transfer Brigade General authority to someone else.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_LEAVE_MASTER_CANT_LEAVE_BEFORE_CHANGE_MASTER() {
		return new SM_SYSTEM_MESSAGE(1300238);
	}

	/**
	 * You are not a member of a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_LEAVE_I_AM_NOT_BELONG_TO_GUILD() {
		return new SM_SYSTEM_MESSAGE(1300239);
	}

	/**
	 * %0 has left the Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_LEAVE_HE_LEFT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300240, value0);
	}

	/**
	 * You have left the %0 Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_LEAVE_DONE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300241, value0);
	}

	/**
	 * You are not a member of a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_BANISH_I_AM_NOT_BELONG_TO_GUILD() {
		return new SM_SYSTEM_MESSAGE(1300242);
	}

	/**
	 * You cannot kick yourself out from a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_BANISH_CANT_BANISH_SELF() {
		return new SM_SYSTEM_MESSAGE(1300243);
	}

	/**
	 * You do not have the authority to kick out a Legion member.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_BANISH_DONT_HAVE_RIGHT_TO_BANISH() {
		return new SM_SYSTEM_MESSAGE(1300244);
	}

	/**
	 * You cannot kick a Legion member out during a war.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_BANISH_CANT_BAN_MEMBER_WHILE_WAR() {
		return new SM_SYSTEM_MESSAGE(1300245);
	}

	/**
	 * You have been kicked out of the %0 Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_BANISHIED_FROM_GUILD_BY_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300246, value0);
	}

	/**
	 * %0 kicked %1 out of the Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_BANSIH_HE_BANISHED_HIM(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300247, value0, value1);
	}

	/**
	 * %0 is not a member of your Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_BANISH_HE_IS_NOT_MY_GUILD_MEMBER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300248, value0);
	}

	/**
	 * You cannot kick out the Legion Brigade General.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_BANISH_CAN_BANISH_MASTER() {
		return new SM_SYSTEM_MESSAGE(1300249);
	}

	/**
	 * You cannot issue a Legion invitation while you are dead.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_CANT_INVITE_WHEN_DEAD() {
		return new SM_SYSTEM_MESSAGE(1300250);
	}

	/**
	 * You are not a member of a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_I_AM_NOT_BELONG_TO_GUILD() {
		return new SM_SYSTEM_MESSAGE(1300251);
	}

	/**
	 * You have no authority to invite others to the Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_DONT_HAVE_RIGHT_TO_INVITE() {
		return new SM_SYSTEM_MESSAGE(1300252);
	}

	/**
	 * There is no user to invite to your Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_NO_USER_TO_INVITE() {
		return new SM_SYSTEM_MESSAGE(1300253);
	}

	/**
	 * You cannot invite yourself to a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_CAN_NOT_INVITE_SELF() {
		return new SM_SYSTEM_MESSAGE(1300254);
	}

	/**
	 * %0 is already a member of your Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_HE_IS_MY_GUILD_MEMBER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300255, value0);
	}

	/**
	 * %0 is a member of another Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_HE_IS_OTHER_GUILD_MEMBER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300256, value0);
	}

	/**
	 * There is no room in the Legion for more members.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_CAN_NOT_ADD_MEMBER_ANY_MORE() {
		return new SM_SYSTEM_MESSAGE(1300257);
	}

	/**
	 * You have sent a Legion invitation to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_SENT_INVITE_MSG_TO_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300258, value0);
	}

	/**
	 * %0 has declined your Legion invitation.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_HE_REJECTED_INVITATION(String value0) {
		return new SM_SYSTEM_MESSAGE(1300259, value0);
	}

	/**
	 * %0 has joined your Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_HE_JOINED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300260, value0);
	}

	/**
	 * You are not a member of a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_RANK_I_AM_NOT_BELONG_TO_GUILD() {
		return new SM_SYSTEM_MESSAGE(1300261);
	}

	/**
	 * You cannot change the ranks of Legion members because you are not the Legion Brigade General.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_RANK_DONT_HAVE_RIGHT() {
		return new SM_SYSTEM_MESSAGE(1300262);
	}

	/**
	 * The Legion Brigade General cannot change its own rank.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_RANK_ERROR_SELF() {
		return new SM_SYSTEM_MESSAGE(1300263);
	}

	/**
	 * There is no one to change rank.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_RANK_NO_USER() {
		return new SM_SYSTEM_MESSAGE(1300264);
	}

	/**
	 * %0 is not a member of your Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_RANK_HE_IS_NOT_MY_GUILD_MEMBER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300265, value0);
	}

	/**
	 * %0 has become the Legion Brigade General.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_RANK_DONE_1_GUILD_MASTER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300266, value0);
	}

	/**
	 * %0 has become a Legion Centurion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_RANK_DONE_2_GUILD_OFFICER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300267, value0);
	}

	/**
	 * %0 has become a Legionary.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_RANK_DONE_3_GUILD_MEMBER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300268, value0);
	}

	/**
	 * You do not have the authority to change the Legion Brigade General.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MASTER_DONT_HAVE_RIGHT() {
		return new SM_SYSTEM_MESSAGE(1300269);
	}

	/**
	 * You cannot transfer your Brigade General authority to an offline user.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MASTER_NO_SUCH_USER() {
		return new SM_SYSTEM_MESSAGE(1300270);
	}

	/**
	 * You are already the Legion Brigade General.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MASTER_ERROR_SELF() {
		return new SM_SYSTEM_MESSAGE(1300271);
	}

	/**
	 * %0 is not a member of your Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MASTER_NOT_MY_GUILD_MEMBER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300272, value0);
	}

	/**
	 * %0 has become the Legion Brigade General.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MASTER_DONE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300273, value0);
	}

	/**
	 * You cannot join the alliance because you are not the Legion Brigade General.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_JOIN_CLAN_DONT_HAVE_RIGHT() {
		return new SM_SYSTEM_MESSAGE(1300274);
	}

	/**
	 * You cannot leave the alliance because you are not the Legion Brigade General.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_LEAVE_CLAN_DONT_HAVE_RIGHT() {
		return new SM_SYSTEM_MESSAGE(1300275);
	}

	/**
	 * You do not have the authority to modify the Legion Announcement.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WRITE_NOTICE_DONT_HAVE_RIGHT() {
		return new SM_SYSTEM_MESSAGE(1300276);
	}

	/**
	 * The Legion Announcement has been modified.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WRITE_NOTICE_DONE() {
		return new SM_SYSTEM_MESSAGE(1300277);
	}

	/**
	 * You must be a Legion member to use the Legion warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_NO_GUILD_TO_DEPOSIT() {
		return new SM_SYSTEM_MESSAGE(1300278);
	}

	/**
	 * You cannot use the Legion warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_USE_GUILD_STORAGE() {
		return new SM_SYSTEM_MESSAGE(1300279);
	}

	/**
	 * Another Legion member is using the warehouse. Please try again later.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WAREHOUSE_IN_USE() {
		return new SM_SYSTEM_MESSAGE(1300280);
	}

	/**
	 * You are not a member of a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WRITE_INTRO_NOT_BELONG_TO_GUILD() {
		return new SM_SYSTEM_MESSAGE(1300281);
	}

	/**
	 * Your Character Information has been modified.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WRITE_INTRO_DONE() {
		return new SM_SYSTEM_MESSAGE(1300282);
	}

	/**
	 * You have no authority to change the Legion authority settings.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_RIGHT_DONT_HAVE_RIGHT() {
		return new SM_SYSTEM_MESSAGE(1300283);
	}

	/**
	 * The Legion authority has been modified.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_RIGHT_DONE() {
		return new SM_SYSTEM_MESSAGE(1300284);
	}

	/**
	 * Legion Information: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INTRO(String value0) {
		return new SM_SYSTEM_MESSAGE(1300285, value0);
	}

	/**
	 * You do not have enough Kinah for cancellation.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CREATE_NOT_ENOUGH_MONEY_1() {
		return new SM_SYSTEM_MESSAGE(1300286);
	}

	/**
	 * There is no room in the Legion for more members.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_CAN_NOT_JOIN_TO_GUILD_BY_SIZE_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1300287);
	}

	/**
	 * You cannot join the Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_CAN_NOT_JOIN_TO_GUILD() {
		return new SM_SYSTEM_MESSAGE(1300288);
	}

	/**
	 * You cannot join the Legion as the player who invited you is dead.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_CAN_NOT_JOIN_TO_GUILD_INVITOR_IS_DEAD() {
		return new SM_SYSTEM_MESSAGE(1300289);
	}

	/**
	 * Currently, the selected player cannot be invited to join your Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_CANT_INVITE_WHEN_HE_IS_QUESTION_ASKED() {
		return new SM_SYSTEM_MESSAGE(1300290);
	}

	/**
	 * The target is not valid. Please select a player.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_INCORRECT_TARGET() {
		return new SM_SYSTEM_MESSAGE(1300291);
	}

	/**
	 * This authority cannot be granted to the rank.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_RIGHT_CANT_GIVE_RIGHT() {
		return new SM_SYSTEM_MESSAGE(1300292);
	}

	/**
	 * You are not a member of a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_I_AM_NOT_BELONG_TO_GUILD() {
		return new SM_SYSTEM_MESSAGE(1300293);
	}

	/**
	 * You are not a member of a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_STAYMODE_CANCEL_I_AM_NOT_BELONG_TO_GUILD() {
		return new SM_SYSTEM_MESSAGE(1300294);
	}

	/**
	 * Please try again after you have closed other dialog boxes.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_STAYMODE_CANCEL_RETRY_WHEN_CLOSE_OTHER_QUESTION_WND() {
		return new SM_SYSTEM_MESSAGE(1300295);
	}

	/**
	 * You are too far from the NPC to cancel the Legion disbanding.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_STAYMODE_CANCEL_TOO_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300296);
	}

	/**
	 * You cannot disband the Legion during a war.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_CANT_DISPERSE_WHILE_WAR() {
		return new SM_SYSTEM_MESSAGE(1300297);
	}

	/**
	 * You cannot disband your Legion while you are using the Legion warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_CANT_DISPERSE_GUILD_WHILE_USING_WAREHOUSE() {
		return new SM_SYSTEM_MESSAGE(1300298);
	}

	/**
	 * You cannot disband a Legion that has a fortress or hideout.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_CANT_DISPERSE_GULILD_HAVING_HOUSE() {
		return new SM_SYSTEM_MESSAGE(1300299);
	}

	/**
	 * You have no authority to disband the Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_ONLY_MASTER_CAN_DISPERSE() {
		return new SM_SYSTEM_MESSAGE(1300300);
	}

	/**
	 * You are not a member of a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_I_AM_NOT_BELONG_TO_GUILD() {
		return new SM_SYSTEM_MESSAGE(1300301);
	}

	/**
	 * The %0 Legion has been disbanded.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_DONE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300302, value0);
	}

	/**
	 * The Brigade General has requested to disband the Legion. The expected time of disbanding is %DATETIME0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_REQUESTED(String datetime0) {
		return new SM_SYSTEM_MESSAGE(1300303, datetime0);
	}

	/**
	 * You have already requested to disband the Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_ALREADY_REQUESTED() {
		return new SM_SYSTEM_MESSAGE(1300304);
	}

	/**
	 * You are too far from the NPC to disband the Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_TOO_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300305);
	}

	/**
	 * You cannot delete a character that joined a Legion. Please try again after it has left the Legion or the Legion is disbanded.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_STAYMODE_CANCEL_1() {
		return new SM_SYSTEM_MESSAGE(1300306);
	}

	/**
	 * The Legion disbanding mode has been cancelled.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_CANCEL() {
		return new SM_SYSTEM_MESSAGE(1300307);
	}

	/**
	 * Time remaining until disbanding: %DURATIONDAY0
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_TIME(String durationday0) {
		return new SM_SYSTEM_MESSAGE(1300308, durationday0);
	}

	/**
	 * Only the Legion Brigade General can cancel the disbanding mode.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_STAYMODE_CANCEL_ONLY_MASTER_CAN_CANCEL() {
		return new SM_SYSTEM_MESSAGE(1300309);
	}

	/**
	 * The Legion is not waiting to be disbanded.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_STAYMODE_CANCEL_YOUR_GUILD_IS_NOT_DISPERS_REQUESTED() {
		return new SM_SYSTEM_MESSAGE(1300310);
	}

	/**
	 * You cannot invite members of other race.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_CAN_NOT_INVITE_OTHER_RACE() {
		return new SM_SYSTEM_MESSAGE(1300311);
	}

	/**
	 * You are not a member of a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_NICKNAME_I_AM_NOT_BELONG_TO_GUILD() {
		return new SM_SYSTEM_MESSAGE(1300312);
	}

	/**
	 * You have no authority to bestow a title.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_NICKNAME_DONT_HAVE_RIGHT_TO_CHANGE_NICKNAME() {
		return new SM_SYSTEM_MESSAGE(1300313);
	}

	/**
	 * %0 is not a member of your Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_NICKNAME_HE_IS_NOT_MY_GUILD_MEMBER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300314, value0);
	}

	/**
	 * Only the Legion Brigade General can request to raise the level.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_LEVEL_DONT_HAVE_RIGHT() {
		return new SM_SYSTEM_MESSAGE(1300315);
	}

	/**
	 * You need to complete the %0 legion task to level up the legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_LEVEL_UP_CHALLENGE_TASK(int currentLevel) {
		return new SM_SYSTEM_MESSAGE(904452, currentLevel);
	}

	/**
	 * The Legion is already at the highest level.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_LEVEL_CANT_LEVEL_UP() {
		return new SM_SYSTEM_MESSAGE(1300316);
	}

	/**
	 * You do not have enough Contribution Points.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_LEVEL_NOT_ENOUGH_POINT() {
		return new SM_SYSTEM_MESSAGE(1300317);
	}

	/**
	 * Your Legion does not have enough members.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_LEVEL_NOT_ENOUGH_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1300318);
	}

	/**
	 * You do not have enough Kinah.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_LEVEL_NOT_ENOUGH_MONEY() {
		return new SM_SYSTEM_MESSAGE(1300319);
	}

	/**
	 * Your Legion is now at level %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_LEVEL_DONE(int value0) {
		return new SM_SYSTEM_MESSAGE(1300320, value0);
	}

	/**
	 * Please try again after you have closed other input boxes.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_LEVEL_RETRY_WHEN_CLOSE_OTHER_QUESTION_WND() {
		return new SM_SYSTEM_MESSAGE(1300321);
	}

	/**
	 * You do not have the authority to use the Legion warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WAREHOUSE_NO_RIGHT() {
		return new SM_SYSTEM_MESSAGE(1300322);
	}

	/**
	 * The Legion warehouse is now loading. Please try again later.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WAREHOUSE_IN_LOADING() {
		return new SM_SYSTEM_MESSAGE(1300323);
	}

	/**
	 * Your Legion does not have enough funds.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WAREHOUSE_NOT_ENOUGH_FUND() {
		return new SM_SYSTEM_MESSAGE(1300324);
	}

	/**
	 * The target is busy and cannot be invited at the moment.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_OTHER_IS_BUSY() {
		return new SM_SYSTEM_MESSAGE(1300325);
	}

	/**
	 * You are too far from the NPC to raise the Legion level.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_LEVEL_TOO_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300326);
	}

	/**
	 * You are already a member of a Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_YOU_ARE_ALREADY_BELONGS_TO_GUILD() {
		return new SM_SYSTEM_MESSAGE(1300327);
	}

	/**
	 * You cannot join another Legion while waiting for your Legion to be created.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_YOU_ARE_WAITING_FOR_GUILD_CREATE() {
		return new SM_SYSTEM_MESSAGE(1300328);
	}

	/**
	 * The Legion you were to join no longer exists.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_CAN_NOT_JOIN_NOT_EXISTS() {
		return new SM_SYSTEM_MESSAGE(1300329);
	}

	/**
	 * You nominated %0 as the next Legion Brigade General.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MASTER_SENT_OFFER_MSG_TO_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300330, value0);
	}

	/**
	 * You cannot request the selected player to become the Legion Brigade General.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MASTER_SENT_CANT_OFFER_WHEN_HE_IS_QUESTION_ASKED() {
		return new SM_SYSTEM_MESSAGE(1300331);
	}

	/**
	 * %0 has declined to become the Legion Brigade General.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MASTER_HE_DECLINE_YOUR_OFFER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300332, value0);
	}

	/**
	 * You cannot use the Legion warehouse during the disbandment waiting period.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WAREHOUSE_CANT_USE_WHILE_DISPERSE() {
		return new SM_SYSTEM_MESSAGE(1300333);
	}

	/**
	 * Limited edition items are all sold out.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_ITEM_SOLD_OUT() {
		return new SM_SYSTEM_MESSAGE(1300334);
	}

	/**
	 * You cannot buy this item.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_USER_BUY_FAILED() {
		return new SM_SYSTEM_MESSAGE(1300335);
	}

	/**
	 * %0 does not sell items.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_HE_DOES_NOT_SELL_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300336, value0);
	}

	/**
	 * %0 does not buy items.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_HE_DOES_NOT_BUY_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300337, value0);
	}

	/**
	 * You are too away to trade.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_TOO_FAR_TO_TRADE() {
		return new SM_SYSTEM_MESSAGE(1300338);
	}

	/**
	 * You do not have enough Kinah to buy the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_NOT_ENOUGH_MONEY_TO_BUY_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300339);
	}

	/**
	 * You have bought the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_USER_BOUGHT_ITEMS() {
		return new SM_SYSTEM_MESSAGE(1300340);
	}

	/**
	 * Sales complete.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_USER_SELL_ITEMS() {
		return new SM_SYSTEM_MESSAGE(1300341);
	}

	/**
	 * You cannot sell equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_CAN_NOT_SELL_EQUIPED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300342);
	}

	/**
	 * The price of the item has changed. Please try buying it again after you have checked the changed price.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_PRICE_CHANGED_RETRY_PLEASE() {
		return new SM_SYSTEM_MESSAGE(1300343);
	}

	/**
	 * %0 is not an item that can be sold.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_ITEM_CAN_NOT_BE_SELLED_TO_NPC(String value0) {
		return new SM_SYSTEM_MESSAGE(1300344, value0);
	}

	/**
	 * You cannot register any more items.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_FULL_BASKET() {
		return new SM_SYSTEM_MESSAGE(1300345);
	}

	/**
	 * You are too far to have a conversation.
	 */
	public static SM_SYSTEM_MESSAGE STR_DIALOG_TOO_FAR_TO_TALK() {
		return new SM_SYSTEM_MESSAGE(1300346);
	}

	/**
	 * You are already trading with someone else.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_YOU_ARE_ALREADY_EXCHANGING() {
		return new SM_SYSTEM_MESSAGE(1300347);
	}

	/**
	 * You cannot trade while you are invisible.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_CANT_EXCHANGE_WHILE_INVISIBLE() {
		return new SM_SYSTEM_MESSAGE(1300348);
	}

	/**
	 * You cannot trade with an invisible player.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_CANT_EXCHANGE_WITH_INVISIBLE_USER() {
		return new SM_SYSTEM_MESSAGE(1300349);
	}

	/**
	 * You cannot trade as you are overburdened with items.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_TOO_HEAVY_TO_TRADE() {
		return new SM_SYSTEM_MESSAGE(1300350);
	}

	/**
	 * You have no one to trade with.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_NO_ONE_TO_EXCHANGE() {
		return new SM_SYSTEM_MESSAGE(1300351);
	}

	/**
	 * You are too far from the target to trade.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_TOO_FAR_TO_EXCHANGE() {
		return new SM_SYSTEM_MESSAGE(1300352);
	}

	/**
	 * You sent a trade message to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_ASKED_EXCHANGE_TO_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300353, value0);
	}

	/**
	 * %0 declined your trade offer.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_HE_REJECTED_EXCHANGE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300354, value0);
	}

	/**
	 * The target is already trading with someone else.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_PARTNER_IS_EXCHANGING_WITH_OTHER() {
		return new SM_SYSTEM_MESSAGE(1300355);
	}

	/**
	 * %0 cannot trade at the moment.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_CANT_ASK_WHEN_HE_IS_ASKED_QUESTION(String value0) {
		return new SM_SYSTEM_MESSAGE(1300356, value0);
	}

	/**
	 * You cannot trade with the target as the target is carrying too many items.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTNER_TOO_HEAVY_TO_EXCHANGE() {
		return new SM_SYSTEM_MESSAGE(1300357);
	}

	/**
	 * %0 is not a tradable item.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_ITEM_CANNOT_BE_EXCHANGED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300358, value0);
	}

	/**
	 * You cannot trade with the target as you are carrying too many items.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_CANT_EXCHANGE_HEAVY_TO_ADD_EXCHANGE_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300359);
	}

	/**
	 * You cannot trade as the target already has the limited possession item %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_CANT_EXCHANGE_PARTNER_HAS_LORE_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300360, value0);
	}

	/**
	 * The trade is complete.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_COMPLETE() {
		return new SM_SYSTEM_MESSAGE(1300361);
	}

	/**
	 * The other player has pressed the Lock List button.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_OTHER_PRESSED_CHECK() {
		return new SM_SYSTEM_MESSAGE(1300362);
	}

	/**
	 * The other player has pressed the Final Confirmation button.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_OTHER_PRESSED_OK() {
		return new SM_SYSTEM_MESSAGE(1300363);
	}

	/**
	 * The trade has been cancelled.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_CANCELED() {
		return new SM_SYSTEM_MESSAGE(1300364);
	}

	/**
	 * You cannot sell equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_CANT_SELL_EQUIPPED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300365);
	}

	/**
	 * You cannot trade any more as your inventory is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_FULL_INVENTORY() {
		return new SM_SYSTEM_MESSAGE(1300366);
	}

	/**
	 * You cannot register any more items.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_FULL_BASKET() {
		return new SM_SYSTEM_MESSAGE(1300367);
	}

	/**
	 * This item cannot be registered.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_CAN_NOT_REGISTER_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300368);
	}

	/**
	 * This is not a tradable item.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_ITEM_CAN_NOT_BE_EXCHANGED() {
		return new SM_SYSTEM_MESSAGE(1300369);
	}

	/**
	 * You cannot use the selected item until you reach the %0 rank.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ITEM_INVALID_RANK(String value0) {
		return new SM_SYSTEM_MESSAGE(1300370, value0);
	}

	/**
	 * Your Class cannot use the selected item.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ITEM_INVALID_CLASS() {
		return new SM_SYSTEM_MESSAGE(1300371);
	}

	/**
	 * You cannot use %1 until you reach level %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ITEM_TOO_LOW_LEVEL_MUST_BE_THIS_LEVEL(String value1, int value0) {
		return new SM_SYSTEM_MESSAGE(1300372, value0, value1);
	}

	/**
	 * Your race cannot use this item.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ITEM_INVALID_RACE() {
		return new SM_SYSTEM_MESSAGE(1300373);
	}

	/**
	 * Your nationality prevents you from using this item.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ITEM_INVALID_NATION() {
		return new SM_SYSTEM_MESSAGE(1300374);
	}

	/**
	 * This item cannot be used by your gender.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ITEM_INVALID_GENDER() {
		return new SM_SYSTEM_MESSAGE(1300375);
	}

	/**
	 * You are too overburdened to pick up any more items.
	 */
	public static SM_SYSTEM_MESSAGE STR_TOO_HEAVY() {
		return new SM_SYSTEM_MESSAGE(1300376);
	}

	/**
	 * Another player has the first chance to pick up this item.
	 */
	public static SM_SYSTEM_MESSAGE STR_PICKUP_ITEM_FAILED_NOT_MY_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300377);
	}

	/**
	 * You are too far away to pick up the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_PICKUP_ITEM_FAILED_TOO_FAR() {
		return new SM_SYSTEM_MESSAGE(1300378);
	}

	/**
	 * You cannot put down any more items at this place.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_DROP_THE_LOC() {
		return new SM_SYSTEM_MESSAGE(1300379);
	}

	/**
	 * You cannot discard equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_DROP_WORN() {
		return new SM_SYSTEM_MESSAGE(1300380);
	}

	/**
	 * You cannot discard %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNBREAKABLE_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300381, value0);
	}

	/**
	 * %0 is currently refusing to accept items.
	 */
	public static SM_SYSTEM_MESSAGE STR_IS_NOT_WILLING_TO_RECEIVE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300382, value0);
	}

	/**
	 * You cannot give equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_GIVE_WORN() {
		return new SM_SYSTEM_MESSAGE(1300383);
	}

	/**
	 * You are too overburdened to fight.
	 */
	public static SM_SYSTEM_MESSAGE STR_TOO_HEAVY_TO_ATTACK() {
		return new SM_SYSTEM_MESSAGE(1300384);
	}

	/**
	 * You ate %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_EAT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300385, value0);
	}

	/**
	 * You cannot equip %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_EQUIP(String value0) {
		return new SM_SYSTEM_MESSAGE(1300386, value0);
	}

	/**
	 * You do not buy %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SELL_TO_NPC_NO_INTEREST_IN(String value0) {
		return new SM_SYSTEM_MESSAGE(1300387, value0);
	}

	/**
	 * You do not have enough Kinah.
	 */
	public static SM_SYSTEM_MESSAGE STR_NOT_ENOUGH_MONEY() {
		return new SM_SYSTEM_MESSAGE(1300388);
	}

	/**
	 * %0 cannot be discarded.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_CANNOT_BE_DROPPED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300389, value0);
	}

	/**
	 * %0 cannot be given to others.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_CANNOT_BE_GIVEN(String value0) {
		return new SM_SYSTEM_MESSAGE(1300390, value0);
	}

	/**
	 * %0 gave you %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_TO_YOU(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300391, value0, value1);
	}

	/**
	 * That item is limited to one per person, and you already have one in your inventory.
	 */
	public static SM_SYSTEM_MESSAGE STR_CAN_NOT_BUY_LORE_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300392);
	}

	/**
	 * You cannot use %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_USE_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300393, value0);
	}

	/**
	 * You cannot use %0 as you have already used it to its maximum usage count.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_USE_ITEM_OUT_OF_USABLE_COUNT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300394, value0);
	}

	/**
	 * You cannot use %0 to the maximum usage count as the item is currently equipped.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_USE_WORN_ITEM_OUT_OF_USABLE_COUNT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300395, value0);
	}

	/**
	 * You have acquired %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GET_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300396, value0);
	}

	/**
	 * You cannot attack because you have no arrow.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_ATTACK_NO_ARROW() {
		return new SM_SYSTEM_MESSAGE(1300397);
	}

	/**
	 * You do not have a weapon to modify the appearance of.
	 */
	public static SM_SYSTEM_MESSAGE STR_ERROR_CHANGE_WEAPON_SKIN__THERE_IS_NO_WEAPON() {
		return new SM_SYSTEM_MESSAGE(1300398);
	}

	/**
	 * You cannot modify the appearance of the selected item as it is not a weapon.
	 */
	public static SM_SYSTEM_MESSAGE STR_ERROR_CHANGE_WEAPON_SKIN__SELECTED_ITEM_IS_NOT_WEAPON() {
		return new SM_SYSTEM_MESSAGE(1300399);
	}

	/**
	 * You can only modify the appearance of the weapon to another of the same type.
	 */
	public static SM_SYSTEM_MESSAGE STR_ERROR_CHANGE_WEAPON_SKIN__DIFFERENT_WEAPON_TYPE() {
		return new SM_SYSTEM_MESSAGE(1300400);
	}

	/**
	 * You have equipped the Stigma Stone and acquired the %0 skill (Level %1).
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_YOU_CAN_USE_THIS_SKILL_BY_STIGMA_STONE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300401, value0, value1);
	}

	/**
	 * You have removed the Stigma Stone, but you can still use the %0 skill (Level %1) as you are equipped with another stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_AFTER_UNEQUIP_STONE_YOU_CAN_USE_THIS_SKILL_LEVEL_BY_OTHER_STONE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300402, value0, value1);
	}

	/**
	 * You have removed the Stigma Stone and can no longer use the %0 skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_YOU_CANNOT_USE_THIS_SKILL_AFTER_UNEQUIP_STIGMA_STONE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300403, value0);
	}

	/**
	 * You need help from a Stigma Master to equip the Stigma Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_TO_EQUIP_STONE_TALK_WITH_STIGMA_NPC() {
		return new SM_SYSTEM_MESSAGE(1300404);
	}

	/**
	 * You need help from a Stigma Master to remove the Stigma Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_TO_UNEQUIP_STONE_TALK_WITH_STIGMA_NPC() {
		return new SM_SYSTEM_MESSAGE(1300405);
	}

	/**
	 * You need %0 Stigma Shard(s) to equip this Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_CANNT_EQUIP_STONE_OUT_OF_AVAILABLE_STIGMA_POINT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300406, value0);
	}

	/**
	 * You cannot equip that Stigma Stone because you have not learned the %0 skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_CANNT_EQUIP_STONE_YOU_DO_NOT_HAVE_THIS_SKILL(String value0) {
		return new SM_SYSTEM_MESSAGE(1300407, value0);
	}

	/**
	 * There is no Stigma slot available.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_SLOT_IS_NOT_OPENED() {
		return new SM_SYSTEM_MESSAGE(1300408);
	}

	/**
	 * %0 cannot be equipped as its rank exceeds the maximum rank of the Stigma slot.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_TOO_HIGH_STONE_RANK_FOR_SLOT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300409, value0);
	}

	/**
	 * You cannot remove the Stigma Stone because %1 is a prerequisite for the %0th Stigma Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_CANNT_UNEQUIP_STONE_OTHER_STONE_NEED_ITS_SKILL(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300410, value0, value1);
	}

	/**
	 * You have spent %num0sp.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_EXHAUST(int num0sp) {
		return new SM_SYSTEM_MESSAGE(1300411, num0sp);
	}

	/**
	 * %num0sp has been returned to you.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_RETURN(int num0sp) {
		return new SM_SYSTEM_MESSAGE(1300412, num0sp);
	}

	/**
	 * You do not have enough Kinah to equip the Stigma Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_NOT_ENOUGH_MONEY() {
		return new SM_SYSTEM_MESSAGE(1300413);
	}

	/**
	 * You can no longer use the %0 skill acquired through the Stigma Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_NOT_USABLE_SKILL(String value0) {
		return new SM_SYSTEM_MESSAGE(1300414, value0);
	}

	/**
	 * This Stigma Stone cannot be equipped.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_CANNOT_EQUIP_STONE() {
		return new SM_SYSTEM_MESSAGE(1300415);
	}

	/**
	 * You are carrying too many items.
	 */
	public static SM_SYSTEM_MESSAGE STR_WAREHOUSE_TOO_MANY_ITEMS_INVENTORY() {
		return new SM_SYSTEM_MESSAGE(1300416);
	}

	/**
	 * There is no space in the warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_WAREHOUSE_TOO_MANY_ITEMS_WAREHOUSE() {
		return new SM_SYSTEM_MESSAGE(1300417);
	}

	/**
	 * You cannot store this in the warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_WAREHOUSE_CANT_DEPOSIT_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300418);
	}

	/**
	 * You are too far from the NPC.
	 */
	public static SM_SYSTEM_MESSAGE STR_WAREHOUSE_TOO_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300419);
	}

	/**
	 * Equipped items cannot be stored in the warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_WAREHOUSE_DEPOSIT_EQUIPPED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300420);
	}

	/**
	 * There is no space in the warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_WAREHOUSE_DEPOSIT_FULL_BASKET() {
		return new SM_SYSTEM_MESSAGE(1300421);
	}

	/**
	 * You cannot have this item as you already have the limited possession item %0%.
	 */
	public static SM_SYSTEM_MESSAGE STR_CAN_NOT_GET_LORE_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300422, value0);
	}

	/**
	 * You have used %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_USE_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300423, value0);
	}

	/**
	 * %0 has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_BREAK_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300424, value0);
	}

	/**
	 * There are no dropped items nearby.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_NO_DROP_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300425);
	}

	/**
	 * You cannot use that item here.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ITEM_INVALID_LOCATION() {
		return new SM_SYSTEM_MESSAGE(1300426);
	}

	/**
	 * You have cancelled using the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_CANCELED() {
		return new SM_SYSTEM_MESSAGE(1300427);
	}

	/**
	 * The other player is carrying too many items.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_PARTNER_HAS_TOON_MANY_ITEMS_INVENTORY() {
		return new SM_SYSTEM_MESSAGE(1300428);
	}

	/**
	 * There are too many items in the target's trade window. The total number of items will exceed the size of your inventory after trading.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_EXCHANGE_RESULT_WILL_BE_OVER_YOUR_INVENTORY_SIZE() {
		return new SM_SYSTEM_MESSAGE(1300429);
	}

	/**
	 * Your cube cannot be further expanded.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_INVENTORY_CANT_EXTEND_MORE() {
		return new SM_SYSTEM_MESSAGE(1300430);
	}

	/**
	 * %0 spaces have been added to your cube.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_INVENTORY_SIZE_EXTENDED(int value0) {
		return new SM_SYSTEM_MESSAGE(1300431, value0);
	}

	/**
	 * Your private warehouse cannot be further expanded.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_CHAR_WAREHOUSE_CANT_EXTEND_MORE() {
		return new SM_SYSTEM_MESSAGE(1300432);
	}

	/**
	 * %0 spaces have been added to your personal warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_CHAR_WAREHOUSE_SIZE_EXTENDED(int value0) {
		return new SM_SYSTEM_MESSAGE(1300433, value0);
	}

	/**
	 * Your account warehouse cannot be further expanded.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_ACCOUNT_WAREHOUSE_CANT_EXTEND_MORE() {
		return new SM_SYSTEM_MESSAGE(1300434);
	}

	/**
	 * %0 spaces have been added to your account warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_ACCOUNT_WAREHOUSE_SIZE_EXTENDED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300435, value0);
	}

	/**
	 * %0 can only upgrade cubes of level %1 or higher.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_INVENTORY_CANT_EXTEND_DUE_TO_MINIMUM_EXTEND_LEVEL_BY_THIS_NPC(String value0, int level) {
		return new SM_SYSTEM_MESSAGE(1300436, value0, level);
	}

	/**
	 * %0 can only upgrade cubes to level %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_INVENTORY_CANT_EXTEND_MORE_DUE_TO_MAXIMUM_EXTEND_LEVEL_BY_THIS_NPC(String value0, int level) {
		return new SM_SYSTEM_MESSAGE(1300437, value0, level);
	}

	/**
	 * %0 can only upgrade private warehouses of level %1 or higher.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_CHAR_WAREHOUSE_CANT_EXTEND_DUE_TO_MINIMUM_EXTEND_LEVEL_BY_THIS_NPC(String value0, int level) {
		return new SM_SYSTEM_MESSAGE(1300438, value0, level);
	}

	/**
	 * %0 can only upgrade warehouses to level %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_CHAR_WAREHOUSE_CANT_EXTEND_MORE_DUE_TO_MAXIMUM_EXTEND_LEVEL_BY_THIS_NPC(String value0, int level) {
		return new SM_SYSTEM_MESSAGE(1300439, value0, level);
	}

	/**
	 * %0 can only upgrade warehouses of level %1 or higher.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_ACCOUNT_WAREHOUSE_CANT_EXTEND_DUE_TO_MINIMUM_EXTEND_LEVEL_BY_THIS_NPC(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300440, value0, value1);
	}

	/**
	 * %0 can only upgrade warehouses to level %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_ACCOUNT_WAREHOUSE_CANT_EXTEND_MORE_DUE_TO_MAXIMUM_EXTEND_LEVEL_BY_THIS_NPC(String value0,
		String value1) {
		return new SM_SYSTEM_MESSAGE(1300441, value0, value1);
	}

	/**
	 * Your cube cannot be upgraded any further through quests.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTEND_INVENTORY_CANT_EXTEND_MORE_BY_QUEST() {
		return new SM_SYSTEM_MESSAGE(1300442);
	}

	/**
	 * You are too far away to view the inventory.
	 */
	public static SM_SYSTEM_MESSAGE STR_VIEW_OTHER_INVENTORY_TOO_FAR_FROM_TARGET() {
		return new SM_SYSTEM_MESSAGE(1300443);
	}

	/**
	 * The Stigma Stone cannot be removed: All items currently equipped via the skills acquired through this Stigma Stone must be removed first.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_CANNT_UNEQUIP_STONE_FIRST_UNEQUIP_CURRENT_EQUIPPED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300444);
	}

	/**
	 * Cannot find the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_DECOMPOSE_ITEM_NO_TARGET_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300445);
	}

	/**
	 * %0 is not an extractable item.
	 */
	public static SM_SYSTEM_MESSAGE STR_DECOMPOSE_ITEM_IT_CAN_NOT_BE_DECOMPOSED(String num0) {
		return new SM_SYSTEM_MESSAGE(1300446, num0);
	}

	/**
	 * You must have at least one empty space in your cube before you can extract an item.
	 */
	public static SM_SYSTEM_MESSAGE STR_DECOMPOSE_ITEM_INVENTORY_IS_FULL() {
		return new SM_SYSTEM_MESSAGE(1300447);
	}

	/**
	 * You have failed to extract from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_DECOMPOSE_ITEM_FAILED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300448, value0);
	}

	/**
	 * You have successfully extracted from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_DECOMPOSE_ITEM_SUCCEED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300449, value0);
	}

	/**
	 * You have cancelled the extraction from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_DECOMPOSE_ITEM_CANCELED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300450, value0);
	}

	/**
	 * You cannot extract item in %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_DECOMPOSE_ITEM_INVALID_STANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300451, value0);
	}

	/**
	 * Cannot find the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENCHANT_ITEM_NO_TARGET_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300452);
	}

	/**
	 * %0 cannot be enchanted.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENCHANT_ITEM_IT_CAN_NOT_BE_ENCHANTED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300453, value0);
	}

	/**
	 * %0 cannot be enchanted any more.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENCHANT_ITEM_IT_CAN_NOT_BE_ENCHANTED_MORE_TIME(String value0) {
		return new SM_SYSTEM_MESSAGE(1300454, value0);
	}

	/**
	 * You have successfully enchanted %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENCHANT_ITEM_SUCCEED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300455, value0);
	}

	/**
	 * You have failed to enchant %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENCHANT_ITEM_FAILED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300456, value0);
	}

	/**
	 * You have cancelled the enchanting of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENCHANT_ITEM_CANCELED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300457, value0);
	}

	/**
	 * You cannot enchant items in %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENCHANT_ITEM_INVALID_STANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300458, value0);
	}

	/**
	 * Cannot find the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_OPTION_NO_TARGET_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300459);
	}

	/**
	 * %0 cannot be enchanted with %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_OPTION_IT_CAN_NOT_BE_GIVEN_OPTION(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300460, value0, value1);
	}

	/**
	 * %0 cannot be enchanted with %1 any more.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_OPTION_IT_CAN_NOT_BE_GIVEN_OPTION_MORE_TIME(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300461, value0, value1);
	}

	/**
	 * You have succeeded in the manastone socketing of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_OPTION_SUCCEED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300462, value0);
	}

	/**
	 * You have failed in the manastone socketing of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_OPTION_FAILED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300463, value0);
	}

	/**
	 * You have cancelled the manastone socketing of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_OPTION_CANCELED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300464, value0);
	}

	/**
	 * All manastones that were socketed in %0 have disappeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_OPTION_ALL_OPTION_REMOVED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300465, value0);
	}

	/**
	 * You cannot socket manastones while %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_OPTION_INVALID_STANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300466, value0);
	}

	/**
	 * You are too far from the NPC to remove the manastone.
	 */
	public static SM_SYSTEM_MESSAGE STR_REMOVE_ITEM_OPTION_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300467);
	}

	/**
	 * Cannot find the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_REMOVE_ITEM_OPTION_NO_TARGET_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300468);
	}

	/**
	 * Manastone socketing / removal is not possible for the item %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_REMOVE_ITEM_OPTION_IT_CAN_NOT_BE_GIVEN_OPTION(String value0) {
		return new SM_SYSTEM_MESSAGE(1300469, value0);
	}

	/**
	 * %0 is not socketed with a manastone.
	 */
	public static SM_SYSTEM_MESSAGE STR_REMOVE_ITEM_OPTION_NO_OPTION_TO_REMOVE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300470, value0);
	}

	/**
	 * The target slot on %0 is not socketed with a manastone.
	 */
	public static SM_SYSTEM_MESSAGE STR_REMOVE_ITEM_OPTION_INVALID_OPTION_SLOT_NUMBER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300471, value0);
	}

	/**
	 * You do not have enough Kinah to remove the manastone from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_REMOVE_ITEM_OPTION_NOT_ENOUGH_GOLD(String value0) {
		return new SM_SYSTEM_MESSAGE(1300472, value0);
	}

	/**
	 * You have removed the manastone from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_REMOVE_ITEM_OPTION_SUCCEED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300473, value0);
	}

	/**
	 * You cannot remove manastones from items in %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_REMOVE_ITEM_OPTION_INVALID_STANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300474, value0);
	}

	/**
	 * You are too far from the NPC to modify the appearance of the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHANGE_ITEM_SKIN_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300475);
	}

	/**
	 * You must be at least level 10 before you can modify the appearance of items.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHANGE_ITEM_SKIN_PC_LEVEL_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1300476);
	}

	/**
	 * Cannot find the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHANGE_ITEM_SKIN_NO_TARGET_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300477);
	}

	/**
	 * The appearance of %0 cannot be modified.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHANGE_ITEM_SKIN_NOT_SKIN_CHANGABLE_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300478, value0);
	}

	/**
	 * The appearance of %0 cannot be modified into %1 and vice versa as they are different types of item.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHANGE_ITEM_SKIN_NOT_SAME_EQUIP_SLOT(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300479, value0, value1);
	}

	/**
	 * The appearance of %0 cannot be modified into %1 and vice versa as they are different type of items.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHANGE_ITEM_SKIN_NOT_COMPATIBLE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300480, value0, value1);
	}

	/**
	 * You do not have enough Kinah to modify the appearance of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHANGE_ITEM_SKIN_NOT_ENOUGH_GOLD(String value0) {
		return new SM_SYSTEM_MESSAGE(1300481, value0);
	}

	/**
	 * You have failed to modify the appearance of the item as you could not remove the skin item %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHANGE_ITEM_SKIN_CAN_NOT_REMOVE_SKIN_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300482, value0);
	}

	/**
	 * You have modified the appearance of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHANGE_ITEM_SKIN_SUCCEED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300483, value0);
	}

	/**
	 * You cannot modify the appearance of items in %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHANGE_ITEM_SKIN_INVALID_STANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300484, value0);
	}

	/**
	 * You have successfully soul-bound %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SOUL_BOUND_ITEM_SUCCEED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300485, value0);
	}

	/**
	 * You have failed to soul-bind %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SOUL_BOUND_ITEM_FAILED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300486, value0);
	}

	/**
	 * You cancelled the soul-binding of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SOUL_BOUND_ITEM_CANCELED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300487, value0);
	}

	/**
	 * Please try the soul-binding again after you have closed other input boxes.
	 */
	public static SM_SYSTEM_MESSAGE STR_SOUL_BOUND_CLOSE_OTHER_MSG_BOX_AND_RETRY() {
		return new SM_SYSTEM_MESSAGE(1300488);
	}

	/**
	 * You cannot soul-bind an item while %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SOUL_BOUND_INVALID_STANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300489, value0);
	}

	/**
	 * You do not have a Power Shard equipped.
	 */
	public static SM_SYSTEM_MESSAGE STR_WEAPON_BOOST_NO_BOOSTER_EQUIPED() {
		return new SM_SYSTEM_MESSAGE(1300490);
	}

	/**
	 * You activate the Power Shard.
	 */
	public static SM_SYSTEM_MESSAGE STR_WEAPON_BOOST_BOOST_MODE_STARTED() {
		return new SM_SYSTEM_MESSAGE(1300491);
	}

	/**
	 * You deactivate the Power Shard.
	 */
	public static SM_SYSTEM_MESSAGE STR_WEAPON_BOOST_BOOST_MODE_ENDED() {
		return new SM_SYSTEM_MESSAGE(1300492);
	}

	/**
	 * You cannot use the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_IS_NOT_USABLE() {
		return new SM_SYSTEM_MESSAGE(1300493);
	}

	/**
	 * You cannot use the item as its cooldown time has not expired yet.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_CANT_USE_UNTIL_DELAY_TIME() {
		return new SM_SYSTEM_MESSAGE(1300494);
	}

	/**
	 * You must be next to a postbox and click it to use the post service.
	 */
	public static SM_SYSTEM_MESSAGE STR_MAIL_CLICK_POSTBOX_TO_USE() {
		return new SM_SYSTEM_MESSAGE(1300495);
	}

	/**
	 * You cannot mail equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MAIL_SEND_CAN_NOT_SEND_EQUIPPED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300496);
	}

	/**
	 * You cannot mail items that are not tradable.
	 */
	public static SM_SYSTEM_MESSAGE STR_MAIL_SEND_ITEM_CAN_NOT_BE_EXCHANGED() {
		return new SM_SYSTEM_MESSAGE(1300497);
	}

	/**
	 * You cannot mail items that you have already used.
	 */
	public static SM_SYSTEM_MESSAGE STR_MAIL_SEND_USED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300498);
	}

	/**
	 * You cannot register any more items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MAIL_SEND_FULL_BASKET() {
		return new SM_SYSTEM_MESSAGE(1300499);
	}

	/**
	 * The selected NPC cannot add ability to the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_GIVE_ITEM_PROC_CANT_GIVE_PROC_BY_THIS_NPC() {
		return new SM_SYSTEM_MESSAGE(1300500);
	}

	/**
	 * You are too far from the NPC to add abilities to the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_GIVE_ITEM_PROC_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300501);
	}

	/**
	 * Failed to find the target item to add the ability to.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_PROC_NO_TARGET_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300502);
	}

	/**
	 * You cannot add an ability to equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_PROC_CANNOT_GIVE_PROC_TO_EQUIPPED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300503);
	}

	/**
	 * %0 is not an item you can add ability to.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_PROC_NOT_PROC_GIVABLE_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300504, value0);
	}

	/**
	 * Cannot find the item to add ability to.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_PROC_NO_PROC_GIVE_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300505);
	}

	/**
	 * %0 is not an item you can add ability to.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_PROC_NOT_PROC_GIVE_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300506, value0);
	}

	/**
	 * You do not have enough Kinah to add ability to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_PROC_NOT_ENOUGH_MONEY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300507, value0);
	}

	/**
	 * You have successfully added ability to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_PROC_ENCHANTED_TARGET_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300508, value0);
	}

	/**
	 * You cannot socket godstones while %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_PROC_INVALID_STANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300509, value0);
	}

	/**
	 * You have removed the dye from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_COLOR_REMOVE_SUCCEED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300510, value0);
	}

	/**
	 * You have dyed %0 %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_COLOR_CHANGE_SUCCEED(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300511, value0, value1);
	}

	/**
	 * %0 cannot be dyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_COLOR_CHANGE_ERROR_CANNOTDYE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300512, value0);
	}

	/**
	 * The item has not been dyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_COLOR_REMOVE_ERROR_CANNOTREMOVE() {
		return new SM_SYSTEM_MESSAGE(1300513);
	}

	/**
	 * The item cannot be found.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_COLOR_ERROR() {
		return new SM_SYSTEM_MESSAGE(1300514);
	}

	/**
	 * You cannot dye equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_COLOR_CANNOT_CHANGE_EQUIPPED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300515);
	}

	/**
	 * The target is busy and cannot trade at the moment.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXCHANGE_START_OHER_IS_BUSY() {
		return new SM_SYSTEM_MESSAGE(1300516);
	}

	/**
	 * %0's Reputation has increased by %1 point(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_POINTUP(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300517, value0, value1);
	}

	/**
	 * %0's Reputation has fallen by %1 point(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_POINTDOWN(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300518, value0, value1);
	}

	/**
	 * %0's Reputation has become Hostile.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_HOSTIL(String value0) {
		return new SM_SYSTEM_MESSAGE(1300519, value0);
	}

	/**
	 * %0's Reputation has become Confrontational.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_OPPOSITE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300520, value0);
	}

	/**
	 * %0's Reputation has become Neutral.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_NEUTRAL(String value0) {
		return new SM_SYSTEM_MESSAGE(1300521, value0);
	}

	/**
	 * %0's Reputation has become Friendly.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_FREINDSHIP(String value0) {
		return new SM_SYSTEM_MESSAGE(1300522, value0);
	}

	/**
	 * %0's Reputation has become Alliance mode.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_ALLY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300523, value0);
	}

	/**
	 * Congratulations! You have joined %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_JOIN(String value0) {
		return new SM_SYSTEM_MESSAGE(1300524, value0);
	}

	/**
	 * You are already a member.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_CAN_NOT_JOIN() {
		return new SM_SYSTEM_MESSAGE(1300525);
	}

	/**
	 * You have left %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_LEAVE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300526, value0);
	}

	/**
	 * The Jeridises
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_ZERIDITH() {
		return new SM_SYSTEM_MESSAGE(1300527);
	}

	/**
	 * The Brugons
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_BRUGON() {
		return new SM_SYSTEM_MESSAGE(1300528);
	}

	/**
	 * The Timoris
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_TIMORITH() {
		return new SM_SYSTEM_MESSAGE(1300529);
	}

	/**
	 * %num0:%num1:%num2
	 */
	public static SM_SYSTEM_MESSAGE STR_HOUR_MIN_SEC(byte num0, byte num1, byte num2) {
		return new SM_SYSTEM_MESSAGE(1300530, num0, num1, num2);
	}

	/**
	 * %num0:%num1
	 */
	public static SM_SYSTEM_MESSAGE STR_HOUR_MIN(byte num0, byte num1) {
		return new SM_SYSTEM_MESSAGE(1300531, num0, num1);
	}

	/**
	 * %num0:0:%num1
	 */
	public static SM_SYSTEM_MESSAGE STR_HOUR_0MIN_SEC(byte num0, byte num1) {
		return new SM_SYSTEM_MESSAGE(1300532, num0, num1);
	}

	/**
	 * %num0h
	 */
	public static SM_SYSTEM_MESSAGE STR_HOUR(byte num0) {
		return new SM_SYSTEM_MESSAGE(1300533, num0);
	}

	/**
	 * %num0:%num1
	 */
	public static SM_SYSTEM_MESSAGE STR_MIN_SEC(byte num0, byte num1) {
		return new SM_SYSTEM_MESSAGE(1300534, num0, num1);
	}

	/**
	 * %num0m
	 */
	public static SM_SYSTEM_MESSAGE STR_MIN(byte num0) {
		return new SM_SYSTEM_MESSAGE(1300535, num0);
	}

	/**
	 * %num0s
	 */
	public static SM_SYSTEM_MESSAGE STR_SEC(byte num0) {
		return new SM_SYSTEM_MESSAGE(1300536, num0);
	}

	/**
	 * Please visit http://support.en.aionfreetoplay.com for customer support.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_VISIT_WEB() {
		return new SM_SYSTEM_MESSAGE(1300537);
	}

	/**
	 * The petition is too short. A Support Petition must be at least 5 words in length.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PETITION_TOO_SHORT() {
		return new SM_SYSTEM_MESSAGE(1300538);
	}

	/**
	 * The Support Petition has been received. The receipt number is %num0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PETITION_RECEIVED(int num0) {
		return new SM_SYSTEM_MESSAGE(1300539, num0);
	}

	/**
	 * This is your %num0th petition. You may make %num1 more Support Petitions today.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PETITION_COUNT(int num0, int num1) {
		return new SM_SYSTEM_MESSAGE(1300540, num0, num1);
	}

	/**
	 * There are %num0 users waiting in the queue to lodge Support Petitions.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PETITION_QUEUE(int num0) {
		return new SM_SYSTEM_MESSAGE(1300541, num0);
	}

	/**
	 * %value0 has received a request from the GM for consultation.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_HE_RECEIVED_CONVERSATION_REQUEST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300542, value0);
	}

	/**
	 * %value0 has received a proxy petition generated by the GM. The petition number is %num1.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_HE_RECEIVED_PETITION(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1300543, value0, num1);
	}

	/**
	 * A proxy petition by the GM has been received, but the user is offline. The petition number is %num0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_HE_RECEIVED_PETITION_OFFLINE(int num0) {
		return new SM_SYSTEM_MESSAGE(1300544, num0);
	}

	/**
	 * Please visit http://support.en.aionfreetoplay.com for customer support.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_VISIT_WEB2() {
		return new SM_SYSTEM_MESSAGE(1300545);
	}

	/**
	 * Your support request has failed. Please try again later.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_REQUEST_FAILED() {
		return new SM_SYSTEM_MESSAGE(1300546);
	}

	/**
	 * You have used up your daily quota of %num0 Support Petitions. You cannot make any more inquiries with this account today.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_QUOTA_REACHED(byte num0) {
		return new SM_SYSTEM_MESSAGE(1300547, num0);
	}

	/**
	 * A Support Petition has already been received. Please wait for a reply.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PETITION_ALREADY_RECEIVED() {
		return new SM_SYSTEM_MESSAGE(1300548);
	}

	/**
	 * Your proxy petition request has failed. %value0 has already received the Support Petition.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PROXY_PETITION_ALREADY_RECEIVED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300549, value0);
	}

	/**
	 * Your proxy petition request for %value0 has failed. The error code is %num1.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PROXY_PETITION_REQUEST_FAILED(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1300550, value0, num1);
	}

	/**
	 * The request for a proxy petition has failed. (The user is currently offline.) The error code is %num0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PROXY_PETITION_REQUEST_FAILED_OFFLINE(int num0) {
		return new SM_SYSTEM_MESSAGE(1300551, num0);
	}

	/**
	 * Petition No. %num0 has been cancelled.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PETITION_CANCELLED(int num0) {
		return new SM_SYSTEM_MESSAGE(1300552, num0);
	}

	/**
	 * The petition has been cancelled. You have %num0 Support Petitions left for today.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PETITION_CANCELLED2(int num0) {
		return new SM_SYSTEM_MESSAGE(1300553, num0);
	}

	/**
	 * You cancelled the proxy petition request for %value0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PROXY_PETITION_CANCELLED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300554, value0);
	}

	/**
	 * Failed to cancel the petition. Please try again later.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PETITION_CANCEL_FAILED() {
		return new SM_SYSTEM_MESSAGE(1300555);
	}

	/**
	 * The support petition is already being processed.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PETITION_ALREADY_IN_PROCESS() {
		return new SM_SYSTEM_MESSAGE(1300556);
	}

	/**
	 * Support Petitions cannot be submitted at the moment.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PETITION_NOT_SUBMITTABLE() {
		return new SM_SYSTEM_MESSAGE(1300557);
	}

	/**
	 * Failed to cancel the request for a proxy petition to %value0. The error code is %num1.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_PROXY_PETITION_REQUEST_CANCEL_FAIL(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1300558, value0, num1);
	}

	/**
	 * The User (%value0) is not in the game server.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_USER_NOT_IN_GAME(String value0) {
		return new SM_SYSTEM_MESSAGE(1300559, value0);
	}

	/**
	 * Your consultation with the GM (%value0) has started.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_CONVERSATION_START(String value0) {
		return new SM_SYSTEM_MESSAGE(1300560, value0);
	}

	/**
	 * The GM (%value0)'s response is complete. Please evaluate the Support Petition service in a moment.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_CONVERSATION_RESPONSE_COMPLETE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300561, value0);
	}

	/**
	 * You are not in a consultation with the GM.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_CONVERSATION_NOT_ACTIVE() {
		return new SM_SYSTEM_MESSAGE(1300562);
	}

	/**
	 * An error has occurred while transmitting the conversation log to the GM. Please try again later.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_CONVERSATION_ERROR() {
		return new SM_SYSTEM_MESSAGE(1300563);
	}

	/**
	 * $value0: $value1
	 */
	public static SM_SYSTEM_MESSAGE STR_SUPPORT_MESSAGE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300564, value0, value1);
	}

	/**
	 * This is a message from the GM: %value0
	 */
	public static SM_SYSTEM_MESSAGE STR_MESSAGE_FROM_GM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300565, value0);
	}

	/**
	 * Only the group leader can receive this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_PARTY_LEADER_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300566);
	}

	/**
	 * You can only receive this quest when your group has %0 or more members.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_PARTY_SIZE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300567, value0);
	}

	/**
	 * Only the Legion Brigade General can receive the quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_GUILD_MASTER_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300568);
	}

	/**
	 * You can only receive this quest when the level of your Legion is %0 or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_GUILD_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1300569, value0);
	}

	/**
	 * You can only receive this quest when the Legion Point of your Legion is %num0 or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_GUILD_EXP(int num0) {
		return new SM_SYSTEM_MESSAGE(1300570, num0);
	}

	/**
	 * You can only receive this quest when your level is %0 or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_MIN_LEVEL(int num0) {
		return new SM_SYSTEM_MESSAGE(1300571, num0);
	}

	/**
	 * You can only receive this quest when your level is %0 or below.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_MAX_LEVEL(int num0) {
		return new SM_SYSTEM_MESSAGE(1300572, num0);
	}

	/**
	 * You can only receive this quest when your rank is %0 or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_MIN_RANK(String value0) {
		return new SM_SYSTEM_MESSAGE(1300573, value0);
	}

	/**
	 * You can only receive this quest when your production job rank is %0 or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_TS_RANK(String value0) {
		return new SM_SYSTEM_MESSAGE(1300574, value0);
	}

	/**
	 * Your race cannot receive this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_RACE() {
		return new SM_SYSTEM_MESSAGE(1300575);
	}

	/**
	 * Your nationality prevents you from receiving this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_NATION() {
		return new SM_SYSTEM_MESSAGE(1300576);
	}

	/**
	 * Only males can receive this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_MAN_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300577);
	}

	/**
	 * Only females can receive this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_WOMAN_ONLY() {
		return new SM_SYSTEM_MESSAGE(1300578);
	}

	/**
	 * Your gender prevents you from receiving this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_GENDER() {
		return new SM_SYSTEM_MESSAGE(1300579);
	}

	/**
	 * Your class prevents you from receiving this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_CLASS() {
		return new SM_SYSTEM_MESSAGE(1300580);
	}

	/**
	 * You must have the %0 voice to receive this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_VOICE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300581, value0);
	}

	/**
	 * You can only receive this quest when you need %num0 or less XP to reach the next level.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_EXP_TO_NEXT_LEVEL(int num0) {
		return new SM_SYSTEM_MESSAGE(1300582, num0);
	}

	/**
	 * You can only receive this quest when your Stigma Point is %num0 or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_STIGMA_PT(int num0) {
		return new SM_SYSTEM_MESSAGE(1300583, num0);
	}

	/**
	 * You can only receive this quest when your PVP point is %num0 or more.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_PVP_PT(int num0) {
		return new SM_SYSTEM_MESSAGE(1300584, num0);
	}

	/**
	 * You can only receive this quest when %0's favor toward you is %num1 or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_FAVOR(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1300585, value0, num1);
	}

	/**
	 * You can only receive this quest when your Faction with %0 is %num1 or higher.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_FACTION(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1300586, value0, num1);
	}

	/**
	 * You can only receive this quest when your National Contribute Point is %num0 or more.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_NATION_PT(int num0) {
		return new SM_SYSTEM_MESSAGE(1300587, num0);
	}

	/**
	 * You can only receive this quest when you have the %0 title.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_TITLE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300588, value0);
	}

	/**
	 * You can only receive this quest when you have the %0 skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_SKILL(String value0) {
		return new SM_SYSTEM_MESSAGE(1300589, value0);
	}

	/**
	 * You can only accept this quest when you have level %1 %0 skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_SKILL_LEVEL(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1300590, value1, value0);
	}

	/**
	 * You are not in the altered state required to receive this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_ABNORMAL_STATUS() {
		return new SM_SYSTEM_MESSAGE(1300591);
	}

	/**
	 * You must have played for a total of at least %num0 hours to receive this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_PLAY_TIME(int num0) {
		return new SM_SYSTEM_MESSAGE(1300592, num0);
	}

	/**
	 * You can only receive this quest when you are equipped with %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_EQUIP_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300593, value0);
	}

	/**
	 * You can only receive this quest when you have %0 in your inventory.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_INVENTORY_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300594, value0);
	}

	/**
	 * You must be a member of the %0 NPC Legion to receive this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_NPC_GUILD(String value0) {
		return new SM_SYSTEM_MESSAGE(1300595, value0);
	}

	/**
	 * You can only receive this quest when you have completed the %0 quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_FINISHED_QUEST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300596, value0);
	}

	/**
	 * You cannot receive a quest that you are already working on.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_WORKING_QUEST() {
		return new SM_SYSTEM_MESSAGE(1300597);
	}

	/**
	 * You cannot receive quests while you are dead.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_DIE() {
		return new SM_SYSTEM_MESSAGE(1300598);
	}

	/**
	 * You can do the %0 quest only once.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_NONE_REPEATABLE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300599, value0);
	}

	/**
	 * You can do the %0 quest only %1 times.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_MAX_REPEAT_COUNT(String value0, int value1) {
		return new SM_SYSTEM_MESSAGE(1300600, value0, value1);
	}

	/**
	 * You cannot get a quest reward while you are dead.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_GET_REWARD_ERROR_DEAD() {
		return new SM_SYSTEM_MESSAGE(1300601);
	}

	/**
	 * You cannot get the quest reward as you don't have %0 %0s.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_GET_REWARD_ERROR_NO_QUEST_ITEM_SINGLE(String value0, String value0s) {
		return new SM_SYSTEM_MESSAGE(1300602, value0, value0s);
	}

	/**
	 * You cannot receive the quest reward as you do not have %1 %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_GET_REWARD_ERROR_NO_QUEST_ITEM_MULTIPLE(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1300603, value1, value0);
	}

	/**
	 * You cannot destroy %0 because it is used in the "%1" quest which cannot be abandoned once started.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_GIVEUP_WHEN_DELETE_QUEST_ITEM_IMPOSSIBLE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300604, value0);
	}

	/**
	 * Please try destroying the quest item again after you have closed other dialog boxes.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_GIVEUP_WHEN_DELETE_QUEST_ITEM_RETRY() {
		return new SM_SYSTEM_MESSAGE(1300605);
	}

	/**
	 * No Quest selected
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_NO_QUEST() {
		return new SM_SYSTEM_MESSAGE(1300606);
	}

	/**
	 * Quest Indicator
	 */
	public static SM_SYSTEM_MESSAGE STR_QUIEST_INDICATOR() {
		return new SM_SYSTEM_MESSAGE(1300607);
	}

	/**
	 * Quest
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_SYSTEMMSG_QUEST() {
		return new SM_SYSTEM_MESSAGE(1300608);
	}

	/**
	 * %1[acquire]%2 %0
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_QIMSG_ACQUIRE() {
		return new SM_SYSTEM_MESSAGE(1300609);
	}

	/**
	 * %1[fail]%2 %0
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_QIMSG_GIVEUP() {
		return new SM_SYSTEM_MESSAGE(1300610);
	}

	/**
	 * %1[update]%2 %0
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_QIMSG_UPDATE() {
		return new SM_SYSTEM_MESSAGE(1300611);
	}

	/**
	 * %1[complete]%2 %0
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_QIMSG_COMPLETE() {
		return new SM_SYSTEM_MESSAGE(1300612);
	}

	/**
	 * Quest acquired: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_SYSTEMMSG_ACQUIRE_QUEST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300613, value0);
	}

	/**
	 * Quest failed: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_SYSTEMMSG_GIVEUP_QUEST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300614, value0);
	}

	/**
	 * Quest updated: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_SYSTEMMSG_UPDATE_QUEST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300615, value0);
	}

	/**
	 * Quest complete: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_SYSTEMMSG_COMPLETE_QUEST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300616, value0);
	}

	/**
	 * Quest acquired: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_SYSTEMMSG_ACQUIRE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300617, value0);
	}

	/**
	 * Quest failed: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_SYSTEMMSG_GIVEUP(String value0) {
		return new SM_SYSTEM_MESSAGE(1300618, value0);
	}

	/**
	 * Quest updated: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_SYSTEMMSG_UPDATE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300619, value0);
	}

	/**
	 * Quest complete: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_SYSTEMMSG_COMPLETE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300620, value0);
	}

	/**
	 * You cannot learn this design.
	 */
	public static SM_SYSTEM_MESSAGE STR_RECIPEITEM_CANT_USE_NO_RECIPE() {
		return new SM_SYSTEM_MESSAGE(1300621);
	}

	/**
	 * You cannot receive any more quests.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ACQUIRE_ERROR_MAX_NORMAL() {
		return new SM_SYSTEM_MESSAGE(1300622);
	}

	/**
	 * %0 has been banned.
	 */
	public static SM_SYSTEM_MESSAGE STR_USER_BANNED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300623, value0);
	}

	/**
	 * %0 has been disconnected from the server.
	 */
	public static SM_SYSTEM_MESSAGE STR_USER_KICKED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300624, value0);
	}

	/**
	 * There is no user named %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_NO_USER_NAMED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300625, value0);
	}

	/**
	 * There is going to be an important announcement from the GM. Please be patient for a while.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_CHAT_DURING_NOTIFICATION() {
		return new SM_SYSTEM_MESSAGE(1300626);
	}

	/**
	 * %0 is not playing the game.
	 */
	public static SM_SYSTEM_MESSAGE STR_NO_SUCH_USER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300627, value0);
	}

	/**
	 * %0 has blocked you.
	 */
	public static SM_SYSTEM_MESSAGE STR_YOU_EXCLUDED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300628, value0);
	}

	/**
	 * %0 is currently not accepting any Whispers.
	 */
	public static SM_SYSTEM_MESSAGE STR_WHISPER_REFUSE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300629, value0);
	}

	/**
	 * Nothing happened.
	 */
	public static SM_SYSTEM_MESSAGE STR_NOTHING_HAPPEN() {
		return new SM_SYSTEM_MESSAGE(1300630);
	}

	/**
	 * You cannot use teleport here.
	 */
	public static SM_SYSTEM_MESSAGE STR_NO_TELEPORT() {
		return new SM_SYSTEM_MESSAGE(1300631);
	}

	/**
	 * You have unblocked %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_ONE_INCLUDED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300632, value0);
	}

	/**
	 * You have blocked %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_ONE_EXCLUDED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300633, value0);
	}

	/**
	 * Blocked users: %num0 users
	 */
	public static SM_SYSTEM_MESSAGE STR_CURRENT_EXCLUDES(int num0) {
		return new SM_SYSTEM_MESSAGE(1300634, num0);
	}

	/**
	 * There are no blocked users.
	 */
	public static SM_SYSTEM_MESSAGE STR_NO_EXCLUDES() {
		return new SM_SYSTEM_MESSAGE(1300635);
	}

	/**
	 * You have excluded %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_ONE_NO_LONGER_INCLUDED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300636, value0);
	}

	/**
	 * You have listed %0 as a friend.
	 */
	public static SM_SYSTEM_MESSAGE STR_ONE_IS_INCLUDED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300637, value0);
	}

	/**
	 * You have too many users listed as friends.
	 */
	public static SM_SYSTEM_MESSAGE STR_TOO_MANY_INCLUDE() {
		return new SM_SYSTEM_MESSAGE(1300638);
	}

	/**
	 * Users listed as friends: %num0 users
	 */
	public static SM_SYSTEM_MESSAGE STR_CURRENT_INCLUDES(int num0) {
		return new SM_SYSTEM_MESSAGE(1300639, num0);
	}

	/**
	 * You have no chat friends.
	 */
	public static SM_SYSTEM_MESSAGE STR_NO_INCLUDES() {
		return new SM_SYSTEM_MESSAGE(1300640);
	}

	/**
	 * Current users: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_LIST_USER(int num0) {
		return new SM_SYSTEM_MESSAGE(1300641, num0);
	}

	/**
	 * Current users: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_LIST_USER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300641, value0);
	}

	/**
	 * The server is due to shut down in %0 seconds. Please quit the game.
	 */
	public static SM_SYSTEM_MESSAGE STR_SERVER_SHUTDOWN(int num0) {
		return new SM_SYSTEM_MESSAGE(1300642, num0);
	}

	/**
	 * Please do not flood chat. Blocked for %0m.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_DISABLED_FOR(int num0) {
		return new SM_SYSTEM_MESSAGE(1300643, num0);
	}

	/**
	 * You may now chat again.
	 */
	public static SM_SYSTEM_MESSAGE STR_CAN_CHAT_NOW() {
		return new SM_SYSTEM_MESSAGE(1300644);
	}

	/**
	 * Please do not flood chat. Blocked for a short while.
	 */
	public static SM_SYSTEM_MESSAGE STR_GLOBAL_CHAT_DISABLED_FOR() {
		return new SM_SYSTEM_MESSAGE(1300645);
	}

	/**
	 * You cannot create a general channel at your discretion.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_MAKE_GENERALCHANNEL() {
		return new SM_SYSTEM_MESSAGE(1300646);
	}

	/**
	 * %0 has already been sold.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_SOLD_OUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300647, value0);
	}

	/**
	 * You do not have enough Kinah to pay the fee.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_NOT_ENOUGH_FEE() {
		return new SM_SYSTEM_MESSAGE(1300648);
	}

	/**
	 * You cannot register any more items as there is no space available.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_FULL_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300649);
	}

	/**
	 * You cannot register items that have already been used.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_REGISTER_USED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300650);
	}

	/**
	 * You cannot register equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_REGISTER_EQUIPPED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300651);
	}

	/**
	 * You cannot list an untradeable item.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_REGISTER_CANNOT_BE_EXCHANGED() {
		return new SM_SYSTEM_MESSAGE(1300652);
	}

	/**
	 * You cannot register any more items.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_REGISTER_FULL_BASKET() {
		return new SM_SYSTEM_MESSAGE(1300653);
	}

	/**
	 * You cannot continue trading as your inventory is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_FULL_INVENTORY() {
		return new SM_SYSTEM_MESSAGE(1300654);
	}

	/**
	 * You cannot register this item.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_CAN_NOT_REGISTER_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300655);
	}

	/**
	 * This item is already registered.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_ALREADY_REGISTERED() {
		return new SM_SYSTEM_MESSAGE(1300656);
	}

	/**
	 * Items for Sale! The best value around!
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_DEFAULT_ADVERTISE_MSG() {
		return new SM_SYSTEM_MESSAGE(1300657);
	}

	/**
	 * You start doing business at your private store.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_START() {
		return new SM_SYSTEM_MESSAGE(1300658);
	}

	/**
	 * You stop doing business at your private store.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_END() {
		return new SM_SYSTEM_MESSAGE(1300659);
	}

	/**
	 * You cannot sell equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_CAN_NOT_SELL_EQUIPED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300660);
	}

	/**
	 * You cannot sell items that cannot be traded with other users.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_CANNOT_BE_EXCHANGED() {
		return new SM_SYSTEM_MESSAGE(1300661);
	}

	/**
	 * You cannot sell used items.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_CAN_NOT_SELL_USED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300662);
	}

	/**
	 * You cannot open a private store while fighting.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_DISABLED_IN_COMBAT_MODE() {
		return new SM_SYSTEM_MESSAGE(1300663);
	}

	/**
	 * As you cannot open a private store while fighting, it will be closed automatically.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_CLOSED_FOR_COMBAT_MODE() {
		return new SM_SYSTEM_MESSAGE(1300664);
	}

	/**
	 * %0 has already been sold.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_SOLD_OUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300665, value0);
	}

	/**
	 * You cannot register any more items.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_FULL_BASKET() {
		return new SM_SYSTEM_MESSAGE(1300666);
	}

	/**
	 * You have not opened Quickbar No.2.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUICKBAR_NOT_OPEN_SECONDBAR() {
		return new SM_SYSTEM_MESSAGE(1300667);
	}

	/**
	 * You have not opened Quickbar No.3.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUICKBAR_NOT_OPEN_THIRDBAR() {
		return new SM_SYSTEM_MESSAGE(1300668);
	}

	/**
	 * As there is no registered bind point, you will resurrect in the city.
	 */
	public static SM_SYSTEM_MESSAGE STR_DEATH_NOT_REGISTERED_RESURRECT_POINT() {
		return new SM_SYSTEM_MESSAGE(1300669);
	}

	/**
	 * You are now bound at [%subzone].
	 */
	public static SM_SYSTEM_MESSAGE STR_DEATH_REGISTER_RESURRECT_POINT() {
		return new SM_SYSTEM_MESSAGE(1300670);
	}

	/**
	 * Please try again after you have closed other dialog boxes.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_ASK_RECOVER_EXPERIENCE_BY_OTHER_QUESTION() {
		return new SM_SYSTEM_MESSAGE(1300671);
	}

	/**
	 * You do not have enough Kinah to recover your XP.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_RECOVER_EXPERIENCE_NOT_ENOUGH_FEE() {
		return new SM_SYSTEM_MESSAGE(1300672);
	}

	/**
	 * You are too far from a healer to receive Soul Healing.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_RECOVER_EXPERIENCE_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300673);
	}

	/**
	 * You received Soul Healing.
	 */
	public static SM_SYSTEM_MESSAGE STR_SUCCESS_RECOVER_EXPERIENCE() {
		return new SM_SYSTEM_MESSAGE(1300674);
	}

	/**
	 * You are too far to change the PVP zone.
	 */
	public static SM_SYSTEM_MESSAGE STR_PVPZONE_CANNOT_MOVE_PVPZONE_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300675);
	}

	/**
	 * You cannot change the PVP zone because you have no means to move.
	 */
	public static SM_SYSTEM_MESSAGE STR_PVPZONE_CANNOT_MOVE_PVPZONE_NPC_NOT_CORRECT() {
		return new SM_SYSTEM_MESSAGE(1300676);
	}

	/**
	 * The target is invalid. Please select a player.
	 */
	public static SM_SYSTEM_MESSAGE STR_ASSISTKEY_INCORRECT_TARGET() {
		return new SM_SYSTEM_MESSAGE(1300677);
	}

	/**
	 * The person you want to assist does not have a target.
	 */
	public static SM_SYSTEM_MESSAGE STR_ASSISTKEY_NO_USER() {
		return new SM_SYSTEM_MESSAGE(1300678);
	}

	/**
	 * The person you want to assist is too far from the target.
	 */
	public static SM_SYSTEM_MESSAGE STR_ASSISTKEY_TOO_FAR() {
		return new SM_SYSTEM_MESSAGE(1300679);
	}

	/**
	 * You are assisting the target %0 has selected.
	 */
	public static SM_SYSTEM_MESSAGE STR_ASSISTKEY_ASSIST_FOR_SOMEONE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300680, value0);
	}

	/**
	 * The map is now loading.
	 */
	public static SM_SYSTEM_MESSAGE STR_WORLDMAP_INFO() {
		return new SM_SYSTEM_MESSAGE(1300681);
	}

	/**
	 * You do not have any XP to recover.
	 */
	public static SM_SYSTEM_MESSAGE STR_DONOT_HAVE_RECOVER_EXPERIENCE() {
		return new SM_SYSTEM_MESSAGE(1300682);
	}

	/**
	 * You can respond to the survey only in this server.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_POLL_ANSWER_IS_NOT_ORG_SERVER() {
		return new SM_SYSTEM_MESSAGE(1300683);
	}

	/**
	 * There is no survey underway.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_FIND_POLL() {
		return new SM_SYSTEM_MESSAGE(1300684);
	}

	/**
	 * You have already responded to this survey.
	 */
	public static SM_SYSTEM_MESSAGE STR_ALREADY_ANSWER_THIS_POLL() {
		return new SM_SYSTEM_MESSAGE(1300685);
	}

	/**
	 * You do not have enough Kinah to register this location as a bind point.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_REGISTER_RESURRECT_POINT_NOT_ENOUGH_FEE() {
		return new SM_SYSTEM_MESSAGE(1300686);
	}

	/**
	 * You cannot bind from here.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_REGISTER_RESURRECT_POINT_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300687);
	}

	/**
	 * You have already bound at this location.
	 */
	public static SM_SYSTEM_MESSAGE STR_ALREADY_REGISTER_THIS_RESURRECT_POINT() {
		return new SM_SYSTEM_MESSAGE(1300688);
	}

	/**
	 * You do not have enough Kinah for teleport.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_MOVE_TO_AIRPORT_NOT_ENOUGH_FEE() {
		return new SM_SYSTEM_MESSAGE(1300689);
	}

	/**
	 * You cannot use it as the required quest has not been completed.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_MOVE_TO_AIRPORT_NEED_FINISH_QUEST() {
		return new SM_SYSTEM_MESSAGE(1300690);
	}

	/**
	 * You cannot move to that destination.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE() {
		return new SM_SYSTEM_MESSAGE(1300691);
	}

	/**
	 * The NPC you selected does not have the ability to teleport you.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_MOVE_TO_AIRPORT_WRONG_NPC() {
		return new SM_SYSTEM_MESSAGE(1300692);
	}

	/**
	 * You are too far from the NPC to teleport.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_MOVE_TO_AIRPORT_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300693);
	}

	/**
	 * You can bind here by clicking the Obelisk.
	 */
	public static SM_SYSTEM_MESSAGE STR_NOTIFY_RESURRECT_POINT() {
		return new SM_SYSTEM_MESSAGE(1300694);
	}

	/**
	 * You are already experiencing the resurrection effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_OTHER_USER_USE_RESURRECT_SKILL_ALREADY() {
		return new SM_SYSTEM_MESSAGE(1300695);
	}

	/**
	 * You cannot teleport to a bind point while flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_AIRPORT_WHEN_FLYING() {
		return new SM_SYSTEM_MESSAGE(1300696);
	}

	/**
	 * The server is being shut down for an update.
	 */
	public static SM_SYSTEM_MESSAGE STR_SHUTDOWN_REASON_UPDATE() {
		return new SM_SYSTEM_MESSAGE(1300697);
	}

	/**
	 * Leaving Atreia.\n\n Please wait %0 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_WAIT_TO_QUIT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300698, value0);
	}

	/**
	 * The account usage time has expired.
	 */
	public static SM_SYSTEM_MESSAGE STR_KICK_TIME_EXPIRED() {
		return new SM_SYSTEM_MESSAGE(1300699);
	}

	/**
	 * Another user has tried to log in.
	 */
	public static SM_SYSTEM_MESSAGE STR_KICK_ANOTHER_USER_TRY_LOGIN() {
		return new SM_SYSTEM_MESSAGE(1300700);
	}

	/**
	 * You do not have enough Kinah to use the artifact.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ARTIFACT_NOT_ENOUGH_FEE() {
		return new SM_SYSTEM_MESSAGE(1300701);
	}

	/**
	 * The Artifact cannot be used at this time.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ARTIFACT_OUT_OF_ORDER() {
		return new SM_SYSTEM_MESSAGE(1300702);
	}

	/**
	 * You have no authority to use the Artifact.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ARTIFACT_HAVE_NO_AUTHORITY() {
		return new SM_SYSTEM_MESSAGE(1300703);
	}

	/**
	 * You cannot use the Artifact from this place.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ARTIFACT_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300704);
	}

	/**
	 * This is not a usable Artifact.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ARTIFACT_IS_NOT_ARTIFACT() {
		return new SM_SYSTEM_MESSAGE(1300705);
	}

	/**
	 * You have no authority to go through the door.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DOOR_HAVE_NO_AUTHORITY() {
		return new SM_SYSTEM_MESSAGE(1300706);
	}

	/**
	 * You cannot use the door from here.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DOOR_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300707);
	}

	/**
	 * Your quest tracker is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_IND_EXCESS() {
		return new SM_SYSTEM_MESSAGE(1300708);
	}

	/**
	 * Macro canceled.
	 */
	public static SM_SYSTEM_MESSAGE STR_MACRO_MSG_CANCEL() {
		return new SM_SYSTEM_MESSAGE(1300709);
	}

	/**
	 * Macro complete.
	 */
	public static SM_SYSTEM_MESSAGE STR_MACRO_MSG_END() {
		return new SM_SYSTEM_MESSAGE(1300710);
	}

	/**
	 * Mail has arrived.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_RECEIVE_MAIL() {
		return new SM_SYSTEM_MESSAGE(1300711);
	}

	/**
	 * All items are already confirmed. You cannot register any more items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EXCHANGE_ALREADY_READY() {
		return new SM_SYSTEM_MESSAGE(1300712);
	}

	/**
	 * You cannot split items in the inventory during a trade.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INVENTORY_SPLIT_DURING_TRADE() {
		return new SM_SYSTEM_MESSAGE(1300713);
	}

	/**
	 * You cannot open the private store on a moving object.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_DISABLED_IN_MOVING_OBJECT() {
		return new SM_SYSTEM_MESSAGE(1300714);
	}

	/**
	 * This Rift is not usable.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DIRECT_PORTAL_NO_PORTAL() {
		return new SM_SYSTEM_MESSAGE(1300715);
	}

	/**
	 * You cannot use a Rift here.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DIRECT_PORTAL_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300716);
	}

	/**
	 * You cannot use a Rift at your level.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DIRECT_PORTAL_LEVEL_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1300717);
	}

	/**
	 * The Rift has already had the maximum number of people travel through it.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DIRECT_PORTAL_USE_COUNT_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1300718);
	}

	/**
	 * The remaining playing time is %*0.
	 */
	public static SM_SYSTEM_MESSAGE STR_REMAIN_PLAYTIME(String value0) {
		return new SM_SYSTEM_MESSAGE(1300719, value0);
	}

	/**
	 * Pre-paid credit is being applied. The remaining playing time is %*0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHANGE_REMAIN_PLAYTIME(String value0) {
		return new SM_SYSTEM_MESSAGE(1300720, value0);
	}

	/**
	 * Please use the key near the door.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_KEY_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300721);
	}

	/**
	 * You need %0 to open the door.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_OPEN_DOOR_NEED_NAMED_KEY_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300722, value0);
	}

	/**
	 * You need a key to open the door.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_OPEN_DOOR_NEED_KEY_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300723);
	}

	/**
	 * Trade Broker
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_RETURN_MAIL_FROM() {
		return new SM_SYSTEM_MESSAGE(1300724);
	}

	/**
	 * Your item has been returned as the sales period has ended.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_RETURN_MAIL_CONTENT() {
		return new SM_SYSTEM_MESSAGE(1300725);
	}

	/**
	 * You have no authority to use it as you are not a member of the Conquering Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DOOR_REPAIR_HAVE_NO_AUTHORITY() {
		return new SM_SYSTEM_MESSAGE(1300726);
	}

	/**
	 * You cannot use that as the cooldown time has not expired yet.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DOOR_REPAIR_OUT_OF_COOLTIME() {
		return new SM_SYSTEM_MESSAGE(1300727);
	}

	/**
	 * You do not have enough items needed for repair. The fee is %0 (per %1).
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DOOR_REPAIR_NOT_ENOUGH_FEE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300728, value0, value1);
	}

	/**
	 * %0 has used level %1 %2.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MONSTER_SKILL(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1300729, value0, value1, value2);
	}

	/**
	 * Moving to Area EE2.
	 */
	public static SM_SYSTEM_MESSAGE STR_TP_EE2_TP0_L_TOEE2() {
		return new SM_SYSTEM_MESSAGE(1300730);
	}

	/**
	 * Moving to Dungeon D3.
	 */
	public static SM_SYSTEM_MESSAGE STR_TP_EE2_TP0_L_TOD3_DUN() {
		return new SM_SYSTEM_MESSAGE(1300731);
	}

	/**
	 * Moving to the Instanced Dungeon.
	 */
	public static SM_SYSTEM_MESSAGE STR_TP_EE2_TP0_L_TOINSTANT_DUN() {
		return new SM_SYSTEM_MESSAGE(1300732);
	}

	/**
	 * Moving to Eltnen.
	 */
	public static SM_SYSTEM_MESSAGE STR_TP_LF1A_TP0_L_TOLF2() {
		return new SM_SYSTEM_MESSAGE(1300733);
	}

	/**
	 * Moving to Verteron.
	 */
	public static SM_SYSTEM_MESSAGE STR_TP_LF2_TP0_L_TOLF1A() {
		return new SM_SYSTEM_MESSAGE(1300734);
	}

	/**
	 * You cannot use any items while flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_FLYING_DISABLE_1() {
		return new SM_SYSTEM_MESSAGE(1300735);
	}

	/**
	 * You cannot use the skill while flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_FLYING_DISABLE_2() {
		return new SM_SYSTEM_MESSAGE(1300736);
	}

	/**
	 * You have died.
	 */
	public static SM_SYSTEM_MESSAGE STR_DEATH_MESSAGE_ME() {
		return new SM_SYSTEM_MESSAGE(1300737);
	}

	/**
	 * You have resurrected.
	 */
	public static SM_SYSTEM_MESSAGE STR_REBIRTH_MASSAGE_ME() {
		return new SM_SYSTEM_MESSAGE(1300738);
	}

	/**
	 * %0 has defeated %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_KILLMSG(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300739, value0, value1);
	}

	/**
	 * Resurrection wait time: %0 sec
	 */
	public static SM_SYSTEM_MESSAGE STR_WATINGTIME(String value0) {
		return new SM_SYSTEM_MESSAGE(1300740, value0);
	}

	/**
	 * Use a skill to resurrect.
	 */
	public static SM_SYSTEM_MESSAGE STR_RESURRECT_DIALOG__SKILL() {
		return new SM_SYSTEM_MESSAGE(1300741);
	}

	/**
	 * Use an item to resurrect.
	 */
	public static SM_SYSTEM_MESSAGE STR_RESURRECT_DIALOG__ITEM() {
		return new SM_SYSTEM_MESSAGE(1300742);
	}

	/**
	 * Press "OK" to resurrect.
	 */
	public static SM_SYSTEM_MESSAGE STR_RESURRECT_DIALOG__BIND() {
		return new SM_SYSTEM_MESSAGE(1300743);
	}

	/**
	 * It will be cancelled if you do not press it in %0 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_RESURRECTOTHER_DIALOG__5MIN(String value0) {
		return new SM_SYSTEM_MESSAGE(1300744, value0);
	}

	/**
	 * It will be cancelled if you do not press it in %0 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_RESURRECT_DIALOG__5MIN(String value0) {
		return new SM_SYSTEM_MESSAGE(1300745, value0);
	}

	/**
	 * You will be resurrected at the registered bind point if you do not press it in %0 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_RESURRECT_DIALOG__30MIN(String value0) {
		return new SM_SYSTEM_MESSAGE(1300746, value0);
	}

	/**
	 * It is at a hard-to-find location.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_UNKNOWN_NAME() {
		return new SM_SYSTEM_MESSAGE(1300747);
	}

	/**
	 * %0 is at the position indicated on the map.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_SUBZONE_FOUND(String value0) {
		return new SM_SYSTEM_MESSAGE(1300748, value0);
	}

	/**
	 * %0 is where it is indicated on the map, but the path leading to it cannot be found.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_TOO_FAR_FROM_SUBZONE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300749, value0);
	}

	/**
	 * %0 is at the position indicated on the map.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_NPC_FOUND(String value0) {
		return new SM_SYSTEM_MESSAGE(1300750, value0);
	}

	/**
	 * %0 is where it is indicated on the map, but the path leading to it cannot be found.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_TOO_FAR_FROM_NPC(String value0) {
		return new SM_SYSTEM_MESSAGE(1300751, value0);
	}

	/**
	 * %0 is at a hard-to-find location.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_NO_NPC_IN_THIS_WORLD(String value0) {
		return new SM_SYSTEM_MESSAGE(1300752, value0);
	}

	/**
	 * Searching for the location. Please wait (max. 30 seconds).
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_FINDING_PLEASE_WAIT() {
		return new SM_SYSTEM_MESSAGE(1300753);
	}

	/**
	 * %0 is in the %1 region.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_NPC_FOUND_IN_OTHER_WORLD(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300754, value0, value1);
	}

	/**
	 * You cannot quit the game during the battle.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_QUIT() {
		return new SM_SYSTEM_MESSAGE(1300755);
	}

	/**
	 * A one-way Rift into Elysea has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIGHT_SIDE_DIRECT_PORTAL_OPEN() {
		return new SM_SYSTEM_MESSAGE(1300756);
	}

	/**
	 * You spent %num0 Kinah.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_USEMONEY(int num0) {
		return new SM_SYSTEM_MESSAGE(1300757, num0);
	}

	/**
	 * Trade Failed
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TITLE_TRADE_FAIL() {
		return new SM_SYSTEM_MESSAGE(1300758);
	}

	/**
	 * You do not have enough Kinah.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOT_ENOUGH_MONEY() {
		return new SM_SYSTEM_MESSAGE(1300759);
	}

	/**
	 * You can list up to %num0 items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EXCEED_MAX_ITEM_COUNT(int num0) {
		return new SM_SYSTEM_MESSAGE(1300760, num0);
	}

	/**
	 * This item cannot be traded.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_EXCHANGE() {
		return new SM_SYSTEM_MESSAGE(1300761);
	}

	/**
	 * You cannot trade as your inventory is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FULL_INVENTORY() {
		return new SM_SYSTEM_MESSAGE(1300762);
	}

	/**
	 * You cannot register any more items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FULL_BASKET() {
		return new SM_SYSTEM_MESSAGE(1300763);
	}

	/**
	 * You already have this limited possession item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_OWNED_LORE_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300764);
	}

	/**
	 * Confirm Registration
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_MSG_TITLE_REGISTER_OK() {
		return new SM_SYSTEM_MESSAGE(1300765);
	}

	/**
	 * Registration Failed
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_MSG_TITLE_REGISTER_ERROR() {
		return new SM_SYSTEM_MESSAGE(1300766);
	}

	/**
	 * You do not have enough Kinah to pay the fee.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_MSG_NOT_ENOUGH_FEE() {
		return new SM_SYSTEM_MESSAGE(1300767);
	}

	/**
	 * You entered the PvP zone.
	 */
	public static SM_SYSTEM_MESSAGE STR_PVP_ZONE_ENTERED() {
		return new SM_SYSTEM_MESSAGE(1300768);
	}

	/**
	 * You left the PvP zone.
	 */
	public static SM_SYSTEM_MESSAGE STR_PVP_ZONE_EXITED() {
		return new SM_SYSTEM_MESSAGE(1300769);
	}

	/**
	 * Start Duel!
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_START() {
		return new SM_SYSTEM_MESSAGE(1300770);
	}

	/**
	 * Stop Duel
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_STOP() {
		return new SM_SYSTEM_MESSAGE(1300771);
	}

	/**
	 * You cannot destroy equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_DESTROY_EQUIP_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300772);
	}

	/**
	 * Permanently Acquired Title
	 */
	public static SM_SYSTEM_MESSAGE STR_TITLE_PERMANENT() {
		return new SM_SYSTEM_MESSAGE(1300773);
	}

	/**
	 * Temporarily Acquired Title
	 */
	public static SM_SYSTEM_MESSAGE STR_TITLE_TEMPORARY() {
		return new SM_SYSTEM_MESSAGE(1300774);
	}

	/**
	 * %0 skill (Level %1)
	 */
	public static SM_SYSTEM_MESSAGE STR_TITLE_BONUS_SKILL(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300775, value0, value1);
	}

	/**
	 * You are already destroying another item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ALREADY_DESTROY_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300776);
	}

	/**
	 * Starting the duel with %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DUEL_START(String value0) {
		return new SM_SYSTEM_MESSAGE(1300777, value0);
	}

	/**
	 * The World Map is currently being prepared.
	 */
	public static SM_SYSTEM_MESSAGE STR_WORLDMAP_UNDER_CONSTRUCTION() {
		return new SM_SYSTEM_MESSAGE(1300778);
	}

	/**
	 * The current screenshot was saved in %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_PRINT_SCREEN(String value0) {
		return new SM_SYSTEM_MESSAGE(1300779, value0);
	}

	/**
	 * You are already chatting with someone.
	 */
	public static SM_SYSTEM_MESSAGE STR_ALREADY_TALKING_TO_SOMEONE() {
		return new SM_SYSTEM_MESSAGE(1300780);
	}

	/**
	 * %0 rolled the dice and got a %num1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_ROLLED(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1300781, value0, num1);
	}

	/**
	 * %0 gave up rolling the dice.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_PASSED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300782, value0);
	}

	/**
	 * %0 does not have the right to roll the dice.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_UNAUTHORIZED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300783, value0);
	}

	/**
	 * You have purchased %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BUY_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300784, value0);
	}

	/**
	 * You have purchased %1 %0s.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BUY_ITEM_MULTI(String value1, String value0s) {
		return new SM_SYSTEM_MESSAGE(1300785, value1, value0s);
	}

	/**
	 * You have sold %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SELL_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300786, value0);
	}

	/**
	 * You have sold %1 %0s.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SELL_ITEM_MULTI(String value1, String value0s) {
		return new SM_SYSTEM_MESSAGE(1300787, value1, value0s);
	}

	/**
	 * You have crafted %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBINE_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300788, value0);
	}

	/**
	 * You have crafted %1 %0s.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBINE_ITEM_MULTI(String value1, String value0s) {
		return new SM_SYSTEM_MESSAGE(1300789, value1, value0s);
	}

	/**
	 * %0 has been sold.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SOLDOUT_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300790, value0);
	}

	/**
	 * %1 %0s have been sold.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SOLDOUT_ITEM_MULTI(String value1, String value0s) {
		return new SM_SYSTEM_MESSAGE(1300791, value1, value0s);
	}

	/**
	 * You have discarded %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DISCARD_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300792, value0);
	}

	/**
	 * You have discarded %1 %0s.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DISCARD_ITEM_MULTI(String value1, String value0s) {
		return new SM_SYSTEM_MESSAGE(1300793, value1, value0s);
	}

	/**
	 * Your builder level is too low to open the selected window.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_OPEN_DIALOG_BY_BUILDER_LEVEL() {
		return new SM_SYSTEM_MESSAGE(1300794);
	}

	/**
	 * Currently, %0 cannot receive any friend requests.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDY_CANT_ADD_WHEN_HE_IS_ASKED_QUESTION(String value0) {
		return new SM_SYSTEM_MESSAGE(1300795, value0);
	}

	/**
	 * You cannot open a private store while flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_DISABLED_IN_FLY_MODE() {
		return new SM_SYSTEM_MESSAGE(1300798);
	}

	/**
	 * You cannot use this Kisk.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_REGISTER_BINDSTONE_HAVE_NO_AUTHORITY() {
		return new SM_SYSTEM_MESSAGE(1300799);
	}

	/**
	 * You cannot use the Kisk here.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_REGISTER_BINDSTONE_FAR_FROM_NPC() {
		return new SM_SYSTEM_MESSAGE(1300800);
	}

	/**
	 * You cannot use the Kisk.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_REGISTER_BINDSTONE_NOT_BINDSTONE() {
		return new SM_SYSTEM_MESSAGE(1300801);
	}

	/**
	 * The Kisk has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_BINDSTONE_IS_DESTROYED() {
		return new SM_SYSTEM_MESSAGE(1300802);
	}

	/**
	 * The Kisk has been dismantled.
	 */
	public static SM_SYSTEM_MESSAGE STR_BINDSTONE_IS_REMOVED() {
		return new SM_SYSTEM_MESSAGE(1300803);
	}

	/**
	 * You cannot install the Kisk as it is too close to an Artifact.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_BINDSTONE_ITEM_NOT_PROPER_AREA() {
		return new SM_SYSTEM_MESSAGE(1300804);
	}

	/**
	 * You can only use the Kisk when the PvP is On.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_BINDSTONE_ITEM_NOT_PROPER_TIME() {
		return new SM_SYSTEM_MESSAGE(1300805);
	}

	/**
	 * You cannot use a Kisk while flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_BINDSTONE_ITEM_WHILE_FLYING() {
		return new SM_SYSTEM_MESSAGE(1300806);
	}

	/**
	 * You are not allowed to move for %0 minutes for the following reason(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_INGAME_BLOCK_ENABLE_NO_MOVE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300807, value0);
	}

	/**
	 * You are not allowed to chat for %0 minutes for the following reason(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_INGAME_BLOCK_ENABLE_NO_CHAT(int value0) {
		return new SM_SYSTEM_MESSAGE(1300808, value0);
	}

	/**
	 * You are not allowed to open the private store for %0 minutes for the following reason(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_INGAME_BLOCK_ENABLE_NO_SHOP(String value0) {
		return new SM_SYSTEM_MESSAGE(1300809, value0);
	}

	/**
	 * You are now allowed to move.
	 */
	public static SM_SYSTEM_MESSAGE STR_INGAME_BLOCK_DISABLE_NO_MOVE() {
		return new SM_SYSTEM_MESSAGE(1300810);
	}

	/**
	 * You are now allowed to chat.
	 */
	public static SM_SYSTEM_MESSAGE STR_INGAME_BLOCK_DISABLE_NO_CHAT() {
		return new SM_SYSTEM_MESSAGE(1300811);
	}

	/**
	 * You now allowed to open a private store.
	 */
	public static SM_SYSTEM_MESSAGE STR_INGAME_BLOCK_DISABLE_NO_SHOP() {
		return new SM_SYSTEM_MESSAGE(1300812);
	}

	/**
	 * You are currently unable to move. There are %0 minute(s) left in your ban.
	 */
	public static SM_SYSTEM_MESSAGE STR_INGAME_BLOCK_IN_NO_MOVE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300813, value0);
	}

	/**
	 * You are currently unable to chat. There are %0 minute(s) left in your ban.
	 */
	public static SM_SYSTEM_MESSAGE STR_INGAME_BLOCK_IN_NO_CHAT(int value0) {
		return new SM_SYSTEM_MESSAGE(1300814, value0);
	}

	/**
	 * You are currently unable to open a private store. There are %0 minute(s) left on your ban.
	 */
	public static SM_SYSTEM_MESSAGE STR_INGAME_BLOCK_IN_NO_SHOP(String value0) {
		return new SM_SYSTEM_MESSAGE(1300815, value0);
	}

	/**
	 * Unknown Error
	 */
	public static SM_SYSTEM_MESSAGE STR_ERROR_UNKNOWN() {
		return new SM_SYSTEM_MESSAGE(1300816);
	}

	/**
	 * Database Error
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_DATABASE_FAIL() {
		return new SM_SYSTEM_MESSAGE(1300817);
	}

	/**
	 * There are unfinished replies.
	 */
	public static SM_SYSTEM_MESSAGE STR_GM_POLL_ANSWERS_NOT_COMPLETED() {
		return new SM_SYSTEM_MESSAGE(1300821);
	}

	/**
	 * Abandon Selected Quest
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ABANDON() {
		return new SM_SYSTEM_MESSAGE(1300822);
	}

	/**
	 * Invalid target.
	 */
	public static SM_SYSTEM_MESSAGE STR_INVALID_TARGET() {
		return new SM_SYSTEM_MESSAGE(1300823);
	}

	/**
	 * You cannot use this menu when you are dead.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_IN_DEAD_STATE() {
		return new SM_SYSTEM_MESSAGE(1300824);
	}

	/**
	 * You cannot destroy items while you are a corpse.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DEAD_BODY_CANT_DESTROY_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300825);
	}

	/**
	 * You cannot use certain chat functions while you are dead.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_CHAT_IN_DEAD_STATE() {
		return new SM_SYSTEM_MESSAGE(1300826);
	}

	/**
	 * /CreateChannel [ChannelName] [OptionalPassword]: Creates a private channel. /JoinChannel [ChannelName] [Password]: Enters an existing private
	 * channel. Password required if one was set. /LeaveChannel [ChannelNumber]: Leaves a private channel. /ChannelMemberInfo [ChannelNumber]: Shows
	 * who's in a channel. /ChannelInfo [ChannelNumber]: Shows information for a channel you are in. /ChannelBanInfo [ChannelNumber]: Shows who is
	 * banned from a channel. /BanFromChannel [ChannelNumber] [CharacterName]: The channel owner can permanently remove a character from the channel.
	 * /UnbanFromChannel [ChannelNumber] [CharacterName]: The channel owner can reinstate a banned character's access to a channel. /ChangeChannelLeader
	 * [ChannelNumber] [CharacterName]: The channel owner makes another character the channel owner. /ChangeChannelPassword [ChannelNumber] [Password]:
	 * The channel owner can change the password. /ChannelHelp: Shows the commands available for channels.
	 */
	public static SM_SYSTEM_MESSAGE STR_CURRENT_STANCE_DOES_NOT_SUPPORTS() {
		return new SM_SYSTEM_MESSAGE(1300827);
	}

	/**
	 * You are too close to attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_TOO_CLOSE_TO_ATTACK() {
		return new SM_SYSTEM_MESSAGE(1300828);
	}

	/**
	 * Someone is already looting that.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOOT_FAIL_ONLOOTING() {
		return new SM_SYSTEM_MESSAGE(1300829);
	}

	/**
	 * You are too far from the target.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOOT_FAIL_TOO_FAR() {
		return new SM_SYSTEM_MESSAGE(1300830);
	}

	/**
	 * You do not have enough Kinah to expand the cube.
	 */
	public static SM_SYSTEM_MESSAGE STR_WAREHOUSE_EXPAND_NOT_ENOUGH_MONEY() {
		return new SM_SYSTEM_MESSAGE(1300831);
	}

	/**
	 * Cannot find the emblem.bmp file in the Aion Game folder.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WARN_NO_EMBLEM_FILE() {
		return new SM_SYSTEM_MESSAGE(1300832);
	}

	/**
	 * %0 cannot be socketed with Manastone.
	 */
	public static SM_SYSTEM_MESSAGE STR_GIVE_ITEM_PROC_NOT_ADD_PROC(String value0) {
		return new SM_SYSTEM_MESSAGE(1300833, value0);
	}

	/**
	 * You must pass the Expert promotion test in order to be promoted.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_CANT_EXTEND_MONEY() {
		return new SM_SYSTEM_MESSAGE(1300834);
	}

	/**
	 * Upload of the Legion emblem file to the server successful.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WARN_SUCCESS_UPLOAD_EMBLEM() {
		return new SM_SYSTEM_MESSAGE(1300835);
	}

	/**
	 * Upload of the Legion emblem file to the server failed.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WARN_FAILURE_UPLOAD_EMBLEM() {
		return new SM_SYSTEM_MESSAGE(1300836);
	}

	/**
	 * Failed to read the Legion emblem file.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WARN_CORRUPT_EMBLEM_FILE() {
		return new SM_SYSTEM_MESSAGE(1300837);
	}

	/**
	 * The size of the Legion emblem file is not 24bit 256X256.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_WARN_IMPROPER_SIZE_EMBLEM_FILE() {
		return new SM_SYSTEM_MESSAGE(1300838);
	}

	/**
	 * Skill Penalty
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_PENALTY_TITLE() {
		return new SM_SYSTEM_MESSAGE(1300839);
	}

	/**
	 * Reduces Evasion, Parry, and Block
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_PENALTY_ACTIVED_EFEND() {
		return new SM_SYSTEM_MESSAGE(1300840);
	}

	/**
	 * Reduces Physical Defense and Magical Resistance
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_PENALTY_DEFEND() {
		return new SM_SYSTEM_MESSAGE(1300841);
	}

	/**
	 * You have joined the %0 Channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_CHANNEL_JOIN(String value0) {
		return new SM_SYSTEM_MESSAGE(1300842, value0);
	}

	/**
	 * You have left the %0 Channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_CHANNEL_LEAVE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300843, value0);
	}

	/**
	 * You have created the %0 Channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_CHANNEL_CREATE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300844, value0);
	}

	/**
	 * Your private channel "%1"% is open as Channel No. %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_CREATE_SUCCESS(String value0) {
		return new SM_SYSTEM_MESSAGE(1300845, value0);
	}

	/**
	 * You cannot open or join any more private channels.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_CREATE_FAILED_MAXROOM() {
		return new SM_SYSTEM_MESSAGE(1300846);
	}

	/**
	 * Incorrect password.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_CREATE_FAILED_WRONG_PASSWORD() {
		return new SM_SYSTEM_MESSAGE(1300847);
	}

	/**
	 * You do not have enough DP for conversion.
	 */
	public static SM_SYSTEM_MESSAGE STR_CONVERT_SKILL_NOT_ENOUGH_DP() {
		return new SM_SYSTEM_MESSAGE(1300848);
	}

	/**
	 * You must have learned the skill to activate it.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_MSG_CANT_WORK() {
		return new SM_SYSTEM_MESSAGE(1300849);
	}

	/**
	 * You have joined the private channel %1 at Channel %0. Confirm if you selected the channel in the chatting tab option.
	 */
	public static SM_SYSTEM_MESSAGE STR_CAHT_ROOM_JOIN_SUCCESS(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1300850, value1, value0);
	}

	/**
	 * That private channel does not exist.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_JOIN_FAIL_ROOM_NOT_FOUND() {
		return new SM_SYSTEM_MESSAGE(1300851);
	}

	/**
	 * You cannot enter the private channel as it is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_JOIN_FAIL_TOO_MANY_ROOM() {
		return new SM_SYSTEM_MESSAGE(1300852);
	}

	/**
	 * An expelled character cannot enter the same private channel again.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_JOIN_FAIL_BANNED_USER() {
		return new SM_SYSTEM_MESSAGE(1300853);
	}

	/**
	 * You need to enter a password to join the private channel %0. Please enter it accurately, in the format of '/JoinChannel [ChannelName]
	 * [password]'.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_JOIN_FAIL_WRONG_PASSWORD(String value0) {
		return new SM_SYSTEM_MESSAGE(1300854, value0);
	}

	/**
	 * You cannot enter the private channel (%0) as it is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_JOIN_FAIL_ROOM_FULL(String value0) {
		return new SM_SYSTEM_MESSAGE(1300855, value0);
	}

	/**
	 * %0 has entered the private channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_JOIN_NOTIFY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300856, value0);
	}

	/**
	 * You have joined the private channel %1 at Channel %0. Confirm if you selected the channel in the chatting tab option.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_JOIN_NOTIFY_SELF(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1300857, value1, value0);
	}

	/**
	 * You are not in the private channel %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_LEAVE_FAIL_NOT_A_MEMBER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300858, value0);
	}

	/**
	 * %0 has left the private channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_LEAVE_NOTIFY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300859, value0);
	}

	/**
	 * You have left the private channel %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_LEAVE_SUCCESS(String value0) {
		return new SM_SYSTEM_MESSAGE(1300860, value0);
	}

	/**
	 * %0 is the new channel leader.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_NEW_ADMIN(String value0) {
		return new SM_SYSTEM_MESSAGE(1300861, value0);
	}

	/**
	 * You have no authority.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_ADMIN_NO_AUTHORITY() {
		return new SM_SYSTEM_MESSAGE(1300862);
	}

	/**
	 * Cannot find the character in this private channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_MEMBER_VOID() {
		return new SM_SYSTEM_MESSAGE(1300863);
	}

	/**
	 * %0 has been kicked out of the private channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_BAN_MEMBER_BANNED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300864, value0);
	}

	/**
	 * You have been kicked out of the private channel %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_BAN_SELF_BANNED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300865, value0);
	}

	/**
	 * The password of the private channel has been changed to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_PASSWORD_CHANGED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300866, value0);
	}

	/**
	 * The maximum password length is %0 characters.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_PASSWORD_TOO_LONG(String value0) {
		return new SM_SYSTEM_MESSAGE(1300867, value0);
	}

	/**
	 * The name of that private channel is invalid.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_INVALID_CHANNEL_NAME() {
		return new SM_SYSTEM_MESSAGE(1300868);
	}

	/**
	 * You have been kicked out.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_KICKED_OUT() {
		return new SM_SYSTEM_MESSAGE(1300869);
	}

	/**
	 * %0 has been deleted from the Ban List.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_UNBAN_SUCCESS(String value0) {
		return new SM_SYSTEM_MESSAGE(1300870, value0);
	}

	/**
	 * The channel has been set as public.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_PROPERTYCHANGE_TOPUBLIC() {
		return new SM_SYSTEM_MESSAGE(1300871);
	}

	/**
	 * The channel has been set as private.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_PROPERTYCHANGE_TOPRIVATE() {
		return new SM_SYSTEM_MESSAGE(1300872);
	}

	/**
	 * Maximum number of users allowed is now set to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_PROPERTYCHANGE_MAXMEMBER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300873, value0);
	}

	/**
	 * You are not participating in any channels.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_NO_JOINED_CHATROOM() {
		return new SM_SYSTEM_MESSAGE(1300874);
	}

	/**
	 * /CreateChannel [ChannelName] [OptionalPassword]: Creates a private channel. /JoinChannel [ChannelName] [Password]: Enters an existing private
	 * channel. Password required if one was set. /LeaveChannel [ChannelNumber]: Leaves a private channel. /ChannelMemberInfo [ChannelNumber]: Shows
	 * who's in a channel. /ChannelInfo [ChannelNumber]: Shows information for a channel you are in. /ChannelBanInfo [ChannelNumber]: Shows who is
	 * banned from a channel. /BanFromChannel [ChannelNumber] [CharacterName]: The channel owner can permanently remove a character from the channel.
	 * /UnbanFromChannel [ChannelNumber] [CharacterName]: The channel owner can reinstate a banned character's access to a channel. /ChangeChannelLeader
	 * [ChannelNumber] [CharacterName]: The channel owner makes another character the channel owner. /ChangeChannelPassword [ChannelNumber] [Password]:
	 * The channel owner can change the password. /ChannelHelp: Shows the commands available for channels.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_HELP() {
		return new SM_SYSTEM_MESSAGE(1300875);
	}

	/**
	 * You cannot learn a design written in an incomprehensible language.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFTRECIPE_RACE_CHECK() {
		return new SM_SYSTEM_MESSAGE(1300876);
	}

	/**
	 * An express courier has already arrived.
	 */
	public static SM_SYSTEM_MESSAGE STR_POSTMAN_ALREADY_SUMMONED() {
		return new SM_SYSTEM_MESSAGE(1300877);
	}

	/**
	 * Please wait for a while before you call for the courier again.
	 */
	public static SM_SYSTEM_MESSAGE STR_POSTMAN_UNABLE_IN_COOLTIME() {
		return new SM_SYSTEM_MESSAGE(1300878);
	}

	/**
	 * You cannot call a courierwhile flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_POSTMAN_UNABLE_IN_FLIGHT() {
		return new SM_SYSTEM_MESSAGE(1300879);
	}

	/**
	 * You cannot call a courier here.
	 */
	public static SM_SYSTEM_MESSAGE STR_POSTMAN_UNABLE_POSITION() {
		return new SM_SYSTEM_MESSAGE(1300880);
	}

	/**
	 * That character does not exist.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_CHARACTER_NONEXISIT() {
		return new SM_SYSTEM_MESSAGE(1300881);
	}

	/**
	 * That person is not logged on.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_NO_OFFLINE_CHARACTER() {
		return new SM_SYSTEM_MESSAGE(1300882);
	}

	/**
	 * The character is already on your Friends List.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_ALREADY_IN_LIST() {
		return new SM_SYSTEM_MESSAGE(1300883);
	}

	/**
	 * A blocked character cannot also be a Friend.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_NO_BLOCKED_CHARACTER() {
		return new SM_SYSTEM_MESSAGE(1300884);
	}

	/**
	 * You have added %0 to your Friends List.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_ADD_BUDDY_ACCEPTED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300885, value0);
	}

	/**
	 * %0 declined your friend request.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_ADD_BUDDY_REJECTED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300886, value0);
	}

	/**
	 * Your Friends List is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_LIST_FULL() {
		return new SM_SYSTEM_MESSAGE(1300887);
	}

	/**
	 * You have removed %0 from your Friends List.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_REMOVE_CHARACTER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300888, value0);
	}

	/**
	 * The character is not on your Friends List.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_NOT_IN_LIST() {
		return new SM_SYSTEM_MESSAGE(1300889);
	}

	/**
	 * Your friend %0 has logged in.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_BUDDY_LOGON(String value0) {
		return new SM_SYSTEM_MESSAGE(1300890, value0);
	}

	/**
	 * You cannot block a character who is currently on your Friends List.
	 */
	public static SM_SYSTEM_MESSAGE STR_BLOCKLIST_NO_BUDDY() {
		return new SM_SYSTEM_MESSAGE(1300891);
	}

	/**
	 * You have blocked %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_BLOCKLIST_ADD_BLOCKED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300892, value0);
	}

	/**
	 * That character does not exist.
	 */
	public static SM_SYSTEM_MESSAGE STR_BLOCKLIST_CHARACTER_NONEXIST() {
		return new SM_SYSTEM_MESSAGE(1300893);
	}

	/**
	 * That character is already blocked.
	 */
	public static SM_SYSTEM_MESSAGE STR_BLOCKLIST_ALREADY_BLOCKED() {
		return new SM_SYSTEM_MESSAGE(1300894);
	}

	/**
	 * Enter the name of the character you want to block.
	 */
	public static SM_SYSTEM_MESSAGE STR_BLOCKLIST_ENTER_CHARACTER_NAME() {
		return new SM_SYSTEM_MESSAGE(1300895);
	}

	/**
	 * You have unblocked %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_BLOCKLIST_REMOVE_FROM_LIST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300896, value0);
	}

	/**
	 * The character is not blocked.
	 */
	public static SM_SYSTEM_MESSAGE STR_BLOCKLIST_NOT_IN_LIST() {
		return new SM_SYSTEM_MESSAGE(1300897);
	}

	/**
	 * You must level up to raise your skill level.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_INFO_MAXPOINT_UP() {
		return new SM_SYSTEM_MESSAGE(1300898);
	}

	/**
	 * Express mail has arrived.
	 */
	public static SM_SYSTEM_MESSAGE STR_POSTMAN_NOTIFY() {
		return new SM_SYSTEM_MESSAGE(1300899);
	}

	/**
	 * Channel information: %0, Name: %1, Password: %2, Users: %num3.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_INFO_FORMAT(String value0, String value1, String value2, int num3) {
		return new SM_SYSTEM_MESSAGE(1300900, value0, value1, value2, num3);
	}

	/**
	 * Channel information: %0, Name: %1, Users: %num2.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_INFO_FORMAT_NOPASSWORD(String value0, String value1, int num2) {
		return new SM_SYSTEM_MESSAGE(1300901, value0, value1, num2);
	}

	/**
	 * You have been disconnected from the server.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_AUTH_CONNECTION_LOST() {
		return new SM_SYSTEM_MESSAGE(1300902);
	}

	/**
	 * A private channel with the same name already exists.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_ROOM_EXISTS() {
		return new SM_SYSTEM_MESSAGE(1300903);
	}

	/**
	 * That private channel already exists.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_CHANNEL_EXISTS() {
		return new SM_SYSTEM_MESSAGE(1300904);
	}

	/**
	 * You have already joined the private channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_ALREADY_JOINED_CHANNEL() {
		return new SM_SYSTEM_MESSAGE(1300905);
	}

	/**
	 * The character has been banned from this channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_ALREADY_BANNED_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1300906);
	}

	/**
	 * You cannot kick yourself out of the channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_CANNOT_BAN_SELF() {
		return new SM_SYSTEM_MESSAGE(1300907);
	}

	/**
	 * You cannot nominate yourself as a room master.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_CANNOT_PROMOTE_SELF() {
		return new SM_SYSTEM_MESSAGE(1300908);
	}

	/**
	 * The character is not on the Ban List.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_NOT_A_BANNED_CHARACTER() {
		return new SM_SYSTEM_MESSAGE(1300909);
	}

	/**
	 * Requires the %0 Equip Skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_TOOLTIP_NEED_MASTERY_SKILL(String value0) {
		return new SM_SYSTEM_MESSAGE(1300910, value0);
	}

	/**
	 * %0 has sent you a friend request.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDY_REQUEST_TO_ADD(String value0) {
		return new SM_SYSTEM_MESSAGE(1300911, value0);
	}

	/**
	 * Your Block List is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_BLOCKLIST_LIST_FULL() {
		return new SM_SYSTEM_MESSAGE(1300912);
	}

	/**
	 * You cannot block yourself.
	 */
	public static SM_SYSTEM_MESSAGE STR_BLOCKLIST_CANNOT_BLOCK_SELF() {
		return new SM_SYSTEM_MESSAGE(1300913);
	}

	/**
	 * You cannot use a Macro yet.
	 */
	public static SM_SYSTEM_MESSAGE STR_MACRO_MSG_CANNOT_READY_TO_USE() {
		return new SM_SYSTEM_MESSAGE(1300914);
	}

	/**
	 * %0 has logged in.
	 */
	public static SM_SYSTEM_MESSAGE STR_NOTIFY_LOGIN_BUDDY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300915, value0);
	}

	/**
	 * %0 has logged out.
	 */
	public static SM_SYSTEM_MESSAGE STR_NOTIFY_LOGOFF_BUDDY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300916, value0);
	}

	/**
	 * %0 has deleted you from their Friends List.
	 */
	public static SM_SYSTEM_MESSAGE STR_NOTIFY_DELETE_BUDDY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300917, value0);
	}

	/**
	 * The selected character is already dead.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_DEAD() {
		return new SM_SYSTEM_MESSAGE(1300918);
	}

	/**
	 * Builder Command %0 requires %1 parameters.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BC_NOT_ENOUGH_PARAMETER(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300919, value0, value1);
	}

	/**
	 * You cannot equip or remove items while in action.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_EQUIP_ITEM_IN_ACTION() {
		return new SM_SYSTEM_MESSAGE(1300920);
	}

	/**
	 * %0: Level %1 (%2)
	 */
	public static SM_SYSTEM_MESSAGE STR_MACRO_MSG_PROCESS(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1300921, value0, value1, value2);
	}

	/**
	 * Macro: Cannot find the skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_MACRO_MSG_CANNOT_FIND_SKILL() {
		return new SM_SYSTEM_MESSAGE(1300922);
	}

	/**
	 * Macro: Cannot find the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MACRO_MSG_CANNOT_FIND_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300926);
	}

	/**
	 * You do not have enough Abyss Points.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOT_ENOUGH_ABYSSPOINT() {
		return new SM_SYSTEM_MESSAGE(1300927);
	}

	/**
	 * You cannot change the channel during a battle.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_CHANGE_CHANNEL_IN_COMBAT() {
		return new SM_SYSTEM_MESSAGE(1300928);
	}

	/**
	 * You cannot change the channel now.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_CHANGE_CHANNEL_NOW() {
		return new SM_SYSTEM_MESSAGE(1300929);
	}

	/**
	 * Campaign quest acquired: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_MISSION_SYSTEMMSG_ACQUIRE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300930, value0);
	}

	/**
	 * Start Punishment
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAR_PUNISH_START_TIME() {
		return new SM_SYSTEM_MESSAGE(1300931);
	}

	/**
	 * End Punishment
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAR_PUNISH_END_TIME() {
		return new SM_SYSTEM_MESSAGE(1300932);
	}

	/**
	 * Macro: There is no item registered in the Quickbar.
	 */
	public static SM_SYSTEM_MESSAGE STR_MACRO_MSG_CANNOT_FIND_SHORTCUT() {
		return new SM_SYSTEM_MESSAGE(1300933);
	}

	/**
	 * Macro: Cannot find the target.
	 */
	public static SM_SYSTEM_MESSAGE STR_MACRO_MSG_CANNOT_FIND_TARGET() {
		return new SM_SYSTEM_MESSAGE(1300934);
	}

	/**
	 * Macro: The sentence cannot be parsed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MACRO_MSG_CANNOT_PARSE() {
		return new SM_SYSTEM_MESSAGE(1300935);
	}

	/**
	 * Please do not flood chat. Blocked for %0m.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_CHANNEL_FLOODING_BLOCKED_1(String value0m) {
		return new SM_SYSTEM_MESSAGE(1300936, value0m);
	}

	/**
	 * You can use the Channel: %0 only once every %1 seconds. Time Remaining: %2 seconds
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_CHANNEL_FLOODING_BLOCKED_2(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1300937, value0, value1, value2);
	}

	/**
	 * Both local and trade channels were moved to the %0 area.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_CHANNEL_LEVEL_CHANGED(String value0) {
		return new SM_SYSTEM_MESSAGE(1300938, value0);
	}

	/**
	 * The channel name must be between 2 and 10 characters.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_CHANNELNAME_SIZE_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1300939);
	}

	/**
	 * You cannot resurrect the target due to its insufficient Abyss Points.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_RESURRECT_FAILED() {
		return new SM_SYSTEM_MESSAGE(1300940);
	}

	/**
	 * You have too few Abyss points to continue the battle.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_RESURRECT() {
		return new SM_SYSTEM_MESSAGE(1300941);
	}

	/**
	 * The same item is already registered.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_ALREAY_REGIST_ITEM() {
		return new SM_SYSTEM_MESSAGE(1300942);
	}

	/**
	 * You cannot register items in the private store while equipped with Stigma.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_CANNOT_REGIST_DURING_STIGMA() {
		return new SM_SYSTEM_MESSAGE(1300943);
	}

	/**
	 * You cannot register items as you are already selling other items.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_CANNOT_REGIST_DURING_SELLING() {
		return new SM_SYSTEM_MESSAGE(1300944);
	}

	/**
	 * You received %0 item as reward for the survey.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_POLL_REWARD_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300945, value0);
	}

	/**
	 * You received %num1 %0 items as reward for the survey.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_POLL_REWARD_ITEM_MULTI(long num1, String value0) {
		return new SM_SYSTEM_MESSAGE(1300946, num1, value0);
	}

	/**
	 * You received %num0 Kinah as reward for the survey.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_POLL_REWARD_MONEY(long num0) {
		return new SM_SYSTEM_MESSAGE(1300947, num0);
	}

	/**
	 * Starting the voice chatting.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_VOICE_START_SUCCESS() {
		return new SM_SYSTEM_MESSAGE(1300948);
	}

	/**
	 * Failed to start the voice chatting.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_VOICE_START_FAILED() {
		return new SM_SYSTEM_MESSAGE(1300949);
	}

	/**
	 * Ending the voice chatting.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_VOICE_FINISH_SUCCESS() {
		return new SM_SYSTEM_MESSAGE(1300950);
	}

	/**
	 * Failed to end the voice chatting.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_VOICE_FINISH_FAILED() {
		return new SM_SYSTEM_MESSAGE(1300951);
	}

	/**
	 * You cannot use private channels before you change your Class.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_REQUIREMENT_UNFULLFILLED() {
		return new SM_SYSTEM_MESSAGE(1300952);
	}

	/**
	 * %0 starts the voice chatting.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_VOICE_START_NOTIFY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300953, value0);
	}

	/**
	 * %0 ends the voice chatting.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_VOICE_FINISH_NOTIFY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300954, value0);
	}

	/**
	 * The password for this private channel has been removed. You can now join the channel without entering the password.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_ROOM_PASSWORD_DELETED() {
		return new SM_SYSTEM_MESSAGE(1300955);
	}

	/**
	 * Purchase Item\n%attachItemName you bought has arrived.
	 */
	public static SM_SYSTEM_MESSAGE STR_MAIL_CASHITEM_BUY(int itemId) {
		return new SM_SYSTEM_MESSAGE(1300956, "[item:" + itemId + "]");
	}

	/**
	 * Purchase Item\n%attachItemName you bought has arrived.
	 */
	public static SM_SYSTEM_MESSAGE STR_MAIL_CASHITEM_GIFT(int itemId) {
		return new SM_SYSTEM_MESSAGE(1300957, "[item:" + itemId + "]");
	}

	/**
	 * You can only send mails to other users of your race.
	 */
	public static SM_SYSTEM_MESSAGE STR_MAIL_MSG_DIFFERENT_RACE() {
		return new SM_SYSTEM_MESSAGE(1300958);
	}

	/**
	 * You cannot fly in this area.
	 */
	public static SM_SYSTEM_MESSAGE STR_FLYING_FORBIDDEN_ZONE() {
		return new SM_SYSTEM_MESSAGE(1300959);
	}

	/**
	 * You cannot fly in this area.
	 */
	public static SM_SYSTEM_MESSAGE STR_FLYING_FORBIDDEN_HERE() {
		return new SM_SYSTEM_MESSAGE(1300960);
	}

	/**
	 * Flight cooldown time has not expired yet.
	 */
	public static SM_SYSTEM_MESSAGE STR_FLYING_TIME_NOT_READY() {
		return new SM_SYSTEM_MESSAGE(1300961);
	}

	/**
	 * Some options are applied when the game is restarted.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_APPLY_OPTION_WHEN_RESTART() {
		return new SM_SYSTEM_MESSAGE(1300963);
	}

	/**
	 * You cannot use special characters in channel name and password.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_INVALID_CHANNEL_NAME_SPECIAL_LETTER() {
		return new SM_SYSTEM_MESSAGE(1300964);
	}

	/**
	 * You used %num0 Abyss Points.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_USE_ABYSSPOINT(int num0) {
		return new SM_SYSTEM_MESSAGE(1300965, num0);
	}

	/**
	 * You have invited %0 to join the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_INVITE_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300966, value0);
	}

	/**
	 * You have received an alliance invitation from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_INVITE_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1300967, value0);
	}

	/**
	 * You have invited %0's group to the alliance. %0's group has a total of %1 members.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_INVITE_PARTY(String value0, int value1) {
		return new SM_SYSTEM_MESSAGE(1300968, value0, value1);
	}

	/**
	 * The leader of %0's group is %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_INVITE_PARTY_HIM(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300969, value0, value1);
	}

	/**
	 * Your group has received an alliance invitation from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_INVITE_PARTY_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1300970, value0);
	}

	/**
	 * You have declined %0's invitation to join the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_REJECT_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1300971, value0);
	}

	/**
	 * %0 has declined your invitation to join the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_REJECT_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300972, value0);
	}

	/**
	 * %0's group has declined your invitation to join the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_REJECT_PARTY(String value0) {
		return new SM_SYSTEM_MESSAGE(1300973, value0);
	}

	/**
	 * %0 is already a member of another alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ALREADY_OTHER_FORCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1300974, value0);
	}

	/**
	 * There is not enough room in the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_INVITE_FAILED_NOT_ENOUGH_SLOT() {
		return new SM_SYSTEM_MESSAGE(1300975);
	}

	/**
	 * You have no authority in the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_RIGHT_NOT_HAVE() {
		return new SM_SYSTEM_MESSAGE(1300976);
	}

	/**
	 * You have left the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_LEAVE_ME() {
		return new SM_SYSTEM_MESSAGE(1300977);
	}

	/**
	 * %0 has left the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_LEAVE_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1300978, value0);
	}

	/**
	 * %0 has kicked you out of the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_BAN_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1300979, value0);
	}

	/**
	 * %0 has kicked out %1 of the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_BAN_HIM(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1300980, value0, value1);
	}

	/**
	 * %0 has left the alliance due to a prolonged absence.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_LEAVE_TIMEOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300981, value0);
	}

	/**
	 * %0 is now Captain of the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CHANGE_LEADER_TIMEOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1300982, value0);
	}

	/**
	 * The alliance has disbanded due to a lack of members.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_DISPERSED() {
		return new SM_SYSTEM_MESSAGE(1300983);
	}

	/**
	 * %0 is now vice Captain of the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_PROMOTE_MANAGER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300984, value0);
	}

	/**
	 * %0 has been demoted to member from vice Captain.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_DEMOTE_MANAGER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300985, value0);
	}

	/**
	 * %0 has promoted %1. From now on, %1 is the alliance captain.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CHANGE_LEADER(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1300986, value0, value1, value2);
	}

	/**
	 * You have failed to change the alliance group as another person is already trying to change it. Please try again later.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_GROUP_FAILED_ALREADY_CHANGED() {
		return new SM_SYSTEM_MESSAGE(1300987);
	}

	/**
	 * You have failed to change the group because there was no group to change.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_GROUP_FAILED_ALREADY_LEAVE() {
		return new SM_SYSTEM_MESSAGE(1300988);
	}

	/**
	 * Checking the readiness of the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CHECK_START() {
		return new SM_SYSTEM_MESSAGE(1300989);
	}

	/**
	 * %0 has requested to check the combat readiness.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CHECK_REQUEST(String value0) {
		return new SM_SYSTEM_MESSAGE(1300990, value0);
	}

	/**
	 * All alliance members are ready.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CHECK_COMPLETE() {
		return new SM_SYSTEM_MESSAGE(1300991);
	}

	/**
	 * Currently Absent:
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CHECK_OUT() {
		return new SM_SYSTEM_MESSAGE(1300992);
	}

	/**
	 * Ready:
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CHECK_READY() {
		return new SM_SYSTEM_MESSAGE(1300993);
	}

	/**
	 * Not Ready:
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CHECK_DENIED() {
		return new SM_SYSTEM_MESSAGE(1300994);
	}

	/**
	 * You cancelled the request to check the readiness of the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CHECK_CANCEL() {
		return new SM_SYSTEM_MESSAGE(1300995);
	}

	/**
	 * You cannot invite any more members to the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANT_ADD_NEW_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1300996);
	}

	/**
	 * Only the alliance Captain can make another person the Captain.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ONLY_LEADER_CAN_CHANGE_LEADER() {
		return new SM_SYSTEM_MESSAGE(1300997);
	}

	/**
	 * %0 is now the alliance captain.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_HE_IS_NEW_LEADER(String value0) {
		return new SM_SYSTEM_MESSAGE(1300998, value0);
	}

	/**
	 * You are now the alliance captain.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_YOU_BECOME_NEW_LEADER() {
		return new SM_SYSTEM_MESSAGE(1300999);
	}

	/**
	 * Only the alliance captain can change the item distribution method.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ONLY_LEADER_CAN_CHANGE_LOOTING() {
		return new SM_SYSTEM_MESSAGE(1301000);
	}

	/**
	 * The item distribution method of the alliance has been changed to Manual.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_LOOTING_CHANGED_TO_MANUAL() {
		return new SM_SYSTEM_MESSAGE(1301001);
	}

	/**
	 * The item distribution method of the alliance has been changed to Auto.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_LOOTING_CHANGED_TO_AUTO() {
		return new SM_SYSTEM_MESSAGE(1301002);
	}

	/**
	 * There is no target to invite to the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_NO_USER_TO_INVITE() {
		return new SM_SYSTEM_MESSAGE(1301003);
	}

	/**
	 * Only the alliance Captain and vice Captain can invite people to the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ONLY_LEADER_CAN_INVITE() {
		return new SM_SYSTEM_MESSAGE(1301004);
	}

	/**
	 * %0 has declined your invitation to join the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_HE_REJECT_INVITATION(String value0) {
		return new SM_SYSTEM_MESSAGE(1301005, value0);
	}

	/**
	 * You cannot invite yourself to the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CAN_NOT_INVITE_SELF() {
		return new SM_SYSTEM_MESSAGE(1301006);
	}

	/**
	 * You cannot issue invitations while you are dead.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANT_INVITE_WHEN_DEAD() {
		return new SM_SYSTEM_MESSAGE(1301007);
	}

	/**
	 * The selected alliance member is currently offline.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_OFFLINE_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1301008);
	}

	/**
	 * Only the alliance captain can kick out a member.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ONLY_LEADER_CAN_BANISH() {
		return new SM_SYSTEM_MESSAGE(1301009);
	}

	/**
	 * You have been kicked out of the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_YOU_ARE_BANISHED() {
		return new SM_SYSTEM_MESSAGE(1301010);
	}

	/**
	 * The alliance has been disbanded.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_IS_DISPERSED() {
		return new SM_SYSTEM_MESSAGE(1301011);
	}

	/**
	 * %0 has left the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_HE_LEAVE_FORCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1301012, value0);
	}

	/**
	 * %0 is a member of another alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_HE_IS_ALREADY_MEMBER_OF_OTHER_FORCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1301013, value0);
	}

	/**
	 * %0 is already a member of your alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_HE_IS_ALREADY_MEMBER_OF_OUR_FORCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1301014, value0);
	}

	/**
	 * You are not in an alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_YOU_ARE_NOT_FORCE_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1301015);
	}

	/**
	 * You are not an alliance member.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_NOT_FORCE_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1301016);
	}

	/**
	 * You have invited %0 to join the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_INVITED_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1301017, value0);
	}

	/**
	 * Currently, %0 cannot accept your invitation to join the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANT_INVITE_WHEN_HE_IS_ASKED_QUESTION(String value0) {
		return new SM_SYSTEM_MESSAGE(1301018, value0);
	}

	/**
	 * %0 has been disconnected.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_HE_BECOME_OFFLINE(String value0) {
		return new SM_SYSTEM_MESSAGE(1301019, value0);
	}

	/**
	 * %0 has been offline for too long and had been automatically kicked out of the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_HE_BECOME_OFFLINE_TIMEOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1301020, value0);
	}

	/**
	 * %0 has been kicked out of the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_HE_IS_BANISHED(String value0) {
		return new SM_SYSTEM_MESSAGE(1301021, value0);
	}

	/**
	 * The rare item distribution method of the alliance has been changed to Free-for-All.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_RARE_LOOTING_CHANGED_TO_MANUAL() {
		return new SM_SYSTEM_MESSAGE(1301022);
	}

	/**
	 * The rare item distribution method of the alliance has been changed to Auto.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_RARE_LOOTING_CHANGED_TO_AUTO() {
		return new SM_SYSTEM_MESSAGE(1301023);
	}

	/**
	 * The rare item distribution method of the alliance has been changed to Dice Roll.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_RARE_LOOTING_CHANGED_TO_DICE() {
		return new SM_SYSTEM_MESSAGE(1301024);
	}

	/**
	 * An alliance member cannot be kicked out before the items have been distributed.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANNOT_BANISH_ITEMPOOL_NOT_EMPTY() {
		return new SM_SYSTEM_MESSAGE(1301025);
	}

	/**
	 * %0 rolled the dice and got a %num1.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ITEM_DICE(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1301026, value0, num1);
	}

	/**
	 * You can roll the dice once more if the rolled number is less than 100.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ITEM_DICE_AGAIN() {
		return new SM_SYSTEM_MESSAGE(1301027);
	}

	/**
	 * The item distribution method of the alliance has been changed to Free-for-All.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_LOOTING_CHANGED_TO_FREEFORALL() {
		return new SM_SYSTEM_MESSAGE(1301028);
	}

	/**
	 * The item distribution method of the alliance has been changed to Round-robin.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_LOOTING_CHANGED_TO_ROUNDROBIN() {
		return new SM_SYSTEM_MESSAGE(1301029);
	}

	/**
	 * The item distribution method of the alliance has been changed to Captain.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_LOOTING_CHANGED_TO_LEADERONLY() {
		return new SM_SYSTEM_MESSAGE(1301030);
	}

	/**
	 * %0 has been kicked out of the arena.
	 */
	public static SM_SYSTEM_MESSAGE STR_PvPZONE_OUT_MESSAGE(String value0) {
		return new SM_SYSTEM_MESSAGE(1301031, value0);
	}

	/**
	 * You cannot fly while you are banned from flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_FLY_NOW_DUE_TO_NOFLY() {
		return new SM_SYSTEM_MESSAGE(1301032);
	}

	/**
	 * The %0 %1 is activating the %2 Artifact.
	 */
	public static SM_SYSTEM_MESSAGE STR_ARTIFACT_CASTING(String race, String name, String artifact) {
		return new SM_SYSTEM_MESSAGE(1301033, race, name, artifact);
	}

	/**
	 * The %1 Artifact core in %0 possession has been deactivated.
	 */
	public static SM_SYSTEM_MESSAGE STR_ARTIFACT_CORE_CASTING(String race, String artifact) {
		return new SM_SYSTEM_MESSAGE(1301034, race, artifact);
	}

	/**
	 * The activation of the %1 Artifact in %0 possession has been cancelled.
	 */
	public static SM_SYSTEM_MESSAGE STR_ARTIFACT_CANCELED(String race, String artifact) {
		return new SM_SYSTEM_MESSAGE(1301035, race, artifact);
	}

	/**
	 * The %0 %1 has succeeded in activating the %2 Artifact.
	 */
	public static SM_SYSTEM_MESSAGE STR_ARTIFACT_FIRE(String race, String name, String artifact) {
		return new SM_SYSTEM_MESSAGE(1301036, race, name, artifact);
	}

	/**
	 * %0 Legion lost %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_GUILD_CASTLE_TAKEN(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1301037, value0, value1);
	}

	/**
	 * %0 has conquered %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_GUILD_WIN_CASTLE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1301038, value0, value1);
	}

	/**
	 * %0 succeeded in conquering %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_WIN_CASTLE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1301039, value0, value1);
	}

	/**
	 * %0 is now vulnerable.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_PVP_ON(String value0) {
		return new SM_SYSTEM_MESSAGE(1301040, value0);
	}

	/**
	 * %0 is no longer vulnerable.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_PVP_OFF(String value0) {
		return new SM_SYSTEM_MESSAGE(1301041, value0);
	}

	/**
	 * The Dredgion has disgorged a horde of Balaur troopers.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_CARRIER_DROP_DRAGON() {
		return new SM_SYSTEM_MESSAGE(1301042);
	}

	/**
	 * The Balaur Teleport Raiders appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_WARP_DRAGON() {
		return new SM_SYSTEM_MESSAGE(1301043);
	}

	/**
	 * A dredgion has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_CARRIER_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1301044);
	}

	/**
	 * Cannot find the target to use the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_CANT_FIND_VALID_TARGET() {
		return new SM_SYSTEM_MESSAGE(1301045);
	}

	/**
	 * %0 failed to defend %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_CASTLE_TAKEN(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1301046, value0, value1);
	}

	/**
	 * The %0 item has been sold by the broker.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_REGISTER_SOLD_OUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1301047, value0);
	}

	/**
	 * %1 of the %0 killed the Aetheric Field Generator.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_SHIELD_BROKEN(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1301048, value1, value0);
	}

	/**
	 * %1 of the %0 destroyed the Castle Gate.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_DOOR_BROKEN(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1301049, value1, value0);
	}

	/**
	 * The Castle Gate is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_DOOR_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1301050);
	}

	/**
	 * The Castle Gate is in danger.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_DOOR_ATSTAKE() {
		return new SM_SYSTEM_MESSAGE(1301051);
	}

	/**
	 * The Aetheric Field Generator is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_SHIELD_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1301052);
	}

	/**
	 * The Gate Guardian Stone is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_REPAIR_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1301053);
	}

	/**
	 * %1 of the %0 destroyed the Gate Guardian Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_REPAIR_BROKEN(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1301054, value1, value0);
	}

	/**
	 * The Guardian General is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_BOSS_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1301055);
	}

	/**
	 * You cannot start gliding as you are moving too slowly.
	 */
	public static SM_SYSTEM_MESSAGE STR_GLIDE_NOT_ENOUGH_SPEED_FOR_GLIDE() {
		return new SM_SYSTEM_MESSAGE(1301056);
	}

	/**
	 * You cannot start gliding while in an Altered State.
	 */
	public static SM_SYSTEM_MESSAGE STR_GLIDE_CANNOT_GLIDE_ABNORMAL_STATUS() {
		return new SM_SYSTEM_MESSAGE(1301057);
	}

	/**
	 * You cannot change to the combat mode while gliding.
	 */
	public static SM_SYSTEM_MESSAGE STR_GLIDE_CANNOT_GLIDE_COMBAT_MODE() {
		return new SM_SYSTEM_MESSAGE(1301058);
	}

	/**
	 * You can glide when you become a Daeva.
	 */
	public static SM_SYSTEM_MESSAGE STR_GLIDE_ONLY_DEVA_CAN() {
		return new SM_SYSTEM_MESSAGE(1301059);
	}

	/**
	 * You do not have enough mana to continue using %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_INSUFFICIENT_COST_FOR_TOGGLE_SKILLL(String value0) {
		return new SM_SYSTEM_MESSAGE(1301060, value0);
	}

	/**
	 * You cannot appoint any more vice captains. The alliance can have a maximum of 4.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANNOT_PROMOTE_MANAGER() {
		return new SM_SYSTEM_MESSAGE(1301061);
	}

	/**
	 * %0 has been activated.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_PROC_EFFECT_OCCURRED(String value0) {
		return new SM_SYSTEM_MESSAGE(1301062, value0);
	}

	/**
	 * You are already under the same effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_SAME_EFFECT_ALREADY_TAKEN() {
		return new SM_SYSTEM_MESSAGE(1301063);
	}

	/**
	 * You declined %0's challenge for a duel.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_REJECT_DUEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1301064, value0);
	}

	/**
	 * %0 has challenged you to a duel.
	 */
	public static SM_SYSTEM_MESSAGE STR_DUEL_REQUESTED(String value0) {
		return new SM_SYSTEM_MESSAGE(1301065, value0);
	}

	/**
	 * You are currently unable to chat.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_DISABLED() {
		return new SM_SYSTEM_MESSAGE(1310000);
	}

	/**
	 * You are unable to chat for 2 minutes as you interrupted the game play through unnecessary chatting.
	 */
	public static SM_SYSTEM_MESSAGE STR_FLOODING() {
		return new SM_SYSTEM_MESSAGE(1310001);
	}

	/**
	 * Characters under level %0 cannot chat.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_CHAT_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1310002, value0);
	}

	/**
	 * You have too many users blocked from chatting with you.
	 */
	public static SM_SYSTEM_MESSAGE STR_TOO_MANY_EXCLUDE() {
		return new SM_SYSTEM_MESSAGE(1310003);
	}

	/**
	 * Characters under level %0 cannot send whispers.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_WHISPER_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1310004, value0);
	}

	/**
	 * The NPC server is down. Please restore it soon.
	 */
	public static SM_SYSTEM_MESSAGE STR_NPC_SERVER_DOWN() {
		return new SM_SYSTEM_MESSAGE(1310005);
	}

	/**
	 * The connection with the cache server has been severed. Please restore it soon.
	 */
	public static SM_SYSTEM_MESSAGE STR_CACHE_SERVER_DOWN() {
		return new SM_SYSTEM_MESSAGE(1310006);
	}

	/**
	 * The connection with the authorization server has been severed. Please restore it soon.
	 */
	public static SM_SYSTEM_MESSAGE STR_AUTH_SERVER_DOWN() {
		return new SM_SYSTEM_MESSAGE(1310007);
	}

	/**
	 * The connection with the ittem billing server has been severed. Please restore it soon.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_BILLING_SERVER_DOWN() {
		return new SM_SYSTEM_MESSAGE(1310008);
	}

	/**
	 * You disabled chatting for %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_DISABLED_OTHER_CHAT(String value0) {
		return new SM_SYSTEM_MESSAGE(1310009, value0);
	}

	/**
	 * This is an Assist Target Key. Use it after you have selected a target.
	 */
	public static SM_SYSTEM_MESSAGE STR_ASSISTKEY_THIS_IS_ASSISTKEY() {
		return new SM_SYSTEM_MESSAGE(1310010);
	}

	/**
	 * Please use the right NPC for your race to register items.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_RACECHECK() {
		return new SM_SYSTEM_MESSAGE(1310011);
	}

	/**
	 * You cannot chat while you are dead.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_CHAT_AT_DIE() {
		return new SM_SYSTEM_MESSAGE(1310012);
	}

	/**
	 * You cannot quit during a battle. Canceling in %0 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_QUIT_DURING_BATTLE(String value0) {
		return new SM_SYSTEM_MESSAGE(1310013, value0);
	}

	/**
	 * An error has occurred while restoring the login list on the Billing server.
	 */
	public static SM_SYSTEM_MESSAGE STR_KICK_BILLGATES_ERROR() {
		return new SM_SYSTEM_MESSAGE(1310014);
	}

	/**
	 * A dual login error has occurred while trying to enter the world.
	 */
	public static SM_SYSTEM_MESSAGE STR_KICK_DUAL_LOGIN_ON_ABOUT_TO_PLAY() {
		return new SM_SYSTEM_MESSAGE(1310015);
	}

	/**
	 * Your account has been banned.
	 */
	public static SM_SYSTEM_MESSAGE STR_KICK_BANNED() {
		return new SM_SYSTEM_MESSAGE(1310016);
	}

	/**
	 * You have been disconnected from the server.
	 */
	public static SM_SYSTEM_MESSAGE STR_KICK_CHARACTER() {
		return new SM_SYSTEM_MESSAGE(1310017);
	}

	/**
	 * Your World access time limit has been exceeded.
	 */
	public static SM_SYSTEM_MESSAGE STR_KICK_ABOUT_TO_PLAY_TIMER_EXPIRED() {
		return new SM_SYSTEM_MESSAGE(1310018);
	}

	/**
	 * The requested target no longer exists.
	 */
	public static SM_SYSTEM_MESSAGE STR_SEARCH_NOT_EXIST() {
		return new SM_SYSTEM_MESSAGE(1310019);
	}

	/**
	 * The user you requested is currently offline.
	 */
	public static SM_SYSTEM_MESSAGE STR_SEARCH_DISCONNECT() {
		return new SM_SYSTEM_MESSAGE(1310020);
	}

	/**
	 * Only Daevas can use that.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_USE_GROUPGATE_BEFORE_CHANGE_CLASS() {
		return new SM_SYSTEM_MESSAGE(1310021);
	}

	/**
	 * You do not have enough credit left in the account.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_NOT_PAID() {
		return new SM_SYSTEM_MESSAGE(1310022);
	}

	/**
	 * Invalid session info.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_INVALID_SESSION() {
		return new SM_SYSTEM_MESSAGE(1310023);
	}

	/**
	 * The server list info in the server is incorrect.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_SERVERLIST_INCORRECT() {
		return new SM_SYSTEM_MESSAGE(1310024);
	}

	/**
	 * Failed to create the character due to a World DB error.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_WORLD_DB_FAIL() {
		return new SM_SYSTEM_MESSAGE(1310025);
	}

	/**
	 * You are disconnected from the game server.
	 */
	public static SM_SYSTEM_MESSAGE STR_ERROR_WORLD_CONNECTION_LOST() {
		return new SM_SYSTEM_MESSAGE(1310026);
	}

	/**
	 * Failed to connect to the game server.
	 */
	public static SM_SYSTEM_MESSAGE STR_ERROR_WORLD_CONNECTION_FAIL() {
		return new SM_SYSTEM_MESSAGE(1310027);
	}

	/**
	 * The client version is not compatible with the game server.
	 */
	public static SM_SYSTEM_MESSAGE STR_ERROR_WORLD_VERSION_FAIL() {
		return new SM_SYSTEM_MESSAGE(1310028);
	}

	/**
	 * Characters of different races exist in the same server.
	 */
	public static SM_SYSTEM_MESSAGE STR_ERROR_WORLD_HAS_MULTIPLE_RACE() {
		return new SM_SYSTEM_MESSAGE(1310029);
	}

	/**
	 * The NPC script version is not compatible with the game server.
	 */
	public static SM_SYSTEM_MESSAGE STR_ERROR_NPC_SCRIPT_VERSION_FAIL() {
		return new SM_SYSTEM_MESSAGE(1310030);
	}

	/**
	 * An unknown error has occurred while checking the game server version.
	 */
	public static SM_SYSTEM_MESSAGE STR_ERROR_UNKNOWN_VERSION_FAIL() {
		return new SM_SYSTEM_MESSAGE(1310031);
	}

	/**
	 * Failed to delete the character.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_DEL_CHAR_FAIL() {
		return new SM_SYSTEM_MESSAGE(1310032);
	}

	/**
	 * Cannot connect to the login server.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_CONNECTION_FAIL() {
		return new SM_SYSTEM_MESSAGE(1310033);
	}

	/**
	 * That character does not exist.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_CHAR_NOT_EXIST() {
		return new SM_SYSTEM_MESSAGE(1310034);
	}

	/**
	 * That character is already set to be deleted.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_CHAR_ALREADY_DELETED() {
		return new SM_SYSTEM_MESSAGE(1310035);
	}

	/**
	 * Failed to create the character.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_FAILED_TO_CREATE_CHAR() {
		return new SM_SYSTEM_MESSAGE(1310036);
	}

	/**
	 * A character with that name already exists.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_CHARACTER_EXIST() {
		return new SM_SYSTEM_MESSAGE(1310037);
	}

	/**
	 * You cannot create any more characters on this server.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_NO_AVAILABLE_SLOT() {
		return new SM_SYSTEM_MESSAGE(1310038);
	}

	/**
	 * Invalid server ID.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_INVALID_SERVERID() {
		return new SM_SYSTEM_MESSAGE(1310039);
	}

	/**
	 * Too many users on the game server. You cannot access the game.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_TOO_MANY_USER() {
		return new SM_SYSTEM_MESSAGE(1310040);
	}

	/**
	 * The game server memory is full. You cannot access the game.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_OUT_OF_MEMORY() {
		return new SM_SYSTEM_MESSAGE(1310041);
	}

	/**
	 * The selected character is already playing on the selected server.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_ALREADY_PLAYING() {
		return new SM_SYSTEM_MESSAGE(1310042);
	}

	/**
	 * You cannot create any more characters on that account.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_MAX_CHAR_COUNT() {
		return new SM_SYSTEM_MESSAGE(1310043);
	}

	/**
	 * Invalid character name.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_INVALID_NAME() {
		return new SM_SYSTEM_MESSAGE(1310044);
	}

	/**
	 * Invalid character gender.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_INVALID_GENDER() {
		return new SM_SYSTEM_MESSAGE(1310045);
	}

	/**
	 * Invalid character class.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_INVALID_CLASS() {
		return new SM_SYSTEM_MESSAGE(1310046);
	}

	/**
	 * The game server is down.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_SERVER_DOWN() {
		return new SM_SYSTEM_MESSAGE(1310047);
	}

	/**
	 * The Billing server is down.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_BILLGATES_DOWN() {
		return new SM_SYSTEM_MESSAGE(1310048);
	}

	/**
	 * Internal game server error
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_ERROR_INTERNAL_SERVER_ERROR() {
		return new SM_SYSTEM_MESSAGE(1310049);
	}

	/**
	 * You have been disconnected from the server by request of the PlayNC Homepage.
	 */
	public static SM_SYSTEM_MESSAGE STR_L2AUTH_S_KICKED_BY_WEB() {
		return new SM_SYSTEM_MESSAGE(1310050);
	}

	/**
	 * You are not old enough to play the game.
	 */
	public static SM_SYSTEM_MESSAGE STR_L2AUTH_S_UNDER_AGE() {
		return new SM_SYSTEM_MESSAGE(1310051);
	}

	/**
	 * Double login attempts have been detected.
	 */
	public static SM_SYSTEM_MESSAGE STR_L2AUTH_S_KICKED_DOUBLE_LOGIN() {
		return new SM_SYSTEM_MESSAGE(1310052);
	}

	/**
	 * You are already logged in.
	 */
	public static SM_SYSTEM_MESSAGE STR_L2AUTH_S_ALREADY_PLAY_GAME() {
		return new SM_SYSTEM_MESSAGE(1310053);
	}

	/**
	 * Sorry, the queue is full. Please try another server.
	 */
	public static SM_SYSTEM_MESSAGE STR_L2AUTH_S_LIMIT_EXCEED() {
		return new SM_SYSTEM_MESSAGE(1310054);
	}

	/**
	 * The server is currently unavailable. Please try again later.
	 */
	public static SM_SYSTEM_MESSAGE STR_L2AUTH_S_SEVER_CHECK() {
		return new SM_SYSTEM_MESSAGE(1310055);
	}

	/**
	 * Please login to the game after you have changed your password.
	 */
	public static SM_SYSTEM_MESSAGE STR_L2AUTH_S_MODIFY_PASSWORD() {
		return new SM_SYSTEM_MESSAGE(1310056);
	}

	/**
	 * Either the usage period has expired or we are experiencing a temporary connection difficulty. For more information, please contact the
	 * administrators or our customer center.
	 */
	public static SM_SYSTEM_MESSAGE STR_L2AUTH_S_NOT_PAID() {
		return new SM_SYSTEM_MESSAGE(1310057);
	}

	/**
	 * You have used up your allocated time and there is no time left on this account.
	 */
	public static SM_SYSTEM_MESSAGE STR_L2AUTH_S_NO_SPECIFICTIME() {
		return new SM_SYSTEM_MESSAGE(1310058);
	}

	/**
	 * System error.
	 */
	public static SM_SYSTEM_MESSAGE STR_L2AUTH_S_SYSTEM_ERROR() {
		return new SM_SYSTEM_MESSAGE(1310059);
	}

	/**
	 * You cannot open a private store in the arena.
	 */
	public static SM_SYSTEM_MESSAGE STR_PvPZONE_CANNOT_OPEN_MARKET() {
		return new SM_SYSTEM_MESSAGE(1310060);
	}

	/**
	 * You cannot continue unless you stop flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_CANNOT_PROCESS_IN_FLIGHT() {
		return new SM_SYSTEM_MESSAGE(1310061);
	}

	/**
	 * You have gained %num0 Abyss Points.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_ABYSS_POINT_GAIN(int num0) {
		return new SM_SYSTEM_MESSAGE(1320000, num0);
	}

	/**
	 * A one-way Rift into Asmodae has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DARK_SIDE_DIRECT_PORTAL_OPEN() {
		return new SM_SYSTEM_MESSAGE(1320001);
	}

	/**
	 * %1 of %0 has captured the %2 Artifact.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_EVENT_WIN_ARTIFACT(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1320002, value0, value1, value2);
	}

	/**
	 * %0 has conquered %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_EVENT_WIN_FORT(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1320003, value0, value1);
	}

	/**
	 * The %0 Artifact has been lost to %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_EVENT_LOSE_ARTIFACT(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1320004, value0, value1);
	}

	/**
	 * %0 Legion lost %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_EVENT_LOSE_FORT(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1320005, value0, value1);
	}

	/**
	 * Someone is already gathering it.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_OUCCPIED_BY_OTHER() {
		return new SM_SYSTEM_MESSAGE(1330000);
	}

	/**
	 * Your %0 skill level is not high enough.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_OUT_OF_SKILL_POINT(String value0) {
		return new SM_SYSTEM_MESSAGE(1330001, value0);
	}

	/**
	 * You are too far from the object to gather it.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_TOO_FAR_FROM_GATHER_SOURCE() {
		return new SM_SYSTEM_MESSAGE(1330002);
	}

	/**
	 * You cannot gather as there are obstacles blocking the way.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_OBSTACLE_EXIST() {
		return new SM_SYSTEM_MESSAGE(1330003);
	}

	/**
	 * You have learned the %0 skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_LEARNED_NEW_GATHER_SKILL(String value0) {
		return new SM_SYSTEM_MESSAGE(1330004, value0);
	}

	/**
	 * Your %0 skill has been upgraded to %1 points.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_SKILL_POINT_UP(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1330005, value0, value1);
	}

	/**
	 * You do not have the basic gathering tools.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_NO_TOOL_1_BASIC() {
		return new SM_SYSTEM_MESSAGE(1330006);
	}

	/**
	 * You do not have the harvesting tools.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_NO_TOOL_2_GATHER() {
		return new SM_SYSTEM_MESSAGE(1330007);
	}

	/**
	 * You do not have the mining tools.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_NO_TOOL_3_MINING() {
		return new SM_SYSTEM_MESSAGE(1330008);
	}

	/**
	 * You do not have the fishing tools.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_NO_TOOL_4_FISHING() {
		return new SM_SYSTEM_MESSAGE(1330009);
	}

	/**
	 * You do not have the forestry tools.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_NO_TOOL_5_FORESTRY() {
		return new SM_SYSTEM_MESSAGE(1330010);
	}

	/**
	 * You are harvesting %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_START_2_GATHER(String value0) {
		return new SM_SYSTEM_MESSAGE(1330012, value0);
	}

	/**
	 * You are mining %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_START_3_MINING(String value0) {
		return new SM_SYSTEM_MESSAGE(1330013, value0);
	}

	/**
	 * You are fishing %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_START_4_FISHING(String value0) {
		return new SM_SYSTEM_MESSAGE(1330014, value0);
	}

	/**
	 * You are felling %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_START_5_FORESTRY(String value0) {
		return new SM_SYSTEM_MESSAGE(1330015, value0);
	}

	/**
	 * You have gathered %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_SUCCESS_1_BASIC(String value0) {
		return new SM_SYSTEM_MESSAGE(1330016, value0);
	}

	/**
	 * You have harvested %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_SUCCESS_2_GATHER(String value0) {
		return new SM_SYSTEM_MESSAGE(1330017, value0);
	}

	/**
	 * You have mined %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_SUCCESS_3_MINING(String value0) {
		return new SM_SYSTEM_MESSAGE(1330018, value0);
	}

	/**
	 * You have caught %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_SUCCESS_4_FISHING(String value0) {
		return new SM_SYSTEM_MESSAGE(1330019, value0);
	}

	/**
	 * You have acquired %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_SUCCESS_5_FORESTRY(String value0) {
		return new SM_SYSTEM_MESSAGE(1330020, value0);
	}

	/**
	 * You have failed to gather %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_FAIL_1_BASIC(String value0) {
		return new SM_SYSTEM_MESSAGE(1330021, value0);
	}

	/**
	 * You have failed to harvest %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_FAIL_2_GATHER(String value0) {
		return new SM_SYSTEM_MESSAGE(1330022, value0);
	}

	/**
	 * You have failed to mine %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_FAIL_3_MINING(String value0) {
		return new SM_SYSTEM_MESSAGE(1330023, value0);
	}

	/**
	 * You have failed to catch %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_FAIL_4_FISHING(String value0) {
		return new SM_SYSTEM_MESSAGE(1330024, value0);
	}

	/**
	 * You have failed to acquire %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_FAIL_5_FORESTRY(String value0) {
		return new SM_SYSTEM_MESSAGE(1330025, value0);
	}

	/**
	 * You have stopped gathering.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_CANCEL_1_BASIC() {
		return new SM_SYSTEM_MESSAGE(1330026);
	}

	/**
	 * You have stopped harvesting.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_CANCEL_2_GATHER() {
		return new SM_SYSTEM_MESSAGE(1330027);
	}

	/**
	 * You have stopped mining.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_CANCEL_3_MINING() {
		return new SM_SYSTEM_MESSAGE(1330028);
	}

	/**
	 * You have stopped fishing.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_CANCEL_4_FISHING() {
		return new SM_SYSTEM_MESSAGE(1330029);
	}

	/**
	 * You have stopped felling.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_CANCEL_5_FORESTRY() {
		return new SM_SYSTEM_MESSAGE(1330030);
	}

	/**
	 * You must be equipped with the basic gathering tools.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_EQUIP_1_BASIC() {
		return new SM_SYSTEM_MESSAGE(1330031);
	}

	/**
	 * You must be equipped with a hoe.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_EQUIP_2_GATHER() {
		return new SM_SYSTEM_MESSAGE(1330032);
	}

	/**
	 * You must be equipped with a pick.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_EQUIP_3_MINING() {
		return new SM_SYSTEM_MESSAGE(1330033);
	}

	/**
	 * You must be equipped with a fishing rod.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_EQUIP_4_FISHING() {
		return new SM_SYSTEM_MESSAGE(1330034);
	}

	/**
	 * You must be equipped with an axe.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_EQUIP_5_FORESTRY() {
		return new SM_SYSTEM_MESSAGE(1330035);
	}

	/**
	 * You must have at least one free space in your cube to gather.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_INVENTORY_IS_FULL() {
		return new SM_SYSTEM_MESSAGE(1330036);
	}

	/**
	 * You must have at least one free space in your cube to craft.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_INVENTORY_IS_FULL() {
		return new SM_SYSTEM_MESSAGE(1330037);
	}

	/**
	 * You cannot craft while in an altered state.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_CAN_NOT_COMBINE_WHILE_IN_ABNORMAL_STATE() {
		return new SM_SYSTEM_MESSAGE(1330038);
	}

	/**
	 * You are already crafting.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_ALREADY_COMBINING() {
		return new SM_SYSTEM_MESSAGE(1330039);
	}

	/**
	 * You are too far from %0 to craft.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_TOO_FAR_FROM_TOOL(String value0) {
		return new SM_SYSTEM_MESSAGE(1330040, value0);
	}

	/**
	 * You cannot craft as you do not have %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_DO_NOT_HAVE_TOOL(String value0) {
		return new SM_SYSTEM_MESSAGE(1330041, value0);
	}

	/**
	 * You cannot start crafting as you have not learned the %0 skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_CANT_USE(String value0) {
		return new SM_SYSTEM_MESSAGE(1330042, value0);
	}

	/**
	 * Cannot find the design.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_CAN_NOT_FIND_RECIPE() {
		return new SM_SYSTEM_MESSAGE(1330043);
	}

	/**
	 * Your %0 skill is not good enough yet.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_OUT_OF_SKILL_POINT(String value0) {
		return new SM_SYSTEM_MESSAGE(1330044, value0);
	}

	/**
	 * You cannot craft as you do not have a required item.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_NO_COMPONENT_ITEM_IN_RECIPE() {
		return new SM_SYSTEM_MESSAGE(1330045);
	}

	/**
	 * You cannot craft as you do not have %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_NO_COMPONENT_ITEM_SINGLE(String value0) {
		return new SM_SYSTEM_MESSAGE(1330046, value0);
	}

	/**
	 * You cannot craft as you do not have %num1 %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_NO_COMPONENT_ITEM_MULTIPLE(int num1, String value0) {
		return new SM_SYSTEM_MESSAGE(1330047, value0, num1);
	}

	/**
	 * You are crafting %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_START(String value0) {
		return new SM_SYSTEM_MESSAGE(1330048, value0);
	}

	/**
	 * You have crafted %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_SUCCESS(String value0) {
		return new SM_SYSTEM_MESSAGE(1330049, value0);
	}

	/**
	 * You have failed to craft %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_FAIL(String value0) {
		return new SM_SYSTEM_MESSAGE(1330050, value0);
	}

	/**
	 * You stopped crafting.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_CANCEL() {
		return new SM_SYSTEM_MESSAGE(1330051);
	}

	/**
	 * You must have learned the %0 skill to use this tool.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_CANT_USE_TOOL(String value0) {
		return new SM_SYSTEM_MESSAGE(1330052, value0);
	}

	/**
	 * Your %0 skill has been upgraded to %1 points.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_SKILL_POINT_UP(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1330053, value0, value1);
	}

	/**
	 * You must learn the %0 skill to start gathering.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_LEARN_SKILL(String value0) {
		return new SM_SYSTEM_MESSAGE(1330054, value0);
	}

	/**
	 * You cannot start crafting as there are obstacles blocking the way.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_OBSTACLE_EXIST() {
		return new SM_SYSTEM_MESSAGE(1330055);
	}

	/**
	 * You cannot craft while in combat.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_DISABLED_IN_COMBAT_MODE() {
		return new SM_SYSTEM_MESSAGE(1330056);
	}

	/**
	 * As you cannot craft while in combat mode, it will be closed automatically.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_CLOSED_FOR_COMBAT_MODE() {
		return new SM_SYSTEM_MESSAGE(1330057);
	}

	/**
	 * You have gathered successfully.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHERING_SUCCESS_GETEXP() {
		return new SM_SYSTEM_MESSAGE(1330058);
	}

	/**
	 * You have crafted successfully.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_SUCCESS_GETEXP() {
		return new SM_SYSTEM_MESSAGE(1330059);
	}

	/**
	 * You have already learned this design.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_RECIPE_LEARNED_ALREADY() {
		return new SM_SYSTEM_MESSAGE(1330060);
	}

	/**
	 * You have learned %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_RECIPE_LEARN(int value0, String name) {
		return new SM_SYSTEM_MESSAGE(1330061, "[recipe_ex:" + value0 + ";" + name + "]");
	}

	/**
	 * You cannot learn the design because you have not learned the %0 skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_RECIPE_CANT_LEARN_SKILL(String value0) {
		return new SM_SYSTEM_MESSAGE(1330062, value0);
	}

	/**
	 * You cannot learn the design because your skill level is not high enough.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_RECIPE_CANT_LEARN_SKILLPOINT() {
		return new SM_SYSTEM_MESSAGE(1330063);
	}

	/**
	 * Maximum skill level of %0 has been upgraded to Level %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_INFO_UPGRADE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1330064, value0, value1);
	}

	/**
	 * Only Daevas can craft it.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_MSG_CAN_WORK_ONLY_DEVA() {
		return new SM_SYSTEM_MESSAGE(1330065);
	}

	/**
	 * You are a Daeva now. Leave this resource for Humans to use.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_INCORRECT_SKILL() {
		return new SM_SYSTEM_MESSAGE(1330066);
	}

	/**
	 * Maximum skill level of %0 has been upgraded to Level %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHERING_INFO_UPGRADE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1330067, value0, value1);
	}

	/**
	 * Maximum skill level of %0 has been upgraded to Level %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_AERIALGATHERING_INFO_UPGRADE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1330068, value0, value1);
	}

	/**
	 * You cannot be promoted any more.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBINE_CBT_CAP() {
		return new SM_SYSTEM_MESSAGE(1330069);
	}

	/**
	 * You cannot be promoted any more.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GATHER_CBT_CAP() {
		return new SM_SYSTEM_MESSAGE(1330070);
	}

	/**
	 * You cannot gather while afflicted with an altered state.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_EXTRACT_GATHER_WHILE_IN_ABNORMAL_STATE() {
		return new SM_SYSTEM_MESSAGE(1330071);
	}

	/**
	 * You cannot gather while in the current stance.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_EXTRACT_GATHER_WHILE_IN_CURRENT_STANCE() {
		return new SM_SYSTEM_MESSAGE(1330072);
	}

	/**
	 * You cannot gather while in the current position.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_EXTRACT_GATHER_IN_CURRENT_POSITION() {
		return new SM_SYSTEM_MESSAGE(1330073);
	}

	/**
	 * Someone else is gathering that object.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTRACT_GATHER_OCCUPIED_BY_OTHER() {
		return new SM_SYSTEM_MESSAGE(1330074);
	}

	/**
	 * You are too far from the target to gather it.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTRACT_GATHER_TOO_FAR_FROM_GATHER_SOURCE() {
		return new SM_SYSTEM_MESSAGE(1330075);
	}

	/**
	 * You cannot gather because an obstacle is in the way.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTRACT_GATHER_OBSTACLE_EXIST() {
		return new SM_SYSTEM_MESSAGE(1330076);
	}

	/**
	 * You have started gathering %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTRACT_GATHER_START_1_BASIC(String value0) {
		return new SM_SYSTEM_MESSAGE(1330077, value0);
	}

	/**
	 * You have gathered %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTRACT_GATHER_SUCCESS_1_BASIC(String value0) {
		return new SM_SYSTEM_MESSAGE(1330078, value0);
	}

	/**
	 * You have failed to gather %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTRACT_GATHER_FAIL_1_BASIC(String value0) {
		return new SM_SYSTEM_MESSAGE(1330079, value0);
	}

	/**
	 * You have stopped gathering.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTRACT_GATHER_CANCEL_1_BASIC() {
		return new SM_SYSTEM_MESSAGE(1330080);
	}

	/**
	 * You cannot gather unless there is at least one free space in your cube.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTRACT_GATHER_INVENTORY_IS_FULL() {
		return new SM_SYSTEM_MESSAGE(1330081);
	}

	/**
	 * You have gained experience from gathering.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTRACT_GATHERING_SUCCESS_GETEXP() {
		return new SM_SYSTEM_MESSAGE(1330082);
	}

	/**
	 * You cannot use the item until its gathering timer expires.
	 */
	public static SM_SYSTEM_MESSAGE STR_EXTRACT_GATHERING_CANT_USE_UNTIL_DELAY_TIME() {
		return new SM_SYSTEM_MESSAGE(1330083);
	}

	/**
	 * You have died.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_DEATH() {
		return new SM_SYSTEM_MESSAGE(1340000);
	}

	/**
	 * You were killed by %0's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PvPZONE_MY_DEATH_TO_B(String value0) {
		return new SM_SYSTEM_MESSAGE(1340001, value0);
	}

	/**
	 * You were killed by %0's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_DEATH_TO_B(String value0) {
		return new SM_SYSTEM_MESSAGE(1340002, value0);
	}

	/**
	 * %0 has died.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_FRIENDLY_DEATH(String value0) {
		return new SM_SYSTEM_MESSAGE(1350000, value0);
	}

	/**
	 * %0 was killed by %1's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_FRIENDLY_DEATH_TO_B(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1350001, value0, value1);
	}

	/**
	 * %0 has died.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_HOSTILE_DEATH(String value0) {
		return new SM_SYSTEM_MESSAGE(1360000, value0);
	}

	/**
	 * You have defeated %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PvPZONE_HOSTILE_DEATH_TO_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1360001, value0);
	}

	/**
	 * %0 has defeated %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PvPZONE_HOSTILE_DEATH_TO_B(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1360002, value0, value1);
	}

	/**
	 * You have defeated %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_HOSTILE_DEATH_TO_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1360003, value0);
	}

	/**
	 * %0 has defeated %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_HOSTILE_DEATH_TO_B(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1360004, value0, value1);
	}

	/**
	 * You have gained %num1 XP from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GET_EXP(String value0, long num1) {
		return new SM_SYSTEM_MESSAGE(1370000, value0, num1);
	}

	/**
	 * You have earned %0 XP.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_EXP_GAIN(String value0) {
		return new SM_SYSTEM_MESSAGE(1370001, value0);
	}

	/**
	 * You have gained %num0 XP.
	 */
	public static SM_SYSTEM_MESSAGE STR_GET_EXP2(long num0) {
		return new SM_SYSTEM_MESSAGE(1370002, num0);
	}

	/**
	 * %0 has received %num1 XP.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENSLAVE_GIVE_EXP_TO_PET_GET_EXP(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1370003, value0, num1);
	}

	/**
	 * %0 has lost %num1 XP.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENSLAVE_PET_LOSS_EXP(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1370004, value0, num1);
	}

	/**
	 * You distributed %1 Kinah each to %0 members.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_DISTRIBUTE_GOLD(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1380000, value1, value0);
	}

	/**
	 * You have earned %num0 Kinah.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GETMONEY(int num0) {
		return new SM_SYSTEM_MESSAGE(1380001, num0);
	}

	/**
	 * You received a refund of %num0 Kinah.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REFUND_MONEY_SYSTEM(int num0) {
		return new SM_SYSTEM_MESSAGE(1380002, num0);
	}

	/**
	 * You have acquired %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GET_ITEM1(String value0) {
		return new SM_SYSTEM_MESSAGE(1390000, value0);
	}

	/**
	 * %0 has acquired %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ITEM_WIN(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390001, value0, value1);
	}

	/**
	 * %0 has acquired %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_ITEM_PARTYNOTICE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390002, value0, value1);
	}

	/**
	 * %0 has acquired %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ITEM_WIN(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390003, value0, value1);
	}

	/**
	 * You have acquired %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1390004, value0);
	}

	/**
	 * You have acquired %num1 %0(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_ITEM_MULTI(int num1, String value0s) {
		return new SM_SYSTEM_MESSAGE(1390005, num1, value0s);
	}

	/**
	 * You cannot close the Craft window while crafting.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_CLOSE_MAKING_DIALOG_DURING_COMBINE() {
		return new SM_SYSTEM_MESSAGE(1390105);
	}

	/**
	 * You cannot change target while crafting.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_SELECT_TARGET_DURING_COMBINE() {
		return new SM_SYSTEM_MESSAGE(1390106);
	}

	/**
	 * You cannot open a private store while fighting.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_DISABLED_IN_EXCHANGE() {
		return new SM_SYSTEM_MESSAGE(1390107);
	}

	/**
	 * Group members cannot organize an alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_MEMBER_CANT_ORGANIZE_FORCE() {
		return new SM_SYSTEM_MESSAGE(1390108);
	}

	/**
	 * You cannot organize an alliance by inviting your own group members.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_ORGANIZE_FORCE_INVITED_PARTY_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1390109);
	}

	/**
	 * Please select a target.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NEED_TARGET() {
		return new SM_SYSTEM_MESSAGE(1390110);
	}

	/**
	 * Invalid name.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOT_CORRECT_CHAR_NAME() {
		return new SM_SYSTEM_MESSAGE(1390111);
	}

	/**
	 * The character name does not exist. Please check the recipient again.
	 */
	public static SM_SYSTEM_MESSAGE STR_MAIL_MSG_RECIPIENT_UNKNOWN() {
		return new SM_SYSTEM_MESSAGE(1390112);
	}

	/**
	 * You cannot send a mail to %0 because his/her mailbox is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_MAIL_MSG_RECIPIENT_MAILBOX_FULL(String value0) {
		return new SM_SYSTEM_MESSAGE(1390113, value0);
	}

	/**
	 * %0 is currently refusing the View Detail access.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REJECTED_WATCH(String value0) {
		return new SM_SYSTEM_MESSAGE(1390114, value0);
	}

	/**
	 * %0 is currently rejecting trade requests.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REJECTED_TRADE(String value0) {
		return new SM_SYSTEM_MESSAGE(1390115, value0);
	}

	/**
	 * %0 is currently rejecting group invitations.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REJECTED_INVITE_PARTY(String value0) {
		return new SM_SYSTEM_MESSAGE(1390116, value0);
	}

	/**
	 * %0 is currently rejecting alliance invitations.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REJECTED_INVITE_FORCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1390117, value0);
	}

	/**
	 * %0 is currently rejecting Legion invitations.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REJECTED_INVITE_GUILD(String value0) {
		return new SM_SYSTEM_MESSAGE(1390118, value0);
	}

	/**
	 * %0 is not currently accepting friend requests.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REJECTED_FRIEND(String value0) {
		return new SM_SYSTEM_MESSAGE(1390119, value0);
	}

	/**
	 * %0 is not currently accepting duel requests.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REJECTED_DUEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1390120, value0);
	}

	/**
	 * You started using the %0 skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_TOGGLE_SKILL_TURNED_ON(String value0) {
		return new SM_SYSTEM_MESSAGE(1390121, value0);
	}

	/**
	 * You have entered zone channel %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TELEPORT_ZONECHANNEL(int num1) {
		return new SM_SYSTEM_MESSAGE(1390122, num1);
	}

	/**
	 * Your Note: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_READ_TODAY_WORDS(String value0) {
		return new SM_SYSTEM_MESSAGE(1390124, value0);
	}

	/**
	 * You did not set Your Note.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOSET_TODAY_WORDS() {
		return new SM_SYSTEM_MESSAGE(1390125);
	}

	/**
	 * Your Note has been cleared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CLEAR_TODAY_WORDS() {
		return new SM_SYSTEM_MESSAGE(1390126);
	}

	/**
	 * You did not set the Legion Announcement.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOSET_GUILD_NOTICE() {
		return new SM_SYSTEM_MESSAGE(1390127);
	}

	/**
	 * Legion Announcement has been cleared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CLEAR_GUILD_NOTICE() {
		return new SM_SYSTEM_MESSAGE(1390128);
	}

	/**
	 * You did not set the Self Intro.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOSET_GUILD_MEMBER_INTRO() {
		return new SM_SYSTEM_MESSAGE(1390129);
	}

	/**
	 * Your Self Intro has been cleared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CLEAR_GUILD_MEMBER_INTRO() {
		return new SM_SYSTEM_MESSAGE(1390130);
	}

	/**
	 * %0 resisted your attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_RESISTED_ME_TO_B(String value0) {
		return new SM_SYSTEM_MESSAGE(1390131, value0);
	}

	/**
	 * You resisted %0's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_RESISTED_A_TO_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1390132, value0);
	}

	/**
	 * %1 resisted %0's attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_RESISTED_A_TO_B(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1390133, value1, value0);
	}

	/**
	 * You changed the connection status to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CONNECTION_STATUS(String value0) {
		return new SM_SYSTEM_MESSAGE(1390134, value0);
	}

	/**
	 * You changed the group to the %0 state.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MY_PARTY_STATE(String value0) {
		return new SM_SYSTEM_MESSAGE(1390135, value0);
	}

	/**
	 * You have no authority to modify the Legion emblem.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_EMBLEM_DONT_HAVE_RIGHT() {
		return new SM_SYSTEM_MESSAGE(1390136);
	}

	/**
	 * The Legion emblem has been changed.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_EMBLEM() {
		return new SM_SYSTEM_MESSAGE(1390137);
	}

	/**
	 * Please enter the name of the member to change the rank.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_RANK_NO_NAME() {
		return new SM_SYSTEM_MESSAGE(1390138);
	}

	/**
	 * The rank to change is incorrect.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_RANK_INCORRECT_RIGHT() {
		return new SM_SYSTEM_MESSAGE(1390139);
	}

	/**
	 * You cannot use a Rift while flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DIRECT_PORTAL_WHILE_FLYING() {
		return new SM_SYSTEM_MESSAGE(1390140);
	}

	/**
	 * Your accumulated play time is %0 hour(s) %1 minute(s). Your accumulated rest time is %2 hour(s) %3 minute(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_NORMAL_REMAIN_PLAYTIME(String value0, String value1, String value2, String value3) {
		return new SM_SYSTEM_MESSAGE(1390141, value0, value1, value2, value3);
	}

	/**
	 * Your accumulated rest time is %0 hour(s) %1 minute(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_HEALTH_REMAIN_PLAYTIME(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390142, value0, value1);
	}

	/**
	 * You are Tired, and the XP or item rewards gained are reduced to 50% of normal. Please log out and take a break for your health.
	 */
	public static SM_SYSTEM_MESSAGE STR_TIRED_REMAIN_PLAYTIME() {
		return new SM_SYSTEM_MESSAGE(1390143);
	}

	/**
	 * You are Exhausted, and the XP or item rewards gained are reduced to 0%. Please log out and take a break for your health. It will be returned to
	 * normal when the accumulated logout time reaches 5 hours.
	 */
	public static SM_SYSTEM_MESSAGE STR_PENALTY_REMAIN_PLAYTIME() {
		return new SM_SYSTEM_MESSAGE(1390144);
	}

	/**
	 * Real Time: %0 %1
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOCAL_TIME(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390145, value0, value1);
	}

	/**
	 * Game Time: %0 %1
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAME_TIME(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390146, value0, value1);
	}

	/**
	 * You do not have enough Kinah to pay the fee.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SEND_MAIL_NOT_ENOUGH_FEE() {
		return new SM_SYSTEM_MESSAGE(1390147);
	}

	/**
	 * You do not have the authority to use the Alert Chat.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NO_AUTHORITY() {
		return new SM_SYSTEM_MESSAGE(1390148);
	}

	/**
	 * You do not have enough space in the inventory.
	 */
	public static SM_SYSTEM_MESSAGE STR_WAREHOUSE_FULL_INVENTORY() {
		return new SM_SYSTEM_MESSAGE(1390149);
	}

	/**
	 * You cannot use items while crafting.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_USE_ITEM_DURING_COMBINE() {
		return new SM_SYSTEM_MESSAGE(1390150);
	}

	/**
	 * You cannot use the entrance to the enemy territory.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_TELEPORT_OPPOSITE_RACIAL() {
		return new SM_SYSTEM_MESSAGE(1390151);
	}

	/**
	 * You must first complete the Abyss Entry Quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_TELEPORT_TO_ABYSS() {
		return new SM_SYSTEM_MESSAGE(1390152);
	}

	/**
	 * The name must be entered in the form of [%0 character name].
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CHAT_CMD_NEED_NAME_FIELD(String value0) {
		return new SM_SYSTEM_MESSAGE(1390153, value0);
	}

	/**
	 * You cannot use the skill in the current form.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_CAN_NOT_CAST_IN_THIS_FORM() {
		return new SM_SYSTEM_MESSAGE(1390154);
	}

	/**
	 * %1 of %0 uses %3 in %2.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_ABYSS_SKILL_IS_FIRED(Player player, String skill) {
		return new SM_SYSTEM_MESSAGE(1390155, player.getRace().getL10n(), player.getName(), "%SubZone:" + player.getPosition().getMapId() + " "
			+ player.getPosition().getX() + " " + player.getPosition().getY() + " " + player.getPosition().getZ(), skill);
	}

	/**
	 * You could not remove the skill effect as your Dispel skill level is too low.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOT_ENOUGH_DISPELLEVEL() {
		return new SM_SYSTEM_MESSAGE(1390156);
	}

	/**
	 * You could not remove all the skill effects as you do not have sufficient Dispel skill count.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOT_ENOUGH_DISPELCOUNT() {
		return new SM_SYSTEM_MESSAGE(1390157);
	}

	/**
	 * The Kisk you registered as a resurrection bind point has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_BINDSTONE_DESTROYED() {
		return new SM_SYSTEM_MESSAGE(1390158);
	}

	/**
	 * You registered the current location as a resurrection bind point.
	 */
	public static SM_SYSTEM_MESSAGE STR_BINDSTONE_REGISTER() {
		return new SM_SYSTEM_MESSAGE(1390159);
	}

	/**
	 * You can install only one Kisk at a time.
	 */
	public static SM_SYSTEM_MESSAGE STR_BINDSTONE_ALREADY_INSTALLED() {
		return new SM_SYSTEM_MESSAGE(1390160);
	}

	/**
	 * You have already bound at this location.
	 */
	public static SM_SYSTEM_MESSAGE STR_BINDSTONE_ALREADY_REGISTERED() {
		return new SM_SYSTEM_MESSAGE(1390161);
	}

	/**
	 * You rolled the dice and got %0 (max. %num1).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_RESULT_ME(int value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1390162, value0, num1);
	}

	/**
	 * %0 rolled the dice and got %1 (max. %num2).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_RESULT_OTHER(String value0, int value1, int num2) {
		return new SM_SYSTEM_MESSAGE(1390163, value0, value1, num2);
	}

	/**
	 * You gave up rolling the dice.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_GIVEUP_ME() {
		return new SM_SYSTEM_MESSAGE(1390164);
	}

	/**
	 * %0 gave up rolling the dice.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_GIVEUP_OTHER(String value0) {
		return new SM_SYSTEM_MESSAGE(1390165, value0);
	}

	/**
	 * The Kisk you registered is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_BINDSTONE_IS_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1390166);
	}

	/**
	 * Items subjected to the group's quality item distribution have been changed to Superior rank or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_LOOTING_CHANGED_TO_RARE_QUALITY() {
		return new SM_SYSTEM_MESSAGE(1390167);
	}

	/**
	 * Items subjected to the alliance's quality item distribution have been changed to Superior rank or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_LOOTING_CHANGED_TO_RARE_QUALITY() {
		return new SM_SYSTEM_MESSAGE(1390168);
	}

	/**
	 * Items subjected to the group's quality item distribution have been changed to Heroic rank or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_LOOTING_CHANGED_TO_LEGEND_QUALITY() {
		return new SM_SYSTEM_MESSAGE(1390169);
	}

	/**
	 * Items subjected to the alliance's quality item distribution have been changed to Heroic rank or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_LOOTING_CHANGED_TO_LEGEND_QUALITY() {
		return new SM_SYSTEM_MESSAGE(1390170);
	}

	/**
	 * Items subjected to the group's quality item distribution have been changed to Fabled rank or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_LOOTING_CHANGED_TO_UNIQUE_QUALITY() {
		return new SM_SYSTEM_MESSAGE(1390171);
	}

	/**
	 * Items subjected to the alliance's quality item distribution have been changed to Fabled rank or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_LOOTING_CHANGED_TO_UNIQUE_QUALITY() {
		return new SM_SYSTEM_MESSAGE(1390172);
	}

	/**
	 * You cannot add any more on the quality item distribution list. Please try again later.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOTING_LIMIT_NUMBER() {
		return new SM_SYSTEM_MESSAGE(1390173);
	}

	/**
	 * The group's quality item distribution rules have been changed to Normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_LOOTING_CHANGED_TO_DEFAULT() {
		return new SM_SYSTEM_MESSAGE(1390174);
	}

	/**
	 * The alliance's quality item distribution rules have been changed to Normal.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_LOOTING_CHANGED_TO_DEFAULT() {
		return new SM_SYSTEM_MESSAGE(1390175);
	}

	/**
	 * The group's quality item distribution rules have been changed to Dice Roll.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_LOOTING_CHANGED_TO_DICE() {
		return new SM_SYSTEM_MESSAGE(1390176);
	}

	/**
	 * The alliance's quality item distribution rules have been changed to Dice Roll.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_LOOTING_CHANGED_TO_DICE() {
		return new SM_SYSTEM_MESSAGE(1390177);
	}

	/**
	 * The group's quality item distribution rules have been changed to Bidding.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_LOOTING_CHANGED_TO_PAY() {
		return new SM_SYSTEM_MESSAGE(1390178);
	}

	/**
	 * The alliance's quality item distribution rules have been changed to Bidding.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_LOOTING_CHANGED_TO_PAY() {
		return new SM_SYSTEM_MESSAGE(1390179);
	}

	/**
	 * You are now the owner of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOT_GET_ITEM_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1390180, value0);
	}

	/**
	 * %0 is now the owner of %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOT_GET_ITEM_OTHER(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390181, value0, value1);
	}

	/**
	 * You cannot acquire the item because there is no space in the inventory.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_INVEN_ERROR() {
		return new SM_SYSTEM_MESSAGE(1390182);
	}

	/**
	 * The account was instantly settled.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PAY_RESULT_ME() {
		return new SM_SYSTEM_MESSAGE(1390183);
	}

	/**
	 * %0 settled the account instantly.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PAY_RESULT_OTHER(String value0) {
		return new SM_SYSTEM_MESSAGE(1390184, value0);
	}

	/**
	 * Your bid was successful and %num0 Kinah has been deducted.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PAY_ACCOUNT_ME(long highestValue) {
		return new SM_SYSTEM_MESSAGE(1390185, highestValue);
	}

	/**
	 * It was won by %0 for %num1 Kinah.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PAY_ACCOUNT_OTHER(String value0, long highestValue) {
		return new SM_SYSTEM_MESSAGE(1390186, value0, highestValue);
	}

	/**
	 * %num0 Kinah is distributed %num2 Kinah each to %1 members.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PAY_DISTRIBUTE(long highestValue, int num2, long distributeKinah) {
		return new SM_SYSTEM_MESSAGE(1390187, highestValue, num2, distributeKinah);
	}

	/**
	 * You pause %0 temporarily.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOT_PAUSE_START_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1390188, value0);
	}

	/**
	 * %0 pauses %1 temporarily.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOT_PAUSE_START_OTHER(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390189, value0, value1);
	}

	/**
	 * %0 ended the pause state of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOT_PAUSE_END_ME(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390190, value0, value1);
	}

	/**
	 * %0 unpauses %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOT_PAUSE_END_OTHER(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390191, value0, value1);
	}

	/**
	 * The distribution resumes as the pause time is over.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOT_PAUSE_CALCEL() {
		return new SM_SYSTEM_MESSAGE(1390192);
	}

	/**
	 * You rolled the dice and got a %0 (1~%1).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_RESULT_EX_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1390193, value0);
	}

	/**
	 * %0's rolled the dice and got a %1 (1~%2).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_RESULT_EX_OTHER(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390194, value0, value1);
	}

	/**
	 * %1 of the %0 killed the Guardian General.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_BOSS_KILLED(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1390195, value1, value0);
	}

	/**
	 * %1 of the %0 has destroyed the Balaur Battleship Dredgion.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_CARRIER_KILLED(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1390196, value1, value0);
	}

	/**
	 * %0 seconds remain until you can cast it again.
	 */
	public static SM_SYSTEM_MESSAGE STR_ARTIFACT_INITIAL_TIME(String value0) {
		return new SM_SYSTEM_MESSAGE(1390197, value0);
	}

	/**
	 * You can use it only after the cooldown time is over.
	 */
	public static SM_SYSTEM_MESSAGE STR_ARTIFACT_COOL_TIME() {
		return new SM_SYSTEM_MESSAGE(1390198);
	}

	/**
	 * The Balaur have defeated %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_DRAGON_BOSS_KILLED(String npcL10n) {
		return new SM_SYSTEM_MESSAGE(1390199, npcL10n);
	}

	/**
	 * The Balaur have destroyed the Castle Gate.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_DRAGON_DOOR_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1390200);
	}

	/**
	 * The Balaur have destroyed the Gate Guardian Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_DRAGON_REPAIR_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1390201);
	}

	/**
	 * The Balaur have killed the Aetheric Field Generator.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_DRAGON_SHIELD_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1390202);
	}

	/**
	 * %0 captured the %1 Artifact.
	 */
	public static SM_SYSTEM_MESSAGE STR_EVENT_WIN_ARTIFACT(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390203, value0, value1);
	}

	/**
	 * %0 lost the %1 Artifact.
	 */
	public static SM_SYSTEM_MESSAGE STR_EVENT_LOSE_ARTIFACT(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390204, value0, value1);
	}

	/**
	 * The dredgion has vanished.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_CARRIER_DESPAWN() {
		return new SM_SYSTEM_MESSAGE(1390205);
	}

	/**
	 * %1 of the %0 has destroyed the Balaur Battleship Dredgion.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_GUILD_CARRIER_KILLED(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1390206, value1, value0);
	}

	/**
	 * You have captured the %0 Artifact.
	 */
	public static SM_SYSTEM_MESSAGE STR_ARTIFACT_WIN_FORT_TO_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1390207, value0);
	}

	/**
	 * %1 of %0 has captured the %2 Artifact.
	 */
	public static SM_SYSTEM_MESSAGE STR_ARTIFACT_WIN_FORT(String value1, String value0, String value2) {
		return new SM_SYSTEM_MESSAGE(1390208, value1, value0, value2);
	}

	/**
	 * The %0 Artifact has been lost to %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_ARTIFACT_LOSE_FORT(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390209, value0, value1);
	}

	/**
	 * Starts the auto-distribution of miscellaneous items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_JUNK_DISTRIBUTE_ON() {
		return new SM_SYSTEM_MESSAGE(1390210);
	}

	/**
	 * Ends the auto-distribution of miscellaneous items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_JUNK_DISTRIBUTE_OF() {
		return new SM_SYSTEM_MESSAGE(1390211);
	}

	/**
	 * You cannot disband your Legion while you have items or money left in the Legion warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_DISPERSE_CANT_DISPERSE_GUILD_STORE_ITEM_IN_WAREHOUSE() {
		return new SM_SYSTEM_MESSAGE(1390212);
	}

	/**
	 * Playing Time: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PLAYING_TIME(String value0) {
		return new SM_SYSTEM_MESSAGE(1390213, value0);
	}

	/**
	 * You have played for %0 hour(s). Please take a break.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOTIFY_PLAYING_TIME(String value0) {
		return new SM_SYSTEM_MESSAGE(1390214, value0);
	}

	/**
	 * You have joined the %0 Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_I_JOINED(String value0) {
		return new SM_SYSTEM_MESSAGE(1390215, value0);
	}

	/**
	 * You recovered %num0 HP.
	 */
	public static SM_SYSTEM_MESSAGE _STR_MSG_Heal_TO_ME(int num0) {
		return new SM_SYSTEM_MESSAGE(1390216, num0);
	}

	/**
	 * You can only buy one %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CAN_BUY_ONLY_ONE(String value0) {
		return new SM_SYSTEM_MESSAGE(1390217, value0);
	}

	/**
	 * Registering %0 on the quality item distribution list.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOT_LISTING_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1390218, value0);
	}

	/**
	 * %0 is one of the quality items waiting to be distributed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOT_ALREADY_DISTRIBUTING_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1390219, value0);
	}

	/**
	 * You do not have the ownership of this item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOT_ANOTHER_OWNER_ITEM() {
		return new SM_SYSTEM_MESSAGE(1390220);
	}

	/**
	 * The skill level for the %0 skill does not increase as the difficulty is too low.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_GET_PRODUCTION_EXP(String value0) {
		return new SM_SYSTEM_MESSAGE(1390221, value0);
	}

	/**
	 * Items subjected to the group's quality item distribution have been changed to Common rank or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_LOOTING_CHANGED_TO_COMMON_QUALITY() {
		return new SM_SYSTEM_MESSAGE(1390222);
	}

	/**
	 * Items subjected to the alliance's quality item distribution have been changed to Common rank or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_LOOTING_CHANGED_TO_COMMON_QUALITY() {
		return new SM_SYSTEM_MESSAGE(1390223);
	}

	/**
	 * You have sent a friend request to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDY_REQUEST_ADD(String value0) {
		return new SM_SYSTEM_MESSAGE(1390224, value0);
	}

	/**
	 * You cannot connect to the game during the character reservation period.
	 */
	public static SM_SYSTEM_MESSAGE STR_ERROR_WORLD_CONNECTION_FAIL_BY_CHAR_RES() {
		return new SM_SYSTEM_MESSAGE(1390225);
	}

	/**
	 * Everyone gave up the Dice Roll.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_ALL_GIVEUP() {
		return new SM_SYSTEM_MESSAGE(1390226);
	}

	/**
	 * Everyone gave up the Bidding.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PAY_ALL_GIVEUP() {
		return new SM_SYSTEM_MESSAGE(1390227);
	}

	/**
	 * You gave up the Bidding.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PAY_GIVEUP_ME() {
		return new SM_SYSTEM_MESSAGE(1390228);
	}

	/**
	 * %0 gave up the Bidding.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PAY_GIVEUP_OTHER(String value0) {
		return new SM_SYSTEM_MESSAGE(1390229, value0);
	}

	/**
	 * You cannot use this function in %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DISABLE(String value0) {
		return new SM_SYSTEM_MESSAGE(1390230, value0);
	}

	/**
	 * The registered Kisk can resurrect %num0 times more.
	 */
	public static SM_SYSTEM_MESSAGE STR_BINDSTONE_CAPACITY_LIMITTED_ALARM(int num0) {
		return new SM_SYSTEM_MESSAGE(1390231, num0);
	}

	/**
	 * Your Abyss Rank has changed to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_CHANGE_RANK(String value0) {
		return new SM_SYSTEM_MESSAGE(1390232, value0);
	}

	/**
	 * You cannot be promoted as your skill level is too low.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_RANK_UP() {
		return new SM_SYSTEM_MESSAGE(1390233);
	}

	/**
	 * An Expert cannot take on a Work Order.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_GET_COMBINETASK() {
		return new SM_SYSTEM_MESSAGE(1390234);
	}

	/**
	 * Your Abyss Rank has been changed to %0. Check the changed ranking on the Abyss Ranking Window.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_CHANGE_RANK_THIS_WEEK(String value0) {
		return new SM_SYSTEM_MESSAGE(1390235, value0);
	}

	/**
	 * You have learned the skill, %0 (Level - %1).
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_LEARNED_ABYSS_SKILL(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390236, value0, value1);
	}

	/**
	 * Only available to alliances.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_SPLIT_FORCE() {
		return new SM_SYSTEM_MESSAGE(1390237);
	}

	/**
	 * Please enter the amount of Kinah to distribute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ENTER_SPLIT_GOLD() {
		return new SM_SYSTEM_MESSAGE(1390238);
	}

	/**
	 * You give up the Bidding as you do not have enough Kinah.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PAY_NOT_ENOUGH_MONEY() {
		return new SM_SYSTEM_MESSAGE(1390239);
	}

	/**
	 * You cannot join the Legion as the player who invited you is no longer a member of the Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_INVITE_CAN_NOT_JOIN_TO_GUILD_INVITOR_IS_LEFT() {
		return new SM_SYSTEM_MESSAGE(1390240);
	}

	/**
	 * You cannot kick out a Legion member of equal or higher rank.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_BANISH_CAN_NOT_BANISH_SAME_MEMBER_RANK() {
		return new SM_SYSTEM_MESSAGE(1390241);
	}

	/**
	 * You have acquired the %0 title.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_CASH_TITLE(String value0) {
		return new SM_SYSTEM_MESSAGE(1390242, value0);
	}

	/**
	 * You have acquired the %0 emote.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_CASH_SOCIALACTION(String value0) {
		return new SM_SYSTEM_MESSAGE(1390243, value0);
	}

	/**
	 * The usage time of %0 title has expired.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DELETE_CASH_TITLE_BY_TIMEOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1390244, value0);
	}

	/**
	 * Usage time for the %0 emote has expired.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DELETE_CASH_SOCIALACTION_BY_TIMEOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1390245, value0);
	}

	public static SM_SYSTEM_MESSAGE STR_MSG_DELETE_CASH_SOCIALACTION_BY_TIMEOUT() {
		return new SM_SYSTEM_MESSAGE(1390245);
	}

	/**
	 * Usage time for the [Lodas Amulet] Bonus 20%% XP has expired.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DELETE_CASH_XPBOOST_BY_TIMEOUT() {
		return new SM_SYSTEM_MESSAGE(1390246);
	}

	/**
	 * You distributed %num0 Kinah to %num1 people, giving each %num2 Kinah.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SPLIT_ME_TO_B(long num0, int num1, long num2) {
		return new SM_SYSTEM_MESSAGE(1390247, num0, num1, num2);
	}

	/**
	 * %0 distributed %num1 Kinah among %num2 people, giving %num3 Kinah each.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SPLIT_B_TO_ME(String value0, long num1, int num2, long num3) {
		return new SM_SYSTEM_MESSAGE(1390248, value0, num1, num2, num3);
	}

	/**
	 * The search found %num0 characters (max. 110).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WHO_DIALOG_RESULT(int num0) {
		return new SM_SYSTEM_MESSAGE(1390249, num0);
	}

	/**
	 * Group loot policy is now %0. %1 items will be distributed by %2.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_LOOTING_CHANGED_RULE(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1390250, value0, value1, value2);
	}

	/**
	 * Alliance loot policy is now %0. %1 items will be distributed by %2.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_LOOTING_CHANGED_RULE(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1390251, value0, value1, value2);
	}

	/**
	 * You cannot be promoted anymore as you are an Expert.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_RANK_UP_MASTER() {
		return new SM_SYSTEM_MESSAGE(1390252);
	}

	/**
	 * You cannot be promoted anymore as you are at the highest rank.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_RANK_UP_GATHERING() {
		return new SM_SYSTEM_MESSAGE(1390253);
	}

	/**
	 * You have not acquired this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_OPEN_QUEST_LINK() {
		return new SM_SYSTEM_MESSAGE(1390254);
	}

	/**
	 * Your skill level does not increase with low level crafting as you are an Expert.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_GET_COMBINE_EXP() {
		return new SM_SYSTEM_MESSAGE(1390255);
	}

	/**
	 * This area is only accessible to groups.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ENTER_ONLY_PARTY_DON() {
		return new SM_SYSTEM_MESSAGE(1390256);
	}

	/**
	 * You do not have enough Medals.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOT_ENOUGH_MEDAL() {
		return new SM_SYSTEM_MESSAGE(1390257);
	}

	/**
	 * A report for the character %0 has been received. You have %1 auto hunting reports remaining.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_SUBMIT(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1390258, value0, value1);
	}

	/**
	 * Only % minutes have passes since the last report.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_CANNOT_SUBMIT(String value) {
		return new SM_SYSTEM_MESSAGE(1390259, value);
	}

	/**
	 * There is a charged item issued to the account. Do you want %0 to have the charged item?
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_WARNING_GET_ITEM1(String value0) {
		return new SM_SYSTEM_MESSAGE(1390260, value0);
	}

	/**
	 * Once the item is given, it cannot be used by other characters. Are you sure you want to keep it in %0?
	 */
	public static SM_SYSTEM_MESSAGE STR_LOGIN_WARNING_GET_ITEM2(String value0) {
		return new SM_SYSTEM_MESSAGE(1390261, value0);
	}

	/**
	 * You have joined the group.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ENTERED_PARTY() {
		return new SM_SYSTEM_MESSAGE(1390262);
	}

	/**
	 * You have joined the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ENTERED_FORCE() {
		return new SM_SYSTEM_MESSAGE(1390263);
	}

	/**
	 * Please complete your current quest first.
	 */
	public static SM_SYSTEM_MESSAGE STR_QUEST_ANOTHER_SINGLE_STEP_NOT_COMPLETED() {
		return new SM_SYSTEM_MESSAGE(1390264);
	}

	/**
	 * You cannot join once it has started.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_LOCKED() {
		return new SM_SYSTEM_MESSAGE(1390265);
	}

	/**
	 * %0 rolled the highest (%0 rolled %1, while you rolled %2).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_ITEM_PARTYNOTICE_DICE(String value0, String value3, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1390266, value0, value3, value1, value2);
	}

	/**
	 * %0 rolled the highest (%0 rolled %1, while you passed).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_ITEM_PARTYNOTICE_DICE_GIVEUP_ROLL(String value0, String value2, String value1) {
		return new SM_SYSTEM_MESSAGE(1390267, value0, value2, value1);
	}

	/**
	 * You rolled the highest.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_ITEM_PARTYNOTICE_DICE_WIN() {
		return new SM_SYSTEM_MESSAGE(1390268);
	}

	/**
	 * Everyone passed on rolling the dice.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_ITEM_PARTYNOTICE_DICE_GIVEUP_ROLL_ALL() {
		return new SM_SYSTEM_MESSAGE(1390269);
	}

	/**
	 * A now-disconnected player rolled the highest (they winner rolled %1, while you rolled %1).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_ITEM_PARTYNOTICE_DICE_OFFLINE_WINNER(String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1390270, value1, value2);
	}

	/**
	 * A now-disconnected player rolled the highest (they winner rolled %1, while you passed).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_ITEM_PARTYNOTICE_DICE_GIVEUP_ROLL_OFFLINE_WINNER(String value1) {
		return new SM_SYSTEM_MESSAGE(1390271, value1);
	}

	/**
	 * The selected Instanced Zone's cooldown time can't be reset.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_INSTANCE_COOL_TIME_INIT() {
		return new SM_SYSTEM_MESSAGE(1390272);
	}

	/**
	 * %0 Shouts:
	 */
	public static SM_SYSTEM_MESSAGE STR_CMD_SHOUT_OUTPUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400000, value0);
	}

	/**
	 * %0 is asking for help from %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_CMD_SHOUT_OUTPUT1(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400001, value0, value1);
	}

	/**
	 * %0 Shouts:
	 */
	public static SM_SYSTEM_MESSAGE STR_CMD_SHOUT_OUTPUT_NPC(String value0) {
		return new SM_SYSTEM_MESSAGE(1400002, value0);
	}

	/**
	 * %0 is asking for help from %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_CMD_SHOUT_OUTPUT1_NPC(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400003, value0, value1);
	}

	/**
	 * You shout "%0".
	 */
	public static SM_SYSTEM_MESSAGE STR_CMD_SHOUT_INPUT() {
		return new SM_SYSTEM_MESSAGE(1400004);
	}

	/**
	 * You shout for help.
	 */
	public static SM_SYSTEM_MESSAGE STR_CMD_SHOUT_INPUT1() {
		return new SM_SYSTEM_MESSAGE(1400005);
	}

	/**
	 * %0 Whispers:
	 */
	public static SM_SYSTEM_MESSAGE STR_CMD_WHISHPER_OUTPUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400006, value0);
	}

	/**
	 * %0 Whispers:
	 */
	public static SM_SYSTEM_MESSAGE STR_CMD_WHISHPER_OUTPUT_NPC(String value0) {
		return new SM_SYSTEM_MESSAGE(1400007, value0);
	}

	/**
	 * You Whisper to %1: %0
	 */
	public static SM_SYSTEM_MESSAGE STR_CMD_WHISHPER_INPUT(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400008, value1, value0);
	}

	/**
	 * %0 has joined your group.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_HE_ENTERED_PARTY(String value0) {
		return new SM_SYSTEM_MESSAGE(1400009, value0);
	}

	/**
	 * Your group has joined %0's alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ENTER_WITH_PARTY(String value0) {
		return new SM_SYSTEM_MESSAGE(1400010, value0);
	}

	/**
	 * %0 has joined the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ENTER_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400011, value0);
	}

	/**
	 * %0's group has joined the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ENTER_WITH_HIS_PARTY(String value0) {
		return new SM_SYSTEM_MESSAGE(1400012, value0);
	}

	/**
	 * %0 has joined the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_HE_ENTERED_FORCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400013, value0);
	}

	/**
	 * %0's group has joined the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_HIS_PARTY_ENTERED_ALLIANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400014, value0);
	}

	/**
	 * Your group has joined the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_MY_PARTY_ENTERED_ALLIANCE() {
		return new SM_SYSTEM_MESSAGE(1400015);
	}

	/**
	 * You have joined a group belonging to an alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_ENTERY_PARTY_AND_ALLIANCE() {
		return new SM_SYSTEM_MESSAGE(1400016);
	}

	/**
	 * %0 has joined the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_HE_ENTERED_ALLIANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400017, value0);
	}

	/**
	 * You have joined %0's allliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_ENTER_MEMBER(String value0) {
		return new SM_SYSTEM_MESSAGE(1400018, value0);
	}

	/**
	 * Legion Message: %0 %DATETIME1
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_NOTICE(String value0, long i) {
		return new SM_SYSTEM_MESSAGE(1400019, value0, i, 2);
	}

	/**
	 * Please report after you select a character from the same race.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DO_NOT_ACCUSE() {
		return new SM_SYSTEM_MESSAGE(1400020);
	}

	/**
	 * %0 killed the Guardian General.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_NPC_BOSS_KILLED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400021, value0);
	}

	/**
	 * You cannot use a Rift.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DIRECT_PORTAL() {
		return new SM_SYSTEM_MESSAGE(1400022);
	}

	/**
	 * %0 %1 %2 has died in %3.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_ORDER_RANKER_DIE(Player victim) {
		return SM_SYSTEM_MESSAGE.STR_ABYSS_ORDER_RANKER_DIE(victim, "%SubZone:" + victim.getPosition().getMapId() + " " + victim.getPosition().getX()
			+ " " + victim.getPosition().getY() + " " + victim.getPosition().getZ());
	}

	/**
	 * %0 %1 %2 has died in %3.
	 */
	public static SM_SYSTEM_MESSAGE STR_ABYSS_ORDER_RANKER_DIE(Player victim, String zoneName) {
		return new SM_SYSTEM_MESSAGE(1400023, victim.getRace().getL10n(), AbyssRankEnum.getRankL10n(victim), victim.getName(), zoneName);
	}

	/**
	 * You cannot continue the battle as you have insufficient Abyss Points. You will be resurrected at %1 if nothing is entered within %0 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_RESURRECT_HERE_BY_ABYSS_POINT_ZERO(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400024, value1, value0);
	}

	/**
	 * %0 has used the Gate Guardian Stone to repair the castle gate by %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REPAIR_ABYSS_DOOR(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400025, value0, value1);
	}

	/**
	 * You have obtained %0 from the Internet Cafe Event.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_PCBANG_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400026, value0);
	}

	/**
	 * %WORLDNAME0% region restricts access. You cannot reenter the region for %1 hour(s) if all your group members left the region or if you left the
	 * current group.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_COOL_TIME_HOUR(int worldId, String value1) {
		return new SM_SYSTEM_MESSAGE(1400027, worldId, value1);
	}

	/**
	 * %WORLDNAME0% region restricts access. You cannot reenter the region for %1 minute(s) if all your group members left the region or if you left the
	 * current group.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_COOL_TIME_MIN(int worldId, String value1) {
		return new SM_SYSTEM_MESSAGE(1400028, worldId, value1);
	}

	/**
	 * You may enter %WORLDNAME0 again after %1 hour(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_ENTER_INSTANCE_COOL_TIME_HOUR(int worldId, String value1) {
		return new SM_SYSTEM_MESSAGE(1400029, worldId, value1);
	}

	/**
	 * You may enter %WORLDNAME0 again after %1 minute(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_ENTER_INSTANCE_COOL_TIME_MIN(int worldId, String value1) {
		return new SM_SYSTEM_MESSAGE(1400030, worldId, value1);
	}

	/**
	 * You can enter %0 area now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CAN_ENTER_INSTANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400031, value0);
	}

	/**
	 * %0: %1(%2)
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CHECK_INSTANCE_COOL_TIME() {
		return new SM_SYSTEM_MESSAGE(1400032);
	}

	/**
	 * Changing Game Preferences to Pseudo Full Screen mode for convenient access of the website.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TEMP_PSEUDO_FULLSCREEN() {
		return new SM_SYSTEM_MESSAGE(1400033);
	}

	/**
	 * Usage time for %0 has expired.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DELETE_CASH_ITEM_BY_TIMEOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400034, value0);
	}

	/**
	 * You cannot gain any Abyss Points for a while as you have gained too many Abyss Points in too short a period of time.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_GET_AP_TIMEBASE_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1400035);
	}

	/**
	 * Your trial has ended. %1, We hope you've enjoyed playing Aion! To continue to play, purchase Aion. Go to AionOnline.com to buy now!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LEVEL_LIMIT_FREE_TIME(String value1) {
		return new SM_SYSTEM_MESSAGE(1400036, value1);
	}

	/**
	 * You may enter %0 again after %1 hour(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_ENTER_INSTANCE_COOL_TIME_HOUR_CLIENT(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400037, value0, value1);
	}

	/**
	 * You may enter %0 again after %1 minute(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_ENTER_INSTANCE_COOL_TIME_MIN_CLIENT(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400038, value0, value1);
	}

	/**
	 * The Macro has been registered.
	 */
	public static SM_SYSTEM_MESSAGE STR_MACRO_MSG_REGIST() {
		return new SM_SYSTEM_MESSAGE(1400039);
	}

	/**
	 * You cannot register any more Macro.
	 */
	public static SM_SYSTEM_MESSAGE STR_MACRO_MSG_CANNOT_REGIST() {
		return new SM_SYSTEM_MESSAGE(1400040);
	}

	/**
	 * You cannot get any Abyss Point from the current target for a while.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_GET_AP_TARGET_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1400041);
	}

	/**
	 * As you are not currently a member of the group for the Instanced Zone, you will be leaving the zone shortly.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LEAVE_INSTANCE_NOT_PARTY() {
		return new SM_SYSTEM_MESSAGE(1400042);
	}

	/**
	 * The zone has been reset. Once reset, you cannot enter the zone again until the reentry time expires. You can check the reentry time by typing
	 * '/CheckEntry'.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_MAKE_INSTANCE_COOL_TIME() {
		return new SM_SYSTEM_MESSAGE(1400043);
	}

	/**
	 * You have exited the Instanced Zone. This zone will be reset in %0 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LEAVE_INSTANCE(int num0) {
		return new SM_SYSTEM_MESSAGE(1400044, num0);
	}

	/**
	 * You have exited the Instanced Zone. This zone will be reset in %0 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LEAVE_INSTANCE_PARTY(int num0) {
		return new SM_SYSTEM_MESSAGE(1400045, num0);
	}

	/**
	 * You have exited the Instanced Zone. This zone will be reset in %0 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LEAVE_INSTANCE_FORCE(int num0) {
		return new SM_SYSTEM_MESSAGE(1400046, num0);
	}

	/**
	 * This account has been suspended for not paying the Internet Cafe usage charge.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BLOCK_PC_ROOM_COMPLAIN() {
		return new SM_SYSTEM_MESSAGE(1400047);
	}

	/**
	 * The playing time remaining is %*0, and there are %1 items of pre-paid credits left.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REMAIN_PLAYTIME_WITH_RESERVATION(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400048, value0, value1);
	}

	/**
	 * The playing time will expire in %*0. If you wish to continue using the service, please make additional payments on the Billing Page of the Plaync
	 * website.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ALARM_REMAIN_PLAYTIME(String value0) {
		return new SM_SYSTEM_MESSAGE(1400049, value0);
	}

	/**
	 * You have %*0 playing time remaining. Pre-paid credit will be applied afterward.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ALARM_REMAIN_PLAYTIME_WITH_RESERVATION(String value0) {
		return new SM_SYSTEM_MESSAGE(1400050, value0);
	}

	/**
	 * The playing time has expired, and the game will end automatically in %*0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COUNT_REMAIN_PLAYTIME(String value0) {
		return new SM_SYSTEM_MESSAGE(1400051, value0);
	}

	/**
	 * The playing time will expire in %*0. Pre-paid credit will be applied after it expires.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COUNT_REMAIN_PLAYTIME_WITH_RESERVATION(String value0) {
		return new SM_SYSTEM_MESSAGE(1400052, value0);
	}

	/**
	 * standing
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACT_STATE_STANDING() {
		return new SM_SYSTEM_MESSAGE(1400053);
	}

	/**
	 * flying
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACT_STATE_PATH_FLYING() {
		return new SM_SYSTEM_MESSAGE(1400054);
	}

	/**
	 * flying
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACT_STATE_FREE_FLYING() {
		return new SM_SYSTEM_MESSAGE(1400055);
	}

	/**
	 * riding
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACT_STATE_RIDING() {
		return new SM_SYSTEM_MESSAGE(1400056);
	}

	/**
	 * resting
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACT_STATE_SITTING() {
		return new SM_SYSTEM_MESSAGE(1400057);
	}

	/**
	 * sitting
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACT_STATE_SITTING_ON_CHAIR() {
		return new SM_SYSTEM_MESSAGE(1400058);
	}

	/**
	 * dead
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACT_STATE_DEAD() {
		return new SM_SYSTEM_MESSAGE(1400059);
	}

	/**
	 * dead
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACT_STATE_FLY_DEAD() {
		return new SM_SYSTEM_MESSAGE(1400060);
	}

	/**
	 * running a Private Store
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACT_STATE_PERSONAL_SHOP() {
		return new SM_SYSTEM_MESSAGE(1400061);
	}

	/**
	 * looting
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACT_STATE_LOOTING() {
		return new SM_SYSTEM_MESSAGE(1400062);
	}

	/**
	 * looting
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACT_STATE_FLY_LOOTING() {
		return new SM_SYSTEM_MESSAGE(1400063);
	}

	/**
	 * in your current status
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACT_STATE_DEFAULT() {
		return new SM_SYSTEM_MESSAGE(1400064);
	}

	/**
	 * You cannot register items of other races.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_OTHER_RACE() {
		return new SM_SYSTEM_MESSAGE(1400065);
	}

	/**
	 * This account has been reported for not paying an internet caf? usage charge. If you believe this is an error, please contact customer support.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BLOCK_PC_ROOM_COMPLAIN2() {
		return new SM_SYSTEM_MESSAGE(1400066);
	}

	/**
	 * The Stigma is already equipped.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_STIGMA_ALREADY_EQUIP_STONE() {
		return new SM_SYSTEM_MESSAGE(1400067);
	}

	/**
	 * You must wait %DURATIONTIME0 to use the channel change function. Time Remaining: %DURATIONTIME1
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REMAIN_CHANGE_CHANNEL_COOLTIME(String durationtime0, String durationtime1) {
		return new SM_SYSTEM_MESSAGE(1400068, durationtime0, durationtime1);
	}

	/**
	 * You entered into the Phase %num0 Restriction state because the auto hunting reports have accumulated. You can check the Restriction Phase and the
	 * Release Time by typing the '/Restriction' command.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_UPGRADE_LEVEL(int num0) {
		return new SM_SYSTEM_MESSAGE(1400069, num0);
	}

	/**
	 * Your restriction phase has been lowered to %num0 as you played fair for a certain period of time. Please continue to play the game in a proper
	 * manner.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_DEGRADE_LEVEL(int num0) {
		return new SM_SYSTEM_MESSAGE(1400070, num0);
	}

	/**
	 * You are currently at Phase 1 Restriction State, and will be released in %0 minutes. While not affecting your game play in anyway, a continued
	 * accumulation of reports will however raise the Restriction Phase and will limit your gaining of XP and items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_INFO_1_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400071, value0);
	}

	/**
	 * You are currently in Phase 2 Restriction State, and will be downgraded to Phase 1 in %0 minutes. You now receive less XP, Kinah and Abyss Points,
	 * and the chance of successful gathering and extraction has been decreased. You will face greater restrictions if reports continue to accumulate.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_INFO_2_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400072, value0);
	}

	/**
	 * You are currently in Phase 3 Restriction State and will be downgraded to Phase 2 in %0 minutes. You cannot acquire any loot, and you now receive
	 * less XP, Kinah and Abyss Points, and the chance of successful gathering and extraction has been significantly decreased. You are banned from
	 * joining a Group or Alliance. You will face greater restrictions if reports continue to accumulate.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_INFO_3_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400073, value0);
	}

	/**
	 * You are currently at Phase 4 Restriction State and will be downgraded to Phase 3 in %0 minutes. You cannot acquire any loot, XP, Kinah, or Abyss
	 * Points, and are unable to gather or extract any items. You are also banned from joining a Group or Alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_INFO_4_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400074, value0);
	}

	/**
	 * You have consumed all equipped Power Shards.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WEAPON_BOOST_MODE_BURN_OUT() {
		return new SM_SYSTEM_MESSAGE(1400075);
	}

	/**
	 * You are in normal state.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_INFO_NORMAL() {
		return new SM_SYSTEM_MESSAGE(1400076);
	}

	/**
	 * You cannot delete the letter because items or Kinah are attached.
	 */
	public static SM_SYSTEM_MESSAGE STR_MAIL_ITEM_DEL_DENIED() {
		return new SM_SYSTEM_MESSAGE(1400077);
	}

	/**
	 * You cannot open a private store while trading.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_OPEN_STORE_DURING_CRAFTING() {
		return new SM_SYSTEM_MESSAGE(1400078);
	}

	/**
	 * in combat
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ASF_COMBAT() {
		return new SM_SYSTEM_MESSAGE(1400079);
	}

	/**
	 * moving
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ASF_MOVE_TYPE_WALK() {
		return new SM_SYSTEM_MESSAGE(1400080);
	}

	/**
	 * using a skill
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ASF_CASTING_SKILL() {
		return new SM_SYSTEM_MESSAGE(1400081);
	}

	/**
	 * gliding
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ASF_GLIDE() {
		return new SM_SYSTEM_MESSAGE(1400082);
	}

	/**
	 * You returned to the normal state as you played fair for a certain period of time. Please continue to play the game in a proper manner.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_DEGRADE_NORMAL_LEVEL() {
		return new SM_SYSTEM_MESSAGE(1400083);
	}

	/**
	 * %0 is not an appearance-modified item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOT_SKIN_CHANGED_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400084, value0);
	}

	/**
	 * You cannot send auto hunting reports right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_ACCUSE() {
		return new SM_SYSTEM_MESSAGE(1400085);
	}

	/**
	 * You cannot report auto hunting in the current region.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_ACCUSE_CITY() {
		return new SM_SYSTEM_MESSAGE(1400086);
	}

	/**
	 * You cannot issue commands in %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SUMMON_CANT_ORDER_BY_INVALID_STANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400087, value0);
	}

	/**
	 * You have already learned this emote.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SOCIALACTION_ALREADY_HAS_SKILL() {
		return new SM_SYSTEM_MESSAGE(1400088);
	}

	/**
	 * This item has not been appearance modified.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CHANGE_ITEM_SKIN_CANNOT_INVALID_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400089);
	}

	/**
	 * You cannot report as you have exceeded the number of auto hunting reports allowed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_CANT_SUBMIT_BY_NO_COUNT() {
		return new SM_SYSTEM_MESSAGE(1400090);
	}

	/**
	 * You currently have %0 auto hunting reports left.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_COUNT_INFO(String value0) {
		return new SM_SYSTEM_MESSAGE(1400091, value0);
	}

	/**
	 * The selected user cannot be invited to a group or a force.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_CANT_BE_INVITED() {
		return new SM_SYSTEM_MESSAGE(1400092);
	}

	/**
	 * You have been reported too many times, and cannot issue an invitation.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_CANT_INVITE_OTHER() {
		return new SM_SYSTEM_MESSAGE(1400093);
	}

	/**
	 * You cannot join the group as you have been reported too many times for auto hunting.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_CANT_JOIN_PARTY() {
		return new SM_SYSTEM_MESSAGE(1400094);
	}

	/**
	 * You cannot join the Alliance as you have been reported too many times for auto hunting.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_CANT_JOIN_FORCE() {
		return new SM_SYSTEM_MESSAGE(1400095);
	}

	/**
	 * You cannot use the manastone on the selected item as the manastone level is too high.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GIVE_ITEM_OPTION_CANT_FOR_TOO_HIGH_LEVEL() {
		return new SM_SYSTEM_MESSAGE(1400096);
	}

	/**
	 * %0 cannot be summoned right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Recall_CANNOT_ACCEPT_EFFECT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400097, value0);
	}

	/**
	 * Summoning of %0 is cancelled as the confirmation stand-by time has been exceeded.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Recall_DONOT_ACCEPT_EFFECT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400098, value0);
	}

	/**
	 * You declined %0's summoning.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Recall_Reject_EFFECT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400099, value0);
	}

	/**
	 * %0 declined your summoning.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Recall_Rejected_EFFECT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400100, value0);
	}

	/**
	 * Summoning of %0 is cancelled.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Recall_CANCEL_EFFECT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400101, value0);
	}

	/**
	 * You cannot summon %0 as you are already under the same effect.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Recall_DUPLICATE_EFFECT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400102, value0);
	}

	/**
	 * %0 is currently unable to join a group or a force.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_OTHER_IS_BANISHED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400103, value0);
	}

	/**
	 * The gift has been delivered successfully.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_GIFT_SUCCESS() {
		return new SM_SYSTEM_MESSAGE(1400104);
	}

	/**
	 * You have failed to purchase the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_ERROR() {
		return new SM_SYSTEM_MESSAGE(1400105);
	}

	/**
	 * You have chosen an invalid target to give the gift.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_NO_USER_TO_GIFT() {
		return new SM_SYSTEM_MESSAGE(1400106);
	}

	/**
	 * The item is not on the list.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_INVALID_GOODS() {
		return new SM_SYSTEM_MESSAGE(1400107);
	}

	/**
	 * You do not have enough Cash Points.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_NOT_ENOUGH_POINT() {
		return new SM_SYSTEM_MESSAGE(1400108);
	}

	/**
	 * Your race cannot purchase the selected item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_INVALID_RACE() {
		return new SM_SYSTEM_MESSAGE(1400109);
	}

	/**
	 * Your gender cannot purchase the selected item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_INVALID_GENDER() {
		return new SM_SYSTEM_MESSAGE(1400110);
	}

	/**
	 * Your Class cannot purchase the selected item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_INVALID_CLASS() {
		return new SM_SYSTEM_MESSAGE(1400111);
	}

	/**
	 * You already have the selected title.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_DUPLICATED_TITLE() {
		return new SM_SYSTEM_MESSAGE(1400112);
	}

	/**
	 * You already have the selected emote.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_DUPLICATED_SOCIAL() {
		return new SM_SYSTEM_MESSAGE(1400113);
	}

	/**
	 * You have purchased the cube expansion item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_DUPLICATED_CUBE() {
		return new SM_SYSTEM_MESSAGE(1400114);
	}

	/**
	 * You cannot register as you are not %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BINDSTONE_CANNOT_FOR_INVALID_RIGHT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400115, value0);
	}

	/**
	 * You cannot give gifts to yourself.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_CANNOT_GIVE_TO_ME() {
		return new SM_SYSTEM_MESSAGE(1400116);
	}

	/**
	 * You cannot send the letter to %0 because you have been blocked by the player.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MAIL_CANT_FOR_YOU_EXCLUDED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400117, value0);
	}

	/**
	 * Network Status: %0 ms
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PING_RESULT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400118, value0);
	}

	/**
	 * You cannot remove a registered item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EXCHANGE_CANNOT_UNREGISTER_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400119);
	}

	/**
	 * You cannot register an amount of Kinah that is lower than the registered amount.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EXCHANGE_CANNOT_DECREASE_MONEY() {
		return new SM_SYSTEM_MESSAGE(1400120);
	}

	/**
	 * The client's regional code is not compatible with the game server.
	 */
	public static SM_SYSTEM_MESSAGE STR_ERROR_WORLD_LOCAL_CODE_FAIL() {
		return new SM_SYSTEM_MESSAGE(1400121);
	}

	/**
	 * You cannot get any PVP XP from the current target for a while.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_GET_PVP_EXP_TARGET_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1400122);
	}

	/**
	 * You cannot get any PVP XP for a while as you have gained too many PVP XP in too short a period of time.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_GET_PVP_EXP_TIMEBASE_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1400123);
	}

	/**
	 * You cannot register the target as your Friend as you have been blocked by the player.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_CANNOT_BLOCK_ME() {
		return new SM_SYSTEM_MESSAGE(1400124);
	}

	/**
	 * You rolled the dice and got a %num0 (max. %num1).
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ITEM_DICE_CUSTOM_ME(int num0, int num1) {
		return new SM_SYSTEM_MESSAGE(1400125, num0, num1);
	}

	/**
	 * You rolled the dice and got a %num0 (max. %num1).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_CUSTOM_ME(int num0, int num1) {
		return new SM_SYSTEM_MESSAGE(1400126, num0, num1);
	}

	/**
	 * %0 rolled the dice and got a %num1 (max. %num2).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DICE_CUSTOM_OTHER(String value0, int num1, int num2) {
		return new SM_SYSTEM_MESSAGE(1400127, value0, num1, num2);
	}

	/**
	 * You cannot invite the player to the force as the group leader of the player is in an Instanced Zone.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANT_INVITE_WHEN_HE_IS_IN_INSTANCE() {
		return new SM_SYSTEM_MESSAGE(1400128);
	}

	/**
	 * You cannot use the selected function in the current restriction phase.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_TARGET_IS_NOT_VALID() {
		return new SM_SYSTEM_MESSAGE(1400129);
	}

	/**
	 * You cannot preview this item as it can only be used by the opposite sex,
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PREVIEW_INVALID_GENDER() {
		return new SM_SYSTEM_MESSAGE(1400130);
	}

	/**
	 * You have item(s) left to settle at the Broker.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_VENDOR_ACCOUNT_IS_NOT_EMPTY() {
		return new SM_SYSTEM_MESSAGE(1400131);
	}

	/**
	 * You cannot use a Rift until the curse is removed.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DIRECT_PORTAL_BY_SLAYER() {
		return new SM_SYSTEM_MESSAGE(1400132);
	}

	/**
	 * %0 has logged in.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOTIFY_LOGIN_GUILD(String value0) {
		return new SM_SYSTEM_MESSAGE(1400133, value0);
	}

	/**
	 * You have sold %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PERSONAL_SHOP_SELL_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400134, value0);
	}

	/**
	 * You have sold %num1 %0s.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PERSONAL_SHOP_SELL_ITEM_MULTI(long num1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400135, value0, num1);
	}

	/**
	 * You can now use the chatting functions again.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CAN_CHAT_NOW() {
		return new SM_SYSTEM_MESSAGE(1400136);
	}

	/**
	 * You are now under level %0 curse of the Empyrean Lords for killing too many lower level targets in the opposition territory.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SLAYER_UPGRADE_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400137, value0);
	}

	/**
	 * The curse of the Empyrean Lords has been reduced to %0 level because you haven't slaughtered the lower level targets for a certain time.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SLAYER_DEGRADE_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400138, value0);
	}

	/**
	 * The curse of Empyrean Lord has been removed because you haven't slaughtered the lower level targets for a certain time.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SLAYER_DEGRADE_TO_NOMAL_LEVEL() {
		return new SM_SYSTEM_MESSAGE(1400139);
	}

	/**
	 * Brave %0 has defeated notorious %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SLAYER_DEATH_TO_B(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400140, value0, value1);
	}

	/**
	 * Hero of Asmodian %0 killed the Divinely Punished Intruder %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SLAYER_LIGHT_DEATH_TO_B(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400141, value0, value1);
	}

	/**
	 * Hero of Elyos %0 killed the Divinely Punished Intruder %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SLAYER_DARK_DEATH_TO_B(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400142, value0, value1);
	}

	/**
	 * You are now in %0 state because you've killed too many lower level targets in the opposition territory.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SLAYER_UP_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400143, value0);
	}

	/**
	 * %0 has been lowered to %1 because you haven't killed the lower level targets for a certain time.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SLAYER_DOWN_LEVEL(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400144, value0, value1);
	}

	/**
	 * %0 is removed because you haven't killed the lower level targets for a certain time.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SLAYER_DOWN_TO_NOMAL_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400145, value0);
	}

	/**
	 * %0 is crafting %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_OTHER_combine_START(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400146, value0, value1);
	}

	/**
	 * %0 successfully crafted %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_OTHER_combine_SUCCESS(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400147, value0, value1);
	}

	/**
	 * %0 failed to craft %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_OTHER_combine_FAIL(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400148, value0, value1);
	}

	/**
	 * You cannot use a Rift until the %0 is removed.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DIRECT_PORTAL_BY_SLAYER_GRADE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400149, value0);
	}

	/**
	 * Only the Legion Brigade General can change his Legion name.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EDIT_GUILD_NAME_ERROR_ONLY_MASTER_CAN_CHANGE_NAME() {
		return new SM_SYSTEM_MESSAGE(1400150);
	}

	/**
	 * Invalid character name.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EDIT_CHAR_NAME_ERROR_WRONG_INPUT() {
		return new SM_SYSTEM_MESSAGE(1400151);
	}

	/**
	 * Invalid Legion name.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EDIT_GUILD_NAME_ERROR_WRONG_INPUT() {
		return new SM_SYSTEM_MESSAGE(1400152);
	}

	/**
	 * The character name is already in use. Enter another name.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EDIT_CHAR_NAME_ERROR_SAME_YOUR_NAME() {
		return new SM_SYSTEM_MESSAGE(1400153);
	}

	/**
	 * The Legion name is already in use. Enter another name.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EDIT_GUILD_NAME_ERROR_SAME_YOUR_NAME() {
		return new SM_SYSTEM_MESSAGE(1400154);
	}

	/**
	 * A character is using the name. Enter another name.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EDIT_CHAR_NAME_ALREADY_EXIST() {
		return new SM_SYSTEM_MESSAGE(1400155);
	}

	/**
	 * A Legion is using the name. Enter another name.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EDIT_GUILD_NAME_ALREADY_EXIST() {
		return new SM_SYSTEM_MESSAGE(1400156);
	}

	/**
	 * The character name has been changed to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EDIT_CHAR_NAME_SUCCESS(String value0) {
		return new SM_SYSTEM_MESSAGE(1400157, value0);
	}

	/**
	 * The Legion name has been changed to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EDIT_GUILD_NAME_SUCCESS(String value0) {
		return new SM_SYSTEM_MESSAGE(1400158, value0);
	}

	/**
	 * Failed to change the name. Error code is %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EDIT_NAME_ERROR_DEFAULT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400159, value0);
	}

	/**
	 * You cannot change the Legion name while occupying the fortress or Artifact.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EDIT_GUILD_NAME_CANT_FOR_HAVING_HOUSE() {
		return new SM_SYSTEM_MESSAGE(1400160);
	}

	/**
	 * You can neither talk with NPCs nor use any useful functions in your current Restriction Phase.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BOT_CANNOT_USE_NPC_UTILITY() {
		return new SM_SYSTEM_MESSAGE(1400161);
	}

	/**
	 * You cannot trade with other characters in your current Restriction Phase.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BOT_CANNOT_USE_PC_TRADE() {
		return new SM_SYSTEM_MESSAGE(1400162);
	}

	/**
	 * You are automatically excluded from the group because the auto hunting reports have accumulated to the limit.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_BANISHED_FROM_PARTY() {
		return new SM_SYSTEM_MESSAGE(1400163);
	}

	/**
	 * You are automatically excluded from the force because the auto hunting reports have accumulated to the limit.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ACCUSE_BANISHED_FROM_FORCE() {
		return new SM_SYSTEM_MESSAGE(1400164);
	}

	/**
	 * The Energy of Repose is ineffective in your current Restriction Phase.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BOT_CANNOT_RECEIVE_VITAL_BONUS() {
		return new SM_SYSTEM_MESSAGE(1400165);
	}

	/**
	 * The selected user cannot do any trading at the moment.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_USE_PC_TRADE_TO_BOT() {
		return new SM_SYSTEM_MESSAGE(1400166);
	}

	/**
	 * You cannot glide in this area.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOGLIDE_AREA() {
		return new SM_SYSTEM_MESSAGE(1400167);
	}

	/**
	 * You are forced to stop gliding because you've entered the no glide area.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOGLIDE_AREA_STOP() {
		return new SM_SYSTEM_MESSAGE(1400168);
	}

	/**
	 * The remaining active time of the registered Kisk is %DURATIONTIME0.
	 */
	public static SM_SYSTEM_MESSAGE STR_BINDSTONE_WARNING_REMAIN_TIME(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400169, durationtime0);
	}

	/**
	 * You cannot change the name of the Legion during the disbanding mode.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EDIT_GUILD_NAME_CANT_FOR_DISPERSING_GUILD() {
		return new SM_SYSTEM_MESSAGE(1400170);
	}

	/**
	 * You cannot report auto hunting in the current region.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_ACCUSE_IN_THIS_ZONE() {
		return new SM_SYSTEM_MESSAGE(1400171);
	}

	/**
	 * You have purchased the warehouse expansion item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INGAMESHOP_DUPLICATED_WAREHOUSE() {
		return new SM_SYSTEM_MESSAGE(1400172);
	}

	/**
	 * Channel Host
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_CHANNEL_HOST() {
		return new SM_SYSTEM_MESSAGE(1400173);
	}

	/**
	 * As your character name has changed, you are removed from all joined channels.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_CHANNEL_CHAR_NAME_CHANGED1() {
		return new SM_SYSTEM_MESSAGE(1400174);
	}

	/**
	 * As your character name has changed, you are removed from all joined channels. (including any participating private channels)
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_CHANNEL_CHAR_NAME_CHANGED2() {
		return new SM_SYSTEM_MESSAGE(1400175);
	}

	/**
	 * Group
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_CHANNEL_PARTY() {
		return new SM_SYSTEM_MESSAGE(1400176);
	}

	/**
	 * You can no longer use %0 as the number of allowed usage has been reached.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_USE_DUPLICATED_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400177, value0);
	}

	/**
	 * You may reenter %WORLDNAME1 after %DURATIONTIME0 has passed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_INSTANCE_COOL_TIME_REMAIN(String worldname1, String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400178, worldname1, durationtime0);
	}

	/**
	 * You cannot enter the selected Instanced Zone at your level.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_INSTANCE_ENTER_LEVEL() {
		return new SM_SYSTEM_MESSAGE(1400179);
	}

	/**
	 * The %num0 player limit of %WORLDNAME1 has been exceeded.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_INSTANCE_TOO_MANY_MEMBERS(int num0, int mapId) {
		return new SM_SYSTEM_MESSAGE(1400180, num0, mapId);
	}

	/**
	 * You have already applied to enter %WORLDNAME0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_INSTANCE_ALREADY_REGISTERED(int worldId) {
		return new SM_SYSTEM_MESSAGE(1400181, worldId);
	}

	/**
	 * Only the force captain, vice captain or group leader can apply for group entry.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_INSTANCE_NOT_LEADER() {
		return new SM_SYSTEM_MESSAGE(1400182);
	}

	/**
	 * You aborted entering %0. You can apply again after 10 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_REGISTER_CANCELED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400183, value0);
	}

	/**
	 * You aborted entering %0. You can try again after 10 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_ENTER_GIVEUP(String value0) {
		return new SM_SYSTEM_MESSAGE(1400184, value0);
	}

	/**
	 * You are not able to enter the Instanced Zone right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_INSTANCE_ENTER_STATE() {
		return new SM_SYSTEM_MESSAGE(1400185);
	}

	/**
	 * You have failed to make an entry application.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_INSTANCE_ENTER_NOTICE() {
		return new SM_SYSTEM_MESSAGE(1400186);
	}

	/**
	 * %0 is not able to enter the Instanced Zone right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_INSTANCE_ENTER_MEMBER(String value0) {
		return new SM_SYSTEM_MESSAGE(1400187, value0);
	}

	/**
	 * The number of your private channel may have been changed with the deletion of the %0 Channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CHANGE_CHANNEL5(String value0) {
		return new SM_SYSTEM_MESSAGE(1400188, value0);
	}

	/**
	 * You have applied to join %0's group.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_MATCH_JUST_SENT_APPLY(String value0) {
		return new SM_SYSTEM_MESSAGE(1400189, value0);
	}

	/**
	 * You have invited %0 to join your group.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_MATCH_JUST_INVITE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400190, value0);
	}

	/**
	 * You have applied to join %0's alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_MATCH_JUST_SENT_APPLY(String value0) {
		return new SM_SYSTEM_MESSAGE(1400191, value0);
	}

	/**
	 * You have invited %0 to join the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_MATCH_JUST_INVITE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400192, value0);
	}

	/**
	 * That player is already being resurrected.
	 */
	public static SM_SYSTEM_MESSAGE STR_OTHER_USER_USE_RESURRECTDEBUFF_SKILL_ALREADY() {
		return new SM_SYSTEM_MESSAGE(1400193);
	}

	/**
	 * You have successfully made an entry application.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_REGISTER_SUCCESS() {
		return new SM_SYSTEM_MESSAGE(1400194);
	}

	/**
	 * The attack time remaining is %DURATIONTIME0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_REMAIN_TIME(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400195, durationtime0);
	}

	/**
	 * Infiltration of Dark Poeta now commences.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_START_IDLF1() {
		return new SM_SYSTEM_MESSAGE(1400196);
	}

	/**
	 * %0 is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_BOSS_ATTACKED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400198, value0);
	}

	/**
	 * %0 has destroyed %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_ROOM_DESTROYED(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400199, value0, value1);
	}

	/**
	 * The group or force no longer exists.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_MATCH_NOT_EXIST() {
		return new SM_SYSTEM_MESSAGE(1400200);
	}

	/**
	 * The %num0 player limit of %WORLDNAME1 has been exceeded.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_ENTER_INSTANCE_MAX_COUNT(int num0, String worldname1) {
		return new SM_SYSTEM_MESSAGE(1400201, num0, worldname1);
	}

	/**
	 * The attack time remaining is %DURATIONTIME0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_REMAIN_TIME_60(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400202, durationtime0);
	}

	/**
	 * The attack time remaining is %DURATIONTIME0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_REMAIN_TIME_30(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400203, durationtime0);
	}

	/**
	 * The attack time remaining is %DURATIONTIME0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_REMAIN_TIME_10(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400204, durationtime0);
	}

	/**
	 * The attack time remaining is %DURATIONTIME0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_REMAIN_TIME_5(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400205, durationtime0);
	}

	/**
	 * The effective time has expired and the link is no longer active.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CMD_LINK_EXPIRED() {
		return new SM_SYSTEM_MESSAGE(1400206);
	}

	/**
	 * %0 can't apply to join the selected group as he or she is already a member of an alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_MATCH_CANT_USE_PARTY_MATCH(String value0) {
		return new SM_SYSTEM_MESSAGE(1400207, value0);
	}

	/**
	 * You cannot preview this item as it can only be used by the other race.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PREVIEW_INVALID_RACE() {
		return new SM_SYSTEM_MESSAGE(1400208);
	}

	/**
	 * You cannot preview this item as you can't use this appearance modifying item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PREVIEW_INVALID_COSMETIC() {
		return new SM_SYSTEM_MESSAGE(1400209);
	}

	/**
	 * You cannot preview this item as there is no appearance image.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PREVIEW_NO_EXIST_COSMETIC_DATA() {
		return new SM_SYSTEM_MESSAGE(1400210);
	}

	/**
	 * There is no Greater Stigma slot available.
	 */
	public static SM_SYSTEM_MESSAGE STR_ENHANCED1_STIGMA_SLOT_IS_NOT_OPENED() {
		return new SM_SYSTEM_MESSAGE(1400211);
	}

	/**
	 * You cannot use invite, leave or kick commands related to your group or force in this region.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_CANT_OPERATE_PARTY_COMMAND() {
		return new SM_SYSTEM_MESSAGE(1400212);
	}

	/**
	 * You must first learn the prerequisite skill to equip %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_TO_EQUIP_STONE_LEARN_PRESKILL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400213, value0);
	}

	/**
	 * You cannot deactivate %0 as it is a prerequisite skill of %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_STIGMA_CANT_UNEQUIP_STONE_FOR_AFTERSKILL(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400214, value0, value1);
	}

	/**
	 * You already applied to join %0's group. You may apply for Recruit Group once every 15 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_MATCH_ALREADY_SENT_APPLY(String value0) {
		return new SM_SYSTEM_MESSAGE(1400215, value0);
	}

	/**
	 * You already applied to join %0's force. You may apply for Recruit Alliance once every 15 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_MATCH_ALREADY_SENT_APPLY(String value0) {
		return new SM_SYSTEM_MESSAGE(1400216, value0);
	}

	/**
	 * It's a shame, but let's play together next time. Have a good time in Aion!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_MATCH_DECLINED() {
		return new SM_SYSTEM_MESSAGE(1400217);
	}

	/**
	 * It's a shame, but let's play together next time. Have a good time in Aion!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_MATCH_DECLINED() {
		return new SM_SYSTEM_MESSAGE(1400218);
	}

	/**
	 * You cannot enter as you do not have the required item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_CANT_ENTER_WITHOUT_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400219);
	}

	/**
	 * %DURATIONTIME0 remaining.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REMAIN_TIME(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400220, durationtime0);
	}

	/**
	 * %DURATIONTIME0 remaining.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REMAIN_TIME_60(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400221, durationtime0);
	}

	/**
	 * %DURATIONTIME0 remaining.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REMAIN_TIME_30(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400222, durationtime0);
	}

	/**
	 * %DURATIONTIME0 remaining.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REMAIN_TIME_10(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400223, durationtime0);
	}

	/**
	 * %DURATIONTIME0 remaining.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REMAIN_TIME_5(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400224, durationtime0);
	}

	/**
	 * Characters under level %0 cannot use Channel Chat.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_CHANNELCHAT_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400225, value0);
	}

	/**
	 * The Portside Defense Shield has been generated at the Ready Room 1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LEFTWALL_CREATED_IDAB1_DREADGION() {
		return new SM_SYSTEM_MESSAGE(1400226);
	}

	/**
	 * The Starboard Defense Shield has been generated at the Ready Room 2.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_RIGHTWALL_CREATED_IDAB1_DREADGION() {
		return new SM_SYSTEM_MESSAGE(1400227);
	}

	/**
	 * A Portside Central Teleporter has been generated at the Escape Hatch.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LEFTTELEPORTER_CREATED_IDAB1_DREADGION() {
		return new SM_SYSTEM_MESSAGE(1400228);
	}

	/**
	 * A Starboard Central Teleporter has been generated at the Secondary Escape Hatch.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_RIGHTTELEPORTER_CREATED_IDAB1_DREADGION() {
		return new SM_SYSTEM_MESSAGE(1400229);
	}

	/**
	 * The Portside Door of Captain's Cabin has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LEFTDOOR_DESTROYED_IDAB1_DREADGION() {
		return new SM_SYSTEM_MESSAGE(1400230);
	}

	/**
	 * The Starboard Door of Captain's Cabin has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_RIGHTDOOR_DESTROYED_IDAB1_DREADGION() {
		return new SM_SYSTEM_MESSAGE(1400231);
	}

	/**
	 * %num1 %0(s) remaining.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_LEFT(int num1, String value0s) {
		return new SM_SYSTEM_MESSAGE(1400232, num1, value0s);
	}

	/**
	 * Prepare for Battle!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_PREPARE_TIME() {
		return new SM_SYSTEM_MESSAGE(1400233);
	}

	/**
	 * A Captain's Cabin Teleport Device that can be used for 3 minutes has been generated at the end of the Central Passage.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BOSSTELEPORTER_CREATED_IDAB1_DREADGION() {
		return new SM_SYSTEM_MESSAGE(1400234);
	}

	/**
	 * You cannot enter %WORLDNAME0 as the entry time has expired.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_CANT_ENTER_FOR_TIMEOVER(int worldId) {
		return new SM_SYSTEM_MESSAGE(1400235, worldId);
	}

	/**
	 * Exceeded %num0 points!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_SCORE_ALARM(int num0) {
		return new SM_SYSTEM_MESSAGE(1400236, num0);
	}

	/**
	 * You have gained %num1 points from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_SCORE(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1400237, value0, num1);
	}

	/**
	 * You cannot open a private store in this region.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_OPEN_STORE_IN_THIS_ZONE() {
		return new SM_SYSTEM_MESSAGE(1400238);
	}

	/**
	 * You have joined the %0 region channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_CHANNEL_JOIN_ZONE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400239, value0);
	}

	/**
	 * You have joined the %0 trade channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_CHANNEL_JOIN_TRADE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400240, value0);
	}

	/**
	 * You have already sent an Unavailable message to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_MATCH_ALREADY_SENT_DECLINE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400241, value0);
	}

	/**
	 * You have already sent a Reject Alliance message to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORCE_MATCH_ALREADY_SENT_DECLINE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400242, value0);
	}

	/**
	 * The protective magic ward of Balaur has been activated.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_START_IDABRE() {
		return new SM_SYSTEM_MESSAGE(1400243);
	}

	/**
	 * All the treasure chests of Balaur have disappeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TREASUREBOX_DESPAWN_ALL() {
		return new SM_SYSTEM_MESSAGE(1400244);
	}

	/**
	 * One treasure chest of Balaur has disappeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TREASUREBOX_DESPAWN_ONE() {
		return new SM_SYSTEM_MESSAGE(1400245);
	}

	/**
	 * %0 is open and you can now access %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDSHULACKSHIP_OPEN_DOOR(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400246, value0, value1);
	}

	/**
	 * You cannot register because the limit of characters that can register on the Kisk has been reached.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_REGISTER_BINDSTONE_FULL() {
		return new SM_SYSTEM_MESSAGE(1400247);
	}

	/**
	 * Grogget's Safe door is open and you can now access Grogget's Safe.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDSHULACKSHIP_OPEN_DOOR_01() {
		return new SM_SYSTEM_MESSAGE(1400248);
	}

	/**
	 * The Brig door is open and you can now access The Brig.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDSHULACKSHIP_OPEN_DOOR_02() {
		return new SM_SYSTEM_MESSAGE(1400249);
	}

	/**
	 * The Generator Chamber access door is open and you can now access the Drana Generator Chamber.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDSHULACKSHIP_OPEN_DOOR_03() {
		return new SM_SYSTEM_MESSAGE(1400250);
	}

	/**
	 * The Large Gun Deck door is open and you can now access the Large Gun Deck.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDSHULACKSHIP_OPEN_DOOR_04() {
		return new SM_SYSTEM_MESSAGE(1400251);
	}

	/**
	 * The infiltration route into Dredgion is open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_OPEN_IDAB1_DREADGION() {
		return new SM_SYSTEM_MESSAGE(1400252);
	}

	/**
	 * The Abyss Gate will operate for 5 minutes only.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_PORTAL_TIME() {
		return new SM_SYSTEM_MESSAGE(1400253);
	}

	/**
	 * You may only battle %0 within the given time limit.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_BATTLE_TIME(String value0) {
		return new SM_SYSTEM_MESSAGE(1400254, value0);
	}

	/**
	 * %0 has left the battle.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_BATTLE_END(String value0) {
		return new SM_SYSTEM_MESSAGE(1400255, value0);
	}

	/**
	 * You cannot gain any more Abyss Points because you reached the maximum Abyss Points you can get for your current level.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_GET_AP_LEVELBASE_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1400256);
	}

	/**
	 * You may only battle Tahabata Pyrelord within the given time limit.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_S_RANK_BATTLE_TIME() {
		return new SM_SYSTEM_MESSAGE(1400257);
	}

	/**
	 * Tahabata Pyrelord has left the battle.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_S_RANK_BATTLE_END() {
		return new SM_SYSTEM_MESSAGE(1400258);
	}

	/**
	 * You may only battle Lord of Flame Calindi within the given time limit.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_A_RANK_BATTLE_TIME() {
		return new SM_SYSTEM_MESSAGE(1400259);
	}

	/**
	 * Lord of Flame Calindi has left the battle.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_A_RANK_BATTLE_END() {
		return new SM_SYSTEM_MESSAGE(1400260);
	}

	/**
	 * Connection will time out in %DURATIONTIME0. Please take a break.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_USER_KICKED_BY_TIMEOUT(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400261, durationtime0);
	}

	/**
	 * The Steel Beard Pirates have begun hiding the Key Boxes.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDSHULACKSHIP_TIMER_START() {
		return new SM_SYSTEM_MESSAGE(1400262);
	}

	/**
	 * All the Key Boxes have disappeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDSHULACKSHIP_TIMER_END() {
		return new SM_SYSTEM_MESSAGE(1400263);
	}

	/**
	 * The opposition has withdrawn from the Dredgion infiltration mission. The mission will stop in %DURATIONTIME0% and you will leave the Dredgion.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ALARM_COLD_GAME_IDAB1_DREADGION(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400264, durationtime0);
	}

	/**
	 * A Nuclear Control Room Teleporter has been created at the Emergency Exit.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NUCLEARTELEPORTER_CREATED_IDAB1_DREADGION() {
		return new SM_SYSTEM_MESSAGE(1400265);
	}

	/**
	 * Characters under level %0 cannot send Alliance invitations.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_ALLIANCE_TOO_LOW_LEVEL_TO_INVITE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400266, value0);
	}

	/**
	 * Only those at or under level %0 can use %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ITEM_TOO_HIGH_LEVEL(int value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400267, value0, value1);
	}

	/**
	 * You were poisoned during extraction and cannot extract for %DURATIONTIME0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CAPTCHA_RESTRICTED(int durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400268, durationtime0);
	}

	/**
	 * You have recovered from poisoning and can extract again.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CAPTCHA_RECOVERED() {
		return new SM_SYSTEM_MESSAGE(1400269);
	}

	/**
	 * You chanted a spell to cleanse the poison from your body. You can now extract again.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CAPTCHA_UNRESTRICT() {
		return new SM_SYSTEM_MESSAGE(1400270);
	}

	/**
	 * Your incantation was incorrect; you failed to purify the poison. You have %0 attempts left.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CAPTCHA_UNRESTRICT_FAILED_RETRY(int value0) {
		return new SM_SYSTEM_MESSAGE(1400271, value0);
	}

	/**
	 * Your incantation was incorrect; you failed to purify the poison.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CAPTCHA_UNRESTRICT_FAILED() {
		return new SM_SYSTEM_MESSAGE(1400272);
	}

	/**
	 * You are currently poisoned and unable to extract. (Time remaining: %DURATIONTIME0)
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CAPTCHA_REMAIN_RESTRICT_TIME(int duration) {
		return new SM_SYSTEM_MESSAGE(1400273, duration);
	}

	/**
	 * You are able to extract.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CAPTCHA_NOT_RESTRICTED() {
		return new SM_SYSTEM_MESSAGE(1400274);
	}

	/**
	 * A dimensional corridor that leads to the Indratu Fortress has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_PORTAL_OPEN_IDLF3_Castle_Indratoo() {
		return new SM_SYSTEM_MESSAGE(1400275);
	}

	/**
	 * A dimensional corridor that leads to the Draupnir Cave has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_PORTAL_OPEN_IDDF3_Dragon() {
		return new SM_SYSTEM_MESSAGE(1400276);
	}

	/**
	 * You gained %num0 points.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_SCORE_FOR_ENEMY(int num0) {
		return new SM_SYSTEM_MESSAGE(1400277, num0);
	}

	/**
	 * You cannot fly while your pet is banned from flying.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_FLY_NOW_DUE_TO_NOFLY_FROM_PET() {
		return new SM_SYSTEM_MESSAGE(1400278);
	}

	/**
	 * You cannot extract from equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_DECOMPOSE_EQUIP_ITEM_CAN_NOT_BE_DECOMPOSED() {
		return new SM_SYSTEM_MESSAGE(1400279);
	}

	/**
	 * The remaining playing time is %*0.
	 */
	public static SM_SYSTEM_MESSAGE STR_REMAIN_PLAYTIME_CENTER_DISPLAY(String value0) {
		return new SM_SYSTEM_MESSAGE(1400280, value0);
	}

	/**
	 * You do not have enough Jewels of Eternity to buy the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUY_SELL_NOT_ENOUGH_AIONJEWELS_TO_BUY_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400281);
	}

	/**
	 * This modification cannot be completed as the gender or race requirements for %0 and %1 are different.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_CHANGE_OPPOSITE_ITEM_SKIN(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400282, value0, value1);
	}

	/**
	 * %0 cannot be used for modification.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHANGE_ITEM_SKIN_NOT_SKIN_EXTRACTABLE_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400283, value0);
	}

	/**
	 * You must pass the Expert test in order to be promoted.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHER_CANT_EXTEND_MASTER() {
		return new SM_SYSTEM_MESSAGE(1400284);
	}

	/**
	 * You must pass the Artisan test in order to be promoted.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_CANT_EXTEND_HIGH_MASTER() {
		return new SM_SYSTEM_MESSAGE(1400285);
	}

	/**
	 * You must pass the Master test in order to be promoted.
	 */
	public static SM_SYSTEM_MESSAGE STR_CRAFT_CANT_EXTEND_GRAND_MASTER() {
		return new SM_SYSTEM_MESSAGE(1400286);
	}

	/**
	 * Crafting %0 has used up the recipe.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMBINE_USAGE_OVER(String value0) {
		return new SM_SYSTEM_MESSAGE(1400287, value0);
	}

	/**
	 * The level of the item to be combined must be higher than that of the one to be extracted.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMPOUND_ERROR_MAIN_REQUIRE_HIGHER_LEVEL() {
		return new SM_SYSTEM_MESSAGE(1400288);
	}

	/**
	 * %0 cannot be combined.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMPOUND_ERROR_NOT_AVAILABLE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400289, value0);
	}

	/**
	 * This modification cannot be completed as the equipment requirements for %0 and %1 are different.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_CHANGE_SKIN_OPPOSITE_REQUIREMENT(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400290, value0, value1);
	}

	/**
	 * The appearance maintain time for %0 has expired and the appearance modification effect has been removed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SKIN_CHANGE_TIME_EXPIRED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400291, value0);
	}

	/**
	 * All fortresses in Inggison and Gelkmaros have changed to the Capturable State.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_PVP_ON() {
		return new SM_SYSTEM_MESSAGE(1400292);
	}

	/**
	 * %0 is no longer vulnerable.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_PVP_OFF(String value0) {
		return new SM_SYSTEM_MESSAGE(1400293, value0);
	}

	/**
	 * The Guardian General is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_BOSS_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1400294);
	}

	/**
	 * %1 of the %0 killed the Guardian General.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_BOSS_KILLED(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400295, value1, value0);
	}

	/**
	 * The Balaur have killed the Guardian General.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DRAGON_BOSS_KILLED() {
		return new SM_SYSTEM_MESSAGE(1400296);
	}

	/**
	 * %0 has conquered %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_GUILD_WIN_CASTLE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400297, value0, value1);
	}

	/**
	 * %0 succeeded in conquering %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_WIN_CASTLE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400298, value0, value1);
	}

	/**
	 * %0 Legion lost %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_GUILD_CASTLE_TAKEN(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400299, value0, value1);
	}

	/**
	 * %0 failed to defend %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_CASTLE_TAKEN(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400300, value0, value1);
	}

	/**
	 * %1 of %0 obtained the Artifact %2.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_EVENT_WIN_FIELDARTIFACT(String value1, String value0, String value2) {
		return new SM_SYSTEM_MESSAGE(1400301, value1, value0, value2);
	}

	/**
	 * %1 lost the Artifact %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_EVENT_LOSE_FIELDARTIFACT(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400302, value1, value0);
	}

	/**
	 * The Castle Gate is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DOOR_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1400303);
	}

	/**
	 * The Castle Gate is in danger.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DOOR_ATSTAKE() {
		return new SM_SYSTEM_MESSAGE(1400304);
	}

	/**
	 * %1 of the %0 destroyed the Castle Gate.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DOOR_BROKEN(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400305, value1, value0);
	}

	/**
	 * The Balaur have destroyed the Castle Gate.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DRAGON_DOOR_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1400306);
	}

	/**
	 * The Gate Guardian Stone is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_REPAIR_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1400307);
	}

	/**
	 * %1 of the %0 destroyed the Gate Guardian Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_REPAIR_BROKEN(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400308, value1, value0);
	}

	/**
	 * The Balaur have destroyed the Gate Guardian Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DRAGON_REPAIR_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1400309);
	}

	/**
	 * The Balaur Dredgion has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_CARRIER_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1400310);
	}

	/**
	 * The Dredgion has dropped Balaur Troopers.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_CARRIER_DROP_DRAGON() {
		return new SM_SYSTEM_MESSAGE(1400311);
	}

	/**
	 * The Balaur Dredgion has disappeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_CARRIER_DESPAWN() {
		return new SM_SYSTEM_MESSAGE(1400312);
	}

	/**
	 * %1 of %0 is activating the Artifact %2.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDARTIFACT_CASTING(String value1, String value0, String value2) {
		return new SM_SYSTEM_MESSAGE(1400313, value1, value0, value2);
	}

	/**
	 * The Artifact %1 core of %0 has been ejected.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDARTIFACT_CORE_CASTING(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400314, value1, value0);
	}

	/**
	 * The activation of the Artifact %1 of %0 was canceled.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDARTIFACT_CANCELED(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400315, value1, value0);
	}

	/**
	 * %1 of %0 has activated the Artifact %2.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDARTIFACT_FIRE(String value1, String value0, String value2) {
		return new SM_SYSTEM_MESSAGE(1400316, value1, value0, value2);
	}

	/**
	 * Kaisinel's Agent Veille has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_LIGHTBOSS_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1400317);
	}

	/**
	 * Marchutan's Agent Mastarius has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DARKBOSS_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1400318);
	}

	/**
	 * Kaisinel's Agent Veille has disappeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_LIGHTBOSS_DESPAWN() {
		return new SM_SYSTEM_MESSAGE(1400319);
	}

	/**
	 * Marchutan's Agent Mastarius has disappeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DARKBOSS_DESPAWN() {
		return new SM_SYSTEM_MESSAGE(1400320);
	}

	/**
	 * Kaisinel's Agent Veille is under attack!
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_LIGHTBOSS_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1400321);
	}

	/**
	 * Marchutan's Agent Mastarius is under attack!
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DARKBOSS_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1400322);
	}

	/**
	 * %1 of %0 has killed Marchutan's Agent Mastarius.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DARKBOSS_KILLED(String playerName, String legionName) {
		return new SM_SYSTEM_MESSAGE(1400323, legionName, playerName);
	}

	/**
	 * %1 of %0 has killed Kaisinel's Agent Veille.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_LIGHTBOSS_KILLED(String playerName, String legionName) {
		return new SM_SYSTEM_MESSAGE(1400324, legionName, playerName);
	}

	/**
	 * %0 is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_BARRIER_ATTACKED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400325, value0);
	}

	/**
	 * %0% is in danger!
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_BARRIER_ATSTAKE(String value0value) {
		return new SM_SYSTEM_MESSAGE(1400326, value0value);
	}

	/**
	 * %1 of %0 has destroyed %SUBZONE2.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_BARRIER_BROKEN(String value1, String value0, String subzone2) {
		return new SM_SYSTEM_MESSAGE(1400327, value1, value0, subzone2);
	}

	/**
	 * Silentera Westgate, the entrance from Inggison to Silentera Canyon, has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_LIGHTUNDERPASS_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1400328);
	}

	/**
	 * Silentera Eastgate, the entrance from Gelkmaros to Silentera Canyon, has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DARKUNDERPASS_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1400329);
	}

	/**
	 * Silentera Westgate, the entrance from Inggison to Silentera Canyon, has closed.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_LIGHTUNDERPASS_DESPAWN() {
		return new SM_SYSTEM_MESSAGE(1400330);
	}

	/**
	 * Silentera Eastgate, the entrance from Gelkmaros to Silentera Canyon, has closed.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DARKUNDERPASS_DESPAWN() {
		return new SM_SYSTEM_MESSAGE(1400331);
	}

	/**
	 * You cannot extract because you do not have the item required for Essencetapping.
	 */
	public static SM_SYSTEM_MESSAGE STR_GATHERING_REQUIRE_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400332);
	}

	/**
	 * You used %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_USE_CASH_TYPE_ITEM1(String value0) {
		return new SM_SYSTEM_MESSAGE(1400333, value0);
	}

	/**
	 * You used %1 %0s.
	 */
	public static SM_SYSTEM_MESSAGE STR_USE_CASH_TYPE_ITEM2(String value1, String value0s) {
		return new SM_SYSTEM_MESSAGE(1400334, value1, value0s);
	}

	/**
	 * The ability combined with %0 has been removed.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMPOUNDED_ITEM_DECOMPOUND_SUCCESS(String value0) {
		return new SM_SYSTEM_MESSAGE(1400335, value0);
	}

	/**
	 * %1 has been combined with %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMPOUND_SUCCESS(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400336, value0, value1);
	}

	/**
	 * You do not have enough Kinah to combine %0 and %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMPOUND_ERROR_NOT_ENOUGH_MONEY(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400337, value0, value1);
	}

	/**
	 * The target is immune to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WRONG_TARGET_CLASS(String value0) {
		return new SM_SYSTEM_MESSAGE(1400338, value0);
	}

	/**
	 * The target is immune to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WRONG_TARGET_RACE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400339, value0);
	}

	/**
	 * Characters under level %0 cannot send letters.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_MAIL_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400340, value0);
	}

	/**
	 * Characters under level %0 cannot use the search function.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_WHO_LEVEL(int value0) {
		return new SM_SYSTEM_MESSAGE(1400341, value0);
	}

	/**
	 * You have gained %num1 XP from %0 (Energy of Repose %num2).
	 */
	public static SM_SYSTEM_MESSAGE STR_GET_EXP_VITAL_BONUS(String value0, long num1, long num2) {
		return new SM_SYSTEM_MESSAGE(1400342, value0, num1, num2);
	}

	/**
	 * You have gained %num1 XP from %0 (Energy of Salvation %num2).
	 */
	public static SM_SYSTEM_MESSAGE STR_GET_EXP_MAKEUP_BONUS(String value0, long num1, long num2) {
		return new SM_SYSTEM_MESSAGE(1400343, value0, num1, num2);
	}

	/**
	 * You have gained %num1 XP from %0 (Energy of Repose %num2, Energy of Salvation %num3).
	 */
	public static SM_SYSTEM_MESSAGE STR_GET_EXP_VITAL_MAKEUP_BONUS(String value0, long num1, long num2, long num3) {
		return new SM_SYSTEM_MESSAGE(1400344, value0, num1, num2, num3);
	}

	/**
	 * You have gained %0 (Energy of Repose %num1).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_EXP_GAIN_VITAL_BONUS(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1400345, value0, num1);
	}

	/**
	 * You have gained %0 (Energy of Salvation %num1).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_EXP_GAIN_MAKEUP_BONUS(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1400346, value0, num1);
	}

	/**
	 * You have gained %0 (Energy of Repose %num1, Energy of Salvation %num2).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBAT_MY_EXP_GAIN_VITAL_MAKEUP_BONUS(String value0, int num1, int num2) {
		return new SM_SYSTEM_MESSAGE(1400347, value0, num1, num2);
	}

	/**
	 * You have gained %num0 XP (Energy of Repose %num1).
	 */
	public static SM_SYSTEM_MESSAGE STR_GET_EXP2_VITAL_BONUS(long num0, long num1) {
		return new SM_SYSTEM_MESSAGE(1400348, num0, num1);
	}

	/**
	 * You have gained %num0 XP (Energy of Salvation %num1).
	 */
	public static SM_SYSTEM_MESSAGE STR_GET_EXP2_MAKEUP_BONUS(long num0, long num1) {
		return new SM_SYSTEM_MESSAGE(1400349, num0, num1);
	}

	/**
	 * You have gained %num0 XP (Energy of Repose %num1, Energy of Salvation %num2).
	 */
	public static SM_SYSTEM_MESSAGE STR_GET_EXP2_VITAL_MAKEUP_BONUS(long num0, long num1, long num2) {
		return new SM_SYSTEM_MESSAGE(1400350, num0, num1, num2);
	}

	/**
	 * You have selected more items than there are remaining.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIMITED_SALE_CANT_SELECT_OVER_ITEMS() {
		return new SM_SYSTEM_MESSAGE(1400351);
	}

	/**
	 * This item is no longer available.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIMITED_SALE_CANT_SELECT_NO_ITEMS() {
		return new SM_SYSTEM_MESSAGE(1400352);
	}

	/**
	 * You cannot purchase the item because you have exceeded the purchase limit.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIMITED_BUYING_CANT_SELECT_NO_ITEMS() {
		return new SM_SYSTEM_MESSAGE(1400353);
	}

	/**
	 * You have selected more than the purchase limit of the item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIMITED_BUYING_CANT_SELECT_OVER_ITEMS() {
		return new SM_SYSTEM_MESSAGE(1400354);
	}

	/**
	 * You cannot store this item in the Legion warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WAREHOUSE_CANT_LEGION_DEPOSIT() {
		return new SM_SYSTEM_MESSAGE(1400355);
	}

	/**
	 * You cannot store this item in the account warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WAREHOUSE_CANT_ACCOUNT_DEPOSIT() {
		return new SM_SYSTEM_MESSAGE(1400356);
	}

	/**
	 * %WORLDNAME1 (difficulty: %2) with a %num0 player limit has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_DUNGEON_WITH_DIFFICULTY_OPENED(String worldname1, String value2, int num0) {
		return new SM_SYSTEM_MESSAGE(1400357, worldname1, value2, num0);
	}

	/**
	 * %WORLDNAME1 with a %num0 player limit has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_DUNGEON_OPENED(String worldname1, int num0) {
		return new SM_SYSTEM_MESSAGE(1400358, worldname1, num0);
	}

	/**
	 * %WORLDNAME1 (difficulty: %2) with a %num0 player limit is currently open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_DUNGEON_WITH_DIFFICULTY_OPENED_INFO(String worldname1, String value2, int num0) {
		return new SM_SYSTEM_MESSAGE(1400359, worldname1, value2, num0);
	}

	/**
	 * %WORLDNAME1 with a %num0 player limit is currently open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_DUNGEON_OPENED_INFO(String worldname1, int num0) {
		return new SM_SYSTEM_MESSAGE(1400360, worldname1, num0);
	}

	/**
	 * You can only enter after the Group Leader has created the instance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_DUNGEON_CANT_ENTER_NOT_OPENED() {
		return new SM_SYSTEM_MESSAGE(1400361);
	}

	/**
	 * You can only use this item in a cube.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REQUIRE_IN_INVENTORY() {
		return new SM_SYSTEM_MESSAGE(1400362);
	}

	/**
	 * Your cube is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DECOMPRESS_INVENTORY_IS_FULL() {
		return new SM_SYSTEM_MESSAGE(1400363);
	}

	/**
	 * You cannot combine different weapon types.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMPOUND_ERROR_DIFFERENT_TYPE() {
		return new SM_SYSTEM_MESSAGE(1400364);
	}

	/**
	 * This item cannot be registered for comparison.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMPOUND_ERROR_NOT_COMPARABLE_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400365);
	}

	/**
	 * The Seal of Uniformity has been weakened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTP_FANATIC_Die_Keynamed() {
		return new SM_SYSTEM_MESSAGE(1400366);
	}

	/**
	 * You can now enter the Chamber of Unity.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTP_FANATIC_DieAll_Keynamed() {
		return new SM_SYSTEM_MESSAGE(1400367);
	}

	/**
	 * %0 has blocked all Whispers from characters under level %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REJECT_WHISPER_FROM_LOW_LEVEL(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400368, value0, value1);
	}

	/**
	 * %0 has blocked all mail from characters under level %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REJECT_MAIL_FROM_LOW_LEVEL(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400369, value0, value1);
	}

	/**
	 * The appearance maintain time for %0 in the warehouse has expired and the appearance modification effect has been removed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SKIN_CHANGE_TIME_EXPIRED_IN_WAREHOUSE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400370, value0);
	}

	/**
	 * This modification cannot be completed as %0 and %1 have the same appearance.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_CHANGE_SAME_ITEM_SKIN(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400371, value0, value1);
	}

	/**
	 * The appearance modification effect of %0 has been removed.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNCHANGE_ITEM_SKIN_SUCCEED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400372, value0);
	}

	/**
	 * %0 is not a combined item.
	 */
	public static SM_SYSTEM_MESSAGE STR_DECOMPOUND_ERROR_NOT_AVAILABLE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400373, value0);
	}

	/**
	 * %0 is now selling rare items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIMIT_SALE_TEST_DESC01(String value0) {
		return new SM_SYSTEM_MESSAGE(1400374, value0);
	}

	/**
	 * You have sent too many mails at once and have been termporarily blocked. Please try again later.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_SEND_OVER_MAILS() {
		return new SM_SYSTEM_MESSAGE(1400375);
	}

	/**
	 * You do not have enough %0 to gather.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_GATHERING_B_ITEM_CHECK(String value0) {
		return new SM_SYSTEM_MESSAGE(1400376, value0);
	}

	/**
	 * Characters under level %0 cannot shout.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_SHOUT_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400377, value0);
	}

	/**
	 * Optimize Fortress Battle function has been toggled on for smooth game play. Characters in the vicinity are displayed in simplified forms.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CHAR_HIDE_AUTO_ON() {
		return new SM_SYSTEM_MESSAGE(1400378);
	}

	/**
	 * You are being blown away by the wind!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WindPathIN() {
		return new SM_SYSTEM_MESSAGE(1400379);
	}

	/**
	 * Manadar's hidden trap has been tripped!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_BombDrakan_TargetMSG() {
		return new SM_SYSTEM_MESSAGE(1400380);
	}

	/**
	 * The Subjugated Souls have been released!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_Spectre_Buff() {
		return new SM_SYSTEM_MESSAGE(1400381);
	}

	/**
	 * Captain Lakhara is preparing his final strike!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_TombDrakan() {
		return new SM_SYSTEM_MESSAGE(1400382);
	}

	/**
	 * Isbariya the Resolute is tapping into his true power!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_ArchPriest2_01() {
		return new SM_SYSTEM_MESSAGE(1400383);
	}

	/**
	 * The treasure chest vanished because you did not destroy the monsters within the time limit.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDAbRe_Core_Oops_Reward_Is_Gone() {
		return new SM_SYSTEM_MESSAGE(1400384);
	}

	/**
	 * The wind is too strong--you can't break away!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WindPathNoOUT() {
		return new SM_SYSTEM_MESSAGE(1400385);
	}

	/**
	 * Soulcaller's eyes glimmer!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_Summoner_Reflect() {
		return new SM_SYSTEM_MESSAGE(1400386);
	}

	/**
	 * Soulcaller casts the Powerful Smite skill!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_Summoner_DeadlyCasting() {
		return new SM_SYSTEM_MESSAGE(1400387);
	}

	/**
	 * Flarestorm is unleashing an unknown power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_ElementalFire_Buff() {
		return new SM_SYSTEM_MESSAGE(1400388);
	}

	/**
	 * %0 is now selling rare items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIMIT_SALE_TEST_DESC02(String value0) {
		return new SM_SYSTEM_MESSAGE(1400389, value0);
	}

	/**
	 * Someone in this village is selling unique items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIMIT_SALE_TEST_DESC03() {
		return new SM_SYSTEM_MESSAGE(1400390);
	}

	/**
	 * This message is for testing %0's limited sale.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIMIT_SALE_TEST_DESC04(String value0) {
		return new SM_SYSTEM_MESSAGE(1400391, value0);
	}

	/**
	 * Your request has been registered on the Recruit Group Member List.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_MATCH_OFFER_PARTY_POSTED() {
		return new SM_SYSTEM_MESSAGE(1400392);
	}

	/**
	 * Your request has been registered on the Apply For Group List.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_MATCH_SEEK_PARTY_POSTED() {
		return new SM_SYSTEM_MESSAGE(1400393);
	}

	/**
	 * Your Find Group request was removed because it has not been updated.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_MATCH_POST_DELETED_TOO_OLD() {
		return new SM_SYSTEM_MESSAGE(1400394);
	}

	/**
	 * Your Find Group request was removed because you have joined a Group or Alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_MATCH_POST_DELETED_ENTERED_PARTY() {
		return new SM_SYSTEM_MESSAGE(1400395);
	}

	/**
	 * Your Find Group request was removed because your Group or Alliance is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_MATCH_POST_DELETED_PARTY_FULL() {
		return new SM_SYSTEM_MESSAGE(1400396);
	}

	/**
	 * Your Find Group request was removed because the Group or Alliance disbanded.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_MATCH_POST_DELETED_PARTY_BROKE() {
		return new SM_SYSTEM_MESSAGE(1400397);
	}

	/**
	 * Characters under level %0 who are using a free trial cannot use the Broker.
	 */
	public static SM_SYSTEM_MESSAGE STR_FREE_EXPERIENCE_CHARACTER_CANT_USE_VENDOR(String value0) {
		return new SM_SYSTEM_MESSAGE(1400398, value0);
	}

	/**
	 * Characters under level %0 who are using a free trial cannot open a private store.
	 */
	public static SM_SYSTEM_MESSAGE STR_FREE_EXPERIENCE_CHARACTER_CANT_OPEN_PERSONAL_SHOP(String value0) {
		return new SM_SYSTEM_MESSAGE(1400399, value0);
	}

	/**
	 * Characters under level %0 who are playing a free trial cannot trade.
	 */
	public static SM_SYSTEM_MESSAGE STR_FREE_EXPERIENCE_CHARACTER_CANT_TRADE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400400, value0);
	}

	/**
	 * Characters under level %0 who are using a free trial cannot send mail containing items or money.
	 */
	public static SM_SYSTEM_MESSAGE STR_FREE_EXPERIENCE_CHARACTER_CANT_SEND_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400401, value0);
	}

	/**
	 * Characters under level %0 who are using a free trial cannot use the private warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_FREE_EXPERIENCE_CHARACTER_CANT_USE_WAREHOUSE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400402, value0);
	}

	/**
	 * Characters under level %0 who are using a free trial cannot use the Legion warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_FREE_EXPERIENCE_CHARACTER_CANT_USE_GUILD_WAREHOUSE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400403, value0);
	}

	/**
	 * Characters under level %0 who are using a free trial cannot use the Account warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_FREE_EXPERIENCE_CHARACTER_CANT_USE_ACCOUNT_WAREHOUSE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400404, value0);
	}

	/**
	 * Captain Adhati has appeared in the Captain's Cabin.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BOSS_SPAWN_IDAB1_DREADGION() {
		return new SM_SYSTEM_MESSAGE(1400405);
	}

	/**
	 * Usage time for %0 in the warehouse has expired.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DELETE_CASH_ITEM_BY_TIMEOUT_IN_WAREHOUSE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400406, value0);
	}

	/**
	 * Matches meeting your search conditions have been found.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_MATCH_SEARCH_FOUND() {
		return new SM_SYSTEM_MESSAGE(1400407);
	}

	/**
	 * 10 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_10() {
		return new SM_SYSTEM_MESSAGE(1400408);
	}

	/**
	 * 20 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_20() {
		return new SM_SYSTEM_MESSAGE(1400409);
	}

	/**
	 * 30 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_30() {
		return new SM_SYSTEM_MESSAGE(1400410);
	}

	/**
	 * 40 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_40() {
		return new SM_SYSTEM_MESSAGE(1400411);
	}

	/**
	 * 50 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_50() {
		return new SM_SYSTEM_MESSAGE(1400412);
	}

	/**
	 * 60 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_60() {
		return new SM_SYSTEM_MESSAGE(1400413);
	}

	/**
	 * 70 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_70() {
		return new SM_SYSTEM_MESSAGE(1400414);
	}

	/**
	 * 80 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_80() {
		return new SM_SYSTEM_MESSAGE(1400415);
	}

	/**
	 * 90 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_90() {
		return new SM_SYSTEM_MESSAGE(1400416);
	}

	/**
	 * 91 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_91() {
		return new SM_SYSTEM_MESSAGE(1400417);
	}

	/**
	 * 92 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_92() {
		return new SM_SYSTEM_MESSAGE(1400418);
	}

	/**
	 * 93 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_93() {
		return new SM_SYSTEM_MESSAGE(1400419);
	}

	/**
	 * 94 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_94() {
		return new SM_SYSTEM_MESSAGE(1400420);
	}

	/**
	 * 95 persons have gathered their power. The Empyrean Avatar has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_95() {
		return new SM_SYSTEM_MESSAGE(1400421);
	}

	/**
	 * 96 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_96() {
		return new SM_SYSTEM_MESSAGE(1400422);
	}

	/**
	 * 97 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_97() {
		return new SM_SYSTEM_MESSAGE(1400423);
	}

	/**
	 * 98 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_98() {
		return new SM_SYSTEM_MESSAGE(1400424);
	}

	/**
	 * 99 persons have gathered their power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_99() {
		return new SM_SYSTEM_MESSAGE(1400425);
	}

	/**
	 * 100 persons have gathered their power. You can now use the Empyrean Avatar.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_COUNT_100() {
		return new SM_SYSTEM_MESSAGE(1400426);
	}

	/**
	 * You have failed to use the Empyrean Avatar. You will need to gather power and summon it again.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_DEATHBLOW_FAIL() {
		return new SM_SYSTEM_MESSAGE(1400427);
	}

	/**
	 * The first Sphere of Mirage has been activated.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_BUFF_FIRST_OBJECT_ON() {
		return new SM_SYSTEM_MESSAGE(1400428);
	}

	/**
	 * The second Sphere of Mirage has been activated. Kaisinel's Agent Veille prepares to cast the Empyrean Lord's blessing.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_BUFF_SECOND_OBJECT_ON() {
		return new SM_SYSTEM_MESSAGE(1400429);
	}

	/**
	 * You may use the Sphere of Mirage again.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_BUFF_CAN_USE_OBJECT() {
		return new SM_SYSTEM_MESSAGE(1400430);
	}

	/**
	 * You need more people to activate the Sphere of Mirage.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_BUFF_CANT_USE_OBJECT_NOT_ENOUGH_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1400431);
	}

	/**
	 * You are marked as Unavailable. Please reset the setting in System Preferences to accept the invitation.
	 */
	public static SM_SYSTEM_MESSAGE STR_INFORM_INVITE_REJECT_STATE() {
		return new SM_SYSTEM_MESSAGE(1400434);
	}

	/**
	 * %0 has succeeded in enchanting %1 to Level 15.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ENCHANT_ITEM_SUCCEEDED_15(String playerName, String value1) {
		return new SM_SYSTEM_MESSAGE(1400435, playerName, value1);
	}

	/**
	 * %0 is selling items to extract vitality and Aether.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIMIT_SALE_GATHERING_DESC01(String value0) {
		return new SM_SYSTEM_MESSAGE(1400436, value0);
	}

	/**
	 * You cannot join this race.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_JOIN_ERROR_RACE() {
		return new SM_SYSTEM_MESSAGE(1400437);
	}

	/**
	 * You can only join when your level is %0 or above.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_JOIN_ERROR_MIN_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400438, value0);
	}

	/**
	 * %0 is selling materials to create the items of Crafting Masters.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIMIT_SALE_M_EPIC_SHOP_DESC01(String value0) {
		return new SM_SYSTEM_MESSAGE(1400439, value0);
	}

	/**
	 * %0 is selling materials to create the items of Crafting Masters.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIMIT_SALE_M_EPIC_SHOP_DESC02(String value0) {
		return new SM_SYSTEM_MESSAGE(1400440, value0);
	}

	/**
	 * %0 is selling special materials for Master Crafting.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIMIT_SALE_EPIC_SHOP_MATERIAL_DESC01(String value0) {
		return new SM_SYSTEM_MESSAGE(1400441, value0);
	}

	/**
	 * Devoted Anurati has appeared in the Great Chapel.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTP_FANATIC_DrakanNamed_SpawnMSG() {
		return new SM_SYSTEM_MESSAGE(1400442);
	}

	/**
	 * Malicious Obscura exhausts the HP of nearby enemies!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Normal_Stalker_DrainHealth() {
		return new SM_SYSTEM_MESSAGE(1400443);
	}

	/**
	 * Misguiding Obscura crouches!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Normal_Stalker_Sanctuary() {
		return new SM_SYSTEM_MESSAGE(1400444);
	}

	/**
	 * Grave Slime is splitting in two!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Normal_Slime_Isolation() {
		return new SM_SYSTEM_MESSAGE(1400445);
	}

	/**
	 * Thurzon the Undying stops its assault and begins reviving.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_BoneDrake_Sanctuary() {
		return new SM_SYSTEM_MESSAGE(1400446);
	}

	/**
	 * Your Apply For Group List request was deleted because you have joined a Group or Alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_MATCH_SEEK_POST_DELETED_ENTERED_PARTY() {
		return new SM_SYSTEM_MESSAGE(1400447);
	}

	/**
	 * You used %1 %0s.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_USE_ITEM_MULTI(String value1, String value0s) {
		return new SM_SYSTEM_MESSAGE(1400448, value1, value0s);
	}

	/**
	 * You are in normal state.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FATIGUE_INFO_0_LEVEL() {
		return new SM_SYSTEM_MESSAGE(1400449);
	}

	/**
	 * A Level 1 Fatigue Penalty has been applied because you have played too long. Please log out and take a break.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FATIGUE_INFO_1_LEVEL() {
		return new SM_SYSTEM_MESSAGE(1400450);
	}

	/**
	 * A Level 2 Fatigue Penalty has been applied because you have played too long. Please log out and take a break.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FATIGUE_INFO_2_LEVEL() {
		return new SM_SYSTEM_MESSAGE(1400451);
	}

	/**
	 * You have opened the %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNCOMPRESS_COMPRESSED_ITEM_SUCCEEDED(String num0) {
		return new SM_SYSTEM_MESSAGE(1400452, num0);
	}

	/**
	 * You have stopped opening the %0 bundle.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNCOMPRESS_COMPRESSED_ITEM_CANCELED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400453, value0);
	}

	/**
	 * The Divine Artifact has been activated!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_ArchPriest_Artifact_Light() {
		return new SM_SYSTEM_MESSAGE(1400454);
	}

	/**
	 * The Magic Artifact has been activated!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_ArchPriest_Artifact_Dark() {
		return new SM_SYSTEM_MESSAGE(1400455);
	}

	/**
	 * Isbariya taps into his power to cause a massive explosion!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_ArchPriest_Artifact_LightBoom() {
		return new SM_SYSTEM_MESSAGE(1400456);
	}

	/**
	 * Isbariya releases his magical power!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_ArchPriest_Artifact_DarkBoom() {
		return new SM_SYSTEM_MESSAGE(1400457);
	}

	/**
	 * %0 has given up following because the distance between you is too great.
	 */
	public static SM_SYSTEM_MESSAGE STR_MERCENARY_FOLLOWING_CANCELED_BY_TOO_DISTANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400458, value0);
	}

	/**
	 * Isbariya the Resolute has boosted his attack power!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_ArchPriest_2phase() {
		return new SM_SYSTEM_MESSAGE(1400459);
	}

	/**
	 * Isbariya the Resolute has boosted his recovery power!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_ArchPriest_3phase() {
		return new SM_SYSTEM_MESSAGE(1400460);
	}

	/**
	 * Isbariya the Resolute unleashes an intense power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_ArchPriest_4phase() {
		return new SM_SYSTEM_MESSAGE(1400461);
	}

	/**
	 * Isbariya the Resolute has summoned a Bodyguard Commissioned Officer.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_ArchPriest_5phase() {
		return new SM_SYSTEM_MESSAGE(1400462);
	}

	/**
	 * Isbariya the Resolute inflicts a devastating curse.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_Boss_ArchPriest_6phase() {
		return new SM_SYSTEM_MESSAGE(1400463);
	}

	/**
	 * A Level %0 Fatigue Penalty has been applied because you have played too long. Monitor your fatigue level with the '/Fatigue' command.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FATIGUE_UPGRADE_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400464, value0);
	}

	/**
	 * The Warrior Monument has been destroyed. Ahbana the Wicked is on alert.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_NmdSpecter_Spawn() {
		return new SM_SYSTEM_MESSAGE(1400465);
	}

	/**
	 * Macunbello's power is weakening.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_NmdLich_weakness1() {
		return new SM_SYSTEM_MESSAGE(1400466);
	}

	/**
	 * Macunbello's power has weakened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_NmdLich_weakness2() {
		return new SM_SYSTEM_MESSAGE(1400467);
	}

	/**
	 * Macunbello has been crippled.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_NmdLich_weakness3() {
		return new SM_SYSTEM_MESSAGE(1400468);
	}

	/**
	 * Macunbello has left his sanctuary.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_NmdLich_Leave() {
		return new SM_SYSTEM_MESSAGE(1400469);
	}

	/**
	 * Ahbana the Wicked has appeared in the Watcher's Nexus.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_NmdSpecter_Start() {
		return new SM_SYSTEM_MESSAGE(1400470);
	}

	/**
	 * Hiding Lupukin has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_NmdShulack_Rufukin() {
		return new SM_SYSTEM_MESSAGE(1400471);
	}

	/**
	 * The Aetheric Field Activation Stone is under attack!
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_SHIELD_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1400472);
	}

	/**
	 * %1 of %0 destroyed the castle gate.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_SHIELD_BROKEN(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400473, value1, value0);
	}

	/**
	 * The Balaur have destroyed the Aetheric Field Activation Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIELDABYSS_DRAGON_SHIELD_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1400474);
	}

	/**
	 * The cocoons are wriggling--something's inside!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDELIM_COCOON_INFO() {
		return new SM_SYSTEM_MESSAGE(1400475);
	}

	/**
	 * Cracks appear on the surface of Queen Mosqua's egg.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDELIM_EGG_BREAK() {
		return new SM_SYSTEM_MESSAGE(1400476);
	}

	/**
	 * An ascending air current is rising from the spot where the egg was. You can fly vertically up by spreading your wings and riding the current.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDELIM_WIND_INFO() {
		return new SM_SYSTEM_MESSAGE(1400477);
	}

	/**
	 * You are unable to obtain items at the current time.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_RESTRICTED_STATE_CANT_GET_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400478);
	}

	/**
	 * You are unable to obtain items at the current time, and cannot participate in the roll.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_RESTRICTED_STATE_CANT_THROW_DICE() {
		return new SM_SYSTEM_MESSAGE(1400479);
	}

	/**
	 * The Seal Protector has fallen. The Rift Orb shines while the seal weakens.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCatacombs_BigOrb_Spawn() {
		return new SM_SYSTEM_MESSAGE(1400480);
	}

	/**
	 * %1 remains before the usage time for %0 expires.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CASH_ITEM_TIME_LEFT(String value0, int minutes) {
		return new SM_SYSTEM_MESSAGE(1400481, value0, minutes + "min");
	}

	/**
	 * %1 remains on the appearance change time of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SKIN_CHANGE_TIME_LEFT(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400482, value1, value0);
	}

	/**
	 * You can only acquire daily quests once per day.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_CAN_NOT_RECEIVE_QUEST_TWICE_A_DAY() {
		return new SM_SYSTEM_MESSAGE(1400483);
	}

	/**
	 * You are too far from %0 to issue an order.
	 */
	public static SM_SYSTEM_MESSAGE STR_MERCENARY_CANT_ORDER_BY_TOO_DISTANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400484, value0);
	}

	/**
	 * Water erupts from the geyser.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_JUMP_TRIGGER_ON_INFO() {
		return new SM_SYSTEM_MESSAGE(1400485);
	}

	/**
	 * A gust of air bursts forth.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WINDBOX_TRIGGER_ON_INFO() {
		return new SM_SYSTEM_MESSAGE(1400486);
	}

	/**
	 * Sematariux has cast defensive magic. You will be removed from Sematariux's Hideout in 2 hours.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_120M() {
		return new SM_SYSTEM_MESSAGE(1400487);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 1 hour and 30 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_90M() {
		return new SM_SYSTEM_MESSAGE(1400488);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 1 hour.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_60M() {
		return new SM_SYSTEM_MESSAGE(1400489);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 30 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_30M() {
		return new SM_SYSTEM_MESSAGE(1400490);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 15 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_15M() {
		return new SM_SYSTEM_MESSAGE(1400491);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 10 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_10M() {
		return new SM_SYSTEM_MESSAGE(1400492);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 5 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_5M() {
		return new SM_SYSTEM_MESSAGE(1400493);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 3 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_3M() {
		return new SM_SYSTEM_MESSAGE(1400494);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 2 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_2M() {
		return new SM_SYSTEM_MESSAGE(1400495);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 1 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_1M() {
		return new SM_SYSTEM_MESSAGE(1400496);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 30 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_30S() {
		return new SM_SYSTEM_MESSAGE(1400497);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 15 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_15S() {
		return new SM_SYSTEM_MESSAGE(1400498);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 10 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_10S() {
		return new SM_SYSTEM_MESSAGE(1400499);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 5 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_5S() {
		return new SM_SYSTEM_MESSAGE(1400500);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 4 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_4S() {
		return new SM_SYSTEM_MESSAGE(1400501);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 3 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_3S() {
		return new SM_SYSTEM_MESSAGE(1400502);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 2 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_2S() {
		return new SM_SYSTEM_MESSAGE(1400503);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 1 second.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_1S() {
		return new SM_SYSTEM_MESSAGE(1400504);
	}

	/**
	 * You have been forcibly removed from Sematariux's Hideout by Sematariux's defensive magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_OUT_TIMER_0S() {
		return new SM_SYSTEM_MESSAGE(1400505);
	}

	/**
	 * Padmarashka has cast defensive magic. You will be removed from Padmarashka's Cave in 2 hours.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_120M() {
		return new SM_SYSTEM_MESSAGE(1400506);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 1 hour and 30 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_90M() {
		return new SM_SYSTEM_MESSAGE(1400507);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 1 hour.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_60M() {
		return new SM_SYSTEM_MESSAGE(1400508);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 30 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_30M() {
		return new SM_SYSTEM_MESSAGE(1400509);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 15 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_15M() {
		return new SM_SYSTEM_MESSAGE(1400510);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 10 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_10M() {
		return new SM_SYSTEM_MESSAGE(1400511);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 5 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_5M() {
		return new SM_SYSTEM_MESSAGE(1400512);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 3 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_3M() {
		return new SM_SYSTEM_MESSAGE(1400513);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 2 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_2M() {
		return new SM_SYSTEM_MESSAGE(1400514);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 1 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_1M() {
		return new SM_SYSTEM_MESSAGE(1400515);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 30 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_30S() {
		return new SM_SYSTEM_MESSAGE(1400516);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 15 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_15S() {
		return new SM_SYSTEM_MESSAGE(1400517);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 10 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_10S() {
		return new SM_SYSTEM_MESSAGE(1400518);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 5 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_5S() {
		return new SM_SYSTEM_MESSAGE(1400519);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 4 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_4S() {
		return new SM_SYSTEM_MESSAGE(1400520);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 3 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_3S() {
		return new SM_SYSTEM_MESSAGE(1400521);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 2 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_2S() {
		return new SM_SYSTEM_MESSAGE(1400522);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 1 second.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_1S() {
		return new SM_SYSTEM_MESSAGE(1400523);
	}

	/**
	 * You have been forcibly removed from Padmarashka's Cave by Padmarashka's defensive magic.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_OUT_TIMER_0S() {
		return new SM_SYSTEM_MESSAGE(1400524);
	}

	/**
	 * Sematariux is about to lay eggs.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_LAY_EGG() {
		return new SM_SYSTEM_MESSAGE(1400525);
	}

	/**
	 * Padmarashka is about to lay eggs.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_LAY_EGG() {
		return new SM_SYSTEM_MESSAGE(1400526);
	}

	/**
	 * Lowly Daevas such as you would dare?
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_START_1() {
		return new SM_SYSTEM_MESSAGE(1400527);
	}

	/**
	 * You have leapt into certain death!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_START_1() {
		return new SM_SYSTEM_MESSAGE(1400528);
	}

	/**
	 * Kaisinel's Agent Veille has engaged in battle to defend Inggison.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_GODELITE_START_1() {
		return new SM_SYSTEM_MESSAGE(1400529);
	}

	/**
	 * Kaisinel's Agent Veille has engaged in battle to defend Inggison.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_GODELITE_START_2() {
		return new SM_SYSTEM_MESSAGE(1400530);
	}

	/**
	 * Kaisinel's Agent Veille has engaged in battle to defend Inggison.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_GODELITE_START_3() {
		return new SM_SYSTEM_MESSAGE(1400531);
	}

	/**
	 * Kaisinel's Agent Veille has engaged in battle to defend Inggison.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_GODELITE_START_4() {
		return new SM_SYSTEM_MESSAGE(1400532);
	}

	/**
	 * Marchutan's Agent Mastarius has engaged in battle to defend Gelkmaros.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_GODELITE_START_1() {
		return new SM_SYSTEM_MESSAGE(1400533);
	}

	/**
	 * Marchutan's Agent Mastarius has engaged in battle to defend Gelkmaros.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_GODELITE_START_2() {
		return new SM_SYSTEM_MESSAGE(1400534);
	}

	/**
	 * Marchutan's Agent Mastarius has engaged in battle to defend Gelkmaros.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_GODELITE_START_3() {
		return new SM_SYSTEM_MESSAGE(1400535);
	}

	/**
	 * Marchutan's Agent Mastarius has engaged in battle to defend Gelkmaros.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_GODELITE_START_4() {
		return new SM_SYSTEM_MESSAGE(1400536);
	}

	/**
	 * I grieve for I couldn't become a dragon!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_START_2() {
		return new SM_SYSTEM_MESSAGE(1400537);
	}

	/**
	 * I never cared much for the responsibility of breeding!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_START_3() {
		return new SM_SYSTEM_MESSAGE(1400538);
	}

	/**
	 * I laugh at you pathetic Daevas who think you can defeat me!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_START_4() {
		return new SM_SYSTEM_MESSAGE(1400539);
	}

	/**
	 * The responsibility of breeding is my will!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_START_2() {
		return new SM_SYSTEM_MESSAGE(1400540);
	}

	/**
	 * I must protect the eggs!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_START_3() {
		return new SM_SYSTEM_MESSAGE(1400541);
	}

	/**
	 * You will never see the light of day again!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_START_4() {
		return new SM_SYSTEM_MESSAGE(1400542);
	}

	/**
	 * %0's buddy list is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_BUDDYS_LIST_FULL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400543, value0);
	}

	/**
	 * You must be in an Alliance to access this area.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ENTER_ONLY_FORCE_DON() {
		return new SM_SYSTEM_MESSAGE(1400544);
	}

	/**
	 * You can advance to level 10 only after you have completed the class change quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_LEVEL_LIMIT_QUEST_NOT_FINISHED1() {
		return new SM_SYSTEM_MESSAGE(1400545);
	}

	/**
	 * %0 is located at %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_SUBZONE_FOUND_DEV(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400546, value0, value1);
	}

	/**
	 * Cannot find the path to %0. %0 is located at %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_TOO_FAR_FROM_SUBZONE_DEV(String value0, String value2, String value1) {
		return new SM_SYSTEM_MESSAGE(1400547, value0, value2, value1);
	}

	/**
	 * %0 is located at %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_NPC_FOUND_DEV(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400548, value0, value1);
	}

	/**
	 * Cannot find the path to %0. %0 is located at %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_TOO_FAR_FROM_NPC_DEV(String value0, String value2, String value1) {
		return new SM_SYSTEM_MESSAGE(1400549, value0, value2, value1);
	}

	/**
	 * %0 is located at %2 in %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_NPC_FOUND_IN_OTHER_WORLD_DEV(String value0, String value2, String value1) {
		return new SM_SYSTEM_MESSAGE(1400550, value0, value2, value1);
	}

	/**
	 * Cannot perform path finding--the cooldown timer has not expired.
	 */
	public static SM_SYSTEM_MESSAGE STR_FIND_POS_CANT_USE_UNTIL_DELAYTIME() {
		return new SM_SYSTEM_MESSAGE(1400551);
	}

	/**
	 * There is 1 minute left to trade with %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ALARM_REMAIN_ONE_MINUTE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400552, value0);
	}

	/**
	 * %0's temporary trade time has expired. %0 can no longer be traded.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_END_OF_EXCHANGE_TIME(String value0) {
		return new SM_SYSTEM_MESSAGE(1400553, value0);
	}

	/**
	 * %0 is not a target you can trade %1 with.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WRONG_EXCHANGE_TARGET(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400554, value0, value1);
	}

	/**
	 * %0's temporary trade time has expired and can no longer be traded.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EXCHANGE_TIME_OVER(String value0) {
		return new SM_SYSTEM_MESSAGE(1400555, value0);
	}

	/**
	 * %0 has acquired %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMPLETE_EXCHANGE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400556, value0, value1);
	}

	/**
	 * You do not have enough %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOT_ENOUGH_TRADE_MONEY(String value0) {
		return new SM_SYSTEM_MESSAGE(1400557, value0);
	}

	/**
	 * You have invited %0's alliance to the Alliance League. %0's alliance has a total of %1 members.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_INVITE_HIM(String value0, int allianceSize) {
		return new SM_SYSTEM_MESSAGE(1400558, value0, allianceSize);
	}

	/**
	 * The alliance captain of the alliance %0 belongs to is %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_INVITE_HIS_LEADER(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400559, value0, value1);
	}

	/**
	 * Your alliance has joined %0's Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_ENTER_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1400560, value0);
	}

	/**
	 * %0's alliance has joined the Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_ENTER_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400561, value0);
	}

	/**
	 * You have declined %0's invitation to join the Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_REJECT_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1400562, value0);
	}

	/**
	 * %0's alliance has declined your invitation to join the Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_REJECT_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400563, value0);
	}

	/**
	 * %0 is already a member of another Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_ALREADY_OTHER_UNION(String value0) {
		return new SM_SYSTEM_MESSAGE(1400564, value0);
	}

	/**
	 * You cannot invite anymore as the Alliance League is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CANT_ADD_NEW_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1400565);
	}

	/**
	 * You have don't have permission to invite people to the League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_ONLY_LEADER_CAN_INVITE() {
		return new SM_SYSTEM_MESSAGE(1400566);
	}

	/**
	 * Currently, %0 cannot accept your invitation to join the alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CANT_INVITE_WHEN_HE_IS_ASKED_QUESTION(String value0) {
		return new SM_SYSTEM_MESSAGE(1400567, value0);
	}

	/**
	 * You cannot invite your own alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CANT_INVITE_SELF() {
		return new SM_SYSTEM_MESSAGE(1400568);
	}

	/**
	 * The player you invited to the Alliance League is currently offline.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_OFFLINE_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1400569);
	}

	/**
	 * You cannot use the Alliance League invitation function while you are dead.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CANT_INVITE_WHEN_DEAD() {
		return new SM_SYSTEM_MESSAGE(1400570);
	}

	/**
	 * You have left the Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_LEAVE_ME() {
		return new SM_SYSTEM_MESSAGE(1400571);
	}

	/**
	 * %0's alliance has left the Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_LEAVE_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400572, value0);
	}

	/**
	 * Only an alliance captain can leave the Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_ONLY_LEADER_CAN_LEAVE() {
		return new SM_SYSTEM_MESSAGE(1400573);
	}

	/**
	 * You have expelled %0's alliance from the Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_BAN_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400574, value0);
	}

	/**
	 * %0 has expelled %1's alliance from the Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_BAN_HIS_LEADER(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400575, value0, value1);
	}

	/**
	 * %0 has expelled your alliance from the Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_BAN_ME(String value0) {
		return new SM_SYSTEM_MESSAGE(1400576, value0);
	}

	/**
	 * Only the league leader can kick out an alliance from the Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_ONLY_LEADER_CAN_BAN() {
		return new SM_SYSTEM_MESSAGE(1400577);
	}

	/**
	 * You cannot remove your own Alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CANT_BAN_SELF() {
		return new SM_SYSTEM_MESSAGE(1400578);
	}

	/**
	 * The Alliance League has disbanded due to an insufficient number of alliances.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_DISPERSED() {
		return new SM_SYSTEM_MESSAGE(1400579);
	}

	/**
	 * You transferred the league leadership to %0. From now on, %0 is the league leader.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CHANGE_LEADER(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400580, value0, value1);
	}

	/**
	 * %0 entrusted %1 with the league leadership authority. From now on, %1 is the league leader.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_HE_IS_NEW_LEADER(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1400581, value0, value1, value2);
	}

	/**
	 * You are now the League leader.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_YOU_BECOME_NEW_LEADER() {
		return new SM_SYSTEM_MESSAGE(1400582);
	}

	/**
	 * Only the league leader can transfer the league leader authority to an alliance captain.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_ONLY_LEADER_CAN_CHANGE_LEADER() {
		return new SM_SYSTEM_MESSAGE(1400583);
	}

	/**
	 * You cannot transfer the league leadership to someone who isn't an alliance captain.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_ONLY_CAN_CHANGE_LEADER_TO_FORCE_LEADER() {
		return new SM_SYSTEM_MESSAGE(1400584);
	}

	/**
	 * You cannot transfer leadership to yourself.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CANT_CHANGE_LEADER_SELF() {
		return new SM_SYSTEM_MESSAGE(1400585);
	}

	/**
	 * You cannot transfer the leadership to a player outside your League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CANT_CHANGE_LEADER_OTHER_UNION() {
		return new SM_SYSTEM_MESSAGE(1400586);
	}

	/**
	 * You are now the League leader.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_YOU_BECOME_NEW_LEADER_TIMEOUT() {
		return new SM_SYSTEM_MESSAGE(1400587);
	}

	/**
	 * %0 was automatically entrusted with the league leader authority.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CHANGE_LEADER_TIMEOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400588, value0);
	}

	/**
	 * The alliance number has been changed to %num0.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CHANGE_FORCE_NUMBER_ME(int num0) {
		return new SM_SYSTEM_MESSAGE(1400589, num0);
	}

	/**
	 * The alliance number of %0 has been changed to %num0.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CHANGE_FORCE_NUMBER_HIM(String value0, int num0) {
		return new SM_SYSTEM_MESSAGE(1400590, value0, num0);
	}

	/**
	 * You cannot change it to an unclaimed alliance number.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CANT_CHANGE_FORCE_NUMBER() {
		return new SM_SYSTEM_MESSAGE(1400591);
	}

	/**
	 * You cannot change the league leader's alliance number.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_CANT_CHANGE_LEADER_NUMBER() {
		return new SM_SYSTEM_MESSAGE(1400592);
	}

	/**
	 * Only the League leader can change the Alliance number.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_ONLY_LEADER_CAN_CHANGE_FORCE_NUMBER() {
		return new SM_SYSTEM_MESSAGE(1400593);
	}

	/**
	 * Your must be at least level %0 to be promoted.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_LEVELLOW_RANK_UP(String value0) {
		return new SM_SYSTEM_MESSAGE(1400594, value0);
	}

	/**
	 * The bulkhead has been activated and the passage between the First Armory and Gravity Control has been sealed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SHIELD_A_SPAWN_IDAB1_Dreadgion01() {
		return new SM_SYSTEM_MESSAGE(1400595);
	}

	/**
	 * The bulkhead has been activated and the passage between the Second Armory and Gravity Control has been sealed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SHIELD_B_SPAWN_IDAB1_Dreadgion01() {
		return new SM_SYSTEM_MESSAGE(1400596);
	}

	/**
	 * You can use Screen Capture once every %0 seconds. Time Remaining: %1 seconds
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_REMAIN_PRINT_SCREEN_COOLTIME(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400600, value0, value1);
	}

	/**
	 * You are not in an Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_YOU_ARE_NOT_UNION_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1400601);
	}

	/**
	 * The alliance captain is not part of the Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_ONLY_CAN_BAN_FORCE_LEADER() {
		return new SM_SYSTEM_MESSAGE(1400602);
	}

	/**
	 * The selected target is already a member of another force league.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_ALREADY_MY_UNION() {
		return new SM_SYSTEM_MESSAGE(1400603);
	}

	/**
	 * The bulkhead has been activated and the passage between the First Armory and Gravity Control has been sealed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SHIELD_A_SPAWN_IDDreadgion02() {
		return new SM_SYSTEM_MESSAGE(1400604);
	}

	/**
	 * The bulkhead has been activated and the passage between the Second Armory and Gravity Control has been sealed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SHIELD_B_SPAWN_IDDreadgion02() {
		return new SM_SYSTEM_MESSAGE(1400605);
	}

	/**
	 * Omega summons a creature.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_RaidShowTime_Phase1() {
		return new SM_SYSTEM_MESSAGE(1400606);
	}

	/**
	 * Omega summons a powerful creature.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_RaidShowTime_Phase2() {
		return new SM_SYSTEM_MESSAGE(1400607);
	}

	/**
	 * Omega summons a healing creature.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_RaidShowTime_Phase3() {
		return new SM_SYSTEM_MESSAGE(1400608);
	}

	/**
	 * Omega summons a creature that creates barriers.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_RaidShowTime_Phase4() {
		return new SM_SYSTEM_MESSAGE(1400609);
	}

	/**
	 * Attack of poison and paralysis begins.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_RaidShowTime_Phase1() {
		return new SM_SYSTEM_MESSAGE(1400610);
	}

	/**
	 * Attack that restricts physical and magical assaults begins.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_RaidShowTime_Phase2() {
		return new SM_SYSTEM_MESSAGE(1400611);
	}

	/**
	 * Ragnarok's acidic fluid appears.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_RaidShowTime_Phase3() {
		return new SM_SYSTEM_MESSAGE(1400612);
	}

	/**
	 * Powerful continuous attacks and reflections begin.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_RaidShowTime_Phase4() {
		return new SM_SYSTEM_MESSAGE(1400613);
	}

	/**
	 * You cannot invite someone who doesn't belong to an alliance to the league.
	 */
	public static SM_SYSTEM_MESSAGE STR_UNION_ONLY_INVITE_FORCE_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1400614);
	}

	/**
	 * %0 Pats its tummy and indicates that its full.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_FEED_PET_FULL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400615, value0);
	}

	/**
	 * %0 eats %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_FEED_START_EATING(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400616, value0, value1);
	}

	/**
	 * Stop feeding %1 to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_FEED_STOP_EATING(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400617, value1, value0);
	}

	/**
	 * %0 spits out %1 and makes a face.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_FEED_FOOD_NOT_LOVEFLAVOR(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400618, value0, value1);
	}

	/**
	 * %0 is grateful and gives you a %1 as a present (Times remaining: %2/%3).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_FEED_CASH_REWARD(String value0, String value1, String value2, String value3) {
		return new SM_SYSTEM_MESSAGE(1400619, value0, value1, value2, value3);
	}

	/**
	 * %0 has enjoyed eating %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_FEED_ATE_FOOD_1(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400620, value0, value1);
	}

	/**
	 * %0 has enjoyed eating %1 and looks pleased.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_FEED_ATE_FOOD_2(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400621, value0, value1);
	}

	/**
	 * %0 has enjoyed eating %1 and looks happy.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_FEED_ATE_FOOD_3(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400622, value0, value1);
	}

	/**
	 * %0 has finished eating %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_FEED_ATE_ALL_FOOD(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400623, value0, value1);
	}

	/**
	 * %0 is thankful and gives you %2.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_FEED_COMMON_REWARD(String value0, String value2) {
		return new SM_SYSTEM_MESSAGE(1400624, value0, value2);
	}

	/**
	 * Only available while in an Alliance League.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_SPLIT_UNION() {
		return new SM_SYSTEM_MESSAGE(1400625);
	}

	/**
	 * The Alliance League's looting method has changed to %0, %1 %2.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_UNION_LOOTING_CHANGED_RULE(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1400626, value0, value1, value2);
	}

	/**
	 * An infiltration passage into the Chantra Dredgion has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_OPEN_IDDREADGION_02() {
		return new SM_SYSTEM_MESSAGE(1400628);
	}

	/**
	 * An infiltration passage into the Terath Dredgion has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_OPEN_IDDREADGION_03() {
		return new SM_SYSTEM_MESSAGE(1401398);
	}

	/**
	 * The opposition has abandoned the Chantra Dredgion infiltration mission. You will leave the Chantra Dredgion when the mission ends in
	 * %DURATIONTIME0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ALARM_COLD_GAME_IDDREADGION_02(String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400629, durationtime0);
	}

	/**
	 * You can save one of the two Reians imprisoned in the cocoon.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDELIM_Cocoon_Yell() {
		return new SM_SYSTEM_MESSAGE(1400630);
	}

	/**
	 * Supplies Storage teleport device has been created at Escape Hatch.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WAREHOUSETELEPORTER_CREATED_IDDREADGION_02_01() {
		return new SM_SYSTEM_MESSAGE(1400631);
	}

	/**
	 * Captain Zanata has appeared in the Captain's Cabin.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BOSS_SPAWN_IDDREADGION_02() {
		return new SM_SYSTEM_MESSAGE(1400632);
	}

	/**
	 * Officer Kamanya has appeared in Gravity Control.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BONUSNPC_SPAWN_IDDREADGION_02() {
		return new SM_SYSTEM_MESSAGE(1400633);
	}

	/**
	 * A treasure chest will appear if you defeat Ebonsoul within one minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDAbRe_Core_NmdC_Light_Die() {
		return new SM_SYSTEM_MESSAGE(1400634);
	}

	/**
	 * A treasure chest will appear if you defeat Rukril within one minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDAbRe_Core_NmdC_Dark_Die() {
		return new SM_SYSTEM_MESSAGE(1400635);
	}

	/**
	 * A treasure chest has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDAbRe_Core_NmdC_BoxSpawn() {
		return new SM_SYSTEM_MESSAGE(1400636);
	}

	/**
	 * Yamennes opens the Spawn Gate and begins to summon his minions.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDAbRe_Core_NmdD_SummonStart() {
		return new SM_SYSTEM_MESSAGE(1400637);
	}

	/**
	 * There is no space in the Pet Pouch.
	 */
	public static SM_SYSTEM_MESSAGE STR_WAREHOUSE_TOO_MANY_ITEMS_TOYPET_WAREHOUSE() {
		return new SM_SYSTEM_MESSAGE(1400638);
	}

	/**
	 * Your Favorites list is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBINE_FAVORIT_LIST_FULL() {
		return new SM_SYSTEM_MESSAGE(1400639);
	}

	/**
	 * You have entered %WORLDNAME0. Your allies are barred from joining you.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_DUNGEON_OPENED_FOR_SELF(int worldId) {
		return new SM_SYSTEM_MESSAGE(1400640, worldId);
	}

	/**
	 * Supplies Storage teleport device has been created at the Secondary Escape Hatch.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WAREHOUSETELEPORTER_CREATED_IDDREADGION_02_02() {
		return new SM_SYSTEM_MESSAGE(1400641);
	}

	/**
	 * This is not a usable pet egg.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_NOT_PET_COUPON() {
		return new SM_SYSTEM_MESSAGE(1400642);
	}

	/**
	 * That name is invalid. Please try another..
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_NOT_AVALIABE_NAME() {
		return new SM_SYSTEM_MESSAGE(1400643);
	}

	/**
	 * You abandoned %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_ABANDON_PET_COMPLETE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400644, value0);
	}

	/**
	 * You summoned %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_SUMMONED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400645, value0);
	}

	/**
	 * %0 has been dismissed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_UNSUMMONED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400646, value0);
	}

	/**
	 * You cannot put this item in the Pet Pouch.
	 */
	public static SM_SYSTEM_MESSAGE STR_WAREHOUSE_CANT_DEPOSIT_ITEM_TOYPET_WAREHOUSE() {
		return new SM_SYSTEM_MESSAGE(1400647);
	}

	/**
	 * You cannot combine equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMPOUND_ERROR_EQUIPED_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400648);
	}

	/**
	 * Life energy begins to course through your body.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BARD_BUFF_LIFE() {
		return new SM_SYSTEM_MESSAGE(1400649);
	}

	/**
	 * You feel all your muscles becoming harder.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BARD_BUFF_PROTECTION() {
		return new SM_SYSTEM_MESSAGE(1400650);
	}

	/**
	 * You already have the selected pet.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_ALREADY_TAMED_PET() {
		return new SM_SYSTEM_MESSAGE(1400651);
	}

	/**
	 * Captain's Cabin teleport device has been created at the end of the Central Passage.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BOSSTELEPORTER_CREATED_IDDREDAGION_02() {
		return new SM_SYSTEM_MESSAGE(1400652);
	}

	/**
	 * There is an object of great power nearby.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCROMEDE_SKILL() {
		return new SM_SYSTEM_MESSAGE(1400653);
	}

	/**
	 * You can use a Silver Blade Rotan to destroy the rock door leading to the Temple Vault.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCROMEDE_DOOR() {
		return new SM_SYSTEM_MESSAGE(1400654);
	}

	/**
	 * You have acquired the 'Cool Water' effect from the garden fountain.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCROMEDE_BUFF_01() {
		return new SM_SYSTEM_MESSAGE(1400655);
	}

	/**
	 * You have acquired the 'Sweet Fruit' effect from the fruit basket.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCROMEDE_BUFF_02() {
		return new SM_SYSTEM_MESSAGE(1400656);
	}

	/**
	 * You have acquired the 'Tasty Meat' effect from the Porgus Barbecue.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCROMEDE_BUFF_03() {
		return new SM_SYSTEM_MESSAGE(1400657);
	}

	/**
	 * You have acquired the 'Prophet's Blessing' effect from the Prophet's Tower.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCROMEDE_BUFF_04() {
		return new SM_SYSTEM_MESSAGE(1400658);
	}

	/**
	 * You sense a movement in Taloc's Roots. You won't be able to meet him unless you hurry.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDELIM_HYAS_SPAWN_INFO() {
		return new SM_SYSTEM_MESSAGE(1400659);
	}

	/**
	 * Smoke is being discharged. Exposure to smoke will destroy Kinquid's Barrier.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDELIM_GAS_INFO() {
		return new SM_SYSTEM_MESSAGE(1400660);
	}

	/**
	 * Sematariux has died. You will be removed from Sematariux's Hideout in 30 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_30M() {
		return new SM_SYSTEM_MESSAGE(1400661);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 25 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_25M() {
		return new SM_SYSTEM_MESSAGE(1400662);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 20 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_20M() {
		return new SM_SYSTEM_MESSAGE(1400663);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 15 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_15M() {
		return new SM_SYSTEM_MESSAGE(1400664);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 10 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_10M() {
		return new SM_SYSTEM_MESSAGE(1400665);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 5 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_5M() {
		return new SM_SYSTEM_MESSAGE(1400666);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 4 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_4M() {
		return new SM_SYSTEM_MESSAGE(1400667);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 3 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_3M() {
		return new SM_SYSTEM_MESSAGE(1400668);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 2 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_2M() {
		return new SM_SYSTEM_MESSAGE(1400669);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 1 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_1M() {
		return new SM_SYSTEM_MESSAGE(1400670);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 30 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_30S() {
		return new SM_SYSTEM_MESSAGE(1400671);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 15 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_15S() {
		return new SM_SYSTEM_MESSAGE(1400672);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 10 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_10S() {
		return new SM_SYSTEM_MESSAGE(1400673);
	}

	/**
	 * You will be removed from Sematariux's Hideout in 5 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_KILLED_OUT_TIMER_5S() {
		return new SM_SYSTEM_MESSAGE(1400674);
	}

	/**
	 * Padmarashka has died. You will be removed from Padmarashka's Cave in 30 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_30M() {
		return new SM_SYSTEM_MESSAGE(1400675);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 25 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_25M() {
		return new SM_SYSTEM_MESSAGE(1400676);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 20 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_20M() {
		return new SM_SYSTEM_MESSAGE(1400677);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 15 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_15M() {
		return new SM_SYSTEM_MESSAGE(1400678);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 10 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_10M() {
		return new SM_SYSTEM_MESSAGE(1400679);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 5 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_5M() {
		return new SM_SYSTEM_MESSAGE(1400680);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 4 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_4M() {
		return new SM_SYSTEM_MESSAGE(1400681);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 3 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_3M() {
		return new SM_SYSTEM_MESSAGE(1400682);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 2 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_2M() {
		return new SM_SYSTEM_MESSAGE(1400683);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 1 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_1M() {
		return new SM_SYSTEM_MESSAGE(1400684);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 30 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_30S() {
		return new SM_SYSTEM_MESSAGE(1400685);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 15 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_15S() {
		return new SM_SYSTEM_MESSAGE(1400686);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 10 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_10S() {
		return new SM_SYSTEM_MESSAGE(1400687);
	}

	/**
	 * You will be removed from Padmarashka's Cave in 5 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_KILLED_OUT_TIMER_5S() {
		return new SM_SYSTEM_MESSAGE(1400688);
	}

	/**
	 * The destruction of the Huge Aether Fragment has destabilized the artifact!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDAbRe_Core_Artifact_Die_01() {
		return new SM_SYSTEM_MESSAGE(1400689);
	}

	/**
	 * The destruction of the Huge Aether Fragment has put the artifact protector on alert!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDAbRe_Core_Artifact_Die_02() {
		return new SM_SYSTEM_MESSAGE(1400690);
	}

	/**
	 * The destruction of the Huge Aether Fragment has caused abnormality on the artifact. The artifact protector is furious!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDAbRe_Core_Artifact_Die_03() {
		return new SM_SYSTEM_MESSAGE(1400691);
	}

	/**
	 * You may change a pet's name once every %DURATIONTIME1 (Time remaining: %DURATIONTIME0).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_NAME_CHANGE_DELAY(String durationtime1, String durationtime0) {
		return new SM_SYSTEM_MESSAGE(1400692, durationtime1, durationtime0);
	}

	/**
	 * The pet has been renamed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_NAME_CHANGED() {
		return new SM_SYSTEM_MESSAGE(1400693);
	}

	/**
	 * You already have a pet of the same name. Please choose another name.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_EXISTING_NAME() {
		return new SM_SYSTEM_MESSAGE(1400694);
	}

	/**
	 * You cannot feed it right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_FEED_CANT_NOW() {
		return new SM_SYSTEM_MESSAGE(1400695);
	}

	/**
	 * %0 indicates that it is not hungry.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_LIMIT_LOVE_COUNT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400696, value0);
	}

	/**
	 * You received %0: %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_PET_TAME_COMPLETE(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400697, value0, value1);
	}

	/**
	 * You cannot feed your pet %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSGBOX_TOYPET_FEED_CANT_FEED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400698, value0);
	}

	/**
	 * You are too far from your pet to feed it.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSGBOX_TOYPET_FEED_CANT_FEED_TOO_FAR() {
		return new SM_SYSTEM_MESSAGE(1400699);
	}

	/**
	 * You cannot feed your pet while moving.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSGBOX_TOYPET_FEED_CANT_FEED_WHEN_MOVING() {
		return new SM_SYSTEM_MESSAGE(1400700);
	}

	/**
	 * You have obtained an object with great power. For quick access, drag the item from your Cube to your Quickbar.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDCROMEDE_SKILL_01() {
		return new SM_SYSTEM_MESSAGE(1400701);
	}

	/**
	 * %0 cannot eat any food.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_PET_CANT_EAT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400702, value0);
	}

	/**
	 * You have obtained a new item from the selected target.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_QUEST_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400703);
	}

	/**
	 * You must destroy the enemies of Taloc. It allows you to acquire objects with great power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOTICE_LOOT_SKILL_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400704);
	}

	/**
	 * You cannot kick yourself out of the channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_CANT_BAN_SELF() {
		return new SM_SYSTEM_MESSAGE(1400705);
	}

	/**
	 * You cannot kick yourself out of the channel.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANT_BAN_SELF() {
		return new SM_SYSTEM_MESSAGE(1400706);
	}

	/**
	 * You cannot transfer leadership to yourself.
	 */
	public static SM_SYSTEM_MESSAGE STR_PARTY_CANT_CHANGE_LEADER_SELF() {
		return new SM_SYSTEM_MESSAGE(1400707);
	}

	/**
	 * You cannot transfer leadership to yourself.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANT_CHANGE_LEADER_SELF() {
		return new SM_SYSTEM_MESSAGE(1400708);
	}

	/**
	 * You cannot use it because the version of your package is too low.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NO_RIGHT_PACKAGE_VERSION() {
		return new SM_SYSTEM_MESSAGE(1400709);
	}

	/**
	 * Items stored in the surrendered pet's bag have been returned to your cube.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_RETURN_MASTER_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400710);
	}

	/**
	 * Your pet's time is up. %0 has gone.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_ABANDON_EXPIRE_TIME_COMPLETE(String value0) {
		return new SM_SYSTEM_MESSAGE(1401194, value0);
	}

	/**
	 * You must defeat the protector within the time limit to wake Padmarashka from the Protective Slumber.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_GUARDIAN_START() {
		return new SM_SYSTEM_MESSAGE(1400711);
	}

	/**
	 * Padmarashka has summoned the protector once again.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_GUARDIAN_FAIL() {
		return new SM_SYSTEM_MESSAGE(1400712);
	}

	/**
	 * Hamerun has dropped a treasure chest.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDNOVICE_HAMEROON_TREASUREBOX_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1400713);
	}

	/**
	 * You have failed to reclaim %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_CANT_RETURN_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400714, value0);
	}

	/**
	 * You have failed to reclaim %num1 %0s.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_CANT_RETURN_ITEM_MULTI(int num1, String value0s) {
		return new SM_SYSTEM_MESSAGE(1400715, num1, value0s);
	}

	/**
	 * %0 is grateful and gives you a %1 as a present.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_FEED_CASH_REWARD_CASH_UNLIMITED(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400716, value0, value1);
	}

	/**
	 * Cannot find the item to combine.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMPOUND_ITEM_NO_TARGET_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400717);
	}

	/**
	 * Cannot find the item to remove the combination from.
	 */
	public static SM_SYSTEM_MESSAGE STR_DECOMPOUND_ITEM_NO_TARGET_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400718);
	}

	/**
	 * You cannot expel the alliance captain.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANT_BANISH_LEADER() {
		return new SM_SYSTEM_MESSAGE(1400719);
	}

	/**
	 * You cannot appoint yourself as an alliance vice captain.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANT_PROMOTE_MANAGER_SELF() {
		return new SM_SYSTEM_MESSAGE(1400720);
	}

	/**
	 * You are already appointed as an alliance vice captain.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANT_PROMOTE_MANAGER_AGAIN() {
		return new SM_SYSTEM_MESSAGE(1400721);
	}

	/**
	 * You cannot demote yourself to an alliance member.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANT_DEMOTE_MANAGER_SELF() {
		return new SM_SYSTEM_MESSAGE(1400722);
	}

	/**
	 * You cannot demote an alliance member.
	 */
	public static SM_SYSTEM_MESSAGE STR_FORCE_CANT_DEMOTE_MANAGER_AGAIN() {
		return new SM_SYSTEM_MESSAGE(1400723);
	}

	/**
	 * Thunder Storm has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_SUMMON_THUNDER() {
		return new SM_SYSTEM_MESSAGE(1400724);
	}

	/**
	 * Terra Blast has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_SUMMON_EARTH() {
		return new SM_SYSTEM_MESSAGE(1400725);
	}

	/**
	 * Acid mist has covered some areas.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_SUMMON_POISON() {
		return new SM_SYSTEM_MESSAGE(1400726);
	}

	/**
	 * Sematariux has awoken from the Protective Slumber.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LF4_DRAMATA_AWAKENING() {
		return new SM_SYSTEM_MESSAGE(1400727);
	}

	/**
	 * Padmarashka has awoken from the Protective Slumber.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DF4_DRAMATA_AWAKENING() {
		return new SM_SYSTEM_MESSAGE(1400728);
	}

	/**
	 * Yamennes's threat level has been reset!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDAbRe_Core_NmdD_ResetAggro() {
		return new SM_SYSTEM_MESSAGE(1400729);
	}

	/**
	 * A summoned Lapilima is healing Yamennes!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDAbRe_Core_NmdD_Heal() {
		return new SM_SYSTEM_MESSAGE(1400730);
	}

	/**
	 * Yamennes Blindsight has appeared!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDAbRe_Core_NmdD_Wakeup() {
		return new SM_SYSTEM_MESSAGE(1400731);
	}

	/**
	 * Yamennes Painflare has appeared!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDAbRe_Core_NmdDH_Wakeup() {
		return new SM_SYSTEM_MESSAGE(1400732);
	}

	/**
	 * You cannot summon a pet in %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_CANT_SUMMON_STATE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400733, value0);
	}

	/**
	 * You are in an altered state and cannot summon a pet.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_CANT_SUMMON_ABNORMAL_STATE() {
		return new SM_SYSTEM_MESSAGE(1400734);
	}

	/**
	 * You cannot dismiss a pet in %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_CANT_UNSUMMON_STATE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400735, value0);
	}

	/**
	 * You are in an altered state and cannot dismiss a pet.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_CANT_UNSUMMON_ABNORMAL_STATE() {
		return new SM_SYSTEM_MESSAGE(1400736);
	}

	/**
	 * You must be at least level %0 to perform extraction.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_GATHERING_B_LEVEL_CHECK(int level) {
		return new SM_SYSTEM_MESSAGE(1400737, level);
	}

	/**
	 * Item couldn't be registered due to a change in the fees.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_CAN_NOT_REGISTER_ITEM_FEE_CHANGED() {
		return new SM_SYSTEM_MESSAGE(1400738);
	}

	/**
	 * The first Sphere of Destiny has been activated.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_BUFF_FIRST_OBJECT_ON_DF() {
		return new SM_SYSTEM_MESSAGE(1400739);
	}

	/**
	 * The second Sphere of Destiny has been activated. Marchutan's Agent Mastarius prepares to cast the Empyrean Lord's blessing.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_BUFF_SECOND_OBJECT_ON_DF() {
		return new SM_SYSTEM_MESSAGE(1400740);
	}

	/**
	 * You may use the Sphere of Destiny again.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_BUFF_CAN_USE_OBJECT_DF() {
		return new SM_SYSTEM_MESSAGE(1400741);
	}

	/**
	 * You need more people to activate the Sphere of Destiny.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GODELITE_BUFF_CANT_USE_OBJECT_NOT_ENOUGH_MEMBER_DF() {
		return new SM_SYSTEM_MESSAGE(1400742);
	}

	/**
	 * Such basic crafting doesn't affect your skill level, Master.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_GET_COMBINE_EXP_GRAND_MASTER() {
		return new SM_SYSTEM_MESSAGE(1400743);
	}

	/**
	 * You cannot modify equipped items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CHANGE_ITEM_SKIN_CANT_CHANGE_EQUIPED_ITEM_SKIN() {
		return new SM_SYSTEM_MESSAGE(1400744);
	}

	/**
	 * You cannot preview an item that you cannot equip.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CHANGE_ITEM_SKIN_PREVIEW_INVALID_COSMETIC() {
		return new SM_SYSTEM_MESSAGE(1400745);
	}

	/**
	 * High Elder Roamim's threat level has reset!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Underpass_Nephilim_Raid_ResetAggro() {
		return new SM_SYSTEM_MESSAGE(1400746);
	}

	/**
	 * High Elder Roamim is furious!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Underpass_Nephilim_Raid_Rage() {
		return new SM_SYSTEM_MESSAGE(1400747);
	}

	/**
	 * High Elder Roamim has summoned players.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Underpass_Nephilim_Raid_Recall() {
		return new SM_SYSTEM_MESSAGE(1400748);
	}

	/**
	 * You do not have the authority to make this decision.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PARTY_FORCE_NO_RIGHT_TO_DECIDE() {
		return new SM_SYSTEM_MESSAGE(1400749);
	}

	/**
	 * You cannot buy an item you have registered yourself.
	 */
	public static SM_SYSTEM_MESSAGE STR_VENDOR_CAN_NOT_BUY_MY_REGISTER_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400750);
	}

	/**
	 * Commander Bakarma has appeared at Beritra's Oracle.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BOSS_SPAWN_IDDF3_DRAGON() {
		return new SM_SYSTEM_MESSAGE(1400751);
	}

	/**
	 * An object of great power waits in your cube. Transform into a mighty being with Taloc's Fruit.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_KASPAFRUIT_INFO() {
		return new SM_SYSTEM_MESSAGE(1400752);
	}

	/**
	 * An object of great power waits in your cube. Launch a powerful aerial attack with Taloc's Tears.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_KASPATEAR_INFO() {
		return new SM_SYSTEM_MESSAGE(1400753);
	}

	/**
	 * An object of great power waits in Shishir's carcass. Obtain it, then register it in the skill window.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SHISHIR_INFO() {
		return new SM_SYSTEM_MESSAGE(1400754);
	}

	/**
	 * An object of great power waits in Gellmar's carcass. Obtain it, then register it in the skill window.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GELMAR_INFO() {
		return new SM_SYSTEM_MESSAGE(1400755);
	}

	/**
	 * An object of great power waits in Neith's carcass. Obtain it, then register it in the skill window.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_RAGOS_INFO() {
		return new SM_SYSTEM_MESSAGE(1400756);
	}

	/**
	 * You must kill Afrane, Saraswati, Lakshmi, and Nimbarka to make Commander Bakarma appear.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BOSS_SPAWN_IDDF3_DRAGON_1() {
		return new SM_SYSTEM_MESSAGE(1400757);
	}

	/**
	 * You must kill 3 more Adjutants to make Commander Bakarma appear.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BOSS_SPAWN_IDDF3_DRAGON_2() {
		return new SM_SYSTEM_MESSAGE(1400758);
	}

	/**
	 * You must kill 2 more Adjutants to make Commander Bakarma appear.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BOSS_SPAWN_IDDF3_DRAGON_3() {
		return new SM_SYSTEM_MESSAGE(1400759);
	}

	/**
	 * You must kill 1 more Adjutant to make Commander Bakarma appear.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BOSS_SPAWN_IDDF3_DRAGON_4() {
		return new SM_SYSTEM_MESSAGE(1400760);
	}

	/**
	 * The %0 sealed by the heat of high summer has been accumulated.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_HCOIN_01(String value0) {
		return new SM_SYSTEM_MESSAGE(1400761, value0);
	}

	/**
	 * You are now a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MENTOR_START() {
		return new SM_SYSTEM_MESSAGE(1400762);
	}

	/**
	 * %0 is now a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MENTOR_START_PARTYMSG(String value0) {
		return new SM_SYSTEM_MESSAGE(1400763, value0);
	}

	/**
	 * You are no longer a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MENTOR_END() {
		return new SM_SYSTEM_MESSAGE(1400764);
	}

	/**
	 * %0 is no longer a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MENTOR_END_PARTYMSG(String value0) {
		return new SM_SYSTEM_MESSAGE(1400765, value0);
	}

	/**
	 * You cannot enter %WORLDNAME0 with a Mentor in your group.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MENTOR_CANT_ENTER(int worldId) {
		return new SM_SYSTEM_MESSAGE(1400766, worldId);
	}

	/**
	 * You were forced to leave %WORLDNAME0 because you stopped Mentoring.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MENTOR_END_BANISH(int worldId) {
		return new SM_SYSTEM_MESSAGE(1400767, worldId);
	}

	/**
	 * %0 was forced to leave %WORLDNAME1 because he or she stopped Mentoring.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MENTOR_END_BANISH_PARTYMSG(String value0, String worldname1) {
		return new SM_SYSTEM_MESSAGE(1400768, value0, worldname1);
	}

	/**
	 * You cannot become a Mentor in %WORLDNAME0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_BE_MENTOR(int worldId) {
		return new SM_SYSTEM_MESSAGE(1400769, worldId);
	}

	/**
	 * You have left %0 because of the level limit.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_LEAVE_BY_LEVEL_LIMIT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400770, value0);
	}

	/**
	 * None of your group members meet the level requirement for %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_KILL_COUNT_BY_WRONG_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400771, value0);
	}

	/**
	 * None of your group members meet the level requirement for %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_DROP_ITEM_BY_WRONG_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400772, value0);
	}

	/**
	 * You cannot use %1 while Mentoring.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_USE_ITEM_BY_NOT_MENTOR(String value1) {
		return new SM_SYSTEM_MESSAGE(1400773, value1);
	}

	/**
	 * You cannot use %1 while Mentoring.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_USE_ITEM_BY_NOT_MENTEE(String value1) {
		return new SM_SYSTEM_MESSAGE(1400774, value1);
	}

	/**
	 * The Tainted Inina is now open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_01() {
		return new SM_SYSTEM_MESSAGE(1400775);
	}

	/**
	 * You cannot open it because there are no users of levels 10 - 19.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_02() {
		return new SM_SYSTEM_MESSAGE(1400776);
	}

	/**
	 * You cannot open that without a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_03() {
		return new SM_SYSTEM_MESSAGE(1400777);
	}

	/**
	 * Speak to the Kaidan Head Priest while disguised as a Draconute Guard to receive a Tribute Chest.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_04() {
		return new SM_SYSTEM_MESSAGE(1400778);
	}

	/**
	 * The Tribute Chest can only be opened by someone disguised as a Drakan Envoy.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_05() {
		return new SM_SYSTEM_MESSAGE(1400779);
	}

	/**
	 * The Lepharist Revolutionary hideout is now open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_06() {
		return new SM_SYSTEM_MESSAGE(1400780);
	}

	/**
	 * You cannot open it because there are no users of levels 20 - 29.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_07() {
		return new SM_SYSTEM_MESSAGE(1400781);
	}

	/**
	 * You cannot open that without a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_08() {
		return new SM_SYSTEM_MESSAGE(1400782);
	}

	/**
	 * The Asmodian hideout is now open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_09() {
		return new SM_SYSTEM_MESSAGE(1400783);
	}

	/**
	 * You cannot open it because there are no users of levels 30 - 39.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_10() {
		return new SM_SYSTEM_MESSAGE(1400784);
	}

	/**
	 * You cannot open that without a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_11() {
		return new SM_SYSTEM_MESSAGE(1400785);
	}

	/**
	 * The Ward Orb is now open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_01() {
		return new SM_SYSTEM_MESSAGE(1400786);
	}

	/**
	 * You cannot open it because there are no users of levels 10 - 19.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_02() {
		return new SM_SYSTEM_MESSAGE(1400787);
	}

	/**
	 * You cannot open that without a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_03() {
		return new SM_SYSTEM_MESSAGE(1400788);
	}

	/**
	 * Speak to the Mau High Priest while disguised as a Draconute Guard to receive a Tribute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_04() {
		return new SM_SYSTEM_MESSAGE(1400789);
	}

	/**
	 * The Tribute Chest can only be opened by someone disguised as a High Rank Drakan Envoy.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_05() {
		return new SM_SYSTEM_MESSAGE(1400790);
	}

	/**
	 * The Ward Globe is now open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_06() {
		return new SM_SYSTEM_MESSAGE(1400791);
	}

	/**
	 * You cannot open it because there are no users of levels 20 - 29.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_07() {
		return new SM_SYSTEM_MESSAGE(1400792);
	}

	/**
	 * You cannot open that without a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_08() {
		return new SM_SYSTEM_MESSAGE(1400793);
	}

	/**
	 * The Morheim Observatory Auxiliary Device is now open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_09() {
		return new SM_SYSTEM_MESSAGE(1400794);
	}

	/**
	 * You cannot open it because there are no users of levels 30 - 39.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_10() {
		return new SM_SYSTEM_MESSAGE(1400795);
	}

	/**
	 * You cannot open that without a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_11() {
		return new SM_SYSTEM_MESSAGE(1400796);
	}

	/**
	 * %0 has been sealed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_START_DONE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400797, value0);
	}

	/**
	 * Canceled sealing %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_START_CANCEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400798, value0);
	}

	/**
	 * Are you sure you want to seal it?
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_CONFIRM_START() {
		return new SM_SYSTEM_MESSAGE(1400799);
	}

	/**
	 * Are you sure you want to unseal it?
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_CONFIRM_UNSEAL() {
		return new SM_SYSTEM_MESSAGE(1400800);
	}

	/**
	 * The seal will be removed immediately.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_CONFIRM_UNSEALINSTANT() {
		return new SM_SYSTEM_MESSAGE(1400801);
	}

	/**
	 * Sealed Item
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_STATUS() {
		return new SM_SYSTEM_MESSAGE(1400802);
	}

	/**
	 * Unseal Pending
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_STATUS_UNSEALWAIT() {
		return new SM_SYSTEM_MESSAGE(1400803);
	}

	/**
	 * It takes %0 days to remove the seal completely.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_STATUS_DURATION(String value0) {
		return new SM_SYSTEM_MESSAGE(1400804, value0);
	}

	/**
	 * You can have only %0 items pending unsealing at a time.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_STATUS_UNSEALMAX(String value0) {
		return new SM_SYSTEM_MESSAGE(1400805, value0);
	}

	/**
	 * %0 is now pending unsealing. This will take 7 days.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_STATUS_UNSEALWAIT_START(String value0) {
		return new SM_SYSTEM_MESSAGE(1400806, value0);
	}

	/**
	 * %0 is now pending unsealing. This will take %DURATIONDAY1 days.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_STATUS_UNSEALPROGRESS(String value0, String durationday1) {
		return new SM_SYSTEM_MESSAGE(1400807, value0, durationday1);
	}

	/**
	 * %0 is unsealed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_STATUS_UNSEALDONE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400808, value0);
	}

	/**
	 * You cannot trade, enhance, destroy, sell, extract, or soulbind sealed items, and it takes %0 days to unseal them completely.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_START(String value0) {
		return new SM_SYSTEM_MESSAGE(1400809, value0);
	}

	/**
	 * You cannot seal an item that is already sealed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_RESEAL() {
		return new SM_SYSTEM_MESSAGE(1400810);
	}

	/**
	 * You cannot trade sealed items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_TRADE() {
		return new SM_SYSTEM_MESSAGE(1400811);
	}

	/**
	 * You cannot sell sealed items in a private store.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_SHOP() {
		return new SM_SYSTEM_MESSAGE(1400812);
	}

	/**
	 * You cannot sell sealed items at the Broker.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_AUCTION() {
		return new SM_SYSTEM_MESSAGE(1400813);
	}

	/**
	 * You cannot mail sealed items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_MAIL() {
		return new SM_SYSTEM_MESSAGE(1400814);
	}

	/**
	 * You cannot store sealed items in the account warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_ACCOUNT() {
		return new SM_SYSTEM_MESSAGE(1400815);
	}

	/**
	 * You cannot store sealed items in the legion warehouse.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_GUILD() {
		return new SM_SYSTEM_MESSAGE(1400816);
	}

	/**
	 * You cannot enhance sealed items with enchantment stones.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_UP() {
		return new SM_SYSTEM_MESSAGE(1400817);
	}

	/**
	 * You cannot enhance sealed items with manastones.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_UP_MSTONE() {
		return new SM_SYSTEM_MESSAGE(1400818);
	}

	/**
	 * You cannot enhance sealed items with godstones.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_UP_GSTONE() {
		return new SM_SYSTEM_MESSAGE(1400819);
	}

	/**
	 * You cannot modify the appearance of sealed items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_LOOKCHANGE() {
		return new SM_SYSTEM_MESSAGE(1400820);
	}

	/**
	 * You cannot combine sealed items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_MERGE() {
		return new SM_SYSTEM_MESSAGE(1400821);
	}

	/**
	 * You cannot destroy sealed items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_DESTROY() {
		return new SM_SYSTEM_MESSAGE(1400822);
	}

	/**
	 * You cannot sell sealed items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_SELL() {
		return new SM_SYSTEM_MESSAGE(1400823);
	}

	/**
	 * You cannot extract sealed items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_EXTRACT() {
		return new SM_SYSTEM_MESSAGE(1400824);
	}

	/**
	 * You cannot remove manastones from sealed items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_REMOVE() {
		return new SM_SYSTEM_MESSAGE(1400825);
	}

	/**
	 * You must unseal your items to complete that quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_QUESTFINISH() {
		return new SM_SYSTEM_MESSAGE(1400826);
	}

	/**
	 * You cannot stack sealed items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_OVERLAP() {
		return new SM_SYSTEM_MESSAGE(1400827);
	}

	/**
	 * You cannot use %0 while you are waiting for more than 3 items to be unsealed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_MAXWAIT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400828, value0);
	}

	/**
	 * Canceled unsealing %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_UNSEALCANCEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400829, value0);
	}

	/**
	 * You cannot use %0 on an item that is pending unsealing.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_ALREADYUNSEAL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400830, value0);
	}

	/**
	 * %0 is not a sealed item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_NOTSEALED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400831, value0);
	}

	/**
	 * Cancel the pending unsealing of another item, and then try again.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_TRYAGAIN() {
		return new SM_SYSTEM_MESSAGE(1400832);
	}

	/**
	 * %0 Premium Seal Breaking Scrolls have arrived. They will vanish in 60 minutes or if you log out.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_SCROLLGET(String value0) {
		return new SM_SYSTEM_MESSAGE(1400833, value0);
	}

	/**
	 * The Seal Obliterator has vanished.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_SCROLLDESTROY() {
		return new SM_SYSTEM_MESSAGE(1400834);
	}

	/**
	 * You cannot use sealed consumable items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_CANTUSE() {
		return new SM_SYSTEM_MESSAGE(1400835);
	}

	/**
	 * Promotion Item Test: You have acquired %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_ALL_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400836, value0);
	}

	/**
	 * Group Leader Loot is not available when the group leader is a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_SELECT_LEADER_LOOTING_BY_MENTOR() {
		return new SM_SYSTEM_MESSAGE(1400837);
	}

	/**
	 * You cannot be a Mentor because all the other Group Members are Mentors.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_BE_MENTOR_BY_LAST_MENTEE() {
		return new SM_SYSTEM_MESSAGE(1400838);
	}

	/**
	 * Not available when the Group Leader is a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LEADER_LOOTING_IS_UNAVAILABLE() {
		return new SM_SYSTEM_MESSAGE(1400839);
	}

	/**
	 * You have stopped Mentoring because the lowest level group member must be at least 10 levels lower than you.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_BE_MENTOR_BY_LEVEL_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1400840);
	}

	/**
	 * You have stopped Mentoring because no group members need your help.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MENTOR_PARTY_END_BY_LEAVE_ALL_MENTEE() {
		return new SM_SYSTEM_MESSAGE(1400841);
	}

	/**
	 * The Mentor group has been converted to an alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MENTOR_PARTY_END_BY_CONVERT_BY_FORCE() {
		return new SM_SYSTEM_MESSAGE(1400842);
	}

	/**
	 * You cannot join the group/alliance because your character name is invalid.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_JOIN_PARTY_FORCE_NOT_NORMAL_CHAR_NAME() {
		return new SM_SYSTEM_MESSAGE(1400843);
	}

	/**
	 * You cannot seal %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_START_FAIL1(String value0) {
		return new SM_SYSTEM_MESSAGE(1400844, value0);
	}

	/**
	 * You cannot seal %0 while it is equipped.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_START_FAIL2(String value0) {
		return new SM_SYSTEM_MESSAGE(1400845, value0);
	}

	/**
	 * You cannot seal a quest item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_START_FAIL3() {
		return new SM_SYSTEM_MESSAGE(1400846);
	}

	/**
	 * %0 cannot break the combination.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_WARNING_DICOMPOSITION(String value0) {
		return new SM_SYSTEM_MESSAGE(1400847, value0);
	}

	/**
	 * You can only Mentor a group member at least 10 levels below you.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MENTOR_CANT_START_WITHOUT_MENTOR_TARGET() {
		return new SM_SYSTEM_MESSAGE(1400848);
	}

	/**
	 * You can only be a Mentor when you're in a group.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_MENTOR_CANT_START_WHEN_NOT_IN_PARTY() {
		return new SM_SYSTEM_MESSAGE(1400849);
	}

	/**
	 * You belong to a Mentor Group. Use the [/Recruit Mentor [your text here]] command to post a message to <Recruit Mentor Group>.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_REGISTER_NORMAL_PARTY_IN_MENTOR_PARTY() {
		return new SM_SYSTEM_MESSAGE(1400850);
	}

	/**
	 * Because you belong to an Alliance, you cannot post a <Recruit Group> message. Use /RecruitAllianceMember and the <Recruit Alliance> window
	 * instead.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_REGISTER_NORMAL_PARTY_IN_FORCE() {
		return new SM_SYSTEM_MESSAGE(1400851);
	}

	/**
	 * Your posting to the Find Group window was canceled because you already belong to a Group or Alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_REGISTER_APPLY_IN_PARTY_OR_FORCE() {
		return new SM_SYSTEM_MESSAGE(1400852);
	}

	/**
	 * You cannot combine items that are temporarily tradable.
	 */
	public static SM_SYSTEM_MESSAGE STR_COMPOUND_ERROR_TEMPORARY_EXCHANGE_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400853);
	}

	/**
	 * You can receive the daily quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_QUEST_LIMIT_RESET_DAILY() {
		return new SM_SYSTEM_MESSAGE(1400854);
	}

	/**
	 * You can receive the daily quest again at %0 in the morning.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_QUEST_LIMIT_START_DAILY(int num0) {
		return new SM_SYSTEM_MESSAGE(1400855, num0);
	}

	/**
	 * You can receive the weekly quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_QUEST_LIMIT_RESET_WEEK() {
		return new SM_SYSTEM_MESSAGE(1400856);
	}

	/**
	 * You can receive the weekly quest again at %1 in the morning on %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_QUEST_LIMIT_START_WEEK(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1400857, value0, num1);
	}

	/**
	 * You cannot soul bind a sealed item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_CANT_SOUL_BIND() {
		return new SM_SYSTEM_MESSAGE(1400858);
	}

	/**
	 * %0 cannot break the combination.
	 */
	public static SM_SYSTEM_MESSAGE STR_DECOMPOUND_ERROR_NOT_DECOMPOUNDABLE_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400859, value0);
	}

	/**
	 * You cannot continue the quest with %0 equipped.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_QUEST_ERROR_UNEQUIP_QUEST_ITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400860, value0);
	}

	/**
	 * That doesn't work without a user of level 35-45.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_12() {
		return new SM_SYSTEM_MESSAGE(1400861);
	}

	/**
	 * That doesn't work without a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Li_13() {
		return new SM_SYSTEM_MESSAGE(1400862);
	}

	/**
	 * That doesn't work without a user of level 25 - 35.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_12() {
		return new SM_SYSTEM_MESSAGE(1400863);
	}

	/**
	 * That doesn't work without a Mentor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Da_13() {
		return new SM_SYSTEM_MESSAGE(1400864);
	}

	/**
	 * You check how %0 feels.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_CONDITION_CARE_01(String value0) {
		return new SM_SYSTEM_MESSAGE(1400865, value0);
	}

	/**
	 * %0 feels a little better.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_CONDITION_UP_01(String value0) {
		return new SM_SYSTEM_MESSAGE(1400866, value0);
	}

	/**
	 * %0 feels much better.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_CONDITION_UP_02(String value0) {
		return new SM_SYSTEM_MESSAGE(1400867, value0);
	}

	/**
	 * %0 feels a lot better.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_CONDITION_UP_02_01(String value0) {
		return new SM_SYSTEM_MESSAGE(1400868, value0);
	}

	/**
	 * You ask %0 to search the area.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_CONDITION_SEARCH_01(String value0) {
		return new SM_SYSTEM_MESSAGE(1400869, value0);
	}

	/**
	 * You cannot issue a command when your cube is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_PET_CONDITION_REWARD_FULL_INVEN() {
		return new SM_SYSTEM_MESSAGE(1400870);
	}

	/**
	 * %0 unearthed a buried %1 for you.
	 */
	public static SM_SYSTEM_MESSAGE STR_PET_CONDITION_REWARD_GET(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400871, value0, value1);
	}

	/**
	 * You cannot glide while you are transformed.
	 */
	public static SM_SYSTEM_MESSAGE STR_GLIDE_CANNOT_GLIDE_POLYMORPH_STATUS() {
		return new SM_SYSTEM_MESSAGE(1400872);
	}

	/**
	 * You cannot fly while you are transformed.
	 */
	public static SM_SYSTEM_MESSAGE STR_FLY_CANNOT_FLY_POLYMORPH_STATUS() {
		return new SM_SYSTEM_MESSAGE(1400873);
	}

	/**
	 * Pets cannot use this item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DOPING_PET_CANNOT_USE() {
		return new SM_SYSTEM_MESSAGE(1400874);
	}

	/**
	 * You cannot list an unusable item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DOPING_PET_MESSAGE02() {
		return new SM_SYSTEM_MESSAGE(1400875);
	}

	/**
	 * The pet will now automatically loot items on your behalf, except for items that require confirmation (such as Dice Roll items.)
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOTING_PET_MESSAGE01() {
		return new SM_SYSTEM_MESSAGE(1400876);
	}

	/**
	 * The pet can't pick up items that can be shared with other group members.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOTING_PET_MESSAGE02() {
		return new SM_SYSTEM_MESSAGE(1400877);
	}

	/**
	 * You cannot use the Pet Loot function when the group is using the Free-for-All loot setting.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOTING_PET_MESSAGE03() {
		return new SM_SYSTEM_MESSAGE(1400878);
	}

	/**
	 * Pet Auto-Buffing activated. Your pet automatically uses Buff Bag items to buff you as old buffs expire.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DOPING_PET_USE_START_MESSAGE() {
		return new SM_SYSTEM_MESSAGE(1400879);
	}

	/**
	 * Stop Pet Auto-Buffing.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DOPING_PET_USE_STOP_MESSAGE() {
		return new SM_SYSTEM_MESSAGE(1400880);
	}

	/**
	 * Deactivates the Pet Loot function.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOTING_PET_USE_STOP_MESSAGE() {
		return new SM_SYSTEM_MESSAGE(1400881);
	}

	/**
	 * The pet can only eat food.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DOPING_PET_USE_CATEGORY_FOOD() {
		return new SM_SYSTEM_MESSAGE(1400882);
	}

	/**
	 * The pet can only drink beverages.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DOPING_PET_USE_CATEGORY_DRINK() {
		return new SM_SYSTEM_MESSAGE(1400883);
	}

	/**
	 * The pet can only use scrolls.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DOPING_PET_USE_CATEGORY_SCROLL() {
		return new SM_SYSTEM_MESSAGE(1400884);
	}

	/**
	 * Your pet cannot pick up items that require your confirmation.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOTING_PET_ITEM_REMAIN() {
		return new SM_SYSTEM_MESSAGE(1400885);
	}

	/**
	 * Your pet cannot pick up items that you must share with other Group Members.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOOTING_PET_ITEM_REMAIN02() {
		return new SM_SYSTEM_MESSAGE(1400886);
	}

	/**
	 * %0 has been conditioned to level %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE_SUCCESS(String value0, int level) {
		return new SM_SYSTEM_MESSAGE(1400887, value0, level);
	}

	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE2_SUCCESS(String value0, int level) {
		return new SM_SYSTEM_MESSAGE(1401335, value0, level);
	}

	/**
	 * Magical power charging has canceled.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE2_CANCELED() {
		return new SM_SYSTEM_MESSAGE(1401339);
	}

	/*
	 * The equipped item has been magically charged.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE2_ALL_COMPLETE() {
		return new SM_SYSTEM_MESSAGE(1401340);
	}

	/*
	 * No equipped items are magically chargeable.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE2_ALL_FAIL_NO_CHARGEABLE_EQUIPMENT() {
		return new SM_SYSTEM_MESSAGE(1401343);
	}

	/**
	 * You cannot condition %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE_FAIL_NOT_CHARGEABLE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400888, value0);
	}

	/**
	 * %0 has been conditioned as much as it can be. You cannot condition it to Level %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE_FAIL_ALREADY_CHARGED(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400889, value0, value1);
	}

	/**
	 * All equipped items are already conditioned. You cannot condition them further.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE_ALL_FAIL_ALREADY_CHARGED() {
		return new SM_SYSTEM_MESSAGE(1400890);
	}

	/**
	 * The trade has been cancelled.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE_CANCELED() {
		return new SM_SYSTEM_MESSAGE(1400891);
	}

	/**
	 * Successfully conditioned equipped item(s).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE_ALL_COMPLETE() {
		return new SM_SYSTEM_MESSAGE(1400892);
	}

	/**
	 * %0 has been conditioned to level %1, and the item's stats have changed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE_LEVEL_DOWN(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400893, value0, value1);
	}

	/**
	 * All equipped items have been conditioned to their maximum level, and cannot be conditioned to level %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE_ALL_FAIL_EQUIPED_ALREADY_CHARGED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400894, value0);
	}

	/**
	 * None of the equipped items are conditionable.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE_ALL_FAIL_NO_CHARGEABLE_EQUIPMENT() {
		return new SM_SYSTEM_MESSAGE(1400895);
	}

	/**
	 * While the Pet Auto-Buffing is active, you cannot change the contents of the Buff Bag.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DOPING_PET_MESSAGE03() {
		return new SM_SYSTEM_MESSAGE(1400896);
	}

	/**
	 * Please log out of the game and take a break.
	 */
	public static SM_SYSTEM_MESSAGE STR_TIRED_REMAIN_PLAYTIME_CHINA_1() {
		return new SM_SYSTEM_MESSAGE(1400897);
	}

	/**
	 * Your in-game gains have been reduced to 50% of normal values. Please log out and taking a break.
	 */
	public static SM_SYSTEM_MESSAGE STR_TIRED_REMAIN_PLAYTIME_CHINA_2() {
		return new SM_SYSTEM_MESSAGE(1400898);
	}

	/**
	 * You can't acquire any quest while fatigued. Please take a break until your fatigue level decreases, and then resume play.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_ACQUIRE_QUEST_FATIGUE() {
		return new SM_SYSTEM_MESSAGE(1400899);
	}

	/**
	 * Quests can't be continued or completed while you are fatigued. Please log out and take a break until your fatigue level decreases.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANT_PROCEED_QUEST_FATIGUE() {
		return new SM_SYSTEM_MESSAGE(1400900);
	}

	/**
	 * %0 has been sealed by a GM. You cannot remove this seal.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_SEAL_STATUS_GMSEAL_UNSEAL_IMPOSSIBLE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400901, value0);
	}

	/**
	 * %0 has become the Legion Deputy.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_RANK_DONE_GUILD_SUBMASTER(String value0) {
		return new SM_SYSTEM_MESSAGE(1400902, value0);
	}

	/**
	 * %0 has become a Legion Member.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_CHANGE_MEMBER_RANK_DONE_GUILD_NEWBIE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400903, value0);
	}

	/**
	 * You may be unable to use certain skills or items in this area.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ENTERED_SKILL_ITEM_RESTRICTED_AREA() {
		return new SM_SYSTEM_MESSAGE(1400904);
	}

	/**
	 * You cannot use %1 in %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SKILL_ITEM_RESTRICTED_AREA(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400905, value1, value0);
	}

	/**
	 * You cannot use %1 until you reach level %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_ITEM_TOO_LOW_GUILD_LEVEL(String value1, String value0) {
		return new SM_SYSTEM_MESSAGE(1400906, value1, value0);
	}

	/**
	 * You have already learned this motion.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CUSTOMANIMATION_ALREADY_HAS_MOTION() {
		return new SM_SYSTEM_MESSAGE(1400907);
	}

	/**
	 * %0 has been conditioned. You cannot condition it further.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE_FAIL_ALREADY_FULLY_CHARGED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400908, value0);
	}

	/**
	 * The Outer Protective Wall is gone, and Weapon H is waking from its dormant state.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_HugenNM_00() {
		return new SM_SYSTEM_MESSAGE(1400909);
	}

	/**
	 * The Energy Generator is becoming unstable.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_HugenNM_01() {
		return new SM_SYSTEM_MESSAGE(1400910);
	}

	/**
	 * The Energy Generator has been destroyed and the power of the Protective Shield has been reduced.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_HugenNM_02() {
		return new SM_SYSTEM_MESSAGE(1400911);
	}

	/**
	 * The Energy Generator has been destroyed and the power of the Protective Shield has been greatly reduced.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_HugenNM_03() {
		return new SM_SYSTEM_MESSAGE(1400912);
	}

	/**
	 * The Energy Generator has been destroyed and the Protective Shield has disappeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_HugenNM_04() {
		return new SM_SYSTEM_MESSAGE(1400913);
	}

	/**
	 * You pet is sulky and can't feel better until you interact with it.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_CONDITION_CARE_END() {
		return new SM_SYSTEM_MESSAGE(1400914);
	}

	/**
	 * You need the aid of a Mentor to open it.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Ask_Mentor() {
		return new SM_SYSTEM_MESSAGE(1400915);
	}

	/**
	 * You need the aid of a lower level Group Member to open it.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DailyQuest_Ask_Mentee() {
		return new SM_SYSTEM_MESSAGE(1400916);
	}

	/**
	 * The %0 motion has expired and can no longer be used.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DELETE_CASH_CUSTOMANIMATION_BY_TIMEOUT(String value0) {
		return new SM_SYSTEM_MESSAGE(1400917, value0);
	}

	public static SM_SYSTEM_MESSAGE STR_MSG_DELETE_CASH_CUSTOMANIMATION_BY_TIMEOUT() {
		return new SM_SYSTEM_MESSAGE(1400917);
	}

	/**
	 * The Bridge to the Drana Production Lab has been raised.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF4Re_Drana_01() {
		return new SM_SYSTEM_MESSAGE(1400918);
	}

	/**
	 * Defeat all Drana Production Lab Section Managers to open the Laboratory Yard door.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF4Re_Drana_02() {
		return new SM_SYSTEM_MESSAGE(1400919);
	}

	/**
	 * The door to the Laboratory Yard is now open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF4Re_Drana_03() {
		return new SM_SYSTEM_MESSAGE(1400920);
	}

	/**
	 * The door to the Laboratory Air Conditioning Room is now open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF4Re_Drana_04() {
		return new SM_SYSTEM_MESSAGE(1400921);
	}

	/**
	 * The Laboratory Air Conditioning Room Ventilator is now open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF4Re_Drana_05() {
		return new SM_SYSTEM_MESSAGE(1400922);
	}

	/**
	 * The Drana Production Lab walkway is now open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF4Re_Drana_06() {
		return new SM_SYSTEM_MESSAGE(1400923);
	}

	/**
	 * The outer wall of the Bio Lab has collapsed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF4Re_Drana_07() {
		return new SM_SYSTEM_MESSAGE(1400924);
	}

	/**
	 * The Airship Weapon has appeared in your cube. Register it to the Skill Window to use it.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_Zone3_Morph_01() {
		return new SM_SYSTEM_MESSAGE(1400925);
	}

	/**
	 * The Recharger is filling your whole body with energy. It seems to be increasing!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_Doping_01() {
		return new SM_SYSTEM_MESSAGE(1400926);
	}

	/**
	 * The Shulack Drink is energizing you!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_Doping_02() {
		return new SM_SYSTEM_MESSAGE(1400927);
	}

	/**
	 * Round %0 begins!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_START_ROUND_IDARENA(String value0) {
		return new SM_SYSTEM_MESSAGE(1400928, value0);
	}

	/**
	 * You have eliminated all enemies in Round %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_COMPLETE_ROUND_IDARENA(String value0) {
		return new SM_SYSTEM_MESSAGE(1400929, value0);
	}

	/**
	 * You have passed Stage %0!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_COMPLETE_STAGE_IDARENA(String value0) {
		return new SM_SYSTEM_MESSAGE(1400930, value0);
	}

	/**
	 * You join Stage %0 Round %1!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_JOIN_ROUND_IDARENA(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400931, value0, value1);
	}

	/**
	 * You failed the training and have been sent to the Ready Room.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_MOVE_BIRTHAREA_ME_IDARENA() {
		return new SM_SYSTEM_MESSAGE(1400932);
	}

	/**
	 * %0 failed the training and has been sent to the Ready Room.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_MOVE_BIRTHAREA_FRIENDLY_IDARENA(String value0) {
		return new SM_SYSTEM_MESSAGE(1400933, value0);
	}

	/**
	 * You have acquired %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_GET_COIN_IDARENA(String value0) {
		return new SM_SYSTEM_MESSAGE(1400934, value0);
	}

	/**
	 * You cannot use that because the wind has weakened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WindPathOff() {
		return new SM_SYSTEM_MESSAGE(1400935);
	}

	/**
	 * You cannot re-enter the Crucible until the cooldown time has expired.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_REENTER_INSTANCE_IDARENA() {
		return new SM_SYSTEM_MESSAGE(1400936);
	}

	/**
	 * You have acquired %num1 %0s.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_GET_COIN_MULTI_IDARENA(int num1, String value0s) {
		return new SM_SYSTEM_MESSAGE(1400937, num1, value0s);
	}

	/**
	 * You have %0 kinah left in your daily sell limit, and this transaction would exceed that.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DAY_CANNOT_SELL_NPC(long value0) {
		return new SM_SYSTEM_MESSAGE(1400938, value0);
	}

	/**
	 * The %0 has crystallized in your cube.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_HCOIN_02(String value0) {
		return new SM_SYSTEM_MESSAGE(1400939, value0);
	}

	/**
	 * The item you bought has arrived.
	 */
	public static SM_SYSTEM_MESSAGE STR_POSTMAN_NOTIFY_CASH() {
		return new SM_SYSTEM_MESSAGE(1400940);
	}

	/**
	 * Your trading partner has reached the daily Private Store trading limit, so the trade cannot be completed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DAY_CANNOT_PARTNER_SHOP() {
		return new SM_SYSTEM_MESSAGE(1400941);
	}

	/**
	 * You have reached the daily Private Store trading limit of %0 Kinah, so the trade cannot be completed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DAY_CANNOT_OWN_SHOP(String value0) {
		return new SM_SYSTEM_MESSAGE(1400942, value0);
	}

	/**
	 * Your trading partner has reached the daily Trading limit, so the trade cannot be completed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DAY_CANNOT_PARTNER_TRADE() {
		return new SM_SYSTEM_MESSAGE(1400943);
	}

	/**
	 * You have reached the daily Trading limit of %0 Kinah, so the trade cannot be completed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DAY_CANNOT_OWN_TRADE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400944, value0);
	}

	/**
	 * You have reached the daily Broker limit of %0 Kinah, so the item cannot be listed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DAY_CANNOT_SELL_AUCTION(String value0) {
		return new SM_SYSTEM_MESSAGE(1400945, value0);
	}

	/**
	 * You have reached the daily Mail attachment limit of %0 Kinah, so the kinah cannot be sent.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DAY_CANNOT_SEND_MAIL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400946, value0);
	}

	/**
	 * You have reached the daily Mail attachment limit of %0 Kinah, so you cannot receive this kinah.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DAY_CANNOT_RECEIVE_MAIL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400947, value0);
	}

	/**
	 * %0 has declined your invitation to the Mini Fortress Battle.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOWER_DEFENCE_REJECT_INVITATION(String value0) {
		return new SM_SYSTEM_MESSAGE(1400948, value0);
	}

	/**
	 * %0 has joined the Mini Fortress Battle.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOWER_DEFENCE_ENTERED_PARTY(String value0) {
		return new SM_SYSTEM_MESSAGE(1400949, value0);
	}

	/**
	 * Your trading partner has reached the daily Trading limit, so the trade has been canceled.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DAY_CANNOT_PARTNER_TRADE_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1400950);
	}

	/**
	 * You have reached the daily Trading limit, so the trade cannot be completed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DAY_CANNOT_OWN_TRADE_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1400951);
	}

	/**
	 * You have invited %0 to join the Mini Game.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOWER_DEFENCE_INVITED_HIM(String value0) {
		return new SM_SYSTEM_MESSAGE(1400952, value0);
	}

	/**
	 * %0 cannot accept your Mini Game invitation right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOWER_DEFENCE_CANT_INVITE_WHEN_HE_IS_ASKED_QUESTION(String value0) {
		return new SM_SYSTEM_MESSAGE(1400953, value0);
	}

	/**
	 * %0 is participating in another Mini Fortress Battle.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOWER_DEFENCE_ALREADY_MEMBER_OF_OTHER_GAME(String value0) {
		return new SM_SYSTEM_MESSAGE(1400954, value0);
	}

	/**
	 * The power binding the soul of %0 has weakened, and %1 has vanished.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DELETE_ITEM_CHANGE_TO_PUBLIC(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400955, value0, value1);
	}

	/**
	 * The power binding the soul of %0 with %1 has weakened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WILL_DELETE_ITEM_CHANGE_TO_PUBLIC(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400956, value0, value1);
	}

	/**
	 * The treasure chest has disappeared because you failed to destroy the monsters within the time limit.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDABRECORE_OOPS_REWARD_IS_GONE() {
		return new SM_SYSTEM_MESSAGE(1400957);
	}

	/**
	 * The Dredgion Generator has been destroyed. Its Protector is coming soon!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_Zone2_Tower_01() {
		return new SM_SYSTEM_MESSAGE(1400958);
	}

	/**
	 * Cannot find the Supplements.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_ENCHANT_ASSISTANT_CANNOT_FIND() {
		return new SM_SYSTEM_MESSAGE(1400959);
	}

	/**
	 * You cannot use sealed Supplements.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_ENCHANT_ASSISTANT_SEALED() {
		return new SM_SYSTEM_MESSAGE(1400960);
	}

	/**
	 * You cannot use those Supplements.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_ENCHANT_ASSISTANT_NO_RIGHT_ITEM() {
		return new SM_SYSTEM_MESSAGE(1400961);
	}

	/**
	 * %0 dropped out of training and left the Empyrean Crucible.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FRIENDLY_LEAVE_IDARENA(String value0) {
		return new SM_SYSTEM_MESSAGE(1400962, value0);
	}

	/**
	 * Training is in progress. You must stay in the Ready Room until you can join.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ENTERED_BIRTHAREA_IDARENA() {
		return new SM_SYSTEM_MESSAGE(1400963);
	}

	/**
	 * %0 has reentered the Illusion Stadium.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FRIENDLY_MOVE_COMBATAREA_IDARENA(String value0) {
		return new SM_SYSTEM_MESSAGE(1400964, value0);
	}

	/**
	 * You do not have enough kinah to condition that item.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_CHARGE_NOT_ENOUGH_GOLD() {
		return new SM_SYSTEM_MESSAGE(1400965);
	}

	/**
	 * All fatigue is gone. You have %0 Fatigue Recovery remaining.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FATIGUE_STATE_RECOVERED(String value0) {
		return new SM_SYSTEM_MESSAGE(1400966, value0);
	}

	/**
	 * You have reached maximum Fatigue, and so can obtain only limited XP, AP, and items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FATIGUE_STATE_INFO_STATE_CHANGE() {
		return new SM_SYSTEM_MESSAGE(1400967);
	}

	/**
	 * You are very Fatigued, and so can obtain only limited XP, AP, and items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FATIGUE_STATE_INFO() {
		return new SM_SYSTEM_MESSAGE(1400968);
	}

	/**
	 * You feel refreshed, and your Fatigue is gone.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FATIGUE_RESET() {
		return new SM_SYSTEM_MESSAGE(1400969);
	}

	/**
	 * You cannot extract items while %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_CHARGE_INVALID_STANCE(String value0) {
		return new SM_SYSTEM_MESSAGE(1400970, value0);
	}

	/**
	 * %0 quit the Mini Fortress Battle, and %1 became the leader.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOWER_DEFENCE_HOST_MOVED(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400971, value0, value1);
	}

	/**
	 * Looted!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_LOOTING_DIALOG() {
		return new SM_SYSTEM_MESSAGE(1400972);
	}

	/**
	 * Buff the Master!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PET_DOPING_DIALOG() {
		return new SM_SYSTEM_MESSAGE(1400973);
	}

	/**
	 * %0 succeeded in crafting %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBINE_BROADCAST_COMBINE_SUCCESS(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1400974, value0, value1);
	}

	/**
	 * A Worthiness Ticket Box has appeared in the Illusion Stadium.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S1_ResurBox1_01() {
		return new SM_SYSTEM_MESSAGE(1400975);
	}

	/**
	 * A Worthiness Ticket Box has appeared in the Ready Room.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S3_ResurBox1_01() {
		return new SM_SYSTEM_MESSAGE(1400976);
	}

	/**
	 * A Worthiness Ticket Box has appeared in the Ready Room.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S6_ResurBox1_01() {
		return new SM_SYSTEM_MESSAGE(1400977);
	}

	/**
	 * You can earn an additional reward if you catch the Saam King.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S2_SAAM_CTRL_01() {
		return new SM_SYSTEM_MESSAGE(1400978);
	}

	/**
	 * King Saam will disappear in 30 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S2_Saam1_01() {
		return new SM_SYSTEM_MESSAGE(1400979);
	}

	/**
	 * King Saam will disappear in 10 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S2_Saam1_02() {
		return new SM_SYSTEM_MESSAGE(1400980);
	}

	/**
	 * King Saam will disappear in 5 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S2_Saam1_03() {
		return new SM_SYSTEM_MESSAGE(1400981);
	}

	/**
	 * The Drakies will appear soon!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S4_Draky_CTRL_01() {
		return new SM_SYSTEM_MESSAGE(1400982);
	}

	/**
	 * The Ornate Treasure Chest has appeared in the Illusion Stadium!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S7_BookBox_01() {
		return new SM_SYSTEM_MESSAGE(1400983);
	}

	/**
	 * Lightning Drakie has appeared!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S9_DuskDraky_55_Ah_01() {
		return new SM_SYSTEM_MESSAGE(1400984);
	}

	/**
	 * You must be under level %0 to join.
	 */
	public static SM_SYSTEM_MESSAGE STR_FACTION_JOIN_ERROR_MAX_LEVEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1400985, value0);
	}

	/**
	 * Administrator Arminos has appeared!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S3_Elemeltal_CTRL_01() {
		return new SM_SYSTEM_MESSAGE(1400986);
	}

	/**
	 * Administrator Arminos has appeared!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S4_Draky_CTRL_00() {
		return new SM_SYSTEM_MESSAGE(1400987);
	}

	/**
	 * 3...
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S4_Draky_CTRL_02() {
		return new SM_SYSTEM_MESSAGE(1400988);
	}

	/**
	 * 2...
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S4_Draky_CTRL_03() {
		return new SM_SYSTEM_MESSAGE(1400989);
	}

	/**
	 * 1...
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S4_Draky_CTRL_04() {
		return new SM_SYSTEM_MESSAGE(1400990);
	}

	/**
	 * Administrator Arminos has appeared!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S6_Ghost_55_Ah_01() {
		return new SM_SYSTEM_MESSAGE(1400991);
	}

	/**
	 * Lightning Drakie has disappeared!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_S9_DuskDraky_55_Ah_02() {
		return new SM_SYSTEM_MESSAGE(1400992);
	}

	/**
	 * You must have a Mentor with you in order to complete this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_KILL_COUNT_WITHOUT_MENTOR() {
		return new SM_SYSTEM_MESSAGE(1400993);
	}

	/**
	 * You must be Mentoring someone in order to complete this quest.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_KILL_COUNT_WITHOUT_MENTEE() {
		return new SM_SYSTEM_MESSAGE(1400994);
	}

	/**
	 * Cannot find the location for the selected quest step.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NEW_MAP_QUEST_CANT_FIND_NPC() {
		return new SM_SYSTEM_MESSAGE(1400995);
	}

	/**
	 * The Surkana Supplier has overloaded.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF4Re_Drana_08() {
		return new SM_SYSTEM_MESSAGE(1400996);
	}

	/**
	 * The Surkana Steam Jet has generated an updraft.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF4Re_Drana_09() {
		return new SM_SYSTEM_MESSAGE(1400997);
	}

	/**
	 * Management Director Surama uses Collapsing Earth.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF4Re_Drana_10() {
		return new SM_SYSTEM_MESSAGE(1400998);
	}

	/**
	 * A large number of Balaur Troopers descend from the Dredgion.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_01() {
		return new SM_SYSTEM_MESSAGE(1400999);
	}

	/**
	 * Kamara explodes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_02() {
		return new SM_SYSTEM_MESSAGE(1401000);
	}

	/**
	 * Norris's eyes turn red.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_03() {
		return new SM_SYSTEM_MESSAGE(1401001);
	}

	/**
	 * The eyes of King Consierd turn red.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_04() {
		return new SM_SYSTEM_MESSAGE(1401002);
	}

	/**
	 * The eyes of Takun the Terrible turn red.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_05() {
		return new SM_SYSTEM_MESSAGE(1401003);
	}

	/**
	 * The eyes of Gojira turn red.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_06() {
		return new SM_SYSTEM_MESSAGE(1401004);
	}

	/**
	 * The eyes of Andre turn red.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_07() {
		return new SM_SYSTEM_MESSAGE(1401005);
	}

	/**
	 * The eyes of Kamara turn red.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_08() {
		return new SM_SYSTEM_MESSAGE(1401006);
	}

	/**
	 * Unlimited Battle Temporary System Message
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_09() {
		return new SM_SYSTEM_MESSAGE(1401007);
	}

	/**
	 * Unlimited Battle Temporary System Message
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_10() {
		return new SM_SYSTEM_MESSAGE(1401008);
	}

	/**
	 * A Worthiness Ticket has appeared in your cube.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ResurBox() {
		return new SM_SYSTEM_MESSAGE(1401009);
	}

	/**
	 * Spirits will disappear in 30 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_S3_Bonus_01() {
		return new SM_SYSTEM_MESSAGE(1401010);
	}

	/**
	 * Spirits will disappear in 10 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_S3_Bonus_02() {
		return new SM_SYSTEM_MESSAGE(1401011);
	}

	/**
	 * Spirits will disappear in 5 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_S3_Bonus_03() {
		return new SM_SYSTEM_MESSAGE(1401012);
	}

	/**
	 * Drakies will disappear in 30 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_S4_Bonus_01() {
		return new SM_SYSTEM_MESSAGE(1401013);
	}

	/**
	 * Drakies will disappear in 10 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_S4_Bonus_02() {
		return new SM_SYSTEM_MESSAGE(1401014);
	}

	/**
	 * Drakies will disappear in 5 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_S4_Bonus_03() {
		return new SM_SYSTEM_MESSAGE(1401015);
	}

	/**
	 * Administrator Arminos will disappear in 30 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_S6_Bonus_01() {
		return new SM_SYSTEM_MESSAGE(1401016);
	}

	/**
	 * Administrator Arminos will disappear in 10 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_S6_Bonus_02() {
		return new SM_SYSTEM_MESSAGE(1401017);
	}

	/**
	 * Administrator Arminos will disappear in 5 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_S6_Bonus_03() {
		return new SM_SYSTEM_MESSAGE(1401018);
	}

	/**
	 * Lightning Drakie will disappear in 30 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_S9_Bonus_01() {
		return new SM_SYSTEM_MESSAGE(1401019);
	}

	/**
	 * Lightning Drakie will disappear in 10 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_S9_Bonus_02() {
		return new SM_SYSTEM_MESSAGE(1401020);
	}

	/**
	 * Lightning Drakie will disappear in 5 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_S9_Bonus_03() {
		return new SM_SYSTEM_MESSAGE(1401021);
	}

	/**
	 * Because you belong to an Alliance, you cannot post a <Recruit Mentor Group> message. Use /RecruitAllianceMember and the <Recruit Alliance> window
	 * instead.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_REGISTER_MENTOR_PARTY_IN_FORCE() {
		return new SM_SYSTEM_MESSAGE(1401022);
	}

	/**
	 * You can see Omega's Recharger. Certainly there would be no harm in trying it.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_Doping_01_AD() {
		return new SM_SYSTEM_MESSAGE(1401023);
	}

	/**
	 * You can see the Energy Drink Can that Shulacks often drink. Certainly there would be no harm in trying it.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_Doping_02_AD() {
		return new SM_SYSTEM_MESSAGE(1401024);
	}

	/**
	 * You failed to purchase the item. Please try again later.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CASH_PURCHASE_ERROR_FAILED_RECEIVING_PRODUCT() {
		return new SM_SYSTEM_MESSAGE(1401025);
	}

	/**
	 * That item is invalid.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CASH_PURCHASE_ERROR_INVALID_PRODUCT() {
		return new SM_SYSTEM_MESSAGE(1401026);
	}

	/**
	 * You have acquired the %0 motion.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GET_CASH_CUSTOMIZE_MOTION(String value0) {
		return new SM_SYSTEM_MESSAGE(1401029, value0);
	}

	/**
	 * One of the Distribution Targets has reached the daily Trading limit, so the trade cannot be completed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DAY_CANNOT_SHARE_TRADE_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1401034);
	}

	/**
	 * Dalia Charlands has vanished.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF4Re_Drana_11() {
		return new SM_SYSTEM_MESSAGE(1401036);
	}

	/**
	 * The Surkana Supplier has been broken.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF4Re_Drana_12() {
		return new SM_SYSTEM_MESSAGE(1401037);
	}

	/**
	 * System error. Please try again later.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CASH_PURCHASE_ERROR_SYSTEM_ERROR() {
		return new SM_SYSTEM_MESSAGE(1401038);
	}

	/**
	 * Your cube is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CASH_PURCHASE_ERROR_NOT_ENOUGH_SPACE() {
		return new SM_SYSTEM_MESSAGE(1401039);
	}

	/**
	 * You cannot summon a pet here.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_CANT_SUMMON_MOVING_STATE() {
		return new SM_SYSTEM_MESSAGE(1401040);
	}

	/**
	 * Tuali is overloaded.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDElemental1_GolemPrime_Forest_Overclock() {
		return new SM_SYSTEM_MESSAGE(1401041);
	}

	/**
	 * You cannot fight a duel here.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DUEL_CANT_IN_THIS_ZONE() {
		return new SM_SYSTEM_MESSAGE(1401047);
	}

	/**
	 * The door to Ashunatal's Ready Room is now open. You can see Ashunatal behind the door.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_3FDoor_322() {
		return new SM_SYSTEM_MESSAGE(1401048);
	}

	/**
	 * The door of the Aircrew Room is now open. Kill the Drakan!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_3FDoor_312() {
		return new SM_SYSTEM_MESSAGE(1401049);
	}

	/**
	 * Alarms rang in the Waiting Room. High-powered Drakan are heading your way!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDStation_3FDoor_311() {
		return new SM_SYSTEM_MESSAGE(1401050);
	}

	/**
	 * You have been disconnected from the Bid Withdrawal Server. Please try again later.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_BILLING_SERVER_DOWN_SA2() {
		return new SM_SYSTEM_MESSAGE(1401051);
	}

	/**
	 * You can not verify Pet Status in the current state.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_CANT_SHOPOPEN_STATE() {
		return new SM_SYSTEM_MESSAGE(1401052);
	}

	/**
	 * You have already requested entry into %WORLDNAME0 (Difficulty: %1).
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_INSTANCE_ALREADY_REGISTERED_WITH_DIFFICULTY(int worldId, String value1) {
		return new SM_SYSTEM_MESSAGE(1401053, worldId, value1);
	}

	/**
	 * You cannot make more entry requests.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_INSTANCE_NO_MORE_REGISTER() {
		return new SM_SYSTEM_MESSAGE(1401054);
	}

	/**
	 * You have aborted entering %0 (Difficulty: %1). You may apply again in 10 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_REGISTER_CANCELED_WTH_DIFFICULTY(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1401055, value0, value1);
	}

	/**
	 * NEW
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_NOTICE_BLANK() {
		return new SM_SYSTEM_MESSAGE(1401057);
	}

	/**
	 * The time for group member recruitment has expired. You cannot recruit more group members.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_CANT_REINFORCE_MEMBER() {
		return new SM_SYSTEM_MESSAGE(1401058);
	}

	/**
	 * You cannot use the commands Invite to Group or Invite to Legion right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_CANT_INVITE_PARTY_COMMAND() {
		return new SM_SYSTEM_MESSAGE(1401059);
	}

	/**
	 * You cannot use the commands Assign Group Leader and Make Alliance Captain right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_CANT_CHANGE_LEADER_PARTY_COMMAND() {
		return new SM_SYSTEM_MESSAGE(1401060);
	}

	/**
	 * You cannot use the commands Leave Group or Leave Alliance right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_CANT_QUIT_PARTY_COMMAND() {
		return new SM_SYSTEM_MESSAGE(1401061);
	}

	/**
	 * You cannot use the commands Ban Group or Ban from Alliance right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_CANT_BANISH_PARTY_COMMAND() {
		return new SM_SYSTEM_MESSAGE(1401062);
	}

	/**
	 * You cannot use the commands Distribute Cash to Group or Distribute Cash to Alliance settings right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_CANT_LOOT_PARTY_COMMAND() {
		return new SM_SYSTEM_MESSAGE(1401063);
	}

	/**
	 * You cannot change group or alliance members right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_CANT_ARRANGE_MEMBER_FORCE_COMMAND() {
		return new SM_SYSTEM_MESSAGE(1401064);
	}

	/**
	 * Quest declined.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_QUEST_ACQUIRE_ERROR_CANCLE() {
		return new SM_SYSTEM_MESSAGE(1401065);
	}

	/**
	 * You must have %0 to do this.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_QUEST_COMPLETE_ERROR_QUEST_ITEM_RETRY(String value0) {
		return new SM_SYSTEM_MESSAGE(1401066, value0);
	}

	/**
	 * Poppy is running from the Dukaki Cooks. Eliminate them and help Poppy to reach the refuge.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_Solo_SB1_START_BROADCAST() {
		return new SM_SYSTEM_MESSAGE(1401067);
	}

	/**
	 * There are 5 Dukaki Cooks remaining.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_Solo_SB1_5Dead_BROADCAST() {
		return new SM_SYSTEM_MESSAGE(1401068);
	}

	/**
	 * Careful! Poppy's health is very low.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_Solo_SB1_AllDead_BROADCAST() {
		return new SM_SYSTEM_MESSAGE(1401069);
	}

	/**
	 * Poppy has almost reached the refuge. Just a little bit further!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_Solo_SB1_HideNear_BROADCAST() {
		return new SM_SYSTEM_MESSAGE(1401070);
	}

	/**
	 * Poppy has reached the refuge safely. A successful rescue!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_Solo_SB1_HideSucc_BROADCAST() {
		return new SM_SYSTEM_MESSAGE(1401071);
	}

	/**
	 * You have eliminated all of the Dukaki Cooks and successfully rescued Poppy!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_Solo_SB1_Succ_BROADCAST() {
		return new SM_SYSTEM_MESSAGE(1401072);
	}

	/**
	 * You already have a pet of this type.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_CANT_USE_ALREADY_HAS_PET() {
		return new SM_SYSTEM_MESSAGE(1401073);
	}

	/**
	 * You already have a pack pet with this functionality.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOYPET_ALREADY_SAME_WAREHOUSE_PET() {
		return new SM_SYSTEM_MESSAGE(1401074);
	}

	/**
	 * Poppy was captured by the Dukaki Cooks... and roasted whole!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_Solo_SB1_Failed_BROADCAST() {
		return new SM_SYSTEM_MESSAGE(1401075);
	}

	/**
	 * The Dukaki Cooks attacked and wounded Poppy!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_Solo_SB1_LowHP_BROADCAST() {
		return new SM_SYSTEM_MESSAGE(1401082);
	}

	/**
	 * Poppy was attacked by the Dukaki Cooks. They're planning to roast Poppy for dinner!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDArena_Solo_SB1_LowHP2_BROADCAST() {
		return new SM_SYSTEM_MESSAGE(1401083);
	}

	/**
	 * Smash the Meat Barrel to lure and destroy the Starved Karnifs.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_IDArena_Solo_S4_System1() {
		return new SM_SYSTEM_MESSAGE(1401084);
	}

	/**
	 * Smash the Aether Barrel to lure and destroy the Thirsty Spirits.
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_IDArena_Solo_S4_System2() {
		return new SM_SYSTEM_MESSAGE(1401085);
	}

	/**
	 * Stop Gomju from perpetrating a senseless massacre!
	 */
	public static SM_SYSTEM_MESSAGE STR_CHAT_IDArena_Solo_S4_System5() {
		return new SM_SYSTEM_MESSAGE(1401086);
	}

	/**
	 * You cannot attack while on board.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ATTACK_RESTRICTION_RIDE() {
		return new SM_SYSTEM_MESSAGE(1401093);
	}

	/**
	 * You cannot use this item while on board.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_RESTRICTION_RIDE() {
		return new SM_SYSTEM_MESSAGE(1401094);
	}

	/**
	 * You cannot open a private store while on board.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PERSONAL_SHOP_RESTRICTION_RIDE() {
		return new SM_SYSTEM_MESSAGE(1401095);
	}

	/**
	 * You cannot gather while on board.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GATHER_RESTRICTION_RIDE() {
		return new SM_SYSTEM_MESSAGE(1401096);
	}

	/**
	 * You cannot craft while on board.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_COMBINE_RESTRICTION_RIDE() {
		return new SM_SYSTEM_MESSAGE(1401097);
	}

	/**
	 * You cannot use the commands /RecruitGroupMember or /RecruitAllianceMember right now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_CANT_POST_PARTY_COMMAND() {
		return new SM_SYSTEM_MESSAGE(1401098);
	}

	/**
	 * You cannot get on the mount here.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_RIDE_INVALID_LOCATION() {
		return new SM_SYSTEM_MESSAGE(1401099);
	}

	/**
	 * Failed to find the instance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NEW_MAP_INFO_CANT_FIND_INSTANCE() {
		return new SM_SYSTEM_MESSAGE(1401106);
	}

	/**
	 * Assembly success.
	 */
	public static SM_SYSTEM_MESSAGE STR_ASSEMBLY_ITEM_SUCCEEDED() {
		return new SM_SYSTEM_MESSAGE(1401122);
	}

	/**
	 * This emblem is already registered.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_ALREADY_POSTED_THIS_EMBLEM() {
		return new SM_SYSTEM_MESSAGE(1401142);
	}

	/**
	 * You can only whisper to your own faction.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_WHISPER_OTHER_RACE() {
		return new SM_SYSTEM_MESSAGE(1401174);
	}

	/**
	 * A Master cannot take Work Orders.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DONT_GET_COMBINETASK_MASTER() {
		return new SM_SYSTEM_MESSAGE(1401182);
	}

	/**
	 * You cannot use it as you don't have enough %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_USE_HOUSE_OBJECT_ITEM_CHECK(String value0) {
		return new SM_SYSTEM_MESSAGE(1401199, value0);
	}

	/**
	 * Your cube is full.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_USE_HOUSE_OBJECT_INVENTORY_IS_FULL() {
		return new SM_SYSTEM_MESSAGE(1401200);
	}

	/**
	 * You cannot mount while %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_RIDE(String l10n) {
		return new SM_SYSTEM_MESSAGE(1401211, l10n);
	}

	/**
	 * You cannot make a bid for your own house.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_BID_MY_HOUSE() {
		return new SM_SYSTEM_MESSAGE(1401221);
	}

	/**
	 * You can only bid on a house one time.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_BID_SUCC_BID_HOUSE() {
		return new SM_SYSTEM_MESSAGE(1401222);
	}

	/**
	 * You are currently the highest bidder for another house.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_BID_OTHER_HOUSE() {
		return new SM_SYSTEM_MESSAGE(1401223);
	}

	/**
	 * You may bid after the grace period ends on your other house.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_BID_GRACE_HOUSE() {
		return new SM_SYSTEM_MESSAGE(1401224);
	}

	/**
	 * You must be Level %0 or higher to bid on the house.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_BID_LOW_LEVEL(int minLevel) {
		return new SM_SYSTEM_MESSAGE(1401225, minLevel);
	}

	/**
	 * Your housing payment is due. Please pay your maintenance costs.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OVERDUE() {
		return new SM_SYSTEM_MESSAGE(1401226);
	}

	/**
	 * Your house has been seized against your unpaid maintenance fees.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_SEQUESTRATE() {
		return new SM_SYSTEM_MESSAGE(1401227);
	}

	/**
	 * Only house owners and their friends may enter.
	 */
	public static SM_SYSTEM_MESSAGE STR_HOUSING_TELEPORT_CANT_USE() {
		return new SM_SYSTEM_MESSAGE(1401244);
	}

	/**
	 * A Spring Agrint has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HF_SpringAgrintAppear() {
		return new SM_SYSTEM_MESSAGE(1401246);
	}

	/**
	 * A Summer Agrint has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HF_SummerAgrintAppear() {
		return new SM_SYSTEM_MESSAGE(1401247);
	}

	/**
	 * A Autumn Agrint has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HF_FallAgrintAppear() {
		return new SM_SYSTEM_MESSAGE(1401248);
	}

	/**
	 * A Winter Agrint has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HF_WinterAgrintAppear() {
		return new SM_SYSTEM_MESSAGE(1401249);
	}

	/**
	 * This area is only accessible to Leagues.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ENTER_ONLY_UNION_DON() {
		return new SM_SYSTEM_MESSAGE(1401251);
	}

	/**
	 * You cannot ride while in an Altered State.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_UNRIDE_ABNORMAL_STATE() {
		return new SM_SYSTEM_MESSAGE(1401254);
	}

	/**
	 * You cannot ride while in an Altered State.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_RIDE_ABNORMAL_STATE() {
		return new SM_SYSTEM_MESSAGE(1401255);
	}

	/**
	 * It is already occupied.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OBJECT_OCCUPIED_BY_OTHER() {
		return new SM_SYSTEM_MESSAGE(1401256);
	}

	/**
	 * Using %0%.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OBJECT_USE(String value0) {
		return new SM_SYSTEM_MESSAGE(1401257, value0);
	}

	/**
	 * You freed the object.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OBJECT_CANCEL_USE() {
		return new SM_SYSTEM_MESSAGE(1401258);
	}

	/**
	 * You can use it only once a day.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OBJECT_CANT_USE_PER_DAY() {
		return new SM_SYSTEM_MESSAGE(1401260);
	}

	/**
	 * %0 is worn out and useless.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OBJECT_DELETE_EXPIRE_TIME(String value0) {
		return new SM_SYSTEM_MESSAGE(1401261, value0);
	}

	/**
	 * %0 is is no longer available.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OBJECT_DELETE_USE_COUNT(String value0) {
		return new SM_SYSTEM_MESSAGE(1401262, value0);
	}

	/**
	 * You have acquired %1% from %0%.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OBJECT_REWARD_ITEM(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1401263, value0, value1);
	}

	/**
	 * You made a bid for %addr0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_BID_SUCCESS(int address) {
		return new SM_SYSTEM_MESSAGE(1401265, address);
	}

	/**
	 * You have been passed over in favor of a higher bid.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_BID_CANCEL() {
		return new SM_SYSTEM_MESSAGE(1401266);
	}

	/**
	 * %addr0 is sold to you.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_BID_WIN(int address) {
		return new SM_SYSTEM_MESSAGE(1401267, address);
	}

	/**
	 * You listed %addr0 for auction.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_AUCTION_MY_HOUSE(int address) {
		return new SM_SYSTEM_MESSAGE(1401268, address);
	}

	/**
	 * You successfully auctioned %addr0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_AUCTION_SUCCESS(int address) {
		return new SM_SYSTEM_MESSAGE(1401269, address);
	}

	/**
	 * Listed %addr0 was not auctioned.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_AUCTION_FAIL(int address) {
		return new SM_SYSTEM_MESSAGE(1401270, address);
	}

	/**
	 * The two-week grace period for %addr0 begins.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_GRACE_START(int address) {
		return new SM_SYSTEM_MESSAGE(1401271, address);
	}

	/**
	 * %addr0's grace period has ended.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_GRACE_SUCCESS(int address) {
		return new SM_SYSTEM_MESSAGE(1401272, address);
	}

	/**
	 * %addr0's grace period has ended. You no longer own %addr1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_GRACE_FAIL(int newAddress, int oldAddress) {
		return new SM_SYSTEM_MESSAGE(1401273, newAddress, oldAddress);
	}

	/**
	 * You cannot make a bid now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_BID_TIMEOUT() {
		return new SM_SYSTEM_MESSAGE(1401274);
	}

	/**
	 * You have bought a studio.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_INS_OWN_SUCCESS() {
		return new SM_SYSTEM_MESSAGE(1401275);
	}

	/**
	 * You already have a house.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_INS_CANT_OWN_MORE_HOUSE() {
		return new SM_SYSTEM_MESSAGE(1401276);
	}

	/**
	 * You must complete %quest0 first.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_OWN_NOT_COMPLETE_QUEST(int questId) {
		return new SM_SYSTEM_MESSAGE(1401277, questId);
	}

	/**
	 * The item's cooldown time has yet to expire.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANNOT_USE_FLOWERPOT_COOLTIME() {
		return new SM_SYSTEM_MESSAGE(1401280);
	}

	/**
	 * Another cabinet is already open.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ALREADY_OPEN_ANOTHER_STORAGE() {
		return new SM_SYSTEM_MESSAGE(1401282);
	}

	/**
	 * You need %num0 Kinah to make a bid.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_BID_NOT_ENOUGH_MONEY(long kinah) {
		return new SM_SYSTEM_MESSAGE(1401283, kinah);
	}

	/**
	 * You are too far away.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OBJECT_TOO_FAR_TO_USE() {
		return new SM_SYSTEM_MESSAGE(1401297);
	}

	/**
	 * Only the owner can use it.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OBJECT_IS_ONLY_FOR_OWNER_VALID() {
		return new SM_SYSTEM_MESSAGE(1401298);
	}

	/**
	 * You must equip %0 to use it.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_USE_HOUSE_OBJECT_ITEM_EQUIP(String value0) {
		return new SM_SYSTEM_MESSAGE(1401294, value0);
	}

	/**
	 * You have reached the maximum usage count.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OBJECT_ACHIEVE_USE_COUNT() {
		return new SM_SYSTEM_MESSAGE(1401295);
	}

	/**
	 * It is unavailable.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OBJECT_ALL_CANT_USE() {
		return new SM_SYSTEM_MESSAGE(1401296);
	}

	/**
	 * You cannot enter %WORLDNAME0 at this time.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_CLOSED_TIME(int worldId) {
		return new SM_SYSTEM_MESSAGE(1401306, worldId);
	}

	/**
	 * The minimum bid price for the selected house has changed. Please, check the new price.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_BID_LOWER() {
		return new SM_SYSTEM_MESSAGE(1401307);
	}

	/**
	 * You cannot register now.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_AUCTION_TIMEOUT() {
		return new SM_SYSTEM_MESSAGE(1401308);
	}

	/**
	 * You need to pay the maintenance fee to list it.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_AUCTION_OVERDUE() {
		return new SM_SYSTEM_MESSAGE(1401317);
	}

	/**
	 * You are not allowed to enter %addr0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_ENTER_NO_RIGHT(int address) {
		return new SM_SYSTEM_MESSAGE(1401322, address);
	}

	/**
	 * The home you have made an offer for has a new high bid of %num0 Kinah.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_PRICE_CHANGE(long kinah) {
		return new SM_SYSTEM_MESSAGE(1401324, kinah);
	}

	/**
	 * %0 disappears in 10 minutes because your Abyss Rank changed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_UNEQUIP_RANKITEM_TIMER_10M(String value0) {
		return new SM_SYSTEM_MESSAGE(1401327, value0);
	}

	/**
	 * %0 disappears in 1 minute because your Abyss Rank changed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_UNEQUIP_RANKITEM_TIMER_1M(String value0) {
		return new SM_SYSTEM_MESSAGE(1401328, value0);
	}

	/**
	 * %0 disappeared because your Abyss Rank changed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_UNEQUIP_RANKITEM(String value0) {
		return new SM_SYSTEM_MESSAGE(1401329, value0);
	}

	/**
	 * You reached your %0 usage goal.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_FLOWERPOT_GOAL(String value0) {
		return new SM_SYSTEM_MESSAGE(1401333, value0);
	}

	/**
	 * Selection invalid. Try refreshing the list.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_BID_FAIL() {
		return new SM_SYSTEM_MESSAGE(1401348);
	}

	/**
	 * You cannot bid until you pay the maintenance fees on your house.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_BID_OVERDUE() {
		return new SM_SYSTEM_MESSAGE(1401349);
	}

	/**
	 * Some areas of the Ammunition Depot are locked.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Station_DoorCtrl_Evileye() {
		return new SM_SYSTEM_MESSAGE(1401350);
	}

	/**
	 * You need a house for that.
	 */
	public static SM_SYSTEM_MESSAGE STR_HOUSING_TELEPORT_NEED_HOUSE() {
		return new SM_SYSTEM_MESSAGE(1401357);
	}

	/**
	 * You cannot decorate in the current state.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_MODE_CANNOT_START() {
		return new SM_SYSTEM_MESSAGE(1401358);
	}

	/**
	 * You need a studio to enter.
	 */
	public static SM_SYSTEM_MESSAGE STR_HOUSING_ENTER_NEED_HOUSE() {
		return new SM_SYSTEM_MESSAGE(1401359);
	}

	/**
	 * You are not authorized to enter.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_ENTER_NO_RIGHT2() {
		return new SM_SYSTEM_MESSAGE(1401364);
	}

	/**
	 * You cannot enter the house until it sells.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_ENTER_HAVE_TO_RECREATE() {
		return new SM_SYSTEM_MESSAGE(1401365);
	}

	/**
	 * Already listed. Please refresh your list.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_AUCTION_FAIL_ALREADY_REGISTED() {
		return new SM_SYSTEM_MESSAGE(1401372);
	}

	/**
	 * Right-click Tuali's Minion to remove the Sap Damage.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDElemental1_Prime_Debuff_Dispel() {
		return new SM_SYSTEM_MESSAGE(1401378);
	}

	/**
	 * Changed House Settings.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_ORDER_OPEN_DOOR() {
		return new SM_SYSTEM_MESSAGE(1401379);
	}

	/**
	 * Changed House Settings.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_ORDER_CLOSE_DOOR_WITHOUT_FRIENDS() {
		return new SM_SYSTEM_MESSAGE(1401380);
	}

	/**
	 * Changed House Settings.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_ORDER_CLOSE_DOOR_ALL() {
		return new SM_SYSTEM_MESSAGE(1401381);
	}

	/**
	 * Only Friends and Legion Members remain.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_ORDER_OUT_WITHOUT_FRIENDS() {
		return new SM_SYSTEM_MESSAGE(1401382);
	}

	/**
	 * All were kicked out.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_ORDER_OUT_ALL() {
		return new SM_SYSTEM_MESSAGE(1401383);
	}

	/**
	 * You were evicted by the house's owner.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_REQUEST_OUT() {
		return new SM_SYSTEM_MESSAGE(1401384);
	}

	/**
	 * The house's owner has changed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CHANGE_OWNER() {
		return new SM_SYSTEM_MESSAGE(1401385);
	}

	/**
	 * You already have the %0% and cannot reuse %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_USE_ALREADY_HAVE_REWARD_ITEM(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1401396, value0, value1);
	}

	/**
	 * The script is too long to apply here.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_SCRIPT_OVERFLOW() {
		return new SM_SYSTEM_MESSAGE(1401399);
	}

	/**
	 * You have removed the paint from %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_PAINT_REMOVE_SUCCEED(String value0) {
		return new SM_SYSTEM_MESSAGE(1401435, value0);
	}

	/**
	 * You have painted %0 with %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_PAINT_SUCCEED(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1401436, value0, value1);
	}

	/**
	 * You can only paint decor that you own.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_PAINT_ERROR_NOTOWNER() {
		return new SM_SYSTEM_MESSAGE(1401438);
	}

	/**
	 * You cannot paint this decor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_PAINT_ERROR_CANNOTPAINT() {
		return new SM_SYSTEM_MESSAGE(1401439);
	}

	/**
	 * This decor is yet to be painted.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_PAINT_ERROR_CANNOTREMOVE() {
		return new SM_SYSTEM_MESSAGE(1401440);
	}

	/**
	 * There is no need to pay a maintenance fee for this house.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_F2P_CASH_HOUSE_FEE_FREE() {
		return new SM_SYSTEM_MESSAGE(1401445);
	}

	/**
	 * &lt;p&gt;A Rift Portal battle has begun.&lt;/p&gt;
	 * &lt;p&gt;If the Rift Generator is destroyed or &lt;/p&gt;
	 * &lt;p&gt;if you leave the battlefield,&lt;/p&gt;
	 * &lt;p&gt;you will automatically return to your camp and be kicked out of the alliance.&lt;/p&gt;
	 * &lt;p&gt;&lt;/p&gt;
	 * &lt;p&gt;You will also quit the alliance if you return by using a skill or an item.&lt;/p&gt;
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INVADE_DIRECT_PORTAL_OPEN_NOTICE() {
		return new SM_SYSTEM_MESSAGE(1401454);
	}

	/**
	 * %0 is gone.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_OBJECT_DELETE_USE_COUNT_FINAL(String value0) {
		return new SM_SYSTEM_MESSAGE(1401470, value0);
	}

	/**
	 * You will be returned to where you entered.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INVADE_DIRECT_PORTAL_OUT_COMPULSION() {
		return new SM_SYSTEM_MESSAGE(1401474);
	}

	/**
	 * You cannot place this bid because the amount exceeds the bid limit.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_HOUSING_CANT_BID_EXCESS_ACCOUNT() {
		return new SM_SYSTEM_MESSAGE(1401497);
	}

	/**
	 * You haven't had any interactions recently.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NO_RELATIONSHIP_RECENTLY() {
		return new SM_SYSTEM_MESSAGE(1401504);
	}

	/**
	 * You have achieved %1 of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOWN_MISSION_COMPLETE(String town, String task) {
		return new SM_SYSTEM_MESSAGE(1401519, town, task);
	}

	/**
	 * %0 has reached level %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TOWN_LEVEL_LEVEL_UP(String town, int level) {
		return new SM_SYSTEM_MESSAGE(1401520, town, level);
	}

	/**
	 * Players from other zones cannot be invited into the Defense Alliance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANNOT_INVITE_DEFENSE_FORCE() {
		return new SM_SYSTEM_MESSAGE(1401527);
	}

	/**
	 * Enter the Internal Passage and destroy Tiamat's Incarnations while Kaisinel is dealing with Tiamat.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_TIAMAT_2PHASE_START_LIGHT() {
		return new SM_SYSTEM_MESSAGE(1401531);
	}

	/**
	 * Enter the Internal Passage and destroy Tiamat's Incarnations while Marchutan is dealing with Tiamat.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_TIAMAT_2PHASE_START_DARK() {
		return new SM_SYSTEM_MESSAGE(1401532);
	}

	/**
	 * Fissure Incarnate has collapsed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_TIAMAT_2PHASE_CLOSE_CRACK() {
		return new SM_SYSTEM_MESSAGE(1401533);
	}

	/**
	 * Wrath Incarnate has collapsed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_TIAMAT_2PHASE_CLOSE_RAGE() {
		return new SM_SYSTEM_MESSAGE(1401534);
	}

	/**
	 * Gravity Incarnate has collapsed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_TIAMAT_2PHASE_CLOSE_GRAVITY() {
		return new SM_SYSTEM_MESSAGE(1401535);
	}

	/**
	 * Petrification Incarnate has collapsed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_TIAMAT_2PHASE_CLOSE_CRYSTAL() {
		return new SM_SYSTEM_MESSAGE(1401536);
	}

	/**
	 * All of Tiamat's Incarnations have collapsed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_TIAMAT_2PHASE_CLOSE_ALL() {
		return new SM_SYSTEM_MESSAGE(1401537);
	}

	/**
	 * Empyrean Lord Kaisinel is attacking with all his might.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_KAISINEL_2PHASE_DEADLYATK() {
		return new SM_SYSTEM_MESSAGE(1401538);
	}

	/**
	 * Empyrean Lord Marchutan is attacking with all his might.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_MARCHUTAN_2PHASE_DEADLYATK() {
		return new SM_SYSTEM_MESSAGE(1401539);
	}

	/**
	 * Empyrean Lord Kaisinel is exhausted. You must take over the fight against Tiamat!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_KAISINEL_2PHASE_GROGGY() {
		return new SM_SYSTEM_MESSAGE(1401540);
	}

	/**
	 * Empyrean Lord Marchutan is exhausted. You must take over the fight against Tiamat!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_MARCHUTAN_2PHASE_GROGGY() {
		return new SM_SYSTEM_MESSAGE(1401541);
	}

	/**
	 * Dragon Lord Tiamat used its Death Roar to defeat the Empyrean Lord.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_TIAMAT_DEADLYHOWLING() {
		return new SM_SYSTEM_MESSAGE(1401542);
	}

	/**
	 * Calindi has absorbed the Surkana's magic to become even stronger!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDTIAMAT_KALYNDI_SURKANA_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1401543);
	}

	/**
	 * The battle with Tiamat will automatically end in 30 minutes.
	 */
	public static SM_SYSTEM_MESSAGE IDTIAMAT_TIAMAT_COUNTDOWN_START() {
		return new SM_SYSTEM_MESSAGE(1401547);
	}

	/**
	 * The Empyrean Lord needs more HP!
	 */
	public static SM_SYSTEM_MESSAGE IDTIAMAT_TIAMAT_GOD_HP_LOWER_THAN_50p() {
		return new SM_SYSTEM_MESSAGE(1401548);
	}

	/**
	 * The Empyrean Lord is getting low on HP. Don't let him die, or Tiamat will defeat you easily!
	 */
	public static SM_SYSTEM_MESSAGE IDTIAMAT_TIAMAT_GOD_HP_LOWER_THAN_15p() {
		return new SM_SYSTEM_MESSAGE(1401549);
	}

	/**
	 * Eliminate the Balaur Spiritualist to grant a beneficial effect to the Empyrean Lord.
	 */
	public static SM_SYSTEM_MESSAGE IDTIAMAT_TIAMAT_DRAKAN_BUFF_MSG() {
		return new SM_SYSTEM_MESSAGE(1401550);
	}

	/**
	 * The Empyrean Lord absorbed the Balaur Spiritualist's mental energy!
	 */
	public static SM_SYSTEM_MESSAGE IDTIAMAT_TIAMAT_DRAKAN_ON_DIE() {
		return new SM_SYSTEM_MESSAGE(1401551);
	}

	/**
	 * Tiamat's power has been enhanced with Siel's Relics. Tiamat can now reflect attacks.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDTIAMAT_TIAMAT_WARNING_MSG() {
		return new SM_SYSTEM_MESSAGE(1401553);
	}

	/**
	 * The Gravity Crusher was not destroyed in time and has become a Gravity Vortex.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDTIAMAT_TIAMAT_SPAWN_BLACKHOLE() {
		return new SM_SYSTEM_MESSAGE(1401554);
	}

	/**
	 * Tiamat has regained power and escaped to safety.
	 */
	public static SM_SYSTEM_MESSAGE IDTIAMAT_TIAMAT_COUNTDOWN_OVER() {
		return new SM_SYSTEM_MESSAGE(1401563);
	}

	/**
	 * %0 has captured %1. %2 has been activated.
	 */
	public static SM_SYSTEM_MESSAGE STR_CASTLE_WIN_BUFF_ON(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1401574, value0, value1, value2);
	}

	/**
	 * %0 did not manage to defend %1. %2 has been activated.
	 */
	public static SM_SYSTEM_MESSAGE STR_CASTLE_LOOSE_BUFF_ON(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1401575, value0, value1, value2);
	}

	/**
	 * You need the Emperor's Golden Tag to move.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_GOLD_MAP() {
		return new SM_SYSTEM_MESSAGE(1401579);
	}

	/**
	 * You need the Empress' Silver Tag to move.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_SILVER_MAP() {
		return new SM_SYSTEM_MESSAGE(1401580);
	}

	/**
	 * You need the Crown Prince's Brass Tag to move.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_BRONZE_MAP() {
		return new SM_SYSTEM_MESSAGE(1401581);
	}

	/**
	 * Pillagers incoming. Guard the Crown Prince's Monument!
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_S1_START() {
		return new SM_SYSTEM_MESSAGE(1401582);
	}

	/**
	 * Pillagers incoming. Guard the Empress' Monument!
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_S2_START() {
		return new SM_SYSTEM_MESSAGE(1401583);
	}

	/**
	 * Pillagers incoming. Guard the Emperor's Monument!
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_S3_START() {
		return new SM_SYSTEM_MESSAGE(1401584);
	}

	/**
	 * Take the treasure with the key you're carrying.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_TB_START() {
		return new SM_SYSTEM_MESSAGE(1401585);
	}

	/**
	 * A second wave of pillagers will arrive in 10 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_PHASE() {
		return new SM_SYSTEM_MESSAGE(1401586);
	}

	/**
	 * You need a key to open this box.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_TREASUREBOX() {
		return new SM_SYSTEM_MESSAGE(1401587);
	}

	/**
	 * More pillagers will arrive in 5 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_PHASE02() {
		return new SM_SYSTEM_MESSAGE(1401607);
	}

	/**
	 * A Balaur Medal Chest appeared in the Noble's Garden.
	 */
	public static SM_SYSTEM_MESSAGE STR_IDTIAMAT_TIAMAT_REWARD_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1401614);
	}

	/**
	 * Canceled tuning of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_IDENTIFY_CANCELED(String itemL10n) {
		return new SM_SYSTEM_MESSAGE(1401625, itemL10n);
	}

	/**
	 * Completing tuning of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_IDENTIFY_SUCCEED(String itemL10n) {
		return new SM_SYSTEM_MESSAGE(1401626, itemL10n);
	}

	/**
	 * %0 cannot retune %1.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_REIDENTIFY_WRONG_SELECT(String tuningScrollL10n, String targetItemL10n) {
		return new SM_SYSTEM_MESSAGE(1401633, tuningScrollL10n, targetItemL10n);
	}

	/**
	 * You cannot retune %1 because %0 has a lower rank.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_REIDENTIFY_WRONG_QUALITY(String tuningScrollL10n, String targetItemL10n) {
		return new SM_SYSTEM_MESSAGE(1401634, tuningScrollL10n, targetItemL10n);
	}

	/**
	 * You cannot retune %1 because %0 has a lower level.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_REIDENTIFY_WRONG_LEVEL(String tuningScrollL10n, String targetItemL10n) {
		return new SM_SYSTEM_MESSAGE(1401635, tuningScrollL10n, targetItemL10n);
	}

	/**
	 * %0 cannot be retuned.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_REIDENTIFY_CANNOT_REIDENTIFY(String itemL10n) {
		return new SM_SYSTEM_MESSAGE(1401636, itemL10n);
	}

	/**
	 * %0 has never been tuned.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_REIDENTIFY_DIDNT_IDENTIFY(String itemL10n) {
		return new SM_SYSTEM_MESSAGE(1401637, itemL10n);
	}

	/**
	 * Canceled tuning of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_REIDENTIFY_CANCELED(String itemL10n) {
		return new SM_SYSTEM_MESSAGE(1401638, itemL10n);
	}

	/**
	 * Completing tuning of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_REIDENTIFY_SUCCEED(String itemL10n) {
		return new SM_SYSTEM_MESSAGE(1401639, itemL10n);
	}

	/**
	 * The Idian level is too high for the selected item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_POLISH_WRONG_LEVEL() {
		return new SM_SYSTEM_MESSAGE(1401649);
	}

	/**
	 * %0's Idian is fully charged.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_POLISH_SUCCEED(String weaponL10n) {
		return new SM_SYSTEM_MESSAGE(1401650, weaponL10n);
	}

	/**
	 * The Idian charge on %0 ran out.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_POLISH_CHANGE_CONDITION_END(String weaponL10n) {
		return new SM_SYSTEM_MESSAGE(1401652, weaponL10n);
	}

	/**
	 * A third wave of pillagers will arrive in 10 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_PHASE03() {
		return new SM_SYSTEM_MESSAGE(1401664);
	}

	/**
	 * More pillagers will arrive in 5 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_PHASE04() {
		return new SM_SYSTEM_MESSAGE(1401665);
	}

	/**
	 * The final wave of pillagers will arrive in 10 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_PHASE09() {
		return new SM_SYSTEM_MESSAGE(1401670);
	}

	/**
	 * More pillagers will arrive in 5 seconds!
	 */
	public static SM_SYSTEM_MESSAGE STR_IDEVENT01_PHASE10() {
		return new SM_SYSTEM_MESSAGE(1401671);
	}

	/**
	 * Modor activated the Danuar Bomb of grudge. You have 15 minutes to defeat her.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5_INDER_RUNE_START() {
		return new SM_SYSTEM_MESSAGE(1401676);
	}

	/**
	 * 10 minutes elapsed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5_INDER_RUNE_10MIN() {
		return new SM_SYSTEM_MESSAGE(1401677);
	}

	/**
	 * The bomb has detonated.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5_INDER_RUNE_END() {
		return new SM_SYSTEM_MESSAGE(1401678);
	}

	/**
	 * This weapon has no power generator and cannot be boarded.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5B_TD_DEFWeapon() {
		return new SM_SYSTEM_MESSAGE(1401679);
	}

	/**
	 * You need a Cannon Starter to board this.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5B_TD_Tank() {
		return new SM_SYSTEM_MESSAGE(1401680);
	}

	/**
	 * Enchantment of %0 to +%num1 was successful.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ENCHANT_ITEM_SUCCEED_NEW(String value0, int num1) {
		return new SM_SYSTEM_MESSAGE(1401681, value0, num1);
	}

	/**
	 * %0 has been defeated and the number of points has been reduced from %1 by %num2.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LOSE_SCORE_ENEMY(String defeated, String faction, int points) {
		return new SM_SYSTEM_MESSAGE(1401721, defeated, faction, points);
	}

	/**
	 * Join the fight in the Kamar Battlefield.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_OPEN_IDKamar() {
		return new SM_SYSTEM_MESSAGE(1401730);
	}

	/**
	 * An enhancement with Idian is not possible until you have identified the equipment item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_POLISH_NEED_IDENTIFY() {
		return new SM_SYSTEM_MESSAGE(1401750);
	}

	/**
	 * Ide Resonators are charging the Hyperion.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDRUNEWP_CHARGING() {
		return new SM_SYSTEM_MESSAGE(1401790);
	}

	/**
	 * Phase 1 of the Ide energy charging complete.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDRUNEWP_CHARGER1_COMPLETED() {
		return new SM_SYSTEM_MESSAGE(1401791);
	}

	/**
	 * Phase 2 of the Ide energy charging complete.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDRUNEWP_CHARGER2_COMPLETED() {
		return new SM_SYSTEM_MESSAGE(1401792);
	}

	/**
	 * Phase 3 of the Ide energy charging complete.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDRUNEWP_CHARGER3_COMPLETED() {
		return new SM_SYSTEM_MESSAGE(1401793);
	}

	/**
	 * Phase 4 of the Ide energy charging complete. Hyperion ultimate attack imminent.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDRUNEWP_CHARGER4_COMPLETED() {
		return new SM_SYSTEM_MESSAGE(1401794);
	}

	/**
	 * The Hyperion's shields are faltering.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDRUNEWP_BROKENPROTECTION() {
		return new SM_SYSTEM_MESSAGE(1401795);
	}

	/**
	 * The Hyperion's shields are down.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDRUNEWP_BROKENPROTECTIONALL() {
		return new SM_SYSTEM_MESSAGE(1401796);
	}

	/**
	 * The Pashid Legion's 1st Siege Troop is attacking the Bastion's gates.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_MainWave_01() {
		return new SM_SYSTEM_MESSAGE(1401815);
	}

	/**
	 * The Pashid Legion's 2nd Siege Troop is attacking the Bastion's gates.<
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_MainWave_02() {
		return new SM_SYSTEM_MESSAGE(1401816);
	}

	/**
	 * The Pashid Legion's 3rd Siege Troop is attacking the Bastion's gates.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_MainWave_03() {
		return new SM_SYSTEM_MESSAGE(1401817);
	}

	/**
	 * The Pashid Legion's Sheban Siege Troop is attacking the Bastion's gates.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_MainWave_04() {
		return new SM_SYSTEM_MESSAGE(1401818);
	}

	/**
	 * Grand Commander Pashid has arrived with the Guard to assault the fortress.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_MainWave_05() {
		return new SM_SYSTEM_MESSAGE(1401819);
	}

	/**
	 * One of the assault machines is faltering and will collapse within the Eternal Bastion.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_AddWave_01() {
		return new SM_SYSTEM_MESSAGE(1401820);
	}

	/**
	 * Another assault machine has been hit and will crash within the Bastion's wall.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_AddWave_02() {
		return new SM_SYSTEM_MESSAGE(1401821);
	}

	/**
	 * Another assault machine has been hit and will crash within the Bastion's wall.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_AddWave_03() {
		return new SM_SYSTEM_MESSAGE(1401822);
	}

	/**
	 * The Pashid Legion is attacking the underground waterway.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_Notice_01() {
		return new SM_SYSTEM_MESSAGE(1401823);
	}

	/**
	 * The Pashid Legion has destroyed the gate at the underground wateray.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_Notice_02() {
		return new SM_SYSTEM_MESSAGE(1401824);
	}

	/**
	 * The Pashid Legion is attacking the Eternal Bastion's walls.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_Notice_03() {
		return new SM_SYSTEM_MESSAGE(1401825);
	}

	/**
	 * The Bastion has been breached. The Pashid Legion is flooding through the hole.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_Notice_04() {
		return new SM_SYSTEM_MESSAGE(1401826);
	}

	/**
	 * Reian Tribe supplies have been deposited in Peace Square.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_YunSupply_Spawn() {
		return new SM_SYSTEM_MESSAGE(1401840);
	}

	/**
	 * A Cannon has arrived in Peace Square.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_SeigeWeapon_Spawn() {
		return new SM_SYSTEM_MESSAGE(1401841);
	}

	/**
	 * The Dredgion has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_Dreadgion_Spawn() {
		return new SM_SYSTEM_MESSAGE(1401842);
	}

	/**
	 * The Dredgion is disgorging a massive number of troops.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_DrakanH_Spawn() {
		return new SM_SYSTEM_MESSAGE(1401843);
	}

	/**
	 * Commander Varga and his Deputy have arrived at the battle.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_DrakanGeneral_Spawn() {
		return new SM_SYSTEM_MESSAGE(1401844);
	}

	/**
	 * Commander Varga is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_DrakanGeneral_Hit() {
		return new SM_SYSTEM_MESSAGE(1401845);
	}

	/**
	 * Commander Varga has died.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_DrakanGeneral_Die() {
		return new SM_SYSTEM_MESSAGE(1401846);
	}

	/**
	 * Reinforcements for the Elyos and Asmodians have arrived.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_LightDarkGeneral_Spawn() {
		return new SM_SYSTEM_MESSAGE(1401847);
	}

	/**
	 * Acting Commander Crispin is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_LightGeneral_Hit() {
		return new SM_SYSTEM_MESSAGE(1401848);
	}

	/**
	 * Acting Commander Crispin has died.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_LightGeneral_Die() {
		return new SM_SYSTEM_MESSAGE(1401849);
	}

	/**
	 * Acting Commander Tepes is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_DarkGeneral_Hit() {
		return new SM_SYSTEM_MESSAGE(1401850);
	}

	/**
	 * Acting Commander Tepes has died.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_DarkGeneral_Die() {
		return new SM_SYSTEM_MESSAGE(1401851);
	}

	/**
	 * The Aetheric Cannon requires fuel before it can be operated.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKAMAR_CANT_USE_SEIGEWEAPON() {
		return new SM_SYSTEM_MESSAGE(1401854);
	}

	/**
	 * You spent %num0 Item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_USEITEM(int num0) {
		return new SM_SYSTEM_MESSAGE(1401873, num0);
	}

	/**
	 * The fully-charged Hyperion has disappeared into another dimension.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDRUNEWP_USER_KILL() {
		return new SM_SYSTEM_MESSAGE(1401909);
	}

	/**
	 * The returned results have been applied to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_REIDENTIFY_APPLY_YES(String itemL10n) {
		return new SM_SYSTEM_MESSAGE(1401910, itemL10n);
	}

	/**
	 * You aborted the action of applying returned results.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_REIDENTIFY_APPLY_NO() {
		return new SM_SYSTEM_MESSAGE(1401911);
	}

	/**
	 * Teleport Statues have appeared at the entrance to Kamar and the boarding site.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDKamar_StartTeleporter_Spawn() {
		return new SM_SYSTEM_MESSAGE(1401913);
	}

	/**
	 * The door to the Defiled Danuar Temple has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDVritra_Base_DoorOpen_01() {
		return new SM_SYSTEM_MESSAGE(1401914);
	}

	/**
	 * The door to the Danuar Meditation Garden has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDVritra_Base_DoorOpen_02() {
		return new SM_SYSTEM_MESSAGE(1401915);
	}

	/**
	 * The door to the Head Researcher's Office has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDVritra_Base_DoorOpen_03() {
		return new SM_SYSTEM_MESSAGE(1401916);
	}

	/**
	 * The door to the Lost Tree of Devotion has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDVritra_Base_DoorOpen_04() {
		return new SM_SYSTEM_MESSAGE(1401917);
	}

	/**
	 * The door to the Sauro Armory has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDVritra_Base_DoorOpen_05() {
		return new SM_SYSTEM_MESSAGE(1401918);
	}

	/**
	 * The door to the Heavy Storage Area has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDVritra_Base_DoorOpen_06() {
		return new SM_SYSTEM_MESSAGE(1401919);
	}

	/**
	 * The door to Moriata's Quarters has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDVritra_Base_DoorOpen_07() {
		return new SM_SYSTEM_MESSAGE(1401920);
	}

	/**
	 * A device leading to the Danuar Omphanium has been activated.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDVritra_Base_DoorOpen_08() {
		return new SM_SYSTEM_MESSAGE(1401921);
	}

	/**
	 * The passage to the Danuar Omphanium will be open for five minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDVritra_Base_DoorOpen_09() {
		return new SM_SYSTEM_MESSAGE(1401922);
	}

	/**
	 * %0 did not capture %1. %2 has been activated.
	 */
	public static SM_SYSTEM_MESSAGE STR_CASTLE_SIEGE_LOOSE_BUFF_ON(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1401930, value0, value1, value2);
	}

	/**
	 * %0 successfully defended %1. %2 is now active.
	 */
	public static SM_SYSTEM_MESSAGE STR_CASTLE_DEFENCE_WIN_BUFF_ON(String value0, String value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1401931, value0, value1, value2);
	}

	/**
	 * The Eternal Bastion defenders have withdrawn in preparation of Pashid's assault.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_MainWave_06() {
		return new SM_SYSTEM_MESSAGE(1401939);
	}

	/**
	 * The commander of the garrison has been killed. The assault force is no longer coordinated and is in retreat.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5b_TD_Notice_06() {
		return new SM_SYSTEM_MESSAGE(1401940);
	}

	/**
	 * You can now participate in the Ophidan Bridge battle.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_OPEN_IDLDF5_Under_01_War() {
		return new SM_SYSTEM_MESSAGE(1401947);
	}

	/**
	 * Supplies have been delivered to some of the sentry posts.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GUARDLIGHTHERO_SPAWN_IDLDF5_UNDER_01_WAR() {
		return new SM_SYSTEM_MESSAGE(1401965);
	}

	/**
	 * A hero and their reinforcements have been spotted at the starting point.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GuardDarkHeroTrigger_Spawn_IDLDF5_Under_01_War() {
		return new SM_SYSTEM_MESSAGE(1401968);
	}

	/**
	 * You can't open any private shops as long as you remain hidden.
	 */
	public static SM_SYSTEM_MESSAGE STR_PERSONAL_SHOP_DISABLED_IN_HIDDEN_MODE() {
		return new SM_SYSTEM_MESSAGE(1401969);
	}

	/**
	 * This sentry post has already been captured.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_Base_IDLDF5_Under_01_War_Flag02() {
		return new SM_SYSTEM_MESSAGE(1402007);
	}

	/**
	 * You cannot wrap %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PACK_ITEM_CANNOT(String value0) {
		return new SM_SYSTEM_MESSAGE(1402015, value0);
	}

	/**
	 * %0's level is lower than %1, so you cannot wrap it.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PACK_ITEM_WRONG_LEVEL(String value0, int levelRequired) {
		return new SM_SYSTEM_MESSAGE(1402016, value0, levelRequired);
	}

	/**
	 * %0's rank is lower than %1, so you cannot wrap it.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PACK_ITEM_WRONG_QUALITY(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1402017, value0, value1);
	}

	/**
	 * %1 cannot be wrapped with %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PACK_ITEM_WRONG_TARGET_ITEM_CATEGORY(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1402018, value0, value1);
	}

	/**
	 * A combined two-handed weapon item cannot be wrapped.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PACK_ITEM_WRONG_COMPOSITION() {
		return new SM_SYSTEM_MESSAGE(1402019);
	}

	/**
	 * You cannot wrap equipped items
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PACK_ITEM_WRONG_EQUIPED() {
		return new SM_SYSTEM_MESSAGE(1402020);
	}

	/**
	 * You cannot wrap sealed items.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PACK_ITEM_WRONG_SEAL() {
		return new SM_SYSTEM_MESSAGE(1402021);
	}

	/**
	 * A tradeable item cannot be wrapped.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PACK_ITEM_WRONG_EXCHANGE() {
		return new SM_SYSTEM_MESSAGE(1402022);
	}

	/**
	 * Cannot find the item to wrap.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PACK_ITEM_NO_TARGET_ITEM() {
		return new SM_SYSTEM_MESSAGE(1402029);
	}

	/**
	 * You must tune your equipment before wrapping.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PACK_ITEM_NEED_IDENTIFY() {
		return new SM_SYSTEM_MESSAGE(1402030);
	}

	/**
	 * You may participate in the Iron Wall Warfront.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_OPEN_IDF5_TD_war() {
		return new SM_SYSTEM_MESSAGE(1402032);
	}

	/**
	 * Wrapping of %0 is complete.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_PACK_ITEM_SUCCEED(String value0) {
		return new SM_SYSTEM_MESSAGE(1402031, value0);
	}

	/**
	 * You received %num0 Glory Points.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GLORY_POINT_GAIN(int additionalGp) {
		return new SM_SYSTEM_MESSAGE(1402081, additionalGp);
	}

	/**
	 * According to your current rank, you will lose a certain amount of Glory Points each day.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GLORY_POINT_LOSE_COMMON() {
		return new SM_SYSTEM_MESSAGE(1402082);
	}

	/**
	 * Supplies have been dropped in a confidential area.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5_Under_01_War_Drop_MSG_01() {
		return new SM_SYSTEM_MESSAGE(1402086);
	}

	/**
	 * The weakened protective shield will disappear in 30 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_GAME_TIMER_01() {
		return new SM_SYSTEM_MESSAGE(1402129);
	}

	/**
	 * The weakened protective shield will disappear in 25 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_GAME_TIMER_02() {
		return new SM_SYSTEM_MESSAGE(1402130);
	}

	/**
	 * The weakened protective shield will disappear in 20 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_GAME_TIMER_03() {
		return new SM_SYSTEM_MESSAGE(1402131);
	}

	/**
	 * The weakened protective shield will disappear in 15 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_GAME_TIMER_04() {
		return new SM_SYSTEM_MESSAGE(1402132);
	}

	/**
	 * The weakened protective shield will disappear in 10 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_GAME_TIMER_05() {
		return new SM_SYSTEM_MESSAGE(1402133);
	}

	/**
	 * The weakened protective shield will disappear in 5 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_GAME_TIMER_06() {
		return new SM_SYSTEM_MESSAGE(1402134);
	}

	/**
	 * The eastern shield power generator is charging.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_COMPLETE_01() {
		return new SM_SYSTEM_MESSAGE(1402135);
	}

	/**
	 * The western shield power generator is charging.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_COMPLETE_02() {
		return new SM_SYSTEM_MESSAGE(1402136);
	}

	/**
	 * The southern shield power generator is charging.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_COMPLETE_03() {
		return new SM_SYSTEM_MESSAGE(1402137);
	}

	/**
	 * The northern shield power generator is charging.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_COMPLETE_04() {
		return new SM_SYSTEM_MESSAGE(1402138);
	}

	/**
	 * The eastern shield power generator has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_DESTROY_01() {
		return new SM_SYSTEM_MESSAGE(1402139);
	}

	/**
	 * The western shield power generator has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_DESTROY_02() {
		return new SM_SYSTEM_MESSAGE(1402140);
	}

	/**
	 * The southern shield power generator has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_DESTROY_03() {
		return new SM_SYSTEM_MESSAGE(1402141);
	}

	/**
	 * The northern shield power generator has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_DESTROY_04() {
		return new SM_SYSTEM_MESSAGE(1402142);
	}

	/**
	 * The Test Weapon Dynatoum's bomb timer has begun its countdown.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_BOSS_TIMER_01() {
		return new SM_SYSTEM_MESSAGE(1402143);
	}

	/**
	 * Test Weapon Dynatoum will go off in 5 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_BOSS_TIMER_02() {
		return new SM_SYSTEM_MESSAGE(1402144);
	}

	/**
	 * Test Weapon Dynatoum will go off in 1 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_BOSS_TIMER_03() {
		return new SM_SYSTEM_MESSAGE(1402145);
	}

	/**
	 * Test Weapon Dynatoum has detonated.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_BOSS_TIMER_04() {
		return new SM_SYSTEM_MESSAGE(1402146);
	}

	/**
	 * You have canceled the tempering of %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_AUTHORIZE_CANCEL(String itemL10n) {
		return new SM_SYSTEM_MESSAGE(1402147, itemL10n);
	}

	/**
	 * You have successfully tempered %0. +%num1 temperance level achieved.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_AUTHORIZE_SUCCEEDED(String itemL10n, int temperingLevel) {
		return new SM_SYSTEM_MESSAGE(1402148, itemL10n, temperingLevel);
	}

	/**
	 * Tempering of %0 has failed and the temperance level has decreased to 0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_AUTHORIZE_FAILED(String itemL10n) {
		return new SM_SYSTEM_MESSAGE(1402149, itemL10n);
	}

	/**
	 * %0 has succeeded in tempering %1 to level %2.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_AUTHORIZE_SUCCEEDED_MAX(String playerName, String value1, int num2) {
		return new SM_SYSTEM_MESSAGE(1402154, playerName, value1, num2);
	}

	/**
	 * You can now participate in the Idgel Dome battle.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_INSTANCE_OPEN_IDLDF5_Fortress_Re() {
		return new SM_SYSTEM_MESSAGE(1402192);
	}

	/**
	 * The entrance to the Illuminary Obelisk has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_DOOR_OPEN() {
		return new SM_SYSTEM_MESSAGE(1402193);
	}

	/**
	 * The eastern power generator is charging.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_CHARGE_01() {
		return new SM_SYSTEM_MESSAGE(1402194);
	}

	/**
	 * The western power generator is charging.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_CHARGE_02() {
		return new SM_SYSTEM_MESSAGE(1402195);
	}

	/**
	 * The southern power generator is charging.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_CHARGE_03() {
		return new SM_SYSTEM_MESSAGE(1402196);
	}

	/**
	 * The northern power generator is charging.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_CHARGE_04() {
		return new SM_SYSTEM_MESSAGE(1402197);
	}

	/**
	 * The eastern power generator will finish charging in 30 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_FINAL_CHARGE_01() {
		return new SM_SYSTEM_MESSAGE(1402198);
	}

	/**
	 * The western power generator will finish charging in 30 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_FINAL_CHARGE_02() {
		return new SM_SYSTEM_MESSAGE(1402199);
	}

	/**
	 * The southern power generator will finish charging in 30 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_FINAL_CHARGE_03() {
		return new SM_SYSTEM_MESSAGE(1402200);
	}

	/**
	 * The northern power generator will finish charging in 30 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_FINAL_CHARGE_04() {
		return new SM_SYSTEM_MESSAGE(1402201);
	}

	/**
	 * The shield is activated and the Pashid Destruction Unit is retreating. The Shield Control Room Teleporter has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_ALL_COMPLETE() {
		return new SM_SYSTEM_MESSAGE(1402202);
	}

	/**
	 * This power generator is fully charged. It cannot be charged any further.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_CHARGE_END() {
		return new SM_SYSTEM_MESSAGE(1402203);
	}

	/**
	 * The amount of Glory Points to be deducted from you, %0, are: %1%[%gchar:glory_point].
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GLORY_POINT_LOSE_PERSONAL(String playerName, int gpLoss) {
		return new SM_SYSTEM_MESSAGE(1402209, playerName, gpLoss);
	}

	/**
	 * You need a Crystalline Idium Piece to charge the generator.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_OBJ_NOITEM() {
		return new SM_SYSTEM_MESSAGE(1402211);
	}

	/**
	 * The Dynatoum has destroyed the teleport device of the shield generation hub.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_BOSS_PORTAL_DESTROY() {
		return new SM_SYSTEM_MESSAGE(1402212);
	}

	/**
	 * %num0 Glory Points have been deducted from you.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GLORY_POINT_LOSE(int num0) {
		return new SM_SYSTEM_MESSAGE(1402219, num0);
	}

	/**
	 * The eastern power shield generator is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_DEFENCE_01_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402220);
	}

	/**
	 * The western power shield generator is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_DEFENCE_02_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402221);
	}

	/**
	 * The southern power shield generator is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_DEFENCE_03_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402222);
	}

	/**
	 * The northern power shield generator is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_DEFENCE_04_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402223);
	}

	/**
	 * An Abyss Gate has opened near the eastern power shield generator. Infiltration by Pashid Destruction Unit is underway.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_N_WAVE_01_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1402224);
	}

	/**
	 * An Abyss Gate has opened near the western power shield generator. Infiltration by Pashid Destruction Unit is underway.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_N_WAVE_02_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1402225);
	}

	/**
	 * An Abyss Gate has opened near the southern power shield generator. Infiltration by Pashid Destruction Unit is underway.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_N_WAVE_03_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1402226);
	}

	/**
	 * An Abyss Gate has opened near the northern power shield generator. Infiltration by Pashid Destruction Unit is underway.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_N_WAVE_04_BEGIN() {
		return new SM_SYSTEM_MESSAGE(1402227);
	}

	/**
	 * The weakened protective shield will disappear in 1 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_GAME_TIMER_07() {
		return new SM_SYSTEM_MESSAGE(1402235);
	}

	/**
	 * The protective shield covering the Illuminary Obelisk has disappeared. The Pashid Destruction Unit's intense bombing commences.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_GAME_TIMER_08() {
		return new SM_SYSTEM_MESSAGE(1402236);
	}

	/**
	 * The %1 on %0 has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BREAK_PROC(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1402237, value0, value1);
	}

	/**
	 * %0's Godstone Socketing cancelled.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GIVE_PROC_CANCEL(String value0) {
		return new SM_SYSTEM_MESSAGE(1402238, value0);
	}

	/**
	 * Unable to receive due to lack of free space in inventory.
	 */
	public static SM_SYSTEM_MESSAGE STR_MAIL_TAKE_ALL_CANCEL() {
		return new SM_SYSTEM_MESSAGE(1402251);
	}

	/**
	 * Loading the Advance Corridor Shield... Please wait.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_01() {
		return new SM_SYSTEM_MESSAGE(1402252);
	}

	/**
	 * The entrance to the Transidium Annex will open in 8 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_02() {
		return new SM_SYSTEM_MESSAGE(1402253);
	}

	/**
	 * The entrance to the Transidium Annex will open in 6 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_03() {
		return new SM_SYSTEM_MESSAGE(1402254);
	}

	/**
	 * The entrance to the Transidium Annex will open in 4 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_04() {
		return new SM_SYSTEM_MESSAGE(1402255);
	}

	/**
	 * The entrance to the Transidium Annex will open in 2 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_05() {
		return new SM_SYSTEM_MESSAGE(1402256);
	}

	/**
	 * The entrance to the Transidium Annex will open in 1 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_06() {
		return new SM_SYSTEM_MESSAGE(1402257);
	}

	/**
	 * Chariot Hangar I Controller is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_TANK_A_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402258);
	}

	/**
	 * Chariot Hangar II Controller is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_TANK_B_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402259);
	}

	/**
	 * Ignus Engine Hangar I Controller is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_TANK_C_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402260);
	}

	/**
	 * Ignus Engine Hangar II Controller is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_TANK_D_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402261);
	}

	/**
	 * Chariot Hangar I Controller has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_TANK_A_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1402262);
	}

	/**
	 * Chariot Hangar II Controller has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_TANK_B_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1402263);
	}

	/**
	 * Ignus Engine Hangar I Controller has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_TANK_C_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1402264);
	}

	/**
	 * Ignus Engine Hangar II Controller has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_TANK_D_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1402265);
	}

	/**
	 * The Belus Advance Corridor Shield is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_PORTAL_DEST_69_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402266);
	}

	/**
	 * The Aspida Advance Corridor Shield is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_PORTAL_DEST_70_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402267);
	}

	/**
	 * The Atanatos Advance Corridor Shield is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_PORTAL_DEST_71_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402268);
	}

	/**
	 * The Disillon Advance Corridor Shield is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_PORTAL_DEST_72_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402269);
	}

	/**
	 * The Belus Advance Corridor Shield has been destroyed. The Daevas from the Belus camp have returned to the Arcadian Fortress.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_PORTAL_DEST_69_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1402270);
	}

	/**
	 * The Aspida Advance Corridor Shield is under attack. The Daevas from the Aspida camp have returned to the Umbral Fortress.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_PORTAL_DEST_70_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1402271);
	}

	/**
	 * The Atanatos Advance Corridor Shield is under attack. The Daevas from the Atanatos camp have returned to the Eternum Fortress.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_PORTAL_DEST_71_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1402272);
	}

	/**
	 * The Disillon Advance Corridor Shield has been destroyed. The Daevas from the Disillon camp have returned to the Skyclash Fortress.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_PORTAL_DEST_72_BROKEN() {
		return new SM_SYSTEM_MESSAGE(1402273);
	}

	/**
	 * %0 has succeeded in enchanting %1 to Level 20.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ENCHANT_ITEM_SUCCEEDED_20(String playerName, String value1) {
		return new SM_SYSTEM_MESSAGE(1402285, playerName, value1);
	}

	/**
	 * Destroyer Kunax has appeared in the Slaying Arena
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORTRESS_RE_BOSSSPAWN() {
		return new SM_SYSTEM_MESSAGE(1402367);
	}

	/**
	 * The Asmodian Flame Vent has been activated.\nThe Asmodians are trapped!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORTRESS_RE_FIRESPAWN_A() {
		return new SM_SYSTEM_MESSAGE(1402368);
	}

	/**
	 * The Elyos Flame Vent has been activated.\nThe Elyos are trapped!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_FORTRESS_RE_FIRESPAWN_B() {
		return new SM_SYSTEM_MESSAGE(1402369);
	}

	/**
	 * The Beritra Legion's Invasion Corridor has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WORLDRAID_MESSAGE_01() {
		return new SM_SYSTEM_MESSAGE(1402383);
	}

	/**
	 * The Devil Unit has infiltrated through the Invasion Corridor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WORLDRAID_MESSAGE_02() {
		return new SM_SYSTEM_MESSAGE(1402384);
	}

	/**
	 * The Devil Unit is preparing for its return.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WORLDRAID_MESSAGE_03() {
		return new SM_SYSTEM_MESSAGE(1402385);
	}

	/**
	 * The Devil Unit has returned through the Invasion Corridor.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WORLDRAID_MESSAGE_04() {
		return new SM_SYSTEM_MESSAGE(1402386);
	}

	/**
	 * The Devil Unit's Magno has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WORLDRAID_MESSAGE_DIE_01() {
		return new SM_SYSTEM_MESSAGE(1402387);
	}

	/**
	 * The Devil Unit's Tarmat Beta has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WORLDRAID_MESSAGE_DIE_02() {
		return new SM_SYSTEM_MESSAGE(1402388);
	}

	/**
	 * The Devil Unit's Initiator has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WORLDRAID_MESSAGE_DIE_03() {
		return new SM_SYSTEM_MESSAGE(1402389);
	}

	/**
	 * The Devil Unit's Tumon has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WORLDRAID_MESSAGE_DIE_04() {
		return new SM_SYSTEM_MESSAGE(1402390);
	}

	/**
	 * The Devil Unit's Raedon Beta has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WORLDRAID_MESSAGE_DIE_05() {
		return new SM_SYSTEM_MESSAGE(1402391);
	}

	/**
	 * The Devil Unit's Benoid has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WORLDRAID_MESSAGE_DIE_06() {
		return new SM_SYSTEM_MESSAGE(1402392);
	}

	/**
	 * It is not ready yet. Please wait.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_CANT_READY_PANGAEA() {
		return new SM_SYSTEM_MESSAGE(1402395);
	}

	/**
	 * Purification System
	 */
	public static SM_SYSTEM_MESSAGE STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT(String value0) {
		return new SM_SYSTEM_MESSAGE(1402397, value0);
	}

	/**
	 * An Advance Corridor to a Rift Portal battle has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SVS_INVADE_DIRECT_PORTAL_OPEN() {
		return new SM_SYSTEM_MESSAGE(1402399);
	}

	/**
	 * Players of your level cannot use the Advance Corridor.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_SVS_DIRECT_PORTAL_LEVEL_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1402400);
	}

	/**
	 * The Advanced Corridor is full. Please Wait.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_SVS_DIRECT_PORTAL_USE_COUNT_LIMIT() {
		return new SM_SYSTEM_MESSAGE(1402401);
	}

	/**
	 * &lt;p&gt;You have moved through the Advance Corridor.&lt;/p&gt;
	 * &lt;p&gt;If the Advance Corridor Generator is destroyed,&lt;/p&gt;
	 * &lt;p&gt;you will be returned to your previous location.&lt;/p&gt;
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_SVS_DIRECT_PORTAL_OPEN_NOTICE() {
		return new SM_SYSTEM_MESSAGE(1402418);
	}

	/**
	 * You cannot register unidentified items.
	 */
	public static SM_SYSTEM_MESSAGE STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT_NO_IDENTIFY() {
		return new SM_SYSTEM_MESSAGE(1402421);
	}

	/**
	 * The Remodeled Dynatoum bomb has begun counting down.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_HARD_BOSS_TIMER_01() {
		return new SM_SYSTEM_MESSAGE(1402425);
	}

	/**
	 * The Remodeled Dynatoum will explode in 5 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_HARD_BOSS_TIMER_02() {
		return new SM_SYSTEM_MESSAGE(1402426);
	}

	/**
	 * The Remodeled Dynatoum will explode in 1 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_HARD_BOSS_TIMER_03() {
		return new SM_SYSTEM_MESSAGE(1402427);
	}

	/**
	 * The Remodeled Dynatoum is going to explode.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_HARD_BOSS_TIMER_04() {
		return new SM_SYSTEM_MESSAGE(1402428);
	}

	/**
	 * The Remodeled Dynatoum has destroyed the shield generator teleporter.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDF5_U3_HARD_BOSS_PORTAL_DESTROY() {
		return new SM_SYSTEM_MESSAGE(1402429);
	}

	/**
	 * You have entered the Linkgate Foundry. Monsters in the lab, except Belsagos, will disappear in 20 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF4_Re_01_Time_01() {
		return new SM_SYSTEM_MESSAGE(1402453);
	}

	/**
	 * Monsters in the lab, except Belsagos, will disappear in 15 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF4_Re_01_Time_02() {
		return new SM_SYSTEM_MESSAGE(1402454);
	}

	/**
	 * Monsters in the lab, except Belsagos, will disappear in 10 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF4_Re_01_Time_03() {
		return new SM_SYSTEM_MESSAGE(1402455);
	}

	/**
	 * Monsters in the lab, except Belsagos, will disappear in 5 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF4_Re_01_Time_04() {
		return new SM_SYSTEM_MESSAGE(1402456);
	}

	/**
	 * Monsters in the lab, except Belsagos, will disappear in 3 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF4_Re_01_Time_05() {
		return new SM_SYSTEM_MESSAGE(1402457);
	}

	/**
	 * Monsters in the lab, except Belsagos, will disappear in 1 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF4_Re_01_Time_06() {
		return new SM_SYSTEM_MESSAGE(1402458);
	}

	/**
	 * Invasion of the Beritra Army
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WORLDRAID_INVADE_VRITRA() {
		return new SM_SYSTEM_MESSAGE(1402459);
	}

	/**
	 * Great Invasion of the Beritra Army
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WORLDRAID_INVADE_VRITRA_SPECIAL() {
		return new SM_SYSTEM_MESSAGE(1402460);
	}

	/**
	 * All monsters except Belsagos have disappeared from the Linkgate Foundry.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF4_Re_01_Time_07() {
		return new SM_SYSTEM_MESSAGE(1402461);
	}

	/**
	 * Tempering %0 has failed, and the item has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ITEM_AUTHORIZE_FAILED_TSHIRT(String plumeL10n) {
		return new SM_SYSTEM_MESSAGE(1402447, plumeL10n);
	}

	/**
	 * Release Berserk Anoha
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF5_FORTRESS_NAMED_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1402483);
	}

	/**
	 * The Anoha Sealing Stone was used to release Anoha.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF5_FORTRESS_NAMED_SPAWN_ITEM() {
		return new SM_SYSTEM_MESSAGE(1402484);
	}

	/**
	 * Summon Berserk Anoha
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ANOHA_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1402503);
	}

	/**
	 * Berserk Anoha has been defeated.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ANOHA_DIE() {
		return new SM_SYSTEM_MESSAGE(1402504);
	}

	/**
	 * Berserk Anoha has disappeared
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ANOHA_DESPAWN() {
		return new SM_SYSTEM_MESSAGE(1402505);
	}

	/**
	 * A battle is raging in the West Picket.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V01() {
		return new SM_SYSTEM_MESSAGE(1402506);
	}

	/**
	 * A battle is raging in the North Picket.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V02() {
		return new SM_SYSTEM_MESSAGE(1402507);
	}

	/**
	 * A battle is raging in the North Outpost.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V03() {
		return new SM_SYSTEM_MESSAGE(1402508);
	}

	/**
	 * A battle is raging in the North Relay.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V04() {
		return new SM_SYSTEM_MESSAGE(1402509);
	}

	/**
	 * A battle is raging in the East Relay.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V05() {
		return new SM_SYSTEM_MESSAGE(1402510);
	}

	/**
	 * A battle is raging in the East Outpost.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V06() {
		return new SM_SYSTEM_MESSAGE(1402511);
	}

	/**
	 * A battle is raging in the East Picket.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V07() {
		return new SM_SYSTEM_MESSAGE(1402512);
	}

	/**
	 * A battle is raging in the South Picket.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V08() {
		return new SM_SYSTEM_MESSAGE(1402513);
	}

	/**
	 * A battle is raging in the South Outpost.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V09() {
		return new SM_SYSTEM_MESSAGE(1402514);
	}

	/**
	 * A battle is raging in the West Relay.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V10() {
		return new SM_SYSTEM_MESSAGE(1402515);
	}

	/**
	 * A battle is raging in the West Outpost.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V11() {
		return new SM_SYSTEM_MESSAGE(1402516);
	}

	/**
	 * A battle is raging in the South Relay.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V12() {
		return new SM_SYSTEM_MESSAGE(1402517);
	}

	/**
	 * A battle is raging in the Shrine.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_CHIEF_V13() {
		return new SM_SYSTEM_MESSAGE(1402518);
	}

	/**
	 * Assassins have been spotted in the West Picket.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V01() {
		return new SM_SYSTEM_MESSAGE(1402519);
	}

	/**
	 * Assassins have been spotted in the North Picket.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V02() {
		return new SM_SYSTEM_MESSAGE(1402520);
	}

	/**
	 * Assassins have been spotted in the North Outpost.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V03() {
		return new SM_SYSTEM_MESSAGE(1402521);
	}

	/**
	 * Special elite assassins have been spotted in the North Relay.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V04() {
		return new SM_SYSTEM_MESSAGE(1402522);
	}

	/**
	 * Assassins have been spotted in the East Relay.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V05() {
		return new SM_SYSTEM_MESSAGE(1402523);
	}

	/**
	 * Assassins have been spotted in the East Outpost.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V06() {
		return new SM_SYSTEM_MESSAGE(1402524);
	}

	/**
	 * Assassins have been spotted in the East Picket.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V07() {
		return new SM_SYSTEM_MESSAGE(1402525);
	}

	/**
	 * Assassins have been spotted in the South Picket.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V08() {
		return new SM_SYSTEM_MESSAGE(1402526);
	}

	/**
	 * Assassins have been spotted in the South Outpost.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V09() {
		return new SM_SYSTEM_MESSAGE(1402527);
	}

	/**
	 * Assassins have been spotted in the West Relay.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V10() {
		return new SM_SYSTEM_MESSAGE(1402528);
	}

	/**
	 * Assassins have been spotted in the West Outpost.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V11() {
		return new SM_SYSTEM_MESSAGE(1402529);
	}

	/**
	 * Special elite assassins have been spotted in the South Relay.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V12() {
		return new SM_SYSTEM_MESSAGE(1402530);
	}

	/**
	 * Special elite assassins have been spotted in the Shrine.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_KILLER_V13() {
		return new SM_SYSTEM_MESSAGE(1402531);
	}

	/**
	 * You need Ancestor's Relics to activate the Vocalith.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_FNAMED_FAIL() {
		return new SM_SYSTEM_MESSAGE(1402539);
	}

	/**
	 * An Ancient Monster has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_FNAMED_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1402540);
	}

	/**
	 * You used 1 Ancestor's Relic.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_FNAMED_SPAWN_ITEM() {
		return new SM_SYSTEM_MESSAGE(1402541);
	}

	/**
	 * %1 equipped in %0 was fractured. %1 will be destroyed in 10 minutes even if it is unequipped.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BREAK_PROC_REMAIN_START(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1402536, value0, value1);
	}

	/**
	 * The %1 equipped in %0 will be destroyed in %2 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BREAK_PROC_REMAIN_MIN(String value0, String value1, int minutes) {
		return new SM_SYSTEM_MESSAGE(1402537, value0, value1, minutes);
	}

	/**
	 * The %1 equipped in %0 will be destroyed in %2 seconds.2
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_BREAK_PROC_REMAIN_SEC(String value0, String value1, int seconds) {
		return new SM_SYSTEM_MESSAGE(1402538, value0, value1, seconds);
	}

	/**
	 * The Agent has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_Advance_GodElite() {
		return new SM_SYSTEM_MESSAGE(1402543);
	}

	/**
	 * The Agent battle will start in 10 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_GODELITE_TIME_01() {
		return new SM_SYSTEM_MESSAGE(1402544);
	}

	/**
	 * The Agent battle will start in 5 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_GODELITE_TIME_02() {
		return new SM_SYSTEM_MESSAGE(1402545);
	}

	/**
	 * The Agent battle has ended.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF4_ADVANCE_GODELITE_TIME_03() {
		return new SM_SYSTEM_MESSAGE(1402546);
	}

	/**
	 * %0 sold the item automatically.
	 */
	public static AionServerPacket STR_MSG_MERCHANT_PET_GET_SELL_ITEM(String name) {
		return new SM_SYSTEM_MESSAGE(1402570, name);
	}

	/**
	 * You do not have enough Abyss Points to perform an Equipment Blessing.
	 */
	public static SM_SYSTEM_MESSAGE STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT_NEED_AP() {
		return new SM_SYSTEM_MESSAGE(1402571);
	}

	/**
	 * You do not have enough Kinah to perform an Equipment Blessing.
	 */
	public static SM_SYSTEM_MESSAGE STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT_NEED_QINA() {
		return new SM_SYSTEM_MESSAGE(1402572);
	}

	/**
	 * You obtained %1% from the Equipment Blessing for %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_ITEM_UPGRADE_MSG_UPGRADE_SUCCESS(String value0, String value1) {
		return new SM_SYSTEM_MESSAGE(1402579, value0, value1);
	}

	/**
	 * Berserk Anoha will return to Kaldor in 30 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LDF5_FORTRESS_NAMED_SPAWN_SYSTEM() {
		return new SM_SYSTEM_MESSAGE(1402584);
	}

	/**
	 * The entrance to the Transidium Annex will open in 30 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_07() {
		return new SM_SYSTEM_MESSAGE(1402586);
	}

	/**
	 * The entrance to the Transidium Annex has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_08() {
		return new SM_SYSTEM_MESSAGE(1402587);
	}

	/**
	 * The power of Kaisinel's Protection surrounds you.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WEAK_RACE_BUFF_LIGHT_GAIN() {
		return new SM_SYSTEM_MESSAGE(1402588);
	}

	/**
	 * You are no longer under Kaisinel's Protection.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WEAK_RACE_BUFF_LIGHT_GET_OUT_AREA() {
		return new SM_SYSTEM_MESSAGE(1402589);
	}

	/**
	 * Kaisinel's Protection has faded because the fortress is invulnerable.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WEAK_RACE_BUFF_LIGHT_MIST_OFF() {
		return new SM_SYSTEM_MESSAGE(1402590);
	}

	/**
	 * Kaisinel's Protection has strengthened the opposing faction.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WEAK_RACE_BUFF_LIGHT_WARNING() {
		return new SM_SYSTEM_MESSAGE(1402591);
	}

	/**
	 * The power of Marchutan's Protection surrounds you.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WEAK_RACE_BUFF_DARK_GAIN() {
		return new SM_SYSTEM_MESSAGE(1402592);
	}

	/**
	 * You are no longer under Marchutan's Protection.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WEAK_RACE_BUFF_DARK_GET_OUT_AREA() {
		return new SM_SYSTEM_MESSAGE(1402593);
	}

	/**
	 * Marchutan's Protection has faded because the fortress is invulnerable.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WEAK_RACE_BUFF_DARK_MIST_OFF() {
		return new SM_SYSTEM_MESSAGE(1402594);
	}

	/**
	 * Marchutan's Protection has strengthened the opposing faction.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_WEAK_RACE_BUFF_DARK_WARNING() {
		return new SM_SYSTEM_MESSAGE(1402595);
	}

	/**
	 * Destroyer Kunax has spawned.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5_FORTRESS_RE_BOSS_SPAWN() {
		return new SM_SYSTEM_MESSAGE(1402598);
	}

	/**
	 * Atreian Passport
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NEW_PASSPORT_AVAIBLE() {
		return new SM_SYSTEM_MESSAGE(1402601);
	}

	/**
	 * The Secret Lab's location was revealed three times. Nothing remains in the lab and the researchers have fled.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF4_Re_01_secret_room_03() {
		return new SM_SYSTEM_MESSAGE(1402603);
	}

	/**
	 * The Advance Corridor Shield has been activated.
	 * If the protection device is destroyed, the corridor will disappear and you will return to the fortress.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_09() {
		return new SM_SYSTEM_MESSAGE(1402637);
	}

	/**
	 * The effect of the Transidium Annex has weakened the Hangar Barricade.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_10() {
		return new SM_SYSTEM_MESSAGE(1402638);
	}

	/**
	 * The effect of the Transidium Annex has weakened the Ahserion's Flight Barrier.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_11() {
		return new SM_SYSTEM_MESSAGE(1402639);
	}

	/**
	 * The effect of the Transidium Annex has weakened the Bulwark Shield.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_12() {
		return new SM_SYSTEM_MESSAGE(1402640);
	}

	/**
	 * The Advance Corridor Shield will disappear soon.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_13() {
		return new SM_SYSTEM_MESSAGE(1402641);
	}

	/**
	 * You will return to the fortress soon.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_SUB_ALARM_14() {
		return new SM_SYSTEM_MESSAGE(1402642);
	}

	/**
	 * One Fortress Soul Anchor has been destroyed.
	 * If one more Fortress Soul Anchor is destroyed, then you can no longer resurrect inside the fortress.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_REBIRTHMA_DE_01() {
		return new SM_SYSTEM_MESSAGE(1402643);
	}

	/**
	 * All of the Fortress Soul Anchors have been destroyed.
	 * Players defending the fortress will return to where they entered Panesterra when resurrected.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_REBIRTHMA_DE_02() {
		return new SM_SYSTEM_MESSAGE(1402644);
	}

	/**
	 * The Northwest Quarter Soul Anchor has been destroyed.
	 * Those players bound to the destroyed Sanctum Soul Anchor will return to the place where they came from when resurrected.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_REBIRTHMA_ATT_01() {
		return new SM_SYSTEM_MESSAGE(1402645);
	}

	/**
	 * The Northeast Quarter Soul Anchor has been destroyed.
	 * Those players bound to the destroyed Sanctum Soul Anchor will return to the place where they came from when resurrected.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_REBIRTHMA_ATT_02() {
		return new SM_SYSTEM_MESSAGE(1402646);
	}

	/**
	 * The Southeast Quarter Soul Anchor has been destroyed.
	 * Those players bound to the destroyed Sanctum Soul Anchor will return to the place where they came from when resurrected.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_REBIRTHMA_ATT_03() {
		return new SM_SYSTEM_MESSAGE(1402647);
	}

	/**
	 * The Southwest Quarter Soul Anchor has been destroyed.
	 * Those players bound to the destroyed Sanctum Soul Anchor will return to the place where they came from when resurrected.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAB1_REBIRTHMA_ATT_04() {
		return new SM_SYSTEM_MESSAGE(1402648);
	}

	/**
	 * The Fortress Soul Anchor Barrier is under attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GAV1_REBIRTHDOOR_ATTACKED() {
		return new SM_SYSTEM_MESSAGE(1402649);
	}

	/**
	 * Amplification is not available for %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EXCEED_CANNOT_01(String value0) {
		return new SM_SYSTEM_MESSAGE(1402650, value0);
	}

	/**
	 * Must be at maximum enchantment level to enable amplification.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EXCEED_CANNOT_02() {
		return new SM_SYSTEM_MESSAGE(1402651);
	}

	/**
	 * Unable to find a qualifying item for amplification.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EXCEED_NO_TARGET_ITEM() {
		return new SM_SYSTEM_MESSAGE(1402655);
	}

	/**
	 * Amplification already applied to this item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EXCEED_ALREADY() {
		return new SM_SYSTEM_MESSAGE(1402656);
	}

	/**
	 * Successfully amplified %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EXCEED_SUCCEED(String value0) {
		return new SM_SYSTEM_MESSAGE(1402657, value0);
	}

	/**
	 * %0 cannot be used to enchant an amplified item.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EXCEED_CANNOT_02(String value0) {
		return new SM_SYSTEM_MESSAGE(1402661, value0);
	}

	/**
	 * %0 successfully enchanted to level %1. The %2 skill has been added.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_EXCEED_SKILL_ENCHANT(String value0, int num1, String value2) {
		return new SM_SYSTEM_MESSAGE(1402662, value0, num1, value2);
	}

	/**
	 * The boost for %0 failed. The item was destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_ENCHANT_TYPE1_ENCHANT_FAIL(String value0) {
		return new SM_SYSTEM_MESSAGE(1402674, value0);
	}

	/**
	 * All the intruders have fled. You've cleared the Vault!
	 */
	public static SM_SYSTEM_MESSAGE STR_IDSweep_Stage2_End() {
		return new SM_SYSTEM_MESSAGE(1402681);
	}

	/**
	 * The Lava Protector and Heatvent Protector are sharing the Fount.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_TWIN_01() {
		return new SM_SYSTEM_MESSAGE(1402682);
	}

	/**
	 * When both Protectors are defeated at the same time, the Fount is destroyed and the Protectors can no longer resurrect.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_TWIN_02() {
		return new SM_SYSTEM_MESSAGE(1402683);
	}

	/**
	 * If the Protectors are not defeated in 5 minutes, the Detachment's Rush Squad will sacrifice themselves to destroy the Fount.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_TWIN_03() {
		return new SM_SYSTEM_MESSAGE(1402684);
	}

	/**
	 * In 1 minute, the Detachment's Rush Squad will resolve to sacrifice themselves and attempt to destroy the Fount.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_TWIN_04() {
		return new SM_SYSTEM_MESSAGE(1402685);
	}

	/**
	 * In a moment, the Detachment's Rush Squad, armed with the resolve to sacrifice themselves, will attack the Fount.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_TWIN_05() {
		return new SM_SYSTEM_MESSAGE(1402686);
	}

	/**
	 * Thanks to the sacrifice of the Detachment's Rush Squad, the Protectors' Fount has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_TWIN_06() {
		return new SM_SYSTEM_MESSAGE(1402687);
	}

	/**
	 * The Protectors' Fount has been destroyed and they will not be resurrected.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_TWIN_07() {
		return new SM_SYSTEM_MESSAGE(1402688);
	}

	/**
	 * Detachment Demolisher has opened the path to the next area.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_TWIN_08() {
		return new SM_SYSTEM_MESSAGE(1402689);
	}

	/**
	 * The Protector that shares the Fount is still alive.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_TWIN_RESSURECT_01() {
		return new SM_SYSTEM_MESSAGE(1402690);
	}

	/**
	 * The vanquished Protector will be resurrected by the power of the Fount in 15 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_TWIN_RESSURECT_02() {
		return new SM_SYSTEM_MESSAGE(1402691);
	}

	/**
	 * The Protector that shares the Fount has successfully resurrected.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_TWIN_RESSURECT_03() {
		return new SM_SYSTEM_MESSAGE(1402692);
	}

	/**
	 * Orissan begins to Ascend through Ascension Dominance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_IMMORTAL_01() {
		return new SM_SYSTEM_MESSAGE(1402693);
	}

	/**
	 * Orissan has Ascended through Ascension Dominance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_IMMORTAL_02() {
		return new SM_SYSTEM_MESSAGE(1402694);
	}

	/**
	 * Immortal Orissan cannot be killed. The Ascended state lasts until Orissan becomes exhausted.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_IMMORTAL_03() {
		return new SM_SYSTEM_MESSAGE(1402695);
	}

	/**
	 * Orissan will soon be exhausted by Ascension Dominance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_IMMORTAL_04() {
		return new SM_SYSTEM_MESSAGE(1402696);
	}

	/**
	 * Orissan has become exhausted by Ascension Dominance.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_IMMORTAL_05() {
		return new SM_SYSTEM_MESSAGE(1402697);
	}

	/**
	 * Slay Orissan before the next Ascension Dominance begins.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_IMMORTAL_06() {
		return new SM_SYSTEM_MESSAGE(1402698);
	}

	/**
	 * The Detachment's Rush Squad has resolved to sacrifice themselves to undo Orissan's Ascended state.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_IMMORTAL_07() {
		return new SM_SYSTEM_MESSAGE(1402699);
	}

	/**
	 * If Orissan Ascends again, the Detachment will risk sacrificing themselves to launch an attack.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_IMMORTAL_08() {
		return new SM_SYSTEM_MESSAGE(1402700);
	}

	/**
	 * Thanks to the sacrifice of the Detachment's Rush Squad, Orissan's Ascension has been blocked.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_IMMORTAL_09() {
		return new SM_SYSTEM_MESSAGE(1402701);
	}

	/**
	 * Due to the sacrifices, Orissan can no longer Ascend.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_IMMORTAL_10() {
		return new SM_SYSTEM_MESSAGE(1402702);
	}

	/**
	 * The Empyrean Lord's Stormcannon is being charged for the Empyrean Firestorm.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_01() {
		return new SM_SYSTEM_MESSAGE(1402703);
	}

	/**
	 * The Guhena Legion has detected intruders and will begin attacking.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_02() {
		return new SM_SYSTEM_MESSAGE(1402704);
	}

	/**
	 * Defend the Detachment and its siege weapons from the Guhena Legion.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_03() {
		return new SM_SYSTEM_MESSAGE(1402705);
	}

	/**
	 * If the Detachment loses too many soldiers, they will not be able to assist during the battle against Beritra.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_04() {
		return new SM_SYSTEM_MESSAGE(1402706);
	}

	/**
	 * The Guhena Legion's second wave of attack has started. There will be three more attack waves.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_05() {
		return new SM_SYSTEM_MESSAGE(1402707);
	}

	/**
	 * The Guhena Legion's third wave of attack has started. There will be two more attack waves.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_06() {
		return new SM_SYSTEM_MESSAGE(1402708);
	}

	/**
	 * The Guhena Legion's fourth wave of attack has started. There will be one more attack wave.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_07() {
		return new SM_SYSTEM_MESSAGE(1402709);
	}

	/**
	 * The Guhena Legion's Commander Virtsha has appeared. You must defeat every captain and commander.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_08() {
		return new SM_SYSTEM_MESSAGE(1402710);
	}

	/**
	 * The Empyrean Lord's Stormcannon has blown open the Seal of Darkness.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_09() {
		return new SM_SYSTEM_MESSAGE(1402711);
	}

	/**
	 * The Detachment has suffered severe losses and will not be able to assist any further.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_BONUS_01() {
		return new SM_SYSTEM_MESSAGE(1402712);
	}

	/**
	 * The Detachment has suffered heavy losses and can only assist in limited capacity.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_BONUS_02() {
		return new SM_SYSTEM_MESSAGE(1402713);
	}

	/**
	 * The Detachment has suffered some losses, but can assist at almost full capacity.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_BONUS_03() {
		return new SM_SYSTEM_MESSAGE(1402714);
	}

	/**
	 * You have successfully protected the Detachment. They will assist you during the battle against Beritra.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_WAVE_BONUS_04() {
		return new SM_SYSTEM_MESSAGE(1402715);
	}

	/**
	 * Beritra uses his Power.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_VRITRA_HUMAN_01() {
		return new SM_SYSTEM_MESSAGE(1402716);
	}

	/**
	 * Unless his Power is deactivated, Beritra cannot be vanquished.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_VRITRA_HUMAN_02() {
		return new SM_SYSTEM_MESSAGE(1402717);
	}

	/**
	 * Beritra uses Immortal Vitality to recover his health completely.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_VRITRA_HUMAN_03() {
		return new SM_SYSTEM_MESSAGE(1402718);
	}

	/**
	 * The extraction of the Balaur Lord's Relic will soon be complete. Beritra will disappear when the relic is completely extracted.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_VRITRA_HUMAN_04() {
		return new SM_SYSTEM_MESSAGE(1402719);
	}

	/**
	 * The extraction of the Balaur Lord's Relic is complete and Beritra has disappeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_VRITRA_HUMAN_05() {
		return new SM_SYSTEM_MESSAGE(1402720);
	}

	/**
	 * Beritra transforms into a dragon.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_VRITRA_DRAGON_01() {
		return new SM_SYSTEM_MESSAGE(1402721);
	}

	/**
	 * Beritra will disappear when the relic is completely extracted in 7 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_VRITRA_DRAGON_02() {
		return new SM_SYSTEM_MESSAGE(1402722);
	}

	/**
	 * Beritra used Balaur Lord's Authority and became invincible. The Power must be deactivated.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_VRITRA_DRAGON_03() {
		return new SM_SYSTEM_MESSAGE(1402723);
	}

	/**
	 * Beritra will disappear when the relic is completely extracted in 5 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_VRITRA_DRAGON_04() {
		return new SM_SYSTEM_MESSAGE(1402724);
	}

	/**
	 * Beritra will disappear when the relic is completely extracted in 1 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_VRITRA_DRAGON_05() {
		return new SM_SYSTEM_MESSAGE(1402725);
	}

	/**
	 * Beritra will disappear when the relic is completely extracted in a moment.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_VRITRA_DRAGON_06() {
		return new SM_SYSTEM_MESSAGE(1402726);
	}

	/**
	 * Due to the desperate interference by the Detachment's Rush Squad, the extraction of the Balaur Lord's Relic has been delayed by 30 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_VRITRA_DRAGON_PCGUARD() {
		return new SM_SYSTEM_MESSAGE(1402727);
	}

	/**
	 * The Seal Protector who watches over the Balaur Lord's Seal has appeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_GUARDIAN_01() {
		return new SM_SYSTEM_MESSAGE(1402728);
	}

	/**
	 * Destroying the Protector releases the Seal Protector Stigma, which will explode immediately.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_GUARDIAN_02() {
		return new SM_SYSTEM_MESSAGE(1402729);
	}

	/**
	 * When the Stigma explodes, the Balaur Lord's Power is deactivated.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDSEAL_GUARDIAN_03() {
		return new SM_SYSTEM_MESSAGE(1402730);
	}

	/**
	 * Xasta flies past overhead.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDYUN_RASTA_SPAWN_01() {
		return new SM_SYSTEM_MESSAGE(1402775);
	}

	/**
	 * Use the anti-aircraft gun to attack Xasta flying overhead.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDYUN_RASTA_SPAWN_02() {
		return new SM_SYSTEM_MESSAGE(1402776);
	}

	/**
	 * Prepare for combat! Enemies approaching!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TAMES_SOLO_A_START() {
		return new SM_SYSTEM_MESSAGE(1402780);
	}

	/**
	 * Use the open entrance to move to the next area.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TAMES_SOLO_A_END() {
		return new SM_SYSTEM_MESSAGE(1402781);
	}

	/**
	 * The switch is now operational.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TAMES_SOLO_B_START() {
		return new SM_SYSTEM_MESSAGE(1402782);
	}

	/**
	 * Prepare for combat! Enemies approaching!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TAMES_SOLO_B_ING() {
		return new SM_SYSTEM_MESSAGE(1402783);
	}

	/**
	 * Use the open entrance to move to the next area.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TAMES_SOLO_B_END() {
		return new SM_SYSTEM_MESSAGE(1402784);
	}

	/**
	 * Prepare for combat! Enemies approaching!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TAMES_SOLO_C_START() {
		return new SM_SYSTEM_MESSAGE(1402785);
	}

	/**
	 * Use the open entrance to move to the next area.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TAMES_SOLO_C_END() {
		return new SM_SYSTEM_MESSAGE(1402786);
	}

	/**
	 * Locate the prison keys to defeat the monsters inside.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5Re_solo_game1_1() {
		return new SM_SYSTEM_MESSAGE(1402801);
	}

	/**
	 * All monsters and key boxes in the library will disappear in 1 minute.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5Re_solo_game1_2() {
		return new SM_SYSTEM_MESSAGE(1402802);
	}

	/**
	 * All monsters and key boxes in the library will disappear in 30 seconds.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5Re_solo_game1_3() {
		return new SM_SYSTEM_MESSAGE(1402803);
	}

	/**
	 * All monsters and key boxes in the library will disappear in a moment.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5Re_solo_game1_4() {
		return new SM_SYSTEM_MESSAGE(1402804);
	}

	/**
	 * All monsters and key boxes in the library have disappeared.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5Re_solo_game1_5() {
		return new SM_SYSTEM_MESSAGE(1402805);
	}

	/**
	 * Locate the prison keys to defeat the monsters inside.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDLDF5Re_solo_game1_6() {
		return new SM_SYSTEM_MESSAGE(1402806);
	}

	/**
	 * The door cannot be opened yet.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_TAMES_SOLO_A_DOOR_CONDITION() {
		return new SM_SYSTEM_MESSAGE(1402831);
	}

	/**
	 * Prepare for combat! More enemies swarming in!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDRAKSHA_SOLO_WAVEMID() {
		return new SM_SYSTEM_MESSAGE(1402832);
	}

	/**
	 * Hold a little longer and you will survive.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDRAKSHA_SOLO_WAVELAST() {
		return new SM_SYSTEM_MESSAGE(1402833);
	}

	/**
	 * Only a few enemies left!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_IDRAKSHA_SOLO_WAVELAST01() {
		return new SM_SYSTEM_MESSAGE(1402834);
	}

	/**
	 * The Aetheric Field Blaststone will explode in 2 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_OBJ_Bomb() {
		return new SM_SYSTEM_MESSAGE(1402874);
	}

	/**
	 * The Invasion Rift to Elysea has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LIGHT_SIDE_LEGION_DIRECT_PORTAL_OPEN() {
		return new SM_SYSTEM_MESSAGE(1402877);
	}

	/**
	 * An Invasion Rift to Asmodae has opened.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_DARK_SIDE_LEGION_DIRECT_PORTAL_OPEN() {
		return new SM_SYSTEM_MESSAGE(1402878);
	}

	/**
	 * You cannot use %0 at your level.
	 */
	public static SM_SYSTEM_MESSAGE STR_CANNOT_USE_DIRECT_PORTAL_LEVEL_LIMIT_COMMON(String npcL10n) {
		return new SM_SYSTEM_MESSAGE(1402880, npcL10n);
	}

	/**
	 * You can no longer use the %2 %0 skill (Level %1) because your Linked Stigma combination has changed
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_STIGMA_DELETE_HIDDEN_SKILL(String value0, int value1, String value2) {
		return new SM_SYSTEM_MESSAGE(1402895, value0, value1, value2);
	}

	/**
	 * %0 Stonespear Siege application has been submitted.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_GUILD_APPLY_DOMINION(String siegeName) {
		return new SM_SYSTEM_MESSAGE(1402902, siegeName);
	}

	/**
	 * %0 has been killed and will be moved to the waiting area.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_LEGION_DOMINION_MOVE_BIRTHAREA_FRIENDLY(String playerName) {
		return new SM_SYSTEM_MESSAGE(1402911, playerName);
	}

	/**
	 * The Aetheric Field Blaststone has exploded!
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_OBJ_Bomb_Die() {
		return new SM_SYSTEM_MESSAGE(1402914);
	}

	/**
	 * You cannot use the skill because the companion has not been summoned.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NOT_NEED_PET() {
		return new SM_SYSTEM_MESSAGE(1402918);
	}

	/**
	 * You are too far from the target to use that skill.
	 */
	public static SM_SYSTEM_MESSAGE STR_SKILL_NOT_ENOUGH_DISTANCE() {
		return new SM_SYSTEM_MESSAGE(1402920);
	}

	/**
	 * Protect the Guardian Stone for 2 minutes.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_OBJ_Start() {
		return new SM_SYSTEM_MESSAGE(1402924);
	}

	/**
	 * You have successfully protected the Guardian Stone.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_OBJ_End() {
		return new SM_SYSTEM_MESSAGE(1402925);
	}

	/**
	 * You have successfully enchanted %0 and the Stigma's enchantment level has increased by 1 level.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_STIGMA_ENCHANT_SUCCESS(String value0) {
		return new SM_SYSTEM_MESSAGE(1402930, value0);
	}

	/**
	 * You have failed to enchant %0 and the Stigma has been destroyed.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_STIGMA_ENCHANT_FAIL(String value0) {
		return new SM_SYSTEM_MESSAGE(1402931, value0);
	}

	/**
	 * Coordinates of current location: %WORLDNAME0 Region, X=%1 Y=%2 Z=%3
	 */
	public static SM_SYSTEM_MESSAGE STR_CMD_LOCATION_DESC(int worldId, float x, float y, float z) {
		return new SM_SYSTEM_MESSAGE(230038, worldId, x, y, z);
	}

	/**
	 * The Legion was leveled up to %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_GUILD_EVENT_LEVELUP(int newLevel) {
		return new SM_SYSTEM_MESSAGE(900700, newLevel);
	}

	/**
	 * Busy in game
	 */
	public static SM_SYSTEM_MESSAGE STR_BUDDYLIST_BUSY() {
		return new SM_SYSTEM_MESSAGE(900847);
	}

	/**
	 * You don't have enough Kinah. It costs %num0 Kinah.
	 */
	public static SM_SYSTEM_MESSAGE STR_MSG_NOT_ENOUGH_KINA(long num0) {
		return new SM_SYSTEM_MESSAGE(901285, num0);
	}

	/**
	 * You are not authorized to examine the corpse.
	 */
	public static SM_SYSTEM_MESSAGE STR_LOOT_NO_RIGHT() {
		return new SM_SYSTEM_MESSAGE(901338);
	}

	/**
	 * You do not have enough %0.
	 */
	public static SM_SYSTEM_MESSAGE STR_INGAMESHOP_NOT_ENOUGH_CASH(String value0) {
		return new SM_SYSTEM_MESSAGE(901706, value0);
	}

	/**
	 * You have already learned this emote.
	 */
	public static SM_SYSTEM_MESSAGE STR_TOOLTIP_LEARNED_EMOTION() {
		return new SM_SYSTEM_MESSAGE(901713);
	}

	/**
	 * You have already learned this title.
	 */
	public static SM_SYSTEM_MESSAGE STR_TOOLTIP_LEARNED_TITLE() {
		return new SM_SYSTEM_MESSAGE(901714);
	}

	/**
	 * You can only use this when you have a Plastic Surgery Ticket.
	 */
	public static SM_SYSTEM_MESSAGE STR_EDIT_CHAR_ALL_CANT_NO_ITEM() {
		return new SM_SYSTEM_MESSAGE(901752);
	}

	/**
	 * You can only use this when you have a Gender Switch Ticket.
	 */
	public static SM_SYSTEM_MESSAGE STR_EDIT_CHAR_GENDER_CANT_NO_ITEM() {
		return new SM_SYSTEM_MESSAGE(901754);
	}

	/**
	 * You cannot use it because you belong to a different race.
	 */
	public static SM_SYSTEM_MESSAGE STR_MOVE_PORTAL_ERROR_INVALID_RACE() {
		return new SM_SYSTEM_MESSAGE(901354);
	}

	private int msgId;
	private byte chatType;
	private int senderObjId;
	private Object[] params;
	private String[] specialParams;

	public SM_SYSTEM_MESSAGE(int msgId, Object... params) {
		this(ChatType.GOLDEN_YELLOW, null, msgId, params, new String[0]);
	}

	public SM_SYSTEM_MESSAGE(ChatType chatType, VisibleObject sender, int msgId, Object... params) {
		this(chatType, sender, msgId, params, new String[0]);
	}

	/**
	 * @param chatType
	 *          - The chat channel the message will be displayed in.
	 * @param sender
	 *          - Object that sends the message, can be null (will display a speech bubble above his head, if the chat type is not a SysMsg chat type)
	 * @param msgId
	 *          - The ID of the client message to send.
	 * @param params
	 *          - Parameters for this client message, like names, level values, etc., can be null
	 * @param specialParams
	 *          - Special parameters, currently only known to work with client messages that want a [%target] parameter
	 */
	public SM_SYSTEM_MESSAGE(ChatType chatType, VisibleObject sender, int msgId, Object[] params, String... specialParams) {
		this.chatType = chatType.getId();
		this.senderObjId = sender == null ? 0 : sender.getObjectId();
		this.msgId = msgId;
		this.params = params == null ? new Object[0] : params;
		this.specialParams = specialParams;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(chatType);
		writeC(0x00); // to do for shoots text encoding (unk dialect)
		writeD(senderObjId);
		writeD(msgId);
		writeC(params.length);
		for (Object param : params)
			writeS(param == null ? null : param.toString());

		writeC(specialParams.length);
		for (String param : specialParams) {
			writeS(param);
		}
	}

	/**
	 * @return the stringId
	 */
	public int getId() {
		return msgId;
	}

}
