package consolecommands;

import java.io.File;
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
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ginho1
 */
public class Teleport_to_named extends ConsoleCommand {

	public Teleport_to_named() {
		super("teleport_to_named");
	}

	@Override
	public void execute(Player admin, String... params) {
		if ((params.length < 0) || (params.length < 1)) {
			info(admin, null);
			return;
		}

		String npcName = params[0];
		int npcId = 0;

		try {

			JAXBContext jc = JAXBContext.newInstance(StaticData.class);
			Unmarshaller un = jc.createUnmarshaller();
			NpcData data = (NpcData) un.unmarshal(new File("./data/scripts/system/handlers/consolecommands/data/npcs.xml"));

			NpcTemplate npcTemplate = data.getNpcTemplate(npcName);

			if(npcTemplate != null){

				System.out.println(npcTemplate.getName());
				npcId = npcTemplate.getTemplateId();
			}

		}
		catch (Exception e) {
			PacketSendUtility.sendMessage(admin, "Npc templates reload failed!" );
			System.out.println(e);
		}

		if (npcId > 0) {
			PacketSendUtility.sendMessage(admin, "Teleporting to Npc: " + npcId);
			TeleportService2.teleportToNpc(admin, npcId);
		}
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///teleport_to_named <named name>");
	}

	@XmlRootElement(name = "ae_static_data")
	@XmlAccessorType(XmlAccessType.NONE)
	private static class StaticData {
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