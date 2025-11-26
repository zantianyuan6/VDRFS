package algorithm;

import calculate.Calculate;
import object.*;

import java.util.*;

public class SA {
    private Solution currentSol = new Solution();
    private Solution candidateSol = new Solution();
    private Solution bestSol = new Solution();
    public Random random = new Random();
    public double T = 1500; //当前温度
    public double T_min = Math.pow(10, -10); //最低温度
    public double rho = 0.8; //降温速率
    public int L = 500; //当前温度循环次数
    public Para para = new Para();
    public Calculate calculator = new Calculate();

    public void SA_sol(Solution solution)
    {
        //初始化当前解
        currentSol = copySolution(solution);
        bestSol = copySolution(solution);

        double bestObj = calculator.calSolObj(bestSol);
        System.out.println("Initial Obj: " + bestObj);

        while (T > T_min)
        {
            double currentObj = calculator.calSolObj(currentSol);

            for (int i = 0; i < L; i++)
            {
                //重置候选解为当前解的副本
                candidateSol = copySolution(currentSol);

                //删除仓库
                deleteDepot(candidateSol);

                //路径扰动
                disorder(candidateSol);

                //添加仓库
                addDepot(candidateSol);

                //检查车辆路径可行性
                if (!isAvailable(candidateSol))
                {
                    continue;
                }

                //无人机使用数量压缩子规划
                subSchedule(candidateSol);

                double obj = calculator.calSolObj(candidateSol);

                //蒙特卡洛准则
                if (obj < currentObj)
                {
                    currentObj = obj;
                    currentSol = copySolution(candidateSol);

                    if (obj < bestObj)
                    {
                        bestObj = obj;
                        bestSol = copySolution(candidateSol);
                    }
                }
                else
                {
                    double delta = obj - currentObj;
                    double probability = Math.exp(-delta / T);

                    if (random.nextDouble() < probability)
                    {
                        currentObj = obj;
                        currentSol = copySolution(candidateSol);
                    }
                }
            }
            T *= (1 - rho);

            System.out.println("Obj: " + currentObj);
        }
        printResult(bestSol);
    }

    //复制解
    public Solution copySolution(Solution original)
    {
        Solution copy = new Solution();

//        //复制车辆列表
//        copy.vehicles = copyVehicles(original.vehicles);
//
//        //复制无人机列表
//        copy.drones = copyDrones(original.drones);
        Map<Integer, Vehicle> vehicleMap = new HashMap<>();
        copy.vehicles = new ArrayList<>();
        for (Vehicle v : original.vehicles)
        {
            Vehicle nv = new Vehicle();
            nv.id = v.id;
            nv.capacity = v.capacity;

            nv.point = new Point();
            nv.point.corX = v.point.corX;
            nv.point.corY = v.point.corY;
            nv.point.time = v.point.time;

            nv.points = new ArrayList<>();
            for (Point p : v.points)
            {
                Point np = new Point();
                np.corX = p.corX;
                np.corY = p.corY;
                np.time = p.time;
                nv.points.add(np);
            }

            nv.nodes = new ArrayList<>(v.nodes);

            copy.vehicles.add(nv);
            vehicleMap.put(nv.id, nv);
        }

        copy.drones = new ArrayList<>();
        for (Drone d : original.drones)
        {
            Drone nd = new Drone();
            nd.id = d.id;
            nd.R = d.R;

            nd.points = new ArrayList<>();
            for (Point p : d.points)
            {
                Point np = new Point();
                np.corX = p.corX;
                np.corY = p.corY;
                np.time = p.time;
                nd.points.add(np);
            }

            nd.vehicles = new ArrayList<>();
            for (Vehicle v : d.vehicles)
            {
                Vehicle mapped = vehicleMap.get(v.id);
                if (mapped != null)
                {
                    nd.vehicles.add(mapped);
                    mapped.drone = nd;
                }
            }
            copy.drones.add(nd);
        }

        return copy;
    }

