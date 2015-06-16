package ai.instance.sauroBase;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import java.util.List;


@AIName("sheba")
public class ShebaAI2 extends AggressiveNpcAI2 {

	private int stage = 0;
	private boolean isStart = false;

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		wakeUp();
	}
	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
		wakeUp();
	}

	private void wakeUp() {
		isStart = true;
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 75 && stage < 1) {
			stage1();
			stage = 1;
		}
		if (hpPercentage <= 70 && stage < 2) {
			stage2();
			stage = 2;
		}
		if (hpPercentage <= 50 && stage < 3) {
			stage3();
			stage = 3;
		}
		if (hpPercentage <= 25 && stage < 4) {
			stage4();
			stage = 4;
		}
	}
	
	private void stage1() {
		int delay = 25000;
		if (isAlreadyDead() || !isStart)
			return;
		else {
		   	sendMsg(1500775);
			SkillEngine.getInstance().getSkill(getOwner(), 21188, 1, getOwner()).useNoAnimationSkill();
			scheduleDelayStage1(delay);
		}
	}
	private void stage2() {
		if (isAlreadyDead() || !isStart)
			return;
		else {
		   	sendMsg(1500774);
			SkillEngine.getInstance().getSkill(getOwner(), 21189, 1, getOwner()).useNoAnimationSkill();
			spawn(284435, 900.12497f, 879.17401f, 411.625f, (byte) 0);
			spawn(284435, 887.1312f, 889.20688f, 411.875f, (byte) 0);
			spawn(284435, 900.1312f, 901.20688f, 411.875f, (byte) 0);
		}
	}
	private void stage3() {
		int delay = 40000;
		if (isAlreadyDead() || !isStart)
			return;
		else {
		   	sendMsg(1500777);
			SkillEngine.getInstance().getSkill(getOwner(), 21183, 65, getTarget()).useNoAnimationSkill();
			scheduleDelayStage3(delay);
		}
	}
	private void stage4() {
		int delay = 45000;
		if (isAlreadyDead() || !isStart)
			return;
		else {
		   	sendMsg(1500776);
			SkillEngine.getInstance().getSkill(getOwner(), 21184, 1, getOwner()).useNoAnimationSkill();
			switch (Rnd.get(1, 2)) {
				case 1:
						despawnNpcs(284436);
					spawn(284436, 900.12497f, 889.17401f, 412.1f, (byte) 0);
					break;
				case 2:
						despawnNpcs(284436);
					spawn(284436, 913.12497f, 876.17401f, 412.1f, (byte) 45);
					spawn(284436, 900.12497f, 870.17401f, 412.1f, (byte) 30);
					spawn(284436, 886.12497f, 876.17401f, 412.1f, (byte) 16);
					spawn(284436, 881.12497f, 889.17401f, 412.1f, (byte) 0);
					spawn(284436, 899.12497f, 909.17401f, 412.1f, (byte) 90);
					spawn(284436, 913.12497f, 902.17401f, 412.1f, (byte) 78);
					spawn(284436, 918.12497f, 890.17401f, 412.1f, (byte) 61);
					break;
			}
			scheduleDelayStage4(delay);
		}
	}

	private void scheduleDelayStage4(int delay) {
		if (!isStart && !isAlreadyDead())
			return;
		else {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					stage4();
				}
			}, delay);
		}
	}	
	private void scheduleDelayStage3(int delay) {
		if (!isStart && !isAlreadyDead())
			return;
		else {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					stage3();
				}
			}, delay);
		}
	}	
	private void scheduleDelayStage1(int delay) {
		if (!isStart && !isAlreadyDead())
			return;
		else {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					stage1();
				}
			}, delay);
		}
	}

	private void despawnNpcs(int npcId) {
		List<Npc> npcs = getPosition().getWorldMapInstance().getNpcs(npcId);
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().onDelete();
			}
		}
	}
	
	private void sendMsg(int msg) {
		NpcShoutsService.getInstance().sendMsg(getOwner(), msg, getObjectId(), 0, 0);
	}
	
	@Override
	protected void handleBackHome() {
	    despawnNpcs(284435);
	    despawnNpcs(284436);
		super.handleBackHome();
		isStart = false;
		stage = 0;
	}

	@Override
	protected void handleDied() {
	    despawnNpcs(284435);
	    despawnNpcs(284436);
		super.handleDied();
		isStart = false;
		stage = 0;
	}

}