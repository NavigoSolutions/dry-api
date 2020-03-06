package com.navigo3.dryapi.test;

import java.util.Arrays;

import org.immutables.value.Value;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.util.JacksonUtils;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;
import com.navigo3.dryapi.sample.defs.form.ImmutablePerson;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint.TopAddressInput;
import com.navigo3.dryapi.test.helpers.RemoteCallsEnvironment;

public class Sandbox {

	private static RemoteCallsEnvironment env = new RemoteCallsEnvironment();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		env.start();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		env.stop();
	}

	@Test
	@Ignore
	public void test() {
		Person input = ImmutablePerson
			.builder()
			.name("James")
			.surname("Hook")
			.age(100)
			.secretNumber(42)
			.putColorsToFavoriteNumbers("red", Arrays.asList(1, 5, 7))
			.putColorsToFavoriteNumbers("blue", Arrays.asList(42))
			.putColorsToFavoriteNumbers("green", Arrays.asList(2, 10, 15, 78))
			.build();
		
		/*IdResult output = */env.getApi().executeBlocking(new FormUpsertEndpoint(), input);
//		JsonUtils.prettyPrint(output);
	}
	
	@Test
	@Ignore
	public void test2() {
		/*IntegerResult output2 = */env.getApi().executeBlocking(new SolveEverythingEndpoint(), TopAddressInput.createSampleData());
	
//		JsonUtils.prettyPrint(output2);
	}
	
	@Test
	@Ignore
	public void test3() throws Exception {
		try {
			ObjectMapper mapper = JacksonUtils.createXmlMapper();
			
			String xml = mapper.writeValueAsString(TopAddressInput.createSampleData());
					
			System.out.println(xml);
			
			JacksonUtils.prettyPrint(mapper.readValue(xml, new TypeReference<TopAddressInput>(){}));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Value.Immutable
	@JsonSerialize(as = ImmutablePlain.class)
	@JsonDeserialize(as = ImmutablePlain.class)
	public interface Plain {
		int getTest();
		String getTest2();
	}
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableFilterSubSub.class)
	@JsonDeserialize(as = ImmutableFilterSubSub.class)
	public interface FilterSubSub<U> {
		int getTest();
		String getTest2();
		U getVal();
	}
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableFilterSub.class)
	@JsonDeserialize(as = ImmutableFilterSub.class)
	public interface FilterSub<T> {
		int getTest();
		String getTest2();
		T getVal();
		FilterSubSub<Boolean> getA();
		FilterSubSub<Plain> getB();
	}
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableFilter.class)
	@JsonDeserialize(as = ImmutableFilter.class)
	public interface Filter {
		int getTest();
		String getTest2();
		FilterSub<Boolean> getOne();
		FilterSub<Plain> getTwo();
	}
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableFilterAndDefault.class)
	@JsonDeserialize(as = ImmutableFilterAndDefault.class)
	public interface FilterAndDefault<T> {
		T getDefaultFilter();
		T getFilter();
		int getFix();
	}
	
//	@Ignore
	@Test
	public void test4() {
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		TypeSchema type = TypeSchema.build(new TypeReference<FilterAndDefault<Filter>>() {});
		type.debugPrint();
	}
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableArr.class)
	@JsonDeserialize(as = ImmutableArr.class)
	public interface Arr<T> {
		String[] getFix();
	}
	
//	@Ignore
	@Test
	public void test5() {
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		TypeSchema type = TypeSchema.build(new TypeReference<Arr>() {});
		type.debugPrint();
	}
}
