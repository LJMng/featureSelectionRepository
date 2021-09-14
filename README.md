# featureSelectionRepository

-----

*This project depends on my another project [featureSelectionBasic](https://github.com/LJMng/featureSelectionBasic).

-----

## Introduction

### What is this project?

This is a java project with several (Filter) **Feature Selection**(FS) algorithms(mostly *Rough Set Theory*(RST) based) 
I implemented throughout the 7 years(2015-2022) of my studies in *Guangdong University of Technology*(GDUT), Guangzhou, 
China.

### Why did I start this project?

In short, the original thought was to meet the needs of experiments that I did with my mentor for our researches.
With more algorithms implemented, it had become necessary to make the project be more well-constructed.

I started to study *RST based FS* algorithms when I was a sophomore/senior as an under-graduate student with the 
guidance of my mentor Pro.Zhao in *GDUT*. At the beginning stage, I was simply asked to implement a few *RST based FS* 
algorithms based on some academic paper sudo-codes, as a way to study *FS*. Then I started to do some experiments 
with my mentor and came up with the original version of this project which was totally not well-constructed. Later, 
I re-constructed the project and came up with a draft version of this project. As I am graduating from college, some 
modifications are made to finalise.

Another thing is that, all the codings in this project were implemented by myself only, so there was actually a lot of 
work there. 'Cause I still needed to do my lessons, exams and other stuff, so I just tried my best to perfect it. And 
still, there remains quite a lot of improvements to be done, like using design patterns and frameworks, efficiency, 
concurrent executions, etc.

### What can I get from this project?

Several (filter) *RST based FS* algorithms from some papers, including *heuristic* based and *optimization*
(*evolutionary*) algorithms based, were implemented in this project. Not sure if they can cover you needs, but if you 
want to use the algorithms to do some data analysis and couldn't find any code for executions. Well, I hope this could 
help you in saving some implementation works. I am not saying that the codes here are perfect(and they are certainly 
not), but I still hope they could be helpful.

Another thing I want to point out is that, all the algorithms implemented based on published academic papers were 
implemented with my best efforts to try to stick to the original sudo-codes and algorithms in the original paper as 
my mentor urged me to. Like what she said, if major modifications were made, the codings would become too different 
from the original algorithm, and become a new one by itself. So, sticking to the original sudo-codes is an important 
thing that I had taken into considerations when implementing. However, I am sure you can come up with lots of ways to 
improve them. And I am gonna leave that to you. Maybe, by doing so, you can come up with your own algorithms too.


-----


## Implemented Feature Select Algorithms

Currently, some *heuristic-based* and *optimization(evolutionary)-algorithm-based FS* algorithms have been implemented:

### Heuristic-based Feature Selection algorithms

  <table>
    <thead>
      <tr>
        <td>#</td>
        <td>Algorithm</td>
        <td>Static data</td>
        <td>Incremental data</td>
        <td>Complete data</td>
        <td>In-Complete data</td>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>! NEC</td>  <td>S-REC/NEC</td>  <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td>! NEC</td>  <td>ID-REC/C-NEC</td>  <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td>! NEC</td>  <td>IP-REC/IP-NEC/G-NEC</td>  <td>√</td>  <td>√</td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td>! NEC</td>  <td>I-NEC (Not present yet)</td>          <td>√</td>  <td>√</td>  <td>√</td>  <td>√</td>
      </tr>
      <tr>
        <td>! NEC</td>  <td>REC (major functions implemented)</td>  <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td>XDC</td>  <td>IDC</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td>XDC</td>  <td>HDC</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td>XDC</td>  <td>DDC</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td></td>  <td>ACC</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td></td>  <td>CT</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td></td>  <td>FAR-DV</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td> </td>  <td>Liu Quick Hash</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td> </td>  <td>Liu Rough Set</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td> </td>  <td>Conflict Decrease Region</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td> </td>  <td>Xu</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td> </td>  <td>Semi-supervised Representative Feature Selection</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td>Sample Pair</td>  <td>Sample Pair Selection</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td>Sample Pair</td>  <td>Active Sample Pair</td>          <td> </td>  <td>√</td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td> </td>  <td>GIARC</td>          <td> </td>  <td>√</td>  <td>√</td>  <td> </td>
      </tr>
      <tr>
        <td> </td>  <td>FSMV/TCPR</td>          <td>√</td>  <td>√</td>  <td>√</td>  <td>√</td>
      </tr>
      <tr>
        <td> </td>  <td>DIDS</td>          <td>√</td>  <td>√</td>  <td>√</td>  <td>√</td>
      </tr>
      <tr>
        <td> </td>  <td>Classic</td>          <td>√</td>  <td> </td>  <td>√</td>  <td> </td>
      </tr>
    </tbody>
  </table>

#### Rough Equivalence Class (REC) based / Nested Equivalence Class (NEC) based series

Originally designed by my mentor, me and our team.

For static, complete data processing:
- **S-REC/NEC**: [Jie Zhao, **Jiaming Liang**, Zhenning Dong, et al. NEC: A nested equivalence class-based dependency calculation approach for fast feature selection using rough set theory\[J\]. Information Sciences. 2020, 536: 431-453.](https://doi.org/10.1016/j.ins.2020.03.092)
- **ID-REC/C-NEC**: [Jie Zhao, **Jiaming Liang**, Zhenning Dong, et al. Accelerating information entropy-based feature selection using rough set theory with classified nested equivalence classes\[J\]. Pattern Recognition. 2020, 107: 107517.](https://doi.org/10.1016/j.ins.2020.03.092)
- **REC**: [ZHAO Jie, ZHANG Kaihang, DONG Zhenning, etc. Rough equivalence class bilateral-decreasing based incremental core and attribute reduction computation with multiple Hashing\[J\]. Systems Engineering — Theory & Practice, 2017, 37(2): 504–522.](http://www.cnki.com.cn/Article/CJFDTotal-XTLL201702022.htm)

For incremental, complete data processing:
- **IP-REC/IP-NEC/G-NEC**: *to be published*.

For incremental, in-complete data processing:
- **I-NEC**: *to be published*.

#### Dependency Calculation series
- **IDC**:
  [Muhammad Summair Raza, Usman Qamar. An incremental dependency calculation technique for feature selection using rough sets\[J\]. Information Sciences. 2016, 343-344: 41-65.](https://www.sciencedirect.com/science/article/pii/S0020025516000785)
- **HDC**:
  [Muhammad Summair Raza, Usman Qamar. A heuristic based dependency calculation technique for rough set theory\[J\]. Pattern Recognition. 2018, 81: 309-325.](https://www.sciencedirect.com/science/article/abs/pii/S0031320318301432)
- **DDC**:
  [Muhammad Summair Raza, Usman Qamar. Feature selection using rough set-based direct dependency calculation by avoiding the positive region\[J\]. International journal of approximate reasoning. 2018, 92(Jan.): 175-197.](https://www.sciencedirect.com/science/article/abs/pii/S0888613X17300178)

#### Positive Approximation Accelerator
- **ACC**: [Yuhua Qian, Jiye Liang, Witold Pedrycz, et al. Positive approximation: An accelerator for attribute reduction in rough set theory\[J\]. Artificial Intelligence. 2010, 174(9-10): 597-618.](https://doi.org/10.1016/j.artint.2010.04.018)

#### Compacted Decision Table
- **CT**: [Wei Wei, Junhong Wang, Jiye Liang, et al. Compacted decision tables based attribute reduction\[J\]. Knowledge-Based Systems. 2015, 86: 261-277.](http://dx.doi.org/10.1016/j.knosys.2015.06.013)

#### Forward Attribute Reduction from the Discernibility View
- **FAR-DV**: [Shu-Hua Teng, Min Lu, A-Feng Yang, et al. Efficient attribute reduction from the viewpoint of discernibility\[J\]. Information Sciences. 2016, 326: 297-314.](https://linkinghub.elsevier.com/retrieve/pii/S0020025515005605)

#### Liu Quick Hash
- **Liu Quick Hash**: [LIU Yong, XIONG Rong, CHU Jian. Quick Attribute Reduction Algorithm with Hash\[J\]. Chinese Journal of computers. 2009, 32(08): 1493-1499.](http://cjc.ict.ac.cn/quanwenjiansuo/2009-8/ly.pdf).

#### Liu Rough Set
- **Liu Rough Set**: [LIU Shao-Hui, SHENG Oiu-Jian, WU Bin, SHI Zhong-Zhi, HU Fei. Research on Efficient Algorithms for Rough Set Methods\[J\]. Chinese Journal of computers. 2003(05): 524-529.](http://cjc.ict.ac.cn/quanwenjiansuo/2003-05.pdf) (see also: [http://cjc.ict.ac.cn/eng/qwjse/view.asp?id=1248](http://cjc.ict.ac.cn/eng/qwjse/view.asp?id=1248))

#### Conflict Decrease Region
- [Ge Hao, Li Longshu, Yang Chuanjian. An Efficient Attribute Reduction Algorithm Based on Conflict Region\[J\]. Chinese Journal of computers. 2012, 35(02): 2342-2350.](http://cjc.ict.ac.cn/eng/qwjse/view.asp?id=3504)
- [Ge Hao, Li Longshu, Yang Chuanjian. Attribute reduction Algorithm based on Conflict Region Decreasing\[J\]. System Engeering - Theory & Practice. 2013, 33(09): 2371-2380.](http://www.sysengi.com/EN/abstract/abstract110260.shtml)

#### Xu
- [Xu Zhangyan, Liu Zuopeng, Yang Bingru, Song Wei. A Quick Attribute Reduction Algorithm with Complexity of max(O(|C||U|), O(|C|^2|U/C|))\[J\] Chinese Journal of computers. 2006(03): 391-399.](http://www.cnki.com.cn/Article/CJFDTotal-JSJX200603005.htm)

#### Semi-supervised Representative Feature Selection
- **SRFS**: [Yintong Wang, Jiandong Wang, Hao Liao, Haiyan Chen. An efficient semi-supervised representatives feature selection algorithm based on information theory\[J\]. Pattern Recognition. 2017, 61: 511-523.](https://linkinghub.elsevier.com/retrieve/pii/S0031320316302242)

#### Sample Pair Selection based series

For static data processing:
- **Sample pair**: [Degang Chen, Suyun Zhao, Lei Zhang, etc. Sample Pair Selection for Attribute Reduction with Rough Set\[J\] IEEE Transactions on Knowledge and Data Engineering. 2012, 24(11): 2080-2093.](https://ieeexplore.ieee.org/document/6308684/)

For incremental data processing:
- **Active Sample pair**: [Yanyan Yang, Degang Chen, Hui Wang. Active Sample Selection Based Incremental Algorithm for Attribute Reduction With Rough Sets\[J\]. IEEE Transactions on Fuzzy Systems. 2017, 25(4): 825-838.](https://ieeexplore.ieee.org/document/7492272/)

#### Group Incremental Approach to Feature Selection
- **GIARC**: [Jiye Liang, Feng Wang, Chuangyin Dang, et al. A Group Incremental Approach to Feature Selection Applying Rough Set Technique\[J\]. IEEE Transactions on Knowledge and Data Engineering. 2014, 26(2): 294-308.](https://ieeexplore.ieee.org/document/6247431)

#### Incremental feature selection based on rough set in dynamic incomplete data
- **FSMV/TCPR**: [Wenhao Shu, Hong Shen. Incremental feature selection based on rough set in dynamic incomplete data\[J\]. Pattern Recognition. 2014, 47(12): 3890-3906.](http://dx.doi.org/10.1016/j.patcog.2014.06.002)

#### A novel incremental attribute reduction approach for dynamic incomplete decision systems
- **DIDS**: [Xiaojun Xie, Xiaolin Qin. A novel incremental attribute reduction approach for dynamic incomplete decision systems\[J\]. International Journal of Approximate Reasoning. 2018, 93: 443-462.](https://doi.org/10.1016/j.ijar.2017.12.002)

#### Classic (Conventional method)
Implemented based on Conventional Heuristic attribute reduction algorithm. The steps are as below:
- Obtain equivalence classes induced by all features. (dataset compressing)
- Calculate the significance when using all features.
- Obtain core. (Optional)
- Loop and add attributes with the greatest outer significance into reduct in each loop until the significance of the
  reduct equals to the value using all features: *Sig(C)* = *Sig(reduct)*.
- Execute redundancy inspection and remove redundant features from the reduct. (Optional)

### Optimization-based Feature Selection algorithms

#### List of algorithms and papers

4 basic *Optimization*(*Evolutionary*)-algorithm-based *FS* were implemented with different feature (subset) 
significance calculation methods of specific *FS* algorithms.

  <table>
    <thead>
      <tr>
        <td>FS</td>
        <td>Genetic Algorithm</td>
        <td>Particle Swarm Optimization</td>
        <td>Improved Harmony Search</td>
        <td>Artificial Fish Swarm</td>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>S-REC/NEC</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
      </tr>
      <tr>
        <td>IP-REC/IP-NEC</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
      </tr>
      <tr>
        <td>IDC</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
      </tr>
      <tr>
        <td>HDC</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
      </tr>
      <tr>
        <td>DDC</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
      </tr>
      <tr>
        <td>classic</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
      </tr>
      <tr>
        <td>Asit.K.Das IFS</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
        <td> </td>
      </tr>
      <tr>
        <td>FSMV/TCPR</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
      </tr>
      <tr>
        <td>DIDS</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
        <td>√</td>
      </tr>
    </tbody>
  </table>

- Theoretically, all *RST* based *FS* can work with different *Optimization* algorithms since they usually only provide 
  a way to calculate significance of feature (subset).

  <table>
    <thead>
      <tr>
        <td>FS</td>
        <td>Static data</td>
        <td>Incremental data</td>
        <td>Complete data</td>
        <td>In-Complete data</td>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>S-REC/NEC</td>
        <td>√</td>    <td> </td>
        <td>√</td>    <td> </td>
      </tr>
      <tr>
        <td>IP-REC/IP-NEC</td>
        <td>√</td>    <td>√</td>
        <td>√</td>    <td> </td>
      </tr>
      <tr>
        <td>I-NEC(Not present YET)</td>
        <td>√</td>    <td>√</td>
        <td>√</td>    <td>√</td>
      </tr>
      <tr>
        <td>IDC</td>
        <td>√</td>    <td> </td>
        <td>√</td>    <td> </td>
      </tr>
      <tr>
        <td>HDC</td>
        <td>√</td>    <td> </td>
        <td>√</td>    <td> </td>
      </tr>
      <tr>
        <td>DDC</td>
        <td>√</td>    <td> </td>
        <td>√</td>    <td> </td>
      </tr>
      <tr>
        <td>classic</td>
        <td>√</td>    <td> </td>
        <td>√</td>    <td> </td>
      </tr>
      <tr>
        <td>Asit.K.Das IFS</td>
        <td>√</td>    <td> </td>
        <td>√</td>    <td> </td>
      </tr>
      <tr>
        <td>FSMV/TCPR</td>
        <td>√</td>    <td> </td>
        <td>√</td>    <td>√</td>
      </tr>
      <tr>
        <td>DIDS</td>
        <td>√</td>    <td> </td>
        <td>√</td>    <td>√</td>
      </tr>
    </tbody>
  </table>

The above *optimization-algorithm-based FS* were implemented based on the following papers.

#### Genetic Algorithm

- **S-REC/NEC**: [Jie Zhao, Jiaming Liang, Zhenning Dong, et al. NEC: A nested equivalence class-based dependency calculation approach for fast feature selection using rough set theory\[J\]. Information Sciences. 2020, 536: 431-453.](https://doi.org/10.1016/j.ins.2020.03.092)
- **IP-REC/IP-NEC/G-NEC**: *to be published*.
- **I-NEC**: *to be published*.
- **IDC**: [Muhammad Summair Raza, Usman Qamar. An incremental dependency calculation technique for feature selection using rough sets\[J\]. Information Sciences. 2016, 343-344: 41-65.](https://www.sciencedirect.com/science/article/pii/S0020025516000785)
- **HDC**: [Muhammad Summair Raza, Usman Qamar. A heuristic based dependency calculation technique for rough set theory\[J\]. Pattern Recognition. 2018, 81: 309-325.](https://www.sciencedirect.com/science/article/abs/pii/S0031320318301432)
- **DDC**: [Muhammad Summair Raza, Usman Qamar. Feature selection using rough set-based direct dependency calculation by avoiding the positive region\[J\]. International journal of approximate reasoning. 2018, 92(Jan.): 175-197.](https://www.sciencedirect.com/science/article/abs/pii/S0888613X17300178)
- **IFS**: [Asit K. Das, Shampa Sengupta, Siddhartha Bhattacharyya. A group incremental feature selection for classification using rough set theory based genetic algorithm\[J\]. Applied Soft Computing. 2018, 65: 400-411.](https://doi.org/10.1016/j.asoc.2018.01.040)

#### Particle Swarm Optimization

- [H. Hannah Inbarani, Ahmad Taher Azar, G. Jothi. Supervised hybrid feature selection based on PSO and rough sets for medical diagnosis\[J\]. Computer Methods and Programs in Biomedicine. 2014, 113(1): 175-185.](http://dx.doi.org/10.1016/j.cmpb.2013.10.007)
- [H. Hannah Inbarani, M. Bagyamathi, Ahmad Taher Azar. A novel hybrid feature selection method based on rough set and improved harmony search\[J\]. Neural Computing and Applications. 2015, 26(8): 1859-1880.](https://link.springer.com/article/10.1007/s00521-015-1840-0)

#### Improved Harmony Search

- [H. Hannah Inbarani, M. Bagyamathi, Ahmad Taher Azar. A novel hybrid feature selection method based on rough set and improved harmony search\[J\]. Neural Computing and Applications. 2015, 26(8): 1859-1880.](https://link.springer.com/article/10.1007/s00521-015-1840-0)
- [Jing Lu, Junhua Gu, Suqi Zhang, Zhan Jin. An improved harmony search algorithm for solving optimization problems\[J\]. Applied Mathematics & Computation. 2007, 188(2): 1567-1579.](https://ieeexplore.ieee.org/document/6818009)

#### Artificial Fish Swarm Algorithm

- [Yumin Chen, Qingxin Zhu, Huarong Xu. Finding rough set reducts with fish swarm algorithm\[J\]. Knowledge-Based Systems. 2015, 81: 22-29.](https://linkinghub.elsevier.com/retrieve/pii/S0950705115000337)


-----


## Structure of the project

	.
	+--src
	  +--main
	    +--java
	      +--featureSelection
	         +--repository
	         |  +--algorithm
	         |  |  +--alg // basic implemented feature selection algorithms here
	         |  |  +--opt // optimization algorithms (/evolutionary algorithms) here
	         |  |     +--alg
	         |  |     +--...  // specific optimization algorithms
	         |  +--entity
	         |  |  +--alg // entitis of basic implemented feature selection algorithms
	         |  |  +--opt // entitis of specific optimization algorithms
	         |  +--support
	         |     +--calculation
	         |     |  +--alg  // specific feature selection algorithm interfaces
	         |     |  +--...  // implemented specific feature (subset) importance calculations
	         |     +--shrink  // shrinking of dataset
	         +--tester
	            +--procedure
	            |  +--heuristic   // heuristic feature selections
	            |  |  +--...      // specific heuristic feature selection algorithm procedures
	            |  +--opt         // optimization algorithm based feature selections
	            |  |  +--...      // specific optimization algorithm procedures
	            |  +--param
	            +--report
	            +--statictis
	            +--utils


-----


## How do I use the codings?

To standardize the codings, I used *ProcedureContainer*s for executions. For a specific *FS* algorithm, you can
follow the test examples of every algorithm(see src/test/java).

	.
	+--src
	  +--test
	    +--java
	      +--featureSelection.tester.procedure
	         +--heuristic
	         |  +--algorithm
	         |     |
	         |     +--activeSampleSampleSelection
	         |     |  |
	         |     |  |  // Accelerated Sample Paired based FS
	         |     |  +--AcceleratedSamplePairSelectionBasedAttributeReductionHeuristicQRTesterTest.java
	         |     |  |  // Active Sample Selection based FS for incremental data processing
	         |     |  +--ActiveSampleSelectionBasedAttributeReductionHeuristicQRTesterTest.java
	         |     |  |  // Sample Paired Selection based FS for static data processing
	         |     |  +--SamplePairSelectionBasedAttributeReductionHeuristicQRTesterTest.java
	         |     |
	         |     +--classic
	         |     |  |
	         |     |  +--hash       // Classic attribute reduction using hash technique
	         |     |  |  +--ClassicAttributeReductionHashMapAlgorithmHeuristicQRTesterTest.java
	         |     |  +--sequential // Classic attribute reduction using hash technique
	         |     |     +--ClassicAttributeReductionSequentialAlgorithmHeuristicQRTesterTest.java
	         |     |
	         |     +--compactedDecisionTable.original
	         |     |  |  // Compacted decision table based
	         |     |  +--CompactedDecisionTableHeuristicQRTesterTest.java
	         |     |
	         |     +--dependencyCalculation
	         |     |  |  // DDC
	         |     |  +--DirectDependencyCalculationAlgorithmHeuristicQRTesterTest.java
	         |     |  |  // HDC
	         |     |  +--HeuristicDependencyCalculationAlgorithmHeuristicQRTesterTest.java
	         |     |  |  // IDC
	         |     |  +--IncrementalDependencyCalculationAlgorithmHeuristicQRTesterTest.java
	         |     |
	         |     +--discernibilityView
	         |     |  |  // FAR-DV
	         |     |  +--TengDiscernibilityViewHeuristicQRTesterTest.java
	         |     |
	         |     +--liangIncrementalAlgorithm
	         |     |  |  // GIARC
	         |     |  +--LiangIncrementalAlgorithmHeuristicQRTester4MultiObjectTest.java
	         |     |
	         |     +--liuQuickHash
	         |     |  +--LiuQuickHashHeuristicQRTesterTest.java
	         |     |
	         |     +--liuRoughSet
	         |     |  +--LiuRoughSetHeuristicQRTesterTest.java
	         |     |
	         |     +--positiveApproximationAccelerator.original
	         |     |  |  // ACC
	         |     |  +--PositiveApproximationAcceleratorAlgorithmHeuristicQRTesterTest.java
	         |     |
	         |     +--roughEquivalenceClassBased
	         |     |  |
	         |     |  +--nestedEquivalenceClass
	         |     |  |  +--incrementalPartition
	         |     |  |  |  |   // IP-NEC for static data processing
	         |     |  |  |  +--NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StaticDataTest.java
	         |     |  |  |  |   // IP-NEC for incremental data processing
	         |     |  |  |  +--NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StreamDataTest.java
	         |     |  |  +--staticData
	         |     |  |     |   // S-NEC
	         |     |  |     +--NestedEquivalenceClassBasedAlgorithmSimpleRealtimeCountingHeuristicQRTesterTest.java
	         |     |  |
	         |     |  +--original.extension
	         |     |     +--incrementalDecision
	         |     |     |  |  // ID-REC / C-NEC
	         |     |     |  +--RoughEquivalenceClassBasedAlgorithmIncrementalDecisionHeuristicQRTesterTest.java
	         |     |     +--incrementalPartition
	         |     |     |  |  // IP-REC / IP-NEC / G-NEC
	         |     |     |  +--RoughEquivalentClassBasedAlgorithmIncrementalPartitionHeuristicQRTesterTest.java
	         |     |     +--simpleCounting.realtime
	         |     |        |  // S-NEC
	         |     |        +--RECBasedAlgorithmSimpleRealtimeCountingHeuristicQRTesterTest.java
	         |     |
	         |     +--semisupervisedRepresentative
	         |     |  |  // SRFS
	         |     |  +--SemisupervisedRepresentativesFeatureSelectionHeuristicQRTesterTest.java
	         |     |
	         |     +--toleranceClassPositiveRegionIncremental
	         |     |  |  // FSMV / TCPR for static data processing
	         |     |  +--ToleranceClassPositiveRegionIncrementalHeuristicQRTester4StaticDataTest.java
	         |     |  |  // FSMV / TCPR for object-vary data processing
	         |     |  +--ToleranceClassPositiveRegionIncrementalHeuristicQRTester4VaryObjectTest.java
	         |     |
	         |     +--xieDynamicIncompleteDSReduction
	         |        |  // DIDS for static data processing
	         |        +--XieDynamicIncompleteDSReductionHeuristicQRTester4DynamicDataTest.java
	         |        |  // DIDS for object/attribute/both-related update
	         |        +--XieDynamicIncompleteDSReductionHeuristicQRTester4StaticDataTest.java
	         |
	         +--opt
	            |
	            +--artificialFishSwarm
	            |  +--ArtificialFishSwarmFeatureSelectionTesterTest.java
	            |
	            +--genetic
	            |  +--GeneticAlgorithmFeatureSelectionTesterTest.java
	            |
	            +--improvedHarmonySearch
	            |  +--ImprovedHarmonySearchFeatureSelectionTesterTest.java
	            |
	            +--particalSwarm
	               +--ParticleSwarmOptimizationFeatureSelectionTesterTest.java



Take S-REC/NEC as an example:

- First, use *ProcedureParameters* to load parameters.


        boolean execCore = true;

        // obtain attributes = {1, 2, ..., C}
        int[] attributes = getAllConditionalAttributes();

        // Load parameters.
        ProcedureParameters parameters = new ProcedureParameters()
                // U
                .set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
                // C
                .set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
                // execute Core ?
                .set(true, ParameterConstants.PARAMETER_QR_EXEC_CORE, execCore)
                // set feature (subset) importance calculation class, one of the following:
                //  PositiveRegionCalculation4RSCREC
                //  DependencyRegionCalculation4RSCEEC
                .set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveRegionCalculation4RSCREC.class)
                // set significance deviation for calculation:
                //  PositiveRegionCalculation4RSCREC: int
                //  DependencyRegionCalculation4RSCEEC: double
                .set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0);


- Second, create an FS procedure. In this case, *RECBasedAlgorithmSimpleRealtimeCountingHeuristicQRTester*:


        boolean logOn = true;

        // Create a procedure.
        RECBasedAlgorithmSimpleRealtimeCountingHeuristicQRTester<Integer> tester =
                new RECBasedAlgorithmSimpleRealtimeCountingHeuristicQRTester<>(parameters, logOn);


- Finally, execute and get the output reduct:


        // Execute
        Collection<Integer> red = tester.exec();
        // print results.
        System.out.println("result : " + red);    // output like {1, 2, 3, ...}


-----


## Some possible questions

### What are the differences between *NEC* and *REC*?

*NEC* is the abbreviation of *Nested Equivalence Class* and *REC* stands for *Rough Equivalence Class*. They are just
two different names referring to the same algorithm, nothing more than that. Here is why:

At the early stage of our researches, our team named our algorithms by *REC*, and that's what I did too back then, like 
*S-REC*, *ID-REC*. But later when we were to publish the papers, our team decided to change the name into *Nested 
Equivalence Class* which seems would be much clearer to imply the thought behind *NEC* series algorithms. But the 
codings had already finished implementing by then, I decided not to rename them in my codings to avoid potential bugs 
despite it is easy and convenient to do so in an IDE. Then, when we started to design *IP-NEC/G-NEC*, we had already 
settled down the name as *NEC*, so I started to use it. As you can see, I also preserved *IP-REC* as the early 
version of *IP-NEC/G-NEC* for the same reason.

For ancestors of *S-REC/NEC* algorithms, you can learn more from the following papers:

- [ZHAO Jie, ZHANG Kaihang, DONG Zhenning, etc. Complete minimum attribute reduction algorithm using fusion of rough equivalence class and tabu search\[J\]. Systems Engineering — Theory & Practice, 2017, 37(7): 1867–1883.](http://www.cnki.com.cn/Article/CJFDTotal-XTLL201707019.htm)
- [ZHAO Jie, ZHANG Kaihang, DONG Zhenning, etc. Rough equivalence class bilateral-decreasing based incremental core and attribute reduction computation with multiple Hashing\[J\]. Systems Engineering — Theory & Practice, 2017, 37(2): 504–522.](http://www.cnki.com.cn/Article/CJFDTotal-XTLL201702022.htm)
- [ZHAO Jie, ZHANG Kaihang, DONG Zhenning. Rough equivalence class based attribute reduction algorithm with bilateral-pruning strategies and multiple Hashing\[J\]. Control and Decision, 2016, 31(011):1921-1935.](http://www.cnki.com.cn/Article/CJFDTotal-KZYC201611001.htm)

### Why is *I-NEC* not present yet?

Up to now, we had just finished *I-NEC* experiments. Even we have finished the major designing of *I-NEC*, there is no
guarantee that we won't come up with a better version, then some major modifications would be made. Second, the paper
is still in progress and has not been published yet. So, I am not allowed to make the relevant codings public yet. 
Please do understand. However, I do hope it could be released soon.

### How do I choose which algorithm to execute?

Well, I don't think there is a perfect answer to this question, different algorithms fit different occasions and
data in different ways. Still, here are some recommendations:

- If you are expecting only **1 reduct**:

  Heuristic algorithms are recommended as they only return 1 reduct within a
  shorter time(also depends on different situations and algorithms). However, optimization-algorithm-based methods
  can return 1 or more than 1 reduct in some implementations(depends on your settings).

- If you are expecting **multiple reducts**:

  Optimization algorithms are recommended as Heuristic ones usually fail to achieve this goal.

- If you are expecting **less parameters** to be set by you:

  Heuristic algorithms are recommended as they usually require no/less parameters set by user.

- If your input dataset contains **in-complete data**:

  FS algorithms compatible with in-complete data are recommended, like *I-NEC*, *FSMV/TCPR*, *DIDS*, for they are
  supported by theories that have taken in-complete data into considerations in discussions(specialized for in-complete
  data. Otherwise, other algorithms that are designed for complete data are recommended.

- If you are handling **incremental data**:

  FS algorithms designed for incremental scenarios are recommended, i.e. *IP-REC/IP-NEC/G-NEC*, *GIARC*, *Active Sample
  Selection based*, *I-NEC*, *DIDS*, *FSMV/TCPR*, for they are designed to process incremental data. When new data
  arrives, specific schemes were designed to process them more efficiently by avoiding re-computing the whole dataset
  including new and old data.

- How do I choose a specific FS algorithm:

  Also, no perfect answer to this one. But, as one of the authors/designers of *REC/NEC* series, *REC/NEC* algorithms are
  recommended for multiple strategies had been introduced to accelerate the algorithms while maintaining the quality
  of reducts. However, other algorithms have their advantages too, so it is best for you to learn more details of the
  algorithms from the original papers and choose based on your interest.

### Are there any algorithm based on other theories implemented?

Apologies. There is no other algorithm based on other theories beside RST has been implemented in this project. As I 
have mentioned, this project only contains codings of FS algorithms that I have implemented during my researches with my 
mentor. Our researches mainly focus on RST based FS in the last few years (along with other researches and topics too). 
However, our team has already turned our attentions to algorithms based on other theories like Markov Blanket, Fuzzy Rough 
Set theory, etc. For now, from what I have learnt, no relevant paper has been published yet. Still, if you are 
interested in them, you can try contacting my mentor Pro.Zhao from *Guangdong University of Technology*.(e-mail: 
zhaojie@gdut.edu.cn)

### Would there be any update or new release of algorithms in this project in the future?

I am afraid not. I don't think my researches on *RST* based *FS* would continue after I graduate from university and 
start to work. So there is no more algorithms to be released here besides *I-NEC*. However, I do not deny the chances 
of myself coming back to it some day in the future. I guest it depends on how things would be in the future.

### Is there any user-friendly click-and-execute interface to be used?

Beside algorithm implementations, I did come up with multiple ways for algorithms execution with interfaces or in other 
forms, including Java GUI, Electron, MQ based service, Socket service, etc. However, the main focuses of the researches
that I was doing was the algorithm designing instead of UI. As the Java GUI and MQ service could satisfy the needs of 
experiments, I didn't spend too much time in building a user-friendly UI. Apologies.


## Statements

- Codings in this project were implemented by myself (as marked as @author Benjamin_L in codings) only, based on the  
  original papers and sudo-codes. However, be aware, I can not guarantee there is no major differences between my 
  implementations and the original algorithms, sudo-codes, and ideas, etc. of the papers, authors. If there is, I will 
  not take any responsibility.
- Codings in this project are only public for the use of researches, studies, but NOT business. I will not take any
  responsibility if anyone uses the codings to do anything illegal or (in fact) do anything.
- Please do not copy and publish any codings or ideas as your own work in any form without permissions. Otherwise, you 
  may face potential accusations. (Not just from me, but also maybe from other scholars too)
- Please do state clear references of this project and relevant papers when using any codes in this project for any 
  purpose, thank you.

  
-----

 <div align="right">Benjamin_L</div>

