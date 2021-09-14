package featureSelection.tester.procedure.opt.particalSwarm.procedure.common.psoGreedySearch;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import featureSelection.repository.entity.opt.particleSwarm.GenerationRecord;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Particle;
import featureSelection.repository.entity.opt.particleSwarm.interf.particle.entity.Position;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.iteration.optimization.BasicIterationInfo4Optimization;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.SupremeMark;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.SupremeMarkType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Particle Swarm Optimization</strong> greedy search part procedure. Mainly, this contains 
 * {@link ProcedureComponent}s : 
 * <ul>
 * 	<li>
 * 		<strong>Greedy search procedure controller</strong>
 * 		<p>To control the greedy search, loop every particle to execute greedy search. 
 * 	</li>
 * 	<li>
 * 		<strong>Greedy search</strong>
 * 		<p>To execute greedy search for a particle.
 * 	</li>
 * 	<li>
 * 		<strong>Inspection</strong>
 * 		<p>Inspect particles' for redundancy.
 * 	</li>
 * </ul>
 * 
 * @see FeatureImportance
 * @see SkipParticleGreedySearchProcedureContainer
 * 
 * @author Benjamin_L
 *
 * @param <Cal>
 * 		Type of implemented {@link FeatureImportance}.
 * @param <Sig>
 * 		Type of feature significance that implements {@link Number}.
 * @param <CollectionItem>
 * 		Universe or EquivalentClass.
 * @param <Velocity>
 * 		Type of {@link Particle}'s Velocity.
 * @param <Posi>
 * 		Type of implemented {@link Position}.
 */
