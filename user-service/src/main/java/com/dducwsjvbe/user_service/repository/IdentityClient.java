package com.dducwsjvbe.user_service.repository;

import com.dducwsjvbe.user_service.dto.identity.TokenExchangeParam;
import com.dducwsjvbe.user_service.dto.identity.TokenExchangeResponse;
import com.dducwsjvbe.user_service.dto.identity.UserTokenExchangeParam;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "identity-client", url = "${idp.url}")
@Component
public interface IdentityClient {
    //get token admin
    @PostMapping(
            value = "/realms/dducwsjvbe/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenExchangeResponse exchangeClientToken(@QueryMap() TokenExchangeParam param);

    //login
    @PostMapping(
            value = "realms/dducwsjvbe/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenExchangeResponse exchangeUserToken(@QueryMap() UserTokenExchangeParam param);

}
