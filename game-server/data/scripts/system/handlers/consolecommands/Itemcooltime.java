package consolecommands;

import java.util.Map.Entry;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemCooldown;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_COOLDOWN;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author Neon
 */
public class Itemcooltime extends ConsoleCommand {

	public Itemcooltime() {
		super("itemcooltime", "Removes cooldowns of all items.");
	}

	@Override
	public void execute(Player player, String... params) {
		if (player.getItemCoolDowns() != null) {
			for (Entry<Integer, ItemCooldown> en : player.getItemCoolDowns().entrySet())
				player.addItemCoolDown(en.getKey(), 0, 0);
			PacketSendUtility.sendPacket(player, new SM_ITEM_COOLDOWN(player.getItemCoolDowns()));
		}
	}
}
