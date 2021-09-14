package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
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
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedUtils;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.EquivalenceClassDecMapXtension;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.RoughEquivalenceClassDecMapXtension;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalDecision.Shrink4RECBasedDecisionMapExt;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalDecision.ShrinkInput4RECIncrementalDecisionExtension;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalDecision.ShrinkResult4RECIncrementalDecisionExtension;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Most significant attribute searching pre-processing for <strong>Quick Reduct -
 * Rough Equivalence Class based extension: Incremental Decision</strong>
 * Feature Selection. This procedure contains 3 ProcedureComponents:
 * <ul>
 * 	<li>
 * 		<strong>Seek significant attributes pre-process procedure controller</strong>
 * 		<p>If reduct is empty, calculate significance by {@link ProcedureComponent} 2,
 * 			else reduct is initiated using core and is not empty, calculate by
 * 			{@link ProcedureComponent} 1.
 * 	</li>
 * 	<li>
 * 		<strong>Sort Rough Equivalence Class using core</strong>
 * 		<p>Obtain Rough Equivalence Classes induced by <code>core</code>.
 * 	</li>
 * 	<li>
 * 		<strong>Get Rough Equivalence Class of empty reduct</strong>
 * 		<p>Wrap Rough Equivalence Class for empty reduct.
 * 	</li>
 * </ul>
 *
 * @param <Sig>
 *     Type of feature (subset) significance.
 *
 * @author Benjamin_L
 */
