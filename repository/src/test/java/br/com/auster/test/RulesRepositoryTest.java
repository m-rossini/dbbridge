package br.com.auster.test;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;

import br.com.auster.repo.ExpanderItem;
import br.com.auster.repo.RuleItem;
import br.com.auster.repo.RulesRepository;
import br.com.auster.repo.tools.RepositoryTools;
import br.com.auster.test.facts.Message;

public class RulesRepositoryTest extends BaseTest {

	public void testAddRule() throws Exception {
		RulesRepository repo = getRepository();

		RuleItem rule = repo.createRule("pkg", "myRule", "content");
		repo.save();
		assertEquals("myRule", rule.getName());
		
		RuleItem loadedRule = repo.loadRule("myRule");

		assertEquals(rule.getName(), loadedRule.getName());
		assertEquals(rule.getContent(), loadedRule.getContent());

	}
	
	public void testAddRuleWithExpander() throws Exception {
		RulesRepository repo = getRepository();

		RuleItem rule = repo.createRule("pkg", "myRule", "rule content", "myExpander", "expander content");
		ExpanderItem expander = rule.getExpander();
		repo.save();
		assertEquals("myRule", rule.getName());
		assertEquals("myExpander", expander.getName());
		
		RuleItem loadedRule = repo.loadRule("myRule");
		assertEquals(rule.getName(), loadedRule.getName());
		assertEquals(rule.getContent(), loadedRule.getContent());

		ExpanderItem loadedExpander = repo.loadExpander("myExpander");
		assertEquals(expander.getName(), loadedExpander.getName());
		assertEquals(expander.getContent(), loadedExpander.getContent());
	}

	public void testLoadRuleByUUID() throws Exception {

		RulesRepository repo = getRepository();

		RuleItem drl1 = repo.createRule("package", "ruleUUID", "content");

		repo.save();

		String uuid = drl1.getNode().getUUID();

		RuleItem loaded = repo.loadRuleByUUID(uuid);
		assertNotNull(loaded);
		assertEquals("ruleUUID", loaded.getName());
		assertEquals("content", loaded.getContent());

		// try loading rule that was not created
		try {
			repo.loadRuleByUUID("01010101-0101-0101-0101-010101010101");
			fail("Exception not thrown loading rule package that was not created.");
		} catch (Exception e) {
			// All right!
			assertNotNull(e.getMessage());
		}
	}

	public void testListRules() {
		RulesRepository rulesRepository = getRepository();

		rulesRepository.createRule("package", "ruleA", "content");
		rulesRepository.createRule("package", "ruleB", "content");

		List<RuleItem> list = rulesRepository.listRuleItems();
		assertFalse(list.isEmpty());

		int count = 0;
		Iterator it = list.iterator();
		while (it.hasNext()) {
			try {
				RuleItem element = (RuleItem) it.next();
				assertNotNull(element);
			} catch (Exception e) {
				fail(e.toString());
			}
			count++;
		}
		assertEquals("DRLs were not correctly created in repository.", 2, count);

	}

	public void testListExpanders() {
		RulesRepository rulesRepository = getRepository();

		rulesRepository.createRule("package", "ruleA", "content", "expanderA", "expander content");
		rulesRepository.createExpander("expanderB", "expanderB content");

		List<ExpanderItem> list = rulesRepository.listExpanderItems();
		assertFalse(list.isEmpty());

		int count = 0;
		Iterator it = list.iterator();
		while (it.hasNext()) {
			try {
				ExpanderItem element = (ExpanderItem) it.next();
				assertNotNull(element);
			} catch (Exception e) {
				fail(e.toString());
			}
			count++;
		}
		assertEquals("DSLs were not correctly created in repository.", 2, count);

	}

	public void testGetLastModifiedOnCheckin() throws Exception {
		RulesRepository rulesRepository = getRepository();
		RuleItem ruleItem1 = rulesRepository.createRule("pkg", "ruleLastModified", "content");

		Calendar cal = Calendar.getInstance();
		long before = cal.getTimeInMillis();

		Thread.sleep(100);
		ruleItem1.updateContent("new content");
		ruleItem1.checkin("new version");
		Calendar cal2 = ruleItem1.getLastModified();
		long lastMod = cal2.getTimeInMillis();

		cal = Calendar.getInstance();
		long after = cal.getTimeInMillis();

		assertTrue(before < lastMod);
		assertTrue(lastMod < after);

	}

	public void testCreateNewVersion() {
		RulesRepository rulesRepository = getRepository();

		RuleItem drl = rulesRepository.createRule("package", "ruleNewVersion", "content");
		assertNotNull("Rule was not created in repository.", drl);

		assertNull(drl.getPrecedingVersion());

		assertEquals(drl.getContent(), "content");

		drl.updateContent("new content").checkin("content updated");

		assertEquals(drl.getContent(), "new content");

		RuleItem precendingDrl = drl.getPrecedingVersion();

		assertNotNull(precendingDrl);
		
		assertEquals(precendingDrl.getContent(), "content");

	}

