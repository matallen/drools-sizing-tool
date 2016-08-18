package org.jboss.poc;

import java.io.IOException;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;

import org.apache.commons.io.IOUtils;
import org.drools.KnowledgeBase;
import org.drools.definition.type.FactType;

public class Generators {
	
	public static void main(String[] args){
		System.out.println(new Generators.RuleGenerator(10, 5).next());
	}
	
	public static class FactGenerator implements Generator<Object>{
		private long propertyCount;
		private KnowledgeBase kBase;
		public FactGenerator(KnowledgeBase kBase, long propertyCount){
			this.propertyCount=propertyCount;
			this.kBase=kBase;
		}
		@Override
		public Object next() {
			FactType factType = kBase.getFactType("testpackage", "MyFact");
			Object fact;
			try {
				fact = factType.newInstance();
				for(int i=0;i<propertyCount;i++){
					factType.set(fact, "field"+i, "value"+i);
				}
				return fact;  
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static class RuleGenerator implements Generator<String>{
		private long propertyCount;
		private long matchingConditionCount;
		public RuleGenerator(long propertyCount, long matchingConditionCount){
			this.propertyCount=propertyCount;
			this.matchingConditionCount=matchingConditionCount;
		}
		
		@Override
		public String next() {
			String rule=null;
			try {
				String matchingConditions="";
				for(int i=0;i<matchingConditionCount;i++){
					matchingConditions+="field"+i+" == 'value"+i+"',";
				}
				
				String factFields="";
				for(int i=0;i<propertyCount;i++)
					factFields+="  field"+i+" : String\n";
				rule=String.format(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("ruletemplate.drl")),
						factFields,
						"rule_"+PrimitiveGenerators.letterStrings(10, 10).next(),
						matchingConditions.substring(0,  matchingConditions.length()-1));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return rule;
		}
		
	}
}
