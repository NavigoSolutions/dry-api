package com.navigo3.dryapi.sample.defs.philosophy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint.TopAddressInput;

public class SolveEverythingEndpoint extends MethodDefinition<TopAddressInput, IntegerResult> {
	@Value.Immutable
	@JsonSerialize(as = ImmutableLowAddressInput.class)
	@JsonDeserialize(as = ImmutableLowAddressInput.class)
	public interface LowAddressInput {
		Optional<String> getState();
		String getCity();
		String getStreet();
		int getNumber();
		Optional<String> getDoor();
	}
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableMiddleAddressInput.class)
	@JsonDeserialize(as = ImmutableMiddleAddressInput.class)
	public interface MiddleAddressInput {
		String getContinent();
		String getCountry();
		
		List<LowAddressInput> getLowAddresses();
		
		Map<String, Integer> getStringCodes();
		
		LocalDateTime getCurrentGalacticDateTime();
		
		Map<LocalDateTime, String> getCronicles();
		
		Optional<LowAddressInput> getMainAddress();
	}
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableTopAddressInput.class)
	@JsonDeserialize(as = ImmutableTopAddressInput.class)
	public interface TopAddressInput {
		String getGalaxy();
		String getCorner();
		String getPlanet();
		
		MiddleAddressInput getMiddleAddress();
		
		public static TopAddressInput createSampleData() {
			ImmutableLowAddressInput.Builder lowAddressBuilder = ImmutableLowAddressInput
				.builder()
				.city("Brno")
				.number(1245)
				.street("Charvatska");
			
			ImmutableMiddleAddressInput.Builder middleAddressBuilder = ImmutableMiddleAddressInput
				.builder()
				.continent("Europe")
				.country("Czech republic")
				.currentGalacticDateTime(LocalDateTime.now())
				.putCronicles(LocalDateTime.now().minusYears(1), "year ago")
				.putCronicles(LocalDateTime.now().minusYears(2), "two years ago")
				.putStringCodes("xxx", 123)
				.putStringCodes("yyy", 745)
				.mainAddress(lowAddressBuilder.build())
				.addLowAddresses(lowAddressBuilder.build())
				.addLowAddresses(lowAddressBuilder.build());
			
			ImmutableTopAddressInput.Builder topAddressBuilder = ImmutableTopAddressInput
				.builder()
				.corner("left")
				.galaxy("Milky way")
				.planet("Earth")
				.middleAddress(middleAddressBuilder.build());
			
			return topAddressBuilder.build();
		}
	}

	@Override
	public TypeReference<TopAddressInput> getInputType() {
		return new TypeReference<TopAddressInput>() {};
	}

	@Override
	public TypeReference<IntegerResult> getOutputType() {
		return new TypeReference<IntegerResult>() {};
	}

	@Override
	public String getQualifiedName() {
		return "philosophy/solve-everything";
	}

	@Override
	public String getDescription() {
		return "Crazy method that process complex input and returns 'ultimate answer to life, the universe, and everything'";
	}
}
