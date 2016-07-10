package ai.siege;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.siege.FortressSiege;
import com.aionemu.gameserver.services.siege.MercenaryLocation;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ViAl
 * @reworked Whoop
 */
@AIName("mercenary")
public class MercenaryAI2 extends GeneralNpcAI2 {

	@Override
	protected void handleDialogStart(Player player) {
		if (!player.isLegionMember()) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
			return;
		}
		int siegeId = ((SiegeNpc) getOwner()).getSiegeId();
		FortressLocation location = SiegeService.getInstance().getFortress(siegeId);
		if (!location.isVulnerable())
			return;
		if (location.getLegionId() != player.getLegion().getLegionId()) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
			return;
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		int siegeId = ((SiegeNpc) getOwner()).getSiegeId();
		int zoneId = 0;
		switch (DialogAction.getByActionId(dialogId)) {
			case SELECT_ACTION_1097:
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1097));
				break;
			case SELECT_ACTION_1182:
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1182));
				break;
			case SELECT_ACTION_1267:
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1267));
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
				switch (siegeId) {
					case 1011:
					case 1221:
					case 1231:
					case 1241:
						zoneId = 1;
						break;
					case 2011:
					case 2021:
					case 3011:
					case 3021:
					case 7011:
						switch (getNpcId()) {
							case 832043: // 2011
							case 832059:
							case 832047: // 2021
							case 832063:
							case 832051: // 3011
							case 832067:
							case 832055: // 3021
							case 832071:
							case 804557: // 7011
							case 804558:
								zoneId = 1;
								break;
							case 832044: // 2011
							case 832060:
							case 832048: // 2021
							case 832064:
							case 832052: // 3011
							case 832068:
							case 832056: // 3021
							case 832072:
								zoneId = 2;
								break;
							case 832045: // 2011
							case 832061:
							case 832049: // 2021
							case 832065:
							case 832053: // 3011
							case 832069:
							case 832057: // 3021
							case 832073:
							case 802435: // 7011
							case 802436:
								zoneId = 3;
								break;
							case 832046: // 2011
							case 832062:
							case 832050: // 2021
							case 832066:
							case 832054: // 3011
							case 832070:
							case 832058: // 3021
							case 832074:
								zoneId = 4;
								break;
							case 804559:
							case 804560:
								zoneId = 5;
						}
						break;
				}
				break;
			case SETPRO2:
				switch (siegeId) {
					case 1011:
					case 1221:
					case 1231:
					case 1241:
						zoneId = 2;
						break;
					case 7011:
						switch (getNpcId()) {
							case 804557:
							case 804558:
								zoneId = 2;
								break;
							case 802435:
							case 802436:
								zoneId = 4;
								break;
							case 804559:
							case 804560:
								zoneId = 6;
								break;
						}
						break;
				}
				break;
			case SETPRO3:
				switch (siegeId) {
					case 1221:
					case 1231:
						zoneId = 3;
						break;
					case 7011: // Currently no npcid switch necessary
						zoneId = 7;
				}
				break;
		}
		checkMercenaryZone(player, siegeId, zoneId);
		return true;
	}

	private void checkMercenaryZone(Player player, int siegeId, int zoneId) {
		FortressSiege siege = (FortressSiege) SiegeService.getInstance().getSiege(siegeId);
		if (siege == null)
			return;
		MercenaryLocation mLoc = siege.getMercenaryLocationByZoneId(zoneId);
		if (mLoc == null)
			return;
		if (!mLoc.isRequestValid())
			return;
		if (!hasRequiredItems(player, mLoc.getCosts()))
			return;
		player.getInventory().decreaseByItemId(186000236, mLoc.getCosts());
		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(mLoc.getMsgId()));
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 2375));
		mLoc.spawn();
	}

	private boolean hasRequiredItems(Player player, long itemCount) {
		long count = player.getInventory().getItemCountByItemId(186000236);
		if (count < itemCount) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 27));
			return false;
		}
		return true;
	}

}