    /**
     * 同一路径节点交换算子
     * 随机选择一条路径上的两个受灾节点，并交换其位置，形成一条新的车辆路径。
     */
    public void swap(Solution solution)
    {
        int vehicleIndex = random.nextInt(solution.vehicles.size());

        Vehicle vehicle = solution.vehicles.get(vehicleIndex);

        if (vehicle.nodes.size() < 2)
        {
            return;
        }

        int node1Index = random.nextInt(vehicle.nodes.size());
        int node2Index = random.nextInt(vehicle.nodes.size());

        //确保选择两个不同的节点
        while (node1Index == node2Index)
        {
            node2Index = random.nextInt(vehicle.nodes.size());
        }

        Node node1 = vehicle.nodes.get(node1Index);
        Node node2 = vehicle.nodes.get(node2Index);

        //交换
        vehicle.nodes.set(node1Index, node2);
        vehicle.nodes.set(node2Index, node1);
    }

    /**
     * 节点迁移算子
     * 随机选择一条车辆路径的受灾节点，将其插入到另一条随机路径的随机位置
     */
    public void relocate(Solution solution)
    {
        int vehicle1Index = random.nextInt(solution.vehicles.size());
        int vehicle2Index = random.nextInt(solution.vehicles.size());

        //确保选择两个不同的车辆
        while (vehicle1Index == vehicle2Index) {
            vehicle2Index = random.nextInt(solution.vehicles.size());
        }

        Vehicle vehicle1 = solution.vehicles.get(vehicle1Index);
        Vehicle vehicle2 = solution.vehicles.get(vehicle2Index);

        if (vehicle1.nodes.size() <= 1) {
            return;
        }

        int nodeIndex = random.nextInt(vehicle1.nodes.size());
        Node node = vehicle1.nodes.get(nodeIndex);

        int insertIndex = random.nextInt(vehicle2.nodes.size() + 1);

        if (node.demand > calculator.vehRemainLoad(vehicle2))
        {
            return;
        }

        vehicle2.nodes.add(insertIndex, node);
        vehicle1.nodes.remove(nodeIndex);
    }

    /**
     * 不同路径节点交换算子
     * 随机选择两条路径上的各一个节点，并交换它们的位置
     */
    public void exchange(Solution solution)
    {
        int vehicle1Index = random.nextInt(solution.vehicles.size());
        int vehicle2Index = random.nextInt(solution.vehicles.size());

        //确保选择两个不同的车辆
        while (vehicle1Index == vehicle2Index)
        {
            vehicle2Index = random.nextInt(solution.vehicles.size());
        }

        Vehicle vehicle1 = solution.vehicles.get(vehicle1Index);
        Vehicle vehicle2 = solution.vehicles.get(vehicle2Index);

        if (vehicle1.nodes.isEmpty() || vehicle2.nodes.isEmpty())
        {
            return;
        }

        int node1Index = random.nextInt(vehicle1.nodes.size());
        int node2Index = random.nextInt(vehicle2.nodes.size());

        Node node1 = vehicle1.nodes.get(node1Index);
        Node node2 = vehicle2.nodes.get(node2Index);

        if ((node1.demand > calculator.vehRemainLoad(vehicle2)) ||
            (node2.demand > calculator.vehRemainLoad(vehicle1)))
        {
            return;
        }

        vehicle1.nodes.set(node1Index, node2);
        vehicle2.nodes.set(node2Index, node1);
    }

    //删除车辆路径中的仓库
    public void deleteDepot(Solution solution)
    {
        for (Vehicle vehicle : solution.vehicles)
        {
            vehicle.nodes.remove(0);
            vehicle.nodes.remove(vehicle.nodes.size() - 1);
        }
    }

    //车辆路径扰动
    public void disorder(Solution solution)
    {
        int operator = random.nextInt(3);

        switch (operator)
        {
            case 0:
                swap(solution);
                break;
            case 1:
                relocate(solution);
                break;
            case 2:
                exchange(solution);
                break;
        }
    }

    //车辆路径是否可行
    public boolean isAvailable(Solution solution)
    {
        for (Vehicle vehicle : solution.vehicles)
        {
            if (vehicle.nodes.size() <= 2)
            {
                return false;
            }
        }
        return true;
    }

    //无人机使用数量压缩子规划
    public void subSchedule(Solution solution)
    {
//        addDepot(solution);

        calculator.calVehiclesPoint(solution);

        reassign(solution);
    }

