package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.craft.CraftService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Mr. Poke
 */
public class CM_CRAFT extends AionClientPacket {

	private int unk;
	private int targetTemplateId;
	private int recipeId;
	private int targetObjId;
	@SuppressWarnings("unused")
	private int materialsCount;
	private int craftType;

	/**
	 * @param opcode
	 */
	public CM_CRAFT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		unk = readC();
		targetTemplateId = readD();
		recipeId = readD();
		targetObjId = readD();
		materialsCount = readH();
		craftType = readC();
		// un used
		// for (int i = 0; i < materialsCount; i++) {
		// readD(); // materialId
		// readQ(); // materialCount
		// }
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (player == null || !player.isSpawned())
			return;
		// disallow crafting in shutdown progress..
		if (player.getController().isInShutdownProgress())
			return;

		// 129 = Morph Substances
		if (unk != 129) {
			VisibleObject staticObject = player.getKnownList().getKnownObjects().get(targetObjId);
			if (staticObject == null || !MathUtil.isIn3dRange(player, staticObject, 10)
				|| staticObject.getObjectTemplate().getTemplateId() != targetTemplateId)
				return;
		}

		if (player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_CRAFT_ITEMS) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		CraftService.startCrafting(player, recipeId, targetObjId, craftType);
	}
}
