package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Gatherable;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Luno, Bobobear, Neon
 */
public class Delete extends AdminCommand {

	public Delete() {
		super("delete", "Removes a spawn from world.");

		// @formatter:off
		setSyntaxInfo(
			" - Deletes the object you are targeting.",
			"<range> - Deletes all objects around you in given radius in meters."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			if (admin.getTarget() == null)
				sendInfo(admin);
			else
				delete(admin, admin.getTarget(), true);
		} else {
			int[] count = { 0 };
			float range = Float.parseFloat(params[0]);
			admin.getKnownList().forEachObject(object -> {
				if (PositionUtil.isInRange(admin, object, range) && delete(admin, object, false))
					count[0]++;
			});
			sendInfo(admin, "Deleted " + count[0] + (count[0] == 1 ? " object." : " objects."));
		}
	}

	private boolean delete(Player admin, VisibleObject target, boolean notifyOnFail) {
		if (!(target instanceof Npc) && !(target instanceof Gatherable) && !(target instanceof HouseObject)) {
			if (notifyOnFail)
				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return false;
		}

		SpawnTemplate spawn = target.getSpawn();
		if (spawn != null) { // house objects have no spawn template
			if (spawn.hasPool()) {
				if (notifyOnFail)
					sendInfo(admin, "Can't delete pooled spawn template.");
				return false;
			}

			if (!spawn.getClass().equals(SpawnTemplate.class)) {
				if (notifyOnFail)
					sendInfo(admin, "Can't delete special spawns (spawn type: " + spawn.getClass().getSimpleName().replace("Template", "") + ").");
				return false;
			}
		}

		target.getController().delete();
		if (DataManager.SPAWNS_DATA.saveSpawn(target, true))
			sendInfo(admin, "Spawn removed permanently. " + target.getClass().getSimpleName() + " will not spawn on server start anymore.");
		return true;
	}
}
