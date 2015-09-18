package br.com.auster.repo.util;

import javax.jcr.Repository;
import javax.jcr.Session;

import br.com.auster.repo.RepositoryConfigurator;
import br.com.auster.repo.RulesRepository;
import br.com.auster.repo.RulesRepositoryException;

/**
 * This is a utility to simulate session behavior for the test suite.
 * @author 
 *
 */
public class RepositorySessionUtil {

	private static ThreadLocal<RulesRepository> repo = new ThreadLocal<RulesRepository>();

	public static RulesRepository getRepository(String configFile) {

		return getRepository(configFile, null);        
	}

	public static RulesRepository getRepository(String configFile, String repositoryHome) {
		RulesRepository repoInstance = repo.get();
		if (repoInstance == null) {
			RepositoryConfigurator config = new RepositoryConfigurator();

			//create a repo instance (startup)
			Repository repository = null;
			if (repositoryHome == null) {
				repositoryHome = RulesRepository.REPOSITORY_HOME;
			}
			repository = config.createRepository(configFile, repositoryHome);				

			//create a session
			Session session;
			try {
				session = config.login(repository);
				//setup
//				config.clearRulesRepository(session);
				config.setupRulesRepository(session);

				repoInstance = new RulesRepository(session);

				repo.set(repoInstance);                
			} catch ( Exception e) {
				throw new RulesRepositoryException("Unable to initialise repository :" + e.getMessage());
			}
		}

		return (RulesRepository) repoInstance;        
	}
}
