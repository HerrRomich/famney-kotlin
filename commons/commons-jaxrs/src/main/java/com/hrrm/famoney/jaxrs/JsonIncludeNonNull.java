package com.hrrm.famoney.jaxrs;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.immutables.annotate.InjectAnnotation;
import org.immutables.annotate.InjectAnnotation.Where;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@InjectAnnotation(
        target = { Where.FIELD },
        type = JsonInclude.class,
        code = "(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)"
)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonIncludeNonNull {
}
