package com.aionemu.gameserver.model.templates.walker;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.spawnengine.WalkerGroupType;

/**
 * @author KKnD
 */
@XmlRootElement(name = "walker_template")
@XmlAccessorType(XmlAccessType.FIELD)
public class WalkerTemplate {

	@XmlElement(name = "routestep")
	private List<RouteStep> routeStepList;

	@XmlAttribute(name = "route_id", required = true)
	private String routeId;

	@XmlAttribute(name = "pool", required = true)
	private int pool = 1;

	@XmlAttribute(name = "formation")
	private WalkerGroupType formation = WalkerGroupType.POINT;

	@XmlAttribute(name = "rows")
	private String rowValues;

	@XmlAttribute(name = "reversed")
	private boolean isReversed = false;

	@XmlTransient
	private int[] rows;

	public WalkerTemplate() {
	}

	public WalkerTemplate(String routeId) {
		this.routeId = routeId;
	}

	/**
	 * @param u
	 * @param parent
	 */
	void afterUnmarshal(Unmarshaller u, Object parent) {
		routeStepList.sort((a, b) -> a.getRouteStep() - b.getRouteStep()); // sort ascending by step, to support scrambled templates
		if (isReversed) { // add steps in backward order, so npcs turn and walk the same way back
			int stepNo = routeStepList.size();
			for (int i = routeStepList.size() - 2; i > 0; i--) {
				RouteStep step = routeStepList.get(i);
				routeStepList.add(new RouteStep(++stepNo, step.getX(), step.getY(), step.getZ(), step.getRestTime()));
			}
		}
		for (int i = 0; i < routeStepList.size() - 1; i++) {
			routeStepList.get(i).setNextStep(routeStepList.get(i + 1));
		}
		routeStepList.get(routeStepList.size() - 1).setNextStep(routeStepList.get(0));

		if (pool == 2) {
			formation = WalkerGroupType.SQUARE;
			rows = new int[1];
			rows[0] = 2;
		} else if (formation == WalkerGroupType.SQUARE) {
			if (rowValues != null) {
				String[] values = rowValues.split(",");
				rows = new int[values.length];
				for (int i = 0; i < values.length; i++)
					rows[i] = Integer.parseInt(values[i]);
			} else {
				formation = WalkerGroupType.POINT;
			}
		}
		rowValues = null;
	}

	public List<RouteStep> getRouteSteps() {
		return routeStepList;
	}

	public RouteStep getRouteStep(int value) {
		return routeStepList.get(value - 1);
	}

	public String getRouteId() {
		return routeId;
	}

	public String getVersionId() {
		return DataManager.WALKER_VERSIONS_DATA.getRouteVersionId(routeId);
	}

	public int getPool() {
		return pool;
	}

	public void setPool(int pool) {
		this.pool = pool;
	}

	public void setRouteSteps(List<RouteStep> newSteps) {
		routeStepList = newSteps;
	}

	public boolean isReversed() {
		return isReversed;
	}

	public void setIsReversed(boolean value) {
		isReversed = value;
	}

	public WalkerGroupType getType() {
		return formation;
	}

	public void setType(WalkerGroupType type) {
		formation = type;
	}

	public int[] getRows() {
		return rows;
	}

	public void setRows(int[] rows) {
		this.rows = rows;
	}
}
