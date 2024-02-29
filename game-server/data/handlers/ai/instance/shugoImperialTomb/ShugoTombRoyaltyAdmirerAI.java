package ai.instance.shugoImperialTomb;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;
import static com.aionemu.gameserver.model.DialogAction.SETPRO2;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.StageList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Ritsu
 */
@AIName("shugo_tomb_royalty_admirer")
public class ShugoTombRoyaltyAdmirerAI extends NpcAI {

	public ShugoTombRoyaltyAdmirerAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		int instanceId = player.getInstanceId();

		switch (dialogActionId) {
			case SETPRO1:
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
				switch (getNpcId()) {
					case 831110:
						changeStageList(player, StageList.START_STAGE_1_PHASE_1);
						break;
					case 831111:
						changeStageList(player, StageList.START_STAGE_2_PHASE_1);
						break;
					case 831112:
						changeStageList(player, StageList.START_STAGE_3_PHASE_1);
						break;
					case 831114: // Crown Prince's Delighted Admirer
					case 831306: // Crown Prince's Disappointed Admirer
						TeleportService.teleportTo(player, 300560000, instanceId, 346.27332f, 424.07101f, 294.75793f, (byte) 90, TeleportAnimation.FADE_OUT_BEAM);
						break;
					case 831115: // Empress' Delighted Admirer
					case 831195: // Empress' Disappointed Admirer
						TeleportService.teleportTo(player, 300560000, instanceId, 450.8527f, 105.94637f, 212.20023f, (byte) 90, TeleportAnimation.FADE_OUT_BEAM);
						break;
				}
				break;
			case SETPRO2:
				switch (getNpcId()) {
					case 831114: // Crown Prince's Delighted Admirer
					case 831306: // Crown Prince's Disappointed Admirer
						SkillEngine.getInstance().applyEffectDirectly(player.getRace() == Race.ASMODIANS ? 21104 : 21095, player, player);
						break;
					case 831115: // Empress' Delighted Admirer
					case 831195: // Empress' Disappointed Admirer
						SkillEngine.getInstance().applyEffectDirectly(player.getRace() == Race.ASMODIANS ? 21105 : 21096, player, player);
						break;
				}
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1012));
		}
		return true;
	}

	private void changeStageList(Player player, StageList stageList) {
		getPosition().getWorldMapInstance().getInstanceHandler().onChangeStageList(stageList);
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		AIActions.deleteOwner(this);
	}
}
