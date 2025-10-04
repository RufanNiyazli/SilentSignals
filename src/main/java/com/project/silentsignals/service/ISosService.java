package com.project.silentsignals.service;

import com.project.silentsignals.dto.SosRequest;

public interface ISosService {
    public void triggerSos(String userEmail, SosRequest sosRequest);

    public void resolveSos(Long alertId, String respondingUserEmail);
}
