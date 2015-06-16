package ai.instance.tiamatStrongHold;

import ai.GeneralNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;


/**
 * @author Cheatkiller
 *
 */
@AIName("suramathetraitor")
public class SuramaTheTraitorAI2 extends GeneralNpcAI2 {
	
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		moveToRaksha();
	}
	
	@Override
	protected void handleDied() {
		super.handleDied();
		NpcShoutsService.getInstance().sendMsg(getOwner(), 390845, getOwner().getObjectId(), 0, 2000);
	}
	
	private void moveToRaksha() {
		setStateIfNot(AIState.WALKING);
  	getOwner().setState(1);
		getMoveController().moveToPoint(651, 1319, 487);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getOwner().getObjectId()));
		ThreadPoolManager.getInstance().schedule(new Runnable() {

		  @Override
		  public void run() {
		  	startDialog();
		  }
	  }, 10000);
	}
	
	
	private void startDialog() {
		final Npc raksha = getPosition().getWorldMapInstance().getNpc(219356);
		NpcShoutsService.getInstance().sendMsg(getOwner(), 390841, getOwner().getObjectId(), 0, 0);
		NpcShoutsService.getInstance().sendMsg(getOwner(), 390842, getOwner().getObjectId(), 0, 3000);
		NpcShoutsService.getInstance().sendMsg(raksha, 390843, raksha.getObjectId(), 0, 6000);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

		  @Override
		  public void run() {
		  	raksha.setTarget(getOwner());
		  	SkillEngine.getInstance().getSkill(raksha, 20952, 60, getOwner()).useNoAnimationSkill();
		  	changeNpcType(raksha, CreatureType.ATTACKABLE.getId());
		  }
	  }, 8000);
	}
	
	private void changeNpcType(Npc npc, final int newType) {
	  npc.setNpcType(newType);
	  for (final Player player : npc.getKnownList().getKnownPlayers().values()) {
	  	PacketSendUtility.sendPacket(player, new SM_CUSTOM_SETTINGS(npc.getObjectId(), 0, newType, 0));
	  }
  }
}
