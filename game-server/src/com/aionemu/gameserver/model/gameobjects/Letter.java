package com.aionemu.gameserver.model.gameobjects;

import java.sql.Timestamp;

/**
 * @author kosyachok
 */
public class Letter extends AionObject implements Persistable {

	private int recipientId;
	private Item attachedItem;
	private long attachedKinahCount;
	private String senderName;
	private String title;
	private String message;
	private boolean unread;
	private boolean express;
	private Timestamp timeStamp;
	private PersistentState persistentState;
	private LetterType letterType;

	/**
	 * @param objId
	 * @param attachedItem
	 * @param attachedKinah
	 * @param title
	 * @param message
	 * @param senderId
	 * @param senderName
	 * @param timeStamp
	 *          new letter constructor
	 */
	public Letter(int objId, int recipientId, Item attachedItem, long attachedKinahCount, String title, String message, String senderName,
		Timestamp timeStamp, boolean unread, LetterType letterType) {
		super(objId);

		this.express = letterType == LetterType.EXPRESS || letterType == LetterType.BLACKCLOUD;
		this.recipientId = recipientId;
		this.attachedItem = attachedItem;
		this.attachedKinahCount = attachedKinahCount;
		this.title = title;
		this.message = message;
		this.senderName = senderName;
		this.timeStamp = timeStamp;
		this.unread = unread;
		this.persistentState = PersistentState.NEW;
		this.letterType = letterType;
	}

	@Override
	public String getName() {
		return title;
	}

	public int getRecipientId() {
		return recipientId;
	}

	public Item getAttachedItem() {
		return attachedItem;
	}

	public void setAttachedItem(Item attachedItem) {
		this.attachedItem = attachedItem;
		this.persistentState = PersistentState.UPDATE_REQUIRED;
	}

	public long getAttachedKinah() {
		return attachedKinahCount;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public String getSenderName() {
		return senderName;
	}

	public LetterType getLetterType() {
		return letterType;
	}

	public boolean isUnread() {
		return unread;
	}

	public void setReadLetter() {
		this.unread = false;
		this.persistentState = PersistentState.UPDATE_REQUIRED;
	}

	public boolean isExpress() {
		return express;
	}

	public void setExpress(boolean express) {
		this.express = express;
		this.persistentState = PersistentState.UPDATE_REQUIRED;
	}

	public void setLetterType(LetterType letterType) {
		this.letterType = letterType;
		if (letterType == LetterType.EXPRESS || letterType == LetterType.BLACKCLOUD)
			this.express = true;
		else
			this.express = false;
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	public void removeAttachedKinah() {
		this.attachedKinahCount = 0;
		this.persistentState = PersistentState.UPDATE_REQUIRED;
	}

	@Override
	public void setPersistentState(PersistentState state) {
		this.persistentState = state;
	}

	/**
	 * @return the timeStamp
	 */
	public Timestamp getTimeStamp() {
		return timeStamp;
	}
}
