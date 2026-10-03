package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.NpcLifeStats;
import com.aionemu.gameserver.model.stats.container.SummonedObjectGameStats;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 * @author ATracer, Rolandas
 */
public class SummonedObject<T extends VisibleObject> extends Npc {

	private final byte level;
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
		return creator instanceof Creature owner ? owner : this;
	}

	@Override
	public CreatureType getType(Creature creature) {
		return creature.isEnemy(getMaster()) ? CreatureType.ATTACKABLE : CreatureType.SUPPORT;
	}

	@Override
	public boolean isEnemy(Creature creature) {
		if (creator instanceof Creature owner)
			return owner.isEnemy(creature);
		return super.isEnemy(creature);
	}

	@Override
	public boolean isEnemyFrom(Npc npc) {
		if (creator instanceof Creature owner)
			return owner.isEnemyFrom(npc);
		return super.isEnemyFrom(npc);
	}

	@Override
	public boolean isEnemyFrom(Player player) {
		if (creator instanceof Player owner)
			return owner.isEnemyFrom(player, this);
		if (creator instanceof Creature owner)
			return owner.isEnemyFrom(player);
		return super.isEnemyFrom(player);
	}

	@Override
	public Race getRace() {
		return creator instanceof Creature owner ? owner.getRace() : super.getRace();
	}

	@Override
	public boolean isPvpTarget(Creature creature) {
		return creator instanceof Player && creature.getMaster() instanceof Player;
	}

}
