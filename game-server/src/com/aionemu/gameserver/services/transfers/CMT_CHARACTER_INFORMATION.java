package com.aionemu.gameserver.services.transfers;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.network.packet.BaseClientPacket;
import com.aionemu.gameserver.configs.main.PlayerTransferConfig;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.PlayerBindPointDAO;
import com.aionemu.gameserver.dao.PlayerNpcFactionsDAO;
import com.aionemu.gameserver.dao.PlayerTitleListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.*;
import com.aionemu.gameserver.model.gameobjects.player.emotion.EmotionList;
import com.aionemu.gameserver.model.gameobjects.player.motion.Motion;
import com.aionemu.gameserver.model.gameobjects.player.motion.MotionList;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.ENpcFactionQuestState;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.NpcFaction;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.NpcFactions;
import com.aionemu.gameserver.model.gameobjects.player.title.Title;
import com.aionemu.gameserver.model.gameobjects.player.title.TitleList;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.skill.PlayerSkillList;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.AccountService;
import com.aionemu.gameserver.services.item.ItemSocketService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author KID
 */
public class CMT_CHARACTER_INFORMATION extends BaseClientPacket<AionConnection> {

	protected CMT_CHARACTER_INFORMATION(ByteBuffer byteBuffer) {
		super(byteBuffer, 0);
	}

	@Override
	public void run() {
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
	}

