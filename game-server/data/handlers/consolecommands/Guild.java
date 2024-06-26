package consolecommands;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionMemberEx;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GM_SHOW_LEGION_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GM_SHOW_LEGION_MEMBERLIST;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.collections.FixedElementCountSplitList;
import com.aionemu.gameserver.utils.collections.SplitList;
import com.aionemu.gameserver.world.World;

/**
 * @author Yeats
 */
public class Guild extends ConsoleCommand {

	public Guild() {
		super("guild", "Displays info about given player's legion.");
	}

	@Override
	protected void execute(Player admin, String... params) {
		Player target = params.length > 0 ? World.getInstance().getPlayer(params[0]) : null;
		if (target != null) {
			Legion legion = target.getLegion();
			if (target.getLegion() != null) {
				PacketSendUtility.sendPacket(admin, new SM_GM_SHOW_LEGION_INFO(legion));
				List<LegionMemberEx> allMembers = LegionService.getInstance().loadLegionMemberExList(legion, null);
				SplitList<LegionMemberEx> legionMemberSplitList = new FixedElementCountSplitList<>(allMembers, true, 80);
				legionMemberSplitList.forEach(part -> PacketSendUtility.sendPacket(admin,
					new SM_GM_SHOW_LEGION_MEMBERLIST(part, part.isFirst(), part.isLast())));
			}
		}
	}
}
