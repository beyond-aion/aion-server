package com.aionemu.gameserver.services.weather;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.dataholders.MapWeatherData;
import com.aionemu.gameserver.model.templates.world.WeatherTable;

import jakarta.xml.bind.JAXBContext;

class WeatherZoneTest {

	@Test
	void roundsDurationsToTenMinutes() {
		assertEquals(0, WeatherZone.roundToTen(0));
		assertEquals(0, WeatherZone.roundToTen(4));
		assertEquals(10, WeatherZone.roundToTen(5));
		assertEquals(10, WeatherZone.roundToTen(14));
		assertEquals(20, WeatherZone.roundToTen(15));
		assertEquals(2050, WeatherZone.roundToTen(2048));
	}

	/**
	 * Rain may start only at 13:00: a 10 minute sign, 40 minutes of rain, a 10 minute remain, then clear sky.
	 */
	@Test
	void playsSignBodyRemainInsideTheHourWindow() throws Exception {
		WeatherZone zone = new WeatherZone(load("""
			<weather_maps>
				<map id="1" zone_count="0">
					<weather code="1" name="Rain" zone="0" priority="1" hours="13" occur_prob="100" duration="40" state="RAIN">
						<sign name="RainBefore" probability="100" duration="10"/>
						<remain name="RainAfter" probability="100" duration="10"/>
					</weather>
					<weather code="2" name="RainBefore" zone="0" priority="1" occur_prob="0" duration="10"/>
					<weather code="3" name="RainAfter" zone="0" priority="1" occur_prob="0" duration="10" state="RAIN"/>
				</map>
			</weather_maps>
			""").getEntriesForZone(0));

		List<String> changes = new ArrayList<>();
		run(zone, 0, 3 * 1440, changes);

		assertEquals(List.of("780:2", "790:1", "830:3", "840:0", "2220:2", "2230:1", "2270:3", "2280:0", "3660:2", "3670:1", "3710:3", "3720:0"),
			changes);
	}

	@Test
	void playsSubWeatherAfterTheBody() throws Exception {
		WeatherZone zone = new WeatherZone(load("""
			<weather_maps>
				<map id="1" zone_count="0">
					<weather code="1" name="Storm" zone="0" priority="1" hours="0" occur_prob="100" duration="10">
						<sub name="StormTail" probability="100"/>
					</weather>
					<weather code="2" name="StormTail" zone="0" priority="1" occur_prob="0" duration="20"/>
				</map>
			</weather_maps>
			""").getEntriesForZone(0));

		List<String> changes = new ArrayList<>();
		run(zone, 0, 90, changes);

		assertEquals(List.of("0:1", "10:2", "30:0"), changes);
	}

	@Test
	void startsOnlyInTheMonthsOfTheRecord() throws Exception {
		WeatherZone zone = new WeatherZone(load("""
			<weather_maps>
				<map id="1" zone_count="0">
					<weather code="1" name="Rain" zone="0" priority="1" months="6" hours="0-23" occur_prob="100" duration="10" state="RAIN"/>
				</map>
			</weather_maps>
			""").getEntriesForZone(0));

		List<String> changes = new ArrayList<>();
		run(zone, 0, 12 * 44640, changes);

		assertFalse(changes.isEmpty());
		for (String change : changes) {
			int minute = Integer.parseInt(change.split(":")[0]);
			assertEquals(5, minute / 44640 % 12, change); // June
		}
	}

	@Test
	void neverStartsWithAnEmptyMonthList() throws Exception {
		WeatherZone zone = new WeatherZone(load("""
			<weather_maps>
				<map id="1" zone_count="0">
					<weather code="1" name="Rain" zone="0" priority="1" months="" hours="0-23" occur_prob="100" duration="10" state="RAIN"/>
				</map>
			</weather_maps>
			""").getEntriesForZone(0));

		List<String> changes = new ArrayList<>();
		run(zone, 0, 12 * 44640, changes);

		assertEquals(List.of(), changes);
	}

	@Test
	void neverStartsWithoutHours() throws Exception {
		WeatherZone zone = new WeatherZone(load("""
			<weather_maps>
				<map id="1" zone_count="0">
					<weather code="1" name="Rain" zone="0" priority="1" occur_prob="100" duration="10" state="RAIN"/>
				</map>
			</weather_maps>
			""").getEntriesForZone(0));

		List<String> changes = new ArrayList<>();
		run(zone, 0, 12 * 44640, changes);

		assertEquals(List.of(), changes);
	}

	/**
	 * Moving the clock back keeps the time the running weather has left.
	 */
	@Test
	void keepsTheRestOfARunningWeatherWhenTheClockGoesBack() throws Exception {
		WeatherZone zone = new WeatherZone(load("""
			<weather_maps>
				<map id="1" zone_count="0">
					<weather code="1" name="Rain" zone="0" priority="1" hours="5" occur_prob="100" duration="60" state="RAIN"/>
				</map>
			</weather_maps>
			""").getEntriesForZone(0));
		Random random = new Random(1);

		assertTrue(zone.roll(300, 0, 5, random));
		assertEquals(360, zone.getNextEventMinute());
		zone.shift(-120); // 05:20 -> 03:20, 40 minutes of rain left
		assertEquals(240, zone.getNextEventMinute());
		assertFalse(zone.advance(239, random));
		assertTrue(zone.advance(240, random));
		assertEquals(0, zone.getCode());
		assertEquals(-1, zone.getNextEventMinute());
	}

	private static WeatherTable load(String xml) throws Exception {
		return ((MapWeatherData) JAXBContext.newInstance(MapWeatherData.class).createUnmarshaller().unmarshal(new StringReader(xml))).getWeather(1);
	}

	private static void run(WeatherZone zone, int from, int to, List<String> changes) {
		Random random = new Random(1);
		for (int minute = from; minute <= to; minute++) {
			boolean changed = zone.advance(minute, random);
			if (minute % 60 == 0)
				changed |= zone.roll(minute, minute / 44640 % 12, minute / 60 % 24, random);
			if (changed)
				changes.add(minute + ":" + zone.getCode());
		}
	}
}
