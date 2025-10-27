package com.aionemu.gameserver.model.templates.spawns;

import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;

/**
 * @author xTz, Rolandas
 */
public class SpawnTemplate {

	public static final String NO_AI = "__NO_AI__";
	private float x;
	private float y;
	private float z;
	private byte h;
	private int staticId;
	private int randomWalk;
	private String walkerId;
	private Integer walkerIdx;
	private String anchor;
	private SpawnGroup spawnGroup;
	private String aiName;
	private int state;
	private int creatorId;
	private TemporarySpawn temporarySpawn;

	public SpawnTemplate(SpawnGroup spawnGroup, SpawnSpotTemplate spot) {
		this.spawnGroup = spawnGroup;
		x = spot.getX();
		y = spot.getY();
		z = spot.getZ();
		h = spot.getHeading();
		staticId = spot.getStaticId();
		randomWalk = spot.getRandomWalk();
		walkerId = spot.getWalkerId();
		anchor = spot.getAnchor();
		walkerIdx = spot.getWalkerIndex();
		aiName = spot.getAi();
		state = spot.getState();
		temporarySpawn = spot.getTemporarySpawn();
	}

	public SpawnTemplate(SpawnGroup spawnGroup, float x, float y, float z, byte heading, int randWalk, String walkerId, int staticId) {
		this(spawnGroup, x, y, z, heading, randWalk, walkerId, staticId, 0, null);
	}

	public SpawnTemplate(SpawnGroup spawnGroup, float x, float y, float z, byte heading, int randWalk, String walkerId, int staticId, int creatorId,
		String aiName) {
		this.spawnGroup = spawnGroup;
		this.x = x;
		this.y = y;
		this.z = z;
		h = heading;
		this.randomWalk = randWalk;
		this.walkerId = walkerId;
		this.staticId = staticId;
		this.aiName = aiName;
		this.creatorId = creatorId;
		addTemplate();
	}

	private void addTemplate() {
		spawnGroup.addSpawnTemplate(this);
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public byte getHeading() {
		return h;
	}

	public void setHeading(byte h) {
		this.h = h;
	}

	public int getStaticId() {
		return staticId;
	}

	public void setStaticId(int staticId) {
		this.staticId = staticId;
	}

	public int getRandomWalkRange() {
		return randomWalk;
	}

	public int getNpcId() {
		return spawnGroup.getNpcId();
	}

	public int getWorldId() {
		return spawnGroup.getWorldId();
	}

	public SpawnTemplate changeTemplate(int instanceId) {
		return spawnGroup.reserveRandomFreePoolSpot(instanceId);
	}

	public int getRespawnTime() {
		return spawnGroup.getRespawnTime();
	}

	public void resetPoolSpot(int instanceId) {
		spawnGroup.resetPoolSpot(instanceId, this);
	}

	public TemporarySpawn getTemporarySpawn() {
		return temporarySpawn != null ? temporarySpawn : spawnGroup.getTemporarySpawn();
	}

	public SpawnHandlerType getHandlerType() {
		return spawnGroup.getHandlerType();
	}

	public String getAnchor() {
		return anchor;
	}

	public boolean isNoRespawn() {
		return spawnGroup.getRespawnTime() == 0;
	}

	public boolean hasPool() {
		return spawnGroup.hasPool();
	}

	public String getWalkerId() {
		return walkerId;
	}

	public void setWalkerId(String walkerId) {
		this.walkerId = walkerId;
	}

	public Integer getWalkerIndex() {
		return walkerIdx;
	}

	public SpawnGroup getGroup() {
		return spawnGroup;
	}

	public boolean isTemporarySpawn() {
		return spawnGroup.isTemporarySpawn();
	}

	public boolean isEventSpawn() {
		return getEventTemplate() != null;
	}

	public EventTemplate getEventTemplate() {
		return spawnGroup.getEventTemplate();
	}

	public String getAiName() {
		return aiName;
	}

	public int getState() {
		return state;
	}

	public int getCreatorId() {
		return creatorId;
	}

}
