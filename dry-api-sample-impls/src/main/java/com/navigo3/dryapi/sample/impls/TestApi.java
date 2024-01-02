package com.navigo3.dryapi.sample.impls;

import com.navigo3.dryapi.core.def.DryApi;
import com.navigo3.dryapi.sample.defs.download.DownloadPersonEndpoint;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint;
import com.navigo3.dryapi.sample.defs.generics.GetGenericDataEndpoint;
import com.navigo3.dryapi.sample.defs.list.ListPersonsEndpoint;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint;
import com.navigo3.dryapi.sample.defs.math.integer.NegateIntegersEndpoint;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint;
import com.navigo3.dryapi.sample.impls.download.DownloadPersonImpl;
import com.navigo3.dryapi.sample.impls.form.FormUpsertImpl;
import com.navigo3.dryapi.sample.impls.generics.GetGenericDataImpl;
import com.navigo3.dryapi.sample.impls.list.ListPersonsImpl;
import com.navigo3.dryapi.sample.impls.math.integer.AddIntegersImpl;
import com.navigo3.dryapi.sample.impls.math.integer.NegateIntegersImpl;
import com.navigo3.dryapi.sample.impls.philosophy.SolveEverythingImpl;

public class TestApi {
	public static DryApi<TestAppContext, TestCallContext, TestValidator> build() {
		DryApi<TestAppContext, TestCallContext, TestValidator> res = new DryApi<>();

		res.register(new AddIntegersEndpoint(), AddIntegersImpl.class);
		res.register(new NegateIntegersEndpoint(), NegateIntegersImpl.class);
		res.register(new SolveEverythingEndpoint(), SolveEverythingImpl.class);
		res.register(new FormUpsertEndpoint(), FormUpsertImpl.class);
		res.register(new ListPersonsEndpoint(), ListPersonsImpl.class);
		res.register(new DownloadPersonEndpoint(), DownloadPersonImpl.class);
		
		res.register(new GetGenericDataEndpoint(), GetGenericDataImpl.class);

		return res;
	}
}