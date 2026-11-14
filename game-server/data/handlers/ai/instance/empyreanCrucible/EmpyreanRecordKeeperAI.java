package ai.instance.empyreanCrucible;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz, Luzien, w4terbomb
 */
@AIName("empyreanrecordkeeper")
public class EmpyreanRecordKeeperAI extends NpcAI {

	private final AtomicBoolean greeted = new AtomicBoolean();

	public EmpyreanRecordKeeperAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		int msg = switch (getNpcId()) {
			case 799568 -> 1111451;
			case 799569 -> 1111452;
			case 205331 -> 1111453;
			case 205332 -> 1111454;
			case 205333 -> 1111455;
			case 205334 -> 1111456;
			case 205335 -> 1111457;
			case 205336 -> 1111458;
			case 205337 -> 1111459;
			case 205338 -> 1111463;
			case 205339 -> 1111464;
			case 205340 -> 1111465;
			case 205341 -> 1111466;
			case 205342 -> 1111467;
			case 205343 -> 1111468;
			case 205344 -> 1111469;
			default -> 0;
		};
		if (msg != 0)
			PacketSendUtility.broadcastMessage(getOwner(), msg, 1000);
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		super.handleCreatureSee(creature);
		// the first record keeper is spawned with the instance, before anyone can hear it
		if (getNpcId() == 799567 && creature instanceof Player && greeted.compareAndSet(false, true))
			PacketSendUtility.broadcastMessage(getOwner(), 1111450, 1000);
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
				case 799567 -> instanceHandler.onChangeStage(StageType.START_STAGE_1_ELEVATOR);
				case 799568 -> instanceHandler.onChangeStage(StageType.START_STAGE_2_ELEVATOR);
				case 799569 -> instanceHandler.onChangeStage(StageType.START_STAGE_3_ELEVATOR);
				case 205331 -> instanceHandler.onChangeStage(StageType.START_STAGE_4_ELEVATOR);
				case 205338 -> instanceHandler.onChangeStage(StageType.START_STAGE_5);
				case 205332 -> instanceHandler.onChangeStage(StageType.START_STAGE_5_ROUND_1);
				case 205339 -> instanceHandler.onChangeStage(StageType.START_STAGE_6);
				case 205333 -> instanceHandler.onChangeStage(StageType.START_STAGE_6_ROUND_1);
				case 205340 -> instanceHandler.onChangeStage(StageType.START_STAGE_7);
				case 205334 -> instanceHandler.onChangeStage(StageType.START_STAGE_7_ROUND_1);
				case 205341 -> instanceHandler.onChangeStage(StageType.START_STAGE_8);
				case 205335 -> instanceHandler.onChangeStage(StageType.START_STAGE_8_ROUND_1);
				case 205342 -> instanceHandler.onChangeStage(StageType.START_STAGE_9);
				case 205336 -> instanceHandler.onChangeStage(StageType.START_STAGE_9_ROUND_1);
				case 205343 -> instanceHandler.onChangeStage(StageType.START_STAGE_10);
				case 205337 -> instanceHandler.onChangeStage(StageType.START_STAGE_10_ROUND_1);
				case 205344 -> instanceHandler.onChangeStage(StageType.PASS_GROUP_STAGE_10); // ends the game for everyone, the keeper stays
			}
			if (getNpcId() != 205344)
				AIActions.deleteOwner(this);
		} else if (dialogActionId == SETPRO2 && getNpcId() == 799567) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
			instanceHandler.onChangeStage(StageType.START_STAGE_7);
			AIActions.deleteOwner(this);
		}
		return true;
	}
}
