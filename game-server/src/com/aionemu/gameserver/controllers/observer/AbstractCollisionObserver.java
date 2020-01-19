package com.aionemu.gameserver.controllers.observer;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.Ray;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.materials.MaterialActCondition;
import com.aionemu.gameserver.model.templates.materials.MaterialSkill;
import com.aionemu.gameserver.model.templates.world.WeatherEntry;
import com.aionemu.gameserver.services.GameTimeService;
import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.time.gametime.DayTime;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author MrPoke
 * @moved Rolandas
 */
public abstract class AbstractCollisionObserver extends ActionObserver {

	protected Creature creature;
	protected Vector3f oldPos;
	protected Spatial geometry;
	protected byte intentions;
	private final CheckType checkType;
	private AtomicBoolean isRunning = new AtomicBoolean();

	public AbstractCollisionObserver(Creature creature, Spatial geometry, byte intentions, CheckType checkType) {
		this(creature, geometry, intentions, checkType, new Vector3f(creature.getX(), creature.getY(), creature.getZ()));
	}

	public AbstractCollisionObserver(Creature creature, Spatial geometry, byte intentions, CheckType checkType, Vector3f initialPosition) {
		super(ObserverType.MOVE_OR_DIE);
		this.creature = creature;
		this.geometry = geometry;
		this.oldPos = initialPosition;
		this.intentions = intentions;
		this.checkType = checkType;
	}

	@Override
	public void moved() {
		if (!isRunning.getAndSet(true)) {
			ThreadPoolManager.getInstance().execute(new Runnable() {

				@Override
				public void run() {
					try {
						Vector3f pos;
						Vector3f dir;
						if (checkType == CheckType.TOUCH) { // check if we are standing on the geometry (either top or bottom)
							float x = creature.getX();
							float y = creature.getY();
							float z = creature.getZ();
							float zMax = z + 0.05f + creature.getObjectTemplate().getBoundRadius().getUpper();
							float zMin = z - 0.11f;
							if (creature instanceof Player) {
								if (((Player) creature).getMoveController().isJumping() || !((Player) creature).isInGlidingState() && !creature.isFlying()) {
									float geoZ = GeoService.getInstance().getZ(creature.getWorldId(), x, y, z, creature.getInstanceId());
									if (!Float.isNaN(geoZ)) {
										zMin = geoZ - 0.11f;
									}
								}
							}
							pos = new Vector3f(x, y, zMax);
							dir = new Vector3f(pos.getX(), pos.getY(), zMin);
						} else { // check if we passed the geometry (either entering or leaving)
							pos = new Vector3f(creature.getX(), creature.getY(), creature.getZ());
							dir = oldPos.clone();
						}
						Float limit = pos.distance(dir);
						dir.subtractLocal(pos).normalizeLocal();
						Ray r = new Ray(pos, dir);
						r.setLimit(limit);
						CollisionResults results = new CollisionResults(intentions, creature.getInstanceId(), true);
						geometry.collideWith(r, results);
						onMoved(results);
						oldPos = pos;
					} finally {
						isRunning.set(false);
					}
				}
			});
		}
	}

	public abstract void onMoved(CollisionResults result);

	public enum CheckType {
		TOUCH,
		PASS
	}

	public CheckType getCheckType() {
		return checkType;
	}

	MaterialSkill findFirstSkillWithMatchingCondition(List<MaterialSkill> matchingSkills, Creature creature) {
		for (MaterialSkill skill : matchingSkills) {
			if (matchActConditions(skill))
				return skill;
		}
		return null;
	}

	private boolean matchActConditions(MaterialSkill skill) {
		if (skill.getConditions().isEmpty())
			return true;
		for (MaterialActCondition condition : skill.getConditions()) {
			if (condition == MaterialActCondition.NIGHT && GameTimeService.getInstance().getGameTime().getDayTime() == DayTime.NIGHT)
				return true;
			if (condition == MaterialActCondition.SUNNY) { // sunny actually means "not raining" (fireplaces don't burn during rain)
				WeatherEntry weatherEntry = WeatherService.getInstance().findWeatherEntry(creature);
				boolean isRain = weatherEntry.getWeatherName() != null && weatherEntry.getWeatherName().startsWith("RAIN");
				if (!isRain || weatherEntry.isBefore()) // before means "before" the weather (e.g. clouds before rain)
					return true;
			}
		}
		return false;
	}
}
