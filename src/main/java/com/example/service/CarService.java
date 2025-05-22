package com.example.service;


import com.example.dto.CarDto;
import com.example.entity.Car;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarService {

    @Autowired
    private CarDto carDto;

    List<Car> carList = new ArrayList<>();
    List<Car> newStatusCarList = new ArrayList<>();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CarService.class);


    static String folderPath = "C:\\Users\\Ranjith.P\\Cars\\Input";
    static String outPutFolderPath = "C:\\Users\\Ranjith.P\\Cars\\Output";
    static String newInputFileFolderPath = "C:\\Users\\Ranjith.P\\Cars\\Archive";

    //inputFilePath
    static String fileInPutPath = null;      //"C:\\Users\\Ranjith.P\\Cars\\Input\\CarsInPut.csv";
    //outFilePath
    static String fileOutPutPath = null;      //"C:\\Users\\Ranjith.P\\Cars\\Output\\CarsOutPut.csv";
    //newInputFilePath
    static String newInputFilePath = null;      //"C:\\Users\\Ranjith.P\\Cars\\Archive\\CarsInPut.csv";

    static String processedFileFolder = "C:\\Users\\Ranjith.P\\Cars\\Archive\\Processed";
    static String processedFilePath = null;
    static String unProcessedFileFolder = "C:\\Users\\Ranjith.P\\Cars\\Archive\\UnProcessed";
    static String unProcessedFilePath = null;


    @Scheduled(fixedDelay = 60000)
    public void schedulerMethod() throws Exception {
        logger.info("Scheduler method started.");
        String fileName = loadingFilePath();
        fileInPutPath = folderPath + "\\" + fileName;
        fileOutPutPath = outPutFolderPath + "\\" + fileName;
        newInputFilePath = newInputFileFolderPath + "\\" + fileName;
        processedFilePath = processedFileFolder + "\\" + fileName;
        unProcessedFilePath = unProcessedFileFolder + "\\" + fileName;
        carList = loadData(folderPath, fileName);
        // this.saveListOfCars(carList);

        //  newStatusCarList=this.getDataFromDB();
        // this.updateTheStatus(newStatusCarList);

        this.changeInputFileLocation();
        logger.info("Scheduler method completed.");
    }

    public String loadingFilePath() {
        try {
            Path directoryPath = Paths.get(folderPath);
            if (Files.exists(directoryPath) && Files.isDirectory(directoryPath)) {
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directoryPath)) {
                    for (Path filePath : directoryStream) {
                        return filePath.getFileName().toString();
                    }
                }
            } else {
                logger.error("The specified path does not exist or is not a directory.");
            }
        } catch (IOException e) {
            logger.error("Error while accessing the folder:{} ", e.getMessage());
        }
        return null;
    }


    public void changeInputFileLocation() throws IOException {
        logger.info("Changing input newInputFile location from {} to {}", fileInPutPath, newInputFilePath);
        String dateFormate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String replace = newInputFilePath.replace(".txt", "");
        File newInputFile = new File(replace + "__" + dateFormate + ".txt");
        File inputFile = new File(fileInPutPath);
        if (!newInputFile.exists()) {
            newInputFile.createNewFile();
        }
        if (!inputFile.exists() || inputFile.length() == 0) {
            logger.error("Input newInputFile doesn't have any inputs or File not exist in location please check");
            return;
        }
        Files.move(inputFile.toPath(), newInputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        logger.info("Input newInputFile location changed successfully.");
    }

    public void updateTheStatus(List<Car> carList) {
        logger.info("Updating status of cars.");
        List<Car> list = carList.stream().peek(car -> car.setStatus("done")).collect(Collectors.toList());
        this.saveListOfCars(list);
    }

    public void saveListOfCars(List<Car> list) {
        if (list.isEmpty()) {
            logger.error("Cars list is empty please provide the validate inputs");
            return;
        }
        logger.info("Saving list of cars to database");
        carDto.saveCarDetails(list);
        logger.info("Cars saved successfully.");

    }

    public List<Car> getDataFromDB() throws IOException {
        logger.info("Fetching data from database to output file.");
        List<Car> newStatusCarList = new ArrayList<>();
        File file = new File(fileOutPutPath);
        if (!file.exists()) {
            logger.info("file not exist , creating new file");
            file.createNewFile();
        }
        for (Car car : carDto.getNewStatusCarList()) {

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


    public static List<Car> loadData(String folderPath, String fileName) throws IOException {
        logger.info("Loading data from file {}", fileInPutPath);
        List<Car> list = new ArrayList<>();
        if (fileName == null) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileInPutPath))) {
            String str;
            while ((str = reader.readLine()) != null) {
                String[] arr = str.split(",");

                if (arr.length == 2) {
                    updateTheUnProcessedRecordsWithOutYears(arr[0], arr[1]);
                    continue;
                }

                if (arr.length == 3) {
                    String manufacturer = arr[0].trim();
                    String model = arr[1].trim();
                    String yearStr = arr[2].trim();

                    if (manufacturer.isEmpty() || model.isEmpty() || yearStr.isEmpty()) {
                        updateTheUnProcessedRecords(manufacturer, model, Integer.parseInt(arr[2]));
                        continue;
                    }
                    int year;
                    try {
                        year = Integer.parseInt(arr[2].trim());
                    } catch (NumberFormatException e) {
                        logger.error("Invalid year format {}", e.getMessage());
                        continue;
                    }
                    updateTheProcessedRecords(manufacturer, model, year);
                }
            }
        } catch (Exception e) {
            logger.error("Error loading data from file {}", e.getMessage());
        }
        logger.info("Data loading completed");
        return list;
    }


    public static void updateTheProcessedRecords(String manufacturer, String model, int year) throws IOException {

        try (FileWriter writer = new FileWriter(processedFilePath, true)) {
            writer.write(manufacturer + "," + model + "," + year + "\n");
        } catch (Exception e) {
            logger.error("Error writing data in the processed  file {}", e.getMessage());
        }
    }

    public static void updateTheUnProcessedRecords(String manufacturer, String model, int year) {
        try (FileWriter writer = new FileWriter(unProcessedFilePath, true)) {
            writer.write(manufacturer + "," + model + "," + year + "\n");
        } catch (Exception e) {
            logger.error("Error writing data in the unProcessed file {}", e.getMessage());
        }
    }

    public static void updateTheUnProcessedRecordsWithOutYears(String manufacturer, String model) {
        try (FileWriter writer = new FileWriter(unProcessedFilePath, true)) {
            writer.write(manufacturer + "," + model + "," + "\n");
        } catch (Exception e) {
            logger.error("Error writing data in unProcessed file  {}", e.getMessage());
        }
    }
}

