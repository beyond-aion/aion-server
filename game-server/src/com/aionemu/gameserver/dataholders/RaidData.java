package com.aionemu.gameserver.dataholders;

import com.aionemu.gameserver.model.raid.RaidLocation;
import com.aionemu.gameserver.model.templates.raid.RaidTemplate;
import javolution.util.FastMap;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import java.util.List;


/**
 * @author Alcapwnd
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "raid_locations")
public class RaidData {

    @XmlElement(name = "raid_location")
    private List<RaidTemplate> raidTemplates;
    @XmlTransient
    private FastMap<Integer, RaidLocation> raid = new FastMap<Integer, RaidLocation>();

    /**
     * @param u
     * @param parent
     */
    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (RaidTemplate template : raidTemplates) {
            raid.put(template.getId(), new RaidLocation(template));
        }
    }

    public int size() {
        return raid.size();
    }

    public FastMap<Integer, RaidLocation> getRaidLocations() {
        return raid;
    }
}
