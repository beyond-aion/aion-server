package com.aionemu.gameserver.services.raid;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.EnhancedObject;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.controllers.attack.AggroList;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.raid.RaidLocation;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.RndArray;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.stats.StatFunctions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javolution.util.FastMap;


/**
 * @author Alcapwnd
 */
public class InvasionRaid<BL extends RaidLocation> {
    private final BL raidLocation;
    private final InvasionRaidDeathListener raidDeathListener = new InvasionRaidDeathListener(this);
    private final AtomicBoolean finished = new AtomicBoolean();
    private boolean started;
    private Npc boss;
    private Npc effect;
    private static final Logger log = LoggerFactory.getLogger(InvasionRaid.class);
    private SpawnTemplate spawn = null;
    private SpawnTemplate eSpawn = null;
    private Map<Integer, SpawnTemplate> spawned = new FastMap<Integer, SpawnTemplate>();;
    Calendar calendar = Calendar.getInstance();
    private int spawnEffect = 702548;
    
    //--------------------Invasion raids--------------------\\
    private int kaldor[] = {234613, 234614, 234615};
    private int levinshor[] = {234609, 234610, 234611, 234612};
    private int danaria[] = {234600, 234601, 234602, 234603, 234604, 234605, 234606, 234607, 234608};
    private int katalam[] = {234593, 234594, 234595, 234596, 234597, 234598, 234599};
    private int reshanta[] = {234588, 234589, 234590, 234591, 234592};
    private int gelkmaros[] = {234585, 234586, 234587};
    private int inggison[] = {234582, 234583, 234584};
    private int beluslan[] = {234579, 234580, 234581};
    private int heiron[] = {234576, 234577, 234578};
    private int brusthonin[] = {234573, 234574, 234575};
    private int theobomos[] = {234570, 234571, 234572};
    private int morheim[] = {234567, 234568, 234569};
    private int eltnen[] = {234564, 234565, 234566};
    private int altgard[] = {234561, 234562, 234563};
    private int verteron[] = {234558, 234559, 234560};
    //--------------------Invasion raids--------------------\\
    
    
    
    public InvasionRaid(BL raidLocation) {
        this.raidLocation = raidLocation;
    }

    public final void start() {
    	
        boolean doubleStart = false;

        synchronized (this) {
            if (started) {
                doubleStart = true;
            } else {
                started = true;
            }
        }

        if (doubleStart) {
            return;
        }

        spawn();
    }

    public final void stop(int id) {
    	synchronized (this) {
    		spawned.remove(id);
    	}
        if (finished.compareAndSet(false, true)) {
            if (getBoss() != null) {
                rmvBossListener();
            }
            if (getEffect() != null) {
            	Npc delNpc = getEffect();
            	delNpc.getController().onDelete();
            }
            onReward();
        }
    }

    protected void spawn() {
        int raidMonster = getNpcId(getRaidLocation().getWorldId());
        getSpawnChance(getRaidLocation().getWorldId(), raidMonster);
        VisibleObject obj = getLocation();
        VisibleObject effect = spawnEffect();
        if (obj == null || effect == null)
            return;
        Npc npc = (Npc) obj;
        Npc npc2 = (Npc) effect;
        setBoss(npc);
        setEffect(npc2);
        addBossListeners();
        addDespawnListener(getRaidLocation().getId());
        spawn = null;
        eSpawn = null;
    }
    
    protected void getSpawnChance(int world, int boss) {
    	try {
            	int chance;
            	
            	if (spawned.containsKey(getRaidLocation().getId())) 
                	return;
            	
            	if (world != getRaidLocation().getWorldId())
            		return;
            	
            	if (calendar.get(Calendar.DAY_OF_MONTH) != 11) {
            		chance = Rnd.get(1, 20);
            	} else {
            		chance = 20;
            	}
            	
            	if (chance == 5 || chance == 10 || chance == 15 || chance == 20 || chance == 1) {
            		spawn = SpawnEngine.addNewSpawn(getRaidLocation().getWorldId(), boss, getRaidLocation().getX(), getRaidLocation().getY(), getRaidLocation().getZ(), (byte) getRaidLocation().getH(), 0);
            		eSpawn = SpawnEngine.addNewSpawn(getRaidLocation().getWorldId(), spawnEffect, getRaidLocation().getX(), getRaidLocation().getY(), getRaidLocation().getZ(), (byte) getRaidLocation().getH(), 0);
            		for (SpawnTemplate s : spawned.values()) {
                    	if (s.getX() == spawn.getX() && s.getY() == spawn.getY()) {
                    		spawn = null;
                    		eSpawn = null;
                    		return;
                    	}
                    }
            		synchronized (this) {
            			spawned.put(getRaidLocation().getId(), spawn);
            		}
            		log.info("Spawned invasion raid: " + getRaidLocation().getId() + "," + getRaidLocation().getWorldId() + "," + getRaidLocation().getX() + "," + getRaidLocation().getY() + "," + getRaidLocation().getZ());
            	} 
        } catch (Exception e) {
            log.error("Error while getting spawn chance for raid boss " + boss);
            e.printStackTrace();
        }
    }
    
