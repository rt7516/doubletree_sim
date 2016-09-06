# doubletree_sim

This is a simulation of Double tree.
For sending requests it uses poisson distribution.
It calculates average queue length, max queue length, standard deviation, average lookup time and average path length of a request.

How to Run :
-----------


javac *.java

java MasterStatic

Sample Output:
--------------

How many servers do you need?
27931

*************************************************************
What do you want to do ?
1. Store a File
2. Search for a File
3. Dispaly Queue Lengths
4. Dispaly Stats
5. Random chosen servers :
*************************************************************

2

Select a Mode
1 --> 1 Request Per File
2 --> All Requests for 1 File
3 --> Multiple equally popular files
4 --> Random
5 --> Max Storage
6 --> One copy & No replications

4

Enter tick
1000

Request rate ?
25137            ==> 0.9 * 27931 = 25137

Enter the lambda value?
0.9             ==> request rate

