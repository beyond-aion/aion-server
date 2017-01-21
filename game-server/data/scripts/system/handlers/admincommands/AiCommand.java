package admincommands;

import java.util.Iterator;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.AIEngine;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.AbstractAI;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventLog;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
public class AiCommand extends AdminCommand {

	public AiCommand() {
		super("ai");
	}

	@Override
	public void execute(Player player, String... params) {
		/**
		 * Non target commands
		 */
		String param0 = params[0];

		if (param0.equals("createlog")) {
			boolean oldValue = AIConfig.ONCREATE_DEBUG;
			AIConfig.ONCREATE_DEBUG = !oldValue;
			PacketSendUtility.sendMessage(player, "New createlog value: " + !oldValue);
			return;
		}

		if (param0.equals("eventlog")) {
			boolean oldValue = AIConfig.EVENT_DEBUG;
			AIConfig.EVENT_DEBUG = !oldValue;
			PacketSendUtility.sendMessage(player, "New eventlog value: " + !oldValue);
			return;
		}

		if (param0.equals("movelog")) {
			boolean oldValue = AIConfig.MOVE_DEBUG;
			AIConfig.MOVE_DEBUG = !oldValue;
			PacketSendUtility.sendMessage(player, "New movelog value: " + !oldValue);
			return;
		}

		if (param0.equals("say")) {
			LoggerFactory.getLogger(AiCommand.class).info("[AI] marker: " + params[1]);
		}

		/**
		 * Target commands
		 */
		VisibleObject target = player.getTarget();

		if (target == null || !(target instanceof Npc)) {
			PacketSendUtility.sendMessage(player, "Select target first (Npc only)");
			return;
		}
		Npc npc = (Npc) target;

		if (param0.equals("info")) {
			PacketSendUtility.sendMessage(player, "Ai name: " + npc.getAi().getName());
			PacketSendUtility.sendMessage(player, "Ai state: " + npc.getAi().getState());
			PacketSendUtility.sendMessage(player, "Ai substate: " + npc.getAi().getSubState());
			return;
		}

		if (param0.equals("log")) {
			boolean oldValue = npc.getAi().isLogging();
			((AbstractAI) npc.getAi()).setLogging(!oldValue);
			PacketSendUtility.sendMessage(player, "New log value: " + !oldValue);
			return;
		}

		if (param0.equals("print")) {
			AIEventLog eventLog = ((AbstractAI) npc.getAi()).getEventLog();
			Iterator<AIEventType> iterator = eventLog.iterator();
			while (iterator.hasNext()) {
				PacketSendUtility.sendMessage(player, "EVENT: " + iterator.next().name());
			}
			return;
		}

		String param1 = params[1];
		if (param0.equals("set")) {
			String aiName = param1;
			AIEngine.getInstance().setupAI(aiName, npc);
		} else if (param0.equals("event")) {
			AIEventType eventType = AIEventType.valueOf(param1.toUpperCase());
			if (eventType != null) {
				npc.getAi().onGeneralEvent(eventType);
			}
		} else if (param0.equals("event2")) {
			AIEventType eventType = AIEventType.valueOf(param1.toUpperCase());
			Creature creature = (Creature) World.getInstance().findVisibleObject(Integer.valueOf(params[2]));
			if (eventType != null) {
				npc.getAi().onCreatureEvent(eventType, creature);
			}
		} else if (param0.equals("state")) {
			AIState state = AIState.valueOf(param1.toUpperCase());
			((NpcAI) npc.getAi()).setStateIfNot(state);
			if (params.length > 2) {
				AISubState substate = AISubState.valueOf(params[2]);
				((NpcAI) npc.getAi()).setSubStateIfNot(substate);
			}
		}
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //ai2 <set|event|event2|info|log|print|createlog|eventlog|movelog>");
	}

}
