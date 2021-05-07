package com.aionemu.gameserver.utils.collections;

import java.util.ArrayList;

/**
 * @author Neon
 */
@SuppressWarnings("serial")
public abstract class ListPart<Type> extends ArrayList<Type> {

	private final int partNo;
	private boolean isLast;

	protected ListPart(int partNo, boolean isLast) {
		this.partNo = partNo;
		this.isLast = isLast;
	}

	public int getPartNo() {
		return partNo;
	}

	public boolean isFirst() {
		return partNo == 1;
	}

	public boolean isLast() {
		return isLast;
	}

	protected void setLast(boolean isLast) {
		this.isLast = isLast;
	}

	protected abstract boolean fits(Type element);

}
