package ai.siege;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AI2Request;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorTemplate;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Source
 */
@AIName("fortressgate")
public class SiegeFortressGateAI2 extends NpcAI2 {

    private String doorName;
    private StaticDoorTemplate template;

    @Override
    protected void handleDialogStart(Player player) {
        AI2Actions.addRequest(this, player, 160017, 0, new AI2Request() {

            @Override
            public void acceptRequest(Creature requester, Player responder, int requestId) {
                if (MathUtil.isInRange(requester, responder, 10)) {
                    TeleportService2.moveToTargetWithDistance(requester, responder,
                            PositionUtil.isBehind(requester, responder) ? 0 : 1, 3);
                } else {
                    PacketSendUtility.sendBrightYellowMessageOnCenter(responder, "You too far away");
                }
            }
        });
    }

    @Override
    protected void handleDialogFinish(Player player) {
    }

    @Override
    protected AIAnswer pollInstance(AIQuestion question) {
        switch (question) {
            case SHOULD_DECAY:
                return AIAnswers.NEGATIVE;
            case SHOULD_RESPAWN:
                return AIAnswers.NEGATIVE;
            default:
                return null;
        }
    }

    @Override
    protected void handleSpawned() {
        super.handleSpawned();
        doorName = GeoService.getInstance().getDoorName(getOwner().getWorldId(), "ab_castledoor_100.cgf", getOwner().getX(), getOwner().getY(), getOwner().getZ());
        if (doorName != null) {
            GeoService.getInstance().setDoorState(getOwner().getWorldId(), getOwner().getInstanceId(), doorName, false);
        }
        else {
            System.out.println(" Not found Siege door name ");
        }
    }

    @Override
    protected void handleDespawned() {
        super.handleDespawned();
        if (/*template != null &&*/ doorName != null) {
            GeoService.getInstance().setDoorState(getOwner().getWorldId(), getOwner().getInstanceId(), doorName, true);
        }
    }

}
