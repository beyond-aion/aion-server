package com.aionemu.gameserver.services;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.raid.RaidLocation;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.raid.InvasionRaid;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Map;


/**
 * @author Alcapwnd
 */
public class InvasionRaidService {

    private static final Logger log = LoggerFactory.getLogger(InvasionRaidService.class);
    private static final String RAID_SPAWN_SCHEDULE = SiegeConfig.RAID_SPAWN_SCHEDULE;
    private static final String RAID_SPAWN_SCHEDULE2 = SiegeConfig.RAID_SPAWN_SCHEDULE2;
    private static final String RAID_SPAWN_ALL_SCHEDULE = "0 0 21 ? * *";
    private final Map<Integer, InvasionRaid<?>> active = new FastMap<Integer, InvasionRaid<?>>().shared();
    private Map<Integer, RaidLocation> raids;
    private boolean disabled;
    private Calendar calendar = Calendar.getInstance();
    private int flagNpc = 832819;

    public void initRaidLocations() {
        if (!SiegeConfig.RAID_ENABLE) {
            disabled = true;
            log.info("Raids are disabled...");
        }
        log.info("Loading Raids...");
        raids = DataManager.RAID_DATA.getRaidLocations();
        log.info("Loaded " + raids.size() + " raids.");
    }

    public void initRaids() {
        if (disabled)
            return;
        log.info("Init Raids...");
        
        spawnFlags();
        
        log.info("Raid spawn schedule one: " + RAID_SPAWN_SCHEDULE);
        log.info("Raid spawn schedule two: " + RAID_SPAWN_SCHEDULE2);
        
        if (calendar.get(Calendar.DAY_OF_MONTH) == 11) {//On 11th every month all invasion monsters will spawn at 9pm
        	CronService.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                	for (RaidLocation raid : getRaidLocation().values()) {
                        start(raid.getId());
                    }
                }
            }, RAID_SPAWN_ALL_SCHEDULE, true);
        } else {
        	CronService.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                	for (RaidLocation raid : getRaidLocation().values()) {
                        start(raid.getId());
                    }
                }
            }, RAID_SPAWN_SCHEDULE, true);
            
            CronService.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                	for (RaidLocation raid : getRaidLocation().values()) {
                        start(raid.getId());
                    }
                }
            }, RAID_SPAWN_SCHEDULE2, true);//NOTE: shouldnt be the same with schedule 1
        }
        
    }
    
    private void spawnFlags() {
        for (RaidLocation rl : raids.values()) {
            SpawnTemplate spawn = SpawnEngine.addNewSpawn(rl.getWorldId(), flagNpc, rl.getX(), rl.getY(), rl.getZ(), (byte) rl.getH(), 0);
            SpawnEngine.spawnObject(spawn, 1);
        }
    }

    public Map<Integer, RaidLocation> getRaidLocation() {
        return raids;
    }

    public RaidLocation getRaidLocation(int id) {
        return raids.get(id);
    }

    public void start(final int id) {
        final InvasionRaid<?> raid;

        synchronized (this) {
            if (active.containsKey(id)) {
                return;
            }
            raid = new InvasionRaid<RaidLocation>(getRaidLocation(id));
            active.put(id, raid);
        }

        raid.start();
        broadcastSpawn(getRaidLocation(id));
    }

    public void stop(int id) {
        if (!isActive(id)) {
            log.info("Trying to stop not active raid:" + id);
            return;
        }

        InvasionRaid<?> raid;
        synchronized (this) {
            raid = active.remove(id);
        }

        if (raid == null || raid.isFinished()) {
            log.info("Trying to stop null or finished raid:" + id);
            return;
        }

        raid.stop(id);
    }

    public void capture(int id) {
        if (!isActive(id)) {
            log.info("Detecting not active raid.");
            return;
        }

        stop(id);
        broadcastUpdate(getRaidLocation(id));
    }

    public boolean isActive(int id) {
        return active.containsKey(id);
    }

    public InvasionRaid<?> getActiveRaid(int id) {
        return active.get(id);
    }

    public void broadcastUpdate(final RaidLocation raidLocation) {
        World.getInstance().getWorldMap(raidLocation.getWorldId()).getMainWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player player) {
                if (isActive(raidLocation.getId())) {
                    player.getController().updateNearbyQuests();
                    PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402385));
                }
            }
        });
    }
    
    protected void broadcastSpawn(final RaidLocation raidLocation) {
    	World.getInstance().getWorldMap(raidLocation.getWorldId()).getMainWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {
            @Override
            public void visit(Player player) {
                if (isActive(raidLocation.getId())) {
                    player.getController().updateNearbyQuests();
                    PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402385));
                }
            }
        });
    }

    public static InvasionRaidService getInstance() {
        return RaidServiceHolder.INSTANCE;
    }

    private static class RaidServiceHolder {

        private static final InvasionRaidService INSTANCE = new InvasionRaidService();
    }
}