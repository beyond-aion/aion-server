package instance;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author bobobear, Luzien, Estrayl
 */
@InstanceID(300520000)
public class DragonLordsRefugeInstance extends GeneralInstanceHandler {

	protected AtomicInteger raceId = new AtomicInteger(10);
	protected AtomicInteger incarnationKills = new AtomicInteger();
	protected AtomicInteger progress = new AtomicInteger();
	protected Future<?> failTask;

	public DragonLordsRefugeInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onSpawn(VisibleObject object) {
		if (object instanceof Npc) {
			switch (((Npc) object).getNpcId()) {
				case 219361: // Tiamat Dragon
					ThreadPoolManager.getInstance().schedule(() -> {
						spawn(730673, 459.548f, 456.849f, 417.405f, (byte) 21);
						spawn(730674, 547.909f, 456.568f, 417.405f, (byte) 45);
						spawn(730675, 460.082f, 571.978f, 417.405f, (byte) 98);
						spawn(730676, 547.822f, 571.876f, 417.405f, (byte) 74);
					}, 60000);
					break;
				case 219362: // Tiamat Weakened Dragon
					sendMsg(SM_SYSTEM_MESSAGE.IDTIAMAT_TIAMAT_COUNTDOWN_START(), 2000);
					PacketSendUtility.broadcastToMap(instance, new SM_QUEST_ACTION(0, 1800));
					failTask = ThreadPoolManager.getInstance().schedule(() -> {
						Npc tiamat = getNpc(219362);
						if (tiamat != null && !tiamat.isDead()) {
							endInstance(Arrays.asList(730625, 730633, 730634, 730635, 730636));
							sendMsg(SM_SYSTEM_MESSAGE.IDTIAMAT_TIAMAT_COUNTDOWN_OVER());
						}
					}, 1800000); // 30'
					break;
				case 219488: // Kaisinel 1st Phase
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_TIAMAT_2PHASE_START_LIGHT(), 20000);
					break;
				case 219489: // Kaisinel 2nd Phase
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_KAISINEL_2PHASE_DEADLYATK(), 3000);
					break;
				case 219490: // Kaisinel 3rd Phase
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_KAISINEL_2PHASE_GROGGY(), 15000);
					break;
				case 219491: // Marchutan 1st Phase
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_TIAMAT_2PHASE_START_DARK(), 20000);
					break;
				case 219492: // Marchutan 2nd Phase
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_MARCHUTAN_2PHASE_DEADLYATK(), 3000);
					break;
				case 219493: // Marchutan 3rd Phase
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_MARCHUTAN_2PHASE_GROGGY(), 15000);
					break;
				case 283140:
					sendMsg(SM_SYSTEM_MESSAGE.STR_IDTIAMAT_TIAMAT_SPAWN_BLACKHOLE(), 2500);
					break;
				case 730673:
					ThreadPoolManager.getInstance().schedule(() -> {
						spawn(283163, 463f, 568f, 417.405f, (byte) 105);
						spawn(283164, 545f, 568f, 417.405f, (byte) 78);
						spawn(283165, 545f, 461f, 417.405f, (byte) 46);
						spawn(283166, 463f, 461f, 417.405f, (byte) 17);
					}, 10000);
					break;
				case 730695: // Surkana
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_KALYNDI_SURKANA_SPAWN(), 2500);
					break;
			}
		}
	}

	@Override
	public void onStartEffect(Effect effect) {
		switch (effect.getSkillId()) {
			case 20918:
				PacketSendUtility.broadcastMessage(getNpc(800341), 1500612);
				break;
			case 20920:
				if (progress.compareAndSet(1, 2)) {
					ThreadPoolManager.getInstance().schedule(() -> spawn(219488 + raceId.get() * 3, 504f, 515f, 417.405f, (byte) 60), 6000);
					spawn(219532, 469f, 563f, 417.41f, (byte) 103);
					spawn(219535, 466f, 560f, 417.41f, (byte) 103);
					spawn(219533, 542f, 559f, 417.41f, (byte) 79);
					spawn(219538, 538f, 562f, 417.41f, (byte) 79);
					spawn(219534, 537f, 466f, 417.41f, (byte) 42);
					spawn(219537, 541f, 469f, 417.41f, (byte) 42);
					spawn(219536, 466f, 471f, 417.41f, (byte) 18);
					spawn(219539, 470f, 467f, 417.41f, (byte) 18);
				}
				break;
			case 20993:
			case 20994:
			case 20995:
			case 20996:
				sendMsg(SM_SYSTEM_MESSAGE.IDTIAMAT_TIAMAT_DRAKAN_ON_DIE());
				break;
		}
	}

	@Override
	public void onEndEffect(Effect effect) {
		switch (effect.getSkillId()) {
			case 20975:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_TIAMAT_2PHASE_CLOSE_CRACK());
				break;
			case 20976:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_TIAMAT_2PHASE_CLOSE_RAGE());
				break;
			case 20977:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_TIAMAT_2PHASE_CLOSE_GRAVITY());
				break;
			case 20978:
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_TIAMAT_2PHASE_CLOSE_CRYSTAL());
				break;
			case 20983:
				if (((Npc) effect.getEffector()).getNpcId() == 219361)
					endInstance(Arrays.asList(730625, 730633, 730634, 730635, 730636));
				break;
		}
	}

	private void endInstance(List<Integer> despawnExceptions) {
		instance.forEachNpc(npc -> {
			if (!despawnExceptions.contains(npc.getNpcId()))
				npc.getController().delete();
		});
		spawn(730630, 548.18683f, 514.54523f, 420f, (byte) 0, 23);
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 219359: // Calindi Flamelord
				deleteAliveNpcs(730695, 730696); // Surkana
				ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(getNpc(219360).getAi(), 20919), 4000);
				ThreadPoolManager.getInstance().schedule(() -> deleteAliveNpcs(730694), 6000); // Aetheric field
				break;
			case 219361: // Tiamat Dragon - killed by Empyrean Lord
				getNpc(730699).getController().die(); // Animates roof destruction
				getNpc(730700).getController().die();
				spawn(283134, 451.97f, 514.55f, 417.40436f, (byte) 0);
				spawn(219362, 451.97f, 514.55f, 417.40436f, (byte) 0);
				spawn(730704, 437.541f, 513.487f, 415.824f, (byte) 0, 17); // Collapsed Debris impaling Tiamat
				break;
			case 219362:
				failTask.cancel(true);
				PacketSendUtility.broadcastToMap(instance, new SM_QUEST_ACTION(0, 0));
				// TODO: play movie
				endInstance(Arrays.asList(219490, 219493, 219362, 730625, 730633, 730634, 730635, 730636));
				spawn(701542, 480f, 514f, 417.405f, (byte) 0); // Treasure Chest
				spawn(800430, 506.8f, 511.4f, 417.405f, (byte) 60); // Kahrun
				spawn(800350 + raceId.get() * 6, 506.7f, 518.4f, 417.405f, (byte) 60); // human Kaisinel/Marchutan
				spawn(800464, 544.964f, 517.898f, 417.405f, (byte) 113);
				spawn(800465, 545.605f, 510.325f, 417.405f, (byte) 17);
				break;
			case 219365: // Incarnations
			case 219366:
			case 219367:
			case 219368:
				if (incarnationKills.incrementAndGet() == 4)
					handlePhaseProgress();
				SkillEngine.getInstance().applyEffectDirectly(npc.getNpcId() - 198386, npc, getNpc(219361)); // 20979 - 20982
				break;
			case 283163: // Balaur Spiritualist
			case 283164:
			case 283165:
			case 283166:
				Npc empyreanLord = getNpc(219488 + raceId.get() * 3);
				if (empyreanLord != null)
					SkillEngine.getInstance().applyEffectDirectly(npc.getNpcId() - 262170, npc, empyreanLord); // 20993 - 20996
				break;
			case 219488: // Kaisinel 1st Phase
			case 219491: // Marchutan 1st Phase
				if (incarnationKills.get() == 4) // in rare cases the empyrean lord dies while the instance have to wait for progress
					return;
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_TIAMAT_DEADLYHOWLING());
				for (int i = 219365; i <= 219368; i++) {
					Npc incarnation = getNpc(i);
					if (incarnation != null)
						AIActions.useSkill(incarnation.getAi(), 20983);
				}
				AIActions.useSkill(getNpc(219361).getAi(), 20983);
				ThreadPoolManager.getInstance().schedule(() -> endInstance(Arrays.asList(730625, 730633, 730634, 730635, 730636, 730699, 730700)), 5000);
				break;
		}
	}

	private void handlePhaseProgress() {
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_TIAMAT_2PHASE_CLOSE_ALL());

		int empyreanLordId = 219488 + raceId.get() * 3;
		getNpc(219361).getEffectController().removeEffect(20984); // Dispel Unbreakable Wing

		// schedule spawn of empyrean lords for final attack to tiamat before became exhausted
		ThreadPoolManager.getInstance().schedule(() -> {
			deleteAliveNpcs(empyreanLordId);
			spawn(219489 + raceId.get() * 3, 516.285f, 514.84f, 417.405f, (byte) 60);
		}, 30000);
	}

	/**
	 * Smalltalk between Tiamat and Kahrun
	 */
	private void handleDialogueEvent(Npc tiamat) {
		Npc kahrun = getNpc(800341);
		AIActions.targetCreature(tiamat.getAi(), kahrun);
		PacketSendUtility.broadcastMessage(tiamat, 1500613, 5000);
		PacketSendUtility.broadcastMessage(kahrun, 1500609, 10000);
		PacketSendUtility.broadcastMessage(tiamat, 1500614, 15000);
		PacketSendUtility.broadcastMessage(kahrun, 1500610, 20000);
		PacketSendUtility.broadcastMessage(tiamat, 1500615, 26500);
		PacketSendUtility.broadcastMessage(kahrun, 1500611, 31500);
		ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(kahrun.getAi(), 20597), 35500);
		PacketSendUtility.broadcastMessage(tiamat, 1500616, 40000);
		ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(tiamat.getAi(), 20918), 49000);
		PacketSendUtility.broadcastMessage(tiamat, 1500617, 44500);
		ThreadPoolManager.getInstance().schedule(() -> {
			kahrun.getController().delete();
			tiamat.getController().delete();
			spawnTiamat();
			spawnCalindi();
		}, 54500);
	}

	protected void spawnTiamat() {
		spawn(219360, 452, 514, 432, (byte) 0);
	}

	protected void spawnCalindi() {
		spawn(219359, 483.463f, 514.519f, 417.404f, (byte) 0);
	}

	@Override
	public void onEnterInstance(Player player) {
		raceId.compareAndSet(10, player.getRace().getRaceId());
	}

	@Override
	public void onCreatureDetected(Npc detector, Creature detected) {
		if (detector.getNpcId() == 219360 && detected instanceof Player && progress.compareAndSet(0, 1))
			handleDialogueEvent(detector);
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch (npc.getNpcId()) {
			case 730625:
				float distance = Rnd.get(250, 450) * 0.1f;
				double radian = Math.toRadians(Rnd.get(-45, 60));
				float x = (float) (Math.cos(radian) * distance);
				float y = (float) (Math.sin(radian) * distance);
				TeleportService.teleportTo(player, instance.getMapId(), 503.389f + x, 514.661f + y, 417.404f);
				Npc tiamat = getNpc(219360);
				if (tiamat != null)
					tiamat.overrideNpcType(CreatureType.PEACE);
				break;
			case 730630: // Exit
				TeleportService.moveToInstanceExit(player, mapId, player.getRace());
				break;
			case 730673:
				TeleportService.teleportTo(player, instance.getMapId(), 217.144f, 195.616f, 246.0712f);
				break;
			case 730674:
				TeleportService.teleportTo(player, instance.getMapId(), 785.866f, 197.713f, 246.0712f);
				break;
			case 730675:
				TeleportService.teleportTo(player, instance.getMapId(), 217.947f, 832.552f, 246.0712f);
				break;
			case 730676:
				TeleportService.teleportTo(player, instance.getMapId(), 779.178f, 833.055f, 246.0712f);
				break;
			case 730633:
				TeleportService.teleportTo(player, instance.getMapId(), 461.493f, 459.308f, 417.405f);
				break;
			case 730634:
				TeleportService.teleportTo(player, instance.getMapId(), 546f, 458.934f, 417.405f);
				break;
			case 730635:
				TeleportService.teleportTo(player, instance.getMapId(), 462.188f, 568.913f, 417.405f);
				break;
			case 730636:
				TeleportService.teleportTo(player, instance.getMapId(), 545.773f, 569.225f, 417.405f);
				break;
		}
	}

	@Override
	public void onInstanceDestroy() {
		if (failTask != null && !failTask.isCancelled())
			failTask.cancel(true);
	}
}
