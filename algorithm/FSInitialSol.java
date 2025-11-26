package algorithm;

import calculate.Calculate;
import object.*;

import java.util.List;

public class FSInitialSol {
    public Calculate calculator = new Calculate();
    public Para para = new Para();

    //无人机资源分配与资源规划初始解
    public List<Drone> fsInitialSol(Solution solution)
    {
        //计算所有车辆的行驶轨迹
        allVehiclesPoints(solution);

        //初始化无人机资源并进行分配
        initDrones(solution);

        //所有无人机的初始行驶路径
        initDronePath(solution);

        //输出
        printResult(solution);

        return solution.drones;
    }

    public void allVehiclesPoints(Solution solution)
    {
        for (Vehicle vehicle : solution.vehicles)
        {
            calculator.getVehiclePoint(vehicle, vehicle.points, solution);
        }
    }

    /**
     * 初始化无人机资源
     * 为每辆车分配一架无人机
     */
    public void initDrones(Solution solution)
    {
        for (int i = 0; i < solution.vehicles.size(); i++)
        {
            Vehicle vehicle = solution.vehicles.get(i);
            Drone drone = new Drone();

            vehicle.drone = drone;
            drone.vehicles.add(vehicle);
            drone.id = i + 1;

            solution.drones.add(drone);
        }
    }

    //初始无人机行驶轨迹
    public void initDronePath(Solution solution)
    {
        for (int i = 0; i < solution.drones.size(); i++)
        {
            Drone drone = solution.drones.get(i);
            drone.points.addAll(drone.vehicles.get(0).points);
        }
    }

    public void printResult(Solution solution)
    {
        System.out.println("========FS Initial Solution========");

        System.out.println("Drone num:" + solution.drones.size());

        System.out.println("Drone-Vehicle Assign:");
        for (int i = 0; i < solution.drones.size(); i++)
        {
            Drone drone = solution.drones.get(i);
            for (int j = 0; j < drone.vehicles.size(); j++)
            {
                Vehicle vehicle = drone.vehicles.get(j);
                System.out.println("Drone " + drone.id + " -- " + "Vehicle" + vehicle.id);
            }
        }
    }
}
