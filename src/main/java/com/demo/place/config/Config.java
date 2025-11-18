package com.demo.place.config;

import java.time.DayOfWeek;
import java.util.List;

import com.demo.place.annotation.DayOrder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class Config {

	public Config() {
		super();
	}
	
	@DayOrder
    @Produces
	public List<DayOfWeek> dayOrder() {
        return List.of(DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY);
    }
	
}
