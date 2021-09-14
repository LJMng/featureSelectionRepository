package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.action.ComponentExecution;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedAlgorithm;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.incrementalPartition.RoughEquivalenceClassDummy;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.PartitionResult;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.Shrink4RECBoundaryClassSetStays;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A {@link ComponentExecution} for IP-NEC recursion based core execution.
 *
 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition.Core.RecursionBased#compute(
 *      AttrProcessStrategy4Comb, AttributeProcessStrategy, Collection, Shrink4RECBoundaryClassSetStays)
 */
@SuppressWarnings("deprecation")
@RequiredArgsConstructor
public class RecursionBasedCoreExecutionAction4IPNEC 
	implements ComponentExecution<Boolean>
{
	@NonNull @Getter private Collection<Integer> core;
	@NonNull @Getter private Collection<Integer> attributesFiltered;
	@NonNull @Getter private AttrProcessStrategy4Comb coreAttributeProcessStrategy;
	@NonNull @Getter private AttributeProcessStrategy incPartitionAttributeProcessStrategy;
	@NonNull private Collection<EquivalenceClass> equClasses;
	@NonNull private Shrink4RECBoundaryClassSetStays shrinkInstance;
	
	@Override
	public Boolean exec(ProcedureComponent<?> component, Object...parameters) throws Exception {
		// initiate pointer: begin = 0, end = m-1
		// Loop until all attributes are checked
		int[] attributeGroup;
		PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>> partitionResult;
		while (coreAttributeProcessStrategy.hasNext()) {
			// Extract attributes from begin to end: B
			attributeGroup = coreAttributeProcessStrategy.getInLineAttr();
			// Use the rest of the attributes to do 1st round of partitioning: U/(C-B)
			partitionResult = 
				RoughEquivalenceClassBasedExtensionAlgorithm
					.IncrementalPartition
					.Basic
					.dynamicIncrementalPartition(
							incPartitionAttributeProcessStrategy.initiate(
								new IntegerArrayIterator(attributeGroup)
							), 
							equClasses
					);
			// if |0-REC|=0, attributes of begin~end are NOT core attributes.
			if (partitionResult.isEmptyBoundaryClassSetTypeClass()) {
				// Attributes in B are not Core for sure.
				//	maintain field: attributesFiltered
				//		Initiate attributesFiltered. (done, skip)
				//		Add attributes in examing line
				for (int attr: coreAttributeProcessStrategy.getExamingLineAttr()) {
					attributesFiltered.add(attr);
				}
				//		Add attribute of examining
				attributesFiltered.add(coreAttributeProcessStrategy.getExamingAttr());
				//		Add attributes out of partitionResult.getAttributes().
				Collection<Integer> partitionAttributes = new HashSet<>(partitionResult.getAttributes());
				for (int attr: attributeGroup) {
					if (!partitionAttributes.contains(attr)){
						attributesFiltered.add(attr);
					}
				}
				//	Update coreAttributeProcessStrategy's attributes for execution. 
				coreAttributeProcessStrategy.initiate(
					new IntegerCollectionIterator(partitionResult.getAttributes())
				);
				//	Recursively do compute().
				return true;
			// else U/(C-B) contains 0-NEC
			}else {
				// remove 1-NEC, -1-NEC.
				shrinkInstance.shrink(partitionResult.getRoughClasses());

				Collection<RoughEquivalenceClassDummy> roughClasses;
				if (attributesFiltered!=null && !attributesFiltered.isEmpty()) {
					// [Important] Use not-core-for-sure attributes to partition, otherwise
					//	in Common.traditionalCoreExam4CoreAttributeProcessStrategy(),
					//	it is not C-{a} that being calculated.
					final IntegerIterator filteredAttrItertaor = new IntegerCollectionIterator(attributesFiltered);
					roughClasses = partitionResult.getRoughClasses().stream().flatMap(rough->
						RoughEquivalenceClassBasedExtensionAlgorithm
								.IncrementalPartition
								.Basic
								.dynamicIncrementalPartition(
									incPartitionAttributeProcessStrategy.initiate(filteredAttrItertaor),
									rough.getItems()
								).getRoughClasses().stream()
					).filter(r-> ClassSetType.BOUNDARY.equals(r.getType()))
					.collect(Collectors.toList());
				}else {
					roughClasses = partitionResult.getRoughClasses();
				}

				// Loop over attributes in coreAttributeProcessStrategy.
				int attr;
				int[] examingLineAttr;
				while (coreAttributeProcessStrategy.hasNextExamAttribute()) {
					// next attribute.
					attr = coreAttributeProcessStrategy.getExamingAttr();
					// Use the rest of the attributes to do partitioning
					examingLineAttr = coreAttributeProcessStrategy.getExamingLineAttr();
					for (RoughEquivalenceClassDummy roughClass: roughClasses) {
						// if 0-REC exists.
						if (RoughEquivalenceClassBasedExtensionAlgorithm
								.IncrementalPartition
								.Basic
								.boundarySensitiveRoughEquivalenceClass(
									roughClass.getItems(),
									new IntegerArrayIterator(examingLineAttr),
									equClasses.size()
								)==null
						) {
							// core = core U {a}.
							core.add(attr);
							break;
						}
					}
					// prepare for the next attribute to be checked.
					coreAttributeProcessStrategy.updateExamAttribute();
				}
				// prepare for the next attribute to be checked.
				coreAttributeProcessStrategy.updateInLineAttrs();
			}
		}
		return false;
	}
}
