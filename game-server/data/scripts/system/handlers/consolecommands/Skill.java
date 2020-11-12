package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GM_SHOW_PLAYER_SKILLS;
import com.aionemu.gameserver.network.aion.skillinfo.SkillEntryWriter;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.utils.collections.DynamicServerPacketBodySplitList;
import com.aionemu.gameserver.utils.collections.SplitList;
import com.aionemu.gameserver.world.World;

/**
 * @author Yeats
 */
public class Skill extends ConsoleCommand {

	public Skill() {
		super("skill");
	}

	@Override
	protected void execute(Player admin, String... params) {
		Player target = null;
		if (params.length > 0)
			target = World.getInstance().getPlayer(params[0]);
		if (target == null && admin.getTarget() instanceof Player player)
			target = player;
		if (target != null) {
			SplitList<PlayerSkillEntry> skillEntrySplitList = new DynamicServerPacketBodySplitList<>(target.getSkillList().getAllSkills(), false,
				SM_GM_SHOW_PLAYER_SKILLS.STATIC_BODY_SIZE, SkillEntryWriter.DYNAMIC_BODY_PART_SIZE_CALCULATOR);
			skillEntrySplitList.forEach(part -> PacketSendUtility.sendPacket(admin, new SM_GM_SHOW_PLAYER_SKILLS(part)));
		}
	}
}
