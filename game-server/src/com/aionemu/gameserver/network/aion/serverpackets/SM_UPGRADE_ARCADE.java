package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.templates.arcadeupgrade.ArcadeTab;
import com.aionemu.gameserver.model.templates.arcadeupgrade.ArcadeTabItemList;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.ArcadeUpgradeService;

/**
 * @author ginho1
 * @modified Estrayl
 */
public class SM_UPGRADE_ARCADE extends AionServerPacket {

	private int action;
	private int frenzyPoints;
	private int level = 1;
	private int neededTokenToResume;
	private int sessionId;
	private boolean showIcon;
	private boolean success;
	private boolean resumeAllowed;
	private ArcadeTabItemList item;

	public SM_UPGRADE_ARCADE(boolean showIcon) {
		this.action = 0;
		this.showIcon = showIcon;
	}

	public SM_UPGRADE_ARCADE() {
		this.action = 1;
	}

	public SM_UPGRADE_ARCADE(int action) {
		this.action = action;
	}

	public SM_UPGRADE_ARCADE(int action, boolean success, int frenzy) {
		this.action = action;
		this.success = success;
		this.frenzyPoints = frenzy;
	}
	
	public SM_UPGRADE_ARCADE(int action, int frenzyLevel, int neededTokenToResume, boolean resumeAllowed) {
		this.action = action;
		this.level = frenzyLevel;
		this.neededTokenToResume = neededTokenToResume;
		this.resumeAllowed = resumeAllowed;
	}
	
	public SM_UPGRADE_ARCADE(int action, int frenzyPoints, int sessionId) {
		this.action = action;
		this.frenzyPoints = frenzyPoints;
		this.sessionId = sessionId;
	}

	public SM_UPGRADE_ARCADE(int action, int level) {
		this.action = action;
		this.level = level;
	}

	public SM_UPGRADE_ARCADE(int action, ArcadeTabItemList item) {
		this.action = action;
		this.item = item;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action);

		switch (action) {
			case 0:// show icon
				writeD(showIcon ? 1 : 0);
				break;
			case 1: // show start upgrade arcade info
				writeD(sessionId);// SessionId
				writeD(frenzyPoints);// frenzy meter
				writeD(1);
				writeD(4);
				writeD(6);
				writeD(8);
				writeD(8);// max upgrade
				writeH(272);
				writeS("success_weapon01");
				writeS("success_weapon01");
				writeS("success_weapon01");
				writeS("success_weapon02");
				writeS("success_weapon02");
				writeS("success_weapon03");
				writeS("success_weapon03");
				writeS("success_weapon04");
				break;
			case 2: // open upgrade arcade
				writeC(1);// unk
				break;
			case 3: // try result
				writeC(success ? 1 : 0);// 1 success - 0 fail
				writeD(frenzyPoints > 100 ? 100 : frenzyPoints);// frenzyPoints
				break;
			case 4: // try result
				writeD(level);// upgradeLevel
				break;
			case 5: // show fail
				writeD(level);// upgradeLevel
				writeC(resumeAllowed ? 1 : 0);// canResume? 1 yes - 0 no
				writeD(neededTokenToResume);// needed Arcade Token
				writeD(0);// unk
				break;
			case 6: // show reward icon
				writeD(item.getItemId());// templateId
				writeD(item.getNormalCount() > 0 ? item.getNormalCount() : item.getFrenzyCount());// itemCount
				writeD(0);// unk
				break;
			case 7: // frenzy time
				writeD(level);// time
				break;
			case 8: // fail due less tokens
				writeC(1);
				break;
			case 10: // show reward list
				List<ArcadeTab> arcadeTabs = ArcadeUpgradeService.getInstance().getTabs();

				for (ArcadeTab arcadetab : arcadeTabs) {
					writeC(arcadetab.getArcadeTabItems().size());
				}

				for (ArcadeTab arcadetab : arcadeTabs) {
					for (ArcadeTabItemList arcadetabitem : arcadetab.getArcadeTabItems()) {
						writeD(arcadetabitem.getItemId());
						writeD(arcadetabitem.getNormalCount());
						writeD(0);
						writeD(arcadetabitem.getFrenzyCount());
						writeD(0);
					}
				}
				break;
		}
	}
}
