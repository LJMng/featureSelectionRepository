package featureSelection.tester.procedure.heuristic.liangIncrementalAlgorithm.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.algorithm.alg.liangIncrementalAlgorithm.LiangIncrementalAlgorithm;
import featureSelection.repository.algorithm.alg.positiveApproximationAccelerator.PositiveApproximationAcceleratorOriginalAlgorithm;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.MixedEquivalenceClassSequentialList;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.MostSignificantAttributeResult;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiangIncremental;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Significant attributes seeking loops for <strong>Liang incremental entropy Calculation
 * </strong> based Incremental Feature Selection. This is a
 * {@link DefaultProcedureContainer}. Procedure contains 6 {@link ProcedureComponent}s,
 * refer to 2 steps:
 * <ul>
 * 	<li><strong>Loop controller</strong>:
 * 		<p>Control loop to seek significant attributes for rounds.
 * 	</li>
 * 	<li><strong>Search for a most significant attribute</strong>:
 * 		<p>Search for the attribute with the most significance in attributes outside of
 * 			reduct.
 * 	</li>
 * </ul>
 *
 * @param <Sig>
 *     Type of Feature (subset) significance.
 */
@Slf4j
public class SignificantAttributeSeekingLoopProcedureContainer4MultiObjectIncremental<Sig extends Number> 
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	
	private int loopCount = 0;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private List<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public SignificantAttributeSeekingLoopProcedureContainer4MultiObjectIncremental(
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
			// 1. Loop controller
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get("globalSigOfCMB"),
								getParameters().get("redSigOfCMB"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> newReduct =
								(Collection<Integer>) parameters[p++];
						FeatureImportance4LiangIncremental<Sig> calculation =
								(FeatureImportance4LiangIncremental<Sig>) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						Sig globalSigOfCMB =
								(Sig) parameters[p++];
						Sig redSigOfCMB =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<MostSignificantAttributeResult<Sig>> comp1 = (ProcedureComponent<MostSignificantAttributeResult<Sig>>) getComponents().get(1);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Loop until ME<sub>U∪U<sub>X</sub></sub>(D|B) == ME<sub>U∪U<sub>X</sub></sub>(D|C)
						MostSignificantAttributeResult<Sig> sigPack;
						while (calculation.value1IsBetter(globalSigOfCMB, redSigOfCMB, sigDeviation)) {
							TimerUtils.timePause((TimeCounted) component);
							loopCount++;
							if (logOn){
								log.info(
										LoggerUtil.spaceFormat(1, "Loop {}"),
										loopCount
								);
							}
							// Loop over a in C-B, compute Sig<sub>U∪U<sub>X</sub></sub><sup>Outer</sup>(a, B, D)
							sigPack = comp1.exec();
							TimerUtils.timeContinue((TimeCounted) component);
							//	Select a<sub>0</sub> = max{Sig(a, B, D), a in C-B}
							//	B <- B ∪ {a<sub>0</sub>}
							newReduct.add(sigPack.getAttribute());
							redSigOfCMB = sigPack.getSignificance();
						}
						/* ------------------------------------------------------------------------------ */
						return newReduct;
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[Sig loop times]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_SIG_LOOP_TIMES,
								loopCount
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(getReport(), (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Loop controller"),
			// 2. Search for a most significant attribute
			new TimeCountedProcedureComponent<MostSignificantAttributeResult<Sig>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
//						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("staticCalculation"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get("decEquClassesCMBResult"),
								getParameters().get("previousSigWithDenominator"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> previousInstances =
								(Collection<Instance>) parameters[p++];
						Collection<Instance> newInstances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						FeatureImportance4LiangIncremental<Sig> calculation = 
								(FeatureImportance4LiangIncremental<Sig>)
								parameters[p++];
						PositiveApproximationAcceleratorCalculation<Sig> staticCalculation =
								(PositiveApproximationAcceleratorCalculation<Sig>)
								parameters[p++];
						Sig sigDeviation = (Sig) parameters[p++];
						MixedEquivalenceClassSequentialList decEquClassesCMBResult =
								(MixedEquivalenceClassSequentialList) parameters[p++];
						boolean previousSigWithDenominator =
								(boolean) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Sig sig, previousSig, newSig, maxSig = null;
						int maxSigAttr = -1;
						MixedEquivalenceClassSequentialList mixEquClassesPack;
						Map<IntArrayKey, Collection<Instance>> previousRedEquClasses, newRedEquClasses;
						for (int attr: attributes) {
							if (red.contains(attr))	continue;
							red.add(attr);
							// obtain U/B
							previousRedEquClasses = 
								LiangIncrementalAlgorithm
									.Basic
									.equivalenceClass(
											previousInstances,
											new IntegerCollectionIterator(red)
									);
							// obtain U<sub>X</sub>/B
							newRedEquClasses = 
								LiangIncrementalAlgorithm
									.Basic
									.equivalenceClass(
											newInstances,
											new IntegerCollectionIterator(red)
									);
							// obtain (U∪U<sub>X</sub>)/red∪{a}
							mixEquClassesPack = 
								LiangIncrementalAlgorithm
									.Incremental
									.combineEquivalenceClassesOfPreviousNNew(
											previousRedEquClasses,
											newRedEquClasses
									);
							// Calculate Sig(previous U)
							previousSig = staticCalculation.calculate(
											PositiveApproximationAcceleratorOriginalAlgorithm
												.Basic
												.equivalenceClass(
														previousInstances,
														new IntegerCollectionIterator(red)
												),
											red.size(),
											previousInstances.size()
										).getResult();
							// Calculate Sig(new U)
							newSig = staticCalculation.calculate(
											PositiveApproximationAcceleratorOriginalAlgorithm
												.Basic
												.equivalenceClass(
														newInstances,
														new IntegerCollectionIterator(red)
												),
											red.size(),
											newInstances.size()
										).getResult();
							// Calculate outer sig
							sig = calculation.calculate(
									mixEquClassesPack, decEquClassesCMBResult, 
									previousInstances.size(), newInstances.size(),
									previousSig, newSig, 
									previousSigWithDenominator, red.size()
								).getResult();
							if (maxSig==null || calculation.value1IsBetter(sig, maxSig, sigDeviation)) {
								maxSig = sig;
								maxSigAttr = attr;
							}
							
							red.remove(attr);
						}
						return new MostSignificantAttributeResult<Sig>(maxSigAttr, maxSig);
					}, 
					(component, sigPack)->{
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(2, "+ attr {}"),
									sigPack.getAttribute()
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_SIG_HISTORY]
						Collection<Sig> sigHistory =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (sigHistory==null){
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY,
									sigHistory=new LinkedList<>()
							);
						}
						sigHistory.add(sigPack.getSignificance());
						//	[STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH]
						Collection<Integer> reduct = getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						ProcedureUtils.Statistics.push(
							statistics.getData(),
							StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH,
							reduct.size()+1
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						saveReportExecutedTime((TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Search for a most significant attribute"),
		};	
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps)	getComponents().add(each);
		return (Collection<Integer>) comps[0].exec();
	}

	public String reportMark() {
		return "Loop["+loopCount+"]";
	}
	
	/**
	 * Save executed time of {@link ProcedureComponent} at the current round.
	 * 
	 * @see ProcedureUtils.Report.ExecutionTime#save(Map, String, long...)
	 * 
	 * @param component
	 * 		{@link TimeCountedProcedureComponent} to be saved.
	 */
	private void saveReportExecutedTime(TimeCountedProcedureComponent<?> component) {
		@SuppressWarnings("unchecked")
		Map<String, Long> executedTime = (Map<String, Long>) localParameters.get("executedTime");
		if (executedTime==null)	localParameters.put("executedTime", executedTime = new HashMap<>());
		Long  historyTime = executedTime.get(component.getDescription());
		if (historyTime==null)	historyTime = 0L;
		ProcedureUtils.Report.ExecutionTime.save(report, reportMark(), component.getTime() - historyTime);
		executedTime.put(component.getDescription(), component.getTime());
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}