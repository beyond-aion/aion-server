package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Luno
 * @modified Bobobear, Neon
 */
public class Delete extends AdminCommand {

	public Delete() {
		super("delete", "Removes a spawn from world.");
	}

	@Override
	public void execute(Player admin, String... params) {
		VisibleObject target = admin.getTarget();
		if (!(target instanceof Npc) && !(target instanceof Gatherable)) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}

		SpawnTemplate spawn = target.getSpawn();
		if (spawn.hasPool()) {
			sendInfo(admin, "Can't delete pooled spawn template.");
			return;
		}

		if (!spawn.getClass().equals(SpawnTemplate.class)) {
			sendInfo(admin, "Can't delete special spawns (spawn type: " + spawn.getClass().getSimpleName().replace("Template", "") + ").");
			return;
		}

		if (target instanceof Creature)
			((Creature) target).getController().cancelTask(TaskId.RESPAWN);

		target.getController().delete();
		sendInfo(admin, "Spawn removed.");

		if (!DataManager.SPAWNS_DATA.saveSpawn(target, true)) {
			sendInfo(admin, "Could not save deleted spawn. Maybe it's a special or temporary spawn (siege, base, invasion, ...) which cannot be altered.");
			return;
		}
	}
}