	public Player readInfo(String name, int targetAccount, String accountName, List<Integer> rsList, Logger textLog) {
		long st = System.currentTimeMillis();
		PlayerCommonData playerCommonData = new PlayerCommonData(IDFactory.getInstance().nextId());
		playerCommonData.setName(name);
		// read common data
		playerCommonData.setPlayerClass(PlayerClass.getPlayerClassById((byte) readD()));
		playerCommonData.setExp(readQ());
		playerCommonData.setRace(readD() == 0 ? Race.ELYOS : Race.ASMODIANS);
		playerCommonData.setGender(readD() == 0 ? Gender.MALE : Gender.FEMALE);
		playerCommonData.setTitleId(readD());
		playerCommonData.setDp(readD());
		playerCommonData.setQuestExpands(readD());
		playerCommonData.setNpcExpands(readD());
		playerCommonData.setItemExpands(readD());
		playerCommonData.setWhNpcExpands(readD());

		PlayerAppearance playerAppearance = new PlayerAppearance();
		playerAppearance.setSkinRGB(readD());
		playerAppearance.setHairRGB(readD());
		playerAppearance.setEyeRGB(readD());
		playerAppearance.setLipRGB(readD());
		playerAppearance.setFace(readUC());
		playerAppearance.setHair(readUC());
		playerAppearance.setDeco(readUC());
		playerAppearance.setTattoo(readUC());
		playerAppearance.setFaceContour(readUC());
		playerAppearance.setExpression(readUC());
		playerAppearance.setJawLine(readUC());
		playerAppearance.setForehead(readUC());
		playerAppearance.setEyeHeight(readUC());
		playerAppearance.setEyeSpace(readUC());
		playerAppearance.setEyeWidth(readUC());
		playerAppearance.setEyeSize(readUC());
		playerAppearance.setEyeShape(readUC());
		playerAppearance.setEyeAngle(readUC());
		playerAppearance.setBrowHeight(readUC());
		playerAppearance.setBrowAngle(readUC());
		playerAppearance.setBrowShape(readUC());
		playerAppearance.setNose(readUC());
		playerAppearance.setNoseBridge(readUC());
		playerAppearance.setNoseWidth(readUC());
		playerAppearance.setNoseTip(readUC());
		playerAppearance.setCheek(readUC());
		playerAppearance.setLipHeight(readUC());
		playerAppearance.setMouthSize(readUC());
		playerAppearance.setLipSize(readUC());
		playerAppearance.setSmile(readUC());
		playerAppearance.setLipShape(readUC());
		playerAppearance.setJawHeigh(readUC());
		playerAppearance.setChinJut(readUC());
		playerAppearance.setEarShape(readUC());
		playerAppearance.setHeadSize(readUC());
		playerAppearance.setNeck(readUC());
		playerAppearance.setNeckLength(readUC());
		playerAppearance.setShoulderSize(readUC());
		playerAppearance.setTorso(readUC());
		playerAppearance.setChest(readUC());
		playerAppearance.setWaist(readUC());
		playerAppearance.setHips(readUC());
		playerAppearance.setArmThickness(readUC());
		playerAppearance.setHandSize(readUC());
		playerAppearance.setLegThickness(readUC());
		playerAppearance.setFootSize(readUC());
		playerAppearance.setFacialRate(readUC());
		playerAppearance.setArmLength(readUC());
		playerAppearance.setLegLength(readUC());
		playerAppearance.setShoulders(readUC());
		playerAppearance.setFaceShape(readUC());
		playerAppearance.setVoice(readUC());
		playerAppearance.setHeight(readF());

		PlayerAccountData accPlData = new PlayerAccountData(playerCommonData, playerAppearance);
		Account account = AccountService.loadAccount(targetAccount);
		account.setName(accountName);
		Player player = PlayerService.newPlayer(accPlData, account);
		float x = readF();
		float y = readF();
		float z = readF();
		byte h = readC();
		int worldId = readD();
		WorldPosition pos = World.getInstance().createPosition(worldId, x, y, z, h, 1);
		player.setPosition(pos);

		if (!PlayerService.storeNewPlayer(player, accountName, targetAccount)) {
			textLog.info("failed to store new player to " + accountName);
			IDFactory.getInstance().releaseId(playerCommonData.getPlayerObjId());
			return null;
		}
		// read items data
		int cnt = readD();
		StringBuilder sb = new StringBuilder();
		for (int a = 0; a < cnt; a++) { // inventory
			int objIdOld = readD();
			int itemId = readD();
			long itemCnt = readQ();
			Integer itemColor = readD();
			if (itemColor == -1)
				itemColor = null;

			String itemCreator = readS();
			int itemExpireTime = readD();
			int itemActivationCnt = readD();
			boolean itemEquipped = readC() == 1;

			boolean itemSoulBound = readC() == 1;
			long equipSlot = readQ();
			int location = readD();
			int enchant = readD();
			int enchantBonus = readD();

			int skinId = readD();
			int fusionId = readD();
			int optSocket = readD();
			int optFusion = readD();

			int charge = readD();
			List<int[]> manastones = new ArrayList<>(), fusions = new ArrayList<>();
			byte len = readC();
			for (byte b = 0; b < len; b++) {
				manastones.add(new int[] { readD(), readD() });
			}
			len = readC();
			for (byte b = 0; b < len; b++) {
				fusions.add(new int[] { readD(), readD() });
			}
			int godstone = readD();
			int colorExpires = readD();
			int tuneCount = readD();
			int bonusStatsId = readD();
			int fusionedItemBonusStatsId = readD();
			int tempering = readD();
			int packCount = readD();
			boolean itemAmplified = readUC() == 1;
			int buffSkill = readUH();
			if (!(location == StorageType.CUBE.getId() && PlayerTransferConfig.ALLOW_INV
				|| location == StorageType.REGULAR_WAREHOUSE.getId() && PlayerTransferConfig.ALLOW_WAREHOUSE)) {
				continue;
			}
			ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(itemId);
			if (template == null) {
				textLog.warn("(accId=" + targetAccount + ") item with id " + itemId + " was not found in templates");
				continue;
			}

			if (template.isStigma() && !PlayerTransferConfig.ALLOW_STIGMA) {
				continue;
			}

			int newId = IDFactory.getInstance().nextId();
			// bonus probably is lost, don't know [RR]
			// dye expiration is lost
			// plume Bonus is lost
			Item item = new Item(newId, itemId, itemCnt, itemColor, colorExpires, itemCreator, itemExpireTime, itemActivationCnt, itemEquipped,
				itemSoulBound, equipSlot, location, enchant, enchantBonus, skinId, fusionId, optSocket, optFusion, charge, tuneCount, bonusStatsId,
				fusionedItemBonusStatsId, tempering, packCount, itemAmplified, buffSkill, 0);
			if (manastones.size() > 0)
				for (int[] stone : manastones)
					ItemSocketService.addManaStone(item, stone[0], stone[1], false);

			if (fusions.size() > 0)
				for (int[] stone : fusions)
					ItemSocketService.addManaStone(item, stone[0], stone[1], true);

			if (godstone != 0)
				item.addGodStone(godstone);

			sb.append("\n(old objId=").append(objIdOld).append(") -> ").append(item);
			item.setPersistentState(PersistentState.NEW);
			player.getInventory().add_CharacterTransfer(item);
		}
		InventoryDAO.store(player);

		textLog.info(sb.toString());

		// read data
		cnt = readD();
		textLog.info("EmotionList:" + cnt);
		player.setEmotions(new EmotionList(player));
		for (int a = 0; a < cnt; a++) { // emotes
			int id = readD(), remainTime = readD();

			if (PlayerTransferConfig.ALLOW_EMOTIONS)
				player.getEmotions().add(id, remainTime, true);
		}

		cnt = readD();
		textLog.info("MotionList:" + cnt);
		player.setMotions(new MotionList(player));
		for (int i = 0; i < cnt; i++) { // motions
			int id = readD(), expiryTime = readD();
			boolean active = readC() == 1;

			if (PlayerTransferConfig.ALLOW_MOTIONS)
				player.getMotions().add(new Motion(id, expiryTime, active), true);
		}

		cnt = readD();
		textLog.info("MacroList:" + cnt);
		player.setMacroList(new MacroList());
		for (int a = 0; a < cnt; a++) { // macros
			int id = readD();
			String xml = readS();

			if (PlayerTransferConfig.ALLOW_MACRO)
				PlayerService.addMacro(player, id, xml);
		}

		cnt = readD();
		textLog.info("NpcFactions:" + cnt);
		player.setNpcFactions(new NpcFactions(player));
		for (int a = 0; a < cnt; a++) { // npc factions
			int id = readD(), time = readD();
			boolean active = readC() == 1;
			String state = readS();
			int questId = readD();

			if (PlayerTransferConfig.ALLOW_NPCFACTIONS)
				player.getNpcFactions().addNpcFaction(new NpcFaction(id, time, active, ENpcFactionQuestState.valueOf(state), questId));
		}
		if (cnt > 0 && PlayerTransferConfig.ALLOW_NPCFACTIONS)
			PlayerNpcFactionsDAO.storeNpcFactions(player);

		cnt = readD();
		textLog.info("Pets:" + cnt);
		for (int i = 0; i < cnt; i++) { // pets
			int petId = readD();
			int decorationId = readD();
			long bday = readQ();
			String petname = readS();
			int expiryTime = readD();

			if (PlayerTransferConfig.ALLOW_PETS) {
				if (bday == 0)
					bday = System.currentTimeMillis();

				player.getPetList().addPet(player, petId, decorationId, bday, petname, expiryTime);
			}
		}

		cnt = readD();
		textLog.info("TitleList:" + cnt);
		player.setTitleList(new TitleList());
		for (int a = 0; a < cnt; a++) { // titles
			int id = readD(), remainTime = readD();

			if (PlayerTransferConfig.ALLOW_TITLES)
				player.getTitleList().addEntry(id, remainTime);
		}
		if (cnt > 0 && PlayerTransferConfig.ALLOW_TITLES)
			for (Title t : player.getTitleList().getTitles()) {
				PlayerTitleListDAO.storeTitles(player, t);
			}

		String[] posBind;
		switch (player.getRace()) {
			case ELYOS:
				posBind = PlayerTransferConfig.BIND_ELYOS.split(" ");
				break;
			default:
				posBind = PlayerTransferConfig.BIND_ASMO.split(" ");
				break;
		}

		player.setBindPoint(new BindPointPosition(Integer.parseInt(posBind[0]), Float.parseFloat(posBind[1]), Float.parseFloat(posBind[2]),
			Float.parseFloat(posBind[3]), Byte.parseByte(posBind[4])));
		PlayerBindPointDAO.store(player);

		int uilen = readD(), shortlen = readD();
		byte[] ui = readB(uilen), sc = readB(shortlen);
		int deny = readD(), penalty = readD();
		player.setPlayerSettings(new PlayerSettings(uilen > 0 ? ui : null, shortlen > 0 ? sc : null, null, deny, penalty));
		player.setAbyssRank(new AbyssRank(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0));

		// read skill data
		cnt = readD();
		textLog.info("PlayerSkillList:" + cnt);
		player.setSkillList(new PlayerSkillList());
		boolean rsCheck = rsList.size() > 0;
		for (int a = 0; a < cnt; a++) { // skills
			int skillId = readD();
			int skillLvl = readD();

			if (rsCheck && rsList.contains(skillId))
				continue;

			SkillTemplate temp = DataManager.SKILL_DATA.getSkillTemplate(skillId);
			if (temp == null) {
				textLog.error(String.format("null skillid:%d name:%s", skillId, name));
				continue;
			}

			if (!PlayerTransferConfig.ALLOW_SKILLS) {
				if (temp.isPassive())
					player.getSkillList().addSkill(player, skillId, skillLvl);
			} else
				player.getSkillList().addSkill(player, skillId, skillLvl);
		}

		// read recipe data
		cnt = readD();
		textLog.info("RecipeList:" + cnt);
		player.setRecipeList(new RecipeList());
		for (int a = 0; a < cnt; a++) { // recipes
			int recipeId = readD();

			if (PlayerTransferConfig.ALLOW_RECIPES)
				player.getRecipeList().addRecipe(player, recipeId);
		}

		// read quest data
		cnt = readD();
		textLog.info("QuestStateList:" + cnt);
		player.setQuestStateList(new QuestStateList());
		for (int a = 0; a < cnt; a++) { // quests
			int questId = readD();
			String status = readS();
			int qvars = readD(), completeCount = readD(), reward = readD();
			Timestamp completeTime = new Timestamp(readQ());
			Timestamp nextRepeatTime = new Timestamp(readQ());
			int flags = readD();

			if (PlayerTransferConfig.ALLOW_QUESTS) {
				player.getQuestStateList().addQuest(questId, new QuestState(questId, QuestStatus.valueOf(status), qvars, flags, completeCount, nextRepeatTime,
					reward == -1 ? null : reward, completeTime));
			}
		}

		PlayerService.storePlayer(player);
		textLog.info("finished in " + (System.currentTimeMillis() - st) + " ms");
		return player;
	}
}
