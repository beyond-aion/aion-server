package ai.instance.infinityShard;

import java.util.Collections;
import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI2;

/**
 * @author Cheatkiller
 * @rework Yeats 26.04.2016
 */
@AIName("hyperion")
public class HyperionAI2 extends AggressiveNpcAI2 {

	private List<Integer> percents = new FastTable<Integer>();
	private byte stage = 0;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
		SkillEngine.getInstance().applyEffectDirectly(21254, getOwner(), getOwner(), 0);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 100:
					case 80:
					case 60:
					case 40:
						usePowerfulEnergyBlast();
						break;
					case 75:
						stage = 0;
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21245, 1, 100, true)));
						break;
					case 65:
						combo();// TODO start in Task
						break;
					case 55:
					case 25:
						stage = 1;
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21245, 1, 100, true)));
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21253, 1, 100, true)));
						break;
					case 50:
					case 15:
						stage = 2;
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21245, 1, 100, true)));
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21253, 1, 100, true)));
						break;
					case 45:
						stage = 3;
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21245, 1, 100, true)));
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21253, 1, 100, true)));
						break;

				}
				break;
			}
		}
	}

	@Override
	public void fireOnEndCastEvents(NpcSkillEntry usedSkill) {
		switch (usedSkill.getSkillId()) {
			case 21245:
				switch (stage) {
					case 0:
						spawnSummons(231096);
						break;
					case 1:
						spawnSummons(231097);
						break;
					case 2:
						spawnSummons(231098);
						break;
					case 3:
						spawnSummons(231099);
						break;
					default:
						break;
				}
				spawnAncientTyrhund();
				break;
			default:
				break;
		}
	}

	private void spawnAncientTyrhund() {
		spawn(231103, 113.02703f, 140.92065f, 114.50903f, (byte) 0);
		spawn(231103, 117.32612f, 143.28151f, 114.50903f, (byte) 0);
		spawn(231103, 122.51532f, 132.23872f, 114.50903f, (byte) 0);
		spawn(231103, 128.51788f, 146.87315f, 114.50903f, (byte) 0);
		spawn(231103, 130.39738f, 128.78751f, 114.50903f, (byte) 0);
		spawn(231103, 134.41382f, 144.65814f, 114.50903f, (byte) 0);
		spawn(231103, 141.98439f, 134.14197f, 114.50903f, (byte) 0);
		spawn(231103, 143.54959f, 132.97408f, 114.50903f, (byte) 0);
	}

	private void spawnSummons(int npcId) {
		// TODO summons count increased?
		final Npc sum1 = (Npc) spawn(npcId, 112.005295f, 122.89348f, 123.30229f, (byte) 0);
		final Npc sum2 = (Npc) spawn(npcId, 148.12657f, 150.3456f, 123.72856f, (byte) 0);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				startWalk(sum1, "hyperionGuards1");
				startWalk(sum2, "hyperionGuards2");
			}
		}, 2000);
	}

	private void startWalk(Npc npc, String walkId) {
		npc.getSpawn().setWalkerId(walkId);
		WalkManager.startWalking((NpcAI2) npc.getAi2());
		npc.setState(1);
		PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.START_EMOTE2, 0, npc.getObjectId()));
	}

	private void combo() {
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21250, 1, 100, true)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21251, 1, 100, true)));
	}

	private void usePowerfulEnergyBlast() {
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21241, 1, 100, true)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21241, 1, 100, true)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21241, 1, 100, true)));
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		stage = 0;
		addPercent();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		percents.clear();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		percents.clear();
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 100, 80, 75, 65, 60, 55, 50, 45, 40, 25, 15 });
	}
}
