package admincommands;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.*;
import com.aionemu.gameserver.ai.event.AIEventLog;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer, Neon
 */
public class Ai extends AdminCommand {

	public Ai() {
		super("ai", "Modifies and shows AI details.");

		// @formatter:off
		setSyntaxInfo(
			"<info> - Show AI info for your target.",
			"<set> <aiName> - Changes the AI of your target.",
			"<state> <stateName> [substateName] - Changes the AI state.",
			"<event> <eventName> - Fires the AI event for the given name.",
			"<event2> <eventName> <creatureObjId> - Fires the creature AI event for the given name and creature.",
			"<events> - Shows last AI events for your target.",
			"<log> - Toggles AI logging for your target on and off.",
			"<createlog|eventlog|movelog> - Toggles logging on and off.",
			"<marker> [text] - Prints a marker with the optional text in log."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		if (params[0].equalsIgnoreCase("createlog")) {
			AIConfig.ONCREATE_DEBUG = !AIConfig.ONCREATE_DEBUG;
			sendInfo(admin, "New createlog value: " + AIConfig.ONCREATE_DEBUG);
		} else if (params[0].equalsIgnoreCase("eventlog")) {
			AIConfig.EVENT_DEBUG = !AIConfig.EVENT_DEBUG;
			sendInfo(admin, "New eventlog value: " + AIConfig.EVENT_DEBUG);
		} else if (params[0].equalsIgnoreCase("movelog")) {
			AIConfig.MOVE_DEBUG = !AIConfig.MOVE_DEBUG;
			sendInfo(admin, "New movelog value: " + AIConfig.MOVE_DEBUG);
		} else if (params[0].equalsIgnoreCase("marker")) {
			if (params.length > 1)
				LoggerFactory.getLogger(AILogger.class).info("[AI] marker: " + StringUtils.join(params, ' ', 1, params.length));
			else
				LoggerFactory.getLogger(AILogger.class).info("[AI] marker");
		} else {
			VisibleObject target = admin.getTarget();
			if (target == null || !(target instanceof Creature)) {
				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
				return;
			}
			Creature npc = (Creature) target;

			if (params[0].equalsIgnoreCase("info")) {
				sendInfo(admin,
					"[AI info]\n\tName: " + npc.getAi().getName() + "\n\tState: " + npc.getAi().getState() + "\n\tSubstate: " + npc.getAi().getSubState());
			} else if (params[0].equalsIgnoreCase("log")) {
				boolean oldValue = npc.getAi().isLogging();
				npc.getAi().setLogging(!oldValue);
				sendInfo(admin, "New log value: " + !oldValue);
			} else if (params[0].equalsIgnoreCase("events")) {
				AIEventLog eventLog = npc.getAi().getEventLog();
				if (eventLog == null || eventLog.isEmpty()) {
					sendInfo(admin, "No events logged" + (AIConfig.EVENT_DEBUG ? "" : " (enable event logging via eventlog parameter)"));
				} else {
					for (AIEventType eventType : eventLog) {
						sendInfo(admin, "EVENT: " + eventType.name());
					}
				}
			} else if (params.length > 1) {
				String param1 = params[1];
				if (params[0].equalsIgnoreCase("set")) {
					String aiName = param1;
					try {
						AI newAi = AIEngine.getInstance().newAI(aiName, npc);
						try {
							Field aiField = npc.getClass().getSuperclass().getDeclaredField("ai");
							aiField.setAccessible(true);
							World.getInstance().despawn(npc, ObjectDeleteAnimation.NONE);
							aiField.set(npc, newAi);
							World.getInstance().spawn(npc); // properly init AI states
						} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
							LoggerFactory.getLogger(Ai.class).error("", e);
						}
						if (npc.getAi() == newAi)
							sendInfo(admin, "Npc now has AI " + newAi.getClass().getSimpleName());
						else
							sendInfo(admin, "Error changing AI (see logs)");
					} catch (IllegalArgumentException e) {
						sendInfo(admin, e.getMessage());
					}
				} else if (params[0].equalsIgnoreCase("event")) {
					try {
						AIEventType eventType = AIEventType.valueOf(param1.toUpperCase());
						npc.getAi().onGeneralEvent(eventType);
					} catch (IllegalArgumentException e) {
						sendInfo(admin, "Found no event with that name");
					}
				} else if (params[0].equalsIgnoreCase("event2")) {
					Creature creature = params.length < 3 ? null : (Creature) World.getInstance().findVisibleObject(Integer.valueOf(params[2]));
					if (creature == null)
						sendInfo(admin, "Please provide a valid creature object ID");
					else {
						try {
							AIEventType eventType = AIEventType.valueOf(param1.toUpperCase());
							npc.getAi().onCreatureEvent(eventType, creature);
						} catch (IllegalArgumentException e) {
							sendInfo(admin, "Found no event with that name");
						}
					}
				} else if (params[0].equalsIgnoreCase("state")) {
					AIState state = null;
					try {
						state = AIState.valueOf(param1.toUpperCase());
						npc.getAi().setStateIfNot(state);
						if (params.length > 2) {
							AISubState substate = AISubState.valueOf(params[2]);
							npc.getAi().setSubStateIfNot(substate);
						}
					} catch (IllegalArgumentException e) {
						sendInfo(admin, "Found no " + (state == null ? "state" : "substate") + " with that name");
					}
				}
			} else {
				sendInfo(admin);
			}
		}
	}

}
