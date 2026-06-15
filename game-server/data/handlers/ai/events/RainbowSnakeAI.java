package ai.events;

import ai.GeneralNpcAI;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@AIName("rainbow_snake")
public class RainbowSnakeAI extends GeneralNpcAI {

	private final Set<Integer> messagedPlayers = ConcurrentHashMap.newKeySet(); // TODO remove + fix NpcShoutsService

	public RainbowSnakeAI(Npc npc) {
		super(npc);
	}

	@Override
	public void handleCreatureDetected(Creature creature) {
		super.handleCreatureDetected(creature);
		if (creature instanceof Player player && messagedPlayers.add(player.getObjectId()))
			PacketSendUtility.sendMessage(player, getOwner(), 1501203); // Hey, you there... Yeah, you... Come here!
	}

	@Override
	protected void handleCreatureNotSee(Creature creature) {
		super.handleCreatureNotSee(creature);
		messagedPlayers.remove(creature.getObjectId());
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		if (dialogActionId != DialogAction.SETPRO1)
			return false;
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
		switch (getNpcId()) {
			case 832963, 832974 -> { // Serpente Rumin (FRI-SUN)
				SkillEngine.getInstance().getSkill(getOwner(), 10976, 1, player).useWithoutPropSkill(); // [Event] Rainbow Snake's Splendor (Drop rate +50%)
				SkillEngine.getInstance().getSkill(getOwner(), 10977, 1, player).useWithoutPropSkill(); // [Event] Rainbow Snake's Grace (Gathering XP +100%)
			}
			case 832964, 832975 -> { // Ahas Rumin (MON-THU)
				SkillEngine.getInstance().getSkill(getOwner(), 10978, 1, player).useWithoutPropSkill(); // [Event] Rainbow Snake's Love (Crafting XP +100%)
				SkillEngine.getInstance().getSkill(getOwner(), 10979, 1, player).useWithoutPropSkill(); // [Event] Rainbow Snake's Judgement (AP +50%)
			}
		}
		return true;
	}
}
