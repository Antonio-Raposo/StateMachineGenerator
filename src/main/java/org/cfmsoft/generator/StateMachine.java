package org.cfmsoft.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Describes the state machine. A state machine is a list of states, each one
 * defining it's transitions to other states.
 *
 * @param states the list of state definitions, if null is replaced by a new
 *               ArrayList and can be filled later
 * @param start  the initial state
 */
public record StateMachine(List<State> states, String start) implements TreeObject {

	public StateMachine {
		states = Objects.requireNonNullElseGet(states, ArrayList::new);
		Objects.requireNonNull(start, "start cannot be null");
	}

	/**
	 * Generate the H source for this state machine.
	 * 
	 * @return the list of source lines
	 */
	public List<String> toHSource() {
		final List<String> result = new ArrayList<>();
		result.add("#ifndef StateMachine_h");
		result.add("#define StateMachine_h");
		result.add("");
		result.add("// definition of state values");
		result.add("enum State {");
		result.add("\t" + states().stream().map(State::name).collect(Collectors.joining(", ")));
		result.add("};");
		result.add("");
		result.add("extern void stateRun();");
		result.add("extern void stateTransition();");
		result.add("");
		result.add("#endif");
		return result;
	}

	/**
	 * Generate the CPP source for this state machine.
	 * 
	 * @return the list of source lines
	 */
	public List<String> toCppSource() {
		final List<String> result = new ArrayList<>();
		// add standard headers
		result.add("#include \"Arduino.h\"");
		result.add("#include \"StateMachine.h\"");
		result.add("");
		// add run function definitions
		states().stream().map(State::run).distinct()
				.forEach(runner -> result.add(String.format("extern void %s();", runner)));
		result.add("");
		// add predicate function definitions
		states().stream().flatMap(state -> state.transitions().stream()).map(Transition::predicate)
				.filter(predicate -> !predicate.isEmpty())
				.map(predicate -> predicate.startsWith("!") ? predicate.substring(1) : predicate).distinct()
				.forEach(runner -> result.add(String.format("extern bool %s();", runner)));
		result.add("");
		// current state global
		result.add("// the current state");
		result.add(String.format("static enum State state = %s;", start()));
		result.add("");
		// generate the switch for the runs
		result.add("void stateRun() {");
		result.add("\tswitch (state) {");
		states().forEach(state -> {
			result.add(String.format("\tcase %s:", state.name()));
			result.add(String.format("\t\t%s();", state.run()));
			result.add("\t\tbreak;");
		});
		result.add("\t}");
		result.add("}");
		result.add("");
		// generate the switch for the transitions
		result.add("void stateTransition() {");
		result.add("\tenum State nextState = state;");
		result.add("\tswitch (state) {");
		states().forEach(state -> {
			result.add(String.format("\tcase %s:", state.name()));
			if (state.transitions().size() == 1 && state.transitions().get(0).predicate().isEmpty()) {
				result.add(String.format("\t\tnextState = %s;", state.transitions().get(0).state()));
			} else {
				boolean flag = false;
				for (Transition transition : state.transitions()) {
					if (transition.predicate().isBlank()) {
						result.add("\t\t} else {");
					} else {
						result.add(String.format("\t\t%sif (%s()) {", (flag ? "} else " : ""), transition.predicate()));
					}
					result.add(String.format("\t\t\tnextState = %s;", transition.state()));
					flag = true;
				}
				result.add("\t\t}");
			}
			result.add("\t\tbreak;");
		});
		result.add("\t}");
		result.add("\tif (nextState != state) {");
		result.add("\t\tstate = nextState;");
		result.add("\t}");
		result.add("}");
		return result;
	}

}
