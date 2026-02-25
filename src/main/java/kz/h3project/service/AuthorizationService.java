package kz.h3project.service;

import kz.h3project.model.user.data.TokenPrincipal;
import kz.h3project.model.user.enums.PermissionDic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("authorization")
public class AuthorizationService {

    public boolean hasAccessToReadUser() {
        return hasRole(PermissionDic.READ_USER.name());
    }

    public boolean hasAccessToWriteUser() {
        return hasRole(PermissionDic.CREATE_USER.name()) || hasRole(PermissionDic.WRITE_USER.name());
    }

    public boolean hasAccessToDeleteUser() {
        return hasRole(PermissionDic.DELETE_USER.name());
    }

    public boolean hasAccessToReadHospital() {
        return hasRole(PermissionDic.READ_HOSPITAL.name());
    }

    public boolean hasAccessToWriteAppointment() {
        return hasRole(PermissionDic.WRITE_APPOINTMENT.name());
    }

    public boolean hasAccessToReadAppointments() {
        return hasRole(PermissionDic.READ_APPOINTMENTS.name());
    }

    public boolean hasAccessToUpdateAppointmentStatus() {
        return hasRole(PermissionDic.UPDATE_APPOINTMENT_STATUS.name());
    }

    public boolean hasAccessToCancelAppointment() {
        return hasRole(PermissionDic.CANCEL_APPOINTMENT.name());
    }

    public boolean hasAccessToWriteHospital() {
        return hasRole(PermissionDic.WRITE_HOSPITAL.name());
    }

    public boolean hasAccessToReadAllUsers() {
        return hasRole(PermissionDic.READ_ALL_USERS.name());
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        TokenPrincipal principal = (TokenPrincipal) auth.getPrincipal();
        return principal.getPermissions().contains(role);
    }
}
