package com.aionemu.gameserver.network.aion.serverpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHER_CANCEL_1_BASIC;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHER_FAIL_1_BASIC;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHER_START_1_BASIC;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_EXTRACT_GATHER_OCCUPIED_BY_OTHER;

import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;
import com.aionemu.gameserver.model.templates.gather.Material;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer, orz
 * @rework Yeats
 */
public class SM_GATHER_UPDATE extends AionServerPacket {

	private GatherableTemplate template;
	private int action;
	private int itemId;
	private int success;
	private int failure;
	private int nameId;
	private int executionSpeed = 700;
	private int delay = 1200;

	/**
	 * 
	 * @param template
	 * @param material
	 * @param success
	 * @param failure
	 * @param action
	 * @param executionSpeed
	 * @param delay
	 */
	public SM_GATHER_UPDATE(GatherableTemplate template, Material material, int success, int failure, int action, int executionSpeed, int delay) {
		this.action = action;
		this.template = template;
		this.itemId = material.getItemid();
		this.success = success;
		this.failure = failure;
		this.nameId = material.getNameid();
		this.executionSpeed = executionSpeed;
		this.delay = delay;
	}
	
	@Override
	protected void writeImpl(AionConnection con) {
		writeH(template.getHarvestSkill());
		writeC(action);
		writeD(itemId);

		switch (action) {
			case 0: { //init
				writeD(success); //max 
				writeD(failure); //max
				writeD(executionSpeed); //executionSpeed
				writeD(delay); //delay
				writeD(STR_EXTRACT_GATHER_START_1_BASIC.getId()); //msgId
				writeH(0x24); //decoding or smth
				writeD(nameId); //nameId
				writeH(0); // 0x24, decoding/symbol or smth
				break;
			}
			case 1: // For updates both for ground and aerial
			case 2: // Light blue bar = +10%
			case 3: // Purple bar = 100%
			{
				writeD(success);
				writeD(failure);
				writeD(executionSpeed);
				writeD(delay);
				writeD(0);
				writeH(0); 
				writeD(0); 
				writeH(0);
				break;
			}
			case 5: //canceled
			{
				writeD(success);
				writeD(failure);
				writeD(executionSpeed);
				writeD(delay);
				writeD(STR_EXTRACT_GATHER_CANCEL_1_BASIC.getId());
				writeH(0x24); 
				writeD(0); 
				writeH(0); 
				break;
			}
			case 6: //success
			{
				writeD(success);
				writeD(failure);
				writeD(executionSpeed);
				writeD(delay);
				writeD(1330078); //You have gathered nameId.
				writeH(0x24); 
				writeD(nameId);
				writeH(0);
				break;
			}
			case 7: //failure
			{ 
				writeD(success);
				writeD(failure);
				writeD(executionSpeed);
				writeD(delay);
				writeD(STR_EXTRACT_GATHER_FAIL_1_BASIC.getId());
				writeH(0x24);
				writeD(nameId);
				writeH(0); 
				break;
			}
			case 8: //occupied by another player
			{
				writeD(success);
				writeD(failure);
				writeD(executionSpeed);
				writeD(delay);
				writeD(STR_EXTRACT_GATHER_OCCUPIED_BY_OTHER.getId());
				writeH(0);
				writeD(0);
				writeH(0);
				break;
			}
		}
	}

}
