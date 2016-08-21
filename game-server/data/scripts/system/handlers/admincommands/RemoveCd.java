package admincommands;

import java.util.Map.Entry;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemCooldown;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_COOLDOWN;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_COOLDOWN;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author kecimis
 */
public class RemoveCd extends AdminCommand {

	public RemoveCd() {
		super("removecd");
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

				if (player.getItemCoolDowns() != null) {
					for (Entry<Integer, ItemCooldown> en : player.getItemCoolDowns().entrySet())
						player.addItemCoolDown(en.getKey(), 0, 0);
					PacketSendUtility.sendPacket(player, new SM_ITEM_COOLDOWN(player.getItemCoolDowns()));
				}

				if (player.getHouseRegistry() != null && player.getHouseObjectCooldownList().getHouseObjectCooldowns() != null) {
					for (Integer objId : player.getHouseObjectCooldownList().getHouseObjectCooldowns().keySet())
						player.getHouseObjectCooldownList().setHouseObjectCooldown(objId, 0);
				}

				if (player.equals(admin))
					sendInfo(admin, "Your cooldowns were removed");
				else {
					sendInfo(admin, "You have removed cooldowns of player: " + player.getName());
					sendInfo(player, "Your cooldowns were removed by admin");
				}
			} else if (params[0].contains("instance")) {
				if (player.getPortalCooldownList() == null || player.getPortalCooldownList().getPortalCoolDowns() == null)
					return;
				if (params.length >= 2) {
					if (params[1].equalsIgnoreCase("all")) {
						player.getPortalCooldownList().setPortalCoolDowns(null);

						if (player.equals(admin))
							sendInfo(admin, "Your instance cooldowns were removed");
						else {
							sendInfo(admin, "You have removed instance cooldowns of player: " + player.getName());
							sendInfo(player, "Your instance cooldowns were removed by admin");
						}
					} else {
						int worldId = 0;
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
				} else
					sendInfo(admin, "syntax: //removecd instance <all|worldId>");
			}
		} else
			sendInfo(admin, "Only players are allowed as target");
	}
}
