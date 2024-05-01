package fr.eseo.tauri.service;

import fr.eseo.tauri.exception.GlobalExceptionHandler;
import fr.eseo.tauri.model.Notification;
import fr.eseo.tauri.exception.ResourceNotFoundException;
import fr.eseo.tauri.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final AuthService authService;
	private final NotificationRepository notificationRepository;
	private final UserService userService;

	/**
	 * Get a notification by its id
	 * @param token the token of the user
	 * @param id the id of the notification
	 * @return the notification
	 */
	public Notification getNotificationById(String token, Integer id) {
		if (!Boolean.TRUE.equals(authService.checkAuth(token, "readNotification"))) {
			throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
		}

		return notificationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("notification", id));
	}

	/**
	 * Get all notifications
	 * @param token the token of the user
	 * @return the list of notifications
	 */
	public List<Notification> getAllNotifications(String token) {
		if (!Boolean.TRUE.equals(authService.checkAuth(token, "readNotifications"))) {
			throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
		}

		return notificationRepository.findAll();
	}

	/**
	 * Create a notification
	 * @param token the token of the user
	 * @param notification the notification to create
	 */
	public void createNotification(String token, Notification notification) {
		if (!Boolean.TRUE.equals(authService.checkAuth(token, "addNotification"))) {
			throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
		}

		notification.userFrom(userService.getUserById(token, notification.userFromId()));
		notification.userTo(userService.getUserById(token, notification.userToId()));

		notificationRepository.save(notification);
	}

	/**
	 * Update a notification by its id
	 * @param token the token of the user
	 * @param id the id of the notification
	 * @param updatedNotification the updated notification
	 */
	public void updateNotification(String token, Integer id, Notification updatedNotification) {
		if (!Boolean.TRUE.equals(authService.checkAuth(token, "updateNotification"))) {
			throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
		}

		var notification = getNotificationById(token, id);

		if (updatedNotification.message() != null) notification.message(updatedNotification.message());
		if (updatedNotification.checked() != null) notification.checked(updatedNotification.checked());
		if (updatedNotification.userToId() != null) notification.userTo(userService.getUserById(token, updatedNotification.userToId()));
		if (updatedNotification.userFromId() != null) notification.userFrom(userService.getUserById(token, updatedNotification.userFromId()));

		notificationRepository.save(notification);
	}

	/**
	 * Delete a notification by its id
	 * @param token the token of the user
	 * @param id the id of the notification
	 */
	public void deleteNotificationById(String token, Integer id) {
		if (!Boolean.TRUE.equals(authService.checkAuth(token, "deleteNotification"))) {
			throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
		}

		getNotificationById(token, id);
		notificationRepository.deleteById(id);

		if (notificationRepository.findById(id).isPresent()) {
			throw new DataAccessException("Error : Could not delete notification with id " + id) {};
		}
	}

	/**
	 * Delete all notifications
	 * @param token the token of the user
	 */
	public void deleteAllNotifications(String token) {
		if (!Boolean.TRUE.equals(authService.checkAuth(token, "deleteNotification"))) {
			throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
		}

		notificationRepository.deleteAll();

		if (!notificationRepository.findAll().isEmpty()) {
			throw new DataAccessException("Error : Could not delete all notifications") {};
		}
	}

}