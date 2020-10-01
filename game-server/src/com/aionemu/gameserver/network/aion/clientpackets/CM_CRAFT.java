package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.HashMap;
import java.util.Map;
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
	private int craftType;
	private Map<Integer, Long> materialsData = new HashMap<>();

	public CM_CRAFT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		unk = readUC();
		targetTemplateId = readD();
		recipeId = readD();
		targetObjId = readD();
		int materialsCount = readUH();
		craftType = readUC();
		for (int i = 0; i < materialsCount; i++)
			materialsData.put(readD(), readQ());
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (player == null || !player.isSpawned())
			return;
		if (GameServer.isShuttingDownSoon()) // stop crafting to avoid unnecessary material loss 
			return;

		// 129 = Morph Substances
		if (unk != 129) {
			VisibleObject staticObject = player.getKnownList().getObject(targetObjId);
			if (staticObject == null || !PositionUtil.isInRange(player, staticObject, 10)
				|| staticObject.getObjectTemplate().getTemplateId() != targetTemplateId)
				return;
		}

		CraftService.startCrafting(player, recipeId, targetObjId, craftType, materialsData);
	}
}
