package com.demo.place.service;

import com.demo.place.records.PlacePatchRecord;
import com.demo.place.records.PlaceRecord;

import java.util.List;

public interface PlaceService {

    public List<PlaceRecord> savePlace(List<PlaceRecord> places);

    public List<PlaceRecord> listAll();

    public PlaceRecord findById(Long id);

    public void deleteById(Long id);

    public PlaceRecord updatePlace(PlaceRecord updatedPlace);

    public PlaceRecord patchPlace(Long id, PlacePatchRecord patch);

}