package ai.portals;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.PortalService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * Created by Yeats on 22.02.2016.
 */
@AIName("sealed_danuar_mysticarium_portal")
public class SealedDanuarMysticariumPortals extends PortalDialogAI {

    @Override
    public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
        if (getNpcId() == 730721 || getNpcId() == 730722) {
            PortalPath portalPath = DataManager.PORTAL2_DATA.getPortalDialog(getNpcId(), dialogId, player.getRace());
            WorldMapInstance instance = InstanceService.getRegisteredInstance(300480000, player.getObjectId());
            if (instance == null) {
                if (!player.getPortalCooldownList().isPortalUseDisabled(300480000)) {
                    if (player.getInventory().decreaseByItemId(185000223, 1)) {
                        PortalService.port(portalPath, player, getObjectId());
                        return true;
                    } else {
                        PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_CANT_ENTER_WITHOUT_ITEM());
                        PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, 0));
                        return true;
                    }
                } else {
                    PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANNOT_MAKE_INSTANCE_COOL_TIME());
                    PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, 0));
                    return true;
                }
            } else {
                PortalService.port(portalPath, player, getObjectId());
                PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, 0));
                return true;
            }
        } else {
            if (!getOwner().isInInstance()) {
                return true;
            }
            if (getNpcId() == 731583) {
                if (dialogId == 10000) {
                    AIActions.handleUseItemFinish(this, player);
                    PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0, questId));
                }
            }
        }
        return true;
    }
}
