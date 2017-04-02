package admincommands;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.assemblednpc.AssembledNpc;
import com.aionemu.gameserver.model.assemblednpc.AssembledNpcPart;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.assemblednpc.AssembledNpcTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_NPC_ASSEMBLER;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author xTz
 */
public class SpawnAssembledNpc extends AdminCommand {

	public SpawnAssembledNpc() {
		super("spawnAssembledNpc");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length != 1) {
			info(player, null);
			return;
		}
		int spawnId = 0;
		try {
			spawnId = Integer.parseInt(params[0]);
		} catch (Exception e) {
			info(player, null);
			return;
		}

		AssembledNpcTemplate template = DataManager.ASSEMBLED_NPC_DATA.getAssembledNpcTemplate(spawnId);
		if (template == null) {
			PacketSendUtility.sendMessage(player, "This spawnId is wrong.");
			return;
		}
		List<AssembledNpcPart> assembledParts = new ArrayList<>();
		for (AssembledNpcTemplate.AssembledNpcPartTemplate npcPart : template.getAssembledNpcPartTemplates()) {
			assembledParts.add(new AssembledNpcPart(IDFactory.getInstance().nextId(), npcPart));
		}
		AssembledNpc npc = new AssembledNpc(template.getRouteId(), template.getMapId(), template.getLiveTime(), assembledParts);
		PacketSendUtility.broadcastToWorld(new SM_NPC_ASSEMBLER(npc));
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //spawnAssembledNpc <spawnId>");
	}
}
