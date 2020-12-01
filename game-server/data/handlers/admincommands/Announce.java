package admincommands;

import org.apache.commons.lang3.ArrayUtils;

import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Neon
 */
public class Announce extends AdminCommand {

	public Announce() {
		super("announce", "Sends a server-wide notice.");

		setSyntaxInfo(
			"<n|a> <message> - Sends the message either with your <n>ame or <a>nonymously.",
			"<ely|asmo> <message> - Sends an anonymous message to <ely>os or <asmo>dian players."
		);
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length <= 1) {
			sendInfo(admin);
			return;
		}

		String flag = params[0].toLowerCase();
		String[] flags = { "n", "a", "ely", "asmo" };
		if (!ArrayUtils.contains(flags, flag)) {
			sendInfo(admin);
			return;
		}

		StringBuilder sb = new StringBuilder();
		Race allowedRace = null;
		switch (flag) {
			case "n":
				sb.append(ChatUtil.name(admin) + ":");
				break;
			case "a":
				sb.append("Announce:");
				break;
			case "ely":
				sb.append("Elyos:");
				allowedRace = Race.ELYOS;
				break;
			case "asmo":
				sb.append("Asmodians:");
				allowedRace = Race.ASMODIANS;
				break;
		}

		for (int i = 1; i < params.length; i++)
			sb.append(" ").append(params[i]);

		for (Player player : World.getInstance().getAllPlayers())
			if (allowedRace == null || player.getRace() == allowedRace || validateAccess(player))
				PacketSendUtility.sendMessage(player, sb.toString(), ChatType.BRIGHT_YELLOW_CENTER);
	}
}
