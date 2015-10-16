package playercommands;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerChatService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Shepper
 * @modified bobobear, Neon
 */
public class Faction extends PlayerCommand {

	public Faction() {
		super("faction", "Faction chat.");

		String priceInfo = CustomConfig.FACTION_USE_PRICE > 0 ? " Price: " + CustomConfig.FACTION_USE_PRICE + " Kinah." : "";
		setParamInfo("<message> - sends the message to all players of your faction." + priceInfo);
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

		if (player.isInPrison() || player.isGagged() || PlayerChatService.isFlooding(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHAT_DISABLED);
			return;
		}

		if (CustomConfig.FACTION_USE_PRICE > 0) {
			if (CustomConfig.FACTION_USE_PRICE > player.getInventory().getKinah()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY);
				return;
			}
			player.getInventory().decreaseKinah(CustomConfig.FACTION_USE_PRICE);
		}

		String senderName = player.getName(player.isGM());
		String message = CustomConfig.FACTION_CHAT_CHANNEL ? StringUtils.join(params, " ") : senderName + ": " + StringUtils.join(params, " ");
		ChatType channel = CustomConfig.FACTION_CHAT_CHANNEL ? ChatType.CH1 : ChatType.BRIGHT_YELLOW;

		World.getInstance().doOnAllPlayers(new Visitor<Player>() {

			@Override
			public void visit(Player listener) {
				// GMs can read both factions (but only write to their own)
				if (listener.getRace() == player.getRace() || listener.isGM()) {
					String name = listener.isGM() ? (player.getRace() == Race.ASMODIANS ? "(A) " : "(E) ") + senderName : senderName;
					PacketSendUtility.sendPacket(listener, new SM_MESSAGE(player.getObjectId(), name, message, channel));
				}
			}
		});

		if (LoggingConfig.LOG_FACTION) {
			PlayerChatService.chatLogging(player, ChatType.NORMAL, "[Faction Msg] " + message);
		}
	}
}
