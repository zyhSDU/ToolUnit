// VALIDATION queries
// VALIDATION queries

simulate 1 [<=100] { Kim.Aalborg + 2*Kim.Bike + 4*Kim.Easy + 6*Kim.Heavy + 8*Kim.Train + 10*Kim.Go + 12*Kim.Wait + 14*Kim.Sydney }
// Compute sample trajectories where the location is encoded as a trip level over time. Select the query, click "Check", then right-click the query and choose "Simulations (1)". To hide the legend: right-click on the plot and untick Show -> Legend.

simulate 100 [<=100] { 14+Kim.Aalborg, 12+Kim.Bike, 10+Kim.Easy, 8+Kim.Heavy, 6+Kim.Train, 4+Kim.Go, 2+Kim.Wait, Kim.Sydney }
// Show an overview of when and where most of the time is being spent.

A<> Kim.Sydney && time<=60
// Is it possible that we can always reach Sydney within 60 minutes? Answer is no (not satisfied), thus not safe. Note that only regular clocks (non-hybrid) can be accessed by symbolic queries.

Pr[<=60](<> Kim.Sydney)
// What is the probability of reaching Sydney within 60 minutes? Answer is about 0.975 (very likely)

Pr[<=200](<> Kim.Sydney)
// Estimate the average trip time to Sydney by inspecting the probability distribution over time (click "Check", then right-click on query and select "Cumulative probability distribution") Answer: about 28 minutes.

E[<=200 ; 500] (max: trip)
// Estimate the maximum time (needed to reach Sydney, where the clock time is stopped in Sydney). Answer is about 28 minutes (similar to the query above) Note that SMC queries may use hybrid clocks.

// SAFE STRATEGY
// SAFE STRATEGY

control: A<> Kim.Sydney && time<=60
// Is it possible to choose means of transport so that we reach Sydnay within 60 minutes no matter what traffic conditions are? Answer: possible (satisfied).

strategy GoSafe = control: A<> Kim.Sydney && time<=60
// Is it possible to choose means of transport so that we reach Sydnay within 60 minutes no matter what traffic conditions are? Answer: possible (satisfied) and the tool computes fully permissive strategy named "GoSafe".

A<> (Kim.Sydney && time<=60) under GoSafe
// Is it possible to reach Sydney within 60 minutes under decisions of GoSafe strategy? Answer: yes (satisfied).

Pr[<=60](<> Kim.Sydney) under GoSafe
// What is the probability of reaching Sydney within 60 minutes under the GoSafe strategy? Answer: >0.99 (almost certain: there was no counterexample found)

E[<=200; 500] (max: trip) under GoSafe
// Estimate the time (needed to reach Sydney, where the clock time is stopped). Answer is about 33 minutes. Notice that it is longer than a trip average without a strategy (about 28 minutes).

simulate 100 [<=100] { 14+Kim.Aalborg, 12+Kim.Bike, 10+Kim.Easy, 8+Kim.Heavy, 6+Kim.Train, 4+Kim.Go, 2+Kim.Wait, Kim.Sydney } under GoSafe
// Generate an overview where and when the time is being spent under GoSafe strategy. Result: most of the time is spent in Bike and Sydney.

// FAST STRATEGY
// FAST STRATEGY

strategy GoFast = minE (trip) [<=200] : <> Kim.Sydney
// Learn a strategy which minimizes the time to get to Sydney. The strategy is generated and stored under the name "GoFast".

Pr[<=60](<> Kim.Sydney) under GoFast
// What is the probability of reaching Sydney within 60 minutes using GoFast strategy? Answer is ~0.94 (smaller then probability without any strategy, i.e. rushing is risky)

Pr[<=200](<> Kim.Sydney) under GoFast
// What is the probability of reaching Sydney within 200 time units using GoFast strategy? Answer is >0.99 (eventuall all runs reach Sydney, no counterexample found)

E[<=200; 500] (max: trip) under GoFast
// Estimate the maximum time (needed to reach Sydney, where the clock time is stopped). Answer is about 15.4 minutes. Notice that it is much shorter/faster than GoSafe (33 minutes) or without any strategy (28 minutes).

simulate 100 [<=100] { 14+Kim.Aalborg, 12+Kim.Bike, 10+Kim.Easy, 8+Kim.Heavy, 6+Kim.Train, 4+Kim.Go, 2+Kim.Wait, Kim.Sydney } under GoFast
// Generate an overview where and when the time is being spent under GoFast strategy. Result: most of the time is spent in Easy and Heavy traffic and Sydney (using Car option).

// SAFE & FAST STRATEGY
// SAFE & FAST STRATEGY

strategy GoFastSafe = minE (trip) [<=200] : <> Kim.Sydney under GoSafe
// Optimize by learning the GoSafe strategy to shorten the time to Sydney. The result is a new strategy stored under name GoFastSafe.

Pr[<=60](<> Kim.Sydney) under GoFastSafe
// Evaluate the GoFastSafe by estimating a probability of reaching Sydney within 60 minutes. Answer is almost certain: with probability >0.99

Pr[<=200](<> Kim.Sydney) under GoFastSafe
E[<=200 ; 500] (max: trip) under GoFastSafe

// Estimate the time (needed to reach Sydney, where the clock time is stopped). Answer is about 22.4 minutes The result is better than GoSafe (33 minutes) but not as good as GoFast (15.4).
simulate 1000 [<=100] { 14+Kim.Aalborg, 12+Kim.Bike, 10+Kim.Easy, 8+Kim.Heavy, 6+Kim.Train, 4+Kim.Go, 2+Kim.Wait, Kim.Sydney } under GoFastSafe

// Generate an overview where and when the time is being spent under GoFast strategy. Result: most of the time is spent in Bike and Sydney (same as with GoSafe).
