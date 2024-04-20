package com.aionemu.gameserver.model.team.legion;

import com.aionemu.gameserver.model.gameobjects.Persistable;

/**
 * @author Simple, cura, Neon
 */
public class LegionEmblem implements Persistable {

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

	private byte[] customEmblemData = {};

	public byte[] getCustomEmblemData() {
		return customEmblemData;
	}

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
		if (this.emblemType == LegionEmblemType.CUSTOM && customEmblemData == null)
			this.emblemType = LegionEmblemType.DEFAULT;

		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public byte getEmblemId() {
		return emblemId;
	}

	/**
	 * @return The alpha value.
	 */
	public byte getColor_a() {
		return color_a;
	}

	public byte getColor_r() {
		return color_r;
	}

	public byte getColor_g() {
		return color_g;
	}

	public byte getColor_b() {
		return color_b;
	}

	public void setUploading(boolean isUploading) {
		this.isUploading = isUploading;
	}

	public boolean isUploading() {
		return isUploading;
	}

	public void setUploadSize(int emblemSize) {
		this.uploadSize = emblemSize;
	}

	public int getUploadSize() {
		return uploadSize;
	}

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

	public byte[] getUploadData() {
		return this.uploadData;
	}

	public void addUploadedSize(int uploadedSize) {
		this.uploadedSize += uploadedSize;
	}

	public int getUploadedSize() {
		return uploadedSize;
	}

	public void setEmblemType(LegionEmblemType emblemType) {
		this.emblemType = emblemType;
	}

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

	@Override
	@SuppressWarnings("fallthrough")
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case UPDATE_REQUIRED:
				if (this.persistentState == PersistentState.NEW)
					break;
			default:
				this.persistentState = persistentState;
		}
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

}
