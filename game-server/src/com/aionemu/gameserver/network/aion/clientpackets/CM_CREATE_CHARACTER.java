package com.aionemu.gameserver.network.aion.clientpackets;

import java.sql.Timestamp;
import java.util.Set;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CREATE_CHARACTER;
import com.aionemu.gameserver.services.AccountService;
import com.aionemu.gameserver.services.NameRestrictionService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * In this packets the Aion client is requesting creation of a character or to enter the character creation menu.
 * 
 * @author -Nemesiss-, cura, Neon
 */
public class CM_CREATE_CHARACTER extends AbstractCharacterEditPacket {

	private int type;

	public CM_CREATE_CHARACTER(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		readD(); // account id
		readS(); // account name
		readBasicInfo(true);
		readAppearance();
		type = readUC();
	}

	@Override
	protected void runImpl() {
		Account account = getConnection().getAccount();

		if (type == 1) { // flag to enter char creation screen
			sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_OPEN_CREATION_WINDOW));
			return;
		}

		AccountService.removeDeletedCharacters(account);
		int responseCode = validateBasicInfo(account);
		if (responseCode != SM_CREATE_CHARACTER.RESPONSE_OK) {
			sendPacket(new SM_CREATE_CHARACTER(null, responseCode));
			return;
		}

		PlayerCommonData playerCommonData = new PlayerCommonData(IDFactory.getInstance().nextId());
		playerCommonData.setName(characterName);
		playerCommonData.setGender(gender);
		playerCommonData.setRace(race);
		playerCommonData.setPlayerClass(playerClass);
		playerCommonData.setLevel(1); // level (exp) must be set after class
		PlayerAccountData accPlData = new PlayerAccountData(playerCommonData, playerAppearance);
		Player player = PlayerService.newPlayer(accPlData, account);

		if (!PlayerService.storeNewPlayer(player, account.getName(), account.getId())) {
			sendPacket(new SM_CREATE_CHARACTER(null, SM_CREATE_CHARACTER.RESPONSE_DB_ERROR));
			IDFactory.getInstance().releaseId(playerCommonData.getPlayerObjId());
		} else {
			accPlData.setVisibleItems(player.getEquipment().getEquippedForAppearance());
			accPlData.setCreationDate(new Timestamp(System.currentTimeMillis()));
			PlayerService.storeCreationTime(player.getObjectId(), accPlData.getCreationDate());

			account.addPlayerAccountData(accPlData);
			sendPacket(new SM_CREATE_CHARACTER(accPlData, SM_CREATE_CHARACTER.RESPONSE_OK));
		}
	}

	private int validateBasicInfo(Account account) {
		int maxCharCount = account.getMembership() >= MembershipConfig.CHARACTER_ADDITIONAL_ENABLE ? MembershipConfig.CHARACTER_ADDITIONAL_COUNT
			: GSConfig.CHARACTER_LIMIT_COUNT;
		if (account.size() > maxCharCount)
			return SM_CREATE_CHARACTER.RESPONSE_SERVER_LIMIT_EXCEEDED;
		if (playerClass == null) // should never happen (only with type == 1 to enter char creation screen, where we won't reach this validation)
			return SM_CREATE_CHARACTER.FAILED_TO_CREATE_THE_CHARACTER;
		if (PlayerService.isNameUsedOrReserved(null, characterName))
			return GSConfig.CHARACTER_CREATION_MODE == 2 ? SM_CREATE_CHARACTER.RESPONSE_NAME_RESERVED : SM_CREATE_CHARACTER.RESPONSE_NAME_ALREADY_USED;
		if (!NameRestrictionService.isValidName(characterName))
			return SM_CREATE_CHARACTER.RESPONSE_INVALID_NAME;
		if (NameRestrictionService.isForbidden(characterName))
			return SM_CREATE_CHARACTER.RESPONSE_FORBIDDEN_CHAR_NAME;
		if (!playerClass.isStartingClass())
			return SM_CREATE_CHARACTER.RESPONSE_FORBIDDEN_CLASS;
		if (GSConfig.CHARACTER_CREATION_MODE == 0 && account.getPlayerAccDataList().stream().anyMatch(p -> p.getPlayerCommonData().getRace() != race)) {
			return SM_CREATE_CHARACTER.RESPONSE_OTHER_RACE;
		}
		return SM_CREATE_CHARACTER.RESPONSE_OK;
	}
}
