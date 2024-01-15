package com.aionemu.gameserver.model.gameobjects;

import java.util.EnumSet;

import com.aionemu.gameserver.controllers.StaticObjectController;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorState;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author MrPoke, Rolandas
 */
public class StaticDoor extends StaticObject {

	private EnumSet<StaticDoorState> states;
	private boolean isLocked = true;

	public StaticDoor(StaticObjectController controller, SpawnTemplate spawnTemplate, StaticDoorTemplate objectTemplate, int instanceId) {
		super(controller, spawnTemplate, objectTemplate);
		states = EnumSet.noneOf(StaticDoorState.class);
		StaticDoorState.setStates(getObjectTemplate().getState(), states);
		if (objectTemplate.getKeyId() < 2) {
			isLocked = false;
		}
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	/**
	 * @return the open state from states set
	 */
	public boolean isOpen() {
		return states.contains(StaticDoorState.OPENED);
	}

	public EnumSet<StaticDoorState> getStates() {
		return states;
	}

	/**
	 * @param open
	 *          the open state to set
	 */
	public void setOpen(boolean open) {
		EmotionType emotion;
		int packetState; // not important IMO, similar to internal state
		if (open) {
			emotion = EmotionType.OPEN_DOOR;
			states.remove(StaticDoorState.CLICKABLE);
			states.add(StaticDoorState.OPENED); // 1001
			packetState = 0x9;
			GeoService.getInstance().setDoorState(getWorldId(), getInstanceId(), getSpawn().getStaticId(), true);
		} else {
			emotion = EmotionType.CLOSE_DOOR;
			if ((getObjectTemplate().getState() & StaticDoorState.CLICKABLE.getFlag()) == StaticDoorState.CLICKABLE.getFlag())
				states.add(StaticDoorState.CLICKABLE);
			states.remove(StaticDoorState.OPENED); // 1010
			packetState = 0xA;
			GeoService.getInstance().setDoorState(getWorldId(), getInstanceId(), this.getSpawn().getStaticId(), false);
		}
		// int stateFlags = StaticDoorState.getFlags(states);
		PacketSendUtility.broadcastPacket(this, new SM_EMOTION(this.getSpawn().getStaticId(), emotion, packetState));
	}

	public void changeState(boolean open, int state) {
		state = state & 0xF;
		StaticDoorState.setStates(state, states);
		EmotionType emotion = open ? EmotionType.OPEN_DOOR : EmotionType.CLOSE_DOOR;
		PacketSendUtility.broadcastPacket(this, new SM_EMOTION(this.getSpawn().getStaticId(), emotion, state));
	}

	@Override
	public StaticDoorTemplate getObjectTemplate() {
		return (StaticDoorTemplate) super.getObjectTemplate();
	}

}