@Slf4j
public class ParticleGreedySearchProcedureContainer<Cal extends FeatureImportance<Sig>,
													Sig extends Number, 
													CollectionItem,
													Velocity,
													Posi extends Position<?>,
													FValue extends FitnessValue<?>>
	extends DefaultProcedureContainer<Object[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public ParticleGreedySearchProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "PSOGreedySearch";
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
			// 1. Controller.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_ALGORITHM),
								getParameters().get("particle"),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						ReductionParameters<Velocity, Posi, FValue> params =
								(ReductionParameters<Velocity, Posi, FValue>)
								parameters[p++];
						ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Sig> redAlg =
								(ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Sig>)
								parameters[p++];
						Particle<Velocity, Posi, FValue>[] particle =
								(Particle<Velocity, Posi, FValue>[]) parameters[p++];
						GenerationRecord<Velocity, Posi, FValue> generRecord =
								(GenerationRecord<Velocity, Posi, FValue>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<Object[]> comp1 = (ProcedureComponent<Object[]>) getComponents().get(1);
						Integer[] addedAttributes = new Integer[particle.length];
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> bestFitnessUpdated = new LinkedList<>();
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						int fitnessCmp;
						boolean resetConvergenceCount=false;
						for (int i=0; i<particle.length; i++) {
							TimerUtils.timePause((TimeCounted) component);
							localParameters.put("particleIndex", i);
							addedAttributes[i] = (Integer) comp1.exec()[0];
							TimerUtils.timeContinue((TimeCounted) component);

							// Initiate global best fitness
							if (generRecord.getGlobalBestAttributeLength()==null) {
								// update gBest = p.position
								generRecord.updateGlobalBestFitness(particle[i].getFitness());
								if (!resetConvergenceCount)	{	resetConvergenceCount = true;	}
								
								TimerUtils.timePause((TimeCounted) component);
								bestFitnessUpdated.add(i);
								TimerUtils.timeContinue((TimeCounted) component);
							// Global best fitness value exits in GenerationRecord, update if needed
							}else {
								fitnessCmp = redAlg.compareFitness(particle[i].getFitness(), generRecord.getGlobalBestFitness());
								// If no better than global best fitness value
								if (fitnessCmp<0) {
									// do nothing ...
								// else if it is the best, update (1st of the best)
								}else if (fitnessCmp>0) {
									// update gBest = p.position
									generRecord.updateGlobalBestFitness(particle[i].getFitness());
									if (!resetConvergenceCount)	{	resetConvergenceCount = true;	}
									
									TimerUtils.timePause((TimeCounted) component);
									if (!bestFitnessUpdated.isEmpty())	bestFitnessUpdated.clear();
									bestFitnessUpdated.add(i);
									TimerUtils.timeContinue((TimeCounted) component);
								// else 1 of the best
								}else {
									// If fitness equals to max fitness(considered as reduct)
									if (redAlg.compareMaxFitness(params.getMaxFitness(), generRecord)<=0) {
										generRecord.addGlobalBestFitness(particle[i].getFitness());
										
										TimerUtils.timePause((TimeCounted) component);
										bestFitnessUpdated.add(i);
										TimerUtils.timeContinue((TimeCounted) component);
									}
								}
							}
						}
						return new Object[] {
								resetConvergenceCount,
								addedAttributes,
								bestFitnessUpdated
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0; 
						boolean resetConvergenceCount = (boolean) result[r++];
						@SuppressWarnings("unused")
						Integer[] addedAttributes = (Integer[]) result[r++];
						Collection<Integer> bestFitnessUpdated = (Collection<Integer>) result[r++];
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						List<BasicIterationInfo4Optimization<?>> iterInfos =
								getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);
						BasicIterationInfo4Optimization<?> iterInfo = iterInfos.get(iterInfos.size()-1);
						
						if (resetConvergenceCount)	SupremeMark.resetGlobalBestCounter();

						if (!bestFitnessUpdated.isEmpty()) {
							for (int i: bestFitnessUpdated) {
								if (iterInfo.getOptimizationEntityBasicInfo()[i].getSupremeMark()!=null &&
									SupremeMarkType.isLocalBest(
										iterInfo.getOptimizationEntityBasicInfo()[i]
												.getSupremeMark()
												.getSupremeMarkType()
									)
								) {
									iterInfo.getOptimizationEntityBasicInfo()[i]
											.setSupremeMark(SupremeMark.nextGlobalBest());
								}
							}
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Controller"),
			// 2. Greedy search.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								getParameters().get("particle"),
								localParameters.get("particleIndex"),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
								getParameters().get("individualUpdated"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Cal calculation =
								(Cal) parameters[p++];
						Collection<CollectionItem> collectionList =
								(Collection<CollectionItem>) parameters[p++];
						Particle<Velocity, Posi, FValue>[] particles =
								(Particle<Velocity, Posi, FValue>[]) parameters[p++];
						int particleIndex =
								(int) parameters[p++];
						ReductionParameters<Velocity, Posi, FValue> params =
								(ReductionParameters<Velocity, Posi, FValue>) parameters[p++];
						boolean[] individualUpdated =
								(boolean[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						Particle<Velocity, Posi, FValue> particle = particles[particleIndex];
						ProcedureComponent<int[]> comp2 = (ProcedureComponent<int[]>) getComponents().get(2);
						/* ------------------------------------------------------------------------------ */
						int[] attrIndexesB4Inspection = null;
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						int[] attributesSrc = params.getAttributes();
						int attrLength = attributesSrc.length;
						
						FValue examSig, maxSig;
						int maxAttrIndex = -1;
						ReductionAlgorithm<CollectionItem, Velocity, Posi, FValue, Cal, Sig> redAlg = params.getReductionAlgorithm();
						int[] examAttr = new int[particle.getPosition().getAttributes().length+1];
						for (int i=0; i<examAttr.length-1; i++)	
							examAttr[i] = attributesSrc[particle.getPosition().getAttributes()[i]];

						// Go through a in C-R, where R is the particle position
						maxSig = particle.getFitness().getFitnessValue();
						for (int i=0; i<attributesSrc.length; i++) {
							if (!particle.containsAttribute(i)) {
								// Calculate the fitness of R U {a}
								examAttr[examAttr.length-1] = attributesSrc[i];
								examSig = redAlg.fitnessValue(calculation, collectionList, examAttr);
								// if Fit > p.bestFitness, mark
								if (Double.compare(
										examSig.getValue().doubleValue(), 
										maxSig.getValue().doubleValue()
									)>0
								) {
									maxAttrIndex = i;
									maxSig = examSig;
								}
							}
						}
						// Update particle best position with redundancy inspection first.
						int[] newIndexes;
						if (maxAttrIndex!=-1) {
							newIndexes = Arrays.copyOf(particle.getPosition().getAttributes(), examAttr.length);
							newIndexes[newIndexes.length-1] = maxAttrIndex;
							attrIndexesB4Inspection = newIndexes;
							// If fitness equals to max fitness(considered as reduct)
							boolean inspected = false;
							if (params.isInspectReductInGreedySearch() && 
								redAlg.compareMaxFitness(params.getMaxFitness(), maxSig)<=0
							) {
								// Inspection.
								TimerUtils.timePause((TimeCounted) component);
								localParameters.put("attributeIndexes", attrIndexesB4Inspection);
								newIndexes = comp2.exec();
								TimerUtils.timeContinue((TimeCounted) component);
								inspected = true;
							}
							// update p.position, p.bestFiness
							Posi newPosi = redAlg.toPosition(newIndexes, attrLength);
							particle.setPosition(newPosi);
							particle.getFitness().setPosition((Posi) ((Posi) newPosi).clone());
							particle.getFitness().setFitnessValue(maxSig);
							// added attribute, inspected, attributes b4 inspection
							return new Object[] {	attributesSrc[maxAttrIndex], inspected, attrIndexesB4Inspection	};
						}else {
							// no updates.
							attrIndexesB4Inspection = particle.getFitness().getPosition().getAttributes();
							// If fitness equals to max fitness(considered as reduct)
							if (params.isInspectReductInGreedySearch() && 
								individualUpdated[particleIndex] &&
								redAlg.compareMaxFitness(params.getMaxFitness(), maxSig)<=0
							) {
								// Inspection.
								TimerUtils.timePause((TimeCounted) component);
								localParameters.put("attributeIndexes", attrIndexesB4Inspection);
								newIndexes = comp2.exec();
								TimerUtils.timeContinue((TimeCounted) component);
								
								// update p.position, p.bestFiness;
								Posi newPosi = redAlg.toPosition(newIndexes, attrLength);
								particle.getFitness().setPosition((Posi) newPosi.clone());
								particle.getFitness().setFitnessValue(maxSig);
								// added attribute, inspected, attributes b4 inspection
								return new Object[] {	null, true, attrIndexesB4Inspection	};
							}else {
								// added attribute, inspected, attributes b4 inspection
								return new Object[] {	null, false, null	};
							}
						}
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Integer attrIndex = (Integer) result[r++];
						boolean inspected = (boolean) result[r++];
						int[] attrsB4Inspection = (int[]) result[r++];
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_ITERATION_INFOS]
						int particleIndex = (int) localParameters.get("particleIndex");
						Particle<Velocity, Posi, FValue>[] particle = getParameters().get("particle");
						ReductionParameters<Velocity, Posi, FValue> params = getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
						
						List<BasicIterationInfo4Optimization<Number>> iterInfos = 
							getParameters().get(StatisticsConstants.Procedure.STATISTIC_ITERATION_INFOS);
						BasicIterationInfo4Optimization<Number> iterInfo = iterInfos.get(iterInfos.size()-1);
						
						if (attrIndex!=null) {
							// update fitness value
							iterInfo.getOptimizationEntityBasicInfo()[particleIndex]
									.setCurrentFitnessValue(particle[particleIndex].getFitness().getFitnessValue().getValue().doubleValue());
							iterInfo.getOptimizationEntityBasicInfo()[particleIndex]
									.setSupremeMark(SupremeMark.nextLocalBest());
							
							boolean[] individualUpdated = getParameters().get("individualUpdated");
							individualUpdated[particleIndex] = true;
						}
						if (attrsB4Inspection!=null) {
							iterInfo.getOptimizationEntityBasicInfo()[particleIndex]
									.setEntityAttributeLength(attrsB4Inspection.length);
							iterInfo.getOptimizationEntityBasicInfo()[particleIndex]
									.setFinalAttributes(
										Arrays.stream(attrsB4Inspection)
											.map(i->params.getAttributes()[i])
											.toArray()
									);
						}
						if (inspected) {
							iterInfo.getOptimizationEntityBasicInfo()[particleIndex]
									.setInspectedReduct(
										Arrays.stream(particle[particleIndex].getFitness().getPosition().getAttributes())
											.map(index->params.getAttributes()[index])
											.boxed()
											.collect(Collectors.toList())
									);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
				@Override public void init() {}
					
				@Override public String staticsName() {
					return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
				}
			}.setDescription("Greedy search"),
			// 3. Inspection.
			new TimeCountedProcedureComponent<int[]>(
					ComponentTags.TAG_INSPECT_DURING,
					this.getParameters(), 
					(component) -> {
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2. "+component.getDescription()));
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_COLLECTION_ITEMS),
								localParameters.get("attributeIndexes"),
								getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Cal calculation = (Cal) parameters[p++];
						Sig sigDeviation = (Sig) parameters[p++];
						Collection<CollectionItem> collectionList = (Collection<CollectionItem>) parameters[p++];
						int[] attributeIndexes = (int[]) parameters[p++];
						ReductionParameters<Velocity, Posi, FValue> params = (ReductionParameters<Velocity, Posi, FValue>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						int[] actualAttributes = new int[attributeIndexes.length];
						for (int i=0; i<actualAttributes.length; i++)
							actualAttributes[i] = params.getAttributes()[attributeIndexes[i]];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> inspectedAttrs;
						inspectedAttrs = params.getReductionAlgorithm()
												.inspection(
													calculation, sigDeviation, collectionList, actualAttributes
												);
						TimerUtils.timePause((TimeCounted) component);
						// Transfer actual attributes back to indexes.
						int[] inspectedIndexes;
						//	if no redundancy
						if (inspectedAttrs.size()==actualAttributes.length) {
							inspectedIndexes = attributeIndexes;
						//	else redundant
						}else {
							inspectedAttrs = new HashSet<>(inspectedAttrs);
							inspectedIndexes = new int[inspectedAttrs.size()];
							for (int i=0, j=0; j<inspectedAttrs.size(); i++) {
								if (inspectedAttrs.contains(actualAttributes[i])) {
									inspectedIndexes[j++] = attributeIndexes[i];
								}
							}
						}
						TimerUtils.timeContinue((TimeCounted) component);
						return inspectedIndexes;
					}, 
					(component, inspectedIndexes) -> {
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
			}.setDescription("Inspection"),
		};
	}

	@Override
	public Object[] exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (Object[]) componentArray[0].exec();
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	@SuppressWarnings("unused")
	private String reportMark() {
		GenerationRecord<Velocity, Posi, FValue> generRecord = 
				getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
		return "Generation "+generRecord.getGeneration()+" Greedy Search";
	}
	
	@SuppressWarnings("unused")
	private String reportMark(int particleIndex) {
		GenerationRecord<Velocity, Posi, FValue> generRecord = 
				getParameters().get(ParameterConstants.PARAMETER_OPTIMIZATION_GENERATION_RECORD);
		return "Generation "+generRecord.getGeneration()+" Particle["+particleIndex+"]";
	}
}