package com.navigo3.dryapi.test.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.navigo3.dryapi.core.util.StringUtils;

public class StringUtilsTest {
	@Test
	public void underscoreToCamelCase() {
		assertEquals("testConversion", StringUtils.underscoreToCamelCase("test_conversion"));
		assertEquals("testConversion", StringUtils.underscoreToCamelCase("testConversion"));

		assertEquals("plannedAttendance_4", StringUtils.underscoreToCamelCase("PLANNED_ATTENDANCE_4"));
		assertEquals("plannedAttendance_5", StringUtils.underscoreToCamelCase("planned_attendance_5"));
		assertEquals("plannedAttendance_6", StringUtils.underscoreToCamelCase("planned_ATTENDANCE_6"));

		assertEquals("timeOfChange", StringUtils.underscoreToCamelCase("time_of_change"));
		assertEquals("timeOfChange", StringUtils.underscoreToCamelCase("TIME_OF_CHANGE"));

		assertEquals("firmId", StringUtils.underscoreToCamelCase("firm_id"));
	}

	@Test
	public void stripAccents() {
		assertEquals("Necht jiz hrisne saxofony dablu rozezvuci sin udesnymi tony waltzu, tanga a quickstepu.",
			StringUtils.stripAccents(
				"Nechť již hříšné saxofony ďáblů rozezvučí síň úděsnými tóny waltzu, tanga a quickstepu."));
		assertEquals("Prilis zlutoucky kun upel dabelske ody.",
			StringUtils.stripAccents("Příliš žluťoučký kůň úpěl ďábelské ódy."));
		assertEquals("Vypata dcera grofa Maxwella s IQ nizsim ako kon nuti celad hryzt hrbu jablk.",
			StringUtils.stripAccents("Vypätá dcéra grófa Maxwella s IQ nižším ako kôň núti čeľaď hrýzť hŕbu jabĺk."));
		assertEquals("Krdel stastnych datlov uci pri usti Vahu mlkveho kona obhryzat koru a zrat cerstve maso.",
			StringUtils.stripAccents(
				"Kŕdeľ šťastných ďatľov učí pri ústí Váhu mĺkveho koňa obhrýzať kôru a žrať čerstvé mäso."));
		assertEquals("Stroz pchnal kosc w quiz gedzb vel fax myjn.",
			StringUtils.stripAccents("Stróż pchnął kość w quiz gędźb vel fax myjń."));
	}
}
