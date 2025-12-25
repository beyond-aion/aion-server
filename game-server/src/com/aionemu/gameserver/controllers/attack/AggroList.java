package com.aionemu.gameserver.controllers.attack;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.SummonedObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.HopType;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.stats.StatFunctions;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer, KKnD
 */
public class AggroList {

	protected final Creature owner;
	private final ConcurrentHashMap<Integer, AggroInfo> aggroList = new ConcurrentHashMap<>();
	private Future<?> hateReductionTask;

	public AggroList(Creature owner) {
		this.owner = owner;
	}

	/**
	 * Only add damage from enemies. (Verify this includes summons, traps, pets, and excludes fall damage.)
	 */
	public void addDamage(Creature attacker, int damage, boolean notifyAttack, HopType hopType) {
		if (!isAware(attacker))
			return;
		// If the incoming damage is higher than the rest life it will decreased to the rest life
		if (damage >= owner.getLifeStats().getCurrentHp()) {
			damage = owner.getLifeStats().getCurrentHp();
		} else if (hateReductionTask == null) {
			startHateReductionTask();
		}
		int hate = 0;
		if (notifyAttack && hopType == HopType.DAMAGE && damage > 0) {
			//damage caused by auto attacks and skills with HopType.DAMAGE is multiplied by 10 and added as hate on retail
			hate = StatFunctions.calculateHate(attacker, damage * 10);
		}
		addDamageAndHate(attacker, damage, hate);
	}

	/**
	 * Hate that is received without dealing damage
	 */
	public void addHate(Creature creature, int hate) {
		if (shouldAddHateToMaster(creature))
			creature = creature.getMaster();
		if (!isAware(creature))
			return;
		if (hate < 0 && !aggroList.containsKey(creature.getObjectId()))
			return;
		addDamageAndHate(creature, 0, hate);
	}

	private void addDamageAndHate(Creature creature, int damage, int hate) {
		AggroInfo ai = aggroList.computeIfAbsent(creature.getObjectId(), _ -> new AggroInfo(creature));
		boolean isNewInAggroList = ai.getHate() == 0;
		ai.addDamage(damage);
		ai.addHate(hate);
		owner.getController().onAddHate(creature, isNewInAggroList);
	}

	private boolean shouldAddHateToMaster(Creature creature) {
		// ice sheet, threatening wave, etc. generate hate for their master. taunting spirit does not!
		return creature instanceof SummonedObject<?> summonedObject && !isTauntingSpirit(summonedObject);
	}

	private boolean isTauntingSpirit(SummonedObject<?> npc) {
		return switch (npc.getNpcId()) {
			case 833403, 833404, 833478, 833479, 833480, 833481 -> true; // spawned by Summon Vexing Energy
			default -> false;
		};
	}

	/**
	 * @return player with most damage, if no other creatures like NPCs dealt more damage
	 */
	public Player getMostPlayerDamage() {
		// Use final damage list to get pet damage as well.
		DamageInfo<Creature> mostDamage = getFinalDamageList().getMostDamage();
		return mostDamage != null && mostDamage.getAttacker() instanceof Player player ? player : null;
	}

	public void stopHating(VisibleObject creature) {
		AggroInfo aggroInfo = aggroList.get(creature.getObjectId());
		if (aggroInfo != null)
			aggroInfo.setHate(0);
	}

	/**
	 * Remove creature from aggro list and transfer its damages to the master
	 */
	public void remove(Creature creature) {
		remove(creature, true);
	}

	public void remove(Creature creature, boolean transferDamagesToMaster) {
		AggroInfo aggroInfo = aggroList.remove(creature.getObjectId());
		if (transferDamagesToMaster && aggroInfo != null)
			transferDamagesToMaster(aggroInfo);
	}

	private void transferDamagesToMaster(AggroInfo aggroInfo) {
		Creature master = aggroInfo.getAttacker().getMaster();
		if (master.equals(aggroInfo.getAttacker()) || !isAware(master))
			return;
		aggroList.compute(master.getObjectId(), (_, masterAggroInfo) -> {
			if (masterAggroInfo == null) {
				masterAggroInfo = new AggroInfo(master);
				masterAggroInfo.setHate(1);
			}
			masterAggroInfo.addDamage(aggroInfo.getDamage());
			return masterAggroInfo;
		});
	}

