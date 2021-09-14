package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure.inspect;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import common.utils.ArrayCollectionUtils;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.action.ComponentExecution;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.PartitionResult;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4IPNEC;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A {@link ComponentExecution} for IP-NEC recursion based reduct inspection execution.
 *
 * @see NestedEquivalenceClassBasedAlgorithm.IncrementalPartition.Inspection#computeEquivalenceClasses(
 *      AttrProcessStrategy4Comb, Collection)
 */
@RequiredArgsConstructor
public class RecursionBasedInspectionExecutionAction4IPNEC 
	implements ComponentExecution<Boolean>
{
	@NonNull @Getter private AttrProcessStrategy4Comb inspectAttributeProcessStrategy;
	@NonNull @Getter private Collection<EquivalenceClass> equClasses;
	
	@Getter private Collection<Integer> reduct;
	
	@Override
	public Boolean exec(ProcedureComponent<?> component, Object... paramaters) throws Exception {
		// If contains 1 attribute only, no need to inspect
		if (inspectAttributeProcessStrategy.getAllLength()==1) {
			reduct = Arrays.asList(inspectAttributeProcessStrategy.getExamingAttr());
			return false;
		}
		Collection<Integer> red = null, redundant = null;
		// Initiate pointer: begin = 0, end = m-1
		int[] attributeGroup;
		PartitionResult<Collection<Integer>, Collection<NestedEquivalenceClass<EquivalenceClass>>> partitionResult;
		// Loop over all attribute groups
		while (inspectAttributeProcessStrategy.hasNext()) {
			Collection<Integer> tmp1 = redundant;
			// Extract attributes from begin to end: B as attributeGroup
			if (redundant==null || redundant.isEmpty()) {
				// no redundant attributes yet, extract directly.
				attributeGroup = inspectAttributeProcessStrategy.getInLineAttr();
			}else {
				// need to check if redundant attributes in extracted group.
				Collection<Integer> examingLineAttrs = new LinkedList<>();
				// Loop over in-line attributes and collect ones that are not redundant.
				for (int each: inspectAttributeProcessStrategy.getInLineAttr()) {
					if (!tmp1.contains(each)){
						examingLineAttrs.add(each);
					}
				}
				// if |examing line attributes|==|in-line attributes|: all not redundant
				//  set attributeGroup = in-line attributes directly
				// else: has redundant attributes
				//  set attributeGroyp = in-line attributes without redundant ones.
				attributeGroup =
						examingLineAttrs.size()==inspectAttributeProcessStrategy.getInLineAttr().length?
								Arrays.copyOf(
										inspectAttributeProcessStrategy.getInLineAttr(),
										inspectAttributeProcessStrategy.getInLineAttr().length
								):
								ArrayCollectionUtils.getIntArrayByCollection(examingLineAttrs);
			}
			// If no non-redundant attributes in attributeGroup
			if (attributeGroup.length==0) {
				// Collect attributes left in examing line attributes, plus the examing
				//  attribute.
				int[] left = Arrays.copyOf(
								inspectAttributeProcessStrategy.getExamingLineAttr(), 
								inspectAttributeProcessStrategy.getExamingLineAttr().length+1
						);
				left[left.length-1] = inspectAttributeProcessStrategy.getExamingAttr();
				
				inspectAttributeProcessStrategy.initiate(new IntegerArrayIterator(left));
				return true;
			}
			// Use attributes in attributeGroup to do 1st round of partitioning: U/(C-B),
			//  one by one.
			partitionResult = PositiveRegionCalculation4IPNEC
								.inTurnPartition(
									equClasses, 
									new IntegerArrayIterator(attributeGroup)
								);
			// if 0-NEC is empty
			if (partitionResult.isEmptyBoundaryClassSetTypeClass()) {
				if (partitionResult.getAttributes().size()==1) {
					reduct = partitionResult.getAttributes();
					return false;
				}else {
					// The rest of the attributes are redundant, inspect the rest of the
					//  attributes by recursively compute().
					inspectAttributeProcessStrategy.initiate(
						new IntegerCollectionIterator(partitionResult.getAttributes())
					);
					return true;
				}
			}else {
				// Attributes used in partitioning are not enough.
				// Loop over the rest of attributes(that are not in the attributes used in
				//  the 1st round partitioning).
				if (red==null) {
					red = new HashSet<>(inspectAttributeProcessStrategy.getAllLength());
					red.addAll(partitionResult.getAttributes());
				}
				
				int attr;
				IntegerIterator examingLine;
				AttributeLoop:
				while (inspectAttributeProcessStrategy.hasNextExamAttribute()) {
					Collection<Integer> tmp2 = redundant;
					// Select an attribute for redundancy inspection.
					attr = inspectAttributeProcessStrategy.getExamingAttr();
					if (tmp2==null || tmp2.isEmpty()) {
						examingLine = new IntegerArrayIterator(
									inspectAttributeProcessStrategy.getExamingLineAttr()
								);
					}else {
						// Filter redundant attributes in examing line attributes.
						Collection<Integer> examingLineAttrs = new LinkedList<>();
						for (int each: inspectAttributeProcessStrategy.getExamingLineAttr()) {
							// filter redundant
							if (!tmp2.contains(each))	examingLineAttrs.add(each);
						}
						examingLine = new IntegerCollectionIterator(examingLineAttrs);
					}
					
					if (examingLine.size()!=0) {
						// Loop over attributes in examing line to partition
						for (NestedEquivalenceClass<EquivalenceClass> necEquClass:
								partitionResult.getRoughClasses()
						) {
							// If |0-REC|!=0, the attribute is required to eliminate
							//  the 0-REC => not redundant.
							if (NestedEquivalenceClassBasedAlgorithm
									.IncrementalPartition
									.Basic
									.boundarySensitiveNestedEquivalenceClass(
										necEquClass.getEquClasses().values(), 
										examingLine,
										necEquClass.getItemSize()
									)==null
							) {
								red.add(attr);
								// Inspect the next attribute in examing attribute line.
								inspectAttributeProcessStrategy.updateExamAttribute();
								continue AttributeLoop;
							}
						}
						// Used all examing line attributes and still no 0-REC occur,
						//  then the current examing attribute is redundant.
						if (redundant==null){
							redundant = new HashSet<>(inspectAttributeProcessStrategy.getAllLength()-1);
						}
						redundant.add(attr);
					}else {
						// not redundant.
						red.add(attr);
					}
					// Inspect the next attribute in examing attribute line.
					inspectAttributeProcessStrategy.updateExamAttribute();
				}
				// Update pointers:
				//  begin=end
				//  end = i+1<g? end+m: |C|-1
				inspectAttributeProcessStrategy.updateInLineAttrs();
			}
		}
		// remove all redundant attributes from reduct.
		if (redundant!=null && !redundant.isEmpty()){
			red.removeAll(redundant);
		}
		reduct = red;
		return false;
	}
}