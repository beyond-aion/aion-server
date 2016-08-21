package com.aionemu.gameserver.dataholders;

import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.siege.AgentLocation;
import com.aionemu.gameserver.model.siege.ArtifactLocation;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.OutpostLocation;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;

import javolution.util.FastMap;

/**
 * @author Sarynth, antness
 */
@XmlRootElement(name = "siege_locations")
@XmlAccessorType(XmlAccessType.FIELD)
public class SiegeLocationData {

	@XmlElement(name = "siege_location")
	private List<SiegeLocationTemplate> siegeLocationTemplates;
	/**
	 * Map that contains skillId - SkillTemplate key-value pair
	 */
	@XmlTransient
	private Map<Integer, ArtifactLocation> artifactLocations = new FastMap<>();
	@XmlTransient
	private Map<Integer, FortressLocation> fortressLocations = new FastMap<>();
	@XmlTransient
	private Map<Integer, OutpostLocation> outpostLocations = new FastMap<>();
	@XmlTransient
	private Map<Integer, SiegeLocation> siegeLocations = new FastMap<>();
	@XmlTransient
	private AgentLocation agentLoc;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		artifactLocations.clear();
		fortressLocations.clear();
		outpostLocations.clear();
		siegeLocations.clear();
		for (SiegeLocationTemplate template : siegeLocationTemplates)
			switch (template.getType()) {
				case FORTRESS:
					FortressLocation fortress = new FortressLocation(template);
					fortressLocations.put(template.getId(), fortress);
					siegeLocations.put(template.getId(), fortress);
					artifactLocations.put(template.getId(), new ArtifactLocation(template));
					break;
				case ARTIFACT:
					ArtifactLocation artifact = new ArtifactLocation(template);
					artifactLocations.put(template.getId(), artifact);
					siegeLocations.put(template.getId(), artifact);
					break;
				case VEILLE:
				case MASTARIUS:
					OutpostLocation protector = new OutpostLocation(template);
					outpostLocations.put(template.getId(), protector);
					siegeLocations.put(template.getId(), protector);
					break;
				case AGENT_FIGHT:
					agentLoc = new AgentLocation(template);
					siegeLocations.put(template.getId(), agentLoc);
					break;
			}
	}

	public int size() {
		return siegeLocations.size();
	}

	public Map<Integer, ArtifactLocation> getArtifacts() {
		return artifactLocations;
	}

	public Map<Integer, FortressLocation> getFortress() {
		return fortressLocations;
	}

	public Map<Integer, OutpostLocation> getOutpost() {
		return outpostLocations;
	}

	public Map<Integer, SiegeLocation> getSiegeLocations() {
		return siegeLocations;
	}
	
	public AgentLocation getAgentLoc() {
		return agentLoc;
	}

}
