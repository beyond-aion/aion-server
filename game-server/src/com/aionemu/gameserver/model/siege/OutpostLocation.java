package com.aionemu.gameserver.model.siege;

import java.util.List;

import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;
import com.aionemu.gameserver.services.SiegeService;

/**
 * @author Source These bosses only appear when an faction conquer all balaurea fortress... If Elyos conquer all fortress the Enraged Mastarius appear
 *         on Ancient City of Marayas If Asmodians conquer all fortress the Enraged Veille appear on Inggison Outpost He/She still active for 2 hours
 *         after that he/she disappear and respawn again next day on the end of Siege (if the faction owns all fortress)
 */
public class OutpostLocation extends SiegeLocation {

	public OutpostLocation() {
	}

	public OutpostLocation(SiegeLocationTemplate template) {
		super(template);
	}

	@Override
	public int getNextState() {
		return isVulnerable() ? STATE_INVULNERABLE : STATE_VULNERABLE;
	}

	/**
	 * @return Outpost Location Race
	 */
	public SiegeRace getLocationRace() {
		// TODO Should be configured from datapack
		switch (getLocationId()) {
			case 3111:
				return SiegeRace.ASMODIANS;
			case 2111:
				return SiegeRace.ELYOS;
			default:
				throw new RuntimeException("Please move this to datapack");
		}
	}

	/**
	 * @return Fortresses that must be captured to own this outpost
	 */
	public List<Integer> getFortressDependency() {
		return template.getFortressDependency();
	}

	public boolean isSiegeAllowed() {
		return getLocationRace() == getRace();
	}

	public boolean isSilenteraAllowed() {
		return !isSiegeAllowed() && !getRace().equals(SiegeRace.BALAUR);
	}

	public boolean isRouteSpawned() {
		for (Integer fortressId : getFortressDependency()) {
			SiegeRace sr = SiegeService.getInstance().getFortresses().get(fortressId).getRace();
			if (sr == getLocationRace()) {
				return true;
			}
		}

		return false;
	}
}
