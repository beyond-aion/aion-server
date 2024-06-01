package ai.instance.darkPoeta;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu, Estrayl
 */
@AIName("calindi_flamelord")
public class CalindiFlamelordAI extends AggressiveNpcAI {

	private Future<?> wipeTask;

	public CalindiFlamelordAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleWipe();
	}

	private void scheduleWipe() {
		PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_A_RANK_BATTLE_TIME());
		wipeTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead())
				getOwner().queueSkill(19679, 50, 3000);
		}, 600000);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 18233:
				if (getLifeStats().getHpPercentage() >= 30 && getLifeStats().getHpPercentage() <= 60) {
					spawn(281267, 1191.2714f, 1220.5795f, 144.2901f, (byte) 36);
					spawn(281267, 1188.3695f, 1257.1322f, 139.66028f, (byte) 80);
					spawn(281267, 1177.1423f, 1253.9136f, 140.58705f, (byte) 97);
					spawn(281267, 1163.5889f, 1231.9149f, 145.40042f, (byte) 118);
				} else {
					rndSpawnInRange(281268, 1, 2);
					rndSpawnInRange(281268, 1, 1);
				}
				break;
			case 19679: // You are unworthy.
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_A_RANK_BATTLE_END());
				AIActions.deleteOwner(this);
				break;
		}
	}

	@Override
	protected void handleDied() {
		if (wipeTask != null && !wipeTask.isCancelled())
			wipeTask.cancel(true);
		super.handleDied();
	}
}
