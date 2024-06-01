package ai.instance.danuarReliquary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.animations.AttackHandAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HEADING_UPDATE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

import ai.AggressiveNpcAI;

/**
 * @author Yeats
 */
@AIName("enraged_queen_modor")
public class EnragedQueenModorAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(100, 90, 70, 52, 25, 10);
	private AtomicInteger stage = new AtomicInteger();
	private List<Vector3f> platformLocations = new ArrayList<>(5);
	// weakened reliquary jotun, weakened idean lapilima, weakened idean obscura, weakened modor guardian, weakened danuar ghost, weakened hoarfrost acheron drake,
	private int[] monsterIds = new int[] {284659, 284660, 284661, 284662, 284663, 284664};
	private float multiplier = 1f;
	private Vector3f nextPosition = null;
	private Future<?> positionUpdateTask = null;

	public EnragedQueenModorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPlatformLocations();
		if (isCrazedModor()) {
			monsterIds = new int[] {856493, 856494, 856495, 856496, 856497, 856498};
		}
	}

	@Override
	public AttackHandAnimation modifyAttackHandAnimation(AttackHandAnimation attackHandAnimation) {
		return Rnd.get(AttackHandAnimation.values());
	}

	@Override
	public int modifyInitialSkillDelay(int delay) {
		return 0;
	}

	@Override
	protected void handleAttack(Creature creature) {
		hpPhases.tryEnterNextPhase(this);
		super.handleAttack(creature);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 100:
				queueSkill(21171, 1); // Grendal's Explosive Wrath
				queueSkill(21169, 1, 4000); // buff
				break;
			case 90:
				queueSkill(21165, 1, 4000); // rend space
				queueSkill(21170, 1, 4000); // Grendal's Rage
				break;
			case 70:
				queueSkill(21165, 2); // rend space
				queueSkill(21176, 1); // electric vengeance
				queueSkill(21229, 1, 4000); // electric wrath
				queueSkill(21742, 1, 4000); // subzero malice
				queueSkill(21743, 1, 4000); // rushing subzero malice
				break;
			case 52:
				queueSkill(21165, 4); // rend space
				queueSkill(21268, 1); // demented scream
				queueSkill(21269, 1, 4000); // spiteful roar
				queueSkill(21742, 1, 4000); // subzero malice
				queueSkill(21743, 1, 4000); // rushing subzero malice
				queueSkill(21165, 5, 4000); // rend space
				queueSkill(21170, 1, 2500); // Grendal's Rage
				break;
			case 25:
				queueSkill(21165, 6); // rend space
				queueSkill(21176, 1); // electric vengeance
				queueSkill(21229, 1); // electric wrath
				queueSkill(21742, 1); // subzero malice
				queueSkill(21743, 1); // rushing subzero malice
				queueSkill(21165, 7, 4000); // rend space
				queueSkill(21170, 1, 0); // Grendal's Rage
				break;
			case 10:
				queueSkill(21165, 8); // rend space
				queueSkill(21176, 1); // electric vengeance
				queueSkill(21229, 1, 4000); // electric wrath
				queueSkill(21165, 9, 4000); // rend space
				queueSkill(21170, 1, 0); // Grendal's Rage
				break;
		}
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		return damage * multiplier;
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		multiplier = 0.5f;
		switch (skillTemplate.getSkillId()) {
			case 21171:
				multiplier = 1.2f;
				if (stage.get() == 0) {
					PacketSendUtility.broadcastMessage(getOwner(), 1500743);
				}
				break;
			case 21165:
				if (skillLevel != 10) {
					stage.set(skillLevel);
					if (skillLevel == 1 || skillLevel == 5 || skillLevel == 7 || skillLevel == 9) {
						PacketSendUtility.broadcastMessage(getOwner(), 1500750);
					} else if (skillLevel == 2 || skillLevel == 6 || skillLevel == 8) {
						nextPosition = getRandomPosFromCenter(8);
						spawn(284385, nextPosition.x, nextPosition.y, nextPosition.getZ(), (byte) 0);
					} else if (skillLevel== 4) {
						PacketSendUtility.broadcastMessage(getOwner(), 1500751);
					}
				}
				break;
			case 21176:
				multiplier = 0.35f;
				break;
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		multiplier = 1f;
		int curStage = stage.get();
		switch (skillTemplate.getSkillId()) {
			case 21165:
				switch (curStage) {
					case 1:
					case 3:
					case 5:
					case 7:
					case 9:
						if (skillLevel != 10 && skillLevel != 3) {
							spawnAdds(curStage);
						}
						if (shouldUsePlatformSkills(skillLevel)) {
							queueSkill(21742, 1, getNextSkillTimeFor(curStage)); // subzero malice
							queueSkill(21743, 1, getNextSkillTimeFor(curStage)); // rushing subzero malice
						}
						break;
				}
				updatePosition(curStage);
				break;
			case 21743:
				switch (curStage) {
					case 1:
					case 3:
					case 5:
					case 7:
					case 9:
						if (shouldUsePlatformSkills(skillLevel)) {
							queueSkill(21165, 10, 5000 - curStage * 350); // rend space
							queueSkill(21179, 1, getNextSkillTimeFor(curStage)); // ice storm
						}
						break;
					case 2:
						if (getLifeStats().getHpPercentage() >= 55) {
							queueSkill(21165, 3, 4000); // rend space
							queueSkill(21170, 1, 3000); // Grendal's Rage
						}
						break;
				}
				break;
			case 21179: // ice storm
				Vector3f pos = getRandomPosFromCenter(Rnd.nextInt(9));
				spawn(284528, pos.getX(), pos.getY(), pos.getZ(), (byte) 10);
				break;
		}
	}

	private void updatePosition(final int curStage) {
		positionUpdateTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!getOwner().isDead() && getOwner().isSpawned()) {
				switch (curStage) {
					case 1:
					case 3: // switch platforms
					case 5:
					case 7:
					case 9:
						Vector3f loc = platformLocations.remove(curStage > 3 ? Rnd.get(0, 3) : 0);
						platformLocations.add(loc); // re-add to be able to teleport to loc again
						World.getInstance().updatePosition(getOwner(), loc.getX(), loc.getY(), loc.getZ(),
								PositionUtil.getHeadingTowards(loc.getX(), loc.getY(), 256.62f, 257.79f));
						break;
					case 4:
						Vector3f lookAt = null;
						Creature cr = getOwner().getAggroList().getMostHated();
						if (cr != null && !cr.isDead()) {
							lookAt = new Vector3f(cr.getX(), cr.getY(), cr.getZ());
						}
						World.getInstance().updatePosition(getOwner(), 256.62f, 257.79f, 241.79f,
								lookAt == null ? (byte) Rnd.nextInt(120) : PositionUtil.getHeadingTowards(256.62f, 257.79f, lookAt.getX(), lookAt.getY()));
						break;
					case 2:
					case 6:
					case 8:
						World.getInstance().updatePosition(getOwner(), nextPosition.getX(), nextPosition.getY(), nextPosition.getZ(),
								PositionUtil.getHeadingTowards(nextPosition.getX(), nextPosition.getY(), 256.62f, 257.79f));
						break;
				}
				PacketSendUtility.broadcastPacket(getOwner(), new SM_HEADING_UPDATE(getOwner()));
				PacketSendUtility.broadcastPacket(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
			}
		}, 500);
	}

	private void spawnAdds(int curStage) {
		switch (curStage) {
			case 1:
				spawn(monsterIds[0], 256.44f, 278.59f, 241.55f, (byte) 90);
				spawn(monsterIds[1], 268.56f, 272.88f, 241.55f, (byte) 78);
				spawn(monsterIds[2], 243.61f, 272.99f, 241.55f, (byte) 105);
				spawn(monsterIds[2], 257.75f, 239.93f, 241.55f, (byte) 32);
				break;
			case 5:
				spawn(monsterIds[3], 260.68f, 262.03f, 241.81f, (byte) 75);
				spawn(monsterIds[3], 252.21f, 253.48f,  241.8f, (byte) 15);
				spawn(monsterIds[4], 246.57f, 273.86f, 241.55f, (byte) 100);
				spawn(monsterIds[5], 269.73f, 244.87f, 241.55f, (byte) 48);
				spawn(monsterIds[5], 240.87f, 250.35f, 241.55f, (byte) 10);
				break;
			case 7:
				spawn(monsterIds[0], 260.68f, 262.03f, 241.81f, (byte) 75);
				spawn(monsterIds[0], 252.21f, 253.48f,  241.8f, (byte) 15);
				spawn(monsterIds[1], 246.57f, 273.86f, 241.55f, (byte) 100);
				spawn(monsterIds[2], 269.73f, 244.87f, 241.55f, (byte) 48);
				spawn(monsterIds[2], 240.87f, 250.35f, 241.55f, (byte) 10);
				break;
			case 9:
				spawn(monsterIds[0], 260.68f, 262.03f, 241.81f, (byte) 75);
				spawn(monsterIds[1], 252.21f, 253.48f,  241.8f, (byte) 15);
				spawn(monsterIds[2], 246.57f, 273.86f, 241.55f, (byte) 100);
				spawn(monsterIds[3], 269.73f, 244.87f, 241.55f, (byte) 48);
				spawn(monsterIds[4], 240.87f, 250.35f, 241.55f, (byte) 10);
				spawn(monsterIds[5], 257.75f, 239.93f, 241.55f, (byte) 32);
				break;
		}
	}

	private int getNextSkillTimeFor(int stage) {
		switch (stage) {
			case 1:
			case 2:
			case 3:
				return Rnd.get(2, 6) * 1000;
			case 4:
			case 5:
			case 6:
			case 7:
				return Rnd.get(2, 4) * 1000;
			default:
				return Rnd.get(0, 4) * 1000;
		}
	}

	private Vector3f getRandomPosFromCenter(int distance) {
		Vector3f center = new Vector3f(256.62f, 257.79f, 241.8f);
		double angleRadians = Math.toRadians(Rnd.nextFloat(360f));
		center.setX(center.x + (float) (Math.cos(angleRadians) * distance));
		center.setY(center.y + (float) (Math.sin(angleRadians) * distance));
		return center;
	}

	private void queueSkill(int id, int lvl) {
		queueSkill(id, lvl, 0);
	}

	private void queueSkill(int id, int lvl, int nextSkillTime) {
		getOwner().queueSkill(id, lvl, nextSkillTime);
	}

	private boolean isCrazedModor() {
		return getNpcId() == 234691; // Crazed Modor (Infernal Danuar Reliquary)
	}

	private void addPlatformLocations() {
		platformLocations.clear();
		platformLocations.add(new Vector3f(255.49063f, 293.35785f, 253.79933f));
		platformLocations.add(new Vector3f(284.359f, 262.854f, 248.76f));
		platformLocations.add(new Vector3f(271.169f, 230.531f, 250.955f));
		platformLocations.add(new Vector3f(240.273f, 235.181f, 251.153f));
		platformLocations.add(new Vector3f(232.448f, 263.886f, 248.642f));
	}

	private boolean shouldUsePlatformSkills(int skillLevel) {
		for (NpcSkillEntry skill : getOwner().getQueuedSkills()) {
			// if another teleport skill(=21165) is queued with level != 10 -> next stage is ready so stop switching platforms
			if (skill.getSkillLevel() != 10 && skill.getSkillId() == 21165 && skill.getSkillLevel() != skillLevel) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void handleDied() {
		if (positionUpdateTask != null && !positionUpdateTask.isCancelled()) {
			positionUpdateTask.cancel(false);
		}
		super.handleDied();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		stage.set(0);
		hpPhases.reset();
		addPlatformLocations();
		World.getInstance().updatePosition(getOwner(), 256.62f, 257.79f, 241.79f, (byte) 90);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_HEADING_UPDATE(getOwner()));
		PacketSendUtility.broadcastPacket(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
	}
}
