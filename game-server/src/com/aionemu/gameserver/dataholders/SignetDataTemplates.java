package com.aionemu.gameserver.dataholders;

import com.aionemu.gameserver.skillengine.model.SignetData;
import com.aionemu.gameserver.skillengine.model.SignetDataTemplate;
import com.aionemu.gameserver.skillengine.model.SignetEnum;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "signet_data_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class SignetDataTemplates {

    @XmlElement(name = "signet_data_template")
    private List<SignetDataTemplate> signetDataTemplateList;

    @XmlTransient
    private Map<SignetEnum, SignetDataTemplate> signets = new EnumMap<>(SignetEnum.class);

    void afterUnmarshal(Unmarshaller u, Object parent) {
        for (SignetDataTemplate data : signetDataTemplateList) {
            signets.put(data.getSignet(), data);
        }
        signetDataTemplateList = null;
    }

    public SignetData getSignetData(SignetEnum signet, int level) {
        SignetDataTemplate template = signets.get(signet);
        if (template != null) {
            return template.getSignetDataForSignetLevel(level);
        }
        return null;
    }

    public int size() {
        return signets.size();
    }
}
