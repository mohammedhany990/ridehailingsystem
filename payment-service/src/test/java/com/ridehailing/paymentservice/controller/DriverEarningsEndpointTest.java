package com.ridehailing.paymentservice.controller;

import com.ridehailing.paymentservice.entity.DriverEarningsDaily;
import com.ridehailing.paymentservice.repository.DriverEarningsDailyRepository;
import com.ridehailing.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverEarningsEndpointTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private DriverEarningsDailyRepository earningsDailyRepository;

    @InjectMocks
    private PaymentController controller;

    @Test
    void myDailyEarningsReadsDriverIdFromUserIdClaimNotSubject() {
        UUID driverId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("driver@e2e.test")
                .claim("userId", driverId.toString())
                .build();
        DriverEarningsDaily row = new DriverEarningsDaily();
        when(earningsDailyRepository.findByDriverIdOrderByEarningDateDesc(driverId))
                .thenReturn(List.of(row));

        var response = controller.myDailyEarnings(jwt);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().data().size());
        verify(earningsDailyRepository).findByDriverIdOrderByEarningDateDesc(driverId);
    }
}
