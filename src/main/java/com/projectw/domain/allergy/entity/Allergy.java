package com.projectw.domain.allergy.entity;

import com.projectw.common.entity.Timestamped;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Allergy extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;   // 알레르기 이름
    private String description; // 알레르기 설명

    public Allergy(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