    //为每个车辆路径加上仓库
    public void addDepot(Solution solution)
    {
        Node depot = Data.nodes.get(0);

        for (Vehicle vehicle : solution.vehicles)
        {
            vehicle.nodes.add(0, depot);
            vehicle.nodes.add(depot);
        }
    }

    //尝试将u_target的观测任务分配给其他无人机
    public void reassign(Solution solution)
    {
        //将无人机加入待检查列表
        List<Drone> uncheck = new ArrayList<>();
        uncheck.addAll(solution.drones);

        while (!uncheck.isEmpty())
        {
            //随机选取一架无人机u_target
            int u_targetIdx = random.nextInt(uncheck.size());
            Drone u_target = uncheck.get(u_targetIdx);

            boolean taskReassigned = false;
            for (int i = 0; i < uncheck.size(); i++)
            {
                //如果遍历到的无人机是u_target，跳过
                if (i == u_targetIdx)
                {
                    continue;
                }

                Drone u_accept = uncheck.get(i);

                //备份
                List<Vehicle> original_vehicles = new ArrayList<>(u_accept.vehicles);
                List<Point> original_points = new ArrayList<>(u_accept.points);

                u_accept.vehicles.addAll(u_target.vehicles);

                schedule_u_accept_route(solution, u_accept);
                if (isAvailable_drone(u_accept))
                {
                    taskReassigned = true;
                    u_target.vehicles.clear();
                    break;
                }
                else
                {
                    //恢复原始状态
                    u_accept.vehicles.clear();
                    u_accept.vehicles.addAll(original_vehicles);
                    u_accept.points.clear();
                    u_accept.points.addAll(original_points);
                }
            }
            uncheck.remove(u_targetIdx);
            if (taskReassigned)
            {
                solution.drones.remove(u_target);
            }
        }
    }

    //重新规划无人机路径
    public void schedule_u_accept_route(Solution solution, Drone drone)
    {
        drone.points.clear();

        double currentTime = 0;
        double totalTime = calculator.calSolCompleteTime(solution);
        int totalTimeSlots = (int) Math.ceil(totalTime / para.deltaT);
        Node depot = Data.nodes.get(0);

        //为无人机的points列表添加仓库（起点）
        Point depotPoint = new Point();
        depotPoint.corX = depot.point.corX;
        depotPoint.corY = depot.point.corY;
        depotPoint.time = currentTime;

        //无人机当前位置
        Point currentPos = depotPoint;
        drone.points.add(currentPos);

        for (int k = 1; k < totalTimeSlots; k++)
        {
            currentTime = k * para.deltaT;

            List<Vehicle> unfinishedVehicles = new ArrayList<>();
            //获取当前未返回仓库的车辆数量
            for (Vehicle vehicle : drone.vehicles)
            {
                if (!returnDepot(vehicle))
                {
                    unfinishedVehicles.add(vehicle);
                }
            }

            Point nextDronePos = new Point();
            nextDronePos.time = currentTime;

            /**
             * 根据未返回仓库的车辆数量规划无人机位置
             */
            //该无人机观测的所有车辆均已返回仓库
            if (unfinishedVehicles.size() == 0)
            {
                //该无人机观测的车辆均已返回仓库，无人机向仓库方向移动
                nextDronePos = singlePointSchedule(currentPos, depotPoint, drone.speed);
            }
            //只有一辆车未返回仓库
            else if (unfinishedVehicles.size() == 1)
            {
                Vehicle vehicle = unfinishedVehicles.get(0);
                Point targetPos = vehicle.points.get(k);
                nextDronePos = singlePointSchedule(currentPos, targetPos, drone.speed);
            }
            //多辆车未返回仓库
            else
            {
                nextDronePos = multiVehSchedule(currentPos, unfinishedVehicles, k, drone.speed);
            }
            drone.points.add(nextDronePos);
            currentPos = nextDronePos;
        }
    }

