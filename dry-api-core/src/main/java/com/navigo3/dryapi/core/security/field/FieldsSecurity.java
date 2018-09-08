package com.navigo3.dryapi.core.security.field;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;

@Value.Immutable
@JsonSerialize(as = ImmutableFieldsSecurity.class)
@JsonDeserialize(as = ImmutableFieldsSecurity.class)
public interface FieldsSecurity<TAppContext extends AppContext, TCallContext extends CallContext> {

}
