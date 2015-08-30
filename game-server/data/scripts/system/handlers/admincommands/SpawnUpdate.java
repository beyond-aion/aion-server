package admincommands;

import static ch.lambdaj.Lambda.extractIterator;
import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.flatten;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GATHERABLE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_NPC_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.knownlist.KnownList.DeleteType;

/**
 * @author KID
 * @modified Rolandas
 */
public class SpawnUpdate extends AdminCommand {

	public SpawnUpdate() {
		super("spawnu");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params[0].equalsIgnoreCase("set")) {
			Npc npc = null;
			Gatherable gather = null;
			SpawnTemplate spawn = null;
			
			if (admin.getTarget() != null && admin.getTarget() instanceof Npc)
				npc = (Npc) admin.getTarget();

			if (admin.getTarget() != null && admin.getTarget() instanceof Gatherable) {
				gather = (Gatherable) admin.getTarget();
			}

			if (npc == null && gather == null) {
				PacketSendUtility.sendMessage(admin, "you need to target an Npc or Gatherable type.");
				return;
			}

			if (npc != null)
				spawn = npc.getSpawn();
			else
				spawn = gather.getSpawn();
			
			if (params[1].equalsIgnoreCase("x")) {
				float x;
				if (params.length < 3)
					x = admin.getX();
				else
					x = Float.parseFloat(params[2]);

				if (npc != null) {
					npc.getPosition().setXYZH(x, null, null, null);
					PacketSendUtility.sendPacket(admin, new SM_DELETE(npc, DeleteType.IN_RANGE));
					PacketSendUtility.sendPacket(admin, new SM_NPC_INFO(npc, admin));
					PacketSendUtility.sendMessage(admin, "updated npcs x to " + x + ".");
				}
				else {
					gather.getPosition().setXYZH(x, null, null, null);
					PacketSendUtility.sendPacket(admin, new SM_DELETE(gather, DeleteType.IN_RANGE));
					PacketSendUtility.sendPacket(admin, new SM_GATHERABLE_INFO(gather));
					PacketSendUtility.sendMessage(admin, "updated gatherable x to " + x + ".");
				}

				try {
					DataManager.SPAWNS_DATA2.saveSpawn(admin, (npc != null ? npc : gather), false);
				}
				catch (IOException e) {
					e.printStackTrace();
					PacketSendUtility.sendMessage(admin, "Could not save spawn");
				}
				return;
			}
			
			if (params[1].equalsIgnoreCase("y")) {
				float y;
				if (params.length < 3)
					y = admin.getY();
				else
					y = Float.parseFloat(params[2]);

				if (npc != null) {
					npc.getPosition().setXYZH(null, y, null, null);
					PacketSendUtility.sendPacket(admin, new SM_DELETE(npc, DeleteType.IN_RANGE));
					PacketSendUtility.sendPacket(admin, new SM_NPC_INFO(npc, admin));
					PacketSendUtility.sendMessage(admin, "updated npcs y to " + y + ".");
				}
				else {
					gather.getPosition().setXYZH(null, y, null, null);
					PacketSendUtility.sendPacket(admin, new SM_DELETE(gather, DeleteType.IN_RANGE));
					PacketSendUtility.sendPacket(admin, new SM_GATHERABLE_INFO(gather));
					PacketSendUtility.sendMessage(admin, "updated gatherable Y to " + y + ".");
				}

				try {
					DataManager.SPAWNS_DATA2.saveSpawn(admin, (npc != null ? npc : gather), false);
				}
				catch (IOException e) {
					e.printStackTrace();
					PacketSendUtility.sendMessage(admin, "Could not save spawn");
				}
				return;
			}
			
			if (params[1].equalsIgnoreCase("z")) {
				float z;
				if (params.length < 3)
					z = admin.getZ();
				else
					z = Float.parseFloat(params[2]);

				if (npc != null) {
					npc.getPosition().setZ(z);
					PacketSendUtility.sendPacket(admin, new SM_DELETE(npc, DeleteType.IN_RANGE));
					PacketSendUtility.sendPacket(admin, new SM_NPC_INFO(npc, admin));
					PacketSendUtility.sendMessage(admin, "updated npcs z to " + z + ".");
				}
				else {
					gather.getPosition().setZ(z);
					PacketSendUtility.sendPacket(admin, new SM_DELETE(gather, DeleteType.IN_RANGE));
					PacketSendUtility.sendPacket(admin, new SM_GATHERABLE_INFO(gather));
					PacketSendUtility.sendMessage(admin, "updated gatherable z to " + z + ".");
				}

				try {
					DataManager.SPAWNS_DATA2.saveSpawn(admin, (npc != null ? npc : gather), false);
				}
				catch (IOException e) {
					e.printStackTrace();
					PacketSendUtility.sendMessage(admin, "Could not save spawn");
				}
				return;
			}
			
			if (params[1].equalsIgnoreCase("h")) {
				byte h;
				if (params.length < 3) {
					byte heading = admin.getHeading();
					if (heading > 60)
						heading -= 60;
					else
						heading += 60;
					h = heading;
				}
				else
					h = Byte.parseByte(params[2]);

				if (npc != null) {
					npc.getPosition().setH(h);
					PacketSendUtility.sendPacket(admin, new SM_DELETE(npc, DeleteType.IN_RANGE));
					PacketSendUtility.sendPacket(admin, new SM_NPC_INFO(npc, admin));
					PacketSendUtility.sendMessage(admin, "updated npcs heading to " + h + ".");
				}
				else {
					gather.getPosition().setH(h);
					PacketSendUtility.sendPacket(admin, new SM_DELETE(gather, DeleteType.IN_RANGE));
					PacketSendUtility.sendPacket(admin, new SM_GATHERABLE_INFO(gather));
					PacketSendUtility.sendMessage(admin, "updated gatherable h to " + h + ".");
				}

				try {
					DataManager.SPAWNS_DATA2.saveSpawn(admin, (npc != null ? npc : gather), false);
				}
				catch (IOException e) {
					e.printStackTrace();
					PacketSendUtility.sendMessage(admin, "Could not save spawn");
				}
				return;
			}
			
			if (params[1].equalsIgnoreCase("xyz")) {
				if (npc != null) {
					PacketSendUtility.sendPacket(admin, new SM_DELETE(npc, DeleteType.IN_RANGE));
					npc.getPosition().setXYZH(admin.getX(), null, null, null);
					try {
						DataManager.SPAWNS_DATA2.saveSpawn(admin, npc, false);
						PacketSendUtility.sendPacket(admin, new SM_NPC_INFO(npc, admin));
						npc.getPosition().setXYZH(null, admin.getY(), null, null);
						DataManager.SPAWNS_DATA2.saveSpawn(admin, npc, false);
						PacketSendUtility.sendPacket(admin, new SM_NPC_INFO(npc, admin));
						npc.getPosition().setXYZH(null, null, admin.getZ(), null);
						DataManager.SPAWNS_DATA2.saveSpawn(admin, npc, false);
						PacketSendUtility.sendPacket(admin, new SM_NPC_INFO(npc, admin));
						PacketSendUtility.sendMessage(admin, "updated npcs coordinates to " + admin.getX() + ", " + admin.getY() + ", " + admin.getZ() + ".");
					}
					catch (IOException e) {
						e.printStackTrace();
						PacketSendUtility.sendMessage(admin, "Could not save spawn");
					}
				}
				else {
					PacketSendUtility.sendPacket(admin, new SM_DELETE(gather, DeleteType.IN_RANGE));
					gather.getPosition().setXYZH(admin.getX(), null, null, null);
					try {
						DataManager.SPAWNS_DATA2.saveSpawn(admin, gather, false);
						PacketSendUtility.sendPacket(admin, new SM_GATHERABLE_INFO(gather));
						gather.getPosition().setXYZH(null, admin.getY(), null, null);
						DataManager.SPAWNS_DATA2.saveSpawn(admin, gather, false);
						PacketSendUtility.sendPacket(admin, new SM_GATHERABLE_INFO(gather));
						gather.getPosition().setXYZH(null, null, admin.getZ(), null);
						DataManager.SPAWNS_DATA2.saveSpawn(admin, gather, false);
						PacketSendUtility.sendPacket(admin, new SM_GATHERABLE_INFO(gather));
						PacketSendUtility.sendMessage(admin, "updated gaterables coordinates to " + admin.getX() + ", " + admin.getY() + ", " + admin.getZ() + ".");
					}
					catch (IOException e) {
						e.printStackTrace();
						PacketSendUtility.sendMessage(admin, "Could not save spawn");
					}
				}
				return;
			}
			
			if (params[1].equalsIgnoreCase("w") && npc != null) {
				String walkerId = null;
				if (params.length == 3)
					walkerId = params[2].toUpperCase();
				if (walkerId != null) {
					WalkerTemplate template = DataManager.WALKER_DATA.getWalkerTemplate(walkerId);
					if (template == null) {
						PacketSendUtility.sendMessage(admin, "No such template exists in npc_walker.xml.");
						return;
					}
					List<SpawnGroup2> allSpawns = DataManager.SPAWNS_DATA2.getSpawnsByWorldId(npc.getWorldId());
					List<SpawnTemplate> allSpots = flatten(extractIterator(allSpawns, on(SpawnGroup2.class).getSpawnTemplates()));
					List<SpawnTemplate> sameIds = filter(having(on(SpawnTemplate.class).getWalkerId(), equalTo(walkerId)), allSpots);
					if (sameIds.size() >= template.getPool()) {
						PacketSendUtility.sendMessage(admin, "Can not assign, walker pool reached the limit.");
						return;
					}
				}
				spawn.setWalkerId(walkerId);
				PacketSendUtility.sendPacket(admin, new SM_DELETE(npc, DeleteType.IN_RANGE));
				PacketSendUtility.sendPacket(admin, new SM_NPC_INFO(npc, admin));
				if (walkerId == null)
					PacketSendUtility.sendMessage(admin, "removed npcs walker_id for " + npc.getNpcId() + ".");
				else
					PacketSendUtility.sendMessage(admin, "updated npcs walker_id to " + walkerId + ".");
				try {
					DataManager.SPAWNS_DATA2.saveSpawn(admin, npc, false);
				}
				catch (IOException e) {
					e.printStackTrace();
					PacketSendUtility.sendMessage(admin, "Could not save spawn");
				}
			}
		}
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "<usage //spawnu set (x | y | z | h | w | xyz)");
	}
}
