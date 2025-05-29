package com.demo.place.helper;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ErrorResponse {

    private String message;
    private List<?> details;

}