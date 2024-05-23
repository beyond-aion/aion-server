package ai.worlds.heiron;

import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */

@AIName("bollvig")
public class BollvigAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(75, 50, 25);
	private Future<?> firstTask;
	private Future<?> secondTask;
	private Future<?> thirdTask;
	private Future<?> lastTask;

	public BollvigAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		Npc npc = getPosition().getWorldMapInstance().getNpc(204655);
		if (npc != null)
			npc.getController().delete();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 75:
			case 50:
				cancelTask();
				useFirstSkillTree();
				break;
			case 25:
				cancelTask();
				firstSkill();
				break;
		}
	}

	private void useFirstSkillTree() {
		useSkill(17861);// Sleep of Death
		rndSpawnInRange(280802);
		rndSpawnInRange(280802);
		rndSpawnInRange(280803);
		rndSpawnInRange(280803);
		firstSkill();
	}

	private void firstSkill() {
		int hpPercent = getLifeStats().getHpPercentage();
		if (50 >= hpPercent && hpPercent > 25) {
			firstTask = ThreadPoolManager.getInstance().schedule(() -> {
				useSkill(18034);// Nerve Absorption
				rndSpawnInRange(280804);
			}, 10000);
		} else if (hpPercent <= 25) {
			useSkill(18037);// Blood Cell Destruction
		}
		secondTask = ThreadPoolManager.getInstance().schedule(this::skillThree, 31000);
	}

	private void skillThree() {
		useSkill(17899);// Charming Attraction
		thirdTask = ThreadPoolManager.getInstance().schedule(() -> {
			int hpPercent = getLifeStats().getHpPercentage();
			if (hpPercent <= 75 && hpPercent > 50) {
				useSkill(18025);// Curse of Soul
				firstSkill();
			} else if (hpPercent <= 50 && hpPercent > 25) {
				useSkill(18025);// Curse of Soul
				firstSkill();
			} else if (hpPercent <= 25) {
				useSkill(18027);// Mortal Cutting
				lastTask = ThreadPoolManager.getInstance().schedule(this::skillThree, 11000);
			}
		}, 5000);
	}

	private void cancelTask() {
		if (firstTask != null && !firstTask.isDone())
			firstTask.cancel(true);
		else if (secondTask != null && !secondTask.isDone())
			secondTask.cancel(true);
		else if (thirdTask != null && !thirdTask.isDone())
			thirdTask.cancel(true);
		else if (lastTask != null && !lastTask.isDone())
			lastTask.cancel(true);
	}

	private void rndSpawnInRange(int npcId) {
		double angleRadians = Math.toRadians(Rnd.nextFloat(360f));
		float x = (float) (Math.cos(angleRadians) * 10);
		float y = (float) (Math.sin(angleRadians) * 10);
		spawn(npcId, 1001 + x, 2828 + y, 235.66f, (byte) 0);
	}

	private void useSkill(int skillId) {
		SkillEngine.getInstance().getSkill(getOwner(), skillId, 50, getTarget()).useSkill();
	}

	@Override
	protected void handleBackHome() {
		cancelTask();
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		deleteSummons(280802);
		deleteSummons(280803);
		deleteSummons(280804);
		super.handleDespawned();
		if (checkNpc())
			spawn(204655, 1001f, 2828f, 235.66f, (byte) 0);
	}

	@Override
	protected void handleDied() {
		cancelTask();
		deleteSummons(280802);
		deleteSummons(280803);
		deleteSummons(280804);
		super.handleDied();
		if (checkNpc())
			spawn(204655, 1001f, 2828f, 235.66f, (byte) 0);
	}

	private void deleteSummons(int npcId) {
		if (getPosition().getWorldMapInstance().getNpcs(npcId) != null) {
			List<Npc> npcs = getPosition().getWorldMapInstance().getNpcs(npcId);
			for (Npc npc : npcs) {
				npc.getController().delete();
			}
		}
	}

	private boolean checkNpc() {
		WorldMapInstance map = getPosition().getWorldMapInstance();
		return map.getNpc(204655) == null && (map.getNpc(212314) == null || map.getNpc(212314).isDead());
	}
}
