package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination;

import common.utils.StringUtils;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.core.attributeCombination.CapacityCalculator;
import org.apache.commons.math3.util.FastMath;

import lombok.Data;
import lombok.Getter;

/**
 * Full name: AttributeProcessStrategy4Combination (Attribute Process Strategy 4 Combination)
 * <p>
 * Implemented {@link AttributeProcessStrategy} for Attributes combination outputs.
 * <p>
 * Initiate <strong>all fields</strong> as soon as <code>this</code> is constructed: 
 * <ul>
 * 	<li>attributes in line (<code>inLineAttr</code>)</li>
 * 	<li>examining attributes in line (<code>examingLineAttr</code>)</li>
 * 	<li>examining attribute (<code>examingAttr</code>)</li>
 * </ul>
 * <p>
 * Supposed the number of examined attributes, <code>in-line attributes</code> are <i>m</i>, <i>n</i> 
 * respectively(i.e. the total number of attributes participates in calculator is <i>m+n+1</i>). The No. 
 * of them are <i>1~m</i>, <i>m+1~n</i>, <i>n+1</i>. We get a source attribute set: <code>P={1,...,m, m+1, 
 * ..., n, n+1}</code>. 
 * <p>
 * This processor iterates the source attributes sequentially and generate examined attribute for each
 * source attributes. <strong>The main idea is to iterate all source attributes as the examined 
 * attribute</strong>. That's what this class is for.
 * <p>
 * For example, to examine attribute <i>n+1</i>, <code>in-line attributes</code> and <code>examined 
 * attributes</code> are <code>{1, .., m}</code> and <code>{m+1, ..., n}</code>. 
 * For the next combination, to examine the next attribute which has not been examined, an attribute in 
 * examined attributes is extracted and switches with the previous examined attribute(e.g. <i>n+1</i>). 
 * Attributes in examined attributes and examined attributes(e.g. <i>n~n+1</i>) will not be updated until 
 * all of them has been examined. 
 * And to update them, a maximum size of <code>capacity</code> attributes which have not been examined 
 * would switch with examined attributes. 
 * The above process will not stop iterating until all attributes have been examined.
 * 
 * @see AttrProcessStrategy4CombInReverse
 * 
 * @author Benjamin_L
 */
