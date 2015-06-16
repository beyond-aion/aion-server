package com.aionemu.gameserver.services.raid;

import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventListener;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.services.InvasionRaidService;


/**
 * @author Alcapwnd
 */
public class InvasionRaidDeathListener extends OnDieEventListener {

    private final InvasionRaid<?> raid;

    @SuppressWarnings("rawtypes")
    public InvasionRaidDeathListener(InvasionRaid raid) {
        this.raid = raid;
    }

    public void onBeforeDie(AbstractAI obj) {
        AionObject winner = raid.getBoss().getAggroList().getMostDamage();

        InvasionRaidService.getInstance().capture(raid.getId());
    }

    public void onAfterDie(AbstractAI obj) {
    }
}
