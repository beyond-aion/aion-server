package consolecommands;

import java.util.HashMap;
import java.util.Map;
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
			Map<Integer, ItemCooldown> dummyCds = new HashMap<>(); // 4.8 client ignores reuseTime <= currentTime, but sending old cds + useDelay 0 works
			for (Entry<Integer, ItemCooldown> en : player.getItemCoolDowns().entrySet()) {
				dummyCds.put(en.getKey(), new ItemCooldown(en.getValue().getReuseTime(), 0));
				player.removeItemCoolDown(en.getKey());
			}
			PacketSendUtility.sendPacket(player, new SM_ITEM_COOLDOWN(dummyCds));
		}
	}
}
