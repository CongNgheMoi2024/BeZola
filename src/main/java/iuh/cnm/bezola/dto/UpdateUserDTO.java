package iuh.cnm.bezola.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDTO {
    private String name;
    private LocalDate birthday;
    private boolean sex;
}
