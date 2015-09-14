package ai.instance.sauroBase;

import java.util.ArrayList;
import java.util.List;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.actions.PlayerActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;


@AIName("rohuka")
public class CaptainRohukaAI2 extends AggressiveNpcAI2 {

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
		if (hpPercentage <= 50 && stage < 1) {
			stage1();
			stage = 1;
		}
		if (hpPercentage <= 45 && stage < 2) {
			stage2();
			stage = 2;
		}
		if (hpPercentage <= 25 && stage < 3) {
			stage3();
			stage = 3;
		}
	}

	private void stage1() {
		if (isAlreadyDead() || !isStart)
			return;
		else {
			SkillEngine.getInstance().getSkill(getOwner(), 21135, 56, getOwner()).useNoAnimationSkill();
		}
	}
	
	private void stage2() {
		int delay = 35000;
		if (isAlreadyDead() || !isStart)
			return;
		else {
		    skill();
			scheduleDelayStage2(delay);
		}
	}	
	
	private void skill() {
			SkillEngine.getInstance().getSkill(getOwner(), 18158, 56, getOwner()).useNoAnimationSkill();
		    ThreadPoolManager.getInstance().schedule(new Runnable() {
			    public void run() {
                    SkillEngine.getInstance().getSkill(getOwner(), 18160, 56, getOwner()).useNoAnimationSkill();
				}
			}, 4000);
	}
	
	private void scheduleDelayStage2(int delay) {
		if (!isStart && !isAlreadyDead())
			return;
		else {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					stage2();
				}
			}, delay);
		}
	}
	

	
	private void stage3() {
		int delay = 15000;
		if (isAlreadyDead() || !isStart)
			return;
		else
			scheduleDelayStage3(delay);
	}
	
	private void scheduleDelayStage3(int delay) {
		if (!isStart && !isAlreadyDead())
			return;
		else {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					getRandomTarget();
					stage3();
				}

			}, delay);
		}
	}
	
    private void getRandomTarget()  {
        List<Player> players = new ArrayList<Player>();
        for (Player player : getKnownList().getKnownPlayers().values()) {
            if (!PlayerActions.isAlreadyDead(player) && MathUtil.isIn3dRange(player, getOwner(), 16))
                players.add(player);
        }
        if (players.isEmpty())
            return;

        getAggroList().clear();
        getAggroList().startHate(players.get(Rnd.get(0, players.size() - 1)));
    }

	@Override
	protected void handleBackHome() {
        super.handleBackHome();
		isStart = false;
		stage = 0;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		isStart = false;
		stage = 0;
	}


}