package featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.liangIncrementalAlgorithm;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.liangIncrementalAlgorithm.LiangIncrementalAlgorithm;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.EquivalenceClassInterf;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.MixedEquivalenceClassSequentialList;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.classSetType.ClassSetType;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiangIncremental;
import featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.DefaultCombinationConditionEntropyCalculation;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

public class CCECalculation4LiangIncrementalAlgorithm
	extends DefaultCombinationConditionEntropyCalculation
	implements FeatureImportance4LiangIncremental<Double>
{
	@Getter private Double result;

	/**
	 * <strong>CCE<sub>U∪{x}</sub>(D|B)</strong> = 
	 * 1/(|U|+1)<sup>2</sup> * (|U|(|U|-1)CCE<sub>U</sub>(D|B)+|X<sub>p</sub>'-Y<sub>q</sub>'|
	 * (3|X<sub>p</sub>'|+3|X<sub>p</sub>'∩Y<sub>q</sub>'|-5))
	 * <p>
	 * where x is the new Instance added; X'<sub>p</sub> &isin; U∪{x}/B; Y'<sub>q</sub> &isin;
	 * U∪{x}/D.
	 * <p>
	 * <strong>Notice:</strong>
	 * Calculated entropy result is the one <strong>with/without</strong> denominator part((|U|+1)<sup>2</sup>) 
	 * bases on the value of <code>previousSigWithDenominator</code>:
	 * <ul>
	 * 	<li>CCE = numerator <del>/ denominator</del> if <code>previousSigWithDenominator</code> is
	 * 			<strong>false</strong>
	 * 	</li>
	 * 	<li>CCE = numerator / denominator if <code>previousSigWithDenominator</code> is
	 * 			<strong>true</strong>
	 * 	</li>
	 * </ul>
	 */
	@Override
	public FeatureImportance4LiangIncremental<Double> calculate(
			Collection<Instance> instances, Collection<Instance> equClass,
			Instance newInstance, Double previousSig, boolean previousSigWithDenominator,
			int attributeLength
	) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		result = combinationEntropy(instances, equClass, newInstance, previousSig, previousSigWithDenominator);
		return this;
	}
	
	/**
	 * <strong>CCE<sub>U∪U<sub>X</sub></sub>(D|B)</strong> = 
	 * 1 / ((|U|+|U<sub>X</sub>|)<sup>2</sup>(|U|+|U<sub>X</sub>|-1)) * 
	 * (|U|<sup>2</sup>(|U|-1)CCE<sub>U</sub>(D|B) + 
	 * 	|U<sub>X</sub>|<sup>2</sup>(|U<sub>X</sub>|-1)CCE<sub>U<sub>X</sub></sub>(D|B))
	 * +<strong>&Delta;</strong>.
	 * <p>
	 * <strong>&Delta;</strong> = &Sigma;<sub>i=1</sub><sup>k</sup>(
	 * 	(|X<sub>i</sub>||M<sub>i</sub>|(3|X<sub>i</sub>|+3|M<sub>i</sub>|-2)) / 
	 * 	((|U|+|U<sub>X</sub>|)<sup>2</sup>(|U|+|U<sub>X</sub>|-1)) - 
	 * 	&Sigma;<sub>j=1</sub><sup>l</sup>(
	 * 		|X<sub>i</sub>∩Y<sub>j</sub>||M<sub>i</sub>∩Z<sub>j</sub>|
	 * 		(3|X<sub>i</sub>∩Y<sub>j</sub>|+3|M<sub>i</sub>∩Z<sub>j</sub>|-2)) /
	 * 		((|U|+|U<sub>X</sub>|)<sup>2</sup>(|U|+|U<sub>X</sub>|-1))
	 * 	)
	 * )
	 * <p>
	 * <strong>Notice:</strong>
	 * Calculated entropy result is the one <strong>with/without</strong> denominator
	 * part(|U∪U<sub>X</sub>| <sup>2</sup>) bases on the value of
	 * <code>previousSigWithDenominator</code>:
	 * <ul>
	 * 	<li>LCE = numerator <del>/ denominator</del> if
	 * 				<code>previousSigWithDenominator</code> is <strong>false</strong>
	 * 	</li>
	 * 	<li>LCE = numerator / denominator if <code>previousSigWithDenominator</code> is
	 * 				<strong>true</strong>
	 * 	</li>
	 * </ul>
	 */
	@Override
	public FeatureImportance4LiangIncremental<Double> calculate(
			MixedEquivalenceClassSequentialList equClassesCMBResult,
			MixedEquivalenceClassSequentialList decEquClassesCMBResult,
			int previousInstance, int newInstance,
			Double previousSig, Double newSig, boolean previousSigWithDenominator, int attributeLength
	) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		result = combinationEntropy(equClassesCMBResult, decEquClassesCMBResult, previousInstance, newInstance, previousSig, newSig, previousSigWithDenominator);
		return this;
	}
	
	private static double combinationEntropy(
		Collection<Instance> instances, Collection<Instance> equClass,
		Instance newInstance, Double previousSig, boolean previousSigWithDenominator
	) {
		// Locate equivalent class that has the same decision value as <code>newInstance</code>
		Map<IntArrayKey, Collection<Instance>> decEquClasses =
				LiangIncrementalAlgorithm
					.Basic
					.equivalenceClass(instances, new IntegerArrayIterator(new int[] {0}));
		Collection<Instance> decEquClassesIdentical2NewIns =
				decEquClasses.get(new IntArrayKey(new int[] {newInstance.getAttributeValue(0)}));
		// if decision value exist.
		if (decEquClassesIdentical2NewIns!=null) {
			// Add the new instance into the equivalent class.
			decEquClassesIdentical2NewIns.add(newInstance);
		// if no such decision value.
		}else {
			// decEquClassesIdentical2NewIns = {x}
			decEquClassesIdentical2NewIns = new LinkedList<>();
			decEquClassesIdentical2NewIns.add(newInstance);
		}
		// Initiate intersect=0, inY=0
		long intersect = 0;
		// Loop ins in X': equClass
		// 	Search for ins in Y'
		//	if exists:	intersect++
		for (Instance ins: equClass) {
			if (decEquClassesIdentical2NewIns.contains(ins))	intersect++;
		}
		// CE = 1/(|U|+1)^2 * (|U|(|U|-1))CE<sup>U</sup>(D/P)+|X'-Y'|(3|X'|+3|X'∩Y'|-5))
		//	  = 1/(|U|+1)^2 * (|U|(|U|-1)) * sig +inX*(3|X'|+3*inBoth-5))
		//	  = (|U|(|U|-1)) * sig + (X'-intersect) * (3|X'|+3*intersect-5)) / (|U|+1)^2
		return previousSigWithDenominator?
				((instances.size() * (instances.size()-1) * previousSig + 
					(equClass.size()-intersect) * (3*equClass.size()+3*intersect-5)) / 
						FastMath.pow(instances.size()+1, 2)): 
				previousSig + (equClass.size()-intersect) * (3*equClass.size()+3*intersect-5);
	}
	
	private static double combinationEntropy(
			MixedEquivalenceClassSequentialList equClassesCMBResult,
			MixedEquivalenceClassSequentialList decEquClassesCMBResult,
			int previousUniverse, int newUniverse,
			Double previousSig, Double newSig, boolean previousSigWithDenominator
	) {
		// CCE<sub>U∪U<sub>X</sub></sub>(D|B) = 
		//	1 / ((|U|+|U<sub>X</sub>|)<sup>2</sup>(|U|+|U<sub>X</sub>|-1)) * 
		//	(|U|<sup>2</sup>(|U|-1)CCE<sub>U</sub>(D|B) + 
		//	|U<sub>X</sub>|<sup>2</sup>(|U<sub>X</sub>|-1)CCE<sub>U<sub>X</sub></sub>(D|B))
		//	+ &Delta;.
		double entropy, delta = delta(equClassesCMBResult, decEquClassesCMBResult);
		if (previousSigWithDenominator) {
			entropy = FastMath.pow(previousUniverse, 2) * (previousUniverse-1) * previousSig + 
						FastMath.pow(newUniverse, 2) * (newUniverse-1) * newSig + delta;
			entropy /= (FastMath.pow(previousUniverse+newUniverse, 2) * (previousUniverse+newUniverse-1));
		}else{
			entropy = previousSig + newSig + delta;
		}
		return entropy;
	}
	
	/**
	 * <strong>&Delta;</strong> = &Sigma;<sub>i=1</sub><sup>k</sup>(
	 * 	(|X<sub>i</sub>||M<sub>i</sub>|(3|X<sub>i</sub>|+3|M<sub>i</sub>|-2)) / 
	 * 	((|U|+|U<sub>X</sub>|)<sup>2</sup>(|U|+|U<sub>X</sub>|-1)) - 
	 * 	&Sigma;<sub>j=1</sub><sup>l</sup>(
	 * 		|X<sub>i</sub>∩Y<sub>j</sub>||M<sub>i</sub>∩Z<sub>j</sub>|
	 * 		(3|X<sub>i</sub>∩Y<sub>j</sub>|+3|M<sub>i</sub>∩Z<sub>j</sub>|-2)) /
	 * 		((|U|+|U<sub>X</sub>|)<sup>2</sup>(|U|+|U<sub>X</sub>|-1))
	 * 	)
	 * )
	 * 
	 * @param equClassesCMBResult
	 * 		Combined Equivalent Classes result in
	 * 		{@link MixedEquivalentClassSequentialList} on B:
	 * 		<strong>(U∪U<sub>X</sub>)/B</strong>
	 * @param decEquClassesCMBResult
	 * 		Combined Equivalent Classes result in
	 * 		{@link MixedEquivalentClassSequentialList} on D:
	 * 		<strong>(U∪U<sub>X</sub>)/D</strong>
	 * @return the result of &Delta;.
	 */
	private static double delta(
			MixedEquivalenceClassSequentialList equClassesCMBResult,
			MixedEquivalenceClassSequentialList decEquClassesCMBResult
	) {
		// Initiate delta.
		double delta = 0.0;
		// Loop over instances in X'[i], where X'[i] is in MixEquivalentClasses on B.
		double redPart4Delta, decPart4Delta;
		Collection<Instance> x, m, y, z;
		EquivalenceClassInterf equClass, decEquClass;
		Iterator<EquivalenceClassInterf> decEquClassCMBIterator;
		Iterator<EquivalenceClassInterf> equClassCMBIterator = equClassesCMBResult.getEquClasses().iterator();
		for (int i=0; i<equClassesCMBResult.getMixed(); i++) {
			equClass = equClassCMBIterator.next();
			// X<sub>i</sub>
			x = equClass.getInstances(ClassSetType.PREVIOUS);
			int xSize = x==null? 0: x.size();
			// M<sub>i</sub>
			m = equClass.getInstances(ClassSetType.NEW);
			int mSize = m==null? 0: m.size();
			// Calculate 
			//	(|X<sub>i</sub>||M<sub>i</sub>|(3|X<sub>i</sub>|+3|M<sub>i</sub>|-2)) / 
			//	((|U|+|U<sub>X</sub>|)<sup>2</sup>(|U|+|U<sub>X</sub>|-1))
			redPart4Delta = xSize * mSize * (3*xSize+3*mSize-2);
			// loop over Y'[j], where Y'[j] is in MixEquivalentClasses on D.
			decPart4Delta = 0.0;
			decEquClassCMBIterator = decEquClassesCMBResult.getEquClasses().iterator();
			for (int j=0; j<decEquClassesCMBResult.getMixed(); j++) {
				decEquClass = decEquClassCMBIterator.next();
				// Y<sub>j</sub>
				y = decEquClass.getInstances(ClassSetType.PREVIOUS);
				// |X<sub>i</sub>∩Y<sub>j</sub>|
				Collection<Instance> intersectionOfXY =
					LiangIncrementalAlgorithm
						.Basic
						.Mathematical
						.intersectionOf(x, y);
				// Z<sub>j</sub>
				z = decEquClass.getInstances(ClassSetType.NEW);
				// |M<sub>i</sub>∩Z<sub>j</sub>|
				Collection<Instance> intersectionOfMZ =
					LiangIncrementalAlgorithm
						.Basic
						.Mathematical
						.intersectionOf(m, z);
				// delta_sub += (|X<sub>i</sub>∩Y<sub>j</sub>||M<sub>i</sub>∩Z<sub>j</sub>|
				//				(3|X<sub>i</sub>∩Y<sub>j</sub>| + 3|M<sub>i</sub>∩Z<sub>j</sub>|-2))/ 
				//				((|U|+|U<sub>X</sub>|)<sup>2</sup>(|U|+|U<sub>X</sub>|-1))
				decPart4Delta += intersectionOfXY.size() * intersectionOfMZ.size() * 
								(3*intersectionOfXY.size() + 3*intersectionOfMZ.size() -2);
			}
			// Calculate &Delta;<sub>i</sub>
			delta += (redPart4Delta - decPart4Delta);
		}
		return delta;
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		throw new UnsupportedOperationException("Unimplemeted Method!");
	}
};