    protected void addDespawnListener(final int id) {
    	ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				synchronized (this) {
		    		spawned.remove(id);
		    	}
		        if (finished.compareAndSet(false, true)) {
		            if (getBoss() != null) {
		                rmvBossListener();
		            }
		            if (getEffect() != null) {
		            	Npc delNpc = getEffect();
		            	delNpc.getController().onDelete();
		            }
		        }
				
			}
    		
    	}, 3600 * 1000);//despawn after 1 hr
    }

    protected VisibleObject getLocation() {
        if (spawn == null) {
            return null;
        }

        VisibleObject visibleObject = SpawnEngine.spawnObject(spawn, 1);

        return visibleObject;
    }
    
    protected VisibleObject spawnEffect() {
    	if (eSpawn == null) {
            return null;
        }

        VisibleObject visibleObject = SpawnEngine.spawnObject(eSpawn, 1);

        return visibleObject;
    }
    
    protected void onReward() {
    	Npc npc = getBoss();
    	AggroList list = npc.getAggroList();
    	Collection<AggroInfo> finalList = list.getFinalDamageList(false);
    	AionObject winner = list.getMostDamage();
    	for (AggroInfo info : finalList) {
    		float totalDmg = 0;
            totalDmg += info.getDamage();
    		float percentage = info.getDamage() / totalDmg;
    		AionObject attacker = info.getAttacker();
    		
    		if (attacker instanceof Player) {
    			float rewardGp = 1;
    			float rewardAp = 1;
    			
    			rewardGp *= percentage;
    			rewardAp *= percentage;
    			
    			Player player = (Player) attacker;
    			int calculatedGp = StatFunctions.calculatePvEGpGained(player, npc);
                rewardGp *= calculatedGp;
                int calculatedAp = StatFunctions.calculatePvEApGained(player, npc);
                rewardAp *= calculatedAp;
                
                // Rewards for defeating the invasion monster
    			GloryPointsService.addGp(player, (int) rewardGp);
    			AbyssPointsService.addAp(player, (int) rewardAp);
    			player.getInventory().increaseKinah(1000000, ItemUpdateType.INC_KINAH_COLLECT);
    			
    			if (attacker.equals(winner)) {
    				addChest(player, true);
    			} else {
    				addChest(player, false);
    			}
    		}
    	}
    	
    }
    
    protected void addChest(Player player, boolean isMost) {
    	Npc npc = getBoss();
    	
    	switch (getRaidLocation().getWorldId()) {
    		case 600100000:
    		case 600090000:
    		case 600060000:
    		case 400010000:
    			if (isMost) 
    				ItemService.addItem(player, 188053131, 1);
    			else 
    				ItemService.addItem(player, 188053130, 1);
    			break;
    		case 600050000:
    			ItemService.addItem(player, 188053130, 1);
    			break;
    		case 210050000:
    		case 220070000:
    		case 220040000:
    		case 210040000:
    		case 220050000:
    		case 210060000:
    			if (isMost) 
    				ItemService.addItem(player, 188053128, 1);
    			else 
    				ItemService.addItem(player, 188053127, 1);
    			break;
    		case 220020000:
    		case 210020000:
    			if (isMost) 
    				ItemService.addItem(player, 188053126, 1);
    			else 
    				ItemService.addItem(player, 188053125, 1);
    			break;
    		case 220030000:
    		case 210030000:
    			ItemService.addItem(player, 188053125, 1);
    			break;
    	}
    	
    	if (npc != null) {
    		npc.getController().onDelete();
    	}
    }

    @SuppressWarnings("rawtypes")
	protected void addBossListeners() {
        AbstractAI ai = (AbstractAI) getBoss().getAi2();
        EnhancedObject eo = (EnhancedObject) ai;
        eo.addCallback((Callback) getBossListener());
    }

    @SuppressWarnings("rawtypes")
	protected void rmvBossListener() {
        AbstractAI ai = (AbstractAI) getBoss().getAi2();
        EnhancedObject eo = (EnhancedObject) ai;
        eo.removeCallback((Callback) getBossListener());
    }

    public Npc getBoss() {
        return boss;
    }

    public void setBoss(Npc boss) {
        this.boss = boss;
    }
    
    public Npc getEffect() {
    	return effect;
    }
    
    public void setEffect(Npc effect) {
    	this.effect = effect;
    }

    public InvasionRaidDeathListener getBossListener() {
        return raidDeathListener;
    }

    public boolean isFinished() {
        return finished.get();
    }

    public BL getRaidLocation() {
        return raidLocation;
    }

    public int getId() {
        return raidLocation.getId();
    }

    public int getNpcId(int worldId) {
        switch (worldId) {
        	case 210030000:
        		return RndArray.get(verteron);
        	case 220030000:
        		return RndArray.get(altgard);
        	case 210020000:
        		return RndArray.get(eltnen);
        	case 220020000:
        		return RndArray.get(morheim);
        	case 210060000:
        		return RndArray.get(theobomos);
        	case 220050000:
        		return RndArray.get(brusthonin);
        	case 210040000:
        		return RndArray.get(heiron);
        	case 220040000:
        		return RndArray.get(beluslan);
        	case 210050000:
        		return RndArray.get(inggison);
        	case 220070000:
        		return RndArray.get(gelkmaros);
        	case 400010000:
        		return RndArray.get(reshanta);
        	case 600050000:
        		return RndArray.get(katalam);
        	case 600060000:
        		return RndArray.get(danaria);
        	case 600100000:
        		return RndArray.get(levinshor);
        	case 600090000:
        		return RndArray.get(kaldor);
        	default:
        		return 0;
        }
    }
}