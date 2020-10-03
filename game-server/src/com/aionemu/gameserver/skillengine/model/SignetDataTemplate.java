package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlType(name = "signet_data_template")
@XmlAccessorType(XmlAccessType.FIELD)
public class SignetDataTemplate {

    @XmlAttribute(name = "signet_skill", required = true)
    private SignetEnum signet;

    @XmlElement(name = "signet_data")
    private List<SignetData> signetDataList;

    public SignetEnum getSignet() {
        return signet;
    }

    public SignetData getSignetDataForSignetLevel(int level) {
        for (SignetData data : signetDataList) {
            if (data.getLevel() == level) {
                return data;
            }
        }
        return null;
    }
}
