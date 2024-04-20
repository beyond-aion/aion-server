package ai.instance.empyreanCrucible;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz, Luzien
 */
@AIName("empyreanrecordkeeper")
public class EmpyreanRecordKeeperAI extends NpcAI {

	public EmpyreanRecordKeeperAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		int msg = 0;
		switch (getNpcId()) {
			case 799568:
				msg = 1111460;
				break;
			case 799569:
				msg = 1111461;
				break;
			case 205331:
				msg = 1111462;
				break;
			case 205338:
				msg = 1111463;
				break;
			case 205339:
				msg = 1111464;
				break;
			case 205340:
				msg = 1111465;
				break;
			case 205341:
				msg = 1111466;
				break;
			case 205342:
				msg = 1111467;
				break;
			case 205343:
				msg = 1111468;
				break;
			case 205344:
				msg = 1111469;
		}
		if (msg != 0) {
			PacketSendUtility.broadcastMessage(getOwner(), msg, 1000);
		}
	}

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		InstanceHandler instanceHandler = getPosition().getWorldMapInstance().getInstanceHandler();
		if (dialogActionId == SETPRO1) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			switch (getNpcId()) {
				case 799567:
					instanceHandler.onChangeStage(StageType.START_STAGE_1_ELEVATOR);
					break;
				case 799568:
					instanceHandler.onChangeStage(StageType.START_STAGE_2_ELEVATOR);
					break;
				case 799569:
					instanceHandler.onChangeStage(StageType.START_STAGE_3_ELEVATOR);
					break;
				case 205331:
					instanceHandler.onChangeStage(StageType.START_STAGE_4_ELEVATOR);
					break;
				case 205338: // teleport to stage 5
					instanceHandler.onChangeStage(StageType.START_STAGE_5);
					break;
				case 205332:
					instanceHandler.onChangeStage(StageType.START_STAGE_5_ROUND_1);
					break;
				case 205339: // teleport to stage 6
					instanceHandler.onChangeStage(StageType.START_STAGE_6);
					break;
				case 205333:
					instanceHandler.onChangeStage(StageType.START_STAGE_6_ROUND_1);
					break;
				case 205340: // teleport to stage 7
					instanceHandler.onChangeStage(StageType.START_STAGE_7);
					break;
				case 205334:
					instanceHandler.onChangeStage(StageType.START_STAGE_7_ROUND_1);
					break;
				case 205341: // teleport to stage 8
					instanceHandler.onChangeStage(StageType.START_STAGE_8);
					break;
				case 205335:
					instanceHandler.onChangeStage(StageType.START_STAGE_8_ROUND_1);
					break;
				case 205342: // teleport to stage 9
					instanceHandler.onChangeStage(StageType.START_STAGE_9);
					break;
				case 205336:
					instanceHandler.onChangeStage(StageType.START_STAGE_9_ROUND_1);
					break;
				case 205343: // teleport to stage 9
					instanceHandler.onChangeStage(StageType.START_STAGE_10);
					break;
				case 205337:
					instanceHandler.onChangeStage(StageType.START_STAGE_10_ROUND_1);
					break;
				case 205344: // get score
					getPosition().getWorldMapInstance().getInstanceHandler().doReward(player);
					break;
			}
			AIActions.deleteOwner(this);
		} else if (dialogActionId == SETPRO2 && getNpcId() == 799567) { // start with stage 7
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			instanceHandler.onChangeStage(StageType.START_STAGE_7);
			AIActions.deleteOwner(this);
		}
		return true;
	}
}
