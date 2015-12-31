package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.dao.PlayerAppearanceDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerEnterWorldService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * In this packets aion client is requesting edit of character.
 * 
 * @author IlBuono
 */
public class CM_CHARACTER_EDIT extends AionClientPacket {

	private int objectId;

	private boolean gender_change;

	private boolean check_ticket = true;

	/**
	 * Constructs new instance of <tt>CM_CREATE_CHARACTER </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_CHARACTER_EDIT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		AionConnection client = getConnection();
		Account account = client.getAccount();
		objectId = readD();
		readB(52);
		if (account.getPlayerAccountData(objectId) == null) {
			return;
		}
		Player player = PlayerService.getPlayer(objectId, account);
		if (player == null) {
			return;
		}

		if (player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_EDIT_CHARACTERS) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		PlayerCommonData playerCommonData = player.getCommonData();
		PlayerAppearance playerAppearance = player.getPlayerAppearance();
		// Before modify appearance, we do a check of ticket
		int gender = readD();
		gender_change = playerCommonData.getGender().getGenderId() == gender ? false : true;
		if (!gender_change) {
			if (player.getInventory().getItemCountByItemId(169650000) == 0 && player.getInventory().getItemCountByItemId(169650001) == 0
				&& player.getInventory().getItemCountByItemId(169650002) == 0 && player.getInventory().getItemCountByItemId(169650003) == 0
				&& player.getInventory().getItemCountByItemId(169650004) == 0 && player.getInventory().getItemCountByItemId(169650005) == 0
				&& player.getInventory().getItemCountByItemId(169650006) == 0 && player.getInventory().getItemCountByItemId(169650007) == 0) {
				check_ticket = false;
				return;
			}
		} else {
			if (player.getInventory().getItemCountByItemId(169660000) == 0 && player.getInventory().getItemCountByItemId(169660001) == 0
				&& player.getInventory().getItemCountByItemId(169660002) == 0 && player.getInventory().getItemCountByItemId(169660003) == 0) {
				check_ticket = false;
				return;
			}
		}
		playerCommonData.setGender(gender == 0 ? Gender.MALE : Gender.FEMALE);
		readD(); // race
		readD(); // player class

		playerAppearance.setVoice(readD());
		playerAppearance.setSkinRGB(readD());
		playerAppearance.setHairRGB(readD());
		playerAppearance.setEyeRGB(readD());
		playerAppearance.setLipRGB(readD());
		playerAppearance.setFace(readC());
		playerAppearance.setHair(readC());
		playerAppearance.setDeco(readC());
		playerAppearance.setTattoo(readC());
		playerAppearance.setFaceContour(readC());
		playerAppearance.setExpression(readC());
		readC(); // always 4 o0 // 5 in 1.5.x
		playerAppearance.setJawLine(readC());
		playerAppearance.setForehead(readC());

		playerAppearance.setEyeHeight(readC());
		playerAppearance.setEyeSpace(readC());
		playerAppearance.setEyeWidth(readC());
		playerAppearance.setEyeSize(readC());
		playerAppearance.setEyeShape(readC());
		playerAppearance.setEyeAngle(readC());

		playerAppearance.setBrowHeight(readC());
		playerAppearance.setBrowAngle(readC());
		playerAppearance.setBrowShape(readC());

		playerAppearance.setNose(readC());
		playerAppearance.setNoseBridge(readC());
		playerAppearance.setNoseWidth(readC());
		playerAppearance.setNoseTip(readC());

		playerAppearance.setCheek(readC());
		playerAppearance.setLipHeight(readC());
		playerAppearance.setMouthSize(readC());
		playerAppearance.setLipSize(readC());
		playerAppearance.setSmile(readC());
		playerAppearance.setLipShape(readC());
		playerAppearance.setJawHeigh(readC());
		playerAppearance.setChinJut(readC());
		playerAppearance.setEarShape(readC());
		playerAppearance.setHeadSize(readC());

		playerAppearance.setNeck(readC());
		playerAppearance.setNeckLength(readC());

		playerAppearance.setShoulderSize(readC());

		playerAppearance.setTorso(readC());
		playerAppearance.setChest(readC()); // only woman
		playerAppearance.setWaist(readC());
		playerAppearance.setHips(readC());

		playerAppearance.setArmThickness(readC());

		playerAppearance.setHandSize(readC());
		playerAppearance.setLegThicnkess(readC());

		playerAppearance.setFootSize(readC());
		playerAppearance.setFacialRate(readC());

		readC(); // always 0
		playerAppearance.setArmLength(readC());
		playerAppearance.setLegLength(readC()); // wrong??
		playerAppearance.setShoulders(readC()); // 1.5.x May be ShoulderSize
		playerAppearance.setFaceShape(readC());
		readC();
		readC();
		readC();
		playerAppearance.setHeight(readF());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		AionConnection client = getConnection();
		PlayerEnterWorldService.enterWorld(client, objectId, true);
		Player player = client.getActivePlayer();
		if (!check_ticket) {
			if (!gender_change)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EDIT_CHAR_ALL_CANT_NO_ITEM);
			else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EDIT_CHAR_GENDER_CANT_NO_ITEM);
		} else {
			// Remove ticket and save appearance
			if (!gender_change) {
				if (player.getInventory().getItemCountByItemId(169650000) > 0) { // Plastic Surgery Ticket
					player.getInventory().decreaseByItemId(169650000, 1);
				} else if (player.getInventory().getItemCountByItemId(169650001) > 0) { // [Event] Plastic Surgery Ticket
					player.getInventory().decreaseByItemId(169650001, 1);
				} else if (player.getInventory().getItemCountByItemId(169650002) > 0) { // [Special] Plastic Surgery Ticket
					player.getInventory().decreaseByItemId(169650002, 1);
				} else if (player.getInventory().getItemCountByItemId(169650003) > 0) { // [Special] Plastic Surgery Ticket
					player.getInventory().decreaseByItemId(169650003, 1);
				} else if (player.getInventory().getItemCountByItemId(169650004) > 0) { // Plastic Surgery Ticket (60 mins)
					player.getInventory().decreaseByItemId(169650004, 1);
				} else if (player.getInventory().getItemCountByItemId(169650005) > 0) { // Plastic Surgery Ticket (60 mins)
					player.getInventory().decreaseByItemId(169650005, 1);
				} else if (player.getInventory().getItemCountByItemId(169650006) > 0) { // [Event] Plastic Surgery Ticket
					player.getInventory().decreaseByItemId(169650006, 1);
				} else if (player.getInventory().getItemCountByItemId(169650007) > 0) { // [Event] Plastic Surgery Ticket
					player.getInventory().decreaseByItemId(169650007, 1);
				}
			} else {
				if (player.getInventory().getItemCountByItemId(169660000) > 0) { // Gender Switch Ticket
					player.getInventory().decreaseByItemId(169660000, 1);
				} else if (player.getInventory().getItemCountByItemId(169660001) > 0) { // [Event] Gender Switch Ticket
					player.getInventory().decreaseByItemId(169660001, 1);
				} else if (player.getInventory().getItemCountByItemId(169660002) > 0) { // Gender Switch Ticket (60 min)
					player.getInventory().decreaseByItemId(169660002, 1);
				} else if (player.getInventory().getItemCountByItemId(169660003) > 0) { // [Event] Gender Switch Ticket
					player.getInventory().decreaseByItemId(169660003, 1);
				}
				DAOManager.getDAO(PlayerDAO.class).storePlayer(player); // save new gender
			}
			DAOManager.getDAO(PlayerAppearanceDAO.class).store(player); // save new appearance
		}
	}
}
