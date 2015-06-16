package instance;

import java.util.Map;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.effect.PlayerEffectController;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.NormalReward;
import com.aionemu.gameserver.network.aion.instanceinfo.NormalScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Ritsu
 */
@InstanceID(300530000)
public class IdgelResearchCenterInstance extends GeneralInstanceHandler {

   private Future<?> checkTask;
   private Map<Integer, StaticDoor> doors;
   private Future<?> instanceTimer;
   private long startTime;
   private NormalReward instanceReward;
   private boolean isInstanceDestroyed;
   private Future<?> finishTimerTask;

   @Override
   public void onOpenDoor(int door) {
	  switch (door) {
		 case 32:
			instanceReward.setInstanceScoreType(InstanceScoreType.START_PROGRESS);
			startTime -= 120000 - (System.currentTimeMillis() - startTime);
			sendPacket(0, 0);
			if (instanceTimer != null) {
			   instanceTimer.cancel(false);
			}
			startFinishTask();
			break;
		 case 48:
			ThreadPoolManager.getInstance().schedule(new Runnable() {

			   @Override
			   public void run() {
				  Npc npc = instance.getNpc(230376);
				  if (npc != null) {
					 npc.getController().delete();
				  }
			   }
			}, 4000);
			break;
	  }
   }

   @Override
   public void onDie(final Npc npc) {
	  Creature master = npc.getMaster();
	  if (master instanceof Player)
		 return;

	  final int points = calculatePointsReward(npc);
	  if (instanceReward.getInstanceScoreType().isStartProgress()) {
		 instanceReward.addPoints(points);
		 if (instanceReward.getPoints() >= 5000) {
			cancelFinishTask();
			checkRank(instanceReward.getPoints());
		 }
		 sendPacket(npc.getObjectTemplate().getNameId(), points);
	  }
   }

   protected int checkRank(int totalPoints) {
	  int rank = 0;
	  if (totalPoints >= 5000) {
		 instanceReward.setFinalAp(1402);
		 instanceReward.setRewardItem1(186000241);
		 instanceReward.setRewardItem1Count(12);
		 instanceReward.setRewardItem2(186000242);
		 instanceReward.setRewardItem2Count(1);
		 instanceReward.setRewardItem3(188052543);
		 instanceReward.setRewardItem3Count(1);
		 rank = 1;
	  }
	  else if (totalPoints >= 3500) {
		 instanceReward.setFinalAp(1020);
		 instanceReward.setRewardItem1(186000241);
		 instanceReward.setRewardItem1Count(8);
		 instanceReward.setRewardItem2(186000243);
		 instanceReward.setRewardItem2Count(4);
		 instanceReward.setRewardItem3(188052547);
		 instanceReward.setRewardItem3Count(1);
		 rank = 2;
	  }
	  else if (totalPoints > 2700) {
		 instanceReward.setFinalAp(892);
		 instanceReward.setRewardItem1(186000241);
		 instanceReward.setRewardItem1Count(7);
		 instanceReward.setRewardItem2(186000243);
		 instanceReward.setRewardItem2Count(2);
		 rank = 3;
	  }
	  else if (totalPoints > 2200) {
		 instanceReward.setFinalAp(765);
		 instanceReward.setRewardItem1(186000241);
		 instanceReward.setRewardItem1Count(6);
		 rank = 4;
	  }
	  else if (totalPoints > 1600) {
		 instanceReward.setFinalAp(382);
		 instanceReward.setRewardItem1(186000241);
		 instanceReward.setRewardItem1Count(3);
		 rank = 5;
	  }
	  else {
		 rank = 8;
	  }
	  instanceReward.setInstanceScoreType(InstanceScoreType.END_PROGRESS);
	  instanceReward.setRank(rank);
	  for (Npc npc : instance.getNpcs()) {
		 npc.getController().onDelete();
	  }
	  doReward(rank);
	  spawn(730730, 571.98926f, 445.22763f, 104.03148f, (byte) 31);
	  return rank;
   }

   private void doReward(int rank) {
	  for (Player player : instance.getPlayersInside()) {
		 float playerRate = player.getRates().getIdgelResearchCenterRewardRate();
		 switch (rank) {
			//TODO if needed

		 }
		 AbyssPointsService.addAp(player, instanceReward.getFinalAp());
		 ItemService.addItem(player, instanceReward.getRewardItem1(), instanceReward.getRewardItem1Count());
		 ItemService.addItem(player, instanceReward.getRewardItem2(), instanceReward.getRewardItem2Count());
		 ItemService.addItem(player, instanceReward.getRewardItem3(), instanceReward.getRewardItem3Count());
		 ItemService.addItem(player, instanceReward.getRewardItem4(), instanceReward.getRewardItem4Count());
		 sendPacket(0, 0);

	  }
   }