	public void clear() {
		synchronized (this) {
			if (hateReductionTask != null) {
				hateReductionTask.cancel(true);
				hateReductionTask = null;
			}
		}
		aggroList.clear();
	}

	public boolean isHating(Creature creature) {
		AggroInfo aggroInfo = aggroList.get(creature.getObjectId());
		return aggroInfo != null && aggroInfo.getHate() > 0;
	}

	public int getHate(Creature creature) {
		AggroInfo aggroInfo = aggroList.get(creature.getObjectId());
		return aggroInfo != null ? aggroInfo.getHate() : 0;
	}

	public Stream<AggroInfo> stream() {
		return aggroList.values().stream();
	}

	/**
	 * @return a living creature with no obstacle between it and the owner
	 */
	public Creature getTarget(AggroTarget targetType) {
		return getTarget(targetType, Integer.MAX_VALUE);
	}

	/**
	 * @return a living creature that is within the given range with no obstacle between it and the owner
	 */
	public Creature getTarget(AggroTarget targetType, float range) {
		Stream<AggroInfo> stream = streamValidTargetInfo(range);
		return switch (targetType) {
			case RANDOM -> Rnd.get(stream.map(AggroInfo::getAttacker).toList());
			case RANDOM_EXCEPT_CURRENT_TARGET -> Rnd.get(stream.map(AggroInfo::getAttacker).filter(c -> !c.equals(owner.getTarget())).toList());
			case MOST_HATED -> stream.max(Comparator.comparingInt(AggroInfo::getHate)).map(AggroInfo::getAttacker).orElse(null);
			case SECOND_MOST_HATED -> stream.sorted(Comparator.comparingInt(AggroInfo::getHate).reversed()).limit(2).reduce((_, b) -> b).map(AggroInfo::getAttacker).orElse(null);
			case THIRD_MOST_HATED -> stream.sorted(Comparator.comparingInt(AggroInfo::getHate).reversed()).limit(3).reduce((_, b) -> b).map(AggroInfo::getAttacker).orElse(null);
		};
	}

	/**
	 * @return living creatures that are within the given range with no obstacle between them and the owner
	 */
	public Stream<Creature> streamValidTargets(float range) {
		return streamValidTargetInfo(range).map(AggroInfo::getAttacker);
	}

	private Stream<AggroInfo> streamValidTargetInfo(float range) {
		return stream()
			.filter(ai -> ai.getHate() > 0 && !ai.getAttacker().isDead() && !ai.getAttacker().getLifeStats().isAboutToDie()
				&& owner.getKnownList().sees(ai.getAttacker())
				&& (range == Integer.MAX_VALUE || PositionUtil.isInRange(owner, ai.getAttacker(), range, false))
				&& GeoService.getInstance().canSee(owner, ai.getAttacker()));
	}

	/**
	 * Damages of summons and summoned objects are added to those of their masters.
	 * 
	 * @return list of DamageInfo with npc and player damages
	 */
	public DamageList getFinalDamageList() {
		return new DamageList(aggroList.values(), owner);
	}

	protected boolean isAware(Creature creature) {
		return creature != null && owner.getKnownList().knows(creature) && !owner.getEffectController().isAbnormalSet(AbnormalState.SANCTUARY)
			&& (aggroList.containsKey(creature.getObjectId()) || creature.isEnemy(owner) || DataManager.TRIBE_RELATIONS_DATA.isHostileRelation(owner.getTribe(), creature.getTribe()));
	}

	private void startHateReductionTask() {
		synchronized (this) {
			if (hateReductionTask == null) {
				hateReductionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
					for (AggroInfo info : aggroList.values()) {
						if (info.getLastInteractionTime() != 0 && System.currentTimeMillis() - info.getLastInteractionTime() > 5000){
							info.reduceHate();
						}
					}
				}, 10000, 10000); // every 10 sec reduce hate of not attacking creatures
			}
		}
	}
}
