package consolecommands;

import java.io.FileReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.enums.EquipType;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.AdminService;
import com.aionemu.gameserver.services.EnchantService;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

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
			int npcId = findNpcId(npcName);
			if (npcId == 0) {
				sendInfo(admin, "There is no npc with that name.");
				return;
			}
			SpawnTemplate spawn = SpawnEngine.newSpawn(admin.getWorldId(), npcId, admin.getX(), admin.getY(), admin.getZ(), admin.getHeading(), 0);
			VisibleObject visibleObject = SpawnEngine.spawnObject(spawn, admin.getInstanceId());
			if (visibleObject == null) {
				sendInfo(admin, "Spawn id " + npcId + " was not found!");
				return;
			}

			String objectName = visibleObject.getObjectTemplate().getName();
			sendInfo(admin, objectName + " spawned");
		} else { // add item
			Player target = admin.getTarget() instanceof Player targetPlayer ? targetPlayer : admin;
			String itemName = params[0];
			long addCount = 1;
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
			int itemId = findItemId(itemName);
			if (itemId == 0) {
				sendInfo(admin, "There is no item named " + itemName + ".");
				return;
			}
			if (!AdminService.getInstance().canOperate(admin, target, itemId, "command ///wish"))
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
				addedCount = addCount - ItemService.addItem(target, newItem);
			} else {
				addedCount = addCount - ItemService.addItem(target, itemId, addCount, true);
			}

			if (addedCount <= 0) {
				sendInfo(admin, "Item couldn't be added");
			} else if (!admin.equals(target)) {
				sendInfo(admin, "You gave " + addedCount + " " + ChatUtil.item(itemId) + " to " + target.getName() + ".");
				sendInfo(target, "You received " + addedCount + " " + ChatUtil.item(itemId) + " from " + admin.getName() + ".");
			}
		}
	}

	private static int findNpcId(String npcName) {
		return findIdInXml("./data/handlers/consolecommands/data/npcs.xml",  "npc", "name", npcName);
	}

	private static int findItemId(String itemName) {
		return findIdInXml("./data/static_data/items/item_templates.xml",  "item_template", "cName", itemName);
	}

	private static int findIdInXml(String xml, String elementName, String attributeName, String attributeValue) {
		try (FileReader fis = new FileReader(xml)) {
			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(fis);
			try {
				while (reader.hasNext()) {
					if (reader.next() == XMLStreamReader.START_ELEMENT && elementName.equals(reader.getLocalName())
						&& attributeValue.equalsIgnoreCase(reader.getAttributeValue(null, attributeName))) {
						return Integer.parseInt(reader.getAttributeValue(null, "id"));
					}
				}
			} finally {
				reader.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return 0;
	}
}
