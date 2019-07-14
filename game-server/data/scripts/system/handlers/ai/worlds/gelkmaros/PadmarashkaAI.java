package ai.worlds.gelkmaros;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("padmarashka_world_boss")
public class PadmarashkaAI extends AggressiveNpcAI {

	private List<Integer> hpEvents = new ArrayList<>();
	private AtomicInteger deadProtectors = new AtomicInteger();

	public PadmarashkaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> SkillEngine.getInstance().applyEffectDirectly(19186, getOwner(), getOwner()), 3000);
		initHpEvents();
		spawnShieldNpcs();
	}

	private void spawnShieldNpcs() {
		spawnAndObserveNpc(281938, new WorldPosition(220070000, 2906.05f, 865.15f, 35.289f, (byte) 107));
		spawnAndObserveNpc(281939, new WorldPosition(220070000, 2920.70f, 878.94f, 35.289f, (byte) 94));
		spawnAndObserveNpc(281940, new WorldPosition(220070000, 2952.03f, 878.61f, 35.266f, (byte) 81));
		spawnAndObserveNpc(281941, new WorldPosition(220070000, 2963.97f, 859.07f, 35.289f, (byte) 69));
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer hpEvent : hpEvents) {
			if (hpPercentage <= hpEvent) {
				hpEvents.remove(hpEvent);
				switch (hpEvent) {
					case 33:
						spawnRockSlides();
						break;
					case 3:
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(18730, 1, 100, 0, 3000))); // Berserk State
						break;
				}
				break;
			}
		}
	}

	private void spawnRockSlides() {
		for (int i = 0; i < 360; i+=9) {
			float distance = Rnd.get(400, 700) * 0.1f;
			double radian = Math.toRadians(i);
			float x = (float) (Math.cos(radian) * distance);
			float y = (float) (Math.sin(radian) * distance);
			spawn(281936, 2940.20f + x, 851.29f + y, 35.89f, (byte) 0);
		}
	}

	@Override
	public void onEffectEnd(Effect effect) {
		if (effect.getSkillId() == 19186)
			PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_DF4_DRAMATA_AWAKENING());
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		despawnNpcs(Arrays.asList(281936));
		initHpEvents();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		despawnNpcs(Arrays.asList(281938, 281939, 281940, 281941, 281936));
		super.handleDespawned();
	}

	private void despawnNpcs(List<Integer> npcIds) {
		getKnownList().getKnownObjects().values().forEach(o -> {
			if (o instanceof Npc && npcIds.contains(((Npc) o).getNpcId()))
				o.getController().delete();
		});
	}

	private void handleObservedNpcDied(Npc npc) {
		switch (npc.getNpcId()) {
			case 281938:
			case 281939:
			case 281940:
			case 281941:
				if (deadProtectors.incrementAndGet() >= 4)
					getOwner().getEffectController().removeEffect(19186); // Protective Slumber
				break;
		}
	}

	private void spawnAndObserveNpc(int npcId, WorldPosition pos) {
		Npc npc = (Npc) spawn(npcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
		npc.getObserveController().addObserver(new ActionObserver(ObserverType.DEATH) {

			@Override
			public void died(Creature creature) {
				handleObservedNpcDied(npc);
			}
		});
	}

	private void initHpEvents() {
		hpEvents.clear();
		Collections.addAll(hpEvents, 33, 5);
	}
}
