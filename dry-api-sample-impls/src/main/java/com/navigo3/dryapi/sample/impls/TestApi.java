package com.navigo3.dryapi.sample.impls;

import com.navigo3.dryapi.core.def.DryApi;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint;
import com.navigo3.dryapi.sample.impls.form.FormUpsertImpl;
import com.navigo3.dryapi.sample.impls.math.integer.AddIntegersImpl;
import com.navigo3.dryapi.sample.impls.philosophy.SolveEverythingImpl;

public class TestApi {
	public static DryApi<TestAppContext, TestCallContext> build() {
		DryApi<TestAppContext, TestCallContext> res = new DryApi<>();
		
		res.register(AddIntegersEndpoint.class, AddIntegersImpl.class);
		res.register(SolveEverythingEndpoint.class, SolveEverythingImpl.class);
		res.register(FormUpsertEndpoint.class, FormUpsertImpl.class);
		
		return res;
	}
}