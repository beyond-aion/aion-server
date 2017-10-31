package ai.instance.darkPoeta;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 * @reworked Estrayl 12.06.2017
 */
@AIName("calindi_flamelord")
public class CalindiFlamelordAI extends AggressiveNpcAI {

	private Future<?> wipeTask;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleWipe();
	}

	private void rndSpawnInRange() {
		float direction = Rnd.get(0, 199) / 100f;
		int distance = Rnd.get(0, 2);
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		WorldPosition p = getPosition();
		spawn(281268, p.getX() + x1, p.getY() + y1, p.getZ(), p.getHeading());
	}

	private void scheduleWipe() {
		PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_A_RANK_BATTLE_TIME());
		wipeTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead())
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(19679, 50, 100, 0, 3000)));
		}, 600000);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate) {
		switch (skillTemplate.getSkillId()) {
			case 18233:
				if (getLifeStats().getHpPercentage() >= 30 && getLifeStats().getHpPercentage() <= 60) {
					spawn(281267, 1191.2714f, 1220.5795f, 144.2901f, (byte) 36);
					spawn(281267, 1188.3695f, 1257.1322f, 139.66028f, (byte) 80);
					spawn(281267, 1177.1423f, 1253.9136f, 140.58705f, (byte) 97);
					spawn(281267, 1163.5889f, 1231.9149f, 145.40042f, (byte) 118);
				} else {
					rndSpawnInRange();
					rndSpawnInRange();
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
