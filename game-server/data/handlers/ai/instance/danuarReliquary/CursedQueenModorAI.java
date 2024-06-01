package ai.instance.danuarReliquary;

import java.util.ArrayList;
import java.util.List;
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
@AIName("cursed_queen_modor")
public class CursedQueenModorAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(100, 81, 77, 61, 50);
	private List<Vector3f> platformLocations = new ArrayList<>(5);
	private AtomicInteger stage = new AtomicInteger();
	private float multiplier = 1f;
	private Vector3f nextPosition = null;

	public CursedQueenModorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPlatformLocations();
	}

	@Override
	public AttackHandAnimation modifyAttackHandAnimation(AttackHandAnimation attackHandAnimation) {
		return Rnd.get(AttackHandAnimation.values());
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
				queueSkill(21181, 1); // Malevolence
				queueSkill(21171, 1); // Grendal's Explosive Wrath
				break;
			case 81:
				PacketSendUtility.broadcastMessage(getOwner(), 1500741);
				queueSkill(21171, 1); // Grendal's Explosive Wrath
				queueSkill(21229, 1);  // Dragon Lords Lightning Shock
				break;
			case 77:
				queueSkill(21165, 2, 4000);
				queueSkill(21179, 1, 4000); // ice storm when going up 1st time
				break;
			case 61:
				queueSkill(21165, 3);
				break;
			case 50:
				queueSkill(21175, 4);
				break;
		}
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		return damage * multiplier;  // fix skill dmg until new calculations are available
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 21171:
				multiplier = 1.2f;
				break;
			case 21165: // teleport
				if (skillLevel != 10) {
					stage.set(skillLevel);
					if (skillLevel == 2) {
						PacketSendUtility.broadcastMessage(getOwner(), 1500750);
					} else if (skillLevel == 3) {
						nextPosition = getRandomPosFromCenter(8);
						spawn(284385, nextPosition.x, nextPosition.y, nextPosition.getZ(), (byte) 0);
					}
				}
				break;
			case 21181: // Malevolence, buff
				stage.set(1);
				PacketSendUtility.broadcastMessage(getOwner(), 1500740);
				break;
			case 21175: // frozen domain of revenge
				if (skillLevel == 4) { // use this skill to despawn modor and spawn clones
					stage.set(4);
					PacketSendUtility.broadcastMessage(getOwner(), 1500742);
				}
			case 21174:
			case 21229:
			case 21173:
				multiplier = 0.5f;
				break;
			default:
				break;
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		int curStage = stage.get();
		switch (skillTemplate.getSkillId()) {
			case 21165: // teleport
				switch (curStage) {
					case 2:
						if (skillLevel != 10) {
							spawnAdds();
						} else {
							queueSkill(21179, 1, Rnd.get(3000, 5000)); // ice storm when going up 1st time
						}
						break;
					case 3:
						queueSkill(21229, 1); //  Dragon Lords Lightning Shock
						break;
					default:
						break;
				}
				updatePosition(curStage);
				break;
			case 21174: // frozen domain of rancour
				multiplier = 1f;
				if (skillLevel == 1 && (curStage == 1 || curStage == 5)) {
					float rnd = Rnd.chance();
					if (rnd < 15) {
						queueSkill(21175, 2, 5000); // lv 2 prevent skill loop
					} else if (rnd < 45) {
						queueSkill(21173, 1, 5000); // summon frost storm
					}
				}
				break;
			case 21175: // frozen domain of revenge
				multiplier = 1f;
				switch (curStage) {
					case 2: // going up 1st time, switch platforms
						if (shouldUsePlatformSkills(skillLevel)) {
							queueSkill(21165, 10, 5000);
						}
						break;
					case 4: // = hp <= 50%
						spawnClones();
						getOwner().getController().delete();
						break;
					case 1:
					case 3: // when she's not on a platform/last stage use aoe skills
						if (skillLevel == 1) {
							float rnd = Rnd.chance();
							if (rnd < 15) {
								queueSkill(21174, 2, 5000); // lv 2 to prevent skill loop
							} else if (rnd < 45) {
								queueSkill(21229, 1, 5000); //  Dragon Lords Lightning Shock
							}
						}
						break;
				}
				break;
			case 21179: // ice storm
				Vector3f pos = getRandomPosFromCenter(Rnd.nextInt(9));
				spawn(284528, pos.getX(), pos.getY(), pos.getZ(), (byte) 10);
				if (curStage == 2 && shouldUsePlatformSkills(skillLevel)) {
					queueSkill(21174, 1, 5000); // frozen domain of rancour
					queueSkill(21175, 1, 5000); // frozen domain of revenge
				}
				break;
			default:
				multiplier = 1f;
				break;
		}
	}

	private void spawnClones() {
		int spawnCase = Rnd.get(1, 5);
		// spawn real clone
		switch (spawnCase) {
			case 1:
				spawn(getRealCloneId(), 255.5489f, 293.42154f, 253.78925f, (byte) 90);
				break;
			case 2:
				spawn(getRealCloneId(), 232.5363f, 263.90112f, 248.65384f, (byte) 114);
				break;
			case 3:
				spawn(getRealCloneId(), 240.11194f, 235.08876f, 251.14906f, (byte) 17);
				break;
			case 4:
				spawn(getRealCloneId(), 271.23627f, 230.30913f, 250.92981f, (byte) 42);
				break;
			case 5:
				spawn(getRealCloneId(), 284.6919f, 262.7201f, 248.75252f, (byte) 63);
				break;
		}
		// spawn fake clones
		if (spawnCase != 1)
			spawn(getFakeCloneId(), 255.5489f, 293.42154f, 253.78925f, (byte) 90);
		if (spawnCase != 2)
			spawn(getFakeCloneId(), 232.5363f, 263.90112f, 248.65384f, (byte) 114);
		if (spawnCase != 3)
			spawn(getFakeCloneId(), 240.11194f, 235.08876f, 251.14906f, (byte) 17);
		if (spawnCase != 4)
			spawn(getFakeCloneId(), 271.23627f, 230.30913f, 250.92981f, (byte) 42);
		if (spawnCase != 5)
			spawn(getFakeCloneId(), 284.6919f, 262.7201f, 248.75252f, (byte) 63);
	}

	private void spawnAdds() {
		spawn(284380, 266.26517f, 273.97614f, 241.54623f, (byte) 83);
		spawn(284381, 256.57727f, 278.18225f, 241.54623f, (byte) 90);
		spawn(284382, 246.65663f, 275.51996f, 241.54623f, (byte) 96);
	}

	private void queueSkill(int id, int lvl) {
		queueSkill(id, lvl, 0);
	}

	private void queueSkill(int id, int lvl, int nextSkillTime) {
		getOwner().queueSkill(id, lvl, nextSkillTime);
	}

	private void updatePosition(final int curStage) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!getOwner().isDead() && getOwner().isSpawned()) {
				switch (curStage) {
					case 1: // coming down for 1st time
						World.getInstance().updatePosition(getOwner(), 256.62f, 257.79f, 241.79f, (byte) 90);
						break;
					case 2: // going up 1st time // switch platform
						Vector3f loc = platformLocations.remove(0);
						platformLocations.add(loc); // re-add to be able to teleport to loc again
						World.getInstance().updatePosition(getOwner(), loc.getX(), loc.getY(), loc.getZ(),
								PositionUtil.getHeadingTowards(loc.getX(), loc.getY(), 256.62f, 257.79f)); // heading towards center
						break;
					case 3:  // coming down 2nd time: spawn at random loc
						World.getInstance().updatePosition(getOwner(), nextPosition.getX(), nextPosition.getY(), nextPosition.getZ(),
								PositionUtil.getHeadingTowards(nextPosition.getX(), nextPosition.getY(), 256.62f, 257.79f));
						break;
					default:
						break;
				}
				PacketSendUtility.broadcastPacket(getOwner(), new SM_HEADING_UPDATE(getOwner()));
				PacketSendUtility.broadcastPacket(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
			}
		}, 500);
	}

	private void addPlatformLocations() {
		platformLocations.clear();
		platformLocations.add(new Vector3f(255.49063f, 293.35785f, 253.79933f));
		platformLocations.add(new Vector3f(284.359f, 262.854f, 248.76f));
		platformLocations.add(new Vector3f(271.169f, 230.531f, 250.955f));
		platformLocations.add(new Vector3f(240.273f, 235.181f, 251.153f));
		platformLocations.add(new Vector3f(232.448f, 263.886f, 248.642f));
	}

	@Override
	public int modifyInitialSkillDelay(int delay) {
		return 0;
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

	private boolean shouldUsePlatformSkills(int skillLevel) {
		for (NpcSkillEntry skill : getOwner().getQueuedSkills()) {
			// if another teleport skill(=21165) is queued with level != 10 -> next stage is ready so stop switching platforms
			if (skill.getSkillLevel() != 10 && skill.getSkillId() == 21165 && skill.getSkillLevel() != skillLevel) {
				return false;
			}
		}
		return true;
	}

	private int getRealCloneId() {
		return getNpcId() == 234690 ? 855244 : 284383;
	}

	private int getFakeCloneId() {
		return getNpcId() == 234690 ? 855245 : 284384;
	}

	private Vector3f getRandomPosFromCenter(int distance) {
		Vector3f center = new Vector3f(256.62f, 257.79f, 241.8f);
		double angleRadians = Math.toRadians(Rnd.nextFloat(360f));
		center.setX(center.x + (float) (Math.cos(angleRadians) * distance));
		center.setY(center.y + (float) (Math.sin(angleRadians) * distance));
		return center;
	}
}
