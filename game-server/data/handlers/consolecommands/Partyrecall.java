package consolecommands;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

import admincommands.MoveToMe;

/**
 * This command gets executed from the GM Panel (Shift + F1) or through the builder command packet. Recalls the party of the
 * executing GM to his position, skipping himself. Functionally identical to {@code //movetome <own name> g} with the retail port
 * animation.
 */
public class Partyrecall extends ConsoleCommand {

	public Partyrecall() {
		super("partyrecall", "Recalls all other members of your party to your location.");
	}

	@Override
	public void execute(Player admin, String... params) {
		MoveToMe moveToMe = ChatProcessor.getInstance().getCommand(MoveToMe.class);
		if (moveToMe == null) {
			sendInfo(admin, "The command movetome is not available.");
			return;
		}
		if (admin.getPlayerGroup() == null) {
			sendInfo(admin, "You are not in a party.");
			return;
		}
		moveToMe.execute(admin, TeleportAnimation.FADE_OUT_BEAM, true, admin.getName(), "g");
	}
}
