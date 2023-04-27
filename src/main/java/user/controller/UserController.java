package user.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import user.model.Response;
import user.model.SignupRequest;
import user.service.UserService;

@Api(tags = "UserController")
@Tag(name = "UserController", description = "회원가입, 유저정보")
@RestController
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping(value = "/signup")
    @Operation(summary="회원가입", description="회원 가입 API")
    @ApiResponses({
        @ApiResponse(code = 200, message="ok",response = Response.class),
        @ApiResponse(code = 400, message="잘못된 요청",response = Response.class),
        @ApiResponse(code = 500, message="서버 에러",response = Response.class)
    })
    public ResponseEntity signup(@RequestBody SignupRequest signupRequest) throws Exception {
        return userService.signup(signupRequest);
    }

    @GetMapping(value = "/me")
    @Operation(summary="내 정보 보기", description="가입한 회원 정보를 가져오는 API(로그인 후 인증정보 - jwt token 필수)")
    @ApiResponses({
        @ApiResponse(code = 200, message="ok",response = Response.class),
        @ApiResponse(code = 400, message="잘못된 요청",response = Response.class),
        @ApiResponse(code = 500, message="서버 에러",response = Response.class)
    })
    public ResponseEntity getMyInfo() {
        return userService.getMyInfo();
    }
}