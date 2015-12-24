package com.aionemu.gameserver.model.team.legion;

import com.aionemu.gameserver.model.gameobjects.PersistentState;

/**
 * @author Simple
 ' @modified cura, Neon
 */
public class LegionEmblem {

	private byte emblemId = 0;
	private byte color_a = 0;
	private byte color_r = 0;
	private byte color_g = 0;
	private byte color_b = 0;
	private LegionEmblemType emblemType = LegionEmblemType.DEFAULT;
	private PersistentState persistentState;

	private boolean isUploading = false;
	private int uploadSize = 0;
	private int uploadedSize = 0;
	private byte[] uploadData;

	private byte[] customEmblemData;

	/**
	 * @return the customEmblemData
	 */
	public byte[] getCustomEmblemData() {
		return customEmblemData;
	}

	/**
	 * @param customEmblemData
	 *          the customEmblemData to set
	 */
	public void setCustomEmblemData(byte[] customEmblemData) {
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		this.customEmblemData = customEmblemData;
		this.emblemType = LegionEmblemType.CUSTOM;
	}

	public LegionEmblem() {
		setPersistentState(PersistentState.NEW);
	}

	public void setEmblem(int emblemId, int color_a, int color_r, int color_g, int color_b, LegionEmblemType emblemType, byte[] emblem_data) {
		this.emblemId = (byte) emblemId;
		this.color_a = (byte) color_a;
		this.color_r = (byte) color_r;
		this.color_g = (byte) color_g;
		this.color_b = (byte) color_b;
		this.emblemType = emblemType;
		this.customEmblemData = emblem_data;
		if (this.emblemType.equals(LegionEmblemType.CUSTOM) && customEmblemData == null)
			this.emblemType = LegionEmblemType.DEFAULT;

		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	/**
	 * @return the emblemId
	 */
	public byte getEmblemId() {
		return emblemId;
	}

	/**
	 * @return The alpha value.
	 */
	public byte getColor_a() {
		return color_a;
	}

	/**
	 * @return the color_r
	 */
	public byte getColor_r() {
		return color_r;
	}

	/**
	 * @return the color_g
	 */
	public byte getColor_g() {
		return color_g;
	}

	/**
	 * @return the color_b
	 */
	public byte getColor_b() {
		return color_b;
	}

	/**
	 * @param isUploading
	 *          the isUploading to set
	 */
	public void setUploading(boolean isUploading) {
		this.isUploading = isUploading;
	}

	/**
	 * @return the isUploading
	 */
	public boolean isUploading() {
		return isUploading;
	}

	/**
	 * @param emblemSize
	 *          the emblemSize to set
	 */
	public void setUploadSize(int emblemSize) {
		this.uploadSize = emblemSize;
	}

	/**
	 * @return the emblemSize
	 */
	public int getUploadSize() {
		return uploadSize;
	}

	/**
	 * @param uploadData
	 *          the uploadData to set
	 */
	public void addUploadData(byte[] data) {
		byte[] newData = new byte[uploadedSize];
		int i = 0;
		if (uploadData != null && uploadData.length > 0) {
			for (byte dataByte : uploadData) {
				newData[i] = dataByte;
				i++;
			}
		}
		for (byte dataByte : data) {
			newData[i] = dataByte;
			i++;
		}
		this.uploadData = newData;
	}

	/**
	 * @return the uploadData
	 */
	public byte[] getUploadData() {
		return this.uploadData;
	}

	/**
	 * @param uploadedSize
	 *          the uploadedSize to set
	 */
	public void addUploadedSize(int uploadedSize) {
		this.uploadedSize += uploadedSize;
	}

	/**
	 * @return the uploadedSize
	 */
	public int getUploadedSize() {
		return uploadedSize;
	}

	/**
	 * @param emblemType
	 *          the emblemType to set
	 */
	public void setEmblemType(LegionEmblemType emblemType) {
		this.emblemType = emblemType;
	}

	/**
	 * @return the emblemType
	 */
	public LegionEmblemType getEmblemType() {
		return emblemType;
	}

	/**
	 * This method will clear out all upload data
	 */
	public void resetUploadSettings() {
		this.isUploading = false;
		this.uploadedSize = 0;
		this.uploadData = null;
	}

	/**
	 * @param persistentState
	 */
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case UPDATE_REQUIRED:
				if (this.persistentState == PersistentState.NEW)
					break;
			default:
				this.persistentState = persistentState;
		}
	}

	/**
	 * @return the persistentState
	 */
	public PersistentState getPersistentState() {
		return persistentState;
	}

}