   @Override
   public void onLeaveInstance(Player player) {
	  removeEffects(player);
   }

   @Override
   public void onPlayerLogOut(Player player) {
	  removeEffects(player);
   }

   private void removeEffects(Player player) {
	  PlayerEffectController effectController = player.getEffectController();
	  effectController.removeEffect(21395); // mortar buff
   }

   @Override
   public void handleUseItemFinish(Player player, Npc npc) {

	  switch (npc.getNpcId()) {
		 case 800568:
			npc.getController().onDelete();
			break;
		 case 700642:
			SkillEngine.getInstance().applyEffectDirectly(21395, player, player, 0);
			npc.getController().onDelete();
			break;

	  }

	  if (!instanceReward.isStartProgress() || player == null) {
		 return;
	  }
	  int points = getNpcBonus(npc.getNpcId());

	  if (instanceReward.getInstanceScoreType().isStartProgress()) {
		 instanceReward.addPoints(points);
		 sendPacket(npc.getObjectTemplate().getNameId(), points);
	  }
   }

   private int getNpcBonus(int npcId) {
	  switch (npcId) {
		 case 800568: // Runaway Reian
			return 400;
		 default:
			return 0;
	  }
   }

   private int getTime() {
	  long result = System.currentTimeMillis() - startTime;
	  if (result < 120000) {
		 return (int) (120000 - result);
	  }
	  else if (result < 720000) {
		 return (int) (600000 - (result - 120000));
	  }
	  return 0;
   }

