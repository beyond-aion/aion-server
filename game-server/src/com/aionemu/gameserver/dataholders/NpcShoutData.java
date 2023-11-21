package com.aionemu.gameserver.dataholders;

import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.npcshout.NpcShout;
import com.aionemu.gameserver.model.templates.npcshout.ShoutEventType;
import com.aionemu.gameserver.model.templates.npcshout.ShoutGroup;
import com.aionemu.gameserver.model.templates.npcshout.ShoutList;

/**
 * <p>
 * Java class for anonymous complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="shout_group" type="{}ShoutGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "shoutGroups" })
@XmlRootElement(name = "npc_shouts")
public class NpcShoutData {

	@XmlElement(name = "shout_group")
	protected List<ShoutGroup> shoutGroups;

	@XmlTransient
	private final Map<Integer, Map<Integer, List<NpcShout>>> shoutsByWorldNpcs = new HashMap<>();

	@XmlTransient
	private int count = 0;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (ShoutGroup group : shoutGroups) {
			for (int i = group.getShoutNpcs().size() - 1; i >= 0; i--) {
				ShoutList shoutList = group.getShoutNpcs().get(i);
				int worldId = shoutList.getRestrictWorld();

				Map<Integer, List<NpcShout>> worldShouts = shoutsByWorldNpcs.get(worldId);
				if (worldShouts == null) {
					worldShouts = new LinkedHashMap<>();
					this.shoutsByWorldNpcs.put(worldId, worldShouts);
				}

				this.count += shoutList.getNpcShouts().size();
				for (int j = shoutList.getNpcIds().size() - 1; j >= 0; j--) {
					int npcId = shoutList.getNpcIds().get(j);
					List<NpcShout> shouts = new ArrayList<>();
					shouts.addAll(shoutList.getNpcShouts());
					if (worldShouts.get(npcId) == null) {
						worldShouts.put(npcId, shouts);
					} else {
						worldShouts.get(npcId).addAll(shouts);
					}
					shoutList.getNpcIds().remove(j);
				}
				shoutList.getNpcShouts().clear();
				shoutList.makeNull();
				group.getShoutNpcs().remove(i);
			}
			group.makeNull();
		}
		this.shoutGroups.clear();
		this.shoutGroups = null;
	}

	public int size() {
		return this.count;
	}

	/**
	 * Get global npc shouts plus world specific shouts.
	 * 
	 * @return null if not found
	 */
	public List<NpcShout> getNpcShouts(int worldId, int npcId) {
		Map<Integer, List<NpcShout>> globalShouts = shoutsByWorldNpcs.get(0);
		Map<Integer, List<NpcShout>> worldShouts = shoutsByWorldNpcs.get(worldId);
		List<NpcShout> globalNpcShouts = globalShouts == null ? null : globalShouts.get(npcId);
		List<NpcShout> worldNpcShouts = worldShouts == null ? null : worldShouts.get(npcId);

		if (globalShouts == null && worldShouts == null)
			return null;

		List<NpcShout> npcShouts = new ArrayList<>();
		if (globalNpcShouts != null)
			npcShouts.addAll(globalNpcShouts);
		if (worldNpcShouts != null)
			npcShouts.addAll(worldNpcShouts);
		return npcShouts;
	}

	public List<NpcShout> getNpcShouts(int worldId, int npcId, ShoutEventType type) {
		List<NpcShout> shouts = getNpcShouts(worldId, npcId);
		if (shouts == null || type == null)
			return shouts;

		return shouts.stream().filter(shout -> shout.getWhen() == type).collect(Collectors.toList());
	}

	/**
	 * Gets shouts for npc
	 * 
	 * @param worldId
	 *          - npc World Id
	 * @param npcId
	 *          - npc Id
	 * @param type
	 *          - shout event type
	 * @param pattern
	 *          - specific pattern; if null, returns all
	 * @param skillNo
	 *          - specific skill number; if 0, returns all
	 */
	public List<NpcShout> getNpcShouts(int worldId, int npcId, ShoutEventType type, String pattern, int skillNo) {
		List<NpcShout> shouts = getNpcShouts(worldId, npcId, type);
		if (shouts == null)
			return shouts;

		shouts.removeIf(shout -> pattern != null && !pattern.equals(shout.getPattern()) || skillNo != 0 && skillNo != shout.getSkillNo());
		return shouts;
	}
}
