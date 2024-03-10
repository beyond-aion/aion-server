package admincommands;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemCooldown;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_COOLDOWN;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_COOLDOWN;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

import consolecommands.Clearusercoolt;

/**
 * @author kecimis
 */
public class RemoveCd extends AdminCommand {

	public RemoveCd() {
		super("removecd", "Clears cooldowns for skills, items and instances.");

		// @formatter:off
		setSyntaxInfo(
			" - Removes all item and skill cds of your target.",
			"<instance> <all|worldId> - Removes specified instance cd(s) of your target."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		VisibleObject target = admin.getTarget();
		if (target == null)
			target = admin;

		if (target instanceof Player player) {
			if (params.length == 0) {
				if (player.getSkillCoolDowns() != null) {
					long currentTime = System.currentTimeMillis();
					for (Entry<Integer, Long> en : player.getSkillCoolDowns().entrySet())
						player.setSkillCoolDown(en.getKey(), currentTime);
					PacketSendUtility.sendPacket(player, new SM_SKILL_COOLDOWN(player.getSkillCoolDowns()));
				}

				Map<Integer, ItemCooldown> dummyCds = new HashMap<>(); // 4.8 client ignores reuseTime <= currentTime, but sending old cds + useDelay 0 works
				for (Entry<Integer, ItemCooldown> en : player.getItemCoolDowns().entrySet()) {
					dummyCds.put(en.getKey(), new ItemCooldown(en.getValue().getReuseTime(), 0));
					player.removeItemCoolDown(en.getKey());
				}
				PacketSendUtility.sendPacket(player, new SM_ITEM_COOLDOWN(dummyCds));

				player.getHouseObjectCooldowns().clear();

				if (player.equals(admin)) {
					sendInfo(admin, "Your item and skill cooldowns were removed.");
				} else {
					sendInfo(admin, "You have removed item and skill cooldowns of " + player.getName() + '.');
					sendInfo(player, admin.getName(true) + " removed your item and skill cooldowns.");
				}
			} else if (params[0].equalsIgnoreCase("instance") && params.length >= 2) {
				if (params[1].equalsIgnoreCase("all")) {
					Clearusercoolt.clearAllInstanceCooldowns(admin, player);
				} else {
					int worldId = Integer.parseInt(params[1]);
					if (player.getPortalCooldownList().isPortalUseDisabled(worldId)) {
						player.getPortalCooldownList().removePortalCooldown(worldId);
						player.getPortalCooldownList().sendEntryInfo(worldId);

						String worldName = World.getInstance().getWorldMap(worldId).getName().replace('_', ' ');
						if (player.equals(admin)) {
							sendInfo(admin, "Your instance cooldown for " + worldName + " was removed.");
						} else {
							sendInfo(admin, "You have removed the instance cooldown for " + worldName + " of " + player.getName() + '.');
							sendInfo(player, admin.getName(true) + " removed your instance cooldown for " + worldName);
						}
					} else
						sendInfo(admin, (player.equals(admin) ? "You have" : player.getName() + " has") + " no cooldown on given instance.");

				}
			} else {
				sendInfo(admin);
			}
		} else
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
	}
}
