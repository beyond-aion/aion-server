package com.aionemu.gameserver.model.templates.siegelocation;


import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DoorRepairData")
public class DoorRepairData {

    @XmlElement(name = "door_repair_stone")
    private List<DoorRepairStone> doorRepairTemplates;

    @XmlAttribute(name = "item_id")
    protected int itemId;
    @XmlAttribute(name = "count")
    protected int count;
    @XmlAttribute(name = "cd")
    protected int cd;

    @XmlTransient
    private Map<Integer, DoorRepairStone> doorRepairStones = new HashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (DoorRepairStone repairStone : doorRepairTemplates) {
            doorRepairStones.put(repairStone.staticId, repairStone);
        }
        doorRepairTemplates = null;
    }

    public int getItemId() {
        return itemId;
    }

    public int getCount() {
        return count;
    }

    public int getCd() {
        return cd * 1000;
    }

    public DoorRepairStone getRepairStone(int stoneStaticId) {
        return doorRepairStones.get(stoneStaticId);
    }

    public Collection<DoorRepairStone> getRepairStones() {
        return doorRepairStones.values();
    }
}
