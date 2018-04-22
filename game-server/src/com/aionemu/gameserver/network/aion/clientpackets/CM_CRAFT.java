package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.craft.CraftService;
import com.aionemu.gameserver.utils.PositionUtil;

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
	public CM_CRAFT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		unk = readUC();
		targetTemplateId = readD();
		recipeId = readD();
		targetObjId = readD();
		materialsCount = readUH();
		craftType = readUC();
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
		if (GameServer.isShuttingDown())
			return;

		// 129 = Morph Substances
		if (unk != 129) {
			VisibleObject staticObject = player.getKnownList().getObject(targetObjId);
			if (staticObject == null || !PositionUtil.isInRange(player, staticObject, 10)
				|| staticObject.getObjectTemplate().getTemplateId() != targetTemplateId)
				return;
		}

		CraftService.startCrafting(player, recipeId, targetObjId, craftType);
	}
}
