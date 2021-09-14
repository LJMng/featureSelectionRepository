package featureSelection.repository.entity.alg.rec.nestedEC.impl.nestedEquivalenceClassesMerge.params;

import java.util.Map;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMerger;
import featureSelection.repository.entity.alg.rec.nestedEC.interf.nestedEquivalenceClassesMerge.NestedEquivalenceClassesMergerParameters;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Full name: DefaultNestedEquivalentClassesMergerParameters (Default Nested Equivalent Classes
 * Merger Parameters)
 * <p>
 * Default parameters for {@link NestedEquivalenceClassesMerger} to merge 2
 * {@link NestedEquivalenceClass}es.
 * <p>
 * In the merging, {@link NestedEquivalenceClass} with less element will be adjusted and merged into
 * the other one.
 * <p>
 * Arguments in constructor: 
 * <li><strong>attributeLength</strong>: 
 * 		{@link Instance}'s condition attribute length
 * </li>
 * <li><strong>previousEquClassSrc</strong>: 
 * 		A {@link Map} whose keys are equivalent value in {@link IntArrayKey} and values are 
 * 		{@link EquivalenceClass} for <code>largerNestedEquClass</code>
 * </li>
 * <li><strong>previousNestedEquClass</strong>: 
 * 		Previous {@link NestedEquivalenceClass}
 * </li>
 * <li><strong>arrivedNestedEquClass</strong>: 
 * 		{@link NestedEquivalenceClass} of arrived data
 * </li>
 * <li><strong>previousSig</strong>: 
 * 		The significance of all previous {@link EquivalenceClass}es calculated by the previous reduct.
 * </li>
 */
@Getter
@AllArgsConstructor
public class DefaultNestedEquivalenceClassesMergerParams<Sig extends Number>
	implements NestedEquivalenceClassesMergerParameters
{
	private int attributeLength;

	private Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> previousReductNestedEquClasses;
	private Sig previousSig;

	private NestedEquivalenceClass<EquivalenceClass> previousNestedEquClass;
	private NestedEquivalenceClass<EquivalenceClass> arrivedNestedEquClass;
}
