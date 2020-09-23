package com.aionemu.gameserver.dataholders;

import com.aionemu.gameserver.skillengine.model.SkillAliasLocation;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "alias_locations")
@XmlAccessorType(XmlAccessType.FIELD)
public class SkillAliasLocationData {

    @XmlElement(name = "alias_location")
    private List<SkillAliasLocation> skillAliasLocationData;

    @XmlTransient
    private Map<String, SkillAliasLocation> skillAliasLocations = new HashMap<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (SkillAliasLocation loc : skillAliasLocationData) {
            skillAliasLocations.put(loc.getAliasName(), loc);
        }
        skillAliasLocationData = null;
    }

    public SkillAliasLocation getSkillAliasLocation(String alias) {
        return skillAliasLocations.get(alias);
    }

    public int size() {
        return skillAliasLocations.size();
    }
}
