package com.aionemu.gameserver.network.aion.serverpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHER_CANCEL_1_BASIC;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHER_FAIL_1_BASIC;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHER_START_1_BASIC;

import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;
import com.aionemu.gameserver.model.templates.gather.Material;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer, orz
 */
public class SM_GATHER_UPDATE extends AionServerPacket {

	private GatherableTemplate template;
	private int action;
	private int itemId;
	private int success;
	private int failure;
	private int nameId;

	public SM_GATHER_UPDATE(GatherableTemplate template, Material material, int success, int failure, int action) {
		this.action = action;
		this.template = template;
		this.itemId = material.getItemid();
		this.success = success;
		this.failure = failure;
		this.nameId = material.getNameid();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(template.getHarvestSkill());
		writeC(action);
		writeD(itemId);

		switch (action) {
			case 0: {
				writeD(template.getSuccessAdj());
				writeD(template.getFailureAdj());
				writeD(0);
				writeD(1200); // timer??
				writeD(STR_EXTRACT_GATHER_START_1_BASIC.getId());
				writeH(0x24); // 0x24
				writeD(nameId);
				writeH(0); // 0x24
				break;
			}
			case 1: // For updates both for ground and aerial
			case 2: // Light blue bar
			case 3: // Purple bar
			case 8: // Occupied by other player
			{
				writeD(success);
				writeD(failure);
				writeD(700);// unk timer??
				writeD(1200); // unk timer??
				writeD(0);
				writeH(0);
				break;
			}
			case 5: {
				writeD(success);
				writeD(failure);
				writeD(700);// unk timer??
				writeD(1200); // unk timer??
				writeD(STR_EXTRACT_GATHER_CANCEL_1_BASIC.getId());
				writeH(0);
				break;
			}
			case 6: { // the last update before success
				writeD(template.getSuccessAdj());
				writeD(failure);
				writeD(700); // unk timer??
				writeD(1200); // unk timer??
				writeD(0);
				writeH(0);
				break;
			}
			case 7: {
				writeD(success);
				writeD(template.getFailureAdj());
				writeD(0);
				writeD(1200); // timer??
				writeD(STR_EXTRACT_GATHER_FAIL_1_BASIC.getId());
				writeH(0x24); // 0x24
				writeD(nameId);
				writeH(0); // 0x24
				break;
			}
		}
	}

}
