package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.autogroup.AutoGroupType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author SheppeR, Guapo, nrg, Estrayl
 */
public class SM_AUTO_GROUP extends AionServerPacket {

	public static final byte WND_ENTRY_ICON = 6;
	private final int maskId;
	private final int mapId;
	private final int messageId;
	private final int titleId;
	private int windowId;
	private int requestTypeId;
	private boolean close;
	private String name = "";

	public SM_AUTO_GROUP(int maskId) {
		AutoGroupType agt = AutoGroupType.getAGTByMaskId(maskId);
		if (agt == null) {
			throw new IllegalArgumentException("AutoGroupType not found for maskId: " + maskId);
		}

		this.maskId = maskId;
		this.messageId = agt.getL10nId();
		this.titleId = agt.getTemplate().getTitleId();
		this.mapId = agt.getTemplate().getInstanceMapId();
	}

	public SM_AUTO_GROUP(int maskId, int windowId) {
		this(maskId);
		this.windowId = windowId;
	}

	public SM_AUTO_GROUP(int maskId, int windowId, boolean close) {
		this(maskId);
		this.windowId = windowId;
		this.close = close;
	}

	public SM_AUTO_GROUP(int maskId, int windowId, int requestTypeId, String name) {
		this(maskId);
		this.windowId = windowId;
		this.requestTypeId = requestTypeId;
		this.name = name;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(maskId);
		writeC(windowId);
		writeD(mapId);
		switch (windowId) {
			case 0, 7 -> { // 0 = request entry, 7 = failed window
				writeD(messageId);
				writeD(titleId);
				writeD(0);
			}
			case 1, 3, 8 -> { // 1 = waiting window, 3 = pass window, 8 = on login
				writeD(0); // progression type: 0 = Group Formation in Progress, 1 = Opponent Group formation in progress
				writeD(0);
				writeD(requestTypeId);
			}
			case 2, 4, 5 -> { // 2 = cancel looking, 4 = enter window, 5 = after clicking enter
				writeD(0);
				writeD(0);
				writeD(0);
			}
			case WND_ENTRY_ICON -> { // entry icon
				writeD(messageId);
				writeD(titleId);
				writeD(close ? 0 : 1);
			}
		}
		writeC(0);
		writeS(name);
	}
}
