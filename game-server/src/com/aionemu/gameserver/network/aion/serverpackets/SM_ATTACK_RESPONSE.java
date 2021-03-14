package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

public class SM_ATTACK_RESPONSE extends AionServerPacket {

    private int message; // 3 unk
    private int attackCount;

    public static SM_ATTACK_RESPONSE TARGET_IN_DIFFERENT_AREA(int count) {
        return new SM_ATTACK_RESPONSE(1, count);
    }

    // stops attacks
    public static SM_ATTACK_RESPONSE STOP_INVALID_TARGET(int count) {
        return new SM_ATTACK_RESPONSE(2, count);
    }

    public static SM_ATTACK_RESPONSE TARGET_TOO_FAR_AWAY(int count) {
        return new SM_ATTACK_RESPONSE(4, count);
    }

    // stops attacks
    public static SM_ATTACK_RESPONSE STOP_OBSTACLE_IN_THE_WAY(int count) {
        return new SM_ATTACK_RESPONSE(5, count);
    }

    // stops attacks
    public static SM_ATTACK_RESPONSE STOP_TOO_CLOSE_TO_ATTACK(int count) {
        return new SM_ATTACK_RESPONSE(6, count);
    }

    // stops attacks
    public static SM_ATTACK_RESPONSE STOP_WITHOUT_MESSAGE(int count) {
        return new SM_ATTACK_RESPONSE(7, count);
    }

    private SM_ATTACK_RESPONSE(int message, int attackCount) {
        this.message = message;
        this.attackCount = attackCount;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(message);
        writeC(attackCount);
    }
}