	public void testUpdateRule() {

		RulesRepository rulesRepository = getRepository();

		RuleItem rule = rulesRepository.createRule("package", "rule", "content");
		assertNotNull("Rule was not created in repository.", rule);
		
		rulesRepository.updateRule("rule", "new content");
		
		RuleItem loadedRule = rulesRepository.loadRule("rule");
		assertEquals(loadedRule.getContent(), "new content");
		
		// Expander
		ExpanderItem expander = rulesRepository.createExpander("expander", "expander content");
		assertNotNull(expander);
		rulesRepository.updateRule("rule", "new content", expander);
		assertNotNull("Rule was not updated correctly", loadedRule.getExpander());

		rulesRepository.updateRule("rule", "expander removed", null);
		assertEquals("Rule was not updated correctly", loadedRule.getContent(), "expander removed");
		assertNull("Expander was not removed", loadedRule.getExpander());

	}

	public void testUpdateExpander() {

		RulesRepository rulesRepository = getRepository();

		ExpanderItem expander = rulesRepository.createExpander("expander", "expander content");
		assertNotNull("Expander was not created in repository.", expander);
		
		ExpanderItem loadedExpander = rulesRepository.loadExpander("expander");
		rulesRepository.updateExpander("expander", "new expander content");
		assertEquals("Expander was not updated correctly", loadedExpander.getContent(), "new expander content");
	}

	public void testCreateOrUpdateRule() {
		
		RulesRepository rulesRepository = getRepository();

		RuleItem rule = rulesRepository.createOrUpdateRule("package", "rule", "content");
		assertNotNull("Rule was not created in repository.", rule);

		rulesRepository.createOrUpdateRule("package", "rule", "new content", "expander", "expander content");
		assertEquals(rule.getContent(), "new content");
		
	}

	public void testCreateOrUpdateExpander() {
		
		RulesRepository rulesRepository = getRepository();

		ExpanderItem expander = rulesRepository.createOrUpdateExpander("expander", "expander content");
		assertNotNull("Expander was not created in repository.", expander);

		rulesRepository.createOrUpdateExpander("expander", "new content");
		assertEquals(expander.getContent(), "new content");
	}

	public void testFireRuleFromRepository() {

		RulesRepository rulesRepository = getRepository();

		try {
			RepositoryTools.importDrlToRepository(this.sampleDrlFile, rulesRepository);
		} catch (IOException e) {
			fail(e.toString());
		}

		// Creating facts
		Message msg1 = new Message();
		msg1.setMessage("Hello msg1 !");
		msg1.setStatus(Message.HELLO);

		Message msg2 = new Message();
		msg2.setMessage("Good bye msg2 !");
		msg2.setStatus(Message.GOODBYE);

		// Reading rule from repository
		RuleItem ruleItem = rulesRepository.loadRule(sampleDrlNode);
		assertNotNull("Rule not found in repository.", ruleItem);

		// Firing rule
		try {
			PackageBuilder builder = new PackageBuilder();
			Reader source = ruleItem.getContentAsReader();
			builder.addPackageFromDrl(source);

			Package pkg = builder.getPackage();

			RuleBase ruleBase = RuleBaseFactory.newRuleBase();
			ruleBase.addPackage(pkg);

			WorkingMemory wm = ruleBase.newWorkingMemory();
			ArrayList results = new ArrayList();

			wm.setGlobal("results", results);
			wm.assertObject(msg1);
			wm.assertObject(msg2);

			wm.fireAllRules();
			// Verifying results
			assertFalse("No results after firing rules.", results.isEmpty());

		} catch (DroolsParserException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testFireRuleWithExpanderFromRepository() {

		// Importing rule to repository
		RulesRepository rulesRepository = getRepository();

		try {
			RepositoryTools.importDrlToRepository(this.sampleDrlWithExpanderFile, rulesRepository);
		} catch (IOException e) {
			fail(e.toString());
		}

		// Creating objects
		Message msg1 = new Message();
		msg1.setMessage("Hello msg1 !");
		msg1.setStatus(Message.HELLO);

		Message msg2 = new Message();
		msg2.setMessage("Good bye msg2 !");
		msg2.setStatus(Message.GOODBYE);

		// Reading rule from repository
		RuleItem ruleItem = rulesRepository.loadRule(sampleDrlWithExpanderNode);
		ExpanderItem expanderItem = ruleItem.getExpander();
		assertNotNull("Rule not found in repository.", ruleItem);

		// Firing rule
		try {
			PackageBuilder builder = new PackageBuilder();
			Reader drl = ruleItem.getContentAsReader();
			Reader dsl = expanderItem.getContentAsReader();
			builder.addPackageFromDrl(drl, dsl);

			Package pkg = builder.getPackage();

			RuleBase ruleBase = RuleBaseFactory.newRuleBase();
			ruleBase.addPackage(pkg);

			WorkingMemory wm = ruleBase.newWorkingMemory();
			ArrayList results = new ArrayList();

			wm.setGlobal("results", results);
			wm.assertObject(msg1);
			wm.assertObject(msg2);

			wm.fireAllRules();
			// Verifying results
			assertFalse("No results after firing rules.", results.isEmpty());

		} catch (DroolsParserException e) {
			fail(e.toString());
		} catch (IOException e) {
			fail(e.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
}
