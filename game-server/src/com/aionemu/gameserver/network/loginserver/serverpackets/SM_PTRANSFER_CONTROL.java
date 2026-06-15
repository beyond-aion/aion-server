package com.aionemu.gameserver.network.loginserver.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.*;
import com.aionemu.gameserver.model.gameobjects.player.emotion.Emotion;
import com.aionemu.gameserver.model.gameobjects.player.emotion.EmotionList;
import com.aionemu.gameserver.model.gameobjects.player.motion.Motion;
import com.aionemu.gameserver.model.gameobjects.player.motion.MotionList;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.NpcFaction;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.NpcFactions;
import com.aionemu.gameserver.model.gameobjects.player.title.Title;
import com.aionemu.gameserver.model.gameobjects.player.title.TitleList;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.skill.PlayerSkillList;
import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.services.transfers.TransferablePlayer;

/**
 * @author KID
 */
public class SM_PTRANSFER_CONTROL extends LsServerPacket {

	private final Logger log = LoggerFactory.getLogger(SM_PTRANSFER_CONTROL.class);
	public static final byte CHARACTER_INFORMATION = 1;
	public static final byte ITEMS_INFORMATION = 5;
	public static final byte DATA_INFORMATION = 6;
	public static final byte SKILL_INFORMATION = 7;
	public static final byte RECIPE_INFORMATION = 8;
	public static final byte QUEST_INFORMATION = 9;
	public static final byte ERROR = 2;
	public static final byte OK = 3;
	public static final byte TASK_STOP = 4;
	private byte type;
	private Player player;
	private String result;
	private int taskId;

	public SM_PTRANSFER_CONTROL(byte type, int taskId) {
		super(13);
		this.type = type;
		this.taskId = taskId;
	}

	public SM_PTRANSFER_CONTROL(byte type, TransferablePlayer tp) {
		super(13);
		this.type = type;
		this.taskId = tp.taskId;
		this.player = tp.player;
	}

	public SM_PTRANSFER_CONTROL(byte type, TransferablePlayer tp, String result) {
		super(13);
		this.type = type;
		this.result = result;
	}

