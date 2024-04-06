package iuh.cnm.bezola.service;

import iuh.cnm.bezola.models.FriendRequest;
import iuh.cnm.bezola.responses.FriendRequestResponse;

import java.util.List;
import java.util.Optional;

public interface FriendRequestService {
    public boolean sendFriendRequest(String fromUserId, String toUserId);

    public boolean acceptFriendRequest(String fromUserId, String toUserId);

    public boolean declineFriendRequest(String fromUserId, String toUserId);

    public Optional<List<FriendRequestResponse>> getFriendRequestByFromUser(String fromUserId);

    public Optional<List<FriendRequestResponse>> getFriendRequestByToUser(String toUserId);
}
