package ai.instance.empyreanCrucible;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author w4terbomb
 */
@AIName("vanktrist")
public class VanktristAI extends CrucibleBossAI {

	private static final int GLOWING_VORTEX = 282438;
	private static final int UMBRAL_VORTEX = 282439;
	private static final int BLACK_HOLE = 282374;
	private static final int LESSER_BLACK_HOLE = 282415;

	private final List<Npc> summons = new ArrayList<>();
	private Future<?> blackHoleTask, waveTask;
	private boolean umbralWave = true;
	private int strikeIn, shieldIn, deathShieldIn;

	public VanktristAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void onFightStart() {
		blackHoleTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::blackHole, 40000, 60000);
		strikeIn = 1;
		shieldIn = 0;
		deathShieldIn = 60;
	}

	@Override
	protected void onFightTick(int seconds, int hp) {
		for (int phaseHpPercent : new int[] { 80, 50, 20 })
			if (passed(phaseHpPercent))
				handleHpPhase(phaseHpPercent);
		if (hp > 50) {
			if (--strikeIn == 0) {
				cast(19564, 10);
				shieldIn = 15;
				strikeIn = 35;
			}
			if (shieldIn > 0 && --shieldIn == 0)
				cast(19566, 10);
		} else if (--deathShieldIn <= 0) {
			cast(19569, 10);
			deathShieldIn = 60;
		}
	}

	private void handleHpPhase(int phaseHpPercent) {
		PacketSendUtility.broadcastMessage(getOwner(), 1500233);
		getOwner().queueSkill(19567, 10);
		PacketSendUtility.broadcastMessage(getOwner(), 1500235);
		getOwner().queueSkill(19568, 10);
		switchToLowestHpTarget();
		switch (phaseHpPercent) {
			case 50 -> {
				spawnSummons(GLOWING_VORTEX, 3);
				cancelTask(blackHoleTask);
				blackHoleTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::blackHole, 15000, 30000);
				scheduleWave(40000);
			}
			case 20 -> {
				spawnSummons(GLOWING_VORTEX, 2);
				spawnSummons(UMBRAL_VORTEX, 2);
			}
		}
	}

	private void blackHole() {
		if (isDead())
			return;
		Player target = getRandomTarget();
		if (target == null)
			return;
		if (getLifeStats().getHpPercentage() > 50) {
			getOwner().queueSkill(19565, 10, 0, NpcSkillTargetAttribute.RANDOM);
			spawnTemporary(LESSER_BLACK_HOLE, target, 5000);
		} else {
			PacketSendUtility.broadcastMessage(getOwner(), 1500234);
			getOwner().queueSkill(19566, 10, 0, NpcSkillTargetAttribute.RANDOM);
			spawnTemporary(BLACK_HOLE, target, 10000);
		}
	}

	private void scheduleWave(int delay) {
		waveTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isDead())
				return;
			int hp = getLifeStats().getHpPercentage();
			if (hp > 50 && hp <= 60) {
				spawnSummons(BLACK_HOLE, 1);
				Player target = getRandomTarget();
				if (target != null)
					spawnTemporary(LESSER_BLACK_HOLE, target, 10000);
			} else if (hp > 20) {
				spawnSummons(umbralWave ? UMBRAL_VORTEX : GLOWING_VORTEX, 3);
			} else if (umbralWave) {
				spawnSummons(UMBRAL_VORTEX, 2);
				spawnSummons(GLOWING_VORTEX, 2);
			} else {
				spawnSummons(UMBRAL_VORTEX, 4);
			}
			umbralWave = !umbralWave;
			scheduleWave(hp > 20 ? 40000 : 35000);
		}, delay);
	}

	private void spawnSummons(int npcId, int count) {
		for (int i = 0; i < count; i++) {
			VisibleObject summon = rndSpawnInRange(npcId, 1, 5);
			if (summon instanceof Npc npc)
				addSummon(npc);
		}
	}

	private void spawnTemporary(int npcId, Player target, int lifeTime) {
		if (spawn(npcId, target.getX(), target.getY(), target.getZ(), (byte) 0) instanceof Npc npc) {
			addSummon(npc);
			ThreadPoolManager.getInstance().schedule(() -> npc.getController().delete(), lifeTime);
		}
	}

	private void addSummon(Npc npc) {
		synchronized (summons) {
			summons.add(npc);
		}
	}

	private void switchToLowestHpTarget() {
		getAttackablePlayers().stream().min(Comparator.comparingInt(p -> p.getLifeStats().getHpPercentage()))
			.ifPresent(player -> getAggroList().addHate(player, 10000));
	}

	private Player getRandomTarget() {
		return Rnd.get(getAttackablePlayers());
	}

	private List<Player> getAttackablePlayers() {
		List<Player> players = new ArrayList<>();
		getKnownList().forEachPlayer(player -> {
			if (!player.isDead() && getAggroList().isHating(player) && GeoService.getInstance().canSee(getOwner(), player))
				players.add(player);
		});
		return players;
	}

	private void cancelTask(Future<?> task) {
		if (task != null && !task.isDone())
			task.cancel(true);
	}

	@Override
	protected void onFightEnd() {
		cancelTask(blackHoleTask);
		cancelTask(waveTask);
		blackHoleTask = null;
		waveTask = null;
		umbralWave = true;
		synchronized (summons) {
			summons.forEach(npc -> npc.getController().delete());
			summons.clear();
		}
	}

	@Override
	protected void handleNotAtHome() {
		PacketSendUtility.broadcastMessage(getOwner(), 1500236);
		super.handleNotAtHome();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		PacketSendUtility.broadcastMessage(getOwner(), 1500232);
	}

	@Override
	protected void handleDied() {
		PacketSendUtility.broadcastMessage(getOwner(), 1500237);
		super.handleDied();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, ALLOW_DECAY -> false;
			default -> super.ask(question);
		};
	}
}
