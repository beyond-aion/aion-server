package consolecommands;

import java.io.File;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * @author ginho1
 */
public class Teleport_to_named extends ConsoleCommand {

	public Teleport_to_named() {
		super("teleport_to_named");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			info(admin, null);
			return;
		}

		String npcName = params[0];

		File xml = new File("./data/handlers/consolecommands/data/npcs.xml");
		NpcData data = JAXBUtil.deserialize(xml, NpcData.class);
		NpcTemplate npcTemplate = data.getNpcTemplate(npcName);

		if (npcTemplate != null) {
			PacketSendUtility.sendMessage(admin, "Teleporting to Npc: " + npcTemplate.getTemplateId());
			TeleportService.teleportToNpc(admin, npcTemplate.getTemplateId());
		}
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///teleport_to_named <named name>");
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