    /**
     * 只有一辆车或要返回仓库
     * 向仓库或车辆方向移动
     * @param currentPos
     * @param targetPoint
     * @param droneSpeed
     * @return
     */
    public Point singlePointSchedule(Point currentPos, Point targetPoint, double droneSpeed)
    {
        Point nextPos = new Point();
        double distance = calculator.distanceCal(currentPos, targetPoint);
        double maxDistance = droneSpeed * para.deltaT;

        //下一时段能够到达仓库
        if (distance <= maxDistance)
        {
            nextPos.corX = targetPoint.corX;
            nextPos.corY = targetPoint.corY;
        }
        //无法到达仓库
        else
        {
            double ratio = maxDistance / distance;
            nextPos.corX = currentPos.corX + ratio * (targetPoint.corX - currentPos.corX);
            nextPos.corY = currentPos.corY + ratio * (targetPoint.corY - currentPos.corY);
        }
        return nextPos;
    }

    public Point multiVehSchedule(Point currentPos, List<Vehicle> vehicles, int timeSlot, double droneSpeed)
    {
        Vehicle[] farthestPair = farthestVehiclePair(vehicles, timeSlot);

        //计算两辆车的连线中点位置
        Point point1 = farthestPair[0].points.get(timeSlot);
        Point point2 = farthestPair[1].points.get(timeSlot);
        Point midPoint = calculator.midPoint(point1, point2);

        return singlePointSchedule(currentPos, midPoint, droneSpeed);
    }

    //距离最远的两辆车
    public Vehicle[] farthestVehiclePair(List<Vehicle> vehicles, int timeSlot)
    {
        Vehicle[] farthestPair = new Vehicle[2];
        double maxDistance = -1;

        for (int i = 0; i < vehicles.size(); i++)
        {
            for (int j = i + 1; j < vehicles.size(); j++)
            {
                Vehicle vehicle1 = vehicles.get(i);
                Vehicle vehicle2 = vehicles.get(j);

                Point point1 = vehicle1.points.get(timeSlot);
                Point point2 = vehicle2.points.get(timeSlot);
                double distance = calculator.distanceCal(point1, point2);
                if (distance >= maxDistance)
                {
                    maxDistance = distance;
                    farthestPair[0] = vehicle1;
                    farthestPair[1] = vehicle2;
                }
            }
        }
        return farthestPair;
    }

    //判断车辆是否已经返回仓库
    public boolean returnDepot(Vehicle vehicle)
    {
        Point vehiclePoint = vehicle.nodes.get(vehicle.nodes.size() - 1).point;
        Point depotPoint = Data.nodes.get(0).point;

        if (vehiclePoint.corX != depotPoint.corX
            || vehiclePoint.corY != depotPoint.corY)
        {
            return false;
        }
        return true;
    }

    //判断无人机的观测范围是否能够持续覆盖其观测的车辆
    public boolean isAvailable_drone(Drone drone)
    {
        for (int i = 0; i < drone.points.size(); i++)
        {
            Point dronePos = drone.points.get(i);
            for (Vehicle vehicle : drone.vehicles)
            {
                Point vehiclePos = vehicle.points.get(i);
                double distance = calculator.distanceCal(dronePos, vehiclePos);

                if (distance > drone.R)
                {
                    return false;
                }
            }
        }
        return true;
    }

    public void printResult(Solution solution)
    {
        System.out.println("========SA Final Solution========");

        //车辆路径
        System.out.println("Vehicle route:");
        for (int i = 0; i < solution.vehicles.size(); i++)
        {
            Vehicle vehicle = solution.vehicles.get(i);

            System.out.println("Vehicle " + (i + 1) + " Route: ");
            System.out.print("[");
            for (int j = 0; j < vehicle.nodes.size(); j++)
            {
                System.out.print(vehicle.nodes.get(j).id + ",");
            }
            System.out.println("]");
        }



        //统计分配给无人机的车辆
        for (Drone drone : solution.drones)
        {
            for (Vehicle vehicle : drone.vehicles)
            {
                System.out.println("Drone " + drone.id + " -- " +"Vehicle " + vehicle.id);
            }
        }

        System.out.println("Complete Time: " + calculator.calSolCompleteTime(solution) + "h");
        System.out.println("Drone num: " + solution.drones.size());
        System.out.println("Final obj: " + calculator.calSolObj(solution));
    }
}