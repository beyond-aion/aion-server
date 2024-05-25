package ai.instance.drakenspire;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNoLootNpcAI;

/**
 * //moveto 301390000 170 530 1750
 * <br/>
 * <br/>
 * Guide:
 * The battle always begins with Beritra applying three buffs to himself, with "Everlasting Life" is always being the first.
 * The battle is effectively divided into three phases, which are triggered either by reaching a certain HP threshold, or
 * after a set interval.
 * Note: The longer the battle lasts, the more difficult it becomes.
 * <br/>
 * <br/>
 * Seal Mark:
 * - 15s buff applied when killing a Drakenspire Protector (Seal Guardian)
 * - Explodes at the end, dealing ~ 20-22k damage to all targets in range
 * - Removes one of Beritra's buffs if hit by the explosion
 * <br/>
 * <br/>
 * Drakenspire Protector (Seal Guardian):
 * - Spawns on one of the four outer platforms {@link #spawnSealGuardian()}
 * - Spawns every 60s if killed
 * - If not attacked for 60s, he will spawn on a new position
 * - When reset (BACK_HOME), he will also teleport
 * - Spawns a "Ghostly" version of himself in the arena that can respawn after 30s, if killed
 * - Applies "Seal Mark" on the player doing the last hit, if killed
 * <br/>
 * <br/>
 * Skill Pattern:
 * Phase 1: HP 100% - 71% || fight time < 6min
 * - Forgiving loop to introduce the standard skills
 * - Ends with "Pulse Wave" the first more complex mechanic (see below)
 * <br/>
 * <br/>
 * Phase 2: HP 70% - 36% || fight time 6min - 11min
 * - Introduces "Soul Extinction Field" (see below)
 * - Introduces "Dimensional Wave" (see below)
 * <br/>
 * <br/>
 * Phase 3: HP < 36% || fight time >= 11min
 * - Introduces a 4-skill chain right after "Dimensional Wave" that can easily cause a team wipe if not handled carefully
 * <br/>
 * <br/>
 * Pulse Wave ({@link #handlePulseWave()}:
 * - A Y-shaped AoE that deals ~10k damage
 * - Can be executed in one of four patterns
 * - Attacks from the center of the arena to its outer perimeter
 * <br/>
 * <br/>
 * Soul Extinction Field ({@link #handleSoulExtinctionFields()}):
 * - Targets up to three random players
 * - Spawns a field at the player's current location
 * - Activates after 4s, dealing massive damage to the players still standing inside
 * <br/>
 * <br/>
 * Dimensional Wave ({@link #handleDimensionalWave()}):
 * - A 15-second channel skill that plunges parts of the arena into dark fields.
 * - Deals up to 10k damage
 * - Always sequencing the same areas: Northern half -> Southern half -> Platforms
 * - Players should avoid getting hit twice
 * <br/>
 * <br/>
 * Lv 1 (Easy Mode) specifics:
 * - Players will receive help from NPCs after 10s of combat
 * - NPCs will sacrifice themselves and remove all buffs, causing Beritra to use "Rending Shadow" twice
 *
 * @author Estrayl
 */
@AIName("drakenspire_lv1_human_beritra")
public class Lv1HumanBeritraAI extends AggressiveNoLootNpcAI {

	private static final Logger log = LoggerFactory.getLogger("INSTANCE_LOG");
	protected final AtomicBoolean isActivated = new AtomicBoolean();
	private Future<?> spawnTask;
	private long fightStartTime;

	public Lv1HumanBeritraAI(Npc owner) {
		super(owner);
	}

