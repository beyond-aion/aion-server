package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropGroup;
import com.aionemu.gameserver.model.drop.NpcDrop;
import com.aionemu.gameserver.model.drop.NpcDrop.ReplaceType;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;

/**
 * @author MrPoke
 */
public class NpcDropData {

	private static Logger log = LoggerFactory.getLogger(DataManager.class);

	private List<NpcDrop> npcDrop;

	/**
	 * @return the npcDrop
	 */
	public List<NpcDrop> getNpcDrop() {
		return npcDrop;
	}

	/**
	 * @param npcDrop
	 *          the npcDrop to set
	 */
	public void setNpcDrop(List<NpcDrop> npcDrop) {
		this.npcDrop = npcDrop;
	}

	public int size() {
		return npcDrop.size();
	}

	public static NpcDropData load() {

		List<Drop> drops = new ArrayList<Drop>();
		List<String> names = new ArrayList<String>();
		List<NpcDrop> npcDrops = new ArrayList<NpcDrop>();
		FileChannel roChannel = null;
		MappedByteBuffer buffer;

		try (RandomAccessFile file = new RandomAccessFile("data/static_data/npc_drop.dat", "r")) {
			roChannel = file.getChannel();
			int size = (int) roChannel.size();
			buffer = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load();
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			int count = buffer.getInt();
			for (int i = 0; i < count; i++) {
				drops.add(Drop.load(buffer));
			}

			count = buffer.getInt();

			for (int i = 0; i < count; i++) {
				int lenght = buffer.get();
				byte[] byteString = new byte[lenght];
				buffer.get(byteString);
				String name = new String(byteString);
				names.add(name);
			}

			count = buffer.getInt();
			for (int i = 0; i < count; i++) {
				int npcId = buffer.getInt();

				int groupCount = buffer.getInt();
				List<DropGroup> dropGroupList = new ArrayList<DropGroup>(groupCount);
				for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
					Race race;
					byte raceId = buffer.get();
					switch (raceId) {
						case 0:
							race = Race.ELYOS;
							break;
						case 1:
							race = Race.ASMODIANS;
							break;
						default:
							race = Race.PC_ALL;
					}

					boolean useCategory = buffer.get() == 1 ? true : false;
					String groupName = names.get(buffer.getShort());
					int maxItems = DropConfig.DROP_ENABLE_SUPPORT_NEW_NPCDROPS_FILES ? buffer.getInt() : 1;
					
					int dropCount = buffer.getInt();
					List<Drop> dropList = new ArrayList<Drop>(dropCount);
					for (int dropIndex = 0; dropIndex < dropCount; dropIndex++) {
						dropList.add(drops.get(buffer.getInt()));
					}
					DropGroup dropGroup = new DropGroup(dropList, race, useCategory, groupName, maxItems);
					dropGroupList.add(dropGroup);
				}
				NpcDrop npcDrop = new NpcDrop(dropGroupList, npcId);
				npcDrops.add(npcDrop);
				
				NpcDrop customDrop = DataManager.CUSTOM_NPC_DROP.getNpcDrop(npcId);
				if(customDrop != null)
					mergeWithCustomDrop(npcDrop, customDrop);
				
				NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(npcId);
				if (npcTemplate != null) {
					npcTemplate.setNpcDrop(npcDrop);
				}
			}
			drops.clear();
			drops = null;
			names.clear();
			names = null;
		}
		catch (FileNotFoundException e) {
			log.error("Drop loader: Missing npc_drop.dat!!!");
		}
		catch (IOException e) {
			log.error("Drop loader: IO error in drop Loading.");
		}
		NpcDropData dropData = new NpcDropData();
		log.info("Drop loader: Npc drops loading done.");
		dropData.setNpcDrop(npcDrops);
		return dropData;

	}

	public static void reload() {
		TIntObjectHashMap<NpcTemplate> npcData = DataManager.NPC_DATA.getNpcData();
		npcData.forEachValue(new TObjectProcedure<NpcTemplate>() {

			@Override
			public boolean execute(NpcTemplate npcTemplate) {
				npcTemplate.setNpcDrop(null);
				return false;
			}
		});
		load();
	}
	
	private static void mergeWithCustomDrop(NpcDrop originalDrop, NpcDrop customDrop) {
		if(customDrop.getReplaceType() == ReplaceType.FULL) {
			originalDrop.getDropGroup().clear();
			originalDrop.getDropGroup().addAll(customDrop.getDropGroup());
		}
		else if(customDrop.getReplaceType() == ReplaceType.PARTIAL) {
			List<DropGroup> toDel = new ArrayList<DropGroup>();
			for(DropGroup customDg : customDrop.getDropGroup()) {
				for(DropGroup originalDg : originalDrop.getDropGroup()) {
					if(originalDg.getGroupName().equals(customDg.getGroupName())) {
						toDel.add(originalDg);
					}
				}
			}
			originalDrop.getDropGroup().removeAll(toDel);
			originalDrop.getDropGroup().addAll(customDrop.getDropGroup());
			toDel.clear();
			toDel = null;
		}
	}
	
}
