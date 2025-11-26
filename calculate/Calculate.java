package calculate;

import object.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Calculate {
    public Para para = new Para();
    public Random random = new Random();

    //两点间距离计算：欧氏距离(m)
    public double distanceCal(Point point1, Point point2)
    {
        double dx = point1.corX - point2.corX;
        double dy = point1.corY - point2.corY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    //节点插入代价计算
    public double insertCost(Vehicle vehicle, Node nodeToInsert, int insertPos)
    {
        //获取插入位置的前后节点
        Node preNode = vehicle.nodes.get(insertPos - 1);
        Node nextNode = vehicle.nodes.get(insertPos);

        double cost = distanceCal(preNode.point, nodeToInsert.point)
                    + distanceCal(nodeToInsert.point, nextNode.point)
                    - distanceCal(preNode.point, nextNode.point);

        return cost;
    }

    //解的目标函数值计算
    public double calSolObj(Solution solution)
    {
        return para.omega_1 * calSolCompleteTime(solution) + para.omega_2 * solution.drones.size();
    }

    //解的物资配送任务完成时间
    public double calSolCompleteTime(Solution solution)
    {
        double maxCompleteTime = -1;

        for (Vehicle vehicle : solution.vehicles)
        {
            double currentVehTime = 0;

            for (int i = 0; i < vehicle.nodes.size() - 1; i++)
            {
                Point point1 = vehicle.nodes.get(i).point;
                Point point2 = vehicle.nodes.get(i + 1).point;

                currentVehTime += distanceCal(point1, point2) / vehicle.speed;
            }

            if (currentVehTime > maxCompleteTime)
            {
                maxCompleteTime = currentVehTime;
            }
        }
        return maxCompleteTime;
    }

    //两个point连线的中点位置计算
    public Point midPoint(Point point1, Point point2)
    {
        Point midPoint = new Point();
        midPoint.corX = (point1.corX + point2.corX) / 2.0;
        midPoint.corY = (point1.corY + point2.corY) / 2.0;

        return midPoint;
    }


    //记录每个时段车辆的位置
    public void getVehiclePoint(Vehicle vehicle, List<Point> points, Solution solution)
    {
        vehicle.points.clear(); //清空之前添加的points（好像本来就是空的？）

        double currentTime = 0;

        //总行驶时间
        double totalTime = calSolCompleteTime(solution);
        int currentSegment = 0;
        int totalTimeSlots = (int) Math.ceil(totalTime / para.deltaT);

        for (int k = 0; k < totalTimeSlots; k++)
        {
            double targetTime = k * para.deltaT;

            //找到当前时段车辆应该在哪个时段
            while (currentSegment < vehicle.nodes.size() - 1)
            {
                Point startPoint = vehicle.nodes.get(currentSegment).point;
                Point endPoint = vehicle.nodes.get(currentSegment + 1).point;

                double segmentDis = distanceCal(startPoint, endPoint);
                double segmentTime = segmentDis / vehicle.speed;

                //如果还在当前路段内
                if (currentTime + segmentTime >= targetTime)
                {
                    double timeInSegment = targetTime - currentTime;
                    double progress = timeInSegment / segmentTime;

                    //计算位置
                    double x = startPoint.corX + (endPoint.corX - startPoint.corX) * progress;
                    double y = startPoint.corY + (endPoint.corY - startPoint.corY) * progress;

                    Point timeSlotPosition = new Point();
                    timeSlotPosition.corX = x;
                    timeSlotPosition.corY = y;
                    timeSlotPosition.time = targetTime;
                    points.add(timeSlotPosition);
                    break;
                }
                else
                {
                    //移动到下一个路段
                    currentTime += segmentTime;
                    currentSegment++;
                }
            }

            //如果已经回到仓库，保持在仓库位置
            if (currentSegment >= vehicle.nodes.size() - 1)
            {
                Point endPoint = vehicle.nodes.get(vehicle.nodes.size() - 1).point;
                Point timeSlotPosition = new Point();
                timeSlotPosition.corX = endPoint.corX;
                timeSlotPosition.corY = endPoint.corY;
                timeSlotPosition.time = targetTime;
                points.add(timeSlotPosition);
            }
        }
    }

    //计算车辆当前负载量
    public double vehRemainLoad(Vehicle vehicle)
    {
        double remainLoad = vehicle.capacity;

        if (vehicle.nodes.size() > 1)
        {
            for (int i = 1; i < vehicle.nodes.size() - 1; i++)
            {
                Node node = vehicle.nodes.get(i);
                remainLoad -= node.demand;
            }
        }
        return remainLoad;
    }

    //计算车辆的point信息
    public void calVehiclesPoint(Solution solution)
    {
        for (Vehicle vehicle : solution.vehicles)
        {
            getVehiclePoint(vehicle, vehicle.points, solution);
        }
    }
}