   private void sendPacket(final int nameId, final int point) {
	  instance.doOnAllPlayers(new Visitor<Player>() {

		 @Override
		 public void visit(Player player) {
			if (nameId != 0) {
			   PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400237, new DescriptionId(nameId * 2 + 1), point));
			}
			PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(new NormalScoreInfo(instanceReward), instanceReward, getTime()));
		 }
	  });
   }

   private int calculatePointsReward(Npc npc) {
	  int pointsReward = 0;
	  switch (npc.getObjectTemplate().getTemplateId()) {
		 // Reian Victim
		 case 230119:
		 case 233163:
		 case 233164:
		 case 233165:
		 case 233166:
		 case 233167:
		 case 233168:
		 case 233169:
		 case 233170:
		 case 233171:
		 case 233172:
		 case 233173:
		 case 233174:
		 case 233175:
		 case 233176:
			pointsReward = 20;
			break;
		 // Encrusted Sparkle
		 case 230117:
			pointsReward = 80;
			break;
				// Speedy Skarpa
		 // Jakka The Swift
		 // Windborne Trana
		 // Parku Wallwalker
		 case 230122:
		 case 230373:
		 case 230374:
		 case 230375:
			pointsReward = 150;
			break;
		 // Sheban Drakan Bladesman
		 case 230120:
			pointsReward = 200;
			break;
				// Marabata The Furious
		 // Purifying Sweetsteppe
		 // Unmaker
		 case 230107:
		 case 230112:
		 case 230110:
			pointsReward = 350;
			break;
				// Weaponized Ragnarok
		 // Reforged ra-45c
		 // Evolved Scar
		 // Chieftain Sukka
		 case 230113:
		 case 230114:
		 case 230115:
		 case 230116:
			pointsReward = 400;
			break;
				// Lightning Engine
		 // Darkcaller Macunbello
		 // Flame Beast
		 case 230108:
		 case 230111:
		 case 230106:
			pointsReward = 500;
			break;
		 // Retributive Effigy
		 case 230121:
			pointsReward = 600;
			break;

	  }
	  return pointsReward;
   }

   @Override
   public void onEnterInstance(final Player player) {
	  sendPacket(0, 0);
   }

   @Override
   public void onInstanceDestroy() {
	  if (instanceTimer != null) {
		 instanceTimer.cancel(false);
	  }
	  cancelFinishTask();
	  cancelCheckTask();
	  isInstanceDestroyed = true;
	  doors.clear();
   }

   @Override
   public void onInstanceCreate(final WorldMapInstance instance) {
	  super.onInstanceCreate(instance);
	  instanceReward = new NormalReward(mapId, instanceId);
	  instanceReward.setInstanceScoreType(InstanceScoreType.PREPARING);
	  doors = instance.getDoors();
	  if (instanceTimer == null) {
		 startTime = System.currentTimeMillis();
		 instanceTimer = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
					// instanceReward.setInstanceScoreType(InstanceScoreType.START_PROGRESS);
			   // sendPacket(0, 0);
			   doors.get(32).setOpen(true);
			   onOpenDoor(32);
			   // startFailTask();
			}
		 }, 122000);
	  }
	  checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
		 @Override
		 public void run() {
			instance.doOnAllPlayers(new Visitor<Player>() {
			   @Override
			   public void visit(Player p) {
				  if (p.isOnline() && p.getTransformModel().getPanelId() == 28) {
					 //TODO

				  }
			   }
			});
		 }
	  }, 10000, 10000);
	  spawnRooms();
	  spawnRandomChest();
   }

   private void spawnRandomChest() {
	  switch ((int) Rnd.get(1, 9)) {
		 case 1:
			spawn(701935, 606.4862f, 396.1939f, 97.3460f, (byte) 45);
			break;
		 case 2:
			spawn(701935, 476.90292f, 428.57773f, 103.11591f, (byte) 0);
			break;
		 case 3:
			spawn(701935, 491.04907f, 511.72363f, 110.1904f, (byte) 5);
			break;
		 case 4:
			spawn(701935, 499.31552f, 430.54712f, 103.11591f, (byte) 29);
			break;
		 case 5:
			spawn(701935, 519.22137f, 545.4762f, 110.1904f, (byte) 95);
			break;
		 case 6:
			spawn(701935, 591.52991f, 506.46173f, 103.11591f, (byte) 85);
			break;
		 case 7:
			spawn(701935, 614.59595f, 502.09766f, 103.11591f, (byte) 53);
			break;
		 case 8:
			spawn(701935, 629.73999f, 433.44684f, 97.723907f, (byte) 65);
			break;
		 case 9:
			spawn(701935, 637.05994f, 506.41516f, 103.11591f, (byte) 90);
			break;

	  }
   }

   private void spawnRooms() {

	  if ((int) Rnd.get(1, 2) == 1) {
		 // Retributive Effigy (600P)
		 spawn(230121, 626.6431f, 431.9898f, 97.206f, (byte) 63);
		 // Retributive Effigy (600P)
		 spawn(230121, 604.7129f, 399.8885f, 97.346f, (byte) 33);
	  }
	  else {
		 // Retributive Effigy (600P)
		 spawn(230121, 626.6431f, 431.9898f, 97.206f, (byte) 63);
			//Total (600P)
		 // Jakka The Swift (150P)
		 spawn(230373, 600.1626f, 401.4944f, 97.34604f, (byte) 33);
		 // Windborne Trana(150P)
		 spawn(230374, 605.8382f, 403.9611f, 97.34604f, (byte) 33);
		 // Parku Wallwalker (150P)
		 spawn(230375, 607.4053f, 401.4944f, 97.34604f, (byte) 33);
		 // Speedy Skarpa (150P)
		 spawn(230122, 600.1626f, 397.91647f, 97.34604f, (byte) 33);
	  }

	  switch ((int) Rnd.get(1, 2)) {
		 case 1:
			// Marabata The furious (350P)
			spawn(230107, 520.45886f, 542.5273f, 110.51807f, (byte) 96);
			// Evolved Scar (400P)
			spawn(230115, 590.42004f, 500.8304f, 102.68871f, (byte) 84);
			// Flame Beast (500P)
			spawn(230106, 662.9196f, 467.3922f, 102.6400f, (byte) 60);
			if ((int) Rnd.get(1, 2) == 1) {
			   // Runaway Reian (400P)
			   spawn(800568, 494.8226f, 513.0217f, 109.686f, (byte) 0);
			   // Weaponized Ragnarok (400P)
			   spawn(230113, 613.3185f, 504.9615f, 102.6887f, (byte) 66);
			}
			else {
			   // Weaponized Ragnarok (400P)
			   spawn(230113, 494.8226f, 513.0217f, 109.686f, (byte) 0);
			   // Runaway Reian (400P)
			   spawn(800568, 613.3185f, 504.9615f, 102.6887f, (byte) 66);
			}
			break;
		 case 2:
			// Marabata The furious (350P)
			spawn(230107, 520.45886f, 542.5273f, 110.51807f, (byte) 96);
			// Evolved Scar (400P)
			spawn(230115, 590.42004f, 500.8304f, 102.68871f, (byte) 84);
			// Flame Beast (500P)
			spawn(230106, 662.9196f, 467.3922f, 102.6400f, (byte) 60);
			if ((int) Rnd.get(1, 2) == 1) {
			   // Runaway Reian (400P)
			   spawn(800568, 494.8226f, 513.0217f, 109.686f, (byte) 0);
			   // Weaponized Ragnarok (400P)
			   spawn(230113, 613.3185f, 504.9615f, 102.6887f, (byte) 66);
			}
			else {
			   // Weaponized Ragnarok (400P)
			   spawn(230113, 494.8226f, 513.0217f, 109.686f, (byte) 0);
			   // Runaway Reian (400P)
			   spawn(800568, 613.3185f, 504.9615f, 102.6887f, (byte) 66);
			}
			break;
	  }
	  switch ((int) Rnd.get(1, 2)) {
		 case 1:
			if ((int) Rnd.get(1, 2) == 1) {
			   // Reforged ra-45c (400P)
			   spawn(230114, 501.5816f, 433.8426f, 102.64236f, (byte) 30);
			}
			else {
					// Total (400P)
			   // Encrusted Sparkle
			   spawn(230117, 498.44806f, 437.47876f, 102.64236f, (byte) 30);
			   // Encrusted Sparkle
			   spawn(230117, 498.44806f, 433.40918f, 102.64236f, (byte) 30);
			   // Encrusted Sparkle
			   spawn(230117, 501.2192f, 435.331f, 102.64236f, (byte) 30);
			   // Encrusted Sparkle
			   spawn(230117, 503.59314f, 437.3477f, 102.64236f, (byte) 30);
			   // Encrusted Sparkle
			   spawn(230117, 503.59314f, 433.40918f, 102.64236f, (byte) 30);
			}
			// Unmaker (350P)
			spawn(230110, 482.32465f, 429.76196f, 103.63428f, (byte) 15);
			// Lightning Engine (500P)
			spawn(230108, 478.8857f, 468.1526f, 102.6373f, (byte) 0);
			// Electrified Reian
			spawn(701883, 497.5672f, 462.6087f, 102.6931f, (byte) 0);
			break;
		 case 2:
			if ((int) Rnd.get(1, 2) == 1) {
			   // Chieftain Sukka (400P)
			   spawn(230116, 501.5816f, 433.8426f, 102.64236f, (byte) 30);
			}
			else {
					// Total (400P)
			   // Encrusted Sparkle
			   spawn(230117, 498.44806f, 437.47876f, 102.64236f, (byte) 30);
			   // Encrusted Sparkle
			   spawn(230117, 498.44806f, 433.40918f, 102.64236f, (byte) 30);
			   // Encrusted Sparkle
			   spawn(230117, 501.2192f, 435.331f, 102.64236f, (byte) 30);
			   // Encrusted Sparkle
			   spawn(230117, 503.59314f, 437.3477f, 102.64236f, (byte) 30);
			   // Encrusted Sparkle
			   spawn(230117, 503.59314f, 433.40918f, 102.64236f, (byte) 30);
			}
			// Purifying Sweetsteppe (350P)
			spawn(230112, 482.32465f, 429.76196f, 103.63428f, (byte) 15);
			//Darkcaller Macunbello (500P)
			spawn(230111, 478.8857f, 468.1526f, 102.6373f, (byte) 0);
			// Rotting Reian
			spawn(701886, 497.5672f, 462.6087f, 102.6931f, (byte) 0);
			break;
	  }
   }

   private void startFinishTask() {
	  finishTimerTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
		 @Override
		 public void run() {
			checkRank(instanceReward.getPoints());
		 }
	  }, 600000);
   }

   private void cancelFinishTask() {
	  if (finishTimerTask != null && !finishTimerTask.isCancelled()) {
		 finishTimerTask.cancel(true);
	  }
   }

   private void cancelCheckTask() {
	  if (checkTask != null && !checkTask.isCancelled()) {
		 checkTask.cancel(true);
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
   public void onExitInstance(Player player) {
	  if (instanceReward.getInstanceScoreType().isEndProgress()) {
		 TeleportService2.moveToInstanceExit(player, mapId, player.getRace());
	  }
   }
}
