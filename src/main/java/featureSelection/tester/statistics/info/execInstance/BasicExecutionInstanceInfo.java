package featureSelection.tester.statistics.info.execInstance;

import java.util.Collection;

import featureSelection.basic.model.universe.instance.Instance;
import lombok.Getter;

/**
 * An entity for the basic info. of executed instances.
 * <p>
 * Info. of <strong>instance</strong> includes:
 * <ul>
 * 	<li>{@link #previousInstanceNumber}</li>
 * 	<li>{@link #currentInstanceNumber}</li>
 * 	<li>{@link #compressedInstanceNumber}</li>
 * 	<li>{@link #executedRecordNumber}</li>
 * </ul>
 * Info. of <strong>attributes</strong> includes:
 * <ul>
 * 	<li>{@link #previousConditionalAttributeNumber}</li>
 * 	<li>{@link #currentConditionalAttributeNumber}</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Getter
public class BasicExecutionInstanceInfo {
	/**
	 * The number of <i>instance</i> which are in <strong>previous</strong> universe
	 * instances as opposed to those newly arrived in streaming executions.
	 * <p>
	 * · For static data execution, this statistic is irrelevant and is recommended to be
	 * 		set into 0.
	 */
	private int previousInstanceNumber;
	/**
	 * The number of <i>instance</i> which are in <strong>current</strong> universe instances.
	 * <p>
	 * · For static data execution, this is the number of the universe instances inputed.
	 * <p>
	 * · For streaming data execution, this is the number of the universe instances inputed/
	 * 		arrived.
	 */
	private int currentInstanceNumber;
	/**
	 * The number of <i>record</i>(e.g. instances, equivalence classes, ...) originated from 
	 * <strong>current</strong> universe instances after specific algorithm <strong>compression
	 * </strong>.
	 * <p>
	 * If no compressing strategy is provided in the specific algorithm, the number should
	 * equal to {@link #currentInstanceNumber}
	 */
	private int compressedInstanceNumber;
	/**
	 * The number of <i>record</i>(e.g. instances, equivalence classes, ...) which actually 
	 * <strong>involved</strong> in the execution.
	 */
	private int executedRecordNumber;
	private Class<?> executedRecordClass;
	
	/**
	 * The number of <i>conditional attributes</i> which are in <strong>previous</strong>
	 * executions as opposed to those newly arrived in streaming executions.
	 * <p>
	 * · For static data execution, this statistic is irrelevant and is recommended to be set
	 * 		into 0.
	 */
	private int previousConditionalAttributeNumber;
	/**
	 * The number of <i>conditional attributes</i> in <strong>current</strong> execution as
	 * opposed to those in the previous one.
	 * <p>
	 * · For static data execution, this is the number of the conditional attributes of
	 * 		universe instances.
	 * <p>
	 * · For streaming data execution, this is the number of the conditional attributes
	 * 		inputed/arrived.
	 */
	private int currentConditionalAttributeNumber;
	
	private BasicExecutionInstanceInfo(
		int previousInstanceNumber, int currentInstanceNumber, int compressedInstanceNumber, 
		int executedRecordNumber, Class<?> executedRecordClass,
		int previousConditionalAttributeNumber, int currentConditionalAttributeNumber
	) {
		this.previousInstanceNumber = previousInstanceNumber;
		this.currentInstanceNumber = currentInstanceNumber;
		this.compressedInstanceNumber = compressedInstanceNumber;
		this.executedRecordNumber = executedRecordNumber;
		this.executedRecordClass = executedRecordClass;
		this.previousConditionalAttributeNumber = previousConditionalAttributeNumber;
		this.currentConditionalAttributeNumber = currentConditionalAttributeNumber;
	}

	/**
	 * A builder for {@link BasicExecutionInstanceInfo}
	 * 
	 * @author Benjamin_L
	 */
	public static class Builder {
		private int previousInstanceNumber = 0;
		private int currentInstanceNumber = 0;
		private int compressedInstanceNumber = 0;
		private int executedRecordNumber = 0;
		private Class<?> executedRecordClass = null;
		
		private int previousConditionalAttributeNumber = 0;
		private int currentConditionalAttributeNumber = 0;
		
		private Builder() {}
		public static Builder newBuilder() {	return new Builder();	}

		public Builder setPreviousInstanceNumber(int num) {
			this.previousInstanceNumber = num;
			return this;
		}
		public Builder setCurrentInstanceNumber(int num) {
			this.currentInstanceNumber = num;
			return this;
		}
		public Builder setCompressedInstanceNumber(int num) {
			this.compressedInstanceNumber = num;
			return this;
		}
		public Builder setExecutedRecordNumberNumber(int num, Class<?> executedRecordClass) {
			this.executedRecordNumber = num;
			this.executedRecordClass = executedRecordClass;
			return this;
		}
		
		public Builder setPreviousConditionalAttributeNumber(int num) {
			this.previousConditionalAttributeNumber = num;
			return this;
		}
		public Builder setCurrentConditionalAttributeNumber(int num) {
			this.currentConditionalAttributeNumber = num;
			return this;
		}
		
		/**
		 * This method loads {@link #currentInstanceNumber},
		 * {@link #currentConditionalAttributeNumber}:
		 * <pre>loadCurrentInfo(Instances, true)</pre>
		 * 
		 * @see #loadCurrentInfo(Collection, boolean)
		 * 
		 * @param instances
		 * 		A {@link Collection} of current {@link Instance}.
		 * @return <code>this</code>
		 */
		public Builder loadCurrentInfo(Collection<Instance> instances) {
			return loadCurrentInfo(instances, true);
		}
		/**
		 * Load {@link #currentInstanceNumber} &amp; {@link #currentConditionalAttributeNumber}.
		 * <p>
		 * if <code>executeAllInstance</code>, {@link #executedRecordNumber} is set to the
		 * value that is the same as {@link #currentInstanceNumber}.
		 * 
		 * @param Instances
		 * 		A {@link Collection} of current {@link Instance}.
		 * @param executeAllInstance
		 * 		Whether the execution involves all instances. If <code>true</code>, 
		 * 		{@link #executedRecordNumber} will be set to the value that is the same as 
		 * 		{@link #currentInstanceNumber}.
		 * @return <code>this</code>
		 */
		public Builder loadCurrentInfo(Collection<Instance> Instances, boolean executeAllInstance) {
			this.setCurrentInstanceNumber(Instances.size());
			this.setCurrentConditionalAttributeNumber(
					Instances.iterator().next().getAttributeValues().length-1
			);

			if (executeAllInstance) {
				setExecutedRecordNumberNumber(Instances.size(), Instance.class);
			}

			return this;
		}
		
		
		/**
		 * Build an instance of {@link BasicExecutionInstanceInfo}.
		 * 
		 * @return {@link BasicExecutionInstanceInfo} instance.
		 */
		public BasicExecutionInstanceInfo build() {
			return new BasicExecutionInstanceInfo(
					previousInstanceNumber, currentInstanceNumber, compressedInstanceNumber, 
					executedRecordNumber, executedRecordClass,
					previousConditionalAttributeNumber, currentConditionalAttributeNumber
				);
		}
	}
}