package com.aionemu.gameserver.network.aion.clientpackets;

import java.sql.Timestamp;
import java.util.List;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CREATE_CHARACTER;
import com.aionemu.gameserver.services.AccountService;
import com.aionemu.gameserver.services.NameRestrictionService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * In this packets aion client is requesting creation of character.
 * 
 * @author -Nemesiss-
 * @modified cura
 */
public class CM_CREATE_CHARACTER extends AionClientPacket {

	/** Character appearance */
	private PlayerAppearance playerAppearance;
	/** Player base data */
	private PlayerCommonData playerCommonData;
	private int type;
	private String characterName;

	/**
	 * Constructs new instance of <tt>CM_CREATE_CHARACTER</tt> packet
	 * 
	 * @param opcode
	 */
	public CM_CREATE_CHARACTER(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		readD(); // account id
		readS(); // account name

		characterName = Util.convertName(readS(52));

		if (characterName.equals("B")) {
			return;
		}

		playerCommonData = new PlayerCommonData(IDFactory.getInstance().nextId());
		playerCommonData.setName(characterName);
		playerCommonData.setGender(readD() == 0 ? Gender.MALE : Gender.FEMALE);
		playerCommonData.setRace(readD() == 0 ? Race.ELYOS : Race.ASMODIANS);
		playerCommonData.setPlayerClass(PlayerClass.getPlayerClassById((byte) readD()));
		playerCommonData.setLevel(1); // level (exp) must be set after class

		playerAppearance = new PlayerAppearance();

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
		type = readC();
	}

	/**
	 * Actually does the dirty job
	 */
	@Override
	protected void runImpl() {
		AionConnection client = getConnection();
		Account account = client.getAccount();

		if (characterName.equals("B")) {
			client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_OPEN_CREATION_WINDOW));
			return;
		}

		/* Some reasons why player can' be created */
		if (client.getActivePlayer() != null) {
			return;
		}
		if (type == 1) {
			client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_PERMISSION_TO_CREATE));
			return;
		}

		AccountService.removeDeletedCharacters(account);

		if (account.getMembership() >= MembershipConfig.CHARACTER_ADDITIONAL_ENABLE) {
			if (MembershipConfig.CHARACTER_ADDITIONAL_COUNT <= account.size()) {
				client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_SERVER_LIMIT_EXCEEDED));
				IDFactory.getInstance().releaseId(playerCommonData.getPlayerObjId());
				return;
			}
		} else if (GSConfig.CHARACTER_LIMIT_COUNT <= account.size()) {
			client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_SERVER_LIMIT_EXCEEDED));
			IDFactory.getInstance().releaseId(playerCommonData.getPlayerObjId());
			return;
		}
		if (!PlayerService.isFreeName(playerCommonData.getName())) {
			if (GSConfig.CHARACTER_CREATION_MODE == 2)
				client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_NAME_RESERVED));
			else
				client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_NAME_ALREADY_USED));
			IDFactory.getInstance().releaseId(playerCommonData.getPlayerObjId());
			return;
		}
		if (PlayerService.isOldName(playerCommonData.getName())) {
			client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_NAME_ALREADY_USED));
			IDFactory.getInstance().releaseId(playerCommonData.getPlayerObjId());
			return;
		}
		if (!NameRestrictionService.isValidName(playerCommonData.getName())) {
			client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_INVALID_NAME));
			IDFactory.getInstance().releaseId(playerCommonData.getPlayerObjId());
			return;
		}
		if (NameRestrictionService.isForbidden(playerCommonData.getName())) {
			client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_FORBIDDEN_CHAR_NAME));
			IDFactory.getInstance().releaseId(playerCommonData.getPlayerObjId());
			return;
		}
		if (!playerCommonData.getPlayerClass().isStartingClass()) {
			client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.FAILED_TO_CREATE_THE_CHARACTER));
			IDFactory.getInstance().releaseId(playerCommonData.getPlayerObjId());
			return;
		}
		if (GSConfig.CHARACTER_CREATION_MODE == 0) {
			for (PlayerAccountData data : account.getPlayerAccDataList()) {
				if (data.getPlayerCommonData().getRace() != playerCommonData.getRace()) {
					client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_OTHER_RACE));
					IDFactory.getInstance().releaseId(playerCommonData.getPlayerObjId());
					return;
				}
			}
		}
		Player player = PlayerService.newPlayer(playerCommonData, playerAppearance, account);

		if (!PlayerService.storeNewPlayer(player, account.getName(), account.getId())) {
			client.sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_DB_ERROR));
			IDFactory.getInstance().releaseId(playerCommonData.getPlayerObjId());
		} else {
			List<Item> equipment = DAOManager.getDAO(InventoryDAO.class).loadEquipment(player.getObjectId());
			PlayerAccountData accPlData = new PlayerAccountData(playerCommonData, null, playerAppearance, equipment, null);

			accPlData.setCreationDate(new Timestamp(System.currentTimeMillis()));
			PlayerService.storeCreationTime(player.getObjectId(), accPlData.getCreationDate());

			account.addPlayerAccountData(accPlData);
			client.sendPacket(new SM_CREATE_CHARACTER(accPlData, SM_CREATE_CHARACTER.RESPONSE_OK));
		}
	}
}
