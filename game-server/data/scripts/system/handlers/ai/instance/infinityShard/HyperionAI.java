package ai.instance.infinityShard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.animations.AttackHandAnimation;
import com.aionemu.gameserver.model.animations.AttackTypeAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 * @reworked Yeats 26.04.2016
 * @modified Estrayl March 6th, 2018
 */
@AIName("hyperion")
public class HyperionAI extends AggressiveNpcAI {

	private List<Integer> possibleSummons = Arrays.asList(231096, 231097, 231098, 231099, 231100, 231101);
	private List<Integer> percents = new ArrayList<>();
	private WorldPosition northernSpawnPos = new WorldPosition(getPosition().getMapId(), 112.006f, 122.894f, 123.303f, (byte) 0);
	private WorldPosition southernSpawnPos = new WorldPosition(getPosition().getMapId(), 148.127f, 150.346f, 123.729f, (byte) 0);
	private Future<?> spawnTask;
	private byte stage = 0;
	private double dist;

	public HyperionAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
	}

	@Override
	public AttackTypeAnimation getAttackTypeAnimation(Creature target) {
		dist = PositionUtil.getDistance(getOwner(), target) - getObjectTemplate().getBoundRadius().getFront() - target.getObjectTemplate().getBoundRadius().getFront();
		if (dist > 4) {
			return AttackTypeAnimation.RANGED;
		}
		return AttackTypeAnimation.MELEE;
	}

	@Override
	protected void handleAttack(Creature creature) {
		checkPercentage(getLifeStats().getHpPercentage());
		super.handleAttack(creature);
	}


	@Override
	public int modifyOwnerDamage(int damage, Creature effected, Effect effect) {
		if (effect == null) {
			if (dist > 10) {
				return (int) (damage * 4.5);
			} else if (dist > 4) {
				return (int) (damage * 1.5);
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

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 100:
						queuePowerfulEnergyBlast();
						break;
					case 75:
						spawnSummons(++stage);
					case 80:
					case 67:
						spawnAncientTyrhund(2);
						break;
					case 55:
						spawnAncientTyrhund(3);
						break;
					case 45:
					case 30:
					case 17:
						spawnAncientTyrhund(4);
						break;
					case 65:
					case 50:
					case 25:
					case 20:
						stage++;
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21253, 56, 100, 0, 0)));
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21244, 56, 100, 0, 5000)));
						break;
					case 40:
						spawnSummons(++stage);
						break;
					case 10:
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21246, 56, 100)));
						break;
				}
				break;
			}
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate) {
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
		Npc npc = (Npc) spawn(npcId == 0 ? Rnd.get(possibleSummons) : 231103, p.getX(), p.getY(), p.getZ(), (byte) 0);
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
		float direction = Rnd.get(0, 199) / 100f;
		float distance = Rnd.get() * distanceMod;
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		return new Point3D(p.getX() + x1, p.getY() + y1, p.getZ());
	}

	private void queuePowerfulEnergyBlast() {
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21241, 56, 100)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21241, 56, 100)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21241, 56, 100, 0, 8000)));
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
		percents.clear();
	}

	@Override
	protected void handleDied() {
		cancelSpawnTask();
		super.handleDied();
		percents.clear();
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, 100, 80, 75, 67, 65, 55, 50, 45, 40, 30, 25, 20, 17, 10);
	}
}
