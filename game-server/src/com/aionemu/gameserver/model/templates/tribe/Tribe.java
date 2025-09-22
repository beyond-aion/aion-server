package com.aionemu.gameserver.model.templates.tribe;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.TribeClass;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Tribe")
public class Tribe {

	@XmlList
	protected List<TribeClass> aggro;
	@XmlList
	protected List<TribeClass> hostile;
	@XmlList
	protected List<TribeClass> friend;
	@XmlList
	protected List<TribeClass> neutral;
	@XmlList
	protected List<TribeClass> none;
	@XmlList
	protected List<TribeClass> support;

	@XmlAttribute
	protected TribeClass base = TribeClass.NONE;

	@XmlAttribute(required = true)
	protected TribeClass name;

	public List<TribeClass> getAggro() {
		if (aggro == null) {
			aggro = Collections.emptyList();
		}
		return this.aggro;
	}

	public List<TribeClass> getHostile() {
		if (hostile == null) {
			hostile = Collections.emptyList();
		}
		return this.hostile;
	}

	public List<TribeClass> getFriend() {
		if (friend == null) {
			friend = Collections.emptyList();
		}
		return this.friend;
	}

	public List<TribeClass> getNeutral() {
		if (neutral == null) {
			neutral = Collections.emptyList();
		}
		return this.neutral;
	}

	public List<TribeClass> getNone() {
		if (none == null) {
			none = Collections.emptyList();
		}
		return this.none;
	}

	public List<TribeClass> getSupport() {
		if (support == null) {
			support = Collections.emptyList();
		}
		return this.support;
	}

	public TribeClass getBase() {
		return base == TribeClass.NONE ? name : base;
	}

	public TribeClass getName() {
		return name;
	}

	public final boolean isGuard() {
		return name.isGuard();
	}

	@Override
	public String toString() {
		return name + " (" + base + ")";
	}
}
