package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.NpcLifeStats;
import com.aionemu.gameserver.model.stats.container.SummonedObjectGameStats;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 * @author ATracer, modified Rolandas
 */
public class SummonedObject<T extends VisibleObject> extends Npc {

	private byte level;

	/**
	 * Creator of this SummonedObject
	 */
	private T creator;

	/**
	 * @param objId
	 * @param controller
	 * @param spawnTemplate
	 * @param objectTemplate
	 * @param level
	 */
	public SummonedObject(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate, byte level) {
		super(objId, controller, spawnTemplate, objectTemplate);
		this.level = level;
	}

	@Override
	protected void setupStatContainers() {
		setGameStats(new SummonedObjectGameStats(this));
		setLifeStats(new NpcLifeStats(this));
	}

	@Override
	public byte getLevel() {
		return this.level;
	}

	@Override
	public T getCreator() {
		return creator;
	}

	public void setCreator(T creator) {
		if (creator instanceof Player)
			((Player) creator).setSummonedObj(this);
		this.creator = creator;
	}

	@Override
	public String getMasterName() {
		return creator != null ? creator.getName() : super.getMasterName();
	}

	@Override
	public int getCreatorId() {
		return creator != null ? creator.getObjectId() : 0;
	}

	@Override
	public final Creature getMaster() {
		if (creator instanceof Creature)
			return (Creature) getCreator();
		return this;
	}

	@Override
	public CreatureType getType(Creature creature) {
		return creature.isEnemy(getMaster()) ? CreatureType.ATTACKABLE : CreatureType.SUPPORT;
	}

	@Override
	public boolean isEnemy(Creature creature) {
		if (creator instanceof Creature)
			return ((Creature) creator).isEnemy(creature);
		return super.isEnemy(creature);
	}

	@Override
	public boolean isEnemyFrom(Npc npc) {
		if (creator instanceof Creature)
			return ((Creature) creator).isEnemyFrom(npc);
		return super.isEnemyFrom(npc);
	}

	@Override
	public boolean isEnemyFrom(Player player) {
		if (creator instanceof Creature)
			return ((Creature) creator).isEnemyFrom(player);
		return super.isEnemyFrom(player);
	}

	@Override
	public TribeClass getTribe() {
		if (creator instanceof Creature)
			return ((Creature) creator).getTribe();
		return super.getTribe();
	}

	@Override
	public Race getRace() {
		return creator instanceof Creature ? ((Creature) creator).getRace() : super.getRace();
	}

	@Override
	public boolean isPvpTarget(Creature creature) {
		return (getActingCreature() instanceof Player) && (creature.getActingCreature() instanceof Player);
	}

}
