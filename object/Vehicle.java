package object;

import java.util.ArrayList;
import java.util.List;

public class Vehicle {
    public int id;
    public int capacity; //车辆容量
    public final double speed = 60; //行驶速度 km/h
    public Point point = new Point();
    public List<Point> points = new ArrayList<>(); //车辆每个时段的位置列表
    public List<Node> nodes = new ArrayList<>(); //节点列表
    public Drone drone; //伴飞该车辆的无人机
}
