package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.TransformType;

/**
 * @author Sweetkr, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransformEffect")
public abstract class TransformEffect extends EffectTemplate {

	@XmlAttribute
	protected int model;

	@XmlAttribute
	protected TransformType type = TransformType.NONE;

	@XmlAttribute
	protected int panelid;

	@XmlAttribute
	protected boolean cantUseSkills;
	@XmlAttribute
	protected boolean cantMove;
	@XmlAttribute
	protected boolean cantRecall;
	@XmlAttribute
	protected boolean cantJump;
	@XmlAttribute
	protected boolean cantAttack;
	@XmlAttribute
	protected boolean cantUseItems;
	@XmlAttribute
	protected boolean cantFly;

	@Override
	public void applyEffect(Effect effect) {
		/**
		 * TODO need more info fix for cases like use itemId: 160010206(Dignified Wyvern Form Candy) after that use cannon skill(ex. 20365) -> candy
		 * should be removed
		 */
		if (type == TransformType.FORM1 && panelid > 0) {
			if (effect.getEffected().getTransformModel().isActive()) {
				effect.getEffected().getEffectController().removeTransformEffects();
			}
		}

		effect.addToEffectedController();
	}

	@Override
	public void endEffect(Effect effect) {
		final Creature effected = effect.getEffected();

		TransformEffect temp = null;
		for (Effect tmp : effected.getEffectController().getAbnormalEffects()) {
			for (EffectTemplate template : tmp.getEffectTemplates()) {
				if (template instanceof TransformEffect && ((TransformEffect) template).getTransformId() != model) {
					temp = (TransformEffect) template;
					break;
				}
			}
		}
		if (temp != null)
			effected.getTransformModel().apply(temp.getTransformId(), temp.getTransformType(), temp.getPanelId(), temp.cantUseSkills(),
				temp.cantMove(), temp.cantRecall(), temp.cantJump(), temp.cantAttack(), temp.cantUseItems(), temp.cantFly());
		else
			effected.endTransformation();
	}

	@Override
	public void startEffect(Effect effect) {
		effect.getEffected().getTransformModel().apply(getTransformId(), getTransformType(), getPanelId(), cantUseSkills(), cantMove(), cantRecall(),
			cantJump(), cantAttack(), cantUseItems(), cantFly());
	}

	public TransformType getTransformType() {
		return type;
	}

	public int getTransformId() {
		return model;
	}

	public int getPanelId() {
		return panelid;
	}

	public boolean cantUseSkills() {
		return cantUseSkills;
	}

	public boolean cantMove() {
		return cantMove;
	}

	public boolean cantRecall() {
		return cantRecall;
	}

	public boolean cantJump() {
		return cantJump;
	}

	public boolean cantAttack() {
		return cantAttack;
	}

	public boolean cantUseItems() {
		return cantUseItems;
	}

	public boolean cantFly() {
		return cantFly;
	}

}
