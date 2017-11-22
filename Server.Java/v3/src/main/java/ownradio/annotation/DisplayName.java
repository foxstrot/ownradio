package ownradio.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Аннотация предназначена для хранения метаинформации по полю класса
 * key -  хранит ключ строкового значения который в зависимости от выборной лакали указывает на строку с нужным переводом
 * isVisible – хранит значение видимости поля
 *
 * @author Alpenov Tanat
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface DisplayName {

	String key() default "";

	boolean isVisible() default true;
}