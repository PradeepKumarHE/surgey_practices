package com.pradeep.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequestModel {
    private String email;
    private String password;

}