	protected void handleFightStarted() {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (isActivated.get()) // just in case he resets immediately
				spawnFactionHelpers(getAttackingPlayerRace() == Race.ELYOS ? List.of(209736, 209737, 209737) : List.of(209801, 209802, 209802));
		}, 10, TimeUnit.SECONDS);
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		if (isActivated.compareAndSet(false, true)) {
			fightStartTime = System.currentTimeMillis();
			applyBuffs();
			scheduleNewSealGuardianSpawn();
			handleFightStarted(); // Only relevant for Lv1 Beritra
		}
	}

	private void applyBuffs() {
		SkillEngine.getInstance().applyEffectDirectly(21612, getOwner(), getOwner());
		SkillEngine.getInstance().applyEffectDirectly(21611, getOwner(), getOwner());
		SkillEngine.getInstance().applyEffectDirectly(21610, getOwner(), getOwner());
	}

	private void scheduleNewSealGuardianSpawn() {
		if (isActivated.get())
			spawnTask = ThreadPoolManager.getInstance().schedule(this::spawnSealGuardian, 60, TimeUnit.SECONDS);
	}

	/**
	 * Retail sequence
	 */
	private void spawnSealGuardian() {
		Npc sealGuardian;
		if (Rnd.chance() < 25) {
			sealGuardian = (Npc) spawn(855460, 128.621f, 461.719f, 1754.576f, (byte) 15); // Chief_01
		} else if (Rnd.chance() < 33) {
			sealGuardian = (Npc) spawn(855461, 207.780f, 496.081f, 1754.524f, (byte) 40); // Chief_02
		} else if (Rnd.chance() < 50) {
			sealGuardian = (Npc) spawn(855462, 208.671f, 542.410f, 1754.609f, (byte) 67); // Chief_03
		} else {
			sealGuardian = (Npc) spawn(855463, 127.028f, 574.691f, 1754.681f, (byte) 103); // Chief_´04
		}
		addObserver(sealGuardian);
	}

	private void addObserver(Npc sealGuardian) {
		sealGuardian.getObserveController().addObserver(new ActionObserver(ObserverType.DEATH) {

			@Override
			public void died(Creature creature) {
				scheduleNewSealGuardianSpawn();
				super.died(creature);
			}
		});
		sealGuardian.getObserveController().addObserver(new ActionObserver(ObserverType.ABNORMALSETTED) {

			@Override
			public void abnormalsetted(AbnormalState state) {
				spawnSealGuardian();
				super.abnormalsetted(state);
			}
		});
	}

	@Override
	public void onEffectApplied(Effect effect) {
		switch (effect.getSkillId()) {
			case 20834, 20835 -> {
				PacketSendUtility.broadcastMessage(getOwner(), 1501272); // You insects think you have a chance against me?
				handleBuffsRemovedByNpc();
			}
		}
	}

	/**
	 * Retail sequence => Beritra will immediately execute 21604 + 21603 (Rending Shadow);
	 */
	protected void handleBuffsRemovedByNpc() {
		int chainId = getOwner().getGameStats().getLastSkill().getNextChainId();
		NpcSkillEntry entry = getOwner().getSkillList().getNpcSkills().stream().filter(nse -> nse.getChainId() == chainId).findAny().orElse(null);

		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21604, 56, 100, 0, 0)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21603, 56, 100, 0, 0)));
		if (entry != null)
			getOwner().getQueuedSkills().offer(entry);
	}

	/**
	 * Retail: Use one of the following patterns:
	 * 25% => 1 3 6
	 * 33% => 2 5 7
	 * 50% => 3 5 8
	 * Fix => 1 4 7
	 */
	private void handlePulseWave() {
		// 855541
		Npc skill1 = (Npc) spawn(855742, 151.9f, 518.6f, 1749.6f, (byte) 0);
		Npc skill2 = (Npc) spawn(855742, 151.9f, 518.6f, 1749.6f, (byte) 0);
		Npc skill3 = (Npc) spawn(855742, 151.9f, 518.6f, 1749.6f, (byte) 0);

		Npc slave1, slave2, slave3;
		if (Rnd.chance() < 25) {
			slave1 = (Npc) spawn(855745, 136.6f, 496.7f, 1749.9f, (byte) 0); // Pos 1
			slave2 = (Npc) spawn(855747, 137.5f, 534.4f, 1749.9f, (byte) 0); // Pos 3
			slave3 = (Npc) spawn(855750, 180.9f, 515.4f, 1749.9f, (byte) 0); // Pos 6
		} else if (Rnd.chance() < 33) {
			slave1 = (Npc) spawn(855746, 129.6f, 516.2f, 1749.9f, (byte) 0); // Pos 2
			slave2 = (Npc) spawn(855749, 173.7f, 534.0f, 1749.9f, (byte) 0); // Pos 5
			slave3 = (Npc) spawn(855751, 174.3f, 498.2f, 1749.9f, (byte) 0); // Pos 7
		} else if (Rnd.chance() < 50) {
			slave1 = (Npc) spawn(855747, 137.5f, 534.4f, 1749.9f, (byte) 0); // Pos 3
			slave2 = (Npc) spawn(855749, 173.7f, 534.0f, 1749.9f, (byte) 0); // Pos 5
			slave3 = (Npc) spawn(855752, 156.4f, 490.2f, 1749.9f, (byte) 0); // Pos 8
		} else {
			slave1 = (Npc) spawn(855745, 136.6f, 496.7f, 1749.9f, (byte) 0); // Pos 1
			slave2 = (Npc) spawn(855748, 154.6f, 540.7f, 1749.9f, (byte) 0); // Pos 4
			slave3 = (Npc) spawn(855751, 174.3f, 498.2f, 1749.9f, (byte) 0); // Pos 7
		}

		ThreadPoolManager.getInstance().schedule(() -> {
			int skillId = getPulseWaveSkillId();
			SkillEngine.getInstance().getSkill(skill1, skillId, 1, slave1).useNoAnimationSkill();
			SkillEngine.getInstance().getSkill(skill2, skillId, 1, slave2).useNoAnimationSkill();
			SkillEngine.getInstance().getSkill(skill3, skillId, 1, slave3).useNoAnimationSkill();
		}, 500);
		ThreadPoolManager.getInstance().schedule(() -> despawnNpcs(skill1, skill2, skill3, slave1, slave2, slave3), 8000);
	}

	private int getPulseWaveSkillId() {
		return getNpcId() == 236247 ? 21623 : 21828;
	}

	/**
	 * Spawn soul extinction fields on three random players within 50m.
	 */
	private void handleSoulExtinctionFields() {
		List<AggroInfo> playersInRange = getAggroList().getList().stream()
			.filter(ai -> ai.getAttacker() instanceof Player && PositionUtil.isInRange(getOwner(), (Player) ai.getAttacker(), 50))
			.collect(Collectors.toList());

		Collections.shuffle(playersInRange);
		playersInRange.stream().limit(3).forEach(ai -> {
			Player p = (Player) ai.getAttacker();
			spawn(855450, p.getX(), p.getY(), p.getZ(), (byte) 0);
		});
	}

	/**
	 * Retail: NPCs are spawned solely to display effects.
	 * There is a new zone mechanic "activate_skillarea" which should handle the calc, which player needs to be affected.
	 * <p>
	 * To keep it simple, we just calculate the effects within the NPC's AI.
	 */
	private void handleDimensionalWave() {
		// equals area_id="10"
		ThreadPoolManager.getInstance().schedule(() -> spawn(855435, 151.991f, 518.583f, 1749.5945f, (byte) 90), 3000);
		// equals area_id="20"
		ThreadPoolManager.getInstance().schedule(() -> spawn(855435, 152.002f, 518.548f, 1749.5945f, (byte) 30), 7500);
		// equals area_id="30"
		ThreadPoolManager.getInstance().schedule(() -> {
			spawn(856300, 126.977f, 574.736f, 1754.6809f, (byte) 0);
			spawn(856300, 208.552f, 542.472f, 1754.6082f, (byte) 0);
			spawn(856300, 177.031f, 458.650f, 1759.8838f, (byte) 0);
			spawn(856300, 176.057f, 579.624f, 1760.0452f, (byte) 0);
			spawn(856300, 207.733f, 496.071f, 1754.5236f, (byte) 0);
			spawn(856300, 128.722f, 461.584f, 1754.5775f, (byte) 0);
		}, 12000);
	}

	@Override
	public void onStartUseSkill(SkillTemplate st, int level) {
		switch (st.getSkillId()) {
			case 21601 -> { // Pulse Wave
				handlePulseWave();
				addHateToRandomTarget();
			}
			case 21602 -> handleDimensionalWave(); // Dimensional Wave
		}
	}

	private void addHateToRandomTarget() {
		List<AggroInfo> attackingPlayers = getAggroList().getList().stream().filter(ai -> ai.getAttacker() instanceof Player player && !player.isDead())
			.toList();
		AggroInfo aggroInfo = Rnd.get(attackingPlayers);
		if (aggroInfo != null)
			aggroInfo.addHate(100000);
	}

	@Override
	public void onEndUseSkill(SkillTemplate st, int level) {
		switch (st.getSkillId()) {
			case 21609 -> { // Soul Extinction Field
				switch (level) {
					case 57 -> PacketSendUtility.broadcastMessage(getOwner(), 1501269); // You're not too bad for an insect! | Could also be 1501274
					case 58 -> PacketSendUtility.broadcastMessage(getOwner(), 1501270); // I'm not playing anymore! | Could also be 1501275
				}
				handleSoulExtinctionFields();
			}
		}
	}

	/**
	 * Deviation to retail implementation:
	 * Guards should spawn with a distance of 8m relative to Beritra's current position.
	 * To avoid spawning them outside the arena, we will spawn them relative to the center of the arena.
	 */
	protected void spawnFactionHelpers(List<Integer> npcIds) {
		SpawnTemplate st = getSpawnTemplate();
		for (int npcId : npcIds) {
			float distance = Rnd.nextFloat() * 5;
			double angleRadians = Math.toRadians(Rnd.nextFloat(360f));
			float x = st.getX() + (float) (Math.cos(angleRadians) * distance);
			float y = st.getY() + (float) (Math.sin(angleRadians) * distance);
			Npc helper = (Npc) spawn(npcId, x, y, st.getZ(), (byte) 0);
			ThreadPoolManager.getInstance().schedule(() -> {
				helper.setTarget(getOwner());
				helper.getAggroList().addHate(getOwner(), 100000);
				helper.getMoveController().moveToTargetObject();
			}, 500);
		}
	}

	private void despawnNpcs(Npc... npcs) {
		for (Npc npc : npcs)
			if (npc != null)
				npc.getController().deleteIfAliveOrCancelRespawn();
	}

	private void despawnNpcsById(int... npcIds) {
		getPosition().getWorldMapInstance().getNpcs(npcIds).forEach(npc -> npc.getController().deleteIfAliveOrCancelRespawn());
	}

	private boolean isTargetInsideArena() {
		SpawnTemplate st = getSpawnTemplate();
		return PositionUtil.isInRange(getTarget(), st.getX(), st.getY(), st.getZ(), 28);
	}

	@Override
	protected void handleTargetTooFar() {
		if (!isTargetInsideArena()) {
			getAggroList().stopHating(getTarget());
			Creature mostHated = getAggroList().getMostHated();
			if (mostHated != null)
				onCreatureEvent(AIEventType.TARGET_CHANGED, mostHated);
			else
				onGeneralEvent(AIEventType.TARGET_GIVEUP);
			return;
		}
		super.handleTargetTooFar();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		despawnNpcsById(209734, 209735, 209736, 209737, 209799, 209800, 209801, 209802, 855446, 855460, 855461, 855462, 855463);
		cancelTasks();
		fightStartTime = 0;
		isActivated.set(false);
	}

	@Override
	protected void handleDied() {
		cancelTasks();
		logMetrics();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelTasks();
		isActivated.set(false);
		despawnNpcsById(209734, 209735, 209736, 209737, 209799, 209800, 209801, 209802, 855446, 855460, 855461, 855462, 855463);
		super.handleDespawned();
	}

	protected Race getAttackingPlayerRace() {
		return getKnownList().getKnownPlayers().values().stream().filter(p -> !p.isStaff()).map(Player::getRace).findAny().orElse(Race.ELYOS);
	}

	private void cancelTasks() {
		if (spawnTask != null && !spawnTask.isDone())
			spawnTask.cancel(true);
	}

	private void logMetrics() {
		long fullFightTime = (System.currentTimeMillis() - fightStartTime) / 1000;
		String damageDealt = getAggroList().getFinalDamageList(false).stream().sorted((Comparator.comparingInt(AggroInfo::getDamage).reversed()))
			.map(ai -> String.format("%s (ID: %d, Dmg: %d)", ai.getAttacker().getName(), ai.getAttacker().getObjectId(), ai.getDamage()))
			.collect(Collectors.joining(", "));

		log.info("[{}] {} (ID:{}) was killed in {}s. Damage List: {}", getPosition().getWorldMapInstance().getTemplate().getName(), getOwner().getName(),
			getNpcId(), fullFightTime, damageDealt);
	}
}
