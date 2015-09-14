package com.aionemu.gameserver.model.gameobjects;

import org.apache.commons.lang3.StringUtils;

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
		super(objId, controller, spawnTemplate, objectTemplate, level);
		this.level = level;
	}

	@Override
	protected void setupStatContainers(byte level) {
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
		return creator != null ? creator.getName() : StringUtils.EMPTY;
	}

	@Override
	public int getCreatorId() {
		return creator != null ? creator.getObjectId() : 0;
	}

	@Override
	public Creature getActingCreature() {
		if (creator instanceof Creature)
			return (Creature) getCreator();
		return this;
	}

	@Override
	public Creature getMaster() {
		if (creator instanceof Creature)
			return (Creature) getCreator();
		return this;
	}

	@Override
	public int getType(Creature creature) {
		return creature.isEnemy(getMaster()) ? CreatureType.ATTACKABLE.getId() : CreatureType.SUPPORT.getId();
	}

	@Override
	public boolean isEnemy(Creature creature) {
		return getMaster() != null ? getMaster().isEnemy(creature) : false;
	}

	@Override
	public boolean isEnemyFrom(Npc npc) {
		return getMaster() != null ? getMaster().isEnemyFrom(npc) : false;
	}

	@Override
	public boolean isEnemyFrom(Player player) {
		return getMaster() != null ? getMaster().isEnemyFrom(player) : false;
	}

	@Override
	public TribeClass getTribe() {
		if (getMaster() == null)
			return ((NpcTemplate) objectTemplate).getTribe();
		return getMaster().getTribe();
	}

	@Override
	public Race getRace() {
		if (creator instanceof Creature) {
			return creator != null ? ((Creature) creator).getRace() : Race.NONE;
		}
		return super.getRace();
	}

}
