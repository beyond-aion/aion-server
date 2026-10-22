package admincommands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemCooldown;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_COOLDOWN;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_COOLDOWN;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

import consolecommands.Clearusercoolt;

/**
 * @author kecimis
 */
public class RemoveCd extends AdminCommand {

	public RemoveCd() {
		super("removecd", "Clears cooldowns for skills, items and instances.", """
			 - Removes all item and skill cooldowns of your target.
			instance all - Removes all instance cooldowns of your target.
			instance <world ID> - Removes the specified instance cooldown of your target.
			Note: Any actions default to your character, if no player is targeted.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		Player target = admin.getTarget() instanceof Player p ? p : admin;
		if (params.length == 0) {
			if (target.getSkillCoolDowns() != null) {
				long nowMillis = System.currentTimeMillis();
				List<Integer> cooldownIds = target.getSkillCoolDowns().entrySet().stream().filter(e -> e.getValue() > nowMillis).map(Entry::getKey).toList();
				PacketSendUtility.sendPacket(target, new SM_SKILL_COOLDOWN(target, cooldownIds));
				target.getSkillCoolDowns().clear();
			}
			removeItemCooldowns(target);
			target.getHouseObjectCooldowns().clear();
			if (target.equals(admin)) {
				sendInfo(admin, "Your item and skill cooldowns were removed.");
			} else {
				sendInfo(admin, "You have removed item and skill cooldowns of " + name(target) + '.');
				sendInfo(target, name(admin) + " removed your item and skill cooldowns.");
			}
		} else if (params[0].equalsIgnoreCase("instance") && params.length >= 2) {
			if (params[1].equalsIgnoreCase("all")) {
				Clearusercoolt.clearAllInstanceCooldowns(admin, target);
			} else {
				int worldId = Integer.parseInt(params[1]);
				if (target.getPortalCooldownList().isPortalUseDisabled(worldId)) {
					target.getPortalCooldownList().removePortalCooldown(worldId);
					target.getPortalCooldownList().sendEntryInfo(worldId);
					if (target.equals(admin)) {
						sendInfo(admin, "Your instance cooldown for " + worldName(worldId) + " was removed.");
					} else {
						sendInfo(admin, "You have removed the instance cooldown for " + worldName(worldId) + " of " + name(target) + '.');
						sendInfo(target, name(admin) + " removed your instance cooldown for " + worldName(worldId) + ".");
					}
				} else
					sendInfo(admin, (target.equals(admin) ? "You have" : name(target) + " has") + " no cooldown on " + worldName(worldId) + ".");
			}
		} else {
			sendInfo(admin);
		}
	}

	public static void removeItemCooldowns(Player player) {
		Map<Integer, ItemCooldown> dummyCds = new HashMap<>(); // 4.8 client ignores reuseTime <= currentTime, but sending old cds + useDelay 0 works
		for (Entry<Integer, ItemCooldown> en : player.getItemCoolDowns().entrySet()) {
			dummyCds.put(en.getKey(), new ItemCooldown(en.getValue().getReuseTime(), 0));
			player.removeItemCoolDown(en.getKey());
		}
		PacketSendUtility.sendPacket(player, new SM_ITEM_COOLDOWN(dummyCds));
	}
}
