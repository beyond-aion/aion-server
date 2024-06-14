package com.aionemu.gameserver.services;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dao.TownDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HouseAddress;
import com.aionemu.gameserver.model.templates.housing.HousingLand;
import com.aionemu.gameserver.model.town.Town;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TOWNS_LIST;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author ViAl
 */
public class TownService {

	private static final Logger log = LoggerFactory.getLogger(TownService.class);
	private Map<Integer, Town> elyosTowns;
	private Map<Integer, Town> asmosTowns;

	private static class SingletonHolder {

		protected static final TownService instance = new TownService();
	}

	public static final TownService getInstance() {
		return SingletonHolder.instance;
	}

	private TownService() {
		elyosTowns = TownDAO.load(Race.ELYOS);
		asmosTowns = TownDAO.load(Race.ASMODIANS);
		if (elyosTowns.size() == 0 && asmosTowns.size() == 0) {
			for (HousingLand land : DataManager.HOUSE_DATA.getLands()) {
				for (HouseAddress address : land.getAddresses()) {
					if (address.getTownId() == 0)
						continue;
					else {
						Race townRace = DataManager.NPC_DATA.getNpcTemplate(land.getManagerNpcId()).getTribe() == TribeClass.GENERAL ? Race.ELYOS
							: Race.ASMODIANS;
						if ((townRace == Race.ELYOS && !elyosTowns.containsKey(address.getTownId()))
							|| (townRace == Race.ASMODIANS && !asmosTowns.containsKey(address.getTownId()))) {
							Town town = new Town(address.getTownId(), townRace);
							if (townRace == Race.ELYOS)
								elyosTowns.put(town.getId(), town);
							else
								asmosTowns.put(town.getId(), town);
							TownDAO.store(town);
						}

					}
				}
			}
		}
		log.info("Loaded " + elyosTowns.size() + " elyos towns.");
		log.info("Loaded " + asmosTowns.size() + " asmodian towns.");
	}

	public Town getTownById(int townId) {
		if (elyosTowns.containsKey(townId))
			return elyosTowns.get(townId);
		else
			return asmosTowns.get(townId);
	}

	public int getTownResidence(Player player) {
		House house = player.getActiveHouse();
		if (house == null)
			return 0;
		else
			return house.getAddress().getTownId();
	}

	public int getTownIdByPosition(Creature creature) {
		if (creature instanceof Npc) {
			if (((Npc) creature).getTownId() != 0)
				return ((Npc) creature).getTownId();
		}
		if (creature.isSpawned()) {
			for (ZoneInstance zone : creature.findZones()) {
				if (zone.getTownId() > 0)
					return zone.getTownId();
			}
		}
		return 0;
	}

	public void onEnterWorld(Player player) {
		switch (player.getRace()) {
			case ELYOS:
				if (player.getWorldId() == 700010000)
					PacketSendUtility.sendPacket(player, new SM_TOWNS_LIST(elyosTowns));
				break;
			case ASMODIANS:
				if (player.getWorldId() == 710010000)
					PacketSendUtility.sendPacket(player, new SM_TOWNS_LIST(asmosTowns));
				break;
		}
	}

}
