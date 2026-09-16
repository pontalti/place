package com.demo.place.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.demo.place.records.PlacePatchRecord;
import com.demo.place.records.PlaceRecord;

public interface PlaceService {

	public PlaceRecord savePlace(PlaceRecord places);
	
    public List<PlaceRecord> savePlace(List<PlaceRecord> places);

    public List<PlaceRecord> listAll();

    public PlaceRecord findById(Long id);

    public void deleteById(Long id);

    public PlaceRecord updatePlace(PlaceRecord updatedPlace);

    public PlaceRecord patchPlace(PlacePatchRecord patch);
    
    public Page<PlaceRecord> listAll(Pageable pageable);

}