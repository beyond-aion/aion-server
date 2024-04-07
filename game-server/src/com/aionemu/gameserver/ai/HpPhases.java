package com.aionemu.gameserver.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class HpPhases {

	private final List<Integer> phaseHpPercents = new ArrayList<>();
	private int currentPhase = 0;

	public HpPhases(int hpPercent, int... moreHpPercents) {
		IntStream.concat(IntStream.of(hpPercent), IntStream.of(moreHpPercents)).distinct().forEach(phaseHpPercents::add);
		phaseHpPercents.sort(Comparator.reverseOrder()); // sort percents in descending order
	}

	public final void reset() {
		synchronized (phaseHpPercents) {
			currentPhase = 0;
		}
	}

	public <T extends NpcAI & PhaseHandler> void tryEnterNextPhase(T ai) {
		if (!ai.getOwner().isSpawned() || ai.isDead() || ai.isInState(AIState.RETURNING))
			return;
		synchronized (phaseHpPercents) {
			if (currentPhase >= phaseHpPercents.size())
				return;
			int phaseHpPercent = phaseHpPercents.get(currentPhase);
			if (phaseHpPercent >= ai.getLifeStats().getHpPercentage()) {
				currentPhase++;
				ai.handleHpPhase(phaseHpPercent);
			}
		}
	}

	public int getCurrentPhase() {
		return currentPhase;
	}

	public interface PhaseHandler {
		void handleHpPhase(int phaseHpPercent);
	}
}
