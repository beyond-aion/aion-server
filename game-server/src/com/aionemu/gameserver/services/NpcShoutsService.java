package com.aionemu.gameserver.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npcshout.NpcShout;
import com.aionemu.gameserver.model.templates.npcshout.ShoutEventType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * This class is handling NPC shouts
 * 
 * @author Rolandas, Neon
 */
public class NpcShoutsService {

	private Map<Integer, Long> shoutCooldowns;

	private NpcShoutsService() {
		shoutCooldowns = new ConcurrentHashMap<>();
	}

	public void registerShoutTask(Npc npc) {
		int worldId = npc.getSpawn().getWorldId();

		List<NpcShout> shouts = DataManager.NPC_SHOUT_DATA.getNpcShouts(worldId, npc.getNpcId(), ShoutEventType.IDLE);
		if (shouts == null || shouts.isEmpty())
			return;

		int pollDelay = Rnd.get(180, 360) * 1000;
		for (NpcShout shout : shouts) {
			if (shout.getPollDelay() != 0 && shout.getPollDelay() < pollDelay)
				pollDelay = shout.getPollDelay();
		}

		npc.getController().addTask(TaskId.SHOUT, ThreadPoolManager.getInstance().scheduleAtFixedRate(new NpcShoutTask(npc, shouts), 0, pollDelay));
	}

	public void removeShoutCooldown(Npc npc) {
		shoutCooldowns.remove(npc.getObjectId());
	}

	public boolean mayShout(Npc npc) {
		Long cd = shoutCooldowns.get(npc.getObjectId());
		return cd == null || System.currentTimeMillis() >= cd;
	}

	public void shoutRandom(Npc sender, Player target, List<NpcShout> shouts, int shoutCooldown) {
		if (shouts == null || shouts.isEmpty())
			return;
		shout(sender, target, Rnd.get(shouts), shoutCooldown);
	}

	public void shout(Npc sender, Player target, NpcShout shout, int shoutCooldown) {
		if (sender == null || shout == null)
			return;

		if (shout.getPattern() != null && !sender.getAi().onPatternShout(shout.getWhen(), shout.getPattern(), shout.getSkillNo()))
			return;

		int shoutRange = sender.getObjectTemplate().getMinimumShoutRange();
		if (target != null && !PositionUtil.isInRange(target, sender, shoutRange))
			return;

		String param = shout.getParam();
		if (sender.getTarget() != null && "target".equals(param))
			param = sender.getTarget().getObjectTemplate().getName();

		if (shoutCooldown > 0 && target != null && "quest".equals(shout.getPattern()))
			shoutCooldown = 0;

		SM_SYSTEM_MESSAGE message = new SM_SYSTEM_MESSAGE(ChatType.NPC, sender, shout.getStringId(), param);

		if (target != null) {
			PacketSendUtility.sendPacket(target, message);
		} else {
			PacketSendUtility.broadcastPacket(sender, message, player -> PositionUtil.isInRange(player, sender, shoutRange));
		}
		if (shoutCooldown <= 0)
			removeShoutCooldown(sender);
		else
			shoutCooldowns.put(sender.getObjectId(), System.currentTimeMillis() + shoutCooldown * 1000 - 50); // 50ms offset to avoid tight cooldown conflicts
	}

	public static final NpcShoutsService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final NpcShoutsService instance = new NpcShoutsService();
	}

	private class NpcShoutTask implements Runnable {

		private Npc npc;
		private List<NpcShout> shouts;

		NpcShoutTask(Npc npc, List<NpcShout> shouts) {
			this.npc = npc;
			this.shouts = shouts;
		}

		@Override
		public void run() {
			if (npc.getPosition().isMapRegionActive() && npc.getAi().ask(AIQuestion.CAN_SHOUT))
				shoutRandom(npc, null, shouts, 0);
		}
	}
}
