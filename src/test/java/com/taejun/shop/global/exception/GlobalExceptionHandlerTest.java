package com.taejun.shop.global.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExceptionTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void CustomException은_지정된_에러_응답으로_변환된다() throws Exception {
        mockMvc.perform(get("/test/custom"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("PRODUCT_001"))
                .andExpect(jsonPath("$.message").value("상품을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.path").value("/test/custom"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void Valid_검증에_실패하면_필드별_오류를_반환한다() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                        {
                                                "name": ""
                                        }
                                        
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").exists());
    }

    @Test
    void 요청_파라미터_타입이_잘못되면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/test/type")
                .param("id", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"))
                .andExpect(jsonPath("$.path").value("/test/type"));
    }

    @Test
    void 처리하지_않은_예외는_500으로_변환된다() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("COMMON_002"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }

    @RestController
    static class ExceptionTestController {

        @GetMapping("/test/custom")
        void customException() {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        @PostMapping("/test/validation")
        void validation(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/type")
        void type(@RequestParam Long id) {
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("테스트 서버 오류");
        }

    }

    record TestRequest(
            @NotBlank(message = "이름은 필수입니다.")
            String name
    ) {
    }
}
