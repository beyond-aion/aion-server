package com.aionemu.gameserver.model.base;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;

/**
 * @author Estrayl
 */
public enum BaseOccupier {

	ELYOS,
	ASMODIANS,

	BALAUR(PanesterraFaction.BALAUR),

	BELUS(PanesterraFaction.BELUS),
	IVY_TEMPLE(PanesterraFaction.IVY_TEMPLE),
	HIGHLAND_TEMPLE(PanesterraFaction.HIGHLAND_TEMPLE),
	ALPINE_TEMPLE(PanesterraFaction.ALPINE_TEMPLE),
	GRANDWEIR_TEMPLE(PanesterraFaction.GRANDWEIR_TEMPLE),

	ASPIDA(PanesterraFaction.ASPIDA),
	NOERREN_TEMPLE(PanesterraFaction.NOERREN_TEMPLE),
	BOREALIS_TEMPLE(PanesterraFaction.BOREALIS_TEMPLE),
	MYRKREN_TEMPLE(PanesterraFaction.MYRKREN_TEMPLE),
	GLUMVEILEN_TEMPLE(PanesterraFaction.GLUMVEILEN_TEMPLE),

	ATANATOS(PanesterraFaction.ATANATOS),
	MEMORIA_TEMPLE(PanesterraFaction.MEMORIA_TEMPLE),
	SYBILLINE_TEMPLE(PanesterraFaction.SYBILLINE_TEMPLE),
	AUSTERITY_TEMPLE(PanesterraFaction.AUSTERITY_TEMPLE),
	SERENITY_TEMPLE(PanesterraFaction.SERENITY_TEMPLE),

	DISILLON(PanesterraFaction.DISILLON),
	NECROLUCE_TEMPLE(PanesterraFaction.NECROLUCE_TEMPLE),
	ESMERAUDUS_TEMPLE(PanesterraFaction.ESMERAUDUS_TEMPLE),
	VOLTAIC_TEMPLE(PanesterraFaction.VOLTAIC_TEMPLE),
	ILLUMINATUS_TEMPLE(PanesterraFaction.ILLUMINATUS_TEMPLE),

	PEACE(PanesterraFaction.PEACE);

	private final PanesterraFaction panesterraFaction;

	BaseOccupier() {
		panesterraFaction = null;
	}

	BaseOccupier(PanesterraFaction panesterraFaction) {
		this.panesterraFaction = panesterraFaction;
	}

	public PanesterraFaction getPanesterraFaction() {
		return panesterraFaction;
	}
	
	public static BaseOccupier findBy(PanesterraFaction panesterraFaction) {
		for (BaseOccupier baseOccupier : values()) {
			if (baseOccupier.panesterraFaction == panesterraFaction) {
				return baseOccupier;
			}
		}
		return null;
	}
	
	public static BaseOccupier findBy(Race race) {
		return switch (race) {
			case ELYOS -> ELYOS;
			case ASMODIANS -> ASMODIANS;
			default -> BALAUR;
		};
	}
}
