package com.henrique.stickermarker.service;

import com.henrique.stickermarker.dto.*;
import com.henrique.stickermarker.model.Friendship;
import com.henrique.stickermarker.model.FriendshipStatus;
import com.henrique.stickermarker.model.User;
import com.henrique.stickermarker.repository.FriendshipRepository;
import com.henrique.stickermarker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public FriendRequestDTO sendRequestByEmail(Long requesterId, AddFriendByEmailDTO dto) {
        User addressee = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Utilizador não encontrado"));
        return sendRequest(requesterId, addressee.getId());
    }

    public FriendRequestDTO sendRequestByTag(Long requesterId, AddFriendByTagDTO dto) {
        String tag = dto.getUserTag().startsWith("@") ? dto.getUserTag().substring(1) : dto.getUserTag();
        User addressee = userRepository.findByUserTag(tag)
                .orElseThrow(() -> new IllegalArgumentException("Utilizador não encontrado"));
        return sendRequest(requesterId, addressee.getId());
    }

    public FriendRequestDTO sendRequest(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("Não podes adicionar-te a ti mesmo");
        }
        User requester = userService.getById(requesterId);
        User addressee = userService.getById(addresseeId);

        friendshipRepository.findBetweenUsers(requesterId, addresseeId).ifPresent(f -> {
            if (f.getStatus() == FriendshipStatus.ACCEPTED) throw new IllegalArgumentException("Já são amigos");
            if (f.getStatus() == FriendshipStatus.PENDING) throw new IllegalArgumentException("Pedido já enviado");
        });

        Friendship f = new Friendship();
        f.setRequester(requester);
        f.setAddressee(addressee);
        return toRequestDTO(friendshipRepository.save(f));
    }

    @Transactional
    public FriendRequestDTO acceptRequest(Long requestId, Long userId) {
        Friendship f = getRequestForAddressee(requestId, userId);
        f.setStatus(FriendshipStatus.ACCEPTED);
        return toRequestDTO(friendshipRepository.save(f));
    }

    @Transactional
    public void rejectRequest(Long requestId, Long userId) {
        Friendship f = getRequestForAddressee(requestId, userId);
        f.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(f);
    }

    public List<FriendDTO> getFriends(Long userId) {
        return friendshipRepository.findFriends(userId).stream()
                .map(f -> toFriendDTO(f, userId))
                .collect(Collectors.toList());
    }

    public List<FriendRequestDTO> getPendingReceived(Long userId) {
        return friendshipRepository.findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING).stream()
                .map(this::toRequestDTO)
                .collect(Collectors.toList());
    }

    public List<FriendRequestDTO> getPendingSent(Long userId) {
        return friendshipRepository.findByRequesterIdAndStatus(userId, FriendshipStatus.PENDING).stream()
                .map(this::toRequestDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        Friendship f = friendshipRepository.findBetweenUsers(userId, friendId)
                .filter(fr -> fr.getStatus() == FriendshipStatus.ACCEPTED)
                .orElseThrow(() -> new IllegalArgumentException("Amizade não encontrada"));
        friendshipRepository.delete(f);
    }

    public List<UserSearchResultDTO> searchUsers(Long myId, String query) {
        return userRepository.searchByDisplayNameOrTag(query).stream()
                .filter(u -> !u.getId().equals(myId))
                .map(u -> toSearchResult(u, myId))
                .collect(Collectors.toList());
    }

    public boolean areFriends(Long userId1, Long userId2) {
        return friendshipRepository.findBetweenUsers(userId1, userId2)
                .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                .orElse(false);
    }

    private Friendship getRequestForAddressee(Long requestId, Long userId) {
        Friendship f = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        if (!f.getAddressee().getId().equals(userId)) {
            throw new IllegalArgumentException("Sem permissão");
        }
        if (f.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("Pedido já processado");
        }
        return f;
    }

    private FriendDTO toFriendDTO(Friendship f, Long myId) {
        User friend = f.getRequester().getId().equals(myId) ? f.getAddressee() : f.getRequester();
        FriendDTO dto = new FriendDTO();
        dto.setId(friend.getId());
        dto.setDisplayName(friend.getDisplayName());
        dto.setUserTag(friend.getUserTag());
        dto.setCollectionVisibility(friend.getCollectionVisibility());
        dto.setFriendshipId(f.getId());
        return dto;
    }

    private FriendRequestDTO toRequestDTO(Friendship f) {
        FriendRequestDTO dto = new FriendRequestDTO();
        dto.setId(f.getId());
        dto.setRequesterId(f.getRequester().getId());
        dto.setRequesterDisplayName(f.getRequester().getDisplayName());
        dto.setRequesterUserTag(f.getRequester().getUserTag());
        dto.setAddresseeId(f.getAddressee().getId());
        dto.setAddresseeDisplayName(f.getAddressee().getDisplayName());
        dto.setAddresseeUserTag(f.getAddressee().getUserTag());
        dto.setStatus(f.getStatus());
        dto.setCreatedAt(f.getCreatedAt());
        return dto;
    }

    private UserSearchResultDTO toSearchResult(User u, Long myId) {
        UserSearchResultDTO dto = new UserSearchResultDTO();
        dto.setId(u.getId());
        dto.setDisplayName(u.getDisplayName());
        dto.setUserTag(u.getUserTag());
        friendshipRepository.findBetweenUsers(myId, u.getId())
                .ifPresent(f -> dto.setFriendshipStatus(f.getStatus()));
        return dto;
    }
}
