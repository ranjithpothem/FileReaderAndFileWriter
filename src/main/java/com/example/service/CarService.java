package com.example.service;


import com.example.dto.CarDto;
import com.example.entity.Car;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarService {

    @Autowired
    private CarDto carDto;

    List<Car> carList=new ArrayList<>();
    List<Car> newStatusCarList =new ArrayList<>();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CarService.class);


    static String fileInPutPath="C:\\Users\\Ranjith.P\\Cars\\Input\\CarsInPut.csv";

    static String fileOutPutPath="C:\\Users\\Ranjith.P\\Cars\\Output\\CarsOutPut.csv";

    static String newInputFilePath="C:\\Users\\Ranjith.P\\Cars\\Archive\\CarsInPut.csv";


    @Scheduled(fixedDelay = 50000)
    public void schedulerMethod() throws  Exception{
        logger.info("Scheduler method started.");
         carList= loadData();
         this.saveListOfCars(carList);
         newStatusCarList=this.getDataFromDB();
         this.updateTheStatus(newStatusCarList);
         this.changeInputFileLocation();
        logger.info("Scheduler method completed.");
    }


    public void changeInputFileLocation() throws IOException {
        logger.info("Changing input newInputFile location from {} to {}", fileInPutPath, newInputFilePath);
        File newInputFile = new File(newInputFilePath);
        File inputFile =new File(fileInPutPath);
        if (!newInputFile.exists()) {
            newInputFile.createNewFile();
        }
        if(!inputFile.exists() || inputFile.length()==0){
            logger.error("Input newInputFile doesn't have any inputs or File not exist in location please check");
            return;
        }
        Files.move(inputFile.toPath(),
                newInputFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        logger.info("Input newInputFile location changed successfully.");
    }

    public void updateTheStatus(List<Car> carList){
        logger.info("Updating status of cars.");
        List<Car> list = carList.stream()
                .peek(car -> car.setStatus("done"))
                .collect(Collectors.toList());
        this.saveListOfCars(list);
    }

    public  void saveListOfCars(List<Car> list) {
        if (list.isEmpty()) {
            logger.error("Cars list is empty please provide the validate inputs");
        } else {
            logger.info("Saving list of cars to database");
            carDto.saveCarDetails(list);
            logger.info("Cars saved successfully.");
        }
    }

    public List<Car> getDataFromDB() throws IOException {
        logger.info("Fetching data from database to output file.");
        List<Car> newStatusCarList=new ArrayList<>();
        logger.info("outputfilepath {}",fileOutPutPath);
        logger.info("inputFilePath {}",fileInPutPath);
        File file = new File(fileOutPutPath);
        if (!file.exists()) {
            file.createNewFile();
        }
         for(Car car:carDto.getNewStatusCarList()) {

             try (FileWriter writer = new FileWriter(file, true)) {
                 writer.write(car.getId() + "," + car.getManufactor() + "," + car.getModel() + "," + car.getYear() + "\n");
             } catch (Exception e) {
                 logger.error("Error writing data  {}", e.getMessage());
             }
             newStatusCarList.add(car);
             logger.info("Data written to output file successfully.");
         }
         return newStatusCarList;
    }


    public static List<Car> loadData() throws IOException {
        logger.info("Loading data from file {}", fileInPutPath);
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
                    logger.error("Invalid year format, skipping row: {}", e.getMessage());
                    continue;
                }
                if(validatingTheInputs(manufacturer,model)){
                    list.add(new Car(manufacturer, model, year,"new"));
                }

            }
        }
        catch (Exception e){
            logger.info("Error loading data from file {}", e.getMessage());
        }
        logger.info("Data loading completed");

        return list;
    }

    public static boolean validatingTheInputs(String manufacturer,String model){
        return !manufacturer.isEmpty() && !model.isEmpty();
    }
}

