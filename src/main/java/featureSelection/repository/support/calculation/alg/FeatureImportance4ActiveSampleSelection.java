package featureSelection.repository.support.calculation.alg;

import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.alg.activeSampleSelection.EquivalenceClass;

import java.util.Collection;

/**
 * An interface for Feature Importance calculation using Rough Set Theory based Sample Pair
 * Selection.
 * <p>
 * Implementations should base on original articles:
 * <ul>
 * 	<li>
 * 		<a href="https://ieeexplore.ieee.org/document/7492272">
 * 		"Active Sample Selection Based Incremental Algorithm for Attribute Reduction With Rough
 * 		Sets"</a>
 * 		by Yanyan Yang, Degang Chen, Hui Wang,
 * 	</li>
 * 	<li>
 * 		<a href="https://ieeexplore.ieee.org/document/6308684/">
 * 		"Sample Pair Selection for Attribute Reduction with Rough Set"</a>
 * 		by Degang Chen, Suyun Zhao, Lei Zhang, Yongping Yang, Xiao Zhang.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		Type of feature importance.
 */
@RoughSet
public interface FeatureImportance4ActiveSampleSelection<V> 
	extends FeatureImportance<V>
{
	FeatureImportance4ActiveSampleSelection<V> calculate(Collection<EquivalenceClass> equClasses);
}