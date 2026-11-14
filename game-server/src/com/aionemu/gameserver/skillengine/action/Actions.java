package com.aionemu.gameserver.skillengine.action;

import java.util.List;

import jakarta.xml.bind.annotation.*;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Actions", propOrder = { "actions" })
public class Actions {

	@XmlElements({ @XmlElement(name = "itemuse", type = ItemUseAction.class), @XmlElement(name = "mpuse", type = MpUseAction.class),
		@XmlElement(name = "hpuse", type = HpUseAction.class), @XmlElement(name = "dpuse", type = DpUseAction.class) })
	protected List<Action> actions;

	public List<Action> getActions() {
		return actions == null ? List.of() : actions;
	}
}
