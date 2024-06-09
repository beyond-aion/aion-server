package ai.instance.dragonLordsRefuge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Estrayl
 */
@AIName("hm_tiamat_weakened_dragon")
public class HM_TiamatWeakenedDragonAI extends TiamatWeakenedDragonAI {

	private List<Integer> gravityTornadoDistanceLeft = new ArrayList<>();
	private List<Integer> gravityTornadoDistanceRight = new ArrayList<>();

	public HM_TiamatWeakenedDragonAI(Npc owner) {
		super(owner);
		hpPhases = new HpPhases(50, 25, 20, 15, 5);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 50:
				scheduleDivisiveCreations(60000);
				break;
			case 25:
				scheduleInfinitePain();
				spawnGravityCrusher();
				break;
			case 20:
			case 15:
			case 5:
				spawnGravityCrusher();
				break;
		}
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 20922: // Ultimate Atrocity
				spawnAtrocity(25);
				break;
			case 20924: // Ultimate Atrocity
				spawnAtrocity(0);
				break;
			case 20926: // Ultimate Atrocity
				spawnAtrocity(95);
				break;
		}
	}

	private void spawnAtrocity(int heading) {
		spawn(283238, getPosition().getX(), getPosition().getY(), getPosition().getZ(), (byte) heading);
	}

	@Override
	protected int calculateAtrocitySkillId() {
		int[] accumulatedAnglesAndPlayers = new int[2];

		getKnownList().forEachPlayer(p -> {
			if (PositionUtil.isInRange(getOwner(), p, 46)) {
				accumulatedAnglesAndPlayers[0] += Math.round(PositionUtil.calculateAngleFrom(getOwner(), p));
				accumulatedAnglesAndPlayers[1]++;
			}
		});
		if (accumulatedAnglesAndPlayers[1] == 0)
			return super.calculateAtrocitySkillId();

		int medianAngle = accumulatedAnglesAndPlayers[0] / accumulatedAnglesAndPlayers[1];
		if (medianAngle <= 30 || medianAngle >= 330)
			return 20924;
		else if (medianAngle < 150)
			return 20922;
		else
			return 20926;
	}

	@Override
	protected void scheduleSinkingSand() {
		spawnTasks.add(ThreadPoolManager.getInstance().scheduleAtFixedRate(this::spawnSinkingSand, 60000, 50000));
	}

	private void spawnSinkingSand() {
		int density = (int) Math.min((100 - getLifeStats().getHpPercentage()) * 1.5f, 100);
		int delay = 250;
		for (int i = 0; i < density; i++)
			spawnTasks.add(ThreadPoolManager.getInstance().schedule(this::spawnSinkingSandOnRndPos, delay += 250));
	}

	private void spawnSinkingSandOnRndPos() {
		float distance = Rnd.get(60, 420) * 0.1f;
		double radian = Math.toRadians(Rnd.get(-900, 900) * 0.1f);
		float x = (float) (Math.cos(radian) * distance);
		float y = (float) (Math.sin(radian) * distance);
		spawn(283135, getPosition().getX() + x, getPosition().getY() + y, 417.4f, (byte) 0);
	}

	@Override
	protected void scheduleDivisiveCreations(int delay) {
		spawnTasks.add(ThreadPoolManager.getInstance().schedule(() -> {
			if (hasAggro.get() && !isDead() && delay > 30000) {
				spawn(856042, 464.24f, 462.26f, 417.4f, (byte) 18);
				spawn(856042, 542.79f, 465.03f, 417.4f, (byte) 43);
				spawn(856042, 541.79f, 563.71f, 417.4f, (byte) 74);
				spawn(856042, 465.79f, 565.43f, 417.4f, (byte) 100);
				scheduleDivisiveCreations(delay - 10000);
			}
		}, delay));
	}

	@Override
	protected void spawnGravityCrusher() {
		if (gravityTornadoDistanceLeft.isEmpty() || gravityTornadoDistanceRight.isEmpty())
			return;

		int distance = gravityTornadoDistanceRight.remove(Rnd.nextInt(gravityTornadoDistanceRight.size()));
		spawnGravityTornadoByDistAndAngle(distance, 330);

		distance = gravityTornadoDistanceLeft.remove(Rnd.nextInt(gravityTornadoDistanceLeft.size()));
		spawnGravityTornadoByDistAndAngle(distance, 30);
	}

	private void spawnGravityTornadoByDistAndAngle(int distance, int angle) {
		double radian = Math.toRadians(angle);
		float x = (float) (Math.cos(radian) * distance);
		float y = (float) (Math.sin(radian) * distance);
		spawn(856046, getPosition().getX() + x, getPosition().getY() + y, 417.4f, (byte) 0);
	}

	@Override
	protected void spawnInfinitePain() {
		spawn(856048, 508.32f, 515.18f, 417.4f, (byte) 0);
	}

	@Override
	protected void despawnAdds() {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		deleteNpcs(instance.getNpcs(856045));
		deleteNpcs(instance.getNpcs(856041));
		deleteNpcs(instance.getNpcs(856042));
		deleteNpcs(instance.getNpcs(856046));
	}

	private void addGravityTornadoSpots() {
		gravityTornadoDistanceLeft.clear();
		gravityTornadoDistanceRight.clear();
		Collections.addAll(gravityTornadoDistanceLeft, 7, 14, 21, 28, 35, 42);
		Collections.addAll(gravityTornadoDistanceRight, 7, 14, 21, 28, 35, 42);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addGravityTornadoSpots();
	}

	@Override
	protected void handleBackHome() {
		addGravityTornadoSpots();
		super.handleBackHome();
	}
}
