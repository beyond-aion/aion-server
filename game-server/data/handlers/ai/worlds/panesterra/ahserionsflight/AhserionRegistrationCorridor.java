package ai.worlds.panesterra.ahserionsflight;

import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIRequest;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.panesterra.PanesterraService;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeam;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Yeats, Estrayl
 */
@AIName("ahserion_registration_corridor")
public class AhserionRegistrationCorridor extends GeneralNpcAI {

	private PanesterraFaction faction;

	public AhserionRegistrationCorridor(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_SVS_INVADE_DIRECT_PORTAL_OPEN());
		switch (getNpcId()) {
			case 802219 -> faction = PanesterraFaction.BELUS;
			case 802221 -> faction = PanesterraFaction.ASPIDA;
			case 802223 -> faction = PanesterraFaction.ATANATOS;
			case 802225 -> faction = PanesterraFaction.DISILLON;
		}
		getOwner().getController().addTask(TaskId.DESPAWN,
			ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().deleteIfAliveOrCancelRespawn(), 10, TimeUnit.MINUTES));
	}

	@Override
	public void handleDialogStart(Player player) {
		if (player.getLevel() < 65) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_SVS_DIRECT_PORTAL_LEVEL_LIMIT());
			return;
		}
		AIActions.addRequest(this, player, SM_QUESTION_WINDOW.STR_ASK_PASS_BY_SVS_DIRECT_PORTAL, new AIRequest() {

			@Override
			public void acceptRequest(Creature requester, Player responder, int requestId) {
				if (!AhserionRaid.getInstance().isStarted()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CANT_READY_PANGAEA());
					return;
				} else if (PanesterraService.getInstance().getTeamMemberCount(faction) >= SiegeConfig.AHSERION_MAX_PLAYERS_PER_TEAM) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_SVS_DIRECT_PORTAL_USE_COUNT_LIMIT());
					return;
				}
				PanesterraTeam team = PanesterraService.getInstance().getTeam(faction);
				team.addTeamMemberIfAbsent(player.getObjectId());
				team.movePlayerToStartPosition(player);
				player.setPanesterraFaction(team.getFaction());
			}
		});
	}

}
