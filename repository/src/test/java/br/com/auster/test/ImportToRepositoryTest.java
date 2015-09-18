/**
 * 
 */
package br.com.auster.test;

import java.io.IOException;

import br.com.auster.repo.RulesRepository;
import br.com.auster.repo.tools.RepositoryTools;

/**
 * @author pvieira
 *
 */
public class ImportToRepositoryTest extends BaseTest {

	public void testImportDrlFile() {
		RulesRepository rulesRepository = getRepository();

		try {
			RepositoryTools.importDrlToRepository(this.fibonacciDrlFile, rulesRepository);
		} catch (IOException e) {
			fail(e.toString());
		}
		
		assertNotNull("DRL file was not found in repository.", rulesRepository.loadRule(fibonacciDrlNode));
	}

	public void testImportDrlFilesDir() {
		RulesRepository rulesRepository = getRepository();

		try {
			RepositoryTools.importDrlFolderToRepository(this.resourcesDir, rulesRepository);
		} catch (IOException e) {
			fail(e.toString());
		}
		
		assertNotNull("DRL file was not found in repository.", rulesRepository.loadRule(fibonacciDrlNode));
	}
}
