package com.lautadev.lautadev.Service.customer;

import com.lautadev.lautadev.DTO.response.dashboard.UserDetailsResponse;

public interface UserService {
    UserDetailsResponse getLoggedInUserDetails();
}
