package featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.XieDynamicIncompleteDecisionSystemReductionStrategy;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainerOriginal;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainerOriginalNCache;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.DynamicUpdateInfoPack;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.DynamicUpdateStrategy;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.PreviousInfoPack;
import featureSelection.repository.support.calculation.alg.xieDynamicIncrementalDSReduction.FeatureImportance4XieDynamicIncompleteDSReduction;
import featureSelection.repository.support.calculation.alg.xieDynamicIncrementalDSReduction.FeatureImportance4XieDynamicIncompleteDSReductionFixed;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component.ReductInitialisationProcedureContainer4Dynamic;
import featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component.ReductInspectionProcedureContainer;
import featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component.dynamicUpdateStrategies.TolerancesUpdateProcedureContainer4AttributeRelated;
import featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component.dynamicUpdateStrategies.TolerancesUpdateProcedureContainer4BothRelated;
import featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction.component.dynamicUpdateStrategies.TolerancesUpdateProcedureContainer4ObjectRelated;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Xie Dynamic In-complete Decision System Reduction(DIDS)</strong> feature 
 * selection for static data processing.
 * <p>
 * This is a {@link DefaultProcedureContainer}. Procedure contains 4 {@link ProcedureComponent}s,
 * referring to steps:
 * <ul>
 * 	<li><strong>Initiate</strong>: 
 * 		<p>Initialise {@link Calculation} instance.
 * 	</li>
 * 	<li><strong>Calculate Global positive region</strong>: 
 * 		<p>Calculate the global Positive Region: POS<sub>C</sub>(D) by tolerance Classes.
 * 	</li>
 * 	<li><strong>Get core</strong>: 
 * 		<p>Calculate Core. 
 * 		<p><code>CoreProcedureContainer</code>
 * 	</li>
 * 	<li><strong>Sig loop</strong>: 
 * 		<p>Loop and seek the most significant attribute to add into reduct.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer</code>
 * 	</li>
 * 	<li><strong>Inspection</strong>:
 * 		<p>Check and remove redundant attributes in reudct.
 * 		<p><code>ReductInspectionProcedureContainer</code>
 * 	</li>
 * </ul>
 * The following parameters are required to be set in {@link ProcedureParameters}:
 * <ul>
 * 	<li>{@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_DEVIATION}</li>
 * 	<li>toleranceClassObtainerClass</li>
 * 	<li>updateStrategy</li>
 * 	<li>updateInfo</li>
 * </ul>
 * 
 * @see SignificantAttributeSeekingLoopProcedureContainer
 * @see ReductInspectionProcedureContainer
 *
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class XieDynamicIncompleteDSReductionHeuristicQRTester4DynamicData<Sig extends Number>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				XieDynamicIncompleteDecisionSystemReductionStrategy,
				StatisticsCalculated,
				QuickReductHeuristicReductStrategy
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	public XieDynamicIncompleteDSReductionHeuristicQRTester4DynamicData(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
		Class<? extends ToleranceClassObtainer> toleranceClassObtainerClass =
				getParameters().get("toleranceClassObtainerClass");
		ToleranceClassObtainer toleranceClassObtainer = null;
		try {
			toleranceClassObtainer = toleranceClassObtainerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// do nothing.
		}
		
		DynamicUpdateStrategy updateStrategy =
				getParameters().get("updateStrategy");
		Class<? extends FeatureImportance4XieDynamicIncompleteDSReduction<?>> calculationClass =
				getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS);
		boolean fixed = 
				DynamicUpdateStrategy.OBJECT_RELATED.equals(updateStrategy) &&
				FeatureImportance4XieDynamicIncompleteDSReductionFixed.class.isAssignableFrom(calculationClass);

		return "QR-DIDS(dynamic)"+
				String.format("(%s)", toleranceClassObtainer==null? null: toleranceClassObtainer.name())+
				(fixed? "(obj-fixed)":"");
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
			// 1. Initialise.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("1. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
								getParameters().get("toleranceClassObtainerClass"),
								getParameters().get("updateInfo"),
								getParameters().get("updateStrategy"),
							});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Class<? extends Calculation<?>> calculationClass =
								(Class<? extends Calculation<?>>) parameters[p++];
						Class<? extends ToleranceClassObtainer> toleranceClassObtainerClass = 
								(Class<? extends ToleranceClassObtainer>) parameters[p++];
						DynamicUpdateInfoPack updateInfo =
								(DynamicUpdateInfoPack) parameters[p++];
						DynamicUpdateStrategy updateStrategy =
								(DynamicUpdateStrategy) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timePause((TimeCounted) component);

						InstancesCollector completeData4Attributes = null;
						ToleranceClassObtainer toleranceClassObtainer = 
								toleranceClassObtainerClass.newInstance();
						
						if (ToleranceClassObtainerOriginal.class.isAssignableFrom(toleranceClassObtainer.getClass())||
							ToleranceClassObtainerOriginalNCache.class.isAssignableFrom(toleranceClassObtainer.getClass())
						) {
							switch (updateStrategy) {
								case ATTRIBUTE_RELATED:
									TimerUtils.timeContinue((TimeCounted) component);
									completeData4Attributes = 
											toleranceClassObtainer.getCacheInstanceGroups(
													updateInfo.getAlteredAttrValAppliedInstances()
											);
									TimerUtils.timePause((TimeCounted) component);
									break;
								default:
									break;
							}
						}
						
						TimerUtils.timeContinue((TimeCounted) component);
						return new Object[] {
								calculationClass.newInstance(),
								toleranceClassObtainer,
								updateInfo.getPreviousInfo(),
								completeData4Attributes
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Calculation<?> calculation = (Calculation<?>) result[r++];
						ToleranceClassObtainer toleranceClassObtainer = (ToleranceClassObtainer) result[r++];
						PreviousInfoPack previousInfos = (PreviousInfoPack) result[r++];
						InstancesCollector completeData4Attributes = (InstancesCollector) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						getParameters().setNonRoot("toleranceClassObtainer", toleranceClassObtainer);
						getParameters().setNonRoot("previousInfos", previousInfos);
						if (completeData4Attributes!=null) {
							getParameters().setNonRoot("completeData4Attributes", completeData4Attributes);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder().loadCurrentInfo(instances)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								previousInfos.getInstances().size(), 0,
								previousInfos.getReduct().size()
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initialise"),
			// 2. Initiate reduct.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("2. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("updateInfo"),
								getParameters().get("previousInfos"),
							});
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						DynamicUpdateInfoPack updateInfo = (DynamicUpdateInfoPack) parameters[p++];
						PreviousInfoPack previousInfos = (PreviousInfoPack) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						return updateInfo.getAlteredAttributeValues()==null?
								new HashSet<>(previousInfos.getReduct()):
								(Collection<Integer>)
								component.getSubProcedureContainers()
										.values()
										.iterator()
										.next()
										.exec();
					}, 
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						PreviousInfoPack previousInfos = getParameters().get("previousInfos");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								previousInfos.getInstances().size(), 0, 
								previousInfos.getReduct().size()
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate reduct")
				.setSubProcedureContainer(
					"ReductInitialisationProcedureContainer4Dynamic", 
					new ReductInitialisationProcedureContainer4Dynamic<>(getParameters(), logOn)
				),
			// 3. Update in-consistency degree based on diff. update strategy.
			new ProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("3. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("updateStrategy"),
								getParameters().get("updateInfo"),
						});
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						DynamicUpdateStrategy updateStrategy = (DynamicUpdateStrategy) parameters[p++];
						DynamicUpdateInfoPack updateInfo = (DynamicUpdateInfoPack) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						Collection<Instance> latestInstances;
						switch(updateStrategy) {
							case OBJECT_RELATED:
								latestInstances = updateInfo.getAlteredObjectAppliedInstances();
								break;
							case ATTRIBUTE_RELATED:
								latestInstances = updateInfo.getAlteredAttrValAppliedInstances();
								break;
							case BOTH_RELATED:
								latestInstances = updateInfo.getAlteredMixAppliedInstances();
								break;
							default:
								throw new UnsupportedOperationException(updateStrategy.name());
						}
						getParameters().setNonRoot(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, latestInstances);
						/* ------------------------------------------------------------------------------ */
						Object[] sigs = 
							(Object[])
							component.getSubProcedureContainers()
									.get(updateStrategy.name())
									.exec();
						return sigs;
					}, 
					(component, sigs) -> {
						/* ------------------------------------------------------------------------------ */
						Sig redSig = (Sig) sigs[0];
						Sig globalSig = (Sig) sigs[1];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("redSig", redSig);
						getParameters().setNonRoot("globalSig", globalSig);
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(1, "globalSig = {}, redSig = {}"), globalSig, redSig);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						PreviousInfoPack previousInfos = getParameters().get("previousInfos");
						Collection<Instance> instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								instances.size(), 
								0, 
								previousInfos.getReduct().size()
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update in-consistency degree based on diff. update strategy")
				.setSubProcedureContainer(
					DynamicUpdateStrategy.OBJECT_RELATED.name(), 
					new TolerancesUpdateProcedureContainer4ObjectRelated<>(getParameters(), logOn)
				)
				.setSubProcedureContainer(
					DynamicUpdateStrategy.ATTRIBUTE_RELATED.name(), 
					new TolerancesUpdateProcedureContainer4AttributeRelated<>(getParameters(), logOn)
				)
				.setSubProcedureContainer(
					DynamicUpdateStrategy.BOTH_RELATED.name(), 
					new TolerancesUpdateProcedureContainer4BothRelated<>(getParameters(), logOn)
				),
			// 4. Sig loop.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					getParameters(), 
					(component)->{
						if (logOn)	log.info("4. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get("toleranceClassObtainer"),
						});
					}, 
					false, (component, parameters)->{
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer = 
								(ToleranceClassObtainer) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						if (toleranceClassObtainer instanceof ToleranceClassObtainerOriginal ||
							toleranceClassObtainer instanceof ToleranceClassObtainerOriginalNCache
						) {
							TimerUtils.timeStart((TimeCounted) component);
							InstancesCollector completeData4Attributes = 
									toleranceClassObtainer.getCacheInstanceGroups(instances);
							
							TimerUtils.timePause((TimeCounted) component);
							getParameters().setNonRoot("completeData4Attributes", completeData4Attributes);
							TimerUtils.timeContinue((TimeCounted) component);
						}else {
							TimerUtils.timeStart((TimeCounted) component);
						}
					
						TimerUtils.timePause((TimeCounted) component);
						Collection<Integer> reduct = 
								(Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
						TimerUtils.timeContinue((TimeCounted) component);
						return reduct;
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						this.getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, red);
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "|Reduct| = {}"), red.size());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								new ArrayList<>(red)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
						/* ------------------------------------------------------------------------------ */
					}
			) {
				@Override public void init() {}
			
				@Override public String staticsName() {
					return shortName()+" | 4. of "+getComponents().size()+".";
				}
			}.setDescription("Sig loop.")
				.setSubProcedureContainer(
					"SignificantAttributeSeekingLoopProcedureContainer", 
					new SignificantAttributeSeekingLoopProcedureContainer<>(getParameters(), logOn)
				),
			// 5. Inspection
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("5. "+component.getDescription());
					}, 
					(component, parameters) -> {
						return (Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "|Reduct Finally| = {}"), red.size());
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_AFTER_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT,
								red
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
						ProcedureUtils.Report.ExecutionTime.save(report, component.getDescription(), time);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
					
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection")
				.setSubProcedureContainer(
					"ReductInspectionProcedureContainer", 
					new ReductInspectionProcedureContainer<>(getParameters(), logOn)
				),
		};
	}

	@Override
	public long getTime() {
		return getComponents().stream()
					.map(comp->ProcedureUtils.Time.sumProcedureComponentTimes(comp))
					.reduce(Long::sum).orElse(0L);
	}

	@Override
	public Map<String, Long> getTimeDetailByTags() {
		return ProcedureUtils.Time.sumProcedureComponentsTimesByTags(this);
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return getComponents().stream().map(ProcedureComponent::getDescription).toArray(String[]::new);
	}
}