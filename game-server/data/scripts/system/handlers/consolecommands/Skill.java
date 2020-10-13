package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GM_SHOW_PLAYER_SKILLS;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.collections.ListSplitter;
import com.aionemu.gameserver.world.World;

/**
 * Created by Yeats on 09.05.2016.
 */
public class Skill extends ConsoleCommand {

	public Skill() {
		super("skill");
	}

	@Override
	protected void execute(Player admin, String... params) {
		Player target = null;
		if (params.length > 0) {
			target = World.getInstance().getPlayer(params[0]);
		}
		if (target == null && admin.getTarget() instanceof Player) {
			target = (Player) admin.getTarget();
		}
		if (target != null) {
			ListSplitter<PlayerSkillEntry> splitter = new ListSplitter<>(target.getSkillList().getAllSkills(), 700, false); // split every 700 (729 worked, 745 crashed)
			while (splitter.hasMore()) {
				PacketSendUtility.sendPacket(admin, new SM_GM_SHOW_PLAYER_SKILLS(splitter.getNext()));
			}
		}
	}
}
