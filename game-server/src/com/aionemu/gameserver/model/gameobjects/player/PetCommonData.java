package com.aionemu.gameserver.model.gameobjects.player;

import java.sql.Timestamp;
import java.util.concurrent.Future;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Expirable;
import com.aionemu.gameserver.model.templates.pet.PetDopingBag;
import com.aionemu.gameserver.model.templates.pet.PetFunctionType;
import com.aionemu.gameserver.model.templates.pet.PetTemplate;
import com.aionemu.gameserver.services.toypet.PetAdoptionService;
import com.aionemu.gameserver.services.toypet.PetFeedProgress;
import com.aionemu.gameserver.services.toypet.PetHungryLevel;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class PetCommonData implements Expirable {

	private final int objectId;
	private final int templateId;
	private final int masterObjectId;
	private int decoration;
	private int color;
	private int face;
	private int head;
	private String name;
	private Timestamp birthday;
	PetFeedProgress feedProgress = null;
	PetDopingBag dopingBag = null;
	private volatile boolean cancelFeed = false;
	private long refeedTime;
	private int moodPoints;
	private long moodCheckTime;
	private long presentTime;
	private boolean socialDone;
	private int expireTime;
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

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getFace() {
		return face;
	}

	public void setFace(int face) {
		this.face = face;
	}

	public int getHead() {
		return head;
	}

	public void setHead(int head) {
		this.head = head;
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

	public boolean getCancelFeed() {
		return cancelFeed;
	}

	public void setCancelFeed(boolean cancelFeed) {
		this.cancelFeed = cancelFeed;
	}

	public void scheduleRefeed(long reFoodTime) {
		cancelRefeedTask();
		refeedTask = ThreadPoolManager.getInstance().schedule(() -> {
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

	/**
	 * Mood points are capped at this value; a full mood lets the player take the pet's present.
	 */
	public static final int MAX_MOOD_POINTS = 9000;
	private static final long MOOD_CHECK_COOLDOWN_MILLIS = 600_000;
	private static final long MOOD_SOCIAL_WINDOW_MILLIS = 30_000;
	private static final long PRESENT_COOLDOWN_MILLIS = 3_600_000;

	public synchronized int getMoodPoints() {
		return moodPoints;
	}

	public synchronized void setMoodPoints(int moodPoints) {
		this.moodPoints = Math.clamp(moodPoints, 0, MAX_MOOD_POINTS);
	}

	/**
	 * @return True if the mood is full after adding the points.
	 */
	public synchronized boolean addMoodPoints(int points) {
		setMoodPoints(moodPoints + points);
		return moodPoints == MAX_MOOD_POINTS;
	}

	public synchronized long getMoodCheckTime() {
		return moodCheckTime;
	}

	public synchronized void setMoodCheckTime(long moodCheckTime) {
		this.moodCheckTime = moodCheckTime;
	}

	/**
	 * Starts a mood check if the mood isn't full and the last check is at least 10 minutes old. The player can then socialize with the pet once,
	 * within the next 30 seconds.
	 */
	public synchronized boolean startMoodCheck() {
		long now = System.currentTimeMillis();
		if (moodPoints >= MAX_MOOD_POINTS || now < moodCheckTime + MOOD_CHECK_COOLDOWN_MILLIS)
			return false;
		moodCheckTime = now;
		socialDone = false;
		return true;
	}

	/**
	 * Consumes the social interaction of the current mood check.
	 */
	public synchronized boolean useSocial() {
		if (socialDone || System.currentTimeMillis() > moodCheckTime + MOOD_SOCIAL_WINDOW_MILLIS)
			return false;
		socialDone = true;
		return true;
	}

	/**
	 * The player cancelled the mood animation; the social interaction is lost until the next mood check.
	 */
	public synchronized void cancelSocial() {
		socialDone = true;
	}

	public synchronized long getPresentTime() {
		return presentTime;
	}

	public synchronized void setPresentTime(long presentTime) {
		this.presentTime = presentTime;
	}

	public synchronized boolean isPresentOnCooldown() {
		return System.currentTimeMillis() < presentTime + PRESENT_COOLDOWN_MILLIS;
	}

	/**
	 * Resets the mood to an empty bar and clears the check and present cooldowns.
	 */
	public synchronized void resetMood() {
		moodPoints = 0;
		moodCheckTime = 0;
		presentTime = 0;
		socialDone = false;
	}

	public synchronized int getMoodCheckRemainingSeconds() {
		return remainingSeconds(moodCheckTime + MOOD_CHECK_COOLDOWN_MILLIS);
	}

	public synchronized int getPresentRemainingSeconds() {
		return remainingSeconds(presentTime + PRESENT_COOLDOWN_MILLIS);
	}

	private static int remainingSeconds(long endMillis) {
		return (int) Math.max(0, (endMillis - System.currentTimeMillis()) / 1000);
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
		PetAdoptionService.surrenderPet(player, templateId, true);
	}

}
