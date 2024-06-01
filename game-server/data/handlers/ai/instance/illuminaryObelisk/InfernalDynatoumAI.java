package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("infernal_dynatoum")
public class InfernalDynatoumAI extends DynatoumAI {

	public InfernalDynatoumAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void scheduleDespawn(int delayInSec) {
		despawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
				switch (delayInSec) {
					case 1:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_HARD_BOSS_TIMER_01());
						scheduleDespawn(2);
						break;
					case 2:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_HARD_BOSS_TIMER_02());
						scheduleDespawn(240);
						break;
					case 240:
						PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_HARD_BOSS_TIMER_03());
						scheduleDespawn(60);
						break;
					case 60:
						getOwner().queueSkill(21534, 1, 3000);
						break;
				}
			}
		}, delayInSec * 1000);
	}

	protected void removeBossEntries() {
		PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_HARD_BOSS_PORTAL_DESTROY());
		getPosition().getWorldMapInstance().getNpcs(702216).stream().filter(p -> p != null).forEach(p -> p.getController().delete());
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 21534:
				getOwner().getController().delete();
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDF5_U3_HARD_BOSS_TIMER_04());
				break;
		}
	}
}
