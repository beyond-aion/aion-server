package admincommands;

import java.util.HashMap;
import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.WalkerData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.walker.RouteStep;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Rolandas
 */
public class FixPath extends AdminCommand {

	static volatile boolean canceled = false;
	static volatile boolean isRunning = false;
	static Player runner = null;

	public FixPath() {
		super("fixpath");
	}

	@Override
	public void execute(final Player admin, String... params) {
		if (params == null || params.length < 1) {
			PacketSendUtility.sendMessage(admin, "Syntax : //fixpath <route id> <jump height> | <cancel>");
			return;
		}

		String routeId = "";
		final float z = admin.getZ();
		float jumpHeight = 0;

		try {
			if (isRunning && runner != null && !admin.equals(runner)) {
				PacketSendUtility.sendMessage(admin, "Someone is already running this command!");
				return;
			}
			if ("cancel".equals(params[0])) {
				if (isRunning) {
					PacketSendUtility.sendMessage(admin, "Canceled.");
					canceled = true;
				}
				return;
			} else if (params.length < 2) {
				PacketSendUtility.sendMessage(admin, "Syntax : //fixpath <route id> <jump height> | <cancel>");
				return;
			} else {
				routeId = params[0];
				jumpHeight = Float.parseFloat(params[1]);
			}
		} catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(admin, "Only numbers please!!!");
		}

		final WalkerTemplate template = DataManager.WALKER_DATA.getWalkerTemplate(routeId);
		if (template == null) {
			PacketSendUtility.sendMessage(admin, "Invalid route id");
			return;
		}

		PacketSendUtility.sendMessage(admin, "Make sure you are at NPC spawn position. If not use cancel!");

		isRunning = true;
		runner = admin;
		final float height = jumpHeight;

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				boolean wasInvul = admin.isInvulnerable();
				admin.setInvulnerable(true);

				float zDelta = 0;
				HashMap<Integer, Float> corrections = new HashMap<Integer, Float>();

				try {
					int i = 1;
					for (RouteStep step : template.getRouteSteps()) {
						if (canceled || admin.isInState(CreatureState.DEAD)) {
							corrections.clear();
							return;
						}
						if (step.getX() == 0 || step.getY() == 0) {
							corrections.put(i++, admin.getZ());
							PacketSendUtility.sendMessage(admin, "Skipping zero coordinate...");
							continue;
						}
						if (zDelta == 0)
							zDelta = z - step.getZ() + height;
						PacketSendUtility.sendMessage(admin, "Teleporting to step " + i + "...");
						TeleportService2.teleportTo(admin, admin.getWorldId(), step.getX(), step.getY(), step.getZ() + zDelta);
						admin.getController().stopProtectionActiveTask();
						PacketSendUtility.sendMessage(admin, "Waiting to get Z...");
						Thread.sleep(5000);
						step.setZ(admin.getZ());
						corrections.put(i++, admin.getZ());
					}

					PacketSendUtility.sendMessage(admin, "Saving corrections...");

					WalkerData data = new WalkerData();
					WalkerTemplate newTemplate = new WalkerTemplate(template.getRouteId());

					i = 1;
					List<RouteStep> newSteps = new FastTable<RouteStep>();

					int lastStep = template.isReversed() ? (template.getRouteSteps().size() + 2) / 2 : template.getRouteSteps().size();
					for (int s = 0; s < lastStep; s++) {
						RouteStep step = template.getRouteSteps().get(s);
						RouteStep fixedStep = new RouteStep(step.getX(), step.getY(), corrections.get(i), 0);
						fixedStep.setRouteStep(i++);
						newSteps.add(fixedStep);
					}

					newTemplate.setRouteSteps(newSteps);
					if (template.isReversed())
						newTemplate.setIsReversed(true);
					newTemplate.setPool(template.getPool());
					data.AddTemplate(newTemplate);
					data.saveData(template.getRouteId());

					PacketSendUtility.sendMessage(admin, "Done.");
				} catch (Exception e) {
				} finally {
					runner = null;
					isRunning = false;
					canceled = false;
					if (!wasInvul)
						admin.setInvulnerable(false);
				}
			}
		}, 5000);
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax : //fixpath <route id> <jump height> | <cancel>");
	}

}
