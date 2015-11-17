package com.aionemu.gameserver.network.aion;

import java.util.List;

import javolution.util.FastTable;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.MailDAO;
import com.aionemu.gameserver.dao.PlayerSettingsDAO;
import com.aionemu.gameserver.model.account.CharacterBanInfo;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.services.BrokerService;

/**
 * @author AEJTester, Nemesiss, Niato
 * @modified Neon
 */
public abstract class PlayerInfo extends AionServerPacket {

	protected PlayerInfo() {
	}

	protected void writePlayerInfo(PlayerAccountData accPlData) {
		PlayerCommonData pcd = accPlData.getPlayerCommonData();
		int playerId = pcd.getPlayerObjId();
		int raceId = pcd.getRace().getRaceId();
		int genderId = pcd.getGender().getGenderId();
		PlayerAppearance playerAppearance = accPlData.getAppearance();
		CharacterBanInfo cbi = accPlData.getCharBanInfo();
		boolean isBanned = (cbi != null && cbi.getEnd() > System.currentTimeMillis() / 1000);

		List<Item> itemList = new FastTable<Item>();
		for (Item item : accPlData.getEquipment()) {
			if (itemList.size() == 16)
				break;
			if (ItemSlot.isVisible(item.getEquipmentSlot()))
				itemList.add(item);
		}

		writeD(playerId);
		writeS(pcd.getName(), 52);
		writeD(genderId);
		writeD(raceId);
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
		writeC(4);// always 4 o0
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
		writeC(playerAppearance.getLegThicnkess());
		writeC(playerAppearance.getFootSize());
		writeC(playerAppearance.getFacialRate());
		writeC(0x00); // 0x00
		writeC(playerAppearance.getArmLength());
		writeC(playerAppearance.getLegLength());
		writeC(playerAppearance.getShoulders());
		writeC(playerAppearance.getFaceShape());
		writeC(0x00); // always 0 may be acessLevel
		writeC(0x00); // always 0 - unk
		writeC(0x00);
		writeF(playerAppearance.getHeight());
		int raceSex = 100000 + raceId * 2 + genderId;
		writeD(raceSex);
		writeD(pcd.getPosition().getMapId());// mapid for preloading map
		writeF(pcd.getPosition().getX());
		writeF(pcd.getPosition().getY());
		writeF(pcd.getPosition().getZ());
		writeD(pcd.getPosition().getHeading());
		writeH(pcd.getLevel());
		writeH(0); // unk 2.5
		writeD(pcd.getTitleId());
		writeD(accPlData.isLegionMember() ? accPlData.getLegion().getLegionId() : 0);
		writeS(accPlData.isLegionMember() ? accPlData.getLegion().getLegionName() : "", 82);
		writeH(accPlData.isLegionMember() ? 1 : 0);
		writeD(pcd.getLastOnline() != null ? (int) pcd.getLastOnline().getTime() : 0);// last online
		for (int i = 0; i < 16; i++) { // 16 items is always expected by the client...
			Item item = i < itemList.size() ? itemList.get(i) : null;
			writeC(item == null ? 0 : ItemSlot.getEquipmentSlotType(item.getEquipmentSlot())); // 0 = not visible, 1 = default (right-hand) slot, 2 = secondary (left-hand) slot
			writeD(item == null ? 0 : item.getItemSkinTemplate().getTemplateId());
			writeD(item == null || item.getGodStone() == null ? 0 : item.getGodStone().getItemId());
			writeD(item == null ? 0 : item.getItemColor());
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
		writeD(0);
		writeD(DAOManager.getDAO(MailDAO.class).haveUnread(playerId) ? 1 : 0); // mail
		writeD(0); // unk
		writeD(0); // unk
		writeQ(BrokerService.getInstance().getCollectedMoney(pcd)); // collected money from broker
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		// client wants int so let's hope we do not reach long limit with timestamp while this server is used :P
		writeD(isBanned ? (int) cbi.getStart() : 0); // startPunishDate
		writeD(isBanned ? (int) cbi.getEnd() : 0); // endPunishDate
		writeS(isBanned ? cbi.getReason() : "");
	}
}
