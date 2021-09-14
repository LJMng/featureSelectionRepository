package featureSelection.tester.procedure.heuristic.activeSampleSelection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
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
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.ActiveSamplePairSelectionStrategy;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.repository.algorithm.alg.activeSampleSelection.ActiveSampleSelectionAlgorithm;
import featureSelection.repository.entity.alg.activeSampleSelection.EquivalenceClass;
import featureSelection.repository.entity.alg.activeSampleSelection.incrementalAttributeReductionResult.ASSResult4Incremental;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePairSelectionResult;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.directDependencyCalculation.PositiveRegion4DDCHash;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.activeSampleSelection.procedure.ReductInspectionProcedureContainer4MinimalElements;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Sample Pair Selection</strong> for Attribute Reduction
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
 * 		<p>Initiate {@link Instance} {@link ArrayList} if the given universe is
 * 			not wrapped in {@link ArrayList}.
 * 	</li>
 * 	<li>
 * 		<strong>Compact</strong>:
 * 		<p>Compact {@link Instance}s into {@link EquivalenceClass}es.
 * 	</li>
 * 	<li>
 * 		<strong>Minimal Elements initialization</strong>:
 * 		<p>Initiate the minimal elements using
 *            {@link ActiveSampleSelectionAlgorithm.Basic#aSamplePairSelection(List, Map, int[])}.
 * 	</li>
 * 	<li>
 * 		<strong>Core</strong>:
 * 		<p>Get the core based on <code>minimal elements</code>. Separate core from the
 * 			rest of the minimal elements.
 * 	</li>
 * 	<li>
 * 		<strong>Reduct loading</strong>:
 * 		<p>Loop and load attributes into reduct. For each round, frequencies of sample
 * 			pairs are counted and sample pairs whose minimal elements are outside of
 * 			Core and reduct are sorted based on their frequencies. The 1st one is
 * 			selected at each round to add into reduct and sample pairs remain
 * 			indiscerned are preserved for the next round to obtain more attributes into
 * 			reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Inspection</strong>:
 * 		<p>Inspect the reduct and remove redundant attributes based on minimal elements.
 * 		<p><code>ReductInspectionProcedureContainer4MinimalElements</code>
 * 	</li>
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
 * @see ReductInspectionProcedureContainer4MinimalElements
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class SamplePairSelectionBasedAttributeReductionHeuristicQRTester
		extends DefaultProcedureContainer<ASSResult4Incremental>
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

	public SamplePairSelectionBasedAttributeReductionHeuristicQRTester(
			ProcedureParameters paramaters, boolean logOn
	) {
		super(logOn ? log : null, paramaters);
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
				new ProcedureComponent<Collection<Instance>>(
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
							/* ------------------------------------------------------------------------------ */
							return instances instanceof ArrayList ?
									instances : new ArrayList<>(instances);
						},
						(component, universes) -> {
							/* ------------------------------------------------------------------------------ */
							getParameters().set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, universes);
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
							if (logOn) {
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
				// 3. Minimal Elements initialization
				new TimeCountedProcedureComponent<SamplePairSelectionResult>(
						ComponentTags.TAG_SIG,
						this.getParameters(),
						(component) -> {
							if (logOn) {
								log.info("3. " + component.getDescription());
							}
							component.setLocalParameters(new Object[]{
									getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
									getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
									getParameters().get("equClasses")
							});
						},
						false,
						(component, parameters) -> {
							/* ------------------------------------------------------------------------------ */
							int p = 0;
							List<Instance> instances =
									(List<Instance>) parameters[p++];
							int[] attributes =
									(int[]) parameters[p++];
							Map<IntArrayKey, EquivalenceClass> equClasses =
									(Map<IntArrayKey, EquivalenceClass>) parameters[p++];
							/* ------------------------------------------------------------------------------ */
							TimerUtils.timeStart((TimeCounted) component);
							/* ------------------------------------------------------------------------------ */
							return ActiveSampleSelectionAlgorithm
									.Basic
									.aSamplePairSelection(
											instances, equClasses, attributes
									);
							/* ------------------------------------------------------------------------------ */
						},
						(component, result) -> {
							/* ------------------------------------------------------------------------------ */
							getParameters().setNonRoot("samplePairSelection", result);
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
						return shortName() + " | 3. of " + getComponents().size() + "." + " " + getDescription();
					}
				}.setDescription("Minimal Elements initialization"),
				// 4. Core
				new TimeCountedProcedureComponent<Object[]>(
						ComponentTags.TAG_CORE,
						this.getParameters(),
						(component) -> {
							if (logOn) log.info("4. " + component.getDescription());
							component.setLocalParameters(new Object[]{
									getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
									getParameters().get("samplePairSelection"),
							});
						},
						false,
						(component, parameters) -> {
							/* ------------------------------------------------------------------------------ */
							int p = 0;
							int[] attributes =
									(int[]) parameters[p++];
							SamplePairSelectionResult samplePairSelection =
									(SamplePairSelectionResult) parameters[p++];
							/* ------------------------------------------------------------------------------ */
							TimerUtils.timeStart((TimeCounted) component);
							/* ------------------------------------------------------------------------------ */
							// initiate reduct.
							Collection<Integer> reduct = new LinkedList<>();
							// Get core
							Collection<?>[] speratedAttributes =
									ActiveSampleSelectionAlgorithm
											.separateCoreNOtherAttributes(
													samplePairSelection,
													attributes.length
											);
							Collection<Integer> core = (Collection<Integer>) speratedAttributes[0];
							reduct.addAll(core);
							List<Collection<Integer>> samplePairSelectionLeft =
									(List<Collection<Integer>>) speratedAttributes[1];
							return new Object[]{reduct, core, samplePairSelectionLeft};
							/* ------------------------------------------------------------------------------ */
						},
						(component, result) -> {
							/* ------------------------------------------------------------------------------ */
							int r = 0;
							Collection<Integer> reduct = (Collection<Integer>) result[r++];
							Collection<Integer> core = (Collection<Integer>) result[r++];
							List<Collection<Integer>> samplePairSelectionLeft = (List<Collection<Integer>>) result[r++];
							/* ------------------------------------------------------------------------------ */
							if (logOn) {
								log.info(LoggerUtil.spaceFormat(2, "|Core| = {}"), core.size());
								log.debug("Core(size={}): {}", core.size(), core);
							}
							/* ------------------------------------------------------------------------------ */
							getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
							getParameters().setNonRoot("samplePairSelectionLeft", samplePairSelectionLeft);
							/* ------------------------------------------------------------------------------ */
							// Statistics
							//	[Core]
							statistics.put(StatisticsConstants.Procedure.STATISTIC_CORE_LIST, core.stream().toArray(Integer[]::new));
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
				}.setDescription("Calculate DIS({a}), DIS({A})"),
				// 5. Reduct loading.
				new TimeCountedProcedureComponent<Collection<Integer>>(
						ComponentTags.TAG_SIG,
						this.getParameters(),
						(component) -> {
							if (logOn) {
								log.info("5. " + component.getDescription());
							}
							component.setLocalParameters(new Object[]{
									getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
									getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
									getParameters().get("samplePairSelectionLeft"),
							});
						},
						false,
						(component, parameters) -> {
							/* ------------------------------------------------------------------------------ */
							int p = 0;
							int[] attributes =
									(int[]) parameters[p++];
							Collection<Integer> reduct =
									(Collection<Integer>) parameters[p++];
							List<Collection<Integer>> samplePairSelectionLeft =
									(List<Collection<Integer>>) parameters[p++];
							/* ------------------------------------------------------------------------------ */
							TimerUtils.timeStart((TimeCounted) component);
							/* ------------------------------------------------------------------------------ */
							// Loop and add attributes into reduct
							int attr, loopCount = 1;
							Iterator<Collection<Integer>> samplePairSelectionLeftIterator;
							while (!samplePairSelectionLeft.isEmpty()) {
								if (logOn) {
									log.info(
											LoggerUtil.spaceFormat(2, "Round {}, |reduct| = {}, {} sample pair(s) left."),
											loopCount++, reduct.size(),
											samplePairSelectionLeft.size()
									);
								}
								// Select the most frequently a in C.
								final Map<Integer, Integer> attrFreq = new HashMap<>(attributes.length - reduct.size());
								samplePairSelectionLeft.stream().forEach(collection -> {
									collection.forEach(a -> {
										Integer frequence = attrFreq.get(a);
										attrFreq.put(a, frequence == null ? 1 : frequence + 1);
									});
								});
								attr = attrFreq.entrySet().stream().max(
										(entry1, entry2) -> entry1.getValue() - entry2.getValue()
									).get().getKey();
								// reduct = reduct U {a}.
								reduct.add(attr);
								// C = C - { C(i, j): a in C(i, j) in C }
								samplePairSelectionLeftIterator = samplePairSelectionLeft.iterator();
								while (samplePairSelectionLeftIterator.hasNext()) {
									Collection<Integer> cij = samplePairSelectionLeftIterator.next();
									if (cij.contains(attr)) samplePairSelectionLeftIterator.remove();
								}
							}
							return reduct;
						},
						(component, reduct) -> {
							/* ------------------------------------------------------------------------------ */
							if (logOn) {
								log.info(
										LoggerUtil.spaceFormat(2, "|reduct| = {}"),
										reduct.size()
								);
							}
							/* ------------------------------------------------------------------------------ */
							// Statistics
							//	[STATISTIC_RED_BEFORE_INSPECT]
							statistics.put(StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT, reduct);
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
				}.setDescription("Reduct loading"),
				// 6. Inspection.
				new ProcedureComponent<ASSResult4Incremental>(
						ComponentTags.TAG_CHECK,
						this.getParameters(),
						(component) -> {
							if (logOn) log.info("6. " + component.getDescription());
							component.setLocalParameters(new Object[]{
									getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
									getParameters().get("samplePairSelection"),
									getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
							});
						},
						(component, parameters) -> {
							/* ------------------------------------------------------------------------------ */
							int p = 0;
							Collection<Instance> instances =
									(Collection<Instance>) parameters[p++];
							SamplePairSelectionResult samplePairSelection =
									(SamplePairSelectionResult) parameters[p++];
							Collection<Integer> reduct =
									(Collection<Integer>) parameters[p++];
							/* ------------------------------------------------------------------------------ */
							getParameters().setNonRoot("inspectAttributes", reduct.stream().mapToInt(v -> v).toArray());
							getParameters().setNonRoot("minimalElements", samplePairSelection.getSamplePairFamilyMap().keySet());
							/* ------------------------------------------------------------------------------ */
							reduct = (Collection<Integer>) component.getSubProcedureContainers().values().iterator().next().exec();
							ASSResult4Incremental result =
									new ASSResult4Incremental(
											reduct,
											samplePairSelection.getSamplePairFamilyMap()
									);
							result.setUpdatedUniverse(instances);
							return result;
						},
						(component, red) -> {
							/* ------------------------------------------------------------------------------ */
							if (logOn) {
								log.info(
										LoggerUtil.spaceFormat(2, "|Reduct Finally| = {}"),
										red.getReduct().size()
								);
								log.debug(
										"reduct after inspection:  {}",
										red.getReduct().stream().sorted().collect(Collectors.toList())
								);
							}
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
				}.setDescription("Inspection")
						.setSubProcedureContainer(
						"ReductInspectionProcedureContainer",
						new ReductInspectionProcedureContainer4MinimalElements(getParameters(), logOn)
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

	@SuppressWarnings("unused")
	private void validatePositiveRegion(
			Collection<Instance> instances, Collection<Integer> reduct,
			int[] attributes
	) {
		int globalPos = new PositiveRegion4DDCHash().calculate(
				instances,
				new IntegerArrayIterator(attributes)
		).getResult();
		if (logOn) log.debug("Pos of U/C   = {}", globalPos);
		int redPos = new PositiveRegion4DDCHash().calculate(
				instances,
				new IntegerCollectionIterator(reduct)
		).getResult();
		if (logOn) log.debug("Pos of U/red = {}", redPos);

		if (globalPos != redPos) {
			Collection<Instance> globalPosU = new HashSet<>();
			ActiveSampleSelectionAlgorithm
					.Basic
					.equivalenceClasses(instances, new IntegerArrayIterator(attributes))
					.values()
					.stream().filter(e -> e.getDecision() != null)
					.forEach(posu -> globalPosU.addAll(posu.getUniverses()));

			Collection<Instance> redPosU = new HashSet<>();
			Collection<EquivalenceClass> redEqu =
					ActiveSampleSelectionAlgorithm
							.Basic
							.equivalenceClasses(instances, new IntegerCollectionIterator(reduct))
							.values();
			redEqu.stream().filter(e -> e.getDecision() != null)
					.forEach(posu -> redPosU.addAll(posu.getUniverses()));

			globalPosU.removeAll(redPosU);
			System.out.println("U/red Pos missing: " + globalPosU.size());
			globalPosU.stream().forEach(System.out::println);
			redEqu.stream().filter(e -> {
				for (Instance u : globalPosU) if (e.getUniverses().contains(u)) return true;
				return false;
			}).forEach(e -> {
				System.out.println("Equ class: ");
				e.getUniverses().forEach(System.out::println);
			});//*/

			throw new RuntimeException("Invalid positive region: " + globalPos + " is required, get " + redPos);
		}
	}
}