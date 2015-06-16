package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.toypet.PetAdoptionService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer
 */
public class Pet extends AdminCommand {

	public Pet() {
		super("pet");
	}

	@Override
	public void execute(Player player, String... params) {
		String command = params[0];
		if ("add".equals(command)) {
			int petId = Integer.parseInt(params[1]);
			String name = params[2];
			PetAdoptionService.addPet(player, petId, name, 0, 0);
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //pet <add [petid name]>");
	}
}
