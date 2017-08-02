Name: Jay Shah
UTA ID: 1001447749
Programming Language: Java

Code structure:
We first load the wumpus rules, the knowledge base and the statement file. Then we perform the TT_entails on the statement from the statement file and negation of the statement. Finally we compare the output of the two entails and store the result.

Running the code:
The program should be invoked from the commandline as follows:

javac CheckTrueFalse.java LogicalExpression.java
java CheckTrueFalse [wumpus_rules_file] [additional_knowledge_file] [statement_file]

For example:

java CheckTrueFalse wumpus_rules.txt kb.txt statement.txt