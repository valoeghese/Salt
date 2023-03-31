package valoeghese.salt.io;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides the serialised name of a field where it differs from that field's name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerialisedName {
	/**
	 * Get the serialised name of the field.
	 * @return the name this field is given in serialised format.
	 */
	String value();
}
