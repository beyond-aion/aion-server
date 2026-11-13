package com.aionemu.gameserver.model.templates.tradelist;

import java.util.List;

import jakarta.xml.bind.annotation.*;

import com.aionemu.gameserver.model.Race;

/**
 * @author orz
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "tradelist_template")
public class TradeListTemplate {

	@XmlAttribute(name = "npc_id", required = true)
	private int npcId;
	@XmlAttribute(name = "npc_type")
	private TradeNpcType tradeNpcType = TradeNpcType.NORMAL;
	@XmlAttribute(name = "sell_price_rate")
	private int sellPriceRate = 100;
	@XmlAttribute(name = "sell_price_rate2")
	private int sellPriceRate2 = 100;
	@XmlAttribute(name = "ap_sell_price_rate2")
	private int apSellPriceRate2 = 100;
	@XmlAttribute(name = "buy_price_rate")
	private int buyPriceRate;
	@XmlAttribute(name = "save_count")
	private Boolean saveCount;
	@XmlElement(name = "tradelist")
	protected List<TradeTab> tradeTablist;

	public int getNpcId() {
		return npcId;
	}

	public TradeNpcType getTradeNpcType() {
		return tradeNpcType;
	}

	public int getSellPriceRate() {
		return sellPriceRate;
	}

	public int getSellPriceRate2() {
		return sellPriceRate2;
	}

	public int getApSellPriceRate2() {
		return apSellPriceRate2;
	}

	public int getBuyPriceRate() {
		return buyPriceRate;
	}

	public Boolean isSaveCount() {
		return saveCount;
	}

	public List<Integer> getTradeTabIds() {
		return tradeTablist == null ? List.of() : tradeTablist.stream().map(TradeTab::getId).toList();
	}

	public List<TradeTab> getTradeTabs() {
		return tradeTablist == null ? List.of() : tradeTablist;
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * <p>
	 * The following schema fragment specifies the expected content contained within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}int" />
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "Tradelist")
	public static class TradeTab {

		@XmlAttribute
		protected int id;

		/**
		 * Only players of this race see the tab. Lets one merchant serve both races, like house merchants that sell the kisks and teleport scrolls of
		 * the buyer's race.<br>
		 * Not retail: later versions give Asmodians their own camping and club merchants (NPCs 810059-810064, housing objects 3195143-3195148). The
		 * 4.8 client has neither and crashes when such an object enters the house registry, so the Asmodian contracts place the Elyos objects and
		 * these merchants carry both races' tabs. Revert to separate merchants once the client data has them.
		 */
		@XmlAttribute
		protected Race race;

		/**
		 * Gets the value of the id property.
		 */
		public int getId() {
			return id;
		}

		public boolean isAvailableFor(Race playerRace) {
			return race == null || race == playerRace;
		}
	}
}
