package com.aionemu.gameserver.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AITemplate;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npcshout.NpcShout;
import com.aionemu.gameserver.model.templates.npcshout.ShoutEventType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * This class is handling NPC shouts
 * 
 * @author Rolandas
 * @reworked Neon
 */
public class NpcShoutsService {

	private Map<Integer, Long> shoutCooldowns;

	private NpcShoutsService() {
		shoutCooldowns = new ConcurrentHashMap<>();
	}

	public void registerShoutTask(Npc npc) {
		int npcId = npc.getNpcId();
		int worldId = npc.getSpawn().getWorldId();
		int objectId = npc.getObjectId();

		List<NpcShout> shouts = DataManager.NPC_SHOUT_DATA.getNpcShouts(worldId, npcId, ShoutEventType.IDLE);
		if (shouts == null || shouts.isEmpty())
			return;

		int pollDelay = Rnd.get(180, 360) * 1000;
		for (NpcShout shout : shouts) {
			if (shout.getPollDelay() != 0 && shout.getPollDelay() < pollDelay)
				pollDelay = shout.getPollDelay();
		}

		npc.getController().addTask(TaskId.SHOUT, ThreadPoolManager.getInstance().scheduleAtFixedRate(new NpcShoutTask(objectId, shouts), 0, pollDelay));
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
		int randomShout = shouts.size() == 1 ? 0 : Rnd.get(shouts.size());
		shout(sender, target, shouts.get(randomShout), shoutCooldown);
	}

	public void shout(Npc sender, Player target, NpcShout shout, int shoutCooldown) {
		if (sender == null || shout == null)
			return;

		if (shout.getPattern() != null && !((AITemplate) sender.getAi2()).onPatternShout(shout.getWhen(), shout.getPattern(), shout.getSkillNo()))
			return;

		int shoutRange = sender.getObjectTemplate().getMinimumShoutRange();
		if (target != null && !MathUtil.isIn3dRange(target, sender, shoutRange))
			return;

		String param = shout.getParam();
		if (sender.getTarget() != null && "target".equals(param))
			param = sender.getTarget().getObjectTemplate().getName();

		if (shoutCooldown > 0 && target instanceof Player && "quest".equals(shout.getPattern()))
			shoutCooldown = 0;

		SM_SYSTEM_MESSAGE message = new SM_SYSTEM_MESSAGE(ChatType.NPC, sender, shout.getStringId(), param);

		if (target != null) {
			PacketSendUtility.sendPacket(target, message);
		} else {
			PacketSendUtility.broadcastPacket(sender, message, false, player -> MathUtil.isIn3dRange(player, sender, shoutRange));
		}
		if (shoutCooldown <= 0)
			removeShoutCooldown(sender);
		else
			shoutCooldowns.put(sender.getObjectId(), System.currentTimeMillis() + shoutCooldown * 1000 - 50); // 50ms offset to avoid tight cooldown conflicts
	}

	public static final NpcShoutsService getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final NpcShoutsService instance = new NpcShoutsService();
	}

	private class NpcShoutTask implements Runnable {

		private int objectId;
		private List<NpcShout> shouts;

		NpcShoutTask(int objectId, List<NpcShout> shouts) {
			this.objectId = objectId;
			this.shouts = shouts;
		}

		@Override
		public void run() {
			Npc npc = World.getInstance().findNpc(objectId);
			if (npc == null || !npc.getPosition().isMapRegionActive() || !npc.getAi2().ask(AIQuestion.CAN_SHOUT))
				return;
			shoutRandom(npc, null, shouts, 0);
		}
	}
}
