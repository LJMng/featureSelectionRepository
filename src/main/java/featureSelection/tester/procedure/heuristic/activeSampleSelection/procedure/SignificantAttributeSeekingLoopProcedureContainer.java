package featureSelection.tester.procedure.heuristic.activeSampleSelection.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.entity.alg.activeSampleSelection.AttrDiscernibilityResult;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePair;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePairAttributeInfo;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Most significant attribute searching for <strong>Sample Pair Selection</strong> for
 * Attribute Reduction (for <strong>Static Data</strong>).
 * This procedure contains 3 ProcedureComponents: 
 * <ul>
 * 	<li>
 * 		<strong>Loop controller</strong>
 * 		<p>Loop controller. To control the looping and exit of the searching: exit if DIS(A) is empty.
 * 	</li>
 * 	<li>
 * 		<strong>Select the attribute with the maximum sample pair number in DIS({a})</strong>
 * 		<p>Select the attribute with the maximum sample pair number/frequency in DIS({a}) as the one
 * 	    	to add into reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Update reduct and DIS(A)</strong>
 * 		<p>Update <code>reduct</code> and delete the attribute selected and correspondent info. in
 * 	    	DIS(A).
 * 	</li>
 * </ul>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>allAttrDiscernibility</li>
 * 	<li>attrDiscernibilityResult</li>
 * 	<li>samplePairAttributesInfo</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Slf4j
public class SignificantAttributeSeekingLoopProcedureContainer
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	
	private int loopCount = 0;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public SignificantAttributeSeekingLoopProcedureContainer(
			ProcedureParameters parameters, boolean logOn
	) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Sig seeking loop";
	}

	@Override
	public String staticsName() {
		return shortName();
	}
	
	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Loop controller.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info("1. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("allAttrDiscernibility"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p = 0;
						List<SamplePair> allAttrDiscernibility =
								(List<SamplePair>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp1 = getComponents().get(1);
						ProcedureComponent<?> comp2 = getComponents().get(2);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> reduct = new LinkedList<>();
						
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("reduct", reduct);
						TimerUtils.timeContinue((TimeCounted) component);
						
						while (allAttrDiscernibility!=null &&
								!allAttrDiscernibility.isEmpty()
						) {
							loopCount++;

							TimerUtils.timePause((TimeCounted) component);
							comp1.exec();
							comp2.exec();
							TimerUtils.timeContinue((TimeCounted) component);
						}				
						return reduct;
					}, 
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(1, "|red| = {}"),
									reduct.size()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								reduct
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Loop controller"),
			// 2. Select the attribute with the maximum sample pair number in DIS({a}).
			new TimeCountedProcedureComponent<Integer>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}."),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("allAttrDiscernibility"),
								getParameters().get("attrDiscernibilityResult"),
								getParameters().get("samplePairAttributesInfo"),
							});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						List<SamplePair> allAttrDiscernibility =
								(List<SamplePair>) parameters[p++];
						AttrDiscernibilityResult attrDiscernibilityResult =
								(AttrDiscernibilityResult) parameters[p++];
						Map<SamplePair, SamplePairAttributeInfo> samplePairAttributesInfo =
								(Map<SamplePair, SamplePairAttributeInfo>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Select the 1st sample pair in DIS(A).
						SamplePair samplePair = allAttrDiscernibility.get(0);
						// collect attributes a, for any DIS({a}) contains (i, j)
						Collection<Integer> samplePairAttributeIndexes = samplePairAttributesInfo.get(samplePair).getAttributeIndexes();
						// Select the attribute with the maximum sample pair number in DIS({a}).
						int maxIndex =
								samplePairAttributeIndexes.stream().max(
										(spa1, spa2)->
											attrDiscernibilityResult.getDiscernibilities()[spa1].size()-
											attrDiscernibilityResult.getDiscernibilities()[spa2].size()
								).get();
						return maxIndex;
					}, 
					(component, maxIndex) -> {
						/* ------------------------------------------------------------------------------ */
						localParameters.put("maxIndex", maxIndex);
						/* ------------------------------------------------------------------------------ */
						// Statistic
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Select the attribute with the maximum sample pair number in DIS({a})"),
			// 3. Update reduct and DIS(A).
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "Loop {} | 3/{}. {}."),
									loopCount, getComponents().size(),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								localParameters.get("maxIndex"),
								localParameters.get("reduct"),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get("attrDiscernibilityResult"),
								getParameters().get("allAttrDiscernibility"),
							});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int maxIndex =
								(int) parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						AttrDiscernibilityResult attrDiscernibilityResult = 
								(AttrDiscernibilityResult) parameters[p++];
						List<SamplePair> allAttrDiscernibility =
								(List<SamplePair>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						reduct.add(attributes[maxIndex]);
						// DIS(A) = DIS(A)-DIS({a*}).
						allAttrDiscernibility.removeAll(
								attrDiscernibilityResult.getDiscernibilities()[maxIndex]
						);
						return null;
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistic
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update reduct and DIS(A)"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (Collection<Integer>) componentArray[0].exec();
	}

	public String reportMark() {
		return "Loop["+loopCount+"]";
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}