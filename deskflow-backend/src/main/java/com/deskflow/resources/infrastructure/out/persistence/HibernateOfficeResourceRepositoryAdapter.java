package com.deskflow.resources.infrastructure.out.persistence;

import com.deskflow.resources.application.port.out.OfficeResourceRepositoryPort;
import com.deskflow.resources.application.domain.model.OfficeResource;
import com.deskflow.common.application.domain.model.vo.ResourceId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HibernateOfficeResourceRepositoryAdapter implements OfficeResourceRepositoryPort {

    private final SpringDataOfficeResourceRepository jpa;
    private final OfficeResourceMapper mapper;

    public HibernateOfficeResourceRepositoryAdapter(SpringDataOfficeResourceRepository jpa, OfficeResourceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public void save(OfficeResource resource) {
        jpa.findById(resource.getId().getValue()).ifPresentOrElse(
                managed -> mapper.updateEntity(resource, managed),
                () -> jpa.save(mapper.toEntity(resource))
        );
    }

    @Override
    public Optional<OfficeResource> find(ResourceId id) {
        return jpa.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public List<OfficeResource> findActive() {
        return jpa.findByActiveTrue().stream().map(mapper::toDomain).toList();
    }
}
