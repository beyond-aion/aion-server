package com.aionemu.gameserver.model.templates.event;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.SpawnsData;
import com.aionemu.gameserver.model.EventType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.Guides.GuideTemplate;
import com.aionemu.gameserver.model.templates.spawns.Spawn;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnMap;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_VERSION_CHECK;
import com.aionemu.gameserver.services.EventService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.time.ServerTime;
import com.aionemu.gameserver.world.World;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventTemplate")
public class EventTemplate {

	private static Logger log = LoggerFactory.getLogger(EventTemplate.class);

	@XmlElement(name = "event_drops", required = false)
	private EventDrops eventDrops;

	@XmlElement(name = "quests", required = false)
	private EventQuestList quests;

	@XmlElement(name = "spawns", required = false)
	private SpawnsData spawns;

	@XmlElement(name = "inventory_drop", required = false)
	private InventoryDrop inventoryDrop;

	@XmlList
	@XmlElement(name = "surveys", required = false)
	private List<String> surveys;

	@XmlAttribute(name = "name", required = true)
	private String name;

	@XmlAttribute(name = "start", required = true)
	@XmlJavaTypeAdapter(ServerTime.XmlAdapter.class)
	private ZonedDateTime startDate;

	@XmlAttribute(name = "end", required = true)
	@XmlJavaTypeAdapter(ServerTime.XmlAdapter.class)
	private ZonedDateTime endDate;

	@XmlAttribute(name = "theme", required = false)
	private String theme;

	@XmlTransient
	private List<VisibleObject> spawnedObjects;

	@XmlTransient
	private Future<?> invDropTask = null;

	public String getName() {
		return name;
	}

	public EventDrops getEventDrops() {
		return eventDrops;
	}

	public SpawnsData getSpawns() {
		return spawns;
	}

	public ZonedDateTime getStartDate() {
		return startDate;
	}

	public ZonedDateTime getEndDate() {
		return endDate;
	}

	public List<Integer> getStartableQuests() {
		if (quests == null)
			return new ArrayList<>();
		return quests.getStartableQuests();
	}

	public List<Integer> getMaintainableQuests() {
		if (quests == null)
			return new ArrayList<>();
		return quests.getMaintainQuests();
	}

	public boolean isActive() {
		ZonedDateTime now = ServerTime.now();
		return getStartDate().isBefore(now) && getEndDate().isAfter(now);
	}

	public boolean isExpired() {
		return !isActive();
	}

	@XmlTransient
	volatile boolean isStarted = false;

	public void setStarted() {
		isStarted = true;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public void start() {
		if (isStarted)
			return;

		if (spawns != null && spawns.size() > 0) { // TODO limit pooled spawns (refactor SpawnEngine to use its methods)
			for (SpawnMap map : spawns.getTemplates()) {
				DataManager.SPAWNS_DATA.addNewSpawnMap(map);
				Collection<Integer> instanceIds = World.getInstance().getWorldMap(map.getMapId()).getAvailableInstanceIds();
				for (Integer instanceId : instanceIds) {
					int spawnCount = 0;
					for (Spawn spawn : map.getSpawns()) {
						spawn.setEventTemplate(this);
						SpawnGroup group = new SpawnGroup(map.getMapId(), spawn);
						if (group.hasPool() && SpawnEngine.checkPool(group)) {
							group.resetTemplates(instanceId);
							for (int i = 0; i < group.getPool(); i++) {
								SpawnTemplate t = group.getRndTemplate(instanceId);
								if (t == null)
									break;
								SpawnEngine.spawnObject(t, instanceId);
								spawnCount++;
							}
						} else {
							for (SpawnTemplate t : group.getSpawnTemplates()) {
								SpawnEngine.spawnObject(t, instanceId);
								spawnCount++;
							}
						}
					}
					log.info("Spawned event objects in " + map.getMapId() + " [" + instanceId + "]: " + spawnCount + " (" + getName() + ")");
				}
			}
			DataManager.SPAWNS_DATA.afterUnmarshal(null, null);
			DataManager.SPAWNS_DATA.clearTemplates();
		}

		if (inventoryDrop != null) {
			invDropTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					World.getInstance().forEachPlayer(new Consumer<Player>() {

						@Override
						public void accept(Player player) {
							if (player.isOnline() && player.getCommonData().getLevel() >= inventoryDrop.getStartLevel())
								// TODO: check the exact type in retail
								ItemService.addItem(player, inventoryDrop.getDropItem(), inventoryDrop.getCount(), true,
									new ItemUpdatePredicate(ItemAddType.ITEM_COLLECT, ItemUpdateType.INC_CASH_ITEM));
						}
					});
				}
			}, 0, inventoryDrop.getInterval() * 60000);
		}

		if (surveys != null) {
			for (String survey : surveys) {
				GuideTemplate template = DataManager.GUIDE_HTML_DATA.getTemplateByTitle(survey);
				if (template != null)
					template.setActivated(true);
			}
		}

		isStarted = true;

		if (theme != null) // show city decoration (visible after teleport)
			PacketSendUtility.broadcastToWorld(new SM_VERSION_CHECK(EventType.getEventType(theme)));
	}

	public void stop() {
		if (!isStarted)
			return;

		if (spawnedObjects != null) {
			for (VisibleObject o : spawnedObjects) {
				if (o instanceof Creature)
					((Creature) o).getController().cancelTask(TaskId.RESPAWN);
				o.getController().delete();
			}
			DataManager.SPAWNS_DATA.removeEventSpawnObjects(spawnedObjects);
			log.info("Deleted " + spawnedObjects.size() + " event objects (" + getName() + ")");
			spawnedObjects = null;
		}

		if (invDropTask != null) {
			invDropTask.cancel(false);
			invDropTask = null;
		}

		if (surveys != null) {
			for (String survey : surveys) {
				GuideTemplate template = DataManager.GUIDE_HTML_DATA.getTemplateByTitle(survey);
				if (template != null)
					template.setActivated(false);
			}
		}

		isStarted = false;

		if (theme != null && EventService.getInstance().getEventType() == EventType.NONE) // remove city decoration (visible after teleport)
			PacketSendUtility.broadcastToWorld(new SM_VERSION_CHECK(EventType.NONE));
	}

	public void addSpawnedObject(VisibleObject object) {
		if (spawnedObjects == null)
			spawnedObjects = new CopyOnWriteArrayList<>();
		spawnedObjects.add(object);
	}

	public void removeSpawnedObject(VisibleObject object) {
		if (spawnedObjects != null)
			spawnedObjects.remove(object);
	}

	/**
	 * @return the theme name
	 */
	public String getTheme() {
		return theme;
	}

}
