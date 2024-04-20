package playercommands;

import java.util.function.Consumer;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.services.player.PlayerChatService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Shepper, bobobear, Neon
 */
public class Faction extends PlayerCommand {

	public Faction() {
		super("faction", "Faction chat.");

		String priceInfo = CustomConfig.FACTION_USE_PRICE > 0 ? " Price: " + CustomConfig.FACTION_USE_PRICE + " Kinah." : "";
		setSyntaxInfo("<message> - Sends the message to all players of your faction." + priceInfo);
	}

	@Override
	public void execute(Player player, String... params) {
		if (!CustomConfig.FACTION_CMD_CHANNEL) {
			sendInfo(player, "The faction channel is disabled.");
			return;
		}

		if (params == null || params.length < 1) {
			sendInfo(player);
			return;
		}

		if (!PlayerRestrictions.canChat(player))
			return;

		if (CustomConfig.FACTION_USE_PRICE > 0) {
			if (CustomConfig.FACTION_USE_PRICE > player.getInventory().getKinah()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY());
				return;
			}
			player.getInventory().decreaseKinah(CustomConfig.FACTION_USE_PRICE);
		}

		String senderName = player.getName(player.isStaff());
		String message = CustomConfig.FACTION_CHAT_CHANNEL ? String.join(" ", params) : senderName + ": " + String.join(" ", params);
		ChatType channel = CustomConfig.FACTION_CHAT_CHANNEL ? ChatType.CH1 : ChatType.BRIGHT_YELLOW;

		PlayerChatService.logMessage(player, ChatType.NORMAL, "[Faction Msg] " + message);

		World.getInstance().forEachPlayer(new Consumer<Player>() {

			@Override
			public void accept(Player listener) {
				// GMs can read both factions (but only write to their own)
				if (listener.getRace() == player.getRace() || listener.isStaff()) {
					String name = listener.isStaff() ? (player.getRace() == Race.ASMODIANS ? "(A) " : "(E) ") + senderName : senderName;
					PacketSendUtility.sendPacket(listener, new SM_MESSAGE(player.getObjectId(), name, message, channel));
				}
			}
		});
	}
}
