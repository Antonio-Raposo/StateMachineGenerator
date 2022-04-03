package org.cfmsoft.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Describes a state in the state machine. A state defines a run function which
 * is evaluated every time stateRun() is called and a list of transitions. The
 * transitions are examined in order until one returns true. When that happens
 * the next state will be the state of the transition, otherwise the state
 * remains the same. Mind that when the state remains the same the run function
 * is evaluated again.
 *
 * @param name        the name of the state, should be an upper case string for
 *                    convenience reading
 * @param run         the name of the function computing the output for this
 *                    state (hint for readability: the same as the name but
 *                    lower case)
 * @param transitions the list of transition definitions for this state, if null
 *                    is replaced by a new ArrayList and can be filled later
 */
public record State(String name, String run, List<Transition> transitions) implements TreeObject {

	public State {
		Objects.requireNonNull(name, "name cannot be null");
		Objects.requireNonNull(run, "run cannot be null");
		transitions = Objects.requireNonNullElseGet(transitions, ArrayList::new);
	}

}
