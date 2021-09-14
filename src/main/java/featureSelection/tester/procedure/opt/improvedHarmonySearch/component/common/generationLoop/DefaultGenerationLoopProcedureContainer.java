package featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.generationLoop;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.improvedHarmonySearch.GenerationRecord;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.HarmonyFactory;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.Fitness;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.ExitInfo;
import featureSelection.tester.statistics.info.iteration.optimization.BasicIterationInfo4Optimization;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.OptEntityBasicInfo;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.SupremeMark;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.SupremeMarkType;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Improved Harmony Search Algorithm</strong> loop executions. 
 * <p>
 * >> "best fitness == max fitness" <strong>NOT</strong> guaranteed. Not equal if exit through
 * reaching max convergence.
 * <p>
 * Mainly, this contains 5 {@link ProcedureComponent}s : 
 * <ul>
 * 	<li>
 * 		<strong>Generation loop procedure controller</strong>
 * 		<p>To control the generation loop, if <code>procedure[4]</code> returns <code>true</code>, i.e.
 * 	 	<strong>reaching max convergence</strong>, break the loop. Otherwise, loop until <strong>
 * 	 	reaching max iteration or max fitness</strong>.
 * 	 	<p>
 * 		 >> {@link ReductionParameters#getMaxDistinctBestFitness()} <strong>ignored</strong>.
 * 	</li>
 * 	<li>
 * 		<strong>Generation loop</strong>
 * 		<p>To control the current generation. Control the searching and harmonies to "study".
 * 	</li>
 * 	<li>
 * 		<strong>Loop and update harmony memory group attributes</strong>
 * 		<p>Update harmony by adding/removing attributes into/from current harmony memories.
 * 	</li>
 * 	<li>
 * 		<strong>Harmony fitnesses calculation</strong>
 * 		<p>Use the given Fitness calculation method in {@link ProcedureParameters} to calculate
 * 	    	fitnesses.
 * 	</li>
 * 	<li>
 * 		<strong>Check if break for reaching max convergence: true-break, false-continue.</strong>
 * 	</li>
 * </ul>
 * <p>
 * In this {@link DefaultProcedureContainer}(only!), the following parameters are used in {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_OPTIMIZATION_PARAMETERS}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_RANDOM_INSTANCE}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_GENERATION_RECORD}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_ALGORITHM}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 * 	<li>{@link ParameterConstants#PARAMETER_OPTIMIZATION_COLLECTION_ITEMS}</li>
 * </ul>
 * 
 * @see FeatureImportance
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <CollectionItem>
 * 		Universe or EquivalentClass.
 * @param <Hrmny>
 * 		Type of implemented {@link Harmony}.
 * @param <FValue>
 * 		Type of implemented {@link FitnessValue}.
 */
@Slf4j
public class DefaultGenerationLoopProcedureContainer<Cal extends FeatureImportance<Sig>,
													Sig extends Number, 
													CollectionItem,
													Hrmny extends Harmony<?>,
													FValue extends FitnessValue<Sig>>
	extends DefaultProcedureContainer<GenerationRecord<FValue>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	protected boolean logOn;
	@Getter protected Statistics statistics;
	@Getter protected Map<String, Map<String, Object>> report;
	protected Collection<String> reportKeys;
	
	protected Map<String, Object> localParameters;

	public DefaultGenerationLoopProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "IHS Generation loops";
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
			// 1. Generation loop procedure controller.
			new TimeCountedProcedureComponent<GenerationRecord<FValue>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
							});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Sig, Hrmny, FValue> params =
								(ReductionParameters<Sig, Hrmny, FValue>) parameters[p++];
						GenerationRecord<FValue> generRecord =
								(GenerationRecord<FValue>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp1 = getComponents().get(1);
						ProcedureComponent<Boolean> comp4 = (ProcedureComponent<Boolean>) getComponents().get(4);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						params.getBwAlg().reset();
						params.getParAlg().reset();

						Fitness<Sig, FValue> bestFitness;
						boolean exit = false, convergence = false;
						do {
							// iter++;
							generRecord.nextGeneration();
							
							TimerUtils.timePause((TimeCounted) component);
							comp1.exec();
							boolean breakLoop = comp4.exec();
							if (breakLoop)	break;
							else			bestFitness = getParameters().get("bestFitness");
							TimerUtils.timeContinue((TimeCounted) component);
							exit =	// if any is satisfied:
									//		reaching max convergence
									(convergence || 
									//		reaching max iteration
									 generRecord.getGeneration() >= params.getIteration() || 
									//		if max distinct is not set
									//			ignore this condition
									//		else
									//			reaching max distinct best fitness value
									//			(in the case of max fitness is satisfied)
									(params.getMaxDistinctBestFitness()==null? 
										false: 
										(params.getRedAlg().compareToMaxFitness(bestFitness, params)>0 &&
										 generRecord.getDistinctBestFitnessCount() >= params.getMaxDistinctBestFitness())
										)
									);
						}while(!exit);
						return generRecord;
					}, 
					(component, geneRecord) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD, geneRecord);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						ReductionParameters<Sig, Hrmny, FValue> params =
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						//	[STATISTIC_EXIT_REASON]
						if (statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_REASON)==null) {
							String exitReason =
									geneRecord.getGeneration()>=params.getIteration()?
											ExitInfo.EXIT_MARK_ITERATION:
											ExitInfo.EXIT_MARK_REACH_MAX_FITNESS;
							statistics.put(
								StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_REASON,
								exitReason
							);
						}
						//	[STATISTIC_OPTIMIZATION_EXIT_ITERATION]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_ITERATION,
							geneRecord.getGeneration()
						);
						//	[STATISTIC_OPTIMIZATION_EXIT_ITERATION]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_FITNESS,
							geneRecord.getBestFitness().getValue()
						);
						//	[STATISTIC_OPTIMIZATION_EXIT_ITERATION]
						statistics.put(
							StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_CONVERGENCE,
							geneRecord.getConvergence()
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
			}.setDescription("Generation loop procedure controller"),
			// 2. Generation loop.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get("gBest"),
								getParameters().get("bestFitness"),
								getParameters().get("harmonyMemoryGroup"),
							});
						
						GenerationRecord<FValue> generRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Generation {}"), generRecord.getGeneration());
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Sig, Hrmny, FValue> params =
								(ReductionParameters<Sig, Hrmny, FValue>) parameters[p++];
						Hrmny gBest =
								(Hrmny) parameters[p++];
						Fitness<Sig, FValue> bestFitness =
								(Fitness<Sig, FValue>) parameters[p++];
						Hrmny[] harmonyMemoryGroup =
								(Hrmny[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp2 = getComponents().get(2);
						ProcedureComponent<Fitness<Sig, FValue>> comp3 = (ProcedureComponent<Fitness<Sig, FValue>>) getComponents().get(3);
						/* ------------------------------------------------------------------------------ */
						Fitness<Sig, FValue>[] fitnessArray = new Fitness[harmonyMemoryGroup.length];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						double par, bw;
						Fitness<Sig, FValue> fitness;
						boolean[] harmonyUpdated = new boolean[harmonyMemoryGroup.length];
						// for j=1 to |C|.
						params.getParAlg().preProcess();
						params.getBwAlg().preProcess();
						for (int j=0; j<params.getAttributes().length; j++) {
							// PAR = updatePitchAdjustingRate(maxPAR, minPAR, times, iter);
							par = params.getParAlg().getPitchAdjustmentRate();
							// bw = updateBandwidth(maxBw, minBw, times, iter);
							bw = params.getBwAlg().getBandwidth();
							// Loop harmony memory group to update each attribute[j].
							TimerUtils.timePause((TimeCounted) component);

							for (int h=0; h<harmonyMemoryGroup.length; h++) {
								localParameters.put("par", par);
								localParameters.put("bw", bw);
								localParameters.put("j", j);
								localParameters.put("h", h);
								harmonyUpdated[h] = (Boolean) comp2.exec();
							}

							TimerUtils.timeContinue((TimeCounted) component);
							// if( fitnessCount(newX) >= fitnessCount(oldX) ). 
							//		oldX = newX;
							for (int h=0; h<harmonyMemoryGroup.length; h++) {
								// if fitnessArray[h] is null or harmony[h] updated 
								//	=> fitnessArray[h] need to be updated.
								if (harmonyUpdated[h] || j==0) { 
									TimerUtils.timePause((TimeCounted) component);
									localParameters.put("h", h);
									fitnessArray[h] = fitness = comp3.exec();
									TimerUtils.timeContinue((TimeCounted) component);
									
									int cmp = fitness.compareToFitness(bestFitness);
									if (cmp>0 || 
										(cmp==0 && harmonyMemoryGroup[h].getAttributes().length<gBest.getAttributes().length)	
									) {
										bestFitness = fitness;
										gBest = (Hrmny) HarmonyFactory.copyHarmony(harmonyMemoryGroup[h]);
									}
								}
							}
						}
						return new Object[] {
								gBest,
								bestFitness,
								fitnessArray
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Hrmny gBest = (Hrmny) result[r++];
						Fitness<Sig, FValue> bestFitness = (Fitness<Sig, FValue>) result[r++];
						Fitness<Sig, FValue>[] fitnessArray = (Fitness<Sig, FValue>[]) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("gBest", gBest);
						getParameters().setNonRoot("bestFitness", bestFitness);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "Global best's length: {}"),
									gBest.getAttributes().length);
							log.info(LoggerUtil.spaceFormat(2, "Global best fitness: {}"), 
									bestFitness.getFitnessValue().getValue() instanceof Integer?
									String.format("%d", bestFitness.getFitnessValue().getValue()):
									String.format("%.2f", bestFitness.getFitnessValue().getValue())
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						GenerationRecord<FValue> generRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						ReductionParameters<Sig, Hrmny, FValue> params = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						Collection<CollectionItem> collectionItems = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS);
						Hrmny[] harmonyMemoryGroup = getParameters().get("harmonyMemoryGroup");
						
						List<BasicIterationInfo4Optimization<Number>> iterInfos = 
								getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);
						statistics.getData().putIfAbsent(
								StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS,
								iterInfos
						);
						
						getParameters().setNonRoot(
							StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS,
							ProcedureUtils.Statistics.IterationInfos.pushInfoOfIteration(
									statistics.getData(),
									params.getIteration(), generRecord.getGeneration(), collectionItems, 
									params.getAttributes(),
									harmonyMemoryGroup,
									Arrays.stream(harmonyMemoryGroup).map(Hrmny::getAttributes).iterator(), 
									Arrays.stream(fitnessArray).map(f->f.getFitnessValue().getValue()).toArray(Number[]::new)
							)
						);

						if (resetGlobalBestMark(bestFitness.getFitnessValue())) {
							SupremeMark.resetGlobalBestCounter();
						}
						
						BasicIterationInfo4Optimization<Number> iterInfo = (BasicIterationInfo4Optimization<Number>) iterInfos.get(iterInfos.size()-1);
						boolean globalBestLocated = false;
						for (int h=0; h<harmonyMemoryGroup.length; h++) {
							if (harmonyMemoryGroup[h].getAttributes().length==gBest.getAttributes().length &&
								Arrays.equals(harmonyMemoryGroup[h].getAttributes(), gBest.getAttributes())
							) {
								iterInfo.getOptimizationEntityBasicInfo()[h]
										.setSupremeMark(SupremeMark.nextGlobalBest());
								globalBestLocated = true;
							}
						}
						
						if (!globalBestLocated) {
							OptEntityBasicInfo<Number>[] upgrade =
								Arrays.copyOf(
									iterInfo.getOptimizationEntityBasicInfo(),
									harmonyMemoryGroup.length+1
								);
							upgrade[upgrade.length-1] = new OptEntityBasicInfo<>();
							upgrade[upgrade.length-1].setEntityAttributeLength(gBest.getAttributes().length);
							upgrade[upgrade.length-1].setFinalAttributes(
									Arrays.stream(gBest.getAttributes())
										.map(i->params.getAttributes()[i])
										.toArray()
							);
							upgrade[upgrade.length-1].setCurrentFitnessValue(bestFitness.getFitnessValue().getValue());
							upgrade[upgrade.length-1].setSupremeMark(SupremeMark.nextGlobalBest());
							
							iterInfo.setOptimizationEntityBasicInfo(upgrade);
						}//*/
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
						
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Generation loop"),
			// 3. Loop and update harmony memory group attributes.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "3. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get("harmonyMemoryGroup"),
								getParameters().get("gBest"),
								getParameters().get(ParameterConstants.PARAMETER_RANDOM_INSTANCE),
								localParameters.get("h"),	// harmony index
								localParameters.get("j"),	// attribute index
								localParameters.get("par"),
								localParameters.get("bw"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Sig, Hrmny, FValue> params =
								(ReductionParameters<Sig, Hrmny, FValue>) parameters[p++];
						Hrmny[] harmonyMemoryGroup =
								(Hrmny[]) parameters[p++];
						Hrmny gBest =
								(Hrmny) parameters[p++];
						Random random =
								(Random) parameters[p++];
						int h =
								(int) parameters[p++];
						int j =
								(int) parameters[p++];
						double par =
								(double) parameters[p++];
						double bw =
								(double) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						boolean harmony_h_contains_j = harmonyMemoryGroup[h].containsAttribute(j);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						double upsAndDown;
						// if (rand()<=HMCR).
						if (Double.compare(
								random.nextDouble(), 
								params.getHarmonyMemoryConsiderationRate()
							) <= 0
						) {
							// newX[j]=oldX[j]
							if (gBest.containsAttribute(j))	harmonyMemoryGroup[h].addAttribute(j);
							else							harmonyMemoryGroup[h].removeAttribute(j);
							// if(rand()<= PAR)
							if (Double.compare(random.nextDouble(), par)<=0) {
								upsAndDown = bw;
								if (random.nextBoolean())	upsAndDown = 1 - bw;
								// temp = +temp or -temp
								if (upsAndDown > 0.5) {
									if (!harmonyMemoryGroup[h].containsAttribute(j))
										harmonyMemoryGroup[h].addAttribute(j);
								}else {
									if (harmonyMemoryGroup[h].containsAttribute(j))
										harmonyMemoryGroup[h].removeAttribute(j);
								}
							}
						// else adjust the attribute randomly
						}else {
							// newX[j] = lowerPVB + rand()*(upperPVB - lowerPVB)
							double temp = params.getHarmonyInitializationAlg()
												.getMinPossibleValueBoundOfHarmonyBit()
												.doubleValue() + 
											random.nextDouble() * 
											(params.getHarmonyInitializationAlg().getMaxPossibleValueBoundOfHarmonyBit().doubleValue() - 
											 params.getHarmonyInitializationAlg().getMinPossibleValueBoundOfHarmonyBit().doubleValue());
							if (temp > 0.5)	harmonyMemoryGroup[h].addAttribute(j);
							else			harmonyMemoryGroup[h].removeAttribute(j);
						}
						// return if j has been updated in harmonyMemoryGroup[h]
						return harmonyMemoryGroup[h].containsAttribute(j)!=harmony_h_contains_j;
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
					return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Loop and update harmony memory group attributes"),
			// 4. Harmony fitnesses calculation.
			new TimeCountedProcedureComponent<Fitness<Sig, FValue>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "3. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get("harmonyMemoryGroup"),
								localParameters.get("h"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Cal calculation = (Cal) parameters[p++];
						ReductionParameters<Sig, Hrmny, FValue> params = (ReductionParameters<Sig, Hrmny, FValue>) parameters[p++];
						Collection<CollectionItem> collection = (Collection<CollectionItem>) parameters[p++];
						Hrmny[] harmonyMemoryGroup = (Hrmny[]) parameters[p++];
						int h = (int) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return params.getRedAlg()
										.fitness(
											calculation, 
											harmonyMemoryGroup[h],
											collection,
											params.getAttributes()
										);
					}, 
					(component, fitness) -> {
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
			}.setDescription("Harmony fitnesses calculation"),
			// 5. Check if break for reaching max convergence: true-break, false-continue.
			new TimeCountedProcedureComponent<Boolean>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "4. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get("gBest"),
								getParameters().get("bestFitness"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						GenerationRecord<FValue> generRecord = (GenerationRecord<FValue>) parameters[p++];
						ReductionParameters<Sig, Hrmny, FValue> params = (ReductionParameters<Sig, Hrmny, FValue>) parameters[p++];
						Harmony<?> gBest = (Harmony<?>) parameters[p++];
						Fitness<Sig, FValue> bestFitness = (Fitness<Sig, FValue>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// if the best value remains unchanged, count <code>convergence</code>
						if (Double.compare(
								generRecord.getBestFitness().getValue().doubleValue(), 
								bestFitness.getFitnessValue().getValue().doubleValue()
							)==0
						) {
							// conNumber++;
							generRecord.countConvergence();
							if (gBest.getAttributes().length!=0)	generRecord.addBestFitness(gBest);
						// else reset <code>convergence</code> for best value having changed.
						}else {
							generRecord.resetConvergence();
							generRecord.updateBestFitness(bestFitness, gBest);
						}
						// if count convergence >= preset value, break loop
						return generRecord.getConvergence() >= params.getConvergence();
					}, 
					(component, breakLoop) -> {
						/* ------------------------------------------------------------------------------ */
						GenerationRecord<FValue> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "convergence = {}"), geneRecord.getConvergence());
							if (breakLoop)	log.info(LoggerUtil.spaceFormat(2, "break!"));
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						ProcedureUtils.Statistics.IterationInfos
									.updateConvergenceInfo(
										statistics.getData(), geneRecord.getConvergence()
									);
						//	[STATISTIC_OPTIMIZATION_EXIT_REASON]
						if (breakLoop) {
							statistics.put(
								StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_REASON,
								ExitInfo.EXIT_MARK_CONVERGENCE
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Check if break for reaching max convergence"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public GenerationRecord<FValue> exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (GenerationRecord<FValue>) componentArray[0].exec();
	}

	protected String reportMark() {
		GenerationRecord<FValue> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
		return "Generation "+geneRecord.getGeneration();
	}
	
	protected String reportMark(int harmonyIndex) {
		GenerationRecord<FValue> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
		return "Generation "+geneRecord.getGeneration()+" harmony["+harmonyIndex+"]";
	
	}
	
	protected String reportMark(int attrIndex, int harmonyIndex) {
		GenerationRecord<FValue> geneRecord = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
		int[] attributes = getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES);
		return "Generation "+geneRecord.getGeneration()+
				" harmony["+harmonyIndex+"] attribute["+attributes[attrIndex]+"]";
	}
	
	/**
	 * Check if the reset of global best mark for {@link SupremeMarkType} is necessary.
	 * <p>
	 * Return <strong>false</strong> if only 1 {@link BasicIterationInfo4Optimization} element in 
	 * {@link StatisticsConstants.Procedure#STATISTIC_ITERATION_INFOS}. (i.e. the 1st iteration)
	 * <p>
	 * Otherwise, check if the global best {@link FValue} of the last iteration equals to the one of 
	 * current iteration. Return <strong>false</strong> if equals, else return <strong>true</strong> 
	 * and reset global best mark.
	 * 
	 * @see SupremeMarkType#GLOBAL_BEST
	 * @see OptEntityBasicInfo#getSupremeMark()
	 * 
	 * @param currentBestFitness
	 * 		Current best fitness value in {@link FValue}.
	 * @return true if reset global best mark.
	 */
	protected boolean resetGlobalBestMark(FValue currentBestFitness) {
		List<BasicIterationInfo4Optimization<Number>> iterInfos =
				getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);

		if (iterInfos.size()==1) {
			return false;
		}else {
			double lastBestFitness = 
				Arrays.stream(iterInfos.get(iterInfos.size()-2).getOptimizationEntityBasicInfo())
					.filter(info->info.getSupremeMark()!=null && SupremeMarkType.GLOBAL_BEST.equals(info.getSupremeMark().getSupremeMarkType()))
					.mapToDouble(info->info.getCurrentFitnessValue().doubleValue())
					.max().orElse(0.0);
			return Double.compare(currentBestFitness.getValue().doubleValue(), lastBestFitness)!=0;
		}
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}
