package ai.instance.shugoImperialTomb;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Ritsu
 */
@AIName("shugodelightedadmirer")
public class ShugoDelightedAdmirerAI extends NpcAI {

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		int instanceId = player.getInstanceId();

		switch (dialogActionId) {
			case SETPRO1:
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
				switch (getNpcId()) {
					case 831114:
					case 831306:
						TeleportService.teleportTo(player, 300560000, instanceId, 346.27332f, 424.07101f, 294.75793f, (byte) 90, TeleportAnimation.FADE_OUT_BEAM);
						break;
					case 831115:
					case 831195:
						TeleportService.teleportTo(player, 300560000, instanceId, 450.8527f, 105.94637f, 212.20023f, (byte) 90, TeleportAnimation.FADE_OUT_BEAM);
						break;
				}
				break;
			case SETPRO2:
				switch (getNpcId()) {
					case 831114:
					case 831306:
						skillId = player.getRace() == Race.ASMODIANS ? 21104 : 21095;
						SkillEngine.getInstance().applyEffectDirectly(skillId, player, player, 0);
						break;
					case 831115:
					case 831195:
						skillId = player.getRace() == Race.ASMODIANS ? 21105 : 21096;
						SkillEngine.getInstance().applyEffectDirectly(skillId, player, player, 0);
						break;
				}
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1012));
		}
		return true;
	}
}
