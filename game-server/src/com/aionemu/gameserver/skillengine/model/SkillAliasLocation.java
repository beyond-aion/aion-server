package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "alias_location")
public class SkillAliasLocation {

    @XmlElement(name = "alias_pos")
    private List<SkillAliasPosition> skillAliasPositionList;

    @XmlAttribute(name = "name", required = true)
    private String aliasName;
    @XmlAttribute(name = "world_id", required = true)
    private int worldId;

    public List<SkillAliasPosition> getSkillAliasPositionList() {
        return skillAliasPositionList;
    }

    public String getAliasName() {
        return aliasName;
    }

    public int getWorldId() {
        return worldId;
    }
}
