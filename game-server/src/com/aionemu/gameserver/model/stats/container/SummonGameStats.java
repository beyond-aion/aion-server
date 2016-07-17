package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.templates.stats.SummonStatsTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SUMMON_UPDATE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class SummonGameStats extends CreatureGameStats<Summon> {

	private int cachedSpeed;
	private final SummonStatsTemplate statsTemplate;

	/**
	 * @param owner
	 * @param statsTemplate
	 */
	public SummonGameStats(Summon owner, SummonStatsTemplate statsTemplate) {
		super(owner);
		this.statsTemplate = statsTemplate;
	}

	@Override
	protected void onStatsChange() {
		updateStatsAndSpeedVisually();
	}

	public void updateStatsAndSpeedVisually() {
		updateStatsVisually();
		checkSpeedStats();
	}

	public void updateStatsVisually() {
		owner.getGameStats().updateStatInfo();
	}

	private void checkSpeedStats() {
		int current = getMovementSpeed().getCurrent();
		if (current != cachedSpeed) {
			owner.getGameStats().updateSpeedInfo();
		}
		cachedSpeed = current;
	}

	@Override
	public Stat2 getStat(StatEnum statEnum, int base) {
		Stat2 stat = super.getStat(statEnum, base);
		if (owner.getMaster() == null)
			return stat;
		switch (statEnum) {
			case MAXHP:
			case BOOST_MAGICAL_SKILL:
			case MAGICAL_ACCURACY:
			case PHYSICAL_DEFENSE:
			case EVASION:
			case PARRY:
			case MAGICAL_RESIST:
			case MAGIC_SKILL_BOOST_RESIST: // needs some tests
				return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
			case PHYSICAL_ACCURACY: // needs some tests
				owner.getMaster().getGameStats().getItemStatBoost(StatEnum.MAIN_HAND_ACCURACY, stat);
				return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
			case PHYSICAL_CRITICAL: // needs some tests
				owner.getMaster().getGameStats().getItemStatBoost(StatEnum.MAIN_HAND_CRITICAL, stat);
				return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
			case MAGICAL_CRITICAL:
				stat.setBonusRate(0.95f);
				return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);
		}
		return stat;
	}

	@Override
	public SummonStatsTemplate getStatsTemplate() {
		return statsTemplate;
	}

	@Override
	public Stat2 getAttackSpeed() {
		return getStat(StatEnum.ATTACK_SPEED, owner.getObjectTemplate().getAttackDelay());
	}

	@Override
	public Stat2 getMovementSpeed() {
		int bonusSpeed = 0;
		Player master = owner.getMaster();
		if (master != null && master.isFlying()) {
			bonusSpeed += 3000;
		}
		return getStat(StatEnum.SPEED, Math.round(getStatsTemplate().getRunSpeed() * 1000) + bonusSpeed);
	}

	@Override
	public Stat2 getAttackRange() {
		return getStat(StatEnum.ATTACK_RANGE, owner.getObjectTemplate().getAttackRange() * 1000);
	}

	@Override
	public Stat2 getHpRegenRate() {
		int base = (int) (owner.getLifeStats().getMaxHp() * (owner.getMode() == SummonMode.REST ? 0.05f : 0.025f));
		return getStat(StatEnum.REGEN_HP, base);
	}

	@Override
	public Stat2 getMpRegenRate() {
		throw new IllegalStateException("No mp regen for Summon");
	}

	@Override
	public void updateStatInfo() {
		Player master = owner.getMaster();
		if (master != null) {
			PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(owner));
		}
	}

	@Override
	public void updateSpeedInfo() {
		PacketSendUtility.broadcastPacket(owner, new SM_EMOTION(owner, EmotionType.START_EMOTE2, 0, 0));
	}

}
