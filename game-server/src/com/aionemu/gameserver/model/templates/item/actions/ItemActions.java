package com.aionemu.gameserver.model.templates.item.actions;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.*;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemActions")
public class ItemActions {

	@XmlElements({ @XmlElement(name = "skilllearn", type = SkillLearnAction.class), @XmlElement(name = "extract", type = ExtractAction.class),
		@XmlElement(name = "skilluse", type = SkillUseAction.class), @XmlElement(name = "enchant", type = EnchantItemAction.class),
		@XmlElement(name = "queststart", type = QuestStartAction.class), @XmlElement(name = "dye", type = DyeAction.class),
		@XmlElement(name = "craftlearn", type = CraftLearnAction.class), @XmlElement(name = "toypetspawn", type = ToyPetSpawnAction.class),
		@XmlElement(name = "decompose", type = DecomposeAction.class), @XmlElement(name = "titleadd", type = TitleAddAction.class),
		@XmlElement(name = "learnemotion", type = EmotionLearnAction.class), @XmlElement(name = "read", type = ReadAction.class),
		@XmlElement(name = "fireworkact", type = FireworksUseAction.class), @XmlElement(name = "instancetimeclear", type = InstanceTimeClear.class),
		@XmlElement(name = "expandinventory", type = ExpandInventoryAction.class), @XmlElement(name = "animation", type = AnimationAddAction.class),
		@XmlElement(name = "cosmetic", type = CosmeticItemAction.class), @XmlElement(name = "charge", type = ChargeAction.class),
		@XmlElement(name = "ride", type = RideAction.class), @XmlElement(name = "houseobject", type = SummonHouseObjectAction.class),
		@XmlElement(name = "housedeco", type = DecorateAction.class), @XmlElement(name = "assemble", type = AssemblyItemAction.class),
		@XmlElement(name = "adoptpet", type = AdoptPetAction.class), @XmlElement(name = "apextract", type = ApExtractAction.class),
		@XmlElement(name = "remodel", type = RemodelAction.class), @XmlElement(name = "expextract", type = ExpExtractAction.class),
		@XmlElement(name = "polish", type = PolishAction.class), @XmlElement(name = "composition", type = CompositionAction.class),
		@XmlElement(name = "tuning", type = TuningAction.class), @XmlElement(name = "megaphone", type = MegaphoneAction.class),
		@XmlElement(name = "pack", type = PackAction.class), @XmlElement(name = "tampering", type = TamperingAction.class),
		@XmlElement(name = "multireturn", type = MultiReturnAction.class) })
	private List<AbstractItemAction> itemActions;

	/**
	 * Gets the value of the itemActions property. Objects of the following type(s) are allowed in the list {@link SkillLearnAction }
	 * {@link SkillUseAction }
	 */
	public List<AbstractItemAction> getItemActions() {
		return itemActions == null ? Collections.emptyList() : itemActions;
	}

	public EnchantItemAction getEnchantAction() {
		if (itemActions == null)
			return null;
		for (AbstractItemAction action : itemActions) {
			if (action instanceof EnchantItemAction)
				return (EnchantItemAction) action;
		}
		return null;
	}

	public SummonHouseObjectAction getHouseObjectAction() {
		if (itemActions == null)
			return null;
		for (AbstractItemAction action : itemActions) {
			if (action instanceof SummonHouseObjectAction)
				return (SummonHouseObjectAction) action;
		}
		return null;
	}

	public CraftLearnAction getCraftLearnAction() {
		if (itemActions == null)
			return null;
		for (AbstractItemAction action : itemActions) {
			if (action instanceof CraftLearnAction)
				return (CraftLearnAction) action;
		}
		return null;
	}

	public DecorateAction getDecorateAction() {
		if (itemActions == null)
			return null;
		for (AbstractItemAction action : itemActions) {
			if (action instanceof DecorateAction)
				return (DecorateAction) action;
		}
		return null;
	}

	public DyeAction getDyeAction() {
		if (itemActions == null)
			return null;
		for (AbstractItemAction action : itemActions) {
			if (action instanceof DyeAction)
				return (DyeAction) action;
		}
		return null;
	}

	public AdoptPetAction getAdoptPetAction() {
		if (itemActions == null)
			return null;
		for (AbstractItemAction action : itemActions) {
			if (action instanceof AdoptPetAction)
				return (AdoptPetAction) action;
		}
		return null;
	}

	public RemodelAction getRemodelAction() {
		if (itemActions == null)
			return null;
		for (AbstractItemAction action : itemActions) {
			if (action instanceof RemodelAction)
				return (RemodelAction) action;
		}
		return null;
	}

	public PolishAction getPolishAction() {
		if (itemActions == null)
			return null;
		for (AbstractItemAction action : itemActions) {
			if (action instanceof PolishAction)
				return (PolishAction) action;
		}
		return null;
	}

	public TuningAction getTuningAction() {
		if (itemActions == null)
			return null;
		for (AbstractItemAction action : itemActions) {
			if (action instanceof TuningAction)
				return (TuningAction) action;
		}
		return null;
	}

	public SkillUseAction getSkillUseAction() {
		if (itemActions == null)
			return null;
		for (AbstractItemAction action : itemActions) {
			if (action instanceof SkillUseAction)
				return (SkillUseAction) action;
		}
		return null;
	}

	public RideAction getRideAction() {
		if (itemActions == null)
			return null;
		for (AbstractItemAction action : itemActions) {
			if (action instanceof RideAction)
				return (RideAction) action;
		}
		return null;
	}
}
