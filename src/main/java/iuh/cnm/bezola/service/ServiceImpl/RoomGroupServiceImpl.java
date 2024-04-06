package iuh.cnm.bezola.service.ServiceImpl;

import iuh.cnm.bezola.models.RoomGroup;
import iuh.cnm.bezola.repository.RoomGroupRepository;
import iuh.cnm.bezola.service.RoomGroupService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RoomGroupServiceImpl implements RoomGroupService {
    private final RoomGroupRepository roomGroupService;

    @Override
    public String createRoomGroup(String groupName, String adminId, String[] members) {
        List<String> membersList = List.of(members);

        RoomGroup roomGroup = RoomGroup.builder()
                .groupName(groupName)
                .adminId(adminId)
                .members(membersList)
                .build();

        roomGroupService.save(roomGroup);

        return roomGroup.getId();
    }
}
