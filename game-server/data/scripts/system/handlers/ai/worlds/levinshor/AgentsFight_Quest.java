package ai.worlds.levinshor;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.ZoneName;

import ai.GeneralNpcAI2;

/**
 * @author Yeats
 *
 */
@AIName("agentsfight_quest")
public class AgentsFight_Quest extends GeneralNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		for (Player player : World.getInstance().getWorldMap(600100000).getMainWorldMapInstance().getPlayersInside()) {
			if (player.isInsideZone(ZoneName.get("DRAGON_LORDS_SHRINE_600100000")) || player.isInsideZone(ZoneName.get("FLAMEBERTH_DOWNS_600100000"))) {
				if (player.getRace() == Race.ELYOS) {
					QuestState qs = player.getQuestStateList().getQuestState(1);
					if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
						QuestService.startQuest(new QuestEnv(null, player, 13744, 0));
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
					}
				} else {
					QuestState qs = player.getQuestStateList().getQuestState(1);
					if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
						QuestService.startQuest(new QuestEnv(null, player, 23744, 0));
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
					}
				}
			}
		}
	}
}
