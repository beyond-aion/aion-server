package com.aionemu.gameserver.model.gameobjects.player;

import java.sql.Timestamp;

import com.aionemu.gameserver.dao.PlayerQuestListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PlayerExperienceTable;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.CreatureTemplate;
import com.aionemu.gameserver.model.stats.container.PlayerGameStats;
import com.aionemu.gameserver.model.templates.BoundRadius;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DP_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATUPDATE_DP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATUPDATE_EXP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.XPLossEnum;
import com.aionemu.gameserver.world.World;

/**
 * This class is holding base information about player, that may be used even when player itself is not online.
 * 
 * @author Luno, cura
 */
public class PlayerCommonData extends CreatureTemplate {

	private final int playerObjId;
	private Race race;
	private String name;
	private PlayerClass playerClass;
	/** Should be changed right after character creation **/
	private int level = 0;
	private long exp = 0;
	private long expRecoverable = 0;
	private Gender gender;
	private Timestamp lastOnline;
	private boolean online;
	private String note;
	private int mapId;
	private float x, y, z;
	private byte heading;
	private int questExpands = 0;
	private int npcExpands = 0;
	private int itemExpands = 0;
	private int warehouseNpcExpands = 0;
	private int warehouseBonusExpands = 0;
	private int titleId = -1;
	private int bonusTitleId = -1;
	private int dp = 0;
	private int mailboxLetters;
	private int soulSickness = 0;
	private boolean noExp = false;
	private long reposeCurrent;
	private long reposeMax;
	private long salvationPoint;
	private int mentorFlagTime;
	private int worldOwnerId;
	private boolean isDaeva;
	private boolean isInEditMode;

	private BoundRadius boundRadius;

	private long lastTransferTime;

	// TODO: Move all function to playerService or Player class.
	public PlayerCommonData(int objId) {
		this.playerObjId = objId;
	}

	public int getPlayerObjId() {
		return playerObjId;
	}

	public long getExp() {
		return this.exp;
	}

	public int getQuestExpands() {
		return this.questExpands;
	}

	public void setQuestExpands(int questExpands) {
		this.questExpands = questExpands;
	}

	public void setNpcExpands(int npcExpands) {
		this.npcExpands = npcExpands;
	}

	public int getNpcExpands() {
		return npcExpands;
	}

	public int getItemExpands() {
		return this.itemExpands;
	}

	public void setItemExpands(int itemExpands) {
		this.itemExpands = itemExpands;
	}

	public long getExpShown() {
		return this.exp - DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(getLevel());
	}

	public long getExpNeed() {
		if (getLevel() == DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel()) {
			return 0;
		}
		return DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(getLevel() + 1)
			- DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(getLevel());
	}

	/**
	 * calculate the lost experience must be called before setexp
	 * 
	 * @author Jangan
	 */
	public void calculateExpLoss() {
		long expLost = XPLossEnum.getExpLoss(getLevel(), this.getExpNeed());

		int unrecoverable = (int) (expLost * 0.33333333);
		int recoverable = (int) expLost - unrecoverable;
		long allExpLost = recoverable + this.expRecoverable;

		if (this.getExpShown() > unrecoverable) {
			this.exp = this.exp - unrecoverable;
		} else {
			this.exp = this.exp - this.getExpShown();
		}
		if (this.getExpShown() > recoverable) {
			this.expRecoverable = allExpLost;
			this.exp = this.exp - recoverable;
		} else {
			this.expRecoverable = this.expRecoverable + this.getExpShown();
			this.exp = this.exp - this.getExpShown();
		}
		if (this.expRecoverable > getExpNeed() * 0.25) {
			this.expRecoverable = Math.round(getExpNeed() * 0.25);
		}
		if (this.getPlayer() != null)
			PacketSendUtility.sendPacket(getPlayer(),
				new SM_STATUPDATE_EXP(getExpShown(), getExpRecoverable(), getExpNeed(), this.getCurrentReposeEnergy(), this.getMaxReposeEnergy()));
	}

	public void setRecoverableExp(long expRecoverable) {
		this.expRecoverable = expRecoverable;
	}

	public void resetRecoverableExp() {
		long el = this.expRecoverable;
		this.expRecoverable = 0;
		this.setExp(this.exp + el);
	}

	public long getExpRecoverable() {
		return this.expRecoverable;
	}

	public void addExp(long value, Rates rates) {
		addExp(value, rates, null);
	}

