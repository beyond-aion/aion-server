package instance;

import java.util.concurrent.Future;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;


/**
 * @author Cheatkiller
 *
 */
@InstanceID(301270000)
public class LinkgateFoundryInstance extends GeneralInstanceHandler {
	
	 private Future<?> timeCheckTask;
   private byte timeInMin = -1;
   private boolean isInstanceDestroyed = false;
   private byte secretLabEntranceCount = 0;
   
  @Override
  public void onEnterInstance(Player player) {
  	spawn(player.getRace() == Race.ELYOS ? 206361 : 206362, 348.00464f, 252.13882f, 311.36136f, (byte) 10);
 	}
   
  @Override
  public void onDie(Npc npc) {
  	Player player = npc.getAggroList().getMostPlayerDamage();
  	switch (npc.getNpcId()) {
  		case 234990:
			case 234991:
				spawn(player.getRace() == Race.ELYOS ? 702338 : 702389, 246.74345f, 258.35843f, 312.32327f, (byte) 10);
				break;
  	}
  }
  		
		 	
	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 804578:
				sendMsg(1402453); // 20 min
				startTimeCheck();
			case 234193:
				npc.getController().die();
				break;
			case 804629:
				TeleportService2.teleportTo(player, 301270000, 228.37f, 262.7f, 313, (byte) 120);
				break;
			case 702592:
				TeleportService2.teleportTo(player, 301270000, 211.32f, 260, 314, (byte) 0, TeleportAnimation.BEAM_ANIMATION);
				break;
			case 702590:
				TeleportService2.teleportTo(player, 301270000, 257.11f, 323, 271, (byte) 60, TeleportAnimation.BEAM_ANIMATION);
				npc.getController().onDelete();
				secretLabEntranceCount++;
				if (secretLabEntranceCount < 3) {
					spawn(234992, 244.1839f, 322.5356f, 270.9474f, (byte) 0);
				}
				else
					sendMsg(1402603);
				break;
		}
	}
	
	private void startTimeCheck() {
		timeCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			 @Override
			 public void run() {
				 timeInMin++;
				 switch (timeInMin) {
					 case 5:
						 sendMsg(1402453); // 15 min
						 break;
					 case 10:
						 sendMsg(1402454); // 10 min
						 break;
					 case 15:
						 sendMsg(1402455); // 5 min
						 break;
					 case 17:
						 sendMsg(1402456); // 3 min
						 break;
					 case 19:
						 sendMsg(1402457); // 1 min
						 break;
					 case 20:
						 sendMsg(1402461);
						 for (Npc npc : instance.getNpcs()) {
							 if (npc.getNpcId() != 233898 && npc.getNpcId() != 702339) {
								 npc.getController().onDelete();
							 }
						 }
						 if (timeCheckTask != null && !timeCheckTask.isDone()) {
               timeCheckTask.cancel(true);
           } 
				 }
			 }
			 
		}, 0, 60000);
	}
	
	@Override
	public void onEnterZone(Player player, ZoneInstance zone) {
		if (zone.getAreaTemplate().getZoneName() == ZoneName.get("IDLDF4RE_01_ITEMUSEAREA_BOSS")) {
			if (timeCheckTask != null && !timeCheckTask.isDone()) {
        timeCheckTask.cancel(true);
			}
		}
	}
	
	@Override
	public boolean onDie(final Player player, Creature lastAttacker) {
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.DIE, 0, player.equals(lastAttacker) ? 0
				: lastAttacker.getObjectId()), true);

		PacketSendUtility.sendPacket(player, new SM_DIE(player.haveSelfRezEffect(), player.haveSelfRezItem(), 0, 8));
		return true;
	}
	
	 @Override
   public void onInstanceDestroy() {
       isInstanceDestroyed = true;
       if (timeCheckTask != null && !timeCheckTask.isDone()) {
         timeCheckTask.cancel(true);
     }
   }
}
