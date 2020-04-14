package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.model.stats.container.HomingGameStats;
import com.aionemu.gameserver.model.stats.container.NpcLifeStats;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.world.knownlist.NpcKnownList;

/**
 * @author ATracer
 */
public class Homing extends SummonedObject<Creature> {

	private final int skillId;
	private final ItemAttackType attackType;

	/**
	 * Number of performed attacks
	 */
	private int attackCount;

	public Homing(NpcController controller, SpawnTemplate spawnTemplate, byte level, Creature creator, int skillId) {
		super(controller, spawnTemplate, level, creator);
		this.skillId = skillId;
		this.attackType = findAttackType();
		setMasterName("");
		setKnownlist(new NpcKnownList(this));
		setEffectController(new EffectController(this));
	}

	@Override
	protected void setupStatContainers() {
		setGameStats(new HomingGameStats(this));
		setLifeStats(new NpcLifeStats(this));
	}

	/**
	 * @param attackCount
	 *          the attackCount to set
	 */
	public void setAttackCount(int attackCount) {
		this.attackCount = attackCount;
	}

	/**
	 * @return the attackCount
	 */
	public int getAttackCount() {
		return attackCount;
	}

	/**
	 * @return NpcObjectType.HOMING
	 */
	@Override
	public NpcObjectType getNpcObjectType() {
		return NpcObjectType.HOMING;
	}

	@Override
	public ItemAttackType getAttackType() {
		return attackType;
	}

	public int getSkillId() {
		return skillId;
	}

	private ItemAttackType findAttackType() {
		if (getName().contains("fire"))
			return ItemAttackType.MAGICAL_FIRE;
		else if (getName().contains("stone") || getName().equals("gryphu"))
			return ItemAttackType.MAGICAL_EARTH;
		else if (getName().contains("water"))
			return ItemAttackType.MAGICAL_WATER;
		else if ((getName().contains("wind")) || (getName().contains("cyclone")) || (getName().contains("elemental")))
			return ItemAttackType.MAGICAL_WIND;
		return ItemAttackType.PHYSICAL;
	}
}
