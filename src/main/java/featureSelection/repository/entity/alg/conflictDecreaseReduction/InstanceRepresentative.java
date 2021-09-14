package featureSelection.repository.entity.alg.conflictDecreaseReduction;

import featureSelection.basic.model.universe.instance.Instance;
import lombok.Getter;

/**
 * An entity extends {@link Instance} as representative.
 * 
 * @author Benjamin_L
 */
public class InstanceRepresentative extends Instance {
	@Getter private int decision;
	
	private static int num_counter = 1;
	private final int num;
	
	public InstanceRepresentative(int[] value) {
		super(value);
		num = num_counter++;
	}

	public InstanceRepresentative setInstanceRep(Instance rep) {
		this.setAttributeValue(rep.getAttributeValues());
		return this;
	}	
	
	public InstanceRepresentative setDecision(int decision) {
		this.decision = decision;
		return this;
	}
	
	public Instance getUniverse() {
		return this;
	}
	
	public String toString() {
		if(this.getAttributeValues()==null)	return "U rep-"+"(Universe-"+getNum()+")"+num;
		
		StringBuilder builder = new StringBuilder();
		builder.append("U rep-"+num+"(Universe-"+getNum()+")"+"	");
		for (int i=1; i<this.getAttributeValues().length; i++) {
			builder.append(this.getAttributeValues()[i]);
			if (i!=this.getAttributeValues().length-1)	builder.append(", ");
		}
		builder.append("	"+"d = "+decision);
		return builder.toString();
	}
}