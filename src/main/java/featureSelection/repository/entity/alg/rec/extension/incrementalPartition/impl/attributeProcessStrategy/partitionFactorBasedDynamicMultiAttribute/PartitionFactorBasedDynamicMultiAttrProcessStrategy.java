package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute;

import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.PartitionFactorBasedGroupNumberCalculator;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.PartitionFactorStrategy;
import lombok.Getter;

/**
 * Full name: PartitionFactorBasedDynamicMultiAttributeProcessStrategy (Partition Factor Based Dynamic
 * Multi-Attribute Process Strategy)
 * <p>
 * <strong>Partition Factor</strong> based attribute process strategy for multi-attributes.
 * <p>
 * Using the given <code>Partition Factor</code> to calculate the size of each attribute 
 * group size: 
 * <pre>
 * 	end = ceil( (|attributes|-attrPointer)/factor -1 );
 * 	size = end-attrPointer +1
 * </pre>
 * where:
 * <ul>
 * 	<li><code>attrPointer</code> is initiated as 0 and always points to the beginning
 * 	(i.e. the 1st attribute's index) of the attribute group.
 * 	<li><code>factor</code> is set by user in the constructor of <code>this</code> instance.
 * 	<li><code>end</code> is the tail (index) of the attribute group.
 * </ul>
 * 
 * @see PartitionFactorBasedGroupNumberCalculator
 * @see DefaultPartitionFactorBasedGroupNumberCalculator
 * @see PartitionFactorStrategy
 * @see PartitionFactorStrategy4FixedFactor
 * @see PartitionFactorStrategy4SqrtAttrNumber
 * 
 * @author Benjamin_L
 */
public class PartitionFactorBasedDynamicMultiAttrProcessStrategy 
	implements AttributeProcessStrategy
{
	public final static String PARAMETER_PARTITION_FACTOR_STRATEGY = "PartitionFactorStrategy";
	public final static String PARAMETER_GROUP_NUMBER_CALCULATOR = "GroupNumberCalculator";
	
	private int attributesSize;
	@Getter private IntegerIterator attributes;
	/**
	 * Calculator for attribute group numbers.
	 */
	private PartitionFactorBasedGroupNumberCalculator groupNumberCalculator;
	/**
	 * Partition Factor strategy for the calculating of {@link #groupNumberCalculator}.
	 */
	private PartitionFactorStrategy partitionFactorStrategy;
	
	public PartitionFactorBasedDynamicMultiAttrProcessStrategy(AttrProcessStrategyParams params) {
		partitionFactorStrategy = params.get(PARAMETER_PARTITION_FACTOR_STRATEGY);
		groupNumberCalculator = params.get(PARAMETER_GROUP_NUMBER_CALCULATOR);
	}
	
	public PartitionFactorBasedDynamicMultiAttrProcessStrategy initiate(
			IntegerIterator attributes
	) {
		this.attributes = attributes.reset();
		this.attributesSize = attributes.size();
		return this;
	}
	
	/**
	 * Get the next attribute group in <code>int[]</code>. 
	 * <p>This method will call {@link #hasNext()} to check if any attribute available.
	 * <p>Using {@link #groupNumberCalculator} to calculate the number of extracted attributes(bases on
	 * {@link #partitionFactorStrategy})
	 * 
	 * @return null if {@link #hasNext()} returns false. / attributes in <code>int[]</code>.
	 */
	@Override
	public int[] next() {
		int extractSize = groupNumberCalculator.calculate(
							this, 
							partitionFactorStrategy.compute(this)
						);
		int[] extract = new int[extractSize];
		for (int i=0; i<extractSize; i++)	extract[i] = attributes.next();
		return extract;
	}

	/**
	 * Get how many attributes left
	 */
	@Override
	public int left() {
		return attributesSize-attributes.currentIndex();
	}

	/**
	 * Get if has next attributes.
	 */
	@Override
	public boolean hasNext() {
		return attributes.hasNext();
	}

	public int attributeLength() {
		return attributes.size();
	}
	
	@Override
	public String shortName() {
		return "PartitionFactor";
	}
}