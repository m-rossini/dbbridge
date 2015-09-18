/**
 * 
 */
package br.com.auster.test;

import java.io.File;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.Session;

import junit.framework.TestCase;
import br.com.auster.repo.RepositoryConfigurator;
import br.com.auster.repo.RulesRepository;

/**
 * @author pvieira
 *
 */
public abstract class BaseTest extends TestCase {

	private static ThreadLocal<RulesRepository> repo = new ThreadLocal<RulesRepository>();

	public static final String REPOSITORY_CONFIG_FILE = "repository.xml";
	public static final String REPOSITORY_HOME = "repotest";

	public static final String RESOURCES_PATH = "src/test/resources";
	public static final String REPO_CONFIG_FILE = "repotest.xml";

	public static final String fibonacciDrlFileName = "Fibonacci.drl";
	public static final String sampleDrlFileName = "Sample.drl";
	public static final String sampleDrlWithExpanderFileName = "SampleDSL.drl";
	public static final String sampleExpanderFileName = "SampleDSL.dsl";

	protected final String fibonacciDrlNode = "Fibonacci";
	protected final String sampleDrlNode = "Sample";
	protected final String sampleDrlWithExpanderNode = "SampleDSL";
	protected final String sampleExpanderNode = "SampleDSL";

	protected File resourcesDir;
	protected File fibonacciDrlFile;
	protected File sampleDrlFile;
	protected File sampleDrlWithExpanderFile;

	protected BaseTest() {
		resourcesDir = new File(RESOURCES_PATH);
		fibonacciDrlFile = new File(RESOURCES_PATH + File.separator + fibonacciDrlFileName);
		sampleDrlFile = new File(RESOURCES_PATH + File.separator + sampleDrlFileName);
		sampleDrlWithExpanderFile = new File(RESOURCES_PATH + File.separator + sampleDrlWithExpanderFileName);
	}

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {

		RulesRepository repoInstance = repo.get();
		
		if (repoInstance != null) {
			Session session = repoInstance.getSession();

			Node ruleNode = session.getRootNode().getNode(RulesRepository.RULES_REPOSITORY_NAME);
				
			Node drlArea = ruleNode.getNode(RulesRepository.DRL_AREA);
			
			NodeIterator drlNodes = drlArea.getNodes();		
			while (drlNodes.hasNext()) {
				Node drlNode = (Node) drlNodes.next();
				drlNode.remove();
			}

			Node dslArea = ruleNode.getNode(RulesRepository.DSL_AREA);
			NodeIterator dslNodes = dslArea.getNodes();
			while (dslNodes.hasNext()) {
				Node drlNode = (Node) dslNodes.next();
				drlNode.remove();
			}
		}

	}

	public static RulesRepository getRepository() {

		RulesRepository repoInstance = repo.get();
		if (repoInstance == null) {
			String configFile = RESOURCES_PATH + File.separator + REPOSITORY_CONFIG_FILE;
			RepositoryConfigurator config = new RepositoryConfigurator();

			//create a repo instance (startup)
			Repository repository = config.createRepository(configFile, REPOSITORY_HOME);

			//create a session
			Session session;
			try {
				session = config.login(repository);
				//clear out and setup
				config.clearRulesRepository(session);
				config.setupRulesRepository(session);
				String user = session.getUserID();
				String name = repository.getDescriptor(Repository.REP_NAME_DESC);

				System.out.println("Logged in as " + user + " to a " + name + " repository.");

				repoInstance = new RulesRepository(session);

				repo.set(repoInstance);                
			} catch ( Exception e) {
				fail("Unable to initialise repository :" + e.getMessage());
			}

		}

		return repoInstance;
	}
}
