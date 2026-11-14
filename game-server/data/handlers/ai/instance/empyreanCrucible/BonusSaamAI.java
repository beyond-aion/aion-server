package ai.instance.empyreanCrucible;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.AggressiveNpcAI;

/**
 * King Saam of the stage 2 bonus: every hit has a 50 % chance to split off a little Saam, up to 30 times, and then he runs from the attacker
 * for 5 seconds. A cut Saam runs from its attacker for 3 seconds with a 50 % chance per hit.
 */
@AIName("empyrean_bonus_saam")
public class BonusSaamAI extends AggressiveNpcAI {

	private static final int CUT_SAAM = 217738, MAX_PIGLETS = 30;
	private final AtomicInteger piglets = new AtomicInteger();
	private final AtomicBoolean fleeing = new AtomicBoolean();

	public BonusSaamAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (creature.equals(getOwner()) || fleeing.get() || isDead() || !Rnd.nextBoolean())
			return;
		if (getNpcId() == CUT_SAAM) {
			flee(creature, 3000);
		} else if (piglets.getAndUpdate(n -> n < MAX_PIGLETS ? n + 1 : n) < MAX_PIGLETS) {
			PacketSendUtility.broadcastMessage(getOwner(), 342352);
			double angle = Math.toRadians(Rnd.get(0, 359));
			float distance = Rnd.get(1, 10);
			spawn(CUT_SAAM, getOwner().getX() + (float) Math.cos(angle) * distance, getOwner().getY() + (float) Math.sin(angle) * distance,
				getOwner().getZ(), getOwner().getHeading(), 0, "empyrean_bonus_saam");
			flee(creature, 5000);
		}
	}

	private void flee(Creature from, int millis) {
		if (fleeing.compareAndSet(false, true))
			CrucibleRun.start(this, from, millis, () -> fleeing.set(false));
	}

	@Override
	protected void handleDied() {
		fleeing.set(false);
		super.handleDied();
	}
}
