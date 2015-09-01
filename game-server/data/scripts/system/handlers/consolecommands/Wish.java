package consolecommands;

import java.io.FileInputStream;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.AdminService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.model.gameobjects.Item;

/**
 * @author ginho1
 */
public class Wish extends ConsoleCommand {

	public Wish() {
		super("wish");
	}

	@Override
	public void execute(Player admin, String... params) {
		if ((params.length < 0) || (params.length < 1)) {
			info(admin, null);
			return;
		}

		final VisibleObject target = admin.getTarget();
		if (target == null) {
			PacketSendUtility.sendMessage(admin, "No target selected.");
			return;
		}

		if (!(target instanceof Player)) {
			PacketSendUtility.sendMessage(admin, "This command can only be used on a player!");
			return;
		}

		final Player player = (Player) target;

		String objName = params[0];
		long objCount = 1;
		int objId = 0;
		int enchant = 0;

		try {
			objCount = Integer.parseInt(params[0]);
			if(objCount > 10)
				objCount = 10;
			objName = params[1];
		}
		catch (NumberFormatException e) {
		}

		
		try {
			enchant = Integer.parseInt(params[1]);
		}
		catch (NumberFormatException e) {
		}

		try {

			JAXBContext jc = JAXBContext.newInstance(StaticData.class);
			Unmarshaller un = jc.createUnmarshaller();
			ItemData data = (ItemData) un.unmarshal(new FileInputStream("./data/scripts/system/handlers/consolecommands/data/items.xml"));

			ItemTemplate itemTemplate = data.getItemTemplate(objName);

			if(itemTemplate != null){
				objId = itemTemplate.getTemplateId();
			}

		}
		catch (Exception e) {
			PacketSendUtility.sendMessage(admin, "Item templates reload failed!" );
			System.out.println(e);
		}

		if(objId > 0) {

			if (!AdminService.getInstance().canOperate(admin, player, objId, "command ///wish"))
				return;

			long count = 1;


			if(enchant > 0){
				Item newItem = ItemFactory.newItem(objId);

				if (newItem == null) {
					return;
				}
				newItem.setEnchantLevel(enchant);
				count = ItemService.addItem(player, newItem);
			}else{
				count = ItemService.addItem(player, objId, objCount);
			}

			if (count == 0) {
				if (admin != player) {
					PacketSendUtility.sendMessage(admin, "You successfully gave " + objCount + " x [item:"
							+ objId + "] to " + player.getName() + ".");
					PacketSendUtility.sendMessage(player, "You successfully received " + objCount + " x [item:"
							+ objId + "] from " + admin.getName() + ".");
				}
				else
					PacketSendUtility.sendMessage(admin, "You successfully received " + objCount + " x [item:"
							+ objId + "]");
			}
			else {
				PacketSendUtility.sendMessage(admin, "Item couldn't be added");
			}
			return;
		}

		try {

			JAXBContext jc = JAXBContext.newInstance(StaticData.class);
			Unmarshaller un = jc.createUnmarshaller();
			NpcData data = (NpcData) un.unmarshal(new FileInputStream("./data/scripts/system/handlers/consolecommands/data/npcs.xml"));

			NpcTemplate npcTemplate = data.getNpcTemplate(objName);

			if(npcTemplate != null){

				System.out.println(npcTemplate.getName());
				objId = npcTemplate.getTemplateId();
			}

		}
		catch (Exception e) {
			PacketSendUtility.sendMessage(admin, "Npc templates reload failed!" );
			System.out.println(e);
		}

		if(objId > 0) {

			float x = admin.getX();
			float y = admin.getY();
			float z = admin.getZ();
			byte heading = admin.getHeading();
			int worldId = admin.getWorldId();

			SpawnTemplate spawn = SpawnEngine.addNewSpawn(worldId, objId, x, y, z, heading, 0);

			if (spawn == null) {
				PacketSendUtility.sendMessage(admin, "There is no template with id " + objId);
				return;
			}

			VisibleObject visibleObject = SpawnEngine.spawnObject(spawn, admin.getInstanceId());

			if (visibleObject == null) {
				PacketSendUtility.sendMessage(admin, "Spawn id " + objId + " was not found!");
			}

			String objectName = visibleObject.getObjectTemplate().getName();
			PacketSendUtility.sendMessage(admin, objectName + " spawned");			
		}
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///wish <item Id | link> <quantity>");
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
	private static class ItemData{

		@XmlElement(name = "item")
		private List<ItemTemplate> its;

		public ItemTemplate getItemTemplate(String item) {

			for (ItemTemplate it : getData()) {
				if(it.getName().equals(item))
					return it;
			}
			return null;
		}

		protected List<ItemTemplate> getData() {
			return its;
		}
	}

	@XmlRootElement(name = "ae_static_data")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class StaticData {
		@XmlElement(name = "items")
		public ItemData itemData;

		@XmlElement(name = "npcs")
		public NpcData npcData;
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
	private static class NpcData{

		@XmlElement(name = "npc")
		private List<NpcTemplate> its;

		public NpcTemplate getNpcTemplate(String npc) {

			for (NpcTemplate it : getData()) {
				if(it.getName().toLowerCase().equals(npc.toLowerCase()))
					return it;
			}
			return null;
		}

		protected List<NpcTemplate> getData() {
			return its;
		}
	}
}