@Data
public class AttrProcessStrategy4Comb 
	implements AttributeProcessStrategy
{
	public static final String PARAMETER_EXAM_CAPACITY_CALCULATOR = "examCapacityCalculator";
	
	/**
	 * Length of attributes. (Not the original one, the value is <code>endExclusive</code> - 
	 * <code>startInclusive</code>)
	 */
	@Getter protected int allLength;
	/**
	 * Attributes in line.
	 */
	@Getter protected int[] inLineAttr;
	/**
	 * Attributes in line to be examined.
	 */
	protected int[] examingLineAttr;
	/**
	 * The attribute that is being examined now.
	 */
	protected int examingAttr;
	/**
	 * Examined attributes capacity. 
	 * Value = |<code>examingLineAttr</code>| + |<code>examingAttr</code>| 
	 * 		 = |<code>examingLineAttr</code>| + 1
	 */
	protected int examCapacity;
	/**
	 * Number of attributes that has been examined already. (0~<code>allLength</code>)
	 */
	protected int processCount;
	/**
	 * Number of examined attributes. (0~<code>examingLineAttr.length</code>)
	 */
	protected int exchangeCount;
	
	@Getter protected IntegerIterator sourceAttr;
	protected CapacityCalculator examCapacityCalculator;
	
	public AttrProcessStrategy4Comb(AttrProcessStrategyParams params) {
		examCapacityCalculator = params.get(PARAMETER_EXAM_CAPACITY_CALCULATOR);
	}
	
	/**
	 * Reset the Attribute Process and prepare for new process. Settings will be reset and updated 
	 * based on the given parameters. Updated settings includes:
	 * <ul>
	 * 	<li>{@link #processCount}</li>
	 * 	<li>{@link #exchangeCount}</li>
	 * 	<li>{@link #allLength}</li>
	 * 	<li>{@link #examCapacity}
	 * 		<p>{@link #examCapacityCalculator} and the size of {@link #sourceAttr} are used to calculate
	 * 		the value of {@link #examCapacity}:
	 * 		<pre>this.examCapacity = FastMath.min(examCapacityCalculator.compute(this), sourceAttr.size());</pre>
	 * 	</li>
	 * 	<li><code>{@link #sourceAttr}.reset()</code></li>
	 * 	<li>{@link #examingAttr}</li>
	 * 	<li>{@link #examingLineAttr}</li>
	 * 	<li>{@link #inLineAttr}</li>
	 * </ul>
	 * 
	 * @param startInclusive
	 * 		Start index of the attributes. (inclusive).
	 * @param endExclusive
	 * 		End index of the attributes. (exclusive).
	 * @return <code>this</code> {@link AttrProcessStrategy4Comb}.
	 */
	public void reset(int startInclusive, int endExclusive) {
		processCount = 0;
		exchangeCount = 0;
		allLength = endExclusive - startInclusive;

		this.examCapacity = FastMath.min(examCapacityCalculator.compute(this), sourceAttr.size());
		
		sourceAttr.reset();
		if (startInclusive!=0)	sourceAttr.skip(startInclusive);
		examingAttr = sourceAttr.next();
		
		examingLineAttr = new int[examCapacity-1];
		for (int i=0; i<examingLineAttr.length; i++)	examingLineAttr[i] = sourceAttr.next();
		inLineAttr = new int[allLength-examCapacity];
		for (int i=0; i<inLineAttr.length; i++)			inLineAttr[i] = sourceAttr.next();
	}
	
	/**
	 * Initiate current instance and prepare for the given next attributes with no changes on
	 * parameters nor settings.
	 * <p>
	 * PS: Calling this function, {@link #reset(int, int)} will be called automatically to reset.
	 * 
	 * @param attributes
	 * 		{@link IntegerIterator} instance with attributes.
	 * @return {@link AttrProcessStrategy4Comb}.
	 */
	@Override
	public AttrProcessStrategy4Comb initiate(IntegerIterator attributes) {
		this.sourceAttr = attributes;
		reset(0, attributes.size());
		return this;
	}
	
	/**
	 * How many attributes left to be process/examined.
	 */
	@Override
	public int left() {
		return allLength-processCount;
	}

	/**
	 * Whether all attributes have been processed/examined. i.e. <code>left()>0</code>
	 */
	@Override
	public boolean hasNext() {
		return left()>0;
	}

	/**
	 * <strong>Unimplemented method...</strong>
	 */
	@Override
	public int[] next() {
		throw new RuntimeException("Unimplemented method!");
	}

	/**
	 * Update attributes in line, to prepare for the next attributes group to be examined.
	 * <p>
	 * <strong>NOTICE</strong>: Call <strong>skipExamAttributes()</strong> if any attribute at 
	 * the current group left to be examined.
	 * 
	 * @see #skipExamAttributes()
	 */
	public void updateInLineAttrs() {
		int left=left();
		if (left>examCapacity) {
			int tmp = examingAttr;
			examingAttr = inLineAttr[processCount-1];
			inLineAttr[processCount-1] = tmp;
			exchange(processCount-examCapacity);
			
			exchangeCount = 0;
		}else if (left>0) {
			int[] newInLineAttr = new int[allLength-left];
			for (int i=0; i<newInLineAttr.length-examCapacity; i++)
				newInLineAttr[i] = inLineAttr[i];
			for (int i=0; i<examingLineAttr.length; i++)
				newInLineAttr[newInLineAttr.length-examCapacity+i] = examingLineAttr[i];
			newInLineAttr[newInLineAttr.length-1] = examingAttr;
			
			examingAttr = inLineAttr[inLineAttr.length-1];
			if (left>1) {
				examingLineAttr = new int[left-1];
				for (int i=0; i<examingLineAttr.length; i++)
					examingLineAttr[i] = inLineAttr[processCount-examCapacity+i];
			}else {
				examingLineAttr = new int[0];
			}

			inLineAttr = newInLineAttr;
			exchangeCount = 0;
		}
	}

	/**
	 * Whether more attributes in the current group have yet to be examined.
	 * 
	 * @return <code>true</code> if more to be examined.
	 */
	public boolean hasNextExamAttribute() {
		return exchangeCount<examCapacity;
	}
	
	/**
	 * Update for the next attribute in examining line (if more attributes left to be examined in line)
	 * 
	 * @see #hasNextExamAttribute()
	 * @see #updateExamAttribute()
	 * @see #skipExamAttributes(int)
	 */
	public void updateExamAttribute() {
		if (!hasNextExamAttribute())
			throw new RuntimeException("Exchange index out of bound: "+exchangeCount);//*/
		
		int result = examingAttr;
		/*System.out.println("result:"+result);
		System.out.println("examingLineAttr:"+Arrays.toString(examingLineAttr));
		System.out.println("inLineAttr:"+Arrays.toString(inLineAttr));//*/
		if (exchangeCount<examingLineAttr.length) {
			examingAttr = examingLineAttr[exchangeCount];
			examingLineAttr[exchangeCount] = result;
		}
		
		processCount++;
		exchangeCount++;
	}
	
	/**
	 * Skip current group of <code>Examing Attributes</code>. 
	 * <pre>
	 * 	int left = examCapacity-exchangeCount;
	 * 	skipExamAttributes(left);
	 * </pre>
	 * 
	 * @see #skipExamAttributes(int)
	 */
	public void skipExamAttributes() {
		skipExamAttributes(examCapacity-exchangeCount);
	}
	
	/**
	 * Skip current group of <code>Examing Attributes</code>: 
	 * <pre>
	 * 	processCount+=skip;
	 * </pre>
	 * 
	 * @param skip
	 * 		Times to skip.
	 */
	public void skipExamAttributes(int skip) {
		if (skip>examCapacity-exchangeCount)
			throw new RuntimeException("Illegal skip: "+skip+", maximum is "+(examCapacity-exchangeCount));
		processCount+=skip;
		exchangeCount+=skip;
	}
	
	/**
	 * Exchange <code>inLineAttr</code> & <code>examingLineAttr</code>: 
	 * <p>
	 * Exchanging attributes from <code>[inlineStart]</code>(<strong>inclusive</strong>) to 
	 * <code>[inlineStart+examingLineAttr.length]</code>(<strong>exclusive</strong>)
	 * 
	 * @param inlineStart
	 * 		<code>inLineAttr</code> start index for exchanging.
	 */
	private void exchange(int inlineStart) {
		int tmp;
		for (int i=0; i<examingLineAttr.length; i++) {
			tmp = examingLineAttr[i];
			examingLineAttr[i] = inLineAttr[inlineStart+i];
			inLineAttr[inlineStart+i] = tmp;
		}
	}

	/**
	 * Get the length of attributes. (Not the original one, the value is <code>endExclusive</code> - 
	 * <code>startInclusive</code>, i.e. <code>allLength</code>)
	 */
	@Override
	public int attributeLength() {
		return allLength;
	}
	
	@Override
	public String shortName() {
		return "AttributeCombinations";
	}

	public String toString() {
		return String.format("AttributeProcessStrategy4Combination [capacity=%d, examing=%d + %s, line=%s]", 
					examCapacity, examingAttr, 
					StringUtils.intToString(examingLineAttr, 50),
					StringUtils.intToString(inLineAttr, 50)
				);
	}
}