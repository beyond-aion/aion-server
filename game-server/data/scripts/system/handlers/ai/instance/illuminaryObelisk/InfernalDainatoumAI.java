package ai.instance.illuminaryObelisk;

import java.util.Collections;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("infernal_dainatoum")
public class InfernalDainatoumAI extends DainatoumAI {

	@Override
	protected int getBombId() {
		return 284860;
	}

	@Override
	protected synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				switch (percent) {
					case 70:
						removeBossEntry();
						break;
					case 50:
					case 10:
						spawnHealers();
						break;
					case 90:
					case 60:
					case 30:
					case 5:
						spawnBombs();
						break;
				}
				percents.remove(percent);
				break;
			}
		}
	}

	@Override
	protected void scheduleDespawn() {
		despawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (!isAlreadyDead()) {
				switch (progress) {
					case 0:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_HARD_BOSS_TIMER_01());
						break;
					case 1:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_HARD_BOSS_TIMER_02());
						break;
					case 4:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_HARD_BOSS_TIMER_03());
						break;
					case 5:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_HARD_BOSS_TIMER_04());
						onDespawn();
						break;
				}
				progress++;
			}
		}, 1000, 60000);
	}

	@Override
	protected void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 90, 70, 60, 50, 30, 10, 5 });
	}
}
