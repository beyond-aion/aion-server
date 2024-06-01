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
@AIName("tahabata_pyrelord")
public class TahabataPyrelordAI extends AggressiveNpcAI {

	private Future<?> wipeTask;

	public TahabataPyrelordAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleWipe();
	}

	private void scheduleWipe() {
		PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_S_RANK_BATTLE_TIME());
		wipeTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead())
				getOwner().queueSkill(19679, 50, 3000);
		}, 300000);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 19679: // You are unworthy.
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_S_RANK_BATTLE_END());
				AIActions.deleteOwner(this);
				break;
			case 18236:
				spawn(281258, 1191.2714f, 1220.5795f, 144.2901f, (byte) 36);
				spawn(281258, 1188.3695f, 1257.1322f, 139.66028f, (byte) 80);
				spawn(281258, 1177.1423f, 1253.9136f, 140.58705f, (byte) 97);
				spawn(281258, 1163.5889f, 1231.9149f, 145.40042f, (byte) 118);
				break;
			case 18241:
				spawn(281259, 1182.0021f, 1244.0125f, 142.67587f, (byte) 88);
				spawn(281259, 1192.3885f, 1236.5231f, 142.50638f, (byte) 68);
				spawn(281259, 1185.647f, 1227.2747f, 144.2261f, (byte) 32);
				spawn(281259, 1172.3302f, 1232.5709f, 144.70761f, (byte) 12);
				break;
		}
	}

	private void cancelTask() {
		if (wipeTask != null && !wipeTask.isCancelled())
			wipeTask.cancel(true);
	}

	@Override
	protected void handleDied() {
		cancelTask();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}
}
