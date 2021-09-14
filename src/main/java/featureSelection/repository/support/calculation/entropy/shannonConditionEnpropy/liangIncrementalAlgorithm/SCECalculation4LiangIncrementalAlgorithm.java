package featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.liangIncrementalAlgorithm;

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
import featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.DefaultShannonConditionEnpropyCalculation;
import org.apache.commons.math3.util.FastMath;

import lombok.Getter;

public class SCECalculation4LiangIncrementalAlgorithm
	extends DefaultShannonConditionEnpropyCalculation
	implements FeatureImportance4LiangIncremental<Double>
{
	@Getter private Double result;

	/**
	 * <strong>SCE<sub>U∩{x}</sub>(D|B)</strong> ≈ 1/(|U|+1) * (|U|H<sub>U</sub>(D|B) - log(|X<sub>p</sub>'
	 * ∩Y<sub>q</sub>'|/|X<sub>p</sub>'|)) 
	 * <p>
	 * where x is the new Instance added; X'<sub>p</sub> &isin; U∪{x}/B; Y'<sub>q</sub> &isin;
	 * U∪{x}/D.
	 * <p>
	 * <strong>Notice:</strong>
	 * Calculated entropy result is the one <strong>with/without</strong> denominator
	 * part(|U|<sup>2</sup>) bases on the value of <code>previousSigWithDenominator</code>:
	 * <ul>
	 * 	<li>SCE = numerator <del>/ denominator</del> if <code>previousSigWithDenominator</code> is
	 * 			<strong>false</strong>
	 * 	</li>
	 * 	<li>SCE = numerator / denominator if <code>previousSigWithDenominator</code> is 
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
		result = shannonEntropy(instances, equClass, newInstance, previousSig, previousSigWithDenominator);
		return this;
	}
	
	/**
	 * <strong>SCE<sub>U∩U<sub>X</sub></sub>(D|B)</strong> = 1/(|U|+|U<sub>X</sub>|) * 
	 * (|U|H<sub>U</sub>(D|B) + |U<sub>X</sub>|H<sub>U</sub>(D|B)) - &Delta;
	 * <p>
	 * where x is the new Instance added; X'<sub>p</sub> &isin; U∪{x}/B; Y'<sub>q</sub> &isin;
	 * U∪{x}/D.
	 * <p>
	 * <strong>Notice:</strong>
	 * Calculated entropy result is the one <strong>with/without</strong> denominator part(|U|<sup>2</sup>) 
	 * bases on the value of <code>previousSigWithDenominator</code>: 
	 * <ul>
	 * <li>SCE = numerator <del>/ denominator</del> if <code>previousSigWithDenominator</code> is 
	 * 		<strong>false</strong>
	 * </li>
	 * <li>SCE = numerator / denominator if <code>previousSigWithDenominator</code> is 
	 * 		<strong>true</strong>
	 * </li>
	 * </ul>
	 */
	@Override
	public FeatureImportance4LiangIncremental<Double> calculate(
			MixedEquivalenceClassSequentialList equClassesCMBResult,
			MixedEquivalenceClassSequentialList decEquClassesCMBResult,
			int previousUniverse, int newUniverse,
			Double previousSig, Double newSig, 
			boolean previousSigWithDenominator, int attributeLength
	) {
		// Count the current calculation
		countCalculate(attributeLength);
		// Calculate
		result = shannonEntropy(equClassesCMBResult, decEquClassesCMBResult, previousUniverse, newUniverse, previousSig, newSig, previousSigWithDenominator);
		return this;
	}
	
	private static double shannonEntropy(
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
		// Initiate intersect=0
		// Loop ins in X': equClass
		// 	Search for ins in Y'
		// 	if doesn't exist: sum += 1
		long intersect=0;
		for (Instance ins: equClass)
			if (!decEquClassesIdentical2NewIns.contains(ins))
				intersect += 1;
		// SE(D/P) = 1/(|U|+1) * (|U| * sig<sub>U</sub>(D/P) - log(|Xp'-Yq'|/|Xp'|)|)
		//		   = 1/(|U|+1) * (|U| * sig<sub>U</sub>(D/P) - log((|Xp'|-intersect)/|Xp'|)|)
		//		   = (|U| * sig<sub>U</sub>(D/P) - log((|Xp'|-intersect)/|Xp'|)) / (|U|+1)
		return previousSigWithDenominator?
				(instances.size() * previousSig - FastMath.log((equClass.size()-intersect)/equClass.size())) 
					/ (instances.size()+1):
				(previousSig - FastMath.log((equClass.size()-intersect)/equClass.size()));
	}

	private static double shannonEntropy(
			MixedEquivalenceClassSequentialList equClassesCMBResult,
			MixedEquivalenceClassSequentialList decEquClassesCMBResult,
			int previousUniverse, int newUniverse,
			Double previousSig, Double newSig, 
			boolean previousSigWithDenominator		
	) {
		// SCE<sub>U∪U<sub>X</sub></sub>(D|B)</strong> = 
		//	1/(|U|+|U<sub>X</sub>|) * 
		// (|U|H<sub>U</sub>(D|B) + |U<sub>X</sub>|H<sub>U</sub>(D|B)) - &Delta;
		double entropy, delta = delta(equClassesCMBResult, decEquClassesCMBResult);
		if (previousSigWithDenominator) {
			entropy = previousUniverse * previousSig + newUniverse * newSig - delta;
			entropy /= (double) (previousUniverse+newUniverse);
		}else{
			entropy = previousSig + newSig - delta;
		}
		return entropy;
	}
	
	/**
	 * <strong>&Delta;</strong> = &Sigma;<sub>i=1</sub><sup>k</sup>(
	 * 	&Sigma;<sub>j=1</sub><sup>l</sup>(
	 * 		(|X<sub>i</sub>∩Y<sub>j</sub>|/(|U|+|U<sub>X</sub>|) * 
	 * 		log((|X<sub>i</sub>||X<sub>i</sub>'∩Y<sub>j</sub>'|)/
	 * 			(|X<sub>i</sub>'||X<sub>i</sub>∩Y<sub>j</sub>|)
	 * 		))+
	 * 		(|M<sub>i</sub>∩Z<sub>j</sub>|/(|U|+|U<sub>X</sub>|) * 
	 * 		log((|M<sub>i</sub>||X<sub>i</sub>'∩Y<sub>j</sub>'|)/
	 * 			(|X<sub>i</sub>'||M<sub>i</sub>∩M<sub>j</sub>|)
	 * 		))
	 *	) + 
	 *	&Sigma;<sub>j=l+1</sub><sup>n</sup>(
	 * 		(|X<sub>i</sub>∩Y<sub>j</sub>|/(|U|+|U<sub>X</sub>|) * log(|X<sub>i</sub>|/|X<sub>i</sub>'|)
	 *	) + 
	 *	&Sigma;<sub>j=l+1</sub><sup>n'</sup>(
	 * 		(|M<sub>i</sub>∩Z<sub>j</sub>|/(|U|+|U<sub>X</sub>|) * log(|M<sub>i</sub>|/|X<sub>i</sub>'|)
	 *	)
	 * )
	 * 
	 * @param equClassesCMBResult
	 * 		Combined Equivalent Classes result in {@link MixedEquivalenceClassSequentialList} on B:
	 * 		<strong>(U∪U<sub>X</sub>)/B</strong>
	 * @param decEquClassesCMBResult
	 * 		Combined Equivalent Classes result in {@link MixedEquivalenceClassSequentialList} on D:
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
		Collection<Instance> x, m, xPlusM, y, z, yPlusZ;
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
			// X<sub>i</sub>'
			xPlusM = equClass.getInstances(ClassSetType.MIXED);
			// loop over Y'[j], where Y'[j] is in MixEquivalentClasses on D.
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
				// M<sub>i</sub>∩Z<sub>j</sub>
				Collection<Instance> intersectionOfMZ =
					LiangIncrementalAlgorithm
						.Basic
						.Mathematical
						.intersectionOf(m, z);
				// Y<sub>i</sub>'
				yPlusZ = decEquClass.getInstances(ClassSetType.MIXED);
				// X<sub>i</sub>'∩Y<sub>i</sub>'
				Collection<Instance> intersectionOfXPlusMNYPlusZ =
					LiangIncrementalAlgorithm
						.Basic
						.Mathematical
						.intersectionOf(xPlusM, yPlusZ);
				// delta += ((|X<sub>i</sub>∩Y<sub>j</sub>| * 
				//			log((|X<sub>i</sub>||X<sub>i</sub>'∩Y<sub>j</sub>'|)/
				//				(|X<sub>i</sub>'||X<sub>i</sub>∩Y<sub>j</sub>|)
				//			))+
				//			(|M<sub>i</sub>∩Z<sub>j</sub>| * 
				//			log((|M<sub>i</sub>||X<sub>i</sub>'∩Y<sub>j</sub>'|)/
				//				(|X<sub>i</sub>'||M<sub>i</sub>∩Z<sub>j</sub>|)
				// 			))) / (|U|+|U<sub>X</sub>|)
				if (!intersectionOfXY.isEmpty()) {
					delta += intersectionOfXY.size() * 
							FastMath.log(10,
								(xSize * intersectionOfXPlusMNYPlusZ.size()) / 
								((double) xPlusM.size() * intersectionOfXY.size())
							);
				}else {
					// delta += 0 * log(...) => delta += 0
				}
				if (!intersectionOfMZ.isEmpty()) {
					delta += intersectionOfMZ.size() * 
							FastMath.log(10,
								(mSize * intersectionOfXPlusMNYPlusZ.size()) / 
								((double) xPlusM.size() * intersectionOfMZ.size())
							);
				}else {
					// delta += 0 * log(...) => delta += 0
				}
			}
			// loop over Y[j], where Y[j] is in EquivalentClasses on D whose ClassSetType is PREVIOUS.
			for (int j=0; j<decEquClassesCMBResult.getPreviousOnly(); j++) {
				decEquClass = decEquClassCMBIterator.next();
				// Y<sub>j</sub>
				y = decEquClass.getInstances(ClassSetType.PREVIOUS);
				// X<sub>i</sub>∩Y<sub>j</sub>
				Collection<Instance> intersectionOfXY =
					LiangIncrementalAlgorithm
						.Basic
						.Mathematical
						.intersectionOf(x, y);
				// delta += (|X<sub>i</sub>∩Y<sub>j</sub>|/(|U|+|U<sub>X</sub>|) * log(|X<sub>i</sub>|/|X<sub>i</sub>'|)
				if (!intersectionOfXY.isEmpty()) {
					delta += intersectionOfXY.size() * FastMath.log(10,xSize / (double) xPlusM.size());
				} else {
					// delta += 0 * log(...) => delta += 0
				}
			}
			// loop over Z[j], where Z[j] is in EquivalentClasses on D whose ClassSetType is NEW.
			for (int j=0; j<decEquClassesCMBResult.getNewOnly(); j++) {
				decEquClass = decEquClassCMBIterator.next();
				// Z<sub>j</sub>
				z = decEquClass.getInstances(ClassSetType.NEW);
				// |M<sub>i</sub>∩Z<sub>j</sub>|
				Collection<Instance> intersectionOfMZ =
						LiangIncrementalAlgorithm
							.Basic
							.Mathematical
							.intersectionOf(m, z);
				// delta += (|M<sub>i</sub>∩Z<sub>j</sub>|/(|U|+|U<sub>X</sub>|) * log(|M<sub>i</sub>|/|X<sub>i</sub>'|)
				if (!intersectionOfMZ.isEmpty()) {
					delta += intersectionOfMZ.size() * FastMath.log(10,mSize / (double) xPlusM.size());
				}else {
					// delta += 0 * log(...) => delta += 0
				}
			}
		}
		return delta;
	}
	
	@Override
	public <Item> boolean calculateAble(Item item) {
		throw new UnsupportedOperationException("Unimplemeted Method!");
	}

};