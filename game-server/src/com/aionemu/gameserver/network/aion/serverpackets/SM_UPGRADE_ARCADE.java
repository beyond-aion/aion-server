package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.templates.arcadeupgrade.ArcadeTab;
import com.aionemu.gameserver.model.templates.arcadeupgrade.ArcadeTabItemList;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.ArcadeUpgradeService;

/**
 * @author ginho1
 */
public class SM_UPGRADE_ARCADE extends AionServerPacket {

	private int action;
	private int showicon = 1;
	private int frenzy = 0;
	private int frenzyLevel = 1;
	private boolean success = false;
	private ArcadeTabItemList item;

	public SM_UPGRADE_ARCADE(boolean showicon) {
		this.action = 0;
		this.showicon = showicon ? 1 : 0;
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
		this.frenzy = frenzy;
	}

	public SM_UPGRADE_ARCADE(int action, int frenzyLevel) {
		this.action = action;
		this.frenzyLevel = frenzyLevel;
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
				writeD(showicon);
				break;
			case 1: // show start upgrade arcade info
				writeD(64519);// SessionId
				writeD(0);// frenzy meter
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
				writeD(frenzy > 100 ? 100 : frenzy);// frenzyPoints
				break;
			case 4: // try result
				writeD(frenzyLevel);// upgradeLevel
				break;
			case 5: // show fail
				writeD(1);// upgradeLevel
				writeC(0);// canResume? 1 yes - 0 no
				writeD(0);// needed Arcade Token
				writeD(0);// unk
				break;
			case 6: // show reward icon
				writeD(item.getItemId());// templateId
				writeD(item.getNormalCount() > 0 ? item.getNormalCount() : item.getFrenzyCount());// itemCount
				writeD(0);// unk
				break;
			case 7: // frenzy time
				writeD(frenzyLevel);// time
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
