package com.navigo3.dryapi.core.def;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Simple interface that mimicks com.fasterxml.jackson.core.type.TypeReference<T> to avoid 
 */
public abstract class IOTypeReference<T> implements Comparable<IOTypeReference<T>>
{
    protected final Type _type;
    
    protected IOTypeReference()
    {
        Type superClass = getClass().getGenericSuperclass();
        
        if (superClass instanceof Class<?>) { 
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        }

        _type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() { 
    	return _type; 
    }

    @Override
    public int compareTo(IOTypeReference<T> o) { 
    	return 0; 
    }
}

