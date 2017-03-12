package com.aionemu.gameserver.model.account;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ViAl
 */
public class PassportsList {

	private final List<Passport> passports;

	public PassportsList() {
		passports = new ArrayList<>();
	}

	public void addPassport(Passport passport) {
		passports.add(passport);
	}

	public void removePassport(Passport passport) {
		passports.remove(passport);
	}

	public Passport getPassport(int passportId, int timestamp) {
		for (Passport passport : this.passports)
			if (passport.getId() == passportId && passport.getArriveDate().getTime() / 1000 == timestamp)
				return passport;
		return null;
	}

	public boolean isPassportPresent(int passportId) {
		for (Passport pp : this.passports) {
			if (pp.getId() == passportId)
				return true;
		}
		return false;
	}

	public List<Passport> getAllPassports() {
		return passports;
	}
}
