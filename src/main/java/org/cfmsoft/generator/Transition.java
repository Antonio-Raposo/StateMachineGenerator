package org.cfmsoft.generator;

import java.util.Objects;

/**
 * Describes a transition in the state. When a transition is evaluated the
 * predicate is evaluated and if the result is true the next state will the
 * transition's state. If the predicate is null ou empty it will evaluate always
 * to true.
 *
 * @param predicate the name of the predicate function returning bool, may be
 *                  prefixed with '!' for negation
 * @param state     the state to jump to if the predicate evaluates to true
 */
public record Transition(String predicate, String state) implements TreeObject {

	public Transition {
		predicate = Objects.requireNonNullElse(predicate, "");
		Objects.requireNonNull(state, "state cannot be null");
	}

}
