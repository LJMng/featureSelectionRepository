package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
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
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.RoughEquivalenceClassDecMapXtension;
import featureSelection.repository.entity.alg.rec.extension.incrementalDecision.MostSignificantAttributeResult;
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
			// 1. Check if break loop : true-continue, false-break.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 1/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("globalSig"),
								getParameters().get("redSig"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Sig globalSig = (Sig) parameters[p++];
						Sig redSig = (Sig) parameters[p++];
						Sig sigDeviation = (Sig) parameters[p++];
						FeatureImportance<Sig> calculation = (FeatureImportance<Sig>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return calculation.value1IsBetter(globalSig, redSig, sigDeviation);
					}, 
					(component, result)->{
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "globalSig = {} | redSig = {}"),
										String.format((getParameters().get("globalSig") instanceof Double?"%.20f":"%d"), ((Number) getParameters().get("globalSig"))),
										String.format((getParameters().get("redSig") instanceof Double?"%.20f":"%d"), ((Number) getParameters().get("redSig")))
									);
							if (result==null || !result.booleanValue())
								log.info(LoggerUtil.spaceFormat(2, "break!"));
						}
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
				}.setDescription("Check if break loop"),
			// 2. Seek the most significant current attribute.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 2/{}. {}."), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("roughClasses"),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
							});
						if (localParameters.get("lastUniverseSize")==null) {
							localParameters.put(
								"lastUniverseSize",
								RoughEquivalenceClassBasedUtils.countUniverseSize(getParameters().get("roughClasses"))
							);
						}
						if (localParameters.get("lastEquClassSize")==null) {
							localParameters.put(
								"lastEquClassSize", 
								RoughEquivalenceClassBasedUtils.countEquivalenceClassSize(getParameters().get("roughClasses"))
							);
						}
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int removeNegUniverse = 0, removeNegEquClass = 0;
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses = (Collection<RoughEquivalenceClassDecMapXtension<Sig>>) parameters[p++];
						Collection<Integer> red = (Collection<Integer>) parameters[p++];
						int[] attributes = (int[]) parameters[p++];
						RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation =
								(RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig>)
								parameters[p++];
						Sig sigDeviation = (Sig) parameters[p++];
						Shrink4RECBasedDecisionMapExt<Sig> shrinkInstance =
							(Shrink4RECBasedDecisionMapExt<Sig>) parameters[p++];
						Collection<Instance> universes = (Collection<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// sig=0
						// a*=0
						int sigAttr=-1;
						// global_sig_static=0
						Sig redSig, staticSig, sigSum;
						Sig maxRedSig = null, maxStaticSig = null, maxSigSum = null;
						// S = null
						// Go through a in C-Red
						ShrinkResult4RECIncrementalDecisionExtension<Sig> shrinkResult;
						Collection<RoughEquivalenceClassDecMapXtension<Sig>>
								sigRoughClasses = null, incrementalRoughClasses;
						for (int attr : attributes) {
							if (red.contains(attr))	continue;
							// a_U = EC_Table
							// a_U = roughEquivalenceClass(a_U, a)
							incrementalRoughClasses =
									RoughEquivalenceClassBasedExtensionAlgorithm
											.IncrementalDecision
											.Basic
											.incrementalPartitionRoughEquivalenceClass(
													roughClasses,
													new IntegerArrayIterator(attr)
											);
							// a_U, sig_static = streamline(a_U, sig calculation)
							shrinkResult = shrinkInstance.shrink(
													new ShrinkInput4RECIncrementalDecisionExtension<Sig>(
															incrementalRoughClasses, 1
													)
												);
							incrementalRoughClasses = shrinkResult.getRoughClasses();
							staticSig = shrinkResult.getRemovedUniverseSignificance();//*/
							// a.outerSig = sig calculation(a_U)
							calculation.calculate(incrementalRoughClasses, 1, universes.size());
							redSig = calculation.getResult();
							// if (a.outerSig > sig)
							sigSum = calculation.plus(redSig, staticSig);
							if (maxSigSum==null || 
								calculation.value1IsBetter(sigSum, maxSigSum, sigDeviation)
							) {
								// a*=a
								sigAttr = attr;
								// sig=a.outerSig
								maxRedSig = redSig;
								// global_sig_static = sig_static
								maxStaticSig = staticSig;
								maxSigSum = sigSum;
								// S = a_U
								sigRoughClasses = incrementalRoughClasses;
								
								removeNegUniverse = shrinkResult.getRemovedNegativeSizeInfo()[0];
								removeNegEquClass = shrinkResult.getRemovedNegativeSizeInfo()[1];
							}
						}
						return new Object[] {
								new MostSignificantAttributeResult<>(maxRedSig, maxStaticSig, sigAttr, sigRoughClasses),
								removeNegUniverse,
								removeNegEquClass,
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r = 0;
						MostSignificantAttributeResult<Sig> sig = (MostSignificantAttributeResult<Sig>) result[r++];
						int removeNegUniverse = (int) result[r++];
						int removeNegEquClass = (int) result[r++];
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot("sig", sig);
						this.getParameters().setNonRoot("roughClasses", sig.getRoughClasses());
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "+ attribute = {} | significance = {}"), 
									sig.getAttribute(), sig.getSignificance());
						}
						/* ------------------------------------------------------------------------------ */
						// Statistic
						int lastUniverseSize = (int) localParameters.get("lastUniverseSize");
						int lastEquClassSize = (int) localParameters.get("lastEquClassSize");
						//	[STATISTIC_POS_COMPACTED_UNIEVRSE_REMOVED]
						int currentEquClassSize = RoughEquivalenceClassBasedUtils.countEquivalenceClassSize(sig.getRoughClasses());
						List<Integer> universeRemoved =
								(List<Integer>) statistics.get(StatisticsConstants.Procedure.STATISTIC_POS_COMPACTED_INSTANCE_REMOVED);
						if (universeRemoved==null) {
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_POS_COMPACTED_INSTANCE_REMOVED,
									universeRemoved = new LinkedList<>()
							);
						}
						universeRemoved.add(lastEquClassSize - currentEquClassSize - removeNegEquClass);
						//	[STATISTIC_NEG_COMPACTED_UNIEVRSE_REMOVED]
						universeRemoved = (List<Integer>) statistics.get(StatisticsConstants.Procedure.STATISTIC_NEG_COMPACTED_UNIVERSE_REMOVED);
						if (universeRemoved==null) {
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_NEG_COMPACTED_UNIVERSE_REMOVED,
									universeRemoved = new LinkedList<>()
							);
						}
						universeRemoved.add(removeNegEquClass);
						localParameters.put("lastEquClassSize", currentEquClassSize);
						//	[STATISTIC_POS_UNIEVRSE_REMOVED]
						int currentUniverseSize = RoughEquivalenceClassBasedUtils.countUniverseSize(sig.getRoughClasses());
						universeRemoved = (List<Integer>) statistics.get(StatisticsConstants.Procedure.STATISTIC_POS_INSTANCE_REMOVED);
						if (universeRemoved==null) {
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_POS_INSTANCE_REMOVED,
									universeRemoved = new LinkedList<>()
							);
						}
						universeRemoved.add(lastUniverseSize - currentUniverseSize - removeNegUniverse);
						//	[STATISTIC_NEG_UNIEVRSE_REMOVED]
						universeRemoved = (List<Integer>) statistics.get(StatisticsConstants.Procedure.STATISTIC_NEG_INSTANCE_REMOVED);
						if (universeRemoved==null) {
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_NEG_INSTANCE_REMOVED,
									universeRemoved = new LinkedList<>()
							);
						}
						universeRemoved.add(removeNegUniverse);
						localParameters.put("lastUniverseSize", currentUniverseSize);
						//	[STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH
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
								currentUniverseSize, 
								currentEquClassSize, 
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
								sig.getAttribute()
						);
						//	[REPORT_UNIVERSE_POS_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_UNIVERSE_POS_REMOVE_HISTORY,
								lastUniverseSize - currentUniverseSize - removeNegUniverse
						);
						//	[REPORT_UNIVERSE_NEG_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_UNIVERSE_NEG_REMOVE_HISTORY,
								removeNegUniverse
						);
						//	[REPORT_COMPACTED_UNIVERSE_POS_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_COMPACTED_UNIVERSE_POS_REMOVE_HISTORY,
								lastEquClassSize - currentEquClassSize - removeNegEquClass
						);
						//	[REPORT_COMPACTED_UNIVERSE_NEG_REMOVE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_COMPACTED_UNIVERSE_NEG_REMOVE_HISTORY,
								removeNegEquClass
						);
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(2, "- {} universe(s)"), lastUniverseSize - currentUniverseSize);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Seek the most significant current attribute"),
			// 3. Update reduct and significance.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Loop {} | 3/{}. {}"), loopCount, getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("sig"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get("staticSig"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						MostSignificantAttributeResult<Sig> sig = (MostSignificantAttributeResult<Sig>) parameters[p++];
						RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation =
								(RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig>)
								parameters[p++];
						Collection<Integer> red = (Collection<Integer>) parameters[p++];
						Sig staticSig = (Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						red.add(sig.getAttribute());
						staticSig = calculation.plus(staticSig, sig.getGlobalStaticSiginificance());
						return new Object[] {
								red,
								staticSig,
								calculation.plus(sig.getSignificance(), staticSig),
						};
					}, 
					(component, result)->{
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Integer> red = (Collection<Integer>) result[r++];
						Sig staticSig = (Sig) result[r++];
						Sig redSig = (Sig) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						if (result[1]!=null)	getParameters().setNonRoot("staticSig", staticSig);
						getParameters().setNonRoot("redSig", redSig);
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(2, "staticSig = {}| sig = {}"), result[1], result[2]);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_SIG_HISTORY]
						List<Sig> posHistory = (List<Sig>) statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (posHistory==null)	statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY, posHistory = new LinkedList<>());
						posHistory.add(redSig);
						//	[calculation.plus]
						ProcedureUtils.Statistics.countInt(
								statistics.getData(),
								"calculation.plus(sig_static, new_sig_static)", 1
						);
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
						//	[REPORT_SIG_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark, 
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								redSig
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
			
			SigLoop:
			while (true) {
				loopCount++;
				for (int i=0; i<comps.length; i++) {
					if (i==0) {
						if (!(Boolean) comps[i].exec())	break SigLoop;
					}else {
						comps[i].exec();
					}
				}
			}
			
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