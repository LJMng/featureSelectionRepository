package featureSelection.repository.support.calculation.alg;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.support.calculation.FeatureImportance;

import java.util.Collection;

/**
 * An interface for Feature Importance calculation from the discernibility view bases on the article:
 * <a href="https://linkinghub.elsevier.com/retrieve/pii/S0020025515005605"> "Efficient attribute reduction 
 * from the viewpoint of discernibility"</a> by Shu-Hua Teng, Min Lu, A-Feng Yang, Jun Zhang, Yongjian Nian, 
 * Mi He.
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		Type of feature importance.
 */
public interface FeatureImportance4TengDiscernibilityView<V>
	extends FeatureImportance<V>
{
	public final String CALCULATION_NAME = "DIS";
	
	/**
	 * Calculate the <strong>Discernibility Degree</strong>: <i>|DIS(P)|</i>.
	 * <p>
	 * <strong>|DIS(P)|</strong> = |U|^2 - &Sigma;<sub>t=1:m</sub>|P<sub>t</sub>|^2
	 * <p>
	 * where P &sube; A, A is the attributes of U, U/P = {P<sub>1</sub>, P<sub>2</sub>, ..., P<sub>m</sub>}
	 * 
	 * @param universeSize
	 * 		The number of {@link Instance} in <code>condEquClasses</code>: <strong>|U|</strong>.
	 * @param equClasses
	 * 		Equivalent classes in {@link Collection} whose elements are an {@link Instance}
	 * 		{@link Collection} as an equivalent class. Marked as <strong>U/P</strong>.
	 * @param args
	 * 		Extra arguments.
	 * @return <code>this</code> instance.
	 */
	public FeatureImportance4TengDiscernibilityView<V> calculate(
			int universeSize, Collection<Collection<Instance>> equClasses,
			Object...args
	);
	
	/**
	 * Calculate the <strong>Relative Discernibility Degree</strong>: <i>|DIS(Q/P)|</i>.
	 * <p>
	 * <strong>|DIS(Q/P)|</strong> = 
	 * 		&Sigma;<sub>i=1:m</sub>|P<sub>i</sub>|^2 - &Sigma;<sub>k=1:n</sub>|M<sub>k</sub><sup>pq</sup>|^2
	 * <p>
	 * where P, Q &sube; A, A is the attributes of U, U/(P&cup;Q) = {
	 * 	M<sub>1</sub><sup>pq</sup>, M<sub>2</sub><sup>pq</sup>, ..., M<sub>n</sub><sup>pq</sup>} and 
	 * 	U/P = {P<sub>1</sub>, P<sub>2</sub>, ..., P<sub>m</sub>}
	 * 
	 * @param condEquClasses
	 * 		Conditional equivalent classes in {@link Collection} whose elements are an {@link Instance}
	 * 		{@link Collection} as an equivalent class. Marked as <strong>U/P</strong>.
	 * @param gainedEquClasses
	 * 		Gained equivalent classes in {@link Collection} whose elements are an {@link Instance}
	 * 		{@link Collection} as an equivalent class. Marked as <strong>U/(P&cup;Q)</strong>.
	 * @param args
	 * 		Extra arguments.
	 * @return <code>this</code> instance.
	 */
	public FeatureImportance4TengDiscernibilityView<V> calculate(
			Collection<Collection<Instance>> condEquClasses,
			Collection<Collection<Instance>> gainedEquClasses,
			Object...args
	);

	/**
	 * Calculate the outer Significance by <i>Relative Discernibility Degree</i>: 
	 * <strong>SIG<sub>dis</sub><sup>outer</sup>(a, P, Q)</strong> = |DIS(Q/P)| - |DIS(Q/P∪{a})|
	 * 
	 * @see #calculate(Collection, Collection, Object...)
	 * @see #calculateOuterSignificance(Collection, Object, IntegerIterator, IntegerIterator, Object...)
	 * 
	 * @param universes
	 * 		An {@link Instance} {@link Collection}.
	 * @param conditionalAttributes
	 * 		Conditional attributes: <strong>P</strong>.
	 * @param outerAttributes
	 * 		Outer attributes: <strong>{a}</strong>.
	 * @param gainedAttributes
	 * 		Gained attributes: <strong>Q</strong>.
	 * @param args
	 * 		Extra arguments.
	 * @return <code>this</code> instance.
	 */
	public FeatureImportance4TengDiscernibilityView<V> calculateOuterSignificance(
			Collection<Instance> universes,
			IntegerIterator conditionalAttributes, IntegerIterator outerAttributes,
			IntegerIterator gainedAttributes, Object...args
	);
	/**
	 * Calculate the outer Significance by <i>Relative Discernibility Degree</i>: 
	 * <strong>SIG<sub>dis</sub><sup>outer</sup>(a, P, Q)</strong> = |DIS(Q/P)| - |DIS(Q/P∪{a})|
	 * 
	 * @see #calculate(Collection, Collection, Object...)
	 * 
	 * @param condEquClasses
	 * 		Equivalent Classes partiton by conditional attributes: <strong>U/P</strong>
	 * @param disB4Gain
	 * 		The value of Relative Discernibility Degree of U/P gaining Q: <strong>|DIS(Q/P)|</strong>.
	 * @param outerAttributes
	 * 		Outer attributes: <strong>{a}</strong>.
	 * @param gainedAttributes
	 * 		Gained attributes: <strong>Q</strong>.
	 * @param args
	 * 		Extra arguments.
	 * @return <code>this</code> instance.
	 */
	public FeatureImportance4TengDiscernibilityView<V> calculateOuterSignificance(
			Collection<Collection<Instance>> condEquClasses, V disB4Gain,
			IntegerIterator outerAttributes, IntegerIterator gainedAttributes, 
			Object... args
	);
}