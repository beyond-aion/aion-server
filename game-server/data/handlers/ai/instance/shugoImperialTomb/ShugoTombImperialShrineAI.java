package ai.instance.shugoImperialTomb;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.GeneralNpcAI;

/**
 * @author Ritsu
 */
@AIName("shugo_tomb_imperial_shrine")
public class ShugoTombImperialShrineAI extends GeneralNpcAI {

	private static final int GOLD_KEY = 182006989; // Emperor's Golden Tag
	private static final int SILVER_KEY = 182006990; // Empress' Silver Tag
	private static final int BRONZE_KEY = 182006991; // Crown Prince's Brass Tag
	private final Map<Integer, Integer> entriesByKeyId = new HashMap<>();

	public ShugoTombImperialShrineAI(Npc owner) {
		super(owner);
		entriesByKeyId.put(GOLD_KEY, 0);
		entriesByKeyId.put(SILVER_KEY, 0);
		entriesByKeyId.put(BRONZE_KEY, 0);
	}

	@Override
	protected void handleDialogStart(Player player) {
		super.handleDialogStart(player);
	}

	@Override
	public boolean onDialogSelect(Player player, int dialogActionId, int questId, int extendedRewardIndex) {
		QuestEnv env = new QuestEnv(getOwner(), player, questId, dialogActionId);
		env.setExtendedRewardIndex(extendedRewardIndex);
		switch (dialogActionId) {
				case SETPRO1, SETPRO2, SETPRO3 -> enterTreasureRoomIfPossible(player, dialogActionId);
				default -> QuestEngine.getInstance().onDialog(env);
		}
		return true;
	}

	private void enterTreasureRoomIfPossible(Player player, int dialogActionId) {
		WorldMapInstance wmi = getPosition().getWorldMapInstance();
		Point3D destination = null;
		switch (dialogActionId) {
			case SETPRO1: // Emperor treasure room
				if (!player.getInventory().decreaseByItemId(GOLD_KEY, 1)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_IDEVENT01_GOLD_MAP());
					return;
				}
				destination = switch (getEntryCountByKeyId(GOLD_KEY)) {
					case 0 -> new Point3D(74.292816f, 350.66525f, 285.14545f);
					case 1 -> new Point3D(375.25629f, 198.02574f, 306.84357f);
					default -> new Point3D(335.55219f, 334.60947f, 458.5939f);
				};
				break;
			case SETPRO2: // Empress treasure room
				if (!player.getInventory().decreaseByItemId(SILVER_KEY, 1)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_IDEVENT01_SILVER_MAP());
					return;
				}
				destination = switch (getEntryCountByKeyId(SILVER_KEY)) {
					case 0 -> new Point3D(347.96069f, 41.424923f, 358.3918f);
					case 1 -> new Point3D(82.877762f, 240.61363f, 421.79004f);
					default -> new Point3D(75.203018f, 432.58603f, 455.82312f);
				};
				break;
			case SETPRO3: // Prince treasure room
				if (!player.getInventory().decreaseByItemId(BRONZE_KEY, 1)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_IDEVENT01_BRONZE_MAP());
					return;
				}
				destination = switch (getEntryCountByKeyId(BRONZE_KEY)) {
					case 0 -> new Point3D(409.74783f, 247.3778f, 516.4457f);
					case 1 -> new Point3D(181.56918f, 385.08932f, 616.1734f);
					default -> new Point3D(177.63902f, 77.778755f, 466.1734f);
				};
				break;
		}
		if (destination != null)
			TeleportService.teleportTo(player, wmi, destination.getX(), destination.getY(), destination.getZ(), (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
		PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 0));
	}

	private synchronized int getEntryCountByKeyId(int keyId) {
		int entryCount = entriesByKeyId.get(keyId);
		entriesByKeyId.put(keyId, entryCount + 1);
		return entryCount;
	}
}
