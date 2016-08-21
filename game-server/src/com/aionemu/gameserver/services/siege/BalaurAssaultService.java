package com.aionemu.gameserver.services.siege;

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
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.assemblednpc.AssembledNpcTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_NPC_ASSEMBLER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;

import javolution.util.FastTable;

/**
 * @author synchro2
 * @reworked Luzien
 * @modified Estrayl
 * TODO: Send Peace Dredgion without assault
 */
public class BalaurAssaultService {
	private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");
	private final Map<Integer, FortressAssault> fortressAssaults = new ConcurrentHashMap<>();
	private final Map<Integer, ArtifactAssault> artifactAssaults = new ConcurrentHashMap<>();

	public static BalaurAssaultService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void onSiegeStart(final Siege<?> siege) {
		if (siege instanceof FortressSiege) {
			if (!calculateFortressAssault(((FortressSiege) siege).getSiegeLocation()))
				return;
			newAssault(siege, Rnd.get(60, 900)); // between 1 and 15 minutes
		} else if (siege instanceof ArtifactSiege) {
			if (artifactAssaults.containsKey(siege.getSiegeLocation().getLocationId()) || siege.getSiegeLocation().getRace() == SiegeRace.BALAUR)
				return;
			newAssault(siege, Rnd.get(180, 2880)); // between 3 and 48 hours
		} else
			return;
		if (LoggingConfig.LOG_SIEGE)
			log.debug("[SIEGE] Balaur Assault scheduled on Siege ID: " + siege.getSiegeLocationId() + "!");
	}

	public void onSiegeFinish(Siege<?> siege) {
		int locId = siege.getSiegeLocationId();
		if (fortressAssaults.containsKey(locId)) {
			Boolean bossIsKilled = siege.isBossKilled();
			fortressAssaults.get(locId).finishAssault(bossIsKilled);
			if (bossIsKilled && siege.getSiegeLocation().getRace().equals(SiegeRace.BALAUR))
				log.debug("[SIEGE] > [FORTRESS:" + siege.getSiegeLocationId() + "] has been captured by Balaur Assault!");
			else
				log.debug("[SIEGE] > [FORTRESS:" + siege.getSiegeLocationId() + "] Balaur Assault finished without capture!");
			fortressAssaults.remove(locId);
		} else if (artifactAssaults.containsKey(locId)) {
			Boolean bossIsKilled = siege.isBossKilled();
			artifactAssaults.get(locId).finishAssault(bossIsKilled);
			if (bossIsKilled && siege.getSiegeLocation().getRace().equals(SiegeRace.BALAUR))
				log.debug("[SIEGE] > [ARTIFACT:" + siege.getSiegeLocationId() + "] has been captured by Balaur Assault!");
			else {
				log.debug("[SIEGE] > [ARTIFACT:" + siege.getSiegeLocationId() + "] Balaur Assault finished without capture!");
			}
			artifactAssaults.remove(locId);
		}
	}

	private boolean calculateFortressAssault(FortressLocation fortress) {
		if (fortress.getRace() == SiegeRace.BALAUR || !fortress.isVulnerable())
			return false;

		boolean isBalaurea = fortress.getWorldId() == 210050000 || fortress.getWorldId() == 220070000;

		if (fortressAssaults.containsKey(fortress.getLocationId()))
			return false;

		int count = 0; // Allow only 2 Balaur attacks per map, 1 per Balaurea map
		for (FortressAssault fa : fortressAssaults.values()) {
			if (fa.getWorldId() == fortress.getWorldId())
				count++;
		}
		if (count >= (isBalaurea ? 1 : 2))
			return false;

		float influence = fortress.getRace() == SiegeRace.ASMODIANS ? Influence.getInstance().getGlobalAsmodiansInfluence() : Influence.getInstance()
			.getGlobalElyosInfluence();

		if (Rnd.get() >= influence * SiegeConfig.BALAUR_ASSAULT_RATE)
			return false;

		return true;
	}

	public void startAssault(Player player, int location, int delay) {
		if (fortressAssaults.containsKey(location) || artifactAssaults.containsKey(location)) {
			PacketSendUtility.sendMessage(player, "Assault on " + location + " was already started.");
			return;
		}

		newAssault(SiegeService.getInstance().getSiege(location), delay);
	}

	private void newAssault(Siege<?> siege, int delay) {
		if (siege instanceof FortressSiege) {
			FortressAssault assault = new FortressAssault((FortressSiege) siege);
			assault.startAssault(delay);
			fortressAssaults.put(siege.getSiegeLocationId(), assault);
		} else if (siege instanceof ArtifactSiege) {
			ArtifactAssault assault = new ArtifactAssault((ArtifactSiege) siege);
			assault.startAssault(delay);
			artifactAssaults.put(siege.getSiegeLocationId(), assault);
		}
	}

	public void spawnDredgion(int spawnId) {
		AssembledNpcTemplate template = DataManager.ASSEMBLED_NPC_DATA.getAssembledNpcTemplate(spawnId);
		FastTable<AssembledNpcPart> assembledParts = new FastTable<>();
		for (AssembledNpcTemplate.AssembledNpcPartTemplate npcPart : template.getAssembledNpcPartTemplates())
			assembledParts.add(new AssembledNpcPart(IDFactory.getInstance().nextId(), npcPart));

		AssembledNpc npc = new AssembledNpc(template.getRouteId(), template.getMapId(), template.getLiveTime(), assembledParts);
		for (Player p : World.getInstance().getAllPlayers()) {
			PacketSendUtility.sendPacket(p, new SM_NPC_ASSEMBLER(npc));
			PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_ABYSS_CARRIER_SPAWN());
		}
	}
	
	private static class SingletonHolder {

		private static final BalaurAssaultService INSTANCE = new BalaurAssaultService();
	}
}
