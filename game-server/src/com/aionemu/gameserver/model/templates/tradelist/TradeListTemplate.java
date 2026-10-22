package com.aionemu.gameserver.model.templates.tradelist;

import java.util.List;

import jakarta.xml.bind.annotation.*;

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
		 * Gets the value of the id property.
		 */
		public int getId() {
			return id;
		}
	}
}
