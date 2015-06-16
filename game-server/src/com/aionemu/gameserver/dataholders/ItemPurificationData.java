package com.aionemu.gameserver.dataholders;

import com.aionemu.gameserver.model.templates.item.purification.ItemPurificationTemplate;
import com.aionemu.gameserver.model.templates.item.purification.PurificationResultItem;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Ranastic
 * @rework Navyan
 */
@XmlRootElement(name = "item_purifications")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemPurificationData {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ItemPurificationData.class);
    @XmlElement(name = "item_purification")
    protected List<ItemPurificationTemplate> ItemPurificationTemplates;
    private TIntObjectHashMap<ItemPurificationTemplate> itemPurificationSets;
    private FastMap<Integer, FastMap<Integer, PurificationResultItem>> ResultItemMap;

    void afterUnmarshal(Unmarshaller u, Object parent) {
        itemPurificationSets = new TIntObjectHashMap<ItemPurificationTemplate>();
        ResultItemMap = new FastMap<Integer, FastMap<Integer, PurificationResultItem>>();

        for (ItemPurificationTemplate set : ItemPurificationTemplates) {
            itemPurificationSets.put(set.getPurification_base_item_id(), set);

            ResultItemMap.put(set.getPurification_base_item_id(), new FastMap<Integer, PurificationResultItem>());

            if (!set.getPurification_result_item().isEmpty()) {
                for (PurificationResultItem resultItem : set.getPurification_result_item()) {
                    ResultItemMap.get(set.getPurification_base_item_id()).put(resultItem.getItem_id(), resultItem);
                }
            }
        }
        ItemPurificationTemplates = null;
    }

    /**
     * @param itemSetId
     * @return
     */
    public ItemPurificationTemplate getItemPurificationTemplate(int itemSetId) {
        return itemPurificationSets.get(itemSetId);
    }

    public FastMap<Integer, PurificationResultItem> getResultItemMap(int baseItemId) {
        if (ResultItemMap.containsKey(baseItemId)) {
            if (!ResultItemMap.get(baseItemId).isEmpty()) {
                return ResultItemMap.get(baseItemId);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * @return itemSets.size()
     */
    public int size() {
        return itemPurificationSets.size();
    }
}
