package com.aionemu.gameserver.network.aion.clientpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_EDIT_CHAR_GENDER_CANT_NO_ITEM;

import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerEnterWorldService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * In this packet Aion client is requesting edit of character.
 * 
 * @author IlBuono, Neon
 */
public class CM_CHARACTER_EDIT extends AionClientPacket {

	private int objectId;
	private int genderId;
	private PlayerAppearance newAppearance;

	/**
	 * Constructs new instance of <tt>CM_CREATE_CHARACTER </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_CHARACTER_EDIT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		objectId = readD();
		readB(52); // name
		PlayerAccountData playerAccData = getConnection().getAccount().getPlayerAccountData(objectId);
		if (playerAccData == null)
			return;
		if (!playerAccData.getPlayerCommonData().isInEditMode())
			return;

		newAppearance = new PlayerAppearance();
		genderId = readD();
		readD(); // race
		readD(); // player class

		newAppearance.setVoice(readD());
		newAppearance.setSkinRGB(readD());
		newAppearance.setHairRGB(readD());
		newAppearance.setEyeRGB(readD());
		newAppearance.setLipRGB(readD());
		newAppearance.setFace(readUC());
		newAppearance.setHair(readUC());
		newAppearance.setDeco(readUC());
		newAppearance.setTattoo(readUC());
		newAppearance.setFaceContour(readUC());
		newAppearance.setExpression(readUC());
		readC(); // always 4 o0 // 5 in 1.5.x
		newAppearance.setJawLine(readUC());
		newAppearance.setForehead(readUC());

		newAppearance.setEyeHeight(readUC());
		newAppearance.setEyeSpace(readUC());
		newAppearance.setEyeWidth(readUC());
		newAppearance.setEyeSize(readUC());
		newAppearance.setEyeShape(readUC());
		newAppearance.setEyeAngle(readUC());

		newAppearance.setBrowHeight(readUC());
		newAppearance.setBrowAngle(readUC());
		newAppearance.setBrowShape(readUC());

		newAppearance.setNose(readUC());
		newAppearance.setNoseBridge(readUC());
		newAppearance.setNoseWidth(readUC());
		newAppearance.setNoseTip(readUC());

		newAppearance.setCheek(readUC());
		newAppearance.setLipHeight(readUC());
		newAppearance.setMouthSize(readUC());
		newAppearance.setLipSize(readUC());
		newAppearance.setSmile(readUC());
		newAppearance.setLipShape(readUC());
		newAppearance.setJawHeigh(readUC());
		newAppearance.setChinJut(readUC());
		newAppearance.setEarShape(readUC());
		newAppearance.setHeadSize(readUC());

		newAppearance.setNeck(readUC());
		newAppearance.setNeckLength(readUC());

		newAppearance.setShoulderSize(readUC());

		newAppearance.setTorso(readUC());
		newAppearance.setChest(readUC()); // only woman
		newAppearance.setWaist(readUC());
		newAppearance.setHips(readUC());

		newAppearance.setArmThickness(readUC());

		newAppearance.setHandSize(readUC());
		newAppearance.setLegThicnkess(readUC());

		newAppearance.setFootSize(readUC());
		newAppearance.setFacialRate(readUC());

		readC(); // always 0
		newAppearance.setArmLength(readUC());
		newAppearance.setLegLength(readUC()); // wrong??
		newAppearance.setShoulders(readUC()); // 1.5.x May be ShoulderSize
		newAppearance.setFaceShape(readUC());
		readC();
		readC();
		readC();
		newAppearance.setHeight(readF());
	}

	@Override
	protected void runImpl() {
		AionConnection client = getConnection();
		int[] appearanceTickets = { 169650000, 169650001, 169650002, 169650003, 169650004, 169650005, 169650006, 169650007, 169650008 };
		int[] genderTickets = { 169660000, 169660001, 169660002, 169660003, 169660004 };
		PlayerEnterWorldService.enterWorld(client, objectId);
		Player player = client.getActivePlayer();

		boolean isGenderSwitch = player.getGender().getGenderId() != genderId;
		int[] ticketIds = isGenderSwitch ? genderTickets : appearanceTickets;
		SM_SYSTEM_MESSAGE errorMsg = isGenderSwitch ? STR_EDIT_CHAR_GENDER_CANT_NO_ITEM() : STR_EDIT_CHAR_GENDER_CANT_NO_ITEM();

		for (int ticketId : ticketIds) {
			if (player.getInventory().decreaseByItemId(ticketId, 1)) {
				errorMsg = null;
				break;
			}
		}
		if (errorMsg != null) {
			PacketSendUtility.sendPacket(player, errorMsg);
			return;
		}

		if (isGenderSwitch)
			player.getCommonData().setGender(genderId == 0 ? Gender.MALE : Gender.FEMALE);
		player.setPlayerAppearance(newAppearance);
		DAOManager.getDAO(PlayerAppearanceDAO.class).store(player); // save new appearance

		// broadcast new appearance (no need to save gender here, will be saved periodically and on logout)
		player.clearKnownlist();
		PacketSendUtility.sendPacket(player, new SM_PLAYER_INFO(player));
		player.updateKnownlist();
	}
}
