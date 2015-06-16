package playercommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author ginho1
 */
public class cmd_updateskills extends PlayerCommand {

	public cmd_updateskills() {
		super("updateskills");
	}

	@Override
	public void execute(Player player, String... params) {
		SkillLearnService.addMissingSkills(player);
	}
}
