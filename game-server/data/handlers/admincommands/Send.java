package admincommands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_PACKET;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_PACKET.PacketElementType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * This admin command is used for sending custom packets from server to client.
 * <p/>
 * Sends packets based on xml mappings in folder "./data/packets".<br />
 * Command details: "//send [1]<br />
 * * 1 - packet mappings name.<br />
 * * - 'demo' for file './data/packets/demo.xml'<br />
 * * - 'test' for file './data/packets/test.xml'<br />
 * <p/>
 * Created on: 14.07.2009 13:54:46
 * 
 * @author Aquanox, Neon
 */
public class Send extends AdminCommand {

	private static final String FOLDER = "./data/packets/";
	private static final String SCHEMAFILE = FOLDER + "packets.xsd";

	public Send() {
		super("send", "Sends custom packets.");

		setSyntaxInfo("<file> - Sends packets to your client, based on the ./data/packets/<file>.xml template.");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		String fileName = params[0] + ".xml";
		File file = new File(FOLDER + fileName);

		if (!file.isFile()) {
			sendInfo(admin, "File " + fileName + " not found");
			return;
		}

		Packets packetsTemplate = JAXBUtil.deserialize(file, Packets.class, SCHEMAFILE);
		send(admin, packetsTemplate);
	}

	private void send(Player player, Packets packets) {
		String senderObjectId = String.valueOf(player.getObjectId());
		String targetObjectId = player.getTarget() != null ? String.valueOf(player.getTarget().getObjectId()) : "0";
		long delay = 0;
		for (Packet packetTemplate : packets) {
			SM_CUSTOM_PACKET packet = new SM_CUSTOM_PACKET(packetTemplate.getOpcode());

			for (Part part : packetTemplate.getParts()) {
				PacketElementType byCode = PacketElementType.getByCode(part.getType());

				String value = part.getValue();

				if (value.contains("${objectId}"))
					value = value.replace("${objectId}", senderObjectId);
				if (value.contains("${targetObjectId}"))
					value = value.replace("${targetObjectId}", targetObjectId);

				for (int i = 0; i < part.getRepeatCount(); i++)
					packet.addElement(byCode, value);
			}

			delay += packetTemplate.getDelay();

			ThreadPoolManager.getInstance().schedule(() -> PacketSendUtility.sendPacket(player, packet), delay);

			delay += packets.getDelay();
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "packets")
	private static class Packets implements Iterable<Packet> {

		@XmlElement(name = "packet")
		private List<Packet> packets;

		@XmlAttribute(name = "delay")
		private long delay = -1;

		public long getDelay() {
			return delay;
		}

		@Override
		public Iterator<Packet> iterator() {
			return packets.iterator();
		}

		@Override
		public String toString() {
			return "Packets" + "{delay=" + delay + ", packets=" + packets + '}';
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "packet")
	private static class Packet {

		@XmlElement(name = "part")
		private Collection<Part> parts = new ArrayList<>();

		@XmlAttribute(name = "opcode")
		private String opcode = "-1";

		@XmlAttribute(name = "delay")
		private long delay = 0;

		public int getOpcode() {
			return Integer.decode(opcode);
		}

		public Collection<Part> getParts() {
			return parts;
		}

		public long getDelay() {
			return delay;
		}

		@Override
		public String toString() {
			return "Packet" + "{opcode=" + opcode + ", parts=" + parts + '}';
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "part")
	private static class Part {

		@XmlAttribute(name = "type", required = true)
		private String type = null;

		@XmlAttribute(name = "value", required = true)
		private String value = null;

		@XmlAttribute(name = "repeat", required = true)
		private int repeatCount = 1;

		public char getType() {
			return type.charAt(0);
		}

		public String getValue() {
			return value;
		}

		public int getRepeatCount() {
			return repeatCount;
		}

		@Override
		public String toString() {
			return "Part" + "{type='" + type + '\'' + ", value='" + value + '\'' + ", repeatCount=" + repeatCount + '}';
		}
	}
}
