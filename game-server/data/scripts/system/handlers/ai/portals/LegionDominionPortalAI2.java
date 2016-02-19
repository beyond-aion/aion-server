package ai.portals;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.teleport.PortalService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import instance.StonespearRanchInstance;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * Created by Yeats on 19.02.2016.
 */
@AIName("legion_dominion_portal")
public class LegionDominionPortalAI2 extends PortalDialogAI2 {

	private static int mapId = 0;

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId != DialogAction.SETPRO1.id()) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, questId));
			return true;
		}if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.WEDNESDAY
				&& LocalDateTime.now().getHour() >= 8 && LocalDateTime.now().getHour() <= 10) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401306, new DescriptionId(mapId)));
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, questId));
			return true;
		}

		if (player.getLevel() < 65) {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400179));
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, questId));
			return true;
		}

		if (player.isInAlliance2() && !player.isInLeague()) {
			PortalPath portalPath = DataManager.PORTAL2_DATA.getPortalDialog(getNpcId(), dialogId, player.getRace());
			WorldMapInstance instance = InstanceService.getRegisteredInstance(mapId, player.getPlayerAlliance2().getObjectId());
			if (portalPath == null) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401106));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, questId));
				return true;
			}
			if (instance == null) {
				if (hasCd(player)) {
					return true;
				}
				// only alliance leader can open this instance
				if (player.getPlayerAlliance2().isSomeCaptain(player)) {
					if (player.getInventory().decreaseByItemId(2, 1)) {
						PortalService.port(portalPath, player, getObjectId());
						return true;
					} else {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_CANT_ENTER_WITHOUT_ITEM);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, 0));
						return true;
					}
				} else {
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400182));
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, questId));
					return true;
				}
			} else if (!instance.isRegistered(player.getObjectId())) {
				if (hasCd(player)) {
					return true;
				}

				if (instance.getInstanceHandler() instanceof StonespearRanchInstance) {
					StonespearRanchInstance handler = (StonespearRanchInstance) instance.getInstanceHandler();

					if (handler.canEnter(player)) {
						PortalService.port(portalPath, player, getObjectId());
					} else {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, questId));
						return true;
					}
				}
			} else if (instance.isRegistered(player.getObjectId())) {
				PortalService.port(portalPath, player, getObjectId());
				return true;
			} else {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400185));
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, questId));
				return true;
			}
		} else {
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400544));
		}

		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, questId));
		return true;
	}

	private boolean hasCd(Player player) {
		if (player.getPortalCooldownList().isPortalUseDisabled(mapId)) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, 0));
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_MAKE_INSTANCE_COOL_TIME);
			return true;
		}
		return false;
	}
}
