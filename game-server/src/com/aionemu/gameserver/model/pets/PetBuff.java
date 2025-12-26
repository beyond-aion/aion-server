package com.aionemu.gameserver.model.pets;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;
import com.aionemu.gameserver.model.templates.pet.PetBonusAttr;
import com.aionemu.gameserver.model.templates.pet.PetFunctionType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PET;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.change.Func;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
 */

public class PetBuff implements StatOwner {
    private static final int FOOD_ID = 182007162;
    private final PetBonusAttr petBonusAttr;
    private final List<IStatFunction> functions = new ArrayList<>();
    private Instant startTime;
    private final AtomicReference<ScheduledFuture<?>> taskRef = new AtomicReference<>();

    public PetBuff(int buffId) {
        this.petBonusAttr = Objects.requireNonNullElse(DataManager.PET_BUFF_DATA.getPetBonusAttr(buffId), null);
    }

    public void applyEffect(Player player, int millis) {
        if (hasPetBuff() || petBonusAttr == null) {
            return;
        }
        if (millis > 0) {
            taskRef.set(ThreadPoolManager.getInstance().schedule(new BuffTask(player), millis));
        }
        startTime = Instant.now();
        for (var penalty : petBonusAttr.getPenaltyAttr()) {
            int base = player.getGameStats().getStat(penalty.getStat(), 0).getBase();
            int value = penalty.getFunc() == Func.PERCENT
                    ? base * penalty.getValue() / 100
                    : penalty.getValue();
            functions.add(new StatAddFunction(penalty.getStat(), value, true));
        }
        player.getGameStats().addEffect(this, List.copyOf(functions));
        PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_BUFF_PET_USE_START_MESSAGE());
        PacketSendUtility.sendPacket(player, new SM_PET(true));
    }

    public void endEffect(Player player) {
        functions.clear();
        var task = taskRef.getAndSet(null);
        if (task != null) {
            task.cancel(false);
        }
        player.getGameStats().endEffect(this);
        PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_BUFF_PET_USE_STOP_MESSAGE());
        PacketSendUtility.sendPacket(player, new SM_PET(false));
        PacketSendUtility.sendPacket(player, new SM_STATS_INFO(player));
    }

    @SuppressWarnings("unused")
    public int getBuffRemainingSeconds() {
        return startTime == null ? 0 : (int) Duration.between(startTime, Instant.now()).toSeconds();
    }

    public boolean hasPetBuff() {
        var task = taskRef.get();
        return task != null && !task.isDone();
    }

    private final class BuffTask implements Runnable {
        private final Player player;

        BuffTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            var pet = player.getPet();
            if (pet == null) {
                endEffect(player);
                return;
            }
            var petTemplate = DataManager.PET_DATA.getPetTemplate(pet.getObjectTemplate().getTemplateId());
            var petBuff = DataManager.PET_BUFF_DATA.getPetBonusAttr(petTemplate.getPetFunction(PetFunctionType.CHERRY).getId());
            int foodCount = petBuff.getFoodCount();
            if (player.getInventory().getItemCountByItemId(FOOD_ID) >= foodCount) {
                player.getInventory().decreaseByItemId(FOOD_ID, foodCount);
                taskRef.set(ThreadPoolManager.getInstance().schedule(this, 300_000));
                PacketSendUtility.sendPacket(player, new SM_PET(true));
            } else {
                endEffect(player);
            }
        }
    }
}