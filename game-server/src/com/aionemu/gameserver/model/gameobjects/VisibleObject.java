package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.VisibleObjectController;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.world.*;
import com.aionemu.gameserver.world.knownlist.KnownList;

/**
 * This class is representing visible objects. It's a base class for all in-game objects that can be spawned in the world at some particular position
 * (such as players, npcs).<br>
 * <br>
 * Objects of this class, as can be spawned in game, can be seen by other visible objects. To keep track of which objects are already "known" by this
 * visible object and which are not, VisibleObject is containing {@link KnownList} which is responsible for holding this information.
 *
 * @author -Nemesiss-
 */
public abstract class VisibleObject extends AionObject {

	private final VisibleObjectTemplate objectTemplate;

	/**
	 * Position of object in the world.
	 */
	protected WorldPosition position;

	/**
	 * KnownList of this VisibleObject.
	 */
	private KnownList knownlist;

	/**
	 * Controller of this VisibleObject
	 */
	private final VisibleObjectController<? extends VisibleObject> controller;

	/**
	 * Visible object's target
	 */
	private VisibleObject target;

	/**
	 * Spawn template of this visibleObject.
	 */
	private final SpawnTemplate spawnTemplate;

	/**
	 * Constructor.
	 *
	 * @param objId
	 * @param objectTemplate
	 */
	public VisibleObject(int objId, VisibleObjectController<? extends VisibleObject> controller, SpawnTemplate spawnTemplate,
		VisibleObjectTemplate objectTemplate, WorldPosition position, boolean autoReleaseObjectId) {
		super(objId, autoReleaseObjectId);
		this.controller = controller;
		this.position = position;
		this.spawnTemplate = spawnTemplate;
		this.objectTemplate = objectTemplate;
	}

	@Override
	public String getName() {
		return objectTemplate.getName();
	}

	public int getInstanceId() {
		return getPosition().getInstanceId();
	}

	/**
	 * Return World map id.
	 */
	public int getWorldId() {
		return getPosition().getMapId();
	}

	/**
	 * Return the WorldType of the current location
	 */
	public WorldType getWorldType() {
		return World.getInstance().getWorldMap(getWorldId()).getWorldType();
	}

	public WorldDropType getWorldDropType() {
		return World.getInstance().getWorldMap(getWorldId()).getWorldDropType();
	}

	/**
	 * Return World position x
	 */
	public float getX() {
		return getPosition().getX();
	}

	/**
	 * Return World position y
	 */
	public float getY() {
		return getPosition().getY();
	}

	/**
	 * Return World position z
	 */
	public float getZ() {
		return getPosition().getZ();
	}

	/**
	 * Heading of the object. Values from <0,120)
	 */
	public byte getHeading() {
		return getPosition().getHeading();
	}

	/**
	 * Return object position
	 *
	 * @return position.
	 */
	public WorldPosition getPosition() {
		return position;
	}

	public WorldMapInstance getWorldMapInstance() {
		return getPosition().getWorldMapInstance();
	}

	/**
	 * Check if object is spawned.
	 *
	 * @return true if object is spawned.
	 */
	public boolean isSpawned() {
		return getPosition().isSpawned();
	}

	/**
	 * @return True if the object is in the world (can be a spawned or despawned object)
	 */
	public boolean isInWorld() {
		return World.getInstance().isInWorld(getObjectId());
	}

	/**
	 * Check if map is instance
	 *
	 * @return true if object in one of the instance maps
	 */
	public boolean isInInstance() {
		return getPosition().isInstanceMap();
	}

	public void clearKnownlist() {
		clearKnownlist(ObjectDeleteAnimation.FADE_OUT);
	}

	public void clearKnownlist(ObjectDeleteAnimation animation) {
		getKnownList().clear(animation);
	}

	public void updateKnownlist() {
		getKnownList().doUpdate();
	}

	/**
	 * @return True, if the object is able to see the given object (basic check without distance validation)
	 */
	public boolean canSee(VisibleObject object) {
		return object != null;
	}

	/**
	 * Set KnownList to this VisibleObject
	 *
	 * @param knownlist
	 */
	public void setKnownlist(KnownList knownlist) {
		this.knownlist = knownlist;
	}

	/**
	 * Returns KnownList of this VisibleObject.
	 *
	 * @return knownList.
	 */
	public KnownList getKnownList() {
		return knownlist;
	}

	/**
	 * Return VisibleObjectController of this VisibleObject
	 *
	 * @return VisibleObjectController.
	 */
	public VisibleObjectController<? extends VisibleObject> getController() {
		return controller;
	}

	/**
	 * @return VisibleObject
	 */
	public final VisibleObject getTarget() {
		return target;
	}

	/**
	 * @param creature
	 */
	public void setTarget(VisibleObject creature) {
		target = creature;
	}

	/**
	 * @param objectId
	 * @return target is object with id equal to objectId
	 */
	public boolean isTargeting(int objectId) {
		return target != null && target.getObjectId() == objectId;
	}

	/**
	 * Return spawnTemplate template of this VisibleObject
	 *
	 * @return SpawnTemplate
	 */
	public SpawnTemplate getSpawn() {
		return spawnTemplate;
	}

	/**
	 * @return the objectTemplate
	 */
	public VisibleObjectTemplate getObjectTemplate() {
		return objectTemplate;
	}

	/**
	 * @param position
	 */
	public void setPosition(WorldPosition position) {
		this.position = position;
	}

	/**
	 * @return Distance in in-game meters how far this object can be seen/detected (a.k.a. known)
	 */
	public float getVisibleDistance() {
		return 95;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + (objectTemplate != null ? "templateId=" + objectTemplate.getTemplateId() + ", " : "") + "objectId="
			+ getObjectId() + ", name=" + getName() + ", position=" + position + "]";
	}
}
