package com.aionemu.gameserver.controllers.attack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.events.AbstractEventSource;
import com.aionemu.gameserver.events.Listenable;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.SummonedObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.HopType;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.annotations.AnnotatedMethod;
import com.aionemu.gameserver.utils.stats.StatFunctions;

/**
 * @author ATracer, KKnD
 */
public class AggroList extends AbstractEventSource<AddDamageEvent> {

	protected final Creature owner;
	private final ConcurrentHashMap<Integer, AggroInfo> aggroList = new ConcurrentHashMap<>();
	private Future<?> hateReductionTask;

	public AggroList(Creature owner) {
		this.owner = owner;
	}

	/**
	 * Only add damage from enemies. (Verify this includes summons, traps, pets, and excludes fall damage.)
	 *
	 * @param attacker
	 * @param damage
	 */
	@Listenable
	public void addDamage(Creature attacker, int damage, boolean notifyAttack, HopType hopType) {
		if (!isAware(attacker))
			return;
		// If the incoming damage is higher than the rest life it will decreased to the rest life
		if (damage >= owner.getLifeStats().getCurrentHp()) {
			damage = owner.getLifeStats().getCurrentHp();
		} else if (hateReductionTask == null) {
			startHateReductionTask();
		}

		AddDamageEvent evObj = null;
		if (hasSubscribers()) {
			evObj = new AddDamageEvent(this, attacker, damage);
			if (!super.fireBeforeEvent(evObj))
				evObj = null;
		}

		AggroInfo ai = getAggroInfo(attacker);
		ai.addDamage(damage);


		// for now we add hate equal to each damage received, additionally effectHate will be broadcast to all hating creatures
		boolean isNewInAggroList = ai.getHate() == 0;
		if (notifyAttack && hopType == HopType.DAMAGE) {
			//damage caused by auto attacks and skills with HopType.DAMAGE is multiplied by 10 and added as hate on retail
			ai.addHate(damage > 0 ? StatFunctions.calculateHate(attacker, damage * 10) : damage);
		} else {
			ai.addHate(1);
		}
		owner.getController().onAddHate(attacker, isNewInAggroList);

		if (evObj != null)
			super.fireAfterEvent(evObj);
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

		AggroInfo ai = getAggroInfo(creature);
		boolean isNewInAggroList = ai.getHate() == 0;
		ai.addHate(hate);
		owner.getController().onAddHate(creature, isNewInAggroList);
	}

	private boolean shouldAddHateToMaster(Creature creature) {
		// ice sheet, threatening wave, etc. generate hate for their master. taunting spirit does not!
		return creature instanceof SummonedObject<?> && !isTauntingSpirit((SummonedObject<?>) creature);
	}

	private boolean isTauntingSpirit(SummonedObject<?> npc) {
		switch (npc.getNpcId()) {
			case 833403:
			case 833404:
			case 833478:
			case 833479:
			case 833480:
			case 833481:
				return true; // spawned by Summon Vexing Energy
		}
		return false;
	}

	/**
	 * @return player/group/alliance with most damage.
	 */
	public AionObject getMostDamage() {
		AionObject mostDamage = null;
		int maxDamage = 0;

		for (AggroInfo ai : getFinalDamageList(true)) {
			if (ai.getAttacker() == null || owner.equals(ai.getAttacker()))
				continue;

			if (ai.getDamage() > maxDamage) {
				mostDamage = ai.getAttacker();
				maxDamage = ai.getDamage();
			}
		}

		return mostDamage;
	}

	public Race getPlayerWinnerRace() {
		AionObject winner = getMostDamage();
		if (winner instanceof PlayerGroup) {
			return ((PlayerGroup) winner).getRace();
		} else if (winner instanceof Player)
			return ((Player) winner).getRace();
		return null;
	}

	/**
	 * @return player with most damage
	 */
	public Player getMostPlayerDamage() {
		if (aggroList.isEmpty())
			return null;

		Player mostDamage = null;
		int maxDamage = 0;

		// Use final damage list to get pet damage as well.
		for (AggroInfo ai : this.getFinalDamageList(false)) {
			if (ai.getDamage() > maxDamage && ai.getAttacker() instanceof Player) {
				mostDamage = (Player) ai.getAttacker();
				maxDamage = ai.getDamage();
			}
		}

		return mostDamage;
	}

	/**
	 * @return player with most damage
	 */
	public Player getMostPlayerDamageOfMembers(Collection<Player> team, int highestLevel) {
		if (aggroList.isEmpty())
			return null;

		Player mostDamage = null;
		int maxDamage = 0;

		// Use final damage list to get pet damage as well.
		for (AggroInfo ai : this.getFinalDamageList(false)) {
			if (!(ai.getAttacker() instanceof Player)) {
				continue;
			}

			if (!team.contains(ai.getAttacker())) {
				continue;
			}

			if (ai.getDamage() > maxDamage) {

				mostDamage = (Player) ai.getAttacker();
				maxDamage = ai.getDamage();
			}
		}

		if (mostDamage != null && mostDamage.isMentor()) {
			for (Player member : team) {
				if (member.getLevel() == highestLevel)
					mostDamage = member;
			}
		}

		return mostDamage;
	}

