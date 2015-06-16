package ai.siege;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.MercenariesService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PacketSendUtility;


/**
 * @author ViAl
 * @modified Whoop
 */
@AIName("mercenary")
public class MercenaryAI2 extends NpcAI2 {
	
	@Override
	protected void handleDialogStart(Player player) {
		if(!player.isLegionMember()) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
			return;
		}
		SiegeNpc owner = (SiegeNpc) getOwner();
		int siegeId = owner.getSiegeId();
		FortressLocation location = SiegeService.getInstance().getFortress(siegeId);
		if(location.getLegionId() != player.getLegion().getLegionId()) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
			return;
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		SiegeNpc owner = (SiegeNpc) getOwner();
		switch (DialogAction.getActionByDialogId(dialogId)) {
			case SELECT_ACTION_1097:
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1097));
				break;
			case SELECT_ACTION_1352:
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1352));
				break;
			case SELECT_ACTION_1693:
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1693));
				break;
			case SELECT_ACTION_2034:
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 2034));
				break;
			case SETPRO1:
				switch (owner.getSiegeId()) {
					case 1011:
						MercenariesService.checkMercenaryZone(player, owner, 36, owner.getSiegeId(), 1402452, 1402448, 1);
						break;
					case 1221:
						MercenariesService.checkMercenaryZone(player, owner, 4, owner.getSiegeId(), 1402176, 1402173, 1);
						break;
					case 1231:
						MercenariesService.checkMercenaryZone(player, owner, 4, owner.getSiegeId(), 1402182, 1402179, 1);
						break;
					case 1241:
						MercenariesService.checkMercenaryZone(player, owner, 4, owner.getSiegeId(), 1402188, 1402185, 1);
						break;
					case 2011:
						switch (owner.getNpcId()) {
							case 832043:
							case 832059:
								MercenariesService.checkMercenaryZone(player, owner, 11, owner.getSiegeId(), 1402301, 1402292, 1);
								break;
							case 832044:
							case 832060:
								MercenariesService.checkMercenaryZone(player, owner, 6, owner.getSiegeId(), 1402305, 1402296, 2);
								break;
							case 832045:
							case 832061:
								MercenariesService.checkMercenaryZone(player, owner, 3, owner.getSiegeId(), 1402302, 1402293, 3);
								break;
							case 832046:
							case 832062:
								MercenariesService.checkMercenaryZone(player, owner, 3, owner.getSiegeId(), 1402304, 1402294, 4);
								break;
						}
						break;
					case 2021:
						switch (owner.getNpcId()) {
							case 832047:
							case 832063:
								MercenariesService.checkMercenaryZone(player, owner, 11, owner.getSiegeId(), 1402319, 1402310, 1);
								break;
							case 832048:
							case 832064:
								MercenariesService.checkMercenaryZone(player, owner, 6, owner.getSiegeId(), 1402323, 1402314, 2);
								break;
							case 832049:
							case 832065:
								MercenariesService.checkMercenaryZone(player, owner, 3, owner.getSiegeId(), 1402320, 1402311, 3);
								break;
							case 832050:
							case 832066:
								MercenariesService.checkMercenaryZone(player, owner, 3, owner.getSiegeId(), 1402322, 1402312, 4);
								break;
						}
						break;
					case 3011:
						switch (owner.getNpcId()) {
							case 832051:
							case 832067:
								MercenariesService.checkMercenaryZone(player, owner, 11, owner.getSiegeId(), 1402337, 1402328, 1);
								break;
							case 832052:
							case 832068:
								MercenariesService.checkMercenaryZone(player, owner, 6, owner.getSiegeId(), 1402341, 1402332, 2);
								break;
							case 832053:
							case 832069:
								MercenariesService.checkMercenaryZone(player, owner, 3, owner.getSiegeId(), 1402338, 1402329, 3);
								break;
							case 832054:
							case 832070:
								MercenariesService.checkMercenaryZone(player, owner, 3, owner.getSiegeId(), 1402339, 1402330, 4);
								break;
						}
						break;
					case 3021:
						switch (owner.getNpcId()) {
							case 832055:
							case 832071:
								MercenariesService.checkMercenaryZone(player, owner, 11, owner.getSiegeId(), 1402355, 1402346, 1);
								break;
							case 832056:
							case 832072:
								MercenariesService.checkMercenaryZone(player, owner, 6, owner.getSiegeId(), 1402359, 1402350, 2);
								break;
							case 832057:
							case 832073:
								MercenariesService.checkMercenaryZone(player, owner, 3, owner.getSiegeId(), 1402356, 1402347, 3);
								break;
							case 832058:
							case 832074:
								MercenariesService.checkMercenaryZone(player, owner, 3, owner.getSiegeId(), 1402358, 1402348, 4);
								break;
						}
						break;
					case 5011:
						MercenariesService.checkMercenaryZone(player, owner, 16, owner.getSiegeId(), 1401864, 1401828, 1);
						break;
					case 6011:
						MercenariesService.checkMercenaryZone(player, owner, 14, owner.getSiegeId(), 1401867, 1401831, 1);
						break;
					case 6021:
						MercenariesService.checkMercenaryZone(player, owner, 12, owner.getSiegeId(), 1401870, 1401834, 1);
						break;
				}
				break;
			case SETPRO2:
				switch (owner.getSiegeId()) {
					case 1011:
						MercenariesService.checkMercenaryZone(player, owner, 36, owner.getSiegeId(), 1402452, 1402449, 1);
						break;
					case 1221:
						MercenariesService.checkMercenaryZone(player, owner, 4, owner.getSiegeId(), 1402177, 1402174, 2);
						break;
					case 1231:
						MercenariesService.checkMercenaryZone(player, owner, 4, owner.getSiegeId(), 1402183, 1402180, 2);
						break;
					case 1241:
						MercenariesService.checkMercenaryZone(player, owner, 4, owner.getSiegeId(), 1402189, 1402186, 2);
						break;
					case 5011:
						MercenariesService.checkMercenaryZone(player, owner, 8, owner.getSiegeId(), 1401865, 1401829, 2);
						break;
					case 6011:
						MercenariesService.checkMercenaryZone(player, owner, 10, owner.getSiegeId(), 1401868, 1401832, 2);
						break;
					case 6021:
						MercenariesService.checkMercenaryZone(player, owner, 20, owner.getSiegeId(), 1401871, 1401835, 2);
						break;
				}
				break;
			case SETPRO3:
				switch (owner.getSiegeId()) {
					case 1221:
						MercenariesService.checkMercenaryZone(player, owner, 12, owner.getSiegeId(), 1402178, 1402175, 3);
						break;
					case 1231:
						MercenariesService.checkMercenaryZone(player, owner, 8, owner.getSiegeId(), 1402184, 1402181, 3);
						break;
					case 5011:
						MercenariesService.checkMercenaryZone(player, owner, 16, owner.getSiegeId(), 1401866, 1401830, 3);
						break;
					case 6011:
						MercenariesService.checkMercenaryZone(player, owner, 10, owner.getSiegeId(), 1401869, 1401833, 3);
						break;
					case 6021:
						MercenariesService.checkMercenaryZone(player, owner, 12, owner.getSiegeId(), 1401872, 1401836, 3);
						break;
				}
				break;
		}
		return true;
	}	
}