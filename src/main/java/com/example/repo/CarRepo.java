package com.example.repo;

import com.example.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepo extends JpaRepository<Car,Long> {

    @Query(value = "Select * from cars where status='new'",nativeQuery = true)
    public List<Car> getAllRecordsStatusIsNew();
}
