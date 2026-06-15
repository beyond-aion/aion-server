package ai.worlds.panesterra;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import java.util.stream.Stream;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.services.panesterra.PanesterraService;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("panesterra_entry_corridor")
public class PanesterraEntryCorridorAI extends GeneralNpcAI {

	private final int relatedFortressId;

	public PanesterraEntryCorridorAI(Npc owner) {
		super(owner);
		relatedFortressId = switch (getNpcId()) {
			case 730946, 730950 -> 10111;
			case 730947, 730951 -> 10211;
			case 730948, 730952 -> 10311;
			case 730949, 730953 -> 10411;
			default -> 0;
		};
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId == SETPRO1 && canTeleport(player)) {
			// teleportToFortress(player);
			PanesterraService.getInstance().teleportToEventLocation(player);
		}
		return true;
	}

	private boolean canTeleport(Player player) {
		if (player.getLevel() < 65) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_TELEPOTER_GAB1_USER04());
			return false;
		}
		// TODO: Remove after event
		if (EventService.getInstance().getActiveEvents().stream()
			.anyMatch(event -> event.getEventTemplate().getName().equalsIgnoreCase("Beyond Aion Birthday Event"))) {
			return true;
		}
		// Don't allow access if any fortress is under siege
		if (Stream.of(10111, 10211, 10311, 10411).anyMatch(id -> SiegeService.getInstance().getSiege(id) != null)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_READY_PANGAEA());
			return false;
		}

		if (SiegeService.getInstance().getFortress(relatedFortressId).getRace() != SiegeRace.getByRace(player.getRace())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_MOVE_TO_AIRPORT_NO_ROUTE());
			return false;
		}
		return true;
	}

	private void teleportToFortress(Player player) {
		// TODO
		player.setPanesterraFaction(PanesterraFaction.getByFortressId(relatedFortressId));
	}
}
