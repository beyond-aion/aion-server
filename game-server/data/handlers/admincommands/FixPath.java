package admincommands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.aionemu.gameserver.controllers.movement.MovementMask;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.WalkerData;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.walker.RouteStep;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate.LoopType;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Rolandas, Neon
 */
public class FixPath extends AdminCommand {

	private static volatile Player runner = null;
	private static boolean oldInvul = false;

	public FixPath() {
		super("fixpath", "Fixes Z-coordinates for npc walk routes using your client (not internal geo data).");

		// @formatter:off
		setSyntaxInfo(
			"[z-offset] - Gathers new Z-coordinates for the route of your target (default: uses old Z values as a base, optional: adds the offset to old values).",
			"<route id> [z-offset] - Gathers new Z-coordinates for the specified route (default: uses old Z values as a base, optional: adds the offset to old values).",
			"<cancel> - Cancels route fixing."
		);
		// @formatter:on
	}

	@Override
	public void execute(final Player admin, String... params) {
		if (params.length == 0 && !(admin.getTarget() instanceof Npc)) {
			sendInfo(admin);
			return;
		}

		if (runner != null) {
			if (!admin.equals(runner)) {
				sendInfo(admin, "Someone is already running this command!");
			} else if ("cancel".equals(params[0])) {
				sendInfo(admin, "Canceled path fixing.");
				stop();
			} else {
				sendInfo(admin);
			}
			return;
		}

		int map = 0;
		float zOffset = 0.1f;
		WalkerTemplate t = null;

		if (params.length > 0 && (t = DataManager.WALKER_DATA.getWalkerTemplate(params[0])) != null) {
			if (params.length > 1) {
				try {
					zOffset = Float.parseFloat(params[1]);
				} catch (NumberFormatException e) {
					sendInfo(admin, "Invalid Z offset.");
					return;
				}
			}
			map = admin.getWorldId();
			sendInfo(admin, "Make sure you are on the correct map. If not use <cancel>!");
		} else {
			if (admin.getTarget() instanceof Npc) {
				if (params.length > 0) {
					try {
						zOffset = Float.parseFloat(params[0]);
					} catch (NumberFormatException e) {
						sendInfo(admin, "Invalid Z offset.");
						return;
					}
				}
				map = admin.getTarget().getWorldId();
				t = DataManager.WALKER_DATA.getWalkerTemplate(admin.getTarget().getSpawn().getWalkerId());
			}
			if (t == null) {
				sendInfo(admin, "Couldn't find route id.");
				return;
			}
		}

		runner = admin;
		oldInvul = admin.isInvulnerable();
		final WalkerTemplate template = t;
		final int worldId = map;
		final float zOff = zOffset;

		ThreadPoolManager.getInstance().execute(() -> { // run in a new thread to avoid blocking everything
			admin.setCustomState(CustomPlayerState.INVULNERABLE);
			RouteStep start = template.getRouteStep(0);
			TeleportService.teleportTo(admin, new WorldPosition(worldId, start.getX(), start.getY(), start.getZ() + zOff, admin.getHeading()));
			float zDelta = getZ(admin) - start.getZ() + zOff;

			List<Float> corrections = new ArrayList<>(template.getRouteSteps().size());
			try {
				int maxNonWalkBackStep = template.getLoopType() == LoopType.WALK_BACK ? (template.getRouteSteps().size() + 1) / 2
					: template.getRouteSteps().size();
				for (RouteStep step : template.getRouteSteps()) {
					if (runner == null) // on cancel
						return;
					if (step.getStepIndex() > maxNonWalkBackStep) // walk back steps are added in afterUnmarshal, not in templates
						break;
					sendInfo(admin, "Teleporting to step " + step.getStepIndex() + "...");
					TeleportService.teleportTo(admin,
						new WorldPosition(admin.getWorldId(), step.getX(), step.getY(), step.getZ() + zDelta, admin.getHeading()));
					corrections.add(getZ(admin));
				}

				sendInfo(admin, "Saving and applying corrections...");

				WalkerData data = new WalkerData();
				WalkerTemplate newTemplate = new WalkerTemplate(template.getRouteId());
				List<RouteStep> newSteps = new ArrayList<>();

				for (int stepIndex = 0; stepIndex < corrections.size(); stepIndex++) {
					float fixedZ = corrections.get(stepIndex);
					RouteStep oldStep = template.getRouteStep(stepIndex);
					newSteps.add(new RouteStep(oldStep.getX(), oldStep.getY(), fixedZ, oldStep.getRestTime()));
					oldStep.setZ(fixedZ); // live fixing
					if (template.getLoopType() == LoopType.WALK_BACK && stepIndex > 0 && stepIndex < maxNonWalkBackStep) // live fixing of walk back steps
						template.getRouteStep(template.getRouteSteps().size() - stepIndex).setZ(fixedZ);
				}

				newTemplate.setRouteSteps(newSteps);
				newTemplate.setLoopType(template.getLoopType());
				newTemplate.setPool(template.getPool());
				newTemplate.setType(template.getType());
				newTemplate.setRows(template.getRows());
				data.addTemplate(newTemplate);
				data.saveData(template.getRouteId());

				sendInfo(admin, "Finished path fixing.");
			} finally {
				stop();
			}
		});
	}

	/**
	 * @return Returns the Z coordinate of the player after he stopped moving, or 0 on timeout.
	 */
	private float getZ(Player admin) {
		sendInfo(admin, "Waiting to get Z...");
		float lastZ[] = { admin.getZ() };
		final ScheduledFuture<?>[] waitTask = { null };
		waitTask[0] = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			float z = admin.getZ();
			if (admin.getMoveController().getMovementMask() == MovementMask.IMMEDIATE && z != lastZ[0] && admin.isSpawned()
				&& !admin.isInState(CreatureState.DEAD)) {
				waitTask[0].cancel(true);
				lastZ[0] = z;
			}
		}, 500, 250);
		try {
			waitTask[0].get(5, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException | CancellationException e) {
			if (e instanceof TimeoutException) {
				sendInfo(admin, "Aborted path fixing due to timeout.");
				stop();
				return 0;
			}
		}
		return lastZ[0];
	}

	private void stop() {
		if (runner != null) {
			if (runner.isOnline()) {
				if (!oldInvul && runner.isInvulnerable())
					runner.unsetCustomState(CustomPlayerState.INVULNERABLE);
			}
			runner = null;
		}
	}
}
