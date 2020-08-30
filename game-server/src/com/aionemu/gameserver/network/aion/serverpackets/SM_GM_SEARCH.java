package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

public class SM_GM_SEARCH extends AionServerPacket {

    private Player player;

    public SM_GM_SEARCH(Player player) {
        this.player = player;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeS("search " + player.getName() + " " + player.getWorldId() + " " + (int)player.getX() + " " + (int)player.getY() + " " + (int)player.getZ());
    }
}
