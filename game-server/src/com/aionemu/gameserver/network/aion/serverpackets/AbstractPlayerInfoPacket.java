package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.MailDAO;
import com.aionemu.gameserver.dao.PlayerSettingsDAO;
import com.aionemu.gameserver.model.account.CharacterBanInfo;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.BrokerService;

/**
 * @author AEJTester, Nemesiss, Niato, Neon
 */
public abstract class AbstractPlayerInfoPacket extends AionServerPacket {

	/**
	 * The maximum number of characters the client can display. The client expects a fixed size text buffer in various packets. 
	 */
	public static final int CHARNAME_MAX_LENGTH = 25;

	protected void writePlayerInfo(PlayerAccountData accPlData) {
		PlayerCommonData pcd = accPlData.getPlayerCommonData();
		int playerId = pcd.getPlayerObjId();
		PlayerAppearance playerAppearance = accPlData.getAppearance();
		CharacterBanInfo cbi = accPlData.getCharBanInfo();
		boolean isBanned = (cbi != null && cbi.getEnd() > System.currentTimeMillis() / 1000);

		List<Item> itemList = new ArrayList<>(16);
		for (Item item : accPlData.getEquipment()) {
			if (itemList.size() == 16)
				break;
			if (ItemSlot.isVisible(item.getEquipmentSlot()))
				itemList.add(item);
		}

		writeD(playerId);
		writeS(pcd.getName(), CHARNAME_MAX_LENGTH);
		writeD(pcd.getGender().getGenderId());
		writeD(pcd.getRace().getRaceId());
		writeD(pcd.getPlayerClass().getClassId());
		writeD(playerAppearance.getVoice());
		writeD(playerAppearance.getSkinRGB());
		writeD(playerAppearance.getHairRGB());
		writeD(playerAppearance.getEyeRGB());
		writeD(playerAppearance.getLipRGB());
		writeC(playerAppearance.getFace());
		writeC(playerAppearance.getHair());
		writeC(playerAppearance.getDeco());
		writeC(playerAppearance.getTattoo());
		writeC(playerAppearance.getFaceContour());
		writeC(playerAppearance.getExpression());
		writeC(5);// always 5 o0
		writeC(playerAppearance.getJawLine());
		writeC(playerAppearance.getForehead());
		writeC(playerAppearance.getEyeHeight());
		writeC(playerAppearance.getEyeSpace());
		writeC(playerAppearance.getEyeWidth());
		writeC(playerAppearance.getEyeSize());
		writeC(playerAppearance.getEyeShape());
		writeC(playerAppearance.getEyeAngle());
		writeC(playerAppearance.getBrowHeight());
		writeC(playerAppearance.getBrowAngle());
		writeC(playerAppearance.getBrowShape());
		writeC(playerAppearance.getNose());
		writeC(playerAppearance.getNoseBridge());
		writeC(playerAppearance.getNoseWidth());
		writeC(playerAppearance.getNoseTip());
		writeC(playerAppearance.getCheek());
		writeC(playerAppearance.getLipHeight());
		writeC(playerAppearance.getMouthSize());
		writeC(playerAppearance.getLipSize());
		writeC(playerAppearance.getSmile());
		writeC(playerAppearance.getLipShape());
		writeC(playerAppearance.getJawHeigh());
		writeC(playerAppearance.getChinJut());
		writeC(playerAppearance.getEarShape());
		writeC(playerAppearance.getHeadSize());
		// 1.5.x 0x00, shoulderSize, armLength, legLength (BYTE) after HeadSize
		writeC(playerAppearance.getNeck());
		writeC(playerAppearance.getNeckLength());
		writeC(playerAppearance.getShoulderSize());
		writeC(playerAppearance.getTorso());
		writeC(playerAppearance.getChest());
		writeC(playerAppearance.getWaist());
		writeC(playerAppearance.getHips());
		writeC(playerAppearance.getArmThickness());
		writeC(playerAppearance.getHandSize());
		writeC(playerAppearance.getLegThickness());
		writeC(playerAppearance.getFootSize());
		writeC(playerAppearance.getFacialRate());
		writeC(0x00); // 0x00
		writeC(playerAppearance.getArmLength());
		writeC(playerAppearance.getLegLength());
		writeC(playerAppearance.getShoulders());
		writeC(playerAppearance.getFaceShape());
		writeC(0x00); // always 0 may be acessLevel
		writeC(0x00); // sometimes 0xC7 (199) for all chars, else 0
		writeC(0x00); // sometimes 0x04 (4) for all chars, else 0
		writeF(playerAppearance.getHeight());
		writeD(pcd.getTemplateId());
		writeD(pcd.getPosition().getMapId());// mapid for preloading map
		writeF(pcd.getPosition().getX());
		writeF(pcd.getPosition().getY());
		writeF(pcd.getPosition().getZ());
		writeD(pcd.getPosition().getHeading());
		writeH(pcd.getLevel());
		writeH(0); // unk 2.5
		writeD(pcd.getTitleId());
		writeD(accPlData.isLegionMember() ? accPlData.getLegion().getLegionId() : 0);
		writeS(accPlData.isLegionMember() ? accPlData.getLegion().getName() : null, 40);
		writeH(accPlData.isLegionMember() ? 1 : 0);
		writeD(pcd.getLastOnlineEpochSeconds());
		for (int i = 0; i < 16; i++) { // 16 items is always expected by the client...
			Item item = i < itemList.size() ? itemList.get(i) : null;
			writeC(item == null ? 0 : ItemSlot.getEquipmentSlotType(item.getEquipmentSlot())); // 0 = not visible, 1 = default (right-hand) slot, 2 = secondary (left-hand) slot
			writeD(item == null ? 0 : item.getItemSkinTemplate().getTemplateId());
			writeD(item == null ? 0 : item.getGodStoneId());
			writeDyeInfo(item == null ? null : item.getItemColor());
		}
		writeD(0);
		writeD(0);
		writeD(0); // 4.5
		writeD(0); // 4.5
		writeD(0); // 4.5
		writeD(0); // 4.5
		writeB(new byte[68]); // 4.7
		writeD(accPlData.getDeletionTimeInSeconds());
		writeH(DAOManager.getDAO(PlayerSettingsDAO.class).loadSettings(playerId).getDisplay()); // display helmet 0 show, 5 dont show , possible bit operation
		writeH(0);
		writeD(0); // total mail count
		writeD(DAOManager.getDAO(MailDAO.class).haveUnread(playerId) ? 1 : 0); // unread mail count
		writeD(0); // express mail count
		writeD(0); // blackcloud mail count
		writeQ(BrokerService.getInstance().getEarnedKinahFromSoldItems(pcd)); // collected money from broker
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(isBanned ? (int) cbi.getStart() : 0); // startPunishDate
		writeD(isBanned ? (int) cbi.getEnd() : 0); // endPunishDate
		writeS(isBanned ? cbi.getReason() : "");
	}

	protected void writeEquippedItems(List<Item> items) {
		int mask = 0;
		for (Item item : items) {
			mask |= item.getEquipmentSlot();
			// remove sub hand mask bits (sub hand is present on TwoHandeds by default and would produce display bugs)
			if (ItemSlot.isTwoHandedWeapon(item.getEquipmentSlot()))
				mask &= ~ItemSlot.SUB_HAND.getSlotIdMask();
		}

		writeD(mask);
		for (Item item : items) {
			writeD(item.getItemSkinTemplate().getTemplateId());
			writeD(item.getGodStoneId());
			writeDyeInfo(item.getItemColor());
			writeH(item.getItemEnchantParam());
			writeH(0); // 4.7
		}
	}
}
