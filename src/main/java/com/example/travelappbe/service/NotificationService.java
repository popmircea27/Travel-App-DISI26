package com.example.travelappbe.service;

import com.example.travelappbe.dto.NotificationDto;
import com.example.travelappbe.entity.Notification;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.repository.NotificationRepository;
import com.example.travelappbe.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void broadcastNotification(String title, String message) {
        List<User> users = userRepository.findAll();
        List<Notification> notifications = users.stream()
                .map(user -> new Notification(user, title, message))
                .collect(Collectors.toList());
        notificationRepository.saveAll(notifications);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(n -> new NotificationDto(n.getId(), n.getTitle(), n.getMessage(), n.isRead(), n.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Access denied");
        }
        notification.setRead(true);
    }
}