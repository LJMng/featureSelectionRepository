package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.staticData.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.nestedEC.MostSignificanceResult;
import featureSelection.repository.entity.alg.rec.nestedEC.NestedEquivalenceClassesInfo;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedRealtimeSimpleCountingCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Most significant attribute searching for <strong>(Simple) Nested Equivalence Classes based (S-NEC)
 * </strong> Feature Selection. This procedure contains 3 {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Check if sig(red)==sig(C), if does, break : true-continue, false-break</strong>
 * 		<p>Check if significance of current reduct equals to the global significance(i.e.
 * 			<code>sig(red)==sig(C)</code>). Return <code>true</code> if it does.
 * 	</li>
 * 	<li>
 * 		<strong>Seek the most significant current attribute</strong>
 * 		<p>Search for the attribute with the max. significance value in the rest of the
 * 			attributes(i.e. attributes outside of the reduct), and return as an attribute
 * 			of the reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Update reduct and significance</strong>
 * 		<p>Add the most significant attribute into the reduct and update current reduct's
 * 			significance value.
 * 	</li>
 * </ul>
 * <p>
 * Loop is controlled by {@link #exec()}.
 *
 * @param <Sig>
 *     Type of feature (subset) significance.
 *
 * @author Benjamin_L
 */
@Slf4j
public class SignificantAttributeSeekingLoopProcedureContainer<Sig extends Number>
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
	
	public SignificantAttributeSeekingLoopProcedureContainer(ProcedureParameters parameters, boolean logOn) {
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
			// 1. Controller.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 1/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<MostSignificanceResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Sig>> comp1 =
								(ProcedureComponent<MostSignificanceResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Sig>>)
								getComponents().get(1);
						ProcedureComponent<Collection<Integer>> comp2 = 
								(ProcedureComponent<Collection<Integer>>) 
								getComponents().get(2);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */

						Collection<Integer> red;
						MostSignificanceResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Sig> incResult;
						do {
							TimerUtils.timePause((TimeCounted) component);
							loopCount++;
							incResult = comp1.exec();
							red = comp2.exec();
							TimerUtils.timeContinue((TimeCounted) component);
						} while (!incResult.isEmptyBoundary());
						return red;
					}, 
					(component, red)->{
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, red);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_SIG_HISTORY]
						List<Sig> posHistory = (List<Sig>) statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (posHistory==null)	statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY, posHistory = new LinkedList<>());
						posHistory.add(getParameters().get("redSig"));
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						reportKeys.add(reportMark);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Controller"),
			// 2. Seek the most significant current attribute.
			new TimeCountedProcedureComponent<MostSignificanceResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}."), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("equClasses"),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						NestedEquivalenceClassBasedRealtimeSimpleCountingCalculation<Sig> calculation =
								(NestedEquivalenceClassBasedRealtimeSimpleCountingCalculation<Sig>)
								parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						int sigAttr = -1;
						Sig maxSig = null;
						// Loop over all potential attributes(attributes not in reduct)
						int[] incRed = new int[red.size()+1];
						int i=0;	for (int each: red)	incRed[i++] = each;
						NestedEquivalenceClassesInfo<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>> sigResult = null;
						for (int a=0; a<attributes.length; a++) {
							if (red.contains(attributes[a]))	continue;
							// calculate the significance of (reduct U a)
							incRed[incRed.length-1] = attributes[a];
							calculation.calculate(equClasses, new IntegerArrayIterator(incRed));
							// if sig>maxSig, update
							if (maxSig==null || calculation.value1IsBetter(calculation.getResult(), maxSig, sigDeviation)) {
								sigAttr = attributes[a];
								maxSig = calculation.getResult();
								sigResult = calculation.getNecInfoWithMap();
							}
						}
						return new MostSignificanceResult<>(
									// the correspondent nested equivalence classes
									sigResult.getNestedEquClasses(),
									// the sig attribute
									sigAttr,
									// the sig value
									maxSig,
									// whether contains 0-NEC?
									sigResult.isEmptyBoundaryClass()
								);
					}, 
					(component, sig) -> {
						/* ------------------------------------------------------------------------------ */
						int sigAttr = sig.getSignificantAttribute();
						Sig maxSig = sig.getSignificance();
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot("sig", sig);
						if (logOn)	log.info(LoggerUtil.spaceFormat(2, "sig = {}"), sig.getSignificance());
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "+ attribute = {} | significance = {}"), 
									sigAttr, maxSig);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistic
						//	[STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH]
						Collection<Integer> reduct = getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						ProcedureUtils.Statistics.push(
							statistics.getData(),
							StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH,
							reduct.size()+1
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>) getParameters().get("equClasses")).size(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY,
								sigAttr
						);
						//	[REPORT_SIG_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								maxSig
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek the most significant current attribute"),
			// 3. Update reduct and significance.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 3/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("sig"),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						MostSignificanceResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Sig> sig =
								(MostSignificanceResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Sig>)
								parameters[p++];
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						red.add(sig.getSignificantAttribute());
						return red;
					}, 
					(component, red)->{
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update reduct and significance"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		localParameters.put("lastUniverseSize", getParameters().get("lastUniverseSize"));
		localParameters.put("lastEquClassSize", getParameters().get("lastEquClassSize"));
		
		if (Integer.compare(
			((Collection<Integer>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size(), 
			((int[]) getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES)).length
			)!=0
		) {
			ProcedureComponent<?>[] comps = initComponents();
			for (ProcedureComponent<?> each : comps)	this.getComponents().add(each);
			
			comps[0].exec();
			
			// Statistics
			//	[Sig loop times]
			statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_LOOP_TIMES, loopCount);
		}
		return this.getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
	}

	public String reportMark() {
		return "Loop["+loopCount+"]";
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}