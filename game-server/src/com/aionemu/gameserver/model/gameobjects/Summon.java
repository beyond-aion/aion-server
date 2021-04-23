package com.aionemu.gameserver.model.gameobjects;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Future;

import com.aionemu.gameserver.controllers.SiegeWeaponController;
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
import com.aionemu.gameserver.model.templates.npc.NpcTemplateType;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author ATracer
 */
public class Summon extends Creature {

	private final Player master;
	private SummonMode mode = SummonMode.GUARD;
	private Queue<SkillOrder> skillOrders = new LinkedList<>();
	private Future<?> releaseTask;
	private SkillElement alwaysResistElement = SkillElement.NONE;
	private int summonedBySkillId, liveTime;

	public Summon(int objId, SummonController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate, Player master, int time, boolean autoReleaseObjectId) {
		super(objId, controller, spawnTemplate, objectTemplate, new WorldPosition(spawnTemplate.getWorldId()), autoReleaseObjectId);
		controller.setOwner(this);
		moveController = controller instanceof SiegeWeaponController ? new SiegeWeaponMoveController(this) : new SummonMoveController(this);
		this.liveTime = time;
		this.master = master;
		setGameStats(new SummonGameStats(this));
		setLifeStats(new SummonLifeStats(this));
		setAlwaysResistElement(objectTemplate);
	}

	private void setAlwaysResistElement(NpcTemplate template) {
		if (template != null) {
			switch (template.getName()) {
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

	public String getL10n() {
		return getObjectTemplate().getL10n();
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
		return master.isEnemy(creature);
	}

	@Override
	public boolean isEnemyFrom(Npc npc) {
		return master.isEnemyFrom(npc);
	}

	@Override
	public boolean isEnemyFrom(Player player) {
		return master.isEnemyFrom(player);
	}

	@Override
	public boolean isPvpTarget(Creature creature) {
		return creature.getActingCreature() instanceof Player;
	}

	@Override
	public TribeClass getTribe() {
		return master.getTribe();
	}

	public CreatureType getType(Creature creature) {
		boolean friend = master.getRace() == creature.getRace() && !creature.isEnemy(master);
		return friend ? CreatureType.SUPPORT : CreatureType.ATTACKABLE;
	}

	@Override
	public SummonMoveController getMoveController() {
		return (SummonMoveController) super.getMoveController();
	}

	@Override
	public Player getActingCreature() {
		return getMaster();
	}

	@Override
	public Race getRace() {
		return getMaster().getRace();
	}

	public boolean isPet() {
		return getObjectTemplate().getNpcTemplateType() == NpcTemplateType.SUMMON_PET;
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

	public void addSkillOrder(int skillId, int skillLvl, Creature target, int hate, boolean release) {
		skillOrders.add(new SkillOrder(skillId, skillLvl, target, hate, release));
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
