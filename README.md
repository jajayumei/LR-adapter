# LR-adapter
This adapter will allow you to run tests on the game Lab Recruits from the Axini Modeling Platform. The adapter implements a set of AMP-labels, which can be viewed in the model `demo_ict_with_industry.aml` in the project `Lab Recruit` in AMP.

## Setup
### Build this project
The most convenient way to build this project, would be to do it from the Eclipse IDE. From Eclipse, use the **import this project as a Maven project** option. This should build the project.

### Install the Lab Recruits game
You will also need to get the [Lab Recruits](https://github.com/iv4xr-project/labrecruits) game. There are build instructions there. To build it, you will need a specific version of UNITY installed. This information is available in the build instructions.

## Demo
1. Run the game Lab Recruits.
1. Go to AMP, open the project `Lab Recruit` and choose the the model `demo_ict_with_industry.aml` in the in AMP.
1. From Eclipse, run the main method of the class `Adapter` (which can be found in ` ict_with_industry/src/main/java/ampLRadapter/Adapter.java `). This will launch this Adapter, load a demo-level to Lab Recruits, and then connect to AMP.
1. You can now start running tests from AMP

## Notes
### Levels
You can choose a different level by changing the `levelName` variable in the `SUTLabRecruits class`, which can be found at the location `ict_with_industry/src/main/java/ampLRadapter/SUTLabRecruits.java`.
The levels themselves can be found at `ict_with_industry/resources/levels`
Here you can also add new levels yourself. See the documentation of Lab Recruits for more details.

### Manual test run
It is also possible to run a simple test manually, without using AMP. To do this, run the code at `ict_with_industry/src/main/java/ampLRadapter/SeparateTesting.java`.
Here you can give the commands, `explore`, `door`, and `button` to interact with elements in the game. 
