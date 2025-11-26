package algorithm;

import calculate.Calculate;
import object.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VRPInitialSol {
    public Calculate calculate = new Calculate();

    public List<Vehicle> vehicleInitSol()
    {
        Solution solution = new Solution();
        solution.vehicles.addAll(Data.vehicles);

        //车辆从仓库出发
        initVehicle(solution);

        //节点与仓库的距离排序
        nodesRank();

        //为节点安排车辆，并将节点插入车辆nodes列表中
        assignVehAndInsertNode(solution);

        printResult(solution);

        return solution.vehicles;
    }

    //初始化：所有车辆从仓库出发，最后回到仓库
    public void initVehicle(Solution solution)
    {
        for (int i = 0; i < solution.vehicles.size(); i++)
        {
            Vehicle vehicle = solution.vehicles.get(i);

            //车辆从仓库出发
            vehicle.nodes.add(Data.nodes.get(0));

            //最后返回仓库
            vehicle.nodes.add(Data.nodes.get(0));
        }
    }

    //对所有受灾节点按距离仓库的远近进行降序排序
    public void nodesRank()
    {
        Node depot = Data.nodes.get(0);

        //受灾节点列表
        List<Node> customersNode = new ArrayList<>();

        //将所有受灾节点添加到customersNode
        for (int i = 1; i < Data.nodes.size(); i++)
        {
            customersNode.add(Data.nodes.get(i));
        }

        Collections.sort(customersNode, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                double dis1 = calculate.distanceCal(depot.point, o1.point);
                double dis2 = calculate.distanceCal(depot.point, o2.point);

                //降序
                return Double.compare(dis2, dis1);
            }
        });

        //重构Nodes列表：仓库+后续节点
        Data.nodes.clear();
        Data.nodes.add(depot);
        Data.nodes.addAll(customersNode);
    }

    //为排序后的节点分配车辆
    public void assignVehAndInsertNode(Solution solution)
    {
        for (int i = 1; i < Data.nodes.size(); i++)
        {
            Node node = Data.nodes.get(i);

            //为当前节点选择车辆
            Vehicle selectedVehicle = vehicleSelect(solution, node);

            insertNode(selectedVehicle, node);
        }
    }

    //选择车辆
    public Vehicle vehicleSelect(Solution solution, Node targetNode)
    {
        Vehicle selectedVeh = new Vehicle();
        double shortestRouteLength = Double.MAX_VALUE;
        double shortestDistanceToNode = Double.MAX_VALUE;

        for (int i = 0; i < solution.vehicles.size(); i++)
        {
            Vehicle vehicle = solution.vehicles.get(i);

            if (calculate.vehRemainLoad(vehicle) < targetNode.demand)
            {
                continue; //当前车辆剩余容量不足，则跳过该车辆
            }

            double currentLength = vehRouteLength(vehicle);

            //如果找到路径长度更短的车辆
            if (currentLength < shortestRouteLength)
            {
                shortestRouteLength = currentLength;
                selectedVeh = vehicle;

                //更新车辆到目标节点的距离
                Point vehLastPoint = getVehLastPoint(vehicle);
                shortestDistanceToNode = calculate.distanceCal(vehLastPoint, targetNode.point);
            }

            //如果路径长度相同，选择距离目标节点最近的车辆
            else if (currentLength == shortestRouteLength)
            {
                Point vehLastPoint = getVehLastPoint(vehicle);
                double distanceToNode = calculate.distanceCal(vehLastPoint, targetNode.point);

                if (distanceToNode < shortestDistanceToNode)
                {
                    shortestDistanceToNode = distanceToNode;
                    selectedVeh = vehicle;
                }
            }
        }
        return selectedVeh;
    }

    //获取车辆当前位置
    public Point getVehLastPoint(Vehicle vehicle)
    {
        if (vehicle.nodes.size() > 0)
        {
            return vehicle.nodes.get(vehicle.nodes.size() - 1).point;
        }
        else
        {
            return vehicle.point;
        }
    }

    //计算车辆行驶路程长度
    public double vehRouteLength(Vehicle vehicle)
    {
        double length = 0;

        if (vehicle.nodes.size() > 1)
        {
            for (int i = 0; i < vehicle.nodes.size() - 1; i++)
            {
                Point point1 = vehicle.nodes.get(i).point;
                Point point2 = vehicle.nodes.get(i +1).point;

                length += calculate.distanceCal(point1, point2);
            }
        }
        return length;
    }

    //节点插入
    public void insertNode(Vehicle vehicle, Node nodeToInsert)
    {
        double minInsertCost = Double.MAX_VALUE;
        int bestInsertPos = -1;

        /**
         * 如果该车辆的nodes列表size≤2（只有从仓库出发并返回仓库）
         * 那么直接添加到该车辆的nodes列表中
         */
        if (vehicle.nodes.size() <= 2)
        {
            vehicle.nodes.add(1, nodeToInsert);
        }

        /**
         * 如果该车辆的nodes列表size大于2
         * 计算插入代价，选择代价最小的插入点进行插入
         */
        else
        {
            for (int pos = 1; pos < vehicle.nodes.size(); pos++)
            {
                double cost = calculate.insertCost(vehicle, nodeToInsert, pos);
                if (cost < minInsertCost)
                {
                    minInsertCost = cost;
                    bestInsertPos = pos;
                }
            }
            vehicle.nodes.add(bestInsertPos, nodeToInsert);
        }
    }

    public void printResult(Solution solution)
    {
        //完工时间计算
        double completionTime = calculate.calSolObj(solution);

        System.out.println("========VRP Initial Solution========");
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
        System.out.println("Completion Time: " + completionTime + "h");
    }
}
