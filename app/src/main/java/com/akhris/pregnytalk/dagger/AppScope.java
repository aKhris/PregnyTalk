package com.akhris.pregnytalk.dagger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by anatoly on 11.03.18.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface AppScope {
}
