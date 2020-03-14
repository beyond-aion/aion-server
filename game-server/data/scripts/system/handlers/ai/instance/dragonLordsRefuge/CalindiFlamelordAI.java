package ai.instance.dragonLordsRefuge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PositionUtil;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller, Yeats, Estrayl
 */
@AIName("IDTiamat_2_calindi_flamelord")
public class CalindiFlamelordAI extends AggressiveNpcAI {

	private List<Integer> percents = new ArrayList<>();

	public CalindiFlamelordAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercents();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		addPercents();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		blazeEngraving();
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 75:
					case 50:
					case 25:
						startHallucinatoryVictoryEvent();
						break;
					case 12:
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(20942, 1, 100)));
						break;
				}
				break;
			}
		}
	}

	protected void startHallucinatoryVictoryEvent() {
		if (getPosition().getWorldMapInstance().getNpc(730695) == null && getPosition().getWorldMapInstance().getNpc(730696) == null)
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(20911, 1, 100)));
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 20911:
				spawn(730695, 482.21f, 458.06f, 427.42f, (byte) 98);
				spawn(730696, 482.21f, 571.16f, 427.42f, (byte) 22);
				rndSpawn(283132);
				break;
			case 20913:
				Player target = getRandomTarget();
				if (target != null)
					spawn(283130, target.getX(), target.getY(), target.getZ(), (byte) 0);
		}
	}

	protected void blazeEngraving() {
		if (Rnd.chance() < 2 && getPosition().getWorldMapInstance().getNpc(283130) == null)
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(20913, 60, 100)));
	}

	protected void rndSpawn(int npcId) {
		for (int i = 0; i < 10; i++) {
			SpawnTemplate template = rndSpawnInRange(npcId);
			SpawnEngine.spawnObject(template, getPosition().getInstanceId());
		}
	}

	private SpawnTemplate rndSpawnInRange(int npcId) {
		float direction = Rnd.get(0, 199) / 100f;
		int range = Rnd.get(5, 20);
		float x1 = (float) (Math.cos(Math.PI * direction) * range);
		float y1 = (float) (Math.sin(Math.PI * direction) * range);
		return SpawnEngine.newSingleTimeSpawn(getPosition().getMapId(), npcId, getPosition().getX() + x1, getPosition().getY() + y1,
			getPosition().getZ(), getPosition().getHeading());
	}

	protected Player getRandomTarget() {
		List<Player> players = getKnownList().getKnownPlayers().values().stream()
			.filter(player -> !player.isDead() && PositionUtil.isInRange(player, getOwner(), 50)).collect(Collectors.toList());
		return Rnd.get(players);
	}

	private void addPercents() {
		percents.clear();
		Collections.addAll(percents, 75, 50, 25, 12);
	}
}
