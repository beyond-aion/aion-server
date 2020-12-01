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

		if (target instanceof Player) {
			Player player = (Player) target;
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

				if (player.equals(admin))
					sendInfo(admin, "Your cooldowns were removed");
				else {
					sendInfo(admin, "You have removed cooldowns of player: " + player.getName());
					sendInfo(player, "Your cooldowns were removed by admin");
				}
			} else if (params[0].equalsIgnoreCase("instance") && params.length >= 2) {
				if (player.getPortalCooldownList().getPortalCoolDowns() == null) {
					sendInfo(admin, "Nothing to reset");
					return;
				}
				if (params[1].equalsIgnoreCase("all")) {
					player.getPortalCooldownList().setPortalCoolDowns(null);

					if (player.equals(admin))
						sendInfo(admin, "Your instance cooldowns were removed");
					else {
						sendInfo(admin, "You have removed instance cooldowns of player: " + player.getName());
						sendInfo(player, "Your instance cooldowns were removed by admin");
					}
				} else {
					int worldId;
					try {
						worldId = Integer.parseInt(params[1]);
					} catch (NumberFormatException e) {
						sendInfo(admin, "WorldId has to be integer or use \"all\"");
						return;
					}

					if (player.getPortalCooldownList().isPortalUseDisabled(worldId)) {
						player.getPortalCooldownList().addPortalCooldown(worldId, 0);

						if (player.equals(admin))
							sendInfo(admin, "Your instance cooldown worldId: " + worldId + " was removed");
						else {
							sendInfo(admin, "You have removed instance cooldown worldId: " + worldId + " of player: " + player.getName());
							sendInfo(player, "Your instance cooldown worldId: " + worldId + " was removed by admin");
						}
					} else
						sendInfo(admin, "You or your target can enter given instance");

				}
			} else {
				sendInfo(admin);
			}
		} else
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
	}
}
