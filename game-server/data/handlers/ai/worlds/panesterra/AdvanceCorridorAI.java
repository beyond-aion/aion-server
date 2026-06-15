package ai.worlds.panesterra;

import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIRequest;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.panesterra.PanesterraService;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeam;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("panesterra_advance_corridor")
public class AdvanceCorridorAI extends GeneralNpcAI {

	protected int despawnInMin = 10;

	public AdvanceCorridorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		getOwner().getController().addTask(TaskId.DESPAWN,
			ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().deleteIfAliveOrCancelRespawn(), despawnInMin, TimeUnit.MINUTES));
	}

	@Override
	public void handleDialogStart(Player player) {
		if (player.getAbyssRank().getRank().ordinal() < AbyssRankEnum.STAR1_OFFICER.ordinal()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_TELEPOTER_GAB1_USER07());
			return;
		}
		if (player.getLevel() < 65) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_SVS_DIRECT_PORTAL_LEVEL_LIMIT());
			return;
		}
		PanesterraFaction faction = getFactionToAssign(player);
		AIActions.addRequest(this, player, SM_QUESTION_WINDOW.STR_ASK_PASS_BY_SVS_DIRECT_PORTAL, new AIRequest() {

			@Override
			public void acceptRequest(Creature requester, Player responder, int requestId) {
				if (PanesterraService.getInstance().getTeamMemberCount(faction) >= SiegeConfig.PANESTERRA_MAX_PLAYERS_PER_TEAM) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_SVS_DIRECT_PORTAL_USE_COUNT_LIMIT());
					return;
				}
				PanesterraTeam team = PanesterraService.getInstance().getTeam(faction);
				team.addTeamMemberIfAbsent(player.getObjectId());
				team.movePlayerToStartPosition(player);
			}

		});
	}

	/**
	 * Hard-coded for now
	 * TODO: Faction selection should be moved into a dedicated matchmaking service
	 */
	private PanesterraFaction getFactionToAssign(Player player) {
		return player.getRace() == Race.ELYOS ? PanesterraFaction.IVY_TEMPLE : PanesterraFaction.ALPINE_TEMPLE;
	}
}
