package ai.instance.empyreanCrucible;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.actions.PlayerActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.world.WorldPosition;

/**
 *
 * @author Luzien
 */
@AIName("priest_preceptor")
public class PriestPreceptorAI2 extends AggressiveNpcAI2 {
	
	private AtomicBoolean is75EventStarted = new AtomicBoolean(false);
	private AtomicBoolean is25EventStarted = new AtomicBoolean(false);
	
	@Override
	public void handleSpawned() {
		super.handleSpawned();
		
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			
			@Override
			public void run() {
				SkillEngine.getInstance().getSkill(getOwner(), 19612, 15, getOwner()).useNoAnimationSkill();
			}
			
		}, 1000);
		
	}
	
	@Override
	public void handleBackHome() {
		is75EventStarted.set(false);
		is25EventStarted.set(false);
		super.handleBackHome();
	}
	
	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}
	
	private void checkPercentage(int percentage) {
		if (percentage <= 75) {
			if (is75EventStarted.compareAndSet(false, true)) {
				SkillEngine.getInstance().getSkill(getOwner(), 19611, 10, getTargetPlayer()).useNoAnimationSkill();
			}
		}
		if (percentage <= 25) {
			if (is25EventStarted.compareAndSet(false, true)) {
				startEvent();
			}
		}
	}
	
	private void startEvent() {
		SkillEngine.getInstance().getSkill(getOwner(), 19610, 10, getOwner()).useNoAnimationSkill();
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			
					@Override
						public void run() {
							SkillEngine.getInstance().getSkill(getOwner(), 19614, 10, getOwner()).useNoAnimationSkill();
							
							ThreadPoolManager.getInstance().schedule(new Runnable() {
			
								@Override
									public void run() {
										WorldPosition p = getPosition();
										applySoulSickness((Npc) spawn(282366, p.getX(), p.getY(), p.getZ(), p.getHeading()));
										applySoulSickness((Npc) spawn(282367, p.getX(), p.getY(), p.getZ(), p.getHeading()));
										applySoulSickness((Npc) spawn(282368, p.getX(), p.getY(), p.getZ(), p.getHeading()));
								}
							}, 5000);
						}
			
		}, 2000);
	}
	
	private Player getTargetPlayer() {
		List<Player> players = new ArrayList<Player>();
		for (Player player : getKnownList().getKnownPlayers().values()) {
			if (!PlayerActions.isAlreadyDead(player) && MathUtil.isIn3dRange(player, getOwner(), 25)) {
				players.add(player);
			}
		}
		return players.get(Rnd.get(players.size()));
	}
	
	private void applySoulSickness(final Npc npc) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			
			@Override
			public void run() {
				npc.getLifeStats().setCurrentHpPercent(50); //TODO: remove this, fix max hp debuffs not reducing current hp properly
				SkillEngine.getInstance().getSkill(npc, 19594, 4, npc).useNoAnimationSkill();
			}
			
		}, 1000);
	}
	
}