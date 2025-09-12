package com.kiszka.prj.components;

import com.kiszka.prj.DTOs.KidDTO;
import com.kiszka.prj.entities.Kid;
import com.kiszka.prj.entities.Parent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KidMapper{
    public KidDTO toDTO(Kid kid){
        List<Integer> parentIds = kid.getParents().stream()
                .map(Parent::getId)
                .toList();
        return new KidDTO(
                kid.getId(),
                kid.getName(),
                kid.getBirthDate(),
                parentIds
        );
    }
}