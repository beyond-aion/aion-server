package ai.instance.eternalBastion;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("eternal_bastion_commander")
public class EternalBastionCommanderAI extends EternalBastionAggressiveNpcAI {

	private Future<?> hpRestoreTask;
	private boolean wasAttacked;

	public EternalBastionCommanderAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleFinishAttack() {
		if (!canThink())
			return;
		EmoteManager.emoteStopAttacking(getOwner());
		getOwner().getController().loseAggro(false);
		hpRestoreTask = ThreadPoolManager.getInstance().schedule(() -> {
			getOwner().getLifeStats().triggerRestoreTask();
			wasAttacked = false;
		}, 120, TimeUnit.SECONDS);
	}

	private void cancelTask() {
		if (hpRestoreTask != null && !hpRestoreTask.isDone())
			hpRestoreTask.cancel(false);
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		if (creature instanceof Npc npc) {
			if (npc.getNpcId() == 231130) { // Pashid
				getAggroList().addHate(npc, 10000000); // Retail
				PacketSendUtility.broadcastMessage(getOwner(), 1500768 + getRace().getRaceId() * 4);
			}
		}
		cancelTask();
		super.handleCreatureAggro(creature);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		cancelTask();
		if (!wasAttacked || Rnd.chance() < 1)
			PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDLDF5b_TD_Notice_05());
		wasAttacked = true;
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		int msgId = getNpcId() == 209516 ? 1500763 : 1500767;
		PacketSendUtility.broadcastMessage(getOwner(), msgId);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}
}
