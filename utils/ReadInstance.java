package utils;

import object.Data;
import object.Node;
import object.Vehicle;

import java.io.BufferedReader;
import java.io.FileReader;

public class ReadInstance {

    /**
     * 读取数据
     * @param filePath
     * @throws Exception
     */
    public void readInstance(String filePath) throws Exception
    {
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath)))
        {
            String line;

            //跳过空行
            reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();

            //车辆信息
            line = reader.readLine().trim();
            String[] vehicleInfo = line.split("\\s+");
            Data.vehicleNum = Integer.parseInt(vehicleInfo[0]);
            for (int i = 0; i < Data.vehicleNum; i++)
            {
                Vehicle vehicle = new Vehicle();
                vehicle.capacity = Integer.parseInt(vehicleInfo[1]);
                vehicle.id = i + 1;
                Data.vehicles.add(vehicle);
            }

            //跳过空行
            reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();

            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (line.isEmpty())
                {
                    continue;//跳过空行
                }

                String parts[] = line.split("\\s+");
                Node node = new Node();
                node.id = Integer.parseInt(parts[0]);
                node.point.corX = Double.parseDouble(parts[1]);
                node.point.corY = Double.parseDouble(parts[2]);
                node.demand = Double.parseDouble(parts[3]);
                Data.nodes.add(node);
            }
        }
    }

//    public static void main(String[] args) throws Exception
//    {
//        ReadInstance reader = new ReadInstance();
//        reader.ReadInstance("Schedule/src/instance/solomon_25/C101.txt");
//
//        for (int i = 0; i < Data.vehicles.size(); i++)
//        {
//            Vehicle vehicle = Data.vehicles.get(i);
//            System.out.print("Vehicle id: " + vehicle.id + " , Capacity: " + vehicle.capacity);
//            System.out.println();
//        }
//
//        for (int i = 0; i < Data.nodes.size(); i++)
//        {
//            Node node = Data.nodes.get(i);
//            System.out.print("Node id: " + node.id + " , "
//                    + "(" + node.point.corX + "," + node.point.corY + ")" +
//                    " , Demand: " + node.demand);
//            System.out.println();
//        }
//    }

}
