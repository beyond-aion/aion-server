package consolecommands;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * Shared part of the builder recall commands: resolving the destination the GM typed and moving a whole team there.
 */
abstract class RecallToCommand extends ConsoleCommand {

	protected RecallToCommand(String alias, String description) {
		this(alias, description, "");
	}

	protected RecallToCommand(String alias, String description, String syntaxInfo) {
		super(alias, description, syntaxInfo);
	}

	/**
	 * @return the player the GM currently targets, if it is a player within the given range, else null after informing him
	 */
	protected Player getTargetPlayer(Player admin, float range) {
		VisibleObject target = admin.getTarget();
		if (!(target instanceof Player)) {
			sendInfo(admin, "No target.");
			return null;
		}
		if (!PositionUtil.isInRange(admin, target, range)) {
			sendInfo(admin, "Target is too far away.");
			return null;
		}
		return (Player) target;
	}

	/**
	 * Parses "&lt;x&gt; &lt;y&gt; &lt;z&gt;" as a position on the GM's current map and grounds the Z coordinate.
	 *
	 * @return the destination, or null after informing the GM
	 */
	protected WorldPosition parseDestination(Player admin, String... params) {
		if (params.length < 3) {
			sendInfo(admin);
			return null;
		}
		float x, y, z;
		try {
			x = Float.parseFloat(params[0]);
			y = Float.parseFloat(params[1]);
			z = Float.parseFloat(params[2]);
		} catch (NumberFormatException e) {
			sendInfo(admin, "Invalid coordinates.");
			return null;
		}
		float groundZ = GeoService.getInstance().getZ(admin.getWorldId(), x, y, z, admin.getInstanceId());
		if (GeoDataConfig.MODE == GeoDataConfig.Mode.ON) {
			if (Float.isNaN(groundZ)) {
				sendInfo(admin, "(%.2f %.2f %.2f) is not walkable.".formatted(x, y, z));
				return null;
			}
			z = groundZ;
		}
		return World.getInstance().createPosition(admin.getWorldId(), x, y, z, (byte) 0, admin.getInstanceId());
	}

	/**
	 * Teleports every online team member except the GM and reports the result to him.
	 *
	 * @param teamNoun what a single member is called in the response ("party member", "alliance member")
	 */
	protected void recall(Player admin, TemporaryPlayerTeam<?> team, WorldPosition destination, String teamNoun) {
		int count = TeleportService.teleportTeamTo(team, destination, admin, TeleportAnimation.FADE_OUT_BEAM).size();
		sendInfo(admin, "Recalled " + count + " " + teamNoun + (count == 1 ? "" : "s") + " to X:%.2f Y:%.2f Z:%.2f."
			.formatted(destination.getX(), destination.getY(), destination.getZ()));
	}
}
