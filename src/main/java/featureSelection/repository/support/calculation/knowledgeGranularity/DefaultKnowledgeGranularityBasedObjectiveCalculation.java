package featureSelection.repository.support.calculation.knowledgeGranularity;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.featureImportance.knowledgeGranularity.KnowledgeGranularityBasedObjectiveCalculation;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

public class DefaultKnowledgeGranularityBasedObjectiveCalculation
	implements KnowledgeGranularityBasedObjectiveCalculation<Double>
{
	@Getter private long calculationTimes = 0;	
	@Getter private Double result;
	
	private Caches caches = new Caches();
	
	/**
	 * Calculate the objective value:
	 * <p>
	 * J(S) = [|S| * GP_YS] / sqrt( |S|+|S|(|S|-1) * GP_SS )
	 * <p>
	 * where
	 * <ul>
	 * 	<li>GP_YS = 1 / |S| * sum GP(Si|Y)</li>
	 * 	<li>GP_SS = 1 / (|S|(|S|-1)) * (sum sum (GP(Si|Sj) + GP(Sj|Si))  ) where i!=j</li>
	 * </ul>
	 *
	 * which can be transformed into the following:
	 * <p>
	 * <strong>J(S) = [GP_YS'] / sqrt( |S|+ GP_SS' )</strong>
	 * <p>
	 * where
	 * <ul>
	 * 	<li>GP_YS' = sum GP(Si|Y)</li>
	 * 	<li>GP_SS' = sum sum (GP(Si|Sj) + GP(Sj|Si)) where i!=j</li>
	 * </ul>
	 * 
	 * @param Instances
	 * 		{@link Instance} {@link Collection}: <strong>U</strong>
	 * @param attributes
	 * 		Attribute subset for Objective value calculation: <strong>S</strong>
	 * @param gpCalculation
	 * 		{@link DefaultKnowledgeGranularityCalculation} instance.
	 * @return this.
	 */
	public DefaultKnowledgeGranularityBasedObjectiveCalculation calculate(
			Collection<Instance> Instances, IntegerIterator attributes,
			DefaultKnowledgeGranularityCalculation gpCalculation
	) {
		// Calculate GP_YS'
		double gp_ys = numerator(Instances, attributes, gpCalculation);
		// Calculate sqrt( |S|+ GP_SS' )
		double gp_ss = denominator(Instances, attributes, gpCalculation);
		
		result = gp_ys / gp_ss;
		
		return this;
	}
	
	/**
	 * Calculate the numerator of the objective value:
	 * <strong>GP_YS' = &sum;<sub>P<sub>i</sub>&isin;P</sub>
	 * GP(P<sub>i</sub>|Y)</strong>
	 * 
	 * @param instances
	 * 		An {@link Instance} {@link Collection}: <strong>U</strong>
	 * @param attributes
	 * 		Attributes (subset) of {@link Instance}(Starts from 1): <strong>P</strong>
	 * @param gpCalculation
	 * 		{@link DefaultKnowledgeGranularityCalculation} instance.
	 * @return the numerator of J(P): GP_YP' = &sum;<sub>P<sub>i</sub>&isin;
	 * 		P</sub>GP(P<sub>i</sub>|Y).
	 */
	private double numerator(
			Collection<Instance> instances, IntegerIterator attributes,
			DefaultKnowledgeGranularityCalculation gpCalculation
	) {
		double gp_ys = 0;
		attributes.reset();
		for (int i=0; i<attributes.size(); i++)
			gp_ys += caches.cacheDecAttrRelativeCache(attributes.next(), instances, gpCalculation);
		return gp_ys;
	}
	
	/**
	 * Calculate the denominator of the objective value: <strong>sqrt(|P|+ GP_SS')</strong>, 
	 * where GP_SS' = &sum;<sub>P<sub>i</sub>&isin;P</sub>&sum;<sub>P<sub>j</sub>&isin;P & j!=i</sub>
	 * (GP(P<sub>i</sub>|P<sub>j</sub>) + GP(P<sub>j</sub>|P<sub>i</sub>)).
	 * 
	 * @param instances
	 * 		An {@link Instance} {@link Collection}: <strong>U</strong>
	 * @param attributes
	 * 		Attributes (subset) of {@link Instance}(Starts from 1): <strong>P</strong>
	 * @param gpCalculation
	 * 		{@link DefaultKnowledgeGranularityCalculation} instance.
	 * @return the denominator of J(P): sqrt(|P|+ GP_SS'), where GP_SS' =
	 * 		&sum;<sub>P<sub>i</sub>&isin;
	 * 		P</sub>&sum;<sub>P<sub>j</sub>&isin;P & j!=i</sub> (GP(P<sub>i</sub>|P<sub>j</sub>) +
	 * 		GP(P<sub>j</sub>|P<sub>i</sub>)).
	 */
	private double denominator(
			Collection<Instance> instances, IntegerIterator attributes,
			DefaultKnowledgeGranularityCalculation gpCalculation
	) {
		IntegerIterator attributes1 = attributes, attributes2 = attributes.clone();
		
		int attr1, attr2;
		attributes1.reset();
		double denominator = 0;
		for (int i=0; i<attributes.size(); i++) {
			attr1 = attributes1.next();
			// U/Si
			attributes2.reset();
			for (int j=0; j<attributes.size(); j++) {
				attr2 = attributes2.next();
				// i!=j
				if (attr1!=attr2){
					// U/Sj
					// Calculate GP(Si|Sj) + GP(Sj|Si)
					//		= GP(Si) - GP(Si∪Sj) + GP(Sj) - GP(Si∪Sj)
					//		= GP(Si) + GP(Sj) - 2 * GP(Si∪Sj)
					double gp_plus = 
						// GP(Si)
						caches.cacheKnowledgeGranularity(attr1, instances, gpCalculation) +
						// GP(Sj)
						caches.cacheKnowledgeGranularity(attr2, instances, gpCalculation) -
						// 2 * GP(Si∪Sj)
						2 * caches.cacheKnowledgeGranularity(new int[] {attr1, attr2}, instances, gpCalculation);
					denominator += gp_plus;
				}else {
					// do nothing.
				}
			}
		}
		// sqrt(|P|+ sum)
		return FastMath.sqrt(attributes.size()+denominator);
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		throw new RuntimeException("Unimplemented method!");
	}

	@Override
	public long getCalculationAttributeLength() {
		throw new RuntimeException("Unimplemented method!");
	}

	@Override
	public boolean value1IsBetter(Double v1, Double v2, Double deviation) {
		return Double.compare((v2==null?0: v2.doubleValue()) - (v1==null?0: v1.doubleValue()), 
								deviation.doubleValue())>0;
	}
	
	@Override
	public Double plus(Double v1, Double v2) throws Exception {
		throw new RuntimeException("Unimplemented method!");
	}
	
	public void clearCaches() {
		caches.clearEquivalenceClassesCache();
		caches.clearKnowledgeGraunlarityCache();
		caches.clearDecAttrRelativeCache();
	}
	
	/**
	 * Cache for Knowledge Granularity based Objective value calculations with the following cache fields:
	 * <ul>
	 * 	<li><strong>{@link #equClassesCache}</strong>
	 * 			<p>For Equivalence Classes induced by a single attribute: <strong>U/{a}</strong>
	 * 	</li>
	 * 	<li><strong>{@link #decAttrRelativeCache}</strong>
	 * 			<p>For <strong>GP(<i>a</i>|<i>Y</i>)</strong> where <i>a</i> is an attribute in
	 * 			{@link Instance}.
	 * 	</li>
	 * 	<li><strong>{@link #knowledgeGraunlarityCache}</strong>
	 * 			<p>For <strong>GP(<i>P</i>)</strong> where <i>P</i> is an attribute or an attribute subset in
	 * 			{@link Instance}.
	 * 	</li>
	 * </ul>
	 *
	 * @author Benjamin_L
	 */
	private class Caches {
		private Map<Integer, Collection<List<Instance>>> equClassesCache = new HashMap<>();
		private Map<Integer, Double> decAttrRelativeCache = new HashMap<>();
		private Map<IntArrayKey, Double> knowledgeGraunlarityCache = new HashMap<>();
		
		public void clearEquivalenceClassesCache() {	equClassesCache.clear();			}
		public void clearKnowledgeGraunlarityCache() {	knowledgeGraunlarityCache.clear();	}
		public void clearDecAttrRelativeCache() {		decAttrRelativeCache.clear();		}
		
		/**
		 * Get U/attribute.  
		 * <p>
		 * Cache if needed.
		 * 
		 * @param attribute
		 * 		An attribute of {@link Instance}. (Starts from 1)
		 * @param instances
		 * 		An {@link Instance} {@link Collection}.
		 * @return an Equivalence classes.
		 */
		private Collection<List<Instance>> cacheEquivalenceClasses(
			int attribute, Collection<Instance> instances
		){
			Collection<List<Instance>> cache = equClassesCache.get(attribute);
			if (cache==null) {
				cache = instances
							.stream()
							.collect(Collectors.groupingBy(ins->ins.getAttributeValue(attribute)))
							.values();
				equClassesCache.put(attribute, cache);
			}
			return cache;
		}
		/**
		 * Get GP(attribute).  
		 * <p>
		 * Cache if needed.
		 * 
		 * @param attribute
		 * 		An attribute of {@link Instance}. (Starts from 1)
		 * @param instances
		 * 		An {@link Instance} {@link Collection}.
		 * @param gpCalculation
		 * 		{@link DefaultKnowledgeGranularityCalculation} instance.
		 * @return Knowledge Granularity of the given attribute.
		 */
		private double cacheKnowledgeGranularity(
				int attribute, Collection<Instance> instances,
				DefaultKnowledgeGranularityCalculation gpCalculation
		) {
			// U/attribute
			Collection<List<Instance>> equClasses = cacheEquivalenceClasses(attribute, instances);
			// Lazy GP(attribute) calculation.
			IntArrayKey key = new IntArrayKey(new int[] {attribute});
			Double cache = knowledgeGraunlarityCache.get(key);
			if (cache==null) {
				cache = gpCalculation.calculate(
							equClasses.stream().mapToInt(Collection::size).toArray(), 
							instances.size()
						).getResult();
				knowledgeGraunlarityCache.put(key, cache);
			}
			return cache;
		}
		/**
		 * Get GP(attributes).  
		 * <p>
		 * Cache if needed.
		 * 
		 * @param attributes
		 * 		Attributes of {@link Instance}. (Starts from 1): <strong>P</strong>
		 * @param instances
		 * 		An {@link Instance} {@link Collection}.
		 * @param gpCalculation
		 * 		{@link DefaultKnowledgeGranularityCalculation} instance.
		 * @return Knowledge Granularity of the given attributes.
		 */
		private double cacheKnowledgeGranularity(
				int[] attributes, Collection<Instance> instances,
				DefaultKnowledgeGranularityCalculation gpCalculation
		) {
			Arrays.sort(attributes);
			// Lazy GP(attributes) calculation.
			IntArrayKey key = new IntArrayKey(attributes);
			Double cache = knowledgeGraunlarityCache.get(key);
			if (cache==null) {
				// U/attributes
				Collection<List<Instance>> equClasses = null;
				Collection<Collection<Instance>> targetEquClasses;
				for (int attr: attributes) {
					equClasses = equClassesCache.get(attr);
					if (equClasses!=null)	break;
				}
				// If no equivalence classes induced by any of the given attributes
				if (equClasses==null) {
					targetEquClasses = 
						ClassicAttributeReductionHashMapAlgorithm
							.Basic
							.equivalenceClass(instances, new IntegerArrayIterator(attributes))
							.values();
				// Otherwise there exists equivalence classes induced by any of the given attributes
				//	do further partitioning based on the equivalence classes.
				}else {
					targetEquClasses = new LinkedList<>();
					for (Collection<Instance> equClass: equClasses) {
						targetEquClasses.addAll(
							ClassicAttributeReductionHashMapAlgorithm
								.Basic
								.equivalenceClass(equClass, new IntegerArrayIterator(attributes))
								.values()	
						);
					}
				}
				// Calculate GP(attributes)
				cache = gpCalculation.calculate(
							targetEquClasses.stream().mapToInt(Collection::size).toArray(), 
							instances.size()
						).getResult();
				knowledgeGraunlarityCache.put(key, cache);
			}
			return cache;
		}
		/**
		 * Calculate GP(attribute | Y).  
		 * <p>
		 * Cache if needed.
		 *
		 * @param attribute
		 * 		An attribute of {@link Instance}. (Starts from 1)
		 * @param instances
		 * 		An {@link Instance} {@link Collection}.
		 * @param gpCalculation
		 * 		{@link DefaultKnowledgeGranularityCalculation} instance.
		 * @return GP(attribute | Y)
		 */
		private double cacheDecAttrRelativeCache(
				int attribute, Collection<Instance> instances,
				DefaultKnowledgeGranularityCalculation gpCalculation
		) {
			// U/D
			Collection<List<Instance>> decEquClasses = cacheEquivalenceClasses(0, instances);
			// GP(D)
			Double decKnowledgeGranularity = cacheKnowledgeGranularity(0, instances, gpCalculation);
			// Calculate GP(attribute, D)
			Double cache = decAttrRelativeCache.get(attribute);
			// If no cache previously.
			if (cache==null) {
				// Calculate GP(Si ∪ D)
				double gpPD = 
					gpCalculation.calculate(
						// (U/D)/P
						decEquClasses.stream().map(equClass->
							equClass.stream()
									.collect(Collectors.groupingBy(ins->ins.getAttributeValue(attribute)))
									.values()
						).flatMapToInt(equClass->equClass.stream().mapToInt(Collection::size))
						.toArray(), 
						instances.size()
					).getResult();
				// Calculate GP(Si|Y)
				cache = gpCalculation.calculateRelative(decKnowledgeGranularity, gpPD)
									.getResult();
				// Save into cache
				decAttrRelativeCache.put(attribute, cache);
			}
			return cache;
		}
	}
}