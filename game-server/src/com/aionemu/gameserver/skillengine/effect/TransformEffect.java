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
	protected int banUseSkills;
	@XmlAttribute
	protected int banMovement;
	@XmlAttribute
	protected int res1;
	@XmlAttribute
	protected int res2;
	@XmlAttribute
	protected int res3;
	@XmlAttribute
	protected int res5;
	@XmlAttribute
	protected int res6;

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
			effected.getTransformModel().apply(temp.getTransformId(), temp.getTransformType(), temp.getPanelId(), temp.getBanUseSkills(),
				temp.getBanMovement(), temp.getRes1(), temp.getRes2(), temp.getRes3(), temp.getRes5(), temp.getRes6());
		else
			effected.endTransformation();
	}

	@Override
	public void startEffect(Effect effect) {
		final Creature effected = effect.getEffected();
		effected.getTransformModel().apply(this.getTransformId(), this.getTransformType(), this.getPanelId(), this.getBanUseSkills(),
			this.getBanMovement(), this.getRes1(), this.getRes2(), this.getRes3(), this.getRes5(), this.getRes6());
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

	/**
	 * @return the banUseSkills
	 */
	public int getBanUseSkills() {
		return banUseSkills;
	}

	/**
	 * @return the banMovement
	 */
	public int getBanMovement() {
		return banMovement;
	}

	/**
	 * @return the res1
	 */
	public int getRes1() {
		return res1;
	}

	/**
	 * @return the res2
	 */
	public int getRes2() {
		return res2;
	}

	/**
	 * @return the res3
	 */
	public int getRes3() {
		return res3;
	}

	/**
	 * @return the res5
	 */
	public int getRes5() {
		return res5;
	}

	/**
	 * @return the res6
	 */
	public int getRes6() {
		return res6;
	}

}
