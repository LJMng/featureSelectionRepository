package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalPartition.procedure.inspect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.action.ComponentExecution;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.incrementalPartition.RoughEquivalenceClassDummy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.PartitionResult;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("deprecation")
@RequiredArgsConstructor
public class RecursionBasedInspectionExecutionAction4IPREC 
	implements ComponentExecution<Boolean>
{
	@NonNull @Getter private AttrProcessStrategy4Comb inspectAttributeProcessStrategy;
	@NonNull private Collection<EquivalenceClass> equClasses;

	@Getter private Collection<Integer> reduct;
	
	@Override
	public Boolean exec(ProcedureComponent<?> component, Object...parameters) throws Exception {
		Collection<Integer> red = null, redundant = null;
		// Initiate pointer: begin = 0, end = m-1
		int[] attributeGroup;
		int hashKeyCapacity = equClasses.size();
		PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>> partitionResult;
		// Loop over all attribute groups
		while (inspectAttributeProcessStrategy.hasNext()) {
			// Extract attributes from begin to end: B as attributeGroup
			Collection<Integer> tmp1 = redundant;
			attributeGroup = redundant==null || redundant.isEmpty()?
								inspectAttributeProcessStrategy.getInLineAttr():
								// Loop over in-line attributes and collect ones that are not redundant.
								Arrays.stream(inspectAttributeProcessStrategy.getInLineAttr())
										.filter(a->!tmp1.contains(a))
										.toArray();
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
			// Use attributes in attributeGroup to do 1st round of partitioning: U/(C-B), one by one.
			partitionResult =
					RoughEquivalenceClassBasedExtensionAlgorithm
							.IncrementalPartition
							.Basic
							.inTurnIncrementalPartition(
									equClasses,
									new IntegerArrayIterator(attributeGroup)
							);
			// if 0-REC is empty
			if (partitionResult.isEmptyBoundaryClassSetTypeClass()) {
				if (partitionResult.getAttributes().size()==1) {
					reduct = new ArrayList<>(1);
					reduct.add(partitionResult.getAttributes().iterator().next());
					return false;
				}else {
					// The rest of the attributes are redundant, inspect the rest of the
					//  attributes by recursive actions.
					inspectAttributeProcessStrategy.initiate(
						// rest of the attributes
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
				int[] examingLine;
				AttributeLoop:
				while (inspectAttributeProcessStrategy.hasNextExamAttribute()) {
					Collection<Integer> tmp2 = redundant;
					// Select an attribute for redundancy inspection.
					attr = inspectAttributeProcessStrategy.getExamingAttr();
					examingLine = tmp2==null || tmp2.isEmpty()?
									inspectAttributeProcessStrategy.getExamingLineAttr():
									Arrays.stream(inspectAttributeProcessStrategy.getExamingLineAttr())
										.filter(a->!tmp2.contains(a))	// filter redundants
										.toArray();

					if (examingLine.length!=0) {
						for (RoughEquivalenceClassDummy roughClass:
								partitionResult.getRoughClasses()
						) {
							// If |0-REC|!=0, the attribute is required to eliminate
							//  the 0-REC => not redundant.
							if (RoughEquivalenceClassBasedExtensionAlgorithm
									.IncrementalPartition
									.Basic
									.boundarySensitiveRoughEquivalenceClass(
											roughClass.getItems(),
											new IntegerArrayIterator(examingLine),
											hashKeyCapacity
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
					}else{
						// not redundant.
						red.add(attr);
					}
					red.remove(attr);
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
