package com.aionemu.gameserver.services.panesterra.ahserion;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javolution.util.FastTable;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Yeats
 *
 */
public class PanesterraMatchmakingService {

	private static final PanesterraMatchmakingService instance = new PanesterraMatchmakingService();
	private List<Integer> asmodians = new FastTable<>();
	private List<Integer> elyos = new FastTable<>();
	private List<PlayerClass> registeredClasses = new FastTable<>();
	private Map<PanesterraTeamId, PanesterraTeam> teams;
	private AtomicBoolean started = new AtomicBoolean(false);
	private AtomicBoolean isFinished = new AtomicBoolean(false);
	private Future<?> notifier;
	
	public static PanesterraMatchmakingService getInstance() {
		return instance;
	}
	
	public void onStart() {
		if (!started.get() && !isFinished.get()) {
			if (asmodians == null) {
				asmodians = new FastTable<>();
			}
			if (elyos == null) {
				elyos = new FastTable<>();
			}
			if (registeredClasses == null) {
				registeredClasses = new FastTable<>();
			}
			started.set(true);
			spawnPortals();
			startNotifier();
		}
	}
	
	private void startNotifier() {		
		notifier = ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				for (Integer id : elyos) {
					Player p = World.getInstance().findPlayer(id);
					if (p != null) {
						PacketSendUtility.sendMessage(p, "Ahserions Flight starts in 30 seconds.", ChatType.WHITE_CENTER);
					}
				}
				for (Integer id : asmodians) {
					Player p = World.getInstance().findPlayer(id);
					if (p != null) {
						PacketSendUtility.sendMessage(p, "Ahserions Flight starts in 30 seconds.", ChatType.WHITE_CENTER);
					}
				}
			}
		}, 570000); //9,5min
	}
	
	private void cancelNotifier() {
		if (notifier != null && !notifier.isCancelled()) {
			notifier.cancel(true);
		}
	}
	
	private void spawnPortals() {
		SpawnTemplate asmo = SpawnEngine.addNewSingleTimeSpawn(120080000, 802219, 400.772f, 231.517f, 93.113f, (byte) 30);
		SpawnTemplate ely = SpawnEngine.addNewSingleTimeSpawn(110070000, 802219, 485.692f, 401.079f, 127.789f, (byte) 0);
		SpawnEngine.spawnObject(asmo, 1);
		SpawnEngine.spawnObject(ely, 1);
	}
	
	public void onStop() {
		started.set(false);
		isFinished.set(false);
		cancelNotifier();
		asmodians = null;
		elyos = null;
		registeredClasses = null;
		Npc asmo = World.getInstance().getWorldMap(120080000).getMainWorldMapInstance().getNpc(802219);
		Npc ely = World.getInstance().getWorldMap(110070000).getMainWorldMapInstance().getNpc(802219);
		if (asmo != null) {
			asmo.getController().onDelete();
		}
		if (ely != null) {
			ely.getController().onDelete();
		}
	}
	
	public synchronized boolean registerPlayer(Player player) {
		if (player == null || !started.get() || isFinished.get() || AhserionRaid.getInstance().getStatus() != AhserionRaidStatus.PREPARING_REGISTRATION) {
			return false;
		}
		switch (player.getRace()) {
			case ELYOS:
				if (!elyos.contains(player.getObjectId())) {
					return elyos.add(player.getObjectId());
				}
				break;
			case ASMODIANS:
				if (!asmodians.contains(player.getObjectId())) {
					return asmodians.add(player.getObjectId());
				}
				break;
		}
		return false;
	}
	
	public boolean isPlayerRegistered(Player player) {
		if (player == null || !started.get() || isFinished.get())
			return false;
		switch (player.getRace()) {
			case ELYOS:
				return elyos.contains(player.getObjectId());
			case ASMODIANS:
				return asmodians.contains(player.getObjectId());
			default:
				return false;
		}
	}
	
	public void prepareTeams() {
		if (started.get() && !isFinished.get()) {
			isFinished.set(true);
		} else {
			onStop();
			return;
		}
		//Remove offline Players
		if (asmodians != null && !asmodians.isEmpty()) {
			Iterator<Integer> iter = asmodians.iterator();
			while (iter.hasNext()) {
				Player curPlayer = World.getInstance().findPlayer(iter.next());
				if (curPlayer == null) {
					iter.remove();
				} else if (!registeredClasses.contains(curPlayer.getPlayerClass())) {
					registeredClasses.add(curPlayer.getPlayerClass());
				}
			}
		} else {
			notifyFail();
			onStop();
			return;
		}
		
		if (elyos != null && !elyos.isEmpty()) {
			Iterator<Integer> iter = elyos.iterator();
			while (iter.hasNext()) {
				Player curPlayer = World.getInstance().findPlayer(iter.next());
				if (curPlayer == null) {
					iter.remove();
				} else if (!registeredClasses.contains(curPlayer.getPlayerClass())) {
					registeredClasses.add(curPlayer.getPlayerClass());
				}
			}
		} else {
			notifyFail();
			onStop();
			return;
		}
		/**
		 * for now we will only allow two teams.
		 * TODO
			byte teamType = SiegeConfig.AHSERION_TEAM_TYPE;
			if (teamType == 3) { //X vs X, and X vs X vs X vs X allowed

			} else if (teamType == 2) { //only X vs X allowed
			
			} else { //only X vs X vs X vs X allowed
			
			}
		*/
		int minPlayers = SiegeConfig.AHSERION_MIN_PLAYERS_TEAM_TYPE_1;
		int maximumPlayers = SiegeConfig.AHSERION_MAX_PLAYERS_TEAM_TYPE_1;

		//set teamType and maximum Players
		if (asmodians.size() < minPlayers || elyos.size() < minPlayers) {
			notifyFail();
			onStop();
			return;
		} 
		if (elyos.size() > asmodians.size()) {
			if (asmodians.size() < maximumPlayers){
				maximumPlayers = asmodians.size();
			}
		} else {
			if (elyos.size() < maximumPlayers) {
				maximumPlayers = elyos.size();
			}
		}
		createTeams(2, maximumPlayers);
	}
	
	private void createTeams(int teamType, int maxPlayers) {
		teams = new ConcurrentHashMap<>();
		switch (teamType) {
			case 1: //X vs X vs X vs X
					teams.put(PanesterraTeamId.GAB1_SUB_DEST_69, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_69));
					teams.put(PanesterraTeamId.GAB1_SUB_DEST_70, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_70));
					teams.put(PanesterraTeamId.GAB1_SUB_DEST_71, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_71));
					teams.put(PanesterraTeamId.GAB1_SUB_DEST_72, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_72));
					buildTeams(1, 24, PanesterraTeamId.GAB1_SUB_DEST_69, PanesterraTeamId.GAB1_SUB_DEST_70, 
						PanesterraTeamId.GAB1_SUB_DEST_71, PanesterraTeamId.GAB1_SUB_DEST_72);
				break;
			case 2: // X vs X
				//random start position
				int rnd2 = Rnd.get(0, 5);
				switch (rnd2) {
					case 0:
						teams.put(PanesterraTeamId.GAB1_SUB_DEST_69, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_69));
						teams.put(PanesterraTeamId.GAB1_SUB_DEST_70, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_70));
						buildTeams(2, maxPlayers, PanesterraTeamId.GAB1_SUB_DEST_69, PanesterraTeamId.GAB1_SUB_DEST_70, null, null);
						break;
					case 1:
						teams.put(PanesterraTeamId.GAB1_SUB_DEST_70, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_70));
						teams.put(PanesterraTeamId.GAB1_SUB_DEST_71, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_71));
						buildTeams(2, maxPlayers, PanesterraTeamId.GAB1_SUB_DEST_70, PanesterraTeamId.GAB1_SUB_DEST_71, null, null);
						break;
					case 2:
						teams.put(PanesterraTeamId.GAB1_SUB_DEST_71, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_71));
						teams.put(PanesterraTeamId.GAB1_SUB_DEST_72, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_72));
						buildTeams(2, maxPlayers, PanesterraTeamId.GAB1_SUB_DEST_71, PanesterraTeamId.GAB1_SUB_DEST_72, null, null);
						break;
					case 3:
						teams.put(PanesterraTeamId.GAB1_SUB_DEST_72, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_72));
						teams.put(PanesterraTeamId.GAB1_SUB_DEST_69, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_69));
						buildTeams(2, maxPlayers, PanesterraTeamId.GAB1_SUB_DEST_72, PanesterraTeamId.GAB1_SUB_DEST_69, null, null);
						break;
					case 4:
						teams.put(PanesterraTeamId.GAB1_SUB_DEST_69, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_69));
						teams.put(PanesterraTeamId.GAB1_SUB_DEST_71, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_71));
						buildTeams(2, maxPlayers, PanesterraTeamId.GAB1_SUB_DEST_69, PanesterraTeamId.GAB1_SUB_DEST_71, null, null);
						break;
					case 5:
						teams.put(PanesterraTeamId.GAB1_SUB_DEST_70, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_70));
						teams.put(PanesterraTeamId.GAB1_SUB_DEST_72, new AhserionTeam(PanesterraTeamId.GAB1_SUB_DEST_72));
						buildTeams(2, maxPlayers, PanesterraTeamId.GAB1_SUB_DEST_70, PanesterraTeamId.GAB1_SUB_DEST_72, null, null);
						break;
				}
				break;
				default:
					break;
		}
	}

	private void buildTeams(int teamType, int maxPlayers, PanesterraTeamId team1, PanesterraTeamId team2, PanesterraTeamId team3, PanesterraTeamId team4) {
		//random team
		int random = Rnd.get(0, 1);
		int currentPlayerNumber = 0;
		/*
		 * for each registered (player-)class try to find a player for each team.
		 * if you can't find a player with the same class for all teams remove the class from the list
		 * if registeredClasses is empty => there are no more players with the same class for all teams
		 * -> fill each team with random players till the list is full
		 */
		if (teamType == 1) {
			while (currentPlayerNumber < maxPlayers) {
				if (registeredClasses == null || elyos == null || elyos.isEmpty() || asmodians == null || asmodians.isEmpty()) {
					notifyFail();
					onStop();
					return;
				} else if (registeredClasses.isEmpty()) {
					Player ely1 = findPlayer(elyos, 0);
					elyos.remove(ely1.getObjectId());
					Player asmo1 = findPlayer(asmodians, 0);
					asmodians.remove(asmo1.getObjectId());
					Player ely2 = findPlayer(elyos, 0);
					elyos.remove(ely2.getObjectId());
					Player asmo2 = findPlayer(asmodians, 0);
					asmodians.remove(asmo2.getObjectId());
					
					teams.get(random == 0 ? team1 : team4).addMember(ely1);
					teams.get(random == 0 ? team3 : team2).addMember(ely2);
					teams.get(random == 0 ? team2 : team3).addMember(asmo1);
					teams.get(random == 0 ? team4 : team1).addMember(asmo2);
					currentPlayerNumber++;
				} else {
					Iterator<PlayerClass> iter = registeredClasses.iterator();
					while (iter.hasNext()) {
						PlayerClass pClass = iter.next();
						Player ely1 = null;
						Player ely2 = null;
						Player asmo1 = null;
						Player asmo2 = null;
						Iterator<Integer> eIter = elyos.iterator();
						while (eIter.hasNext()) {
							Player p = World.getInstance().findPlayer(eIter.next());
							if (p != null) {
								if (p.getPlayerClass() == pClass) {
									if (ely1 == null) {
										ely1 = p;
									} else if (ely2 == null){
										ely2 = p;
										break;
									} else {
										break;
									}
								}
							} else {
								eIter.remove();
							}
						}
				
						if (ely1 != null && ely2 != null) {
							Iterator<Integer> aIter = asmodians.iterator();
							while (aIter.hasNext()) {
								Player p = World.getInstance().findPlayer(aIter.next());
								if (p != null) {
									if (asmo1 == null) {
										asmo1 = p;
									} else if (asmo2 == null) {
										asmo2 = p;
										break;
									} else {
										break;
									}
								} else {
									aIter.remove();
								}
							}
						}
				
						if (ely1 != null && ely2 != null && asmo1 != null && asmo2 != null) {
							if (currentPlayerNumber < maxPlayers) {
								elyos.remove(ely1.getObjectId());
								elyos.remove(ely2.getObjectId());
								teams.get(random == 0 ? team1 : team4).addMember(ely1);
								teams.get(random == 0 ? team3 : team2).addMember(ely2);
								asmodians.remove(asmo1.getObjectId());
								asmodians.remove(asmo2.getObjectId());
								teams.get(random == 0 ? team2 : team3).addMember(asmo1);
								teams.get(random == 0 ? team4 : team1).addMember(asmo2);
								currentPlayerNumber++;
							} else {
								break;
							}
						} else {
							iter.remove();
						}
					}
				}
			}
		} else {
			while (currentPlayerNumber < maxPlayers) {
				if (registeredClasses == null) {
					notifyFail();
					onStop();
					return;
				} else if (registeredClasses.isEmpty()) {
					Player ely = findPlayer(elyos, 0);
					Player asmo = findPlayer(asmodians, 0);
					if (ely == null || asmo == null) {
						notifyFail();
						onStop();
						return;
					}
					elyos.remove(ely.getObjectId());
					asmodians.remove(asmo.getObjectId());
					teams.get(random == 0 ? team1 : team2).addMember(ely);
					teams.get(random == 0 ? team2 : team1).addMember(asmo);
					currentPlayerNumber++;
				} else {
					Iterator<PlayerClass> iter = registeredClasses.iterator();
					while (iter.hasNext()) {
						PlayerClass pClass = iter.next();
						Player ely = null;
						Player asmo = null;
						Iterator<Integer> eIter = elyos.iterator();
						while (eIter.hasNext()) {
							Player p = World.getInstance().findPlayer(eIter.next());
							if (p != null) {
								if (p.getPlayerClass() == pClass) {
									ely = p;
									break;
								}
							} else {
								eIter.remove();
							}
						}
				
						if (ely != null) {
							Iterator<Integer> aIter = asmodians.iterator();
							while (aIter.hasNext()) {
								Player p = World.getInstance().findPlayer(aIter.next());
								if (p != null) {
									if (p.getPlayerClass() == pClass) {
										asmo = p;
										break;
									}
								} else {
									aIter.remove();
								}
							}
						}
				
						if (ely != null && asmo != null) {
							if (currentPlayerNumber < maxPlayers) {
								elyos.remove(ely.getObjectId());
								teams.get(random == 0 ? team1 : team2).addMember(ely);
								asmodians.remove(asmo.getObjectId());
								teams.get(random == 0 ? team2 : team1).addMember(asmo);
								currentPlayerNumber++;
							} else {
								break;
							}
						} else {
							iter.remove();
						}
					}
				}
			}
		}
	}
	
	private Player findPlayer(List<Integer> players, int objectId) {
		Player player = null;
		if (objectId == 0 && players != null && !players.isEmpty()) {
			Iterator<Integer> iter = players.iterator();
			while (iter.hasNext()) {
				Player foundPlayer = World.getInstance().findPlayer(iter.next());
				if (foundPlayer != null) {
					 player = foundPlayer;
					 break;
				 } else {
					 iter.remove();
				 }
			}
		} else if (objectId > 0) {
			player = World.getInstance().findPlayer(objectId);
		}
		return player;
	}
	
	public void notifyFail() {
		for (Integer id : elyos) {
			Player p = World.getInstance().findPlayer(id);
			if (p != null) {
				PacketSendUtility.sendMessage(p, "Ahserions Flight could not start because some requirements were not met.", ChatType.WHITE_CENTER);
			}
		}
		for (Integer id : asmodians) {
			Player p = World.getInstance().findPlayer(id);
			if (p != null) {
				PacketSendUtility.sendMessage(p, "Ahserions Flight could not start because some requirements were not met.", ChatType.WHITE_CENTER);
			}
		}
		if (teams != null && !teams.isEmpty()) {
			for (PanesterraTeam team : teams.values()) {
				if (team != null && team.getMembers() != null && !team.getMembers().isEmpty()) {
					for (Integer id : team.getMembers()) {
						Player p = World.getInstance().findPlayer(id);
						if (p != null) {
							PacketSendUtility.sendMessage(p, "Ahserions Flight could not start because some requirements were not met.", ChatType.WHITE_CENTER);
						}
					}
				}
			}
		}
	}
	
	public Map<PanesterraTeamId, PanesterraTeam> getRegisteredTeams() {
		return teams;
	}
}
