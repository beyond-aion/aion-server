package ai.instance.infinityShard;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 * @modified Luzien
 */
@AIName("ideresonator")
public class IdeResonatorAI extends NpcAI {

	private Future<?> task1, task2;

	public IdeResonatorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		final Npc hyperion = getPosition().getWorldMapInstance().getNpc(231073);
		AIActions.targetCreature(IdeResonatorAI.this, hyperion);
		task1 = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (getOwner() == null || getOwner().isDead() || hyperion == null || hyperion.isDead()) {
					return;
				}
				int firstBuff = 21257;
				switch (getOwner().getNpcId()) {
					case 231092:
						firstBuff = 21257;
						break;
					case 231093:
						firstBuff = 21381;
						break;
					case 231094:
						firstBuff = 21383;
						break;

				}
				AIActions.useSkill(IdeResonatorAI.this, firstBuff);
			}
		}, 10000);
		task2 = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (getOwner() == null || getOwner().isDead() || hyperion == null || hyperion.isDead()) {
					return;
				}
				int secondBuff = 0;
				if (!hyperion.getEffectController().hasAbnormalEffect(21258)) {
					PacketSendUtility.broadcastToMap(getOwner(), 1401791);
					secondBuff = 21258;
				} else if (!hyperion.getEffectController().hasAbnormalEffect(21382)) {
					PacketSendUtility.broadcastToMap(getOwner(), 1401792);
					secondBuff = 21382;
				} else if (!hyperion.getEffectController().hasAbnormalEffect(21384)) {
					PacketSendUtility.broadcastToMap(getOwner(), 1401793);
					secondBuff = 21384;
				} else if (!hyperion.getEffectController().hasAbnormalEffect(21416)) {
					PacketSendUtility.broadcastToMap(getOwner(), 1401794);
					secondBuff = 21416;
				}
				if (secondBuff != 0) {
					SkillEngine.getInstance().applyEffectDirectly(secondBuff, getOwner(), hyperion, 0);
					SkillEngine.getInstance().applyEffectDirectly(21371, getOwner(), getOwner(), 0);
				}
			}
		}, 30000);
	}

	@Override
	public void handleDespawned() {
		super.handleDespawned();
		if (task1 != null && !task1.isDone())
			task1.cancel(true);
		if (task2 != null && !task2.isDone())
			task2.cancel(true);
	}

	@Override
	public void handleDied() {
		super.handleDied();
		if (task1 != null && !task1.isDone())
			task1.cancel(true);
		if (task2 != null && !task2.isDone())
			task2.cancel(true);
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