@Slf4j
public class SigLoopPreprocessProcedureContainer<Sig extends Number> 
	extends DefaultProcedureContainer<Collection<RoughEquivalenceClassDecMapXtension<Sig>>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;

	public SigLoopPreprocessProcedureContainer(
			ProcedureParameters parameters, boolean logOn
	) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new ArrayList<>(1);
		
		localParameters = new HashMap<>();
	}

	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String shortName() {
		return "Sig loop pre-process.";
	}

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// *. Seek significant attributes pre-process procedure controller.
			new ProcedureComponent<Collection<RoughEquivalenceClassDecMapXtension<Sig>>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "*. {}"),
									component.getDescription()
							);
						}
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> red = getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						ProcedureComponent<Object[]> component1 = (ProcedureComponent<Object[]>) getComponents().get(1);
						ProcedureComponent<Object[]> component2 = (ProcedureComponent<Object[]>) getComponents().get(2);
						/* ------------------------------------------------------------------------------ */
						return red.isEmpty()? 
								(Collection<RoughEquivalenceClassDecMapXtension<Sig>>)
								((Object[]) component2.exec())[0]:
								(Collection<RoughEquivalenceClassDecMapXtension<Sig>>)
								((Object[]) component1.exec())[0];
					}, 
					(component, roughClasses) -> {
						getParameters().setNonRoot("roughClasses", roughClasses);
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | *. "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek significant attributes pre-process procedure controller"),
			// 1. Sort Rough Equivalence Class using core.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "1. {}"),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("equClasses"),
								getParameters().get(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
						});
						localParameters.put(
								"lastInstanceSize",
								((Collection<Instance>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size()
						);
						localParameters.put(
								"lastEquClassSize", 
								((Collection<EquivalenceClassDecMapXtension<Sig>>) getParameters().get("equClasses")).size()
						);
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<EquivalenceClassDecMapXtension<Sig>> equClasses =
								(Collection<EquivalenceClassDecMapXtension<Sig>>)
								parameters[p++];
						Shrink4RECBasedDecisionMapExt<Sig> shrinkInstance =
								(Shrink4RECBasedDecisionMapExt<Sig>)
								parameters[p++];
						Collection<Integer> red = (Collection<Integer>) parameters[p++];
						RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation =
								(RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig>)
								parameters[p++];
						Collection<Instance> instances = (Collection<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						ShrinkResult4RECIncrementalDecisionExtension<Sig> shrinkResult;
						// if Red != null
						// newU = roughEquivelentClass(EC_Table, C)
						Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses =
								RoughEquivalenceClassBasedExtensionAlgorithm
									.IncrementalDecision
									.Basic
									.roughEquivalenceClass(
											equClasses,
											new IntegerCollectionIterator(red)
									);
						// newU, sig_static = streamline(newU, sig calculation)
						shrinkResult =
								shrinkInstance.shrink(
									new ShrinkInput4RECIncrementalDecisionExtension<>(
											roughClasses, red.size()
									)
								);
						roughClasses = shrinkResult.getRoughClasses();
						// sig(Red)=sig calculation(newU)
						calculation.calculate(roughClasses, red.size(), instances.size());
						return new Object[] {
								roughClasses,
								shrinkResult.getRemovedUniverseSignificance(),
								calculation.plus(calculation.getResult(), shrinkResult.getRemovedUniverseSignificance()),
								calculation,
								shrinkResult.getRemovedNegativeSizeInfo(),
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses = (Collection<RoughEquivalenceClassDecMapXtension<Sig>>) result[r++];
						Sig staticSig = (Sig) result[r++];
						Sig redSig = (Sig) result[r++];
						FeatureImportance<Sig> calculation = (FeatureImportance<Sig>) result[r++];
						int currentInsSize = RoughEquivalenceClassBasedUtils.countUniverseSize(roughClasses);
						int currentEquClassSize = RoughEquivalenceClassBasedUtils.countEquivalenceClassSize(roughClasses);
						int[] removedNegInfo = (int[]) result[r++];
						int removedNegIns = removedNegInfo[0],
							removedNegEquClass = removedNegInfo[1];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("roughClasses", result[0]);
						if (staticSig!=null)	this.getParameters().setNonRoot("staticSig", staticSig);
						getParameters().setNonRoot("redSig", redSig);
						getParameters().setNonRoot("calculation", calculation);
						getParameters().setNonRoot("lastInstanceSize", currentInsSize);
						getParameters().setNonRoot("lastEquClassSize", currentEquClassSize);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(2, "Static Sig = {} | Red sig = {}"),
									result[1], result[2]
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						reportKeys.add(component.getDescription());
						//	[STATISTIC_POS_HISTORY]
						List<Sig> posHistory =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (posHistory==null){
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY,
									posHistory=new LinkedList<>()
							);
						}
						posHistory.add(redSig);
						//	[calculation.plus]
						ProcedureUtils.Statistics.countInt(
								statistics.getData(),
								"calculation.plus(sig_static, new_sig_static)",
								1
						);
						//	[universeStreamline.streamline()]
						ProcedureUtils.Statistics.countInt(
								statistics.getData(),
								"UniverseStreamline4RoughEquivalenceClassBasedDecisionMapExt.streamline()",
								1
						);
						//	[STATISTIC_POS_COMPACTED_UNIEVRSE_REMOVED]
						List<Integer> insRemoved =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_POS_COMPACTED_INSTANCE_REMOVED);
						if (insRemoved==null) {
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_POS_COMPACTED_INSTANCE_REMOVED,
									insRemoved = new LinkedList<>()
							);
						}
						int lastEquClassSize = (int) localParameters.get("lastEquClassSize");
						insRemoved.add(lastEquClassSize - currentEquClassSize - removedNegEquClass);
						//	[STATISTIC_NEG_COMPACTED_UNIEVRSE_REMOVED]
						insRemoved = statistics.get(StatisticsConstants.Procedure.STATISTIC_NEG_COMPACTED_UNIVERSE_REMOVED);
						if (insRemoved==null) {
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_NEG_COMPACTED_UNIVERSE_REMOVED,
									insRemoved = new LinkedList<>()
							);
						}
						insRemoved.add(removedNegEquClass);
						//	[STATISTIC_POS_UNIEVRSE_REMOVED]
						insRemoved =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_POS_INSTANCE_REMOVED);
						if (insRemoved==null) {
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_POS_INSTANCE_REMOVED,
									insRemoved = new LinkedList<>()
							);
						}
						int lastInstanceSize = (int) localParameters.get("lastInstanceSize");
						insRemoved.add(lastInstanceSize - currentInsSize - removedNegIns);
						//	[STATISTIC_NEG_UNIEVRSE_REMOVED]
						insRemoved =
								statistics.get(StatisticsConstants.Procedure.STATISTIC_NEG_INSTANCE_REMOVED);
						if (insRemoved==null) {
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_NEG_INSTANCE_REMOVED,
									insRemoved = new LinkedList<>()
							);
						}
						insRemoved.add(removedNegIns);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								currentInsSize,
								currentEquClassSize, 
								((Collection<Integer>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						//	[REPORT_UNIVERSE_POS_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, component.getDescription(),
								ReportConstants.Procedure.REPORT_UNIVERSE_POS_REMOVE_HISTORY,
								lastInstanceSize - currentInsSize - removedNegIns
						);
						//	[REPORT_COMPACTED_UNIVERSE_POS_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, component.getDescription(),
								ReportConstants.Procedure.REPORT_COMPACTED_UNIVERSE_POS_REMOVE_HISTORY,
								lastEquClassSize - currentEquClassSize - removedNegEquClass
						);
						//	[REPORT_UNIVERSE_NEG_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, component.getDescription(),
								ReportConstants.Procedure.REPORT_UNIVERSE_NEG_REMOVE_HISTORY,
								removedNegIns
						);
						//	[REPORT_COMPACTED_UNIVERSE_NEG_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, component.getDescription(),
								ReportConstants.Procedure.REPORT_COMPACTED_UNIVERSE_NEG_REMOVE_HISTORY,
								removedNegEquClass
						);
						//	[REPORT_POSITIVE_INCREMENT_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, component.getDescription(),
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								redSig
						);
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "- {} instance(s)"),
									lastInstanceSize - currentInsSize
							);
						}
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Sort Rough Equivalence Class using core"),
			// 2. Get Rough Equivalence Class of empty reduct.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "2. {}"),
									component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get("equClasses"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<EquivalenceClassDecMapXtension<Sig>> equClasses =
								(Collection<EquivalenceClassDecMapXtension<Sig>>)
								parameters[p++];
						RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation =
								(RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig>)
								parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						RoughEquivalenceClassDecMapXtension<Sig> roughClass = new RoughEquivalenceClassDecMapXtension<>();
						for (EquivalenceClassDecMapXtension<Sig> equ: equClasses)	roughClass.addClassItem(equ);
						Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses = new HashSet<>();
						roughClasses.add(roughClass);
						// sig(Red)=sig calculation(newU)
						calculation.calculate(roughClasses, 0, instances.size());
						return new Object[] {
								roughClasses,
								calculation,
								calculation.getResult(),
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses = (Collection<RoughEquivalenceClassDecMapXtension<Sig>>) result[r++];
						FeatureImportance<Sig> calculation = (FeatureImportance<Sig>) result[1];
						Sig redSig = (Sig) result[2];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("roughClasses", roughClasses);
						getParameters().setNonRoot("calculation", calculation);
						getParameters().setNonRoot("redSig", redSig);
						/* ------------------------------------------------------------------------------ */
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(2, "redSig = {}"),
									redSig
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>) getParameters().get("equClasses")).size(), 
								((Collection<Integer>) getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						//	[REPORT_POSITIVE_INCREMENT_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, component.getDescription(), 
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								redSig
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Get Rough Equivalence Class of empty reduct"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<RoughEquivalenceClassDecMapXtension<Sig>> exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray)		this.getComponents().add(each);
		return (Collection<RoughEquivalenceClassDecMapXtension<Sig>>) componentArray[0].exec();
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}
