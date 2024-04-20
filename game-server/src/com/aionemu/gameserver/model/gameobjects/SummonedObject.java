package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.NpcLifeStats;
import com.aionemu.gameserver.model.stats.container.SummonedObjectGameStats;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 * @author ATracer, Rolandas
 */
public class SummonedObject<T extends VisibleObject> extends Npc {

	private final byte level;

	/**
	 * Creator of this SummonedObject
	 */
	private final T creator;

	public SummonedObject(NpcController controller, SpawnTemplate spawnTemplate, byte level, T creator) {
		super(controller, spawnTemplate, DataManager.NPC_DATA.getNpcTemplate(spawnTemplate.getNpcId()));
		this.level = level;
		this.creator = creator;
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

	@Override
	public String getMasterName() {
		return super.getMasterName() == null && creator != null ? creator.getName() : super.getMasterName();
	}

	@Override
	public int getCreatorId() {
		return super.getCreatorId() == 0 && creator != null ? creator.getObjectId() : super.getCreatorId();
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
