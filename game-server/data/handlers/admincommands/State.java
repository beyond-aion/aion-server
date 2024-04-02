package admincommands;

import java.awt.Color;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TARGET_SELECTED;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TARGET_UPDATE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Rolandas
 */
public class State extends AdminCommand {

	public State() {
		super("state", "Views and adjusts your target's creature states.");

		// @formatter:off
		setSyntaxInfo(
				" - Shows your target's creature states.",
				"<state> - Sets given creature state(s) by name or ID, replacing existing states.",
				"add <state> - Sets given creature state(s) by name or ID.",
				"remove <state> - Removes given creature state(s) by name or ID. Use -1 to remove all states.",
				"list - Shows possible state names and ID. Add ID values together to add or remove multiple states at once."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		VisibleObject target = admin.getTarget();
		if (target == null) {
			sendInfo(admin);
			return;
		}
		if (!(target instanceof Creature creature)) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}

		if (params.length == 0) {
			sendInfo(admin, creature.getName() + "'s state: " + getStateDescription(creature.getState()) + "\nSee "+ ChatUtil.color(getAliasWithPrefix() + " help", Color.WHITE) + " for more options.");
		} else if ("list".equalsIgnoreCase(params[0])) {
			sendInfo(admin, "Known states:\n\t" + Arrays.stream(CreatureState.values()).map(c -> c.name() + " (" + c.getId() + ')').collect(Collectors.joining("\n\t")));
		} else {
			int stateIndex = "add".equalsIgnoreCase(params[0]) || "remove".equalsIgnoreCase(params[0]) ? 1 : 0;
			if (params.length <= stateIndex) {
				sendInfo(admin, "Please provide a state name or ID.");
				return;
			}
			int stateId;
			try {
				stateId = CreatureState.valueOf(params[stateIndex].toUpperCase()).getId();
			} catch (IllegalArgumentException e) {
				stateId = Integer.parseInt(params[stateIndex]);
				if (stateId < 0 || stateId > 0xFFFF) {
					sendInfo(admin, "Out of range state ID.");
					return;
				}
			}
			int newState;
			if (stateIndex == 0)
				newState = stateId & 0xFFFF;
			else if ("add".equalsIgnoreCase(params[0]))
				newState = (creature.getState() | stateId) & 0xFFFF;
			else
				newState = (creature.getState() & ~stateId) & 0xFFFF;

			creature.setState(newState);

			if (target instanceof Player player) {
				player.getController().onChangedPlayerAttributes();
			} else {
				creature.clearKnownlist();
				creature.updateKnownlist();
			}
			ThreadPoolManager.getInstance().schedule(() -> {
				admin.setTarget(target);
				PacketSendUtility.sendPacket(admin, new SM_TARGET_SELECTED(target));
				PacketSendUtility.broadcastToSightedPlayers(admin, new SM_TARGET_UPDATE(admin));
			}, 200);

			sendInfo(admin, creature.getName() + "'s state changed to " + getStateDescription(creature.getState()));
		}
	}

	private String getStateDescription(int state) {
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= (state & 0xFFFF); i *= 2) {
			if ((state & i) == i) {
				if (!sb.isEmpty())
					sb.append(" + ");
				sb.append(findStateName(i, "UNK")).append(" (").append(i).append(')');
			}
		}
		return state + (sb.isEmpty() ? "" : " = " + sb.toString());
	}

	private String findStateName(int creatureStateId, String defaultName) {
		return Arrays.stream(CreatureState.values()).filter(s -> s.getId() == creatureStateId).findFirst().map(Object::toString).orElse(defaultName);
	}
}