	public void addExp(long value, Rates rates, String name) {
		if (noExp)
			return;

		long reward = value;
		long repose = 0;
		long salvation = 0;
		Player player = getPlayer();
		if (player != null && player.getWorldId() == 301200000) // nightmare circus
			return;

		if (player != null)
			reward = rates.calcResult(player, value);

		if (reward > 0) {
			if (getCurrentReposeEnergy() > 0) {
				long allowedExp = Math.min(getCurrentReposeEnergy(), reward);
				addReposeEnergy(-allowedExp);
				repose = (long) ((allowedExp / 100f) * 40); // 40% bonus for the amount of used repose energy
			}

			if (isReadyForSalvationPoints() && getCurrentSalvationPercent() > 0) {
				salvation = (long) ((reward / 100f) * getCurrentSalvationPercent());
				// TODO! remove salvation points?
			}

			reward += repose + salvation;
		}

		setExp(exp + reward);
		if (player != null) {
			if (repose > 0 && salvation > 0) {
				if (name != null) // You have gained %num1 XP from %0 (Energy of Repose %num2, Energy of Salvation %num3).
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GET_EXP_VITAL_MAKEUP_BONUS(name, reward, repose, salvation));
				else // You have gained %num1 XP(Energy of Repose %num2, Energy of Salvation %num3).
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GET_EXP2_VITAL_MAKEUP_BONUS(reward, repose, salvation));
			} else if (repose > 0 && salvation == 0) {
				if (name != null) // You have gained %num1 XP from %0 (Energy of Repose %num2).
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GET_EXP_VITAL_BONUS(name, reward, repose));
				else // You have gained %num1 XP(Energy of Repose %num2).
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GET_EXP2_VITAL_BONUS(reward, repose));
			} else if (repose == 0 && salvation > 0) {
				if (name != null) // You have gained %num1 XP from %0 (Energy of Salvation %num2).
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GET_EXP_MAKEUP_BONUS(name, reward, salvation));
				else // You have gained %num1 XP (Energy of Salvation %num2).
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GET_EXP2_MAKEUP_BONUS(reward, salvation));
			} else {
				if (name != null) // You have gained %num1 XP from %0.
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GET_EXP(name, reward));
				else // You have gained %num1 XP.
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GET_EXP2(reward));
			}
			if (getLevel() == 9 && exp >= DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(10))
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_LEVEL_LIMIT_QUEST_NOT_FINISHED1());
		}
	}

	public boolean isInEditMode() {
		return isInEditMode;
	}

	public void setInEditMode(boolean isInEditMode) {
		this.isInEditMode = isInEditMode;
	}

	public boolean isReadyForSalvationPoints() {
		return getLevel() >= 15;
	}

	public boolean isReadyForReposeEnergy() {
		return getLevel() >= 10;
	}

	public void addReposeEnergy(long add) {
		reposeCurrent += add;
		if (reposeCurrent < 0)
			reposeCurrent = 0;
		else if (reposeCurrent > getMaxReposeEnergy())
			reposeCurrent = getMaxReposeEnergy();
	}

	public void updateMaxRepose() {
		if (!isReadyForReposeEnergy()) {
			reposeCurrent = 0;
			reposeMax = 0;
		} else {
			reposeMax = (long) (getExpNeed() * 0.25f); // Retail 99%
			reposeCurrent = Math.min(reposeMax, reposeCurrent);
		}
	}

	public void setCurrentReposeEnergy(long value) {
		reposeCurrent = value;
	}

	public long getCurrentReposeEnergy() {
		return reposeCurrent;
	}

	public long getMaxReposeEnergy() {
		return reposeMax;
	}

	/**
	 * sets the exp and level value
	 */
	public void setExp(long exp) {
		if (exp != this.exp || level == 0 && exp == 0) {
			PlayerExperienceTable pxt = DataManager.PLAYER_EXPERIENCE_TABLE;
			int maxLevel = isDaeva || !online && (updateDaeva() || exp > pxt.getStartExpForLevel(10)) ? pxt.getMaxLevel() : 10;
			int oldLevel = level;

			this.exp = Math.min(exp, pxt.getStartExpForLevel(maxLevel));
			// maxLevel is 66 (10 for non daeva) but 65 (9 for non daeva) should be shown with full XP bar
			level = Math.min(pxt.getLevelForExp(this.exp), maxLevel - 1);

			Player player = getPlayer();
			if (player != null) {
				player.getController().onLevelChange(oldLevel, level);
				PacketSendUtility.sendPacket(player,
					new SM_STATUPDATE_EXP(getExpShown(), getExpRecoverable(), getExpNeed(), getCurrentReposeEnergy(), getMaxReposeEnergy()));
			}
		}
	}

	public void setNoExp(boolean value) {
		this.noExp = value;
	}

	public boolean getNoExp() {
		return noExp;
	}

	public final Race getRace() {
		return race;
	}

	public int getMentorFlagTime() {
		return mentorFlagTime;
	}

	public boolean isHaveMentorFlag() {
		return mentorFlagTime > System.currentTimeMillis() / 1000;
	}

	public void setMentorFlagTime(int mentorFlagTime) {
		this.mentorFlagTime = mentorFlagTime;
	}

	public void setRace(Race race) {
		this.race = race;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PlayerClass getPlayerClass() {
		return playerClass;
	}

	public void setPlayerClass(PlayerClass playerClass) {
		this.playerClass = playerClass;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public byte getHeading() {
		return heading;
	}

	public void setHeading(byte heading) {
		this.heading = heading;
	}

	/**
	 * @return Timestamp the player was last online. May be null
	 */
	public Timestamp getLastOnline() {
		return lastOnline;
	}

	/**
	 * @return Unix timestamp the player was last online (measured in seconds since 1970-01-01T00:00:00Z). 0 if he was never online before.
	 */
	public int getLastOnlineEpochSeconds() {
		return lastOnline == null ? 0 : (int) (lastOnline.getTime() / 1000);
	}

	public void setLastOnline(Timestamp timestamp) {
		lastOnline = timestamp;
	}

	public int getLevel() {
		return level;
	}

	/**
	 * This will only set the specified level >= 10 if the player is a daeva.
	 */
	public void setLevel(int level) {
		setExp(DataManager.PLAYER_EXPERIENCE_TABLE.getStartExpForLevel(level));
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public int getTitleId() {
		return titleId;
	}

	public void setTitleId(int titleId) {
		this.titleId = titleId;
	}

	public int getBonusTitleId() {
		return bonusTitleId;
	}

	public void setBonusTitleId(int bonusTitleId) {
		this.bonusTitleId = bonusTitleId;
	}

	/**
	 * Gets the corresponding Player for this common data. Returns null if the player is not online
	 * 
	 * @return Player or null
	 */
	public Player getPlayer() {
		return online ? World.getInstance().getPlayer(playerObjId) : null;
	}

	public void addDp(int dp) {
		setDp(this.dp + dp);
	}

	/**
	 * //TODO move to lifestats -> db save?<br>
	 * => {@link PlayerGameStats#onStatsChange()}
	 */
	public void setDp(int dp) {
		if (playerClass.isStartingClass())
			return;

		int maxDp = (getPlayer() == null) ? -1 : getPlayer().getGameStats().getMaxDp().getCurrent();
		this.dp = (maxDp >= 0 && dp > maxDp) ? maxDp : dp;

		if (getPlayer() != null) {
			PacketSendUtility.broadcastPacket(getPlayer(), new SM_DP_INFO(playerObjId, this.dp), true);
			getPlayer().getGameStats().updateStatsAndSpeedVisually();
			PacketSendUtility.sendPacket(getPlayer(), new SM_STATUPDATE_DP(this.dp));
		}
	}

	public int getDp() {
		return this.dp;
	}

	@Override
	public int getTemplateId() {
		return 100000 + race.getRaceId() * 2 + gender.getGenderId();
	}

	@Override
	public int getL10nId() {
		return 0;
	}

	public void setWhNpcExpands(int value) {
		this.warehouseNpcExpands = value;
	}

	public int getWhNpcExpands() {
		return warehouseNpcExpands;
	}

	public int getWhBonusExpands() {
		return warehouseBonusExpands;
	}

	public void setWhBonusExpands(int value) {
		this.warehouseBonusExpands = value;
	}

	public void setMailboxLetters(int count) {
		this.mailboxLetters = count;
	}

	public int getMailboxLetters() {
		return mailboxLetters;
	}

	public void setBoundingRadius(BoundRadius boundRadius) {
		this.boundRadius = boundRadius;
	}

	@Override
	public BoundRadius getBoundRadius() {
		return boundRadius;
	}

	public void setDeathCount(int count) {
		this.soulSickness = count;
	}

	public int getDeathCount() {
		return this.soulSickness;
	}

	/**
	 * Value returned here means % of exp bonus.
	 */
	public byte getCurrentSalvationPercent() {
		if (salvationPoint <= 0)
			return 0;

		long per = salvationPoint / 1000;
		if (per > 30)
			return 30;

		return (byte) per;
	}

	public void addSalvationPoints(long points) {
		salvationPoint += points;
	}

	public void setCurrentSalvationPoints(long points) {
		salvationPoint = points;
	}

	public void resetSalvationPoints() {
		salvationPoint = 0;
	}

	public void setLastTransferTime(long value) {
		this.lastTransferTime = value;
	}

	public long getLastTransferTime() {
		return this.lastTransferTime;
	}

	public int getWorldOwnerId() {
		return worldOwnerId;
	}

	public void setWorldOwnerId(int worldOwnerId) {
		this.worldOwnerId = worldOwnerId;
	}

	/**
	 * @return True, if the player has a main class and completed the ascension quest (gets updated on login and quest completion).
	 */
	public boolean isDaeva() {
		return isDaeva;
	}

	public void setDaeva(boolean isDaeva) {
		this.isDaeva = isDaeva;
	}

	/**
	 * @return True, if player was promoted to daeva. False if he already has daeva status or wasn't promoted.
	 */
	public boolean updateDaeva() {
		if (isDaeva)
			return false;

		if (playerClass.isStartingClass())
			return false;

		QuestStateList qsl;
		Player player = getPlayer();
		if (player != null)
			qsl = player.getQuestStateList();
		else
			qsl = PlayerQuestListDAO.load(playerObjId);

		// check both quest states in case a player changed race
		QuestStatus elyAscentQuestStatus = qsl.getQuestState(1006) != null ? qsl.getQuestState(1006).getStatus() : null;
		QuestStatus asmoAscentQuestStatus = qsl.getQuestState(2008) != null ? qsl.getQuestState(2008).getStatus() : null;
		if (elyAscentQuestStatus != QuestStatus.COMPLETE && asmoAscentQuestStatus != QuestStatus.COMPLETE)
			return false;

		setDaeva(true);
		return true;
	}
}
