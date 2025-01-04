package io.prada.listener.repository;

import io.prada.listener.repository.model.SignalEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SignalRepository extends JpaRepository<SignalEntity, Long> {

    @Query(value = "SELECT s.* FROM signals s ORDER BY s.id DESC LIMIT 10", nativeQuery = true)
    List<SignalEntity> findTopByIdDescLimit10();
}
