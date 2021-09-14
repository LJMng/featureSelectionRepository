package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure.core;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import common.utils.LoggerUtil;
import common.utils.StringUtils;
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
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.EquivalenceClassDecMapXtension;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.RoughEquivalenceClassDecMapXtension;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Core searching for <strong>Incremental Decision Rough Equivalence Class based(ID-REC)
 * </strong> Feature Selection. <strong>Class improved version</strong>. (Check out the
 * description of ProcedureComponent 2 and 3)
 * <p>
 * This procedure contains 3 ProcedureComponents: 
 * <ul>
 * 	<li>
 * 		<strong>Core procedure controller</strong>
 * 		<p>Control loop over attributes that are not in reduct, and calculate their
 * 			inner significance. If removing the attribute from the feature subset
 * 			doesn't have any effect on the significance(i.e <code>sig(C-{a})==sig(C)
 * 			</code>), it is NOT a core attribute.
 * 	</li>
 * 	<li>
 * 		<strong>Rough Equivalence Class partition</strong>
 * 		<p>Use the given feature subset to partition {@link EquivalenceClass}es and get
 * 			the correspondent Rough Equivalence Classes. Different from the normal
 * 			version, it is {@link ClassSetType#BOUNDARY} sensitive and it returns
 * 			<code>null</code> if encountering one.
 * 		<p>In this case, using <code>C-{a}</code> to partition {@link EquivalenceClass}es
 * 			and get at least 1 Rough Equivalence class which is
 * 			{@link ClassSetType#BOUNDARY} means without the attribute <code>{a}</code> is
 * 			in-dispensable and it is a Core attribute.
 * 	</li>
 * 	<li>
 * 		<strong>Core</strong>
 * 		<p>Calculate the inner significance of the attribute and determine if it is a
 * 			Core attribute. In the calculation,
 * 			{@link EquivalenceClassDecMapXtension#getSingleSigMark()} is used to avoid
 * 			the re-calculating entropy of rough equivalence class is
 * 			{@link ClassSetType#NEGATIVE} and contains only 1 equivalence class. Which
 * 			is an accelerating strategy to calculate Core.
 * 		<p>Different from {@link ClassicCoreProcedureContainer}, a strategy bases on
 * 			{@link ClassSetType#BOUNDARY} sensitive rough equivalence class partition
 * 			strategy is used to accelerate the process of determining whether an
 * 			attribute is within core or not.
 * 	</li>
 * </ul>
 * 
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalDecision.Core.ClassicImproved
 * 
 * @author Benjamin_L
 */
@Slf4j
public class ClassicImprovedCoreProcedureContainer<Sig extends Number> 
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	private int boundarySkipCount;
	
	public ClassicImprovedCoreProcedureContainer(
			ProcedureParameters paramaters, boolean logOn
	) {
		super(logOn? null: log, paramaters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Core(Classic.Imp)";
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
			// 1. Core procedure controller.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component) -> {
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int[] attributes = (int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp1 = getComponents().get(1);
						ProcedureComponent<?> comp2 = getComponents().get(2);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// core={}
						Collection<Integer> core = new HashSet<>(attributes.length);
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("core", core);
						TimerUtils.timeContinue((TimeCounted) component);
						// attrCheck=C, S=null, sig=0
						int[] examAttr = new int[attributes.length-1];
						for (int i=0; i<examAttr.length; i++)	examAttr[i] = attributes[i+1];
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("examAttr", examAttr);
						TimerUtils.timeContinue((TimeCounted) component);
						// Go through a in attrCheck
						for (int i=0; i<attributes.length; i++) {
							TimerUtils.timePause((TimeCounted) component);
							localParameters.put("i", i);
							comp1.exec();
							comp2.exec();
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return core;
					}, 
					(component, core) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, core);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(1, "|core|={}, core={}"),
									core.size(), 
									StringUtils.numberToString(core, 50, 0)
								);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						int[] attributes = getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES);
						//	[STATISTIC_CORE_LIST]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_CORE_LIST,
								core==null? null: core.toArray(new Integer[core.size()])
						);
						//	[STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH,
								attributes.length
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>) getParameters().get("equClasses")).size(), 
								core.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						//	[REPORT_CORE_4_IDREC_0_REC_DIRECT]
						ProcedureUtils.Report.saveItem(
								report, component.getDescription(),
								ReportConstants.Procedure.REPORT_CORE_4_IDREC_0_REC_DIRECT,
								boundarySkipCount
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Core procedure controller"),
			// 2. Rough Equivalence Class partition.
			new TimeCountedProcedureComponent<Collection<RoughEquivalenceClassDecMapXtension<Sig>>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component) -> {
						component.setLocalParameters(new Object[] {
								getParameters().get("equClasses"),
								localParameters.get("examAttr"),
						});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p = 0;
						Collection<EquivalenceClassDecMapXtension<Sig>> equClasses =
								(Collection<EquivalenceClassDecMapXtension<Sig>>) parameters[p++];
						int[] examAttr =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// S = roughEquivalenceClass(EC_Table, C-{a})
						return RoughEquivalenceClassBasedExtensionAlgorithm
								.IncrementalDecision
								.Core
								.ClassicImproved
								.boundarySensitiveRoughEquivalenceClass(
										equClasses, 
										new IntegerArrayIterator(examAttr)
									);
					}, 
					(component, roughClasses) -> {
						/* ------------------------------------------------------------------------------ */
						localParameters.put("roughClasses", roughClasses);
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(reportMark());
						//	[REPORT_EXECUTION_TIME]
						saveReportExecutedTime((TimeCountedProcedureComponent<?>) component);
					}
				){
					@Override public void init() {}
														
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Rough Equivalence Class partition"),
			// 3. Get core.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component) -> {
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("globalSig"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								localParameters.get("i"),
								localParameters.get("examAttr"),
								localParameters.get("core"),
								localParameters.get("roughClasses"),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
						});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						boolean attributesIsCore = false, boundaryRECSkip = false;
						int calculatePlusSingleMark = 0, calculateGetResult = 0;
						/* ------------------------------------------------------------------------------ */
						int p = 0;
						RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation =
								(RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig>)
								parameters[p++];
						Sig globalSig =
								(Sig) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						int i =
								(int) parameters[p++];
						int[] examAttr =
								(int[]) parameters[p++];
						Collection<Integer> core =
								(Collection<Integer>) parameters[p++];
						Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses =
								(Collection<RoughEquivalenceClassDecMapXtension<Sig>>)
								parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Go through a in attrCheck
						Sig sig = null;
						// Go through E in S
						sig = null;
						if (roughClasses!=null) {
							for (RoughEquivalenceClassDecMapXtension<Sig> roughClass: roughClasses) {
								if (roughClass.getType().isNegative()) {
									if (roughClass.getItemSize()==1) {
										// sig = sig + E.sig
										for (EquivalenceClassDecMapXtension<Sig> equClass: roughClass.getItems()) {
											sig = calculation.plus(sig, equClass.getSingleSigMark());
										}
										
										TimerUtils.timePause((TimeCounted) component);
										calculatePlusSingleMark++;
										TimerUtils.timeContinue((TimeCounted) component);
									}else {
										// sig = sig + sig calculation(E)
										calculation.calculate(roughClass, 1, instances.size());
										sig = calculation.plus(sig, calculation.getResult());
										
										TimerUtils.timePause((TimeCounted) component);
										calculateGetResult++;
										TimerUtils.timeContinue((TimeCounted) component);
									}
								}
							}//*/
							if (calculation.value1IsBetter(globalSig, sig, sigDeviation)) {
								core.add(attributes[i]);
							
								TimerUtils.timePause((TimeCounted) component);
								attributesIsCore = true;
								TimerUtils.timeContinue((TimeCounted) component);
							}
						}else {
							core.add(attributes[i]);
							
							TimerUtils.timePause((TimeCounted) component);
							boundaryRECSkip = true;
							boundarySkipCount++;
							attributesIsCore = true;
							TimerUtils.timeContinue((TimeCounted) component);
						}
						if (i<examAttr.length)	examAttr[i] = attributes[i];
						return new Object[] {
								calculatePlusSingleMark,
								calculateGetResult,
								boundaryRECSkip,
								attributes[i],
								attributesIsCore, 
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						int calculatePlusSingleMark = (int) result[r++];
						int calculateGetResult = (int) result[r++];
						boolean boundaryRECSkip = (boolean) result[r++];
						int attributes_i = (int) result[r++];
						boolean attributeIsCore = (boolean) result[r++];
						/* ------------------------------------------------------------------------------ */
						Integer p_calculatePlusSingleMark = (Integer) localParameters.get("calculatePlusSingleMark");
						if (p_calculatePlusSingleMark==null)	p_calculatePlusSingleMark = 0;
						Integer p_calculateGetResult = (Integer) localParameters.get("calculateGetResult");
						if (p_calculateGetResult==null)			p_calculateGetResult = 0;
						localParameters.put("calculatePlusSingleMark", calculatePlusSingleMark + p_calculatePlusSingleMark);
						localParameters.put("calculateGetResult", calculateGetResult + p_calculateGetResult);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						Collection<Integer> core = (Collection<Integer>) localParameters.get("core");
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>) getParameters().get("equClasses")).size(), 
								core.size()
						);
						//	[REPORT_EXECUTION_TIME]
						saveReportExecutedTime((TimeCountedProcedureComponent<?>) component);
						//	[REPORT_CORE_CURRENT_ATTRIBUTE]
						ProcedureUtils.Report.saveItem(
								report, reportMark(), 
								ReportConstants.Procedure.REPORT_CORE_CURRENT_ATTRIBUTE,
								attributes_i
						);
						//	[REPORT_CORE_CURRENT_ATTRIBUTE]
						ProcedureUtils.Report.saveItem(
								report, reportMark(),
								ReportConstants.Procedure.REPORT_CORE_INDIVIDUAL_RESULT,
								attributeIsCore
						);
						//	[REPORT_CORE_4_IDREC_CURRENT_STATIC_CALCULATE_TIMES]
						ProcedureUtils.Report.saveItem(
								report, reportMark(),
								ReportConstants.Procedure.REPORT_CORE_4_IDREC_CURRENT_STATIC_CALCULATE_TIMES,
								calculatePlusSingleMark
						);
						//	[REPORT_CORE_4_IDREC_CURRENT_COMMON_CALCULATE_TIMES]
						ProcedureUtils.Report.saveItem(
								report, reportMark(),
								ReportConstants.Procedure.REPORT_CORE_4_IDREC_CURRENT_COMMON_CALCULATE_TIMES,
								calculateGetResult
						);
						//	[REPORT_CORE_4_IDREC_0_REC_DIRECT]
						ProcedureUtils.Report.saveItem(
								report, reportMark(),
								ReportConstants.Procedure.REPORT_CORE_4_IDREC_0_REC_DIRECT,
								boundaryRECSkip
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Get Core"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] components = initComponents();
		for (ProcedureComponent<?> each : components)	this.getComponents().add(each);
		return (Collection<Integer>) components[0].exec();
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	public String reportMark() {
		return "Loop["+localParameters.get("i")+"]";
	}
	
	/**
	 * Save executed time of {@link Component} at the current round.
	 * 
	 * @see {@link ProcedureUtils.Report.ExecutionTime#save(Map, String, long...)}.
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

}