package com.aionemu.gameserver.world.knownlist;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.world.MapRegion;

/**
 * KnownList.
 * 
 * @author -Nemesiss-
 * @modified kosyachok, Neon
 */
public class KnownList {

	private static final Logger log = LoggerFactory.getLogger(KnownList.class);

	/**
	 * Owner of this KnownList.
	 */
	protected final VisibleObject owner;

	/**
	 * List of objects that this KnownList owner knows
	 */
	protected final Map<Integer, VisibleObject> knownObjects = new ConcurrentHashMap<>();

	/**
	 * List of player that this KnownList owner knows
	 */
	protected volatile Map<Integer, Player> knownPlayers;

	/**
	 * List of objects that this KnownList owner sees
	 */
	protected final Map<Integer, VisibleObject> visualObjects = new ConcurrentHashMap<>();

	/**
	 * List of players that this KnownList owner sees
	 */
	protected volatile Map<Integer, Player> visualPlayers;

	private ReentrantLock lock = new ReentrantLock();

	/**
	 * @param owner
	 */
	public KnownList(VisibleObject owner) {
		this.owner = owner;
	}

	/**
	 * Do KnownList update.
	 */
	public void doUpdate() {
		lock.lock();
		try {
			forgetObjects();
			findVisibleObjects();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Clear known list. Used when object is despawned.
	 * 
	 * @param animation
	 *          - the animation others will see on despawn
	 */
	public void clear(ObjectDeleteAnimation animation) {
		for (VisibleObject object : knownObjects.values())
			object.getKnownList().del(owner, animation);

		knownObjects.clear();
		if (knownPlayers != null) {
			knownPlayers.clear();
		}
		visualObjects.clear();
		if (visualPlayers != null) {
			visualPlayers.clear();
		}
	}

	/**
	 * Checks if owner knows the object.
	 */
	public boolean knows(VisibleObject object) {
		return knownObjects.containsKey(object.getObjectId());
	}

	/**
	 * Checks if owner sees the object.
	 */
	public boolean sees(VisibleObject object) {
		return visualObjects.containsKey(object.getObjectId());
	}

	/**
	 * Add VisibleObject to this KnownList.
	 * 
	 * @param object
	 */
	protected boolean add(VisibleObject object) {
		if (!isAwareOf(object))
			return false;

		if (knownObjects.put(object.getObjectId(), object) == null) {
			if (object instanceof Player) {
				checkKnownPlayersInitialized();
				knownPlayers.put(object.getObjectId(), (Player) object);
			}

			if (object instanceof Player && !owner.canSee((Player) object))
				return true;
			if (object instanceof Pet && !sees(((Pet) object).getMaster()) && !owner.equals(((Pet) object).getMaster()))
				return true; // pet won't display if client doesn't already see the master (spawn order is important, so playercontroller must handle it)

			addVisualObject(object);
			return true;
		}

		return false;
	}

	public void addVisualObject(VisibleObject object) {
		if (visualObjects.put(object.getObjectId(), object) == null) {
			if (object instanceof Player) {
				checkVisiblePlayersInitialized();
				visualPlayers.put(object.getObjectId(), (Player) object);
			}
			owner.getController().see(object);
		}
	}

	/**
	 * Removes VisibleObject from this KnownList and deletes it.
	 * 
	 * @param object
	 * @param animation
	 *          - the disappear animation others will see
	 */
	private void del(VisibleObject object, ObjectDeleteAnimation animation) {
		if (knownObjects.remove(object.getObjectId()) != null) {
			if (knownPlayers != null)
				knownPlayers.remove(object.getObjectId());
			delVisualObject(object, animation);
		}
	}

	/**
	 * Deletes VisibleObject.
	 * 
	 * @param object
	 * @param animation
	 *          - the disappear animation others will see
	 */
	public void delVisualObject(VisibleObject object, ObjectDeleteAnimation animation) {
		if (visualObjects.remove(object.getObjectId()) != null) {
			if (visualPlayers != null)
				visualPlayers.remove(object.getObjectId());
			owner.getController().notSee(object, animation);
		}
	}

	/**
	 * forget out of distance objects.
	 */
	private void forgetObjects() {
		for (VisibleObject object : knownObjects.values()) {
			if (!checkObjectInRange(object) && !object.getKnownList().checkReversedObjectInRange(owner)) {
				del(object, ObjectDeleteAnimation.NONE);
				object.getKnownList().del(owner, ObjectDeleteAnimation.NONE);
			}
		}
	}

	/**
	 * Find objects that are in visibility range.
	 */
	protected void findVisibleObjects() {
		if (owner == null || !owner.isSpawned())
			return;

		if (owner instanceof Npc) {
			if (((Creature) owner).isFlag()) {
				owner.getPosition().getWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player player) {
						if (add(player)) {
							player.getKnownList().add(owner);
						}
					}
				});
			}
		} else if (owner instanceof Player) {
			for (Npc npc : owner.getPosition().getWorldMapInstance().getNpcs()) {
				if (npc.isFlag()) {
					if (add(npc)) {
						npc.getKnownList().add(owner);
					}
				}
			}
		}
		MapRegion[] regions = owner.getActiveRegion().getNeighbours();
		for (int i = 0; i < regions.length; i++) {
			MapRegion r = regions[i];
			Map<Integer, VisibleObject> objects = r.getObjects();
			for (Entry<Integer, VisibleObject> e : objects.entrySet()) {
				VisibleObject newObject = e.getValue();
				if (newObject == null || owner.equals(newObject))
					continue;

				if (!isAwareOf(newObject))
					continue;

				if (knownObjects.containsKey(newObject.getObjectId()))
					continue;

				if (!checkObjectInRange(newObject) && !newObject.getKnownList().checkReversedObjectInRange(owner))
					continue;

				/**
				 * New object is not known.
				 */
				if (add(newObject)) {
					newObject.getKnownList().add(owner);
				}
			}
		}
	}

	/**
	 * Whether knownlist owner aware of found object (should be kept in knownlist)
	 * 
	 * @param newObject
	 * @return
	 */
	protected boolean isAwareOf(VisibleObject newObject) {
		return true;
	}

	protected boolean checkObjectInRange(VisibleObject newObject) {
		// flags/raidmonsters should not be deleted while moving.
		if (newObject instanceof Npc && (((Npc) newObject).isFlag() || ((Npc) newObject).isRaidMonster())) {
			return newObject.getWorldId() == owner.getWorldId();
		} else if (Math.abs(owner.getZ() - newObject.getZ()) > owner.getMaxZVisibleDistance()) {
			return false;
		} else {
			return MathUtil.isInRange(owner, newObject, owner.getVisibilityDistance());
		}
	}

	/**
	 * Check can be overriden if new object has different known range and that value should be used
	 * 
	 * @param newObject
	 * @return
	 */
	protected boolean checkReversedObjectInRange(VisibleObject newObject) {
		return false;
	}

	public void doOnAllNpcs(Visitor<Npc> visitor) {
		doOnAllNpcs(visitor, Integer.MAX_VALUE);
	}

	public int doOnAllNpcs(Visitor<Npc> visitor, int iterationLimit) {
		int counter = 0;
		try {
			for (Entry<Integer, VisibleObject> e : knownObjects.entrySet()) {
				VisibleObject newObject = e.getValue();
				if (newObject instanceof Npc) {
					if ((++counter) == iterationLimit)
						break;
					visitor.visit((Npc) newObject);
				}
			}
		} catch (Exception ex) {
			log.error("Exception when running visitor on all npcs", ex);
		}
		return counter;
	}

	public void doOnAllNpcsWithOwner(VisitorWithOwner<Npc, VisibleObject> visitor) {
		doOnAllNpcsWithOwner(visitor, Integer.MAX_VALUE);
	}

	public int doOnAllNpcsWithOwner(VisitorWithOwner<Npc, VisibleObject> visitor, int iterationLimit) {
		int counter = 0;
		try {
			for (Entry<Integer, VisibleObject> e : knownObjects.entrySet()) {
				VisibleObject newObject = e.getValue();
				if (newObject instanceof Npc) {
					if ((++counter) == iterationLimit)
						break;
					visitor.visit((Npc) newObject, owner);
				}
			}
		} catch (Exception ex) {
			log.error("Exception when running visitor on all npcs", ex);
		}
		return counter;
	}

	public void doOnAllPlayers(Visitor<Player> visitor) {
		if (knownPlayers == null) {
			return;
		}
		try {
			for (Entry<Integer, Player> e : knownPlayers.entrySet()) {
				Player player = e.getValue();
				if (player != null) {
					visitor.visit(player);
				}
			}
		} catch (Exception ex) {
			log.error("Exception when running visitor on all players", ex);
		}
	}

	public void doOnAllObjects(Visitor<VisibleObject> visitor) {
		try {
			for (Entry<Integer, VisibleObject> e : knownObjects.entrySet()) {
				VisibleObject newObject = e.getValue();
				if (newObject != null) {
					visitor.visit(newObject);
				}
			}
		} catch (Exception ex) {
			log.error("Exception when running visitor on all objects", ex);
		}
	}

	public Map<Integer, VisibleObject> getKnownObjects() {
		return knownObjects;
	}

	public Map<Integer, VisibleObject> getVisibleObjects() {
		return visualObjects;
	}

	public Map<Integer, Player> getKnownPlayers() {
		return knownPlayers != null ? knownPlayers : Collections.<Integer, Player> emptyMap();
	}

	public Map<Integer, Player> getVisiblePlayers() {
		return visualPlayers != null ? visualPlayers : Collections.<Integer, Player> emptyMap();
	}

	final void checkKnownPlayersInitialized() {
		if (knownPlayers == null) {
			synchronized (this) {
				if (knownPlayers == null) {
					knownPlayers = new ConcurrentHashMap<Integer, Player>();
				}
			}
		}
	}

	final void checkVisiblePlayersInitialized() {
		if (visualPlayers == null) {
			synchronized (this) {
				if (visualPlayers == null) {
					visualPlayers = new ConcurrentHashMap<Integer, Player>();
				}
			}
		}
	}

	public VisibleObject getObject(int targetObjectId) {
		return knownObjects.get(targetObjectId);
	}

	/**
	 * Updates visibility of all known players, depending on the current see state of the owner of this {@link KnownList}.
	 */
	public void updateVisiblePlayers() {
		if (!(owner instanceof Player))
			return;

		doOnAllPlayers(other -> {
			boolean canSee = owner.canSee(other);
			boolean isSee = sees(other);

			if (canSee && !isSee) {
				addVisualObject(other);
				if (other.getPet() != null)
					addVisualObject(other.getPet());
			} else if (!canSee && isSee) {
				delVisualObject(other, ObjectDeleteAnimation.FADE_OUT);
				if (other.getPet() != null)
					delVisualObject(other.getPet(), ObjectDeleteAnimation.FADE_OUT);
			}
		});
	}
}
