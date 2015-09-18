/**
 * 
 */
package br.com.auster.repo.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

import br.com.auster.repo.RulesRepository;
import br.com.auster.repo.util.RepositorySessionUtil;

/**
 * @author pvieira
 * 
 */
public class ImportRules2Repository {

	private static final Logger log = Logger.getLogger(RepositoryTools.class);

	public static final String CMDLINE_OPTS_RULEFILE_NAME = "rulefile-name";
	public static final String CMDLINE_OPTS_RULEFILE_MNEMONIC = "r";

	public static final String CMDLINE_OPTS_REPOSITORYCONFIG_PATH = "repository-config";
	public static final String CMDLINE_OPTS_REPOSITORYCONFIG_PATH_MNEMONIC = "c";

	public static final String CMDLINE_OPTS_REPOSITORYHOME_PATH = "repository-home";
	public static final String CMDLINE_OPTS_REPOSITORYHOME_PATH_MNEMONIC = "h";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String fileName = null;
		String repositoryConfig = null;
		String repositoryHome = null;

		try {

			CommandLineParser cmdParser = new PosixParser();

			CommandLine line = cmdParser.parse(createOptions(), args);

			fileName = line.getOptionValue(CMDLINE_OPTS_RULEFILE_MNEMONIC);
			repositoryConfig = line.getOptionValue(CMDLINE_OPTS_REPOSITORYCONFIG_PATH_MNEMONIC);
			repositoryHome = line.getOptionValue(CMDLINE_OPTS_REPOSITORYHOME_PATH_MNEMONIC);

			RulesRepository repository = RepositorySessionUtil.getRepository(
					repositoryConfig, repositoryHome);

			File drl = new File(fileName);

			log.info("Start importing DRL files to repository...");
			System.out.println("Start importing DRL files to repository...");
			if (drl.isDirectory()) {
				RepositoryTools.importDrlFolderToRepository(drl, repository);
			} else {
				RepositoryTools.importDrlToRepository(drl, repository);
			}
			log.info("DRL files importing finished.");
			System.out.println("DRL files importing finished.");

		} catch (FileNotFoundException e) {
			System.err.println("File " + fileName + " not found.");
			System.exit(1);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("<import-run>", createOptions(), true);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Error importing file: " + fileName);
			System.exit(1);
		}
	}

	protected static Options createOptions() {

		OptionBuilder.withArgName(CMDLINE_OPTS_RULEFILE_NAME);
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(true);
		OptionBuilder.withDescription("DRL file or DRL files directory path to import");
		Option drl = OptionBuilder.create(CMDLINE_OPTS_RULEFILE_MNEMONIC);

		OptionBuilder.withArgName(CMDLINE_OPTS_REPOSITORYHOME_PATH);
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(true);
		OptionBuilder.withDescription("Repository home path");
		Option home = OptionBuilder.create(CMDLINE_OPTS_REPOSITORYHOME_PATH_MNEMONIC);

		OptionBuilder.withArgName(CMDLINE_OPTS_REPOSITORYCONFIG_PATH);
		OptionBuilder.hasArg(true);
		OptionBuilder.isRequired(true);
		OptionBuilder.withDescription("Repository config file path");
		Option config = OptionBuilder.create(CMDLINE_OPTS_REPOSITORYCONFIG_PATH_MNEMONIC);

		Options options = new Options();
		options.addOption(drl);
		options.addOption(home);
		options.addOption(config);

		return options;
	}
}