	public SM_PTRANSFER_CONTROL(byte type, int taskId, String result) {
		super(13);
		this.type = type;
		this.taskId = taskId;
		this.result = result;
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeC(type);
		switch (type) {
			case OK:
				writeD(this.taskId);
				break;
			case ERROR:
				writeD(this.taskId);
				writeS(this.result);
				break;
			case TASK_STOP:
				writeD(this.taskId);
				writeS(this.result);
				break;
			case CHARACTER_INFORMATION:
				writeD(this.taskId);
				writeS(this.player.getName());
				writeD(this.player.getPlayerClass().getClassId());
				writeQ(this.player.getCommonData().getExp());
				writeD(this.player.getRace().getRaceId());
				writeD(this.player.getCommonData().getGender().getGenderId());
				writeD(this.player.getCommonData().getTitleId());
				writeD(this.player.getCommonData().getDp());
				writeD(this.player.getCommonData().getQuestExpands());
				writeD(this.player.getCommonData().getNpcExpands());
				writeD(this.player.getCommonData().getItemExpands());
				writeD(this.player.getCommonData().getWhNpcExpands());

				PlayerAppearance playerAppearance = this.player.getPlayerAppearance();
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
				writeC(playerAppearance.getNeck());
				writeC(playerAppearance.getNeckLength());
				writeC(playerAppearance.getShoulderSize());
				writeC(playerAppearance.getTorso());
				writeC(playerAppearance.getChest()); // only woman
				writeC(playerAppearance.getWaist());
				writeC(playerAppearance.getHips());
				writeC(playerAppearance.getArmThickness());
				writeC(playerAppearance.getHandSize());
				writeC(playerAppearance.getLegThickness());
				writeC(playerAppearance.getFootSize());
				writeC(playerAppearance.getFacialRate());
				writeC(playerAppearance.getArmLength());
				writeC(playerAppearance.getLegLength());
				writeC(playerAppearance.getShoulders());
				writeC(playerAppearance.getFaceShape());
				writeC(playerAppearance.getVoice());
				writeF(playerAppearance.getHeight());

				writeF(this.player.getX());
				writeF(this.player.getY());
				writeF(this.player.getZ());
				writeC(this.player.getHeading());
				writeD(this.player.getWorldId());
				break;
			case ITEMS_INFORMATION:
				writeD(this.taskId);
				// inventory
				List<Item> inv = InventoryDAO.loadItems(player.getObjectId(), StorageType.CUBE);
				inv.addAll(InventoryDAO.loadItems(player.getObjectId(), StorageType.REGULAR_WAREHOUSE));
				ItemStoneListDAO.load(inv);
				writeD(inv.size());
				for (Item item : inv) {
					writeD(item.getObjectId());
					writeD(item.getItemId());
					writeQ(item.getItemCount());
					writeD(item.getItemColor() == null ? -1 : item.getItemColor());

					writeS(item.getItemCreator());
					writeD(item.getExpireTime());
					writeD(item.getActivationCount());
					writeC(item.isEquipped() ? 1 : 0);

					writeC(item.isSoulBound() ? 1 : 0);
					writeQ(item.getEquipmentSlot());
					writeD(item.getItemLocation());
					writeD(item.getEnchantLevel());
					writeD(item.getEnchantBonus());

					writeD(item.getItemSkinTemplate().getTemplateId());
					writeD(item.getFusionedItemId());
					writeD(item.getOptionalSockets());
					writeD(item.getFusionedItemOptionalSockets());

					writeD(item.getChargePoints());
					Set<ManaStone> itemStones = item.getItemStones();
					writeC(itemStones.size());
					for (ManaStone stone : itemStones) {
						writeD(stone.getItemId());
						writeD(stone.getSlot());
					}
					itemStones = item.getFusionStones();
					writeC(itemStones.size());
					for (ManaStone stone : itemStones) {
						writeD(stone.getItemId());
						writeD(stone.getSlot());
					}
					writeD(item.getGodStoneId());
					writeD(item.getColorExpireTime());
					writeD(item.getTuneCount());
					writeD(item.getBonusStatsId());
					writeD(item.getFusionedItemBonusStatsId());
					writeD(item.getTempering());
					writeD(item.getPackCount());
					writeC(item.isAmplified() ? 1 : 0);
					writeH(item.getBuffSkill());
				}
				break;
			case DATA_INFORMATION:
				writeD(this.taskId);
				EmotionList emo = this.player.getEmotions();
				writeD(emo.getEmotions().size());
				for (Emotion e : emo.getEmotions()) {
					writeD(e.getId());
					writeD(e.secondsUntilExpiration());
				}

				MotionList motions = this.player.getMotions();
				writeD(motions.getMotions().size());
				for (Motion motion : motions.getMotions().values()) {
					writeD(motion.getId());
					writeD(motion.getExpireTime());
					writeC(motion.isActive() ? 1 : 0);
				}

				List<Macros.Macro> macros = player.getMacros().getAll();
				writeD(macros.size());
				for (Macros.Macro m : macros) {
					writeD(m.id());
					writeS(m.xml());
				}

				NpcFactions nf = this.player.getNpcFactions();
				writeD(nf.getNpcFactions().size());
				for (NpcFaction f : nf.getNpcFactions()) {
					writeD(f.getId());
					writeD(f.getTime());
					writeC(f.isActive() ? 1 : 0);
					writeS(f.getState().toString());
					writeD(f.getQuestId());
				}

				Collection<PetCommonData> pets = this.player.getPetList().getPets();
				writeD(pets.size());
				for (PetCommonData pet : pets) {
					writeD(pet.getTemplateId());
					writeD(pet.getDecoration());
					writeQ(pet.getBirthdayTimestamp() == null ? 0 : pet.getBirthdayTimestamp().getTime());
					writeS(pet.getName());
					writeD(pet.getExpireTime()); // 26-08-2013 kid
				}
				TitleList titles = this.player.getTitleList();
				writeD(titles.getTitles().size());
				for (Title t : titles.getTitles()) {
					writeD(t.getId());
					writeD(t.secondsUntilExpiration());
				}

				PlayerSettings ps = this.player.getPlayerSettings();
				writeD(ps.getUiSettings() == null ? 0 : ps.getUiSettings().length);
				writeD(ps.getShortcuts() == null ? 0 : ps.getShortcuts().length);
				if (ps.getUiSettings() != null) {
					writeB(ps.getUiSettings());
				}
				if (ps.getShortcuts() != null) {
					writeB(ps.getShortcuts());
				}
				writeD(ps.getDeny());
				writeD(ps.getDisplay());
				break;
			case SKILL_INFORMATION:
				writeD(this.taskId);
				PlayerSkillList skillList = this.player.getSkillList();

				// discard stigma skills
				List<PlayerSkillEntry> skills = new ArrayList<>();
				for (PlayerSkillEntry sk : skillList.getAllSkills()) {
					if (!sk.isStigmaSkill()) {
						skills.add(sk);
					}
				}

				writeD(skills.size());
				for (PlayerSkillEntry sk : skills) {
					writeD(sk.getSkillId());
					writeD(sk.getSkillLevel());
				}

				break;
			case RECIPE_INFORMATION:
				writeD(this.taskId);
				RecipeList rec = this.player.getRecipeList();
				writeD(rec.getRecipeList().size());
				for (int id : rec.getRecipeList()) {
					writeD(id);
				}
				break;
			case QUEST_INFORMATION:
				writeD(this.taskId);
				QuestStateList qsl = this.player.getQuestStateList();
				List<QuestState> quests = new ArrayList<>();
				for (QuestState qs : qsl.getAllQuestState()) {
					if (qs == null) {
						log.warn("there are null quest on player " + this.player.getName() + ". taskId #" + this.taskId + ". transfer skip that");
						continue;
					}
					quests.add(qs);
				}
				writeD(quests.size());
				for (QuestState qs : quests) {
					writeD(qs.getQuestId());
					writeS(qs.getStatus().toString());
					writeD(qs.getQuestVars().getQuestVars());
					writeD(qs.getCompleteCount());
					writeD(qs.getRewardGroup() == null ? -1 : qs.getRewardGroup());
					writeQ(qs.getLastCompleteTime().getTime());
					writeQ(qs.getNextRepeatTime().getTime());
					writeD(qs.getFlags());
				}
				break;
		}
	}
}
