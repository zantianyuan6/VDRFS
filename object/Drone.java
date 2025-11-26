package object;

import java.util.ArrayList;
import java.util.List;

public class Drone {
    public int id;
    public final double speed = 60; //无人机飞行速度 km/h
    public List<Vehicle> vehicles = new ArrayList<>(); //无人机观测的车辆列表
    public List<Point> points = new ArrayList<>(); //无人机每个时段的坐标列表
    public double R = 15; //无人机观测范围半径：15km
}
