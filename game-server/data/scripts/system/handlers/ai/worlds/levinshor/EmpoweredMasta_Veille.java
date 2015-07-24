package ai.worlds.levinshor;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.manager.EmoteManager;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.agentsfight.EmpoweredAgentDamageListener;
import com.aionemu.gameserver.services.agentsfight.EmpoweredAgentDeathListener;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.AggressiveNpcAI2;

/**
 * @author Yeats
 *
 */
@AIName("empowered_agent")
public class EmpoweredMasta_Veille extends AggressiveNpcAI2 {
	
	private boolean canThink = true;
	private final EmpoweredAgentDeathListener listener = new EmpoweredAgentDeathListener(this);
	private final EmpoweredAgentDamageListener damageListener = new EmpoweredAgentDamageListener();
	
	@Override
	protected void handleSpawned() {
		canThink = false;
		super.handleSpawned();
		SkillEngine.getInstance().getSkill(getOwner(), 21779, 1, getOwner()).useNoAnimationSkill();
		WalkManager.startWalking(this);
	}
	
	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		String walkerId = getOwner().getSpawn().getWalkerId();
		if (walkerId == null) {
			return;
		}
		int step = getOwner().getMoveController().getCurrentPoint();
		int stopAtStep = 0;
		if (getOwner().getNpcId() == 235064) {
			stopAtStep = 0; //change to 48 when fixed 
		} else if (getOwner().getNpcId() == 235065){
			stopAtStep = 3; //change to 37 when fixed
		} else {
			return;
		}
		if (stopAtStep == step) {
			getSpawnTemplate().setWalkerId(null);
			WalkManager.stopWalking(this);
			getSpawnTemplate().setX(getOwner().getX());
			getSpawnTemplate().setY(getOwner().getY());
			getSpawnTemplate().setZ(getOwner().getZ());
			addEventListener(listener);
			getAggroList().addEventListener(damageListener);
			canThink = true;
			addHate();
		}
	}
	
	private void addHate() {
		getOwner().getKnownList().addVisualObject(getOwner());
		EmoteManager.emoteStopAttacking(getOwner());
		switch (getOwner().getNpcId()) {
			case 235064: //Veille
				Npc masta = getOwner().getPosition().getWorldMapInstance().getNpc(235065);
				if (masta != null && !masta.getLifeStats().isAlreadyDead()) {
					getOwner().getAggroList().addHate(masta, 100000000);
				} 
				break;
			case 235065: //Mastarius
				Npc veille = getOwner().getPosition().getWorldMapInstance().getNpc(235064);
				if (veille != null && !veille.getLifeStats().isAlreadyDead()) {
					getOwner().getAggroList().addHate(veille, 100000000);
				} 
				break;
				default:
					break;
				
		}
	}
	
	@Override
	public boolean canThink() {
		return canThink;
	}
	
	@Override
	protected void handleDied() {
		super.handleDied();
		removeEventListener(listener);
		getAggroList().removeEventListener(damageListener);
	}
	
	@Override
	protected void handleDespawned() {
		removeEventListener(listener);
		getAggroList().removeEventListener(damageListener);
		super.handleDespawned();
	}
}
