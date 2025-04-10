package com.example.service;


import com.example.dto.CarDto;
import com.example.entity.Car;
import com.example.repo.CarRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class CarService {

    @Autowired
    private CarDto carDto;

    List<Car> carList=new ArrayList<>();


    //Configure input file location
    static String fileInPutPath="C:\\Users\\Ranjith.P\\CarsInPut.csv";
    //Configure output file locaton
    static String fileOutPutPath= "C:\\Users\\Ranjith.P\\CarsOutPut.csv";
    //Configure new Input file location
    static String newInputFilePath="C:\\Users\\Ranjith.P\\Cars\\CarsInPut.csv";


    @Scheduled(fixedDelay = 50000)
    public void schedulerMethod() throws  Exception{
         carList= loadData();
         this.saveListOfCars(carList);
         this.getDataFromDB();
         this.updateTheStatus();
         this.changeInputFileLocation();
    }


    public void changeInputFileLocation() throws IOException {
        File file = new File(newInputFilePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        Files.move(Paths.get(fileInPutPath),
                Paths.get(file.toString()),
                StandardCopyOption.REPLACE_EXISTING);
        if(!new File(fileInPutPath).exists()){
            new File(fileInPutPath).createNewFile();
        }
    }

    public void updateTheStatus(){
        List<Car> list=new ArrayList<>();
        for (Car car : carDto.getAllData()) {
            if(car.getStatus().equals("new")){
                car.setStatus("done");
                list.add(car);
            }
        }
        this.saveListOfCars(list);

    }

    public  void saveListOfCars(List<Car> list){
        carDto.saveCarDetails(list);
    }

    public void getDataFromDB() throws IOException {
         for(Car car:carDto.getAllData()) {

             File file = new File(fileOutPutPath);
             if (!file.exists()) {
                 file.createNewFile();
             }
             if (car.getStatus().equals("new")) {
                 try (FileWriter writer = new FileWriter(file, true)) {
                     writer.write(car.getId() + "," + car.getManufactor() + "," + car.getManufactor() + "," + car.getYear() + "\n");
                 } catch (Exception e) {
                     System.out.println(e.getMessage());
                 }
             }
         }
    }


    public static List<Car> loadData() throws IOException {
        List<Car> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileInPutPath))) {
            String str;
            while ((str = reader.readLine()) != null) {
                String[] arr = str.split(",");

                String manufacturer = arr[0].trim();
                String model = arr[1].trim();

                int year;
                try {
                    year = Integer.parseInt(arr[2].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid year format, skipping row: " + str);
                    continue;
                }
                list.add(new Car(manufacturer, model, year,"new"));
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return list;
    }
}

