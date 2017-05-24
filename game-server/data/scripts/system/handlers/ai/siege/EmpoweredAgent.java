package ai.siege;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("empowered_agent")
public class EmpoweredAgent extends AggressiveNpcAI {
	
	private final List<Integer> guardIds = new ArrayList<>();
	private final List<Integer> percents = new ArrayList<>();
	private boolean canThink = true;
	
	@Override
	public boolean canThink() {
		return canThink;
	}
	
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		SkillEngine.getInstance().getSkill(getOwner(), 21779, 1, getOwner()).useNoAnimationSkill();
		canThink = false;
		EmoteManager.emoteStopAttacking(getOwner());
		switch (getOwner().getNpcId()) {
			case 235064:
				Collections.addAll(guardIds, new Integer[] {235334, 235335, 235336, 235337, 235338, 235339});
				break;
			case 235065:
				Collections.addAll(guardIds, new Integer[] {235340, 235341, 235342, 235343, 235344, 235345});
				break;
		}
		Collections.addAll(percents, new Integer[] {80, 70, 60, 50, 40, 30, 25, 20, 5});
	}
	
	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}
	
	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				switch (percent) {
					case 80:
					case 70:
					case 60:
					case 50:
					case 40:
					case 30:
					case 20:
						onGuardSpawnEvent();
						break;
					case 25:
					case 5:
						SkillEngine.getInstance().getSkill(getOwner(), 21778, 1, getOwner()).useWithoutPropSkill(); // 20% heal
						break;
				}
				percents.remove(percent);
				break;
			}
		}
	}
	
	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		String walkerId = getOwner().getSpawn().getWalkerId();
		if (walkerId == null)
			return;
		int step = getOwner().getMoveController().getCurrentPoint();
		int stop = 0;
		switch (getOwner().getNpcId()) {
		case 235064:
			stop = 48;
			break;
		case 235065:
			stop = 37;
			break;
		}
		if (stop == step) {
			ThreadPoolManager.getInstance().schedule(() -> onDestinationArrived(), 5000);
			
		}
	}
	
	private void onDestinationArrived() {
		getSpawnTemplate().setWalkerId(null);
		getSpawnTemplate().setX(getOwner().getX());
		getSpawnTemplate().setY(getOwner().getY());
		getSpawnTemplate().setZ(getOwner().getZ());
		WalkManager.stopWalking(this);
		getOwner().getEffectController().removeEffect(21779);
		getOwner().getLifeStats().setCurrentHpPercent(100);
		onAddHateEvent();
		onReactiveThinking();
	}
	
	private void onAddHateEvent() {
		Npc target = null;
		switch (getOwner().getNpcId()) {
			case 235064: //Veille
				target = getOwner().getPosition().getWorldMapInstance().getNpc(235065);
				break;
			case 235065: //Mastarius
				target = getOwner().getPosition().getWorldMapInstance().getNpc(235064);
				break;
		}
		if (target != null)
			getOwner().getAggroList().addHate(target, Integer.MAX_VALUE / 2);
	}
	
	private void onReactiveThinking() {
		canThink = true;
		Creature creature = getAggroList().getMostHated();
		if (creature == null || creature.getLifeStats().isAlreadyDead() || !getOwner().canSee(creature)) {
			setStateIfNot(AIState.FIGHT);
			think();
		} else {
			getMoveController().abortMove();
			getOwner().setTarget(creature);
			getOwner().getGameStats().renewLastAttackTime();
			getOwner().getGameStats().renewLastAttackedTime();
			getOwner().getGameStats().renewLastChangeTargetTime();
			getOwner().getGameStats().renewLastSkillTime();
			setStateIfNot(AIState.FIGHT);
			getOwner().setState(CreatureState.ACTIVE, true);
			handleMoveValidate();
			EmoteManager.emoteStartAttacking(getOwner(), creature);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getOwner().getObjectId()));
		}
	}
	
	private void onGuardSpawnEvent() {
		int worldId = getOwner().getWorldId();
		float guardAmount = getOwner().getAggroList().getList().size() / 2.5f;
		if (guardAmount < 6)
			guardAmount = 6;
		for (int i = 0; i < guardAmount; i++) {
			Point3D pos = getRndPos();
			//TODO: change to dynamic siegeID
			SiegeSpawnTemplate template = SpawnEngine.newSiegeSpawn(worldId, Rnd.get(guardIds), 8011, SiegeRace.BALAUR, SiegeModType.SIEGE,
				pos.getX(), pos.getY(), pos.getZ(), (byte) 0);
			SpawnEngine.spawnObject(template, 1);
		}
	}
	
	private Point3D getRndPos() {
		float direction = Rnd.get(0, 199) / 100f;
		float distance = Rnd.get() * 10;
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		WorldPosition p = getPosition();
		return new Point3D(p.getX() + x1, p.getY() + y1, p.getZ());
	}
	
	@Override
	protected void handleDied() {
		super.handleDied();
		
	}
	
	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_RESPAWN:
				return false;
			default:
				return super.ask(question);
		}
	}
}
