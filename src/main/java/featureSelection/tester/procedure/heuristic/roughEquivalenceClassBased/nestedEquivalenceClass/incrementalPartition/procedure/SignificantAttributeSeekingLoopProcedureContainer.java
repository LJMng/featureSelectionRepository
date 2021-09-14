package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
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
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.PartitionResult;
import featureSelection.repository.entity.alg.rec.nestedEC.MostSignificanceResult;
import featureSelection.repository.entity.alg.rec.nestedEC.impl.nestedEquivalenceClassesMerge.result.ReductionResult;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4IPNEC;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Most significant attribute searching for <strong>Incremental Partition Nested Equivalence Classes
 * based (IP-NEC)</strong> Feature Selection. This procedure contains 3 {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Controller</strong>
 * 		<p>Control the searching of most significant attribuets for reduct. Check if significance of
 * 	    	current reduct equals to the global significance(i.e. <code>sig(red)==sig(C)</code>).
 * 	    	Return <code>true</code> if it does.
 * 	</li>
 * 	<li>
 * 		<strong>Seek the current most significant attribute</strong>
 * 		<p>Search for the attribute with the max. significance value in the rest of the
 * 			attributes(i.e. attributes outside of the reduct), and return as an attribute
 * 			of the reduct.
 * 	</li>
 * </ul>
 *
 * @param <Sig>
 *     Type of feature (subset) significance.
 *
 * @author Benjamin_L
 */
@Slf4j
public class SignificantAttributeSeekingLoopProcedureContainer<Sig extends Number>
	extends DefaultProcedureContainer<ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>>
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
			new TimeCountedProcedureComponent<ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 1/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get("nestedEquClasses"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> reduct = (Collection<Integer>) parameters[p++];
						Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClasses =
								(Collection<NestedEquivalenceClass<EquivalenceClass>>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						localParameters.put("reduct", reduct);
						ProcedureComponent<MostSignificanceResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Sig>> comp1 =
								(ProcedureComponent<MostSignificanceResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Sig>>)
								getComponents().get(1);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						boolean exit = false;
						Sig redSig = null;
						Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClassesValues = nestedEquClasses;
						MostSignificanceResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Sig> sigSearchResult;
						while (!exit) {
							TimerUtils.timePause((TimeCounted) component);
							loopCount++;
							localParameters.put("nestedEquClassesValues", nestedEquClassesValues);
							sigSearchResult = comp1.exec();
							TimerUtils.timeContinue((TimeCounted) component);
							
							reduct.add(sigSearchResult.getSignificantAttribute());
							nestedEquClassesValues = sigSearchResult.getNestedEquClasses();
							exit = sigSearchResult.isEmptyBoundary();
							
							redSig = sigSearchResult.getSignificance();
						};
						return new ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>(
								nestedEquClassesValues, reduct, redSig
						);
					}, 
					(component, reductionResult)->{
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reductionResult.getReduct());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_SIG_HISTORY]
						List<Sig> posHistory = (List<Sig>) statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (posHistory==null)	statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY, posHistory = new LinkedList<>());
						posHistory.add(reductionResult.getReductSig());
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
			// 2. Seek the current most significant current attribute.
			new TimeCountedProcedureComponent<MostSignificanceResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}."), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								localParameters.get("nestedEquClassesValues"),
								localParameters.get("reduct"),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int[] attributes = (int[]) parameters[p++];
						Collection<NestedEquivalenceClass<EquivalenceClass>> nestedEquClassesValues =
								(Collection<NestedEquivalenceClass<EquivalenceClass>>) parameters[p++];
						Collection<Integer> reduct = (Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Go through a in P
						PartitionResult<Integer, Collection<NestedEquivalenceClass<EquivalenceClass>>> result;
						PartitionResult<Integer, Collection<NestedEquivalenceClass<EquivalenceClass>>> sigResult = null;
						for (int a=0; a<attributes.length; a++) {
							// red = red U a
							if (reduct.contains(attributes[a]))	continue;
							// NEC, sig, flag = NEC(NEC, P)
							result = PositiveRegionCalculation4IPNEC.inTurnIncrementalPartition(
										nestedEquClassesValues, attributes[a]
									);
							// if sig>maxSig, mark
							if (sigResult==null||result.getPositive()>sigResult.getPositive())
								sigResult = result;
						}//*/
						// return NEC, newRed, sig, flag
						return new MostSignificanceResult<>(
									sigResult.getRoughClasses(),
									sigResult.getAttributes(),
									(Sig) (Integer) sigResult.getPositive(),
									sigResult.isEmptyBoundaryClassSetTypeClass()
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
						ProcedureUtils.Statistics.push(
							statistics.getData(),
							StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH, 1
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
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig> exec() throws Exception {
		Object obj;
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps)	this.getComponents().add(each);
		obj = comps[0].exec();
		// Statistics
		//	[Sig loop times]
		statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_LOOP_TIMES, loopCount);
		return (ReductionResult<Collection<NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Sig>) obj;
	}

	public String reportMark() {
		return "Loop["+loopCount+"]";
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}