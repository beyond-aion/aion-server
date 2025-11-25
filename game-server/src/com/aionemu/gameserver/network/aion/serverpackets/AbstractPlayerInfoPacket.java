package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.dao.MailDAO;
import com.aionemu.gameserver.dao.PlayerSettingsDAO;
import com.aionemu.gameserver.model.account.CharacterBanInfo;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.player.MultiClientingService;

/**
 * @author AEJTester, Nemesiss, Niato, Neon
 */
public abstract class AbstractPlayerInfoPacket extends AionServerPacket {

	/**
	 * The maximum number of characters the client can display. The client expects a fixed size text buffer in various packets.
	 */
	public static final int CHARNAME_MAX_LENGTH = 25;

	protected void writePlayerInfo(PlayerAccountData accPlData, AionConnection con) {
		PlayerCommonData pcd = accPlData.getPlayerCommonData();
		int playerId = pcd.getPlayerObjId();
		LegionMember legionMember = LegionService.getInstance().getLegionMember(pcd);
		PlayerAppearance playerAppearance = accPlData.getAppearance();
		CharacterBanInfo cbi = getCharBanInfo(accPlData, con);

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
		writeD(pcd.getMapId()); // mapid for preloading map
		writeF(pcd.getX());
		writeF(pcd.getY());
		writeF(pcd.getZ());
		writeD(pcd.getHeading());
		writeH(pcd.getLevel());
		writeH(0); // unk 2.5
		writeD(pcd.getTitleId());
		writeD(legionMember != null ? legionMember.getLegion().getLegionId() : 0);
		writeS(legionMember != null ? legionMember.getLegion().getName() : null, 40);
		writeH(legionMember != null ? 1 : 0);
		writeD(pcd.getLastOnlineEpochSeconds());
		for (int i = 0; i < 16; i++) { // 16 items is always expected by the client...
			PlayerAccountData.VisibleItem item = i < accPlData.getVisibleItems().size() ? accPlData.getVisibleItems().get(i) : null;
			writeC(item == null ? 0 : item.slotType()); // 0 = not visible, 1 = default (right-hand) slot, 2 = secondary (left-hand) slot
			writeD(item == null ? 0 : item.itemId());
			writeD(item == null ? 0 : item.godStoneId());
			writeDyeInfo(item == null ? null : item.color());
		}
		writeD(0);
		writeD(0);
		writeD(0); // 4.5
		writeD(0); // 4.5
		writeD(0); // 4.5
		writeD(0); // 4.5
		writeB(new byte[68]); // 4.7
		writeD(accPlData.getDeletionTimeInSeconds());
		writeH(PlayerSettingsDAO.loadSettings(playerId).getDisplay()); // display helmet 0 show, 5 dont show , possible bit operation
		writeH(0);
		writeD(0); // total mail count
		writeD(MailDAO.haveUnread(playerId) ? 1 : 0); // unread mail count
		writeD(0); // express mail count
		writeD(0); // blackcloud mail count
		writeQ(BrokerService.getInstance().getEarnedKinahFromSoldItems(pcd)); // collected money from broker
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(cbi == null ? 0 : (int) cbi.getStart()); // startPunishDate
		writeD(cbi == null ? 0 : (int) cbi.getEnd()); // endPunishDate
		writeS(cbi == null ? "" : cbi.getReason());
	}

	@SuppressWarnings("lossy-conversions")
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

	private CharacterBanInfo getCharBanInfo(PlayerAccountData playerAccountData, AionConnection con) {
		CharacterBanInfo cbi = playerAccountData.getCharBanInfo();
		long nowSeconds = System.currentTimeMillis() / 1000;
		if (cbi != null && nowSeconds >= cbi.getEnd())
			cbi = null;
		if (cbi == null && SecurityConfig.MULTI_CLIENTING_RESTRICTION_MODE == SecurityConfig.MultiClientingRestrictionMode.SAME_FACTION) {
			int cdMinutes = SecurityConfig.MULTI_CLIENTING_FACTION_SWITCH_COOLDOWN_MINUTES;
			if (cdMinutes > 0 && MultiClientingService.checkForFactionSwitchCooldownTime(playerAccountData.getPlayerCommonData().getRace(), con) != null) {
				int durationSeconds = 61; // client will send CM_CHARACTER_LIST after this duration to update the ban info (<61s corrupts the ban info)
				cbi = new CharacterBanInfo(nowSeconds, durationSeconds, "\n\n\n\uE026 " + cdMinutes + " minute cooldown between switching factions\n\n\n\n\n\n\n");
			}
		}
		return cbi;
	}
}
