package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.npcshout.ShoutEventType;
import com.aionemu.gameserver.services.SiegeService;

/**
 * @author Rolandas
 */
@AIName("speaker")
public class SpeakerAI extends GeneralNpcAI {

	public SpeakerAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean onPatternShout(ShoutEventType event, String pattern, int skillNumber) {
		if (getOwner().getWorldId() != 210050000 && getOwner().getWorldId() != 220070000)
			return false;
		if (event != ShoutEventType.IDLE || pattern == null)
			return false;

		final SiegeService srv = SiegeService.getInstance();

		Race npcRace = getOwner().getRace();
		if (npcRace == Race.ASMODIANS) {
			if ("1".equals(pattern)) {
				// TODO: find if Dredgion Ship is spawned
			} else if ("2".equals(pattern)) {
				return srv.isSiegeInProgress(1142) && srv.getSiegeLocation(1142).getRace() == SiegeRace.ASMODIANS
					|| srv.isSiegeInProgress(1143) && srv.getSiegeLocation(1143).getRace() == SiegeRace.ASMODIANS
					|| srv.isSiegeInProgress(1144) && srv.getSiegeLocation(1144).getRace() == SiegeRace.ASMODIANS
					|| srv.isSiegeInProgress(1145) && srv.getSiegeLocation(1145).getRace() == SiegeRace.ASMODIANS;
			} else if ("3".equals(pattern)) {
				return srv.isSiegeInProgress(1132) && srv.getSiegeLocation(1132).getRace() == SiegeRace.ASMODIANS
					|| srv.isSiegeInProgress(1251) && srv.getSiegeLocation(1251).getRace() == SiegeRace.ASMODIANS;
			} else if ("4".equals(pattern)) {
				return srv.isSiegeInProgress(1221) && srv.getSiegeLocation(1221).getRace() == SiegeRace.BALAUR;
			} else if ("5".equals(pattern)) {
				return srv.isSiegeInProgress(1131);
			} else if ("6".equals(pattern)) {
				return srv.getSiegeLocation(3011) != null && srv.getSiegeLocation(3011).getRace() == SiegeRace.ELYOS && srv.getSiegeLocation(3021) != null
					&& srv.getSiegeLocation(3021).getRace() == SiegeRace.ELYOS;
			}
		} else if (npcRace == Race.ELYOS) {
			if ("1".equals(pattern)) {
				// TODO: find if Dredgion Ship is spawned
			} else if ("2".equals(pattern)) {
				return srv.isSiegeInProgress(1142) && srv.getSiegeLocation(1142).getRace() == SiegeRace.ELYOS
					|| srv.isSiegeInProgress(1143) && srv.getSiegeLocation(1143).getRace() == SiegeRace.ELYOS
					|| srv.isSiegeInProgress(1144) && srv.getSiegeLocation(1144).getRace() == SiegeRace.ELYOS
					|| srv.isSiegeInProgress(1145) && srv.getSiegeLocation(1145).getRace() == SiegeRace.ELYOS;
			} else if ("3".equals(pattern)) {
				return srv.isSiegeInProgress(1141) && srv.getSiegeLocation(1141).getRace() == SiegeRace.ELYOS
					|| srv.isSiegeInProgress(1211) && srv.getSiegeLocation(1211).getRace() == SiegeRace.ELYOS;
			} else if ("4".equals(pattern)) {
				return srv.isSiegeInProgress(1241) && srv.getSiegeLocation(1241).getRace() == SiegeRace.BALAUR;
			} else if ("5".equals(pattern)) {
				return srv.isSiegeInProgress(1141);
			} else if ("6".equals(pattern)) {
				return srv.getSiegeLocation(2011) != null && srv.getSiegeLocation(2011).getRace() == SiegeRace.ASMODIANS && srv.getSiegeLocation(2021) != null
					&& srv.getSiegeLocation(2021).getRace() == SiegeRace.ASMODIANS;
			}
		}

		return false;
	}
}
