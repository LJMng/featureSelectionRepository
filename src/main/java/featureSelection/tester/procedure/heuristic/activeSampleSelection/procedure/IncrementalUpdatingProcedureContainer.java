package featureSelection.tester.procedure.heuristic.activeSampleSelection.procedure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.algorithm.alg.activeSampleSelection.ActiveSampleSelectionAlgorithm;
import featureSelection.repository.entity.alg.activeSampleSelection.EquivalenceClass;
import featureSelection.repository.entity.alg.activeSampleSelection.incrementalAttributeReductionResult.ASSResult4Incremental;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePair;
import featureSelection.repository.entity.alg.activeSampleSelection.samplePair.SamplePairSelectionResult4Incremental;
import featureSelection.repository.support.calculation.alg.FeatureImportance4ActiveSampleSelection;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Incremental updating for <strong>Active Sample Selection (ASS)</strong> based
 * Attribute Reduction(for <strong>Dynamic incremental Data</strong>).
 * This procedure contains 8 ProcedureComponents: 
 * <ul>
 * 	<li>
 * 		<strong>Update controller</strong>
 * 		<p>Control loop over new {@link Instance}s and update reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Locate the equivalence class of new Universe in U/C</strong>
 * 		<p>Search the equivalence class in U∪{x}/C. <==> Search the equivalence class in U/C and add {x} into
 * 			the equivalence class(implemented using this one) if exists, otherwise return {x}.
 * 	</li>
 * 	<li>
 * 		<strong>Update Minimal Elements</strong>
 * 		<p>Update the selected Sample pairs and the corresponding Minimal Elements for the new 
 * 			{@link Instance}.
 * 	</li>
 * 	<li>
 * 		<strong>Initializations for reduct update</strong>
 * 		<p>Initializations and preparations for the reduct updating: 3 collections are initiated as "a", "b",
 * 			"s" according to the original paper for reduct updating.
 * 	</li>
 * 	<li>
 * 		<strong>Case dispenser</strong>
 * 		<p>Check out whether the current situation is case 3 or case 4.
 * 	</li>
 * 	<li>
 * 		<strong>Case 3</strong>
 * 		<p>Actions for case 3: <i>|d([x]<sub>C</sub>-{x})|=1 and |d([x]<sub>C</sub>)|>1</i>.
 * 	</li>
 * 	<li>
 * 		<strong>Case 4</strong>
 * 		<p>Actions for case 4: <i>[x]<sub>C</sub>={x}</i>.
 * 	</li>
 * 	<li>
 * 		<strong>Inspection</strong>: 
 * 		<p>Inspect the new reduct.
 * 	</li>
 * </ul>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_COLLECTION_ITEM}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_REDUCT}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ASE_SAMPLE_PAIR_SELECTION}</li>
 * 	<li>equClasses</li>
 * 	<li>incrementalASEResult</li>
 * </ul>
 * <p>
 * <strong>Notice</strong>: Please use {@link ArrayList} to contain {@link Instance}s as
 * <strong>{@link ParameterConstants#PARAMETER_PREVIOUS_COLLECTION_ITEM}.</strong>
 * 
 * @author Benjamin_L
 */
@Slf4j
public class IncrementalUpdatingProcedureContainer<Sig>
	extends DefaultProcedureContainer<ASSResult4Incremental>
	implements StatisticsCalculated
{
	private boolean logOn;
	
	private Map<String, Object> localParameters;
	@Getter private Statistics statistics;
	
	public IncrementalUpdatingProcedureContainer(
			ProcedureParameters parameters, boolean logOn
	) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		
		localParameters = new HashMap<>();
		statistics = new Statistics();
	}

	@Override
	public String shortName() {
		return "Incremental Updating for ASE";
	}

	@Override
	public String staticsName() {
		return shortName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Update controller
			new TimeCountedProcedureComponent<ASSResult4Incremental>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(LoggerUtil.spaceFormat(1, "1/{}. {}"),
									getComponents().size(), component.getDescription()
							);
						}
						
						Collection<Instance> previousInstances =
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM);
						
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								previousInstances,
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get("equClasses"),
								getParameters().get("incrementalASEResult"),
						});
						
						localParameters.put("previousInstancesSize", previousInstances.size());
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> newInstances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						List<Instance> previousInstances =
								(List<Instance>) parameters[p++];
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						Map<IntArrayKey, EquivalenceClass> equClasses =
								(Map<IntArrayKey, EquivalenceClass>) parameters[p++];
						ASSResult4Incremental incrementalASEResult =
								(ASSResult4Incremental) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						int newInstanceCount = 0;
						List<ProcedureComponent<?>> comps = getComponents();
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						for (Instance newInstance: newInstances) {
							TimerUtils.timePause((TimeCounted) component);
							if (logOn) {
								newInstanceCount++;
								log.info(
										LoggerUtil.spaceFormat(1, "Add {}): Instance #{}"),
										newInstanceCount, newInstance.getNum()
								);
							}
							TimerUtils.timeContinue((TimeCounted) component);
							
							// cr = oneSampleSelection()
							if (!ActiveSampleSelectionAlgorithm
									.Basic
									.Incremental
									.insIsUseful(equClasses, newInstance)
							) {
								TimerUtils.timePause((TimeCounted) component);
								if (logOn){
									log.info(LoggerUtil.spaceFormat(2, "× Instance is [USELESS]"));
								}
								TimerUtils.timeContinue((TimeCounted) component);
								// New Instance is useless.
								// U = U, red<sub>x</sub> = red
							}else {
								// New Instance is useful.
								TimerUtils.timePause((TimeCounted) component);
								
								if (logOn){
									log.info(LoggerUtil.spaceFormat(2, "√ Instance is [USEFUL]"));
								}
								localParameters.put("newInstance", newInstance);

								for (int i=1; i<=3; i++)	comps.get(i).exec();
								int caseIndex = (Integer) comps.get(4).exec();
								Object[] objs = (Object[]) comps.get(caseIndex).exec();
								
								if (((boolean) objs[0])) {
									incrementalASEResult = (ASSResult4Incremental) objs[1];
								}else {
									// Inspection
									Collection<Integer> reduct = (Collection<Integer>) comps.get(7).exec();
									
									SamplePairSelectionResult4Incremental samplePairSelection4Inc =
											(SamplePairSelectionResult4Incremental) 
											localParameters.get("samplePairSelection4Inc");
									incrementalASEResult =
											new ASSResult4Incremental(
													reduct, samplePairSelection4Inc.getUpdatedSamplePairFamilyMap()
											);
								}
								// Update previous reduct.
								getParameters().setNonRoot(ParameterConstants.PARAMETER_ASE_SAMPLE_PAIR_SELECTION, incrementalASEResult);
								getParameters().setNonRoot(ParameterConstants.PARAMETER_PREVIOUS_REDUCT, incrementalASEResult.getReduct());
								if (logOn) {
									log.info(
											LoggerUtil.spaceFormat(2, "|Red|: x{}"),
											incrementalASEResult.getReduct().size()
									);
								}
								
								TimerUtils.timeContinue((TimeCounted) component);
							}
						}
						// if it is the first time using incremental and no update was made to ME.
						//	still need to initialize ME.
						if (incrementalASEResult==null) {
							incrementalASEResult = 
								new ASSResult4Incremental(
										previousReduct, 
										ActiveSampleSelectionAlgorithm
											.Basic
											.aSamplePairSelection(
												previousInstances,
												equClasses, 
												attributes
											).getSamplePairFamilyMap()
									);
						}
						// return updated universe instances too.
						incrementalASEResult.setUpdatedUniverse(previousInstances);
						return incrementalASEResult;
					}, 
					(component, aseResult) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						@SuppressWarnings("unused")
						int previousInstancesSize = (int) localParameters.get("previousInstancesSize");
						BasicExecutionInstanceInfo.Builder builder = 
							statistics.get(StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER);
						if (builder==null) {
							statistics.put(
								StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
								builder = getParameters().get(StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER)
							);
						}
						builder.setExecutedRecordNumberNumber(
							aseResult.getUpdatedUniverse().size(), //- previousInstancesSize,
							Instance.class
						);
						//	[PARAMETER_REDUCT_LIST]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								aseResult.getReduct()
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update controller"),
			// 2. Locate the equivalence class of new Instance in U/C
			new TimeCountedProcedureComponent<EquivalenceClass[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "2/{}. {}"),
									getComponents().size(), component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get("equClasses"),
								localParameters.get("newInstance"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						Map<IntArrayKey, EquivalenceClass> equClasses =
								(Map<IntArrayKey, EquivalenceClass>) parameters[p++];
						Instance newInstance =
								(Instance) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Search h in U/C, where h=[x]c is the equivalence class in
						//  U/C which share the same condition description as x:
						//  [x]c - {x}
						IntArrayKey key = new IntArrayKey(newInstance.getConditionAttributeValues());
						EquivalenceClass previousEquClass = equClasses.get(key);
						if (previousEquClass!=null){
							previousEquClass = previousEquClass.clone();
						}
						// [x]red = equivalenceClassOfnewInstance(U, red, x)
						EquivalenceClass equClassWithNewIns =
							ActiveSampleSelectionAlgorithm
								.Basic
								.Incremental
								.equivalenceClassWithUniverse(
									instances,
									new IntegerCollectionIterator(previousReduct),
									newInstance
								);
						return new EquivalenceClass[] {
								previousEquClass,	// [x]<sub>C</sub>-{x} (Clone version)
								equClassWithNewIns	// [x]<sub>red</sub>
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						EquivalenceClass previousEquClass = result[r++];
						EquivalenceClass equClassWithNewU = result[r++];
						/* ------------------------------------------------------------------------------ */
						localParameters.put("previousEquClass", previousEquClass);
						localParameters.put("equClassWithNewU", equClassWithNewU);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Locate the equivalence class of new Universe in U/C"),
			// 3. Update Minimal Elements
			new TimeCountedProcedureComponent<SamplePairSelectionResult4Incremental>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "3/{}. {}"),
									getComponents().size(), component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_ASE_SAMPLE_PAIR_SELECTION),
								localParameters.get("newInstance"),
								getParameters().get("equClasses"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int[] attributes =
								(int[]) parameters[p++];
						ASSResult4Incremental previousSamplePairResult = 
								(ASSResult4Incremental) parameters[p++];
						Instance newInstance =
								(Instance) parameters[p++];
						Map<IntArrayKey, EquivalenceClass> equClasses = 
								(Map<IntArrayKey, EquivalenceClass>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Map<IntArrayKey, Collection<SamplePair>> minimalElements =
								previousSamplePairResult.getSamplePairInfo();

						// Update minimal elements and sample pairs.
						Map<IntArrayKey, Collection<SamplePair>> samplePairSelection4Inc = 
							ActiveSampleSelectionAlgorithm
								.Basic
								.Incremental
								.MinimalElementsUpdating
								.execute(
									minimalElements,
									newInstance,
									equClasses, 
									new IntegerArrayIterator(attributes)
								);
						
						return new SamplePairSelectionResult4Incremental(
									samplePairSelection4Inc, equClasses
								);
					}, 
					(component, samplePairSelection4Inc) -> {
						/* ------------------------------------------------------------------------------ */
						localParameters.put("samplePairSelection4Inc", samplePairSelection4Inc);
						Collection<Integer> previousReduct =
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT);
						localParameters.put("newReduct", new LinkedList<>(previousReduct));
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Update Minimal Elements"),
			// 4. Initializations for reduct update
			new TimeCountedProcedureComponent<Collection<Integer>[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "4/{}. {}"),
									getComponents().size(), component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int[] attributes =
								(int[]) parameters[p++];
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// a: previous reduct
						Collection<Integer> a = new HashSet<>(previousReduct);
						// b: new attributes added into the reduct
						Collection<Integer> b = new LinkedList<>();
						// s: potential attributes for adding into reduct
						Collection<Integer> s = Arrays.stream(attributes).boxed().filter(attr->!a.contains(attr)).collect(Collectors.toSet());
						return new Collection[] {	a, b, s	};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Integer> a = result[r++];
						Collection<Integer> b = result[r++];
						Collection<Integer> s = result[r++];
						localParameters.put("a", a);
						localParameters.put("b", b);
						localParameters.put("s", s);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initializations for reduct update."),
			// 5. Case dispenser
			new TimeCountedProcedureComponent<Integer>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "5/{}. {}"),
									getComponents().size(), component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								localParameters.get("newInstance"),
								localParameters.get("previousEquClass"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Instance newInstance =
								(Instance) parameters[p++];
						EquivalenceClass previousEquClass =
								(EquivalenceClass) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return (previousEquClass!=null && 
								previousEquClass.getDecision()!=null &&
								previousEquClass.getDecision()!=newInstance.getAttributeValue(0))?
								5: 6;
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Case dispenser"),
			// 6. Case 3
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "6/{}. {}"), getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM),
								getParameters().get("equClasses"),
								localParameters.get("newInstance"),
								localParameters.get("equClassWithNewU"),
								localParameters.get("previousEquClass"),
								localParameters.get("samplePairSelection4Inc"),
								localParameters.get("newReduct"),
								localParameters.get("a"),
								localParameters.get("b"),
								localParameters.get("s"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						FeatureImportance4ActiveSampleSelection<Integer> calculation =
								(FeatureImportance4ActiveSampleSelection<Integer>) 
								parameters[p++];
						Collection<Instance> previousUniverse =
								(Collection<Instance>) parameters[p++];
						Map<IntArrayKey, EquivalenceClass> equClasses =
								(Map<IntArrayKey, EquivalenceClass>) parameters[p++];
						Instance newInstance =
								(Instance) parameters[p++];
						EquivalenceClass equClassWithNewU =
								(EquivalenceClass) parameters[p++];
						EquivalenceClass previousEquClass =
								(EquivalenceClass) parameters[p++];
						SamplePairSelectionResult4Incremental samplePairSelection4Inc = 
								(SamplePairSelectionResult4Incremental) parameters[p++];
						Collection<Integer> newReduct =
								(Collection<Integer>) parameters[p++];
						Collection<Integer> a =
								(Collection<Integer>) parameters[p++];
						Collection<Integer> b =
								(Collection<Integer>) parameters[p++];
						Collection<Integer> s =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<Collection<Integer>> comp7 = (ProcedureComponent<Collection<Integer>>) getComponents().get(7);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// If [x]c==[x]red: if previousEquClass equals to equClassWithNewU without the new Universe.
						//	==> if [x]c - {x} == [x]red - {x}
						equClassWithNewU.getUniverses().remove(newInstance);
						if (ActiveSampleSelectionAlgorithm
								.Basic
								.Incremental
								.equivalenceClassesEquals(
									previousEquClass.getUniverses(), 
									equClassWithNewU.getUniverses()
								)
						) {
							// add x into the correspondent equivalence class and
							//  update its consistency.
							previousUniverse.add(newInstance);
							EquivalenceClass equClass4newInstance =
								equClasses.get(
									new IntArrayKey(newInstance.getConditionAttributeValues())
								);
							equClass4newInstance.addUniverse(newInstance);
							equClass4newInstance.setDecision(null);
							// execute Inspection
							TimerUtils.timePause((TimeCounted) component);
							localParameters.put("previousReduct", new HashSet<>(a));
							newReduct = comp7.exec();
							TimerUtils.timeContinue((TimeCounted) component);
							// return red<sub>x</sub>
							return new Object[] {
								true, 
								new ASSResult4Incremental(
									newReduct, 
									samplePairSelection4Inc.getUpdatedSamplePairFamilyMap()
								)
							};
						// else
						}else {
							equClassWithNewU.addUniverse(newInstance);
							// Calculate cmp = [x]red - [x]c
							Collection<Instance> cmp = new HashSet<>(equClassWithNewU.getUniverses());
							cmp.removeAll(previousEquClass.getUniverses());
							cmp.remove(newInstance);
							// Loop until |POS([x]red, B ∪ red<sub>x</sub>, D)|==
							//  |[x]red-[x]c|
							int pos;
							Collection<EquivalenceClass> cmpEquClasses;
							//	B ∪ red[x]
							Collection<Integer> bPlusNewReduct = new LinkedList<>(b);
							bPlusNewReduct.addAll(a);
							do {
								// Calculate POS([x]red, B ∪ red<sub>x</sub>, D)
								//	POS([x]red, B ∪ red<sub>x</sub>, D)
								cmpEquClasses = 
									ActiveSampleSelectionAlgorithm
										.Basic
										.equivalenceClasses(
											equClassWithNewU.getUniverses(), 
											new IntegerCollectionIterator(bPlusNewReduct)
										).values();
								pos = calculation.calculate(cmpEquClasses).getResult();
								// Check if |POS([x]red, B ∪ red<sub>x</sub>, D)|==
								//  |[x]red-[x]c|
								//	if does, break.
								if (pos==cmp.size())	break;
								// Loop over a[i] in S
								int[] bPlusNewReductPlusAi = new int[bPlusNewReduct.size()+1];
								int i=0;	for (int each: bPlusNewReduct)	bPlusNewReductPlusAi[i++] = each;
								int maxConditionalPositiveValue = 0, maxConditionalPositiveAttribute = -1;
								i=0;
								for (int a_i: s) {
									// Compute POS([x]red, B ∪ red<sub>x</sub> ∪ {a[i]}, D)
									//	B ∪ red[x] ∪ {a[i]}
									bPlusNewReductPlusAi[bPlusNewReductPlusAi.length-1] = a_i;
									//	POS([x]red, B ∪ red<sub>x</sub> ∪ {a[i]}, D)
									cmpEquClasses = 
										ActiveSampleSelectionAlgorithm
											.Basic
											.equivalenceClasses(
												equClassWithNewU.getUniverses(), 
												new IntegerArrayIterator(bPlusNewReductPlusAi)
											).values();
									pos = calculation.calculate(cmpEquClasses)
													.getResult();
									// Select the one with max POS as a[k]
									if (maxConditionalPositiveAttribute==-1 ||
											pos > maxConditionalPositiveValue
									) {
										maxConditionalPositiveValue = pos;
										maxConditionalPositiveAttribute = a_i;
									}
								}
								if (maxConditionalPositiveAttribute==-1) {
									throw new IllegalStateException("Illegal attribute to add into B: -1");
								}
								
								TimerUtils.timePause((TimeCounted) component);
								//	[STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH]
								ProcedureUtils.Statistics.push(
									statistics.getData(),
									StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH,
									bPlusNewReductPlusAi.length
								);								
								TimerUtils.timeContinue((TimeCounted) component);
								
								// B = [B, a[k]] and S = S - {a[k]}
								b.add(maxConditionalPositiveAttribute);
								bPlusNewReduct.add(maxConditionalPositiveAttribute);
								s.remove(maxConditionalPositiveAttribute);
							}while(true);
							// Update <code>newReduct</code> += b
							newReduct = bPlusNewReduct;
							TimerUtils.timePause((TimeCounted) component);
							localParameters.put("newReduct", newReduct);
							localParameters.put("previousReduct", a);	// Prepare for inspection.
							TimerUtils.timeContinue((TimeCounted) component);
							
							// add x into the correspondent equivalence class and update its consistency.
							previousUniverse.add(newInstance);
							EquivalenceClass equClass4newInstance =
								equClasses.get(
									new IntArrayKey(newInstance.getConditionAttributeValues())
								);
							equClass4newInstance.addUniverse(newInstance);
							equClass4newInstance.setDecision(null);
						}
						return new Object[] {	false, null	};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						boolean exit =
								(boolean) result[r++];
						ASSResult4Incremental aseResult =
								(ASSResult4Incremental) result[r++];
						localParameters.put("exit", exit);
						localParameters.put("aseResult", aseResult);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Case 3"),
			// 7. Case 4
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "7/{}. {}"),
									getComponents().size(), component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM),
								getParameters().get("equClasses"),
								localParameters.get("newInstance"),
								localParameters.get("equClassWithNewU"),
								localParameters.get("samplePairSelection4Inc"),
								localParameters.get("newReduct"),
								localParameters.get("a"),
								localParameters.get("b"),
								localParameters.get("s"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> Instances =
								(Collection<Instance>) parameters[p++];
						Collection<Instance> previousUniverse =
								(Collection<Instance>) parameters[p++];
						Map<IntArrayKey, EquivalenceClass> equClasses =
								(Map<IntArrayKey, EquivalenceClass>) parameters[p++];
						Instance newInstance =
								(Instance) parameters[p++];
						EquivalenceClass equClassWithNewU =
								(EquivalenceClass) parameters[p++];
						SamplePairSelectionResult4Incremental samplePairSelection4Inc = 
								(SamplePairSelectionResult4Incremental) parameters[p++];
						Collection<Integer> newReduct =
								(Collection<Integer>) parameters[p++];
						Collection<Integer> a =
								(Collection<Integer>) parameters[p++];
						Collection<Integer> b =
								(Collection<Integer>) parameters[p++];
						Collection<Integer> s =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// if |d([x]red)|=1
						if (equClassWithNewU.getDecision()!=null) {
							// add x into the correspondent equivalence class.
							previousUniverse.add(newInstance);
							EquivalenceClass equClass = new EquivalenceClass();
							equClass.addUniverse(newInstance);
							equClass.setDecision(newInstance.getAttributeValue(0));
							equClasses.put(
								new IntArrayKey(newInstance.getConditionAttributeValues()),
								equClass
							);
							// return red<sub>x</sub>
							return new Object[] {
								true, 
								new ASSResult4Incremental(
									newReduct, samplePairSelection4Inc.getUpdatedSamplePairFamilyMap()
								)
							};
						// else
						}else {
							// while |d([x]<sub>B ∪ red<sub>x</sub></sub>)|>1
							//	B ∪ red<sub>x</sub>
							Collection<Integer> bPlusNewReduct = new LinkedList<>(b);
							bPlusNewReduct.addAll(newReduct);
							//	[x]<sub>B ∪ red<sub>x</sub></sub>
							EquivalenceClass cmpEquClass = 
								ActiveSampleSelectionAlgorithm
									.Basic
									.Incremental
									.equivalenceClassWithUniverse(
										Instances,
										new IntegerCollectionIterator(bPlusNewReduct), 
										newInstance
									);
							boolean consistent = cmpEquClass.getDecision()!=null;
							while (!consistent) {
								// Loop over a[i] in S, compute |d([x]<sub>(a[i]∪B∪red[x]</sub>)|,
								//	select the one with minimum value as a[k].
								// Loop over a[i] in S
								int pos;
								int[] bPlusNewReductPlusAi = new int[bPlusNewReduct.size()+1];
								int i=0;	for (int each: bPlusNewReduct)	bPlusNewReductPlusAi[i++] = each;
								int maxConditionalPositiveValue = -1, maxConditionalPositiveAttribute = -1;
								i=0;
								for (int a_i: s) {
									// Compute POS([x]red, B ∪ red<sub>x</sub> ∪ {a[i]}, D)
									//	B ∪ red[x] ∪ {a[i]}
									bPlusNewReductPlusAi[bPlusNewReductPlusAi.length-1] = a_i;
									//	POS([x]red, B ∪ red<sub>x</sub> ∪ {a[i]}, D)
									cmpEquClass = 
										ActiveSampleSelectionAlgorithm
											.Basic
											.Incremental
											.equivalenceClassWithUniverse(
												Instances,
												new IntegerArrayIterator(bPlusNewReductPlusAi), 
												newInstance
											);
									pos = cmpEquClass.getDecision()==null? 0: cmpEquClass.getUniverses().size();
									// Select the one with max POS as a[k]
									if (pos>maxConditionalPositiveValue) {
										maxConditionalPositiveValue = pos;
										maxConditionalPositiveAttribute = a_i;
									}
								}
								if (maxConditionalPositiveAttribute==-1)
									throw new Exception("Illegal attribute to add into B: -1");
								
								TimerUtils.timePause((TimeCounted) component);
								//	[STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH]
								ProcedureUtils.Statistics.push(
									statistics.getData(),
									StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH,
									bPlusNewReductPlusAi.length
								);								
								TimerUtils.timeContinue((TimeCounted) component);
								
								// B = [B, a[k]] and S = S - {a[k]}
								b.add(maxConditionalPositiveAttribute);
								bPlusNewReduct.add(maxConditionalPositiveAttribute);
								s.remove(maxConditionalPositiveAttribute);
								// Update consistent status.
								consistent = maxConditionalPositiveValue>0;
							}
							// Update <code>newReduct</code> += b
							newReduct = bPlusNewReduct;
							TimerUtils.timePause((TimeCounted) component);
							localParameters.put("newReduct", newReduct);
							localParameters.put("previousReduct", a);	// Prepare for inspection.
							TimerUtils.timeContinue((TimeCounted) component);
							
							// add x into the correspondent equivalence class.
							previousUniverse.add(newInstance);
							EquivalenceClass equClass = new EquivalenceClass();
							equClass.addUniverse(newInstance);
							equClass.setDecision(newInstance.getAttributeValue(0));
							equClasses.put(
								new IntArrayKey(newInstance.getConditionAttributeValues()),
								equClass
							);
						}
						return new Object[] {	false, null	};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						boolean exit = (boolean) result[r++];
						ASSResult4Incremental aseResult = (ASSResult4Incremental) result[r++];
						localParameters.put("exit", exit);
						localParameters.put("aseResult", aseResult);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 7. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Case 4"),
			// 8. Inspection
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "8/{}. {}"), getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								localParameters.get("samplePairSelection4Inc"),
								localParameters.get("newReduct"),
								localParameters.get("previousReduct"),
								localParameters.get("a"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						SamplePairSelectionResult4Incremental samplePairSelection4Inc = 
								(SamplePairSelectionResult4Incremental) parameters[p++];
						Collection<Integer> newReduct =
								(Collection<Integer>) parameters[p++];
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						Collection<Integer> a =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return ActiveSampleSelectionAlgorithm
								.Inspection
								.execute(
									a.stream().mapToInt(v->v).toArray(), previousReduct, 
									newReduct, samplePairSelection4Inc.getSelectionAttributes()
								);//*/
						/* ------------------------------------------------------------------------------ */
					}, 
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 8. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection"),
		};
	}

	@Override
	public ASSResult4Incremental exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each: comps)	this.getComponents().add(each);
		return (ASSResult4Incremental) comps[0].exec();
	}
}