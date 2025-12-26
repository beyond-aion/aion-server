package com.aionemu.gameserver.dataholders;

import com.aionemu.gameserver.model.templates.pet.PetBonusAttr;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"petBonusAttr"})
@XmlRootElement(name = "pet_buffs")
public final class PetBuffData {
    @XmlElement(name = "pet_buffs")
    private List<PetBonusAttr> petBonusAttr;
    @XmlTransient
    private final Map<Integer, PetBonusAttr> byBuffId = new ConcurrentHashMap<>();
    @XmlTransient
    private final Map<Integer, PetBonusAttr> byFoodCnt = new ConcurrentHashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        if (petBonusAttr == null) {
            return;
        }
        for (var attr : petBonusAttr) {
            if (attr == null) {
                continue;
            }
            if (attr.getBuffId() > 0) {
                byBuffId.put(attr.getBuffId(), attr);
            }
            if (attr.getFoodCount() > 0) {
                byFoodCnt.put(attr.getFoodCount(), attr);
            }
        }
        petBonusAttr = null;
    }

    public int size() {
        return byBuffId.size();
    }

    public PetBonusAttr getPetBonusAttr(int buffId) {
        return byBuffId.get(buffId);
    }

    @SuppressWarnings("unused")
    public PetBonusAttr getFoodCount(int count) {
        return byFoodCnt.get(count);
    }
}