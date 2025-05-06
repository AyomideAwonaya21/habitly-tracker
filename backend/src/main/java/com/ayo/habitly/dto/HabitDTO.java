package com.ayo.habitly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HabitDTO {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private Long userId;
    private String userEmail;
    private String userName;
}
