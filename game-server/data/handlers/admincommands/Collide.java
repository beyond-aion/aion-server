package admincommands;

import java.util.Iterator;

import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResult;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Rolandas
 */
public class Collide extends AdminCommand {

	public Collide() {
		super("collide", "Geo debugging tool.");

		// @formatter:off
		setSyntaxInfo(
			" - Lists collisions between your target and the ground.",
			"me - Lists collisions between you and your target."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		boolean isMe = false;
		if (params.length > 0 && !(isMe = "me".equalsIgnoreCase(params[0]))) {
			sendInfo(admin);
			return;
		}
		VisibleObject target = admin.getTarget();
		if (target == null) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}

		final byte intentions = CollisionIntention.PHYSICAL.getId();
		float x = target.getX();
		float y = target.getY();
		float z = target.getZ();
		float targetX, targetY, targetZ;

		if (!isMe) {
			targetX = x;
			targetY = y;
			targetZ = z - 10;
		} else {
			targetX = admin.getX();
			targetY = admin.getY();
			targetZ = admin.getZ() + admin.getObjectTemplate().getBoundRadius().getUpper() / 6;
			sendInfo(admin, "From target towards you:");
		}

		sendInfo(admin, "Target: X=" + x + "; Y=" + y + "; Z=" + z);

		CollisionResults results = GeoService.getInstance().getCollisions(target, targetX, targetY, targetZ, intentions, null);
		CollisionResult closest = results.getClosestCollision();

		if (results.size() == 0) {
			sendInfo(admin, "No collisions found.");
			closest = null;
		} else {
			listCollisions(admin, results, closest);
		}

		CollisionResult closestOpposite = null;

		if (isMe) {
			sendInfo(admin, "From you towards your target:");
			sendInfo(admin, "Admin: X=" + admin.getX() + "; Y=" + admin.getY() + "; Z=" + admin.getZ());

			results = GeoService.getInstance().getCollisions(admin, target.getX(), target.getY(),
				target.getZ() + target.getObjectTemplate().getBoundRadius().getUpper() / 2, intentions, null);
			closestOpposite = results.getClosestCollision();

			if (results.size() == 0) {
				sendInfo(admin, "No collisions found.");
				closestOpposite = null;
			} else {
				listCollisions(admin, results, closestOpposite);
			}
		}

		if (!isMe && closest != null && closest.getContactPoint().z + 0.5f < target.getZ()) {
			sendInfo(admin, "Closest collision is below target's Z coordinate!");
		} else {
			if (closest != null) {
				SpawnTemplate spawn = SpawnEngine.newSpawn(admin.getWorldId(), 200000, closest.getContactPoint().x, closest.getContactPoint().y,
					closest.getContactPoint().z, (byte) 0, 0);
				SpawnEngine.spawnObject(spawn, admin.getInstanceId());
			}
			if (closestOpposite != null) {
				SpawnTemplate spawn = SpawnEngine.newSpawn(admin.getWorldId(), 200000, closestOpposite.getContactPoint().x,
					closestOpposite.getContactPoint().y, closestOpposite.getContactPoint().z, (byte) 0, 0);
				SpawnEngine.spawnObject(spawn, admin.getInstanceId());
			}
		}
	}

	private void listCollisions(Player admin, CollisionResults results, CollisionResult closestOpposite) {
		int count = 1;
		int closestId = 0;
		String description = "";

		for (Iterator<CollisionResult> iter = results.iterator(); iter.hasNext(); count++) {
			CollisionResult result = iter.next();
			if (result.equals(closestOpposite))
				closestId = count;
			if (result.getGeometry() == null)
				description += count + ". " + result.getContactPoint().toString() + "\n";
			else {
				if (result.getGeometry().getName() == null) {
					description += count + ". " + result.getContactPoint().toString() + "; parent=" + result.getGeometry().getParent().getName() + "\n";
				} else
					description += count + ". " + result.getContactPoint().toString() + "; name=" + result.getGeometry().getName() + "\n";
			}
		}
		description += "-----------------------\nClosest: " + closestId + ". Distance: " + closestOpposite.getDistance();
		sendInfo(admin, description);
	}
}
