package com.aionemu.gameserver.model.gameobjects;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIEngine;
import com.aionemu.gameserver.controllers.SummonController;
import com.aionemu.gameserver.controllers.attack.AggroList;
import com.aionemu.gameserver.controllers.attack.PlayerAggroList;
import com.aionemu.gameserver.controllers.movement.SiegeWeaponMoveController;
import com.aionemu.gameserver.controllers.movement.SummonMoveController;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.SummonGameStats;
import com.aionemu.gameserver.model.stats.container.SummonLifeStats;
import com.aionemu.gameserver.model.summons.SkillOrder;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author ATracer
 */
public class Summon extends Creature {

	private Player master;
	private SummonMode mode = SummonMode.GUARD;
	private Queue<SkillOrder> skillOrders = new LinkedList<>();
	private Future<?> releaseTask;
	private SkillElement alwaysResistElement = SkillElement.NONE;
	private int summonedBySkillId, liveTime;

	/**
	 * @param objId
	 * @param controller
	 * @param spawnTemplate
	 * @param objectTemplate
	 * @param time
	 */
	public Summon(int objId, SummonController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate, int time) {
		super(objId, controller, spawnTemplate, objectTemplate, new WorldPosition(spawnTemplate.getWorldId()));
		controller.setOwner(this);
		String ai = objectTemplate.getAi();
		AIEngine.getInstance().setupAI(ai, this);
		moveController = ai.equals("siege_weapon") ? new SiegeWeaponMoveController(this) : new SummonMoveController(this);
		this.liveTime = time;
		setGameStats(new SummonGameStats(this));
		setLifeStats(new SummonLifeStats(this));
		setAlwaysResistElement(objectTemplate);
	}

	private void setAlwaysResistElement(NpcTemplate template) {
		if (template != null) {
			switch (template.getName()) {
				case "lava spirit":
					this.alwaysResistElement = SkillElement.MAGMA;
					break;
				case "tempest spirit":
					this.alwaysResistElement = SkillElement.TEMPEST;
					break;
				case "earth spirit":
					this.alwaysResistElement = SkillElement.EARTH;
					break;
				case "fire spirit":
					this.alwaysResistElement = SkillElement.FIRE;
					break;
				case "water spirit":
					this.alwaysResistElement = SkillElement.WATER;
					break;
				case "wind spirit":
					this.alwaysResistElement = SkillElement.WIND;
					break;
			}
		}
	}

	@Override
	protected AggroList createAggroList() {
		return new PlayerAggroList(this);
	}

	@Override
	public SummonGameStats getGameStats() {
		return (SummonGameStats) super.getGameStats();
	}

	@Override
	public Player getMaster() {
		return master;
	}

	/**
	 * @param master
	 *          the master to set
	 */
	public void setMaster(Player master) {
		this.master = master;
	}

	@Override
	public String getName() {
		return objectTemplate.getName();
	}

	/**
	 * @return the level
	 */
	@Override
	public byte getLevel() {
		return getObjectTemplate().getLevel();
	}

	@Override
	public NpcTemplate getObjectTemplate() {
		return (NpcTemplate) super.getObjectTemplate();
	}

	public int getNpcId() {
		return getObjectTemplate().getTemplateId();
	}

	public int getNameId() {
		return getObjectTemplate().getNameId();
	}

	/**
	 * @return NpcObjectType.SUMMON
	 */
	@Override
	public NpcObjectType getNpcObjectType() {
		return NpcObjectType.SUMMON;
	}

	@Override
	public SummonController getController() {
		return (SummonController) super.getController();
	}

	/**
	 * @return the mode
	 */
	public SummonMode getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *          the mode to set
	 */
	public void setMode(SummonMode mode) {
		if (mode != SummonMode.ATTACK)
			skillOrders.clear();
		this.mode = mode;
	}

	@Override
	public boolean isEnemy(Creature creature) {
		return master != null && master.isEnemy(creature);
	}

	@Override
	public boolean isEnemyFrom(Npc npc) {
		return master != null && master.isEnemyFrom(npc);
	}

	@Override
	public boolean isEnemyFrom(Player player) {
		return master != null && master.isEnemyFrom(player);
	}

	@Override
	public boolean isPvpTarget(Creature creature) {
		return getMaster() != null && creature.getActingCreature() instanceof Player;
	}

	@Override
	public TribeClass getTribe() {
		if (master == null)
			return ((NpcTemplate) objectTemplate).getTribe();
		return master.getTribe();
	}

	@Override
	public CreatureType getType(Creature creature) {
		boolean friend = master == null || master.getRace().equals(creature.getRace()) && !creature.isEnemy(master);
		return friend ? CreatureType.SUPPORT : CreatureType.ATTACKABLE;
	}

	@Override
	public SummonMoveController getMoveController() {
		return (SummonMoveController) super.getMoveController();
	}

	@Override
	public Creature getActingCreature() {
		return getMaster() == null ? this : getMaster();
	}

	@Override
	public Race getRace() {
		return getMaster() != null ? getMaster().getRace() : Race.NONE;
	}

	/**
	 * @return liveTime in sec.
	 */
	public int getLiveTime() {
		return liveTime;
	}

	/**
	 * @param liveTime
	 *          in sec.
	 */
	public void setLiveTime(int liveTime) {
		this.liveTime = liveTime;
	}

	/**
	 * @return the summonedBySkillId
	 */
	public int getSummonedBySkillId() {
		return summonedBySkillId;
	}

	/**
	 * @param summonedBySkillId
	 *          the summonedBySkillId to set
	 */
	public void setSummonedBySkillId(int summonedBySkillId) {
		this.summonedBySkillId = summonedBySkillId;
	}

	public Future<?> getReleaseTask() {
		return releaseTask;
	}

	public void setReleaseTask(Future<?> task) {
		releaseTask = task;
	}

	public void cancelReleaseTask() {
		if (releaseTask != null && !releaseTask.isDone()) {
			releaseTask.cancel(true);
		}
	}

	@Override
	public void setTarget(VisibleObject target) {
		SkillOrder order = skillOrders.peek();
		if (order != null && !Objects.equals(target, order.getTarget())) {
			skillOrders.clear();
		}
		super.setTarget(target);
	}

	public void addSkillOrder(int skillId, int skillLvl, boolean release, Creature target) {
		this.skillOrders.add(new SkillOrder(skillId, skillLvl, release, target));
	}

	public SkillOrder retrieveNextSkillOrder() {
		return skillOrders.poll();
	}

	public SkillOrder getNextSkillOrder() {
		return skillOrders.peek();
	}

	public SkillElement getAlwaysResistElement() {
		return alwaysResistElement;
	}
}
