package com.navigo3.dryapi.test.helpers;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint.TopAddressInput;

public class TypeSchemaTest {
	private enum Abc {
		A,
		B,
		C
	}
	
	private interface Plain {
		int getInteger();
		String getString();
		BigDecimal getBigDecimal();
		boolean getBoolean();
		Abc getAbc();
	}
	
	private interface Colls {
		Plain[] getPlainArray();
		String[] getStringArray();
		List<String> getStringList();
		Map<String, BigDecimal> getStringMap();
		Optional<String> getStringOptional();
	}
	
	private interface Recursive {
		List<Recursive> getChildren();
	}
	
	private interface NestedColls {
		Optional<Map<Integer, List<Boolean>>> getNested();
	}
	
	private interface Typed1<T> {
		T getValue();
	}
	
	private interface Typed2<U, W> {
		U getValue1();
		W getValue2();
	}
	
	private interface ComplexTyped<T, U, V> {
		Optional<List<Map<T, U>>> getNested();
		Typed2<Typed1<T>, U> getTwoLevel();
		Typed2<Typed2<Typed1<T>, U>, Typed2<Typed1<V>, U>> getThreeLevel();
	}

	private ByteArrayOutputStream os;
	private PrintStream ps;
	
	@Before
	public void beforeTest() {
		os = new ByteArrayOutputStream();
		ps = new PrintStream(os);
    }
  
	@After
	public void afterTest() throws IOException {
		ps.close();
		os.close();
    }
	
	@Test
	public void testPlainStructure() throws IOException {
		TypeSchema type = TypeSchema.build(new TypeReference<Plain>() {});
		type.debugPrint(ps);
		
		compareWithStored("/TypeSchema/plain.txt");
	}
	
	@Test
	public void testPlainColls() throws IOException {
		TypeSchema type = TypeSchema.build(new TypeReference<Colls>() {});
		type.debugPrint(ps);
		
		compareWithStored("/TypeSchema/colls.txt");
	}
	
	@Test
	public void testRecursive() throws IOException {
		TypeSchema type = TypeSchema.build(new TypeReference<Recursive>() {});
		type.debugPrint(ps);
		
		compareWithStored("/TypeSchema/recursive.txt");
	}
	
	@Test
	public void testNestedColls() throws IOException {
		TypeSchema type = TypeSchema.build(new TypeReference<NestedColls>() {});
		type.debugPrint(ps);
		
		compareWithStored("/TypeSchema/nestedColls.txt");
	}
	
	@Test
	public void testComplexData() throws IOException {
		TypeSchema type = TypeSchema.build(new TypeReference<TopAddressInput>() {});
		type.debugPrint(ps);
		
		compareWithStored("/TypeSchema/complexData.txt");
	}
	
	@Test
	public void testSimpleTyped() throws IOException {
		TypeSchema type = TypeSchema.build(new TypeReference<Typed2<Typed1<String>, Typed1<Integer>>>() {});
		type.debugPrint(ps);
		
		compareWithStored("/TypeSchema/simpleTyped.txt");
	}
	
	@Test
	public void testComplextTyped() throws IOException {
		TypeSchema type = TypeSchema.build(new TypeReference<ComplexTyped<String, Typed1<Integer>, Boolean>>() {});
		type.debugPrint(ps);
		
		compareWithStored("/TypeSchema/complexTyped.txt");
	}

	private void compareWithStored(String resourcePath) throws IOException {
		String correct = IOUtils.toString(getClass().getResourceAsStream(resourcePath), StandardCharsets.UTF_8).trim();
		String current = os.toString(StandardCharsets.UTF_8.name()).trim();
		
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println(current);
		
		assertEquals(correct, current);
	}
}
