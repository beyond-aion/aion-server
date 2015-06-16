package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer
 */
public class GiveMissingSkills extends AdminCommand {

	public GiveMissingSkills() {
		super("givemissingskills");
	}

	@Override
	public void execute(Player player, String... params) {
		SkillLearnService.addMissingSkills(player);
	}

	@Override
	public void onFail(Player player, String message) {
		// TODO Auto-generated method stub
	}
}
