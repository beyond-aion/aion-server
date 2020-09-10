package com.aionemu.gameserver.model.animations;

import com.aionemu.gameserver.network.aion.serverpackets.SM_ACTION_ANIMATION;

/**
 * These IDs are for use with {@link SM_ACTION_ANIMATION}.
 *
 * @author Yeats
 */
public enum ActionAnimation {

    LEVEL_UP(0),
    UNK(1),
    BIND_KISK(2),
    REPAIR_GATE(3),
    CRAFT_LEVEL_UP(4),
    CLASS_CHANGE(4);

    private int id;

    ActionAnimation(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
