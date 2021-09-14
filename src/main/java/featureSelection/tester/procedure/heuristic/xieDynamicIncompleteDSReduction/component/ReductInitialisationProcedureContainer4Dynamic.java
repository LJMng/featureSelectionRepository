package featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import common.utils.LoggerUtil;
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
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.DynamicUpdateInfoPack;
import featureSelection.repository.support.calculation.alg.xieDynamicIncrementalDSReduction.FeatureImportance4XieDynamicIncompleteDSReduction;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Initiate a new reduct by checking altered attributes one by one for <strong>Xie Dynamic In-complete 
 * Decision System Reduction(DIDS)</strong> Feature Selection. This procedure contains 2 
 * ProcedureComponents: 
 * <ul>
 * 	<li>
 * 		<strong>Controller</strong>
 * 		<p>Control loop over altered attributes and compare the significance values of the attribute 
 * 			before and after the attribute values changed one by one.
 * 	</li>
 * 	<li>
 * 		<strong>Compute in-consistency degree using the altered attribute</strong>
 * 		<p>Calculate the significance of an attribute and determine if remove from/add into the new 
 * 			reduct.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Slf4j
public class ReductInitialisationProcedureContainer4Dynamic<Sig extends Number>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public ReductInitialisationProcedureContainer4Dynamic(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "DIDS-DynamicReductInit";
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
			// 1. Controller
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get("updateInfo"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						DynamicUpdateInfoPack updateInfo = (DynamicUpdateInfoPack) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp1 = this.getComponents().get(1);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> initNewReduct = new HashSet<>(updateInfo.getPreviousInfo().getReduct());
						
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("initNewReduct", initNewReduct);
						TimerUtils.timeContinue((TimeCounted) component);
						
						for (int alteredAttribute: updateInfo.getAlteredAttributeValues().getAlteredAttributes()) {
							TimerUtils.timePause((TimeCounted) component);
							localParameters.put("alteredAttribute", alteredAttribute);
							comp1.exec();
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return initNewReduct;
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, result);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(1, "Initiated reduct: {}"), result);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								result.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Core procedure controller"),
			// 2. Compute in-consistency degree using the altered attribute
			new TimeCountedProcedureComponent<Integer>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get("updateInfo"),
								getParameters().get("toleranceClassObtainer"),
								getParameters().get("completeData4Attributes"),
								localParameters.get("initNewReduct"),
								localParameters.get("alteredAttribute"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						FeatureImportance4XieDynamicIncompleteDSReduction<Sig> calculation =
								(FeatureImportance4XieDynamicIncompleteDSReduction<Sig>)
								parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						DynamicUpdateInfoPack updateInfo =
								(DynamicUpdateInfoPack) parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer =
								(ToleranceClassObtainer) parameters[p++];
						InstancesCollector completeData4Attributes =
								(InstancesCollector) parameters[p++];
						Collection<Integer> initNewReduct = (Collection<Integer>) parameters[p++];
						int alteredAttribute = (int) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Compute in-consistency degree on the previous and newest instances value using the 
						//	altered attribute
						Sig previousSig = 
							calculation.calculate(
								updateInfo.getPreviousInfo().getInstances(),
								new IntegerArrayIterator(alteredAttribute),
								toleranceClassObtainer, completeData4Attributes
							).getResult();
						Sig alteredSig = 
							calculation.calculate(
								updateInfo.getAlteredAttrValAppliedInstances(), 
								new IntegerArrayIterator(alteredAttribute),
								toleranceClassObtainer,
								toleranceClassObtainer.getCacheInstanceGroups(
									updateInfo.getAlteredAttrValAppliedInstances(), 
									new IntegerArrayIterator(alteredAttribute)
								)
							).getResult();
						// if altered attribute is in previous reduct.
						if (updateInfo.getPreviousInfo().getReduct().contains(alteredAttribute)) {
							// if sig value of newest <= value of previous (== not newest > previous)
							if (!calculation.value1IsBetter(alteredSig, previousSig, sigDeviation)) {
								// initialize REC<sub>N+1</sub> ← RED<sub>N</sub>
							}else {
								// initialize REC<sub>N+1</sub> ← RED<sub>N</sub> - {a<sub>ALT</sub>}
								initNewReduct.remove(alteredAttribute);
							}
						// else	altered attribute is not in the previous reduct.
						}else {
							// if sig value of newest < value of previous
							if (calculation.value1IsBetter(previousSig, alteredSig, sigDeviation)) {
								// initialize REC<sub>N+1</sub> ← RED<sub>N</sub> ∪ {a<sub>ALT</sub>}
								initNewReduct.add(alteredAttribute);
							}else {
								// initialize REC<sub>N+1</sub> ← RED<sub>N</sub>
							}
						}
						
						return alteredAttribute;
					}, 
					(component, alteredAttribute) -> {
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = "Attr "+alteredAttribute;
						reportKeys.add(reportMark);
						//	[DatasetRealTimeInfo]
						Collection<Integer> initNewReduct = (Collection<Integer>) localParameters.get("initNewReduct");
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								initNewReduct.size()
						);
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
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Compute in-consistency degree using the altered attribute"),
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
}