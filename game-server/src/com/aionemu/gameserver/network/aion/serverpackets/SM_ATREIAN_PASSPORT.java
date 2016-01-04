package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.account.Passport;
import com.aionemu.gameserver.model.account.PassportsList;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ViAl
 */
public class SM_ATREIAN_PASSPORT extends AionServerPacket {

	private PassportsList passports;
	private int stamps;
	private int year;
	private int month;

	public SM_ATREIAN_PASSPORT(PassportsList passports, int stamps, int year, int month) {
		this.passports = passports;
		this.stamps = stamps;
		this.month = month;
		this.year = year;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(year);
		writeH(month);
		writeH(0);// unk //can be variable
		writeH(passports.getAllPassports().size());

		for (Passport pp : passports.getAllPassports()) {
			writeD(pp.getId());
			writeD(stamps);
			writeH(pp.getRewardStatus());
			writeH(0);
			writeD((int) (pp.getArriveDate().getTime() / 1000));
		}
	}
}
