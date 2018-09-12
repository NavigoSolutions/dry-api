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
}
