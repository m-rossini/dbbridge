/**
 * 
 */
package br.com.auster.repo.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.log4j.Logger;

import br.com.auster.repo.RulesRepository;
import br.com.auster.repo.parser.DrlFileParser;
import br.com.auster.repo.parser.ParserFactory;
import br.com.auster.repo.parser.RulePackage;
import br.com.auster.repo.parser.complete.Expander;

/**
 * @author pvieira
 *
 */
public class RepositoryTools {

	private static final Logger log = Logger.getLogger(RepositoryTools.class);        

	private static DrlFileParser parser = ParserFactory.createSimpleParser();

	/**
	 * Imports rules from a DRL file to the given repository
	 * @param drlFile DRL file
	 * @param repository <code>RulesRepository</code> instance
	 * @throws IOException 
	 */
	public static void importDrlToRepository(File drlFile, RulesRepository repository) throws IOException {

		log.info("Importing " + drlFile.getName() + " to repository.");
		RulePackage pkg = parser.parse(drlFile);

		String packageName = pkg.getName();
		String drlName = drlFile.getName().substring(0, drlFile.getName().toLowerCase().lastIndexOf(".drl"));

		Expander dsl = pkg.getExpander();
		if (dsl != null) {
			repository.createOrUpdateRule(packageName, drlName, pkg.getContent(), dsl.getName(), dsl.getContent());
		} else {
			repository.createOrUpdateRule(packageName, drlName, pkg.getContent());
		}

	}

	/**
	 * Imports DRL files in a folder to the given repository
	 * @param dir DRL files folder
	 * @param repository <code>RulesRepository</code> instance
	 * @throws IOException 
	 */
	public static void importDrlFolderToRepository(File dir, RulesRepository repository) throws IOException {

		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(dir.getName() + " must be a directory.");
		}

		File[] drlFiles = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String fileName) {
				return fileName.toLowerCase().endsWith(".drl");
			}
		});

		for (int i = 0; i < drlFiles.length; i++) {
			File drlFile = drlFiles[i];
			if (!drlFile.isDirectory()) {
				importDrlToRepository(drlFile, repository);
			}
		}
	}
}
