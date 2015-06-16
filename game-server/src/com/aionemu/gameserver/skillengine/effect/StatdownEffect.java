package com.aionemu.gameserver.skillengine.effect;

import com.aionemu.gameserver.skillengine.model.Effect;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatdownEffect")
public class StatdownEffect extends BufEffect {

   @Override
   public void startEffect(Effect effect) {
	  super.startEffect(effect);
	  effect.getEffected().getLifeStats().updateCurrentStats();
   }
   
	//TODO bosses are resistent to this?
}
