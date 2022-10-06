package instance;

import java.util.Arrays;
import java.util.List;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.instance.handlers.InstanceID;
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
 * @author Estrayl
 */
@InstanceID(300630000)
public class AnguishedDragonLordsRefugeInstance extends DragonLordsRefugeInstance {

	public AnguishedDragonLordsRefugeInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onSpawn(VisibleObject object) {
		if (object instanceof Npc npc) {
			switch (npc.getNpcId()) {
				case 236276: // Tiamat Dragon
					ThreadPoolManager.getInstance().schedule(() -> {
						spawn(730673, 459.548f, 456.849f, 417.405f, (byte) 21);
						spawn(730674, 547.909f, 456.568f, 417.405f, (byte) 45);
						spawn(730675, 460.082f, 571.978f, 417.405f, (byte) 98);
						spawn(730676, 547.822f, 571.876f, 417.405f, (byte) 74);
					}, 60000);
					break;
				case 236277: // Tiamat Weakened Dragon
					sendMsg(SM_SYSTEM_MESSAGE.IDTIAMAT_TIAMAT_COUNTDOWN_START(), 2000);
					PacketSendUtility.broadcastToMap(instance, new SM_QUEST_ACTION(0, 1800));
					failTask = ThreadPoolManager.getInstance().schedule(() -> {
						Npc tiamat = getNpc(236277);
						if (tiamat != null && !tiamat.isDead()) {
							endInstance(Arrays.asList(730625, 730633, 730634, 730635, 730636));
							sendMsg(SM_SYSTEM_MESSAGE.IDTIAMAT_TIAMAT_COUNTDOWN_OVER());
						}
					}, 1800000); // 30'
					break;
				case 856020: // Kaisinel 1st Phase
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_TIAMAT_2PHASE_START_LIGHT(), 20000);
					break;
				case 856021: // Kaisinel 2nd Phase
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_KAISINEL_2PHASE_DEADLYATK(), 3000);
					break;
				case 856022: // Kaisinel 3rd Phase
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_KAISINEL_2PHASE_GROGGY(), 15000);
					break;
				case 856023: // Marchutan 1st Phase
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_TIAMAT_2PHASE_START_DARK(), 20000);
					break;
				case 856024: // Marchutan 2nd Phase
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_MARCHUTAN_2PHASE_DEADLYATK(), 3000);
					break;
				case 856025: // Marchutan 3rd Phase
					sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_MARCHUTAN_2PHASE_GROGGY(), 15000);
					break;
				case 856026:
					sendMsg(SM_SYSTEM_MESSAGE.STR_IDTIAMAT_TIAMAT_SPAWN_BLACKHOLE(), 2500);
					break;
				case 730673:
					ThreadPoolManager.getInstance().schedule(() -> {
						spawn(856483, 463f, 568f, 417.405f, (byte) 105);
						spawn(856484, 545f, 568f, 417.405f, (byte) 78);
						spawn(856485, 545f, 461f, 417.405f, (byte) 46);
						spawn(856486, 463f, 461f, 417.405f, (byte) 17);
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
					ThreadPoolManager.getInstance().schedule(() -> spawn(856020 + raceId.get() * 3, 504f, 515f, 417.405f, (byte) 60), 6000);
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
				if (((Npc) effect.getEffector()).getNpcId() == 236276)
					endInstance(Arrays.asList(730625, 730633, 730634, 730635, 730636));
				break;
		}
	}

	private void endInstance(List<Integer> despawnExceptions) {
		instance.forEachNpc(npc -> {
			if (!despawnExceptions.contains(npc.getNpcId()))
				npc.getController().delete();
		});
		spawn(833482, 548.18683f, 514.54523f, 420f, (byte) 0, 23);
	}

	@Override
	public void onDie(Npc npc) {
		super.onDie(npc);
		switch (npc.getNpcId()) {
			case 236274: // Calindi Flamelord
				deleteAliveNpcs(730695, 730696); // Surkana
				ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(getNpc(236275).getAi(), 20919), 4000);
				ThreadPoolManager.getInstance().schedule(() -> deleteAliveNpcs(730694), 6000); // Aetheric field
				break;
			case 236276: // Tiamat Dragon - killed by Empyrean Lord
				getNpc(730699).getController().die(); // Animates roof destruction
				getNpc(730700).getController().die();
				spawn(283134, 451.97f, 514.55f, 417.40436f, (byte) 0);
				spawn(236277, 451.97f, 514.55f, 417.40436f, (byte) 0);
				spawn(730704, 437.541f, 513.487f, 415.824f, (byte) 0, 17); // Collapsed Debris impaling Tiamat
				break;
			case 236277:
				failTask.cancel(true);
				PacketSendUtility.broadcastToMap(instance, new SM_QUEST_ACTION(0, 0));
				// TODO: play movie
				endInstance(Arrays.asList(219490, 219493, 219362, 730625, 730633, 730634, 730635, 730636));
				spawn(702729, 480f, 514f, 417.405f, (byte) 0); // Treasure Chest
				spawn(800430, 506.8f, 511.4f, 417.405f, (byte) 60); // Kahrun
				spawn(800350 + raceId.get() * 6, 506.7f, 518.4f, 417.405f, (byte) 60); // human Kaisinel/Marchutan
				spawn(800464, 544.964f, 517.898f, 417.405f, (byte) 113);
				spawn(800465, 545.605f, 510.325f, 417.405f, (byte) 17);
				break;
			case 236278: // Incarnations
			case 236279:
			case 236280:
			case 236281:
				if (incarnationKills.incrementAndGet() == 4)
					handlePhaseProgress();
				SkillEngine.getInstance().applyEffectDirectly(npc.getNpcId() - 215299, npc, getNpc(236276)); // 20979 - 20982
				break;
			case 856483: // Balaur Spiritualist
			case 856484:
			case 856485:
			case 856486:
				Npc empyreanLord = getNpc(856020 + raceId.get() * 3);
				if (empyreanLord != null)
					SkillEngine.getInstance().applyEffectDirectly(npc.getNpcId() - 835490, npc, empyreanLord); // 20993 - 20996
				npc.getController().delete();
				break;
			case 856020: // Kaisinel 1st Phase
			case 856023: // Marchutan 1st Phase
				if (incarnationKills.get() == 4) // in rare cases the empyrean lord dies while the instance have to wait for progress
					return;
				sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_TIAMAT_DEADLYHOWLING());
				for (int i = 283163; i <= 283166; i++) {
					Npc incarnation = getNpc(i);
					if (incarnation != null)
						AIActions.useSkill(incarnation.getAi(), 20983);
				}
				AIActions.useSkill(getNpc(236276).getAi(), 20983);
				ThreadPoolManager.getInstance().schedule(() -> endInstance(Arrays.asList(730625, 730633, 730634, 730635, 730636, 730699, 730700)), 7000);
				break;
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		super.handleUseItemFinish(player, npc);
		if (npc.getNpcId() == 833482) // Exit
			TeleportService.moveToInstanceExit(player, mapId, player.getRace());
	}

	private void handlePhaseProgress() {
		sendMsg(SM_SYSTEM_MESSAGE.STR_MSG_IDTIAMAT_TIAMAT_2PHASE_CLOSE_ALL());

		int empyreanLordId = 856020 + raceId.get() * 3;
		getNpc(236276).getEffectController().removeEffect(20984); // Dispel Unbreakable Wing

		// schedule spawn of empyrean lords for final attack to tiamat before getting exhausted
		ThreadPoolManager.getInstance().schedule(() -> {
			deleteAliveNpcs(empyreanLordId);
			spawn(856021 + raceId.get() * 3, 516.285f, 514.84f, 417.405f, (byte) 60);
		}, 30000);
	}

	@Override
	protected void spawnTiamat() {
		spawn(236275, 452, 514, 432, (byte) 0);
	}

	@Override
	protected void spawnCalindi() {
		spawn(236274, 483.463f, 514.519f, 417.404f, (byte) 0);
	}
}
