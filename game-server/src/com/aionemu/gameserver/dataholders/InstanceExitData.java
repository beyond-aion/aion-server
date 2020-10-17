package com.aionemu.gameserver.dataholders;

import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.portal.InstanceExit;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "instanceExit" })
@XmlRootElement(name = "instance_exits")
public class InstanceExitData {

	@XmlElement(name = "instance_exit")
	protected List<InstanceExit> instanceExit;

	@XmlTransient
	private final Map<Integer, List<InstanceExit>> instanceExitByWorldId = new HashMap<>();

	void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		instanceExit.forEach(exit -> instanceExitByWorldId.computeIfAbsent(exit.getInstanceId(), i -> new ArrayList<>()).add(exit));
		instanceExit = null;
	}

	public InstanceExit getInstanceExit(int worldId, Race race) {
		List<InstanceExit> instanceExits = instanceExitByWorldId.getOrDefault(worldId, Collections.emptyList());
		if (instanceExits.isEmpty())
			return null;
		for (InstanceExit instanceExit : instanceExits)
			if (instanceExit.getRace() == Race.PC_ALL || instanceExit.getRace() == race)
				return instanceExit;
		return null;
	}

	public int size() {
		return instanceExitByWorldId.values().stream().mapToInt(List::size).sum();
	}

}
