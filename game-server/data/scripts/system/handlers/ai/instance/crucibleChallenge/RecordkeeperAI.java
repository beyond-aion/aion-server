package ai.instance.crucibleChallenge;

import static com.aionemu.gameserver.model.DialogAction.SETPRO1;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
@AIName("recordkeeper")
public class RecordkeeperAI extends NpcAI {

	public RecordkeeperAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		int instanceId = getPosition().getInstanceId();
		InstanceHandler instanceHandler = getPosition().getWorldMapInstance().getInstanceHandler();
		if (dialogActionId == SETPRO1) {
			switch (getNpcId()) {
				case 205668: // start stage 1
					instanceHandler.onChangeStage(StageType.START_STAGE_1_ROUND_1);
					break;
				case 205674: // move to stage 2
					TeleportService.teleportTo(player, 300320000, instanceId, 1796.5513f, 306.9967f, 469.25f, (byte) 60);
					spawn(205683, 1821.5643f, 311.92484f, 469.4562f, (byte) 60);
					spawn(205669, 1784.4633f, 306.98645f, 469.25f, (byte) 0);
					break;
				case 205669: // start stage 2
					instanceHandler.onChangeStage(StageType.START_STAGE_2_ROUND_1);
					break;
				case 205675: // move to stage 3
					TeleportService.teleportTo(player, 300320000, instanceId, 1324.433f, 1738.2279f, 316.476f, (byte) 70);
					spawn(205684, 1358.4021f, 1758.744f, 319.1873f, (byte) 70);
					spawn(205670, 1307.5472f, 1732.9865f, 316.0777f, (byte) 6);
					break;
				case 205670: // start stage 3
					instanceHandler.onChangeStage(StageType.START_STAGE_3_ROUND_1);
					break;
				case 205676: // movet to stage 4
					switch (Rnd.get(1, 2)) {
						case 1:
							TeleportService.teleportTo(player, 300320000, instanceId, 1283.1246f, 791.6683f, 436.6403f, (byte) 60);
							spawn(205685, 1308.9664f, 796.20276f, 437.29678f, (byte) 60);
							spawn(205671, 1271.4222f, 791.36145f, 436.64017f, (byte) 0);
							break;
						case 2:
							TeleportService.teleportTo(player, 300320000, instanceId, 1270.8877f, 237.93307f, 405.38028f, (byte) 60);
							spawn(205663, 1295.7217f, 242.15009f, 406.03677f, (byte) 60);
							spawn(205666, 1258.7214f, 237.85518f, 405.3968f, (byte) 0);
							break;
					}
					break;
				case 205666: // start stage 4
					instanceHandler.onChangeStage(StageType.START_ALTERNATIVE_STAGE_4_ROUND_1);
					break;
				case 205671:
					instanceHandler.onChangeStage(StageType.START_STAGE_4_ROUND_1);
					break;
				case 205667: // move to stage 5
				case 205677:
					TeleportService.teleportTo(player, 300320000, instanceId, 357.98798f, 349.19116f, 96.09108f, (byte) 60);
					spawn(205686, 383.30933f, 354.07846f, 96.07846f, (byte) 60);
					spawn(205672, 346.52298f, 349.25586f, 96.0098f, (byte) 0);
					break;
				case 205672: // start stage 5
					instanceHandler.onChangeStage(StageType.START_STAGE_5_ROUND_1);
					break;
				case 205678: // move to stage 6
					TeleportService.teleportTo(player, 300320000, instanceId, 1759.5004f, 1273.5414f, 389.11743f, (byte) 10);
					spawn(205687, 1747.3901f, 1250.201f, 389.11765f, (byte) 16);
					spawn(205673, 1767.1036f, 1288.4425f, 389.11728f, (byte) 76);
					break;
				case 205673: // start stage 6
					instanceHandler.onChangeStage(StageType.START_STAGE_6_ROUND_1);
					break;
				case 205679: // get score
					getPosition().getWorldMapInstance().getInstanceHandler().doReward(player);
					break;
			}
			if (getNpcId() != 205679) {
				AIActions.deleteOwner(this);
			}
		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}
}
