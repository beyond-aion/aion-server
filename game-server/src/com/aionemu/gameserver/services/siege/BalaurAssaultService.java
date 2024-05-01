package com.aionemu.gameserver.services.siege;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.assemblednpc.AssembledNpc;
import com.aionemu.gameserver.model.assemblednpc.AssembledNpcPart;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.assemblednpc.AssembledNpcTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_NPC_ASSEMBLER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;

/**
 * @author synchro2, Luzien, Estrayl
 *         TODO: Send Peace Dredgion without assault
 *         TODO: Dredgion Battleship as real NPC
 */
public class BalaurAssaultService {

	private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");
	private final Map<Integer, FortressAssault> fortressAssaults = new ConcurrentHashMap<>();
	private final Map<Integer, ArtifactAssault> artifactAssaults = new ConcurrentHashMap<>();

	public static BalaurAssaultService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void onSiegeStart(Siege<?> siege) {
		if (siege instanceof FortressSiege) {
			if (!calculateFortressAssault(((FortressSiege) siege).getSiegeLocation()))
				return;
			newAssault(siege, Rnd.get(60, 900)); // between 1 and 15 minutes
		} else if (siege instanceof ArtifactSiege) {
			if (artifactAssaults.containsKey(siege.getSiegeLocation().getLocationId()) || siege.getSiegeLocation().getRace() == SiegeRace.BALAUR)
				return;
			newAssault(siege, Rnd.get(10800, 172800)); // between 3 and 48 hours
		}
	}

	public void onSiegeFinish(Siege<?> siege) {
		int locId = siege.getSiegeLocationId();
		boolean isBossKilled = siege.isBossKilled();
		if (fortressAssaults.containsKey(locId)) {
			fortressAssaults.remove(locId).finishAssault(isBossKilled);
			if (isBossKilled && siege.getSiegeLocation().getRace().equals(SiegeRace.BALAUR))
				log.info(siege + " has been captured by Balaur assault!");
			else
				log.info(siege + " Balaur assault finished without capture!");
		} else if (artifactAssaults.containsKey(locId)) {
			artifactAssaults.remove(locId).finishAssault(isBossKilled);
			if (isBossKilled && siege.getSiegeLocation().getRace().equals(SiegeRace.BALAUR))
				log.info(siege + " has been captured by Balaur assault!");
			else
				log.info(siege + " Balaur assault finished without capture!");
		}
	}

	private boolean calculateFortressAssault(FortressLocation fortress) {
		if (fortress.getRace() == SiegeRace.BALAUR || !fortress.isVulnerable())
			return false;

		boolean isBalaurea = fortress.getWorldId() == 210050000 || fortress.getWorldId() == 220070000;

		if (fortressAssaults.containsKey(fortress.getLocationId()))
			return false;

		int count = 0;
		for (FortressAssault fa : fortressAssaults.values()) {
			if (fa.getWorldId() == fortress.getWorldId())
				count++;
		}
		if (count >= (isBalaurea ? 1 : 2)) // Allow only 2 Balaur attacks per map, 1 per Balaurea map
			return false;

		float influence = fortress.getRace() == SiegeRace.ASMODIANS ? Influence.getInstance().getAsmodianInfluenceRate()
			: Influence.getInstance().getElyosInfluenceRate();

		return Rnd.chance() < influence * 100f * SiegeConfig.BALAUR_ASSAULT_RATE;
	}

	public boolean startAssault(int location, int delay) {
		Siege<? extends SiegeLocation> siege = SiegeService.getInstance().getSiege(location);
		if (siege == null || fortressAssaults.containsKey(location) || artifactAssaults.containsKey(location)) {
			return false;
		}
		newAssault(siege, delay);
		return true;
	}

	private void newAssault(Siege<?> siege, int delay) {
		if (siege instanceof FortressSiege fortressSiege) {
			FortressAssault assault = new FortressAssault(fortressSiege);
			assault.startAssault(delay);
			fortressAssaults.put(siege.getSiegeLocationId(), assault);
		} else if (siege instanceof ArtifactSiege artifactSiege) {
			ArtifactAssault assault = new ArtifactAssault(artifactSiege);
			assault.startAssault(delay);
			artifactAssaults.put(siege.getSiegeLocationId(), assault);
		} else {
			throw new IllegalArgumentException("Unsupported fortress siege type: " + siege.getClass().getSimpleName());
		}
		if (LoggingConfig.LOG_SIEGE)
			log.info("Scheduled assault of " + siege + " in " + delay + " seconds");
	}

	public void spawnDredgion(int spawnId) {
		AssembledNpcTemplate template = DataManager.ASSEMBLED_NPC_DATA.getAssembledNpcTemplate(spawnId);
		List<AssembledNpcPart> assembledParts = new ArrayList<>();
		template.getAssembledNpcPartTemplates().forEach(part -> assembledParts.add(new AssembledNpcPart(IDFactory.getInstance().nextId(), part)));

		AssembledNpc npc = new AssembledNpc(template.getRouteId(), template.getMapId(), template.getLiveTime(), assembledParts);
		World.getInstance().forEachPlayer(p -> {
			PacketSendUtility.sendPacket(p, new SM_NPC_ASSEMBLER(npc));
			PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_ABYSS_CARRIER_SPAWN());
		});
	}

	/**
	 * @return
	 * 				- the FortressAssault object or null if none is active
	 */
	public FortressAssault getFortressAssaultBySiegeId(int siegeId) {
		return fortressAssaults.get(siegeId);
	}

	private static class SingletonHolder {

		private static final BalaurAssaultService INSTANCE = new BalaurAssaultService();
	}
}
