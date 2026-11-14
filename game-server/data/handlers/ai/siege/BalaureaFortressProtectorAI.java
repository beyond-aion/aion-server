package ai.siege;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * Inggison / Gelkmaros fortress protectors
 * retail AI: BGuard_Chief4_{Li,Da}_ver47(_1) / BGuard_Chief4_ver47(_1).
 * <br>
 * On aggro the boss makes itself invulnerable (Deny Aggression) and summons two protection towers. The invulnerability is removed as soon as both
 * towers are gone (destroyed or out of combat). Wrath of Deity General is not cast while the boss is invulnerable.
 *
 * @author Estrayl
 */
@AIName("balaurea_fortress_protector")
public class BalaureaFortressProtectorAI extends FortressProtectorNpcAI {

	/**
	 * Sent by {@link BalaureaSiegeTowerAI} when a tower dies or leaves combat (retail message 10032), args[0] = the tower.
	 */
	static final int EVENT_TOWER_GONE = 10032;
	private static final int DENY_AGGRESSION = 21772; // sanctuary + shield on self
	private static final int WRATH_OF_DEITY_GENERAL = 18009;
	private static final String TOWER_AI = "balaurea_siege_tower";

	private final AtomicBoolean isStarted = new AtomicBoolean();
	private final List<Npc> towers = new CopyOnWriteArrayList<>();

	public BalaureaFortressProtectorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		startEncounter();
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		startEncounter();
	}

	private void startEncounter() {
		if (!isStarted.compareAndSet(false, true))
			return;
		SkillEngine.getInstance().applyEffectDirectly(DENY_AGGRESSION, getOwner(), getOwner());
		getOwner().setSkillCoolDown(WRATH_OF_DEITY_GENERAL, Long.MAX_VALUE); // retail skips Wrath while the boss is in sanctuary
		int[] towerIds = getTowerIds();
		float[][] towerPositions = getTowerPositions();
		spawnTower(towerIds[0], towerPositions[0]);
		spawnTower(towerIds[1], towerPositions[1]);
	}

	private float[][] getTowerPositions() {
		return switch (getNpcId() / 100) {
			case 2570 -> new float[][] { { 1709.577f, 2248.684f, 329.594f }, { 1740.996f, 2257.034f, 329.594f } }; // Temple of Scales (South)
			case 2573 -> new float[][] { { 877.271f, 2000.683f, 344.266f }, { 907.165f, 1988.223f, 344.266f } }; // Altar of Avarice (North)
			case 2576 -> new float[][] { { 1177.221f, 785.144f, 315.293f }, { 1211.469f, 784.693f, 315.293f } }; // Vorgaltem Citadel (North)
			case 2579 -> new float[][] { { 1861.784f, 1023.897f, 332.485f }, { 1896.033f, 1023.656f, 332.485f } }; // Crimson Temple (South)
			default -> throw new IllegalStateException("No protection tower positions for npc " + getNpcId());
		};
	}

	/**
	 * Tower A and B for this boss. Every fortress has 15 boss templates: x00-x04 Elyos (Dux), x05-x09 Asmodian (Stallari), x10-x14 Balaur (Ulsaruk).
	 */
	private int[] getTowerIds() {
		return switch ((getNpcId() % 100) / 5) {
			case 0 -> new int[] { 297163, 297165 };
			case 1 -> new int[] { 297168, 297170 };
			default -> new int[] { 297173, 297175 };
		};
	}

	private void spawnTower(int npcId, float[] pos) {
		float z = GeoService.getInstance().getZ(getOwner().getWorldId(), pos[0], pos[1], pos[2] + 1, pos[2] - 3, getOwner().getInstanceId());
		if (Float.isNaN(z))
			z = pos[2];
		towers.add((Npc) spawn(npcId, pos[0], pos[1], z, getOwner().getSpawn().getHeading(), 0, TOWER_AI));
	}

	@Override
	protected void handleCustomEvent(int eventId, Object... args) {
		if (eventId == EVENT_TOWER_GONE && args.length > 0 && args[0] instanceof Npc tower)
			onTowerGone(tower);
		else
			super.handleCustomEvent(eventId, args);
	}

	private void onTowerGone(Npc tower) {
		if (towers.remove(tower) && towers.isEmpty() && isStarted.get() && !isDead()) {
			getOwner().getEffectController().removeEffect(DENY_AGGRESSION);
			getOwner().removeSkillCoolDown(WRATH_OF_DEITY_GENERAL);
		}
	}

	private void endEncounter() {
		isStarted.set(false);
		for (Npc tower : towers) {
			towers.remove(tower);
			if (tower.isSpawned())
				tower.getController().delete();
		}
		getOwner().getEffectController().removeEffect(DENY_AGGRESSION);
		getOwner().removeSkillCoolDown(WRATH_OF_DEITY_GENERAL);
	}

	@Override
	public void handleBackHome() {
		endEncounter();
		super.handleBackHome();
	}

	@Override
	protected void handleDied() {
		endEncounter();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		endEncounter();
		super.handleDespawned();
	}
}
