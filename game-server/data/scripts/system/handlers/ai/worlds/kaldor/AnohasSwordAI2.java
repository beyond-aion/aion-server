package ai.worlds.kaldor;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Ritsu
 */
@AIName("anohas_sword")
public class AnohasSwordAI2 extends NpcAI2 {

	@Override
	protected void handleDialogStart(Player player) {
		int siegeId = SiegeService.getInstance().getFortress(7011).getLegionId();
		int legionId = player.getLegion().getLegionId();
		if (legionId == siegeId && player.getLegionMember().isBrigadeGeneral()) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 10));
		} else
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		if (dialogId == DialogAction.SETPRO1.id()) {
			if (player.getInventory().decreaseByItemId(185000215, 1)) {
				World.getInstance().doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player receiver) {
						PacketSendUtility.sendPacket(receiver, new SM_SYSTEM_MESSAGE(1402584)); // Spawn in 30 minutes
					}
				});
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						spawn(855263, 791.38214f, 488.93655f, 143.47517f, (byte) 0); // spawn Berserk Anoha
						World.getInstance().doOnAllPlayers(new Visitor<Player>() {

							@Override
							public void visit(Player receiver) {
								PacketSendUtility.sendPacket(receiver, new SM_SYSTEM_MESSAGE(1402503)); // Msg Anoha has spawned
								despawn(getOwner().getNpcId());
								int race = receiver.getRace().getRaceId();
								switch (race) {
									case 0:
										if (SiegeService.getInstance().getFortress(7011).getRace().getRaceId() == receiver.getRace().getRaceId()) {
											final QuestState qs = receiver.getQuestStateList().getQuestState(13817);
											QuestEnv env = new QuestEnv(null, receiver, 13817, 0);
											if (qs == null || qs.getStatus() == QuestStatus.NONE)
												QuestService.startQuest(env, QuestStatus.START);
										} else {
											final QuestState qs = receiver.getQuestStateList().getQuestState(13818);
											QuestEnv env = new QuestEnv(null, receiver, 13818, 0);
											if (qs == null || qs.getStatus() == QuestStatus.NONE)
												QuestService.startQuest(env, QuestStatus.START);
										}
										break;
									case 1:
										if (SiegeService.getInstance().getFortress(7011).getRace().getRaceId() == receiver.getRace().getRaceId()) {
											final QuestState qs = receiver.getQuestStateList().getQuestState(13817);
											QuestEnv env = new QuestEnv(null, receiver, 23817, 0);
											if (qs == null || qs.getStatus() == QuestStatus.NONE)
												QuestService.startQuest(env, QuestStatus.START);
										} else {
											final QuestState qs = receiver.getQuestStateList().getQuestState(23818);
											QuestEnv env = new QuestEnv(null, receiver, 23818, 0);
											if (qs == null || qs.getStatus() == QuestStatus.NONE)
												QuestService.startQuest(env, QuestStatus.START);
										}
										break;
								}
							}
						});
					}
				}, 1800000);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 2375));
			} else
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 27));
		}
		return true;
	}

	private void despawn(int npcId) {
		for (Npc npc : getPosition().getWorldMapInstance().getNpcs(npcId)) {
			npc.getController().onDelete();
		}
	}
}
