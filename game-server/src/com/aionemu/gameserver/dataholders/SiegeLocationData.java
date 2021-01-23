package com.aionemu.gameserver.dataholders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.siege.*;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;

/**
 * @author Sarynth, antness
 */
@XmlRootElement(name = "siege_locations")
@XmlAccessorType(XmlAccessType.FIELD)
public class SiegeLocationData {

	@XmlElement(name = "siege_location")
	private List<SiegeLocationTemplate> siegeLocationTemplates;
	@XmlTransient
	private Map<Integer, ArtifactLocation> artifactLocations = new LinkedHashMap<>();
	@XmlTransient
	private Map<Integer, FortressLocation> fortressLocations = new LinkedHashMap<>();
	@XmlTransient
	private Map<Integer, OutpostLocation> outpostLocations = new LinkedHashMap<>();
	@XmlTransient
	private Map<Integer, SiegeLocation> siegeLocations = new LinkedHashMap<>();
	@XmlTransient
	private AgentLocation agentLoc;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		artifactLocations.clear();
		fortressLocations.clear();
		outpostLocations.clear();
		siegeLocations.clear();
		for (SiegeLocationTemplate template : siegeLocationTemplates)
			switch (template.getType()) {
				case FORTRESS -> {
					FortressLocation fortress = new FortressLocation(template);
					fortressLocations.put(template.getId(), fortress);
					siegeLocations.put(template.getId(), fortress);
					artifactLocations.put(template.getId(), new ArtifactLocation(template));
				}
				case ARTIFACT -> {
					ArtifactLocation artifact = new ArtifactLocation(template);
					artifactLocations.put(template.getId(), artifact);
					siegeLocations.put(template.getId(), artifact);
				}
				case OUTPOST -> {
					OutpostLocation outpost = new OutpostLocation(template);
					if (outpost.getLocationId() == 2111)
						outpost.setRace(SiegeRace.ELYOS);
					else if (outpost.getLocationId() == 3111)
						outpost.setRace(SiegeRace.ASMODIANS);
					outpostLocations.put(template.getId(), outpost);
					siegeLocations.put(template.getId(), outpost);
				}
				case AGENT_FIGHT -> {
					agentLoc = new AgentLocation(template);
					siegeLocations.put(template.getId(), agentLoc);
				}
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
