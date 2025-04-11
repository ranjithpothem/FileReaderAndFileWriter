package com.example.dto;


import com.example.entity.Car;
import com.example.repo.CarRepo;
import jakarta.transaction.Transactional;
import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarDto {
    @Autowired
    private CarRepo carRepo;


    @Transactional
    public void saveCarDetails(List<Car> list){
        carRepo.saveAll(list);
    }

    public List<Car> getNewStatusCarList(){
      return  carRepo.getAllRecordsStatusIsNew();
    }
}
