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
import com.aionemu.gameserver.utils.collections.ListSplitter;
import com.aionemu.gameserver.world.World;

/**
 * @author Yeats.
 */
public class Guild extends ConsoleCommand {

	public Guild() {
		super("guild");
	}

	@Override
	protected void execute(Player admin, String... params) {

		Player target = null;
		if (params.length > 0) {
			target = World.getInstance().findPlayer(params[0]);
		}
		if (target == null && admin.getTarget() instanceof Player) {
			target = (Player) admin.getTarget();
		}
		if (target != null) {
			Legion legion = target.getLegion();
			if (target.getLegion() != null) {
				PacketSendUtility.sendPacket(admin, new SM_GM_SHOW_LEGION_INFO(legion));
				List<LegionMemberEx> totalMembers = LegionService.getInstance().loadLegionMemberExList(legion, null);
				// Send member list to player
				ListSplitter<LegionMemberEx> splits = new ListSplitter<>(totalMembers, 80, true);
				// Send the member list to the new legion member
				while (splits.hasMore()) {
					boolean isSplit = false;
					boolean isFirst = splits.isFirst();
					List<LegionMemberEx> curentMembers = splits.getNext();
					if (isFirst && curentMembers.size() < totalMembers.size()) {
						isSplit = true;
					}
					PacketSendUtility.sendPacket(admin, new SM_GM_SHOW_LEGION_MEMBERLIST(curentMembers, isSplit, isFirst));
				}
			}
		}
	}
}
