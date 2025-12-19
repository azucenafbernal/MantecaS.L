package com.mantecasl.accommodationapp.business.persistance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.mantecasl.accommodationapp.business.entity.Pago;

@DataJpaTest
class PagoDAOTest {

    @Autowired
    private PagoDAO pagoDAO;

    @Test
    void guardar_y_buscar_pago() {
        Pago pago = new Pago();

        // set por reflexión porque no tienes setter
        try {
            var field = Pago.class.getDeclaredField("referencia");
            field.setAccessible(true);
            field.set(pago, UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Pago guardado = pagoDAO.save(pago);

        Optional<Pago> encontrado = pagoDAO.findById(guardado.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getReferencia()).isNotNull();
    }
}
