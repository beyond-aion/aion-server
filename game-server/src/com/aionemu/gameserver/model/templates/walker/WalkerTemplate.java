package com.aionemu.gameserver.model.templates.walker;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.spawnengine.WalkerGroupType;

/**
 * @author KKnD
 */
@XmlRootElement(name = "walker_template")
@XmlAccessorType(XmlAccessType.FIELD)
public class WalkerTemplate {

	@XmlType(name = "LoopType")
	@XmlEnum
	public enum LoopType {
		NONE,
		NORMAL,
		WALK_BACK
	}

	@XmlElement(name = "routestep", required = true)
	private List<RouteStep> routeStepList;

	@XmlAttribute(name = "route_id", required = true)
	private String routeId;

	@XmlAttribute(name = "pool")
	private int pool = 1;

	@XmlAttribute(name = "formation")
	private WalkerGroupType formation = WalkerGroupType.POINT;

	@XmlAttribute(name = "rows")
	private String rowValues;

	@XmlAttribute(name = "loop_type")
	private LoopType loopType = LoopType.NORMAL;

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
		if (loopType == LoopType.WALK_BACK) { // add steps in backward order, so npcs turn and walk the same way back
			for (int i = routeStepList.size() - 2; i > 0; i--) { // skip first and last step
				RouteStep step = routeStepList.get(i);
				routeStepList.add(new RouteStep(step.getX(), step.getY(), step.getZ(), step.getRestTime()));
			}
		}
		for (int i = 0; i < routeStepList.size() - 1; i++) {
			RouteStep step = routeStepList.get(i);
			step.setStepIndex(i);
		}
		RouteStep lastStep = routeStepList.get(routeStepList.size() - 1);
		lastStep.setStepIndex(routeStepList.size() - 1);
		lastStep.setIsLastStep(true);

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

	public RouteStep getRouteStep(int stepIndex) {
		return routeStepList.get(stepIndex);
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

	public WalkerGroupType getType() {
		return formation;
	}

	public void setType(WalkerGroupType type) {
		formation = type;
	}

	public LoopType getLoopType() {
		return loopType;
	}

	public void setLoopType(LoopType loopType) {
		this.loopType = loopType;
	}

	public int[] getRows() {
		return rows;
	}

	public void setRows(int[] rows) {
		this.rows = rows;
	}

}
