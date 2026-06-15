package ai.instance.infinityShard;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.animations.AttackHandAnimation;
import com.aionemu.gameserver.model.animations.AttackTypeAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller, Yeats, Estrayl
 */
@AIName("hyperion")
public class HyperionAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(100, 80, 75, 67, 65, 55, 50, 45, 40, 30, 25, 20, 17, 10);
	private List<Integer> possibleSummons = Arrays.asList(231096, 231097, 231098, 231099, 231100, 231101);
	private WorldPosition northernSpawnPos = new WorldPosition(getPosition().getMapId(), 112.006f, 122.894f, 123.303f, (byte) 0);
	private WorldPosition southernSpawnPos = new WorldPosition(getPosition().getMapId(), 148.127f, 150.346f, 123.729f, (byte) 0);
	private Future<?> spawnTask;
	private byte stage = 0;
	private double dist;

	public HyperionAI(Npc owner) {
		super(owner);
	}

	@Override
	public AttackTypeAnimation getAttackTypeAnimation(Creature target) {
		dist = PositionUtil.getDistance(getOwner(), target) - getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide() - target.getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide();
		if (dist > 4) {
			return AttackTypeAnimation.RANGED;
		}
		return AttackTypeAnimation.MELEE;
	}

	@Override
	protected void handleAttack(Creature creature) {
		hpPhases.tryEnterNextPhase(this);
		super.handleAttack(creature);
	}


	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		if (effect == null) {
			if (dist > 10) {
				return damage * 4.5f;
			} else if (dist > 4) {
				return damage * 1.5f;
			}
		}
		return damage;
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		if (dist > 4) {
			return ItemAttackType.MAGICAL_EARTH;
		}
		return ItemAttackType.PHYSICAL;
	}

	@Override
	public AttackHandAnimation modifyAttackHandAnimation(AttackHandAnimation attackHandAnimation) {
		return Rnd.get(AttackHandAnimation.values());
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 100 -> queuePowerfulEnergyBlast();
			case 75 -> {
				spawnSummons(++stage);
				spawnAncientTyrhund(2);
			}
			case 80, 67 -> spawnAncientTyrhund(2);
			case 55 -> spawnAncientTyrhund(3);
			case 45, 30, 17 -> spawnAncientTyrhund(4);
			case 65, 50, 25, 20 -> {
				stage++;
				getOwner().queueSkill(21253, 56, 0);
				getOwner().queueSkill(21244, 56, 5000);
			}
			case 40 -> spawnSummons(++stage);
			case 10 -> getOwner().queueSkill(21246, 56);
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 21253:
				switch (stage) {
					case 1:
					case 2:
						spawnSummons(1);
						break;
					case 3:
					case 4:
						spawnSummons(2);
						break;
					case 5:
						spawnSummons(3);
						break;
					case 6:
						spawnSummons(4);
						break;
				}
				break;
			case 21246:
				scheduleLastPhase();
				break;
		}
	}

	private void scheduleLastPhase() {
		spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (!isDead()) {
				spawnSummons(5);
				spawnAncientTyrhund(4);
			}
		}, 35000, 60000);
	}

	private void spawnAncientTyrhund(int count) {
		for (int i = 0; i < count; i++) {
			spawnWithWalker(231103, getRndPos(getPosition(), 12), null);
		}
	}

	/**
	 * @param spawnCase
	 *          - 1 for one add per side
	 *          - 2 for two adds per side
	 *          - 3 for one add north + two adds south
	 *          - 4 for one add south + two adds south
	 *          - 5 for four adds per side (last phase)
	 */
	private void spawnSummons(int spawnCase) {
		switch (spawnCase) {
			case 1:
				spawnWithWalker(0, getRndPos(northernSpawnPos, 3), "hyperionGuards1");
				spawnWithWalker(0, getRndPos(southernSpawnPos, 3), "hyperionGuards2");
				break;
			case 2:
				spawnWithWalker(0, getRndPos(northernSpawnPos, 3), "hyperionGuards1");
				spawnWithWalker(0, getRndPos(northernSpawnPos, 3), "hyperionGuards1");
				spawnWithWalker(0, getRndPos(southernSpawnPos, 3), "hyperionGuards2");
				spawnWithWalker(0, getRndPos(southernSpawnPos, 3), "hyperionGuards2");
				break;
			case 3:
				spawnWithWalker(0, getRndPos(northernSpawnPos, 3), "hyperionGuards1");
				spawnWithWalker(0, getRndPos(southernSpawnPos, 3), "hyperionGuards2");
				spawnWithWalker(0, getRndPos(southernSpawnPos, 3), "hyperionGuards2");
				break;
			case 4:
				spawnWithWalker(0, getRndPos(northernSpawnPos, 3), "hyperionGuards1");
				spawnWithWalker(0, getRndPos(northernSpawnPos, 3), "hyperionGuards1");
				spawnWithWalker(0, getRndPos(southernSpawnPos, 3), "hyperionGuards2");
				break;
			case 5:
				spawnWithWalker(0, getRndPos(northernSpawnPos, 3), "hyperionGuards1");
				spawnWithWalker(0, getRndPos(northernSpawnPos, 3), "hyperionGuards1");
				spawnWithWalker(0, getRndPos(northernSpawnPos, 3), "hyperionGuards1");
				spawnWithWalker(0, getRndPos(northernSpawnPos, 3), "hyperionGuards1");
				spawnWithWalker(0, getRndPos(southernSpawnPos, 3), "hyperionGuards2");
				spawnWithWalker(0, getRndPos(southernSpawnPos, 3), "hyperionGuards2");
				spawnWithWalker(0, getRndPos(southernSpawnPos, 3), "hyperionGuards2");
				spawnWithWalker(0, getRndPos(southernSpawnPos, 3), "hyperionGuards2");
				break;
		}
	}

	private void spawnWithWalker(int npcId, Point3D p, String walkerId) {
		Npc npc = (Npc) spawn(npcId == 0 ? Rnd.get(possibleSummons) : npcId, p.getX(), p.getY(), p.getZ(), (byte) 0);
		if (walkerId != null) {
			ThreadPoolManager.getInstance().schedule(() -> {
				if (npc.isDead())
					return;
				npc.getSpawn().setWalkerId(walkerId);
				WalkManager.startWalking((NpcAI) npc.getAi());
				npc.setState(CreatureState.ACTIVE, true);
				PacketSendUtility.broadcastToMap(getOwner(), new SM_EMOTION(getOwner(), EmotionType.RUN));
			}, 2500);
		}
	}

	private Point3D getRndPos(WorldPosition p, float distanceMod) {
		double angleRadians = Math.toRadians(Rnd.nextFloat(360f));
		float distance = Rnd.nextFloat(distanceMod);
		float x1 = (float) (Math.cos(angleRadians) * distance);
		float y1 = (float) (Math.sin(angleRadians) * distance);
		return new Point3D(p.getX() + x1, p.getY() + y1, p.getZ());
	}

	private void queuePowerfulEnergyBlast() {
		getOwner().queueSkill(21241, 56, 0);
		getOwner().queueSkill(21241, 56, 0);
		getOwner().queueSkill(21241, 56, 8000);
	}

	private void cancelSpawnTask() {
		if (spawnTask != null && !spawnTask.isCancelled())
			spawnTask.cancel(true);
	}

	@Override
	public int modifyInitialSkillDelay(int delay) {
		return 0;
	}

	@Override
	protected void handleBackHome() {
		cancelSpawnTask();
		super.handleBackHome();
	}

	@Override
	protected void handleDespawned() {
		cancelSpawnTask();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cancelSpawnTask();
		super.handleDied();
	}
}
