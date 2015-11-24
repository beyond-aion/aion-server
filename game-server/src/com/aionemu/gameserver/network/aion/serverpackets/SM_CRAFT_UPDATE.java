package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Mr. Poke
 * @rework Yeats
 */
public class SM_CRAFT_UPDATE extends AionServerPacket {

	private int skillId;
	private int itemId;
	private int action;
	private int success;
	private int failure;
	private int nameId;
	private int executionSpeed;
	private int delay = 1200;

	/**
	 * @param skillId
	 * @param item
	 * @param success
	 * @param failure
	 * @param action
	 * @param executionSpeed
	 * @param delay
	 */
	public SM_CRAFT_UPDATE(int skillId, ItemTemplate item, int success, int failure, int action, int executionSpeed, int delay) {
		this.action = action;
		this.skillId = skillId;
		this.itemId = item.getTemplateId();
		this.success = success;
		this.failure = failure;
		this.nameId = item.getNameId();
		this.executionSpeed = executionSpeed;
		if (skillId == 40009) {
			this.delay = 1000;
		} else {
			this.delay = delay;
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillId);
		writeC(action);
		writeD(itemId);

		switch (action) {
			case 0: // init
			{
				writeD(success); //max
				writeD(failure); //max
				writeD(executionSpeed);
				writeD(delay); //delay
				writeD(1330048); //msgId
				writeH(0x24); // 0x24 //decode or smth
				writeD(nameId); //nameId of item
				writeH(0); //decoding or symbol
				break;
			}
			case 1: // update (normal)
			case 2: // crit (blue) = +10%
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
			case 3: //crit = proc
			{
				writeD(success);
				writeD(failure);
				writeD(0);
				writeD(delay); 
				writeD(0);
				writeH(0x24);
				writeD(0);
				writeH(0);
				break;
			}
			case 4: //canceled
			{
				writeD(success);
				writeD(failure);
				writeD(executionSpeed);
				writeD(delay);
				writeD(1330051);
				writeH(0);
				writeD(0);
				writeH(0);
				break;
			}
			case 5: // sucess (end)
			{
				writeD(success);
				writeD(failure);
				writeD(executionSpeed);
				writeD(delay);
				writeD(1330049);
				writeH(0x24);
				writeD(nameId);
				writeH(0);
				break;
			}
			case 6: // failed (end)
			{
				writeD(success);
				writeD(failure);
				writeD(executionSpeed);
				writeD(delay);
				writeD(1330050);
				writeH(0x24);
				writeD(nameId);
				writeH(0);
				break;
			}
			case 7: //failure //never used?
			{
				writeD(success);
				writeD(failure);
				writeD(executionSpeed);
				writeD(delay);
				writeD(1330050);
				writeH(0x24);
				writeD(nameId);
				writeH(0);
				break;
			}
		}
	}
}
