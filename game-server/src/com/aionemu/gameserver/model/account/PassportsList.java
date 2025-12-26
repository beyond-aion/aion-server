package com.aionemu.gameserver.model.account;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
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
        for (var passport : this.passports) {
            if (passport.getId() == passportId && passport.getArriveDate().getTime() / 1000 == timestamp) {
                return passport;
            }
        }
        return null;
    }

    public boolean isPassportPresent(int passportId) {
        for (var pp : this.passports) {
            if (pp.getId() == passportId) {
                return true;
            }
        }
        return false;
    }

    public List<Passport> getAllPassports() {
        return passports;
    }

    public boolean hasPassportForDay(int passportId, LocalDate attendDay) {
        for (var pp : passports) {
            if (pp.getId() != passportId) {
                continue;
            }
            var ppDay = pp.getArriveDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (ppDay.equals(attendDay)) {
                return true;
            }
        }
        return false;
    }
}
