package com.aionemu.gameserver.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECALLED_BY_OTHER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.ZoneAttributes;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * Handles pending summon requests (Summon Group Member, example skillId: 3777).<br>
 * The request is not re-validated when the summoned player accepts it. Instead every state change which would invalidate it cancels the request, see
 * {@link #cancel(Player, CancelReason)}.
 */
public class RecallService {

	public enum CancelReason {
		/** Nobody answered within {@link #CONFIRMATION_SECONDS}. Only the caster is notified. */
		TIMEOUT,
		/** The summoned player declined. */
		DECLINED,
		/** Something invalidated the request (combat, death, teleport, ...). Both sides are notified. */
		CANCELLED,
		/** The request is dropped without notifying anyone. */
		SILENT
	}

	private static final int CONFIRMATION_SECONDS = 30;

	private final Map<Integer, Request> requests = new ConcurrentHashMap<>();

	public static RecallService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private RecallService() {
	}

	public boolean hasPendingRequest(Player summoned) {
		return requests.containsKey(summoned.getObjectId());
	}

	/**
	 * Asks the summoned player whether he wants to be teleported to the position the caster is standing on.
	 */
	public void requestSummon(Player caster, Player summoned, int skillId) {
		Request request = new Request(caster.getObjectId(), caster.getWorldId(), caster.getInstanceId(), caster.getX(), caster.getY(), caster.getZ(),
			caster.getHeading());
		if (requests.putIfAbsent(summoned.getObjectId(), request) != null)
			return;
		request.timeout = ThreadPoolManager.getInstance().schedule(() -> {
			if (requests.get(summoned.getObjectId()) == request) // never time out a request which replaced this one
				cancel(summoned, CancelReason.TIMEOUT);
		}, CONFIRMATION_SECONDS * 1000);
		PacketSendUtility.sendPacket(summoned, new SM_RECALLED_BY_OTHER(caster.getName(), skillId, CONFIRMATION_SECONDS));
	}

	/**
	 * Teleports the player to the caster of his pending request, without validating it again.
	 */
	public void accept(Player summoned) {
		Request request = remove(summoned);
		if (request != null)
			TeleportService.teleportTo(summoned, request.worldId, request.instanceId, request.x, request.y, request.z, request.heading);
	}

	/**
	 * Drops the pending request of the given player, if there is one, and notifies both sides as the reason dictates.
	 */
	public void cancel(Player summoned, CancelReason reason) {
		Request request = remove(summoned);
		if (request == null)
			return;
		if (reason == CancelReason.TIMEOUT || reason == CancelReason.CANCELLED)
			PacketSendUtility.sendPacket(summoned, new SM_RECALLED_BY_OTHER()); // the client closes the window itself only when it answered
		if (reason == CancelReason.SILENT)
			return;

		Player caster = World.getInstance().getPlayer(request.casterObjectId);
		if (caster == null)
			return;
		switch (reason) {
			case TIMEOUT -> PacketSendUtility.sendPacket(caster, SM_SYSTEM_MESSAGE.STR_MSG_Recall_DONOT_ACCEPT_EFFECT(summoned.getName()));
			case DECLINED -> {
				PacketSendUtility.sendPacket(caster, SM_SYSTEM_MESSAGE.STR_MSG_Recall_Rejected_EFFECT(summoned.getName()));
				PacketSendUtility.sendPacket(summoned, SM_SYSTEM_MESSAGE.STR_MSG_Recall_Reject_EFFECT(caster.getName()));
			}
			case CANCELLED -> {
				PacketSendUtility.sendPacket(caster, SM_SYSTEM_MESSAGE.STR_MSG_Recall_CANCEL_EFFECT(summoned.getName()));
				PacketSendUtility.sendPacket(summoned, SM_SYSTEM_MESSAGE.STR_MSG_Recall_CANCEL_EFFECT(caster.getName()));
			}
		}
	}

	private Request remove(Player summoned) {
		Request request = requests.remove(summoned.getObjectId());
		if (request != null && request.timeout != null)
			request.timeout.cancel(false);
		return request;
	}

	/**
	 * Checks everything a summon skill needs before it may be cast and tells the caster why it failed.
	 *
	 * @return True, if the cast may start
	 */
	public static boolean validateCast(Player caster, VisibleObject target) {
		if (caster.isFlying()) {
			PacketSendUtility.sendPacket(caster, SM_SYSTEM_MESSAGE.STR_SKILL_RESTRICTION_NO_FLY());
			return false;
		}
		if (!canRecallAt(caster)) {
			PacketSendUtility.sendPacket(caster, SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST_IN_CURRENT_POSTION());
			return false;
		}
		if (!(target instanceof Player targetPlayer)) {
			PacketSendUtility.sendPacket(caster, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID());
			return false;
		}
		if (getInstance().hasPendingRequest(targetPlayer)) {
			PacketSendUtility.sendPacket(caster, SM_SYSTEM_MESSAGE.STR_MSG_Recall_DUPLICATE_EFFECT(targetPlayer.getName()));
			return false;
		}
		if (!canBeSummoned(caster, targetPlayer)) {
			PacketSendUtility.sendPacket(caster, SM_SYSTEM_MESSAGE.STR_MSG_Recall_CANNOT_ACCEPT_EFFECT(targetPlayer.getName()));
			return false;
		}
		return true;
	}

	/**
	 * @return True, if the given player may be summoned by the caster right now.
	 */
	public static boolean canBeSummoned(Creature caster, Creature summoned) {
		if (!(summoned instanceof Player summonedPlayer) || caster == summoned)
			return false;
		if (caster.getWorldId() != summoned.getWorldId() || caster.getInstanceId() != summoned.getInstanceId())
			return false;
		if (caster.isEnemy(summoned) || summonedPlayer.isDead())
			return false;
		if (summonedPlayer.getController().isInCombat() || summonedPlayer.isUsingFlightTransporterOrWindstream())
			return false;
		if (summonedPlayer.isInState(CreatureState.PRIVATE_SHOP))
			return false;
		if (summonedPlayer.getInteractionTask() != null) // gathering or crafting
			return false;
		return !summonedPlayer.getTransformModel().cantRecall();
	}

	/**
	 * @return True, if summon skills may be cast at the position of the given player. Of the limit zones covering him, the one with the lowest
	 *         priority decides, and it can only forbid the summon, never allow one the world map forbids.
	 */
	public static boolean canRecallAt(Player caster) {
		ZoneTemplate decisive = null;
		for (ZoneInstance zone : caster.findZones()) {
			ZoneTemplate template = zone.getZoneTemplate();
			if (template.getZoneType() != ZoneClassName.LIMIT || template.getFlags() == -1) // no flags at all means no information
				continue;
			if (decisive == null || template.getPriority() < decisive.getPriority())
				decisive = template;
		}
		if (decisive != null && (decisive.getFlags() & ZoneAttributes.RECALL.getId()) == 0)
			return false;
		return World.getInstance().getWorldMap(caster.getWorldId()).canRecall();
	}

	private static class Request {

		private final int casterObjectId;
		private final int worldId;
		private final int instanceId;
		private final float x, y, z;
		private final byte heading;
		private Future<?> timeout;

		private Request(int casterObjectId, int worldId, int instanceId, float x, float y, float z, byte heading) {
			this.casterObjectId = casterObjectId;
			this.worldId = worldId;
			this.instanceId = instanceId;
			this.x = x;
			this.y = y;
			this.z = z;
			this.heading = heading;
		}
	}

	private static class SingletonHolder {

		protected static final RecallService INSTANCE = new RecallService();
	}
}
