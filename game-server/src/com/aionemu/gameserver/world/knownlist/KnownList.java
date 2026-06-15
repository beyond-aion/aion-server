package com.aionemu.gameserver.world.knownlist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.collections.CollectionUtil;
import com.aionemu.gameserver.world.MapRegion;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * The KnownList contains every object the owner currently knows (visible and invisible). Which objects are found is controlled by distance and
 * awareness modifiers.<br>
 * Knowing is always a two-way relation, so if A knows B, then B also knows A. If just one is not aware of the other, both can't know each other.
 * 
 * @author -Nemesiss-, kosyachok, Neon
 */
public class KnownList {

	private static final Logger log = LoggerFactory.getLogger(KnownList.class);

	protected final VisibleObject owner;
	protected final Map<Integer, KnownObject> knownObjects = new ConcurrentHashMap<>();

	public KnownList(VisibleObject owner) {
		this.owner = owner;
	}

	/**
	 * Updates the cached visibility state of all objects in this list and adds or removes objects based on their current distance to the owner.
	 */
	public synchronized void update() {
		forgetObjectsOrUpdateVisibility();
		findVisibleObjects();
	}

	/**
	 * Removes all objects from this list and sends a despawn animation for the owner to all removed players.
	 */
	public synchronized void clear(ObjectDeleteAnimation animation) {
		for (KnownObject object : knownObjects.values()) {
			del(object.get(), ObjectDeleteAnimation.NONE);
			object.get().getKnownList().del(owner, animation);
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
		KnownObject knownObject = knownObjects.get(object.getObjectId());
		return knownObject != null && knownObject.isVisible();
	}

	protected boolean add(VisibleObject object) {
		if (!isAwareOf(object))
			return false;
		KnownObject knownObject = new KnownObject(object);
		if (knownObjects.putIfAbsent(object.getObjectId(), knownObject) != null)
			return false;
		updateVisibility(knownObject);
		return true;
	}

	/**
	 * Updates the object's cached visibility state in this list, depending on the current see state of the owner.
	 */
	public void updateVisibleObject(VisibleObject object) {
		KnownObject knownObject = knownObjects.get(object.getObjectId());
		if (knownObject != null)
			updateVisibility(knownObject);
	}

	private void updateVisibility(KnownObject knownObject) {
		boolean visible = owner.canSee(knownObject.get());
		if (!knownObject.updateVisible(visible))
			return;
		if (visible) {
			notifySee(knownObject.get());
		} else {
			notifyNotSee(knownObject.get(), ObjectDeleteAnimation.FADE_OUT);
		}
		updatePetVisibility(knownObject); // pet spawn packet must be sent after SM_PLAYER_INFO, otherwise the pet will not be displayed
	}

	private void updatePetVisibility(KnownObject knownObject) {
		if (knownObject.get() instanceof Player player && player.getPet() instanceof Pet pet) {
			KnownObject petKnownObject = knownObjects.get(pet.getObjectId());
			if (petKnownObject != null)
				updateVisibility(petKnownObject);
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
		KnownObject knownObject = knownObjects.remove(object.getObjectId());
		if (knownObject != null) {
			if (knownObject.updateVisible(false))
				notifyNotSee(object, animation);
			notifyNotKnow(object);
		}
	}

	private void notifySee(VisibleObject object) {
		try {
			owner.getController().see(object);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private void notifyNotSee(VisibleObject object, ObjectDeleteAnimation animation) {
		try {
			owner.getController().notSee(object, animation);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private void notifyNotKnow(VisibleObject object) {
		try {
			owner.getController().notKnow(object);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * forget out of distance objects or update visible state.
	 */
	private void forgetObjectsOrUpdateVisibility() {
		for (KnownObject object : knownObjects.values()) {
			if (isInRange(object.get())) {
				updateVisibility(object);
			} else {
				del(object.get(), ObjectDeleteAnimation.NONE);
				object.get().getKnownList().del(owner, ObjectDeleteAnimation.NONE);
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
		if (owner instanceof Player) {
			position.getWorldMapInstance().forEachNpc(npc -> {
				if (npc.isFlag() && npc.getKnownList().add(owner)) {
					add(npc);
				}
			});
		}
		for (MapRegion region : position.getMapRegion().getNeighbours()) {
			for (VisibleObject newObject : region.getObjects().values()) {
				if (!isAwareOf(newObject))
					continue;

				if (knows(newObject))
					continue;

				if (!isInRange(newObject))
					continue;

				if (newObject.getKnownList().add(owner))
					add(newObject);
			}
		}
	}

	/**
	 * @return True if the knownlist owner is aware of newObject (should be kept in knownlist)
	 */
	protected boolean isAwareOf(VisibleObject newObject) {
		return newObject != null && !newObject.equals(owner);
	}

	/**
	 * @return Detection radius in meters of this knownlist.
	 */
	protected float getVisibleDistance() {
		return owner.getVisibleDistance();
	}

	private boolean isInRange(VisibleObject newObject) {
		// the two-way relation of KnownLists requires checking the maximum valid distance for both objects to avoid flickering and other display errors
		float distance = Math.max(getVisibleDistance(), newObject.getKnownList().getVisibleDistance());
		return PositionUtil.isInRange(owner, newObject, distance);
	}

	public VisibleObject findObject(Predicate<? super KnownObject> predicate) {
		for (KnownObject value : knownObjects.values()) {
			if (predicate.test(value))
				return value.get();
		}
		return null;
	}

	public VisibleObject getObject(int targetObjectId) {
		KnownObject knownObject = knownObjects.get(targetObjectId);
		return knownObject == null ? null : knownObject.get();
	}

	public Player getPlayer(int targetObjectId) {
		KnownObject knownObject = knownObjects.get(targetObjectId);
		return knownObject != null && knownObject.get() instanceof Player player ? player : null;
	}

	public void forEach(Consumer<? super KnownObject> action) {
		CollectionUtil.forEach(knownObjects.values(), action, () -> "KnownList owner: " + owner);
	}

	public void forEachObject(Consumer<VisibleObject> consumer) {
		forEach(object -> consumer.accept(object.get()));
	}

	public void forEachNpc(Consumer<Npc> consumer) {
		forEach(o -> {
			if (o.get() instanceof Npc npc)
				consumer.accept(npc);
		});
	}

	public void forEachPlayer(Consumer<Player> consumer) {
		forEach(object -> {
			if (object.get() instanceof Player player)
				consumer.accept(player);
		});
	}

	public Stream<KnownObject> stream() {
		return knownObjects.values().stream();
	}

	public Stream<Player> streamPlayers() {
		return stream().filter(o -> o.get() instanceof Player).map(o -> (Player) o.get());
	}

	public Stream<Player> streamVisiblePlayers() {
		return stream().filter(o -> o.isVisible() && o.get() instanceof Player).map(o -> (Player) o.get());
	}
}
