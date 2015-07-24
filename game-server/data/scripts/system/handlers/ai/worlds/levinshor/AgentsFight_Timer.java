package ai.worlds.levinshor;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI2;

/**
 * @author Yeats
 *
 */
@AIName("agentsfight_timer")
public class AgentsFight_Timer extends GeneralNpcAI2 {
	
	private Future<?> TEN_MINUTES_LEFT;
	private Future<?> FIVE_MINUTES_LEFT;
	
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		sendTimer1();
	}
	
	private void sendTimer1() {
		TEN_MINUTES_LEFT = ThreadPoolManager.getInstance().schedule(new Runnable() {
			
			@Override
			public void run() {
				sendMsg(1402544);
				sendTimer2();
			}
		}, 1000 * 60 * 20);
	}
	
	private void sendTimer2() {
		FIVE_MINUTES_LEFT = ThreadPoolManager.getInstance().schedule(new Runnable() {
			
			@Override
			public void run() {
				sendMsg(1402545);
			}
		}, 1000 * 60 * 5);
	}
	
	private void sendMsg(int msgId) {
		for (Player player : getOwner().getKnownList().getKnownPlayers().values()) {
			if (player != null && player.isOnline()) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(msgId));
			}
		}
	}
	
	@Override
	protected void handleDespawned() {
		cancelTasks();
		sendMsg(1402546);
		super.handleDespawned();
	}
	
	private void cancelTasks() {
		if (TEN_MINUTES_LEFT != null && !TEN_MINUTES_LEFT.isCancelled()) {
			TEN_MINUTES_LEFT.cancel(true);
		}
		if (FIVE_MINUTES_LEFT != null && !FIVE_MINUTES_LEFT.isCancelled()) {
			FIVE_MINUTES_LEFT.cancel(true);
		}
	}
}
