package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.PetController;
import com.aionemu.gameserver.controllers.movement.CreatureMoveController;
import com.aionemu.gameserver.controllers.movement.MoveController;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.pet.PetTemplate;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author ATracer
 */
public class Pet extends VisibleObject {

	private final Player master;
	private MoveController moveController;
	private final PetTemplate petTemplate;

	/**
	 * @param petTemplate
	 * @param controller
	 * @param commonData
	 * @param master
	 */
	public Pet(PetTemplate petTemplate, PetController controller, PetCommonData commonData, Player master) {
		super(commonData.getObjectId(), controller, null, commonData, new WorldPosition(master.getWorldId()));
		controller.setOwner(this);
		this.master = master;
		this.petTemplate = petTemplate;
		this.moveController = new CreatureMoveController<Pet>(this) {};
	}

	public Player getMaster() {
		return master;
	}

	public int getPetId() {
		return getObjectTemplate().getTemplateId();
	}

	public final PetCommonData getCommonData() {
		return (PetCommonData) super.getObjectTemplate();
	}

	public final MoveController getMoveController() {
		return moveController;
	}

	public final PetTemplate getPetTemplate() {
		return petTemplate;
	}

}
