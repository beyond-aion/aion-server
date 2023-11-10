package com.aionemu.gameserver.model.siege;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeReward;
import com.aionemu.gameserver.world.zone.SiegeZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.handler.ZoneHandler;

/**
 * @author Sarynth, Source, Wakizashi
 */
public class SiegeLocation implements ZoneHandler {

	private static final Logger log = LoggerFactory.getLogger(SiegeLocation.class);
	public static final int STATE_INVULNERABLE = 0;
	public static final int STATE_VULNERABLE = 1;

	private final SiegeLocationTemplate template;
	private final List<SiegeZoneInstance> zones = new ArrayList<>();
	private final Map<Integer, Creature> creatures = new ConcurrentHashMap<>();
	private final Map<Integer, Player> players = new ConcurrentHashMap<>();
	private SiegeRace siegeRace = SiegeRace.BALAUR;
	private int legionId;
	private boolean vulnerable;
	private int nextState;
	private List<SiegeShield> shields;
	private boolean isUnderShield;
	private boolean canTeleport = true;
	private int occupiedCount;
	private int factionBalance;

	public SiegeLocation(SiegeLocationTemplate template) {
		this.template = template;
	}

	public SiegeLocationTemplate getTemplate() {
		return template;
	}

	/**
	 * @return unique LocationId of this Siege Location
	 */
	public int getLocationId() {
		return template.getId();
	}

	public int getWorldId() {
		return template.getWorldId();
	}

	public SiegeType getType() {
		return template.getType();
	}

	public int getSiegeDuration() {
		return template.getSiegeDuration();
	}

	public List<SiegeReward> getRewards() {
		return template.getSiegeRewards();
	}

	public SiegeRace getRace() {
		return siegeRace;
	}

	public void setRace(SiegeRace siegeRace) {
		this.siegeRace = siegeRace;
	}

	public int getLegionId() {
		return legionId;
	}

	public void setLegionId(int legionId) {
		this.legionId = legionId;
	}

	/**
	 * Next State: 0 invulnerable 1 vulnerable
	 * 
	 * @return nextState
	 */
	public int getNextState() {
		return nextState;
	}

	public void setNextState(int nextState) {
		this.nextState = nextState;
	}

	public boolean isVulnerable() {
		return vulnerable;
	}

	public boolean isUnderShield() {
		return isUnderShield;
	}

	public int getOccupiedCount() {
		return occupiedCount;
	}

	public void increaseOccupiedCount() {
		occupiedCount += 1;
	}

	public void setOccupiedCount(int occupiedCount) {
		this.occupiedCount = occupiedCount;
	}

	/**
	 * Gets the balance between factions of this location. A positive value between 1 and 9 means asmodians are handicapped and will therefore get a
	 * support buff. Vice versa, -1 to -9 means elyos are handicapped and will get a support buff.<br>
	 * 0 = balanced, no support buff<br>
	 * ±1 = weakest support buff<br>
	 * ±9 = strongest support buff<br>
	 * <br>
	 * Should only be used for {@link FortressLocation}.
	 * 
	 * @return factionBalance
	 */
	public int getFactionBalance() {
		return factionBalance;
	}

	/**
	 * In- or decrements the faction balance whether adjustment is positive or negative and limits
	 * it to 9 or -9;
	 * 
	 * @param adjustment
	 */
	public void adjustFactionBalance(int adjustment) {
		factionBalance += Integer.signum(adjustment);
		if (factionBalance > 9)
			factionBalance = 9;
		else if (factionBalance < -9)
			factionBalance = -9;
	}

	public void setFactionBalance(int factionBalance) {
		this.factionBalance = factionBalance;
	}

	public void setUnderShield(boolean value) {
		this.isUnderShield = value;
		if (shields != null) {
			for (SiegeShield shield : shields)
				shield.setEnabled(value);
		}
	}

	public void setShields(List<SiegeShield> shields) {
		this.shields = shields;
	}

	public boolean isCanTeleport(Player player) {
		if (player == null)
			return canTeleport;
		return canTeleport && player.getRace().getRaceId() == getRace().getRaceId();
	}

	public int getLegionGp() {
		return template.getLegionGp();
	}

	public void setCanTeleport(boolean canTeleport) {
		this.canTeleport = canTeleport;
	}

	public void setVulnerable(boolean value) {
		this.vulnerable = value;
	}

	public int getInfluenceValue() {
		return template.getInfluenceValue();
	}

	public List<SiegeZoneInstance> getZone() {
		return zones;
	}

	public void addZone(SiegeZoneInstance zone) {
		zones.add(zone);
		zone.addHandler(this);
	}

	public boolean isInsideLocation(Creature creature) {
		if (zones.isEmpty())
			return false;
		for (SiegeZoneInstance zone : zones)
			if (zone.isInsideCreature(creature))
				return true;
		return false;
	}

	public boolean isInsideLocation(float x, float y, float z) {
		if (zones.isEmpty())
			return false;
		for (SiegeZoneInstance zone : zones)
			if (zone.isInsideCordinate(x, y, z))
				return true;
		return false;
	}

	public void clearLocation() {
	}

	@Override
	public void onEnterZone(Creature creature, ZoneInstance zone) {
		if (!creatures.containsKey(creature.getObjectId())) {
			creatures.put(creature.getObjectId(), creature);
			if (creature instanceof Player) {
				players.put(creature.getObjectId(), (Player) creature);
			}
		}
	}

	@Override
	public void onLeaveZone(Creature creature, ZoneInstance zone) {
		if (!isInsideLocation(creature)) {
			creatures.remove(creature.getObjectId());
			players.remove(creature.getObjectId());
		}
	}

	public void forEachCreature(Consumer<Creature> function) {
		try {
			creatures.values().forEach(c -> {
				if (c != null) // can be null if entry got removed after iterator allocation
					function.accept(c);
			});
		} catch (Exception ex) {
			log.error("Exception while iterating creatures", ex);
		}
	}

	public void forEachPlayer(Consumer<Player> function) {
		try {
			players.values().forEach(p -> {
				if (p != null) // can be null if entry got removed after iterator allocation
					function.accept(p);
			});
		} catch (Exception ex) {
			log.error("Exception while iterating players", ex);
		}
	}
}
