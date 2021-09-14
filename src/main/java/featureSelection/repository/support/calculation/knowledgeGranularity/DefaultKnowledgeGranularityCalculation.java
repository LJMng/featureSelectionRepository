package featureSelection.repository.support.calculation.knowledgeGranularity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.featureImportance.knowledgeGranularity.KnowledgeGranularityCalculation;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

/**
 * Default implementation of {@link KnowledgeGranularityCalculation} with
 * <strong>double</strong> <i>Knowledge Granularity</i> value.
 * 
 * @see KnowledgeGranularityCalculation
 * 
 * @author Benjamin_L
 */
public abstract class DefaultKnowledgeGranularityCalculation
	implements KnowledgeGranularityCalculation<Double>
{
	@Getter private Double result;
	
	@Getter private Map<IntArrayKey, Double> knowledgeGranularityCache = new HashMap<>();
	public void clearKnowledgeGranularityCache() {	knowledgeGranularityCache.clear();	}
	
	/**
	 * Calculate the <strong>knowledge granularity</strong> of A:
	 * <p>
	 * GP<sub>U</sub>(A) = &Sigma;<sub>i=1</sub><sup>v</sup>(|X<sub>i</sub>|<sup>2</sup>/|U|<sup>2</sup>).
	 * <p>
	 * where |·| is the cardinal number of an instance set.(i.e. the number of universe
	 * instances inside)
	 * 
	 * @see #calculate(Collection, int)
	 * 
	 * @param cardinalNums
	 * 		The cardinal numbers of the equivalence classes induced by <i>A</i> (i.e.
	 * 		<strong>U/A</strong>) in {@link IntegerIterator}. The sum of the numbers
	 * 		should be equals to |U|.
	 * @param insSize
	 * 		The total number of {@link Instance} in the the equivalence classes induced by <i>A</i>:
	 * 		<strong>|U|</strong>.
	 * @return this.
	 */
	public DefaultKnowledgeGranularityCalculation calculate(int[] cardinalNums, int insSize) {
		Double cache = knowledgeGranularityCache.get(new IntArrayKey(cardinalNums));
		if (cache==null) {
			double knowledgeGranularity = 0;
			// sum |X<sub>i</sub>|^2 in U/A
			for (int i=0; i<cardinalNums.length; i++)
				knowledgeGranularity += FastMath.pow(cardinalNums[i], 2);
			// divide by |U|^2.
			cache = knowledgeGranularity / FastMath.pow(insSize, 2);
		}
		result = cache;
		return this;
	}
	/**
	 * Calculate the <strong>knowledge granularity</strong> of A:
	 * <p>
	 * GP<sub>U</sub>(A) = &Sigma;<sub>i=1</sub><sup>v</sup>(|X<sub>i</sub>|<sup>2</sup>/|U|<sup>2</sup>).
	 * <p>
	 * where |·| is the cardinal number of an instance set.(i.e. the number of universe instance inside)
	 * 
	 * @see {@link #calculate(int[], int)}
	 * 
	 * @param cardinalNums
	 * 		The cardinal numbers of the equivalence classes induced by <i>A</i> (i.e. <strong>U/A</strong>)
	 * 		in {@link IntegerIterator}. The sum of the numbers should be equals to |U|.
	 * @param instanceSize
	 * 		The total number of {@link Instance} in the the equivalence classes induced by <i>A</i>:
	 * 		<strong>|U|</strong>.
	 * @return this.
	 */
	public DefaultKnowledgeGranularityCalculation calculate(
			Collection<Integer> cardinalNums, int instanceSize
	) {
		Double cache = knowledgeGranularityCache.get(new IntArrayKey(cardinalNums.stream().sorted().mapToInt(v->v).toArray()));
		if (cache==null) {
			double knowledgeGranularity = 0;
			// sum |X<sub>i</sub>|^2 in U/A
			for (int num: cardinalNums)	knowledgeGranularity += FastMath.pow(num, 2);
			// divide by |U|^2.
			cache = knowledgeGranularity / FastMath.pow(instanceSize, 2);
		}
		result = cache;
		return this;
	}
	
	/**
	 * Calculate the <strong>knowledge granularity</strong> of A relative to B:
	 * <p>
	 * GP<sub>U</sub>(B|A) = GP<sub>U</sub>(A) - GP<sub>U</sub>(A ∪ B)
	 * 
	 * @param prioriKnowledgeGranularity
	 * 		The Knowledge Granularity value of A: <strong>GP<sub>U</sub>(A)</strong>
	 * @param knowledgeGranularityWithBoth
	 * 		The Knowledge Granularity value of A and B: <strong>GP<sub>U</sub>(A ∪ B)</strong>
	 * @return this.
	 */
	public DefaultKnowledgeGranularityCalculation calculateRelative(
			double prioriKnowledgeGranularity, double knowledgeGranularityWithBoth
	) {
		result = prioriKnowledgeGranularity - knowledgeGranularityWithBoth;
		return this;
	}
	
	@Override
	public boolean value1IsBetter(Double v1, Double v2, Double deviation) {
		return Double.compare((v2==null?0: v2.doubleValue()) - (v1==null?0: v1.doubleValue()), 
								deviation.doubleValue())>0;
	}
	
	@Override
	public Double plus(Double v1, Double v2) throws Exception {
		throw new UnsupportedOperationException("Unimplemented method!");
	}
}