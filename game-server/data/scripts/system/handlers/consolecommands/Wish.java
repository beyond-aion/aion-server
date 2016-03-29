package consolecommands;

import java.io.FileInputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AdminService;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 * @modified Neon
 */
public class Wish extends ConsoleCommand {

	public Wish() {
		super("wish", "Spawns npcs and adds items.");

		setParamInfo(
			"<npc name> - Spawns the specified npc on your targets position.",
			"<count> <item name> - Adds the specified item to your target.",
			"<item name> <enchant> - Adds the specified item with the enchant level to your target."
		);
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		if (params.length == 1) { // spawn npc
			String npcName = params[0];
			try {
				JAXBContext jc = JAXBContext.newInstance(NpcData.class);
				Unmarshaller un = jc.createUnmarshaller();
				NpcData data = (NpcData) un.unmarshal(new FileInputStream("./data/scripts/system/handlers/consolecommands/data/npcs.xml"));

				NpcTemplate npcTemplate = data.getNpcTemplate(npcName);

				if (npcTemplate != null) {
					int npcId = npcTemplate.getTemplateId();
					SpawnTemplate spawn = SpawnEngine.addNewSpawn(admin.getWorldId(), npcId, admin.getX(), admin.getY(), admin.getZ(), admin.getHeading(), 0);
					if (spawn == null) {
						sendInfo(admin, "There is no template with id " + npcId);
						return;
					}

					VisibleObject visibleObject = SpawnEngine.spawnObject(spawn, admin.getInstanceId());
					if (visibleObject == null) {
						sendInfo(admin, "Spawn id " + npcId + " was not found!");
						return;
					}

					String objectName = visibleObject.getObjectTemplate().getName();
					sendInfo(admin, objectName + " spawned");
				}
			} catch (Exception e) {
				sendInfo(admin, "Npc templates reload failed!");
				System.out.println(e);
			}
		} else { // add item
			if (!(admin.getTarget() instanceof Player)) {
				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
				return;
			}

			Player target = (Player) admin.getTarget();
			String itemName = params[0];
			long addCount = 1;
			int itemId = 0;
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

			try {
				JAXBContext jc = JAXBContext.newInstance(ItemData.class);
				Unmarshaller un = jc.createUnmarshaller();
				ItemData data = (ItemData) un.unmarshal(new FileInputStream("./data/scripts/system/handlers/consolecommands/data/items.xml"));
				ItemTemplate itemTemplate = data.getItemTemplate(itemName);

				if (itemTemplate != null) {
					itemId = itemTemplate.getTemplateId();
					if (!AdminService.getInstance().canOperate(admin, target, itemId, "command ///wish"))
						return;

					long addedCount = 0;
					if (enchant > 0) {
						Item newItem = ItemFactory.newItem(itemId);

						if (newItem == null)
							return;
						newItem.setEnchantLevel(enchant);
						addedCount = addCount - ItemService.addItem(target, newItem);
					} else {
						addedCount = addCount - ItemService.addItem(target, itemId, addCount, true);
					}

					if (addedCount <= 0) {
						sendInfo(admin, "Item couldn't be added");
					} else {
						if (!admin.equals(target)) {
							sendInfo(admin, "You gave " + addedCount + " " + ChatUtil.item(itemId) + " to " + target.getName() + ".");
							sendInfo(target, "You received " + addedCount + " " + ChatUtil.item(itemId) + " from " + admin.getName() + ".");
						}
					}
				}
			} catch (Exception e) {
				sendInfo(admin, "Item templates reload failed!");
				System.out.println(e);
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

		public NpcTemplate getNpcTemplate(String npc) {

			for (NpcTemplate it : getData()) {
				if (it.getName().toLowerCase().equals(npc.toLowerCase()))
					return it;
			}
			return null;
		}

		protected List<NpcTemplate> getData() {
			return its;
		}
	}
}
