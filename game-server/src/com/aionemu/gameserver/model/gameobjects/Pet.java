package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.PetController;
import com.aionemu.gameserver.controllers.movement.CreatureMoveController;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.pet.PetTemplate;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author ATracer
 */
public class Pet extends VisibleObject {

	private final Player master;
	private CreatureMoveController<Pet> moveController;
	private final PetCommonData commonData;

	public Pet(PetTemplate petTemplate, PetController controller, PetCommonData commonData, Player master) {
		super(commonData.getObjectId(), controller, null, petTemplate, new WorldPosition(master.getWorldId()), false);
		controller.setOwner(this);
		this.master = master;
		this.commonData = commonData;
		this.moveController = new CreatureMoveController<Pet>(this) {};
	}

	@Override
	public String getName() {
		return commonData.getName();
	}

	public Player getMaster() {
		return master;
	}

	public final PetCommonData getCommonData() {
		return commonData;
	}

	public final CreatureMoveController<Pet> getMoveController() {
		return moveController;
	}

	@Override
	public final PetTemplate getObjectTemplate() {
		return (PetTemplate) super.getObjectTemplate();
	}

}
