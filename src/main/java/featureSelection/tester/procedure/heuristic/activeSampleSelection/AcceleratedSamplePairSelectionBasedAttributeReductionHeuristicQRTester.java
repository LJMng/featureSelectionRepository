package featureSelection.tester.procedure.heuristic.activeSampleSelection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
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
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.ActiveSamplePairSelectionStrategy;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.repository.algorithm.alg.activeSampleSelection.ActiveSampleSelectionAlgorithm;
import featureSelection.repository.entity.alg.activeSampleSelection.AttrDiscernibilityResult;
import featureSelection.repository.entity.alg.activeSampleSelection.EquivalenceClass;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePair;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePairAttributeInfo;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.activeSampleSelection.procedure.ReductInspectionProcedureContainer;
import featureSelection.tester.procedure.heuristic.activeSampleSelection.procedure.SignificantAttributeSeekingLoopProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for (accelerated) <strong>Sample Pair Selection</strong> for Attribute Reduction
 * (for <strong>Static Data</strong>).
 * <p>
 * Original paper: <a href="https://ieeexplore.ieee.org/document/6308684">
 * "Sample Pair Selection for Attribute Reduction with Rough Set"</a> by Degang Chen,
 * Suyun Zhao, Lei Zhang, Yongping Yang, Xiao Zhang.
 * <p>
 * This is a {@link DefaultProcedureContainer}. Procedure contains 6
 * {@link ProcedureComponent}s, refer to steps:
 * <ul>
 * 	<li>
 * 		<strong>Pre-initialization</strong>:
 * 		<p>Initiate {@link Instance} {@link ArrayList} which is a copy of
 *            {@link Collection} and {@link Calculation} instances.
 * 	</li>
 * 	<li>
 * 		<strong>Compact</strong>:
 * 		<p>Compact {@link Instance}s into {@link EquivalenceClass}es.
 * 	</li>
 * 	<li>
 * 		<strong>Calculate DIS({a}), DIS({A})</strong>:
 * 		<p>Calculate the discernibility matrix: DIS({a}) for every condition attribute
 * 			and DIS({A}) as the collection of all elements in DIS({a}).
 * 	</li>
 * 	<li>
 * 		<strong>Sort elements in DIS(A)</strong>:
 * 		<p>Sort elements in DIS(A) bases on their frequencies in DIS({a}) in ascending
 * 			order.
 * 	</li>
 * 	<li>
 * 		<strong>Heuristic search loop</strong>:
 * 		<p>Using heuristic search strategy to continuously search for attributes adding into
 * 			reduct. In searching, the attribute with the maximum sample pair number/
 * 			frequency in DIS({a}) is selected into reduct.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer</code>
 * 	</li>
 * 	<li>
 * 		<strong>Inspection</strong>:
 * 		<p>Inspect the reduct and remove redundant attributes.
 * 		<p><code>ReductInspectionProcedureContainer</code>
 * 	 </li>
 * </ul>
 * The following parameters are required to be set in {@link ProcedureParameters}:
 * <ul>
 * 	<li>{@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_DEVIATION}</li>
 * </ul>
 *
 * @author Benjamin_L
 * @see SignificantAttributeSeekingLoopProcedureContainer
 * @see ReductInspectionProcedureContainer
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class AcceleratedSamplePairSelectionBasedAttributeReductionHeuristicQRTester
		extends DefaultProcedureContainer<Collection<Integer>>
		implements TimeSum,
					ReportMapGenerated<String, Map<String, Object>>,
					HashSearchStrategy,
					StatisticsCalculated,
					ActiveSamplePairSelectionStrategy,
					QuickReductHeuristicReductStrategy
{
	private boolean logOn;
	@Getter
	private Statistics statistics;
	@Getter
	private Map<String, Map<String, Object>> report;

	public AcceleratedSamplePairSelectionBasedAttributeReductionHeuristicQRTester(
			ProcedureParameters parameters, boolean logOn
	) {
		super(logOn ? log : null, parameters);
		this.logOn = logOn;

		statistics = new Statistics();
		report = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "QR-ASE" +
				"(" + ProcedureUtils.ShortName.byCore(getParameters()) + ")" +
				"(" + ProcedureUtils.ShortName.calculation(getParameters()) + ")";
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
		return new ProcedureComponent<?>[]{
				// 1. Pre-initialization
				new ProcedureComponent<Object[]>(
						ComponentTags.TAG_SIG,
						this.getParameters(),
						(component) -> {
							if (logOn){
								log.info("1. " + component.getDescription());
							}
						},
						(component, parameters) -> {
							/* ------------------------------------------------------------------------------ */
							Collection<Instance> instances =
									getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
							Class<? extends Calculation<?>> calculationClass =
									getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS);
							/* ------------------------------------------------------------------------------ */
							return new Object[]{
									instances instanceof ArrayList ? instances : new ArrayList<>(instances),
									calculationClass.newInstance()
							};
						},
						(component, result) -> {
							/* ------------------------------------------------------------------------------ */
							int r = 0;
							Collection<Instance> instances = (Collection<Instance>) result[r++];
							Calculation<?> calculation = (Calculation<?>) result[r++];
							/* ------------------------------------------------------------------------------ */
							getParameters().set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances);
							getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
							/* ------------------------------------------------------------------------------ */
							// Statistics
							/* ------------------------------------------------------------------------------ */
							// Report
							/* ------------------------------------------------------------------------------ */
						}
				) {
					@Override
					public void init() {
					}

					@Override
					public String staticsName() {
						return shortName() + " | 1. of " + getComponents().size() + "." + " " + getDescription();
					}
				}.setDescription("Pre-initialization"),
				// 2. Compact
				new TimeCountedProcedureComponent<Map<IntArrayKey, EquivalenceClass>>(
						ComponentTags.TAG_COMPACT,
						this.getParameters(),
						(component) -> {
							if (logOn){
								log.info("2. " + component.getDescription());
							}
							component.setLocalParameters(new Object[]{
									getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
									getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
							});
						},
						false,
						(component, parameters) -> {
							/* ------------------------------------------------------------------------------ */
							int p = 0;
							Collection<Instance> instances =
									(Collection<Instance>) parameters[p++];
							int[] attributes =
									(int[]) parameters[p++];
							/* ------------------------------------------------------------------------------ */
							TimerUtils.timeStart((TimeCounted) component);
							/* ------------------------------------------------------------------------------ */
							return ActiveSampleSelectionAlgorithm
									.Basic
									.equivalenceClasses(
											instances,
											new IntegerArrayIterator(attributes)
									);
						},
						(component, equClasses) -> {
							/* ------------------------------------------------------------------------------ */
							getParameters().setNonRoot("equClasses", equClasses);
							/* ------------------------------------------------------------------------------ */
							// Statistics
							//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
							Collection<Instance> Instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
									BasicExecutionInstanceInfo.Builder.newBuilder()
											.loadCurrentInfo(Instances, false)
											.setCompressedInstanceNumber(equClasses.size())
											.setExecutedRecordNumberNumber(equClasses.size(), EquivalenceClass.class)
							);
							/* ------------------------------------------------------------------------------ */
							// Report
							/* ------------------------------------------------------------------------------ */
						}
				) {
					@Override
					public void init() {
					}

					@Override
					public String staticsName() {
						return shortName() + " | 2. of " + getComponents().size() + "." + " " + getDescription();
					}
				}.setDescription("Compact"),
				// 3. Calculate DIS({a}), DIS({A})
				new TimeCountedProcedureComponent<Object[]>(
						ComponentTags.TAG_SIG,
						this.getParameters(),
						(component) -> {
							if (logOn) log.info("3. " + component.getDescription());
							component.setLocalParameters(new Object[]{
									getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
									getParameters().get("equClasses"),
									getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
							});
						},
						false,
						(component, parameters) -> {
							/* ------------------------------------------------------------------------------ */
							int p = 0;
							List<Instance> instances =
									(List<Instance>) parameters[p++];
							Map<IntArrayKey, EquivalenceClass> equClasses =
									(Map<IntArrayKey, EquivalenceClass>) parameters[p++];
							int[] attributes =
									(int[]) parameters[p++];
							/* ------------------------------------------------------------------------------ */
							TimerUtils.timeStart((TimeCounted) component);
							/* ------------------------------------------------------------------------------ */
							AttrDiscernibilityResult attrDiscernibilityResult =
									ActiveSampleSelectionAlgorithm
											.Basic
											.attributeDiscernibility(
													equClasses, instances.size(),
													attributes
											);
							List<SamplePair> allAttrDiscernibility =
									new ArrayList<>(
											ActiveSampleSelectionAlgorithm
													.Basic
													.SamplePairs
													.distinct(
															attrDiscernibilityResult.getDiscernibilities(),
															attrDiscernibilityResult.getSamplePairNumber()
													)
									);
							return new Object[]{
									attrDiscernibilityResult,
									allAttrDiscernibility
							};
						},
						(component, result) -> {
							/* ------------------------------------------------------------------------------ */
							int r = 0;
							AttrDiscernibilityResult attrDiscernibilityResult = (AttrDiscernibilityResult) result[r++];
							List<SamplePair> allAttrDiscernibility = (List<SamplePair>) result[r++];
							/* ------------------------------------------------------------------------------ */
							getParameters().setNonRoot("attrDiscernibilityResult", attrDiscernibilityResult);
							getParameters().setNonRoot("allAttrDiscernibility", allAttrDiscernibility);
							/* ------------------------------------------------------------------------------ */
							// Statistics
							/* ------------------------------------------------------------------------------ */
							// Report
							/* ------------------------------------------------------------------------------ */
						}
				) {
					@Override
					public void init() {}

					@Override
					public String staticsName() {
						return shortName() + " | 3. of " + getComponents().size() + "." + " " + getDescription();
					}
				}.setDescription("Calculate DIS({a}), DIS({A})"),
				// 4. Sort elements in DIS(A)
				new TimeCountedProcedureComponent<Map<SamplePair, SamplePairAttributeInfo>>(
						ComponentTags.TAG_SIG,
						this.getParameters(),
						(component) -> {
							if (logOn){
								log.info("4. " + component.getDescription());
							}
							component.setLocalParameters(new Object[]{
									getParameters().get("attrDiscernibilityResult"),
									getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
									getParameters().get("allAttrDiscernibility"),
							});
						},
						false,
						(component, parameters) -> {
							/* ------------------------------------------------------------------------------ */
							int p = 0;
							AttrDiscernibilityResult attrDiscernibilityResult =
									(AttrDiscernibilityResult) parameters[p++];
							int[] attributes =
									(int[]) parameters[p++];
							List<SamplePair> allAttrDiscernibility =
									(List<SamplePair>) parameters[p++];
							/* ------------------------------------------------------------------------------ */
							TimerUtils.timeStart((TimeCounted) component);
							/* ------------------------------------------------------------------------------ */
							Map<SamplePair, SamplePairAttributeInfo> samplePairAttributesInfo =
									Collections.unmodifiableMap(
											ActiveSampleSelectionAlgorithm
													.Basic
													.SamplePairs
													.collectAttributes(
															attrDiscernibilityResult.getDiscernibilities(),
															attrDiscernibilityResult.getSamplePairNumber(),
															attributes
													)
									);
							Collections.sort(
									allAttrDiscernibility,
									(d1, d2) -> samplePairAttributesInfo.get(d1).getFrequency() - samplePairAttributesInfo.get(d2).getFrequency()
							);
							return samplePairAttributesInfo;
						},
						(component, samplePairAttributesInfo) -> {
							/* ------------------------------------------------------------------------------ */
							getParameters().setNonRoot("samplePairAttributesInfo", samplePairAttributesInfo);
							/* ------------------------------------------------------------------------------ */
							// Statistics
							/* ------------------------------------------------------------------------------ */
							// Report
							/* ------------------------------------------------------------------------------ */
						}
				) {
					@Override
					public void init() {
					}

					@Override
					public String staticsName() {
						return shortName() + " | 4. of " + getComponents().size() + "." + " " + getDescription();
					}
				}.setDescription("Sort elements in DIS(A)"),
				// 5. Heuristic search loop.
				new ProcedureComponent<Collection<Integer>>(
						ComponentTags.TAG_SIG,
						this.getParameters(),
						(component) -> {
							if (logOn) {
								log.info("5. " + component.getDescription());
							}
						},
						(component, parameters) -> {
							return (Collection<Integer>)
									CollectionUtils.firstOf(component.getSubProcedureContainers().values())
													.exec();
						},
						(component, reduct) -> {
							/* ------------------------------------------------------------------------------ */
							// Statistics
							/* ------------------------------------------------------------------------------ */
							// Report
							/* ------------------------------------------------------------------------------ */
						}
				) {
					@Override
					public void init() {
					}

					@Override
					public String staticsName() {
						return shortName() + " | 5. of " + getComponents().size() + "." + " " + getDescription();
					}
				}.setDescription("Heuristic search loop")
						.setSubProcedureContainer(
								"SignificantAttributeSeekingLoopProcedureContainer",
								new SignificantAttributeSeekingLoopProcedureContainer(getParameters(), logOn)
						),
				// 6. Inspection.
				new ProcedureComponent<Collection<Integer>>(
						ComponentTags.TAG_CHECK,
						this.getParameters(),
						(component) -> {
							if (logOn) {
								log.info("6. " + component.getDescription());
							}
						},
						(component, parameters) -> {
							return (Collection<Integer>)
									CollectionUtils.firstOf(component.getSubProcedureContainers().values())
													.exec();
						},
						(component, red) -> {
							/* ------------------------------------------------------------------------------ */
							if (logOn) {
								log.info(
										LoggerUtil.spaceFormat(2, "|Reduct Finally| = {}"),
										red.size()
								);
							}
							/* ------------------------------------------------------------------------------ */
							// Statistics
							/* ------------------------------------------------------------------------------ */
							// Report
							//	[DatasetRealTimeInfo]
							ProcedureUtils.Report.DatasetRealTimeInfo.save(
									report, component.getDescription(),
									((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(),
									0,
									red.size()
							);
							//	[REPORT_EXECUTION_TIME]
							long time = ProcedureUtils.Time.sumProcedureComponentTimes(component);
							ProcedureUtils.Report.ExecutionTime.save(
									report, component.getDescription(), time
							);
							/* ------------------------------------------------------------------------------ */
						}
				) {
					@Override
					public void init() {}

					@Override
					public String staticsName() {
						return shortName() + " | 6. of " + getComponents().size() + "." + " " + getDescription();
					}
				}.setDescription("Inspection")
						.setSubProcedureContainer(
								"ReductInspectionProcedureContainer",
								new ReductInspectionProcedureContainer<>(getParameters(), logOn)
						),
		};
	}

	public long getTime() {
		return getComponents().stream()
				.map(comp -> ProcedureUtils.Time.sumProcedureComponentTimes(comp))
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
