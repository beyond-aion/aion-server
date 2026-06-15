package consolecommands;

import java.io.File;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.enums.EquipType;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AdminService;
import com.aionemu.gameserver.services.EnchantService;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * Sent in the following cases:<br>
 * - Spawning npcs from the npc tab in the GM Panel (Shift + F1)<br>
 * - Adding items from the item tab in the GM Panel (Shift + F1)<br>
 * - Pressing Ctrl + Shift + Alt while clicking on an item if the console has been activated via "\con_disable_console 0" from the command tab of the
 *   GM Panel (Shift + F1). Left-clicking allows to choose how many items to add, right-clicking always adds one.<br>
 * 
 * @author ginho1, Neon
 */
public class Wish extends ConsoleCommand {

	public Wish() {
		super("wish", "Spawns npcs and adds items.");

		// @formatter:off
		setSyntaxInfo(
			"<npc name> - Spawns the specified npc on your targets position.",
			"<count> <item name> - Adds the specified item to your target.",
			"<item name> <enchant> - Adds the specified item with the enchant level to your target."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		if (params.length == 1) { // spawn npc
			String npcName = params[0];
			File xml = new File("./data/handlers/consolecommands/data/npcs.xml");
			NpcData data = JAXBUtil.deserialize(xml, NpcData.class);
			NpcTemplate npcTemplate = data.getNpcTemplate(npcName);

			if (npcTemplate == null) {
				sendInfo(admin, "There is no template with this name");
				return;
			}
			int npcId = npcTemplate.getTemplateId();
			SpawnTemplate spawn = SpawnEngine.newSpawn(admin.getWorldId(), npcId, admin.getX(), admin.getY(), admin.getZ(), admin.getHeading(), 0);
			VisibleObject visibleObject = SpawnEngine.spawnObject(spawn, admin.getInstanceId());
			if (visibleObject == null) {
				sendInfo(admin, "Spawn id " + npcId + " was not found!");
				return;
			}

			String objectName = visibleObject.getObjectTemplate().getName();
			sendInfo(admin, objectName + " spawned");
		} else { // add item
			if (!(admin.getTarget() instanceof Player player)) {
				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
				return;
			}

			String itemName = params[0];
			long addCount = 1;
			int itemId;
			int enchant = 0;
			try {
				addCount = Integer.parseInt(params[0]);
				itemName = params[1];
			} catch (NumberFormatException e) {
				try {
					enchant = Integer.parseInt(params[1]);
				} catch (NumberFormatException e2) {
				}
			}

			File xml = new File("./data/handlers/consolecommands/data/items.xml");
			ItemData data = JAXBUtil.deserialize(xml, ItemData.class);
			ItemTemplate itemTemplate = data.getItemTemplate(itemName);

			if (itemTemplate != null) {
				itemId = itemTemplate.getTemplateId();
				if (!AdminService.getInstance().canOperate(admin, player, itemId, "command ///wish"))
					return;

				long addedCount;
				if (enchant > 0) {
					Item newItem = ItemFactory.newItem(itemId);

					if (newItem == null)
						return;
					enchant = Math.min(enchant, 255);
					if (newItem.getItemTemplate().getEquipmentType() != EquipType.PLUME) {
						if (newItem.getItemTemplate().canTune() && newItem.getItemTemplate().getMaxEnchantBonus() > 0)
							enchant = Math.min(enchant, newItem.getItemTemplate().getMaxEnchantLevel());
						newItem.setEnchantLevel(enchant);
						if (enchant > newItem.getItemTemplate().getMaxEnchantLevel()) {
							newItem.setAmplified(true);
							if (enchant >= 20)
								newItem.setBuffSkill(EnchantService.getEquipBuff(newItem));
						}
					} else {
						newItem.setTempering(enchant);
					}
					addedCount = addCount - ItemService.addItem(player, newItem);
				} else {
					addedCount = addCount - ItemService.addItem(player, itemId, addCount, true);
				}

				if (addedCount <= 0) {
					sendInfo(admin, "Item couldn't be added");
				} else {
					if (!admin.equals(player)) {
						sendInfo(admin, "You gave " + addedCount + " " + ChatUtil.item(itemId) + " to " + player.getName() + ".");
						sendInfo(player, "You received " + addedCount + " " + ChatUtil.item(itemId) + " from " + admin.getName() + ".");
					}
				}
			}
		}
	}

	@XmlAccessorType(XmlAccessType.NONE)
	@XmlType(namespace = "", name = "ItemTemplate")
	private static class ItemTemplate {

		@XmlAttribute(name = "id", required = true)
		@XmlID
		private String id;

		@XmlAttribute(name = "name")
		private String name;

		public String getName() {
			return name;
		}

		public int getTemplateId() {
			return itemId;
		}

		private int itemId;

		public void setItemId(int itemId) {
			this.itemId = itemId;
		}

		@SuppressWarnings("unused")
		void afterUnmarshal(Unmarshaller u, Object parent) {
			setItemId(Integer.parseInt(id));
		}

	}

	@XmlRootElement(name = "items")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class ItemData {

		@XmlElement(name = "item")
		private List<ItemTemplate> its;

		public ItemTemplate getItemTemplate(String item) {

			for (ItemTemplate it : getData()) {
				if (it.getName().equals(item))
					return it;
			}
			return null;
		}

		protected List<ItemTemplate> getData() {
			return its;
		}
	}

	@XmlAccessorType(XmlAccessType.NONE)
	@XmlType(namespace = "", name = "NpcTemplate")
	private static class NpcTemplate {

		@XmlAttribute(name = "id", required = true)
		@XmlID
		private String id;

		@XmlAttribute(name = "name")
		private String name;

		public String getName() {
			return name;
		}

		public int getTemplateId() {
			return npcId;
		}

		private int npcId;

		public void setNpcId(int npcId) {
			this.npcId = npcId;
		}

		@SuppressWarnings("unused")
		void afterUnmarshal(Unmarshaller u, Object parent) {
			setNpcId(Integer.parseInt(id));
		}

	}

	@XmlRootElement(name = "npcs")
	@XmlAccessorType(XmlAccessType.FIELD)
	private static class NpcData {

		@XmlElement(name = "npc")
		private List<NpcTemplate> its;

		public NpcTemplate getNpcTemplate(String npcName) {

			for (NpcTemplate it : getData()) {
				if (it.getName().equalsIgnoreCase(npcName))
					return it;
			}
			return null;
		}

		protected List<NpcTemplate> getData() {
			return its;
		}
	}
}
