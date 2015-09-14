package ai.instance.engulfedOphidianBridgeInstance;

import java.util.List;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Cheatkiller
 */
@AIName("engulfedophidiangenerals")
public class RaceGeneralsAI2 extends NpcAI2 {

	@Override
	protected void handleDialogStart(Player player) {
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogId, int questId, int extendedRewardIndex) {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		switch (getOwner().getNpcId()) {
			case 701989: // asmo
				switch (dialogId) {
					case 10000:
						deleteNpcs(instance.getNpcs(701987));
						deleteNpcs(instance.getNpcs(701985));
						spawn(233495, 678.5313f, 471.29727f, 599.6582f, (byte) 116);
						spawn(233493, 677.62946f, 468.9142f, 599.625f, (byte) 116);
						spawn(233493, 678.6859f, 473.77505f, 599.6679f, (byte) 116);
						NpcShoutsService.getInstance().sendMsg(getOwner(), 1402060);
						AI2Actions.deleteOwner(this);
						break;
					case 10001:
						deleteNpcs(instance.getNpcs(701987));
						deleteNpcs(instance.getNpcs(701985));
						spawn(233495, 519.66113f, 446.24088f, 620.125f, (byte) 116);
						spawn(233493, 517.5026f, 444.2977f, 620.125f, (byte) 116);
						spawn(233493, 518.73016f, 449.66315f, 620.22894f, (byte) 116);
						NpcShoutsService.getInstance().sendMsg(getOwner(), 1402055);
						AI2Actions.deleteOwner(this);
						break;
					case 10002:
						deleteNpcs(instance.getNpcs(701987));
						deleteNpcs(instance.getNpcs(701985));
						spawn(233495, 603.4207f, 538.19196f, 590.976f, (byte) 28);
						spawn(233493, 600.8837f, 538.39116f, 591.0416f, (byte) 28);
						spawn(233493, 605.8344f, 538.0445f, 590.99445f, (byte) 28);
						NpcShoutsService.getInstance().sendMsg(getOwner(), 1402070);
						AI2Actions.deleteOwner(this);
						break;
					case 10003:
						deleteNpcs(instance.getNpcs(701987));
						deleteNpcs(instance.getNpcs(701985));
						spawn(233495, 481.47342f, 526.27606f, 597.375f, (byte) 19);
						spawn(233493, 479.6977f, 528.80994f, 597.375f, (byte) 19);
						spawn(233493, 481.879f, 523.497f, 597.49243f, (byte) 19);
						NpcShoutsService.getInstance().sendMsg(getOwner(), 1402065);
						AI2Actions.deleteOwner(this);
						break;
				}
				break;
			default: // elyos
				switch (dialogId) {
					case 10000:
						deleteNpcs(instance.getNpcs(701986));
						deleteNpcs(instance.getNpcs(701984));
						spawn(233494, 691.1125f, 467.0932f, 599.875f, (byte) 54);
						spawn(233492, 690.11017f, 464.7947f, 599.875f, (byte) 54);
						spawn(233492, 691.4471f, 471.80127f, 599.84045f, (byte) 54);
						AI2Actions.deleteOwner(this);
						NpcShoutsService.getInstance().sendMsg(getOwner(), 1402060);
						break;
					case 10001:
						deleteNpcs(instance.getNpcs(701986));
						deleteNpcs(instance.getNpcs(701984));
						spawn(233494, 531.18066f, 446.37927f, 620.25f, (byte) 58);
						spawn(233492, 532.5186f, 444.30832f, 620.25f, (byte) 58);
						spawn(233492, 532.3772f, 449.40405f, 620.25f, (byte) 58);
						AI2Actions.deleteOwner(this);
						NpcShoutsService.getInstance().sendMsg(getOwner(), 1402055);
						break;
					case 10002:
						deleteNpcs(instance.getNpcs(701986));
						deleteNpcs(instance.getNpcs(701984));
						spawn(233494, 618.9949f, 551.4716f, 590.75f, (byte) 55);
						spawn(233492, 621.22687f, 555.14624f, 590.67834f, (byte) 55);
						spawn(233492, 620.4509f, 547.35284f, 590.75f, (byte) 55);
						AI2Actions.deleteOwner(this);
						NpcShoutsService.getInstance().sendMsg(getOwner(), 1402070);
						break;
					case 10003:
						deleteNpcs(instance.getNpcs(701986));
						deleteNpcs(instance.getNpcs(701984));
						spawn(233494, 478.23563f, 543.6911f, 597.5f, (byte) 112);
						spawn(233492, 479.92032f, 545.8376f, 597.5f, (byte) 112);
						spawn(233492, 476.69766f, 542.24774f, 597.5f, (byte) 112);
						AI2Actions.deleteOwner(this);
						NpcShoutsService.getInstance().sendMsg(getOwner(), 1402065);
						break;
				}
				break;

		}
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
		return true;
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().onDelete();
			}
		}
	}
}
