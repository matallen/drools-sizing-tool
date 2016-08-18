package org.jboss.poc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.java.quickcheck.Generator;

import org.apache.commons.io.IOUtils;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Test;

import com.google.common.collect.Lists;

public class RulesPerformanceGridTest {
	private static final File storageLocation=new File("target/performance_metrics");
	private static final CSVLog csvLog=new RulesPerformanceGridTest.CSVLog(new File(storageLocation, "0-result.csv"));
	static class CSVLog{
		File f;
		CSVLog(File f){
			this.f=f;
			if (f.exists()) f.delete();
		}
		public void write(String value) throws FileNotFoundException, IOException{
			String content="";
			if (!f.exists()){f.getParentFile().mkdirs(); f.createNewFile();}else{
				content=IOUtils.toString(new FileInputStream(f));
			}
			IOUtils.write(content +value+"\n", new FileOutputStream(f));
		}
	}
	
	@Test
	public void createPerformanceMetrics() throws Exception{
		// # of rules + # of rules that are fired
		// # of facts
		// # of fact properties
		
		csvLog.write("Test Case, # of Rules, # of Facts, # of Fact Properties, # of matching Fact CONDITION/LHS's, Fact Generation, Session Fact Insertion, Rules Execution, kBase Memory Size, Facts Memory Size, Populated Session Memory Size");
		
		List<RuleTestCase> ruleTestCases=getRuleTestCases();
		buildCompileAndStoreRuleTestCases(ruleTestCases, storageLocation);
		
		Collections.sort(ruleTestCases, new Comparator<RuleTestCase>() {
			public int compare(RuleTestCase o1, RuleTestCase o2) {
				return o1.getName().compareTo(o2.getName());
			}});
		
		for(RuleTestCase rtc:ruleTestCases){
			ObjectInputStream ois=new ObjectInputStream(new FileInputStream(new File(storageLocation, rtc.getStoredName())));
			KnowledgeBase kBase=(KnowledgeBase)ois.readObject();
			
			// generate facts
			long factGenerationStart=System.currentTimeMillis();
			Generator<Object> factGenerator=new Generators.FactGenerator(kBase, rtc.getNumberOfFactProperties());
			long sizeOfkBase=getSize(kBase);
			StatefulKnowledgeSession session = kBase.newStatefulKnowledgeSession();
			List<Object> facts=Lists.newArrayList();
			for(int i=0; i<rtc.getNumberOfFacts();i++)
				facts.add(factGenerator.next());
			long sizeOfFacts=getSize(facts);
			long factGenerationDuration=(System.currentTimeMillis()-factGenerationStart);
			
			// insert facts
			long factInsertionStart=System.currentTimeMillis();
			for(int i=0; i<rtc.getNumberOfFacts();i++)
				session.insert(factGenerator.next());
			long sizeOfPopulatedSession=-1;//getSize(session);
			long factInsertionDuration=(System.currentTimeMillis()-factInsertionStart);
			
			// execute rules
			long executionStart=System.currentTimeMillis();
			session.fireAllRules();
			long executionDuration=(System.currentTimeMillis()-executionStart);
			
			session.dispose();
			
			csvLog.write(rtc.getName()+","+rtc.getNumberOfRules()+","+rtc.getNumberOfFacts()+","+rtc.getNumberOfFactProperties()+","+rtc.getNumberofRuleActivations()+","+factGenerationDuration+","+factInsertionDuration+","+executionDuration+","+sizeOfkBase+","+sizeOfFacts+","+sizeOfPopulatedSession);
		}
		
	}
	
	private long getSize(Object o) throws IOException{
		File f=new File(storageLocation, o.getClass().getSimpleName()+".sizeCalc");
		ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(f));
		oos.writeObject(o);
		return f.length();
	}
	
	private void buildCompileAndStoreRuleTestCases(List<RuleTestCase> ruleTestCases, File storageLocation) throws FileNotFoundException, IOException{
		for(RuleTestCase rtc:ruleTestCases){
			Generator<String> ruleGenerator=new Generators.RuleGenerator(rtc.getNumberOfFactProperties(), rtc.getMatchingConditionCount());
			
			KnowledgeBuilder kBuilder=KnowledgeBuilderFactory.newKnowledgeBuilder();
			KnowledgeBase kBase=KnowledgeBaseFactory.newKnowledgeBase();
			
			// generate rules
			String rules=ruleGenerator.next();
			rules=rules.substring(0, rules.indexOf("rule"));
			for(int i=0; i<rtc.getNumberOfRules();i++){
				String rule=ruleGenerator.next();
				rule=rule.substring(rule.indexOf("rule"))+"\n";
//				System.out.println(rule);
				rules+=rule;
			}
			kBuilder.add(ResourceFactory.newByteArrayResource(rules.getBytes()), ResourceType.DRL);
			if (kBuilder.hasErrors()){
				System.err.println(rules);
				System.err.println("Failure on Test Case ["+rtc.getName()+"]");
				throw new RuntimeException(kBuilder.getErrors().toString());
			}
			kBase.addKnowledgePackages(kBuilder.getKnowledgePackages());
			
			ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(new File(storageLocation, rtc.getStoredName())));
			oos.writeObject(kBase);
			
//			ObjectInputStream ois=new ObjectInputStream(new FileInputStream(new File(fileCacheLocation, key)));
//			return (T)ois.readObject();

			
//			Generator<Object> factGenerator=new Generators.FactGenerator(kBase, rtc.getNumberOfFactProperties());
//			StatefulKnowledgeSession session = kBase.newStatefulKnowledgeSession();
//			for(int i=0; i<rtc.getNumberOfFacts();i++){
//				Object fact=factGenerator.next();
//				session.insert(fact);
//			}
		}
	}
	
	private List<RuleTestCase> getRuleTestCases() throws FileNotFoundException{
		KnowledgeBuilder builder=KnowledgeBuilderFactory.newKnowledgeBuilder();
//		String drl=new SpreadsheetCompiler().compile(this.getClass().getClassLoader().getResourceAsStream("performance-config.xls"), InputType.XLS);
		String drl=new SpreadsheetCompiler().compile(new FileInputStream(new File("src/test/resources","performance-config.xls")), InputType.XLS);
		
//		System.out.println(drl);
		builder.add(ResourceFactory.newByteArrayResource(drl.getBytes()), ResourceType.DRL);
		if (builder.hasErrors()){
			System.err.println(builder.getErrors());
			throw new RuntimeException("compilation errors");
		}
		KnowledgeBase kBase=KnowledgeBaseFactory.newKnowledgeBase();
		kBase.addKnowledgePackages(builder.getKnowledgePackages());
		StatefulKnowledgeSession session = kBase.newStatefulKnowledgeSession();
		session.fireAllRules();
		List<RuleTestCase> ruleTestCases=Lists.newArrayList();
		for(Object o:session.getObjects()){
			if (RuleTestCase.class.isAssignableFrom(o.getClass()))
				ruleTestCases.add((RuleTestCase)o);
		}
		return ruleTestCases;
	}
	
}
