package consolecommands;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

import admincommands.MoveToMe;

/**
 * This command gets executed from the GM Panel (Shift + F1) or through the builder command packet. Recalls the alliance of the
 * executing GM to his position, skipping himself. Functionally identical to {@code //movetome <own name> a} with the retail port
 * animation.
 */
public class Forcerecall extends ConsoleCommand {

	public Forcerecall() {
		super("forcerecall", "Recalls all other members of your alliance to your location.");
	}

	@Override
	public void execute(Player admin, String... params) {
		MoveToMe moveToMe = ChatProcessor.getInstance().getCommand(MoveToMe.class);
		if (moveToMe == null) {
			sendInfo(admin, "The command movetome is not available.");
			return;
		}
		if (admin.getPlayerAlliance() == null) {
			sendInfo(admin, "You are not in an alliance.");
			return;
		}
		moveToMe.execute(admin, TeleportAnimation.FADE_OUT_BEAM, true, admin.getName(), "a");
	}
}
