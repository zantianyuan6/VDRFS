package algorithm;

import object.Solution;
import utils.ReadInstance;

public class Algorithm {
    public ReadInstance readInstance = new ReadInstance();
    public VRPInitialSol vrpInitialSol = new VRPInitialSol();
    public FSInitialSol fsInitialSol = new FSInitialSol();
    public SA sa = new SA();


    public void solute() throws Exception
    {
        long startTime = System.currentTimeMillis();

        readInstance.readInstance("Schedule/src/instance/solomon_100/RC104.txt");

        Solution solution = new Solution();

        solution.vehicles = vrpInitialSol.vehicleInitSol();

        solution.drones = fsInitialSol.fsInitialSol(solution);

        sa.SA_sol(solution);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Run Time: " + totalTime + "ms");
    }
}
