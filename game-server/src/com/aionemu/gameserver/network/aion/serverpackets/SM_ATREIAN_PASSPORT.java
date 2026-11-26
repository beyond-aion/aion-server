package com.aionemu.gameserver.network.aion.serverpackets;

import java.time.LocalDate;
import java.util.List;

import com.aionemu.gameserver.model.account.Passport;
import com.aionemu.gameserver.model.account.PassportsList;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ViAl, Neon
 */
public class SM_ATREIAN_PASSPORT extends AionServerPacket {

	private LocalDate accountCreationDate;
	private final List<Passport> passports;
	private int stamps;

	public SM_ATREIAN_PASSPORT(PassportsList passports, int stamps, LocalDate accountCreationDate) {
		this.accountCreationDate = accountCreationDate;
		this.passports = List.copyOf(passports.getAllPassports()); // the player's list changes while the packet waits to be written
		this.stamps = stamps;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(accountCreationDate.getYear());
		writeH(accountCreationDate.getMonthValue());
		writeH(accountCreationDate.getDayOfMonth());
		writeH(passports.size());
		for (Passport pp : passports) {
			writeD(pp.getId());
			writeD(stamps); // wrong, this is the stamp count when each passport was received (current month sends current count for upcoming rewards)
			writeD(pp.getRewardStatus().getId()); // 0 = not yet arrived (upcoming this months rewards), 1 = arrived and not taken, 2 = arrived and taken, 3 = not arrived (last months rewards)
			writeD((int) (pp.getArriveDate().getTime() / 1000)); // for upcoming rewards it's the first login time each day
		}
	}
}