	/**
	 * @return most hated creature
	 */
	public Creature getMostHated() {
		if (aggroList.isEmpty())
			return null;

		Creature mostHated = null;
		int maxHate = 0;

		for (ConcurrentHashMap.Entry<Integer, AggroInfo> e : aggroList.entrySet()) {
			AggroInfo ai = e.getValue();
			if (ai == null)
				continue;

			// aggroList will never contain anything but creatures
			Creature attacker = (Creature) ai.getAttacker();

			if (attacker.isDead() || !attacker.isSpawned()) {
				if (!attacker.getMaster().equals(attacker)) { // remove creature from aggro list and transfer its damages to master
					remove(attacker);
					return getMostHated(); // re-evaluate so we don't skip the summon's master
				} else
					ai.setHate(0);
			}

			if (ai.getHate() > maxHate && owner.canSee(attacker)) { // skip invisible attackers
				mostHated = attacker;
				maxHate = ai.getHate();
			}
		}

		return mostHated;
	}

	/**
	 * @param creature
	 */
	public void stopHating(VisibleObject creature) {
		AggroInfo aggroInfo = aggroList.get(creature.getObjectId());
		if (aggroInfo != null)
			aggroInfo.setHate(0);
	}

	/**
	 * Remove creature from aggro list, transfer its damages to the master
	 *
	 * @param creature
	 */
	public void remove(Creature creature) {
		AggroInfo aggroInfo = aggroList.remove(creature.getObjectId());
		if (aggroInfo != null)
			transferDamagesToMaster(aggroInfo);
	}

	private void transferDamagesToMaster(AggroInfo aggroInfo) {
		Creature master = ((Creature) aggroInfo.getAttacker()).getMaster();
		if (!master.equals(aggroInfo.getAttacker())) {
			aggroList.compute(master.getObjectId(), (key, masterAggroInfo) -> {
				if (masterAggroInfo == null) {
					masterAggroInfo = new AggroInfo(master);
					masterAggroInfo.setHate(1);
				}
				masterAggroInfo.addDamage(aggroInfo.getDamage());
				return masterAggroInfo;
			});
		}
	}

	/**
	 * Clear aggroList
	 */
	public void clear() {
		synchronized (this) {
			if (hateReductionTask != null) {
				hateReductionTask.cancel(true);
				hateReductionTask = null;
			}
		}
		aggroList.clear();
	}

	/**
	 * @param creature
	 * @return aggroInfo
	 */
	public AggroInfo getAggroInfo(Creature creature) {
		AggroInfo ai = aggroList.get(creature.getObjectId());
		if (ai == null) {
			ai = new AggroInfo(creature);
			AggroInfo oldAi = aggroList.putIfAbsent(creature.getObjectId(), ai);
			if (oldAi != null)
				return oldAi;
		}
		return ai;
	}

	/**
	 * @param creature
	 * @return boolean
	 */
	public boolean isHating(Creature creature) {
		return aggroList.containsKey(creature.getObjectId());
	}

	/**
	 * @return aggro list
	 */
	public Collection<AggroInfo> getList() {
		return aggroList.values();
	}

	/**
	 * @return total damage
	 */
	public int getTotalDamage() {
		int totalDamage = 0;
		for (AggroInfo ai : aggroList.values()) {
			totalDamage += ai.getDamage();
		}
		return totalDamage;
	}

	/**
	 * Used to get a list of AggroInfo with npc and player/group/alliance damages combined.
	 *
	 * @return finalDamageList
	 */
	public Collection<AggroInfo> getFinalDamageList(boolean mergeGroupDamage) {
		Map<Integer, AggroInfo> list = new HashMap<>();

		for (AggroInfo ai : aggroList.values()) {
			// Get master only to control damage.
			Creature creature = ((Creature) ai.getAttacker()).getMaster();

			// Don't include damage from creatures outside the known list.
			if (creature == null || !owner.getKnownList().knows(creature)) {
				continue;
			}

			if (mergeGroupDamage) {
				AionObject source;

				if (creature instanceof Player && ((Player) creature).isInTeam()) {
					source = ((Player) creature).getCurrentTeam();
				} else {
					source = creature;
				}

				if (list.containsKey(source.getObjectId())) {
					list.get(source.getObjectId()).addDamage(ai.getDamage());
				} else {
					AggroInfo aggro = new AggroInfo(source);
					aggro.setDamage(ai.getDamage());
					list.put(source.getObjectId(), aggro);
				}
			} else if (list.containsKey(creature.getObjectId())) {
				// Summon or other assistance
				list.get(creature.getObjectId()).addDamage(ai.getDamage());
			} else {
				// Create a separate object so we don't taint current list.
				AggroInfo aggro = new AggroInfo(creature);
				aggro.addDamage(ai.getDamage());
				list.put(creature.getObjectId(), aggro);
			}
		}

		return list.values();
	}

	protected boolean isAware(Creature creature) {
		return creature != null && !creature.equals(owner) && !owner.getEffectController().isAbnormalSet(AbnormalState.SANCTUARY)
			&& (isHating(creature) || creature.isEnemy(owner) || DataManager.TRIBE_RELATIONS_DATA.isHostileRelation(owner.getTribe(), creature.getTribe()));
	}

	@Override
	protected boolean addListenable(AnnotatedMethod annotatedMethod) {
		return annotatedMethod.getAnnotation(Listenable.class) != null;
	}

	@Override
	protected boolean canHaveEventNotifications(AddDamageEvent event) {
		return event.getDamage() > 0;
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
