package com.aionemu.gameserver.model.gameobjects.player;

import java.sql.Timestamp;
import java.util.concurrent.Future;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Expirable;
import com.aionemu.gameserver.model.templates.pet.PetDopingBag;
import com.aionemu.gameserver.model.templates.pet.PetFunctionType;
import com.aionemu.gameserver.model.templates.pet.PetTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.toypet.PetAdoptionService;
import com.aionemu.gameserver.services.toypet.PetFeedProgress;
import com.aionemu.gameserver.services.toypet.PetHungryLevel;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class PetCommonData implements Expirable {

	private final int objectId;
	private final int templateId;
	private final int masterObjectId;
	private int decoration;
	private String name;
	private Timestamp birthday;
	PetFeedProgress feedProgress = null;
	PetDopingBag dopingBag = null;
	private volatile boolean cancelFeed = false;
	private boolean feedingTime = true;
	private long refeedTime;
	private long startMoodTime;
	private int shuggleCounter;
	private int lastSentPoints;
	private long moodCdStarted;
	private long giftCdStarted;
	private int expireTime;
	private Timestamp despawnTime;
	private boolean isLooting = false;
	private boolean isSelling = false;
	private volatile Future<?> refeedTask;

	public PetCommonData(int objectId, int templateId, int masterObjectId, int expireTime) {
		this.objectId = objectId;
		this.templateId = templateId;
		this.masterObjectId = masterObjectId;
		this.expireTime = expireTime;
		PetTemplate template = DataManager.PET_DATA.getPetTemplate(templateId);
		if (template.containsFunction(PetFunctionType.FOOD)) {
			int flavourId = template.getPetFunction(PetFunctionType.FOOD).getId();
			int lovedLimit = DataManager.PET_FEED_DATA.getFlavourById(flavourId).getLovedFoodLimit();
			feedProgress = new PetFeedProgress((byte) (lovedLimit & 0xFF));
		}
		if (template.containsFunction(PetFunctionType.DOPING)) {
			dopingBag = new PetDopingBag();
		}
	}

	public int getObjectId() {
		return objectId;
	}

	public int getMasterObjectId() {
		return masterObjectId;
	}

	public int getDecoration() {
		return decoration;
	}

	public void setDecoration(int decoration) {
		this.decoration = decoration;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTemplateId() {
		return templateId;
	}

	public int getBirthday() {
		if (birthday == null)
			return 0;

		return (int) (birthday.getTime() / 1000);
	}

	public Timestamp getBirthdayTimestamp() {
		return birthday;
	}

	public void setBirthday(Timestamp birthday) {
		this.birthday = birthday;
	}

	public long getRefeedTime() {
		return refeedTime;
	}

	public void setRefeedTime(long curentTime) {
		this.refeedTime = curentTime;
	}

	public void setIsFeedingTime(boolean food) {
		this.feedingTime = food;
	}

	public boolean isFeedingTime() {
		return feedingTime;
	}

	public boolean getCancelFeed() {
		return cancelFeed;
	}

	public void setCancelFeed(boolean cancelFeed) {
		this.cancelFeed = cancelFeed;
	}

	public void scheduleRefeed(long reFoodTime) {
		setIsFeedingTime(false);
		cancelRefeedTask();
		refeedTask = ThreadPoolManager.getInstance().schedule(() -> {
			feedingTime = true;
			refeedTime = 0;
			feedProgress.setHungryLevel(PetHungryLevel.HUNGRY);
		}, reFoodTime);
	}

	public void cancelRefeedTask() {
		if (refeedTask != null)
			refeedTask.cancel(false);
	}

	public long getRefeedDelay() {
		long time = refeedTime - System.currentTimeMillis();
		if (time < 0) {
			refeedTime = 0;
			time = 0;
		}

		return time;
	}

	public final long getMoodStartTime() {
		return startMoodTime;
	}

	public final int getShuggleCounter() {
		return shuggleCounter;
	}

	public final void setShuggleCounter(int shuggleCounter) {
		this.shuggleCounter = shuggleCounter;
	}

	public final int getMoodPoints(boolean forPacket) {
		if (startMoodTime == 0)
			startMoodTime = System.currentTimeMillis();
		int points = Math.round((System.currentTimeMillis() - startMoodTime) / 1000f) + shuggleCounter * 1000;
		if (forPacket && points > 9000)
			return 9000;
		return points;
	}

	public final int getLastSentPoints() {
		return lastSentPoints;
	}

	public final void setLastSentPoints(int points) {
		lastSentPoints = points;
	}

	public final boolean increaseShuggleCounter() {
		if (getMoodRemainingTime() > 0)
			return false;
		this.moodCdStarted = System.currentTimeMillis();
		this.shuggleCounter++;
		return true;
	}

	public final void clearMoodStatistics() {
		this.startMoodTime = 0;
		this.shuggleCounter = 0;
	}

	public final void setStartMoodTime(long startMoodTime) {
		this.startMoodTime = startMoodTime;
	}

	/**
	 * @return moodCdStarted
	 */
	public long getMoodCdStarted() {
		return moodCdStarted;
	}

	/**
	 * @param moodCdStarted
	 *          the moodCdStarted to set
	 */
	public void setMoodCdStarted(long moodCdStarted) {
		this.moodCdStarted = moodCdStarted;
	}

	public int getMoodRemainingTime() {
		long stop = moodCdStarted + 600000;
		long remains = stop - System.currentTimeMillis();
		if (remains <= 0) {
			setMoodCdStarted(0);
			return 0;
		}
		return (int) (remains / 1000);
	}

	/**
	 * @return the giftCdStarted
	 */
	public long getGiftCdStarted() {
		return giftCdStarted;
	}

	/**
	 * @param giftCdStarted
	 *          the giftCdStarted to set
	 */
	public void setGiftCdStarted(long giftCdStarted) {
		this.giftCdStarted = giftCdStarted;
	}

	public int getGiftRemainingTime() {
		long stop = giftCdStarted + 3600 * 1000;
		long remains = stop - System.currentTimeMillis();
		if (remains <= 0) {
			setGiftCdStarted(0);
			return 0;
		}
		return (int) (remains / 1000);
	}

	/**
	 * @return the despawnTime
	 */
	public Timestamp getDespawnTime() {
		return despawnTime;
	}

	/**
	 * @param despawnTime
	 *          the despawnTime to set
	 */
	public void setDespawnTime(Timestamp despawnTime) {
		this.despawnTime = despawnTime;
	}

	/**
	 * @return feedProgress, null if pet has no feed function
	 */
	public PetFeedProgress getFeedProgress() {
		return feedProgress;
	}

	public void setIsLooting(boolean isLooting) {
		this.isLooting = isLooting;
	}

	public boolean isLooting() {
		return this.isLooting;
	}

	public boolean isSelling() {
		return isSelling;
	}

	public void setIsSelling(boolean selling) {
		isSelling = selling;
	}

	public PetDopingBag getDopingBag() {
		return dopingBag;
	}

	@Override
	public int getExpireTime() {
		return expireTime;
	}

	@Override
	public void onExpire(Player player) {
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PET_ABANDON_EXPIRE_TIME_COMPLETE(name));
		PetAdoptionService.surrenderPet(player, templateId);
	}

}
