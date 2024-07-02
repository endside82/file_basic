package com.endside.file.user.model;

import com.endside.file.user.constants.LoginType;
import com.endside.file.user.constants.Os;
import com.endside.file.user.constants.ProviderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginAddInfo {
    private LoginType loginType;
    private ProviderType providerType;
    private Os os;
}
