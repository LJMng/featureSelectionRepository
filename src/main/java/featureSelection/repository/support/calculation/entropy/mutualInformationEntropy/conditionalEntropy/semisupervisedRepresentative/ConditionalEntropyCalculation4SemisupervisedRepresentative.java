package featureSelection.repository.support.calculation.entropy.mutualInformationEntropy.conditionalEntropy.semisupervisedRepresentative;

import java.util.Collection;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.alg.SemisupervisedRepresentativeStrategy;
import featureSelection.repository.algorithm.alg.semisupervisedRepresentative.SemisupervisedRepresentativeAlgorithm;
import featureSelection.repository.support.calculation.alg.semisupervisedRepresentative.FeatureImportance4SemisupervisedRepresentative;
import featureSelection.repository.support.calculation.entropy.mutualInformationEntropy.conditionalEntropy.DefaultConditionalEntropyCalculation;
import org.apache.commons.math3.util.FastMath;

/**
 * Conditional Entropy Calculation:
 * <p>
 * H(A|B) = - &Sigma;<sub>f<sub>j</sub> in B</sub> &Sigma;<sub>f<sub>i</sub> &isin; 
 * A</sub> ( p(f<sub>i</sub>, f<sub>j</sub>) * log p(f<sub>i</sub> | f<sub>j</sub>) )
 * 
 * @author Benjamin_L
 */
public class ConditionalEntropyCalculation4SemisupervisedRepresentative
	extends DefaultConditionalEntropyCalculation
	implements SemisupervisedRepresentativeStrategy,
				FeatureImportance4SemisupervisedRepresentative<Double>
{
	private double entropy;
	@Override
	public Double getResult() {
		return entropy;
	}
	
	/**
	 * H(A|B) = - &Sigma;<sub>f<sub>j</sub> in B</sub> &Sigma;<sub>f<sub>i</sub> &isin; 
	 * A</sub> ( p(f<sub>i</sub>, f<sub>j</sub>) * log p(f<sub>i</sub> | f<sub>j</sub>) )
	 * 
	 * @param equClasses
	 * 		Equivalent classes partitioned by B: <strong>U/B</strong>
	 * @param args
	 * 		Extra arguments including: 
	 * 		<ul>
	 * 		<li>Conditional partitioned attributes in {@link IntegerIterator}: 
	 * 			<strong>A</strong>.
	 * 		</li>
	 * 		<li>The size of total {@link Instance} in <code>equClasses</code>:
	 * 			<strong>|U|</strong>
	 * 		</li>
	 * 		</ul>
	 */
	@Override
	public FeatureImportance4SemisupervisedRepresentative<Double> calculate(
			Collection<Collection<Instance>> equClasses, Object... args
	) {
		IntegerIterator conditionalPartition = (IntegerIterator) args[0];
		int universeSize = (int) args[1];
		
		entropy = 0;
		// Loop h in U/B: equClasses
		int condEquUniverseSize, equivalentClassUniverseSize;
		Collection<Collection<Instance>> conditionalEquClasses;
		for (Collection<Instance> equClass: equClasses) {
			// E = h/Y: conditionalEquClasses
			conditionalEquClasses = 
				SemisupervisedRepresentativeAlgorithm
					.Basic
					.equivalenceClass(equClass, conditionalPartition)
					.values();
			// |E| = |h|
			equivalentClassUniverseSize = equClass.size();
			// Loop y in E
			for (Collection<Instance> condEquClass: conditionalEquClasses) {
				condEquUniverseSize = condEquClass.size();
				// H -= |y| / |U| * log(|y| / |E|)
				entropy -= condEquUniverseSize / (double) universeSize * 
							FastMath.log(condEquUniverseSize / (double) equivalentClassUniverseSize);
			}
		}
		return this;
	}
	
	@Override
	public Double plus(Double v1, Double v2) throws Exception {
		throw new RuntimeException("Unimplemented method!");
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		if (item instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) item;
			if (map.values() instanceof Collection)
				return map.isEmpty()? true: map.values().iterator().next() instanceof Instance;
			else
				return false;
		}else {
			return false;
		}
	}
}