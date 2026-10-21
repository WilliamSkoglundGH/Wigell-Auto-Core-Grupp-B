package com.wac.autocore.mapper;

import com.wac.autocore.dto.mechanic.MechanicSummaryDto;
import com.wac.autocore.dto.mechanic.MechanicCreateDto;
import com.wac.autocore.model.Mechanic;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public final class MechanicMapper {

    private MechanicMapper() {}

    // Gör om en entitet till en visnings-DTO för tabellen
    public static MechanicSummaryDto toSummaryDto(Mechanic mechanic) {
        if (mechanic == null) {
            return null;
        }
        return new MechanicSummaryDto(
                mechanic.getId(),
                mechanic.getName(),
                mechanic.getPhone(),
                mechanic.getSpecialization(),
                mechanic.isAvailable()
        );
    }

    // Gör om en lista av entiteter till en lista av DTO:er (Java 8-kompatibel)
    public static List<MechanicSummaryDto> toSummaryDtoList(List<Mechanic> mechanics) {
        if (mechanics == null) {
            return Collections.emptyList();
        }
        return mechanics.stream()
                .map(MechanicMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    // Gör om från skapande-DTO till en ny entitet (när man sparar i databasen)
  /*  public static Mechanic toEntity(MechanicCreateDto dto) {
        if (dto == null) {
            return null;
        }
        Mechanic mechanic = new Mechanic(dto.);
        mechanic.setName(dto.getName());
        mechanic.setPhone(dto.getPhone());
        mechanic.setSpecialization(dto.getSpecialization());
        return mechanic;
    }

   */
}