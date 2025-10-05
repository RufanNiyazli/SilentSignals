package com.project.silentsignals.service;

import com.project.silentsignals.entity.User;

public interface INotificationService {
    public boolean sendEmailAlert(User contactUser, User triggeringUser, Double latitude, Double longitude);
}
