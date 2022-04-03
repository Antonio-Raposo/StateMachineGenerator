package org.cfmsoft.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class StateMachineGenerator {

	public static void main(String[] args) throws Exception {
		String yamlFilename = null;
		String resultDirectory = ".";
		if (args.length == 1) {
			yamlFilename = args[0];
		} else if (args.length == 2) {
			yamlFilename = args[0];
			resultDirectory = args[1];
		} else {
			System.out.println("Usage:");
			System.out.println("  StateMachineGenerator <yaml filename>");
			System.out.println("or:");
			System.out.println("  StateMachineGenerator <yaml filename> <result directory>");
		}
		if (yamlFilename != null) {
			final ObjectMapper objMapper = new YAMLMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			final JsonFactory jsonFactory = new YAMLFactory().setCodec(objMapper);
			JsonParser parser = jsonFactory.createParser(new FileReader(yamlFilename));
			StateMachine stateMachine = parser.readValueAs(StateMachine.class);
			System.out.println("Input parse successful.");
			checkCreateFile(resultDirectory, "StateMachine.h", stateMachine.toHSource());
			checkCreateFile(resultDirectory, "StateMachine.cpp", stateMachine.toCppSource());
			System.out.println("Done.");
		}
	}

	/**
	 * Check if the file exists and if the contents are the same before writing.
	 * 
	 * @param resultDirectory the directory for the result file
	 * @param fileName        the file name
	 * @param contents        the file contents
	 * @throws FileNotFoundException If the given file object does not denote an
	 *                               existing, writable regular file and a new
	 *                               regular file of that name cannot be created, or
	 *                               if some other error occurs while opening or
	 *                               creating the file
	 * @throws IOException           If an I/O error occurs
	 * @throws SecurityException     If a security manager is present and
	 *                               {@link SecurityManager#checkWrite
	 *                               checkWrite(file.getPath())} denies write access
	 *                               to the file
	 */
	private static void checkCreateFile(final String resultDirectory, final String fileName,
			final List<String> contents) throws FileNotFoundException, IOException {
		final File file = new File(resultDirectory, fileName);
		if (file.exists()) {
			try (BufferedReader in = new BufferedReader(new FileReader(file))) {
				final List<String> actual = in.lines().toList();
				if (contents.equals(actual)) {
					System.out.println(String.format("file %s not changed.", fileName));
					return;
				}
			}
		}
		try (PrintStream out = new PrintStream(file)) {
			contents.forEach(out::println);
		}
	}

}
