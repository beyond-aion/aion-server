package com.aionemu.gameserver.world.knownlist;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.MapRegion;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * The Knownlist contains every object the owner currently knows (visible and invisble). Which objects are found is controlled by distance and
 * awareness modifiers. Knowing is always a two way relation, so if A knows B, then B also knows A. 
 * 
 * @author -Nemesiss-, kosyachok, Neon
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

	public KnownList(VisibleObject owner) {
		this.owner = owner;
	}

	/**
	 * Do KnownList update.
	 */
	public synchronized void doUpdate() {
		try {
			forgetObjects();
			findVisibleObjects();
		} catch (Exception e) {
			log.error("Exception during KnownList update of {}", owner, e);
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
	private boolean add(VisibleObject object) {
		if (!isAwareOf(object))
			return false;

		if (knownObjects.put(object.getObjectId(), object) == null) {
			if (object instanceof Player) {
				checkKnownPlayersInitialized();
				knownPlayers.put(object.getObjectId(), (Player) object);
			}

			updateVisibleObject(object);
			return true;
		}

		return false;
	}

	/**
	 * Updates the visibility state in this {@link KnownList}, depending on the current see state of the {@link KnownList} owner.<br>
	 * IMPORTANT: This method should only be called if the owner already knows the given object. There are no checks preventing adding unknown objects
	 * to in sight list.
	 */
	public void updateVisibleObject(VisibleObject other) {
		if (owner.canSee(other))
			addToVisibleObjects(other);
		else
			delFromVisibleObjects(other, ObjectDeleteAnimation.FADE_OUT);
	}

	private void addToVisibleObjects(VisibleObject object) {
		if (visualObjects.putIfAbsent(object.getObjectId(), object) == null) {
			try {
				owner.getController().see(object);
			} catch (Exception e) {
				log.error("", e);
			}
			if (object instanceof Player) {
				Pet pet = ((Player) object).getPet(); // pet spawn packet must be sent after SM_PLAYER_INFO, otherwise the pet will not be displayed
				// only add if the pet can know this knownlists owner (currently players), otherwise we get memory leaks if the pet gets dismissed (despawns)
				if (pet != null && pet.getKnownList().isAwareOf(owner))
					addToVisibleObjects(pet);
			}
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
			delFromVisibleObjects(object, animation);
			try {
				owner.getController().notKnow(object);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	/**
	 * Deletes VisibleObject.
	 * 
	 * @param object
	 * @param animation
	 *          - the disappear animation others will see
	 */
	private void delFromVisibleObjects(VisibleObject object, ObjectDeleteAnimation animation) {
		if (visualObjects.remove(object.getObjectId()) != null) {
			if (object instanceof Player player && player.getPet() != null) // pets have no visual/see state, so we sync visibility with their master
				delFromVisibleObjects(player.getPet(), ObjectDeleteAnimation.FADE_OUT);
			try {
				owner.getController().notSee(object, animation);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	/**
	 * forget out of distance objects.
	 */
	private void forgetObjects() {
		for (VisibleObject object : knownObjects.values()) {
			if (!PositionUtil.isInRange(owner, object, getVisibleDistance(object))) {
				del(object, ObjectDeleteAnimation.NONE);
				object.getKnownList().del(owner, ObjectDeleteAnimation.NONE);
			}
		}
	}

	/**
	 * Find objects that are in visibility range.
	 */
	protected void findVisibleObjects() {
		if (!owner.isSpawned())
			return;

		WorldPosition position = owner.getPosition();
		if (owner instanceof Npc) {
			if (((Creature) owner).isFlag()) {
				position.getWorldMapInstance().forEachPlayer(player -> {
					if (add(player)) {
						player.getKnownList().add(owner);
					}
				});
			}
		} else if (owner instanceof Player) {
			position.getWorldMapInstance().forEachNpc(npc -> {
				if (npc.isFlag()) {
					if (add(npc)) {
						npc.getKnownList().add(owner);
					}
				}
			});
		}
		for (MapRegion region : position.getMapRegion().getNeighbours()) {
			for (VisibleObject newObject : region.getObjects().values()) {
				if (newObject == null || owner.equals(newObject))
					continue;

				if (!isAwareOf(newObject))
					continue;

				if (knownObjects.containsKey(newObject.getObjectId())) {
					updateVisibleObject(newObject);
					continue;
				}

				if (!PositionUtil.isInRange(owner, newObject, getVisibleDistance(newObject)))
					continue;

				if (add(newObject))
					newObject.getKnownList().add(owner);
			}
		}
	}

	/**
	 * @return True if the knownlist owner is aware of newObject (should be kept in knownlist)
	 */
	protected boolean isAwareOf(VisibleObject newObject) {
		return true;
	}

	/**
	 * @return Detection radius in meters in which newObject can be added to this knownlist.
	 */
	protected float getVisibleDistance(VisibleObject newObject) {
		return newObject.getVisibleDistance();
	}

	public void forEachNpc(Consumer<Npc> function) {
		forEachNpc(function, Integer.MAX_VALUE);
	}

	public int forEachNpc(Consumer<Npc> function, int iterationLimit) {
		int counter = 0;
		try {
			for (VisibleObject newObject : knownObjects.values()) {
				if (newObject instanceof Npc) {
					if (++counter > iterationLimit)
						break;
					function.accept((Npc) newObject);
				}
			}
		} catch (Exception ex) {
			log.error("Exception when iterating over npcs known by " + owner, ex);
		}
		return counter;
	}

	public void forEachNpcWithOwner(BiConsumer<Npc, VisibleObject> function) {
		forEachNpcWithOwner(function, Integer.MAX_VALUE);
	}

	public int forEachNpcWithOwner(BiConsumer<Npc, VisibleObject> function, int iterationLimit) {
		int counter = 0;
		try {
			for (VisibleObject newObject : knownObjects.values()) {
				if (newObject instanceof Npc) {
					if (++counter > iterationLimit)
						break;
					function.accept((Npc) newObject, owner);
				}
			}
		} catch (Exception ex) {
			log.error("Exception when iterating over npcs known by " + owner, ex);
		}
		return counter;
	}

	public void forEachPlayer(Consumer<Player> function) {
		if (knownPlayers == null)
			return;

		try {
			knownPlayers.values().forEach(player -> {
				if (player != null) // can be null if entry got removed after iterator allocation
					function.accept(player);
			});
		} catch (Exception ex) {
			log.error("Exception when iterating over players known by " + owner, ex);
		}
	}

	public void forEachObject(Consumer<VisibleObject> function) {
		try {
			knownObjects.values().forEach(object -> {
				if (object != null) // can be null if entry got removed after iterator allocation
					function.accept(object);
			});
		} catch (Exception ex) {
			log.error("Exception when iterating over objects known by " + owner, ex);
		}
	}

	public void forEachVisibleObject(Consumer<VisibleObject> function) {
		try {
			visualObjects.values().forEach(object -> {
				if (object != null) // can be null if entry got removed after iterator allocation
					function.accept(object);
			});
		} catch (Exception ex) {
			log.error("Exception when iterating over visual objects seen by " + owner, ex);
		}
	}

	public Map<Integer, VisibleObject> getKnownObjects() {
		return knownObjects;
	}

	public Map<Integer, Player> getKnownPlayers() {
		return knownPlayers != null ? knownPlayers : Collections.emptyMap();
	}

	private void checkKnownPlayersInitialized() {
		if (knownPlayers == null) {
			synchronized (this) {
				if (knownPlayers == null)
					knownPlayers = new ConcurrentHashMap<>();
			}
		}
	}

	/**
	 * @param templateId
	 * @return The visible object with the given template ID or null if not found.
	 */
	public VisibleObject findObject(int templateId) {
		for (VisibleObject v : knownObjects.values()) {
			if (v != null && v.getObjectTemplate().getTemplateId() == templateId) // can be null if entry got removed after iterator allocation
				return v;
		}
		return null;
	}

	public VisibleObject getObject(int targetObjectId) {
		return knownObjects.get(targetObjectId);
	}

	public Player getPlayer(int targetObjectId) {
		return knownPlayers == null ? null : knownPlayers.get(targetObjectId);
	}

}
