package featureSelection.repository.entity.alg.directDependencyCalculation;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import lombok.Getter;

/**
 * A <code>grid record</code> as a line in <code>grid</code> for Direct Dependency Calculation Algorithm.
 * 
 * @author Benjamin_L
 */
public class GridRecord {
	@Getter private IntArrayKey conditionalAttributes;
	@Getter private final int decisionClass;
	@Getter private int instanceCount;
	private boolean classStatus;					// true:0-Unique, false:1-Non-Unique
	
	public GridRecord(int[] attributeValues, int decisionClass) {
		if (attributeValues!=null)	this.conditionalAttributes = new IntArrayKey(attributeValues);
		this.decisionClass = decisionClass;
		this.instanceCount = 1;
		this.classStatus = true;
	}
	
	public GridRecord addInstanceCount() {
		this.instanceCount++;
		return this;
	}
	
	/**
	 * Set the {@link #classStatus} of the grid record: 0 as unique.
	 * 
	 * @param unique
	 * 		The status to be set. true : 0 as unique, false : 1 as non-unique.
	 * @return <code>this</code>.
	 */
	public GridRecord setClassStatus(boolean unique) {
		this.classStatus = unique;
		return this;
	}
	
	/**
	 * Return if {@link #classStatus} is 0, i.e. unique.
	 * 
	 * @return true if unique.
	 */
	public boolean unique() {
		return classStatus;
	}
	
	@Override
	public String toString() {
		return "GridRecord [conditionalAttributes=" + conditionalAttributes + ", decisionClass=" + decisionClass
				+ ", instanceCount=" + instanceCount + ", classStatus=" + classStatus + "]";
	}
}