package ai.instance.shugoImperialTomb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.world.World;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("captain_lediar")
public class CaptainLediarAI extends AggressiveNpcAI {

	private boolean canThink = true;
	private final List<Integer> spawnedNpc = new ArrayList<>();
	private AtomicBoolean isSpawnedHelpers = new AtomicBoolean(false);
	private final static int[] npc_ids = { 831251, 831250, 831305 };

	public CaptainLediarAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage == 75) {
			if (isSpawnedHelpers.compareAndSet(false, true)) {
				spawnHelpers();
			}
		}
	}

	private void spawnHelpers() {
		if (!isDead()) {
			int count = 4;
			int npcId = 219509;
			for (int i = 0; i < count; i++) {
				rndSpawnInRange(npcId, 2);
			}
		}
	}

	private void rndSpawnInRange(int npcId, float distance) {
		float direction = Rnd.get(0, 199) / 100f;
		float x = (float) (Math.cos(Math.PI * direction) * distance);
		float y = (float) (Math.sin(Math.PI * direction) * distance);
		spawn(npcId, getPosition().getX() + x, getPosition().getY() + y, getPosition().getZ(), (byte) 0);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		removeHelpersSpawn();
	}

	private void removeHelpersSpawn() {
		for (Integer object : spawnedNpc) {
			VisibleObject npc = World.getInstance().findVisibleObject(object);
			if (npc != null && npc.isSpawned()) {
				npc.getController().delete();
			}
		}
	}

	@Override
	public int modifyOwnerDamage(int damage, Creature effected, Effect effect) {
		return 1;
	}

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	protected void handleSpawned() {
		canThink = false;
		super.handleSpawned();
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getMoveController().getCurrentStep().isLastStep()) {
			getSpawnTemplate().setWalkerId(null);
			WalkManager.stopWalking(this);
			canThink = true;
			addHate();
		}
	}

	private void addHate() {
		EmoteManager.emoteStopAttacking(getOwner());
		for (int npc_id : npc_ids) {
			Npc tower = getOwner().getPosition().getWorldMapInstance().getNpc(npc_id);
			if (tower != null && !tower.isDead()) {
				switch (npc_id) {
					case 831251:
					case 831250:
					case 831305:
						getOwner().getAggroList().addHate(tower, 100);
						break;
				}
			}
		}
	}
}
