package org.jboss.poc;

public class RuleTestCase {
	private String name;
	private long numberOfRules;
	private long numberofRuleActivations;
	private long matchingConditionCount;
	private long numberOfFacts;
	private long numberOfFactProperties;
	
	public RuleTestCase(String name, String numberOfRules, String numberofRuleActivations, String matchingConditionCount, String numberOfFacts, String numberOfFactProperties) {
		super();
		this.name=name;
		this.numberOfRules = Long.parseLong(numberOfRules);
		this.numberofRuleActivations = Long.parseLong(numberofRuleActivations);
		this.matchingConditionCount = Long.parseLong(matchingConditionCount);
		this.numberOfFacts = Long.parseLong(numberOfFacts);
		this.numberOfFactProperties = Long.parseLong(numberOfFactProperties);
	}
	public String getStoredName(){
		return name.replaceAll(" ", "_")+".kBase";
	}
	
	public String getName(){
		return name;
	}
	public long getNumberOfRules() {
		return numberOfRules;
	}
	public long getNumberofRuleActivations() {
		return numberofRuleActivations;
	}
	public long getMatchingConditionCount() {
		return matchingConditionCount;
	}
	public long getNumberOfFacts() {
		return numberOfFacts;
	}
	public long getNumberOfFactProperties() {
		return numberOfFactProperties;
	}
	
}
